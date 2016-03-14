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
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
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

    @RequestMapping({"/userview/(*:appId)/(*:userviewId)/(~:key)","/userview/(*:appId)/(*:userviewId)","/userview/(*:appId)/(*:userviewId)/(*:key)/(*:menuId)"})
    public String view(ModelMap map, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam("userviewId") String userviewId, @RequestParam(value = "menuId", required = false) String menuId, @RequestParam(value = "key", required = false) String key, @RequestParam(value = "embed", required = false) Boolean embed) throws Exception {
        if (embed == null) {
            embed = false;
        }
        return embedView(map, request, response, appId, userviewId, menuId, key, embed, null);
    }
    
    @RequestMapping({"/embed/userview/(*:appId)/(*:userviewId)/(~:key)","/embed/userview/(*:appId)/(*:userviewId)","/embed/userview/(*:appId)/(*:userviewId)/(*:key)/(*:menuId)"})
    public String embedView(ModelMap map, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam("userviewId") String userviewId, @RequestParam(value = "menuId", required = false) String menuId, @RequestParam(value = "key", required = false) String key, Boolean embed, @RequestParam(value = "embed", required = false) Boolean embedParam) throws Exception {
        // validate input
        SecurityUtil.validateStringInput(appId);        
        SecurityUtil.validateStringInput(menuId);        
        SecurityUtil.validateStringInput(key);
        SecurityUtil.validateBooleanInput(embed);
        SecurityUtil.validateBooleanInput(embedParam);

        if (embedParam != null && !embedParam) {
            //exit embed mode by param
            return "redirect:/web/userview/" + appId + "/" + userviewId + "/" + ((key != null )?key:"") + "/" + menuId + '?' +StringUtil.decodeURL(request.getQueryString());
        } else if (embed == null) {
            embed = true;
        }
        
        //check for empty key
        if (key != null && key.equals(Userview.USERVIEW_KEY_EMPTY_VALUE)) {
            key = "";
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
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("key", key);
        map.addAttribute("menuId", menuId);
        map.addAttribute("embed", embed);
        map.addAttribute("queryString", request.getQueryString());
        UserviewDefinition userview = userviewDefinitionDao.loadById(userviewId, appDef);
        if (userview != null) {
            String json = userview.getJson();
            Userview userviewObject = userviewService.createUserview(json, menuId, false, request.getContextPath(), request.getParameterMap(), key, embed);
            UserviewThemeProcesser processer = new UserviewThemeProcesser(userviewObject, request);
            map.addAttribute("userview", userviewObject);
            map.addAttribute("processer", processer);
            String view = processer.getView();
            if (view != null) {
                if (view.startsWith("redirect:")) {
                    map.clear();
                }
                return view;
            }
        }
        return "ubuilder/view";
    }
    
    @RequestMapping({"/ulogin/(*:appId)/(*:userviewId)/(~:key)","/ulogin/(*:appId)/(*:userviewId)","/ulogin/(*:appId)/(*:userviewId)/(*:key)/(*:menuId)"})
    public String login(ModelMap map, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam("userviewId") String userviewId, @RequestParam(value = "menuId", required = false) String menuId, @RequestParam(value = "key", required = false) String key, @RequestParam(value = "embed", required = false) Boolean embed) throws Exception {
        if (embed == null) {
            embed = false;
        }
        return embedLogin(map, request, response, appId, userviewId, menuId, key, embed);
    }

    @RequestMapping({"/embed/ulogin/(*:appId)/(*:userviewId)/(~:key)","/embed/ulogin/(*:appId)/(*:userviewId)","/embed/ulogin/(*:appId)/(*:userviewId)/(*:key)/(*:menuId)"})
    public String embedLogin(ModelMap map, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam("userviewId") String userviewId, @RequestParam(value = "menuId", required = false) String menuId, @RequestParam(value = "key", required = false) String key, Boolean embed) throws Exception {
        if (embed == null) {
            embed = true;
        }
        
        //check for empty key
        if (key != null && key.equals(Userview.USERVIEW_KEY_EMPTY_VALUE)) {
            key = null;
        }
        
        // validate input
        SecurityUtil.validateStringInput(appId);        
        SecurityUtil.validateStringInput(menuId);        
        SecurityUtil.validateStringInput(key);
        SecurityUtil.validateBooleanInput(embed);

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
        map.addAttribute("appId", appDef.getId());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("key", key);
        map.addAttribute("menuId", menuId);
        map.addAttribute("embed", embed);
        map.addAttribute("queryString", request.getQueryString());
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
    }
}
