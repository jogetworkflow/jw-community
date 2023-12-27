package org.joget.apps.app.lib;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.model.AppOverviewData;
import org.joget.apps.app.model.AppOverviewToolAbstract;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.json.JSONObject;
import org.springframework.util.ClassUtils;

public class InstalledPluginOverviewTool extends AppOverviewToolAbstract {
    protected Map<String, String> plugins;
    
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
        if (pluginClassName != null && !pluginClassName.isEmpty() && getOsgiPlugins().containsKey(pluginClassName)) {
            data.addItemData(key, this, path, getOsgiPlugins().get(pluginClassName), pluginClassName);
        }
    }
    
    protected Map<String, String> getOsgiPlugins() {
        if (plugins == null) {
            plugins = new HashMap<String, String>();
                    
            // get osgi plugins
            PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
            Collection<Plugin> pluginList = pluginManager.listOsgiPlugin(null);
            
            for (Plugin plugin: pluginList) {
                plugins.put(ClassUtils.getUserClass(plugin).getName(), plugin.getI18nLabel());
            }
        }
        return plugins;
    }

    @Override
    public String getIcon() {
        return "<i class=\"las la-plug\" aria-hidden=\"true\"></i>";
    }
}
