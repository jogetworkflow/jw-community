package org.joget.apps.app.model;

import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.plugin.property.service.PropertyUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Regression test for the default escaping of Hash Variables in plugin properties that are
 * eventually handed to a script interpreter or concatenated into an SQL statement.
 *
 * The plugin property map is the actual mechanism under test: every plugin property is read
 * through a HashVariableSupportedMapImpl which expands the Hash Variables lazily.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class TestHashVariableDefaultEscape {

    private static final String APP_ID = "testHashVariableDefaultEscape";

    /** BeanShell breakout attempt: closes the string literal and appends a statement. */
    private static final String JAVA_PAYLOAD = "x\");Runtime.getRuntime().exec(\"id\");(\"";

    /** Classic SQL injection payload. */
    private static final String SQL_PAYLOAD = "x' OR '1'='1";

    private MockHttpServletRequest request;

    @Before
    public void setup() {
        AppDefinition appDef = TestUtil.createAppDefinition(APP_ID, 1l);
        AppUtil.setCurrentAppDefinition(appDef);
        TestUtil.createEnvVariable(appDef, "tableSuffix", "_main");

        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @After
    public void clean() {
        RequestContextHolder.resetRequestAttributes();
        TestUtil.deleteAllVersions(APP_ID);
    }

    /**
     * Reads a property the same way a plugin does, ie. through the Hash Variable supported map.
     */
    protected String getProperty(String key, String value) {
        Map<String, Object> props = new HashMap<String, Object>();
        props.put(key, value);
        Map<String, Object> hashSupported = PropertyUtil.getHashVariableSupportedMap(props);
        Object result = hashSupported.get(key);
        return (result != null) ? result.toString() : null;
    }

    //
    // (a) an injection payload supplied by the end user is neutralized by default
    //

    @Test
    public void testScriptPropertyEscapesRequestParamByDefault() {
        request.setParameter("q", JAVA_PAYLOAD);

        String result = getProperty("script", "String v = \"#requestParam.q#\"; return v;");

        //the double quote can no longer terminate the java string literal
        Assert.assertFalse("payload broke out of the string literal: " + result, result.contains("x\");"));
        Assert.assertTrue("payload was not java escaped: " + result, result.contains("x\\\");"));
    }

    @Test
    public void testCamelCaseScriptPropertyEscapesRequestParamByDefault() {
        request.setParameter("q", JAVA_PAYLOAD);

        //eg. BeanShellDatalistBinder getDataScript / getTotalScript / getColumnScript
        String result = getProperty("getDataScript", "String v = \"#requestParam.q#\";");

        Assert.assertFalse("payload broke out of the string literal: " + result, result.contains("x\");"));
    }

    @Test
    public void testSqlPropertyEscapesRequestParamByDefault() {
        request.setParameter("id", SQL_PAYLOAD);

        String result = getProperty("sql", "select * from app_fd_test where c_id = '#requestParam.id#'");

        Assert.assertFalse("payload broke out of the SQL literal: " + result, result.contains("x' OR"));
        Assert.assertTrue("payload was not SQL escaped: " + result, result.contains("x'' OR"));
    }

    /**
     * escapeSql() only doubles single quotes; a backslash survives untouched. On MySQL/MariaDB
     * (default sql_mode, NO_BACKSLASH_ESCAPES not set) a backslash is itself an in-literal escape
     * character, so "\' OR 1=1 -- " can still close the literal the doubled quote was meant to
     * keep closed. GitLab issue #2271, finding F1.
     */
    @Test
    public void testSqlPropertyEscapesBackslashQuoteBreakout() {
        request.setParameter("id", "\\' OR 1=1 -- ");

        String result = getProperty("sql", "select * from app_fd_test where c_id = '#requestParam.id#'");

        //assert the POSITIVE property (backslash doubled) rather than the absence of the raw
        //payload: "assertFalse(contains(raw payload))" is also true of the vulnerable output
        //"\'' OR 1=1 -- " (the payload's own text never survives escapeSql's quote-doubling
        //either way), so it would pass on un-fixed code too. This assumes a MySQL/MariaDB test
        //datasource, where backslashIsSqlEscapeChar() doubles the backslash; on a datasource where
        //it does not, the correct output has a single backslash instead.
        Assert.assertTrue("backslash was not doubled, the MySQL/MariaDB literal is still breakable: " + result,
                result.contains("\\\\''"));
    }

    @Test
    public void testExtraConditionAndQueryPropertiesEscapeRequestParamByDefault() {
        request.setParameter("id", SQL_PAYLOAD);

        String extraCondition = getProperty("extraCondition", "and c_id = '#requestParam.id#'");
        Assert.assertTrue("payload was not SQL escaped: " + extraCondition, extraCondition.contains("x'' OR"));

        String query = getProperty("query", "select 1 from dual where c_id = '#requestParam.id#'");
        Assert.assertTrue("payload was not SQL escaped: " + query, query.contains("x'' OR"));
    }

    //
    // (b) benign existing configurations keep producing the exact same output
    //

    @Test
    public void testBenignValuesAreUnchanged() {
        request.setParameter("id", "abc123");
        request.setParameter("name", "John Doe");

        Assert.assertEquals("select * from app_fd_test where c_id = 'abc123'",
                getProperty("sql", "select * from app_fd_test where c_id = '#requestParam.id#'"));
        Assert.assertEquals("String v = \"John Doe\"; return v;",
                getProperty("script", "String v = \"#requestParam.name#\"; return v;"));
        Assert.assertEquals("and c_status = 'abc123'",
                getProperty("extraCondition", "and c_status = '#requestParam.id#'"));
    }

    /**
     * "description" contains the substring "script" and "colorTextDescription" contains it too,
     * so a loose keyword match would wrongly escape plain text properties.
     */
    @Test
    public void testNonScriptAndNonSqlPropertiesAreNotEscaped() {
        request.setParameter("q", JAVA_PAYLOAD);

        Assert.assertTrue(getProperty("description", "#requestParam.q#").contains("x\");"));
        Assert.assertTrue(getProperty("colorTextDescription", "#requestParam.q#").contains("x\");"));
        Assert.assertTrue(getProperty("label", "#requestParam.q#").contains("x\");"));
        Assert.assertTrue(getProperty("url", "#requestParam.q#").contains("x\");"));
    }

    /**
     * Designer controlled sources are not part of the default escaping, they are commonly used to
     * compose dynamic SQL and script fragments.
     */
    @Test
    public void testDesignerControlledHashVariablesAreNotEscaped() {
        Assert.assertEquals("select * from app_fd_test_main",
                getProperty("sql", "select * from app_fd_test#envVariable.tableSuffix#"));
    }

    //
    // opt out & no double escaping
    //

    @Test
    public void testNoEscapeSuffixRestoresRawSubstitution() {
        request.setParameter("q", JAVA_PAYLOAD);

        String result = getProperty("script", "String v = \"#requestParam.q?noescape#\";");
        Assert.assertTrue("?noescape did not return the raw value: " + result, result.contains("x\");"));
    }

    @Test
    public void testExplicitFormatIsNotAppliedTwice() {
        request.setParameter("q", "a\\b");

        //escaped once by the explicit ?java suffix, the default must not escape it again
        Assert.assertEquals("String v = \"a\\\\b\";",
                getProperty("script", "String v = \"#requestParam.q?java#\";"));
    }

    /**
     * "?expression" (TYPE_EXP) doubles single quotes exactly like the "sql" default's own
     * escaping - it satisfies the same guarantee under a different name, so it must not be
     * double-escaped on top of. Before the fix, "hasExplicitFormat" tested exact format-name
     * equality against "sql" and missed this, corrupting the value (each quote doubled twice).
     * GitLab issue #2271, finding G3.
     */
    @Test
    public void testExplicitExpressionFormatIsNotDoubledOnSqlProperty() {
        request.setParameter("id", SQL_PAYLOAD);

        String result = getProperty("sql", "select * from app_fd_test where c_id = '#requestParam.id?expression#'");

        Assert.assertEquals("select * from app_fd_test where c_id = 'x'' OR ''1''=''1'", result);
    }

    /**
     * "?javascript" (TYPE_JAVASCIPT) escapes backslash/quote for a string literal exactly like
     * the "java" default's own escaping - the sane format to write on a JS-mode "script"
     * property. Before the fix it was not string-equal to "java", so it was double-escaped:
     * a single backslash became four, breaking the literal instead of protecting it.
     * GitLab issue #2271, finding G3.
     */
    @Test
    public void testExplicitJavascriptFormatIsNotDoubledOnScriptProperty() {
        request.setParameter("q", "a\\b");

        String result = getProperty("script", "String v = \"#requestParam.q?javascript#\";");

        Assert.assertEquals("String v = \"a\\\\b\";", result);
    }

    /**
     * "?html" (and "?json"/"?url") are accepted "hash escape formats" but give no SQL-literal or
     * java-literal safety guarantee. Before the fix these silently suppressed the key-derived
     * default entirely; now they must be chained with it, not replace it. GitLab issue #2271,
     * finding F11.
     */
    @Test
    public void testExplicitHtmlFormatDoesNotSuppressSqlDefault() {
        request.setParameter("id", SQL_PAYLOAD);

        String result = getProperty("sql", "select * from app_fd_test where c_id = '#requestParam.id?html#'");

        Assert.assertFalse("?html left the SQL literal breakable: " + result, result.contains("x' OR"));
        Assert.assertTrue("SQL default was not applied after ?html: " + result, result.contains("x'' OR"));
    }

    @Test
    public void testExplicitJsonFormatDoesNotSuppressSqlDefault() {
        request.setParameter("id", SQL_PAYLOAD);

        String result = getProperty("sql", "select * from app_fd_test where c_id = '#requestParam.id?json#'");

        Assert.assertFalse("?json left the SQL literal breakable: " + result, result.contains("x' OR"));
        Assert.assertTrue("SQL default was not applied after ?json: " + result, result.contains("x'' OR"));
    }

    @Test
    public void testExplicitHtmlFormatDoesNotSuppressJavaDefault() {
        request.setParameter("q", "a\\");

        String result = getProperty("script", "String v = \"#requestParam.q?html#\";");

        Assert.assertTrue("java default was not applied after ?html: " + result, result.contains("a\\\\"));
    }

    @Test
    public void testDefaultEscapeCanBeDisabledBySystemProperty() {
        request.setParameter("q", JAVA_PAYLOAD);
        System.setProperty(AppUtil.SYSTEM_PROPERTY_HASH_DEFAULT_ESCAPE, "false");
        try {
            String result = getProperty("script", "String v = \"#requestParam.q#\";");
            Assert.assertTrue("kill switch did not restore the raw substitution: " + result, result.contains("x\");"));
        } finally {
            System.clearProperty(AppUtil.SYSTEM_PROPERTY_HASH_DEFAULT_ESCAPE);
        }
    }

    @Test
    public void testPropertyKeyClassification() {
        Assert.assertEquals("java", AppUtil.getPropertyDefaultHashEscapeFormat("script"));
        Assert.assertEquals("java", AppUtil.getPropertyDefaultHashEscapeFormat("getDataScript"));
        Assert.assertEquals("sql", AppUtil.getPropertyDefaultHashEscapeFormat("sql"));
        Assert.assertEquals("sql", AppUtil.getPropertyDefaultHashEscapeFormat("query"));
        Assert.assertEquals("sql", AppUtil.getPropertyDefaultHashEscapeFormat("extraCondition"));
        Assert.assertEquals("sql", AppUtil.getPropertyDefaultHashEscapeFormat("jdbcSql"));
        //snake_case SQL keys of the bundled JDBC Store Binder, which hands these four straight to
        //Connection.prepareStatement and matches none of the camel case suffixes above.
        //GitLab issue #2271, finding F12.
        Assert.assertEquals("sql", AppUtil.getPropertyDefaultHashEscapeFormat("check_sql"));
        Assert.assertEquals("sql", AppUtil.getPropertyDefaultHashEscapeFormat("insert_sql"));
        Assert.assertEquals("sql", AppUtil.getPropertyDefaultHashEscapeFormat("update_sql"));
        Assert.assertEquals("sql", AppUtil.getPropertyDefaultHashEscapeFormat("delete_sql"));
        Assert.assertEquals("sql", AppUtil.getPropertyDefaultHashEscapeFormat("count_query"));
        Assert.assertEquals("sql", AppUtil.getPropertyDefaultHashEscapeFormat("CHECK_SQL"));
        //"_sql"/"_query" must be a SUFFIX, not a substring anywhere in the key
        Assert.assertNull(AppUtil.getPropertyDefaultHashEscapeFormat("sql_label"));
        Assert.assertNull(AppUtil.getPropertyDefaultHashEscapeFormat("query_description"));
        Assert.assertNull(AppUtil.getPropertyDefaultHashEscapeFormat("description"));
        Assert.assertNull(AppUtil.getPropertyDefaultHashEscapeFormat("colorTextDescription"));
        Assert.assertNull(AppUtil.getPropertyDefaultHashEscapeFormat("javascript"));
        Assert.assertNull(AppUtil.getPropertyDefaultHashEscapeFormat(null));
        Assert.assertNull(AppUtil.getPropertyDefaultHashEscapeFormat(""));
    }

    /**
     * The prefix list and the config string it was parsed from are cached together in one
     * immutable holder (GitLab issue #2271, finding F7 - two independent volatile fields could be
     * read torn). This pins the behaviour that holder must preserve: a change to the configured
     * prefix list is picked up, and the previous list stops applying.
     */
    @Test
    public void testEscapePrefixesConfigChangeIsPickedUpByTheCache() {
        request.setParameter("id", SQL_PAYLOAD);

        //baseline: "requestParam" is one of the default prefixes
        Assert.assertTrue(getProperty("sql", "where c = '#requestParam.id#'").contains("x'' OR"));

        System.setProperty(AppUtil.SYSTEM_PROPERTY_HASH_DEFAULT_ESCAPE_PREFIXES, "form,variable");
        try {
            //the new config must take effect, not the previously cached set
            Assert.assertTrue("the previously cached prefix set was still used: ",
                    getProperty("sql", "where c = '#requestParam.id#'").contains("x' OR"));

            System.setProperty(AppUtil.SYSTEM_PROPERTY_HASH_DEFAULT_ESCAPE_PREFIXES, "requestParam");
            Assert.assertTrue("the second config change was not picked up: ",
                    getProperty("sql", "where c = '#requestParam.id#'").contains("x'' OR"));
        } finally {
            System.clearProperty(AppUtil.SYSTEM_PROPERTY_HASH_DEFAULT_ESCAPE_PREFIXES);
        }

        //back to the default list once the override is cleared
        Assert.assertTrue(getProperty("sql", "where c = '#requestParam.id#'").contains("x'' OR"));
    }

    /**
     * End-to-end proof that the snake_case classifier change actually reaches the property map:
     * "check_sql" is a real, in-tree property key of the bundled JDBC Store Binder.
     * GitLab issue #2271, finding F12.
     */
    @Test
    public void testSnakeCaseSqlPropertyEscapesRequestParamByDefault() {
        request.setParameter("id", SQL_PAYLOAD);

        String result = getProperty("check_sql", "select count(*) from app_fd_test where c_id = '#requestParam.id#'");

        Assert.assertFalse("payload broke out of the SQL literal: " + result, result.contains("x' OR"));
        Assert.assertTrue("payload was not SQL escaped: " + result, result.contains("x'' OR"));
    }

    //
    // a "?format" must be one the DESIGNER wrote, never one smuggled in by a nested expansion
    //

    /**
     * The nested hash substitution rewrites the Hash Variable in place BEFORE the "?format"
     * suffix is parsed off it. An attacker who controls the inner selector could therefore make
     * the expansion contribute a "?noescape" the designer never wrote, and switch the default
     * escaping off for their own value. GitLab issue #2271, finding G6.
     */
    @Test
    public void testNestedHashVariableCannotSmuggleNoEscapeSuffix() {
        request.setParameter("inner", "id?noescape");
        request.setParameter("id", SQL_PAYLOAD);

        String result = getProperty("sql", "where c = '#requestParam.{requestParam.inner}#'");

        Assert.assertFalse("a nested expansion smuggled in ?noescape and suppressed the SQL default: "
                + result, result.contains("x' OR"));
        Assert.assertTrue("SQL default was not applied to the nested expansion's value: " + result,
                result.contains("x'' OR"));
    }

    /**
     * The same smuggling attempt on a "script" property.
     */
    @Test
    public void testNestedHashVariableCannotSmuggleNoEscapeSuffixOnScriptProperty() {
        request.setParameter("inner", "q?noescape");
        request.setParameter("q", JAVA_PAYLOAD);

        String result = getProperty("script", "String v = \"#requestParam.{requestParam.inner}#\";");

        Assert.assertFalse("a nested expansion smuggled in ?noescape and suppressed the java default: "
                + result, result.contains("x\");"));
    }

    /**
     * The fix must not break a legitimate nested Hash Variable: the inner value is still resolved
     * and substituted exactly as before, it simply no longer gets to choose the escaping.
     */
    @Test
    public void testLegitimateNestedHashVariableStillResolves() {
        request.setParameter("inner", "id");
        request.setParameter("id", "abc123");

        Assert.assertEquals("where c = 'abc123'",
                getProperty("sql", "where c = '#requestParam.{requestParam.inner}#'"));
    }

    /**
     * A nested value that legitimately contains a "?" must keep working - it is data, and it was
     * never a format suffix either before or after the fix.
     */
    @Test
    public void testNestedHashVariableValueContainingAQuestionMarkStillResolves() {
        request.setParameter("inner", "id");
        request.setParameter("id", "what?now");

        Assert.assertEquals("where c = 'what?now'",
                getProperty("sql", "where c = '#requestParam.{requestParam.inner}#'"));
    }

    /**
     * A "?noescape" the DESIGNER wrote on a Hash Variable that also contains a nested selector is
     * still an explicit opt-out - the fix only rejects a format that appeared out of the
     * expansion, not one written in the property itself.
     */
    @Test
    public void testDesignerWrittenNoEscapeOnANestedHashVariableStillOptsOut() {
        request.setParameter("inner", "id");
        request.setParameter("id", SQL_PAYLOAD);

        String result = getProperty("sql", "where c = '#requestParam.{requestParam.inner}?noescape#'");

        Assert.assertTrue("an explicit designer written ?noescape must still opt out: " + result,
                result.contains("x' OR"));
    }

    //
    // the key derived default must reach every branch of the property map
    //

    /**
     * The String branch of HashVariableSupportedMapImpl passes the key derived default format;
     * the FormRowSet branch used to call a shorter overload and drop it, so a FormRowSet valued
     * "sql"/"script" property was expanded with no default escaping at all.
     * GitLab issue #2271, finding F15.
     */
    @Test
    public void testFormRowSetPropertyValueAlsoGetsTheKeyDerivedDefault() {
        request.setParameter("id", SQL_PAYLOAD);

        FormRow row = new FormRow();
        row.setProperty("statement", "select * from app_fd_test where c_id = '#requestParam.id#'");
        FormRowSet rowSet = new FormRowSet();
        rowSet.add(row);

        Map<String, Object> props = new HashMap<String, Object>();
        props.put("sql", rowSet);
        Map<String, Object> hashSupported = PropertyUtil.getHashVariableSupportedMap(props);

        FormRowSet result = (FormRowSet) hashSupported.get("sql");
        String statement = result.get(0).getProperty("statement");

        Assert.assertFalse("payload broke out of the SQL literal: " + statement, statement.contains("x' OR"));
        Assert.assertTrue("the FormRowSet branch dropped the key derived default: " + statement,
                statement.contains("x'' OR"));
    }
}
