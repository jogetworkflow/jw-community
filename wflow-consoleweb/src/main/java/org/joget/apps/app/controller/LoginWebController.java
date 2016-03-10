package org.joget.apps.app.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.service.UserviewService;
import org.joget.apps.userview.service.UserviewThemeProcesser;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginWebController {

    @Autowired
    private UserviewService userviewService;
    @Autowired
    AppService appService;
    @Autowired
    UserviewDefinitionDao userviewDefinitionDao;
    @Autowired
    WorkflowUserManager workflowUserManager;

    @RequestMapping("/login")
    public String login(ModelMap map, HttpServletRequest request, HttpServletResponse response) throws Exception {
        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
        String savedUrl = "";
        if (savedRequest != null) {
            savedUrl = savedRequest.getRedirectUrl();
        } else if (request.getHeader("referer") != null) { //for userview logout
            savedUrl = request.getHeader("referer");
        }

        if (savedUrl.contains("/web/userview") || savedUrl.contains("/web/embed/userview")) {
            String embedPrefix = "";
            if (savedUrl.contains("/web/userview")) {
                savedUrl = savedUrl.substring(savedUrl.indexOf("/web/userview"));
                savedUrl = savedUrl.replace("/web/userview/", "");
            } else {
                savedUrl = savedUrl.substring(savedUrl.indexOf("/web/embed/userview"));
                savedUrl = savedUrl.replace("/web/embed/userview/", "");
                embedPrefix = "embed/";
            }
            
            if (request.getParameter("embed") != null && Boolean.parseBoolean((String) request.getParameter("embed"))) {
                embedPrefix = "embed/";
            }
            
            String[] urlKey = savedUrl.split("/");
            String appId = urlKey[0];
            String userviewId = urlKey[1];

            if (savedRequest == null) { //for userview logout
                return "redirect:/web/" + embedPrefix + "userview/" + appId + "/" + userviewId;
            }
        } else if ((savedUrl.contains("/web/mobile") || savedUrl.contains("/web/embed/mobile")) && !workflowUserManager.isCurrentUserAnonymous()) {
            String embedPrefix = "";
            if (savedUrl.endsWith("/web/mobile") || savedUrl.endsWith("/web/embed/mobile")) {
                savedUrl = "";
            } else if (savedUrl.contains("/web/mobile")) {
                savedUrl = savedUrl.substring(savedUrl.indexOf("/web/mobile"));
                savedUrl = savedUrl.replace("/web/mobile/", "");
            } else {
                savedUrl = savedUrl.substring(savedUrl.indexOf("/web/embed/mobile"));
                savedUrl = savedUrl.replace("/web/embed/mobile/", "");
                embedPrefix = "embed/";
            }
            
            if (savedUrl.isEmpty() || "apps".equals(savedUrl)) {
                return "redirect:/web/mobile/apps?_=" + System.currentTimeMillis();
            }
            
            if (request.getParameter("embed") != null && Boolean.parseBoolean((String) request.getParameter("embed"))) {
                embedPrefix = "embed/";
            }
            
            String[] urlKey = savedUrl.split("/");
            String appId = urlKey[0];
            String userviewId = null;
            if (urlKey.length > 1) {
                userviewId = urlKey[1];
            }

            if (savedRequest == null) { //for userview logout
                String redirectUrl = (userviewId != null) ? "redirect:/web/" + embedPrefix + "mobile/" + appId + "/" + userviewId + "//landing?_=" + System.currentTimeMillis() : "redirect:/web/mobile/apps?_=" + System.currentTimeMillis();
                return redirectUrl;
            }
        } else if (savedUrl.contains("/web/ulogin") || savedUrl.contains("/web/embed/ulogin")) {
            Boolean embed = false;
            if (savedUrl.contains("/web/ulogin")) {
                savedUrl = savedUrl.substring(savedUrl.indexOf("/web/ulogin"));
                savedUrl = savedUrl.replace("/web/ulogin/", "");
            } else {
                savedUrl = savedUrl.substring(savedUrl.indexOf("/web/embed/ulogin"));
                savedUrl = savedUrl.replace("/web/embed/ulogin/", "");
                embed = true;
            }
            
            String[] urlKey = savedUrl.split("/");
            String appId = urlKey[0];
            String userviewId = urlKey[1];
            String key = null;
            String menuId = null;
            if (urlKey.length > 2) {
                key = urlKey[2];
                
                if (urlKey.length > 3) {
                    menuId = urlKey[3];
                }
            }

            Long appVersion = appService.getPublishedVersion(appId);
            if (appVersion == null || appVersion == 0) {
                return "error404";
            }

            // retrieve app and userview
            AppDefinition appDef = appService.getAppDefinition(appId, appVersion.toString());
            if (appDef == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
            map.addAttribute("appId", appId);
            map.addAttribute("appDefinition", appDef);
            map.addAttribute("appVersion", appDef.getVersion());
            map.addAttribute("key", key);
            map.addAttribute("menuId", menuId);
            map.addAttribute("embed", embed);
            UserviewDefinition userview = userviewDefinitionDao.loadById(userviewId, appDef);
            if (userview != null) {
                String json = userview.getJson();
                Userview userviewObject = userviewService.createUserview(json, null, false, request.getContextPath(), request.getParameterMap(), key, embed);
                UserviewThemeProcesser processer = new UserviewThemeProcesser(userviewObject, request);
                map.addAttribute("userview", userviewObject);
                map.addAttribute("processer", processer);
                String view = processer.getLoginView();
                if (view != null) {
                    if (view.startsWith("redirect:")) {
                        map.clear();
                    }
                    return view;
                }
            }

            return "ubuilder/login";
        } else if (savedUrl.contains("/web/mlogin") || savedUrl.contains("/web/embed/mlogin") || savedUrl.contains("/web/mobile") || savedUrl.contains("/web/embed/mobile")) {
            Boolean embed = false;
            if (savedUrl.equals("/web/mlogin") || savedUrl.equals("/web/embed/mlogin") || savedUrl.endsWith("/web/mobile") || savedUrl.endsWith("/web/embed/mobile")) {
                savedUrl = "";
            } else if (savedUrl.contains("/web/mlogin")) {
                savedUrl = savedUrl.substring(savedUrl.indexOf("/web/mlogin"));
                savedUrl = savedUrl.replace("/web/mlogin/", "");
            } else if (savedUrl.contains("/web/embed/mlogin")) {
                savedUrl = savedUrl.substring(savedUrl.indexOf("/web/embed/mlogin"));
                savedUrl = savedUrl.replace("/web/embed/mlogin/", "");
                embed = true;
            } else if (savedUrl.contains("/web/mobile")) {
                savedUrl = savedUrl.substring(savedUrl.indexOf("/web/mobile"));
                savedUrl = savedUrl.replace("/web/mobile/", "");
            } else {
                savedUrl = savedUrl.substring(savedUrl.indexOf("/web/embed/mobile"));
                savedUrl = savedUrl.replace("/web/embed/mobile/", "");
                embed = true;
            }
            
            if (savedUrl.isEmpty()) {
                return "mobile/mLogin";
            }
            
            String[] urlKey = savedUrl.split("/");
            String appId = urlKey[0];
            String userviewId = urlKey[1];
            String key = null;
            String menuId = null;
            if (urlKey.length > 2) {
                key = urlKey[2];
                
                if (urlKey.length > 3) {
                    menuId = urlKey[3];
                }
            }

            Long appVersion = appService.getPublishedVersion(appId);
            if (appVersion == null || appVersion == 0) {
                return "error404";
            }

            // retrieve app and userview
            AppDefinition appDef = appService.getAppDefinition(appId, appVersion.toString());
            if (appDef == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
            map.addAttribute("appId", appId);
            map.addAttribute("appDefinition", appDef);
            map.addAttribute("appVersion", appDef.getVersion());
            map.addAttribute("key", key);
            map.addAttribute("menuId", menuId);
            map.addAttribute("embed", embed);
            UserviewDefinition userview = userviewDefinitionDao.loadById(userviewId, appDef);
            if (userview != null) {
                String json = userview.getJson();
                map.addAttribute("userview", userviewService.createUserview(json, null, false, request.getContextPath(), request.getParameterMap(), key, embed));
            }

            return "mobile/mLogin";
        } else if (savedUrl.contains(request.getContextPath() + "/mobile")) {
            return "mobile/mLogin";
        }

        return "login";
    }

    @RequestMapping("/unauthorized")
    public String unauthorized(ModelMap map) {
        return "unauthorized";
    }
    
    @RequestMapping("/mlogin")
    public String mlogin(ModelMap map, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return "mobile/mLogin";
    }
    
}
