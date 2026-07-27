package org.joget.apps.app.service;

import org.joget.apps.app.model.AppDefinition;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.PluginThread;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Exercises the file-based lock protocol in isolation from AppServiceImpl. The
 * "should not release while process migration is running" guard is AppServiceImpl's own
 * responsibility (see AppServiceImplTest in wflow-consoleweb), not this class's — these tests
 * cover only the underlying claim/release/heartbeat/staleness mechanics themselves.
 */
public class ProcessUpdateLockManagerTest {

    private ProcessUpdateLockManager lockManager;
    private AppDefinition appDef;
    private Path baseDirectory;
    private String processMigrationPath;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        baseDirectory = temporaryFolder.newFolder("wflow").toPath();
        lockManager = new ProcessUpdateLockManager(baseDirectory);

        appDef = new AppDefinition();
        appDef.setAppId("testApp");
        appDef.setVersion(1L);

        processMigrationPath = baseDirectory.resolve("app_migration").toString() + File.separator;

        // Ensure directory exists
        File dir = new File(processMigrationPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Simulates a lock left behind by a node that crashed mid-migration: the lock file exists
     * but is far older than the expiry window and nothing is heartbeating it. A fresh save
     * request (possibly on a different cluster node) must be able to reclaim it instead of
     * being blocked forever.
     */
    @Test
    public void testStaleLock_isReclaimedByLockProcessUpdate() throws Exception {
        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        Files.createFile(lockPath);
        backdateLock(lockPath, 3);

        boolean reclaimed = lockManager.lockProcessUpdate(appDef);

        assertTrue("A stale lock (no heartbeat, past expiry) should be reclaimable", reclaimed);
        assertTrue("Lock file should exist after being reclaimed", Files.exists(lockPath));
    }

    /**
     * A lock that was only just acquired must not be reclaimed by a concurrent request —
     * otherwise two saves/migrations could run against the same app+version at once.
     */
    @Test
    public void testFreshLock_isNotReclaimed() throws Exception {
        assertTrue("First caller should acquire the lock", lockManager.lockProcessUpdate(appDef));

        boolean secondAttempt = lockManager.lockProcessUpdate(appDef);

        assertFalse("A freshly held lock must not be reclaimed by another caller", secondAttempt);
    }

    /**
     * hasProcessUpdate() must self-heal a stale lock rather than only relying on the next save
     * attempt to reclaim it — otherwise Process Builder JSON reads would keep getting HTTP 503
     * indefinitely after a node crash, even if no one ever tries to save again.
     */
    @Test
    public void testStaleLock_hasProcessUpdateSelfHeals() throws Exception {
        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        Files.createFile(lockPath);
        backdateLock(lockPath, 3);

        boolean hasUpdate = lockManager.hasProcessUpdate(appDef);

        assertFalse("A stale lock should be treated as no update in progress", hasUpdate);
        assertFalse("hasProcessUpdate should clear the stale lock file", Files.exists(lockPath));
    }

    /**
     * touchProcessUpdateLock() is the heartbeat a genuinely long-running migration relies on so
     * it is not mistaken for an abandoned lock while it is still actively making progress.
     */
    @Test
    public void testTouchProcessUpdateLock_preventsStaleReclaim() throws Exception {
        assertTrue("Lock should be acquired by this node first", lockManager.lockProcessUpdate(appDef));

        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        backdateLock(lockPath, 3);

        lockManager.touchProcessUpdateLock(appDef);

        assertTrue("A freshly-touched lock should still be considered active", lockManager.hasProcessUpdate(appDef));
    }

    @Test
    public void testHasProcessUpdate_doesNotClearStaleLockStillOwnedByThisManager() throws Exception {
        assertTrue("Lock should be acquired by this node first", lockManager.lockProcessUpdate(appDef));

        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        backdateLock(lockPath, 3);

        assertTrue("A status check must not clear a stale-looking generation still owned locally", lockManager.hasProcessUpdate(appDef));
        assertTrue("The locally owned main lock must remain in place", Files.exists(lockPath));

        lockManager.tryReleaseProcessUpdate(appDef);
        awaitLockFileDeleted(lockPath);
    }

    /**
     * A node that never itself acquired the lock (no recorded owner token) must not be able to
     * heartbeat whatever happens to be sitting at the lock path — otherwise an unrelated caller,
     * or a node that lost the lock to a reclaim, could revive a lock generation it does not own.
     */
    @Test
    public void testTouchProcessUpdateLock_doesNothingWithoutRecordedOwnership() throws Exception {
        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        Files.createFile(lockPath);
        backdateLock(lockPath, 3);

        // This lockManager instance never called lockProcessUpdate, so it holds no owner token.
        lockManager.touchProcessUpdateLock(appDef);

        assertFalse("An untouched, unowned stale lock must still be reclaimable/self-heal as stale", lockManager.hasProcessUpdate(appDef));
    }

    /**
     * The core race this fix closes: node A acquires the lock, then goes silent long enough to
     * be considered abandoned. Node B (modeled as a second ProcessUpdateLockManager instance)
     * reclaims the lock. Node A must not be able to refresh or release the lock generation B now
     * owns.
     */
    @Test
    public void testOldOwner_cannotReviveOrReleaseLockReclaimedByAnotherNode() throws Exception {
        ProcessUpdateLockManager nodeA = lockManager;
        ProcessUpdateLockManager nodeB = new ProcessUpdateLockManager(baseDirectory);

        assertTrue("Node A acquires the lock first", nodeA.lockProcessUpdate(appDef));

        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        backdateLock(lockPath, 3);

        assertTrue("Node B reclaims the abandoned lock", nodeB.lockProcessUpdate(appDef));

        // Node A, unaware it lost the lock, tries to heartbeat and then release it.
        nodeA.touchProcessUpdateLock(appDef);
        assertTrue("Node B's reclaimed lock must still be reported as active after node A's stale touch", nodeB.hasProcessUpdate(appDef));

        nodeA.tryReleaseProcessUpdate(appDef);
        assertTrue("Node A's release must not delete node B's reclaimed lock", Files.exists(lockPath));

        nodeB.tryReleaseProcessUpdate(appDef);
        assertFalse("Node B, the true owner, can still release the lock it reclaimed", Files.exists(lockPath));
    }

    /**
     * Two nodes racing to reclaim the same stale lock at the same instant must not both succeed —
     * exactly one reclaim can win, matching the exclusivity that a single createFile() gave the
     * original (non-stale) acquisition path.
     */
    @Test
    public void testConcurrentReclaim_onlyOneNodeWins() throws Exception {
        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        Files.createFile(lockPath);
        backdateLock(lockPath, 3);

        int contenders = 8;
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(contenders);
        java.util.concurrent.CyclicBarrier barrier = new java.util.concurrent.CyclicBarrier(contenders);
        java.util.List<java.util.concurrent.Future<Boolean>> futures = new java.util.ArrayList<>();

        for (int i = 0; i < contenders; i++) {
            final ProcessUpdateLockManager node = new ProcessUpdateLockManager(baseDirectory);
            futures.add(pool.submit(() -> {
                barrier.await();
                return node.lockProcessUpdate(appDef);
            }));
        }

        int winners = 0;
        for (java.util.concurrent.Future<Boolean> f : futures) {
            if (f.get()) {
                winners++;
            }
        }
        pool.shutdown();

        assertEquals("Exactly one contender may reclaim a given stale lock generation", 1, winners);
        assertTrue("The lock file must exist after the race resolves", Files.exists(lockPath));
    }

    /**
     * hasProcessUpdate() must not report a false "all clear" when it could not confirm the stale
     * lock was actually removed — e.g. another node is concurrently reclaiming or clearing the
     * very same lock and is holding the OS-backed mutex. Otherwise a caller could start reading
     * package data just as that other node begins its own save.
     */
    @Test
    public void testHasProcessUpdate_reportsBusyWhenCleanupCannotBeConfirmed() throws Exception {
        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        Files.createFile(lockPath);
        backdateLock(lockPath, 3);

        // Simulate another node concurrently holding the OS mutex for this same lock.
        Path ticketPath = Paths.get(processMigrationPath + "testApp_1.lock.reclaiming");
        try (FileChannel channel = FileChannel.open(ticketPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock ignored = channel.lock()) {
            boolean hasUpdate = lockManager.hasProcessUpdate(appDef);

            assertTrue("Cleanup could not be confirmed (mutex held elsewhere); must report busy rather than a false all-clear", hasUpdate);
            assertTrue("The lock file must be left alone since it could not be safely cleared", Files.exists(lockPath));
        }
    }

    /**
     * A read must stay lock-free when no lock file exists: hasProcessUpdate must report "no update"
     * without touching the shared mutex, so a concurrent read can never make a save's
     * lockProcessUpdate fail (or a getJson read return a spurious 503). Modeled by holding the
     * mutex externally while no main lock file is present — the read must short-circuit to false
     * regardless, rather than reporting busy because it could not acquire the mutex.
     */
    @Test
    public void testHasProcessUpdate_withNoLockFileIsLockFreeAndNotBusy() throws Exception {
        Path ticketPath = Paths.get(processMigrationPath + "testApp_1.lock.reclaiming");
        try (FileChannel channel = FileChannel.open(ticketPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                FileLock ignored = channel.lock()) {
            assertFalse("A read with no lock file present must report no update without contending on the mutex",
                    lockManager.hasProcessUpdate(appDef));
        }
    }

    /**
     * A release that finds the mutex briefly busy must retry on the caller thread; otherwise the
     * main lock remains until the stale timeout even though this node still owns it.
     */
    @Test
    public void testTryReleaseProcessUpdate_retriesAfterMutexContention() throws Exception {
        assertTrue("Lock should be acquired", lockManager.lockProcessUpdate(appDef));

        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        Path ticketPath = Paths.get(processMigrationPath + "testApp_1.lock.reclaiming");
        final FileChannel channel = FileChannel.open(ticketPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        final FileLock mutex = channel.lock();
        HostManager.setCurrentProfile("test-profile");
        Thread mutexReleaser;
        try {
            mutexReleaser = new PluginThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100L);
                        mutex.release();
                        channel.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } finally {
            HostManager.setCurrentProfile(null);
        }
        mutexReleaser.start();

        lockManager.tryReleaseProcessUpdate(appDef);
        mutexReleaser.join(TimeUnit.SECONDS.toMillis(5));

        assertFalse("Mutex release helper must finish", mutexReleaser.isAlive());
        assertFalse("A synchronous retry should delete the lock after contention clears", Files.exists(lockPath));
    }

    /**
     * A transient failure while deleting the lock file during release must not make this node give
     * up prematurely: the on-thread retry loop keeps this node's ownership across the failing
     * attempts and completes the release within the same call once the transient condition clears,
     * rather than deferring the unlink to a later status check.
     */
    @Test
    public void testTryReleaseProcessUpdate_retriesUntilTransientDeleteFailureClears() throws Exception {
        // A privileged test runner (e.g. root in a CI container) or a filesystem that ignores
        // POSIX permission bits would let the delete below succeed anyway, turning this into a
        // false failure unrelated to the behavior under test — skip rather than fail there.
        Assume.assumeTrue("This runner does not enforce directory write permissions (e.g. running "
                + "as root, or a filesystem ignoring POSIX permission bits); the transient delete "
                + "failure below cannot be simulated by revoking permissions here.",
                directoryWritePermissionIsEnforced());

        assertTrue("Lock should be acquired", lockManager.lockProcessUpdate(appDef));

        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        // Deletion needs write access to the containing directory; revoke it so the first release
        // attempts fail while the lock token itself stays readable (read only needs directory read
        // access, which is unaffected).
        final File dir = new File(processMigrationPath);
        assertTrue("Test setup: must be able to revoke write permission on the lock directory", dir.setWritable(false));

        HostManager.setCurrentProfile("test-profile");
        Thread permissionRestorer;
        try {
            permissionRestorer = new PluginThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Restore write access partway through the retry window so a later attempt
                        // in the same tryReleaseProcessUpdate call can finally unlink the file.
                        Thread.sleep(100L);
                        dir.setWritable(true);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } finally {
            HostManager.setCurrentProfile(null);
        }
        permissionRestorer.start();

        try {
            lockManager.tryReleaseProcessUpdate(appDef);
        } finally {
            permissionRestorer.join(TimeUnit.SECONDS.toMillis(5));
            dir.setWritable(true);
        }

        assertFalse("The permission-restore helper must finish", permissionRestorer.isAlive());
        assertFalse("The retry loop should delete the lock once the transient failure clears", Files.exists(lockPath));
    }

    /**
     * A release that fails every retry (e.g. a persistent delete failure) must not strand the lock
     * forever on a single node. The operation is over, so a later status check has to clear the
     * leftover file rather than keep reporting a phantom in-progress update — even before the stale
     * timeout, since the same node's still-present owner token would otherwise suppress stale-lock
     * recovery indefinitely.
     */
    @Test
    public void testExhaustedRelease_hasProcessUpdateSelfHealsSameNode() throws Exception {
        Assume.assumeTrue("This runner does not enforce directory write permissions; the persistent "
                + "delete failure below cannot be simulated by revoking permissions here.",
                directoryWritePermissionIsEnforced());

        assertTrue("Lock should be acquired", lockManager.lockProcessUpdate(appDef));

        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        File dir = new File(processMigrationPath);
        assertTrue("Test setup: must be able to revoke write permission on the lock directory", dir.setWritable(false));
        try {
            // All release attempts fail to unlink the file while the directory is unwritable.
            lockManager.tryReleaseProcessUpdate(appDef);
            assertTrue("The lock file must remain since every deletion attempt failed", Files.exists(lockPath));
        } finally {
            dir.setWritable(true);
        }

        // The lock is fresh (not stale) yet its owning operation has ended; recovery must not wait
        // for the expiry window nor be blocked by this node's leftover ownership record.
        assertFalse("A finished-but-unreleased lock must not report an in-progress update", lockManager.hasProcessUpdate(appDef));
        assertFalse("The finished-but-unreleased lock file must be cleared", Files.exists(lockPath));
    }

    /**
     * After a release that fails every retry, the same node must still be able to start a new
     * save/migration for that app version, rather than being permanently refused because it still
     * believes it owns the (now defunct) lock generation.
     */
    @Test
    public void testExhaustedRelease_sameNodeCanReclaim() throws Exception {
        Assume.assumeTrue("This runner does not enforce directory write permissions; the persistent "
                + "delete failure below cannot be simulated by revoking permissions here.",
                directoryWritePermissionIsEnforced());

        assertTrue("Lock should be acquired", lockManager.lockProcessUpdate(appDef));

        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        File dir = new File(processMigrationPath);
        assertTrue("Test setup: must be able to revoke write permission on the lock directory", dir.setWritable(false));
        try {
            lockManager.tryReleaseProcessUpdate(appDef);
            assertTrue("The lock file must remain since every deletion attempt failed", Files.exists(lockPath));
        } finally {
            dir.setWritable(true);
        }

        assertTrue("The same node must be able to claim again after a release that could not complete",
                lockManager.lockProcessUpdate(appDef));
        assertTrue("The freshly reclaimed lock file must exist", Files.exists(lockPath));

        lockManager.tryReleaseProcessUpdate(appDef);
        awaitLockFileDeleted(lockPath);
    }

    /**
     * Probes whether this runner actually honors POSIX directory write permissions. Returns false
     * for a root test runner or a filesystem that ignores the bit, in which case revoking write
     * permission cannot be used to simulate a delete failure.
     */
    private boolean directoryWritePermissionIsEnforced() throws Exception {
        File dir = new File(processMigrationPath);
        Path probe = Paths.get(processMigrationPath + "permission-probe.tmp");
        Files.write(probe, "probe".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        try {
            if (!dir.setWritable(false)) {
                // This runner cannot revoke directory write permission at all (e.g. Windows, where
                // the read-only attribute is not enforced for directories) — nothing to restore.
                return false;
            }
            try {
                Files.delete(probe);
                // Deletion succeeded despite the revoked permission; this runner does not enforce it.
                return false;
            } catch (IOException e) {
                return true;
            }
        } finally {
            dir.setWritable(true);
            Files.deleteIfExists(probe);
        }
    }

    private void backdateLock(Path lockPath, int hoursAgo) throws Exception {
        long pastMillis = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(hoursAgo);
        Files.setLastModifiedTime(lockPath, FileTime.fromMillis(pastMillis));
    }

    private void awaitLockFileDeleted(Path lockPath) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (Files.exists(lockPath) && System.nanoTime() < deadline) {
            Thread.sleep(25L);
        }
        assertFalse("The asynchronously retried release must delete the lock", Files.exists(lockPath));
    }
}
