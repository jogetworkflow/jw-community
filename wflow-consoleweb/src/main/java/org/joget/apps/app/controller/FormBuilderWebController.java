package org.joget.apps.app.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.ext.ConsoleWebPlugin;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.dao.FormDataDaoImpl;
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
import org.joget.apps.form.service.CustomFormDataTableUtil;
import org.joget.apps.form.service.FormERD;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    @Autowired
    FormDataDao formDataDao;

    @RequestMapping("/console/app/(*:appId)/(~:version)/form/builder/(*:formId)")
    public String formBuilder(ModelMap model, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("formId") String formId, @RequestParam(required = false) String json) {
        // verify app version
        ConsoleWebPlugin consoleWebPlugin = (ConsoleWebPlugin)pluginManager.getPlugin(ConsoleWebPlugin.class.getName());
        String page = consoleWebPlugin.verifyAppVersion(appId, version);
        if (page != null) {
            return page;
        }
        
        // set flag in request
        FormUtil.setFormBuilderActive(true);

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
                try {
                    // validate JSON
                    new JSONObject(json);

                    // read custom JSON from request
                    formJson = json;
                } catch (JSONException ex) {
                    formJson = "{}";
                }
            } else {
                // get JSON from form definition
                formJson = formDef.getJson();
            }
            if (formJson != null && formJson.trim().length() > 0) {
                String processedformJson = PropertyUtil.propertiesJsonLoadProcessing(formJson);
                
                try {
                    FormUtil.setProcessedFormJson(processedformJson);
                    String elementHtml = formService.previewElement(formJson);
                    model.addAttribute("elementHtml", elementHtml);
                    model.addAttribute("elementJson", processedformJson);
                } finally {
                    FormUtil.clearProcessedFormJson();
                }
            } else {
                // default empty form
                String defaultJson = FormUtil.generateDefaultForm(formId, formDef);
                String formHtml = formService.previewElement(defaultJson);
                model.addAttribute("elementHtml", formHtml);
            }
        } else {
            // default empty form
            String formJson = FormUtil.generateDefaultForm(formId, null);
            String formHtml = formService.previewElement(formJson);
            model.addAttribute("elementHtml", formHtml);
        }

        // add palette
        model.addAttribute("palette", formBuilderPalette);

        // add form def id
        model.addAttribute("formId", formId);
        model.addAttribute("formDef", formDef);
        
        response.addHeader("X-XSS-Protection", "0");
        
        return "fbuilder/formBuilder";
    }
    
    @RequestMapping(value = "/fbuilder/app/(*:appId)/(~:appVersion)/form/(*:formId)/save", method = RequestMethod.POST)
    @Transactional
    public String save(Writer writer, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String version, @RequestParam("formId") String formId, @RequestParam("json") String json) throws Exception {
        // verify app license
        ConsoleWebPlugin consoleWebPlugin = (ConsoleWebPlugin)pluginManager.getPlugin(ConsoleWebPlugin.class.getName());
        String page = consoleWebPlugin.verifyAppVersion(appId, version);
        if (page != null) {
            return page;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        // load existing form definition and update fields
        FormDefinition formDef = formDefinitionDao.loadById(formId, appDef);
        Form form = (Form) formService.createElementFromJson(json);
        formDef.setName(form.getPropertyString("name"));
        formDef.setTableName(form.getPropertyString("tableName"));
        formDef.setJson(PropertyUtil.propertiesJsonStoreProcessing(formDef.getJson(), json));
        formDef.setDescription(form.getPropertyString("description"));

        // update
        boolean success = formDefinitionDao.update(formDef);
        formDataDao.clearFormCache(form);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", success);
        jsonObject.put("data", PropertyUtil.propertiesJsonLoadProcessing(formDef.getJson()));
        jsonObject.write(writer);
        return null;
    }

    @RequestMapping("/fbuilder/app/(*:appId)/(~:appVersion)/form/(*:formId)/preview/")
    public String previewForm(ModelMap model, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam("formId") String formId, @RequestParam("json") String json) {
        try {
            FormUtil.setProcessedFormJson(json);

            model.addAttribute("appId", appId);
            model.addAttribute("appVersion", appVersion);
            AppDefinition appDef = appService.getAppDefinition(appId, appVersion);

            String tempJson = json;
            if (tempJson.contains(SecurityUtil.ENVELOPE) || tempJson.contains(PropertyUtil.PASSWORD_PROTECTED_VALUE)) {
                FormDefinition formDef = formDefinitionDao.loadById(formId, appDef);

                if (formDef != null) {
                    tempJson = PropertyUtil.propertiesJsonStoreProcessing(formDef.getJson(), tempJson);
                }
            }

            String elementHtml = formService.previewElement(tempJson, false);
            model.addAttribute("elementTemplate", elementHtml);
            model.addAttribute("elementJson", json);

        } finally {
            FormUtil.clearProcessedFormJson();
        }
        
        response.addHeader("X-XSS-Protection", "0");
        
        return "fbuilder/previewForm";
    }

    @RequestMapping("/fbuilder/app/(*:appId)/(~:appVersion)/form/(*:formId)/element/preview")
    public String previewElement(ModelMap model, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam("formId") String formId, @RequestParam("json") String json) {
        try {
            // set flag in request
            FormUtil.setFormBuilderActive(true);
        
            FormUtil.setProcessedFormJson(json);

            model.addAttribute("appId", appId);
            model.addAttribute("appVersion", appVersion);
            AppDefinition appDef = appService.getAppDefinition(appId, appVersion);

            String tempJson = json;
            if (tempJson.contains(SecurityUtil.ENVELOPE) || tempJson.contains(PropertyUtil.PASSWORD_PROTECTED_VALUE)) {
                FormDefinition formDef = formDefinitionDao.loadById(formId, appDef);

                if (formDef != null) {
                    tempJson = PropertyUtil.propertiesJsonStoreProcessing(formDef.getJson(), tempJson);
                }
            }

            String elementHtml = formService.previewElement(tempJson);
            model.addAttribute("elementTemplate", elementHtml);
            model.addAttribute("elementJson", json);
        } finally {
            FormUtil.clearProcessedFormJson();
        }
        
        response.addHeader("X-XSS-Protection", "0");
        
        return "fbuilder/previewElement";
    }
    
    @RequestMapping("/app/(*:appId)/(~:appVersion)/form/embed")
    public String appEmbedForm(ModelMap model, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam("_submitButtonLabel") String buttonLabel, @RequestParam("_json") String json, @RequestParam("_callback") String callback, @RequestParam("_setting") String callbackSetting, @RequestParam(required = false) String id, @RequestParam(value = "_a", required = false) String action) throws JSONException, UnsupportedEncodingException {
        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
        
        if (appDef == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        AppUtil.setCurrentAppDefinition(appDef);
        return embedForm(model, request, response, buttonLabel, json, callback, callbackSetting, id, action);
    }

    @RequestMapping("/form/embed")
    public String embedForm(ModelMap model, HttpServletRequest request, HttpServletResponse response, @RequestParam("_submitButtonLabel") String buttonLabel, @RequestParam("_json") String json, @RequestParam("_callback") String callback, @RequestParam("_setting") String callbackSetting, @RequestParam(required = false) String id, @RequestParam(value = "_a", required = false) String action) throws JSONException, UnsupportedEncodingException {

        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String appId  = "";
        String appVersion = "";
        if (appDef != null) {
            appId = appDef.getAppId();
            appVersion = appDef.getVersion().toString();
        }        

        String nonce = request.getParameter("_nonce");
        if (!SecurityUtil.verifyNonce(nonce, new String[]{"EmbedForm", appId, appVersion, json})) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        FormData formData = new FormData();
        if(id != null && !id.isEmpty()){
            formData.setPrimaryKeyValue(id);
        }
        formData = formService.retrieveFormDataFromRequest(formData, request);
        String decryptedJson = SecurityUtil.decrypt(json);
        Form form = formService.loadFormFromJson(decryptedJson, formData);

        if (form == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        
        if(callbackSetting == null || callbackSetting.isEmpty()){
            callbackSetting = "{}";
        }
        String encodedCallbackSetting = URLEncoder.encode(StringEscapeUtils.escapeHtml(callbackSetting), "UTF-8");

        String csrfToken = SecurityUtil.getCsrfTokenName() + "=" + SecurityUtil.getCsrfTokenValue(request);
        form.setProperty("url", "?_nonce="+URLEncoder.encode(nonce, "UTF-8")+"&_a=submit&_callback="+callback+"&_setting="+encodedCallbackSetting+"&_submitButtonLabel="+StringEscapeUtils.escapeHtml(buttonLabel) + "&" + csrfToken);

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
        hiddenField.setProperty(FormUtil.PROPERTY_VALUE, json);
        columnChildren.add((Element) hiddenField);

        Element submitButton = (Element) pluginManager.getPlugin(SubmitButton.class.getName());
        submitButton.setProperty(FormUtil.PROPERTY_ID, "submit");
        submitButton.setProperty("label", buttonLabel);
        columnChildren.add((Element) submitButton);

        model.addAttribute("readonly", FormUtil.isReadonly(form, formData));

        // generate form HTML
        String formHtml = null;

        if("submit".equals(action)){
            formData = formService.executeFormActions(form, formData);

            // check for validation errors
            Map<String, String> errors = formData.getFormErrors();
            int errorCount = 0;
            if (formData.getStay() && (errors == null || errors.isEmpty())) {
                // render normal template
                formHtml = formService.generateElementHtml(form, formData);
            } else if (!formData.getStay() && (errors == null || errors.isEmpty())) {
                //convert submitted 
                JSONObject jsonResult = new JSONObject();
                
                //get binder of main form
                FormStoreBinder mainBinder = form.getStoreBinder();
                FormRowSet rows = formData.getStoreBinderData(mainBinder);
                
                for (FormRow row : rows) {
                    Map<String, String> decrypted = new HashMap<String, String>();
                    
                    for (Object o : row.keySet()) {
                        Object value = row.get(o);
                        jsonResult.accumulate(o.toString(), value);
                        if (value instanceof String && SecurityUtil.hasSecurityEnvelope(value.toString())) {
                            decrypted.put(o.toString(), SecurityUtil.decrypt(value.toString()));
                        }
                    }
                    Map<String, String[]> tempFilePathMap = row.getTempFilePathMap();
                    if (tempFilePathMap != null && !tempFilePathMap.isEmpty()) {
                        jsonResult.put(FormUtil.PROPERTY_TEMP_FILE_PATH, tempFilePathMap);
                    }
                    Map<String, String[]> deleteFilePathMap = row.getDeleteFilePathMap();
                    if (deleteFilePathMap != null && !deleteFilePathMap.isEmpty()) {
                        jsonResult.put(FormUtil.PROPERTY_DELETE_FILE_PATH, deleteFilePathMap);
                    }
                    if (!decrypted.isEmpty()) {
                        jsonResult.put(FormUtil.PROPERTY_DECRYPTED_DATA, decrypted);
                    }
                }
                
                //should use the original request param map instead of the one modified by other form processing in form data. Retrieve it again from request 
                FormData newFormData = new FormData();
                newFormData = formService.retrieveFormDataFromRequest(newFormData, request);
                Map<String, String[]> requestParams = newFormData.getRequestParams();
                if (requestParams != null && !requestParams.isEmpty()) {
                    requestParams.remove("_json");
                    jsonResult.put(FormUtil.PROPERTY_TEMP_REQUEST_PARAMS, requestParams);
                }
                
                model.addAttribute("jsonResult", StringEscapeUtils.escapeJavaScript(jsonResult.toString()));
            } else if (!errors.isEmpty()) {
                // render error template
                formHtml = formService.generateElementErrorHtml(form, formData);
                errorCount = errors.size();
            }
            
            model.addAttribute("setting", callbackSetting);
            model.addAttribute("callback", callback);
            model.addAttribute("submitted", Boolean.TRUE);
            model.addAttribute("errorCount", errorCount);
            model.addAttribute("stay", formData.getStay());
        }else{
            formHtml = formService.retrieveFormHtml(form, formData);
        }

        model.addAttribute("formHtml", formHtml);
        
        if (request.getParameter("_mapp") != null) {
            String origin = request.getHeader("Origin");
            if (origin != null) {
                origin = origin.replace("\n", "").replace("\r", "");
            }
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Content-type", "application/xml");
        
            return "mapp/embedForm";
        } else {    
            return "fbuilder/embedForm";
        }
    }
    
    @RequestMapping("/json/app/(*:appId)/(~:appVersion)/form/options")
    public void formAjaxOptions(Writer writer, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam("_dv") String dependencyValue, @RequestParam("_n") String nonce, @RequestParam("_bd") String binderData) throws JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
        FormRowSet rowSet = FormUtil.getAjaxOptionsBinderData(dependencyValue, appDef, nonce, binderData);
        
        JSONArray jsonArray = new JSONArray();
        if (rowSet != null) {
            for (FormRow row : rowSet) {
                Map<String, String> data = new HashMap<String, String>();
                data.put(FormUtil.PROPERTY_LABEL, (String) row.getProperty(FormUtil.PROPERTY_LABEL));
                data.put(FormUtil.PROPERTY_VALUE, (String) row.getProperty(FormUtil.PROPERTY_VALUE));
                if (row.containsKey(FormUtil.PROPERTY_SELECTED)) {
                    data.put(FormUtil.PROPERTY_SELECTED, row.getProperty(FormUtil.PROPERTY_SELECTED));
                }
                jsonArray.put(data);
            }
        }
        
        jsonArray.write(writer);
    }
    
    @RequestMapping("/json/app/(*:appId)/(~:version)/form/erd")
    public void formERD(Writer writer, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef != null) {
            FormERD erd = new FormERD(appDef);
            writer.write(erd.getJson());
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "App does not exist.");
        }
    }
    
    @RequestMapping("/json/app/(*:appId)/(~:appVersion)/form/(*:formId)/columns")
    public void formAjaxColumns(Writer writer, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam("formId") String formId) throws JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
        JSONArray jsonArray = new JSONArray();
        try {
            Collection<Map<String, String>> columns = FormUtil.getFormColumns(appDef, formId);
            for (Map c : columns) {        
                jsonArray.put(c);
            }
        } catch (Exception e) {
        }

        jsonArray.write(writer);
    }
    
    @RequestMapping(value = "/fbuilder/app/(*:appId)/(~:appVersion)/form/erd/indexes", method = RequestMethod.POST)
    public void saveIndexes(Writer writer, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String version, @RequestParam("indexes") String indexes) throws Exception {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "App does not exist.");
        }
        
        JSONObject obj = new JSONObject(indexes);
        Iterator keys = obj.keys();
        while (keys.hasNext()) {
            String tableName = SecurityUtil.validateStringInput((String) keys.next());
            JSONArray arr = obj.getJSONArray(tableName);
            String[] indexArray = new String[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                indexArray[i] = SecurityUtil.validateStringInput(arr.get(i).toString());
            }
            if (tableName.startsWith(FormDataDaoImpl.FORM_PREFIX_TABLE_NAME)) {
                tableName = tableName.substring(FormDataDaoImpl.FORM_PREFIX_TABLE_NAME.length());
            }
            CustomFormDataTableUtil.createTableIndexes(appDef, tableName, indexArray);
        }
    }
}
