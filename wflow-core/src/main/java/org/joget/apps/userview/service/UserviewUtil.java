package org.joget.apps.userview.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.directwebremoting.util.SwallowingHttpServletResponse;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.userview.model.Permission;
import org.joget.apps.userview.model.PwaOfflineResources;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewCategory;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.model.UserviewPwaTheme;
import org.joget.apps.userview.model.UserviewSetting;
import org.joget.apps.userview.model.UserviewTheme;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.directory.model.User;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.ServletContextAware;

/**
 * Utility methods used by userview for rendering
 * 
 */
@Service("userviewUtil")
public class UserviewUtil implements ApplicationContextAware, ServletContextAware {

    static ApplicationContext appContext;
    static ServletContext servletContext;

    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        appContext = ac;
    }

    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    public void setServletContext(ServletContext sc) throws BeansException {
        servletContext = sc;
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * Method used to retrieve HTML template of an userview theme
     * @param theme
     * @param data
     * @param templatePath
     * @return 
     */
    public static String getTemplate(UserviewTheme theme, Map data, String templatePath) {
        return getTemplate(theme, data, templatePath, null);
    }

    /**
     * Method used to retrieve HTML template of an userview theme with i18n supported
     * @param theme
     * @param data
     * @param templatePath
     * @return 
     */
    public static String getTemplate(UserviewTheme theme, Map data, String templatePath, String translationPath) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        if (data == null) {
            data = new HashMap();
        }

        data.put("theme", theme);

        String content = pluginManager.getPluginFreeMarkerTemplate(data, theme.getClassName(), templatePath, translationPath);
        return content;
    }
    
    /**
     * Method used to retrieve HTML template of an userview menu
     * @param menu
     * @return
     * @throws RuntimeException 
     */
    public static String getUserviewMenuHtml(UserviewMenu menu) throws RuntimeException {
        String content = UserviewCache.getCachedContent(menu, UserviewCache.CACHE_TYPE_PAGE);
        if (content == null) {
            String jspPage = menu.getReadyJspPage();
            if (jspPage != null && !jspPage.isEmpty()) {
                Map<String, Object> modelMap = new HashMap<String, Object>();
                modelMap.put("properties", menu.getProperties());
                modelMap.put("requestParameters", menu.getRequestParameters());
                content = UserviewUtil.renderJspAsString(jspPage, modelMap);
            } else {
                content = menu.getReadyRenderPage();
            }
            UserviewCache.setCachedContent(menu, UserviewCache.CACHE_TYPE_PAGE, content);
        }
        return content;
    }

    /**
     * Method used to convert a jsp page as HTML template
     * @param viewName
     * @param modelMap
     * @return 
     */
    public static String renderJspAsString(String viewName, Map<String, Object> modelMap) {
        if (viewName == null) {
            return null;
        }

        String result = null;

        StringWriter sout = new StringWriter();
        StringBuffer sbuffer = sout.getBuffer();

        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        HttpServletResponse response = WorkflowUtil.getHttpServletResponse();
        HttpServletResponse swallowingResponse = new SwallowingHttpServletResponse(response, sout, "UTF-8");
        HttpServletResponse wrapper = new HttpServletResponseWrapper(swallowingResponse);

        try {
            //Add the modelMap to the request as attributes
            addModelAsRequestAttributes(request, modelMap);

            // Using UTF-8 for the rendered JSP
            swallowingResponse.setContentType("text/html; charset=utf-8");

            RequestDispatcher dispatcher = servletContext.getRequestDispatcher("/WEB-INF/jsp/" + viewName);

            dispatcher.include(request, wrapper);

            result = sbuffer.toString();
        } catch (Exception e) {
            LogUtil.error("UserviewUtil", e, viewName);
        }

        return result;
    }

    private static void addModelAsRequestAttributes(ServletRequest request, Map<String, Object> modelMap) {
        if (modelMap != null && request != null) {
            for (Map.Entry<String, Object> entry : modelMap.entrySet()) {
                String modelName = entry.getKey();
                Object modelValue = entry.getValue();
                if (modelValue != null) {
                    request.setAttribute(modelName, modelValue);
                } else {
                    request.removeAttribute(modelName);
                }
            }
        }
    }
    
    public static String appendPropertyOptions(String propertyOptions, String additionalProperties) {
        if (propertyOptions != null && !propertyOptions.isEmpty() && additionalProperties != null && !additionalProperties.isEmpty() ) {
            propertyOptions = propertyOptions.substring(0, propertyOptions.lastIndexOf("]")) + "," + additionalProperties + "]"; 
        }
        return propertyOptions;
    }
    
    public static boolean checkUserviewInboxEnabled(UserviewDefinition userviewDef) {
        boolean inboxEnabled = false;
        if (userviewDef != null) {
            try {
                String json = userviewDef.getJson();
                JSONObject userviewObj = new JSONObject(json);
                JSONObject settingObj = userviewObj.getJSONObject("setting");
                JSONObject themeObj = settingObj.getJSONObject("properties").getJSONObject("theme");
                String inboxSetting = themeObj.getJSONObject("properties").getString("inbox");
                if (inboxSetting != null) {
                    inboxEnabled = !"".equals(inboxSetting);
                }
            } catch (JSONException ex) {
                //ignore
            }
        }
        return inboxEnabled;
    }
    
    public static Boolean getPermisionResult(JSONObject permissionObj, Map requestParameters, User currentUser) throws JSONException {
        Boolean isAuthorize = true;
        if (permissionObj != null && permissionObj.has("className")) {
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
                permission.setRequestParameters(requestParameters);
                permission.setCurrentUser(currentUser);

                isAuthorize = permission.isAuthorize();
            }
        }
        return isAuthorize;
    }

    public static String getManifest(String appId, String userviewId) {
        String manifest = "";
        AppService appService = (AppService)AppUtil.getApplicationContext().getBean("appService");
        UserviewService userviewService = (UserviewService)AppUtil.getApplicationContext().getBean("userviewService");
        UserviewDefinitionDao userviewDefinitionDao = (UserviewDefinitionDao)AppUtil.getApplicationContext().getBean("userviewDefinitionDao");
        AppDefinition appDef = appService.getPublishedAppDefinition(appId);
        if (appDef != null) {
            UserviewDefinition userviewDef = userviewDefinitionDao.loadById(userviewId, appDef);
            if (userviewDef != null) {
                String json = userviewDef.getJson();
                UserviewSetting userviewSetting = userviewService.getUserviewSetting(appDef, json);
                UserviewTheme theme = userviewSetting.getTheme();
                if (theme instanceof UserviewPwaTheme) {
                    manifest = ((UserviewPwaTheme)theme).getManifest(appId, userviewId);
                }
            }
        }
        return manifest;    
    }
    
    public static String getServiceWorker(String appId, String userviewId, String userviewKey) {
        String serviceWorkerJs = "";
        AppService appService = (AppService)AppUtil.getApplicationContext().getBean("appService");
        UserviewService userviewService = (UserviewService)AppUtil.getApplicationContext().getBean("userviewService");
        UserviewDefinitionDao userviewDefinitionDao = (UserviewDefinitionDao)AppUtil.getApplicationContext().getBean("userviewDefinitionDao");
        AppDefinition appDef = appService.getPublishedAppDefinition(appId);
        if (appDef != null) {
            UserviewDefinition userviewDef = userviewDefinitionDao.loadById(userviewId, appDef);
            if (userviewDef != null) {
                String json = userviewDef.getJson();
                UserviewSetting userviewSetting = userviewService.getUserviewSetting(appDef, json);
                UserviewTheme theme = userviewSetting.getTheme();
                if (theme instanceof UserviewPwaTheme) {
                    serviceWorkerJs = ((UserviewPwaTheme)theme).getServiceWorker(appId, userviewId, userviewKey);
                }
            }
        }
        return serviceWorkerJs;
    }
    
    public static Set<String> getAppStaticResources(AppDefinition appDef) {
        Set<String> urls = new HashSet<String>();
        
        try {
            String resourceFilePath = SecurityUtil.normalizedFileName(SetupManager.getBaseDirectory() + "/app_pwa/" + appDef.getAppId() + "/resources.json");
            Gson gson = new Gson();
            Map<Long, Set<String>> data = null;
            if ((new File(resourceFilePath)).exists()) {
                String json = FileUtils.readFileToString(new File(resourceFilePath), "UTF-8");
                data = gson.fromJson(json, new TypeToken<Map<Long, Set<String>>>(){}.getType());
            } else {
                (new File(resourceFilePath)).getParentFile().mkdirs();
            }
            
            Long lastModified = null;
            Date appLastModified = appDef.getDateModified();
            if (appLastModified != null) {
                lastModified = appLastModified.getTime();
            } else {
                // git folder not available yet
                return urls;
            }
            if (data != null && !data.isEmpty()) {
                //check date
                Long resourceLastModified = data.keySet().iterator().next();
                if (lastModified.compareTo(resourceLastModified) != 0) {
                    data = null;
                } else {
                    return data.get(resourceLastModified);
                }
            }
            
            if (data == null) {
                data = new HashMap<Long, Set<String>>();
                
                PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
                Collection<Plugin> pluginList = pluginManager.list(PwaOfflineResources.class);
                
                if (pluginList != null && !pluginList.isEmpty()) {
                    String concatAppDef = "";
                    if (appDef.getFormDefinitionList() != null) {
                        for (FormDefinition o : appDef.getFormDefinitionList()) {
                            concatAppDef += o.getJson() + "~~~";
                        }
                    }
                    if (appDef.getDatalistDefinitionList() != null) {
                        for (DatalistDefinition o : appDef.getDatalistDefinitionList()) {
                            concatAppDef += o.getJson() + "~~~";
                        }
                    }
                    if (appDef.getUserviewDefinitionList() != null) {
                        for (UserviewDefinition o : appDef.getUserviewDefinitionList()) {
                            concatAppDef += o.getJson() + "~~~";
                        }
                    }
                    if (appDef.getBuilderDefinitionList() != null) {
                        for (BuilderDefinition o : appDef.getBuilderDefinitionList()) {
                            concatAppDef += o.getJson() + "~~~";
                        }
                    }
                    PackageDefinition packageDef = appDef.getPackageDefinition();
                    if (packageDef != null) {
                        if (packageDef.getPackageActivityPluginMap() != null) {
                            for (PackageActivityPlugin o : packageDef.getPackageActivityPluginMap().values()) {
                                concatAppDef += o.getPluginName() + "~~~";
                                concatAppDef += o.getPluginProperties() + "~~~";
                            }
                        }
                    }
                    
                    // look for plugins used in any definition file
                    Set<String> temp = null;
                    for (Plugin plugin: pluginList) {
                        String pluginClassName = ClassUtils.getUserClass(plugin).getName();
                        if (concatAppDef.contains(pluginClassName)) {
                            temp = ((PwaOfflineResources) plugin).getOfflineStaticResources();
                            if (temp != null) {
                                urls.addAll(temp);
                            }
                        }                
                    }
                }
                
                data.put(lastModified, urls);
                String json = gson.toJson(data, new TypeToken<Map<Long, Set<String>>>(){}.getType());
                FileUtils.writeStringToFile(new File(resourceFilePath), json, "UTF-8");
            }
        } catch (Exception e) {
            LogUtil.error(UserviewUtil.class.getName(), e, "");
        }
        return urls;
    }
       
    public static String getCacheUrls(String appId, String userviewId, String userviewKey, String contextPath) {
        AppService appService = (AppService)AppUtil.getApplicationContext().getBean("appService");
        UserviewService userviewService = (UserviewService)AppUtil.getApplicationContext().getBean("userviewService");
        UserviewDefinitionDao userviewDefinitionDao = (UserviewDefinitionDao)AppUtil.getApplicationContext().getBean("userviewDefinitionDao");
        AppDefinition appDef = appService.getPublishedAppDefinition(appId);
        if (appDef != null) {
            UserviewDefinition userviewDef = userviewDefinitionDao.loadById(userviewId, appDef);
            if (userviewDef != null) {
                String json = userviewDef.getJson();
                Userview userview = userviewService.createUserview(appDef, json, null, false, contextPath, null, userviewKey, false);
                Set<String> cacheUrls = new HashSet<String>();
                
                JSONObject returnJson = new JSONObject();
                
                try {
                    UserviewTheme theme = userview.getSetting().getTheme();
                    if (theme instanceof UserviewPwaTheme) {
                        returnJson.accumulate("static", getAppStaticResources(appDef));
                        
                        Set<String> themeUrls = ((UserviewPwaTheme) theme).getCacheUrls(appId, userviewId, userviewKey);
                        if (themeUrls != null && !themeUrls.isEmpty()) {
                            for (String u : themeUrls) {
                                cacheUrls.add(processUrl(u, appId, userviewId, userviewKey, null, contextPath));
                            }
                        }      
                        if (userview.getCategories() != null) {
                            for (UserviewCategory c : userview.getCategories()) {
                                if (c.getMenus() != null) {
                                    for (UserviewMenu m : c.getMenus()) {
                                        Set<String> urls = m.getOfflineCacheUrls();
                                        if (urls != null && !urls.isEmpty()) {
                                            for (String u : urls) {
                                                cacheUrls.add(processUrl(u, appId, userviewId, userviewKey, userviewService.getMenuId(m), contextPath));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    returnJson.accumulate("app", cacheUrls);
                    return returnJson.toString();
                } catch (Exception e) {
                    LogUtil.error(UserviewUtil.class.getName(), e, appId + ":" + userviewId);
                }
            }
        }
        return "{\"app\":[],\"static\":[]}";
    }
    
    protected static String processUrl(String url, String appId, String userviewId, String userviewKey, String menuId, String contextPath) {
        if (url.startsWith("?") && menuId != null && !menuId.isEmpty()) {
            url = menuId + url;
        }
        
        if (url.startsWith(contextPath) || url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        } else {
            url = contextPath + "/web/userview/"+appId+"/"+userviewId+"/"+userviewKey+"/"+url;
        }
        return url;
    }
    
    public static Set<String> getDatalistCacheUrls(DataList list, boolean cacheListAction, boolean cacheAllLinks){
        Set<String> urls = new HashSet<String>();
        
        if (list != null) {
            if (cacheListAction) {
                DataListAction[] actions = list.getActions();
                if (actions != null) {
                    for (DataListAction ac : actions) {
                        String href = ac.getHref();
                        String hrefParam = ac.getHrefParam();
                        String hrefTarget = ac.getTarget();
                        if (!"post".equalsIgnoreCase(hrefTarget) 
                                && href != null && !href.isEmpty() 
                                && (hrefParam == null || hrefParam.isEmpty())) {
                            urls.add(href);
                        }
                    }
                }
            }
            if (cacheAllLinks) {
                DataListAction[] rowActions = list.getRowActions();
                DataListColumn[] columns = list.getColumns();
                if ((rowActions != null && rowActions.length > 0) || (columns != null && columns.length > 0)) {
                    DataListCollection rows = list.getRows();
                    if (rows != null) {
                        for (Object r : rows) {
                            if (columns != null) {
                                for (DataListColumn c : columns) {
                                    if (c.getAction() != null) {
                                        addDatalistRowCacheUrl(urls, c.getAction(), list, r);
                                    }
                                }
                            }
                            if (rowActions != null) {
                                for (DataListAction rac : rowActions) {
                                    addDatalistRowCacheUrl(urls, rac, list, r);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return urls;
    }
    
    protected static void addDatalistRowCacheUrl(Set<String> urls, DataListAction a, DataList list, Object row) {
        String link = a.getHref();
        if (link != null && !link.isEmpty()) {
            if (link.startsWith("?" + list.getActionParamName() + "=")) {
                return;
            }
            
            String hrefParam = a.getHrefParam();
            String hrefColumn = a.getHrefColumn();
            if (hrefParam != null && !hrefParam.isEmpty() && hrefColumn != null && !hrefColumn.isEmpty()) {
                String[] params = hrefParam.split(";");
                String[] columns = hrefColumn.split(";");
                for (int i = 0; i < columns.length; i++ ) {
                    if (columns[i] != null && !columns[i].isEmpty()) {
                        boolean isValid = false;
                        if (params.length > i && params[i] != null && !params[i].isEmpty()) {
                            if (link.contains("?")) {
                                link += "&";
                            } else {
                                link += "?";
                            }
                            link += StringEscapeUtils.escapeHtml(params[i]);
                            link += "=";
                            isValid = true;
                        } if (!link.contains("?")) {
                            if (!link.endsWith("/")) {
                                link += "/";
                            }
                            isValid = true;
                        }
                        
                        if (isValid) {
                            Object paramValue = DataListService.evaluateColumnValueFromRow(row, columns[i]);
                            if (paramValue == null) {
                                paramValue = StringEscapeUtils.escapeHtml(columns[i]);
                            }
                            try {
                                link += (paramValue != null) ? URLEncoder.encode(paramValue.toString(), "UTF-8") : null;
                            } catch (UnsupportedEncodingException ex) {
                                link += paramValue;
                            }
                        }
                    }
                }
            }
            urls.add(link);
        }
    }
}
