package org.joget.plugin.base;

import org.joget.commons.util.LogUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

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
        InputStream input = pluginManager.getPluginResource(pluginName, "/resources/SampleApplicationPlugin.txt");
        Assert.isTrue(input != null);
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
}
