package org.joget.apps.form.model;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.collections.map.LRUMap;
import static org.joget.apps.form.dao.FormDataDaoImpl.FORM_PREFIX_TABLE_NAME;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.LongTermCache;

/**
 * Cache to store list of form column definitions for each table name i.e. tableName -> collection of columns
 *
 * <p>Owns every piece of per-table column cache state: the shared {@link LongTermCache} entries
 * (columns and indexes), the throttle for verifying a warm entry against the database, and the
 * node-local {@link ColumnBase} that lets {@code FormDataDaoImpl} rebuild incrementally. Keeping
 * them together is what makes "what does invalidation mean" a single decision — see
 * {@link #remove} versus {@link #reset}.</p>
 */
public class FormColumnCache {


    private LongTermCache cache;

    /**
     * How long a warm entry may be trusted before its CACHE_LAST_CLEAR_ flag is re-read from the
     * database. Negative disables the check and restores pre-fix behaviour, as a support escape
     * hatch. See {@link #getVerified}.
     */
    protected static final long VERIFY_INTERVAL_MS =
            Long.getLong("wflow.formColumnStaleCheckIntervalMs", 1000L);

    /** Force a full rebuild after this many incremental ones. See {@link ColumnBase#isExhausted}. */
    protected static final int BASE_MAX_INCREMENTAL_BUILDS =
            Integer.getInteger("wflow.formColumnIncrementalMaxBuilds", 100);

    /** Force a full rebuild once the base is this old. See {@link ColumnBase#isExhausted}. */
    protected static final long BASE_MAX_AGE_MS =
            Long.getLong("wflow.formColumnIncrementalMaxAgeMs", 900000L);

    /**
     * Bounded, node-local state per cache key: the incremental base, when it was last discarded,
     * and when the entry was last verified against the database.
     *
     * <p>Not in the {@link LongTermCache} itself, deliberately: an entry's eviction there is what
     * triggers a rebuild, so a base living alongside it would vanish exactly when it is needed.
     * Keyed by {@link #getCacheKey} all the same, so the base can never be scoped to a different
     * tenant than the columns it was derived from.</p>
     *
     * <p>Guarded by its own monitor — {@link LRUMap} is not thread safe, and the callers'
     * per-table build locks serialise builds, not access to this shared map.</p>
     */
    private final Map tableStates = new LRUMap(2000);

    public void setCache(LongTermCache cache) {
        this.cache = cache;
    }

    public Collection<String> get(String tableName) {
        String cacheKey = getCacheKey(tableName);
        Collection<String> columnList = (Collection<String>)cache.getObject(cacheKey);
        return columnList;
    }

    /**
     * Like {@link #get(String)}, but a warm entry is periodically verified against the database
     * rather than against the ~20 second debounced setup cache.
     *
     * <p>A warm entry is not proof of freshness: for ~20-25 seconds after a form change on another
     * node, the invalidation flag has not reached this node and this cache cheerfully reports an
     * OLD column set as current. That set then matches the OLD Hibernate mapping, so the stale
     * mapping is served and any HQL naming a new column fails — the widest window of #2085.</p>
     *
     * <p>Verification is throttled to one database read per table per {@link #VERIFY_INTERVAL_MS},
     * which is also the residual staleness window. Without the throttle it would run
     * O(columns x joined tables) times per datalist request, because that is how often
     * {@code AdvancedFormRowDataListBinder.getColumnName} asks for the column set; one indexed row
     * read is cheap, but not that cheap.</p>
     */
    public Collection<String> getVerified(String tableName) {
        Collection<String> columnList = get(tableName);
        if (columnList != null && isVerificationDue(tableName)) {
            columnList = getFresh(tableName);
        }
        return columnList;
    }

    /** Unthrottled database-verified read. {@link #getVerified} is what callers want. */
    public Collection<String> getFresh(String tableName) {
        return (Collection<String>) cache.getObjectFresh(getCacheKey(tableName));
    }

    protected boolean isVerificationDue(String tableName) {
        if (VERIFY_INTERVAL_MS < 0) {
            return false;
        }
        long now = System.currentTimeMillis();
        synchronized (tableStates) {
            TableState state = state(getCacheKey(tableName));
            if ((now - state.lastVerified) < VERIFY_INTERVAL_MS) {
                return false;
            }
            state.lastVerified = now;
            return true;
        }
    }

    public void put(String tableName, Collection<String> columnList) {
        put(tableName, columnList, null);
    }

    public void put(String tableName, Collection<String> columnList, Date requestTime) {
        if (columnList != null) {
            String cacheKey = getCacheKey(tableName);
            cache.putObject(cacheKey, columnList, requestTime);
        } else {
            remove(tableName);
        }
    }

    public Collection<String> getIndexes(String tableName) {
        String cacheKey = getIndexCacheKey(tableName);
        Collection<String> indexesList = (Collection<String>)cache.getObject(cacheKey);
        return indexesList;
    }

    public void putIndexes(String tableName, Collection<String> indexesList) {
        putIndexes(tableName, indexesList, null);
    }

    public void putIndexes(String tableName, Collection<String> indexesList, Date requestTime) {
        if (indexesList != null) {
            String cacheKey = getIndexCacheKey(tableName);
            cache.putObject(cacheKey, indexesList, requestTime);
        } else {
            remove(tableName);
        }
    }

    /**
     * Invalidates the cached columns and indexes, KEEPING the incremental base.
     *
     * <p>This is the ordinary "a definition changed" invalidation, and the base is exactly what
     * makes the following rebuild cheap: it re-reads only the definitions whose change key moved.
     * Dropping the base here instead would make every form save a full rebuild — measured at 1000
     * definitions, 7.4 ms becomes 274 ms. Use {@link #reset} when a definition has LEFT the table.</p>
     */
    public void remove(String tableName) {
        String cacheKey = getCacheKey(tableName);
        cache.remove(cacheKey);
        String indexCacheKey = getIndexCacheKey(tableName);
        cache.remove(indexCacheKey);
    }

    /**
     * Forgets everything about a table: the cached columns and indexes AND the incremental base.
     *
     * <p>Required whenever a definition has been REMOVED from the table (a form retargeted away, a
     * form deleted) and for the operator's explicit cache clear. The incremental rebuild is
     * additive — it can add columns but never remove them — so evicting the entry alone would just
     * re-derive the same union from the surviving base, and the departed form's columns would never
     * disappear. This is the only route back to an exact column set.</p>
     */
    public void reset(String tableName) {
        remove(tableName);
        String cacheKey = getCacheKey(tableName);
        synchronized (tableStates) {
            TableState state = state(cacheKey);
            state.base = null;
            state.lastVerified = 0;
            // Remembered so a build already in flight cannot put the base back: without it, a build
            // that started before the reset finishes after it and stores a base derived from the
            // very union the caller asked to discard.
            state.resetAt = System.currentTimeMillis();
        }
    }

    /** The incremental rebuild base for a table, or null if this node has none. */
    public ColumnBase getBase(String tableName) {
        synchronized (tableStates) {
            TableState state = (TableState) tableStates.get(getCacheKey(tableName));
            return (state != null) ? state.base : null;
        }
    }

    /**
     * Records what a build observed, for the next one to diff against.
     *
     * @param requestTime when the build that produced this base STARTED. A base derived before the
     *                    last {@link #reset} is discarded rather than stored.
     */
    public void putBase(String tableName, ColumnBase base, Date requestTime) {
        String cacheKey = getCacheKey(tableName);
        synchronized (tableStates) {
            TableState state = state(cacheKey);
            if (requestTime != null && state.resetAt >= requestTime.getTime()) {
                return;
            }
            state.base = base;
        }
    }

    /** Caller must hold the {@code tableStates} monitor. */
    private TableState state(String cacheKey) {
        TableState state = (TableState) tableStates.get(cacheKey);
        if (state == null) {
            state = new TableState();
            tableStates.put(cacheKey, state);
        }
        return state;
    }

    protected String getCacheKey(String tableName) {
        // strip table prefix
        if (tableName != null && tableName.startsWith(FORM_PREFIX_TABLE_NAME)) {
            tableName = tableName.substring(FORM_PREFIX_TABLE_NAME.length());
        }
        String cacheKey = DynamicDataSourceManager.getCurrentProfile() + "_" + "FORM_COLUMNS_" + tableName;
        return cacheKey;
    }

    protected String getIndexCacheKey(String tableName) {
        // strip table prefix
        if (tableName != null && tableName.startsWith(FORM_PREFIX_TABLE_NAME)) {
            tableName = tableName.substring(FORM_PREFIX_TABLE_NAME.length());
        }
        String cacheKey = DynamicDataSourceManager.getCurrentProfile() + "_" + "FORM_INDEXES_" + tableName;
        return cacheKey;
    }

    public void clear() {
        String prefix = DynamicDataSourceManager.getCurrentProfile() + "_" + "FORM_COLUMNS_";
        // #2261: this method is already whitelisted by caller-frame identity
        // (MultiTenantSecurityManager.WHITELIST_RESTRICTED_CLASSES has
        // "org.joget.apps.form.model.FormColumnCache.clear"), so a restricted caller was never
        // actually blocked here -- but that whitelist entry trusts this frame UNCONDITIONALLY,
        // with no check at all on whether the prefix it enumerates by could collide with another
        // real tenant's own entries. removeAllForCurrentProfile(..) adds that check (see its own
        // javadoc): it builds the identical "<profile>_FORM_COLUMNS_"/"<profile>_FORM_INDEXES_"
        // prefix this method always used, but refuses instead of removing anything if that prefix
        // cannot be proven collision-safe. Its own boolean return distinguishes "genuinely
        // cleared" from "refused" so the node-local tableStates cleanup below stays consistent
        // with the cache instead of always running regardless -- clearing tableStates when the
        // cache itself was NOT cleared would let the next build rebuild on top of entries that
        // were never actually evicted.
        boolean columnsCleared = cache.removeAllForCurrentProfile("FORM_COLUMNS_");
        boolean indexesCleared = cache.removeAllForCurrentProfile("FORM_INDEXES_");
        if (!columnsCleared || !indexesCleared) {
            LogUtil.warn(FormColumnCache.class.getName(),
                    "clear() could not fully clear the shared cache for prefix `" + prefix
                            + "` -- leaving node-local state (and CACHE_LAST_CLEAR_ notification to "
                            + "other nodes) untouched to stay consistent; the next read may serve a "
                            + "stale column/index set until this profile's own collision with "
                            + "another real tenant profile is resolved.");
            return;
        }
        // Same reasoning as reset(): a surviving base would let the next build rebuild ON TOP of
        // what was just discarded. Scoped to the current profile, like the eviction above — one
        // tenant clearing its cache must not cost every other tenant a full rebuild.
        synchronized (tableStates) {
            Iterator keys = tableStates.keySet().iterator();
            while (keys.hasNext()) {
                if (String.valueOf(keys.next()).startsWith(prefix)) {
                    keys.remove();
                }
            }
        }
    }

    /** Node-local, per cache key. Not shared across nodes and never required for correctness. */
    private static class TableState {
        ColumnBase base;
        long resetAt;
        long lastVerified;
    }

    /**
     * What a node knew about a table's form definitions the last time it built the column union.
     *
     * <p>{@code observed} maps a definition's identity to the {@code dateModified} this node last
     * parsed for it. Comparison is by EQUALITY against the database's current value — never against
     * a clock reading. Both sides originate in the database, so no node's clock is ever compared
     * with another's and cross-node skew cannot hide an edit.</p>
     */
    public static class ColumnBase {
        public final Map<String, Long> observed;
        /**
         * Keys whose {@code observed} timestamp is not yet PROVEN stable, and must therefore be
         * re-read on the next build even though their change key has not moved.
         *
         * <p>A stored {@code dateModified} is only conclusive once it can no longer gain a second
         * write without changing: {@code app_form.dateModified} is {@code datetime} on MySQL, i.e.
         * whole seconds, so two saves inside one second leave it byte-identical. If this node
         * observed a definition DURING that second, a later save in the same second is invisible to
         * change-key comparison forever — the #2085 symptom, for up to the base's lifetime.</p>
         *
         * <p>Proof of stability uses only values that came from the DATABASE — never this node's
         * clock — so no cross-node skew can enter the decision. See
         * {@code FormColumnResolver.storeBase}.</p>
         */
        public final java.util.Set<String> provisional;
        public final Collection<String> union;
        public final Map<String, String> duplicateMap;
        /** When the last FULL build happened, and how many incremental builds have run since. */
        public final long fullBuiltAt;
        public final int incrementalCount;

        public ColumnBase(Map<String, Long> observed, Collection<String> union, Map<String, String> duplicateMap,
                long fullBuiltAt, int incrementalCount) {
            this(observed, java.util.Collections.<String>emptySet(), union, duplicateMap,
                    fullBuiltAt, incrementalCount);
        }

        public ColumnBase(Map<String, Long> observed, java.util.Set<String> provisional,
                Collection<String> union, Map<String, String> duplicateMap,
                long fullBuiltAt, int incrementalCount) {
            this.observed = observed;
            this.provisional = provisional;
            this.union = union;
            this.duplicateMap = duplicateMap;
            this.fullBuiltAt = fullBuiltAt;
            this.incrementalCount = incrementalCount;
        }

        /**
         * Whether this base has drifted far enough from a known-exact state to be distrusted.
         *
         * <p>Without a bound, a base latches whatever it once observed. That matters because the
         * payload load goes through a cacheable query — {@code AbstractSpringDao.find} calls
         * {@code setCacheable}, and Ignite sets {@code hibernate.cache.use_query_cache=true} — so a
         * transiently stale read can be recorded against a FRESH change key and then re-served
         * forever. Re-reading everything periodically restores the self-healing property that the
         * unconditional full rebuild used to provide for free.</p>
         */
        public boolean isExhausted() {
            return incrementalCount >= BASE_MAX_INCREMENTAL_BUILDS
                    || (System.currentTimeMillis() - fullBuiltAt) >= BASE_MAX_AGE_MS;
        }
    }
}
