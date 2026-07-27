package org.joget.apps.app.service;

import org.joget.apps.app.model.AppDefinition;
import org.joget.commons.util.HostManager;
import org.joget.directory.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Covers AppServiceImpl's own responsibilities around the process update lock: guarding
 * tryReleaseProcessUpdate() behind the processMigration map, and the processMigration key
 * lookup used to protect versions during cleanup. The underlying file-based lock protocol itself
 * (claim/release/heartbeat/staleness/ticket arbitration) is exercised in isolation by
 * ProcessUpdateLockManagerTest in wflow-core.
 */
public class AppServiceImplTest {

    private AppServiceImpl appService;
    private AppDefinition appDef;
    private Path baseDirectory;
    private String processMigrationPath;
    private static final String TEST_PROFILE = "default";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        // Set the current profile in the thread to avoid DynamicDataSourceManager.cache
        // NPE
        HostManager.setCurrentProfile(TEST_PROFILE);

        baseDirectory = temporaryFolder.newFolder("wflow").toPath();
        appService = new AppServiceImpl(new ProcessUpdateLockManager(baseDirectory));

        appDef = new AppDefinition();
        appDef.setAppId("testApp");
        appDef.setVersion(1L);

        processMigrationPath = baseDirectory.resolve("app_migration").toString() + File.separator;

        // Ensure directory exists
        File dir = new File(processMigrationPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Clean up any existing lock file before test
        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        Files.deleteIfExists(lockPath);
    }

    @After
    public void tearDown() throws Exception {
        // Clean up lock file and processMigration map after test
        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        Files.deleteIfExists(lockPath);
        appService.processMigration.clear();
        AppUtil.setCurrentAppDefinition(null);
    }

    /**
     * Test 1: Normal flow — lock is created, and released after migration finishes.
     * When tryReleaseProcessUpdate() is called with an empty processMigration map,
     * the lock file should be deleted.
     */
    @Test
    public void testNormalFlow_lockReleasedAfterMigration() throws Exception {

        // 1. Lock acquired
        boolean locked = appService.lockProcessUpdate(appDef);
        assertTrue("Should be able to acquire lock initially", locked);

        // 2. Migration starts (Map is populated)
        String migrationKey = TEST_PROFILE + "::" + appDef.getAppId() + "_" + appDef.getVersion() + "::1";
        appService.processMigration.put(migrationKey, "2");

        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        assertTrue("Lock file should exist after locking", Files.exists(lockPath));

        // 3. Migration finishes (Map is cleared)
        appService.processMigration.remove(migrationKey);

        // 4. tryRelease is called
        appService.tryReleaseProcessUpdate(appDef);

        // Assert: Lock is deleted
        boolean exists = Files.exists(lockPath);
        assertFalse("Lock file should be deleted after migration completes", exists);
    }

    /**
     * Test 2: No-migration flow — when updateRunningProcesses() is not called,
     * the controller still calls tryReleaseProcessUpdate() in its finally block.
     * Since the processMigration map is empty, the lock must be released.
     */
    @Test
    public void testNoMigrationFlow_lockStillReleasedByController() throws Exception {

        // 1. Lock acquired
        boolean locked = appService.lockProcessUpdate(appDef);
        assertTrue("Lock should be acquired at start of save", locked);

        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");
        assertTrue("Lock file must exist after acquiring lock", Files.exists(lockPath));

        // 2. Migration is skipped (Map remains empty)

        // 3. Controller's finally-block calls tryReleaseProcessUpdate()
        appService.tryReleaseProcessUpdate(appDef);

        // Assert: Lock is released
        boolean exists = Files.exists(lockPath);
        assertFalse("Lock file should be deleted when no migration is running", exists);
    }

    /**
     * Test 3: Concurrency protection — when updateRunningProcesses() is called and
     * adds to the processMigration map, an immediate tryReleaseProcessUpdate() should not
     * delete the lock file (because migration is still running in the background thread).
     */
    @Test
    public void testConcurrencyProtection_lockRemainsWhileMigrationRunning() throws Exception {

        // 1. Lock acquired
        appService.lockProcessUpdate(appDef);

        // 2. Migration starts (Map is populated)
        String migrationKey = TEST_PROFILE + "::" + appDef.getAppId() + "_" + appDef.getVersion() + "::1";
        appService.processMigration.put(migrationKey, "2");

        Path lockPath = Paths.get(processMigrationPath + "testApp_1.lock");

        // 3. Concurrent save attempt calls tryRelease
        appService.tryReleaseProcessUpdate(appDef);

        // Assert: Lock remains (Migration still running)
        boolean exists = Files.exists(lockPath);
        assertTrue("Lock file must remain while processMigration map has an entry for this app", exists);
    }

    /**
     * Verifies cleanup can recognize both sides of an active migration.
     *
     * The production cleanup path receives only a package id and Shark package version, but
     * processMigration stores the source version in the key and the target version in the
     * value. Both versions must be protected from removeUnusedXpdl while migration is active.
     */
    @Test
    public void testProcessMigrationVersionLookup_matchesSourceAndTargetVersions() {
        String migrationKey = TEST_PROFILE + "::" + appDef.getAppId() + "_" + appDef.getVersion() + "::1";
        appService.processMigration.put(migrationKey, "2");

        assertTrue("Source package version should be recognized as active migration", appService.isPackageVersionInProcessMigration(TEST_PROFILE, appDef.getAppId(), "1"));
        assertTrue("Target package version should be recognized as active migration", appService.isPackageVersionInProcessMigration(TEST_PROFILE, appDef.getAppId(), "2"));
        assertFalse("Unrelated package version should not be recognized as active migration", appService.isPackageVersionInProcessMigration(TEST_PROFILE, appDef.getAppId(), "3"));
    }

    /**
     * Migration must not start from afterCommit because higher-priority package-cache eviction
     * callbacks execute during afterCompletion. Keeping the migration marker until the committed
     * completion callback runs prevents both the controller and a fast migration from releasing
     * the Process Builder save lock before that cache fence is complete.
     */
    @Test
    public void testUpdateRunningProcesses_startsOnlyFromCommittedAfterCompletion() {
        RecordingAppServiceImpl service = new RecordingAppServiceImpl(new ProcessUpdateLockManager(baseDirectory));
        service.workflowUserManager = mock(org.joget.workflow.model.service.WorkflowUserManager.class);
        AppUtil.setCurrentAppDefinition(appDef);

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            service.updateRunningProcesses("testApp", 1L, 2L);

            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            assertEquals(1, synchronizations.size());
            TransactionSynchronization synchronization = synchronizations.get(0);
            assertEquals("Migration startup must run after higher-priority cache eviction callbacks",
                    Ordered.LOWEST_PRECEDENCE, synchronization.getOrder());

            synchronization.afterCommit();
            assertEquals("afterCommit is too early to start migration", 0, service.migrationStarts);

            synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
            assertEquals("Committed afterCompletion should start migration", 1, service.migrationStarts);
        } finally {
            service.processMigration.clear();
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private static class RecordingAppServiceImpl extends AppServiceImpl {
        private int migrationStarts;

        private RecordingAppServiceImpl(ProcessUpdateLockManager processUpdateLockManager) {
            super(processUpdateLockManager);
        }

        @Override
        protected void startProcessMigrationThread(String profile, AppDefinition appDef, User currentUser,
                String packageId, Long fromVersion, Long toVersion, String migrationKey) {
            migrationStarts++;
        }
    }
}
