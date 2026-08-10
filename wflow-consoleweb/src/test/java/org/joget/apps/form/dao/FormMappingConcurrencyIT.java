package org.joget.apps.form.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.hibernate.HibernateException;
import org.joget.apps.app.model.TestUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * What the new mapping single-flight actually costs on a REAL stack under concurrent invalidation.
 *
 * <p>The open question this answers: {@code createSessionFactoryWithSingleFlight} now THROWS a
 * retriable {@code HibernateException} where the code previously proceeded — on lock timeout, on a
 * nested caller that must not wait, and on re-entrancy. The unit suite proves the mechanism fires
 * correctly and that regeneration never overlaps, but it cannot say how often a real request would
 * be told to retry, because that depends on real regeneration timing: writing
 * {@code <entity>.hbm.xml}, building a {@code SessionFactory} and running {@code SchemaUpdate} DDL
 * against a real database. If the answer were "often", the fix would have traded a silent race for
 * visible 500s.</p>
 *
 * <p>So this hammers the genuine article: N threads, each repeatedly invalidating the table's caches
 * and then issuing a query that forces change detection and regeneration. Every outcome is counted
 * and the retry rate is printed. The correctness assertions are that nothing OTHER than the retriable
 * exception escapes, that progress is made, and that no thread hangs — a deadlock here would be the
 * ABBA cycle the shared guard exists to rule out.</p>
 *
 * <pre>
 *   mvn -o test -Dtest=FormMappingConcurrencyIT
 * </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class FormMappingConcurrencyIT {

    private static final String APP_ID = "mappingConcurrencyIT";
    private static final String TABLE = "mappingconcit";
    private static final String FORM_ID = "f1";

    private static final int THREADS = 8;
    private static final int PER_THREAD = 12;

    private FormDataDao formDataDao() {
        return (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
    }

    private DataSource dataSource() {
        return (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
    }

    @Before
    public void seed() throws Exception {
        String json;
        try {
            json = TestUtil.readFile("/forms/sameTable1.json");
        } catch (java.io.IOException e) {
            throw new IllegalStateException("fixture /forms/sameTable1.json unreadable", e);
        }
        try (Connection c = dataSource().getConnection()) {
            clean(c);
            java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis() - 3600000L);
            try (PreparedStatement app = c.prepareStatement(
                        "INSERT INTO app_app (appId, appVersion, name, dateCreated, dateModified)"
                        + " VALUES (?, ?, ?, ?, ?)");
                 PreparedStatement form = c.prepareStatement(
                        "INSERT INTO app_form (appId, appVersion, formId, name, tableName, json,"
                        + " dateCreated, dateModified) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                app.setString(1, APP_ID); app.setLong(2, 1L); app.setString(3, APP_ID);
                app.setTimestamp(4, ts); app.setTimestamp(5, ts);
                app.executeUpdate();
                form.setString(1, APP_ID); form.setLong(2, 1L); form.setString(3, FORM_ID);
                form.setString(4, FORM_ID); form.setString(5, TABLE); form.setString(6, json);
                form.setTimestamp(7, ts); form.setTimestamp(8, ts);
                form.executeUpdate();
            }
        }
        // Create the table and its first mapping serially, so the measurement below is about
        // CONTENTION rather than about first-time DDL.
        formDataDao().count(APP_ID + ":" + FORM_ID, TABLE, "", new Object[0]);
    }

    @After
    public void cleanUp() throws Exception {
        try (Connection c = dataSource().getConnection()) {
            clean(c);
        }
    }

    private void clean(Connection c) throws Exception {
        try (Statement s = c.createStatement()) {
            s.executeUpdate("DELETE FROM app_form WHERE appId = '" + APP_ID + "'");
            s.executeUpdate("DELETE FROM app_app WHERE appId = '" + APP_ID + "'");
        }
    }

    @Test
    public void concurrentInvalidationAndRegenerationMakesProgressWithoutHangingOrLeakingErrors()
            throws Exception {
        final AtomicInteger ok = new AtomicInteger();
        final AtomicInteger retriable = new AtomicInteger();
        final List<Throwable> unexpected = new CopyOnWriteArrayList<Throwable>();
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(THREADS);

        for (int t = 0; t < THREADS; t++) {
            new Thread(() -> {
                try {
                    start.await(10, TimeUnit.SECONDS);
                    for (int i = 0; i < PER_THREAD; i++) {
                        try {
                            // Invalidate everything about the table, then force the full decision
                            // path: column resolution, change detection, and regeneration.
                            formDataDao().clearFormTableCache(TABLE);
                            formDataDao().count(APP_ID + ":" + FORM_ID, TABLE, "", new Object[0]);
                            ok.incrementAndGet();
                        } catch (HibernateException expected) {
                            retriable.incrementAndGet();
                        } catch (Throwable other) {
                            unexpected.add(other);
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

        assertTrue("threads did not finish within 120s — a deadlock or an unbounded wait, which is "
                + "what the shared FormBuildGuard exists to rule out", done.await(120, TimeUnit.SECONDS));

        int total = THREADS * PER_THREAD;
        System.out.println("[mapping concurrency, real stack] " + total + " invalidate+query cycles on "
                + THREADS + " threads: " + ok.get() + " served, " + retriable.get()
                + " told to retry (" + (retriable.get() * 100 / total) + "%)");
        if (!unexpected.isEmpty()) {
            unexpected.get(0).printStackTrace();
        }

        assertEquals("only the retriable HibernateException may escape this path; anything else is a "
                + "regression: " + (unexpected.isEmpty() ? "" : unexpected.get(0)),
                Collections.<Throwable>emptyList(), unexpected);
        assertEquals("every cycle must end either served or retriable",
                total, ok.get() + retriable.get());
        assertTrue("no request was served at all — failing closed has become failing always",
                ok.get() > 0);
    }
}
