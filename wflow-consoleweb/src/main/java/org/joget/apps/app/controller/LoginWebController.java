package org.joget.apps.app.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.userview.service.UserviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.ui.AbstractProcessingFilter;
import org.springframework.security.ui.savedrequest.SavedRequest;
import org.springframework.security.util.UrlUtils;
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

    @RequestMapping("/login")
    public String login(ModelMap map, HttpServletRequest request, HttpServletResponse response) throws Exception {
        SavedRequest savedRequest = (SavedRequest) request.getSession().getAttribute(AbstractProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY);
        String savedUrl = "";
        if (savedRequest != null) {
            savedUrl = UrlUtils.getRequestUrl(savedRequest);
        } else if (request.getHeader("referer") != null) { //for userview logout
            savedUrl = request.getHeader("referer");
        }

        if (savedUrl.contains("/web/userview")) {
            savedUrl = savedUrl.substring(savedUrl.indexOf("/web/userview"));
            String[] urlKey = savedUrl.split("/");
            String appId = urlKey[3];
            String userviewId = urlKey[4];

            if (savedRequest == null) { //for userview logout
                return "redirect:/web/userview/" + appId + "/" + userviewId;
            }
        } else if (savedUrl.contains("/web/ulogin")) {
            savedUrl = savedUrl.substring(savedUrl.indexOf("/web/ulogin"));
            String[] urlKey = savedUrl.split("/");
            String appId = urlKey[3];
            String userviewId = urlKey[4];
            String key = null;
            if (urlKey.length > 5) {
                key = urlKey[5];
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
            UserviewDefinition userview = userviewDefinitionDao.loadById(userviewId, appDef);
            if (userview != null) {
                String json = userview.getJson();
                map.addAttribute("userview", userviewService.createUserview(json, null, false, request.getContextPath(), request.getParameterMap(), key));
            }

            return "ubuilder/login";
        }

        return "login";
    }

    @RequestMapping("/unauthorized")
    public String unauthorized(ModelMap map) {
        return "unauthorized";
    }
}
