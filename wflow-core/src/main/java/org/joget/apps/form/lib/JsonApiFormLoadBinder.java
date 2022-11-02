package org.joget.apps.form.lib;

import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.JsonApiUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormLoadElementBinder;
import org.joget.apps.form.model.FormLoadMultiRowElementBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;

public class JsonApiFormLoadBinder extends FormBinder implements FormLoadBinder, FormLoadElementBinder, FormLoadMultiRowElementBinder {

    @Override
    public String getName() {
        return "JSON API Form Load Binder";
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
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/jsonApiFormLoadBinder.json", null, true, null);
    }

    @Override
    public FormRowSet load(Element element, String primaryKey, FormData formData) {
        FormRowSet rows = new FormRowSet();
        
        Map<String, String> params = new HashMap<String, String>();
        if (primaryKey == null) {
            primaryKey = "";
        }
        params.put("primaryKey", primaryKey);
        
        Map<String,Object> results = JsonApiUtil.callApi(getProperties(), params);
        if (results != null) {
            String multirowBaseObjectName = getPropertyString("multirowBaseObject");
            if (!multirowBaseObjectName.isEmpty()) {
                rows.setMultiRow(true);
                
                Object temp = JsonApiUtil.getObjectFromMap(multirowBaseObjectName, results);
                if (temp != null && temp.getClass().isArray()) {
                    Object[] baseObjectArray = (Object[]) temp;
                    if (baseObjectArray.length > 0) {
                        for (Object rowObj : baseObjectArray) {
                            rows.add(getRow((Map) rowObj, multirowBaseObjectName));
                        }
                    }
                }
            } else {
                rows.add(getRow(results, ""));
            }
        }
        
        return rows;
    }
    
    protected FormRow getRow(Map object, String multirowBaseObjectName) {
        FormRow r = new FormRow();
        
        if ("true".equalsIgnoreCase(getPropertyString("manualFieldMapping"))) {
            Object[] fieldMapping = (Object[]) getProperty("fieldMapping");
            for (Object o : fieldMapping) {
                Map mapping = (HashMap) o;
                String fieldName = mapping.get("field").toString();
                String jsonObjectName = mapping.get("jsonObjectName").toString();

                if (!multirowBaseObjectName.isEmpty()) {
                    jsonObjectName = jsonObjectName.replace(multirowBaseObjectName + ".", "");
                }

                Object value = JsonApiUtil.getObjectFromMap(jsonObjectName, object);

                if (value == null) {
                    value = jsonObjectName;
                }

                if (FormUtil.PROPERTY_ID.equals(fieldName)) {
                    r.setId(value.toString());
                } else {
                    r.put(fieldName, value.toString());
                }
            }
        } else {
            r.putAll(object);
        }
        
        String idField = getPropertyString("primaryKey");
        if (multirowBaseObjectName != null && !multirowBaseObjectName.isEmpty()) {
            idField = idField.replace(multirowBaseObjectName + ".", "");
        }
        Object id = JsonApiUtil.getObjectFromMap(idField, object);
        if (id != null) {
            r.setId(id.toString());
        }
        return r;
    }
    
    @Override
    public String getDeveloperMode() {
        return "advanced";
    }
}
