package org.joget.apps.app.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.commons.util.CsvUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

/**
 * Utility class to retrieve plugin propertise value and i18n message 
 * 
 */
@Service
public class AppPluginUtil implements ApplicationContextAware {

    static ApplicationContext appContext;

    /**
     * Utility method to retrieve the ApplicationContext of the system
     * @return 
     */
    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        appContext = context;
    }
    
    public static PluginDefaultProperties getPluginDefaultProperties(String id, AppDefinition appDef) {
        if (appDef != null) {
            Collection<PluginDefaultProperties> list = appDef.getPluginDefaultPropertiesList();
            if (list != null && !list.isEmpty()) {
                for (PluginDefaultProperties p : list) {
                    if (p.getId().equals(id)) {
                        return p;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Method to retrieve the final plugin properties value by passing the configured properties to override the default plugin properties in an App 
     * @param plugin
     * @param properties
     * @param appDef
     * @return 
     */
    public static Map getDefaultProperties(Plugin plugin, String properties, AppDefinition appDef) {
        return getDefaultProperties(plugin, properties, appDef, null);
    }

    /**
     * Method to retrieve the final plugin properties value by passing the configured properties to override the default plugin properties in an App.
     * Options to pass in WorkflowAssignment object for Hash Variable parsing.
     * @param plugin
     * @param properties
     * @param appDef
     * @param assignment
     * @return 
     */
    public static Map getDefaultProperties(Plugin plugin, String properties, AppDefinition appDef, WorkflowAssignment assignment) {
        
        Map propertyMap = new HashMap();

        try {
            properties = AppUtil.processHashVariable(properties, assignment, StringUtil.TYPE_JSON, null, appDef);
            if (!(plugin instanceof PropertyEditable)) {
                propertyMap = CsvUtil.getPluginPropertyMap(properties);
            } else {
                propertyMap = PropertyUtil.getPropertiesValueFromJson(properties);
            }
        } catch (Exception e) {
            LogUtil.error(AppPluginUtil.class.getName(), e, "Error @ getDefaultProperties");
        }
        
        propertyMap = getDefaultProperties(plugin, propertyMap, appDef, assignment);

        return propertyMap;
    }
    
    /**
     * Method to retrieve the final plugin properties value by passing the configured properties to override the default plugin properties in an App.
     * Options to pass in WorkflowAssignment object for Hash Variable parsing.
     * @param plugin
     * @param propertyMap
     * @param appDef
     * @param assignment
     * @return 
     */
    public static Map getDefaultProperties(Plugin plugin, Map propertyMap, AppDefinition appDef, WorkflowAssignment assignment) {
        
        if (propertyMap == null) {
            propertyMap = new HashMap();
        }

        try {
            PluginDefaultProperties pluginDefaultProperties = getPluginDefaultProperties(ClassUtils.getUserClass(plugin).getName(), appDef);

            if (pluginDefaultProperties != null && pluginDefaultProperties.getPluginProperties() != null && pluginDefaultProperties.getPluginProperties().trim().length() > 0) {
                Map defaultPropertyMap = new HashMap();

                if (!(plugin instanceof PropertyEditable)) {
                    defaultPropertyMap = CsvUtil.getPluginPropertyMap(pluginDefaultProperties.getPluginProperties());
                } else {
                    String json = pluginDefaultProperties.getPluginProperties();

                    //process basic hash variable
                    json = AppUtil.processHashVariable(json, null, StringUtil.TYPE_JSON, null, appDef);

                    defaultPropertyMap = PropertyUtil.getPropertiesValueFromJson(json);
                }

                Map tempPropertyMap = new HashMap(propertyMap);
                for (Object s : defaultPropertyMap.keySet()) {
                    String key = (String) s;
                    String defaultValue = defaultPropertyMap.get(key).toString().trim();
                    String value = "";

                    if (propertyMap.get(key) != null) {
                        value = propertyMap.get(key).toString().trim();
                    }

                    if (value.equals("") && !defaultValue.equals("")) {
                        tempPropertyMap.put(key, defaultValue);
                    }
                }
                propertyMap = tempPropertyMap;
            }
        } catch (Exception e) {
            LogUtil.error(AppPluginUtil.class.getName(), e, "Error @ getDefaultProperties");
        }

        return propertyMap;
    }
    
    /**
     * Method to get a message from a plugin messages bundle
     * @param key
     * @param pluginName
     * @param translationPath
     * @return 
     */
    public static String getMessage(String key, String pluginName, String translationPath){
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        return pluginManager.getMessage(key, pluginName, translationPath);
    }
}
