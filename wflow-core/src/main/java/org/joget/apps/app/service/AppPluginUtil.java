package org.joget.apps.app.service;

import bsh.Interpreter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.lib.RulesDecisionPlugin;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.commons.util.CsvUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
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
        if (appContext != null) {
            PluginManager pluginManager = (PluginManager) appContext.getBean("pluginManager");
            return pluginManager.getMessage(key, pluginName, translationPath);
        } else {
            return key;
        }
    }
    
    /**
     * Method to get rule editor script
     * @param plugin
     * @param request
     * @param response
     * @return 
     */
    public static String getRuleEditorScript(HttpServletRequest request, HttpServletResponse response) {
        String variables = "";
        String transitions = "";
        
        String processId = SecurityUtil.validateStringInput(request.getParameter("processId"));
        
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        PackageDefinition packageDef = appDef.getPackageDefinition();
        if (packageDef != null) {
            processId = AppUtil.getProcessDefIdWithVersion(packageDef.getId(), packageDef.getVersion().toString(), processId);
        }
        if (!processId.isEmpty()) {
            String actId = SecurityUtil.validateStringInput(request.getParameter("actId"));
            
            WorkflowManager workflowManager = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");
            
            //get variable list
            Collection<WorkflowVariable> variableList = workflowManager.getProcessVariableDefinitionList(processId);
            if (variableList != null && !variableList.isEmpty()) {
                for (WorkflowVariable v : variableList) {
                    if (!variables.isEmpty()) {
                        variables += ",";
                    }
                    variables += "\"" + v.getId() + "\"";
                }
            }
            
            //get transision list
            Map<String, String> transitionsList = workflowManager.getNonExceptionalOutgoingTransitions(processId, actId);
            if (transitionsList != null && !transitionsList.isEmpty()) {
                for (String t : transitionsList.keySet()) {
                    if (!transitions.isEmpty()) {
                        transitions += ",";
                    }
                    transitions += "{\"value\":\"" + t + "\", \"label\":\"" + StringUtil.escapeString(transitionsList.get(t), StringUtil.TYPE_JSON, null) + "\"}";
                }
            }
        }
        
        return AppUtil.readPluginResource(RulesDecisionPlugin.class.getName(), "/properties/app/rulesEditor.js", new String[]{variables, transitions}, false, null);
    }
    
    /**
     * Utility method of RulesDecisionPlugin to replace variable in a value
     * @param value
     * @param variables
     * @return 
     */
    public static String getVariable(String value, Map<String, String> variables) {
        if (value != null && value.contains("${") && value.contains("}")) {
            Pattern pattern = Pattern.compile("\\$\\{[^\\}]+\\}");
            Matcher matcher = pattern.matcher(value);
            while (matcher.find()) {
                String found = matcher.group();
                String variableKey = found.substring(2, found.length() -1);
                if (variables.containsKey(variableKey)) {
                    value = value.replaceAll(StringUtil.escapeRegex(found), StringUtil.escapeRegex((String)variables.get(variableKey)));
                }
            }
        } else if (value != null && variables.containsKey(value.trim())) {
            value = variables.get(value.trim());
        }
        return value;
    }
    
    public static Object executeScript(String script, Map properties) {
        Object result = null;
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.setClassLoader(AppPluginUtil.class.getClassLoader());
            for (Object key : properties.keySet()) {
                interpreter.set(key.toString(), properties.get(key));
            }
            LogUtil.debug(AppPluginUtil.class.getName(), "Executing script " + script);
            result = interpreter.eval(script);
            return result;
        } catch (Exception e) {
            LogUtil.error(AppPluginUtil.class.getName(), e, "Error executing script");
            return null;
        }
    }
}
