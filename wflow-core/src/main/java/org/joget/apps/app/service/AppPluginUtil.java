package org.joget.apps.app.service;

import bsh.EvalError;
import bsh.Interpreter;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.map.LRUMap;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.lib.RulesDecisionPlugin;
import org.joget.apps.app.model.AppDefinition;
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
            //use dao to retrieve as there is cache implementation for it
            PluginDefaultPropertiesDao dao = (PluginDefaultPropertiesDao) appContext.getBean("pluginDefaultPropertiesDao");
            return dao.loadById(id, appDef);
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
        AppUtil.setCurrentAssignment(assignment, true); // set the assignment for HashVariableSupportedMapImpl
        
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
                    defaultPropertyMap = PropertyUtil.getPropertiesValueFromJson(json);
                }

                Map tempPropertyMap = new HashMap(propertyMap);
                for (Object s : defaultPropertyMap.keySet()) {
                    String key = (String) s;
                    String defaultValue = defaultPropertyMap.get(key).toString().trim();
                    String value = "";

                    if (propertyMap.get(key) != null) {
                        value = SecurityUtil.decrypt(propertyMap.get(key).toString().trim());
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
        
        propertyMap = PropertyUtil.getHashVariableSupportedMap(propertyMap);

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
        return AppUtil.readPluginResource(RulesDecisionPlugin.class.getName(), "/properties/app/rulesEditor.js", null, false, null);
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
        try {
            return executeScript(script, properties, false);
        } catch (Exception e) {
            LogUtil.error(AppPluginUtil.class.getName(), e, "Error executing script");
            return null;
        }
    }
    
    public static Object executeScript(String script, Map properties, boolean throwException) throws RuntimeException {
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
        } catch (EvalError e) {
            LogUtil.error(AppPluginUtil.class.getName(), e, "Error executing script, line number: " + e.getErrorLineNumber());
            if (throwException) {
                throw new RuntimeException("Error executing script");
            }
        } catch (Exception e) {
            LogUtil.error(AppPluginUtil.class.getName(), e, "Error executing script");
            if (throwException) {
                throw new RuntimeException("Error executing script");
            }
        }
        return null;
    }
    
    // least recently used (LRU) cache to hold generated styles
    static Map<String, Map<String, String>> styleCache = Collections.synchronizedMap(new LRUMap<>(1000));

    static String generateStyleCacheKey(Map properties, String prefix) {
        String cacheKey = prefix + "::";
        for (Object keyObj: properties.keySet()) {
            String key = keyObj.toString();
            
            // should only cache style related value, else when no prefix, it parsed all hash variable unnecessary
            if (key.startsWith(prefix+"css-") || key.startsWith(prefix+"attr-") || key.startsWith(prefix+"style-")) { 
                Object val = properties.get(keyObj);
                String strVal = "";
                if (val != null) {
                    if (val.getClass().isArray()) {
                        // expand array
                        for (Object el: (Object[])val) {
                            if (el instanceof Map) {
                                // recurse into map                            
                                strVal = generateStyleCacheKey((Map)el, prefix);                            
                            } else {
                                strVal = Arrays.toString((Object[])el);
                            }
                        }
                    } else if (val instanceof Map) {
                        // recurse into map 
                        strVal = generateStyleCacheKey((Map)val, prefix);
                    } else {
                        strVal = val.toString();
                    }
                }
                cacheKey += key + "="+ strVal + ";";
            }
        }
        String hashedCacheKey = StringUtil.md5Base16Utf8(cacheKey); // hash to minimize memory usage
        return hashedCacheKey;
    }

    public static Map<String, String> generateAttrAndStyles(Map<String, Object> properties, String prefix) {
        
        if (prefix == null) {
            prefix = ""; 
        }
                
        // lookup from LRU cache
        String cacheKey = generateStyleCacheKey(properties, prefix);
        Map<String, String> result = styleCache.get(cacheKey);
        if (result != null) {
            return result;
        }

        result = new HashMap<>();
        
        String desktopStyle = "";
        String tabletStyle = "";
        String mobileStyle = "";
        String hoverDesktopStyle = "";
        String hoverTabletStyle = "";
        String hoverMobileStyle = "";
        String cssClass = "";
        String attr = ""; 
        
        for (String key : properties.keySet()) {
            if ((key.startsWith(prefix+"css-") 
                        || key.startsWith(prefix+"attr-")
                        || key.startsWith(prefix+"style-hover-mobile-")
                        || key.startsWith(prefix+"style-hover-tablet-")
                        || key.startsWith(prefix+"style-hover-")
                        || key.startsWith(prefix+"style-mobile-")
                        || key.startsWith(prefix+"style-tablet-")
                        || key.startsWith(prefix+"style-"))
                     && !properties.get(key).toString().isEmpty()) {
                
                String value = "";
                if (!(properties.get(key) instanceof String)) {
                    //to support userview ajax event
                    try {
                        Gson gson = new Gson();
                        value = gson.toJson(properties.get(key));
                    } catch (Exception e) {
                        LogUtil.error(AppPluginUtil.class.getName(), e, "");
                    }
                    if (value.equals("[]") || value.equals("{}")) {
                        continue;
                    }
                } else {
                    value =  properties.get(key).toString();
                }
                if (key.contains("style") && key.endsWith("-background-image")) {
                    value = "url('"+value+"')";
                }

                if (key.startsWith(prefix+"css-")) {
                    if (!value.equalsIgnoreCase("true")) {
                        cssClass += " " + value;
                    } else {
                        cssClass += " " + key.replace(prefix+"css-", "");
                    }
                } else if (key.startsWith(prefix+"attr-")) {
                    attr += " " + key.replace(prefix+"attr-", "") + "=\"" + StringUtil.escapeString(value, StringUtil.TYPE_HTML, null) + "\"";
                } else if (key.startsWith(prefix+"style-hover-mobile-")) {
                    hoverMobileStyle += generateStyle(value, key, prefix+"style-hover-mobile-");
                } else if (key.startsWith(prefix+"style-hover-tablet-")) {
                    hoverTabletStyle += generateStyle(value, key, prefix+"style-hover-tablet-");
                } else if (key.startsWith(prefix+"style-hover-")) {
                    hoverDesktopStyle += generateStyle(value, key, prefix+"style-hover-");
                } else if (key.startsWith(prefix+"style-mobile-")) {
                    mobileStyle += generateStyle(value, key, prefix+"style-mobile-");
                } else if (key.startsWith(prefix+"style-tablet-")) {
                    tabletStyle += generateStyle(value, key, prefix+"style-tablet-");
                } else if (key.startsWith(prefix+"style-")) {
                    desktopStyle += generateStyle(value, key, prefix+"style-");
                }
            }
        }
        
        result.put("desktopStyle", desktopStyle);
        result.put("tabletStyle", tabletStyle);
        result.put("mobileStyle", mobileStyle);
        result.put("hoverDesktopStyle", hoverDesktopStyle);
        result.put("hoverTabletStyle", hoverTabletStyle);
        result.put("hoverMobileStyle", hoverMobileStyle);
        result.put("cssClass", cssClass);
        result.put("attr", attr);
        
        // save into cache
        styleCache.put(cacheKey, result);
        
        return result;
    }
    
    protected static String generateStyle(String value, String key, String prefix) {
        if (key.equals(prefix + "custom")) {
            String[] values = value.split(";");
            String temp = "";
            for (String v : values) {
                if (!v.isEmpty()) {
                    if (!v.contains("!important")) {
                        v += " !important";
                    }
                    temp += v + ";";
                }
            }
            return temp;
        } else {
            return key.replace(prefix, "") + ":" + value + " !important;";
        }
    }
}
