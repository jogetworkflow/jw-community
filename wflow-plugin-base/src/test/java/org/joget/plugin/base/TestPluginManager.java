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
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

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
}
