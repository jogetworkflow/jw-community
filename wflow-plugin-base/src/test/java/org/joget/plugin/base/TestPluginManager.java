package org.joget.plugin.base;

import java.io.*;
import java.util.Collection;
import org.joget.commons.util.LogUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

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
}
