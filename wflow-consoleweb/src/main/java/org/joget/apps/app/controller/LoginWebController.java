package org.joget.apps.app.controller;

import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.service.UserviewService;
import org.joget.apps.userview.service.UserviewThemeProcesser;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.commons.util.StringUtil;
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
        
        if (savedUrl.contains("/web/client/app/assignment/")) {
            UserviewDefinition defaultUserview = userviewService.getDefaultUserview();
            if (UserviewUtil.checkUserviewInboxEnabled(defaultUserview)) {
                String activityId = savedUrl.substring(savedUrl.lastIndexOf("/")+1);
                // redirect to app center userview
                String path = "redirect:/web/userview/" + defaultUserview.getAppId() + "/" +  defaultUserview.getId() + "/_/_ja_inbox?_mode=assignment&activityId=" + URLEncoder.encode(activityId, "UTF-8");
                return path;
            }
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
            
            if (menuId != null && menuId.contains("?")) {
                String queryString = menuId.substring(menuId.indexOf("?")+1);
                map.addAttribute("queryString", StringUtil.encodeUrlParam(queryString));
                
                menuId = menuId.substring(0, menuId.indexOf("?"));
            }

            // retrieve app and userview
            AppDefinition appDef = appService.getPublishedAppDefinition(appId);
            if (appDef == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
            map.addAttribute("appId", appDef.getId());
            map.addAttribute("appDefinition", appDef);
            map.addAttribute("appVersion", appDef.getVersion());
            if (key != null) {
                key = StringEscapeUtils.escapeHtml(key);
            }
            map.addAttribute("key", key);
            if (menuId != null) {
                menuId = StringEscapeUtils.escapeHtml(menuId);
            }
            map.addAttribute("menuId", menuId);
            map.addAttribute("embed", embed);
            UserviewDefinition userview = userviewDefinitionDao.loadById(userviewId, appDef);
            if (userview != null) {
                String json = userview.getJson();
                Userview userviewObject = userviewService.createUserview(json, menuId, false, request.getContextPath(), request.getParameterMap(), key, embed);
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
        }
        
        if (!savedUrl.contains("/web/console")) {
            UserviewDefinition userviewDef = userviewService.getDefaultUserview();
            if (userviewDef != null) {
                AppDefinition appDef = userviewDef.getAppDefinition();
                AppUtil.setCurrentAppDefinition(appDef);
                map.addAttribute("appId", appDef.getId());
                map.addAttribute("appDefinition", appDef);
                map.addAttribute("appVersion", appDef.getVersion());
                UserviewDefinition userview = userviewDefinitionDao.loadById(userviewDef.getId(), appDef);
                if (userview != null) {
                    String json = userview.getJson();
                    Userview userviewObject = userviewService.createUserview(json, null, false, request.getContextPath(), request.getParameterMap(), null, false);
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
            }
        }

        return "login";
    }

    @RequestMapping("/unauthorized")
    public String unauthorized(ModelMap map) {
        return "unauthorized";
    }
    
}
