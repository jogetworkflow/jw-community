package org.joget.apps.form.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.TestUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Regression suite for the dynamically created form data columns, written to pin the CURRENT
 * correct behaviour before the #2253 performance work changes how schema maintenance runs.
 *
 * <p><b>Why this exists.</b> #2253 is about the cost of Joget's {@code hbm2ddl} pass: every time a
 * form {@code SessionFactory} is rebuilt, {@code FormDataDaoImpl.internalUpdateSchema()} runs a
 * Hibernate {@code SchemaUpdate}, and Hibernate's default {@code grouped} metadata extraction
 * reads the column metadata of the WHOLE database to do it. The intended fix narrows or skips
 * that read. Every candidate fix therefore trades away metadata that the DDL step currently has
 * available to it, and the failure mode of getting that wrong is not a slow request — it is a
 * column that never gets created and form data that is silently dropped.</p>
 *
 * <p>So these tests assert the observable contract that must survive the optimisation:</p>
 * <ol>
 *   <li>a new form's fields become real physical columns, and values round-trip;</li>
 *   <li>a field ADDED to an existing form becomes a real column, driven by the form definition
 *       alone (the read path) and by a submitted row (the write path), without disturbing rows
 *       already stored;</li>
 *   <li>a field REMOVED from a form does not drop its column or its data;</li>
 *   <li>digit-prefixed field names — which are mapped under a {@code t__} property alias, so the
 *       Hibernate property name and the physical column name deliberately differ — still resolve.
 *       This is the case most likely to break an optimisation that decides "nothing to add" by
 *       comparing mapping property names against physical column names;</li>
 *   <li>repeated schema changes each land, not just the first;</li>
 *   <li>several forms sharing one table produce the union of their columns.</li>
 * </ol>
 *
 * <p><b>Portability.</b> Joget ships on MariaDB/MySQL, PostgreSQL, SQL Server and Oracle, and the
 * DDL path is exactly the kind of code that behaves differently on each. Everything here is
 * dialect-neutral: the physical schema is inspected with {@code SELECT * ... WHERE 1=0} and
 * {@code ResultSetMetaData} rather than {@code information_schema} (which SQL Server and Oracle
 * spell differently), and identifiers are compared case-insensitively because Oracle folds
 * unquoted names to upper case and PostgreSQL to lower. Run the whole suite with:
 *
 * <pre>bash skills/joget-java-testing/scripts/run-db-matrix.sh -m wflow-core -t 'FormSchemaEvolutionTest'</pre>
 *
 * <p><b>What this suite deliberately does NOT do.</b> It does not assert the performance property
 * itself — that the metadata read is no longer whole-catalog. That is measured against a real
 * SQL Server by {@code skills/_shared/tests/issue2253/}, because the row count of the driver's
 * {@code getColumns()} call is the metric and it is only observable server-side. A green run here
 * means the fix did not break correctness; it does not mean the fix worked.</p>
 *
 * @see <a href="https://gitlab.joget.org/joget/jw-community/-/issues/2253">#2253</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class FormSchemaEvolutionTest {

    private static final String APP_ID = "testSchemaEvolution";
    private static final Long APP_VERSION = 1L;
    private static final String TABLE = "schemaevo";
    private static final String FORM_ID = "schemaEvoForm";

    @Autowired
    private FormDataDao formDataDao;

    @Autowired
    private FormDefinitionDao formDefinitionDao;

    private AppDefinition appDef;

    /**
     * Oracle needs Joget's PATCHED Hibernate dialect, and on some branches that patch is not on
     * the unit-test classpath.
     *
     * <p>Joget maps every dynamic form column as Hibernate type {@code text}, i.e.
     * {@code Types.LONGVARCHAR}, which STOCK Hibernate's Oracle dialect renders as {@code long}.
     * Oracle permits at most ONE {@code LONG} column per table, so a form with two fields emits
     * {@code create table ... (c_title long, c_status long, ...)} and the CREATE fails. Hibernate's
     * {@code SchemaExport} logs DDL failures instead of throwing, so the table simply never
     * appears and every later statement dies with {@code ORA-00942 table or view does not exist} —
     * a confusing symptom several steps downstream of the cause.
     *
     * <p>The shipped product does not have this problem: Joget overrides the Oracle dialect to
     * register {@code varchar2(4000)} for that type, which is why a real deployment on Oracle
     * creates {@code C_TITLE VARCHAR2(4000)} and works. That override lives in
     * {@code wflow-commons/src/main/java/org/hibernate/dialect/} on 9.1-SNAPSHOT — where this
     * test runs for real on Oracle — but is absent from the 8.2-SNAPSHOT tree, so the 8.2
     * {@code wflow-commons} jar puts no dialect on the test classpath at all.
     *
     * <p>Rather than let that surface as a mystery {@code ORA-00942}, skip with the reason. A skip
     * is NOT a pass: when this triggers, Oracle coverage for this behaviour comes from the
     * deployed-WAR check in {@code skills/_shared/tests/issue2253/oracle-runtime-check.sh}, which
     * exercises the same paths against a real Joget container on Oracle.
     */
    private void skipOnOracleWithoutJogetsPatchedDialect() throws Exception {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection conn = null;
        String product;
        try {
            conn = ds.getConnection();
            product = conn.getMetaData().getDatabaseProductName();
        } finally {
            close(null, null, conn);
        }
        if (product != null && product.toLowerCase().contains("oracle")) {
            boolean patched = true;
            try {
                Class.forName("org.hibernate.dialect.CustomOracleDialect");
            } catch (ClassNotFoundException e) {
                patched = false;
            }
            Assume.assumeTrue("SKIPPED on Oracle: Joget's patched Hibernate Oracle dialect is not on"
                    + " the test classpath, so form columns would be emitted as Oracle LONG (only one"
                    + " allowed per table) and no table would be created. This is a classpath gap on"
                    + " this branch, NOT a product defect — verify Oracle with"
                    + " skills/_shared/tests/issue2253/oracle-runtime-check.sh instead.", patched);
        }
    }

    @Before
    public void setUp() throws Exception {
        // First, because JUnit 4 does not order multiple @Before methods.
        skipOnOracleWithoutJogetsPatchedDialect();

        // Start from no table at all. Reusing a table left behind by an earlier run would make
        // "the column was added" pass without the column ever having been added THIS run — a
        // false green that is worse than no test.
        TestUtil.deleteAllVersions(APP_ID);
        TestUtil.cleanFormMappingFile();
        dropTableQuietly(FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + TABLE);
        resetAllDaoCaches();

        appDef = TestUtil.createAppDefinition(APP_ID, APP_VERSION);
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.deleteAllVersions(APP_ID);
        TestUtil.cleanFormMappingFile();
        dropTableQuietly(FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + TABLE);
        resetAllDaoCaches();
    }

    /**
     * Drops EVERY cached SessionFactory, not just this table's.
     *
     * <p>{@code clearFormTableCache(table)} is not enough here, and the reason is worth stating:
     * it evicts the load-side key {@code <profile>;app_fd_<table>;app_fd_<table>;} only. The
     * store side is cached under a different key that also carries the submitted row's key set
     * ({@code getSessionFactoryCacheKey}), and those entries survive. That is harmless in
     * production — nothing drops a table behind the DAO's back — but this suite does exactly
     * that, and a surviving store-side factory then inserts into a table that no longer exists.
     * Two tests failed that way before this was added, which is a harness bug, not a product
     * one.</p>
     */
    private void resetAllDaoCaches() {
        // formDataDao is a JDK dynamic proxy (transaction advice) and clearCache() is not on the
        // FormDataDao interface, so it has to be reached on the target object.
        FormDataDao target = AopTestUtils.getUltimateTargetObject(formDataDao);
        Assert.assertTrue("expected to unwrap a FormDataDaoImpl, got " + target.getClass(),
                target instanceof FormDataDaoImpl);
        ((FormDataDaoImpl) target).clearCache();
    }

    // ------------------------------------------------------------------ 1. creation

    @Test
    public void newFormFieldsBecomePhysicalColumnsAndValuesRoundTrip() throws Exception {
        createForm(FORM_ID, "title", "status");
        Assert.assertFalse("app_fd_" + TABLE + " exists before this test created anything — it would"
                + " assert against a previous run's columns",
                tableExists(FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + TABLE));

        store("row1", new String[]{"title", "Hello"}, new String[]{"status", "open"});

        Set<String> columns = physicalColumns(TABLE);
        Assert.assertTrue("c_title missing; created columns were " + columns, columns.contains("c_title"));
        Assert.assertTrue("c_status missing; created columns were " + columns, columns.contains("c_status"));

        FormRow loaded = formDataDao.load(FORM_ID, TABLE, "row1");
        Assert.assertNotNull("row1 was not stored at all", loaded);
        Assert.assertEquals("Hello", loaded.getProperty("title"));
        Assert.assertEquals("open", loaded.getProperty("status"));
    }

    // ------------------------------------------------------------------ 2. the #2253 case

    /**
     * The read path: a field added to the form DEFINITION must produce a physical column on the
     * next load, before anything is written. This is the path that resolves columns from
     * {@code getFormDefinitionColumnNames()} and is the one an over-eager "nothing changed, skip
     * the DDL" optimisation would break.
     */
    @Test
    public void fieldAddedToTheDefinitionCreatesItsColumnOnRead() throws Exception {
        createForm(FORM_ID, "title", "status");
        store("row1", new String[]{"title", "Before"}, new String[]{"status", "open"});
        Assert.assertFalse("c_added exists before the schema change — the test proves nothing",
                physicalColumns(TABLE).contains("c_added"));

        redefineForm(FORM_ID, "title", "status", "added");

        // A read is enough: the mapping is regenerated from the definition and the DDL follows.
        formDataDao.find(FORM_ID, TABLE, null, null, null, null, null, null);

        Set<String> columns = physicalColumns(TABLE);
        Assert.assertTrue("the added field did not become a column; columns were " + columns,
                columns.contains("c_added"));
    }

    /**
     * The write path, and the data-safety half of the contract: storing a row that carries the new
     * field must persist it, and must not disturb rows written before the schema changed.
     */
    @Test
    public void fieldAddedToTheDefinitionStoresItsValueAndPreservesExistingRows() throws Exception {
        createForm(FORM_ID, "title", "status");
        store("row1", new String[]{"title", "Before"}, new String[]{"status", "open"});

        redefineForm(FORM_ID, "title", "status", "added");
        store("row2", new String[]{"title", "After"}, new String[]{"status", "open"},
                new String[]{"added", "NewValue"});

        FormRow after = formDataDao.load(FORM_ID, TABLE, "row2");
        Assert.assertNotNull("row2 was not stored", after);
        Assert.assertEquals("the value written to the newly added column did not persist",
                "NewValue", after.getProperty("added"));

        FormRow before = formDataDao.load(FORM_ID, TABLE, "row1");
        Assert.assertNotNull("the row stored before the schema change was lost", before);
        Assert.assertEquals("Before", before.getProperty("title"));
        Assert.assertEquals("open", before.getProperty("status"));
    }

    // ------------------------------------------------------------------ 3. no destructive DDL

    /**
     * Hibernate's {@code SchemaUpdate} is additive and never drops a column, and Joget relies on
     * that: a field removed from a form must leave both the column and the data it holds alone,
     * so that removing a field is reversible and does not destroy customer data. An optimisation
     * that swaps {@code SchemaUpdate} for something that reconciles the table to the mapping would
     * pass every other test here and fail this one.
     */
    @Test
    public void fieldRemovedFromTheDefinitionKeepsItsColumnAndData() throws Exception {
        createForm(FORM_ID, "title", "status", "doomed");
        store("row1", new String[]{"title", "Keep"}, new String[]{"status", "open"},
                new String[]{"doomed", "PreciousData"});
        Assert.assertTrue(physicalColumns(TABLE).contains("c_doomed"));

        redefineForm(FORM_ID, "title", "status");
        formDataDao.find(FORM_ID, TABLE, null, null, null, null, null, null);

        Set<String> columns = physicalColumns(TABLE);
        Assert.assertTrue("c_doomed was dropped — removing a form field must not destroy data; "
                + "columns were " + columns, columns.contains("c_doomed"));
        Assert.assertEquals("the removed field's data was destroyed",
                "PreciousData", rawColumnValue(TABLE, "c_doomed", "row1"));
    }

    // ------------------------------------------------------------------ 4. aliased property names

    /**
     * A field whose id starts with a digit cannot be a Hibernate property name, so the mapping
     * aliases it to {@code t__<name>} while the physical column stays {@code c_<name>}. Property
     * name and column name therefore differ ON PURPOSE for these fields.
     *
     * <p>This is the trap for the intended #2253 fix. A cheap "does the table already have every
     * column this mapping needs?" check that compares Hibernate property names against physical
     * column names will conclude that {@code t__1stfield} is missing and re-run the DDL forever,
     * or — worse, if it strips prefixes wrongly — conclude nothing is missing and never create the
     * column at all.</p>
     */
    @Test
    public void digitPrefixedFieldNamesStillMapAndRoundTrip() throws Exception {
        // Deliberately added by a schema CHANGE rather than declared up front. Creating the table
        // fresh goes through SchemaExport.createOnly, which emits every column in one CREATE TABLE
        // and so never exercises the ALTER path this issue is about — an earlier version of this
        // test declared the aliased fields at creation time and kept passing while the DDL update
        // was disabled outright.
        createForm(FORM_ID, "title");
        store("row0", new String[]{"title", "seed"});
        Assert.assertFalse("c_1stfield exists before the schema change — the test proves nothing",
                physicalColumns(TABLE).contains("c_1stfield"));

        redefineForm(FORM_ID, "title", "1stfield", "2ndfield");

        store("row1", new String[]{"title", "Aliased"}, new String[]{"1stfield", "one"},
                new String[]{"2ndfield", "two"});

        Set<String> columns = physicalColumns(TABLE);
        Assert.assertTrue("c_1stfield missing; columns were " + columns, columns.contains("c_1stfield"));
        Assert.assertTrue("c_2ndfield missing; columns were " + columns, columns.contains("c_2ndfield"));

        // Through the read path, which is where the t__ alias is applied.
        FormRow reread = formDataDao.load(FORM_ID, TABLE, "row1");
        Assert.assertNotNull("row1 was not stored", reread);
        Assert.assertEquals("one", reread.getProperty("1stfield"));
        Assert.assertEquals("two", reread.getProperty("2ndfield"));

        // And the values really are in the aliased columns, not somewhere else that happens to read back.
        Assert.assertEquals("one", rawColumnValue(TABLE, "c_1stfield", "row1"));
        Assert.assertEquals("two", rawColumnValue(TABLE, "c_2ndfield", "row1"));
    }

    // ------------------------------------------------------------------ 5. repeated evolution

    /**
     * Three successive additions. A fix that caches "this table is up to date" too aggressively
     * typically lands the first change and silently drops the rest.
     */
    @Test
    public void repeatedSchemaChangesEachCreateTheirColumn() throws Exception {
        createForm(FORM_ID, "title");
        store("row0", new String[]{"title", "v0"});

        for (int i = 1; i <= 3; i++) {
            String field = "extra" + i;
            String[] fields = new String[i + 1];
            fields[0] = "title";
            for (int j = 1; j <= i; j++) {
                fields[j] = "extra" + j;
            }
            redefineForm(FORM_ID, fields);

            store("row" + i, new String[]{"title", "v" + i}, new String[]{field, "value" + i});

            Set<String> columns = physicalColumns(TABLE);
            Assert.assertTrue("round " + i + ": c_" + field + " was never created; columns were " + columns,
                    columns.contains("c_" + field));
            FormRow row = formDataDao.load(FORM_ID, TABLE, "row" + i);
            Assert.assertNotNull("round " + i + ": row was not stored", row);
            Assert.assertEquals("round " + i + ": value did not persist in the new column",
                    "value" + i, row.getProperty(field));
        }

        // every column from every round must still be there
        Set<String> columns = physicalColumns(TABLE);
        for (int i = 1; i <= 3; i++) {
            Assert.assertTrue("c_extra" + i + " disappeared by the end; columns were " + columns,
                    columns.contains("c_extra" + i));
        }
    }

    // ------------------------------------------------------------------ 6. shared table

    /**
     * Several forms can write to one table; the physical table must carry the UNION of their
     * fields. The column set for the table is resolved from all form definitions mapped to it, so
     * an optimisation that narrows the metadata read to "the form being saved" rather than "the
     * table being written" would drop the other forms' columns.
     */
    @Test
    public void formsSharingATableProduceTheUnionOfTheirColumns() throws Exception {
        createForm("schemaEvoA", "title", "onlyInA");
        createForm("schemaEvoB", "title", "onlyInB");
        Assert.assertFalse("app_fd_" + TABLE + " exists before this test created anything — the"
                + " union assertion would be measuring a previous run",
                tableExists(FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + TABLE));

        // touch the table through each form
        store("schemaEvoA", "rowA", new String[]{"title", "A"}, new String[]{"onlyInA", "a"});
        store("schemaEvoB", "rowB", new String[]{"title", "B"}, new String[]{"onlyInB", "b"});

        Collection<String> defined = formDataDao.getFormDefinitionColumnNames(TABLE);
        Assert.assertNotNull("the table's column set could not be resolved", defined);
        Assert.assertTrue("onlyInA missing from the resolved column set " + defined, defined.contains("onlyInA"));
        Assert.assertTrue("onlyInB missing from the resolved column set " + defined, defined.contains("onlyInB"));

        Set<String> columns = physicalColumns(TABLE);
        Assert.assertTrue("c_onlyina missing; columns were " + columns, columns.contains("c_onlyina"));
        Assert.assertTrue("c_onlyinb missing; columns were " + columns, columns.contains("c_onlyinb"));

        Assert.assertEquals("a", rawColumnValue(TABLE, "c_onlyInA", "rowA"));
        Assert.assertEquals("b", rawColumnValue(TABLE, "c_onlyInB", "rowB"));
    }

    // ================================================================== helpers

    private void createForm(String formId, String... fieldIds) throws Exception {
        FormDefinition formDef = new FormDefinition();
        formDef.setId(formId);
        formDef.setAppId(appDef.getAppId());
        formDef.setAppVersion(appDef.getVersion());
        formDef.setAppDefinition(appDef);
        formDef.setName(formId);
        formDef.setTableName(TABLE);
        formDef.setJson(formJson(formId, fieldIds));
        formDefinitionDao.add(formDef);
        formDataDao.clearFormTableCache(TABLE);
    }

    /** Rewrites the form's field list, the way publishing an edited form does. */
    private void redefineForm(String formId, String... fieldIds) throws Exception {
        FormDefinition formDef = formDefinitionDao.loadById(formId, appDef);
        Assert.assertNotNull("form " + formId + " not found; the test setup is broken", formDef);
        formDef.setJson(formJson(formId, fieldIds));
        formDefinitionDao.update(formDef);
        formDataDao.clearFormTableCache(TABLE);
    }

    private void store(String rowId, String[]... properties) {
        store(FORM_ID, rowId, properties);
    }

    private void store(String formId, String rowId, String[]... properties) {
        FormRow row = new FormRow();
        row.setId(rowId);
        for (String[] property : properties) {
            row.setProperty(property[0], property[1]);
        }
        FormRowSet rowSet = new FormRowSet();
        rowSet.add(row);
        formDataDao.saveOrUpdate(formId, TABLE, rowSet);
    }

    private static String formJson(String formId, String... fieldIds) {
        StringBuilder fields = new StringBuilder();
        for (int i = 0; i < fieldIds.length; i++) {
            if (i > 0) {
                fields.append(",");
            }
            fields.append("{\"className\":\"org.joget.apps.form.lib.TextField\",\"properties\":{")
                  .append("\"id\":\"").append(fieldIds[i]).append("\",")
                  .append("\"label\":\"").append(fieldIds[i]).append("\"}}");
        }
        return "{\"className\":\"org.joget.apps.form.model.Form\",\"properties\":{"
                + "\"id\":\"" + formId + "\",\"name\":\"" + formId + "\","
                + "\"tableName\":\"" + TABLE + "\","
                + "\"loadBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"},"
                + "\"storeBinder\":{\"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\"}},"
                + "\"elements\":[{\"className\":\"org.joget.apps.form.model.Section\",\"properties\":{"
                + "\"id\":\"section1\",\"label\":\"Section\"},"
                + "\"elements\":[{\"className\":\"org.joget.apps.form.model.Column\",\"properties\":{\"width\":\"100%\"},"
                + "\"elements\":[" + fields + "]}]}]}";
    }

    /**
     * The physical column names of a form data table, lower-cased.
     *
     * <p>Read through {@code ResultSetMetaData} rather than {@code information_schema} or
     * {@code DatabaseMetaData.getColumns}: {@code information_schema} is not spelled the same on
     * SQL Server and does not exist on Oracle, and {@code getColumns} needs the table name in the
     * dialect's own identifier case (Oracle folds unquoted names upper, PostgreSQL lower), which
     * is exactly the kind of per-dialect branching this suite is meant to avoid. {@code WHERE 1=0}
     * is valid on all four engines and returns no rows.</p>
     */
    private Set<String> physicalColumns(String tableName) throws SQLException {
        Set<String> columns = new LinkedHashSet<String>();
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM " + FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + tableName + " WHERE 1=0");
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                columns.add(md.getColumnLabel(i).toLowerCase());
            }
        } finally {
            close(rs, stmt, conn);
        }
        return columns;
    }

    /**
     * Reads one column of one row with plain JDBC, bypassing the Hibernate mapping entirely.
     *
     * <p>Asserting through {@code formDataDao.load()} alone is not enough for the aliased-column
     * cases: a mapping that reads and writes the same wrong column round-trips perfectly and still
     * has the data in the wrong place. This looks at the table itself.</p>
     */
    private String rawColumnValue(String tableName, String columnName, String rowId) throws SQLException {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection conn = null;
        java.sql.PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            stmt = conn.prepareStatement("SELECT " + columnName + " FROM "
                    + FormDataDaoImpl.FORM_PREFIX_TABLE_NAME + tableName + " WHERE id = ?");
            stmt.setString(1, rowId);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } finally {
            close(rs, stmt, conn);
        }
    }

    /**
     * Best-effort DROP. There is no portable {@code DROP TABLE IF EXISTS} across all four engines
     * (Oracle has none), so the failure when the table is absent is expected and swallowed.
     */
    private void dropTableQuietly(String tableName) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate("DROP TABLE " + tableName);
        } catch (SQLException e) {
            // Expected when the table does not exist. NOT proof of anything on its own — the
            // assertion below is what makes the drop trustworthy.
        } finally {
            close(null, stmt, conn);
        }
        // Swallowing every SQLException would also swallow "could not drop, it is locked", and the
        // suite would then assert against a table left over from an earlier run — a column created
        // last time reads exactly like a column created this time.
        Assert.assertFalse("could not drop " + tableName + "; every assertion in this test would be"
                + " measuring a previous run's table", tableExists(tableName));
    }

    /** True if the table can be selected from. Dialect-neutral: WHERE 1=0 is valid on all four. */
    private boolean tableExists(String tableName) {
        DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM " + tableName + " WHERE 1=0");
            return true;
        } catch (SQLException e) {
            return false;
        } finally {
            close(rs, stmt, conn);
        }
    }

    private static void close(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { /* ignore */ }
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException e) { /* ignore */ }
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) { /* ignore */ }
        }
    }
}
