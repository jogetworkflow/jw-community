package org.joget.apps.app.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.CustomBuilder;
import org.joget.apps.userview.model.Permission;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.User;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.json.JSONObject;

public class CustomBuilderUtil {
    
    public static Map<String, CustomBuilder> getBuilderList() {
        Map<String, CustomBuilder> list = new HashMap<String, CustomBuilder>();
        
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        Collection<Plugin> builders = pluginManager.list(CustomBuilder.class);
        
        if (builders != null && !builders.isEmpty()) {
            for (Plugin plugin : builders) {
                CustomBuilder cb = (CustomBuilder) plugin;
                list.put(cb.getObjectName(), cb);
            }
        }
        
        return list;
    }
    
    public static boolean hasBuilders() {
        return !getBuilderList().isEmpty();
    }
    
    public static CustomBuilder getBuilder(String name) {
        return getBuilderList().get(name);
    }
    
    public static boolean hasBuilder(String name) {
        return getBuilderList().containsKey(name);
    }
    
    public static String createNewJSON(CustomBuilder builder, String id, String name, String description, BuilderDefinition copyDef) {
        String json = "";
        if (copyDef != null) {
            String copyJson = copyDef.getJson();
            try {
                JSONObject obj = new JSONObject(copyJson);
                if (!obj.isNull("properties")) {
                    JSONObject objProperty = obj.getJSONObject("properties");
                    objProperty.put("id", id);
                    objProperty.put("name", name);
                    objProperty.put("description", description);
                }
                json = obj.toString();
            } catch (Exception e) {
            }
        } else {
            json = AppUtil.readPluginResource(builder.getClassName(), "/defaultDefinition.json", new String[]{id, name, description}, true, null);
        }
        return json;
    }
    
    public static String getNameFromJSON(String json) {
        if (json != null) {
            try {
                JSONObject obj = new JSONObject(json);
                if (!obj.isNull("properties")) {
                    JSONObject objProperty = obj.getJSONObject("properties");
                    if (objProperty.has("name")) {
                        return objProperty.getString("name");
                    }
                }
            } catch (Exception e) {
            }
        }
        return "";
    }
    
    public static String getDescriptionFromJSON(String json) {
        if (json != null) {
            try {
                JSONObject obj = new JSONObject(json);
                if (!obj.isNull("properties")) {
                    JSONObject objProperty = obj.getJSONObject("properties");
                    if (objProperty.has("description")) {
                        return objProperty.getString("description");
                    }
                }
            } catch (Exception e) {
            }
        }
        return "";
    }
    
    /**
     * Generates HTML output using a FreeMarker template.
     * @param builder
     * @param templatePath
     * @param dataModel
     * @param request
     * @return
     */
    public static String generateHtml(final CustomBuilder builder, final String templatePath, Map dataModel, HttpServletRequest request) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        String content = pluginManager.getPluginFreeMarkerTemplate(dataModel, builder.getClassName(), templatePath, builder.getResourceBundlePath());
        
        try {
            if (request != null) {
                request.setAttribute(builder.getClassName(), true);
            }
        } catch (Exception e) {
            // ignore if servlet request is not available
        }
        
        return content;
    }
    
    /**
     * Check permission result
     * @param permissionObjJson
     * @param requestParam
     * @param currentUser
     * @return
     */
    public static boolean getPermisionResult(String permissionObjJson, Map requestParam, User currentUser) {
        Boolean isAuthorize = true;
        try {
            if (permissionObjJson != null) {
                JSONObject permissionObj = new JSONObject(permissionObjJson);
                if (permissionObj.has("className")) {
                    PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
                    String permissionClassName = permissionObj.getString("className");
                    Permission permission = null;
                    if (permissionClassName != null && !permissionClassName.isEmpty()) {
                        permission = (Permission) pluginManager.getPlugin(permissionClassName);
                    }
                    if (permission != null) {
                        if (permissionObj.has("properties")) {
                            permission.setProperties(PropertyUtil.getProperties(permissionObj.getJSONObject("properties")));
                        }
                        permission.setRequestParameters(requestParam);
                        permission.setCurrentUser(currentUser);

                        isAuthorize = permission.isAuthorize();
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(CustomBuilderUtil.class.getName(), e, permissionObjJson);
        }
        return isAuthorize;
    }
}
