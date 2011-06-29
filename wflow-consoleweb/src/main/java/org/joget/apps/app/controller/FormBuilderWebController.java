package org.joget.apps.app.controller;

import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FormBuilderWebController {

    @Autowired
    FormService formService;
    @Autowired
    FormBuilderPalette formBuilderPalette;
    @Autowired
    AppService appService;
    @Autowired
    FormDefinitionDao formDefinitionDao;

    @RequestMapping("/console/app/(*:appId)/(*:version)/form/builder/(*:formId)")
    public String formBuilder(ModelMap model, @RequestParam("appId") String appId, @RequestParam("version") String version, @RequestParam("formId") String formId, @RequestParam(required = false) String json) {
        // load form definition
        model.addAttribute("appId", appId);
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        FormDefinition formDef = null;
        if (appDef == null) {
            // TODO: handle invalid app
        } else {
            model.addAttribute("appDefinition", appDef);
            formDef = formDefinitionDao.loadById(formId, appDef);
        }

        if (formDef != null) {
            String formJson = null;
            if (json != null && !json.trim().isEmpty()) {
                // read custom JSON from request
                formJson = json;
            } else {
                // get JSON from form definition
                formJson = formDef.getJson();
            }
            if (formJson != null && formJson.trim().length() > 0) {
                String elementHtml = formService.previewElement(formJson);
                model.addAttribute("elementHtml", elementHtml);
                model.addAttribute("elementJson", formJson);
            } else {
                // default empty form
                String tableName = formDef.getTableName();
                if (tableName == null || tableName.isEmpty()) {
                    tableName = formDef.getId();
                }
                String defaultJson = "{className: 'org.joget.apps.form.model.Form',  \"properties\":{ \"id\":\"" + formId + "\", \"name\":\"" + formDef.getName() + "\", \"tableName\":\"" + tableName + "\", \"loadBinder\":{ \"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\" }, \"storeBinder\":{ \"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\" } }}";
                String formHtml = formService.previewElement(defaultJson);
                model.addAttribute("elementHtml", formHtml);
            }
        } else {
            // default empty form
            String formJson = "{className: 'org.joget.apps.form.model.Form',  \"properties\":{ \"id\":\"" + formId + "\", \"name\":\"\" \"loadBinder\":{ \"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\" }, \"storeBinder\":{ \"className\":\"org.joget.apps.form.lib.WorkflowFormBinder\" } }}";
            String formHtml = formService.previewElement(formJson);
            model.addAttribute("elementHtml", formHtml);
        }

        // add palette
        model.addAttribute("palette", formBuilderPalette);

        // add form def id
        model.addAttribute("formId", formId);
        model.addAttribute("formDef", formDef);

        return "fbuilder/formBuilder";
    }

    @RequestMapping("/fbuilder/app/(*:appId)/(*:appVersion)/form/preview/")
    public String previewForm(ModelMap model, @RequestParam("appId") String appId, @RequestParam("appVersion") String appVersion, @RequestParam("json") String json) {

        model.addAttribute("appId", appId);
        model.addAttribute("appVersion", appVersion);
        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);

        String elementHtml = formService.previewElement(json, false);
        model.addAttribute("elementTemplate", elementHtml);
        model.addAttribute("elementJson", json);

        return "fbuilder/previewForm";
    }

    @RequestMapping("/fbuilder/element/preview")
    public String previewElement(ModelMap model, @RequestParam("json") String json) {

        String elementHtml = formService.previewElement(json);
        model.addAttribute("elementTemplate", elementHtml);
        model.addAttribute("elementJson", json);

        return "fbuilder/previewElement";
    }
}
