package org.joget.commons.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * GitLab #2261: {@code PluginThread} now automatically propagates the spawning thread's already-
 * established {@code WorkflowUserManager} identity (and, for the reusable-executor case, tenant
 * profile) into its own worker thread, instead of requiring each plugin (e.g. {@code
 * process_enhancement}'s {@code AssignmentFormActions}) to call {@code setCurrentThreadUser} itself
 * -- see {@code PluginThread.wrapWithCurrentUserIdentity}'s own comment for the full history and
 * {@code MultiTenantSecurityManager}/{@code MultiTenantPluginAspect}/{@code
 * BshReflectionGuardAspect} (joget-enterprise) for the security-guard side of this change, reviewed
 * separately.
 *
 * <p>This module has no AspectJ weaving of its own (the guards live in joget-enterprise's
 * cloud-wfengine), so these tests exercise {@code PluginThread}'s own functional correctness only --
 * does it actually capture/restore/clear identity and profile correctly, exclude system-scoped
 * identity, and handle a REUSED worker thread's per-task isolation -- using a hand-written {@code
 * ThreadUserIdentitySource} test double registered via {@code PluginThread
 * .TEST_IDENTITY_SOURCE_OVERRIDE} (the same test-only-seam pattern already established elsewhere in
 * this codebase for a write-once production value, e.g. {@code MultiTenantSecurityManager
 * .TEST_TRUSTED_PLUGIN_BUNDLE_LOCATOR_OVERRIDE}) rather than a real Spring context -- {@code
 * PluginThread} no longer resolves this via a live {@code ApplicationContext} lookup at all (see
 * {@code resolveIdentitySource()}'s own comment for why that was found insufficient).
 */
public class TestPluginThreadUserIdentityPropagation {

    /**
     * Records every identity ever active on the CURRENT thread, so a test can assert exactly what a
     * spawned worker thread saw without needing a real WorkflowUserManager or Spring Security
     * context at all.
     */
    private static final class FakeIdentitySource implements ThreadUserIdentitySource {
        private final ThreadLocal<String> current = new ThreadLocal<>();
        private volatile String spawningThreadUsername;
        private volatile boolean spawningThreadIsSystemUser;

        void setSpawningThreadIdentity(String username, boolean isSystemUser) {
            this.spawningThreadUsername = username;
            this.spawningThreadIsSystemUser = isSystemUser;
        }

        String getOnCurrentThread() {
            return current.get();
        }

        @Override
        public String captureUsernameForPluginThreadPropagation() {
            if (spawningThreadIsSystemUser) {
                return null;
            }
            return spawningThreadUsername;
        }

        @Override
        public void setCurrentThreadUser(String username) {
            current.set(username);
        }

        @Override
        public void clearCurrentThreadUser() {
            current.remove();
        }
    }

    private FakeIdentitySource fakeIdentitySource;

    @Before
    public void setUp() {
        fakeIdentitySource = new FakeIdentitySource();
        // Test-only seam -- see this class's own javadoc. Deliberately NOT
        // PluginThread.pinIdentitySourceIfAbsent(..): that method is write-once by design (the
        // whole point of the production fix this session), so a second test method in the same
        // JUnit fork calling it again would silently do nothing, leaving every later test stuck
        // with the FIRST test's fake. The override field has no such restriction.
        PluginThread.TEST_IDENTITY_SOURCE_OVERRIDE = fakeIdentitySource;
        HostManager.setCurrentProfile("somePluginThreadTestProfile-2261");
    }

    @After
    public void tearDown() {
        PluginThread.TEST_IDENTITY_SOURCE_OVERRIDE = null;
        HostManager.setCurrentProfile(null);
    }

    /**
     * The core fix: a one-shot {@code new PluginThread(runnable)} (matching {@code
     * AssignmentFormActions}'s own usage) must run the wrapped {@code Runnable} with the SAME
     * identity that was active on the spawning thread, with NO call from the wrapped Runnable's own
     * code needed at all.
     */
    @Test
    public void testOneShotPluginThreadInheritsSpawningThreadIdentity() throws Exception {
        fakeIdentitySource.setSpawningThreadIdentity("someTenantUser", false);

        final AtomicReference<String> seenInsideWorker = new AtomicReference<>();
        Thread worker = new PluginThread(new Runnable() {
            @Override
            public void run() {
                seenInsideWorker.set(fakeIdentitySource.getOnCurrentThread());
            }
        });
        worker.start();
        worker.join(10000);

        Assert.assertEquals("a PluginThread-spawned worker must automatically inherit the SAME "
                + "identity that was active on the thread that spawned it, with no explicit "
                + "setCurrentThreadUser call from the wrapped Runnable's own code",
                "someTenantUser", seenInsideWorker.get());
    }

    /**
     * The identity set for one PluginThread's own task must not leak to whatever the SAME (or a
     * different) thread does afterward -- clearCapturedIdentity() must actually run.
     */
    @Test
    public void testIdentityIsClearedAfterOneShotPluginThreadCompletes() throws Exception {
        fakeIdentitySource.setSpawningThreadIdentity("someTenantUser", false);

        Thread worker = new PluginThread(new Runnable() {
            @Override
            public void run() {
                // no-op; just needs to complete
            }
        });
        worker.start();
        worker.join(10000);

        Assert.assertNull("identity must be cleared again once the PluginThread's own task "
                + "completes, not left set on whatever thread the JVM happens to reuse next",
                fakeIdentitySource.getOnCurrentThread());
    }

    /**
     * GitLab #2261 regression (Sol P2, review round 1): a system-scoped spawning thread's identity
     * must NEVER be propagated through this mechanism -- WorkflowUserManager.getCurrentUsername()
     * would otherwise return the literal string "roleSystem", which a generic plugin-spawned worker
     * would then treat as an ordinary (non-existent) username.
     */
    @Test
    public void testSystemScopedSpawningThreadIdentityIsNotPropagated() throws Exception {
        fakeIdentitySource.setSpawningThreadIdentity("roleSystem", true);

        final AtomicReference<String> seenInsideWorker = new AtomicReference<>();
        final AtomicReference<Boolean> sawNull = new AtomicReference<>(Boolean.FALSE);
        Thread worker = new PluginThread(new Runnable() {
            @Override
            public void run() {
                String v = fakeIdentitySource.getOnCurrentThread();
                seenInsideWorker.set(v);
                sawNull.set(v == null);
            }
        });
        worker.start();
        worker.join(10000);

        Assert.assertTrue("a system-scoped spawning thread must NOT have any identity propagated "
                + "into the worker thread at all (must stay unset, never the literal string "
                + "\"roleSystem\"); saw: " + seenInsideWorker.get(),
                sawNull.get());
    }

    /**
     * GitLab #2261 regression (Sol P1, review round 1): {@code PluginThread
     * .getAsyncExecutorService()} wraps a SINGLE, LONG-LIVED worker thread reused across every task
     * ever submitted to it. Capturing identity once, at thread-construction time, would let whoever
     * is first to trigger the executor's creation leak their identity into every LATER task
     * submitted by a completely different user. This test submits two tasks under two DIFFERENT
     * identities to the SAME executor and asserts each one saw only its own.
     */
    @Test
    public void testAsyncExecutorServicePropagatesIdentityPerTaskNotPerWorkerConstruction() throws Exception {
        ExecutorService executor = PluginThread.getAsyncExecutorService();
        try {
            fakeIdentitySource.setSpawningThreadIdentity("firstUser", false);
            final CountDownLatch firstTaskRunning = new CountDownLatch(1);
            final CountDownLatch releaseFirstTask = new CountDownLatch(1);
            final AtomicReference<String> seenByFirstTask = new AtomicReference<>();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    seenByFirstTask.set(fakeIdentitySource.getOnCurrentThread());
                    firstTaskRunning.countDown();
                    try {
                        releaseFirstTask.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException ignored) {
                    }
                }
            });
            Assert.assertTrue("first task did not start in time", firstTaskRunning.await(10, TimeUnit.SECONDS));
            Assert.assertEquals("firstUser", seenByFirstTask.get());

            // A DIFFERENT identity is now active on whatever thread submits the SECOND task --
            // simulating a different tenant/user's own request submitting to this SAME, shared,
            // already-constructed executor.
            fakeIdentitySource.setSpawningThreadIdentity("secondUser", false);
            final AtomicReference<String> seenBySecondTask = new AtomicReference<>();
            final CountDownLatch secondTaskDone = new CountDownLatch(1);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    seenBySecondTask.set(fakeIdentitySource.getOnCurrentThread());
                    secondTaskDone.countDown();
                }
            });
            releaseFirstTask.countDown();
            Assert.assertTrue("second task did not complete in time", secondTaskDone.await(10, TimeUnit.SECONDS));

            Assert.assertEquals("each task submitted to the SAME long-lived executor must see only "
                    + "its OWN submitting thread's identity, not whichever identity happened to be "
                    + "active when the executor's underlying worker thread was first constructed",
                    "secondUser", seenBySecondTask.get());
        } finally {
            executor.shutdown();
        }
    }

    /**
     * GitLab #2261 regression (Sol P1, review round 2): the SAME reused-executor worker thread also
     * has a pre-existing, unrelated field -- PluginThread's OWN {@code profile}, captured ONCE at
     * the worker's construction time by PluginThread's own constructor -- with the identical
     * leak-across-tasks shape identity itself had. A fix that only covers identity but leaves
     * profile wrong would let a task see the CORRECT identity but the WRONG (stale, first-task's)
     * tenant profile -- arguably worse than before, since the two would now silently disagree within
     * the same task. This test submits two tasks under two DIFFERENT profiles to the SAME executor
     * and asserts each one saw only its own.
     */
    @Test
    public void testAsyncExecutorServicePropagatesProfilePerTaskNotPerWorkerConstruction() throws Exception {
        ExecutorService executor = PluginThread.getAsyncExecutorService();
        try {
            HostManager.setCurrentProfile("firstProfile-2261");
            final CountDownLatch firstTaskRunning = new CountDownLatch(1);
            final CountDownLatch releaseFirstTask = new CountDownLatch(1);
            final AtomicReference<String> seenByFirstTask = new AtomicReference<>();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    seenByFirstTask.set(HostManager.getCurrentProfile());
                    firstTaskRunning.countDown();
                    try {
                        releaseFirstTask.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException ignored) {
                    }
                }
            });
            Assert.assertTrue("first task did not start in time", firstTaskRunning.await(10, TimeUnit.SECONDS));
            Assert.assertEquals("firstProfile-2261", seenByFirstTask.get());

            HostManager.setCurrentProfile("secondProfile-2261");
            final AtomicReference<String> seenBySecondTask = new AtomicReference<>();
            final CountDownLatch secondTaskDone = new CountDownLatch(1);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    seenBySecondTask.set(HostManager.getCurrentProfile());
                    secondTaskDone.countDown();
                }
            });
            releaseFirstTask.countDown();
            Assert.assertTrue("second task did not complete in time", secondTaskDone.await(10, TimeUnit.SECONDS));

            Assert.assertEquals("each task submitted to the SAME long-lived executor must see only "
                    + "its OWN submitting thread's tenant profile, not whichever profile happened to "
                    + "be active when the executor's underlying worker thread was first constructed",
                    "secondProfile-2261", seenBySecondTask.get());
        } finally {
            executor.shutdown();
            HostManager.setCurrentProfile("somePluginThreadTestProfile-2261");
        }
    }

    /**
     * GitLab #2261 regression (Sol P1, review round 2, joget-enterprise): pinIdentitySourceIfAbsent
     * must be genuinely write-once in production -- a SECOND call (simulating some other object
     * later also implementing ThreadUserIdentitySource and attempting to register itself) must not
     * displace whichever instance was pinned first. This is the ONE test in this class that
     * exercises the real production pinnedIdentitySource field directly (every other test uses
     * TEST_IDENTITY_SOURCE_OVERRIDE, which is unaffected by this field) -- restores the field to
     * whatever it was before via reflection, since it has no public reset and must not leak into
     * unrelated tests running later in the same fork.
     */
    @Test
    public void testPinIdentitySourceIfAbsentIsWriteOnce() throws Exception {
        java.lang.reflect.Field pinnedField = PluginThread.class.getDeclaredField("pinnedIdentitySource");
        pinnedField.setAccessible(true);
        Object previous = pinnedField.get(null);
        try {
            pinnedField.set(null, null);
            FakeIdentitySource first = new FakeIdentitySource();
            FakeIdentitySource second = new FakeIdentitySource();
            PluginThread.pinIdentitySourceIfAbsent(first);
            PluginThread.pinIdentitySourceIfAbsent(second);

            Assert.assertSame("a second pin attempt must never displace the already-pinned genuine "
                    + "instance, whether the second caller is a legitimate duplicate call or a "
                    + "malicious later registration attempt",
                    first, pinnedField.get(null));
        } finally {
            pinnedField.set(null, previous);
        }
    }
}
