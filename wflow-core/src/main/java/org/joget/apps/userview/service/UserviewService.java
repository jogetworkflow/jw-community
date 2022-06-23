package org.joget.apps.userview.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.MobileElement;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.MobileUtil;
import org.joget.apps.userview.model.CachedUserviewMenu;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewCategory;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.model.Permission;
import org.joget.apps.userview.model.UserviewSetting;
import org.joget.apps.userview.model.UserviewTheme;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Service methods used to parse userview json definition to create Userview
 * 
 */
@Service("userviewService")
public class UserviewService {

    @Autowired
    private PluginManager pluginManager;
    @Autowired
    private WorkflowUserManager workflowUserManager;
    @Autowired
    private AppService appService;
    @Autowired
    UserviewDefinitionDao userviewDefinitionDao;
    @Autowired
    private SetupManager setupManager;
    @Autowired
    @Qualifier("main")
    ExtDirectoryManager directoryManager;

    /**
     * Get userview setting object
     * @param appDef
     * @param json
     * @return setting
     */
    public UserviewSetting getUserviewSetting(AppDefinition appDef, String json) {
        UserviewSetting setting = null;
        
        //process json with hash variable
        json = AppUtil.processHashVariable(json, null, StringUtil.TYPE_JSON, null, appDef);
        
        User currentUser = workflowUserManager.getCurrentUser();
        
        Map<String, Object> requestParameters = new HashMap<String, Object>();
        requestParameters.put("appId", appDef.getAppId());
        requestParameters.put("appVersion", appDef.getVersion().toString());
        
        Userview userview = new Userview();
        
        try {
            //set userview properties
            JSONObject userviewObj = new JSONObject(json);
            userview.setProperties(PropertyUtil.getPropertiesValueFromJson(userviewObj.getJSONObject("properties").toString()));

            //set Setting
            JSONObject settingObj = userviewObj.getJSONObject("setting");
            setting = new UserviewSetting();
            setting.setProperties(PropertyUtil.getPropertiesValueFromJson(settingObj.getJSONObject("properties").toString()));

            //set theme & permission
            try {
                JSONObject themeObj = settingObj.getJSONObject("properties").getJSONObject("theme");
                UserviewTheme theme = (UserviewTheme) pluginManager.getPlugin(themeObj.getString("className"));
                theme.setProperties(PropertyUtil.getPropertiesValueFromJson(themeObj.getJSONObject("properties").toString()));
                theme.setRequestParameters(requestParameters);
                theme.setUserview(userview);
                setting.setTheme(theme);
            } catch (Exception e) {
                LogUtil.debug(getClass().getName(), "set theme error.");
            }
            try {
                if (!"true".equals(setting.getPropertyString("tempDisablePermissionChecking"))) {
                    JSONObject permissionObj = settingObj.getJSONObject("properties").getJSONObject("permission");
                    Permission permission = null;
                    String permissionClassName = permissionObj.getString("className");
                    if (permissionClassName != null && !permissionClassName.isEmpty()) {
                        permission = (Permission) pluginManager.getPlugin(permissionClassName);
                    }
                    if (permission != null) {
                        permission.setProperties(PropertyUtil.getPropertiesValueFromJson(permissionObj.getJSONObject("properties").toString()));
                        permission.setRequestParameters(requestParameters);
                        permission.setCurrentUser(currentUser);
                        setting.setPermission(permission);
                    }
                }
            } catch (Exception e) {
                LogUtil.debug(getClass().getName(), "set permission error.");
            }
            userview.setSetting(setting);
        } catch (Exception ex) {
            LogUtil.debug(getClass().getName(), "set userview setting error.");
        }

        return setting;
    }
    
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
        String permissionKey = Permission.DEFAULT;
                
        if (key != null && key.trim().length() == 0) {
            key = null;
        }
        if (key != null) {
            key = StringEscapeUtils.escapeHtml(key);
        }

        //process json with hash variable
        json = AppUtil.processHashVariable(json, null, StringUtil.TYPE_JSON, null, appDef);
        json = AppUtil.replaceAppMessages(json, StringUtil.TYPE_JSON);

        User currentUser = workflowUserManager.getCurrentUser();

        if (requestParameters == null) {
            requestParameters = new HashMap<String, Object>();
        }
        requestParameters = convertRequestParamMap(requestParameters);
        requestParameters.put("contextPath", contextPath);
        requestParameters.put("isPreview", Boolean.toString(preview));
        requestParameters.put("embed", Boolean.toString(embed));
        requestParameters.put("appId", appDef.getAppId());
        requestParameters.put("appVersion", appDef.getVersion().toString());

        String appId = appDef.getId();
        String appVersion = appDef.getVersion().toString();
        Userview userview = new Userview();
        userview.setParams(requestParameters);
        
        boolean userviewPermission = false;
        
        //if screenshot, set user to null (anonymous)
        User currentThreadUser = currentUser;
        boolean isScreenCapture = workflowUserManager.isCurrentUserInRole(WorkflowUserManager.ROLE_ADMIN) && "true".equalsIgnoreCase((String) requestParameters.get("_isScreenCapture"));
        if (isScreenCapture) {
            currentUser = null;
            workflowUserManager.setCurrentThreadUser(WorkflowUserManager.ROLE_ANONYMOUS);
        }

        try {
            //set userview properties
            JSONObject userviewObj = new JSONObject(json);
            userview.setProperties(PropertyUtil.getProperties(userviewObj.getJSONObject("properties")));

            //set Setting
            JSONObject settingObj = userviewObj.getJSONObject("setting");
            UserviewSetting setting = new UserviewSetting();
            setting.setProperties(PropertyUtil.getProperties(settingObj.getJSONObject("properties")));

            //set theme & permission
            try {
                JSONObject themeObj = settingObj.getJSONObject("properties").getJSONObject("theme");
                JSONObject themeProperties = themeObj.getJSONObject("properties");
                UserviewTheme theme = (UserviewTheme) pluginManager.getPlugin(themeObj.getString("className"));
                if (theme == null) {
                    String defaultTheme = ResourceBundleUtil.getMessage("generator.userview.theme");
                    theme = (UserviewTheme) pluginManager.getPlugin(defaultTheme);
                    String defaultThemePropertiesKey = "generator.userview.theme." + defaultTheme + ".properties";
                    String defaultThemeProperties = "{" + ResourceBundleUtil.getMessage(defaultThemePropertiesKey) + "}";
                    themeProperties = new JSONObject(defaultThemeProperties);
                }
                theme.setProperties(PropertyUtil.getProperties(themeProperties));
                theme.setRequestParameters(requestParameters);
                theme.setUserview(userview);
                setting.setTheme(theme);
            } catch (Exception e) {
                LogUtil.debug(getClass().getName(), "set theme error.");
            }
            try {
                if (!"true".equals(setting.getPropertyString("tempDisablePermissionChecking"))) {
                    if (settingObj.getJSONObject("properties").has("permission_rules")) {
                        JSONArray permissionRules = settingObj.getJSONObject("properties").getJSONArray("permission_rules");
                        if (permissionRules != null && permissionRules.length() > 0) {
                            for (int i = 0; i < permissionRules.length(); i++) {
                                JSONObject rule = permissionRules.getJSONObject(i);
                                if (rule.has("permission")) {
                                    JSONObject permissionObj = rule.optJSONObject("permission");
                                    userviewPermission = UserviewUtil.getPermisionResult(permissionObj, requestParameters, currentUser);
                                    if (userviewPermission) {
                                        permissionKey = rule.getString("permission_key");
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    if (!userviewPermission) {
                        if (settingObj.getJSONObject("properties").has("permission")) {
                            JSONObject permissionObj = settingObj.getJSONObject("properties").getJSONObject("permission");
                            userviewPermission = UserviewUtil.getPermisionResult(permissionObj, requestParameters, currentUser);
                        } else {
                            userviewPermission = true;
                        }
                    }
                } else {
                    userviewPermission = true;
                }
            } catch (Exception e) {
                LogUtil.debug(getClass().getName(), "set permission error.");
            }
            userview.setSetting(setting);

            //set categories
            Collection<UserviewCategory> categories = new ArrayList<UserviewCategory>();
            
            if (userviewPermission) {
                JSONArray categoriesArray = userviewObj.getJSONArray("categories");
                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject categoryObj = (JSONObject) categoriesArray.get(i);

                    UserviewCategory category = new UserviewCategory();
                    category.setProperties(PropertyUtil.getProperties(categoryObj.getJSONObject("properties")));
                    
                    //check for permission
                    JSONObject ruleObj = null;
                    if (Permission.DEFAULT.equals(permissionKey)) {
                        ruleObj = categoryObj.getJSONObject("properties");
                    } else if (categoryObj.getJSONObject("properties").has("permission_rules")) {
                        JSONObject permissionRules = categoryObj.getJSONObject("properties").getJSONObject("permission_rules");
                        if (permissionRules != null && permissionRules.has(permissionKey)) {
                            ruleObj = permissionRules.getJSONObject(permissionKey);
                        }
                    }
                        
                    boolean hasPermis = false;
                    if (preview || "true".equals(setting.getPropertyString("tempDisablePermissionChecking"))) {
                        hasPermis = true;
                    } else {
                        if (ruleObj != null) {
                            if (ruleObj.has("permissionDeny") && "true".equals(ruleObj.getString("permissionDeny"))) {
                                hasPermis = false;
                            } else if (ruleObj.has("permission")){
                                try {
                                    JSONObject permissionObj = ruleObj.getJSONObject("permission");
                                    hasPermis = UserviewUtil.getPermisionResult(permissionObj, requestParameters, currentUser);
                                } catch (Exception e) {
                                    LogUtil.debug(getClass().getName(), "set category permission error.");
                                }
                            } else {
                                hasPermis = true;
                            }
                            
                            //handle for permission rule to override the default setting
                            if (ruleObj.has("hide") && "yes".equals(ruleObj.getString("hide"))) {
                                category.setProperty("hide", "yes");
                            } else { 
                                category.setProperty("hide", "");
                            }
                        } else { //when no properties found for the category object
                            hasPermis = true;
                            category.setProperty("hide", "");
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

                                // check for mobile support
                                boolean isMobileView = MobileUtil.isMobileView();
                                if (isMobileView && (menu instanceof MobileElement) && !((MobileElement)menu).isMobileSupported()) {
                                    // mobile not supported, skip this menu
                                    continue;
                                }
                                
                                //check for deny
                                JSONObject menuRuleObj = null;
                                if (Permission.DEFAULT.equals(permissionKey)) {
                                    menuRuleObj = menuObj.getJSONObject("properties");
                                } else if (menuObj.getJSONObject("properties").has("permission_rules")) {
                                    JSONObject permissionRules = menuObj.getJSONObject("properties").getJSONObject("permission_rules");
                                    if (permissionRules != null && permissionRules.has(permissionKey)) {
                                        menuRuleObj = permissionRules.getJSONObject(permissionKey);
                                    }
                                }
                                if (menuRuleObj != null && menuRuleObj.has("permissionDeny") && "true".equals(menuRuleObj.getString("permissionDeny"))) {
                                    continue;
                                }

                                menu.setProperties(PropertyUtil.getProperties(menuObj.getJSONObject("properties")));
                                menu.setRequestParameters(requestParameters);
                                menu.setUserview(userview);
                                String mId = getMenuId(menu);
                                menu.setProperty("menuId", mId);

                                if (preview) {
                                    menu.setUrl(contextPath + "/web/console/app/" + appId + "/" + appVersion + "/userview/builderPreview/" + userview.getPropertyString("id") + "/" + mId);
                                } else {
                                    menu.setKey(key);
                                    String prefix = "/web/userview/";

                                    if (embed) {
                                        prefix = "/web/embed/userview/";
                                    }

                                    menu.setUrl(contextPath + prefix + appId + "/" + userview.getPropertyString("id") + "/" + ((key != null) ? StringEscapeUtils.escapeHtml(key) : Userview.USERVIEW_KEY_EMPTY_VALUE) + "/" + mId);
                                }

                                //set Current, if current menu id is empty, search the 1st valid menu
                                if ((("".equals(menuId) || "index".equals(menuId) || menuId == null) && userview.getCurrent() == null && menu.isHomePageSupported())
                                        || (menuId != null && menuId.equals(mId))) {
                                    userview.setCurrent(menu);
                                    userview.setCurrentCategory(category);
                                }

                                //set home menu Id
                                if (userview.getPropertyString("homeMenuId") == null || userview.getPropertyString("homeMenuId").isEmpty() && menu.isHomePageSupported()) {
                                    userview.setProperty("homeMenuId", mId);
                                }
                                
                                if (menuRuleObj == null || !menuRuleObj.has("permissionHidden") || !"true".equals(menuRuleObj.getString("permissionHidden"))) {
                                    menu = new CachedUserviewMenu(menu);
                                    menus.add(menu);
                                }
                            } catch (Exception e) {
                                LogUtil.debug(getClass().getName(), "Userview Menu class file not found");
                            }
                        }

                        category.setMenus(menus);
                        if (!"yes".equals(category.getPropertyString("hide")) && menus.size() > 0) {
                            categories.add(category);
                        }
                    }
                }
            }
            userview.setCategories(categories);
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "Create Userview Error!!");
        } finally {
            if (isScreenCapture) {
                workflowUserManager.setCurrentThreadUser(currentThreadUser);
            }
        }
        return userview;
    }

    /**
     * Gets the id of an userview menu
     * @param menu
     * @return 
     */
    public String getMenuId(UserviewMenu menu) {
        String menuId = menu.getPropertyString("id");
        if (menu.getPropertyString("customId") != null && menu.getPropertyString("customId").trim().length() > 0) {
            menuId = menu.getPropertyString("customId");
        }
        return menuId;
    }

    /**
     * Gets the name of a userview from json definition
     * @param json
     * @return 
     */
    public String getUserviewName(String json) {
        try {
            JSONObject userviewObj = new JSONObject(json);
            return PropertyUtil.getProperties(userviewObj.getJSONObject("properties")).get("name").toString();
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "Get Userview Name Error!!");
        }
        return "";
    }
    
    /**
     * Gets the userview theme used by an userview
     * @param appId
     * @param userviewId
     * @return 
     */
    public UserviewTheme getUserviewTheme(String appId, String userviewId) {
        UserviewTheme theme = null;
        
        AppDefinition appDef = appService.getPublishedAppDefinition(appId);
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (appDef != null && request != null) {
            UserviewDefinition userviewDef = userviewDefinitionDao.loadById(userviewId, appDef);
            if (userviewDef != null) {
                String json = userviewDef.getJson();
                //process json with hash variable
                json = AppUtil.processHashVariable(json, null, StringUtil.TYPE_JSON, null, appDef);
                
                Map requestParameters = convertRequestParamMap(request.getParameterMap());
                requestParameters.put("contextPath", request.getContextPath());
                requestParameters.put("appId", appDef.getAppId());
                requestParameters.put("appVersion", appDef.getVersion().toString());

                try {
                    Userview userview = new Userview();

                    //set userview properties
                    JSONObject userviewObj = new JSONObject(json);
                    userview.setProperties(PropertyUtil.getProperties(userviewObj.getJSONObject("properties")));

                    JSONObject settingObj = userviewObj.getJSONObject("setting");
                    JSONObject themeObj = settingObj.getJSONObject("properties").getJSONObject("theme");

                    theme = (UserviewTheme) pluginManager.getPlugin(themeObj.getString("className"));
                    if (theme != null) {
                        theme.setProperties(PropertyUtil.getProperties(themeObj.getJSONObject("properties")));
                        theme.setRequestParameters(requestParameters);
                        theme.setUserview(userview);
                    }

                } catch (Exception e) {
                    LogUtil.debug(getClass().getName(), "get userview theme error.");
                }
            }
        }
        return theme;
    }

    /**
     * Gets userview description from json definition
     * @param json
     * @return 
     */
    public String getUserviewDescription(String json) {
        try {
            JSONObject userviewObj = new JSONObject(json);
            JSONObject settingObj = userviewObj.getJSONObject("setting");
            Object description = PropertyUtil.getProperties(settingObj.getJSONObject("properties")).get("userviewDescription");
            return (description != null) ? description.toString() : "";
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "Get Userview Description Error!!");
        }
        return "";
    }

    /**
     * Gets the userview theme used by an userview
     * @param appId
     * @param version
     * @param userviewId
     * @return 
     */
    public Set<String> getAllMenuIds(String appId, String version, String userviewId) {
        Set<String> ids = new HashSet<String>();
        
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        UserviewDefinition userviewDef = userviewDefinitionDao.loadById(userviewId, appDef);
        if (userviewDef != null) {
            String json = userviewDef.getJson();

            try {
                //set userview properties
                JSONObject userviewObj = new JSONObject(json);
                JSONArray categoriesArray = userviewObj.getJSONArray("categories");
                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject categoryObj = (JSONObject) categoriesArray.get(i);
                    JSONArray menusArray = categoryObj.getJSONArray("menus");
                    for (int j = 0; j < menusArray.length(); j++) {
                        JSONObject menuObj = (JSONObject) menusArray.get(j);
                        JSONObject props = menuObj.getJSONObject("properties");
                        String id = props.getString("id");
                        String customId = (props.has("customId"))?props.getString("customId"):null;
                        if (customId != null && !customId.isEmpty()) {
                            ids.add(customId);
                        } else {
                            ids.add(id);
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.debug(getClass().getName(), "get userview menu ids error.");
            }
        }
        
        return ids;
    }
    
    private Map convertRequestParamMap(Map params) {
        Map result = new HashMap();
        for (String key : (Set<String>) params.keySet()) {
            key = StringEscapeUtils.escapeHtml(key);
            String[] paramValue = (String[]) params.get(key);
            if (paramValue.length == 1) {
                result.put(key, paramValue[0]);
            } else {
                result.put(key, paramValue);
            }
        }
        return result;
    }
    
    public UserviewDefinition getDefaultUserview() {
        // check for app center userview setting
        String defaultUserviewProperty = "defaultUserview";
        UserviewDefinition defaultUserview = null;
        Setting defaultUserviewSetting = setupManager.getSettingByProperty(defaultUserviewProperty);
        if (defaultUserviewSetting != null) {
            // check app center userview is published
            String defaultUserviewValue = defaultUserviewSetting.getValue();
            StringTokenizer st = new StringTokenizer(defaultUserviewValue, "/");
            String appId = (st.hasMoreTokens()) ? st.nextToken() : null;
            String userviewId = (st.hasMoreTokens()) ? st.nextToken() : null;
            if (appId != null && userviewId != null) {
                AppDefinition appDef = appService.getPublishedAppDefinition(appId);
                if (appDef != null) {
                    defaultUserview = userviewDefinitionDao.loadById(userviewId, appDef);
                }
            }            
        } else {
            // import default app center app
            String path = "/setup/apps/APP_appcenter7-1.zip";
            LogUtil.info(getClass().getName(), "Import default app center " + path);
            InputStream in = null;
            try {
                in = getClass().getResourceAsStream(path);
                byte[] fileContent = IOUtils.toByteArray(in);
                final AppDefinition appDef = appService.importApp(fileContent);
                if (appDef != null) {
                    TransactionTemplate transactionTemplate = (TransactionTemplate) AppUtil.getApplicationContext().getBean("transactionTemplate");
                    transactionTemplate.execute(new TransactionCallback<Object>() {
                        public Object doInTransaction(TransactionStatus ts) {
                            appService.publishApp(appDef.getId(), null);
                            return null;
                        }
                    });
                    // get app center userview
                    Collection<UserviewDefinition> userviewList = appDef.getUserviewDefinitionList();
                    if (!userviewList.isEmpty()) {
                        String userviewId = userviewList.iterator().next().getId();
                        defaultUserview = userviewDefinitionDao.loadById(userviewId, appDef);
                        
                        // save setting
                        String value = defaultUserview.getAppId() + "/" + defaultUserview.getId();
                        Setting newSetting = new Setting();
                        newSetting.setProperty(defaultUserviewProperty);
                        newSetting.setValue(value);
                        setupManager.saveSetting(newSetting);                        
                    }
                }
            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), ex, "Failed to import default app center " + path);
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                }
            }
        }
        return defaultUserview;
    }

    public boolean isDefaultUserview(String appId, String userviewId) {
        boolean result = false;
        String userviewPath = appId + "/" + userviewId;
        Setting defaultUserviewSetting = setupManager.getSettingByProperty("defaultUserview");
        if (defaultUserviewSetting != null) {
            // check app center userview is published
            String defaultUserviewValue = defaultUserviewSetting.getValue();
            result = userviewPath.equals(defaultUserviewValue);
        }
        return result;
    }
    
}
