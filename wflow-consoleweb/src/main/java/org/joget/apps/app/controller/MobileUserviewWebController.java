package org.joget.apps.app.controller;

import java.io.Writer;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.MobileUtil;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewCategory;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.model.UserviewSetting;
import org.joget.apps.userview.service.UserviewService;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MobileUserviewWebController {

    @Autowired
    UserviewService userviewService;
    
    @Autowired
    AppService appService;
    
    @Autowired
    AppDefinitionDao appDefinitionDao;
    
    @Autowired
    UserviewDefinitionDao userviewDefinitionDao;

    @RequestMapping({"/mobile/(*:appId)/(*:userviewId)/(~:key)", "/mobile/(*:appId)/(*:userviewId)", "/mobile/(*:appId)/(*:userviewId)/(*:key)/(*:menuId)"})
    public String mobileView(ModelMap map, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam("userviewId") String userviewId, @RequestParam(value = "menuId", required = false) String menuId, @RequestParam(value = "key", required = false) String key, @RequestParam(value = "embed", required = false) Boolean embed) throws Exception {
        if (embed == null) {
            embed = false;
        }
        return embedMobileView(map, request, response, appId, userviewId, menuId, key, embed);
    }

    @RequestMapping({"/embed/mobile/(*:appId)/(*:userviewId)/(~:key)", "/embed/mobile/(*:appId)/(*:userviewId)", "/embed/mobile/(*:appId)/(*:userviewId)/(*:key)/(*:menuId)"})
    public String embedMobileView(ModelMap map, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam("userviewId") String userviewId, @RequestParam(value = "menuId", required = false) String menuId, @RequestParam(value = "key", required = false) String key, Boolean embed) throws Exception {
        if (embed == null) {
            embed = true;
        }
        
        //check for empty key
        if (key != null && key.equals(Userview.USERVIEW_KEY_EMPTY_VALUE)) {
            key = null;
        }

        // validate input
        SecurityUtil.validateStringInput(appId);
        SecurityUtil.validateStringInput(userviewId);
        SecurityUtil.validateStringInput(menuId);        
        SecurityUtil.validateStringInput(key);
        SecurityUtil.validateBooleanInput(embed);
        
        // retrieve app and userview
        AppDefinition appDef = appService.getPublishedAppDefinition(appId);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        Map<String, Cookie> cookiesMap = getCookiesMap(request);
        
        if (cookiesMap.get("cordova") != null && "true".equals(cookiesMap.get("cordova").getValue())) {
            map.addAttribute("showDesktopButton", "false");
        }
        if (cookiesMap.get("all-apps") != null && "true".equals(cookiesMap.get("all-apps").getValue())) {
            map.addAttribute("showAllAppsButton", "true");
        } else {
            map.addAttribute("showAllAppsButton", "false");
        }

        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("key", key);
        map.addAttribute("menuId", menuId);
        map.addAttribute("embed", embed);
        MobileUtil.setMobileView(request, Boolean.TRUE);
        UserviewDefinition userviewDef = userviewDefinitionDao.loadById(userviewId, appDef);
        if (userviewDef != null) {
            String json = userviewDef.getJson();
            Userview userview = userviewService.createUserview(json, menuId, false, request.getContextPath(), request.getParameterMap(), key, embed);
            boolean loginRequired = "true".equals(userview.getSetting().getProperty("mobileLoginRequired"));
            if (loginRequired) {
                boolean isAnonymous = WorkflowUtil.isCurrentUserAnonymous();
                if (isAnonymous) {
                    return "redirect:/web/mlogin/" + appId + "/" + userviewId + "/"+(key != null?key:Userview.USERVIEW_KEY_EMPTY_VALUE)+"/landing";
                }
            }
            map.addAttribute("userview", userview);
        }
        LogUtil.debug(getClass().getName(), "Request: " + request.getRequestURI());
        return "mobile/mView";
    }

    @RequestMapping({"/mlogin/(*:appId)/(*:userviewId)/(~:key)", "/mlogin/(*:appId)/(*:userviewId)", "/mlogin/(*:appId)/(*:userviewId)/(*:key)/(*:menuId)"})
    public String mobileLogin(ModelMap map, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam("userviewId") String userviewId, @RequestParam(value = "menuId", required = false) String menuId, @RequestParam(value = "key", required = false) String key, @RequestParam(value = "embed", required = false) Boolean embed) throws Exception {
        if (embed == null) {
            embed = false;
        }
        return embedMobileLogin(map, request, response, appId, userviewId, menuId, key, embed);
    }

    @RequestMapping({"/embed/mlogin/(*:appId)/(*:userviewId)/(~:key)", "/embed/mlogin/(*:appId)/(*:userviewId)", "/embed/mlogin/(*:appId)/(*:userviewId)/(*:key)/(*:menuId)"})
    public String embedMobileLogin(ModelMap map, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam("userviewId") String userviewId, @RequestParam(value = "menuId", required = false) String menuId, @RequestParam(value = "key", required = false) String key, Boolean embed) throws Exception {
        if (embed == null) {
            embed = true;
        }
        
        //check for empty key
        if (key != null && key.equals(Userview.USERVIEW_KEY_EMPTY_VALUE)) {
            key = null;
        }
        
        // retrieve app and userview
        SecurityUtil.validateStringInput(appId);
        SecurityUtil.validateStringInput(menuId);        
        SecurityUtil.validateStringInput(key);
        SecurityUtil.validateBooleanInput(embed);        
        AppDefinition appDef = appService.getPublishedAppDefinition(appId);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("key", key);
        map.addAttribute("menuId", menuId);
        map.addAttribute("embed", embed);
        MobileUtil.setMobileView(request, Boolean.TRUE);
        UserviewDefinition userview = userviewDefinitionDao.loadById(userviewId, appDef);
        if (userview != null) {
            String json = userview.getJson();
            map.addAttribute("userview", userviewService.createUserview(json, null, false, request.getContextPath(), request.getParameterMap(), key, embed));
        }

        return "mobile/mLogin";
    }

    @RequestMapping({"/mobile", "/mobile/", "/mobile/apps"})
    public String mobileRunApps(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        return "redirect:/";
    }

    @RequestMapping({"/mobilecache/(*:appId)/(*:userviewId)"})
    public void mobileAppCacheManifest(HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam("userviewId") String userviewId) throws Exception {
        // retrieve app and userview
        AppDefinition appDef = appService.getPublishedAppDefinition(appId);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        UserviewDefinition userviewDef = userviewDefinitionDao.loadById(userviewId, appDef);
        if (userviewDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String json = userviewDef.getJson();
        Userview userview = userviewService.createUserview(json, null, false, request.getContextPath(), request.getParameterMap(), null, false);
        boolean cacheEnabled = "true".equals(userview.getSetting().getProperty("mobileCacheEnabled"));
        if (!cacheEnabled) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // set response type
        response.setContentType("text/cache-manifest");

        // set expires header
        response.addDateHeader("Expires", 0);

        // generate cache manifest for the app userview
        String contextPath = AppUtil.getRequestContextPath();
        String currentUser = WorkflowUtil.getCurrentUsername();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        String manifest = "CACHE MANIFEST\n"
                + "# " + appDef.getAppId() + " v" + appDef.getVersion() + " " + userviewDef.getId() + " " + userviewDef.getDateModified() + " " + currentUser + " " + cal.getTime() + "\n"
                + "NETWORK:\n"
                + "*\n"
                + "\n"
                + "CACHE:\n"
                + contextPath + "/home/logo.png\n"
                + contextPath + "/home/style.css\n"
                + contextPath + "/wro/mobile_common.css\n"
                + contextPath + "/wro/mobile_common.js\n"
                + contextPath + "/mobile/jqm/images/ajax-loader.gif\n";

        UserviewSetting setting = userview.getSetting();
        String backgroundUrl = setting.getPropertyString("mobileViewBackgroundUrl");
        String logoUrl = setting.getPropertyString("mobileViewLogoUrl");
        if (backgroundUrl != null && !backgroundUrl.trim().isEmpty()) {
            manifest += backgroundUrl + "\n";
        }
        if (logoUrl != null && !logoUrl.trim().isEmpty()) {
            manifest += logoUrl + "\n";
        }

        Collection<UserviewCategory> categories = userview.getCategories();
        for (UserviewCategory cat : categories) {
            Collection<UserviewMenu> menus = cat.getMenus();
            for (UserviewMenu menu : menus) {
                String menuId = userviewService.getMenuId(menu);
                manifest += contextPath + "/web/mobile/" + appDef.getId() + "/" + userviewDef.getId() + "/"+Userview.USERVIEW_KEY_EMPTY_VALUE+"/" + menuId + "\n";
            }
        }

        LogUtil.debug(getClass().getName(), "Request: " + request.getRequestURI());
        // output manifest
        Writer out = response.getWriter();
        out.write(manifest);
        out.flush();
    }

    @RequestMapping({"/mobilecache/default"})
    public void mobileDefaultCacheManifest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // set response type
        response.setContentType("text/cache-manifest");

        // set expires header
        response.addDateHeader("Expires", 0);

        // generate default cache manifest
        String contextPath = AppUtil.getRequestContextPath();
        String currentUser = WorkflowUtil.getCurrentUsername();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        String manifest = "CACHE MANIFEST\n"
                + "# default " + currentUser + " " + cal.getTime() + "\n"
                + "NETWORK:\n"
                + "*\n"
                + "\n"
                + "FALLBACK:\n"
                + "/ " + contextPath + "/mobile/offline.jsp \n"
                + "\n"
                + "CACHE:\n"
                + contextPath + "/home/logo.png\n"
                + contextPath + "/home/style.css\n";
        
        Map<String, Cookie> cookiesMap = getCookiesMap(request);
        if (cookiesMap.get("all-apps") != null && "true".equals(cookiesMap.get("all-apps").getValue())) {
            manifest += contextPath + "/web/mobile/\n";
            manifest += contextPath + "/web/mobile/apps\n";
        }
        
        manifest += contextPath + "/wro/mobile_common.css\n"
                + contextPath + "/wro/mobile_common.js\n"
                + contextPath + "/mobile/jqm/images/ajax-loader.gif\n";
        /*
         // cache landing page for each app
         Collection<AppDefinition> resultAppDefinitionList = appService.getPublishedApps(null, true, false);
         for (AppDefinition appDef: resultAppDefinitionList) {
            Collection<UserviewDefinition> userviewDefList = appDef.getUserviewDefinitionList();
            for (UserviewDefinition userviewDef: userviewDefList) {
               manifest += contextPath + "/web/mobile/" + appDef.getId() + "/" + userviewDef.getId() + "//landing\n";
               manifest += "# " + appDef.getName() + " v" + appDef.getVersion() + " " + userviewDef.getName() + "\n"; 
            }
         }
         */
        
        LogUtil.debug(getClass().getName(), "Request: " + request.getRequestURI());
        // output manifest
        Writer out = response.getWriter();
        out.write(manifest);
        out.flush();
    }
    
    @RequestMapping({"/mapp/(*:appId)/(*:userviewId)/(~:key)", "/mapp/(*:appId)/(*:userviewId)", "/mapp/(*:appId)/(*:userviewId)/(*:key)/(*:menuId)"})
    public String embedMobileView(ModelMap map, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam("userviewId") String userviewId, @RequestParam(value = "menuId", required = false) String menuId, @RequestParam(value = "key", required = false) String key) throws Exception {
        String origin = request.getHeader("Origin");
        if (origin != null) {
            origin = origin.replace("\n", "").replace("\r", "");
        }
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Content-type", "application/xml");
        
        //check for empty key
        if (key != null && key.equals(Userview.USERVIEW_KEY_EMPTY_VALUE)) {
            key = null;
        }
        
        //require login to use
        if (WorkflowUtil.isCurrentUserAnonymous()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        
        // validate input
        appId = SecurityUtil.validateStringInput(appId);
        menuId = SecurityUtil.validateStringInput(menuId);        
        key = SecurityUtil.validateStringInput(key);
        
        // retrieve app and userview
        AppDefinition appDef = appService.getPublishedAppDefinition(appId);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("key", key);
        map.addAttribute("menuId", menuId);
        //try to support more by not setting mobile view
        //MobileUtil.setMobileView(request, Boolean.TRUE);
        UserviewDefinition userviewDef = userviewDefinitionDao.loadById(userviewId, appDef);
        if (userviewDef != null) {
            String json = userviewDef.getJson();
            Userview userview = userviewService.createUserview(json, menuId, false, request.getContextPath(), request.getParameterMap(), key, false);
            boolean loginRequired = "true".equals(userview.getSetting().getProperty("mobileLoginRequired"));
            if (loginRequired) {
                boolean isAnonymous = WorkflowUtil.isCurrentUserAnonymous();
                if (isAnonymous) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return null;
                }
            }
            
            map.addAttribute("userview", userview);
        }
        LogUtil.debug(getClass().getName(), "Mobile App Request: " + request.getRequestURI());
        return "mapp/view";
    }

    private Map<String, Cookie> getCookiesMap(HttpServletRequest request) {
        Map<String, Cookie> cookiesMap = new HashMap<String, Cookie>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookiesMap.put(cookie.getName(), cookie);
            }
        }
        return cookiesMap;
    }
}
