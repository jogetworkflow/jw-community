package org.joget.apps.app.service;

import org.joget.apps.app.model.AppDefinition;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.SetupManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class AppServiceImplTest {

    private AppServiceImpl appService;
    private AppDefinition appDef;
    private String processMigrationPath;
    private static final String TEST_PROFILE = "default";

    @Before
    public void setUp() throws Exception {
        // Set the current profile in the thread to avoid DynamicDataSourceManager.cache
        // NPE
        HostManager.setCurrentProfile(TEST_PROFILE);

        appService = new AppServiceImpl();

        appDef = new AppDefinition();
        appDef.setAppId("testApp");
        appDef.setVersion(1L);

        processMigrationPath = SetupManager.getBaseDirectory() + "app_migration" + File.separator;

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
}
