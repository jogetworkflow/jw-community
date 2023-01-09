package org.joget.apps.app.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.service.DataListDecorator;
import org.joget.apps.datalist.service.DataListService;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.SecurityUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Data JSON API to cater to the loading, storing and deletion of Form and List data.
 */
@Controller
public class FormListDataJsonController {
    
    @Autowired
    AppService appService;
    
    @Autowired
    FormService formService;
    
    @Autowired
    FormDataDao formDataDao;
    
    @Autowired
    DataListService dataListService;

    @Autowired
    DatalistDefinitionDao datalistDefinitionDao;
    
    /**
     * Load a specific form record
     * GET /json/data/form/load/(*:appId)/(*:formId)/(*:primaryKeyValue)
     * curl -v -X POST -d "j_username=admin&j_password=admin" http://localhost:8080/jw/web/json/data/form/load/crm/crm_account/001
     * @param writer
     * @param response
     * @param appId
     * @param formId
     * @param primaryKeyValue
     * @param includeSubformData true to recursively include subform data
     * @param includeReferenceElements true to include data from reference elements e.g. selectbox, etc.
     * @param flatten true to flatten data into a one level key-value map
     * @param callback
     * @throws IOException
     * @throws JSONException 
     */
    @RequestMapping("/json/data/form/load/(*:appId)/(*:formId)/(*:primaryKeyValue)")
    public void formDataLoad(Writer writer, HttpServletResponse response, @RequestParam(value = "appId", required = true) String appId, @RequestParam(value = "formId", required = true) String formId, @RequestParam(value="primaryKeyValue", required = true) String primaryKeyValue, @RequestParam(value = "includeSubformData", required = false) String includeSubformData, @RequestParam(value = "includeReferenceElements", required = false) String includeReferenceElements, @RequestParam(value = "flatten", required = false) String flatten, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        AppDefinition appDef = appService.getPublishedAppDefinition(appId);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        formId = SecurityUtil.validateStringInput(formId);
        primaryKeyValue = SecurityUtil.validateStringInput(primaryKeyValue); 
        Map<String, Object> result = FormUtil.loadFormData(appDef.getId(), appDef.getVersion().toString(), formId, primaryKeyValue, Boolean.valueOf(includeSubformData), Boolean.valueOf(includeReferenceElements), Boolean.valueOf(flatten), null);
        if (result.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        JSONObject jsonObject = new JSONObject(result);
        AppUtil.writeJson(writer, jsonObject, callback);        
    }    

    /**
     * Create or update a form record
     * POST /json/data/form/store/(*:appId)/(*:formId)/(*:primaryKeyValue)
     * curl -v -X POST -d "j_username=admin&j_password=admin&accountName=001%Updated&address=001%20Address" http://localhost:8080/jw/web/json/data/form/store/crm//crm_account/001
     * curl -v --header "Authorization: Basic YWRtaW46YWRtaW4=" -F "accountName=001 Updated" -F "address=001 Address" http://localhost:8080/jw/web/json/data/form/store/crm//crm_account/001
     * @param writer
     * @param request
     * @param response
     * @param appId
     * @param formId
     * @param primaryKeyValue
     * @param callback
     * @throws IOException
     * @throws JSONException 
     */
    @RequestMapping(value="/json/data/form/store/(*:appId)/(*:formId)/(*:primaryKeyValue)", method=RequestMethod.POST)
    public void formDataStore(Writer writer, HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "appId", required = true) String appId, @RequestParam(value = "formId", required = true) String formId, @RequestParam(value="primaryKeyValue", required = true) String primaryKeyValue, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        AppDefinition appDef = appService.getPublishedAppDefinition(appId);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        formId = SecurityUtil.validateStringInput(formId);
        primaryKeyValue = SecurityUtil.validateStringInput(primaryKeyValue);        
        
        // retrieve form
        FormData formData = new FormData();
        formData.setPrimaryKeyValue(primaryKeyValue);
        formData = formService.retrieveFormDataFromRequest(formData, request);
        Form form = appService.viewDataForm(appDef.getId(), appDef.getVersion().toString(), formId, null, null, null, "window", formData, null, null);
        if (form == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // make primary key read-only
        Element el = FormUtil.findElement(FormUtil.PROPERTY_ID, form, formData);
        if (el != null) {
            String idValue = FormUtil.getElementPropertyValue(el, formData);
            if (idValue != null && !idValue.trim().isEmpty() && !"".equals(formData.getRequestParameter(FormUtil.FORM_META_ORIGINAL_ID))) {
                el.setProperty(FormUtil.PROPERTY_READONLY, "true");
            }
        }

        // submit form data
        FormData updatedFormData = appService.submitForm(form, formData, false);
        
        // handle response
        Map<String, String> formErrors = updatedFormData.getFormErrors();
        Map<String, String> fileErrors = updatedFormData.getFileErrors();
        if (formErrors.isEmpty() && fileErrors.isEmpty()) {
            // no errors
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", updatedFormData.getPrimaryKeyValue());
            AppUtil.writeJson(writer, jsonObject, callback);        
        } else {
            // return errors
            JSONObject jsonObject = new JSONObject();
            if (!formErrors.isEmpty()) {
                JSONObject formErrorsObject = new JSONObject();
                for (Iterator i = formErrors.keySet().iterator(); i.hasNext();) {
                    String errorKey = (String)i.next();
                    String errorValue = formErrors.get(errorKey);
                    formErrorsObject.put(errorKey, errorValue);
                }
                jsonObject.accumulate("error", formErrorsObject);
            }
            if (!fileErrors.isEmpty()) {
                JSONObject fileErrorsObject = new JSONObject();
                for (Iterator i = fileErrors.keySet().iterator(); i.hasNext();) {
                    String errorKey = (String)i.next();
                    String errorValue = fileErrors.get(errorKey);
                    fileErrorsObject.put(errorKey, errorValue);
                }
                jsonObject.accumulate("error", fileErrorsObject);
            }
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, jsonObject.toString());
        }
    }

    /**
     * Delete an existing form record
     * POST /json/data/form/delete/(*:appId)/(*:formId)/(*:primaryKeyValue)
     * curl -v -X POST -d "j_username=admin&j_password=admin" http://localhost:8080/jw/web/json/data/form/delete/crm//crm_account/001
     * @param writer
     * @param request
     * @param response
     * @param appId
     * @param formId
     * @param primaryKeyValue
     * @param callback
     * @throws IOException
     * @throws JSONException 
     */
    @RequestMapping(value="/json/data/form/delete/(*:appId)/(*:formId)/(*:primaryKeyValue)", method=RequestMethod.POST)
    public void formDataDelete(Writer writer, HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "appId", required = true) String appId, @RequestParam(value = "formId", required = true) String formId, @RequestParam(value="primaryKeyValue", required = true) String primaryKeyValue, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        AppDefinition appDef = appService.getPublishedAppDefinition(appId);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        formId = SecurityUtil.validateStringInput(formId);
        primaryKeyValue = SecurityUtil.validateStringInput(primaryKeyValue);        
        
        // retrieve form record
        FormData formData = new FormData();
        formData.setPrimaryKeyValue(primaryKeyValue);
        formData = formService.retrieveFormDataFromRequest(formData, request);
        Form form = appService.viewDataForm(appDef.getId(), appDef.getVersion().toString(), formId, null, null, null, "window", formData, null, null);
        if (form == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        FormRowSet loadedRowSet = appService.loadFormData(form, primaryKeyValue);
        if (loadedRowSet.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // delete record
        formDataDao.delete(form, new String[] { primaryKeyValue });
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", primaryKeyValue);
        AppUtil.writeJson(writer, jsonObject, callback);        
    }

    /**
     * Retrieve records from a data list
     * GET /web/json/data/list/crm/crm_account_list
     * curl -v -d "j_username=admin&j_password=admin" http://localhost:8080/jw/web/json/data/list/crm/crm_account_list
     * @param writer
     * @param response
     * @param appId
     * @param listId
     * @param callback
     * @param start
     * @param rows
     * @throws IOException
     * @throws JSONException 
     */
    @RequestMapping("/json/data/list/(*:appId)/(*:listId)")
    public void formDataList(Writer writer, HttpServletResponse response, @RequestParam(value = "appId", required = true) String appId, @RequestParam(value = "listId", required = true) String listId, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {
        AppDefinition appDef = appService.getPublishedAppDefinition(appId);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        listId = SecurityUtil.validateStringInput(listId);
        
        // get datalist
        DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(listId, appDef);        
        if (datalistDefinition == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }        
        String json = datalistDefinition.getJson();
        DataList dataList = dataListService.fromJson(json);
        
        // get data size and total rows
        int total = dataList.getTotal();
        DataListDecorator decorator = new DataListDecorator(dataList);
        DataListColumn[] columns = dataList.getColumns();
        DataListCollection results = dataList.getRows(rows, start);
        
        // output JSON
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("total", total);
        JSONArray data = new JSONArray();
        for (Iterator i=results.iterator(); i.hasNext();) {
            Map row = (Map)i.next();
            JSONObject rowObject = new JSONObject();
            for (int j=0; j<columns.length; j++) {
                DataListColumn column = columns[j];
                String columnName = column.getName();
                Object value = row.get(columnName);
                String formattedValue = decorator.formatColumn(column, row, value);
                rowObject.put(columnName, formattedValue);
            }
            data.put(rowObject);
        }
        jsonObject.put("data", data);
        AppUtil.writeJson(writer, jsonObject, callback);        
    }
        
}
