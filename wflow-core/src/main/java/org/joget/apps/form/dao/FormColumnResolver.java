package org.joget.apps.form.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.form.model.AbstractSubForm;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormColumnCache;
import org.joget.apps.form.model.FormContainer;
import static org.joget.apps.form.dao.FormDataDaoImpl.FORM_PREFIX_TABLE_NAME;
import org.joget.apps.form.service.CustomFormDataTableUtil;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.apache.commons.collections.map.LRUMap;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.json.JSONObject;

/**
 * Resolves the set of custom column names a form data table must be mapped with: the union of the
 * field ids of every form definition mapped to that table, across every app version, plus any
 * custom-table builder definition.
 *
 * <p>Extracted from {@code FormDataDaoImpl}, which is concerned with Hibernate mappings, session
 * factories and CRUD. This class answers one question — "what columns does this table have?" — and
 * owns everything needed to answer it cheaply and correctly: the single-flight guard, the
 * incremental manifest diff, and the merge rules.</p>
 *
 * <p>Two bugs shape almost every decision here:</p>
 * <ul>
 *   <li><b>#2085</b> — a stale column set validates a stale Hibernate mapping, and HQL naming a new
 *       column then fails with {@code UnknownPathException} while the value is silently not saved.
 *       Everything here therefore fails CLOSED: an unknown column set is never treated as
 *       "unchanged", and a definition that could not be read is never recorded as observed.</li>
 *   <li><b>#7709</b> — a full rebuild loads every definition's JSON for the table, which is
 *       expensive enough to dominate a request. Hence the cache, the single-flight, and the
 *       incremental path.</li>
 * </ul>
 *
 * <p>The cached result and the incremental base both live in {@link FormColumnCache}; this class
 * decides what goes into them.</p>
 */
public class FormColumnResolver {

    /**
     * What may legally become a custom column name. Anything else is not a column at all and must
     * be discarded before it can influence anything — see {@link #collectColumn}.
     *
     * <p>Shared with {@code FormDataDaoImpl.getFormRowColumnNames}, which applies the same rule to
     * a submitted row: the two must agree, or a column the store path accepts is one the load
     * mapping does not carry.</p>
     */
    static final Pattern COLUMN_NAME_PATTERN = Pattern.compile("^[0-9a-zA-Z_\\-]+$");

    /** Columns FormRow.hbm.xml already declares; never emitted again as custom columns. */
    static final Set<String> PREDEFINED_COLUMNS = Collections.unmodifiableSet(
        new HashSet<String>(java.util.Arrays.asList(
            FormUtil.PROPERTY_ID, FormUtil.PROPERTY_DATE_CREATED, FormUtil.PROPERTY_DATE_MODIFIED,
            FormUtil.PROPERTY_CREATED_BY, FormUtil.PROPERTY_CREATED_BY_NAME,
            FormUtil.PROPERTY_MODIFIED_BY, FormUtil.PROPERTY_MODIFIED_BY_NAME))
    );

    /**
     * Sentinel meaning "this table's column set could not be resolved" (a circular dependency was
     * cut). Distinct from a legitimately empty set BY IDENTITY, and the distinction is
     * load-bearing: {@code FormDataDaoImpl.hasMappingChanged} treats a known-empty set as "no
     * change", so if a cycle result were indistinguishable from empty it would silently validate a
     * stale mapping — reintroducing #2085 — or, worse, let {@code createSessionFactory} publish a
     * mapping with no custom columns and run DDL on it. Both of those paths abort instead.
     */
    public static final Collection<String> UNRESOLVED_COLUMNS =
            Collections.unmodifiableSet(new HashSet<String>());

    /**
     * Kill switch for the incremental rebuild. {@code false} restores the unconditional full
     * rebuild, which is the behaviour every release before this one had.
     */
    private static final boolean INCREMENTAL_ENABLED =
            !"false".equalsIgnoreCase(System.getProperty("wflow.formColumnIncrementalEnabled", "true"));

    /** Coarsest dateModified granularity across supported databases; see {@link #isRecentlyModified}. */
    private static final long TIMESTAMP_GRANULARITY_MS =
            Long.getLong("wflow.formColumnTimestampGranularityMs", 1000L);

    /**
     * PER-TENANT, EXPIRING backoff on the manifest projection: profile -> when it may be tried again.
     *
     * <p>Some backoff is required. Retrying a statement a database cannot run costs a failed query
     * on every rebuild, and worse: Hibernate converts the {@code JDBCException} through
     * {@code ExceptionConverterImpl}, which marks the surrounding Spring transaction rollback-only,
     * so the caller's request dies at commit with {@code UnexpectedRollbackException} and nothing in
     * the log connects the two.</p>
     *
     * <p><b>Keyed per profile, and that is not cosmetic.</b> This was a single JVM-wide
     * {@code AtomicBoolean}: on a multi-tenant node, one tenant whose database rejected the
     * projection disabled incremental rebuilds for EVERY tenant. Tenants can run different database
     * products and versions, so one failing says nothing about the others.</p>
     *
     * <p><b>And expiring, not latched.</b> A latch cannot tell a permanent grammar error from a
     * transient {@code JDBCConnectionException}, so one blip during a failover used to cost the
     * optimisation for the life of the JVM. Retrying costs a single failed query per tenant per
     * window.</p>
     *
     * <p>Keyed by {@link #manifestProfile()}.</p>
     */
    private static final Map MANIFEST_RETRY_AT = new LRUMap(200);

    private FormDefinitionDao formDefinitionDao;
    private BuilderDefinitionDao builderDefinitionDao;
    private FormService formService;
    private FormColumnCache formColumnCache;

    public FormDefinitionDao getFormDefinitionDao() {
        return formDefinitionDao;
    }

    public void setFormDefinitionDao(FormDefinitionDao formDefinitionDao) {
        this.formDefinitionDao = formDefinitionDao;
    }

    public BuilderDefinitionDao getBuilderDefinitionDao() {
        return builderDefinitionDao;
    }

    public void setBuilderDefinitionDao(BuilderDefinitionDao builderDefinitionDao) {
        this.builderDefinitionDao = builderDefinitionDao;
    }

    public FormService getFormService() {
        return formService;
    }

    public void setFormService(FormService formService) {
        this.formService = formService;
    }

    public FormColumnCache getFormColumnCache() {
        return formColumnCache;
    }

    public void setFormColumnCache(FormColumnCache formColumnCache) {
        this.formColumnCache = formColumnCache;
    }

    /** Overridable so tests can drive the timeout paths without a 20s wait. */
    protected int getLockTimeoutSeconds() {
        return FormBuildGuard.DEFAULT_LOCK_TIMEOUT_SECONDS;
    }

    /** The current manifest, keyed by {@link #manifestKey}. */
    protected static class DefinitionManifest {
        final Map<String, ManifestEntry> entries = new HashMap<String, ManifestEntry>();
    }

    /**
     * What {@link #mergeFormDefinitions} was able to establish about the rows it merged.
     *
     * <p>Three outcomes, and they are deliberately not collapsed into one:</p>
     * <ul>
     *   <li>{@link #confirmed} — parsed, and the row really is the revision the manifest described.
     *       Only these may be recorded as observed in the base.</li>
     *   <li>{@link #seen} — a payload row arrived for this key at all, whatever became of it. A row
     *       that arrived and failed to PARSE is seen but not confirmed, and that distinction is what
     *       keeps a permanently protected or corrupted definition from blocking publication
     *       forever — skipping it is long-standing, deliberate behaviour.</li>
     *   <li>{@link #mismatched} — a row arrived carrying a DIFFERENT revision than the manifest
     *       reported. That is cache incoherence, not bad content, and it is not self-correcting
     *       within this build.</li>
     * </ul>
     */
    protected static class MergeResult {
        final Set<String> confirmed = new HashSet<String>();
        final Set<String> seen = new HashSet<String>();
        final Set<String> mismatched = new HashSet<String>();
    }

    /** One definition as the manifest read it. {@code dateModified} may be null in the database. */
    protected static class ManifestEntry {
        final String formId;
        final Long dateModified;

        ManifestEntry(String formId, Long dateModified) {
            this.formId = formId;
            this.dateModified = dateModified;
        }
    }

    /**
     * The custom column names for a table: served from {@link FormColumnCache} when it holds an
     * entry the database agrees is current, and rebuilt otherwise.
     *
     * @param tableName with or without the {@code app_fd_} prefix
     */
    public Collection<String> getColumnNames(String tableName) {
        Collection<String> columnList;
        Map<String, String> checkDuplicateMap = new HashMap<String, String>();

        // strip table prefix
        if (tableName.startsWith(FORM_PREFIX_TABLE_NAME)) {
            tableName = tableName.substring(FORM_PREFIX_TABLE_NAME.length());
        }

        // getVerified, not get: a warm entry is not proof of freshness. The cross-node
        // CACHE_LAST_CLEAR_ flag that is supposed to invalidate it is read through a ~20s debounced
        // cache, so a warm entry can report an OLD column set as current, that set matches the OLD
        // mapping, and the stale mapping is served — the widest window of #2085.
        columnList = formColumnCache.getVerified(tableName);

        if (columnList == null) {
            return buildWithSingleFlight(tableName, checkDuplicateMap);
        }
        return columnList;
    }

    /**
     * Runs a column-cache build, collapsing concurrent callers for the same table onto one.

     * <p>The build is the expensive one (#7709): it loads every form definition mapped to the
     * table across every app version and parses each one's JSON. Without single-flight, N
     * concurrent requests on a cold cache would each run it. The re-read inside the lock is what
     * makes waiters cheap — whoever loses the race finds the cache populated and does no work.</p>
     *
     * <p>Deadlock freedom rests on the never-wait-while-nested rule in {@link FormBuildGuard}.</p>
     */
    protected Collection<String> buildWithSingleFlight(String tableName, Map<String, String> checkDuplicateMap) {
        final String key = columnBuildKey(tableName);
        final String table = tableName;
        final Map<String, String> duplicates = checkDuplicateMap;
        Supplier<Collection<String>> build =
                () -> FormBuildGuard.runGuarded(key, () -> buildFormDefinitionColumnNames(table, duplicates));

        if (FormBuildGuard.isNested()) {
            if (FormBuildGuard.holds(key)) {
                // CYCLE: this table's build is already on this thread's stack, and plugin code
                // has asked for it again (A references B, B references A). Neither result is
                // cached until its build unwinds, so rebuilding here would recurse until
                // StackOverflowError. Break the edge and return the unresolved sentinel.
                //
                // Callers must not treat that as an empty column set: an empty set means
                // hasMappingChanged() reports "no change", so a validation reached through a cycle
                // would serve a stale mapping — a narrow reappearance of #2085. Returning null
                // instead would fail closed into a rebuild, and that rebuild would hit the same
                // cycle and generate a mapping with NO custom columns, then run DDL against it.
                // Both callers therefore abort on the sentinel and let the request be retried; by
                // then the OUTERMOST build has completed and cached, so no cycle is left.
                LogUtil.warn(FormColumnResolver.class.getName(), "Circular form column dependency detected while building table \""
                        + tableName + "\"; the inner reference resolves to UNRESOLVED to break the cycle.");
                return UNRESOLVED_COLUMNS;
            }
            // Nested build for a DIFFERENT table. Never wait for a lock here: a thread holding one
            // build lock while blocking on another is the only way an ABBA cycle could form.
            return build.get();
        }

        ReentrantLock lock = FormBuildGuard.tryLock(key, getLockTimeoutSeconds());
        if (lock == null) {
            // Whoever holds it is pathologically slow, or we were interrupted. Do the work
            // ourselves rather than block the request further — wasteful but always correct.
            // Contrast mapping regeneration, where a duplicated run would corrupt the mapping file
            // and the caller fails the request instead.
            LogUtil.warn(FormColumnResolver.class.getName(), "Timed out waiting for the form column cache build of table \""
                    + tableName + "\"; rebuilding on this thread instead.");
            return build.get();
        }

        try {
            // Double-check: another thread may have populated the cache while we queued.
            // Safe to accept whatever is here — a caller that arrived because getVerified() found
            // the entry stale got it EVICTED by that check, so nothing stale is left to re-serve.
            Collection<String> columnList = formColumnCache.get(tableName);
            if (columnList != null) {
                return columnList;
            }
            return build.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Profile-scoped identity of a column build, used for striping and cycle detection.
     *
     * <p>The {@code COL:} prefix keeps it distinct from the {@code MAP:} keys used for mapping
     * regeneration. Both share the stripe array and the in-progress set, but they are different
     * operations on the same table and must not be mistaken for a cycle in each other — a
     * mapping regeneration legitimately resolves that table's columns.</p>
     */
    protected String columnBuildKey(String tableName) {
        return "COL:" + HostManager.getCurrentProfile() + ":" + tableName;
    }

    /**
     * The expensive half of {@link #getFormDefinitionColumnNames}: reads every form definition
     * mapped to this table across all app versions, plus any custom-table builder definition,
     * and unions their column names. Callers must hold the per-table build lock.
     *
     * @param tableName table name with the {@code app_fd_} prefix already stripped
     */
    protected Collection<String> buildFormDefinitionColumnNames(String tableName, Map<String, String> checkDuplicateMap) {
        // Taken before the reads below, so an invalidation that lands DURING this build condemns
        // the entry we are about to write rather than being masked by it.
        Date requestTime = new Date();
        boolean debug = LogUtil.isDebugEnabled(FormColumnResolver.class.getName());

        // The manifest is read BEFORE any definition JSON, and the order is load-bearing. If a
        // definition changes between the two reads, the manifest holds the OLDER change key, so the
        // next rebuild sees a difference and re-reads it. The reverse order would record a key for
        // content this build never parsed, and that definition would be skipped forever.
        DefinitionManifest manifest = (INCREMENTAL_ENABLED && isManifestAvailable())
                ? loadDefinitionManifest(tableName) : null;

        if (manifest != null) {
            FormColumnCache.ColumnBase base = formColumnCache.getBase(tableName);
            // isExhausted(): periodically fall back to a full read so a base that recorded a
            // transiently stale payload cannot serve it forever. See ColumnBase#isExhausted.
            if (base != null && !base.isExhausted()) {
                Collection<String> incremental =
                        incrementalBuild(tableName, manifest, base, requestTime, debug);
                if (incremental != null) {
                    return incremental;
                }
                // null means the incremental result could not be vouched for; fall through.
            }
        }
        return fullBuild(tableName, checkDuplicateMap, manifest, requestTime, debug);
    }

    /**
     * The original build: reads EVERY form definition for the table and unions their columns.
     *
     * <p>Still the path taken whenever this node has no usable base — a cold JVM, an evicted base,
     * or a database that will not run the manifest projection. Correct on its own; the incremental
     * path is only ever an optimisation layered on top of it.</p>
     */
    protected Collection<String> fullBuild(String tableName, Map<String, String> checkDuplicateMap,
            DefinitionManifest manifest, Date requestTime, boolean debug) {
        if (debug) {
            LogUtil.debug(FormColumnResolver.class.getName(), "======== Build Form Column Cache for table \""+ tableName +"\" START ========");
        }
        Collection<String> columnList = new HashSet<String>();
        Collection<String> indexesList = new HashSet<String>();

        Collection<FormDefinition> formList = getFormDefinitionDao().loadFormDefinitionByTableName(tableName);
        Collection<BuilderDefinition> builderDefs = loadBuilderDefinitions(tableName);

        boolean haveForms = formList != null && !formList.isEmpty();
        boolean haveBuilderDefs = builderDefs != null && !builderDefs.isEmpty();

        if (haveForms || haveBuilderDefs) {
            MergeResult merged = haveForms
                    ? mergeFormDefinitions(formList, columnList, checkDuplicateMap, debug, manifest)
                    : new MergeResult();
            if (haveBuilderDefs) {
                mergeBuilderDefinitions(tableName, builderDefs, columnList, checkDuplicateMap, indexesList, debug);
            }
            // Do not CACHE a union the manifest says is incomplete. The incremental path already
            // fails closed on this, but the full build it falls back to reads through the SAME
            // cacheable query, so it can reproduce the very staleness that triggered the fallback —
            // and then publish it. That is detecting #2085 and serving it anyway: nothing would
            // correct the entry until the next invalidation, because a warm entry is not rebuilt.
            //
            // The union is still RETURNED, deliberately. Throwing here would turn a few seconds of
            // ordinary post-edit cache incoherence into failed requests for every caller of this
            // table; leaving the entry cold costs one extra build and self-corrects on the very
            // next request, once the payload query catches up.
            if (isComplete(manifest, merged)) {
                publish(tableName, columnList, indexesList, requestTime, debug);
            } else {
                LogUtil.warn(FormColumnResolver.class.getName(), "Column set for table \"" + tableName
                        + "\" disagrees with the definition manifest (" + merged.mismatched.size()
                        + " stale, " + (manifest.entries.size() - merged.seen.size())
                        + " missing); not caching it. The next request rebuilds.");
            }
            // The base is stored either way: it records only CONFIRMED keys, so anything
            // unconfirmed stays unobserved and is re-read by the next build, which then publishes.
            storeBase(tableName, manifest, merged.confirmed, null, columnList, checkDuplicateMap,
                    System.currentTimeMillis(), 0, requestTime);
        }
        if (debug) {
            LogUtil.debug(FormColumnResolver.class.getName(), "======== Build Form Column Cache for table \""+ tableName +"\" END   ========");
        }
        return columnList;
    }

    /**
     * Re-reads ONLY the definitions whose change key differs from the last build, and ADDS their
     * columns to the union already known.
     *
     * <p>This is what #7709 actually needs. The cold cost of a rebuild is dominated by hauling every
     * version's {@code json} out of the database, not by parsing it, so the win comes from not
     * transferring the definitions that did not change — normally all but one of them.</p>
     *
     * <p><b>Additive by design, and that is the safe direction.</b> A definition that VANISHED is
     * ignored, so a removed field lingers until this node next does a full build. A surplus column
     * is inert: the store mapping is built from the submitted row's own keys, not from this union,
     * and Hibernate's SchemaUpdate never drops a column. A MISSING column is the opposite — a 500
     * on any HQL naming it, plus a silently dropped value. That asymmetry is why drifting upwards
     * is acceptable and drifting downwards would not be.</p>
     *
     * <p>Builder definitions are re-read in full every time rather than tracked in the manifest.
     * They are one id per table with a small JSON body, so there is nothing to reclaim, and it keeps
     * the index set exact instead of making it another thing that can drift.</p>
     */
    protected Collection<String> incrementalBuild(String tableName, DefinitionManifest manifest,
            FormColumnCache.ColumnBase base, Date requestTime, boolean debug) {
        long buildStart = requestTime.getTime();
        Set<String> changedKeys = new HashSet<String>();
        Set<String> changedFormIds = new HashSet<String>();
        for (Map.Entry<String, ManifestEntry> entry : manifest.entries.entrySet()) {
            String key = entry.getKey();
            ManifestEntry current = entry.getValue();
            // containsKey, not get: a definition with a null dateModified in the database is still
            // an OBSERVED one, and must not look unobserved forever.
            boolean moved = !base.observed.containsKey(key)
                    || !java.util.Objects.equals(base.observed.get(key), current.dateModified);
            // Observed too soon after its own write for its timestamp to be conclusive, so an edit
            // landing in the same timestamp bucket would be invisible to the comparison above. Costs
            // one payload row until a later edit proves the window closed - see isTimestampStable.
            boolean unproven = base.provisional.contains(key);
            // A stored timestamp is only trustworthy once it is provably OLDER than this build.
            // Within one granularity unit it is not: MySQL stores dateModified as `datetime`, whole
            // seconds, so a second save in the same second leaves the key byte-identical and the
            // edit would never be seen again. Re-reading anything modified that recently closes it
            // portably, without needing a function the dialects spell differently.
            if (moved || unproven || isRecentlyModified(current.dateModified, buildStart)) {
                changedKeys.add(key);
                changedFormIds.add(current.formId);
            }
        }

        if (debug) {
            LogUtil.debug(FormColumnResolver.class.getName(), "======== Incremental Form Column Cache for table \""
                    + tableName + "\": " + changedFormIds.size() + " of " + manifest.entries.size()
                    + " definition(s) changed ========");
        }

        // Copies, not the base's own collections: a reader may still hold the previous union, and a
        // half-merged one must never become visible.
        Collection<String> columnList = new HashSet<String>(base.union);
        Map<String, String> checkDuplicateMap = new HashMap<String, String>(base.duplicateMap);
        Collection<String> indexesList = new HashSet<String>();

        MergeResult merged = new MergeResult();
        if (!changedFormIds.isEmpty()) {
            Collection<FormDefinition> changed =
                    getFormDefinitionDao().loadFormDefinitionByTableNameAndIds(tableName, changedFormIds);
            if (changed != null && !changed.isEmpty()) {
                merged = mergeFormDefinitions(changed, columnList, checkDuplicateMap, debug, manifest);
            }
        }

        Collection<BuilderDefinition> builderDefs = loadBuilderDefinitions(tableName);
        if (builderDefs != null && !builderDefs.isEmpty()) {
            mergeBuilderDefinitions(tableName, builderDefs, columnList, checkDuplicateMap, indexesList, debug);
        }

        // FAIL CLOSED. If any definition we set out to re-read was not confirmed — its payload
        // disagreed with the manifest, or the load simply did not return it — then this union is
        // known to be missing that definition's columns. Publishing it anyway would write a
        // knowingly-incomplete column set into the cache, where nothing would correct it until the
        // next eviction: detecting the staleness and then serving it. Fall back to the full build.
        if (!merged.confirmed.containsAll(changedKeys)) {
            if (debug) {
                LogUtil.debug(FormColumnResolver.class.getName(), "Incremental rebuild of table \""
                        + tableName + "\" could not confirm every changed definition; falling back"
                        + " to a full rebuild.");
            }
            return null;
        }

        publish(tableName, columnList, indexesList, requestTime, debug);
        storeBase(tableName, manifest, merged.confirmed, changedKeys, columnList, checkDuplicateMap,
                base.fullBuiltAt, base.incrementalCount + 1, requestTime);
        return columnList;
    }

    /**
     * Whether a full build's result can be vouched for against the manifest it was read alongside.
     *
     * <p>Deliberately NOT "everything parsed". A definition whose JSON is protected or corrupted has
     * always been skipped with a debug line, and that is a property of the content, not of this
     * build — gating on it would leave such a table uncached and rebuilding on every single request,
     * which is #7709 exactly. What is gated on is DISAGREEMENT: a row that arrived carrying a
     * different revision than the manifest reported, or a row the manifest listed that the payload
     * query did not return at all. Both mean this node read two different points in time, and both
     * are transient, so refusing to cache is enough to correct them.</p>
     *
     * <p>The manifest is read BEFORE the payload, so a definition ADDED between the two reads shows
     * up in the payload but not the manifest. That is not a disagreement — it is extra information,
     * already merged into the union, and the next build's manifest will include it.</p>
     */
    protected boolean isComplete(DefinitionManifest manifest, MergeResult merged) {
        if (manifest == null || manifest.entries.isEmpty()) {
            return true;
        }
        if (!merged.seen.isEmpty() && Collections.disjoint(merged.seen, manifest.entries.keySet())) {
            // NOT staleness. Rows were read and rows were listed, yet not one key is common to both:
            // the two sides are deriving keys differently, which is a defect in this class, not a
            // fact about the database. Refusing to publish on that basis would leave EVERY table
            // uncached and rebuilding on every request — #7709 restored product-wide, from a
            // condition no test that mocks the DAO can see. It happened once already (getId() vs
            // getAppId() on the composite key), and the benchmark, not the suite, is what caught it.
            // So: publish, and make the real problem loud rather than converting it into a
            // catastrophic slowdown.
            LogUtil.error(FormColumnResolver.class.getName(), null, "Form column manifest keys do not"
                    + " correspond to any loaded definition (" + merged.seen.size() + " read, "
                    + manifest.entries.size() + " listed). This is a key-derivation defect;"
                    + " the incremental rebuild cannot verify anything until it is fixed.");
            return true;
        }
        return merged.mismatched.isEmpty() && merged.seen.containsAll(manifest.entries.keySet());
    }

    /** The custom-table builder definitions for this table, across app versions. */
    protected Collection<BuilderDefinition> loadBuilderDefinitions(String tableName) {
        return getBuilderDefinitionDao().find(" and type = ? and id = ?",
                new String[]{CustomFormDataTableUtil.TYPE, FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + tableName},
                null, null, null, null, null);
    }

    /**
     * Parses each definition and merges its columns.
     *
     * @return what could be established about each row — see {@link MergeResult}. A definition that
     *         threw must not be recorded as observed, or its change would be skipped forever.
     */
    protected MergeResult mergeFormDefinitions(Collection<FormDefinition> formList,
            Collection<String> columnList, Map<String, String> checkDuplicateMap, boolean debug,
            DefinitionManifest manifest) {
        MergeResult result = new MergeResult();
        // Parse each distinct JSON once, but REPLAY its columns for every row that carries it:
        // collectColumn resolves case-only duplicates in favour of the spelling seen LAST, so
        // dropping a repeat row changes which spelling wins. Recorded only on SUCCESS, so one
        // transiently failing row cannot suppress the retry on an identical row.
        Map<String, Collection<String>> parsedColumnsByJson = new HashMap<String, Collection<String>>();
        for (FormDefinition formDef : formList) {
            // Recorded BEFORE anything can skip this row: "a payload row arrived for this key" is a
            // separate fact from "its columns could be extracted", and only the first one tells us
            // whether the payload query and the manifest were looking at the same database state.
            // Reading the key does not initialise the AppDefinition proxy — see manifestKeyFor.
            String key = manifestKeyFor(formDef);
            result.seen.add(key);
            if (manifest != null && manifest.entries.containsKey(key) && !matchesManifest(manifest, key, formDef)) {
                result.mismatched.add(key);
            }
            String json = formDef.getJson();
            if (json == null) {
                continue;
            }
            // LAZY, and this is a measured fix rather than a tidy-up. getAppId()/getAppVersion()
            // dereference FormDefinition's lazy many-to-one AppDefinition proxy, so building this
            // label eagerly issued one SELECT and one connection acquisition PER DEFINITION: 1000
            // definitions cost 2000 entity loads and 1003 statements, about 45% of a full rebuild.
            // The label is only ever consumed by a debug line or a duplicate-column warning.
            final FormDefinition current = formDef;
            java.util.function.Supplier<String> source = () -> "Form \"" + current.getId() + "\" ["
                    + current.getAppId() + " v" + current.getAppVersion() + "]";
            Collection<String> tempColumnList = parsedColumnsByJson.get(json);
            if (tempColumnList == null) {
                try {
                    Form form = (Form) getFormService().createElementFromJson(json, false);
                    tempColumnList = new HashSet<String>();
                    findAllElementIds(form, tempColumnList);
                    parsedColumnsByJson.put(json, tempColumnList);
                } catch (Exception e) {
                    LogUtil.debug(FormColumnResolver.class.getName(), "JSON definition of form["+formDef.getAppId()+":"+formDef.getAppVersion()+":"+formDef.getId()+"] is either protected or corrupted.");
                    continue;
                }
            }

            if (debug) {
                LogUtil.debug(FormColumnResolver.class.getName(), "Columns of " + source.get() + " - " + tempColumnList.toString());
            }
            for (String c : tempColumnList) {
                collectColumn(c, columnList, checkDuplicateMap, source);
            }
            // Only record this definition as OBSERVED when the payload we parsed really is the row
            // the manifest described. The manifest is a native projection and always hits the
            // database; the payload comes through a CACHEABLE query (AbstractSpringDao.find calls
            // setCacheable, and Ignite enables the query cache), so the two can disagree. Recording
            // a fresh change key against a stale payload would latch that staleness permanently —
            // #2085 by a new route. A mismatch simply leaves the key unrecorded, so it is retried.
            if (manifest == null || matchesManifest(manifest, key, formDef)) {
                result.confirmed.add(key);
            } else if (debug) {
                LogUtil.debug(FormColumnResolver.class.getName(), "Definition " + key
                        + " did not match the manifest it was loaded for; leaving it unobserved.");
            }
        }
        return result;
    }

    /**
     * Whether a definition was modified too recently for its stored timestamp to be conclusive.
     *
     * <p>Bounded by the coarsest {@code dateModified} granularity across the supported databases:
     * MySQL {@code datetime} is whole seconds, SQL Server {@code datetime} ~3 ms, PostgreSQL and
     * Oracle microseconds. One second therefore covers all four. The cost is re-reading the handful
     * of definitions touched in the last second, which is normally none.</p>
     */
    protected boolean isRecentlyModified(Long dateModified, long buildStart) {
        return dateModified == null || dateModified.longValue() >= (buildStart - getTimestampGranularityMs());
    }

    /** True when the loaded row is the same revision the manifest reported. */
    protected boolean matchesManifest(DefinitionManifest manifest, String key, FormDefinition formDef) {
        ManifestEntry entry = manifest.entries.get(key);
        if (entry == null) {
            return false;
        }
        Date actual = formDef.getDateModified();
        return (entry.dateModified == null) ? (actual == null)
                : (actual != null && entry.dateModified.longValue() == actual.getTime());
    }

    /** Merges the custom-table builder columns and collects its declared indexes. */
    protected void mergeBuilderDefinitions(String tableName, Collection<BuilderDefinition> builderDefs,
            Collection<String> columnList, Map<String, String> checkDuplicateMap,
            Collection<String> indexesList, boolean debug) {
        for (BuilderDefinition def : builderDefs) {
            try {
                JSONObject defObj = new JSONObject(def.getJson());
                if (defObj.has("columns")) {
                    final BuilderDefinition current = def;
                    java.util.function.Supplier<String> source = () -> "custom table \"" + tableName
                            + "\" [" + current.getAppId() + " v" + current.getAppVersion() + "]";
                    JSONObject columnsObj = defObj.getJSONObject("columns");
                    Iterator keys = columnsObj.keys();
                    while (keys.hasNext()) {
                        collectColumn((String) keys.next(), columnList, checkDuplicateMap, source);
                    }
                }

                if (defObj.has("indexes")) {
                    JSONObject indexesObj = defObj.getJSONObject("indexes");
                    Iterator ikeys = indexesObj.keys();
                    while (ikeys.hasNext()) {
                        String c = (String) ikeys.next();
                        indexesList.add(c);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(FormColumnResolver.class.getName(), e, "fail to retrieve columns for custom table \"" + tableName + "\" [" + def.getAppId() + " v" + def.getAppVersion() + "]");
            }
        }
    }

    /** Strips the predefined columns and writes both cache entries. */
    protected void publish(String tableName, Collection<String> columnList, Collection<String> indexesList,
            Date requestTime, boolean debug) {
        //remove predefined column
        columnList.removeAll(PREDEFINED_COLUMNS);

        if (debug) {
            LogUtil.debug(FormColumnResolver.class.getName(), "All Columns - " + columnList.toString());
        }
        formColumnCache.put(tableName, columnList, requestTime);
        formColumnCache.putIndexes(tableName, indexesList, requestTime);
    }

    /**
     * Reads the cheap per-definition manifest, or null if it cannot be used.
     *
     * <p>Null is always safe: every caller falls back to the full build, which needs no manifest.</p>
     */
    protected DefinitionManifest loadDefinitionManifest(String tableName) {
        try {
            Collection<Object[]> rows = getFormDefinitionDao().getFormDefinitionManifestByTableName(tableName);
            if (rows == null) {
                return null;
            }
            DefinitionManifest manifest = new DefinitionManifest();
            for (Object[] row : rows) {
                // NEVER cast a native-query value to a concrete type. app_form.appVersion is bigint
                // on MySQL/PostgreSQL (-> Long) but NUMBER(19,0) on Oracle and numeric(19,0) on SQL
                // Server (-> BigDecimal); dateModified is Timestamp on most drivers but
                // OffsetDateTime for PostgreSQL's "timestamp with time zone". A ClassCastException
                // here is caught below and would silently disable this feature for the whole JVM,
                // on exactly the databases hardest to test.
                String appId = (String) row[0];
                Long appVersion = asLong(row[1]);
                String formId = (String) row[2];
                Date dateModified = asDate(row[3]);

                manifest.entries.put(manifestKey(appId, appVersion, formId),
                        new ManifestEntry(formId, dateModified == null ? null : dateModified.getTime()));
            }
            return manifest;
        } catch (Exception e) {
            backOffManifest(e);
            return null;
        }
    }

    /** How long a tenant's manifest failure suppresses further attempts. */
    protected long getManifestRetryIntervalMs() {
        return Long.getLong("wflow.formColumnManifestRetryIntervalMs", 900000L);
    }

    /**
     * The tenant whose database the manifest query runs against. See {@link #MANIFEST_RETRY_AT}.
     *
     * <p>{@link HostManager}, not {@code DynamicDataSourceManager.getCurrentProfile()}, for the same
     * reason as {@link #columnBuildKey}: the latter falls back to reading the datasource properties
     * through a static cache that only Spring populates, so it both dereferences Spring-initialised
     * state and memoises a resolved profile back onto a thread that had none.</p>
     *
     * <p>The trade: when no profile is set on the thread, every such request shares the {@code null}
     * key. That can only make one tenant's failure suppress another's manifest for one window — a
     * lost optimisation, never a wrong column set — and it is the same trade the build locks already
     * make. Real request paths always have a profile.</p>
     */
    protected String manifestProfile() {
        return String.valueOf(HostManager.getCurrentProfile());
    }

    /** Whether the manifest projection may be attempted for the current tenant right now. */
    protected boolean isManifestAvailable() {
        synchronized (MANIFEST_RETRY_AT) {
            Long retryAt = (Long) MANIFEST_RETRY_AT.get(manifestProfile());
            return retryAt == null || System.currentTimeMillis() >= retryAt.longValue();
        }
    }

    /** Suppresses the manifest for this tenant only, for one retry window. */
    protected void backOffManifest(Exception e) {
        long now = System.currentTimeMillis();
        String profile = manifestProfile();
        boolean wasAvailable;
        synchronized (MANIFEST_RETRY_AT) {
            Long previous = (Long) MANIFEST_RETRY_AT.get(profile);
            wasAvailable = previous == null || now >= previous.longValue();
            MANIFEST_RETRY_AT.put(profile, Long.valueOf(now + getManifestRetryIntervalMs()));
        }
        // Once per tenant per window, not once per JVM: a second tenant failing for its own reason
        // is a separate fact, and a permanent one-shot log hides a failure that keeps recurring.
        if (wasAvailable) {
            LogUtil.error(FormColumnResolver.class.getName(), e,
                    "Form column manifest query failed for profile \"" + profile + "\"; incremental"
                    + " column rebuilds are suspended for that tenant for "
                    + getManifestRetryIntervalMs() + "ms and full rebuilds will be used.");
        }
    }

    /** Coerces any JDBC numeric (Long, BigDecimal, BigInteger, Integer) to a Long. */
    protected static Long asLong(Object value) {
        return (value instanceof Number) ? Long.valueOf(((Number) value).longValue()) : null;
    }

    /**
     * Coerces whatever temporal a JDBC driver returns into {@code java.util.Date}.
     *
     * <p>{@code java.sql.Timestamp} already is one, but PostgreSQL declares
     * {@code datemodified timestamp with time zone}, which Hibernate 6 resolves to
     * {@code OffsetDateTime}. Returning null for that would make every definition look changed on
     * every build — the manifest query PLUS a full payload load each time, i.e. this feature turning
     * into a pessimisation on one of the four supported databases.</p>
     */
    protected static Date asDate(Object value) {
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof java.time.Instant) {
            return Date.from((java.time.Instant) value);
        }
        if (value instanceof java.time.OffsetDateTime) {
            return Date.from(((java.time.OffsetDateTime) value).toInstant());
        }
        if (value instanceof java.time.ZonedDateTime) {
            return Date.from(((java.time.ZonedDateTime) value).toInstant());
        }
        if (value instanceof java.time.LocalDateTime) {
            return Date.from(((java.time.LocalDateTime) value)
                    .atZone(java.time.ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * The manifest key for a loaded definition, WITHOUT initialising its {@code AppDefinition}.
     *
     * <p>This is the other half of the lazy {@code Supplier} in {@link #mergeFormDefinitions}, and
     * without it that optimisation is pointless: {@code getAppId()}/{@code getAppVersion()} both
     * dereference the lazy many-to-one, so calling them here — once per definition, unconditionally
     * — reinstates exactly the N+1 the Supplier defers. An earlier version did precisely that, 27
     * lines below the Supplier, and cancelled it.</p>
     *
     * <p>{@code AppDefinition}'s id is a {@code mapped="true"} composite, so the proxy's identifier
     * is a key object carrying just the key properties — reading them off it is a plain field access
     * that triggers no SELECT. Falls back to the getters when the association is not a proxy
     * (already initialised, or a test double), which is correct, just not free.</p>
     *
     * <p><b>The identifier is an {@link org.joget.apps.app.model.AppDefinitionKey}, not an
     * {@code AppDefinition}</b> — {@code AppDefinition.hbm.xml} declares
     * {@code composite-id class="AppDefinitionKey"}, and the two are SIBLINGS under
     * {@link org.joget.apps.app.model.AbstractVersionedObject} rather than one extending the other.
     * An earlier version tested {@code instanceof AppDefinition}, which is therefore never true in
     * production: every definition fell through to {@code getAppId()} and initialised the proxy
     * anyway, so the N+1 this method exists to avoid was still being paid in full.</p>
     *
     * <p><b>And it must be read with {@code getAppId()}, not {@code getId()}.</b> The mapping is
     * {@code <key-property column="appId" name="appId"/>}, so Hibernate populates the key object's
     * {@code appId} and {@code version} and leaves its {@code id} field NULL — those are separate
     * fields on {@code AbstractVersionedObject}, and only {@code AppDefinition} itself overrides
     * {@code setAppId} to keep the two in step. Reading {@code getId()} here produced keys like
     * {@code null:1:formId} that matched nothing in the manifest, which the benchmark caught as
     * "0 stale, 0 missing" builds that were nevertheless judged incomplete — and, combined with
     * {@link #isComplete}, meant the column cache was never populated at all.</p>
     */
    protected static String manifestKeyFor(FormDefinition formDef) {
        Object appDef = formDef.getAppDefinition();
        if (appDef instanceof org.hibernate.proxy.HibernateProxy) {
            Object id = ((org.hibernate.proxy.HibernateProxy) appDef)
                    .getHibernateLazyInitializer().getIdentifier();
            if (id instanceof org.joget.apps.app.model.AbstractVersionedObject) {
                org.joget.apps.app.model.AbstractVersionedObject key =
                        (org.joget.apps.app.model.AbstractVersionedObject) id;
                // getAppId(): on the key object that is the populated field; on an AppDefinition it
                // is overridden to return getId(). Correct for both.
                return manifestKey(key.getAppId(), key.getVersion(), formDef.getId());
            }
        }
        return manifestKey(formDef.getAppId(), formDef.getAppVersion(), formDef.getId());
    }

    /** Profile-scoped identity of one form definition, stable across nodes. */
    protected static String manifestKey(String appId, Long appVersion, String formId) {
        return appId + ":" + appVersion + ":" + formId;
    }

    /**
     * Records what this build actually observed, for the next one to diff against.
     *
     * <p>A definition that FAILED to parse is deliberately left out, so the next build sees it as
     * new and retries. Recording it would mean a transient plugin failure permanently froze that
     * definition's columns out of the union — fail-open, which is the #2085 mistake.</p>
     *
     * <p>Also marks which observations are still PROVISIONAL — see
     * {@link FormColumnCache.ColumnBase#provisional} and {@link #isTimestampStable}.</p>
     *
     * @param processedKeys keys whose definition parsed successfully in this build
     * @param attemptedKeys keys this build tried to re-read; null means "all of them" (a full build)
     * @param requestTime   when this build started; {@link FormColumnCache#putBase} drops a base
     *                      that a {@link FormColumnCache#reset} has since superseded
     */
    protected void storeBase(String tableName, DefinitionManifest manifest,
            Set<String> processedKeys, Set<String> attemptedKeys,
            Collection<String> union, Map<String, String> duplicateMap,
            long fullBuiltAt, int incrementalCount, Date requestTime) {
        if (manifest == null) {
            return;
        }
        long buildStart = (requestTime != null) ? requestTime.getTime() : System.currentTimeMillis();
        Map<String, Long> observed = new HashMap<String, Long>();
        Set<String> provisional = new HashSet<String>();
        for (Map.Entry<String, ManifestEntry> entry : manifest.entries.entrySet()) {
            boolean attempted = attemptedKeys == null || attemptedKeys.contains(entry.getKey());
            if (!attempted || processedKeys.contains(entry.getKey())) {
                Long dateModified = entry.getValue().dateModified;
                observed.put(entry.getKey(), dateModified);
                if (!isTimestampStable(dateModified, buildStart)) {
                    provisional.add(entry.getKey());
                }
            }
        }
        formColumnCache.putBase(tableName, new FormColumnCache.ColumnBase(observed, provisional,
                new HashSet<String>(union), new HashMap<String, String>(duplicateMap),
                fullBuiltAt, incrementalCount), requestTime);
    }

    /**
     * Whether a stored {@code dateModified} can no longer gain a second write without changing.
     *
     * <p>The stored value has a granularity of {@link #getTimestampGranularityMs()} — MySQL's
     * {@code datetime} is whole seconds — so any write landing before {@code dateModified + G} leaves
     * it byte-identical, and therefore invisible to change-key comparison. An observation made INSIDE
     * that window is inconclusive; one made after it has closed is conclusive, because a later write
     * would necessarily have moved the value.</p>
     *
     * <p><b>Sticky, and that is what makes it cheap.</b> Recording the verdict means the extra re-read
     * happens ONCE per ambiguous observation rather than on every rebuild. The steady-state property
     * the incremental path is built on — an unchanged table reads no definition payloads at all —
     * survives intact.</p>
     *
     * <p>The flip from provisional to stable can only happen in a build that RE-READ the key: a
     * provisional key is always treated as changed, so the observation recorded alongside the flip is
     * fresh. There is no window in which a key becomes stable while a missed edit is still hidden.</p>
     *
     * <p><b>Skew, stated plainly.</b> {@code dateModified} is stamped by whichever node performed the
     * save, and {@code buildStart} is this node's clock, so this is a cross-node comparison. If the
     * reading node's clock runs more than one granularity unit AHEAD of the writing node's, a
     * same-bucket observation can be marked stable prematurely and the hole reopens. That is the
     * SAME exposure {@link #isRecentlyModified} already has, so it adds no new class of risk — and
     * unlike that rule, this one converts the common case from "lost for up to 15 minutes" into
     * "re-read once". A skew-proof version would have to compare only database-sourced timestamps,
     * which costs a payload row on every rebuild forever; that trade was measured and rejected.</p>
     */
    protected boolean isTimestampStable(Long dateModified, long buildStart) {
        if (dateModified == null) {
            // Unknown: isRecentlyModified already forces a null dateModified to be re-read, and this
            // keeps the two rules agreeing rather than depending on that.
            return false;
        }
        return (buildStart - dateModified.longValue()) >= getTimestampGranularityMs();
    }

    /** Overridable so tests can drive the granularity boundary without sleeping a whole second. */
    protected long getTimestampGranularityMs() {
        return TIMESTAMP_GRANULARITY_MS;
    }

    /**
     * Accumulates one candidate column, resolving a case-only duplicate in favour of the spelling
     * seen most recently.
     *
     * <p><b>Validity is checked FIRST, and the ordering is deliberate.</b> The original code ran
     * duplicate resolution before the pattern check, so a name that could never become a column
     * still claimed the lowercase slot and evicted whatever valid column held it — then failed the
     * pattern itself, leaving NEITHER. The mapping loses a column that holds live data, which is
     * the #2085 failure arriving by a different route.</p>
     *
     * <p><b>Narrow, and worth stating precisely rather than overselling.</b> Pure ASCII cannot hit
     * it: {@code toLowerCase()} maps {@code A-Z} to {@code a-z}, both inside the allowed set, so an
     * invalid name and a valid one never share a key. It needs a character that folds INTO the
     * allowed set from outside it — U+212A KELVIN SIGN lowercases to a plain {@code "k"}, and the
     * default-locale {@code toLowerCase()} does the same on a Turkish-locale JVM, where {@code "I"}
     * folds to a dotless {@code ı}. So this is hardening plus a cheaper hot loop (unusable names
     * now skip the lowercase and the map write entirely), not a fix for a common failure.</p>
     *
     * <p>The null guard is the one with real teeth. The old {@code c.isEmpty()} threw NPE on a null
     * ID inside the caller's {@code catch (Exception e)} that logs "protected or corrupted" at
     * DEBUG — so one null silently discarded EVERY column of that form, and the mapping was then
     * generated without them. Core elements cannot produce a null ({@code getPropertyString}
     * returns {@code ""}), but {@code getDynamicFieldNames()} is a public extension point and this
     * build already runs arbitrary plugin code (see {@link #COLUMN_BUILD_IN_PROGRESS}).</p>
     *
     * @param source lazily-built origin of the column, evaluated ONLY when a duplicate is warned about;
     *               resolving it dereferences a lazy Hibernate association (see mergeFormDefinitions)
     */
    private void collectColumn(String column, Collection<String> columnList, Map<String, String> checkDuplicateMap,
            java.util.function.Supplier<String> source) {
        if (column == null || column.isEmpty() || !COLUMN_NAME_PATTERN.matcher(column).matches()) {
            return;
        }
        String exist = checkDuplicateMap.put(column.toLowerCase(), column);
        if (exist != null && !exist.equals(column)) {
            LogUtil.warn(FormColumnResolver.class.getName(), "Detected duplicated column in " + source.get() + ": \""
                    + exist + "\" and \"" + column + "\". Removed \"" + exist + "\" and replaced with \"" + column + "\".");
            columnList.remove(exist);
        }
        columnList.add(column);
    }

    /**
     * Recurse into child elements to find add all element property ID to columnList.
     * @param element
     * @param columnList
     */
    protected void findAllElementIds(org.joget.apps.form.model.Element element, Collection<String> columnList) {
        Collection<String> fieldNames = element.getDynamicFieldNames();
        if (fieldNames != null && !fieldNames.isEmpty()) {
            columnList.addAll(fieldNames);
        }
        if (!(element instanceof FormContainer) && element.getProperties() != null) {
            String id = element.getPropertyString(FormUtil.PROPERTY_ID);
            if (id != null && !id.isEmpty()) {
                columnList.add(id);
            }
        }
        if (!(element instanceof AbstractSubForm)) { // do not recurse into subforms
            Collection<org.joget.apps.form.model.Element> children = element.getChildren();
            if (children != null) {
                for (org.joget.apps.form.model.Element child : children) {
                    findAllElementIds(child, columnList);
                }
            }
        }
    }
}
