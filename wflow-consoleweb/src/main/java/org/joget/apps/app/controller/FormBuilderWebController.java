package org.joget.apps.app.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.form.lib.HiddenField;
import org.joget.apps.form.lib.SubmitButton;
import org.joget.apps.form.model.Column;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.FormStoreBinder;
import org.joget.apps.form.model.Section;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.PluginManager;
import org.json.JSONException;
import org.json.JSONObject;
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
    PluginManager pluginManager;
    @Autowired
    FormDefinitionDao formDefinitionDao;

    @RequestMapping("/console/app/(*:appId)/(~:version)/form/builder/(*:formId)")
    public String formBuilder(ModelMap model, @RequestParam("appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("formId") String formId, @RequestParam(required = false) String json) {
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

    @RequestMapping("/fbuilder/app/(*:appId)/(~:appVersion)/form/preview/")
    public String previewForm(ModelMap model, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam("json") String json) {

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

    @RequestMapping("/form/embed")
    public String embedForm(ModelMap model, HttpServletRequest request, @RequestParam("_submitButtonLabel") String buttonLabel, @RequestParam("_json") String json, @RequestParam("_callback") String callback, @RequestParam("_setting") String callbackSetting, @RequestParam(required = false) String id, @RequestParam(value = "_a", required = false) String action) throws JSONException {
        FormData formData = new FormData();
        if(id != null && !id.isEmpty()){
            formData.setPrimaryKeyValue(id);
        }
        Form form = formService.loadFormFromJson(json, formData);

        if(callbackSetting == null || (callbackSetting != null && callbackSetting.isEmpty())){
            callbackSetting = "{}";
        }

        form.setProperty("url", "?_a=submit&_callback="+callback+"&_setting="+StringEscapeUtils.escapeHtml(callbackSetting)+"&_submitButtonLabel="+StringEscapeUtils.escapeHtml(buttonLabel));

        if(form != null){
            //if id field not exist, automatically add an id hidden field
            Element idElement = FormUtil.findElement(FormUtil.PROPERTY_ID, form, formData);
            if (idElement == null) {
                Collection<Element> formElements = form.getChildren();
                idElement = new HiddenField();
                idElement.setProperty(FormUtil.PROPERTY_ID, FormUtil.PROPERTY_ID);
                idElement.setParent(form);
                formElements.add(idElement);
            }
            
            // create new section for buttons
            Section section = new Section();
            section.setProperty(FormUtil.PROPERTY_ID, "section-actions");
            Collection<Element> sectionChildren = new ArrayList<Element>();
            section.setChildren(sectionChildren);
            Collection<Element> formChildren = form.getChildren(formData);
            if (formChildren == null) {
                formChildren = new ArrayList<Element>();
            }
            formChildren.add(section);

            // add new horizontal column to section
            Column column = new Column();
            column.setProperty("horizontal", "true");
            Collection<Element> columnChildren = new ArrayList<Element>();
            column.setChildren(columnChildren);
            sectionChildren.add(column);

            Element hiddenField = (Element) pluginManager.getPlugin(HiddenField.class.getName());
            hiddenField.setProperty(FormUtil.PROPERTY_ID, "_json");
            hiddenField.setProperty(FormUtil.PROPERTY_VALUE, StringEscapeUtils.escapeHtml(json));
            columnChildren.add((Element) hiddenField);

            Element submitButton = (Element) pluginManager.getPlugin(SubmitButton.class.getName());
            submitButton.setProperty(FormUtil.PROPERTY_ID, "submit");
            submitButton.setProperty("label", buttonLabel);
            columnChildren.add((Element) submitButton);
        }

        // generate form HTML
        String formHtml = null;

        if("submit".equals(action)){
            formData = formService.retrieveFormDataFromRequest(formData, request);
            formData = formService.executeFormActions(form, formData);

            // check for validation errors
            Map<String, String> errors = formData.getFormErrors();
            int errorCount = 0;
            if (errors == null || errors.isEmpty()) {
                // render normal template
                formHtml = formService.generateElementHtml(form, formData);
            } else {
                // render error template
                formHtml = formService.generateElementErrorHtml(form, formData);
                errorCount = errors.size();
            }

            //convert submitted 
            JSONObject jsonResult = new JSONObject();
            for(FormStoreBinder binder: formData.getStoreBinders()){
                FormRowSet rows = formData.getStoreBinderData(binder);
                for(FormRow row : rows){
                    for(Object o : row.keySet()){
                        jsonResult.accumulate(o.toString(), row.get(o));
                    }
                    Map<String, String> tempFilePathMap = row.getTempFilePathMap();
                    if (tempFilePathMap != null && !tempFilePathMap.isEmpty()) {
                        jsonResult.put(FormUtil.PROPERTY_TEMP_FILE_PATH, tempFilePathMap);
                    }
                }
            }

            model.addAttribute("jsonResult", StringEscapeUtils.escapeJavaScript(jsonResult.toString()));
            model.addAttribute("setting", callbackSetting);
            model.addAttribute("callback", callback);
            model.addAttribute("submitted", Boolean.TRUE);
            model.addAttribute("errorCount", errorCount);
        }else{
            formHtml = formService.retrieveFormHtml(form, formData);
        }

        model.addAttribute("formHtml", formHtml);
        return "fbuilder/embedForm";
    }
}
