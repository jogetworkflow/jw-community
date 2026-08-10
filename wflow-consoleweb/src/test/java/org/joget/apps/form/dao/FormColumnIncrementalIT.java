package org.joget.apps.form.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import javax.sql.DataSource;
import org.joget.apps.app.model.TestUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.FormColumnCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Drives the INCREMENTAL column rebuild against a real database, on whatever dialect this runs on.
 *
 * <p>Everything else that covers the incremental path mocks the DAO, so the whole flow — read the
 * manifest, diff it against the stored base, re-read only what moved, merge additively — has only
 * ever run against Mockito answers. {@code FormDefinitionManifestIT} proved the manifest QUERY works
 * on all four engines; this proves the BEHAVIOUR built on top of it does, which is a different claim:
 * it depends on {@code dateModified} surviving a write-then-read round trip identically on both the
 * native projection and the entity load, on the same dialect, at whatever precision that dialect
 * stores.</p>
 *
 * <pre>
 *   bash skills/joget-java-testing/scripts/run-db-matrix.sh -m wflow-consoleweb -t 'FormColumnIncrementalIT'
 * </pre>
 *
 * <p><b>Deliberately NOT {@code @Transactional}.</b> The edit half-way through the first test is
 * committed on a separate connection, and under MySQL's default REPEATABLE READ a test-scoped
 * transaction would hold a snapshot taken before it — the manifest would keep reporting the OLD
 * dateModified and the incremental path would correctly conclude nothing changed, failing the test
 * for a reason that has nothing to do with the code. Each DAO call getting its own transaction is
 * also what a production request does.</p>
 *
 * <p>Seeding is plain JDBC so the write path shares no code with the read path under test, and the
 * timestamps are set explicitly rather than with {@code NOW()} — a definition written "now" is inside
 * the recency window and would be re-read for that reason alone, which would mask whether change-key
 * comparison works at all.</p>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class FormColumnIncrementalIT {

    private static final String APP_ID = "incrementalIT";
    private static final String TABLE = "incrementalit";
    /** Well outside the recency window, so only the change key can explain a re-read. */
    private static final long HOUR_AGO = System.currentTimeMillis() - 3600000L;
    private static final long HALF_HOUR_AGO = System.currentTimeMillis() - 1800000L;

    private FormDataDao formDataDao() {
        return (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
    }

    private FormColumnCache formColumnCache() {
        return (FormColumnCache) AppUtil.getApplicationContext().getBean("formColumnCache");
    }

    private DataSource dataSource() {
        return (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
    }

    private String baseJson() {
        try {
            return TestUtil.readFile("/forms/sameTable1.json");
        } catch (java.io.IOException e) {
            throw new IllegalStateException("test fixture /forms/sameTable1.json is unreadable", e);
        }
    }

    /** The same form with one field renamed, i.e. what a designer adding a field looks like here. */
    private String editedJson() {
        return baseJson().replace("\"field1\"", "\"newcol\"");
    }

    @Before
    public void seed() throws Exception {
        // Reset BEFORE seeding, not after. FormColumnCache.putBase drops a base whose build STARTED
        // at or before the last reset, so a reset immediately followed by the first resolve can land
        // in the same millisecond and the base is silently discarded - which is correct production
        // behaviour and a broken fixture. The JDBC work below guarantees the gap.
        formColumnCache().reset(TABLE);
        try (Connection c = dataSource().getConnection()) {
            clean(c);
            java.sql.Timestamp ts = new java.sql.Timestamp(HOUR_AGO);
            try (PreparedStatement app = c.prepareStatement(
                        "INSERT INTO app_app (appId, appVersion, name, dateCreated, dateModified)"
                        + " VALUES (?, ?, ?, ?, ?)");
                 PreparedStatement form = c.prepareStatement(
                        "INSERT INTO app_form (appId, appVersion, formId, name, tableName, json,"
                        + " dateCreated, dateModified) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                for (int v = 1; v <= 2; v++) {
                    app.setString(1, APP_ID); app.setLong(2, v); app.setString(3, APP_ID);
                    app.setTimestamp(4, ts); app.setTimestamp(5, ts);
                    app.addBatch();
                    form.setString(1, APP_ID); form.setLong(2, v); form.setString(3, "f" + v);
                    form.setString(4, "f" + v); form.setString(5, TABLE);
                    form.setString(6, baseJson());
                    form.setTimestamp(7, ts); form.setTimestamp(8, ts);
                    form.addBatch();
                }
                app.executeBatch();
                form.executeBatch();
            }
        }
    }

    @After
    public void cleanUp() throws Exception {
        try (Connection c = dataSource().getConnection()) {
            clean(c);
        }
        formColumnCache().reset(TABLE);
    }

    private void clean(Connection c) throws Exception {
        try (Statement s = c.createStatement()) {
            s.executeUpdate("DELETE FROM app_form WHERE appId = '" + APP_ID + "'");
            s.executeUpdate("DELETE FROM app_app WHERE appId = '" + APP_ID + "'");
        }
    }

    /**
     * A change to one definition must reach the column set THROUGH the incremental path.
     *
     * <p>{@code incrementalCount} is the observable that separates the two paths: a full build stores
     * a base with 0, an incremental build stores one more than the base it consumed. Asserting the
     * new column alone would pass even if the incremental path silently never engaged and every
     * rebuild was full — which is exactly the failure mode a mocked DAO cannot detect, and how the
     * {@code manifestKeyFor} defect survived 233 unit tests.</p>
     *
     * <p>Falsifiable two ways: run with {@code -Dwflow.formColumnIncrementalEnabled=false} and the
     * count stays 0; break change detection and {@code newcol} never appears.</p>
     */
    @Test
    public void aChangedDefinitionIsPickedUpByTheIncrementalRebuild() throws Exception {
        Collection<String> first = formDataDao().getFormDefinitionColumnNames(TABLE);
        assertTrue("the full build must find the seeded fields: " + first, first.contains("field2"));

        FormColumnCache.ColumnBase base = formColumnCache().getBase(TABLE);
        assertNotNull("a full build must leave a base for the next rebuild to diff against", base);
        assertEquals("a full build's base starts at zero incremental rebuilds",
                0, base.incrementalCount);
        assertTrue("the base must record what it observed", base.observed.size() >= 2);

        // One definition is edited on another node: its json and dateModified both move.
        try (Connection c = dataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE app_form SET json = ?, dateModified = ? WHERE appId = ? AND formId = ?")) {
            ps.setString(1, editedJson());
            ps.setTimestamp(2, new java.sql.Timestamp(HALF_HOUR_AGO));
            ps.setString(3, APP_ID);
            ps.setString(4, "f1");
            ps.executeUpdate();
        }
        // remove(), not reset(): this is the ordinary "a definition changed" invalidation, which
        // KEEPS the base. That is the whole point - reset() here would force a full rebuild and the
        // incremental path would never be exercised.
        formColumnCache().remove(TABLE);

        Collection<String> second = formDataDao().getFormDefinitionColumnNames(TABLE);

        assertTrue("the edited definition's new column must reach the union: " + second,
                second.contains("newcol"));
        FormColumnCache.ColumnBase after = formColumnCache().getBase(TABLE);
        assertNotNull(after);
        assertEquals("the rebuild must have gone through the INCREMENTAL path, not a full read",
                1, after.incrementalCount);
        // Additive by design: the renamed-away field survives in the union until the next full
        // build. Asserted rather than tolerated, so a future change to the merge rule is visible.
        assertTrue("the incremental merge is additive, so field1 must still be present: " + second,
                second.contains("field1"));
    }

    /**
     * With nothing changed, a rebuild must still go incremental and must not lose columns.
     *
     * <p>This is the common case in production — an invalidation caused by some other node's edit to
     * a different table, or by any {@code LongTermCache} eviction — and it is where the optimisation
     * earns its keep: same answer, no definition payloads read.</p>
     */
    @Test
    public void anInvalidationWithNoChangesRebuildsIncrementallyAndKeepsEveryColumn() {
        Collection<String> first = formDataDao().getFormDefinitionColumnNames(TABLE);
        assertEquals(0, formColumnCache().getBase(TABLE).incrementalCount);

        formColumnCache().remove(TABLE);
        Collection<String> second = formDataDao().getFormDefinitionColumnNames(TABLE);

        assertEquals("an unchanged rebuild must take the incremental path",
                1, formColumnCache().getBase(TABLE).incrementalCount);
        assertEquals("an unchanged rebuild must produce the identical column set",
                new java.util.HashSet<String>(first), new java.util.HashSet<String>(second));
    }
}
