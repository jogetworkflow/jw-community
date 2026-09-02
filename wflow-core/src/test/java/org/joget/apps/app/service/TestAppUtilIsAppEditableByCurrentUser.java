package org.joget.apps.app.service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.TestUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Regression test for the GitLab issue #2271 Issue 9 backport of
 * {@link AppUtil#isAppEditableByCurrentUser(AppDefinition, Boolean)} to the 8.x line, used by
 * {@code InformationTileComponent}'s design-time "columns" API to replace a
 * {@code ROLE_ADMIN}-only check that wrongly excluded delegate app designers.
 *
 * This backport intentionally drops the 9.x overload's "app creator" branch: 8.x's
 * {@code AppDefinition} has no created-by owner field and {@code WorkflowUtil} has no
 * {@code ROLE_APP_CREATOR}, so only admin and the custom "Delegate app designer" role/group
 * assignment are covered here.
 *
 * <p>Also covers finding F4 of the same issue: the per-session memoization of that decision used
 * to be discarded only when the session's cached username changed, so removing a user from
 * "Delegate app designer" left them authorized for the remaining lifetime of their session. Now
 * that this method is the sole authorization control on a data-returning API, the memoization is
 * bounded by a short TTL.</p>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class TestAppUtilIsAppEditableByCurrentUser {

    private static final String APP_ID = "testAppEditableByCurrentUser";

    private MockHttpServletRequest request;
    private AppDefinition appDef;

    @Before
    public void setup() {
        appDef = TestUtil.createAppDefinition(APP_ID, 1l);
        AppUtil.setCurrentAppDefinition(appDef);

        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        System.clearProperty(AppUtil.SYSTEM_PROPERTY_USER_EDITABLE_APPS_CACHE_TTL);
    }

    @After
    public void clean() {
        System.clearProperty(AppUtil.SYSTEM_PROPERTY_USER_EDITABLE_APPS_CACHE_TTL);
        RequestContextHolder.resetRequestAttributes();
        TestUtil.deleteAllVersions(APP_ID);
    }

    /**
     * Puts a decision into the session cache by hand, the way an earlier request that still had
     * the "Delegate app designer" assignment would have. The seeded value is deliberately the
     * OPPOSITE of what a fresh evaluation returns for this app (a freshly created test app has no
     * assignment on disk, so a real evaluation returns false), which is what lets these tests tell
     * "cache honoured" apart from "cache ignored" without mocking anything.
     *
     * @param populatedAt the value for the cache timestamp attribute, or null to leave the
     *      attribute unset - ie. a session that predates the TTL attribute entirely
     */
    private void seedCachedDecision(boolean editable, Long populatedAt) {
        Map<String, Boolean> cached = Collections.synchronizedMap(new LinkedHashMap<String, Boolean>());
        cached.put(APP_ID, editable);

        HttpSession session = request.getSession();
        session.setAttribute(AppUtil.SESSION_ATTRIBUTE_USER_EDITABLE_APPS, cached);
        session.setAttribute(AppUtil.SESSION_ATTRIBUTE_USER_EDITABLE_APPS_USERNAME, WorkflowUtil.getCurrentUsername());
        if (populatedAt != null) {
            session.setAttribute(AppUtil.SESSION_ATTRIBUTE_USER_EDITABLE_APPS_TIME, populatedAt);
        } else {
            session.removeAttribute(AppUtil.SESSION_ATTRIBUTE_USER_EDITABLE_APPS_TIME);
        }
    }

    @Test
    public void adminIsAlwaysEditable() {
        assertTrue("an admin must always be allowed to edit the app",
                AppUtil.isAppEditableByCurrentUser(appDef, true));
    }

    @Test
    public void nonAdminWithNoDelegateDesignerAssignmentIsNotEditable() {
        // a freshly created test app has no "Delegate app designer" role/group assignment on disk
        assertFalse("a non-admin user with no delegate-designer assignment must not be allowed to "
                + "edit the app - this is exactly the check InformationTileComponent's design-time "
                + "columns API now relies on instead of a bare ROLE_ADMIN check",
                AppUtil.isAppEditableByCurrentUser(appDef, false));
    }

    @Test
    public void resultIsCachedPerSessionPerApp() {
        // first call computes and caches the result in the user's own HttpSession
        assertFalse(AppUtil.isAppEditableByCurrentUser(appDef, false));
        // second call for the same app must return the cached value without recomputation
        assertFalse(AppUtil.isAppEditableByCurrentUser(appDef, false));

        Object cached = request.getSession().getAttribute(AppUtil.SESSION_ATTRIBUTE_USER_EDITABLE_APPS);
        assertNotNull("result must be cached on the caller's own HttpSession, "
                + "not on any shared/static state", cached);
        assertNotNull("the cache must record when it was populated, otherwise its lifetime cannot "
                + "be bounded", request.getSession().getAttribute(AppUtil.SESSION_ATTRIBUTE_USER_EDITABLE_APPS_TIME));
    }

    /**
     * The TTL must not defeat the point of the cache: within the window, a memoized decision is
     * still returned as-is, without re-reading the app's role/group assignments (and, for a group
     * assignment, without re-querying the directory) on every check.
     */
    @Test
    public void aCachedEditableResultIsHonouredWithinTheTtl() {
        seedCachedDecision(true, System.currentTimeMillis());

        assertTrue("a decision cached within the TTL must be returned from the cache - a fresh "
                + "evaluation of this app would have returned false, so returning true proves the "
                + "cache was honoured and not needlessly re-evaluated",
                AppUtil.isAppEditableByCurrentUser(appDef, false));
    }

    /**
     * F4 proper: once the TTL has elapsed the stale "editable" decision must be discarded and
     * re-evaluated, so a user whose "Delegate app designer" assignment was revoked stops being
     * authorized instead of staying authorized for the rest of their session.
     */
    @Test
    public void aCachedEditableResultIsDiscardedOnceTheTtlHasElapsed() {
        seedCachedDecision(true, System.currentTimeMillis() - (AppUtil.DEFAULT_USER_EDITABLE_APPS_CACHE_TTL + 1000L));

        assertFalse("a decision cached longer ago than the TTL must be discarded and re-evaluated "
                + "- otherwise revoking a delegate-designer assignment leaves the user authorized "
                + "for the remaining lifetime of their session",
                AppUtil.isAppEditableByCurrentUser(appDef, false));
    }

    /**
     * A session created before this attribute existed (or deserialized without it) has no way to
     * prove its cache is fresh, so it must be treated as expired rather than as "never expires".
     */
    @Test
    public void aCachedResultWithNoTimestampIsTreatedAsExpired() {
        seedCachedDecision(true, null);

        assertFalse("a cache with no populated-at timestamp must fail closed and be re-evaluated",
                AppUtil.isAppEditableByCurrentUser(appDef, false));
    }

    /**
     * If the wall clock moved backwards the real age of the cache is unknowable, so it must be
     * treated as expired rather than as valid for however long the clock is out by.
     */
    @Test
    public void aCachedResultTimestampedInTheFutureIsTreatedAsExpired() {
        seedCachedDecision(true, System.currentTimeMillis() + (10L * 60L * 1000L));

        assertFalse("a populated-at timestamp in the future must fail closed and be re-evaluated",
                AppUtil.isAppEditableByCurrentUser(appDef, false));
    }

    /**
     * The TTL is deployment configurable, so a site that needs revocation to take effect
     * immediately can turn the memoization off, and one that needs the memoization can widen it -
     * without a code change either way.
     */
    @Test
    public void theTtlIsConfigurableBySystemProperty() {
        long twoMinutesAgo = System.currentTimeMillis() - (2L * 60L * 1000L);

        System.setProperty(AppUtil.SYSTEM_PROPERTY_USER_EDITABLE_APPS_CACHE_TTL, "600000");
        seedCachedDecision(true, twoMinutesAgo);
        assertTrue("a widened TTL must be honoured - two minutes is past the 60s default but well "
                + "within the configured ten minutes",
                AppUtil.isAppEditableByCurrentUser(appDef, false));

        System.setProperty(AppUtil.SYSTEM_PROPERTY_USER_EDITABLE_APPS_CACHE_TTL, "0");
        seedCachedDecision(true, System.currentTimeMillis());
        assertFalse("a TTL of 0 must disable the memoization entirely",
                AppUtil.isAppEditableByCurrentUser(appDef, false));
    }

    /**
     * An unparseable or negative override must not silently become "never expires".
     */
    @Test
    public void anInvalidTtlOverrideFallsBackToTheDefaultAndANegativeOneDisablesTheCache() {
        System.setProperty(AppUtil.SYSTEM_PROPERTY_USER_EDITABLE_APPS_CACHE_TTL, "not-a-number");
        seedCachedDecision(true, System.currentTimeMillis() - (AppUtil.DEFAULT_USER_EDITABLE_APPS_CACHE_TTL + 1000L));
        assertFalse("an unparseable TTL must fall back to the default, not to 'never expires'",
                AppUtil.isAppEditableByCurrentUser(appDef, false));

        System.setProperty(AppUtil.SYSTEM_PROPERTY_USER_EDITABLE_APPS_CACHE_TTL, "-1");
        seedCachedDecision(true, System.currentTimeMillis());
        assertFalse("a negative TTL must clamp to 0 (no memoization), not to 'never expires'",
                AppUtil.isAppEditableByCurrentUser(appDef, false));
    }
}
