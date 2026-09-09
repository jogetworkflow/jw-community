package org.joget.commons.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestHostManager {

    @Before
    public void setUp() {
        // Surefire reuses threads across test classes/forks, so another class's HostManager use
        // (or DynamicDataSourceManager.getCurrentProfile() memoizing a default profile back into
        // HostManager) can leave this thread non-null before this class even runs. Resetting here
        // reproduces a genuinely fresh thread's state so each test starts deterministically.
        HostManager.setCurrentProfile(null);
    }

    @After
    public void tearDown() {
        // Leave the JUnit-runner thread in a clean state regardless of test outcome, since JUnit
        // may reuse the same thread across test methods/classes.
        HostManager.setCurrentProfile(null);
    }

    /**
     * Regression test for the pooled-thread profile leak: a fresh thread (currentProfile == null,
     * previousProfile never populated) running one tenant task, guarded by the standard
     * {@code try { setCurrentProfile(x); ... } finally { resetProfile(); } } idiom used across
     * ReportTask, FormOptionsCacheTask, GovHealthCheckTask, PluginThread, DeadlineChecker, and
     * others, must end with the profile actually cleared -- not silently left pinned to the first
     * tenant that ever ran on that thread.
     */
    @Test
    public void testResetProfileClearsOnFreshThread() {
        assertNull("precondition: thread starts with no profile", HostManager.getCurrentProfile());

        HostManager.setCurrentProfile("tenantA");
        assertEquals("tenantA", HostManager.getCurrentProfile());
        HostManager.resetProfile();

        assertNull("resetProfile() must clear back to null when the thread had no profile "
                + "before the task started, not leave the tenant profile pinned",
                HostManager.getCurrentProfile());
    }

    /**
     * A second, unrelated task landing on the same (now-reused) pool thread must not inherit the
     * first task's tenant profile.
     */
    @Test
    public void testSequentialPooledTasksDoNotLeakProfileBetweenEachOther() {
        HostManager.setCurrentProfile("tenantA");
        HostManager.resetProfile();
        assertNull(HostManager.getCurrentProfile());

        HostManager.setCurrentProfile("tenantB");
        HostManager.resetProfile();
        assertNull("second task's reset must not restore the first task's tenant",
                HostManager.getCurrentProfile());

        // A third task that reads the profile without ever setting it must see no stale tenant.
        assertNull(HostManager.getCurrentProfile());
    }

    /**
     * Restoring a genuine, non-null prior profile after a single tracked override completes must
     * still work -- only the "prior state was null" case gets special handling elsewhere. Seeds
     * the ambient profile directly (same-package field access), bypassing setCurrentProfile's own
     * tracking, so only the one tracked transition under test gets recorded.
     *
     * <p>See {@link #testThreeLevelNestingRestoresEachLevelCorrectly} for genuine multi-level
     * nesting (an inner scope's own try/finally-guarded override, itself nested inside an outer
     * one): a per-thread stack, not a single-slot field, lets each level restore its own
     * immediately-preceding value regardless of nesting depth.
     */
    @Test
    public void testResetProfileRestoresGenuineOuterProfile() {
        HostManager.currentProfile.set("outerTenant");

        HostManager.setCurrentProfile("innerTenant");
        assertEquals("innerTenant", HostManager.getCurrentProfile());

        HostManager.resetProfile();
        assertEquals("resetProfile() must restore a genuine (non-null) prior profile",
                "outerTenant", HostManager.getCurrentProfile());
    }

    /**
     * Nested profile scopes on a fresh thread must each restore their own immediately-preceding
     * value, not escalate to the platform's default/admin profile. With a single restore slot,
     * nesting on a fresh thread (this test's precondition -- the state every pooled/scheduled
     * worker starts in) would resolve the ambient profile to {@code null} once the inner reset
     * consumed the slot recorded for the outer call, and {@code null} resolves to the platform's
     * default/admin profile on the next lookup -- reachable whenever any two of the ~20 real
     * {@code try/finally { resetProfile(); } } call sites run nested on the same thread (e.g. a
     * scheduled report-generation task that internally triggers a cache-sync task guarded by its
     * own {@code setCurrentProfile}/{@code resetProfile} pair). With a per-thread stack, the inner
     * {@code resetProfile()} pops and restores only its own immediately-preceding value
     * (innerTenant's predecessor, "outerTenant"), leaving the outer scope's remaining work at the
     * correct profile; the outer scope's own {@code resetProfile()} then correctly restores the
     * true pre-outer-scope state (null).
     */
    @Test
    public void testNestedResetOnFreshThreadRestoresOuterTenantNotNull() {
        assertNull("precondition: thread starts with no profile", HostManager.getCurrentProfile());

        HostManager.setCurrentProfile("outerTenant");
        HostManager.setCurrentProfile("innerTenant");
        HostManager.resetProfile(); // the INNER scope's own resetProfile()

        assertEquals("the inner resetProfile() must restore its OWN immediately-preceding value "
                + "(outerTenant), not escalate to null (admin) by consuming the outer call's slot",
                "outerTenant", HostManager.getCurrentProfile());

        HostManager.resetProfile(); // the OUTER scope's own resetProfile()
        assertNull("the outer resetProfile() must then correctly restore the true pre-outer-scope "
                + "state (null), now that nesting is tracked correctly",
                HostManager.getCurrentProfile());
    }

    /**
     * Genuine 3-level nesting: on a fresh thread, a single restore slot can only remember one
     * pending value, so it fails by the 2nd nested level already, let alone the 3rd. Each of the
     * 3 levels must restore exactly its own immediately-preceding value, proving the stack-based
     * fix generalizes to any nesting depth, not just the 2-level case above.
     */
    @Test
    public void testThreeLevelNestingRestoresEachLevelCorrectly() {
        assertNull("precondition: thread starts with no profile", HostManager.getCurrentProfile());

        HostManager.setCurrentProfile("level1");
        HostManager.setCurrentProfile("level2");
        HostManager.setCurrentProfile("level3");
        assertEquals("level3", HostManager.getCurrentProfile());

        HostManager.resetProfile();
        assertEquals("innermost reset must restore level2, its own immediately-preceding value",
                "level2", HostManager.getCurrentProfile());

        HostManager.resetProfile();
        assertEquals("middle reset must restore level1, its own immediately-preceding value",
                "level1", HostManager.getCurrentProfile());

        HostManager.resetProfile();
        assertNull("outermost reset must restore the true pre-nesting state (null)",
                HostManager.getCurrentProfile());
    }

    /**
     * setCurrentProfile(null) is the existing "hard clear" primitive (used by initHost()) and must
     * still make a subsequent resetProfile() a no-op, since there is nothing pending to restore.
     */
    @Test
    public void testExplicitNullSetSuppressesSubsequentReset() {
        HostManager.setCurrentProfile("tenantA");
        HostManager.setCurrentProfile(null);
        assertNull(HostManager.getCurrentProfile());

        HostManager.resetProfile();
        assertNull("resetProfile() after an explicit setCurrentProfile(null) must remain a no-op",
                HostManager.getCurrentProfile());
    }

    /**
     * An unpaired {@code resetProfile()} call -- one with no corresponding {@code
     * setCurrentProfile} override of its own to undo -- must remain a safe no-op. ({@code
     * resetProfile()} also requires the caller to be an explicit entry on {@code
     * MultiTenantSecurityManager.WHITELIST_RESTRICTED_CLASSES}; this test covers the underlying
     * primitive's own correctness, independent of that separate caller-identity gate.) {@code
     * DynamicDataSourceManager.getCurrentProfile()} memoizes its resolved profile back into
     * HostManager on effectively every thread's first profile lookup; if that memoization used the
     * TRACKED setter, it would claim the one-shot restore slot with {@code NULL_PROFILE} (since
     * nothing was set before it), and a later unpaired {@code resetProfile()} call -- exactly the
     * "safe no-op" case this method exists to support -- would instead clear {@code
     * currentProfile} to {@code null}, which resolves to the platform's default/admin profile the
     * next time it is looked up. Simulates that memoization with {@code
     * setCurrentProfileWithoutTracking} (what {@code DynamicDataSourceManager} now actually calls)
     * to prove it does NOT create this escalation primitive.
     */
    @Test
    public void testUnpairedResetProfileIsNoOpAfterUntrackedMemoization() {
        HostManager.setCurrentProfileWithoutTracking("tenantA");
        assertEquals("tenantA", HostManager.getCurrentProfile());

        // An arbitrary, unpaired resetProfile() call -- no setCurrentProfile override of its own
        // preceded it on this thread, only the untracked memoization above.
        HostManager.resetProfile();

        assertEquals("an unpaired resetProfile() after only an untracked memoization must be a "
                + "no-op, not clear the memoized profile to null (null resolves to the "
                + "default/admin profile on next lookup -- a fail-open escalation)",
                "tenantA", HostManager.getCurrentProfile());
    }

    /**
     * Companion to the above: a GENUINE scoped override that happens AFTER an untracked
     * memoization must still restore the memoized profile correctly, not null. Routing the
     * memoization through the tracked setter would claim the restore slot for the memoization
     * itself (recording {@code NULL_PROFILE}), leaving nothing for this later, genuinely scoped
     * override to record -- so its {@code resetProfile()} would wrongly restore null instead of
     * "tenantA".
     */
    @Test
    public void testResetProfileRestoresMemoizedProfileNotNull() {
        HostManager.setCurrentProfileWithoutTracking("tenantA");

        HostManager.setCurrentProfile("tenantB");
        assertEquals("tenantB", HostManager.getCurrentProfile());
        HostManager.resetProfile();

        assertEquals("resetProfile() must restore the memoized profile that was ambient before "
                + "the scoped override, not null",
                "tenantA", HostManager.getCurrentProfile());
    }

    /**
     * setCurrentProfileWithoutTracking must never itself populate the restore slot -- otherwise
     * it would be no different from the tracked setter and would reopen both regressions above.
     */
    @Test
    public void testSetCurrentProfileWithoutTrackingNeverClaimsRestoreSlot() {
        HostManager.setCurrentProfileWithoutTracking("tenantA");
        HostManager.setCurrentProfileWithoutTracking("tenantB");
        assertEquals("tenantB", HostManager.getCurrentProfile());

        HostManager.resetProfile();
        assertEquals("resetProfile() after only untracked assignments must be a no-op",
                "tenantB", HostManager.getCurrentProfile());
    }

    /**
     * Models a concrete, fully self-contained inner scoped pair -- e.g.
     * {@code ContainerSystemKeyGenerator.isNodeLicensed()} / {@code checkClusterLicense()}, both of
     * which do exactly {@code try { setCurrentProfile(clusterLicenseProfile); ... } finally {
     * resetProfile(); } } and are on MultiTenantSecurityManager's caller whitelist so they proceed
     * even with a tenant OSGi/BeanShell frame beneath them -- nested inside an OUTER scoped worker
     * task ({@code FormOptionsCacheTask.run}/{@code DeadlineChecker.runMe}/{@code
     * GovHealthCheckTask.run}) running on a pooled/dedicated thread whose {@code currentProfile}
     * was {@code null} at task entry (the state a pool thread constructed before any tenant context
     * starts in).
     *
     * <p>Distinct from {@link #testNestedResetOnFreshThreadRestoresOuterTenantNotNull}: that test
     * uses two consecutive {@code setCurrentProfile} calls with a single trailing reset; this one
     * uses a genuine balanced inner {@code set}/{@code reset} pair -- the precise shape a real
     * license check (or any other whitelisted synchronous scoped method) presents when tenant code
     * triggers it mid-scope. The outer scope's remaining work must continue at tenantA rather than
     * escalate to null (admin).
     */
    @Test
    public void testNestedLicenseStyleScopedPairRestoresOuterTenantNotNull() {
        // Outer worker task entry on a fresh pool thread: currentProfile == null.
        assertNull("precondition: pooled worker thread starts with no profile",
                HostManager.getCurrentProfile());

        // Outer tracked scope opens for tenantA (pushes NULL_PROFILE, since currentProfile==null).
        HostManager.setCurrentProfile("tenantA");
        assertEquals("tenantA", HostManager.getCurrentProfile());

        // ---- tenant code, still inside the outer scope, synchronously triggers a whitelisted
        // ---- platform method (isNodeLicensed) that opens+closes its OWN balanced scoped pair ----
        HostManager.setCurrentProfile("clusterLicenseProfile");
        HostManager.resetProfile(); // isNodeLicensed()'s own finally

        assertEquals("the nested balanced license-style scoped pair must restore its OWN "
                + "immediately-preceding value (tenantA) via the stack, not consume the outer "
                + "call's slot and escalate to null (admin)",
                "tenantA", HostManager.getCurrentProfile());

        HostManager.resetProfile(); // outer worker's own finally
        assertNull("outer worker's own finally must then restore the true pre-scope state (null)",
                HostManager.getCurrentProfile());
    }

    /**
     * Companion boundary case proving the escalation above is confined to the "tracked outer on
     * a null-profile thread" shape and does NOT affect an ordinary HTTP request thread. On a
     * normal request, the profile is established by {@code
     * DynamicDataSourceManager.getCurrentProfile()}'s memoization via {@code
     * setCurrentProfileWithoutTracking} (which never claims the restore slot), so the SAME nested
     * license-style pair correctly records and restores "tenantA" -- this is what keeps the
     * escalation from being reachable on every ordinary request.
     */
    @Test
    public void testNestedLicenseStyleScopedPairIsSafeUnderMemoizedRequestProfile() {
        // Normal request thread: profile memoized (untracked), previousProfile never claimed.
        HostManager.setCurrentProfileWithoutTracking("tenantA");
        assertEquals("tenantA", HostManager.getCurrentProfile());

        // Same nested balanced license-style scoped pair as the escalating test above.
        HostManager.setCurrentProfile("clusterLicenseProfile");
        HostManager.resetProfile();

        assertEquals("under a memoized (untracked) request profile the nested pair correctly "
                + "records and restores tenantA -- no escalation on the ordinary request path",
                "tenantA", HostManager.getCurrentProfile());
    }
}
