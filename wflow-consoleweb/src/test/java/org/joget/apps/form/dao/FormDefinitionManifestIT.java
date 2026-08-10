package org.joget.apps.form.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Executes the #2085 manifest projection against a REAL database.
 *
 * <p><b>Why this exists.</b> {@code getFormDefinitionManifestByTableName} is the one piece of this
 * change written as native SQL, and every other test in the change set mocks the DAO — so swapping
 * {@code appId} for {@code formId} in the SELECT, or misreading a column position, would pass the
 * entire suite. It is also the piece with the most dialect exposure: {@code app_form.appVersion} is
 * {@code bigint} on MySQL/PostgreSQL but {@code NUMBER(19,0)} on Oracle and {@code numeric(19,0)} on
 * SQL Server (BigDecimal, not Long), and {@code dateModified} comes back as {@code Timestamp} on
 * most drivers but {@code OffsetDateTime} for PostgreSQL's {@code timestamp with time zone}. Those
 * coercions — {@code FormColumnResolver.asLong} / {@code asDate} — were written from reasoning about
 * driver behaviour and had never actually run against Oracle, SQL Server or PostgreSQL.</p>
 *
 * <p>Run it on all four engines with the matrix rig, which loads each dialect's own install schema:</p>
 * <pre>
 *   bash skills/joget-java-testing/scripts/run-db-matrix.sh -m wflow-consoleweb -t 'FormDefinitionManifestIT'
 * </pre>
 *
 * <p>Seeding goes through plain JDBC rather than the DAO on purpose: the point is to compare what
 * the native projection returns against what is provably IN the table, so the write path must not
 * share code with the read path under test.</p>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class FormDefinitionManifestIT {

    private static final String APP_ID = "manifestIT";
    private static final String TABLE = "manifestit";
    /** Whole seconds: MySQL's {@code datetime} has no sub-second part, so anything finer is lost. */
    private static final long MODIFIED = 1_700_000_000_000L;

    private FormDefinitionDao formDefinitionDao() {
        return (FormDefinitionDao) AppUtil.getApplicationContext().getBean("formDefinitionDao");
    }

    private DataSource dataSource() {
        return (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
    }

    @Before
    public void seed() throws Exception {
        try (Connection c = dataSource().getConnection()) {
            clean(c);
            // `published` is deliberately omitted. It is nullable on all four dialects, and its
            // TYPE is not portable — boolean on PostgreSQL, NUMBER(1,0) on Oracle, integer-ish on
            // MySQL/SQL Server — so any literal binding fails somewhere. The first matrix run
            // proved it: PostgreSQL rejected setInt(0) with "column published is of type boolean
            // but expression is of type integer". Nothing here tests that column.
            try (PreparedStatement app = c.prepareStatement(
                        "INSERT INTO app_app (appId, appVersion, name, dateCreated, dateModified)"
                        + " VALUES (?, ?, ?, ?, ?)");
                 PreparedStatement form = c.prepareStatement(
                        "INSERT INTO app_form (appId, appVersion, formId, name, tableName, json,"
                        + " dateCreated, dateModified) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                java.sql.Timestamp ts = new java.sql.Timestamp(MODIFIED);
                for (int v = 1; v <= 2; v++) {
                    app.setString(1, APP_ID); app.setLong(2, v); app.setString(3, APP_ID);
                    app.setTimestamp(4, ts); app.setTimestamp(5, ts);
                    app.addBatch();
                    form.setString(1, APP_ID); form.setLong(2, v); form.setString(3, "f" + v);
                    form.setString(4, "f" + v); form.setString(5, TABLE);
                    form.setString(6, "{\"className\":\"org.joget.apps.form.model.Form\"}");
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
    }

    private void clean(Connection c) throws Exception {
        try (Statement s = c.createStatement()) {
            s.executeUpdate("DELETE FROM app_form WHERE appId = '" + APP_ID + "'");
            s.executeUpdate("DELETE FROM app_app WHERE appId = '" + APP_ID + "'");
        }
    }

    /**
     * The projection returns the four columns it claims to, in the order the caller reads them.
     *
     * <p>Every assertion here is one a mock cannot make: the column NAMES are resolved by the
     * database, and the value TYPES are chosen by the JDBC driver.</p>
     */
    @Test
    @Transactional
    public void manifestReturnsAppIdVersionFormIdAndDateModifiedInThatOrder() {
        Collection<Object[]> rows = formDefinitionDao().getFormDefinitionManifestByTableName(TABLE);

        assertNotNull(rows);
        assertEquals("one row per definition mapped to the table", 2, rows.size());
        boolean sawV1 = false, sawV2 = false;
        for (Object[] row : rows) {
            assertEquals("column 0 must be appId", APP_ID, row[0]);

            // Position 1 is appVersion. Asserting through asLong is the point: on Oracle and SQL
            // Server the driver hands back a BigDecimal, so a direct (Long) cast throws
            // ClassCastException — which loadDefinitionManifest catches, silently disabling the
            // incremental rebuild on exactly the databases hardest to test.
            Long version = FormColumnResolver.asLong(row[1]);
            assertNotNull("appVersion must coerce to a Long, was " + describe(row[1]), version);
            assertTrue("appVersion must be 1 or 2, was " + version,
                    version == 1L || version == 2L);

            assertEquals("column 2 must be formId", "f" + version, row[2]);

            // Position 3 is dateModified. PostgreSQL's "timestamp with time zone" resolves to
            // OffsetDateTime under Hibernate 6; returning null for it would make every definition
            // look changed on every build, turning this feature into a pessimisation.
            Date modified = FormColumnResolver.asDate(row[3]);
            assertNotNull("dateModified must coerce to a Date, was " + describe(row[3]), modified);
            assertEquals("dateModified must survive the round trip to the second",
                    MODIFIED / 1000L, modified.getTime() / 1000L);

            sawV1 |= version == 1L;
            sawV2 |= version == 2L;
        }
        assertTrue("both app versions must appear", sawV1 && sawV2);
    }

    /**
     * The manifest and the payload loader must agree on WHICH rows belong to a table — they are
     * two different queries against the same predicate, and {@code isComplete} compares their
     * results. If they ever disagree the column cache stops being written at all.
     */
    @Test
    @Transactional
    public void manifestAndPayloadLoaderSelectTheSameRows() {
        Collection<Object[]> manifest = formDefinitionDao().getFormDefinitionManifestByTableName(TABLE);
        Collection<org.joget.apps.app.model.FormDefinition> payload =
                formDefinitionDao().loadFormDefinitionByTableName(TABLE);

        assertEquals("the two queries must select the same row set",
                manifest.size(), payload.size());
        for (org.joget.apps.app.model.FormDefinition def : payload) {
            String key = FormColumnResolver.manifestKeyFor(def);
            boolean found = false;
            for (Object[] row : manifest) {
                if (key.equals(FormColumnResolver.manifestKey(
                        (String) row[0], FormColumnResolver.asLong(row[1]), (String) row[2]))) {
                    found = true;
                    break;
                }
            }
            // This is the assertion that would have caught the getId()/getAppId() defect: the key
            // read off the lazy AppDefinition proxy has to match the key built from the raw
            // columns, and only a real Hibernate proxy can prove it.
            assertTrue("no manifest row corresponds to loaded definition key " + key, found);
        }
    }

    /**
     * The timestamp the payload carries must equal the one the manifest reports for the same row.
     * {@code matchesManifest} compares them for equality, so a driver that rounds one side
     * differently from the other would make every definition look permanently unconfirmed.
     */
    @Test
    @Transactional
    public void theTwoQueriesReportTheSameDateModifiedForARow() {
        Collection<Object[]> manifest = formDefinitionDao().getFormDefinitionManifestByTableName(TABLE);
        Collection<org.joget.apps.app.model.FormDefinition> payload =
                formDefinitionDao().loadFormDefinitionByTableName(TABLE);

        for (org.joget.apps.app.model.FormDefinition def : payload) {
            Date fromPayload = def.getDateModified();
            assertNotNull("the entity must carry a dateModified", fromPayload);
            Date fromManifest = null;
            for (Object[] row : manifest) {
                if (def.getId().equals(row[2])) {
                    fromManifest = FormColumnResolver.asDate(row[3]);
                }
            }
            assertNotNull("no manifest row for " + def.getId(), fromManifest);
            assertEquals("manifest and payload must agree exactly, or nothing is ever confirmed",
                    fromPayload.getTime(), fromManifest.getTime());
        }
    }

    private static String describe(Object value) {
        return (value == null) ? "null" : (value.getClass().getName() + " " + value);
    }
}
