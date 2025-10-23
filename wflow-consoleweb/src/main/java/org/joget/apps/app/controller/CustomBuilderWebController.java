package org.joget.apps.app.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.CustomBuilder;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.CustomBuilderUtil;
import org.joget.apps.ext.ConsoleWebPlugin;
import org.joget.apps.workflow.security.EnhancedWorkflowUserManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CustomBuilderWebController {
    
    @Autowired
    Validator validator;
    @Autowired
    AppService appService;
    @Autowired
    BuilderDefinitionDao builderDefinitionDao;
    @Autowired
    PluginManager pluginManager;
    @Autowired
    SetupManager setupManager;
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/cbuilders")
    public void consoleBuildersJson(Writer writer, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        blank.put("type", "");
        jsonArray.put(blank);
        for (CustomBuilder cb : CustomBuilderUtil.getBuilderList().values()) {
            Map data = new HashMap();
            data.put("value", cb.getObjectName());
            data.put("label", cb.getObjectLabel());
            data.put("icon", cb.getIcon());
            jsonArray.put(data);
        }
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/cbuilderAllOptions")
    public void consoleBuilderAllOptionsJson(Writer writer, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        Collection<BuilderDefinition> builderDefinitionList = null;

        if (sort == null) {
            sort = "name";
            desc = false;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        builderDefinitionList = builderDefinitionDao.getList(appDef, sort, desc, start, rows);

        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        blank.put("type", "");
        jsonArray.put(blank);
        for (BuilderDefinition def : builderDefinitionList) {
            Map data = new HashMap();
            data.put("value", def.getId());
            data.put("label", def.getName());
            data.put("type", def.getType());
            jsonArray.put(data);
        }
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/cbuilder/(*:type)/list")
    public void consoleBuilderListJson(Writer writer, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "type") String type, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        if (!CustomBuilderUtil.hasBuilder(type)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
            
        Collection<BuilderDefinition> builderDefinitionList = null;
        Long count = null;

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        builderDefinitionList = builderDefinitionDao.getBuilderDefinitionList(type, name, appDef, sort, desc, start, rows);
        count = builderDefinitionDao.getBuilderDefinitionListCount(type, name, appDef);

        JSONObject jsonObject = new JSONObject();
        for (BuilderDefinition def : builderDefinitionList) {
            Map data = new HashMap();
            data.put("id", def.getId());
            data.put("name", def.getName());
            data.put("type", def.getType());
            data.put("dateCreated", TimeZoneUtil.convertToTimeZone(def.getDateCreated(), null, AppUtil.getAppDateFormat()));
            data.put("dateModified", TimeZoneUtil.convertToTimeZone(def.getDateModified(), null, AppUtil.getAppDateFormat()));
            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", count);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/cbuilder/(*:type)/options")
    public void consoleBuilderOptionsJson(Writer writer, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "type") String type, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "name", required = false) String name, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        if (!CustomBuilderUtil.hasBuilder(type)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        Collection<BuilderDefinition> builderDefinitionList = null;

        if (sort == null) {
            sort = "name";
            desc = false;
        }
        AppDefinition appDef = null;
        if (version == null || version.isEmpty()) {
            Long appVersion = appService.getPublishedVersion(appId);
            if (appVersion != null) {
                version = appVersion.toString();
            }
        }
        appDef = appService.getAppDefinition(appId, version);
        if (appDef != null) {
            builderDefinitionList = builderDefinitionDao.getBuilderDefinitionList(type, null, appDef, sort, desc, start, rows);
        } else {
            builderDefinitionList = new ArrayList<BuilderDefinition>();
        }

        JSONArray jsonArray = new JSONArray();
        Map blank = new HashMap();
        blank.put("value", "");
        blank.put("label", "");
        jsonArray.put(blank);
        for (BuilderDefinition def : builderDefinitionList) {
            Map data = new HashMap();
            data.put("value", def.getId());
            data.put("label", def.getName());
            jsonArray.put(data);
        }
        AppUtil.writeJson(writer, jsonArray, callback);
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/cbuilder/(*:type)/create")
    public String consoleBuilderCreate(ModelMap model, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "type") String type) throws IOException {
        CustomBuilder builder = CustomBuilderUtil.getBuilder(type);
        if (builder == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        model.addAttribute("appId", appDef.getId());
        model.addAttribute("appVersion", appDef.getVersion());
        model.addAttribute("appDefinition", appDef);
        model.addAttribute("builder", builder);
        
        BuilderDefinition builderDefinition = new BuilderDefinition();
        model.addAttribute("builderDefinition", builderDefinition);
        
        Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
        model.addAttribute("appList", appDefinitionList);
        
        return "cbuilder/create";
    }
    
    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/cbuilder/(*:type)/submit", method = RequestMethod.POST)
    public String consoleBuilderSubmit(ModelMap model, HttpServletResponse response, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam(value = "type") String type, @ModelAttribute("builderDefinition") BuilderDefinition builderDefinition, BindingResult result, @RequestParam(value = "copyAppId", required = false) String copyAppId, @RequestParam(value = "copyId", required = false) String copyId) throws IOException {
        CustomBuilder builder = CustomBuilderUtil.getBuilder(type);
        if (builder == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        BuilderDefinition copy = null;
        if (copyAppId != null && !copyAppId.isEmpty() && copyId != null && !copyId.isEmpty()) {
            Long copyVersion = appService.getPublishedVersion(copyAppId);
            AppDefinition copyAppDef = appService.getAppDefinition(copyAppId, (copyVersion != null)?copyVersion.toString():null);
            copy = builderDefinitionDao.loadById(copyId, copyAppDef);
        }
        
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        model.addAttribute("appId", appDef.getId());
        model.addAttribute("appVersion", appDef.getVersion());
        model.addAttribute("appDefinition", appDef);
        model.addAttribute("builder", builder);
        
        builderDefinition.setId(builder.getIdPrefix() + builderDefinition.getId());
        builderDefinition.setType(type);
        
        // validation
        validator.validate(builderDefinition, result);
        builderDefinition.setAppDefinition(appDef);

        boolean invalid = result.hasErrors();
        if (!invalid) {
            // check error
            Collection<String> errors = new ArrayList<String>();

            // check exist
            if (builderDefinitionDao.loadById(builderDefinition.getId(), appDef) != null) {
                errors.add("console.datalist.error.label.exists");
            } else {
                String json = builder.createNewJSON(builderDefinition.getId(), builderDefinition.getName(), builderDefinition.getDescription(), copy);
                builderDefinition.setJson(json);
                invalid = !builderDefinitionDao.add(builderDefinition);
            }

            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                invalid = true;
            }
        }
        
        if (invalid) {
            builderDefinition.setId(builderDefinition.getId().substring(builder.getIdPrefix().length()));
        }

        model.addAttribute("builderDefinition", builderDefinition);

        if (invalid) {
            Collection<AppDefinition> appDefinitionList = appService.getUnprotectedAppList();
            model.addAttribute("appList", appDefinitionList);
        
            return "cbuilder/create";
        } else {
            return "cbuilder/saved";
        }
    }
    
    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/cbuilder/(*:type)/delete", method = RequestMethod.POST)
    public String consoleBuilderDelete(HttpServletResponse response, @RequestParam(value = "ids") String ids, @RequestParam String appId, @RequestParam(required = false) String version, @RequestParam(value = "type") String type) throws IOException {
        CustomBuilder builder = CustomBuilderUtil.getBuilder(type);
        if (builder == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        StringTokenizer strToken = new StringTokenizer(ids, ",");
        while (strToken.hasMoreTokens()) {
            String id = (String) strToken.nextElement();
            builderDefinitionDao.delete(id, appDef);
        }
        return "console/dialogClose";
    }
    
    @RequestMapping({"/json/console/app/(*:appId)/(~:version)/cbuilder/(*:type)/json/(*:id)"})
    public void getJson(Writer writer, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "type") String type, @RequestParam(value = "id") String id) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        CustomBuilder builder = CustomBuilderUtil.getBuilder(type);
        if (builder == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        BuilderDefinition builderDefinition = builderDefinitionDao.loadById(id, appDef);
        if (builderDefinition == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String json = builderDefinition.getJson();
        writer.write(PropertyUtil.propertiesJsonLoadProcessing(json));
    }
    
    @RequestMapping({"/json/console/app/(*:appId)/(~:version)/check"})
    public void checkPermission(Writer writer, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException {
        try {
            JSONObject obj = new JSONObject();
            if (WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN) || EnhancedWorkflowUserManager.isAppAdminRole()) {
                obj.put("status", true);
            } else {
                obj.put("status", false);
            }
            AppUtil.writeJson(writer, obj, null);
        } catch (Exception e) {
            LogUtil.error(CustomBuilderWebController.class.getName(), e, "");
        }
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:appVersion)/cbuilder/(*:type)/design/(*:id)")
    public String builder(ModelMap map, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam(value = "type") String type, @RequestParam("id") String id, @RequestParam(required = false) String json) throws Exception {
        CustomBuilder builder = CustomBuilderUtil.getBuilder(type);
        if (builder == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        // verify app license
        ConsoleWebPlugin consoleWebPlugin = (ConsoleWebPlugin)pluginManager.getPlugin(ConsoleWebPlugin.class.getName());
        String page = consoleWebPlugin.verifyAppVersion(appId, appVersion);
        if (page != null) {
            return page;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);
        map.addAttribute("builder", builder);

        BuilderDefinition builderDefinition = builderDefinitionDao.loadById(id, appDef);

        String builderJson = null;
        if (json != null && !json.trim().isEmpty()) {
            try {
                // validate JSON
                new JSONObject(json);

                // read custom JSON from request
                builderJson = json;
            } catch (JSONException ex) {
                builderJson = "{}";
            }
        } else {
            // get JSON from form definition
            builderJson = builderDefinition.getJson();
        }

        map.addAttribute("id", id);
        map.addAttribute("builderDefinition", builderDefinition);
        map.addAttribute("json", PropertyUtil.propertiesJsonLoadProcessing(builderJson));
        
        map.addAttribute("builderHTML", builder.getBuilderHTML(builderDefinition, json, request, response));
        
        String systemTheme = setupManager.getSettingValue("systemTheme");
        map.addAttribute("systemTheme", systemTheme);
        
        response.addHeader("X-XSS-Protection", "0");

        return "cbuilder/builder";
    }
    
    @RequestMapping(value = "/console/app/(*:appId)/(~:appVersion)/cbuilder/(*:type)/save/(*:id)", method = RequestMethod.POST)
    @Transactional
    public String save(Writer writer, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam("id") String id, @RequestParam(value = "type") String type, @RequestParam(value = "json", required = false) String json) throws Exception {
        CustomBuilder builder = CustomBuilderUtil.getBuilder(type);
        if (builder == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        // verify app version
        ConsoleWebPlugin consoleWebPlugin = (ConsoleWebPlugin)pluginManager.getPlugin(ConsoleWebPlugin.class.getName());
        String page = consoleWebPlugin.verifyAppVersion(appId, appVersion);
        if (page != null) {
            return page;
        }
        
        //retrieve from request body if the json is send in file
        json = AppUtil.getSubmittedJsonDefinition(json);

        JSONObject jsonObject = new JSONObject();

        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
        BuilderDefinition def = builderDefinitionDao.loadById(id, appDef);
        def.setName(builder.getNameFromJSON(json));
        def.setDescription(builder.getDescriptionFromJSON(json));
        def.setJson(PropertyUtil.propertiesJsonStoreProcessing(def.getJson(), json));

        boolean success = builderDefinitionDao.update(def);
        jsonObject.put("success", success);
        jsonObject.put("data", PropertyUtil.propertiesJsonLoadProcessing(def.getJson()));

        jsonObject.write(writer);
        return null;
    }

    @RequestMapping(value = "/console/app/(*:appId)/(~:appVersion)/cbuilder/(*:type)/preview/(*:id)", method = RequestMethod.POST)
    public void preview(HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam(value = "type") String type, @RequestParam("id") String id, @RequestParam("json") String json) throws Exception {
        CustomBuilder builder = CustomBuilderUtil.getBuilder(type);
        if (builder == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // get app definition so that it's set in the current thread
        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
        
        String tempJson = json;
        if (tempJson.contains(SecurityUtil.ENVELOPE) || tempJson.contains(PropertyUtil.PASSWORD_PROTECTED_VALUE)) {
            BuilderDefinition def = builderDefinitionDao.loadById(id, appDef);

            if (def != null) {
                tempJson = PropertyUtil.propertiesJsonStoreProcessing(def.getJson(), tempJson);
            }
        }
        
        response.addHeader("X-XSS-Protection", "0");

        builder.builderPreview(tempJson, request, response);
    }
}