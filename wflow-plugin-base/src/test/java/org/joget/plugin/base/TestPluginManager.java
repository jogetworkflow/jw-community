package org.joget.plugin.base;

import org.joget.commons.util.LogUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

@RunWith(value=SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:pluginBaseApplicationContext.xml"})
public class TestPluginManager {

    @Autowired
    PluginManager pluginManager;

    private String samplePluginFile = "../wflow-plugins/wflow-plugin-sample/target/wflow-plugin-sample.jar";
    private String samplePlugin = "org.joget.plugin.sample.SamplePlugin";

    public String getSamplePluginFile() {
        return samplePluginFile;
    }

    public String getSamplePlugin() {
        return samplePlugin;
    }

    @Test
    public void testPluginManager() {
        Assert.notNull(pluginManager);
    }

    //@Test
    public void testInstall() {

        InputStream in = null;
        try {
            LogUtil.info(getClass().getName(), " ===testInstall=== ");
            File file = new File(getSamplePluginFile());
            if (file.exists()) {
                in = new FileInputStream(file);
                pluginManager.upload(file.getName(), in);
            }
        } catch (Exception ex) {
            LogUtil.error(PluginManager.class.getName(), ex, "");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                LogUtil.error(PluginManager.class.getName(), ex, "");
            }
        }
    }

    @Test
    public void testList() {
        LogUtil.info(getClass().getName(), " ===testList=== ");
        Collection<Plugin> list = pluginManager.list();
        for (Plugin p : list) {
            LogUtil.info(getClass().getName(), " plugin: " + p.getName() + "; " + p.getClass().getName());
            System.out.println(" plugin: " + p.getName() + "; " + p.getClass().getName());
        }
        Assert.isTrue(list.size() > 0);
    }

    @Test
    public void testFilteredList() {
        LogUtil.info(getClass().getName(), " ===testFilteredList=== ");
        boolean validPlugins = true;
        Collection<Plugin> list = pluginManager.list(ApplicationPlugin.class);
        for (Plugin p : list) {
            validPlugins = validPlugins && p instanceof ApplicationPlugin;
            LogUtil.info(getClass().getName(), " plugin: " + p.getName() + "; " + p.getClass().getName());
            System.out.println(" plugin: " + p.getName() + "; " + p.getClass().getName());
        }
        Assert.isTrue(validPlugins);
    }

    @Test
    public void testLoadOsgiPlugin() {
        LogUtil.info(getClass().getName(), " ===testLoadOsgiPlugin=== ");
        String reportPlugin = "org.joget.plugin.report.ReportPlugin";
        Plugin plugin = pluginManager.getPlugin(reportPlugin);
        if (plugin != null) {
            // Assertion only valid when the ReportPlugin jar is available
            //Assert.isTrue(plugin.getClass().getClassLoader() != ReportPlugin.class.getClassLoader());
        }
    }

    @Test
    public void testLoadClassPlugin() {
        LogUtil.info(getClass().getName(), " ===testLoadClassPlugin=== ");
        String pluginName = "org.joget.plugin.base.SampleApplicationPlugin";
        Plugin plugin = pluginManager.getPlugin(pluginName);
        Assert.isTrue(plugin.getClass().getClassLoader() == SampleApplicationPlugin.class.getClassLoader());
    }

    @Test
    public void testLoadClassPluginResource() throws IOException {
        LogUtil.info(getClass().getName(), " ===testLoadClassPluginResource=== ");
        String pluginName = "org.joget.plugin.base.SampleApplicationPlugin";
        InputStream input = null;
        try {
            input = pluginManager.getPluginResource(pluginName, "/resources/SampleApplicationPlugin.txt");
            Assert.isTrue(input != null);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    @Test
    public void testPluginWebSupoort() throws Exception {
        LogUtil.info(getClass().getName(), " ===testPluginWebSupoort=== ");
        String pluginName = "org.joget.plugin.base.SampleApplicationPlugin";
        Plugin plugin = pluginManager.getPlugin(pluginName);
        PluginWebSupport pluginWeb = (PluginWebSupport) plugin;

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addParameter("arg1", "arg1");
        request.addParameter("arg2", "arg2");

        pluginWeb.webService(request, response);
        Assert.isTrue("{arg1:\"arg1\", arg2:\"arg2\"}".equals(response.getContentAsString()));
    }

    //@Test
    public void testExecute() {
        LogUtil.info(getClass().getName(), " ===testExecute=== ");

        Object result = pluginManager.execute(getSamplePlugin(), null);
        //Assert.isTrue(result != null);
    }

    //@Test
    public void testUninstall() {
        LogUtil.info(getClass().getName(), " ===testUninstall=== ");
        pluginManager.uninstall(getSamplePlugin());
    }

    @Test
    public void testPluginTest() {
        LogUtil.info(getClass().getName(), " ===testPluginTest=== ");
        pluginManager.testPlugin(getSamplePlugin(), getSamplePluginFile(), null, true);
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
     * {@code upload(..)} must not follow a pre-existing symlink at the upload target path. In a
     * multi-tenant Cloud deployment, {@code getUploadDir()} is overridden to a per-tenant profile
     * directory (see {@code MultiTenantPluginManager.getUploadDir()}) that the same tenant's own
     * OSGi/BeanShell code can otherwise write inside of -- including planting a symlink there,
     * under a chosen filename, pointing outside that directory. Plants a symlink at the upload
     * target path pointing to a file elsewhere, then uploads: the uploaded bytes must not land at
     * the symlink's target, and the fix must instead refuse to write through the symlink and
     * create an ordinary file at the literal path.
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
                // This minimal JAR is a valid zip but not a loadable OSGi bundle, so upload(..)'s
                // later plugin-load validation is expected to reject it and clean up the file it
                // wrote -- irrelevant here. The property under test (the write never reaches the
                // symlink's outside target) is established earlier, before that validation runs.
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
     * The symlink check above only guards {@code outputFile}'s own leaf path component --
     * {@code filename} can contain path separators ({@code SecurityUtil.normalizedFileName(..)}
     * rejects "../" traversal but not a plain subdirectory segment), so an ancestor directory
     * under {@code getUploadDir()} planted as a symlink (by some other write reaching the same
     * directory tree) could resolve the write outside the intended upload directory even though
     * the leaf itself is never a symlink. Reproduces that shape directly: {@code tempDir/subdir}
     * is pre-planted as a symlink to {@code outsideDir}, then {@code upload("subdir/planted.jar",
     * ..)} is called.
     *
     * <p>Asserts on the thrown exception's message rather than on {@code outsideDir}'s file
     * listing, because {@code upload(..)}'s later install-failure cleanup also deletes
     * {@code outputFile} on the same path, so a written-then-cleaned-up file and a write refused
     * outright both leave {@code outsideDir} empty -- only the message tells them apart: the
     * containment check throws {@code ERROR_INVALID_PLUGIN_NAME} synchronously before the write,
     * while a write that was actually attempted fails later with {@code ERROR_INSTALL_FAILED}.
     *
     * <p>Note: {@code upload(..)} also rejects any {@code /} or {@code \} in the filename
     * outright, before this containment check even runs -- see {@link
     * #testUploadRejectsFilenameContainingPathSeparatorOutright()}. This test still passes (both
     * checks throw the identical message), with the outright rejection firing first and the
     * containment check remaining as defense-in-depth for any future caller that legitimately
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
     * The ancestor-containment check above is check-then-act -- if the ancestor directory doesn't
     * exist yet at check time (the normal case for a new subdirectory), the check passes trivially
     * and a concurrent actor could replace it with a symlink before the write. Rather than making
     * the containment check atomic with the write, {@code upload(..)} rejects any path separator
     * in the filename outright, unconditionally -- no legitimate caller needs a subdirectory. Proves
     * the rejection fires even with no symlink anywhere in the picture (a genuinely benign
     * subdirectory name), confirming it is unconditional and not merely a side effect of the
     * containment check.
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
