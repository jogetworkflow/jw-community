package org.joget.apps.app.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.ProcessMappingInfo;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PluginThread;
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
            boolean waitAllFinish = "true".equalsIgnoreCase(getPropertyString("waitAllFinish"));
            boolean runInMultiThread = "true".equalsIgnoreCase(getPropertyString("runInMultiThread"));

            Thread newThread;
            Collection<Thread> threads = new ArrayList<Thread>();
        
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            for (Object tool : tools) {
                if (tool != null && tool instanceof Map) {
                    Map toolMap = (Map) tool;
                    if (toolMap != null && toolMap.containsKey("className") && !toolMap.get("className").toString().isEmpty()) {
                        String className = toolMap.get("className").toString();
                        ApplicationPlugin p = (ApplicationPlugin) pluginManager.getPlugin(className);

                        if (p != null) {
                            final Map propertiesMap = new HashMap(properties);
                            propertiesMap.putAll((Map) toolMap.get("properties"));
                            final ApplicationPlugin appPlugin = (ApplicationPlugin) p;

                            if (appPlugin instanceof PropertyEditable) {
                                ((PropertyEditable) appPlugin).setProperties(propertiesMap);
                            }

                            if (runInMultiThread) {
                                newThread = new PluginThread(new Runnable() {
                                    public void run() {
                                        appPlugin.execute(propertiesMap);
                                    }
                                });
                                newThread.start();
                                threads.add(newThread);
                            } else {
                                appPlugin.execute(propertiesMap);
                            }
                        }
                    }
                }
            }
            
            if(runInMultiThread && waitAllFinish){
                //wait for all threads to finish before reaching to the end of this plugin.
                //if do not wait, the tool will reach the end and workflow process will continue without waiting for the end of all threads' executions.
                for(Thread thread : threads){
                    try {
                        thread.join();
                    } catch (InterruptedException ex) {
                        LogUtil.error(getClassName(), ex, "");
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
