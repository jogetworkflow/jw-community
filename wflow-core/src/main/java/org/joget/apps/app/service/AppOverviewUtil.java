package org.joget.apps.app.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.model.AbstractAppVersionedObject;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AppOverviewData;
import org.joget.apps.app.model.AppOverviewTool;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.CustomBuilder;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.userview.service.UserviewService;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.ClassUtils;

public class AppOverviewUtil {
    protected static final String APP_OVERVIEW_DEFINITION = "APP_OVERVIEW_DEFINITION";
    
    /**
     * Update the overview scan result and return it
     * @param appDef
     * @return 
     */
    public static String getOverview(AppDefinition appDef) {
        Map<String, AppOverviewTool> tools = getTools();
        
        if (appDef != null && tools != null && !tools.isEmpty()) {
            String lang = AppUtil.getAppLocale();
            if ("en_US".equals(lang)) {
                lang = "";
            } else {
                lang = "_" + lang;
            }
            
            BuilderDefinitionDao builderDefinitionDao = (BuilderDefinitionDao) AppUtil.getApplicationContext().getBean("builderDefinitionDao");
            BuilderDefinition def = builderDefinitionDao.loadById(APP_OVERVIEW_DEFINITION + lang, appDef);
            
            Gson gson = new Gson();
            AppOverviewData data;
            String toolsList = StringUtils.join(tools.keySet().toArray(new String[0]));
            
            if (def != null) {
                //return the previous scan result if there is no last modified date is same or the tool list is same
                if (def.getDateModified().equals(appDef.getDateModified()) && Integer.toString(toolsList.hashCode()).equals(def.getName())) {
                    return def.getJson();
                }
                
                //parse json to data
                data = gson.fromJson(def.getJson(), new TypeToken<AppOverviewData>(){}.getType());
            } else {
                data = new AppOverviewData();
            }
            
            //run scanning
            scanApp(appDef, data, tools.values());
            
            //add or update the scan result
            String json = gson.toJson(data);
            if (def == null) {
                def = new BuilderDefinition();
                def.setId(APP_OVERVIEW_DEFINITION + lang);
                def.setName(Integer.toString(toolsList.hashCode()));
                def.setAppDefinition(appDef);
                def.setJson(json);
                
                builderDefinitionDao.add(def);
            } else {
                def.setName(Integer.toString(toolsList.hashCode()));
                def.setJson(json);
                builderDefinitionDao.update(def);
            }
            return json;
        }
        
        return "";
    }
    
    /**
     * Scan the app components
     * 
     * @param appDef
     * @param data
     * @param tools 
     */
    protected static void scanApp(AppDefinition appDef, AppOverviewData data, Collection<AppOverviewTool> tools) {
        if (appDef != null) {
            Set<String> checkedKeys = new HashSet<String>();
            
            //scan forms
            if (appDef.getFormDefinitionList() != null) {
                for (FormDefinition form : appDef.getFormDefinitionList()) {
                    scanBuilderItem("form", form, data, tools, checkedKeys);
                }
            }
            
            //scan Lists
            if (appDef.getDatalistDefinitionList() != null) {
                for (DatalistDefinition list : appDef.getDatalistDefinitionList()) {
                    scanBuilderItem("datalist", list, data, tools, checkedKeys);
                }
            }
            
            //scan UIs
            if (appDef.getUserviewDefinitionList() != null) {
                UserviewService userviewService = (UserviewService) AppUtil.getApplicationContext().getBean("userviewService");
                for (UserviewDefinition ui : appDef.getUserviewDefinitionList()) {
                    scanBuilderItem("userview", userviewService.combinedUserviewDefinition(ui), data, tools, checkedKeys);
                }
            }
            
            //scan custom builder items
            Map<String, CustomBuilder> customBuilders = CustomBuilderUtil.getBuilderList();
            if (appDef.getBuilderDefinitionList() != null) {
                for (BuilderDefinition def : appDef.getBuilderDefinitionList()) {
                    if (customBuilders.containsKey(def.getType())) {
                        scanBuilderItem(def.getType(), def, data, tools, checkedKeys);
                    }
                }
            }
            
            //scan Process & Mapping
            if (appDef.getPackageDefinition() != null) {
                scanProcess(appDef, appDef.getPackageDefinition(), data, tools, checkedKeys);
            }
            
            //remove deleted items
            data.checkAndRemoveDeletedItems(checkedKeys);
        }
    }
    
    /**
     * Check an app component item is require to update or not and scan it if needed to update
     * @param type
     * @param obj
     * @param data
     * @param tools
     * @param checkedKeys 
     */
    protected static void scanBuilderItem(String type, AbstractAppVersionedObject obj, AppOverviewData data, Collection<AppOverviewTool> tools, Set<String> checkedKeys) {
        String key = type + ":" + obj.getId();
        if (data.isItemRequireUpdate(key, obj.getDateModified())) {
            String json = obj.getJson();
            try {
                JSONObject jsonobj = new JSONObject(json);
                scanJsonObject(key, null, jsonobj, "", data, tools);
            } catch (Exception e) {
                LogUtil.error(AppOverviewUtil.class.getName(), e, key);
            }
        }
        
        checkedKeys.add(key);
    }
    
    protected static void scanProcess(AppDefinition appDef, PackageDefinition packageDefinition, AppOverviewData data, Collection<AppOverviewTool> tools, Set<String> checkedKeys) {
        JSONObject xpdlAndMapping = AppUtil.getXpdlAndMappingJsonObj(appDef);
        if (xpdlAndMapping != null && xpdlAndMapping.has("xpdl")) {
            JSONObject xpdl = xpdlAndMapping.getJSONObject("xpdl");
            JSONObject packageObj = (xpdl.has("Package"))?xpdl.getJSONObject("Package"):(new JSONObject());
            JSONObject workflowProcessesObj = (packageObj.has("WorkflowProcesses"))?packageObj.getJSONObject("WorkflowProcesses"):(new JSONObject());
            
            //loop processes
            if (workflowProcessesObj.has("WorkflowProcess")) {
                
                //the WorkflowProcess will be JSONObject when there is only 1 process, else it will be JSONArray
                Object workflowProcessObj = workflowProcessesObj.get("WorkflowProcess");
                JSONArray workflowProcessesArr;
                if (workflowProcessObj instanceof JSONArray) {
                    workflowProcessesArr = (JSONArray) workflowProcessObj;
                } else {
                    workflowProcessesArr = new JSONArray();
                    workflowProcessesArr.put(workflowProcessObj);
                }
                
                for (int i = 0; i < workflowProcessesArr.length(); i++) {
                    JSONObject process = workflowProcessesArr.getJSONObject(i);
                    
                    String key = "process:" + process.getString("-Id");
                    
                    if (data.isItemRequireUpdate(key, packageDefinition.getDateModified())) {
                        try {
                            scanJsonObject(key, null, process, "xpdl.Package.WorkflowProcesses.WorkflowProcess["+i+"]", data, tools);
                        } catch (Exception e) {
                            LogUtil.error(AppOverviewUtil.class.getName(), e, key);
                        }
                    }

                    checkedKeys.add(key);
                }
            }
            
            //scan form mapping
            JSONObject formMapping = (xpdlAndMapping.has("activityForms"))?xpdlAndMapping.getJSONObject("activityForms"):(new JSONObject());
            Iterator keys = formMapping.keys();
            while (keys.hasNext()) {
                String pkey = (String) keys.next();
                if (!formMapping.isNull(pkey)) {
                    String[] temp = pkey.split("::");
                    Object value = formMapping.get(pkey);
                    if (value instanceof JSONObject) {
                        scanJsonObject("process:" + temp[0], null, (JSONObject) value, "activityForms." + pkey, data, tools);
                    }
                }
            }
            
            //scan tool mapping
            JSONObject toolMapping = (xpdlAndMapping.has("activityPlugins"))?xpdlAndMapping.getJSONObject("activityPlugins"):(new JSONObject());
            keys = toolMapping.keys();
            while (keys.hasNext()) {
                String pkey = (String) keys.next();
                if (!toolMapping.isNull(pkey)) {
                    String[] temp = pkey.split("::");
                    Object value = toolMapping.get(pkey);
                    if (value instanceof JSONObject) {
                        scanJsonObject("process:" + temp[0], null, (JSONObject) value, "activityPlugins." + pkey, data, tools);
                    }
                }
            }
            
            //scan participant mapping
            JSONObject participantMapping = (xpdlAndMapping.has("participants"))?xpdlAndMapping.getJSONObject("participants"):(new JSONObject());
            keys = participantMapping.keys();
            while (keys.hasNext()) {
                String pkey = (String) keys.next();
                if (!participantMapping.isNull(pkey)) {
                    String[] temp = pkey.split("::");
                    Object value = participantMapping.get(pkey);
                    if (value instanceof JSONObject) {
                        JSONObject participantMappingObj = (JSONObject) value;
                        
                        String type = (participantMappingObj.has("type"))?participantMappingObj.getString("type"):"";
                        
                        //check if plugin
                        if ("plugin".equals(type) && participantMappingObj.has("value") && participantMappingObj.has("properties")) {
                            scan("process:" + temp[0], "participants." + pkey, participantMappingObj.getString("value"), participantMappingObj.optJSONObject("properties"), null, data, tools);
                        } else {
                            scanJsonObject("process:" + temp[0], null, participantMappingObj, "participants." + pkey, data, tools);
                        }
                    }
                }
            }
        }
    }
    
    protected static void scanJsonObject(String key, JSONObject parent, JSONObject obj, String path, AppOverviewData data, Collection<AppOverviewTool> tools) {
        if (obj != null) {
            String pathPrefix = path + (!path.isEmpty()?".":"");
            
            JSONObject nonPluginAttrs = null;
            
            //check is plugin or not, if it is plugin, scan it
            if (obj.has("className") && obj.has("properties")) {
                Object properties = obj.get("properties");
                if (properties instanceof String) {
                    properties = new JSONObject(properties.toString());
                }
                
                scan(key, path, obj.getString("className"), (JSONObject) properties, obj, data, tools);
            } else if (parent == null || !(parent.has("className") && path.endsWith(".properties"))) {
                nonPluginAttrs = new JSONObject();
            }
            
            //loop through object keys to find inner plugin
            Iterator keys = obj.keys();
            while (keys.hasNext()) {
                String pkey = (String) keys.next();
                if (!obj.isNull(pkey)) {
                    Object value = obj.get(pkey);
                    if (value instanceof JSONArray) {
                        scanJsonArray(key, obj, (JSONArray) value, pathPrefix + pkey, data, tools, pkey, nonPluginAttrs);
                    } else if (value instanceof JSONObject) {
                        scanJsonObject(key, obj, (JSONObject) value, pathPrefix + pkey, data, tools);
                    } else if (nonPluginAttrs != null) { //if non plugin attr
                        nonPluginAttrs.put(pkey, value);
                    }
                }
            }
            
            //scan non plugin attribute too
            if (nonPluginAttrs != null) {
                scan(key, path, null, nonPluginAttrs, obj, data, tools);
            }
        }
    }
    
    protected static void scanJsonArray(String key, JSONObject parent, JSONArray arr, String path, AppOverviewData data, Collection<AppOverviewTool> tools, String propKey, JSONObject nonPluginAttrs) {
        if (arr != null && arr.length() > 0) {
            Collection<String> temp = new ArrayList<String>();
            
            for (int i = 0; i < arr.length(); i++) {
                Object value = arr.get(i);
                if (value != null) {
                    if (value instanceof JSONArray) {
                        scanJsonArray(key, parent, (JSONArray) value, path + "[" + i + "]", data, tools, null, null);
                    } else if (value instanceof JSONObject) {
                        scanJsonObject(key, parent, (JSONObject) value, path + "[" + i + "]", data, tools);
                    } else {
                        temp.add(value.toString());
                    }
                }
            }
            
            //in case it is just an array of string
            if (nonPluginAttrs != null && !temp.isEmpty()) {
                nonPluginAttrs.put(propKey, StringUtils.join(temp, ";"));
            }
        }
    }
    
    /**
     * Scan a JSON object or a plugin properties
     * 
     * @param key
     * @param selector
     * @param pluginClassName
     * @param properties
     * @param parent
     * @param data
     * @param tools 
     */
    protected static void scan(String key, String selector, String pluginClassName, JSONObject properties, JSONObject parent, AppOverviewData data, Collection<AppOverviewTool> tools) {
        //pass in the property in string too to prevent keep convert to string multiple time when needed in overview tool
        String propertiesString = (properties != null)?properties.toString():"";
        
        //prepare a map of properties field type and properies field ids of a plugin
        if (pluginClassName != null && !pluginClassName.isEmpty()) {
            Map<String, Set<String>> fields = (Map<String, Set<String>>) data.getTempData(pluginClassName);
            if (fields == null) {
                data.putTempData(pluginClassName, getPluginPropertyFieldsAndType(pluginClassName));
            }
        }
        
        for (AppOverviewTool tool : tools) {
            tool.scan(key, selector, pluginClassName, properties, propertiesString, parent, data);
        }
    }
    
    /**
     * Return a map of properties field type and field ids of a plugin
     * 
     * @param pluginClassName
     * @return 
     */
    protected static Map<String, Set<String>> getPluginPropertyFieldsAndType(String pluginClassName) {
        Map<String, Set<String>> fields = new HashMap<String, Set<String>>();
        
        try {
            PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
            Plugin plugin = pluginManager.getPlugin(pluginClassName);

            if (plugin != null && plugin instanceof PropertyEditable) {
                String propertiesOptions = ((PropertyEditable)plugin).getPropertyOptions();
                if (propertiesOptions != null && !propertiesOptions.isEmpty()) {
                    JSONArray pages = new JSONArray(propertiesOptions);
                    
                    //loop page
                    for (int i = 0; i < pages.length(); i++) {
                        JSONObject page = (JSONObject) pages.get(i);

                        if (page.has("properties")) {
                            
                            //loop properties
                            JSONArray properties = (JSONArray) page.get("properties");
                            for (int j = 0; j < properties.length(); j++) {
                                
                                //get the name and type
                                JSONObject property = (JSONObject) properties.get(j);
                                if (property.has("type") && property.has("name")) {
                                    String name = property.getString("name");
                                    String type = property.getString("type");
                                    
                                    //if there is mode
                                    if (property.has("mode")) {
                                        type += "_" + property.getString("mode");
                                    }
                                    
                                    //add to type list
                                    Set<String> typeFields = fields.get(type);
                                    if (typeFields == null) {
                                        typeFields = new HashSet<String>();
                                        fields.put(type, typeFields);
                                    }
                                    typeFields.add(name);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.debug(AppOverviewUtil.class.getName(), "Not able to retrieve properties fields of " + pluginClassName);
        }
        
        return fields;
    }
    
    /**
     * Retrieve a list of app overview tools
     * @return 
     */
    public static Map<String, AppOverviewTool> getTools() {
        Map<String, AppOverviewTool> tools = new HashMap<String, AppOverviewTool>();
        
        if (AppUtil.isEnterprise()) { //only support enterprise version
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

            Collection<Plugin> list = pluginManager.list(AppOverviewTool.class);

            for (Plugin p : list) {
                String name = ClassUtils.getUserClass(p).getName();
                tools.put(name, (AppOverviewTool) pluginManager.getPlugin(name));
            }
        }
        
        return tools;
    }
    
    /**
     * Find all hash variables with the prefix used in a property value
     * 
     * @param valueStr
     * @param hashVariablePrefix
     * @param key
     * @param path
     * @param plugin
     * @param data 
     */
    public static void findHashVariables(String valueStr, String hashVariablePrefix, String key, String path, AppOverviewTool plugin, AppOverviewData data) {
        if (valueStr.contains(hashVariablePrefix)) {
            //find all hash variables with the prefix
            int index = valueStr.indexOf(hashVariablePrefix);
            while (index > 0) {
                char startChar = valueStr.charAt(index-1);
                String hashVariable = "";

                if (startChar == '{') {
                    //if it is nested hash variable, find closing
                    hashVariable = valueStr.substring(index-1, valueStr.indexOf("}", index + 10) + 1);
                    int count = StringUtils.countMatches(hashVariable, "{");
                    if (count > 1) {
                        int nextCharIndex = index + hashVariable.length();
                        while (count > 1 && nextCharIndex < valueStr.length()) {
                            char nextChar = valueStr.charAt(nextCharIndex);
                            if (nextChar == '}') {
                                count--;
                            } else if (nextChar == '{') {
                                count++;
                            }
                            hashVariable += nextChar;
                            nextCharIndex++;
                        }
                    }
                } else {
                    hashVariable = valueStr.substring(index-1, valueStr.indexOf("#", index + 10) + 1);
                }

                if (!hashVariable.isEmpty()) {
                    data.addItemData(key, plugin, path, "", hashVariable);

                    //find next hash variable
                    index = valueStr.indexOf(hashVariablePrefix, index + hashVariable.length() - 1);
                } else {
                    break;
                }
            }
        }
    }
    
    /**
     * Retrieve all OSGI plugins list for scanning usage
     * 
     * @param data
     * @return 
     */
    public static Map<String, String> getOsgiPlugins(AppOverviewData data) {
        Map<String, String> osgiPlugins = (Map<String, String>) data.getTempData("osgiPluginsList");
            
        if (osgiPlugins == null) {
            osgiPlugins = new HashMap<String, String>();

            // get osgi plugins
            PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
            Collection<Plugin> pluginList = pluginManager.listOsgiPlugin(null);

            for (Plugin plugin: pluginList) {
                osgiPlugins.put(ClassUtils.getUserClass(plugin).getName(), plugin.getI18nLabel());
            }
            data.putTempData("osgiPluginsList", osgiPlugins); //add it for other plugin to use
        }
        
        return osgiPlugins;
    }
    
    /**
     * Retrieve all plugins list for scanning usage
     * 
     * @param data
     * @return 
     */
    public static Map<String, String> getAllPlugins(AppOverviewData data) {
        Map<String, String> allPlugins = (Map<String, String>) data.getTempData("allPluginsList");

        if (allPlugins == null) {
            allPlugins = new HashMap<String, String>();

            // get all plugins
            PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
            Collection<Plugin> pluginList = pluginManager.list(null);

            for (Plugin plugin: pluginList) {
                allPlugins.put(ClassUtils.getUserClass(plugin).getName(), plugin.getI18nLabel());
            }

            //add joget default classes
            allPlugins.put("org.joget.apps.userview.model.Userview", "");
            allPlugins.put("org.joget.apps.userview.model.UserviewCategory", "");
            allPlugins.put("org.joget.apps.userview.model.UserviewSetting", "");
            allPlugins.put("org.joget.apps.userview.model.UserviewPage", "");
            allPlugins.put("org.joget.apps.userview.model.UserviewLayout", "");
            allPlugins.put("org.joget.apps.userview.model.UserviewPermission", "");

            data.putTempData("allPluginsList", allPlugins); //add it for other plugin to use
        }
            
        return allPlugins;   
    }
}
