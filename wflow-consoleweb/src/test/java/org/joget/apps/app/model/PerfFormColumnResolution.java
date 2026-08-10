package org.joget.apps.app.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Benchmark for what the #2085 fix put on the request critical path. NOT an assertion test —
 * opt-in, and it prints numbers rather than passing or failing.
 *
 * <pre>
 *   mvn -o test -Dtest=PerfFormColumnResolution -Dwflow.runPerf=true
 *   mvn -o test -Dtest=PerfFormColumnResolution -Dwflow.runPerf=true -Dwflow.formColumnStaleCheckIntervalMs=-1
 * </pre>
 *
 * <p>Those two invocations are the A/B. The second disables the staleness check, which for this
 * code path is the pre-fix warm behaviour, so the difference is exactly the cost the fix adds.
 * Running both in one JVM is not possible: the interval is a {@code static final} read at class
 * init.</p>
 *
 * <p><b>Why not the 2-node Docker harness.</b> The released Joget image is amd64-only, so on an
 * arm64 host every JVM instruction is emulated; a 40-thread run there did not merely produce
 * inflated numbers, it made the node exit and restart mid-test (exit 0, not OOM — the emulation
 * flakiness already recorded in PLANS.md). Throughput measured under emulation would say nothing
 * about production. This runs natively against the same real Spring context and database.</p>
 *
 * <p><b>What matters here is #7709</b> — "long processing time to create hibernate mapping file
 * when there are many forms in many app versions pointing to the same table name". The fix made
 * column resolution synchronous on the request thread, so the cost of resolving a table with many
 * forms is now request latency. COLD below is that cost.</p>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class PerfFormColumnResolution {

    private static final String APP_ID = "perfFormColumns";
    private static final int[] FORM_COUNTS = {1, 100, 500, 1000};
    private static final int WARM_ITERATIONS = 50000;
    private static final int THREADS = 16;
    private static final int PER_THREAD = 5000;

    /** Repeats per (arm, formCount, phase) for {@link #ab()}. */
    private static final int AB_REPEATS = Integer.getInteger("wflow.perfRepeats", 11);
    /**
     * Seed each app version with a DISTINCT json body instead of 1000 byte-identical copies.
     *
     * <p>Load-bearing for what the benchmark actually measures. {@code mergeFormDefinitions} caches
     * parsed columns keyed on the json STRING, so 1000 identical bodies are parsed ONCE — the full
     * build then costs one parse plus the transfer, and the incremental path has almost nothing
     * left to save. Real app versions differ from one another; that is why a new version exists.
     * With this on, the full build pays 1000 parses and the incremental path pays one.</p>
     */
    private static final boolean DISTINCT_JSON = Boolean.getBoolean("wflow.perfDistinctJson");

    @Autowired
    private FormDataDao formDataDao;

    @Autowired
    private org.joget.apps.form.model.FormColumnCache formColumnCache;

    @BeforeClass
    public static void optIn() {
        Assume.assumeTrue("set -Dwflow.runPerf=true to run this benchmark",
                Boolean.getBoolean("wflow.runPerf"));
    }

    @Test
    public void measure() throws Exception {
        System.out.println();
        System.out.println("=== #2085 column resolution — staleCheckIntervalMs="
                + System.getProperty("wflow.formColumnStaleCheckIntervalMs", "1000 (default)"));
        System.out.printf("%-8s %12s %12s %12s %14s %16s %14s%n",
                "forms", "cold_ms", "edit1_ms", "noop_ms", "warm_us/call", "16thr_calls/s", "16thr_p99_us");

        // JIT warm-up on a throwaway table FIRST. Without it the earliest row pays for compiling
        // the whole path and the results come out non-monotonic — a 1000-form table appearing
        // cheaper than a 500-form one, which is a measurement artifact, not a finding.
        seed("perfcolwarmup", 50);
        for (int i = 0; i < 30; i++) {
            formDataDao.clearFormTableCache("perfcolwarmup");
            formDataDao.getFormDefinitionColumnNames("perfcolwarmup");
        }
        for (int i = 0; i < 20000; i++) {
            formDataDao.getFormDefinitionColumnNames("perfcolwarmup");
        }

        for (int forms : FORM_COUNTS) {
            String table = "perfcol" + forms;
            seed(table, forms);

            // COLD — mapping/column cache built from nothing. The #7709 metric.
            // Median of 5: a single cold sample on a JVM this busy is not a measurement.
            double[] coldSamples = new double[5];
            Collection<String> cols = null;
            for (int i = 0; i < coldSamples.length; i++) {
                formDataDao.clearFormTableCache(table);
                long c0 = System.nanoTime();
                cols = formDataDao.getFormDefinitionColumnNames(table);
                coldSamples[i] = (System.nanoTime() - c0) / 1e6;
            }
            java.util.Arrays.sort(coldSamples);
            double coldMs = coldSamples[coldSamples.length / 2];

            // EDIT — what a form save actually costs. FormDefinitionDaoImpl.update() calls
            // formColumnCache.remove(), NOT clearFormTableCache(), so the incremental base survives
            // and only the edited definition is re-read. The cold loop above cannot show this:
            // clearFormTableCache deliberately resets the base to force an exact rebuild.
            final String t = table;
            final int n = forms;
            double editMs = median(5, () -> { touchOneForm(t, n); formColumnCache.remove(t); }, table);

            // NO-OP — an invalidation where nothing actually changed (an unrelated app edit, or a
            // cache clear elsewhere). Should read the manifest and no definition JSON at all.
            double noopMs = median(5, () -> formColumnCache.remove(t), table);

            long t0;
            if (cols == null || cols.isEmpty()) {
                System.out.println("  !! " + table + " resolved no columns — seed failed, skipping");
                continue;
            }

            // WARM single-threaded — the per-call cost on a hot cache, including the throttle
            // check and the once-per-second database read.
            for (int i = 0; i < 1000; i++) {
                formDataDao.getFormDefinitionColumnNames(table);
            }
            t0 = System.nanoTime();
            for (int i = 0; i < WARM_ITERATIONS; i++) {
                formDataDao.getFormDefinitionColumnNames(table);
            }
            double warmUs = (System.nanoTime() - t0) / 1e3 / WARM_ITERATIONS;

            // WARM concurrent — this is where the throttle's single global monitor shows up.
            long[] result = concurrentWarm(table);
            System.out.printf("%-8d %12.1f %12.1f %12.1f %14.2f %16d %14.1f%n",
                    forms, coldMs, editMs, noopMs, warmUs, result[0], result[1] / 1000.0);
        }
        System.out.println();
    }

    /** @return {calls per second, p99 latency in nanos} */
    private long[] concurrentWarm(final String table) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        final CountDownLatch start = new CountDownLatch(1);
        List<Future<long[]>> futures = new ArrayList<Future<long[]>>();
        for (int t = 0; t < THREADS; t++) {
            futures.add(pool.submit(new Callable<long[]>() {
                @Override
                public long[] call() throws Exception {
                    long[] samples = new long[PER_THREAD];
                    start.await();
                    for (int i = 0; i < PER_THREAD; i++) {
                        long s = System.nanoTime();
                        formDataDao.getFormDefinitionColumnNames(table);
                        samples[i] = System.nanoTime() - s;
                    }
                    return samples;
                }
            }));
        }
        long wall0 = System.nanoTime();
        start.countDown();
        List<Long> all = new ArrayList<Long>();
        for (Future<long[]> f : futures) {
            for (long s : f.get(300, TimeUnit.SECONDS)) {
                all.add(s);
            }
        }
        long wall = System.nanoTime() - wall0;
        pool.shutdown();
        java.util.Collections.sort(all);
        long p99 = all.get((int) (all.size() * 0.99));
        long perSec = (long) (all.size() / (wall / 1e9));
        return new long[]{perSec, p99};
    }

    /** Median of n timed rebuilds, each preceded by the given invalidation. */
    private double median(int n, Runnable invalidate, String table) {
        double[] samples = new double[n];
        for (int i = 0; i < n; i++) {
            invalidate.run();
            long t = System.nanoTime();
            formDataDao.getFormDefinitionColumnNames(table);
            samples[i] = (System.nanoTime() - t) / 1e6;
        }
        java.util.Arrays.sort(samples);
        return samples[samples.length / 2];
    }

    /** Table name for a given definition count, kept distinct per json shape. */
    private static String tableFor(int forms) {
        return (DISTINCT_JSON ? "perfcold" : "perfcol") + forms;
    }

    /** App id for a given definition count, kept distinct per json shape. */
    private static String appIdFor(int forms) {
        return APP_ID + (DISTINCT_JSON ? "d" : "") + forms;
    }

    /** Simulates one form edit: bumps that row's json and dateModified, as a real save would. */
    private void touchOneForm(String table, int forms) {
        try {
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            String appId = appIdFor(forms);
            try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
                s.executeUpdate("UPDATE app_form SET dateModified = NOW(), json = CONCAT(json, ' ') "
                        + "WHERE appId = '" + appId + "' AND formId = 'f1'");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Inserts {@code forms} form definitions all mapping the same table, across that many app
     * versions — the #7709 shape. Raw JDBC: going through appService would take minutes at n=1000
     * and is not what is being measured.
     */
    private void seed(String table, int forms) throws Exception {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        String appId = appIdFor(forms);
        String json = TestUtil.readFile("/forms/sameTable1.json");
        try (Connection c = ds.getConnection()) {
            try (Statement s = c.createStatement()) {
                s.executeUpdate("DELETE FROM app_form WHERE appId = '" + appId + "'");
                s.executeUpdate("DELETE FROM app_app WHERE appId = '" + appId + "'");
            }
            try (PreparedStatement app = c.prepareStatement(
                         "INSERT INTO app_app (appId, appVersion, name, published, dateCreated, dateModified) "
                                 + "VALUES (?, ?, ?, 0, NOW(), NOW())");
                 PreparedStatement form = c.prepareStatement(
                         "INSERT INTO app_form (appId, appVersion, formId, name, tableName, json, dateCreated, dateModified) "
                                 + "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())")) {
                for (int v = 1; v <= forms; v++) {
                    app.setString(1, appId); app.setInt(2, v); app.setString(3, appId);
                    app.addBatch();
                    form.setString(1, appId); form.setInt(2, v); form.setString(3, "f" + v);
                    form.setString(4, "f" + v); form.setString(5, table);
                    // Trailing whitespace: still valid JSON, parses to the identical element tree
                    // and the same column set, but a distinct String, so the parse cache misses.
                    form.setString(6, DISTINCT_JSON ? json + spaces(v) : json);
                    form.addBatch();
                }
                app.executeBatch();
                form.executeBatch();
            }
        }
        formDataDao.clearFormTableCache(table);
    }

    private static String spaces(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------------------------------
    // A/B — cold / edit1 / noop only, many repeats, every sample printed.
    //
    //   mvn -o test -Dtest=PerfFormColumnResolution#ab -Dwflow.runPerf=true \
    //       -Dwflow.formColumnIncrementalEnabled=true|false [-Dwflow.perfDistinctJson=true]
    //
    // The two flag values are separate JVMs by necessity: INCREMENTAL_ENABLED is a static final
    // read at class init.
    // ---------------------------------------------------------------------------------------

    @Test
    public void ab() throws Exception {
        Assume.assumeTrue("select this method explicitly", Boolean.getBoolean("wflow.runAb"));
        System.out.println();
        System.out.println("=== AB incremental=" + System.getProperty("wflow.formColumnIncrementalEnabled", "true(default)")
                + " distinctJson=" + DISTINCT_JSON + " repeats=" + AB_REPEATS);

        jitWarmup();

        System.out.printf("%-6s %-7s %9s %9s %9s %9s %9s%n",
                "forms", "phase", "median", "min", "p25", "p75", "max");
        for (int forms : FORM_COUNTS) {
            final String table = tableFor(forms);
            final int n = forms;
            seed(table, forms);

            double[] cold = samples(AB_REPEATS, () -> formDataDao.clearFormTableCache(table), table);
            double[] edit = samples(AB_REPEATS, () -> { touchOneForm(table, n); formColumnCache.remove(table); }, table);
            double[] noop = samples(AB_REPEATS, () -> formColumnCache.remove(table), table);

            report(forms, "cold", cold);
            report(forms, "edit1", edit);
            report(forms, "noop", noop);
        }
        System.out.println();
    }

    private void jitWarmup() throws Exception {
        String warm = DISTINCT_JSON ? "perfcoldwarmup" : "perfcolwarmup";
        seed(warm, 50);
        for (int i = 0; i < 30; i++) {
            formDataDao.clearFormTableCache(warm);
            formDataDao.getFormDefinitionColumnNames(warm);
        }
        for (int i = 0; i < 5000; i++) {
            formDataDao.getFormDefinitionColumnNames(warm);
        }
    }

    private double[] samples(int n, Runnable invalidate, String table) {
        double[] s = new double[n];
        for (int i = 0; i < n; i++) {
            invalidate.run();
            long t = System.nanoTime();
            formDataDao.getFormDefinitionColumnNames(table);
            s[i] = (System.nanoTime() - t) / 1e6;
        }
        java.util.Arrays.sort(s);
        return s;
    }

    private static void report(int forms, String phase, double[] sorted) {
        System.out.printf("%-6d %-7s %9.2f %9.2f %9.2f %9.2f %9.2f%n", forms, phase,
                sorted[sorted.length / 2], sorted[0],
                sorted[(int) (sorted.length * 0.25)], sorted[(int) (sorted.length * 0.75)],
                sorted[sorted.length - 1]);
    }

    // ---------------------------------------------------------------------------------------
    // Attribution — where the per-row cost of a rebuild actually goes.
    //
    //   mvn -o test -Dtest=PerfFormColumnResolution#attribution -Dwflow.runPerf=true -Dwflow.runAttr=true
    // ---------------------------------------------------------------------------------------

    private static final int ATTR_WARM = Integer.getInteger("wflow.attrWarm", 8);
    private static final int ATTR_REPS = Integer.getInteger("wflow.attrReps", 15);

    @Test
    public void attribution() throws Exception {
        Assume.assumeTrue("select this method explicitly", Boolean.getBoolean("wflow.runAttr"));

        final DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        final org.hibernate.SessionFactory sf =
                (org.hibernate.SessionFactory) AppUtil.getApplicationContext().getBean("appSessionFactory");
        final Object formDefDao = AppUtil.getApplicationContext().getBean("formDefinitionDao");
        final Object builderDao = AppUtil.getApplicationContext().getBean("builderDefinitionDao");

        for (int forms : FORM_COUNTS) {
            seed(tableFor(forms), forms);
        }

        final String MANIFEST_SQL =
                "SELECT appId, appVersion, formId, dateModified, length(json) FROM app_form WHERE tableName = ?";
        final String NO_LENGTH_SQL =
                "SELECT appId, appVersion, formId, dateModified FROM app_form WHERE tableName = ?";
        final String ONE_COL_SQL = "SELECT formId FROM app_form WHERE tableName = ?";
        final String FULL_SQL =
                "SELECT appId, appVersion, formId, dateModified, json FROM app_form WHERE tableName = ?";
        final String AGG_SQL =
                "SELECT COUNT(*), MAX(dateModified), SUM(length(json)) FROM app_form WHERE tableName = ?";
        final String MANIFEST_HQL =
                "SELECT e.appId, e.appVersion, e.id, e.dateModified, length(e.json) FROM FormDefinition e WHERE e.tableName = ?1";
        final String AGG_HQL =
                "SELECT COUNT(e), MAX(e.dateModified), SUM(length(e.json)) FROM FormDefinition e WHERE e.tableName = ?1";

        System.out.println();
        System.out.println("  formDefinitionDao bean = " + formDefDao.getClass());
        for (Class<?> i : formDefDao.getClass().getInterfaces()) {
            System.out.println("    implements " + i.getName() + " from "
                    + (i.getProtectionDomain().getCodeSource() == null ? "?" : i.getProtectionDomain().getCodeSource().getLocation()));
        }
        System.out.println("=== ATTRIBUTION  distinctJson=" + DISTINCT_JSON
                + "  warm=" + ATTR_WARM + " reps=" + ATTR_REPS + " (all values ms, median of reps)");
        System.out.printf("%-46s %10s %10s %10s %10s%n", "component", "n=1", "n=100", "n=500", "n=1000");

        // -- fixed-cost floors (independent of row count), measured once ---------------------
        row("pool: getConnection + close", ds, null, () -> {
            try (Connection c = ds.getConnection()) { return c != null ? 1 : 0; }
        });
        row("jdbc: SELECT 1 round trip", ds, null, () -> {
            try (Connection c = ds.getConnection();
                 PreparedStatement p = c.prepareStatement("SELECT 1");
                 java.sql.ResultSet rs = p.executeQuery()) {
                int k = 0; while (rs.next()) { k += rs.getInt(1); } return k;
            }
        });
        row("hibernate: openSession + close", ds, null, () -> {
            org.hibernate.Session s = sf.openSession();
            try { return 1; } finally { s.close(); }
        });

        // -- per-row scaling ------------------------------------------------------------------
        scaled("jdbc: manifest rs.next() only", ds, (c, t) -> countOnly(c, MANIFEST_SQL, t));
        scaled("jdbc: manifest read 5 cols", ds, (c, t) -> readManifest(c, MANIFEST_SQL, t, true));
        scaled("jdbc: manifest read 4 cols (no length)", ds, (c, t) -> readManifest(c, NO_LENGTH_SQL, t, false));
        scaled("jdbc: 1 col (formId) read", ds, (c, t) -> {
            try (PreparedStatement p = c.prepareStatement(ONE_COL_SQL)) {
                p.setString(1, t);
                try (java.sql.ResultSet rs = p.executeQuery()) {
                    int k = 0; while (rs.next()) { if (rs.getString(1) != null) k++; } return k;
                }
            }
        });
        scaled("jdbc: full rows incl json", ds, (c, t) -> {
            try (PreparedStatement p = c.prepareStatement(FULL_SQL)) {
                p.setString(1, t);
                try (java.sql.ResultSet rs = p.executeQuery()) {
                    int k = 0; while (rs.next()) { k += rs.getString(5).length(); } return k;
                }
            }
        });
        scaled("jdbc: AGGREGATE count/max/sum(len)", ds, (c, t) -> {
            try (PreparedStatement p = c.prepareStatement(AGG_SQL)) {
                p.setString(1, t);
                try (java.sql.ResultSet rs = p.executeQuery()) {
                    int k = 0; while (rs.next()) { k += rs.getInt(1); } return k;
                }
            }
        });

        scaledNoConn("hibernate: manifest HQL (openSession)", t -> hql(sf, MANIFEST_HQL, t));
        scaledNoConn("hibernate: AGGREGATE HQL (openSession)", t -> hql(sf, AGG_HQL, t));
        scaledNoConn("dao: getFormDefinitionManifestByTableName", t -> {
            Collection<?> r = (Collection<?>) invoke(formDefDao, "getFormDefinitionManifestByTableName", new Class[]{String.class}, new Object[]{t});
            return r.size();
        });
        scaledNoConn("dao: loadFormDefinitionByTableName (full)", t -> {
            Collection<?> r = (Collection<?>) invoke(formDefDao, "loadFormDefinitionByTableName", new Class[]{String.class}, new Object[]{t});
            return r.size();
        });
        scaledNoConn("dao: builderDefinitionDao.find (per rebuild)", t -> {
            Object r = invoke(builderDao, "find", null,
                    new Object[]{" and type = ? and id = ?",
                            new String[]{"customFormDataTable", "app_fd_" + t}, null, null, null, null, null});
            return r == null ? 0 : ((Collection<?>) r).size();
        });

        // The work the incremental path exists to avoid: parsing every definition's json.
        final Object formService = AppUtil.getApplicationContext().getBean("formService");
        scaledNoConn("cpu: createElementFromJson per definition", t -> {
            int k = 0;
            for (Object[] r : MANIFEST_ROWS.get(t)) {
                k += invoke(formService, "createElementFromJson", null,
                        new Object[]{JSON_BY_KEY.get(r[0] + ":" + r[1] + ":" + r[2]), Boolean.FALSE}) == null ? 0 : 1;
            }
            return k;
        });
        scaledNoConn("cpu: createElementFromJson ONCE (dedup hit)", t -> {
            Object[] r = MANIFEST_ROWS.get(t).get(0);
            return invoke(formService, "createElementFromJson", null,
                    new Object[]{JSON_BY_KEY.get(r[0] + ":" + r[1] + ":" + r[2]), Boolean.FALSE}) == null ? 0 : 1;
        });
        scaledNoConn("dao: loadFormDefinitionByTableNameAndIds(1)", t -> {
            Collection<?> r = (Collection<?>) invoke(formDefDao, "loadFormDefinitionByTableNameAndIds", null,
                    new Object[]{t, java.util.Collections.singletonList("f1")});
            return r.size();
        });

        // -- pure CPU pieces of the incremental path -----------------------------------------
        scaledNoConn("cpu: build manifest maps from rows", t -> {
            java.util.Map<String, String> keys = new java.util.HashMap<String, String>();
            java.util.Map<String, String> ids = new java.util.HashMap<String, String>();
            for (Object[] r : MANIFEST_ROWS.get(t)) {
                String k = r[0] + ":" + r[1] + ":" + r[2];
                keys.put(k, Long.toString(((java.util.Date) r[3]).getTime()) + ":" + r[4]);
                ids.put(k, (String) r[2]);
            }
            return keys.size() + ids.size();
        });
        scaledNoConn("cpu: diff + storeBase copies", t -> {
            java.util.Map<String, String> base = BASE_KEYS.get(t);
            java.util.Set<String> changed = new java.util.HashSet<String>();
            for (java.util.Map.Entry<String, String> e : base.entrySet()) {
                String prev = base.get(e.getKey());
                if (prev == null || !prev.equals(e.getValue())) { changed.add(e.getKey()); }
            }
            java.util.Map<String, String> copy = new java.util.HashMap<String, String>(base);
            return copy.size() + changed.size();
        });

        // -- setting reads on the hot path ----------------------------------------------------
        final String hot = tableFor(1000);
        formDataDao.getFormDefinitionColumnNames(hot);
        row("cache: formColumnCache.get (warm hit)", ds, null, () -> formColumnCache.get(hot) == null ? 0 : 1);
        row("cache: formColumnCache.getFresh (db read)", ds, null,
                () -> invoke(formColumnCache, "getFresh", new Class[]{String.class}, new Object[]{hot}) == null ? 0 : 1);
        row("setup: getSettingValuesFresh (db read)", ds, null, () -> {
            Object sm = AppUtil.getApplicationContext().getBean("setupManager");
            return invoke(sm, "getSettingValuesFresh", new Class[]{String.class},
                    new Object[]{"CACHE_LAST_CLEAR_formColumnCache_" + hot}) == null ? 0 : 1;
        });

        // -- whole-path reconciliation --------------------------------------------------------
        scaledNoConn("WHOLE: noop rebuild via public API", t -> {
            formColumnCache.remove(t);
            return formDataDao.getFormDefinitionColumnNames(t).size();
        });

        // The DAO runs its queries on sessionFactory.getCurrentSession(). If that session has
        // accumulated entities, every subsequent query pays an auto-flush dirty check over the
        // whole persistence context — O(entities), not O(rows returned). Clearing it before each
        // timed rebuild is the control: if the cost collapses, the per-row cost is persistence
        // context size, not row materialisation.
        System.out.println("  persistence context before clear: " + entityCount(sf));
        scaledNoConn("WHOLE: noop rebuild, session.clear() first", t -> {
            clearSession(sf);
            formColumnCache.remove(t);
            return formDataDao.getFormDefinitionColumnNames(t).size();
        });
        scaledNoConn("dao: manifest on currentSession (dirty ctx)", t -> {
            Collection<?> r = (Collection<?>) invoke(formDefDao, "getFormDefinitionManifestByTableName", null, new Object[]{t});
            return r.size();
        });
        scaledNoConn("dao: manifest on currentSession (cleared)", t -> {
            clearSession(sf);
            Collection<?> r = (Collection<?>) invoke(formDefDao, "getFormDefinitionManifestByTableName", null, new Object[]{t});
            return r.size();
        });
        System.out.println("  persistence context after: " + entityCount(sf));

        System.out.println();
    }

    private static int entityCount(org.hibernate.SessionFactory sf) {
        try {
            return sf.getCurrentSession().getStatistics().getEntityCount();
        } catch (Throwable e) {
            return -1;
        }
    }

    private static void clearSession(org.hibernate.SessionFactory sf) {
        try {
            sf.getCurrentSession().clear();
        } catch (Throwable e) {
            // no contextual session; nothing to clear
        }
    }

    // ---------------------------------------------------------------------------------------
    // Gate — is a single-row aggregate actually cheap enough to short-circuit a rebuild, and how
    // much of the full build's per-row cost is Hibernate hydration rather than JDBC?
    //
    //   mvn -o test -Dtest=PerfFormColumnResolution#gate -Dwflow.runPerf=true -Dwflow.runGate=true
    // ---------------------------------------------------------------------------------------

    @Test
    public void gate() throws Exception {
        Assume.assumeTrue("select this method explicitly", Boolean.getBoolean("wflow.runGate"));

        final DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        final org.hibernate.SessionFactory sf =
                (org.hibernate.SessionFactory) AppUtil.getApplicationContext().getBean("appSessionFactory");
        final Object formDefDao = AppUtil.getApplicationContext().getBean("formDefinitionDao");

        for (int forms : FORM_COUNTS) {
            seed(tableFor(forms), forms);
        }

        System.out.println();
        System.out.println("=== GATE  distinctJson=" + DISTINCT_JSON + " (ms, median of " + ATTR_REPS + ")");
        System.out.printf("%-46s %10s %10s %10s %10s%n", "component", "n=1", "n=100", "n=500", "n=1000");

        // Which part of the aggregate costs anything?
        scaled("jdbc: COUNT(*) only", ds, (c, t) -> agg(c, "SELECT COUNT(*) FROM app_form WHERE tableName = ?", t));
        scaled("jdbc: COUNT + MAX(dateModified)", ds, (c, t) ->
                agg(c, "SELECT COUNT(*), MAX(dateModified) FROM app_form WHERE tableName = ?", t));
        scaled("jdbc: COUNT + MAX + SUM(length(json))", ds, (c, t) ->
                agg(c, "SELECT COUNT(*), MAX(dateModified), SUM(length(json)) FROM app_form WHERE tableName = ?", t));

        final String AGG_HQL = "SELECT COUNT(e), MAX(e.dateModified), SUM(length(e.json)) FROM FormDefinition e WHERE e.tableName = ?1";
        scaledNoConn("hql: aggregate, new session each call", t -> {
            org.hibernate.Session s = sf.openSession();
            try {
                org.hibernate.query.Query q = s.createQuery(AGG_HQL);
                q.setParameter(1, t);
                return q.list().size();
            } finally { s.close(); }
        });

        // One session held open across the whole measurement: separates per-query cost from
        // whatever openSession/close costs.
        final org.hibernate.Session held = sf.openSession();
        scaledNoConn("hql: aggregate, one held session", t -> {
            org.hibernate.query.Query q = held.createQuery(AGG_HQL);
            q.setParameter(1, t);
            return q.list().size();
        });
        held.close();

        // Which HQL term is responsible for the scaling of a one-row result?
        hqlRow(sf, "hql: COUNT(e) only",
                "SELECT COUNT(e) FROM FormDefinition e WHERE e.tableName = ?1");
        hqlRow(sf, "hql: COUNT(e) + MAX(dateModified)",
                "SELECT COUNT(e), MAX(e.dateModified) FROM FormDefinition e WHERE e.tableName = ?1");
        hqlRow(sf, "hql: full aggregate",
                "SELECT COUNT(e), MAX(e.dateModified), SUM(length(e.json)) FROM FormDefinition e WHERE e.tableName = ?1");
        hqlRow(sf, "hql: manifest projection (5 scalars)",
                "SELECT e.appId, e.appVersion, e.id, e.dateModified, length(e.json) FROM FormDefinition e WHERE e.tableName = ?1");
        // Same statements as native SQL through the same Hibernate session, to separate the
        // Hibernate layer from the database and the driver.
        nativeRow(sf, "native(hib): aggregate",
                "SELECT COUNT(*), MAX(dateModified), SUM(length(json)) FROM app_form WHERE tableName = ?1");
        nativeRow(sf, "native(hib): manifest projection",
                "SELECT appId, appVersion, formId, dateModified, length(json) FROM app_form WHERE tableName = ?1");

        // Hydration: the same entity load, but with the persistence context emptied first, so
        // every call really does materialise 1000 entities instead of hitting the identity map.
        scaledNoConn("dao: loadFormDefinitionByTableName, ctx cleared", t -> {
            clearSession(sf);
            Collection<?> r = (Collection<?>) invoke(formDefDao, "loadFormDefinitionByTableName", null, new Object[]{t});
            return r.size();
        });
        scaledNoConn("dao: loadFormDefinitionByTableName, ctx warm", t -> {
            Collection<?> r = (Collection<?>) invoke(formDefDao, "loadFormDefinitionByTableName", null, new Object[]{t});
            return r.size();
        });

        // Is a repeated entity load actually hydrating 1000 entities, or being served from a
        // cache? Without this, an isolated micro-benchmark of the load can look far cheaper than
        // the same call inside a real rebuild, and the attribution silently goes wrong.
        org.hibernate.stat.Statistics st = sf.getStatistics();
        String t1000 = tableFor(1000);
        for (int i = 1; i <= 3; i++) {
            long e0 = st.getEntityLoadCount(), q0 = st.getQueryExecutionCount();
            long qh0 = st.getQueryCacheHitCount(), s0 = st.getSecondLevelCacheHitCount();
            long n0 = System.nanoTime();
            clearSession(sf);
            Collection<?> r = (Collection<?>) invoke(formDefDao, "loadFormDefinitionByTableName", null, new Object[]{t1000});
            double ms = (System.nanoTime() - n0) / 1e6;
            System.out.printf("  load#%d rows=%d %.2fms entityLoads=+%d queries=+%d qCacheHits=+%d l2Hits=+%d%n",
                    i, r.size(), ms, st.getEntityLoadCount() - e0, st.getQueryExecutionCount() - q0,
                    st.getQueryCacheHitCount() - qh0, st.getSecondLevelCacheHitCount() - s0);
        }
        System.out.println("  statistics enabled=" + st.isStatisticsEnabled());

        // Isolate the suspect: AbstractAppVersionedObject.getAppId()/getAppVersion() delegate to
        // the lazy="proxy" appDefinition association. mergeFormDefinitions builds a "source"
        // string from them for EVERY definition, so if that initialises the proxy it is one
        // SELECT per definition.
        for (int i = 1; i <= 3; i++) {
            clearSession(sf);
            Collection<?> defs = (Collection<?>) invoke(formDefDao, "loadFormDefinitionByTableName", null, new Object[]{t1000});
            long p0 = st.getPrepareStatementCount(), e0 = st.getEntityLoadCount();
            long n0 = System.nanoTime();
            int k = 0;
            for (Object d : defs) {
                k += String.valueOf(invoke(d, "getAppVersion", null, new Object[0])).length();
            }
            System.out.printf("  getAppVersion x%d: %8.2fms stmts=+%d entityLoads=+%d (k=%d)%n",
                    defs.size(), (System.nanoTime() - n0) / 1e6,
                    st.getPrepareStatementCount() - p0, st.getEntityLoadCount() - e0, k);

            clearSession(sf);
            defs = (Collection<?>) invoke(formDefDao, "loadFormDefinitionByTableName", null, new Object[]{t1000});
            p0 = st.getPrepareStatementCount();
            n0 = System.nanoTime();
            k = 0;
            for (Object d : defs) {
                k += String.valueOf(invoke(d, "getJson", null, new Object[0])).length();
            }
            System.out.printf("  getJson       x%d: %8.2fms stmts=+%d (k=%d)%n",
                    defs.size(), (System.nanoTime() - n0) / 1e6, st.getPrepareStatementCount() - p0, k);
        }

        // What does a REAL rebuild actually issue? clearFormTableCache resets the incremental
        // base, so the first of each pair is a full build and the second is incremental.
        for (int i = 1; i <= 3; i++) {
            probe(st, "full  ", () -> { formDataDao.clearFormTableCache(t1000); return null; }, t1000);
            probe(st, "incr  ", () -> { formColumnCache.remove(t1000); return null; }, t1000);
        }
        System.out.println();
    }

    private void probe(org.hibernate.stat.Statistics st, String label, java.util.concurrent.Callable<Void> invalidate, String table) {
        try {
            invalidate.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long e0 = st.getEntityLoadCount(), q0 = st.getQueryExecutionCount(), p0 = st.getPrepareStatementCount();
        long c0 = st.getConnectCount(), f0 = st.getFlushCount();
        java.util.Map<String, Long> before = new java.util.HashMap<String, Long>();
        for (String n : st.getEntityNames()) {
            before.put(n, st.getEntityStatistics(n).getLoadCount());
        }
        long n0 = System.nanoTime();
        int cols = formDataDao.getFormDefinitionColumnNames(table).size();
        double ms = (System.nanoTime() - n0) / 1e6;
        StringBuilder by = new StringBuilder();
        for (String n : st.getEntityNames()) {
            long d = st.getEntityStatistics(n).getLoadCount() - (before.containsKey(n) ? before.get(n) : 0L);
            if (d > 0) {
                by.append(' ').append(n).append('=').append(d);
            }
        }
        System.out.printf("  rebuild[%s] %8.2fms cols=%d entityLoads=+%d hqlQueries=+%d stmts=+%d connects=+%d flushes=+%d by:%s%n",
                label, ms, cols, st.getEntityLoadCount() - e0, st.getQueryExecutionCount() - q0,
                st.getPrepareStatementCount() - p0, st.getConnectCount() - c0, st.getFlushCount() - f0, by);
    }

    private static void hqlRow(final org.hibernate.SessionFactory sf, String label, final String q) {
        scaledNoConn(label, t -> {
            org.hibernate.Session s = sf.openSession();
            try {
                org.hibernate.query.Query query = s.createQuery(q);
                query.setParameter(1, t);
                return query.list().size();
            } finally { s.close(); }
        });
    }

    private static void nativeRow(final org.hibernate.SessionFactory sf, String label, final String q) {
        scaledNoConn(label, t -> {
            org.hibernate.Session s = sf.openSession();
            try {
                org.hibernate.query.NativeQuery query = s.createNativeQuery(q);
                query.setParameter(1, t);
                return query.list().size();
            } finally { s.close(); }
        });
    }

    private static int agg(Connection c, String sql, String table) throws Exception {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, table);
            try (java.sql.ResultSet rs = p.executeQuery()) {
                int k = 0; while (rs.next()) { k += rs.getInt(1); } return k;
            }
        }
    }

    // --- attribution plumbing --------------------------------------------------------------

    private interface Work { int run() throws Exception; }
    private interface ConnWork { int run(Connection c, String table) throws Exception; }
    private interface TableWork { int run(String table) throws Exception; }

    /** Cached manifest rows per table, so the CPU-only rows do not pay for the query. */
    private static final java.util.Map<String, List<Object[]>> MANIFEST_ROWS = new java.util.HashMap<String, List<Object[]>>();
    private static final java.util.Map<String, java.util.Map<String, String>> BASE_KEYS = new java.util.HashMap<String, java.util.Map<String, String>>();
    private static final java.util.Map<String, String> JSON_BY_KEY = new java.util.HashMap<String, String>();

    private static int hql(org.hibernate.SessionFactory sf, String q, String table) {
        org.hibernate.Session s = sf.openSession();
        try {
            org.hibernate.query.Query query = s.createQuery(q);
            query.setParameter(1, table);
            return query.list().size();
        } finally {
            s.close();
        }
    }

    private static int countOnly(Connection c, String sql, String table) throws Exception {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, table);
            try (java.sql.ResultSet rs = p.executeQuery()) {
                int k = 0; while (rs.next()) { k++; } return k;
            }
        }
    }

    private static int readManifest(Connection c, String sql, String table, boolean withLength) throws Exception {
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, table);
            try (java.sql.ResultSet rs = p.executeQuery()) {
                int k = 0;
                while (rs.next()) {
                    Object[] r = new Object[withLength ? 5 : 4];
                    r[0] = rs.getString(1);
                    r[1] = rs.getLong(2);
                    r[2] = rs.getString(3);
                    r[3] = rs.getTimestamp(4);
                    if (withLength) { r[4] = rs.getLong(5); }
                    k += r.length;
                }
                return k;
            }
        }
    }

    /** Name+arity lookup, because the beans are JDK proxies and exact signatures do not always match. */
    private static Object invoke(Object target, String name, Class[] sig, Object[] args) {
        try {
            for (java.lang.reflect.Method m : target.getClass().getMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == args.length) {
                    m.setAccessible(true);
                    return m.invoke(target, args);
                }
            }
            throw new NoSuchMethodException(name + "/" + args.length + " on " + target.getClass());
        } catch (Exception e) {
            throw new RuntimeException(name + ": " + e, e);
        }
    }

    private static double medianOf(Work w) {
        try {
            for (int i = 0; i < ATTR_WARM; i++) { w.run(); }
            double[] s = new double[ATTR_REPS];
            for (int i = 0; i < ATTR_REPS; i++) {
                long t = System.nanoTime();
                w.run();
                s[i] = (System.nanoTime() - t) / 1e6;
            }
            java.util.Arrays.sort(s);
            return s[s.length / 2];
        } catch (Throwable e) {
            System.out.println("    !! " + e);
            return Double.NaN;
        }
    }

    /** A component whose cost does not depend on row count: printed in the n=1000 column only. */
    private static void row(String label, DataSource ds, String unused, Work w) {
        System.out.printf("%-46s %10s %10s %10s %10.4f%n", label, "-", "-", "-", medianOf(w));
    }

    /** A component measured at every form count, on a connection taken per iteration. */
    private static void scaled(String label, final DataSource ds, final ConnWork w) {
        double[] out = new double[FORM_COUNTS.length];
        for (int i = 0; i < FORM_COUNTS.length; i++) {
            final String t = tableFor(FORM_COUNTS[i]);
            out[i] = medianOf(() -> {
                try (Connection c = ds.getConnection()) { return w.run(c, t); }
            });
        }
        print(label, out);
    }

    private static void scaledNoConn(String label, final TableWork w) {
        double[] out = new double[FORM_COUNTS.length];
        for (int i = 0; i < FORM_COUNTS.length; i++) {
            final String t = tableFor(FORM_COUNTS[i]);
            primeCaches(t);
            out[i] = medianOf(() -> w.run(t));
        }
        print(label, out);
    }

    /** Fills MANIFEST_ROWS / BASE_KEYS so the CPU-only rows have data to chew on. */
    private static void primeCaches(String table) {
        if (MANIFEST_ROWS.containsKey(table)) {
            return;
        }
        try {
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            List<Object[]> rows = new ArrayList<Object[]>();
            java.util.Map<String, String> keys = new java.util.HashMap<String, String>();
            try (Connection c = ds.getConnection();
                 PreparedStatement p = c.prepareStatement(
                         "SELECT appId, appVersion, formId, dateModified, length(json), json FROM app_form WHERE tableName = ?")) {
                p.setString(1, table);
                try (java.sql.ResultSet rs = p.executeQuery()) {
                    while (rs.next()) {
                        Object[] r = {rs.getString(1), rs.getLong(2), rs.getString(3), rs.getTimestamp(4), rs.getLong(5)};
                        rows.add(r);
                        String k = r[0] + ":" + r[1] + ":" + r[2];
                        keys.put(k, Long.toString(((java.util.Date) r[3]).getTime()) + ":" + r[4]);
                        JSON_BY_KEY.put(k, rs.getString(6));
                    }
                }
            }
            MANIFEST_ROWS.put(table, rows);
            BASE_KEYS.put(table, keys);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void print(String label, double[] out) {
        System.out.printf("%-46s %10.4f %10.4f %10.4f %10.4f%n", label, out[0], out[1], out[2], out[3]);
    }
}
