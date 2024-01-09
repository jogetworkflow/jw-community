package org.joget.apps.app.lib;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.joget.apps.app.model.AppOverviewData;
import org.joget.apps.app.model.AppOverviewToolAbstract;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.json.JSONObject;
import org.springframework.util.ClassUtils;

public class InstalledPluginOverviewTool extends AppOverviewToolAbstract {
    protected Map<String, String> osgiPlugins;
    protected Set<String> allPlugins;
    
    @Override
    public String getName() {
        return "InstalledPluginOverviewTool";
    }

    @Override
    public String getVersion() {
        return "9.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getLabel() {
        return "Installed Plugin";
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public void scan(String key, String path, String pluginClassName, JSONObject properties, JSONObject parentObject, AppOverviewData data) {
        if (pluginClassName != null && !pluginClassName.isEmpty() && pluginClassName.contains(".")) { //make sure it is classname
            if (getOsgiPlugins().containsKey(pluginClassName)) {
                data.addItemData(key, this, path, getOsgiPlugins().get(pluginClassName), pluginClassName);
            } else if (!getAllPlugins().contains(pluginClassName)) { //missing plugins
                data.addItemData(key, this, path, pluginClassName + " (Missing)", pluginClassName, true);
            }
        }
    }
    
    protected Map<String, String> getOsgiPlugins() {
        if (osgiPlugins == null) {
            osgiPlugins = new HashMap<String, String>();
                    
            // get osgi plugins
            PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
            Collection<Plugin> pluginList = pluginManager.listOsgiPlugin(null);
            
            for (Plugin plugin: pluginList) {
                osgiPlugins.put(ClassUtils.getUserClass(plugin).getName(), plugin.getI18nLabel());
            }
        }
        return osgiPlugins;
    }
    
    protected Set<String> getAllPlugins() {
        if (allPlugins == null) {
            allPlugins = new HashSet<String>();
                    
            // get all plugins
            PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
            Collection<Plugin> pluginList = pluginManager.list(null);
            
            for (Plugin plugin: pluginList) {
                allPlugins.add(ClassUtils.getUserClass(plugin).getName());
            }
            
            //add joget default classes
            allPlugins.add("org.joget.apps.userview.model.Userview");
            allPlugins.add("org.joget.apps.userview.model.UserviewCategory");
            allPlugins.add("org.joget.apps.userview.model.UserviewSetting");
            allPlugins.add("org.joget.apps.userview.model.UserviewPage");
            allPlugins.add("org.joget.apps.userview.model.UserviewLayout");
            allPlugins.add("org.joget.apps.userview.model.UserviewPermission");
        }
        return allPlugins;
    }

    @Override
    public String getIcon() {
        return "<i class=\"las la-plug\" aria-hidden=\"true\"></i>";
    }
}
