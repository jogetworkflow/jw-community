package org.joget.apps.form.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.JsonApiUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.FormStoreBinder;
import org.joget.apps.form.model.FormStoreElementBinder;
import org.joget.apps.form.model.FormStoreMultiRowElementBinder;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;

public class JsonApiFormStoreBinder extends FormBinder implements FormStoreBinder, FormStoreElementBinder, FormStoreMultiRowElementBinder {
    @Override
    public String getName() {
        return "JSON API Form Store Binder";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getLabel() {
        return "JSON API Form Binder";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/jsonApiFormStoreBinder.json", null, true, null);
    }

    @Override
    public FormRowSet store(Element element, FormRowSet rows, FormData formData) {
        
        //prevent recursive call
        if (JsonApiUtil.isRecursiveCall(getPropertyString("jsonUrl"), WorkflowUtil.getHttpServletRequest())) {
            //fallback to use workflow form binder
            WorkflowFormBinder binder = new WorkflowFormBinder();
            return binder.store(element, rows, formData);
        }
        
        
        boolean isUpdate = true;
        // find root form
        Form form = FormUtil.findRootForm(element);
        String primaryKey = form.getPrimaryKeyValue(formData);
        String status = "update";
        if (primaryKey != null && !primaryKey.equals(formData.getRequestParameter(FormUtil.FORM_META_ORIGINAL_ID))) {
            isUpdate = false;
        }
            
        Map<String, String> params = new HashMap<String, String>();

        Date currentDate = new Date();
        WorkflowUserManager workflowUserManager= (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        User user = workflowUserManager.getCurrentUser();
        String name = null;
        String username = workflowUserManager.getCurrentUsername();
        if (user != null) {
            name = DirectoryUtil.getUserFullName(user);
        }
        for (FormRow r : rows) {
            if (r.getId() == null || r.getId().isEmpty()) {
                r.setId(UuidGenerator.getInstance().getUuid());
                r.setCreatedBy(username);
                r.setCreatedByName(name);
                r.setDateCreated(currentDate);
            }
            r.setModifiedBy(username);
            r.setModifiedByName(name);
            r.setDateModified(currentDate);
        }
        
        if (isUpdate && "true".equalsIgnoreCase(getPropertyString("useDifferentApiForUpdate"))) {
            setProperty("jsonUrl", getPropertyString("updateJsonUrl"));
            setProperty("requestType", getPropertyString("updateRequestType"));
        }
        
        if (!rows.isMultiRow() && !rows.isEmpty()) {
            FormRow row = rows.get(0);
            if (primaryKey != null && !primaryKey.isEmpty()) {
                row.setId(primaryKey);
            }
            params.put("primaryKey", row.getId());
            
            for (Object key : row.keySet()) {
                params.put(key.toString(), row.getProperty(key.toString()));
            }
        } else if (primaryKey != null && !primaryKey.isEmpty()) {
            params.put("primaryKey", primaryKey);
        }
        
        if ("formPayload".equalsIgnoreCase(getPropertyString("postMethod"))) {
            setProperty("postMethod", "custom");
            
            String jsonPayload = FormUtil.formRowSetToJson(rows, true);
            if (!jsonPayload.isEmpty() && !rows.isMultiRow()) {
                //unwrap
                jsonPayload = jsonPayload.substring(1, jsonPayload.length()-1);
            }
            setProperty("customPayload", jsonPayload);
        } else if ("formDataParam".equalsIgnoreCase(getPropertyString("postMethod")) && !rows.isMultiRow() && !rows.isEmpty()) {
            Collection<Map> temp = new ArrayList<Map>();
            Object[] paramsValues = (Object[]) getProperty("params");
            if (paramsValues != null) {
                for (Object o : paramsValues) {
                    Map mapping = (HashMap) o;
                    temp.add(mapping);
                }
            }
            
            FormRow row = rows.get(0);
            for (Object k : row.keySet()) {
                Map data = new HashMap();
                data.put("name", k);
                data.put("value", row.getProperty(k.toString()));
                temp.add(data);
            }
            
            setProperty("params", temp.toArray(new Object[0]));
        }
        
        JsonApiUtil.callApi(getProperties(), params);
        
        return rows;
    }
    
    @Override
    public String getDeveloperMode() {
        return "advanced";
    }
}
