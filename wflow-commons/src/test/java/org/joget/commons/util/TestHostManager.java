package org.joget.commons.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestHostManager {

    @Before
    public void setUp() {
        // Surefire reuses threads across test classes in the same module/fork, and another
        // class's HostManager use (or DynamicDataSourceManager.getCurrentProfile() memoizing a
        // default profile back into HostManager) can leave this thread non-null before this class
        // even runs. setCurrentProfile(null) reproduces the same previousProfile-unset/
        // currentProfile-null state as a genuinely fresh thread, so each test starts from that
        // state deterministically instead of assuming it.
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
     * The pre-existing behavior of restoring a genuine, non-null prior profile after a single
     * tracked override completes must still work -- this fix must not regress the original
     * intended use case, only the previously-unhandled "prior state was null" case. Seeds the
     * ambient profile directly (same-package field access), bypassing setCurrentProfile's own
     * tracking, so the seed itself is not what is being tracked/restored -- isolating exactly one
     * tracked transition, the case this method exists to cover.
     *
     * <p>See {@link #testThreeLevelNestingRestoresEachLevelCorrectly} for genuine multi-level
     * nesting (an inner scope's own try/finally-guarded override, itself nested inside an outer
     * one). A per-thread stack (replacing the old single-slot {@code previousProfile} field) lets
     * each level correctly restore its own immediately-preceding value regardless of nesting
     * depth, instead of always restoring the OUTERMOST tracked value.
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
     * On a FRESH thread (this test's precondition -- exactly the state every pooled/scheduled
     * worker starts in), the old single-slot design resolved the ambient profile to {@code null}
     * once the inner reset consumed the slot recorded for the outer call -- and {@code null}
     * resolves to the platform's default/admin profile on the next lookup elsewhere in the
     * codebase, a non-admin-to-admin escalation reachable whenever two of the ~20 real {@code
     * try/finally { resetProfile(); } } call sites nest on the same thread (e.g. a scheduled
     * report-generation task that internally triggers a cache-sync task also guarded by its own
     * {@code setCurrentProfile}/{@code resetProfile} pair). With the per-thread stack, the inner
     * {@code resetProfile()} pops and restores only its OWN immediately-preceding value
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
     * Genuine 3-level nesting: on a fresh thread, a legitimate 3-level nested {@code
     * setCurrentProfile}/{@code resetProfile} sequence used to fail toward admin (null) as early
     * as the 2nd level, since the single slot could only ever remember one pending value. Each of
     * the 3 levels must now restore exactly its own immediately-preceding value, proving the fix
     * generalizes to any nesting depth, not just the 2-level case above.
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
     * Regression test: an unpaired {@code resetProfile()} call -- one with no corresponding
     * {@code setCurrentProfile} override of its own to undo -- must always be a safe no-op. {@code
     * DynamicDataSourceManager.getCurrentProfile()} memoizes its resolved profile back into
     * HostManager on effectively every thread's first profile lookup; if that memoization used the
     * TRACKED setter, it would claim the one-shot restore slot with {@code NULL_PROFILE} (since
     * nothing was set before it), and a later unpaired {@code resetProfile()} call -- exactly the
     * "safe no-op" case this method exists to support -- would instead clear {@code
     * currentProfile} to {@code null}, which resolves to the platform's default/admin profile the
     * next time it is looked up. ({@code resetProfile()} separately requires an explicit
     * CALLER-based {@code MultiTenantSecurityManager.WHITELIST_RESTRICTED_CLASSES} entry for every
     * legitimate call site, so an unpaired non-whitelisted caller is denied outright regardless --
     * this test covers the underlying primitive's own correctness, independent of that gate.)
     * Simulates the memoization with {@code setCurrentProfileWithoutTracking} (what {@code
     * DynamicDataSourceManager} actually calls) to prove it does NOT create this escalation.
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
     * memoization must still restore the memoized profile correctly, not null. Before
     * setCurrentProfileWithoutTracking existed, routing the memoization through the tracked
     * setter would have claimed the restore slot for the memoization itself (recording
     * NULL_PROFILE), leaving nothing for this later, genuinely scoped override to record --
     * so its resetProfile() would have wrongly restored null instead of "tenantA".
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
     * triggers it mid-scope. The outer scope's remaining work now correctly continues at tenantA
     * rather than escalating to null (admin).
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
     * Companion boundary case proving WHY the escalation above is confined to the "tracked outer on
     * a null-profile thread" shape and does NOT affect an ordinary HTTP request thread. On a normal
     * request, the profile is established by {@code DynamicDataSourceManager.getCurrentProfile()}'s
     * memoization via {@code setCurrentProfileWithoutTracking} (which never claims the restore
     * slot), so the SAME nested license-style pair correctly records and restores "tenantA". This
     * is the mitigation that keeps the escalation above from being trivially reachable on every
     * request.
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
