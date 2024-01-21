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
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.userview.service.UserviewService;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
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
        
        if (appDef != null) {
            BuilderDefinitionDao builderDefinitionDao = (BuilderDefinitionDao) AppUtil.getApplicationContext().getBean("builderDefinitionDao");
            BuilderDefinition def = builderDefinitionDao.loadById(APP_OVERVIEW_DEFINITION, appDef);
            
            Gson gson = new Gson();
            AppOverviewData data;
            String toolsList = StringUtils.join(tools.keySet().toArray(new String[0]));
            
            if (def != null) {
                //return the previous scan result if there is no last modified date is same or the tool list is same
                if (def.getDateModified().equals(appDef.getDateModified()) && toolsList.equals(def.getName())) {
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
                def.setId(APP_OVERVIEW_DEFINITION);
                def.setName(toolsList);
                def.setAppDefinition(appDef);
                def.setJson(json);
                
                builderDefinitionDao.add(def);
            } else {
                def.setName(toolsList);
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
    public static void scanApp(AppDefinition appDef, AppOverviewData data, Collection<AppOverviewTool> tools) {
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
    public static void scanBuilderItem(String type, AbstractAppVersionedObject obj, AppOverviewData data, Collection<AppOverviewTool> tools, Set<String> checkedKeys) {
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
    
    
    public static void scanJsonObject(String key, JSONObject parent, JSONObject obj, String path, AppOverviewData data, Collection<AppOverviewTool> tools) {
        if (obj != null) {
            String pathPrefix = path + (!path.isEmpty()?".":"");
            
            JSONObject nonPluginAttrs = null;
            
            //check is plugin or not, if it is plugin, scan it
            if (obj.has("className") && obj.has("properties") && !(obj.get("properties") instanceof String)) {
                scan(key, path, obj.getString("className"), obj.getJSONObject("properties"), obj, data, tools);
            } else if (parent == null || (parent != null && !parent.has("className") && !parent.has("properties"))) {
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
    
    public static void scanJsonArray(String key, JSONObject parent, JSONArray arr, String path, AppOverviewData data, Collection<AppOverviewTool> tools, String propKey, JSONObject nonPluginAttrs) {
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
    
    public static void scan(String key, String selector, String pluginClassName, JSONObject properties, JSONObject parent, AppOverviewData data, Collection<AppOverviewTool> tools) {
        for (AppOverviewTool tool : tools) {
            tool.scan(key, selector, pluginClassName, properties, parent, data);
        }
    }
    
    public static void scanProcess(AppOverviewData data, Collection<AppOverviewTool> tools) {
        
    }
    
    /**
     * Retrieve a list of app overview tools
     * @return 
     */
    public static Map<String, AppOverviewTool> getTools() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
                
        Collection<Plugin> list = pluginManager.list(AppOverviewTool.class);
        Map<String, AppOverviewTool> tools = new HashMap<String, AppOverviewTool>();
        
        for (Plugin p : list) {
            String name = ClassUtils.getUserClass(p).getName();
            tools.put(name, (AppOverviewTool) pluginManager.getPlugin(name));
        }
        
        return tools;
    }
}
