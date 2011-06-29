package org.joget.apps.app.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.userview.service.UserviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserviewWebController {

    @Autowired
    private UserviewService userviewService;
    @Autowired
    AppService appService;
    @Autowired
    UserviewDefinitionDao userviewDefinitionDao;

    @RequestMapping(value = "/userview/(*:appId)/(*:userviewId)/(*:key)/(*:menuId)")
    public String view(ModelMap map, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam("userviewId") String userviewId, @RequestParam(value = "menuId", required = false) String menuId, @RequestParam(value = "key", required = false) String key) throws Exception {

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
        UserviewDefinition userview = userviewDefinitionDao.loadById(userviewId, appDef);
        if (userview != null) {
            String json = userview.getJson();
            map.addAttribute("userview", userviewService.createUserview(json, menuId, false, request.getContextPath(), request.getParameterMap(), key));
        }
        return "ubuilder/view";
    }

    @RequestMapping(value = "/ulogin/(*:appId)/(*:userviewId)/(*:key)")
    public String login(ModelMap map, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam("userviewId") String userviewId, @RequestParam(value = "menuId", required = false) String menuId, @RequestParam(value = "key", required = false) String key) throws Exception {

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
        UserviewDefinition userview = userviewDefinitionDao.loadById(userviewId, appDef);
        if (userview != null) {
            String json = userview.getJson();
            map.addAttribute("userview", userviewService.createUserview(json, null, false, request.getContextPath(), request.getParameterMap(), key));
        }

        return "ubuilder/login";
    }
}
