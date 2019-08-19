package org.joget.apps.app.lib;

import java.util.Map;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.ProcessMappingInfo;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.ApplicationPlugin;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;

public class MultiTools extends DefaultApplicationPlugin implements ProcessMappingInfo {
    
    @Override
    public String getName() {
        return "Multi Tools";
    }

    @Override
    public String getVersion() {
        return "7.0.0";
    }

    @Override
    public String getDescription() {
        return "Enable the use of multiple tools";
    }
    
    @Override
    public String getLabel() {
        return "Multi Tools";
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }
    
    @Override
    public String getPropertyOptions() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String appId = appDef.getId();
        String appVersion = appDef.getVersion().toString();
        Object[] arguments = new Object[]{appId, appVersion};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/app/multiTools.json", arguments, true, null);
        return json;
    }

    @Override
    public Object execute(Map properties) {
        Object[] tools = (Object[]) getProperty("tools");
        if (tools != null && tools.length > 0) {
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            for (Object tool : tools) {
                if (tool != null && tool instanceof Map) {
                    Map toolMap = (Map) tool;
                    if (toolMap != null && toolMap.containsKey("className") && !toolMap.get("className").toString().isEmpty()) {
                        String className = toolMap.get("className").toString();
                        ApplicationPlugin p = (ApplicationPlugin)pluginManager.getPlugin(className);

                        if (p != null) {
                            Map propertiesMap = (Map) toolMap.get("properties");
                            ApplicationPlugin appPlugin = (ApplicationPlugin) p;

                            if (appPlugin instanceof PropertyEditable) {
                                ((PropertyEditable) appPlugin).setProperties(propertiesMap);
                            }
                            appPlugin.execute(propertiesMap);
                        }
                    }
                }
            }
        }
        
        return null;
    }

    @Override
    public String getMappingInfo() {
        String info = "";
        Object[] tools = (Object[]) getProperty("tools");
        if (tools != null && tools.length > 0) {
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            int i = 1;
            for (Object tool : tools) {
                if (tool != null && tool instanceof Map) {
                    Map toolMap = (Map) tool;
                    if (toolMap != null && toolMap.containsKey("className") && !toolMap.get("className").toString().isEmpty()) {
                        String className = toolMap.get("className").toString();
                        Plugin p = pluginManager.getPlugin(className);

                        if (p != null) {
                            if (!info.isEmpty()) {
                                info += "<br/>";
                            }
                            info += i++ + ". " + p.getI18nLabel() + " (" + p.getVersion() + ")";
                        }
                    }
                }
            }
        }
        return info;
    }
}
