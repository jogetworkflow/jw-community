package org.joget.plugin.base;

import org.joget.commons.util.LogUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testPluginBaseApplicationContext.xml"})
public class TestPluginManager {

    @Autowired
    PluginManager pluginManager;

    private final String samplePluginFile = "/wflow-plugin-test-9.0-SNAPSHOT.jar";
    private final String samplePluginNoManifestFile = "/wflow-plugin-test-9.0-SNAPSHOT-noManifest.jar";
    private final String samplePlugin = "org.joget.plugin.sample.dx9.Dx9SamplePlugin";
    private final String sampleJdbcPlugin = "org.joget.plugin.sample.dx9.Dx9JdbcSamplePlugin";
    private final String sampleMigrationPluginFile = "/wflow-plugin-test-migration-9.0-TEST.jar";
    private final String sampleMigrationPlugin = "org.joget.plugin.sample.SamplePlugin";
    private final String sampleJdbcMigrationPlugin = "org.joget.plugin.sample.JdbcSamplePlugin";

    public String getSamplePluginFile() {
        return TestPluginManager.class.getResource(samplePluginFile).getPath();
    }
    
    public String getSampleMigrationPluginFile() {
        return TestPluginManager.class.getResource(sampleMigrationPluginFile).getPath();
    }

    public String getSamplePluginNoManifestFile() {
        return TestPluginManager.class.getResource(samplePluginNoManifestFile).getPath();
    }

    @Test
    public void testPluginManager() {
        Assert.notNull(pluginManager, "false");
    }
    
    @Test
    public void testDx9Plugin() {
        System.out.println(" === testDX9Plugin === ");
        testPlugin(getSamplePluginFile(), samplePlugin, sampleJdbcPlugin);
    }
    
    @Test
    public void testDX8PluginMigration() {
        System.out.println(" === testDX8PluginMigration === ");
        testPlugin(getSampleMigrationPluginFile(), sampleMigrationPlugin, sampleJdbcMigrationPlugin);
    }
    
    /**
     * Test a installed plugin with hibernate, webservice & jdbc
     * @param path
     * @param pluginName
     * @param jdbcPluginName 
     */
    public void testPlugin(String path, String pluginName, String jdbcPluginName) {
        Plugin plugin;
        Object result;
        try {
            //make sure plugin is not exist first
            pluginManager.uninstall(pluginName);
            plugin = pluginManager.getPlugin(pluginName);
            Assert.isTrue(plugin == null, "Plugin should not exist");

            InputStream in = null;
            try {
                File file = new File(path);
                if (file.exists()) {
                    in = new FileInputStream(file);
                    pluginManager.upload(file.getName(), in);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            //check plugin is exist after installed
            plugin = pluginManager.getPlugin(pluginName);
            Assert.isTrue(plugin != null, "Plugin should installed");
            
            try {
                //test web service
                MockHttpServletRequest request = new MockHttpServletRequest();
                MockHttpServletResponse response = new MockHttpServletResponse();

                request.addParameter("_action", "add");
                request.addParameter("name", "product_name");
                request.addParameter("desc", "product_description");
                
                //call the web service
                ((PluginWebSupport) plugin).webService(request, response);
                
                // Verify that the response contains the expected output
                result = response.getContentAsString();
                System.out.println(">>> " + result);
                Assert.isTrue(result.toString().contains("success"), "the web service response is wrong");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //test jdbc execution
            result = pluginManager.execute(jdbcPluginName, null);
            System.out.println(">>> " + result);
            Assert.isTrue("1".equals((String) result), "JDBC Plugin fail to return correct result after executed");

            //test hibernate execution
            result = pluginManager.execute(pluginName, null);
            System.out.println(">>> " + result);
            Assert.isTrue("test".equals((String) result), "Hibernate Plugin fail to return correct result after executed");
        
        } finally {
            //Uninstall the plugin
            pluginManager.uninstall(pluginName);
            plugin = pluginManager.getPlugin(pluginName);
            Assert.isTrue(plugin == null, "Plugin should uninstalled");
        }
    }
    
    @Test
    public void testList() {
        System.out.println(" ===testList=== ");
        Collection<Plugin> list = pluginManager.list();
        for (Plugin p : list) {
            LogUtil.info(getClass().getName(), " plugin: " + p.getName() + "; " + p.getClass().getName());
            System.out.println(" plugin: " + p.getName() + "; " + p.getClass().getName());
        }
        Assert.isTrue(!list.isEmpty(), "false");
    }

    @Test
    public void testFilteredList() {
        System.out.println(" ===testFilteredList=== ");
        boolean validPlugins = true;
        Collection<Plugin> list = pluginManager.list(ApplicationPlugin.class);
        for (Plugin p : list) {
            validPlugins = validPlugins && p instanceof ApplicationPlugin;
            LogUtil.info(getClass().getName(), " plugin: " + p.getName() + "; " + p.getClass().getName());
            System.out.println(" plugin: " + p.getName() + "; " + p.getClass().getName());
        }
        Assert.isTrue(validPlugins, "false");
    }

    @Test
    public void testLoadClassPlugin() {
        System.out.println(" ===testLoadClassPlugin=== ");
        String pluginName = "org.joget.plugin.base.SampleApplicationPlugin";
        Plugin plugin = pluginManager.getPlugin(pluginName);
        Assert.isTrue(plugin.getClass().getClassLoader() == SampleApplicationPlugin.class.getClassLoader(), "false");
    }

    @Test
    public void testLoadClassPluginResource() throws IOException {
        System.out.println(" ===testLoadClassPluginResource=== ");
        String pluginName = "org.joget.plugin.base.SampleApplicationPlugin";
        InputStream input = null;
        try {
            input = pluginManager.getPluginResource(pluginName, "/resources/SampleApplicationPlugin.txt");
            Assert.isTrue(input != null, "false");
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    @Test
    public void testPluginWebSupoort() throws Exception {
        System.out.println(" ===testPluginWebSupport=== ");
        String pluginName = "org.joget.plugin.base.SampleApplicationPlugin";
        Plugin plugin = pluginManager.getPlugin(pluginName);
        PluginWebSupport pluginWeb = (PluginWebSupport) plugin;

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addParameter("arg1", "arg1");
        request.addParameter("arg2", "arg2");

        pluginWeb.webService(request, response);
        Assert.isTrue("{arg1:\"arg1\", arg2:\"arg2\"}".equals(response.getContentAsString()), "false");
    }

    @Test
    public void testPluginRequiresMigration() throws IOException {
        System.out.println(" ===testPluginRequiresMigration===");

        // should not require transformation
        File notRequireTransformationFile = new File(getSamplePluginFile());
        boolean requiresTransform = pluginManager.requiresMigration(notRequireTransformationFile);
        Assert.isTrue(!requiresTransform, "The plugin should not require transformation!");

        // should require transformation
        File requireTransformationFile = new File(getSampleMigrationPluginFile());
        requiresTransform = pluginManager.requiresMigration(requireTransformationFile);
        Assert.isTrue(requiresTransform, "The plugin should require transformation!");

        // should throw FileNotFoundException (no manifest file)
        final File noManifestFile = new File(getSamplePluginNoManifestFile());
        org.junit.Assert.assertThrows(FileNotFoundException.class, () -> pluginManager.requiresMigration(noManifestFile));
    }

    @Test
    public void testPluginFileLockRepeat() {
        System.out.println(" ===testPluginFileLockRepeat===");

        PluginManager p1 = new PluginManager();
        PluginManager p2 = new PluginManager();
        File pluginFile = new File(getSamplePluginFile());
        File checkPluginLock = new File(getSamplePluginFile() + ".lock");

        // Test obtain lock
        boolean p1LockObtained = p1.obtainPluginFileLock(pluginFile);
        Assert.isTrue(p1LockObtained && checkPluginLock.exists(), "Plugin lock should be obtained by p1!");

        // Test obtain lock again
        boolean p1LockObtainedAgain = p1.obtainPluginFileLock(pluginFile);
        Assert.isTrue(p1LockObtainedAgain && checkPluginLock.exists(), "Plugin lock should be reported as obtained by p1!");

        // Test second node obtain file lock
        boolean p2LockObtained = p2.obtainPluginFileLock(pluginFile);
        Assert.isTrue(!p2LockObtained, "Plugin lock should NOT be obtained by p2!");

        // Test second node release file lock
        boolean p2LockReleased = p2.releasePluginFileLock(pluginFile);
        Assert.isTrue(!p2LockReleased, "Plugin lock should NOT be released by p2!");

        // Test release lock
        boolean p1LockReleased = p1.releasePluginFileLock(pluginFile);
        Assert.isTrue(p1LockReleased && !checkPluginLock.exists(), "Plugin lock should be released by p1!");

        // Test release lock again
        boolean p1LockReleasedAgain = p1.releasePluginFileLock(pluginFile);
        Assert.isTrue(p1LockReleasedAgain && !checkPluginLock.exists(), "Plugin lock should be reported as released by p1!");

        // Test second node release file lock again
        boolean p2LockReleasedAgain = p2.releasePluginFileLock(pluginFile);
        Assert.isTrue(p2LockReleasedAgain, "Plugin lock should be reported as released by p2!");
    }

    /**
     * To test whether two nodes (represented by different threads) can obtain a lock on the same file simultaneously
     * Expected: Only one of the threads can obtain the lock for the same file. (t1Lock != t2Lock)
     */
    @Test
    public void testSimultaneousPluginFileLock() {
        System.out.println(" ===testSimultaneousPluginFileLock===");

        final File pluginFile = new File(getSamplePluginFile());
        final AtomicBoolean t1Lock = new AtomicBoolean(false);
        final AtomicBoolean t2Lock = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        final PluginManager p1 = new PluginManager();
        final PluginManager p2 = new PluginManager();

        System.out.println("p1 upload: " + p1.getUploadDir());
        System.out.println("p2 upload: " + p2.getUploadDir());
        try {
            // Create worker threads to simulate nodes attempting to lock a shared file at (roughly) the same time
            Thread t1 = new Thread(() -> {
                try {
                    latch.await();
                    System.out.println("t1 start: " + Instant.now());
                    t1Lock.set(p1.obtainPluginFileLock(pluginFile));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            Thread t2 = new Thread(() -> {
                try {
                    latch.await();
                    System.out.println("t2 start: " + Instant.now());
                    t2Lock.set(p2.obtainPluginFileLock(pluginFile));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            t1.start();
            t2.start();
            // execute all worker threads at the same time
            latch.countDown();
            t1.join();
            t2.join();

            System.out.println("t1Lock: " + t1Lock.get());
            System.out.println("t2Lock: " + t2Lock.get());
            Assert.isTrue(t1Lock.get() != t2Lock.get(), "Only one thread should obtain the lock!");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            p1.releasePluginFileLock(pluginFile);
            p2.releasePluginFileLock(pluginFile);
        }
    }

    @Test
    public void testHandleFileChange() {
        System.out.println(" ===testHandleFileChange===");
        File pluginFile = new File(getSamplePluginFile());
        PluginManager p = new PluginManager();
        p.handleFileChange(pluginFile);
        Assert.notNull(p.getPlugin(samplePlugin), "Plugin should be installed by handleFileChange");
    }

    /**
     * This tests whether the handleFileChange method will install the plugin if the plugin file is locked.
     * The following should be the expected result (in order):
     * <ol>
     *     <li>First node successfully obtains file lock</li>
     *     <li>Second node fails to obtain file lock and unable to proceed "handleFileChange"</li>
     * </ol>
     */
    @Test
    public void testPluginFileLockHandleFileChange() {
        System.out.println(" ===testPluginFileLockHandleFileChange===");

        File pluginFile = new File(getSamplePluginFile());
        PluginManager p1 = new PluginManager();
        PluginManager p2 = new PluginManager();

        try {
            boolean lockObtained = p1.obtainPluginFileLock(pluginFile);
            Assert.isTrue(lockObtained, "Node 1 should have obtained the lock!");

            p2.handleFileChange(pluginFile);
            Plugin plugin = p2.getPlugin(samplePlugin);
            Assert.isNull(plugin, "Node 2 should not have installed the pluginFile!");
        } finally {
            p1.releasePluginFileLock(pluginFile);
        }
    }

    @Test
    public void testRefreshRunsOnceForSingleCaller() {
        CountingRefreshPluginManager p = new CountingRefreshPluginManager();

        p.refresh();

        Assert.isTrue(p.getUninstallCount() == 1, "Refresh should uninstall once");
        Assert.isTrue(p.getInstallCount() == 1, "Refresh should install once");
    }

    @Test
    public void testConcurrentRefreshWaitsWithoutDuplicateWork() throws Exception {
        CountDownLatch refreshStarted = new CountDownLatch(1);
        CountDownLatch releaseRefresh = new CountDownLatch(1);
        CountingRefreshPluginManager p = new CountingRefreshPluginManager(refreshStarted, releaseRefresh);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread leader = new Thread(() -> runRefresh(p, failure), "refresh-leader");
        leader.start();
        Assert.isTrue(refreshStarted.await(5, TimeUnit.SECONDS), "Leader refresh should start");

        Thread follower = new Thread(() -> runRefresh(p, failure), "refresh-follower");
        follower.start();
        Thread.sleep(100);
        Assert.isTrue(follower.isAlive(), "Follower should wait for the active refresh");

        releaseRefresh.countDown();
        leader.join(5000);
        follower.join(5000);

        Assert.isTrue(!leader.isAlive(), "Leader refresh should finish");
        Assert.isTrue(!follower.isAlive(), "Follower refresh should finish");
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
        Assert.isTrue(p.getUninstallCount() == 1, "Concurrent refresh should uninstall once");
        Assert.isTrue(p.getInstallCount() == 1, "Concurrent refresh should install once");
    }

    @Test
    public void testRefreshRunsAfterWaitingForNonRefreshLockHolder() throws Exception {
        CountingRefreshPluginManager p = new CountingRefreshPluginManager();
        PluginManagerCache cache = PluginManager.getCache();
        ReentrantLock pluginListLock = cache.getPluginListLock();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        pluginListLock.lock();
        Thread refresher = new Thread(() -> runRefresh(p, failure), "refresh-after-list-lock");
        try {
            refresher.start();
            Thread.sleep(100);
            Assert.isTrue(p.getUninstallCount() == 0, "Refresh should wait while plugin list lock is held");
            Assert.isTrue(refresher.isAlive(), "Refresh thread should wait for plugin list lock");
        } finally {
            pluginListLock.unlock();
        }

        refresher.join(5000);

        Assert.isTrue(!refresher.isAlive(), "Refresh should finish after plugin list lock is released");
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
        Assert.isTrue(p.getUninstallCount() == 1, "Refresh should uninstall once after non-refresh lock holder");
        Assert.isTrue(p.getInstallCount() == 1, "Refresh should install once after non-refresh lock holder");
    }

    /**
     * A plain {@code FileOutputStream} follows a pre-existing symlink unconditionally. In a
     * multi-tenant Cloud deployment, {@code getUploadDir()} is overridden to a per-tenant profile
     * directory (see {@code MultiTenantPluginManager.getUploadDir()}) that the same tenant's own
     * OSGi/BeanShell code can otherwise write inside of, including planting a symlink under a
     * chosen filename pointing outside that directory. Reproduces that shape: plant a symlink at
     * the upload target path pointing to a file elsewhere, then upload -- the uploaded bytes must
     * never land at the symlink's target; {@code upload(..)} must refuse to write through the
     * symlink and create an ordinary file at the literal path instead.
     */
    @Test
    public void testUploadDoesNotFollowPreExistingSymlink() throws Exception {
        final Path tempDir = Files.createTempDirectory("testUploadSymlink");
        final Path outsideDir = Files.createTempDirectory("testUploadSymlinkOutside");
        try {
            final Path outsideTarget = outsideDir.resolve("planted.jar");
            final Path uploadTargetPath = tempDir.resolve("planted.jar");
            Files.createSymbolicLink(uploadTargetPath, outsideTarget);
            Assert.isTrue(Files.isSymbolicLink(uploadTargetPath), "setup: symlink must exist before upload");

            PluginManager p = new PluginManager() {
                @Override
                protected String getUploadDir() {
                    return tempDir.toString();
                }
            };

            byte[] uploadedBytes = buildMinimalValidJarBytes();
            try {
                p.upload("planted.jar", new ByteArrayInputStream(uploadedBytes));
            } catch (Exception expected) {
                // This test's minimal JAR is a valid zip/jar but not a real, loadable OSGi
                // bundle/plugin, so upload(..)'s own later plugin-load validation is expected to
                // reject it and clean up the file it just wrote -- irrelevant here. The property
                // under test (the write never reaches the symlink's outside target) is
                // established EARLIER in upload(..), before that later validation/cleanup runs,
                // and must hold regardless of what upload(..) does afterward for unrelated
                // plugin-validity reasons.
            }

            Assert.isTrue(!Files.exists(outsideTarget),
                    "the uploaded bytes must NOT have been written to the symlink's outside target -- "
                    + "this is the actual security property under test, independent of whatever "
                    + "upload(..) does afterward at the tenant-directory path itself");
        } finally {
            deleteRecursively(tempDir);
            deleteRecursively(outsideDir);
        }
    }

    /**
     * The symlink check in {@link #testUploadDoesNotFollowPreExistingSymlink()} only guards
     * {@code outputFile}'s own leaf path component -- {@code filename} can contain path
     * separators ({@code SecurityUtil.normalizedFileName(..)} rejects "../" traversal but not a
     * plain subdirectory segment), so an ancestor directory under {@code getUploadDir()} planted
     * as a symlink could resolve the write outside the intended upload directory even though the
     * leaf itself is never a symlink. Reproduces that shape: {@code tempDir/subdir} is pre-planted
     * as a symlink to {@code outsideDir}, then {@code upload("subdir/planted.jar", ..)} is called.
     *
     * <p>Asserts on the thrown exception's message rather than on {@code outsideDir}'s final
     * file-listing, since {@code upload(..)}'s later cleanup on install failure deletes the same
     * path regardless of whether the write was ever attempted, so file existence alone can't tell
     * a refused write apart from a written-then-cleaned-up one. The message can: the containment
     * check throws {@code ERROR_INVALID_PLUGIN_NAME} synchronously before the write ever runs,
     * while the pre-fix code reaches the write and fails later with a different message
     * ({@code ERROR_INSTALL_FAILED}).
     *
     * <p>{@code upload(..)} now also rejects any {@code /} or {@code \} in the filename outright,
     * before this containment check even runs -- see {@link
     * #testUploadRejectsFilenameContainingPathSeparatorOutright()}. This test still passes (both
     * checks throw the identical message) but the outright rejection is what actually fires now;
     * the containment check remains as defense-in-depth for any future caller that legitimately
     * needs a subdirectory.
     */
    @Test
    public void testUploadDoesNotFollowPreExistingSymlinkedAncestorDirectory() throws Exception {
        final Path tempDir = Files.createTempDirectory("testUploadSymlinkAncestor");
        final Path outsideDir = Files.createTempDirectory("testUploadSymlinkAncestorOutside");
        try {
            final Path plantedSymlinkDir = tempDir.resolve("subdir");
            Files.createSymbolicLink(plantedSymlinkDir, outsideDir);
            Assert.isTrue(Files.isSymbolicLink(plantedSymlinkDir), "setup: ancestor symlink must exist before upload");

            PluginManager p = new PluginManager() {
                @Override
                protected String getUploadDir() {
                    return tempDir.toString();
                }
            };

            byte[] uploadedBytes = buildMinimalValidJarBytes();
            try {
                p.upload("subdir/planted.jar", new ByteArrayInputStream(uploadedBytes));
                Assert.isTrue(false, "upload(..) through a symlinked ancestor directory must throw");
            } catch (PluginException actual) {
                Assert.isTrue(PluginManager.ERROR_INVALID_PLUGIN_NAME.equals(actual.getMessage()),
                        "upload(..) must refuse a path resolving outside the intended upload "
                        + "directory BEFORE ever attempting the write -- got \"" + actual.getMessage()
                        + "\" instead of the containment-check's own message, meaning the write "
                        + "was attempted (and only failed/cleaned up later, for an unrelated reason)");
            }

            Assert.isTrue(!Files.exists(outsideDir.resolve("planted.jar")),
                    "the uploaded bytes must NOT have been written under the symlinked ancestor "
                    + "directory's real target");
        } finally {
            deleteRecursively(tempDir);
            deleteRecursively(outsideDir);
        }
    }

    /**
     * The ancestor-containment check is check-then-act: if the ancestor directory doesn't exist
     * yet at check time (the ordinary case for a new subdirectory), the check trivially passes,
     * and a concurrent actor could replace it with a symlink before the write. Rather than trying
     * to make the containment check atomic with the write, {@code upload(..)} rejects any path
     * separator in the filename outright, unconditionally -- no legitimate caller needs a
     * subdirectory. Proves this fires even with no symlink anywhere in the picture (a genuinely
     * benign subdirectory name), confirming the rejection is unconditional on any {@code /}/
     * {@code \}, not merely a side effect of the containment check.
     */
    @Test
    public void testUploadRejectsFilenameContainingPathSeparatorOutright() throws Exception {
        final Path tempDir = Files.createTempDirectory("testUploadRejectsSlash");
        try {
            PluginManager p = new PluginManager() {
                @Override
                protected String getUploadDir() {
                    return tempDir.toString();
                }
            };

            byte[] uploadedBytes = buildMinimalValidJarBytes();
            try {
                p.upload("perfectly/ordinary/subdir/plugin.jar", new ByteArrayInputStream(uploadedBytes));
                Assert.isTrue(false, "upload(..) must reject a filename containing a path "
                        + "separator outright, even with no symlink involved at all");
            } catch (PluginException actual) {
                Assert.isTrue(PluginManager.ERROR_INVALID_PLUGIN_NAME.equals(actual.getMessage()),
                        "expected the invalid-plugin-name message, got \"" + actual.getMessage() + "\"");
            }

            Assert.isTrue(!Files.exists(tempDir.resolve("perfectly")),
                    "no subdirectory should ever have been created under the upload directory");
        } finally {
            deleteRecursively(tempDir);
        }
    }

    /**
     * Sanity/non-regression: an ordinary, slash-free filename must NOT be rejected by the new
     * outright path-separator check. Deliberately asserts on the exception MESSAGE, not file
     * existence: this minimal jar is not a loadable OSGi bundle, so upload(..)'s later
     * install-failure cleanup deletes the file it just wrote regardless of whether the filename
     * check passed -- the same reason the ancestor-symlink test above can't use file existence
     * either. If the new outright-rejection check wrongly fired here, the exception would be
     * ERROR_INVALID_PLUGIN_NAME; since it doesn't, the failure must come from the (expected,
     * unrelated) later install-failure path instead.
     */
    @Test
    public void testUploadStillAcceptsAnOrdinaryFilenameWithNoPathSeparator() throws Exception {
        final Path tempDir = Files.createTempDirectory("testUploadOrdinaryFilename");
        try {
            PluginManager p = new PluginManager() {
                @Override
                protected String getUploadDir() {
                    return tempDir.toString();
                }
            };

            byte[] uploadedBytes = buildMinimalValidJarBytes();
            try {
                p.upload("plugin.jar", new ByteArrayInputStream(uploadedBytes));
                Assert.isTrue(false, "expected this non-loadable-bundle jar to fail at the "
                        + "LATER install-failure stage, not to succeed outright");
            } catch (PluginException actual) {
                Assert.isTrue(!PluginManager.ERROR_INVALID_PLUGIN_NAME.equals(actual.getMessage()),
                        "an ordinary, slash-free filename must NOT be rejected by the outright "
                        + "path-separator check -- got the invalid-plugin-name message, meaning "
                        + "the new check wrongly fired for a filename with no separator at all");
            }
        } finally {
            deleteRecursively(tempDir);
        }
    }

    private static byte[] buildMinimalValidJarBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        try (JarOutputStream jos = new JarOutputStream(baos, manifest)) {
            jos.putNextEntry(new JarEntry("marker.txt"));
            jos.write("PROOF_2261_UPLOAD_MARKER".getBytes("UTF-8"));
            jos.closeEntry();
        }
        return baos.toByteArray();
    }

    private static void deleteRecursively(Path dir) {
        try {
            java.util.List<Path> paths = new java.util.ArrayList<>();
            Files.walk(dir).forEach(paths::add);
            java.util.Collections.reverse(paths);
            for (Path p : paths) {
                Files.deleteIfExists(p);
            }
        } catch (IOException ignore) {
            // best-effort cleanup only
        }
    }

    private void runRefresh(PluginManager pluginManager, AtomicReference<Throwable> failure) {
        try {
            pluginManager.refresh();
        } catch (Throwable t) {
            failure.compareAndSet(null, t);
        }
    }

    private static class CountingRefreshPluginManager extends PluginManager {
        private final AtomicInteger uninstallCount = new AtomicInteger();
        private final AtomicInteger installCount = new AtomicInteger();
        private final CountDownLatch refreshStarted;
        private final CountDownLatch releaseRefresh;

        CountingRefreshPluginManager() {
            this(null, null);
        }

        CountingRefreshPluginManager(CountDownLatch refreshStarted, CountDownLatch releaseRefresh) {
            this.refreshStarted = refreshStarted;
            this.releaseRefresh = releaseRefresh;
        }

        @Override
        protected void init() {
            // Avoid starting the real OSGi container for lock-behavior tests.
        }

        @Override
        public void uninstallAll(boolean deleteFiles) {
            uninstallCount.incrementAndGet();
        }

        @Override
        protected void installBundles() {
            installCount.incrementAndGet();
            if (refreshStarted != null) {
                refreshStarted.countDown();
            }
            if (releaseRefresh != null) {
                try {
                    releaseRefresh.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }

        int getUninstallCount() {
            return uninstallCount.get();
        }

        int getInstallCount() {
            return installCount.get();
        }
    }
}
