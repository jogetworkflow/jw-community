package org.joget.apps.userview.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewCategory;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.model.UserviewPermission;
import org.joget.apps.userview.model.UserviewSetting;
import org.joget.apps.userview.model.UserviewTheme;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("userviewService")
public class UserviewService {

    @Autowired
    private PluginManager pluginManager;
    @Autowired
    private WorkflowUserManager workflowUserManager;
    @Autowired
    @Qualifier("main")
    ExtDirectoryManager directoryManager;

    /**
     * Create userview fron json
     * @return
     */
    public Userview createUserview(String json, String menuId, boolean preview, String contextPath, Map requestParameters, String key, Boolean embed) {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        return createUserview(appDef, json, menuId, preview, contextPath, requestParameters, key, embed);
    }

    /**
     * Create userview fron json
     * @return
     */
    public Userview createUserview(AppDefinition appDef, String json, String menuId, boolean preview, String contextPath, Map requestParameters, String key, Boolean embed) {
        if (key != null && key.trim().length() == 0) {
            key = null;
        }

        //process json with hash variable
        json = AppUtil.processHashVariable(json, null, null, null);

        User currentUser = directoryManager.getUserByUsername(workflowUserManager.getCurrentUsername());

        if (requestParameters == null) {
            requestParameters = new HashMap<String, Object>();
        }
        requestParameters = convertRequestParamMap(requestParameters);
        requestParameters.put("contextPath", contextPath);
        requestParameters.put("isPreview", Boolean.toString(preview));

        String appId = appDef.getId();
        String appVersion = appDef.getVersion().toString();
        Userview userview = new Userview();

        try {
            //set userview properties
            JSONObject userviewObj = new JSONObject(json);
            userview.setProperties(PropertyUtil.getPropertiesValueFromJson(userviewObj.getJSONObject("properties").toString()));

            //set Setting
            JSONObject settingObj = userviewObj.getJSONObject("setting");
            UserviewSetting setting = new UserviewSetting();
            setting.setProperties(PropertyUtil.getPropertiesValueFromJson(settingObj.getJSONObject("properties").toString()));

            //set theme & permission
            try {
                JSONObject themeObj = settingObj.getJSONObject("properties").getJSONObject("theme");
                UserviewTheme theme = (UserviewTheme) pluginManager.getPlugin(themeObj.getString("className"));
                theme.setProperties(PropertyUtil.getPropertiesValueFromJson(themeObj.getJSONObject("properties").toString()));
                theme.setRequestParameters(requestParameters);
                setting.setTheme(theme);
            } catch (Exception e) {
                LogUtil.debug(getClass().getName(), "set theme error.");
            }
            try {
                JSONObject permissionObj = settingObj.getJSONObject("properties").getJSONObject("permission");
                UserviewPermission permission = null;
                String permissionClassName = permissionObj.getString("className");
                if (permissionClassName != null && !permissionClassName.isEmpty()) {
                    permission = (UserviewPermission) pluginManager.getPlugin(permissionClassName);
                }
                if (permission != null) {
                    permission.setProperties(PropertyUtil.getPropertiesValueFromJson(permissionObj.getJSONObject("properties").toString()));
                    permission.setRequestParameters(requestParameters);
                    permission.setCurrentUser(currentUser);
                    setting.setPermission(permission);
                }
            } catch (Exception e) {
                LogUtil.debug(getClass().getName(), "set permission error.");
            }
            userview.setSetting(setting);

            //set categories
            JSONArray categoriesArray = userviewObj.getJSONArray("categories");
            Collection<UserviewCategory> categories = new ArrayList<UserviewCategory>();
            for (int i = 0; i < categoriesArray.length(); i++) {
                JSONObject categoryObj = (JSONObject) categoriesArray.get(i);

                UserviewCategory category = new UserviewCategory();
                category.setProperties(PropertyUtil.getPropertiesValueFromJson(categoryObj.getJSONObject("properties").toString()));

                boolean hasPermis = false;
                if (preview) {
                    hasPermis = true;
                } else {
                    //check for permission
                    JSONObject permissionObj = null;
                    UserviewPermission permission = null;

                    try {
                        permissionObj = categoryObj.getJSONObject("properties").getJSONObject("permission");
                        String permissionClassName = permissionObj.getString("className");
                        if (permissionClassName != null && !permissionClassName.isEmpty()) {
                            permission = (UserviewPermission) pluginManager.getPlugin(permissionClassName);
                        }
                    } catch (Exception e) {
                        LogUtil.debug(getClass().getName(), "set category permission error.");
                    }

                    if (permission != null) {
                        permission.setProperties(PropertyUtil.getPropertiesValueFromJson(permissionObj.getJSONObject("properties").toString()));
                        permission.setRequestParameters(requestParameters);
                        permission.setCurrentUser(currentUser);

                        hasPermis = permission.isAuthorize();
                    } else {
                        hasPermis = true;
                    }
                }

                if (hasPermis) {
                    //set menus
                    JSONArray menusArray = categoryObj.getJSONArray("menus");
                    Collection<UserviewMenu> menus = new ArrayList<UserviewMenu>();
                    for (int j = 0; j < menusArray.length(); j++) {
                        try {
                            //set menu
                            JSONObject menuObj = (JSONObject) menusArray.get(j);
                            UserviewMenu menu = (UserviewMenu) pluginManager.getPlugin(menuObj.getString("className"));
                            menu.setProperties(PropertyUtil.getPropertiesValueFromJson(menuObj.getJSONObject("properties").toString()));
                            menu.setRequestParameters(requestParameters);
                            menu.setUserview(userview);
                            String mId = getMenuId(menu);

                            if (preview) {
                                menu.setUrl(contextPath + "/web/console/app/" + appId + "/" + appVersion + "/userview/builderPreview/" + userview.getPropertyString("id") + "/" + mId);
                            } else {
                                menu.setKey(key);
                                String prefix = "/web/userview/";
                                
                                if (embed) {
                                    prefix = "/web/embed/userview/";
                                }
                                
                                menu.setUrl(contextPath + prefix + appId + "/" + userview.getPropertyString("id") + "/" + ((key != null) ? key : "") + "/" + mId);
                            }

                            //set Current, if current menu id is empty, search the 1st valid menu
                            if ((("".equals(menuId) || menuId == null) && userview.getCurrent() == null && menu.isHomePageSupported())
                                    || (menuId != null && menuId.equals(mId))) {
                                userview.setCurrent(menu);
                                userview.setCurrentCategory(category);
                            }

                            menus.add(menu);
                        } catch (Exception e) {
                            LogUtil.debug(getClass().getName(), "Userview Menu class file not found");
                        }
                    }

                    if (menus.size() > 0) {
                        category.setMenus(menus);
                        categories.add(category);
                    }
                }
            }
            userview.setCategories(categories);
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "Create Userview Error!!");
        }
        return userview;
    }

    public String getMenuId(UserviewMenu menu) {
        String menuId = menu.getPropertyString("id");
        if (menu.getPropertyString("customId") != null && menu.getPropertyString("customId").trim().length() > 0) {
            menuId = menu.getPropertyString("customId");
        }
        return menuId;
    }

    public String getUserviewName(String json) {
        try {
            JSONObject userviewObj = new JSONObject(json);
            return PropertyUtil.getPropertiesValueFromJson(userviewObj.getJSONObject("properties").toString()).get("name").toString();
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "Get Userview Name Error!!");
        }
        return "";
    }

    public String getUserviewDescription(String json) {
        try {
            JSONObject userviewObj = new JSONObject(json);
            return PropertyUtil.getPropertiesValueFromJson(userviewObj.getJSONObject("properties").toString()).get("description").toString();
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "Get Userview Description Error!!");
        }
        return "";
    }

    private Map convertRequestParamMap(Map params) {
        Map result = new HashMap();
        for (String key : (Set<String>) params.keySet()) {
            String[] paramValue = (String[]) params.get(key);
            if (paramValue.length == 1) {
                result.put(key, paramValue[0]);
            } else {
                result.put(key, paramValue);
            }
        }
        return result;
    }
}
