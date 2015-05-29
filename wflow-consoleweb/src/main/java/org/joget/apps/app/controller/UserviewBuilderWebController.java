package org.joget.apps.app.controller;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.apps.userview.model.UserviewCategory;
import org.joget.apps.userview.model.UserviewSetting;
import org.joget.apps.userview.service.UserviewService;
import org.joget.apps.userview.service.UserviewThemeProcesser;
import org.joget.commons.util.SecurityUtil;
import org.joget.plugin.property.service.PropertyUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserviewBuilderWebController {

    @Autowired
    private UserviewService userviewService;
    @Autowired
    private UserviewBuilderPalette userviewBuilderPalette;
    @Autowired
    AppService appService;
    @Autowired
    UserviewDefinitionDao userviewDefinitionDao;

    @RequestMapping("/console/app/(*:appId)/(~:appVersion)/userview/builder/(*:userviewId)")
    public String builder(ModelMap map, HttpServletRequest request, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam("userviewId") String userviewId, @RequestParam(required = false) String json) throws Exception {
        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        UserviewDefinition userview = userviewDefinitionDao.loadById(userviewId, appDef);

        String userviewJson = null;
        if (json != null && !json.trim().isEmpty()) {
            // read custom JSON from request
            userviewJson = json;
        } else {
            // get JSON from form definition
            userviewJson = userview.getJson();
        }

        map.addAttribute("userviewId", userviewId);
        map.addAttribute("userview", userview);
        map.addAttribute("json", PropertyUtil.propertiesJsonLoadProcessing(userviewJson));

        map.addAttribute("setting", new UserviewSetting());
        map.addAttribute("category", new UserviewCategory());

        Map basicRequestParams = new HashMap();
        basicRequestParams.put("appId", appId);
        basicRequestParams.put("appVersion", appVersion);
        basicRequestParams.put("userviewId", userviewId);
        basicRequestParams.put("contextPath", request.getContextPath());

        map.addAttribute("menuTypeCategories", userviewBuilderPalette.getUserviewMenuCategoryMap(basicRequestParams));

        return "ubuilder/builder";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:appVersion)/userview/builderSave/(*:userviewId)", method = RequestMethod.POST)
    public void save(Writer writer, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam("userviewId") String userviewId, @RequestParam("json") String json) throws Exception {
        JSONObject jsonObject = new JSONObject();

        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
        UserviewDefinition userview = userviewDefinitionDao.loadById(userviewId, appDef);
        userview.setName(userviewService.getUserviewName(json));
        userview.setDescription(userviewService.getUserviewDescription(json));
        userview.setJson(PropertyUtil.propertiesJsonStoreProcessing(userview.getJson(), json));

        boolean success = userviewDefinitionDao.update(userview);
        jsonObject.accumulate("success", success);

        jsonObject.write(writer);
    }

    @RequestMapping(value = {"/console/app/(*:appId)/(~:appVersion)/userview/builderPreview/(*:userviewId)","/console/app/(*:appId)/(~:appVersion)/userview/builderPreview/(*:userviewId)/(*:menuId)"}, method = RequestMethod.POST)
    public String preview(ModelMap map, HttpServletRequest request, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam("userviewId") String userviewId, @RequestParam("json") String json, @RequestParam(value = "menuId", required = false) String menuId) throws Exception {
        // get app definition so that it's set in the current thread
        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        
        String tempJson = json;
        if (tempJson.contains(SecurityUtil.ENVELOPE) || tempJson.contains(PropertyUtil.PASSWORD_PROTECTED_VALUE)) {
            UserviewDefinition userview = userviewDefinitionDao.loadById(userviewId, appDef);

            if (userview != null) {
                tempJson = PropertyUtil.propertiesJsonStoreProcessing(userview.getJson(), tempJson);
            }
        }

        Userview userviewObject = userviewService.createUserview(tempJson, menuId, true, request.getContextPath(), request.getParameterMap(), null, false);
        UserviewThemeProcesser processer = new UserviewThemeProcesser(userviewObject, request);
        map.addAttribute("userview", userviewObject);
        map.addAttribute("processer", processer);
        map.addAttribute("json", json);
        return processer.getPreviewView();
    }
}