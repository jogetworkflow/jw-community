package org.joget.apps.form.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.cache.Cache;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormColumnCache;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Regression suite for the cross-node stale Hibernate mapping bug (#2085) and for the older
 * performance issue whose fix introduced the defect (#7709).
 *
 * <h3>Two families, opposite expectations</h3>
 *
 * <p><b>{@code inv*} — performance invariants (#7709).</b> #7709 was <i>"Long processing time to
 * create hibernate mapping file when there are many forms in many app versions pointing to the
 * same table name"</i>. These must be GREEN before and after the #2085 fix; a failure means the
 * fix reintroduced #7709.</p>
 * <ul>
 *   <li>INV-1 (read cost) — {@link FormDefinitionDao#loadFormDefinitionByTableName} is unbounded
 *       across every app version and materialises each form's JSON, so it must run only when the
 *       column cache is genuinely cold, and then only once.</li>
 *   <li>INV-2 (creation cost) — the mapping is regenerated only when the column <i>set</i>
 *       changed, never merely because a form was touched or an app version was created.</li>
 *   <li>INV-3 (herd) — concurrent cold-cache callers collapse to one rebuild.</li>
 * </ul>
 *
 * <p><b>{@code bug2085*} — correctness guards.</b> Expected RED against the unfixed code.</p>
 *
 * <p>Tests drive the real {@code findSessionFactory}; only {@code createSessionFactory} is
 * stubbed, as a counter plus a sentinel, so "was the mapping regenerated?" is observable —
 * which is what INV-2 and the #2085 cases both need to assert.</p>
 *
 * <h3>Scope limits — read before trusting a green run</h3>
 * <ul>
 *   <li><b>Window 1 of #2085 is not covered here.</b> The widest window is a column cache that is
 *       stale yet still reports itself valid, because the {@code CACHE_LAST_CLEAR} flag is read
 *       through {@code SetupManagerCache}'s 20s debounce. There is no seam on which to state
 *       "the database moved on while the cache claims freshness" until the fresh check lands.
 *       Until then only the 2-node harness in {@code skills/_shared/tests/issue2085/} covers it.</li>
 *   <li><b>Counting stubbed {@code createSessionFactory} calls is a proxy</b> for "the expensive
 *       mapping generation happened". It is the best available seam at unit level; the real cost
 *       is measured by the load test, not here.</li>
 * </ul>
 */
public class FormDataDaoMappingStalenessTest {

    private static final String TABLE = "app_fd_t2085";
    /** What the column cache is keyed by — the prefix is stripped before lookup. */
    private static final String UNPREFIXED = "t2085";

    private Cache sessionFactoryCache;
    private Cache persistentClassCache;
    private FormColumnCache formColumnCache;
    private FormDefinitionDao formDefinitionDao;
    private BuilderDefinitionDao builderDefinitionDao;
    private FormService formService;

    private SessionFactory staleSessionFactory;
    private SessionFactory rebuiltSessionFactory;

    private AtomicInteger rebuildCount;
    /** How many times the stale factory's close() was actually invoked - see closeCallCount tests. */
    private AtomicInteger closeCallCount;
    private RecordingDao dao;

    /** Backing store for the mocked {@link FormColumnCache}'s incremental base. */
    private Map<String, FormColumnCache.ColumnBase> bases;

    private RecordingResolver resolver;

    /**
     * Stateful stand-ins for the two node-local caches. A rebuild replaces both, exactly as
     * production does — without this, every repeated call would see the stale mapping again and
     * "rebuilt exactly once" could never be distinguished from "rebuilds on every request".
     */
    private AtomicReference<SessionFactory> sfRef;
    private AtomicReference<PersistentClass> pcRef;

    /**
     * The real (unmocked) {@code formSessionFactorySourceCache} map, captured via reflection - see
     * that field's javadoc on {@link FormDataDaoImpl}. Unlike {@code sessionFactoryCache}/
     * {@code persistentClassCache} above, this is NOT a Mockito mock: findSessionFactory's new
     * #2085 decoupling check reads it directly as a plain Map, so it must be seeded with what
     * {@code sfRef}'s current factory was actually built from (mirroring {@code pcRef} at the same
     * moments), identity-paired with the SAME sf instance, or that check fails closed and forces a
     * spurious rebuild.
     */
    private Map<String, FormDataDaoImpl.SessionFactoryProvenance> sourceCache;

    /** Columns the mapping will carry after a rebuild — i.e. what the column cache now says. */
    private String[] columnsAfterRebuild;

    private class RecordingDao extends FormDataDaoImpl {
        /**
         * Stands in for the authoritative column set — what the shared database says right now,
         * as opposed to what this node has cached. Null means "use the real implementation".
         */
        Collection<String> authoritativeColumns = null;

        /**
         * When set, stands in for the element IDs a parsed form contributes — the only seam through
         * which a test can hand the build a null or otherwise unusable ID. Real element trees are
         * built by plugins, so producing one that yields a null ID would mean wiring FormService,
         * the plugin manager and a Spring context just to express one bad string.
         */
        Collection<String> stubElementIds = null;

        /** Per-parsed-element ids, so different JSON can contribute different columns. */
        Map<Object, Collection<String>> stubIdsByElement = null;

        @Override
        protected String getSessionFactoryCacheKey(String entityName, String tableName, FormRowSet rowSet) {
            return "sf_" + entityName;
        }

        @Override
        protected String getPersistentClassCacheKey(String entityName) {
            return "pc_" + entityName;
        }

        @Override
        public Collection<String> getFormDefinitionColumnNames(String tableName) {
            if (authoritativeColumns != null) {
                return authoritativeColumns;
            }
            return super.getFormDefinitionColumnNames(tableName);
        }

        @Override
        protected SessionFactory createSessionFactory(String entityName, String tableName, FormRowSet rowSet, int actionType) {
            rebuildCount.incrementAndGet();
            // Model what production does after a rebuild: both caches now hold the NEW mapping.
            sfRef.set(rebuiltSessionFactory);
            if (columnsAfterRebuild != null) {
                PersistentClass rebuiltPc = mapping(columnsAfterRebuild);
                pcRef.set(rebuiltPc);
                // Keep sourceCache in the SAME generation as pcRef/sfRef, identity-paired with the
                // SAME sf instance just published - otherwise the #2085 decoupling check would
                // (correctly, per its fail-closed and identity-checked contract) treat every call
                // AFTER this rebuild as unverifiable and force another one.
                if (sourceCache != null) {
                    sourceCache.put("sf_" + TABLE, new FormDataDaoImpl.SessionFactoryProvenance(rebuiltSessionFactory, rebuiltPc));
                }
            }
            return rebuiltSessionFactory;
        }
    }

    /**
     * The column build moved to {@link FormColumnResolver}, so the element-id seam lives here now.
     * It still reads the stubs off {@code RecordingDao} — those are what the tests set.
     */
    private class RecordingResolver extends FormColumnResolver {
        /** Lets a test collapse the manifest backoff window instead of waiting 15 minutes. */
        Long manifestRetryIntervalMs = null;

        /**
         * Lets a test drive the timestamp-granularity boundary in milliseconds instead of sleeping a
         * whole second. Production reads it from a static final system property.
         */
        Long granularityMs = null;

        @Override
        protected long getTimestampGranularityMs() {
            return granularityMs != null ? granularityMs : super.getTimestampGranularityMs();
        }

        @Override
        protected long getManifestRetryIntervalMs() {
            return manifestRetryIntervalMs != null ? manifestRetryIntervalMs : super.getManifestRetryIntervalMs();
        }

        @Override
        protected void findAllElementIds(org.joget.apps.form.model.Element element, Collection<String> columnList) {
            if (dao.stubIdsByElement != null && dao.stubIdsByElement.containsKey(element)) {
                columnList.addAll(dao.stubIdsByElement.get(element));
                return;
            }
            if (dao.stubElementIds != null) {
                columnList.addAll(dao.stubElementIds);
                return;
            }
            super.findAllElementIds(element, columnList);
        }
    }

    @Before
    public void setUp() throws Exception {
        clearCacheInProgress();

        sessionFactoryCache = mock(Cache.class);
        persistentClassCache = mock(Cache.class);
        formColumnCache = mock(FormColumnCache.class);
        formDefinitionDao = mock(FormDefinitionDao.class);
        builderDefinitionDao = mock(BuilderDefinitionDao.class);
        formService = mock(FormService.class);
        staleSessionFactory = mock(SessionFactory.class);
        rebuiltSessionFactory = mock(SessionFactory.class);
        rebuildCount = new AtomicInteger();
        closeCallCount = new AtomicInteger();

        // The incremental base lives in FormColumnCache, which is mocked here. Back it with a real
        // per-test map: an unstubbed getBase() would return null forever, so every rebuild would
        // silently take the full path and no incremental test could fail.
        bases = new HashMap<String, FormColumnCache.ColumnBase>();
        when(formColumnCache.getBase(anyString())).thenAnswer(inv -> bases.get(inv.getArgument(0)));
        doAnswer(inv -> {
            bases.put(inv.getArgument(0), (FormColumnCache.ColumnBase) inv.getArgument(1));
            return null;
        }).when(formColumnCache).putBase(anyString(), any(), any());
        doAnswer(inv -> {
            bases.remove(inv.getArgument(0));
            return null;
        }).when(formColumnCache).reset(anyString());

        dao = new RecordingDao();
        resolver = new RecordingResolver();
        dao.setColumnResolver(resolver);
        dao.setFormColumnCache(formColumnCache);
        dao.setFormDefinitionDao(formDefinitionDao);
        dao.setBuilderDefinitionDao(builderDefinitionDao);
        dao.setFormService(formService);
        inject(dao, "formSessionFactoryCache", sessionFactoryCache);
        inject(dao, "formPersistentClassCache", persistentClassCache);
        // clearJoinCache() iterates this on every detected change and would NPE on a bare mock.
        Cache joinCache = mock(Cache.class);
        when(joinCache.iterator()).thenReturn(new ArrayList<Cache.Entry>().iterator());
        inject(dao, "joinFormSessionFactoryCache", joinCache);

        // A node that has already served this datalist: the mapping it cached was built from the
        // pre-change column set. NOTE: mapping() must be built BEFORE when(...) is opened —
        // building it inside when(...).thenReturn(...) starts nested stubbing and throws
        // UnfinishedStubbingException, which errors out every test in the class.
        PersistentClass cachedMapping = mapping("title", "status");
        sfRef = new AtomicReference<SessionFactory>(staleSessionFactory);
        pcRef = new AtomicReference<PersistentClass>(cachedMapping);
        columnsAfterRebuild = null;
        when(sessionFactoryCache.get("sf_" + TABLE)).thenAnswer(inv -> sfRef.get());
        when(persistentClassCache.get("pc_" + TABLE)).thenAnswer(inv -> pcRef.get());

        // staleSessionFactory really was built from cachedMapping - state the #2085 decoupling
        // check needs directly, since it reads formSessionFactorySourceCache as a plain map, not
        // through a mock. Identity-paired with staleSessionFactory itself (the check verifies
        // provenance.sf == sf). Kept in sync with sfRef/pcRef by RecordingDao.createSessionFactory.
        sourceCache = sourceCache(dao);
        sourceCache.put("sf_" + TABLE, new FormDataDaoImpl.SessionFactoryProvenance(staleSessionFactory, cachedMapping));

        // isClosed() must actually track close(). A mock that always answers false is not merely
        // imprecise here — the regeneration path re-reads the session factory cache and treats a
        // non-closed entry as "someone else already rebuilt it", so a permanently-open stale mock
        // silently suppresses every rebuild and the suite reports rebuildCount 0 everywhere.
        final AtomicBoolean staleClosed = new AtomicBoolean(false);
        when(staleSessionFactory.isClosed()).thenAnswer(inv -> staleClosed.get());
        doAnswer(inv -> {
            closeCallCount.incrementAndGet();
            staleClosed.set(true);
            return null;
        }).when(staleSessionFactory).close();
    }

    @After
    public void tearDown() throws Exception {
        // HostManager's profile is a static ThreadLocal on a pooled thread: a tenant test that
        // left "tenantB" behind would silently re-scope every later test in this JVM.
        org.joget.commons.util.HostManager.setCurrentProfile(null);
        // cacheInProgress is static: without this, a cold-cache test leaves "null:t2085" behind
        // and makes any later cold-cache test in this JVM order-dependent.
        clearCacheInProgress();
    }

    // ------------------------------------------------------------------ helpers

    private PersistentClass mapping(String... columns) {
        PersistentClass pc = mock(PersistentClass.class);
        Table table = mock(Table.class);
        when(table.getName()).thenReturn(TABLE);
        when(pc.getTable()).thenReturn(table);
        // Anything but "transactional" independently forces changes = true and would mask
        // whatever the test is actually asserting.
        when(pc.getCacheConcurrencyStrategy()).thenReturn("transactional");
        when(pc.getProperty("createdBy")).thenReturn(mock(Property.class));

        Component component = mock(Component.class);
        List<Property> props = new ArrayList<Property>();
        for (String c : columns) {
            Property p = mock(Property.class);
            when(p.getName()).thenReturn(c);
            props.add(p);
        }
        when(component.getPropertySpan()).thenReturn(props.size());
        when(component.getProperties()).thenReturn(props);

        Property custom = mock(Property.class);
        when(custom.getValue()).thenReturn(component);
        when(pc.getProperty("customProperties")).thenReturn(custom);
        return pc;
    }

    private Set<String> columns(String... names) {
        return new LinkedHashSet<String>(Arrays.asList(names));
    }

    /**
     * A warm entry that also survives the database staleness check — i.e. no other node has
     * invalidated it. {@code getVerified} MUST be stubbed alongside {@code get}: an unstubbed
     * Mockito method returns null, which the production code reads as "verified stale", and every
     * warm cache test would then silently become a cold cache test.
     */
    private void columnCacheHolds(String... names) {
        when(formColumnCache.get(anyString())).thenReturn(columns(names));
        when(formColumnCache.getVerified(anyString())).thenReturn(columns(names));
        when(formColumnCache.getIndexes(anyString())).thenReturn(new HashSet<String>());
    }

    private void columnCacheIsCold() {
        when(formColumnCache.get(anyString())).thenReturn(null);
        when(formColumnCache.getVerified(anyString())).thenReturn(null);
        when(formColumnCache.getIndexes(anyString())).thenReturn(null);
    }

    /** Makes the real getFormDefinitionColumnNames run against empty DAO results. */
    private void daosReturnNothing() {
        when(builderDefinitionDao.find(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList());
        when(formDefinitionDao.loadFormDefinitionByTableName(anyString()))
                .thenReturn(new ArrayList());
    }

    /**
     * Makes the column build produce a non-empty result, which is what actually matters:
     * getFormDefinitionColumnNames only writes to the cache inside the
     * "formList non-empty OR builderDefs non-empty" guard, so with empty DAO results nothing is
     * ever cached and every call re-runs both queries.
     *
     * <p>A BuilderDefinition is used rather than a FormDefinition because that branch parses
     * plain JSON and needs no FormService / element-tree construction.</p>
     */
    private void daosReturnTwoColumns() {
        BuilderDefinition def = mock(BuilderDefinition.class);
        when(def.getJson()).thenReturn("{\"columns\":{\"title\":{},\"status\":{}}}");
        List<BuilderDefinition> defs = new ArrayList<BuilderDefinition>();
        defs.add(def);
        when(builderDefinitionDao.find(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(defs);
        when(formDefinitionDao.loadFormDefinitionByTableName(anyString()))
                .thenReturn(new ArrayList());
    }

    /**
     * Wires formColumnCache as a real read/write cache rather than a constant.
     *
     * <p>Needed by any test about avoiding repeat work: with a permanently-null mock the
     * production double-check has nothing to find, so even a correct single-flight lock still
     * shows one load per caller. Must be paired with {@link #daosReturnTwoColumns()} — production
     * skips the cache write entirely for an empty result.</p>
     */
    private AtomicReference<Collection<String>> statefulColumnCache() {
        final AtomicReference<Collection<String>> cached = new AtomicReference<Collection<String>>(null);
        when(formColumnCache.get(anyString())).thenAnswer(inv -> cached.get());
        when(formColumnCache.getVerified(anyString())).thenAnswer(inv -> cached.get());
        when(formColumnCache.getIndexes(anyString())).thenReturn(null);
        doAnswer(inv -> {
            cached.set((Collection<String>) inv.getArgument(1));
            return null;
        }).when(formColumnCache).put(anyString(), any(), any());
        return cached;
    }

    private SessionFactory findSessionFactory() {
        return dao.findSessionFactory(TABLE, TABLE, null, FormDataDaoImpl.ACTION_TYPE_LOAD);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, FormDataDaoImpl.SessionFactoryProvenance> sourceCache(Object target) throws Exception {
        java.lang.reflect.Field f = FormDataDaoImpl.class.getDeclaredField("formSessionFactorySourceCache");
        f.setAccessible(true);
        return (Map<String, FormDataDaoImpl.SessionFactoryProvenance>) f.get(target);
    }

    private static void inject(Object target, String field, Object value) throws Exception {
        java.lang.reflect.Field f = FormDataDaoImpl.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private static Set<String> cacheInProgress() throws Exception {
        try {
            java.lang.reflect.Field f = FormDataDaoImpl.class.getDeclaredField("cacheInProgress");
            f.setAccessible(true);
            return (Set<String>) f.get(null);
        } catch (NoSuchFieldException removedByTheFix) {
            // Phase 1 deletes cacheInProgress outright; nothing to reset once it is gone.
            return null;
        }
    }

    private static void clearCacheInProgress() throws Exception {
        Set<String> s = cacheInProgress();
        if (s != null) {
            s.clear();
        }
        clearManifestLatch();
    }

    /**
     * The manifest backoff is STATIC and per-profile: the first test whose manifest read throws
     * suspends the incremental path for that profile, and every later test in the same JVM using
     * that profile inherits it. Without this reset a single genuine failure cascades into unrelated
     * ones, and the suite becomes order-dependent.
     *
     * <p>The incremental base and the staleness throttle used to need the same treatment. They now
     * live in {@link FormColumnCache}, which is mocked per test, so they cannot leak between
     * tests at all — see the {@code bases} map in {@code setUp}.</p>
     */
    @SuppressWarnings("unchecked")
    private static void clearManifestLatch() throws Exception {
        java.lang.reflect.Field f = FormColumnResolver.class.getDeclaredField("MANIFEST_RETRY_AT");
        f.setAccessible(true);
        ((Map<Object, Object>) f.get(null)).clear();
    }

    /**
     * Marks the table as already having an async column rebuild in flight.
     *
     * <p>This is a real production state, not a convenience: it is the second half of Window 2 —
     * one request kicked off the background rebuild, and every request arriving before it
     * finishes gets {@code null} from {@code syncFormDefinitionColumnNamesCache}.</p>
     *
     * <p>It also keeps the test off Spring. Taking the "not yet in progress" branch constructs a
     * real {@link org.joget.commons.util.PluginThread}, whose constructor calls
     * {@code DynamicDataSourceManager.getCurrentProfile()} and NPEs without a Spring context —
     * which would turn this test into an ERROR whose assertion never runs.</p>
     *
     * <p>The key is {@code HostManager.getCurrentProfile() + ":" + unprefixedTable}, and that
     * profile is null outside Spring, hence the literal "null" prefix.</p>
     */
    private static void markRebuildInProgress() throws Exception {
        Set<String> s = cacheInProgress();
        if (s != null) {
            s.add("null:" + UNPREFIXED);
        }
    }

    // ============================================================ INV-1: read cost (#7709)

    /**
     * A warm column cache must satisfy validation without ever reaching the unbounded
     * loadFormDefinitionByTableName query.
     *
     * <p>Falsifiable against the obvious wrong fix for #2085: "always ask the authoritative
     * source" — i.e. bypassing the column cache to be safe. That implementation makes every
     * datalist request pay the #7709 cost and turns this test red.</p>
     *
     * <p>Narrow on its own, though: with a warm cache the current code short-circuits before the
     * DAO under every configuration this test can express, so it only catches a fix that removes
     * the cache read. {@link #inv1_coldColumnCacheLoadsFormDefinitionsOnlyOnce} is the half that
     * catches a fix which reloads on every request.</p>
     */
    @Test
    public void inv1_warmColumnCacheNeverLoadsFormDefinitions() {
        columnCacheHolds("title", "status");

        for (int i = 0; i < 25; i++) {
            findSessionFactory();
        }

        verify(formDefinitionDao, never()).loadFormDefinitionByTableName(anyString());
    }

    /**
     * A cold column cache may pay the expensive load exactly ONCE; the result must then be
     * cached and reused.
     *
     * <p>This is the falsifiable half of INV-1. A fix that resolves the column set on every
     * request — rather than caching it — yields 25 loads here while
     * {@link #inv1_warmColumnCacheNeverLoadsFormDefinitions} stays green, so without this test
     * that regression ships unnoticed.</p>
     */
    @Test
    public void inv1_coldColumnCacheLoadsFormDefinitionsOnlyOnce() {
        // Must yield a NON-EMPTY column set, or production skips the cache write entirely and
        // this test would measure 25 loads for a reason unrelated to caching.
        final AtomicReference<Collection<String>> cached = statefulColumnCache();
        daosReturnTwoColumns();

        for (int i = 0; i < 25; i++) {
            dao.getFormDefinitionColumnNames(TABLE);
        }

        assertEquals("the first call must populate the cache", columns("title", "status"), cached.get());
        verify(formDefinitionDao, times(1)).loadFormDefinitionByTableName(anyString());
    }

    /**
     * The DAO passes the UNPREFIXED table name to the column cache. Getting this wrong is the
     * classic cause of a cache that never hits, which would make every request pay the #7709
     * cost.
     *
     * <p>Scope: this asserts the DAO-to-cache calling convention only. It does NOT verify the
     * effective cache key — {@code FormColumnCache.getCacheKey} independently strips the prefix
     * again and adds the tenant profile, and this test mocks {@code FormColumnCache} entirely.
     * Tenant qualification of the real key is therefore not covered here.</p>
     */
    @Test
    public void inv1_daoPassesUnprefixedTableNameToColumnCache() {
        columnCacheHolds("title", "status");

        dao.getFormDefinitionColumnNames(TABLE);

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(formColumnCache, atLeastOnce()).getVerified(key.capture());
        assertEquals("the app_fd_ prefix must be stripped before the cache lookup",
                UNPREFIXED, key.getValue());
    }

    // ============================================================ INV-2: creation cost (#7709)

    /**
     * The literal #7709. A form can be saved without its field set changing — a label edit, a
     * field reorder, or publishing a new app version that copies every form row onto the same
     * table. None of those may regenerate the mapping file.
     */
    @Test
    public void inv2_formTouchedButColumnSetIdenticalDoesNotRebuildMapping() {
        columnCacheHolds("status", "title");

        for (int i = 0; i < 25; i++) {
            findSessionFactory();
        }

        assertEquals("a form edit that changes no columns must not regenerate the mapping",
                0, rebuildCount.get());
    }

    /**
     * Mirror of the above: a genuine column-set change rebuilds, and repeated requests do not
     * each rebuild. Guards both "never rebuilds" and "rebuilds on every request" regressions.
     *
     * <p>The loop is load-bearing. With a single call, "exactly once" would be guaranteed by
     * arity, and an implementation that regenerates the mapping on EVERY request would pass —
     * which is the most likely way a #2085 fix reintroduces #7709. The stateful cache fakes make
     * the second and later calls see the rebuilt mapping, so a cumulative count above one means
     * the code is rebuilding when it no longer needs to.</p>
     */
    @Test
    public void inv2_columnSetChangedRebuildsMappingOnceNotPerRequest() {
        columnCacheHolds("title", "status", "newcol");
        columnsAfterRebuild = new String[]{"title", "status", "newcol"};

        SessionFactory first = findSessionFactory();
        for (int i = 0; i < 24; i++) {
            findSessionFactory();
        }

        assertSame(rebuiltSessionFactory, first);
        assertEquals("the mapping must be regenerated once, not on every request",
                1, rebuildCount.get());
    }

    /**
     * An EMPTY column set does not shrink the mapping — the {@code !formFields.isEmpty()} guard
     * treats "no columns" as a reason to leave the mapping alone.
     *
     * <p>Scope: the fixture is a warm cache holding an empty set, so this pins the
     * <i>empty-set</i> branch, not an authoritatively-verified emptiness. A stale empty cache
     * over a non-empty database produces the same pass. That is acceptable only because the
     * distinction it does draw is the one that matters here: empty ("looked, found nothing")
     * must not be conflated with cold ("unknown, go and look"), which is the branch #2085 lives
     * in.</p>
     *
     * <p>This preserves existing behaviour rather than asserting it is ideal. Arguably a table
     * whose forms were all deleted should shrink its mapping — a real staleness hole in the same
     * family as #2085 — but changing it is out of scope and would risk unmapping columns that
     * still hold data. If a later change deliberately makes an empty set rebuild, update this
     * test; do not work around it in production code.</p>
     */
    @Test
    public void inv2_emptyColumnSetDoesNotShrinkMapping() {
        columnCacheHolds();

        findSessionFactory();

        assertEquals(0, rebuildCount.get());
    }

    // ============================================================ INV-3: herd control (#7709)

    /**
     * Concurrent callers finding a cold cache must collapse to a single expensive load.
     *
     * <p>EXPECTED RED before the fix: getFormDefinitionColumnNames holds no lock today, so all
     * eight callers run the full load. This is a herd that already exists; the fix's
     * single-flight lock is what turns it green.</p>
     */
    @Test
    public void inv3_concurrentColdCacheCollapsesToOneLoad() throws Exception {
        // A stateful cache and a NON-EMPTY result are both required. With a permanently-cold
        // mock (or an empty result, which production never caches) the double-check inside the
        // lock finds nothing and every caller rebuilds regardless — the test would then be
        // measuring the uncached-empty-result bug, not single-flight.
        statefulColumnCache();
        daosReturnTwoColumns();

        final AtomicInteger loads = new AtomicInteger();
        final AtomicReference<Throwable> firstFailure = new AtomicReference<Throwable>();
        when(formDefinitionDao.loadFormDefinitionByTableName(anyString())).thenAnswer(inv -> {
            loads.incrementAndGet();
            Thread.sleep(50); // widen the window so a missing lock is reliably observed
            return new ArrayList();
        });

        int threads = 8;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            new Thread(() -> {
                try {
                    start.await();
                    dao.getFormDefinitionColumnNames(TABLE);
                } catch (Throwable t) {
                    // Surface it rather than letting loads==0 masquerade as a lock failure.
                    firstFailure.compareAndSet(null, t);
                } finally {
                    done.countDown();
                }
            }).start();
        }
        start.countDown();
        assertTrue("workers did not finish", done.await(30, TimeUnit.SECONDS));
        if (firstFailure.get() != null) {
            throw new AssertionError("worker threw", firstFailure.get());
        }

        assertEquals("concurrent cold-cache callers must collapse to one expensive load",
                1, loads.get());
    }

    /**
     * A build that re-enters for a DIFFERENT table must not wait on that table's lock.
     *
     * <p>This is the deadlock-freedom property, and it is not hypothetical: a build runs
     * {@code createElementFromJson} and {@code getDynamicFieldNames()}, i.e. plugin code, and
     * {@code MultirowFormBinder.getFormPropertyName} (a load binder that lives on form elements)
     * calls {@code getFormDefinitionColumnNames} for another table. If a thread could hold
     * lock(A) while waiting for lock(B), two threads building mutually-referencing tables would
     * deadlock ABBA.</p>
     *
     * <p>Here thread 1 holds lock(t2085) and stays inside its build; the main thread then builds
     * a nested second table. It must complete rather than block. A 10s bound is used instead of
     * an unbounded call so a regression shows up as a clean failure rather than a hung suite.</p>
     */
    @Test
    public void inv3_nestedBuildForAnotherTableDoesNotBlock() throws Exception {
        daosReturnTwoColumns();
        // Deliberately NOT a stateful cache: every lookup stays cold, so any lock that is taken
        // is genuinely contended and a missing guard shows up as a block rather than a cache hit.
        columnCacheIsCold();

        final CountDownLatch holderInsideBuild = new CountDownLatch(1);
        final CountDownLatch releaseHolder = new CountDownLatch(1);
        final AtomicReference<Thread> holderThread = new AtomicReference<Thread>();
        final AtomicReference<Throwable> nestedFailure = new AtomicReference<Throwable>();

        when(formDefinitionDao.loadFormDefinitionByTableName(anyString())).thenAnswer(inv -> {
            String requested = inv.getArgument(0);
            if (Thread.currentThread() == holderThread.get()) {
                // The holder parks INSIDE its build, keeping lock("other") held.
                holderInsideBuild.countDown();
                releaseHolder.await(10, TimeUnit.SECONDS);
            } else if (UNPREFIXED.equals(requested)) {
                // Stand in for plugin code (e.g. MultirowFormBinder on a form element) resolving
                // ANOTHER table's columns from inside this build, on this same thread.
                dao.getFormDefinitionColumnNames("other");
            }
            return new ArrayList();
        });

        Thread holder = new Thread(() -> dao.getFormDefinitionColumnNames("other"));
        holder.setDaemon(true);
        holderThread.set(holder);
        holder.start();
        assertTrue("holder never entered its build", holderInsideBuild.await(10, TimeUnit.SECONDS));

        final CountDownLatch nestedDone = new CountDownLatch(1);
        Thread nested = new Thread(() -> {
            try {
                dao.getFormDefinitionColumnNames(TABLE);
            } catch (Throwable t) {
                nestedFailure.compareAndSet(null, t);
            } finally {
                nestedDone.countDown();
            }
        });
        nested.setDaemon(true);
        nested.start();

        boolean finished = nestedDone.await(10, TimeUnit.SECONDS);
        releaseHolder.countDown();
        if (nestedFailure.get() != null) {
            throw new AssertionError("nested build threw", nestedFailure.get());
        }
        assertTrue("a build that re-enters for another table must not wait on that table's lock "
                + "- holding one column lock while blocking on another is the ABBA deadlock", finished);
    }

    /**
     * Concurrent threads that all detect the same column change must regenerate the mapping ONCE.
     *
     * <p>Regeneration is not cheap bookkeeping: it writes {@code <entity>.hbm.xml}, builds a
     * {@code SessionFactory} and runs {@code SchemaUpdate} DDL. Letting N threads do that
     * simultaneously means concurrent DDL on one table plus N-1 discarded factories. Failing
     * closed on change detection makes this fire reliably, where the old fail-open path largely
     * hid it — so it is a regression risk introduced by the #2085 fix, not a pre-existing one
     * that can be waved through.</p>
     */
    @Test(timeout = 30000)
    public void inv2_concurrentChangeDetectionRegeneratesMappingOnce() throws Exception {
        columnCacheHolds("title", "status", "newcol");
        columnsAfterRebuild = new String[]{"title", "status", "newcol"};

        int threads = 8;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(() -> {
                try {
                    start.await();
                    findSessionFactory();
                } catch (Throwable e) {
                    failure.compareAndSet(null, e);
                } finally {
                    done.countDown();
                }
            });
            t.setDaemon(true);
            t.start();
        }
        start.countDown();
        assertTrue("workers did not finish", done.await(20, TimeUnit.SECONDS));
        if (failure.get() != null) {
            throw new AssertionError("worker threw", failure.get());
        }

        assertEquals("concurrent change detection must regenerate the mapping once, not per thread "
                + "- each regeneration writes the .hbm.xml and runs SchemaUpdate DDL",
                1, rebuildCount.get());
    }

    /**
     * Quantifies a reviewer finding on the just-shipped #2085 fix (Finding 1, 2026-09-08 audit):
     * {@code findSessionFactory}'s own check-close-rebuild sequence (lines ~945-1077) runs BEFORE
     * {@code createSessionFactoryWithSingleFlight}'s {@code FormBuildGuard} lock is ever taken, so
     * every one of N concurrent callers that reaches "changes detected" independently evaluates
     * {@code !sf.isClosed()} and calls {@code sf.close()} on the SAME shared, still-open factory —
     * a genuine check-then-act race, not merely N threads calling close() in some safe sequence.
     *
     * <p>{@link #inv2_concurrentChangeDetectionRegeneratesMappingOnce} already proves the EXPENSIVE
     * part (the real {@code createSessionFactory} rebuild - DDL, disk write) collapses to one
     * regardless, at 8 threads. This test raises concurrency to 30 (the top of the reviewer's
     * claimed 20-30-way range) and additionally counts {@code close()} invocations, which that test
     * does not, to put a real number on the "resource-storm" claim rather than accepting it on
     * faith.</p>
     *
     * <p><b>Expected, and confirmed by this test</b>: {@code rebuildCount} stays at 1 (single-flight
     * still holds at 30 threads), but {@code closeCallCount} is routinely {@code > 1} - i.e. multiple
     * threads DO redundantly call {@code close()} on the same factory before any of them reaches the
     * guard, confirming the reviewer's finding is real as a wasted-work concern. It is NOT a
     * correctness bug: Hibernate 6.6's {@code SessionFactoryImpl.close()} (read from
     * {@code hibernate-core-6.6.1.Final-sources.jar}) is internally {@code synchronized(this)} and
     * checks {@code status != Status.OPEN} before doing any teardown work, so redundant calls are
     * safe, cheap no-ops at the Hibernate level - this test's own mock does not model that
     * idempotency (a bare Mockito mock has no such guard), so a naive assertion of "close() called
     * exactly once" would be testing a stronger guarantee than production actually needs or
     * provides.</p>
     */
    @Test(timeout = 30000)
    public void inv2_concurrentChangeDetectionCallsCloseRedundantlyButRebuildsOnce() throws Exception {
        columnCacheHolds("title", "status", "newcol");
        columnsAfterRebuild = new String[]{"title", "status", "newcol"};

        int threads = 30;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(() -> {
                try {
                    start.await();
                    findSessionFactory();
                } catch (Throwable e) {
                    failure.compareAndSet(null, e);
                } finally {
                    done.countDown();
                }
            });
            t.setDaemon(true);
            t.start();
        }
        start.countDown();
        assertTrue("workers did not finish", done.await(20, TimeUnit.SECONDS));
        if (failure.get() != null) {
            throw new AssertionError("worker threw", failure.get());
        }

        System.out.println("[bug2085 finding1] threads=" + threads
                + " rebuildCount=" + rebuildCount.get()
                + " closeCallCount=" + closeCallCount.get());

        assertEquals("the expensive rebuild must still collapse to one at 30-way concurrency",
                1, rebuildCount.get());
        assertTrue("at least one close() call is expected (the factory IS stale and must be closed)",
                closeCallCount.get() >= 1);
        // Deliberately NOT asserting closeCallCount == 1: the whole point of this test is to show
        // that it usually is NOT 1, quantifying the reviewer's finding rather than asserting the
        // absence of the very redundancy being measured. See the class javadoc above for why this
        // is wasteful-but-safe rather than a correctness bug.
    }

    /**
     * A circular column dependency must terminate rather than blow the stack.
     *
     * <p>Form A's plugin code resolves form B's columns and B's resolves A's. Neither result is
     * cached until its build unwinds, so a guard that simply "builds directly when nested"
     * recurses A→B→A→B until {@code StackOverflowError}. The cycle edge has to be detected and
     * cut. This hazard predates the single-flight work — the old code also built inline on a
     * cold cache — but the fix must not preserve it.</p>
     */
    @Test(timeout = 30000)
    public void inv3_circularColumnDependencyTerminates() {
        columnCacheIsCold();
        when(builderDefinitionDao.find(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList());
        when(formDefinitionDao.loadFormDefinitionByTableName(anyString())).thenAnswer(inv -> {
            String requested = inv.getArgument(0);
            // A references B, B references A.
            dao.getFormDefinitionColumnNames(UNPREFIXED.equals(requested) ? "other" : UNPREFIXED);
            return new ArrayList();
        });

        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertNotNull("a cyclic dependency must resolve, not recurse forever", result);
    }

    /**
     * A cycle-cut result must NOT be mistaken for a legitimately empty column set.
     *
     * <p>{@link FormDataDaoImpl#hasMappingChanged} treats known-empty as "no change". If the
     * cycle result were an ordinary empty set, a validation reached through a cycle would serve a
     * mapping it cannot vouch for — which is #2085 itself, reintroduced by the fix for it. The
     * two states must be distinguishable, and the unresolved one must abort rather than publish.</p>
     */
    @Test
    public void bug2085_unresolvedColumnSetMustNotBeTreatedAsUnchanged() {
        PersistentClass pc = mapping("title", "status");

        try {
            dao.hasMappingChanged(pc, FormColumnResolver.UNRESOLVED_COLUMNS);
            fail("an unresolved column set must not be reported as 'no change'");
        } catch (HibernateException expected) {
            assertTrue("should explain the circular dependency, got: " + expected.getMessage(),
                    expected.getMessage().contains("circular"));
        }

        // A genuinely empty set is a different state and must still mean "no change".
        assertFalse("a known-empty column set must remain a non-change",
                dao.hasMappingChanged(pc, new HashSet<String>()));
    }

    /**
     * A failed COLUMN LOAD must not stop later loads from being attempted.
     *
     * <p>Scope, stated precisely because the obvious reading is wrong: this exercises
     * {@code getFormDefinitionColumnNames} directly. It does not perform a mapping rebuild and
     * it does not touch today's {@code cacheInProgress} — that wedge lives on the background
     * thread inside {@code syncFormDefinitionColumnNamesCache}, and the fix deletes it rather
     * than repairing it, so there is nothing there worth pinning.</p>
     *
     * <p>It passes today only because that method holds no single-flight state at all. Its value
     * is FORWARD: the fix adds a per-table lock here, and if that lock is ever taken without a
     * try/finally — or leaves an in-progress marker behind on failure — this turns red. That is
     * exactly the shape of the bug being removed, reintroduced one layer down.</p>
     */
    @Test
    public void inv3_failedColumnLoadDoesNotWedgeLaterLoads() {
        columnCacheIsCold();
        when(builderDefinitionDao.find(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList());
        when(formDefinitionDao.loadFormDefinitionByTableName(anyString()))
                .thenThrow(new RuntimeException("boom"))
                .thenReturn(new ArrayList());

        try {
            dao.getFormDefinitionColumnNames(TABLE);
        } catch (RuntimeException expected) {
            // first attempt fails
        }
        dao.getFormDefinitionColumnNames(TABLE);

        verify(formDefinitionDao, times(2)).loadFormDefinitionByTableName(anyString());
    }

    // ============================================================ #2085 correctness (RED today)

    /**
     * Window 2 — the fail-open branch. The column cache is cold, so the async variant returns
     * null and change detection is skipped entirely; the unverified mapping is served.
     *
     * <p>The authoritative answer is that a third column now exists. A cold cache is a reason to
     * go and look, not a reason to assume nothing changed.</p>
     *
     * <p>EXPECTED RED before the fix.</p>
     */
    @Test
    public void bug2085_coldColumnCacheMustConsultAuthoritativeColumnSet() throws Exception {
        columnCacheIsCold();
        markRebuildInProgress();
        dao.authoritativeColumns = columns("title", "status", "newcol");

        SessionFactory result = findSessionFactory();

        assertEquals("an unverifiable mapping must be rebuilt, not served", 1, rebuildCount.get());
        assertSame(rebuiltSessionFactory, result);
    }

    /**
     * WINDOW 1 — the widest window, and the actual #2085 failure observed on the 2-node cluster.
     *
     * <p>The node's column cache is warm and, as far as it can tell, valid: the
     * {@code CACHE_LAST_CLEAR} flag that should have invalidated it is read through
     * {@code SetupManagerCache}, which refreshes on a 20s debounce. So for ~20-25s after a form
     * change on another node this node still reports the OLD column set, that set matches the OLD
     * mapping, change detection says "no change", and the stale mapping serves HQL naming the new
     * column.</p>
     *
     * <p>Handling null does nothing here — the column set is non-null and self-consistent. Only
     * asking the database directly catches it.</p>
     */
    @Test
    public void bug2085_warmButStaleColumnCacheMustBeRevalidatedAgainstDatabase() {
        // NOTE: deliberately NOT using dao.authoritativeColumns here. That override replaces
        // getFormDefinitionColumnNames wholesale, which bypasses the cache and the staleness
        // check — the test would then pass with the check disabled, proving nothing.
        //
        // get() returns the warm, stale entry: this is what the debounced setup cache reports.
        // getVerified() re-reads CACHE_LAST_CLEAR_ from the database, finds the other node's
        // invalidation, evicts, and returns null. Which of the two the DAO calls is the entire fix.
        final AtomicReference<Collection<String>> cachedCols = new AtomicReference<Collection<String>>(columns("title", "status"));
        when(formColumnCache.get(anyString())).thenAnswer(inv -> cachedCols.get());
        when(formColumnCache.getIndexes(anyString())).thenReturn(new HashSet<String>());
        // Model getVerified's real contract, not just its return value: it EVICTS the entry it
        // finds stale before returning null. That eviction is load-bearing — it is what stops the
        // double-check inside the single-flight lock from handing the very same stale list straight
        // back and skipping the rebuild. A stub that only returned null would leave this test red
        // against correct production code.
        when(formColumnCache.getVerified(anyString())).thenAnswer(inv -> {
            cachedCols.set(null);
            return null;
        });
        doAnswer(inv -> {
            cachedCols.set((Collection<String>) inv.getArgument(1));
            return null;
        }).when(formColumnCache).put(anyString(), any(), any());

        // The shared database has moved on: a third column was added on the other node.
        BuilderDefinition def = mock(BuilderDefinition.class);
        when(def.getJson()).thenReturn("{\"columns\":{\"title\":{},\"status\":{},\"newcol\":{}}}");
        List<BuilderDefinition> defs = new ArrayList<BuilderDefinition>();
        defs.add(def);
        when(builderDefinitionDao.find(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(defs);
        when(formDefinitionDao.loadFormDefinitionByTableName(anyString())).thenReturn(new ArrayList());

        SessionFactory result = findSessionFactory();

        assertEquals("a warm column cache that the database says was invalidated on another node "
                + "must not be accepted as proof the mapping is current", 1, rebuildCount.get());
        assertSame(rebuiltSessionFactory, result);
    }

    /**
     * WINDOW 3 - the pc/sf decoupling gap identified in anis.yusra's 2026-09-09 review comment on
     * MR !1248 (GitLab note_48496) and closed by this session's {@code sessionFactoryMappingDiffersFrom}
     * check.
     *
     * <p>Windows 1 and 2 above are both "is the {@code PersistentClass} we currently have
     * trustworthy" checks - they compare {@code pc} against the database or the column cache. Both
     * are satisfied here: {@code pc} is moved straight to the POST-change 3-column mapping (standing
     * in for {@code formPersistentClassCache}'s 120s TTL having expired and the disk reload finding
     * a mapping file another node already updated), and the authoritative column set agrees - 3
     * columns, matching {@code pc} exactly. Nothing about {@code pc} looks stale.</p>
     *
     * <p>But the CACHED {@code sf} (and its recorded provenance in
     * {@code formSessionFactorySourceCache}, seeded in {@code setUp} and never touched by this test)
     * is still the ORIGINAL 2-column factory - it was never rebuilt. A check that only asks "is pc
     * current" cannot see this: pc genuinely IS current. Only asking "does the cached sf's own
     * provenance match pc" catches it.</p>
     *
     * <p>EXPECTED RED without {@code sessionFactoryMappingDiffersFrom} - mutation-verified by
     * temporarily short-circuiting it to always return {@code false} (i.e. exactly the code this
     * fix touched, reverted): this test fails (rebuildCount stays 0) as expected; restoring the
     * real check turns it green again.</p>
     */
    @Test
    public void bug2085_pcMatchingCurrentTruthDoesNotProveTheCachedSfMatchesIt() {
        // pc lands on the POST-change mapping - standing in for a disk reload after pc's own cache
        // entry expired, reading a file another node had already updated.
        pcRef.set(mapping("title", "status", "newcol"));
        // The authoritative truth agrees with pc exactly - nothing for Window 1/2's checks to
        // catch on their own.
        columnCacheHolds("title", "status", "newcol");
        // sfRef (staleSessionFactory) and sourceCache are UNCHANGED from setUp: sf itself is still
        // paired with the ORIGINAL 2-column mapping - it was never rebuilt to match.

        SessionFactory result = findSessionFactory();

        assertEquals("pc matching current truth does not prove the CACHED sf was ever rebuilt to "
                + "match it - the cross-node staleness gap identified in GitLab note_48496",
                1, rebuildCount.get());
        assertSame(rebuiltSessionFactory, result);
    }

    /**
     * The mirror, and the guard that stops the staleness check becoming a #7709 regression: when
     * the database reports no invalidation, the warm entry must be served without rebuilding and
     * without paying for the unbounded load.
     */
    @Test
    public void inv1_freshCheckFindingNoInvalidationSkipsTheExpensiveLoad() {
        columnCacheHolds("title", "status");

        for (int i = 0; i < 25; i++) {
            findSessionFactory();
        }

        assertEquals("an entry the database agrees is current must not regenerate the mapping",
                0, rebuildCount.get());
        verify(formDefinitionDao, never()).loadFormDefinitionByTableName(anyString());
    }

    /**
     * The DAO must always ask the VERIFIED read, never the plain one — the throttling that keeps
     * that affordable is FormColumnCache's business, and is tested in {@code FormColumnCacheTest}.
     *
     * <p>Falsifiable: switch production back to {@code formColumnCache.get(...)} and this fails.</p>
     */
    @Test
    public void inv1_theColumnSetIsReadThroughTheVerifiedLookup() {
        columnCacheHolds("title", "status");

        for (int i = 0; i < 25; i++) {
            findSessionFactory();
        }

        verify(formColumnCache, atLeastOnce()).getVerified(anyString());
        assertEquals(0, rebuildCount.get());
    }

    // ================================================== buildFormDefinitionColumnNames internals

    /**
     * Each builder definition contributes its {@code columns} keys, in list order.
     *
     * <p>Separate definitions rather than several keys in one JSON object: {@code JSONObject} keys
     * come back in hash order, so a two-key object could not state "status was seen first" and the
     * eviction test below would pass or fail by luck.</p>
     */
    private void builderDefsWithColumns(String... columnNamePerDef) {
        List<BuilderDefinition> defs = new ArrayList<BuilderDefinition>();
        for (String name : columnNamePerDef) {
            BuilderDefinition def = mock(BuilderDefinition.class);
            when(def.getJson()).thenReturn("{\"columns\":{\"" + name + "\":{}}}");
            defs.add(def);
        }
        when(builderDefinitionDao.find(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(defs);
        when(formDefinitionDao.loadFormDefinitionByTableName(anyString()))
                .thenReturn(new ArrayList());
    }

    /** One mocked FormDefinition per JSON string, all mapped to the table under test. */
    private void formDefsWithJson(String... jsons) {
        List<FormDefinition> defs = new ArrayList<FormDefinition>();
        for (int i = 0; i < jsons.length; i++) {
            FormDefinition def = mock(FormDefinition.class);
            when(def.getId()).thenReturn("f" + i);
            when(def.getJson()).thenReturn(jsons[i]);
            defs.add(def);
        }
        when(formDefinitionDao.loadFormDefinitionByTableName(anyString())).thenReturn(defs);
        when(builderDefinitionDao.find(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList());
        when(formService.createElementFromJson(anyString(), anyBoolean())).thenReturn(mock(Form.class));
    }

    /**
     * A name that can never be a column must not evict the valid column holding its lowercase slot.
     *
     * <p>The original order was: resolve duplicates, claim the slot, and only THEN check the name
     * against the column pattern. So a rejected name still displaced whatever valid column shared
     * its lowercase key, and was then dropped itself — leaving NEITHER. The mapping loses a column
     * that holds live data, which is #2085's failure by another route.</p>
     *
     * <p><b>Narrow by construction, and the test says so.</b> For pure ASCII this cannot happen:
     * {@code toLowerCase()} only maps {@code A-Z} to {@code a-z}, both inside the allowed set, so an
     * invalid name and a valid one can never share a key. It needs a character that folds INTO the
     * allowed set from outside it — here U+212A KELVIN SIGN, which fails {@code [0-9a-zA-Z_-]} yet
     * lowercases to plain {@code "k"}. The default-locale {@code toLowerCase()} opens the same door
     * on a Turkish-locale JVM, where {@code "I"} folds to a dotless {@code ı}.</p>
     *
     * <p>Falsifiable: check the pattern after duplicate resolution instead of before, and the
     * column set comes back empty.</p>
     */
    @Test
    public void bug_invalidColumnNameMustNotEvictAValidColumn() {
        columnCacheIsCold();
        // U+212A KELVIN SIGN, written as an escape so this cannot silently degrade into an ordinary
        // "k"/"K" case-duplicate if the file's encoding is ever mangled.
        String kelvin = "\u212A";
        assertEquals("precondition: the kelvin sign must fold to a plain k", "k", kelvin.toLowerCase());
        builderDefsWithColumns("k", kelvin);

        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertTrue("the valid column was evicted by a name that cannot be a column: " + result,
                result.contains("k"));
        assertFalse("an invalid name must never become a column: " + result,
                result.contains(kelvin));
    }

    /**
     * A genuine case-only duplicate is still resolved in favour of the spelling seen last.
     *
     * <p>The guard against over-correcting the test above: a fix that simply stopped evicting would
     * leave both {@code status} and {@code Status} in the set, which is two columns differing only
     * by case and cannot be mapped.</p>
     */
    @Test
    public void buildResolvesCaseOnlyDuplicatesInFavourOfTheLastSpelling() {
        columnCacheIsCold();
        builderDefsWithColumns("status", "Status");

        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertEquals("exactly one spelling may survive: " + result, 1, result.size());
        assertTrue(result.contains("Status"));
    }

    /**
     * A null element ID must not take the whole form's columns down with it.
     *
     * <p>RED before the fix (0 columns instead of 2). {@code c.isEmpty()} threw NPE inside the
     * per-form {@code catch (Exception e)} that logs "protected or corrupted", so every column of
     * that form was discarded — silently, at DEBUG level — and the mapping was generated without
     * them.</p>
     *
     * <p>Reachability, stated accurately: core elements cannot produce a null, because
     * {@code getPropertyString} returns {@code ""} for a missing property. {@code
     * getDynamicFieldNames()} is a public extension point, and this build already runs arbitrary
     * plugin code, so the null arrives from a third-party element — not from anything in-tree.</p>
     */
    @Test
    public void bug_nullElementIdMustNotDiscardTheFormsColumns() {
        columnCacheIsCold();
        formDefsWithJson("{\"className\":\"org.joget.apps.form.model.Form\"}");
        dao.stubElementIds = Arrays.asList("title", null, "status");

        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertEquals("a null id must be skipped, not fatal to the form: " + result, 2, result.size());
        assertTrue(result.containsAll(Arrays.asList("title", "status")));
    }

    /**
     * INV-1, cold-path cost. #7709 is literally "many forms in many app versions pointing to the
     * same table name": {@code loadFormDefinitionByTableName} has no DISTINCT, and an unchanged form
     * carries byte-identical JSON in every app version. Parsing it once per row is the dominant cost
     * of this build — {@code createElementFromJson} constructs a whole element tree and instantiates
     * plugins — and it cannot produce a different answer the second time.
     *
     * <p>Falsifiable: drop the {@code parsedDefinitions} guard and this reports 8 parses.</p>
     */
    @Test
    public void inv1_identicalFormJsonAcrossAppVersionsIsParsedOnce() {
        columnCacheIsCold();
        String json = "{\"className\":\"org.joget.apps.form.model.Form\"}";
        formDefsWithJson(json, json, json, json, json, json, json, json);
        dao.stubElementIds = Arrays.asList("title", "status");

        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        verify(formService, times(1)).createElementFromJson(anyString(), anyBoolean());
        assertEquals("dedup must not change the answer: " + result, 2, result.size());
    }

    /**
     * The other half of the dedup guard: definitions that genuinely differ must each be parsed.
     * Without this, "parse only the first one" would pass the test above.
     */
    @Test
    public void inv1_distinctFormJsonIsParsedPerDefinition() {
        columnCacheIsCold();
        formDefsWithJson("{\"className\":\"a\"}", "{\"className\":\"b\"}", "{\"className\":\"c\"}");
        dao.stubElementIds = Arrays.asList("title");

        dao.getFormDefinitionColumnNames(TABLE);

        verify(formService, times(3)).createElementFromJson(anyString(), anyBoolean());
    }

    /**
     * Rows in the given order, each carrying the named JSON. Each DISTINCT json parses to its own
     * Form stand-in contributing its own element ids, so a test can say "row 3 carries the same json
     * as row 1" and then observe whether row 3's contribution is replayed.
     */
    private void formDefsWithPerJsonIds(Map<String, Collection<String>> idsByJson, String... jsonPerRow) {
        final Map<String, Form> formByJson = new HashMap<String, Form>();
        final Map<Object, Collection<String>> idsByElement = new HashMap<Object, Collection<String>>();
        for (Map.Entry<String, Collection<String>> e : idsByJson.entrySet()) {
            Form f = mock(Form.class);
            formByJson.put(e.getKey(), f);
            idsByElement.put(f, e.getValue());
        }
        List<FormDefinition> defs = new ArrayList<FormDefinition>();
        for (int i = 0; i < jsonPerRow.length; i++) {
            FormDefinition def = mock(FormDefinition.class);
            when(def.getId()).thenReturn("f" + i);
            when(def.getJson()).thenReturn(jsonPerRow[i]);
            defs.add(def);
        }
        when(formDefinitionDao.loadFormDefinitionByTableName(anyString())).thenReturn(defs);
        when(builderDefinitionDao.find(anyString(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new ArrayList());
        when(formService.createElementFromJson(anyString(), anyBoolean()))
                .thenAnswer(inv -> formByJson.get((String) inv.getArgument(0)));
        dao.stubIdsByElement = idsByElement;
    }

    /**
     * A repeated JSON must still REPLAY its columns, even though it is only parsed once.
     *
     * <p>Found by an external review (Sol) of the parse-dedup, and red against the first version of
     * it. {@code collectColumn} resolves a case-only duplicate in favour of the spelling seen LAST,
     * so a row that merely repeats an earlier definition is not a no-op — it re-asserts its
     * spelling. Rows contributing {@code foo}, {@code Foo}, {@code foo} must end on {@code foo};
     * skipping the third row as "already parsed" leaves {@code Foo}, the mapping is generated with
     * {@code c_Foo}, and HQL naming {@code foo} fails with UnknownPathException — #2085 by another
     * route. Memoising the parse is free; skipping the row is not.</p>
     */
    @Test
    public void bug_repeatedJsonMustStillReplayItsColumnsForDuplicateResolution() {
        columnCacheIsCold();
        String jsonLower = "{\"form\":\"lower\"}";
        String jsonUpper = "{\"form\":\"upper\"}";
        Map<String, Collection<String>> ids = new HashMap<String, Collection<String>>();
        ids.put(jsonLower, Arrays.<String>asList("foo"));
        ids.put(jsonUpper, Arrays.<String>asList("Foo"));
        formDefsWithPerJsonIds(ids, jsonLower, jsonUpper, jsonLower);

        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertEquals("case-only duplicates must collapse to one spelling: " + result, 1, result.size());
        assertTrue("the LAST contributing row must win: " + result, result.contains("foo"));
    }

    /**
     * A definition whose parse throws must not suppress the retry on an identical definition.
     *
     * <p>Also from the Sol review, also red against the first dedup. Parsing runs plugin
     * construction and {@code getDynamicFieldNames()}, so a failure can be transient. Marking the
     * JSON "seen" BEFORE the parse succeeded meant one unlucky row silently cost every identical row
     * after it — and the caller swallows the exception at DEBUG as "protected or corrupted", so the
     * whole form's columns vanish with no visible error.</p>
     */
    @Test
    public void bug_failedParseMustNotSuppressRetryOnAnIdenticalDefinition() {
        columnCacheIsCold();
        String json = "{\"className\":\"org.joget.apps.form.model.Form\"}";
        formDefsWithJson(json, json);
        dao.stubElementIds = Arrays.asList("title", "status");
        final AtomicInteger parseCalls = new AtomicInteger();
        when(formService.createElementFromJson(anyString(), anyBoolean())).thenAnswer(inv -> {
            if (parseCalls.incrementAndGet() == 1) {
                throw new RuntimeException("transient plugin failure during element creation");
            }
            return mock(Form.class);
        });

        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertEquals("the identical second row must retry the parse: " + result, 2, result.size());
        assertTrue(result.containsAll(Arrays.asList("title", "status")));
    }

    // =========================================== incremental rebuild (manifest comparison)

    private static class Def {
        final String id; final String json; final long modified; final int length; final String[] columns;
        Def(String id, String json, long modified, int length, String... columns) {
            this.id = id; this.json = json; this.modified = modified;
            this.length = length; this.columns = columns;
        }
    }

    private static Def def(String id, String json, long modified, int length, String... columns) {
        return new Def(id, json, modified, length, columns);
    }

    /**
     * Wires the manifest projection AND the definition loaders so they agree. The FormDefinition's
     * own dateModified is set to match its manifest row, because production now cross-checks them.
     * Callable twice per test: that is how a test says "and now the database looks like THIS".
     */
    private void definitions(Def... defs) {
        definitions(true, defs);
    }

    /**
     * The manifest reports {@code manifestSays}, but the definition loader hands back
     * {@code payloadIs} — a DIFFERENT, older revision. That is what a cacheable payload query
     * serving a stale entity behind an uncached manifest projection looks like.
     */
    private void definitionsWithStalePayload(Def manifestSays, Def payloadIs) {
        List<Object[]> manifest = new ArrayList<Object[]>();
        manifest.add(new Object[]{"app1", 1L, manifestSays.id,
                new java.util.Date(manifestSays.modified), manifestSays.length});

        FormDefinition fd = mock(FormDefinition.class);
        when(fd.getId()).thenReturn(payloadIs.id);
        when(fd.getJson()).thenReturn(payloadIs.json);
        when(fd.getAppId()).thenReturn("app1");
        when(fd.getAppVersion()).thenReturn(1L);
        when(fd.getDateModified()).thenReturn(new java.util.Date(payloadIs.modified));
        final List<FormDefinition> all = new ArrayList<FormDefinition>(Arrays.asList(fd));

        Form form = mock(Form.class);
        final Map<Object, Collection<String>> idsByElement = new HashMap<Object, Collection<String>>();
        idsByElement.put(form, Arrays.asList(payloadIs.columns));

        doReturn(manifest).when(formDefinitionDao).getFormDefinitionManifestByTableName(anyString());
        doReturn(all).when(formDefinitionDao).loadFormDefinitionByTableName(anyString());
        doReturn(all).when(formDefinitionDao).loadFormDefinitionByTableNameAndIds(anyString(), any());
        doReturn(form).when(formService).createElementFromJson(anyString(), anyBoolean());
        doReturn(new ArrayList()).when(builderDefinitionDao)
                .find(anyString(), any(), any(), any(), any(), any(), any());
        dao.stubIdsByElement = idsByElement;
    }

    private void definitions(boolean payloadMatchesManifest, Def... defs) {
        List<Object[]> manifest = new ArrayList<Object[]>();
        final List<FormDefinition> all = new ArrayList<FormDefinition>();
        final Map<Object, Collection<String>> idsByElement = new HashMap<Object, Collection<String>>();
        final Map<String, Form> formByJson = new HashMap<String, Form>();

        for (Def d : defs) {
            manifest.add(new Object[]{"app1", 1L, d.id, new java.util.Date(d.modified), d.length});
            FormDefinition fd = mock(FormDefinition.class);
            when(fd.getId()).thenReturn(d.id);
            when(fd.getJson()).thenReturn(d.json);
            when(fd.getAppId()).thenReturn("app1");
            when(fd.getAppVersion()).thenReturn(1L);
            when(fd.getDateModified()).thenReturn(new java.util.Date(
                    payloadMatchesManifest ? d.modified : d.modified - 60000L));
            all.add(fd);
            Form form = formByJson.get(d.json);
            if (form == null) { form = mock(Form.class); formByJson.put(d.json, form); }
            idsByElement.put(form, Arrays.asList(d.columns));
        }

        // doReturn/doAnswer, NOT when(...): re-stubbing with when() invokes the mock, which runs the
        // PREVIOUS answer with null arguments and blows up inside the old lambda.
        doReturn(manifest).when(formDefinitionDao).getFormDefinitionManifestByTableName(anyString());
        doReturn(all).when(formDefinitionDao).loadFormDefinitionByTableName(anyString());
        doAnswer(inv -> {
            Collection<String> wanted = inv.getArgument(1);
            List<FormDefinition> picked = new ArrayList<FormDefinition>();
            for (FormDefinition fd : all) {
                if (wanted != null && wanted.contains(fd.getId())) { picked.add(fd); }
            }
            return picked;
        }).when(formDefinitionDao).loadFormDefinitionByTableNameAndIds(anyString(), any());
        doAnswer(inv -> formByJson.get((String) inv.getArgument(0)))
                .when(formService).createElementFromJson(anyString(), anyBoolean());
        doReturn(new ArrayList()).when(builderDefinitionDao)
                .find(anyString(), any(), any(), any(), any(), any(), any());
        dao.stubIdsByElement = idsByElement;
    }

    /**
     * The manifest must survive the JDBC types Oracle and SQL Server actually return.
     *
     * <p>{@code app_form.appVersion} is {@code bigint} on MySQL and PostgreSQL (JDBC gives a Long)
     * but {@code NUMBER(19,0)} on Oracle and {@code numeric(19,0)} on SQL Server, where the driver
     * gives a {@code BigDecimal}. Casting the native-query result straight to {@code Long} threw
     * ClassCastException there — caught, latching MANIFEST_UNAVAILABLE, silently disabling
     * incremental rebuilds for the life of the JVM on exactly the two databases that are hardest to
     * test locally. Red before {@code asLong}/{@code asDate}.</p>
     *
     * <p>Also covers the SQL Server degradation: when {@code LENGTH()} is unsupported the DAO
     * re-issues the query with a NULL length term, so a null length must not break key building.</p>
     */
    @Test
    public void manifestToleratesTheNumericTypesOracleAndSqlServerReturn() {
        columnCacheIsCold();
        definitions(def("f1", "json1", 100L, 10, "title"));
        // Re-stub the manifest with Oracle/SQL Server shapes: BigDecimal version, java.sql.Timestamp
        // date, and a NULL length (the LENGTH()-unsupported fallback).
        List<Object[]> oracleShaped = new ArrayList<Object[]>();
        oracleShaped.add(new Object[]{"app1", new java.math.BigDecimal("1"), "f1",
                new java.sql.Timestamp(100L), null});
        doReturn(oracleShaped).when(formDefinitionDao).getFormDefinitionManifestByTableName(anyString());

        Collection<String> first = dao.getFormDefinitionColumnNames(TABLE);
        assertEquals("the full build must still work: " + first, 1, first.size());

        // And the base must be usable: a second call with the same manifest reads no JSON.
        dao.getFormDefinitionColumnNames(TABLE);
        verify(formDefinitionDao, times(1)).loadFormDefinitionByTableName(anyString());
    }

    // ------------------------------------------------- manifest failure is per-tenant and expiring

    /**
     * A manifest failure must suspend the incremental path for THAT TENANT ONLY.
     *
     * <p>This was a JVM-wide {@code AtomicBoolean}. On a multi-tenant node, one tenant whose
     * database rejected the projection — a different product or version, an unmigrated schema —
     * silently cost EVERY tenant the optimisation, permanently, behind a single log line.</p>
     *
     * <p>Falsifiable: key the backoff on anything JVM-wide and tenant B's second call does a full
     * read, so the {@code times(1)} below becomes {@code times(2)}.</p>
     */
    @Test
    public void aManifestFailureOnOneTenantDoesNotDisableTheOthers() {
        columnCacheIsCold();
        definitions(def("f1", "json1", 100L, 10, "title"));

        // Tenant A: the manifest query blows up.
        org.joget.commons.util.HostManager.setCurrentProfile("tenantA");
        doThrow(new RuntimeException("ORA-00904: invalid identifier"))
                .when(formDefinitionDao).getFormDefinitionManifestByTableName(anyString());
        dao.getFormDefinitionColumnNames(TABLE);

        // Tenant B: a healthy database. It must still get an incremental second build.
        org.joget.commons.util.HostManager.setCurrentProfile("tenantB");
        reset(formDefinitionDao);
        definitions(def("f1", "json1", 100L, 10, "title"));

        dao.getFormDefinitionColumnNames(TABLE);   // seeds tenant B's base
        dao.getFormDefinitionColumnNames(TABLE);   // must go incremental

        verify(formDefinitionDao, times(1)).loadFormDefinitionByTableName(anyString());
    }

    /**
     * The backoff must EXPIRE, so a transient failure self-heals.
     *
     * <p>A latch cannot tell a permanent grammar error from a {@code JDBCConnectionException}, so
     * one blip during a database failover used to cost the optimisation for the life of the JVM.
     * Falsifiable: make the backoff permanent again and the retry below never happens, so the
     * manifest is queried once rather than twice.</p>
     */
    @Test
    public void aTransientManifestFailureIsRetriedAfterTheBackoffWindow() {
        columnCacheIsCold();
        definitions(def("f1", "json1", 100L, 10, "title"));
        resolver.manifestRetryIntervalMs = 0L;   // expire immediately

        doThrow(new RuntimeException("connection reset"))
                .when(formDefinitionDao).getFormDefinitionManifestByTableName(anyString());
        dao.getFormDefinitionColumnNames(TABLE);

        // The database recovers.
        definitions(def("f1", "json1", 100L, 10, "title"));
        dao.getFormDefinitionColumnNames(TABLE);

        verify(formDefinitionDao, times(2)).getFormDefinitionManifestByTableName(anyString());
    }

    /** THE POINT: when nothing changed, no definition JSON is read at all. */
    @Test
    public void incremental_anUnchangedManifestReadsNoDefinitionJson() {
        columnCacheIsCold();
        definitions(def("f1", "json1", 100L, 10, "title"), def("f2", "json2", 100L, 20, "status"));

        dao.getFormDefinitionColumnNames(TABLE);
        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        verify(formDefinitionDao, times(1)).loadFormDefinitionByTableName(anyString());
        verify(formDefinitionDao, never()).loadFormDefinitionByTableNameAndIds(anyString(), any());
        assertEquals("the union must survive intact: " + result, 2, result.size());
    }

    /** Only the definition whose change key moved is re-read, and its new column appears. */
    @Test
    public void incremental_onlyTheChangedDefinitionIsReloaded() {
        columnCacheIsCold();
        definitions(def("f1", "json1", 100L, 10, "title"), def("f2", "json2", 100L, 20, "status"));
        dao.getFormDefinitionColumnNames(TABLE);

        definitions(def("f1", "json1", 100L, 10, "title"), def("f2", "json2b", 200L, 25, "status", "newcol"));
        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        ArgumentCaptor<Collection> ids = ArgumentCaptor.forClass(Collection.class);
        verify(formDefinitionDao, times(1)).loadFormDefinitionByTableNameAndIds(anyString(), ids.capture());
        assertEquals("only the changed definition may be re-read", 1, ids.getValue().size());
        assertTrue(ids.getValue().contains("f2"));
        assertTrue("the new column must reach the union: " + result, result.contains("newcol"));
    }

    /**
     * A second edit inside the same second must still be seen, even though the change key is
     * byte-identical.
     *
     * <p>MySQL stores {@code dateModified} as {@code datetime} — whole seconds — so two saves in the
     * same second produce the SAME key, and key comparison alone can never distinguish them. The
     * recency rule closes it: any definition modified within one granularity unit of the build start
     * is re-read regardless of its key, because its timestamp is not yet provably older than the
     * build.</p>
     *
     * <p>An earlier version of this test used a differing json LENGTH and so only ever exercised the
     * easy case, giving false confidence in exactly the scenario that was broken. Red without
     * {@code isRecentlyModified}.</p>
     */
    @Test
    public void incremental_aSameSecondEditIsStillDetectedViaRecency() {
        columnCacheIsCold();
        long now = System.currentTimeMillis();
        definitions(def("f1", "json1", now, 10, "title"));
        dao.getFormDefinitionColumnNames(TABLE);

        // Same timestamp AND same length: the change key is identical in every component.
        definitions(def("f1", "json1b", now, 10, "title", "newcol"));
        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertTrue("a same-second edit must not be missed: " + result, result.contains("newcol"));
    }

    /**
     * The companion guard: recency must not defeat the optimisation. A definition whose timestamp is
     * comfortably older than the build carries an unchanged key and must NOT be re-read — otherwise
     * every build degenerates into a full one and the feature is worthless.
     */
    @Test
    public void incremental_anOldUnchangedDefinitionIsStillNotReRead() {
        columnCacheIsCold();
        long wellInThePast = System.currentTimeMillis() - 3600000L;
        definitions(def("f1", "json1", wellInThePast, 10, "title"));

        dao.getFormDefinitionColumnNames(TABLE);
        dao.getFormDefinitionColumnNames(TABLE);

        verify(formDefinitionDao, times(1)).loadFormDefinitionByTableName(anyString());
        verify(formDefinitionDao, never()).loadFormDefinitionByTableNameAndIds(anyString(), any());
    }

    /**
     * Case-collapsing must survive the incremental path. Red against a plain set union: without the
     * carried-forward duplicate map BOTH spellings survive, and the mapping then emits c_Foo and
     * c_foo, which collide on a case-insensitive database.
     */
    @Test
    public void incremental_caseOnlyDuplicatesStillCollapseAcrossBuilds() {
        columnCacheIsCold();
        definitions(def("f1", "json1", 100L, 10, "Foo"));
        dao.getFormDefinitionColumnNames(TABLE);

        definitions(def("f1", "json1b", 200L, 11, "foo"));
        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertEquals("exactly one spelling may survive: " + result, 1, result.size());
        assertTrue(result.contains("foo"));
    }

    /** Additive by design: a vanished definition's columns linger until the next full build. */
    @Test
    public void incremental_aVanishedDefinitionsColumnsLingerByDesign() {
        columnCacheIsCold();
        definitions(def("f1", "json1", 100L, 10, "title"), def("f2", "json2", 100L, 20, "status"));
        dao.getFormDefinitionColumnNames(TABLE);

        definitions(def("f1", "json1", 100L, 10, "title"));
        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertTrue("additive by design: " + result, result.contains("status"));
    }

    /** A changed definition whose parse throws must be retried, not recorded as observed. */
    @Test
    public void incremental_aFailedParseIsRetriedOnTheNextBuild() {
        columnCacheIsCold();
        definitions(def("f1", "json1", 100L, 10, "title"));
        dao.getFormDefinitionColumnNames(TABLE);

        definitions(def("f1", "json1b", 200L, 15, "title", "newcol"));
        doThrow(new RuntimeException("transient plugin failure"))
                .when(formService).createElementFromJson(anyString(), anyBoolean());
        dao.getFormDefinitionColumnNames(TABLE);

        definitions(def("f1", "json1b", 200L, 15, "title", "newcol"));
        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertTrue("a failed parse must not be recorded as observed: " + result, result.contains("newcol"));
    }

    /**
     * A payload that does NOT match the manifest row it was loaded for must not be recorded.
     *
     * <p>The review case: the manifest is a native projection and always hits the database, but the
     * payload comes through a CACHEABLE query — {@code AbstractSpringDao.find} calls
     * {@code setCacheable} and Ignite sets {@code hibernate.cache.use_query_cache=true}. Recording a
     * FRESH change key against a STALE payload would latch that staleness forever, which is #2085
     * through a new door. Red without the cross-check.</p>
     */
    @Test
    public void incremental_aPayloadThatDisagreesWithTheManifestIsNotRecorded() {
        columnCacheIsCold();
        definitions(def("f1", "json1", 100L, 10, "title"));
        dao.getFormDefinitionColumnNames(TABLE);

        // Manifest reports the NEW revision; the payload query serves the OLD one, still without
        // "newcol". Without the cross-check this records the new change key against the old union.
        definitionsWithStalePayload(def("f1", "json1b", 200L, 15),
                                    def("f1", "json1", 100L, 10, "title"));
        dao.getFormDefinitionColumnNames(TABLE);

        // Cache now serves the correct payload. The definition must still count as CHANGED and be
        // re-read; if the stale read was recorded as observed, "newcol" is lost permanently.
        definitions(def("f1", "json1b", 200L, 15, "title", "newcol"));
        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertTrue("a stale payload must not latch: " + result, result.contains("newcol"));
    }

    /**
     * An incremental build that cannot confirm every changed definition must NOT publish its union —
     * it must fall back to the full read.
     *
     * <p>The earlier version of the cross-check withheld the unconfirmed key from the base but let
     * {@code publish()} run anyway, writing a union known to be missing that definition's columns
     * into {@code formColumnCache}. Nothing schedules the recorded retry, because the cache is only
     * rebuilt on eviction — so the code detected the staleness and then served it. Observable as a
     * second call to {@code loadFormDefinitionByTableName}: the full read that the fallback forces.
     * Red without the {@code merged.confirmed.containsAll(changedKeys)} guard.</p>
     */
    @Test
    public void incremental_anUnconfirmedResultFallsBackToTheFullBuildInsteadOfPublishing() {
        columnCacheIsCold();
        long old = System.currentTimeMillis() - 3600000L;
        definitions(def("f1", "json1", old, 10, "title"));
        dao.getFormDefinitionColumnNames(TABLE);
        verify(formDefinitionDao, times(1)).loadFormDefinitionByTableName(anyString());

        // Manifest reports a new revision; the payload query hands back the old one.
        definitionsWithStalePayload(def("f1", "json1b", old + 60000L, 15),
                                    def("f1", "json1", old, 10, "title"));
        dao.getFormDefinitionColumnNames(TABLE);

        verify(formDefinitionDao, times(2)).loadFormDefinitionByTableName(anyString());
        // And the fallback must not publish either. Only the FIRST build — the one whose payload
        // agreed with its manifest — is allowed to have written the cache. Without this line the
        // test says nothing about what was cached, which is the whole point of failing closed.
        verify(formColumnCache, times(1)).put(anyString(), any(), any());
    }

    /**
     * THE FULL BUILD IS NOT A SAFE HARBOUR. It must withhold a union the manifest contradicts.
     *
     * <p>Found by review. {@code incrementalBuild} correctly fails closed when it cannot confirm a
     * changed definition — but it falls back to {@code fullBuild}, which re-reads through the SAME
     * cacheable query ({@code AbstractSpringDao.find} calls {@code setCacheable}, and Ignite turns
     * the query cache on), so it can reproduce the very staleness that triggered the fallback and
     * then {@code publish()} it. Nothing would correct that entry: a warm entry is not rebuilt, and
     * the invalidation that caused this build is already older than it. Detecting #2085 and serving
     * it anyway.</p>
     *
     * <p>Uses a STATEFUL cache rather than {@link #columnCacheIsCold()} on purpose — with a
     * permanently-cold mock {@code publish()} is unobservable and this assertion cannot be made at
     * all, which is exactly how the defect survived the earlier round of tests.</p>
     *
     * <p>Falsifiable: drop the {@code isComplete} guard in {@code fullBuild} and the stale union is
     * cached, so {@code cached.get()} is non-null and this goes red.</p>
     */
    @Test
    public void fullBuild_aUnionThatContradictsTheManifestIsNotCached() {
        AtomicReference<Collection<String>> cached = statefulColumnCache();
        long old = System.currentTimeMillis() - 3600000L;

        definitions(def("f1", "json1", old, 10, "title"));
        dao.getFormDefinitionColumnNames(TABLE);
        assertNotNull("a build that agrees with its manifest must be cached", cached.get());

        // Another node edits the form and invalidates: this node's entry is evicted, its manifest
        // read sees the new revision, but every payload read still serves the old one.
        cached.set(null);
        definitionsWithStalePayload(def("f1", "json1b", old + 60000L, 15),
                                    def("f1", "json1", old, 10, "title"));
        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertNull("a union the manifest contradicts must not be cached: " + cached.get(),
                cached.get());
        // Returned, not thrown: a few seconds of ordinary post-edit incoherence must not fail every
        // request for this table. The caller gets what this node can see, and the next request —
        // finding the cache still cold — rebuilds and self-corrects.
        assertTrue("the caller still gets the columns this node could confirm: " + result,
                result.contains("title"));
    }

    /**
     * THE "SELF-CORRECTS ON THE NEXT REQUEST" ASSUMPTION HAS NO BACKSTOP. Found reviewing a field
     * report against 9.1-SNAPSHOT build 21723de (GitLab #2085): a customer on the cloud/cluster
     * build kept hitting the reported {@code UnknownPathException} on the SAME node that made the
     * edit, repeatedly, for a ~19-minute span with no server restart in between — far past both of
     * this fix's designed windows (the ≤1s database-verification throttle and the harness's
     * observed ~6-10s self-heal).
     *
     * <p>{@link #fullBuild_aUnionThatContradictsTheManifestIsNotCached} shows the union is withheld
     * once, and its own comment banks on "the next request... finding the cache still cold,
     * rebuilds and self-corrects." That is an assumption about the read underneath
     * {@code loadFormDefinitionByTableName} eventually returning the new revision — nothing in this
     * class enforces it. In production that read is genuinely Hibernate-cacheable:
     * {@code AbstractSpringDao.find} calls {@code setCacheable}, and Joget's Ignite region factory
     * ({@code wflow-cache-ignite}'s {@code IgniteHibernateRegionFactory}) turns query caching on for
     * every cloud/cluster deployment — the community default {@code NoInMemoryCacheManager} makes it
     * a no-op, which is why the community-only 2-node harness in
     * {@code skills/_shared/tests/issue2085/} only ever observes the short self-healing window.
     * If that Ignite-backed cache entry is not invalidated in step with the write — a delayed or
     * dropped {@code TimestampsRegion} update under load is the concrete mechanism read out of that
     * module, see {@code IgniteGeneralDataRegion.putIntoCache}'s async path, which silently swallows
     * an {@code IgniteFutureTimeoutException} when {@code wflow.igniteAsyncTimeout} is configured —
     * this loop has no second mechanism to fall back on: it is the SAME cacheable query on every
     * iteration, so "eventually" can mean never for the life of the process, i.e. until a restart
     * clears it. Five consecutive calls here, all against a payload that never changes, never once
     * see the new column and never publish anything for another node (or a later request) to
     * inherit — structurally reproducing "occurs until node restart" rather than "self-heals in a
     * few seconds".</p>
     *
     * <p><b>Evidence level: reasoned from code, not verified live.</b> A mocked
     * {@code FormDefinitionDao} cannot observe Hibernate's own query cache, so this test cannot
     * distinguish "the DAO call is slow to refresh" from "the DAO call is stubbed to never refresh"
     * — it pins the CALLER-visible symptom (the resolver has no retry-until-fresh backstop) and the
     * reasoning above for why a real Ignite-backed deployment can hit it, not the Ignite mechanics
     * themselves. The actual fix lives one layer below what this test can see:
     * {@code FormDefinitionDaoImpl.loadFormDefinitionByTableName} no longer participates in the
     * Hibernate query cache at all — it now reads directly, exactly like its sibling
     * {@code loadFormDefinitionByTableNameAndIds} already did, so the "full, authoritative rebuild"
     * this class falls back to is no longer able to be served the same stale row twice.</p>
     */
    @Test
    public void fullBuild_aPermanentlyStalePayloadIsNeverPickedUpNoMatterHowManyRetries() {
        columnCacheIsCold();
        long old = System.currentTimeMillis() - 3600000L;
        // The mock never catches up, on purpose: this is standing in for a Hibernate/Ignite query
        // cache entry that never gets invalidated, not for an ordinary transient race.
        definitionsWithStalePayload(def("f1", "json1b", old + 60000L, 15),
                                    def("f1", "json1", old, 10, "title"));

        for (int i = 0; i < 5; i++) {
            Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);
            assertTrue("call " + i + " must still return what this node could confirm: " + result,
                    result.contains("title"));
            assertFalse("a cacheable read that never invalidates must never surface the new "
                    + "column on call " + i + ", or the caller would wrongly conclude the mapping "
                    + "is already current: " + result, result.contains("newcol"));
        }
        // Never cached, on every single retry: a warm entry here is what would let this defect
        // latch permanently rather than merely repeat per-request.
        verify(formColumnCache, never()).put(anyString(), any(), any());
    }

    /**
     * The counterpart, and the reason {@code isComplete} tests DISAGREEMENT rather than "everything
     * parsed": a definition whose JSON is protected or corrupted has always been skipped, and it is
     * a property of the content, not of this build. Gating publication on it would leave such a
     * table uncached and rebuilding on every single request — #7709 restored.
     *
     * <p>Falsifiable: widen {@code isComplete} to require {@code confirmed} to cover the manifest
     * and this goes red.</p>
     */
    @Test
    public void fullBuild_aCorruptedDefinitionDoesNotBlockCaching() {
        AtomicReference<Collection<String>> cached = statefulColumnCache();
        long old = System.currentTimeMillis() - 3600000L;
        definitions(def("f1", "json1", old, 10, "title"));
        doThrow(new RuntimeException("protected or corrupted"))
                .when(formService).createElementFromJson(anyString(), anyBoolean());

        dao.getFormDefinitionColumnNames(TABLE);

        assertNotNull("an unparseable definition must not keep the table permanently uncached",
                cached.get());
    }

    /**
     * A NORMAL invalidation must NOT drop the base — that is the whole point of the feature.
     *
     * <p>Learned the hard way: coupling the base reset to every {@code FormColumnCache.remove} made
     * every form save a full rebuild, taking the 1000-definition edit case from 7.8 ms back to
     * 274 ms. The measurement caught it; the unit tests did not, so this pins it. A removal still
     * needs a full read, but that is handled at the two places a definition actually leaves a table
     * (retarget and delete), not on every eviction.</p>
     */
    @Test
    public void aNormalInvalidationKeepsTheBaseSoTheNextRebuildStaysIncremental() throws Exception {
        columnCacheIsCold();
        long old = System.currentTimeMillis() - 3600000L;
        definitions(def("f1", "json1", old, 10, "title"));

        dao.getFormDefinitionColumnNames(TABLE);   // seeds the base
        dao.getFormDefinitionColumnNames(TABLE);   // the invalidated rebuild

        // One full read, from seeding. The second went down the incremental path.
        verify(formDefinitionDao, times(1)).loadFormDefinitionByTableName(anyString());
    }

    /**
     * A build that could not confirm a definition must not record it as observed — and the base it
     * hands to the cache must say so.
     *
     * <p>The reset-during-an-in-flight-build guard that used to be asserted here now lives in
     * {@link FormColumnCache#putBase} and is tested in {@code FormColumnCacheTest}.</p>
     */
    @Test
    public void aBuildRecordsOnlyTheDefinitionsItSuccessfullyParsed() {
        columnCacheIsCold();
        long old = System.currentTimeMillis() - 3600000L;
        definitions(def("f1", "json1", old, 10, "title"));
        // The parse fails, so nothing may be recorded as observed.
        when(formService.createElementFromJson(anyString(), anyBoolean()))
                .thenThrow(new RuntimeException("corrupt"));

        dao.getFormDefinitionColumnNames(TABLE);

        FormColumnCache.ColumnBase base = bases.get(UNPREFIXED);
        assertTrue("a definition that failed to parse must be left unobserved so it is retried",
                base == null || base.observed.isEmpty());
    }

    /**
     * The base must expire, so drift cannot be served indefinitely.
     *
     * <p>The unconditional full rebuild used to give self-healing for free: any transiently bad read
     * was corrected on the next eviction. An incremental base latches what it observed, so it needs
     * an explicit bound. Red without {@code ColumnBase#isExhausted}.</p>
     */
    @Test
    public void incremental_theBaseExpiresAndForcesAFullRebuild() {
        columnCacheIsCold();
        definitions(def("f1", "json1", 100L, 10, "title"));

        int max = Integer.getInteger("wflow.formColumnIncrementalMaxBuilds", 100);
        for (int i = 0; i <= max + 1; i++) {
            dao.getFormDefinitionColumnNames(TABLE);
        }

        // One seeding full build, plus at least one more once the base exhausted itself.
        verify(formDefinitionDao, atLeast(2)).loadFormDefinitionByTableName(anyString());
    }

    /** The columns FormRow.hbm.xml already declares must never be emitted as custom columns. */
    @Test
    public void buildExcludesPredefinedColumns() {
        columnCacheIsCold();
        formDefsWithJson("{\"className\":\"org.joget.apps.form.model.Form\"}");
        dao.stubElementIds = Arrays.asList(
                "id", "dateCreated", "dateModified", "createdBy", "createdByName",
                "modifiedBy", "modifiedByName", "title");

        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertEquals("only the non-predefined column may survive: " + result, 1, result.size());
        assertTrue(result.contains("title"));
    }

    // ------------------------------------------------------- nested mapping regeneration (review)

    /** The {@code MAP:} guard key {@code createSessionFactoryWithSingleFlight} derives internally. */
    private static String mapKey() {
        return "MAP:" + org.joget.commons.util.HostManager.getCurrentProfile() + ":" + TABLE;
    }

    private SessionFactory regenerate() {
        return dao.createSessionFactoryWithSingleFlight(TABLE, TABLE, null,
                FormDataDaoImpl.ACTION_TYPE_LOAD, "sf_" + TABLE);
    }

    /** Runs {@code body} as if reached from inside a column build for some OTHER table. */
    private void asNestedCall(Runnable body) {
        FormBuildGuard.<Object>runGuarded("COL:" + org.joget.commons.util.HostManager.getCurrentProfile()
                + ":someOtherTable", () -> {
            body.run();
            return null;
        });
    }

    /**
     * A NESTED regeneration must not write the mapping file while another thread holds its lock.
     *
     * <p>Found by review. The nested branch used to return {@code createSessionFactory} directly —
     * no lock at all — which contradicted this method's own timeout branch, where the reason given
     * for failing rather than proceeding is that "concurrent writers would corrupt the mapping
     * file". The nested path is reachable in production: a column build parses form JSON, which runs
     * plugin code, which can call {@code find()} and land back in mapping regeneration. So a plugin
     * could truncate and rewrite {@code <entity>.hbm.xml} underneath the thread parsing it.</p>
     *
     * <p>Falsifiable: restore {@code if (isNested()) return createSessionFactory(...)} and the call
     * regenerates instead of throwing, so both assertions here go red.</p>
     */
    @Test
    public void mapping_aNestedRegenerationWillNotWriteTheMappingFileWithoutTheLock() throws Exception {
        final CountDownLatch held = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        Thread holder = new Thread(() -> {
            java.util.concurrent.locks.ReentrantLock l = FormBuildGuard.tryLock(mapKey(), 5);
            held.countDown();
            try {
                release.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            l.unlock();
        });
        holder.start();
        assertTrue("the holder thread never took the lock", held.await(5, TimeUnit.SECONDS));

        try {
            asNestedCall(() -> regenerate());
            fail("a nested regeneration must not proceed while another thread holds the mapping lock");
        } catch (HibernateException expected) {
            // The caller is told to retry rather than being silently allowed to race the writer.
        } finally {
            release.countDown();
            holder.join(5000);
        }

        assertEquals("the mapping must not have been regenerated", 0, rebuildCount.get());
    }

    /**
     * The other half: the fix must not break the nested path when nothing is contending. A nested
     * caller never WAITS, but a free lock is taken immediately and the work proceeds.
     *
     * <p>Without this, "never regenerate while nested" would be an equally passing implementation of
     * the test above, and it would break every plugin that reaches a second table.</p>
     */
    @Test
    public void mapping_aNestedRegenerationStillProceedsWhenNothingHoldsTheLock() {
        // Nothing usable is cached. Without this the double-check legitimately returns the fixture's
        // still-open factory and no regeneration is needed, so the assertion would be measuring the
        // fixture rather than the lock.
        sfRef.set(null);

        asNestedCall(() -> regenerate());

        assertEquals("an uncontended nested regeneration must still run", 1, rebuildCount.get());
    }

    /**
     * Re-entering regeneration for the SAME entity must be rejected, not recursed into.
     *
     * <p>The stripe is a {@link java.util.concurrent.locks.ReentrantLock}, so the lock alone cannot
     * catch this — the same thread re-acquires it happily and rewrites the mapping file underneath
     * the frame that is still parsing it. Only the guard stack can.</p>
     *
     * <p>Falsifiable: delete the {@code FormBuildGuard.holds(key)} check and the re-entrant call
     * regenerates, so {@code rebuildCount} becomes 1 and {@code fail()} is reached.</p>
     */
    @Test
    public void mapping_aReEntrantRegenerationIsRejectedRatherThanRecursing() {
        try {
            FormBuildGuard.<Object>runGuarded(mapKey(), () -> {
                regenerate();
                return null;
            });
            fail("re-entrant regeneration of the same entity must be rejected");
        } catch (HibernateException expected) {
            // Retriable: by the time the caller retries, the outer regeneration has completed.
        }

        assertEquals("the mapping must not have been regenerated re-entrantly", 0, rebuildCount.get());
    }

    /**
     * The manifest key must be readable off an UNINITIALISED {@code AppDefinition} proxy.
     *
     * <p>This is the whole point of {@code manifestKeyFor}: {@code getAppId()} and
     * {@code getAppVersion()} both dereference {@code FormDefinition}'s lazy many-to-one, so calling
     * either one per definition reinstates the N+1 that the lazy {@code Supplier} in
     * {@code mergeFormDefinitions} exists to avoid — measured at 1000 definitions as 2000 entity
     * loads and 1003 statements, about 45% of a full rebuild.</p>
     *
     * <p>Found by review: the check used to be {@code id instanceof AppDefinition}, but
     * {@code AppDefinition.hbm.xml} declares {@code composite-id class="AppDefinitionKey"}, and
     * {@code AppDefinitionKey} is a SIBLING of {@code AppDefinition} under
     * {@code AbstractVersionedObject} — not a subclass. So the fast path never fired in production
     * and the proxy was initialised anyway. Mocks hid it completely, because a mocked
     * {@code FormDefinition} answers {@code getAppId()} without any proxy at all.</p>
     *
     * <p><b>The key object is populated the way the MAPPING populates it</b>, which is the detail
     * that made the first version of this test worthless: it called {@code setId("app1")}, but
     * {@code AppDefinition.hbm.xml} declares {@code <key-property column="appId" name="appId"/>},
     * so Hibernate sets {@code appId} and leaves {@code id} null. The test passed while production
     * built keys of {@code null:1:formId} — caught only by the benchmark, as builds reporting
     * "0 stale, 0 missing" that were still judged incomplete, with the column cache never
     * populated. A fixture that populates an object differently from the framework under test
     * proves nothing about the framework.</p>
     *
     * <p>Falsifiable twice over: narrow the check back to {@code instanceof AppDefinition}, or read
     * {@code getId()} instead of {@code getAppId()}, and this goes red.</p>
     */
    @Test
    public void manifestKeyIsReadOffTheProxyIdentifierWithoutInitialisingIt() {
        org.joget.apps.app.model.AppDefinitionKey identifier =
                new org.joget.apps.app.model.AppDefinitionKey();
        // Exactly what <key-property name="appId"/> and <key-property name="version"/> set, and
        // deliberately NOT setId(): the mapping never calls it, so neither may this fixture.
        identifier.setAppId("app1");
        identifier.setVersion(7L);

        org.hibernate.proxy.LazyInitializer initializer = mock(org.hibernate.proxy.LazyInitializer.class);
        when(initializer.getIdentifier()).thenReturn(identifier);
        org.joget.apps.app.model.AppDefinition proxy = mock(org.joget.apps.app.model.AppDefinition.class,
                withSettings().extraInterfaces(org.hibernate.proxy.HibernateProxy.class));
        when(((org.hibernate.proxy.HibernateProxy) proxy).getHibernateLazyInitializer())
                .thenReturn(initializer);

        FormDefinition formDef = mock(FormDefinition.class);
        when(formDef.getId()).thenReturn("f1");
        when(formDef.getAppDefinition()).thenReturn(proxy);

        assertEquals("app1:7:f1", FormColumnResolver.manifestKeyFor(formDef));
        verify(formDef, never()).getAppId();
        verify(formDef, never()).getAppVersion();
    }

    /**
     * A key-derivation defect must degrade to "cannot verify", never to "never cache anything".
     *
     * <p>The safety net exists because of what actually happened: {@code manifestKeyFor} read the
     * wrong property off the composite key, so no loaded definition's key matched any manifest key,
     * {@code isComplete} was false on every build, and the column cache was never populated —
     * #7709 restored across every table. The unit suite could not see it (it mocks the DAO, so both
     * sides derive keys from the same stubs) and the benchmark did: 7.4 ms became 214 ms.</p>
     *
     * <p>So when the two key sets are entirely disjoint — rows were read, rows were listed, nothing
     * corresponds — that is this class being wrong about identity, not the database being stale.
     * Publish, and log an error loudly enough to be found.</p>
     *
     * <p>Falsifiable: remove the disjoint branch from {@code isComplete} and this goes red.</p>
     */
    @Test
    public void aTotalKeyMismatchDoesNotSilentlyDisableTheColumnCache() {
        FormColumnResolver.DefinitionManifest manifest = new FormColumnResolver.DefinitionManifest();
        manifest.entries.put("app1:1:f1", new FormColumnResolver.ManifestEntry("f1", 100L));

        FormColumnResolver.MergeResult merged = new FormColumnResolver.MergeResult();
        merged.seen.add("null:1:f1");      // what a wrong derivation produces
        merged.confirmed.add("null:1:f1");

        assertTrue("a derivation defect must not turn into a permanently uncached table",
                resolver.isComplete(manifest, merged));
    }

    /** The net must not swallow genuine staleness: overlapping keys with one stale row still fail. */
    @Test
    public void aPartialKeyOverlapIsStillTreatedAsStaleness() {
        FormColumnResolver.DefinitionManifest manifest = new FormColumnResolver.DefinitionManifest();
        manifest.entries.put("app1:1:f1", new FormColumnResolver.ManifestEntry("f1", 100L));
        manifest.entries.put("app1:1:f2", new FormColumnResolver.ManifestEntry("f2", 100L));

        FormColumnResolver.MergeResult merged = new FormColumnResolver.MergeResult();
        merged.seen.add("app1:1:f1");
        merged.confirmed.add("app1:1:f1");

        assertFalse("a row the manifest listed but the payload never returned is real staleness",
                resolver.isComplete(manifest, merged));
    }

    // ------------------------------------------------------- same-second double save (provisional)

    /**
     * TWO SAVES INSIDE ONE TIMESTAMP BUCKET, with a rebuild in between, must not lose the second one.
     *
     * <p>{@code app_form.dateModified} is {@code datetime} on MySQL — whole seconds — so both saves
     * store the identical value and the change key does not move. The recency backstop does not help
     * either: it compares against the LATER rebuild's start time, and that rebuild waits for the next
     * request, so it is normally more than a second after the edit. Without the provisional rule the
     * second save's column is invisible until the base expires (up to 15 minutes), and during that
     * window this is #2085 exactly — {@code UnknownPathException} on any HQL naming the column, and
     * the value silently not saved.</p>
     *
     * <p><b>No clock skew is required</b>, which is the correction that prompted this fix: the hole
     * was previously recorded as needing a cross-node clock more than a second out.</p>
     *
     * <p>The granularity is shrunk to 50 ms so the boundary can be crossed with a short sleep rather
     * than a one-second one; the logic under test is identical at either scale.</p>
     *
     * <p>Falsifiable: drop {@code unproven} from the changed-key test in {@code incrementalBuild} and
     * {@code newcol} never appears.</p>
     */
    @Test
    public void incremental_aSecondSaveInTheSameTimestampBucketIsStillDetected() throws Exception {
        columnCacheIsCold();
        resolver.granularityMs = 50L;
        long now = System.currentTimeMillis();

        // Observed INSIDE its own bucket: the build happens within 50 ms of the write, so the stored
        // timestamp cannot yet be trusted to be final.
        definitions(def("f1", "json1", now, 10, "title"));
        dao.getFormDefinitionColumnNames(TABLE);

        // The second save: different json, IDENTICAL dateModified, because it landed in the same
        // bucket. Change-key comparison cannot see this.
        definitions(def("f1", "json1b", now, 15, "title", "newcol"));
        // Past the bucket, so the recency rule is now silent and only the recorded provisional
        // verdict can force the re-read.
        Thread.sleep(80);
        Collection<String> result = dao.getFormDefinitionColumnNames(TABLE);

        assertTrue("a second save inside the same timestamp bucket must still be re-read: " + result,
                result.contains("newcol"));
    }

    /**
     * …and the extra re-read must happen ONCE, not on every rebuild.
     *
     * <p>This is what makes the fix affordable. The verdict is recorded in the base, so once an
     * observation has been made after the bucket closed the definition is never re-read again on that
     * account — the steady-state property the incremental path is built on, that an unchanged table
     * reads no definition payloads at all, is preserved. A version that recomputed stability from the
     * manifest's own newest timestamp instead (skew-proof, but never able to prove the NEWEST
     * definition stable) re-read one row on every single rebuild and broke three existing tests;
     * this is the trade that was chosen and it deserves a guard.</p>
     *
     * <p>Falsifiable: make {@code isTimestampStable} always return false and the final build re-reads
     * a definition instead of none.</p>
     */
    @Test
    public void incremental_theProvisionalReReadHappensOnceNotEveryRebuild() throws Exception {
        columnCacheIsCold();
        resolver.granularityMs = 50L;
        long now = System.currentTimeMillis();

        definitions(def("f1", "json1", now, 10, "title"));
        dao.getFormDefinitionColumnNames(TABLE);          // observes inside the bucket -> provisional

        Thread.sleep(80);
        definitions(def("f1", "json1", now, 10, "title"));
        dao.getFormDefinitionColumnNames(TABLE);          // re-reads once, and records it as stable

        clearInvocations(formDefinitionDao);
        definitions(def("f1", "json1", now, 10, "title"));
        dao.getFormDefinitionColumnNames(TABLE);          // nothing left to prove

        verify(formDefinitionDao, never()).loadFormDefinitionByTableNameAndIds(anyString(), any());
        verify(formDefinitionDao, never()).loadFormDefinitionByTableName(anyString());
    }

    // ------------------------------------------------------- concurrency cost of the mapping lock

    /**
     * THE SAFETY PROPERTY: mapping regeneration must never overlap, under any amount of contention.
     *
     * <p>This is what {@code createSessionFactoryWithSingleFlight} exists for. Regeneration truncates
     * and rewrites {@code <entity>.hbm.xml} and then runs {@code SchemaUpdate} DDL, so two
     * simultaneous regenerations of one entity mean a second writer truncating the file the first is
     * parsing. The assertion is therefore not "how many rebuilds" but "never two at once" — measured
     * directly, by having the stubbed regeneration record its own concurrency.</p>
     *
     * <p>Half the threads enter through a NESTED guard, which is the path that changed: a plugin
     * reached from a column build lands back here, and that caller is not allowed to wait. Before the
     * fix it proceeded with no lock at all, which is exactly the overlap this asserts against.</p>
     *
     * <p>It also QUANTIFIES the cost of failing closed, which is the open question the unit suite
     * could not answer: threads that cannot take the lock get a retriable {@code HibernateException}
     * instead of proceeding unsafely. The count is printed rather than asserted against a threshold —
     * it is a property of timing, not of correctness — but every thread must end in exactly one of
     * the two states, and at least one must succeed, or "fail closed" would just mean "fail".</p>
     */
    @Test
    public void mapping_regenerationNeverOverlapsUnderConcurrency() throws Exception {
        final int threads = 16;
        final int perThread = 25;
        final AtomicInteger inFlight = new AtomicInteger();
        final AtomicInteger maxInFlight = new AtomicInteger();
        final AtomicInteger succeeded = new AtomicInteger();
        final AtomicInteger retriable = new AtomicInteger();
        final AtomicInteger unexpected = new AtomicInteger();

        // Every regeneration records how many are running at once. This is the measurement; the
        // rest of the test just generates contention.
        dao = new RecordingDao() {
            @Override
            protected SessionFactory createSessionFactory(String entityName, String tableName,
                    FormRowSet rowSet, int actionType) {
                int now = inFlight.incrementAndGet();
                maxInFlight.accumulateAndGet(now, Math::max);
                try {
                    Thread.sleep(2);   // widen the window a real file write would occupy
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    inFlight.decrementAndGet();
                }
                rebuildCount.incrementAndGet();
                return rebuiltSessionFactory;
            }
        };
        dao.setColumnResolver(resolver);
        dao.setFormColumnCache(formColumnCache);
        dao.setFormDefinitionDao(formDefinitionDao);
        dao.setBuilderDefinitionDao(builderDefinitionDao);
        dao.setFormService(formService);
        inject(dao, "formSessionFactoryCache", sessionFactoryCache);
        inject(dao, "formPersistentClassCache", persistentClassCache);
        Cache joinCache = mock(Cache.class);
        when(joinCache.iterator()).thenReturn(new ArrayList<Cache.Entry>().iterator());
        inject(dao, "joinFormSessionFactoryCache", joinCache);
        // Nothing cached, so every call must regenerate — maximum contention.
        sfRef.set(null);

        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            final boolean nested = (t % 2 == 0);
            new Thread(() -> {
                try {
                    start.await(5, TimeUnit.SECONDS);
                    for (int i = 0; i < perThread; i++) {
                        try {
                            if (nested) {
                                asNestedCall(() -> regenerate());
                            } else {
                                regenerate();
                            }
                            succeeded.incrementAndGet();
                        } catch (HibernateException expected) {
                            // "cannot wait", "timed out", or re-entrancy: all retriable by contract.
                            retriable.incrementAndGet();
                        } catch (Throwable other) {
                            unexpected.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }
        start.countDown();

        assertTrue("threads did not finish — a deadlock or an unbounded wait",
                done.await(60, TimeUnit.SECONDS));

        int total = threads * perThread;
        // The retry PERCENTAGE here is a property of this test, not a prediction. Eight threads hold
        // the stripe essentially continuously, so the eight nested threads — which by contract must
        // never wait — lose every time. Read it as "under a saturated lock, nested callers always
        // retry and nothing overlaps", and read
        // mapping_aNestedRegenerationStillProceedsWhenNothingHoldsTheLock for the uncontended case.
        // A production-shaped rate needs the load test, not this.
        System.out.println("[mapping lock] " + total + " attempts under saturation: " + succeeded.get()
                + " regenerated, " + retriable.get() + " told to retry ("
                + (retriable.get() * 100 / total) + "%), max " + maxInFlight.get()
                + " concurrent regeneration(s)");

        assertEquals("no exception other than the retriable HibernateException may escape",
                0, unexpected.get());
        assertEquals("EVERY attempt must end either regenerated or retriable",
                total, succeeded.get() + retriable.get());
        assertTrue("failing closed must not mean failing always", succeeded.get() > 0);
        assertEquals("mapping regeneration overlapped — concurrent writers to <entity>.hbm.xml, "
                + "which is what the lock exists to prevent", 1, maxInFlight.get());
    }
}
