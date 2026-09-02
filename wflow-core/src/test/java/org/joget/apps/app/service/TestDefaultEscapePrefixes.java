package org.joget.apps.app.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Pins the outcome of the Hash Variable prefix provenance audit that decides which prefixes get
 * the key derived default escaping in script &amp; SQL typed plugin properties
 * ({@code AppUtil.DEFAULT_ESCAPE_PREFIXES}, consulted by
 * {@link AppUtil#isDefaultEscapeApplicable(String)}). GitLab issue #2271, finding F16.
 *
 * <p>Every registered {@code HashVariablePlugin} on both the community and the enterprise
 * classpath was read and classified by whether a specific code path in its
 * {@code processHashVariable} makes the resolved value depend on end user supplied input. The
 * inclusions below close a real gap; the exclusions are asserted just as deliberately, because
 * escaping a designer authored fragment is precisely what breaks designer composed dynamic
 * script/SQL - the failure mode the prefix list exists to avoid.</p>
 *
 * <p>The three added prefixes are implemented by enterprise only plugins, so a community suite
 * cannot resolve {@code #binder.x.y#} end to end. That end to end plumbing - a prefix in this list
 * actually causing the property map to escape the resolved value - is already covered for
 * {@code requestParam}/{@code form} by {@code TestHashVariableDefaultEscape}; what is specific to
 * this change, and all that is specific to it, is which prefix names the control applies to.</p>
 */
public class TestDefaultEscapePrefixes {

    @Before
    public void setup() {
        //assert the shipped default, not whatever a sibling test left behind
        System.clearProperty(AppUtil.SYSTEM_PROPERTY_HASH_DEFAULT_ESCAPE_PREFIXES);
    }

    @After
    public void clean() {
        System.clearProperty(AppUtil.SYSTEM_PROPERTY_HASH_DEFAULT_ESCAPE_PREFIXES);
    }

    /**
     * The prefixes that were already covered before this change. Guards against a rewrite of the
     * list silently dropping one of them.
     */
    @Test
    public void theAlreadyCoveredEndUserSuppliedPrefixesStayCovered() {
        assertTrue(AppUtil.isDefaultEscapeApplicable("requestParam"));
        assertTrue(AppUtil.isDefaultEscapeApplicable("request"));
        assertTrue(AppUtil.isDefaultEscapeApplicable("form"));
        assertTrue(AppUtil.isDefaultEscapeApplicable("variable"));
        assertTrue(AppUtil.isDefaultEscapeApplicable("currentUser"));
        assertTrue(AppUtil.isDefaultEscapeApplicable("performer"));
        assertTrue(AppUtil.isDefaultEscapeApplicable("user"));
        assertTrue(AppUtil.isDefaultEscapeApplicable("uservariable"));
    }

    /**
     * {@code FormBinderHashVariable} (enterprise, prefix "binder") resolves a stored form field
     * for a primary key taken straight off the request - {@code FormHashVariable
     * .getPrimaryKeyFromRequest()} reads the "id" request parameter/attribute - and returns it via
     * {@code FormUtil.getElementPropertyValues} after running the form's load binders. That is the
     * same "attacker chosen record, end user submitted value" path as the {@code form} prefix,
     * which was already covered.
     */
    @Test
    public void binderIsCoveredBecauseItReturnsStoredFormInputForARequestChosenKey() {
        assertTrue("#binder.FORM.FIELD# resolves end user submitted form data for a primary key "
                + "read off the request, so it must get the default escaping",
                AppUtil.isDefaultEscapeApplicable("binder"));
    }

    /**
     * {@code DatalistHashVariable} (enterprise, prefix "datalist") runs a designer named list and
     * returns the row data itself - {@code DataListService.evaluateColumnValueFromRow} for the
     * "col" formats, and the whole record set for "json"/"html"/"csv". For a form data bound list
     * that data is end user submitted input.
     */
    @Test
    public void datalistIsCoveredBecauseItReturnsStoredRecordData() {
        assertTrue("#datalist.col.csv.LIST.COLUMN# resolves stored record values, so it must get "
                + "the default escaping",
                AppUtil.isDefaultEscapeApplicable("datalist"));
    }

    /**
     * {@code UsersHashVariable} (enterprise, prefix "users") returns
     * username/firstName/lastName/fullName/email for the users of a group, grade, department or
     * organization. Four of those five are user editable profile fields - the same fields the
     * already covered {@code user} and {@code currentUser} prefixes are covered for.
     */
    @Test
    public void usersIsCoveredBecauseItReturnsUserEditableProfileFields() {
        assertTrue("#users.group.X.firstName# resolves user editable profile text, so it must get "
                + "the default escaping",
                AppUtil.isDefaultEscapeApplicable("users"));
    }

    /**
     * The prefix match must be case insensitive, since the prefix arriving here comes from the
     * Hash Variable as the designer typed it.
     */
    @Test
    public void theAddedPrefixesMatchCaseInsensitively() {
        assertTrue(AppUtil.isDefaultEscapeApplicable("BINDER"));
        assertTrue(AppUtil.isDefaultEscapeApplicable("DataList"));
        assertTrue(AppUtil.isDefaultEscapeApplicable("Users"));
    }

    /**
     * Audited and deliberately left out: "process" and "assignment" resolve to engine generated
     * instance ids, process/activity definition names authored by the designer, timestamps,
     * statuses and usernames ({@code WorkflowActivity.getNameOfAcceptedUser()} is populated from
     * {@code CustomWfActivityWrapper.getPerformers()}, ie. usernames, not display names). A
     * username is created through the directory rather than typed in as free text by the end user,
     * so no code path in either plugin carries end user text.
     */
    @Test
    public void processAndAssignmentAreNotCovered() {
        assertFalse("#process...# resolves engine/definition metadata, not end user text",
                AppUtil.isDefaultEscapeApplicable("process"));
        assertFalse("#assignment...# resolves engine/definition metadata, not end user text",
                AppUtil.isDefaultEscapeApplicable("assignment"));
    }

    /**
     * Audited and deliberately left out: "stats" returns counts, APM/governance telemetry, admin
     * created group names and designer authored app/form/list/ui names; "uiListCount" always
     * returns an integer ({@code DataList.getTotal()}), so it cannot carry a payload even though
     * its key is derived from request parameters.
     */
    @Test
    public void statsAndUiListCountAreNotCovered() {
        assertFalse("#stats...# resolves counts and admin/designer authored names",
                AppUtil.isDefaultEscapeApplicable("stats"));
        assertFalse("#uiListCount...# always resolves to an integer",
                AppUtil.isDefaultEscapeApplicable("uiListCount"));
    }

    /**
     * The designer/admin authored sources stay exempt. These are the ones deliberately used to
     * compose dynamic script and SQL, so covering them is the documented non goal - this test is
     * what makes a future widening of the list a conscious decision rather than an accident.
     */
    @Test
    public void designerAuthoredSourcesStayExempt() {
        assertFalse(AppUtil.isDefaultEscapeApplicable("envVariable"));
        assertFalse(AppUtil.isDefaultEscapeApplicable("appVariable"));
        assertFalse(AppUtil.isDefaultEscapeApplicable("sysVariable"));
        assertFalse(AppUtil.isDefaultEscapeApplicable("beanshell"));
        assertFalse(AppUtil.isDefaultEscapeApplicable("i18n"));
        assertFalse(AppUtil.isDefaultEscapeApplicable("appResource"));
        assertFalse(AppUtil.isDefaultEscapeApplicable("appDef"));
        assertFalse(AppUtil.isDefaultEscapeApplicable("platform"));
        assertFalse(AppUtil.isDefaultEscapeApplicable("date"));
        assertFalse(AppUtil.isDefaultEscapeApplicable("dateLocale"));
        assertFalse(AppUtil.isDefaultEscapeApplicable("dateEN"));
        assertFalse(AppUtil.isDefaultEscapeApplicable("exp"));
    }

    /**
     * A deployment can still narrow the list back down, including to the pre audit set, without a
     * code change - which is what makes widening the default reversible.
     */
    @Test
    public void anExplicitOverrideStillWinsOverTheWidenedDefault() {
        System.setProperty(AppUtil.SYSTEM_PROPERTY_HASH_DEFAULT_ESCAPE_PREFIXES,
                "requestParam,request,form,variable,currentUser,performer,user,uservariable");
        assertFalse("an override that omits the new prefixes must be honoured",
                AppUtil.isDefaultEscapeApplicable("binder"));
        assertFalse(AppUtil.isDefaultEscapeApplicable("datalist"));
        assertFalse(AppUtil.isDefaultEscapeApplicable("users"));
        assertTrue(AppUtil.isDefaultEscapeApplicable("requestParam"));
    }

    @Test
    public void anUnknownPrefixIsNeverCovered() {
        assertFalse(AppUtil.isDefaultEscapeApplicable("notAPrefix"));
        assertFalse(AppUtil.isDefaultEscapeApplicable(""));
        assertFalse(AppUtil.isDefaultEscapeApplicable(null));
    }
}
