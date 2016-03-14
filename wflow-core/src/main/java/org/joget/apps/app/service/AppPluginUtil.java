package org.joget.apps.app.service;

import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
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

@Service
public class AppPluginUtil implements ApplicationContextAware {

    static ApplicationContext appContext;

    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        appContext = context;
    }
    
    public static Map getDefaultProperties(Plugin plugin, String properties, AppDefinition appDef) {
        return getDefaultProperties(plugin, properties, appDef, null);
    }

    public static Map getDefaultProperties(Plugin plugin, String properties, AppDefinition appDef, WorkflowAssignment assignment) {
        
        Map propertyMap = new HashMap();

        try {
            properties = AppUtil.processHashVariable(properties, assignment, StringUtil.TYPE_JSON, null, appDef);
            if (!(plugin instanceof PropertyEditable)) {
                propertyMap = CsvUtil.getPluginPropertyMap(properties);
            } else {
                propertyMap = PropertyUtil.getPropertiesValueFromJson(properties);
            }

            PluginDefaultPropertiesDao pluginDefaultPropertiesDao = (PluginDefaultPropertiesDao) appContext.getBean("pluginDefaultPropertiesDao");
            PluginDefaultProperties pluginDefaultProperties = pluginDefaultPropertiesDao.loadById(plugin.getClass().getName(), appDef);

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
    
    public static String getMessage(String key, String pluginName, String translationPath){
        PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
        return pluginManager.getMessage(key, pluginName, translationPath);
    }
}
