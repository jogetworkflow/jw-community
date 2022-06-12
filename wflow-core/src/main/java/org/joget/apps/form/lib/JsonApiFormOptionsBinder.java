package org.joget.apps.form.lib;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.JsonApiUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormAjaxOptionsBinder;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadOptionsBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.SecurityUtil;

public class JsonApiFormOptionsBinder extends FormBinder implements FormLoadOptionsBinder, FormAjaxOptionsBinder {
    @Override
    public String getName() {
        return "JSON API Form Options Binder";
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
        String useAjax = "";
        if (SecurityUtil.getDataEncryption() != null && SecurityUtil.getNonceGenerator() != null) {
            useAjax = ",{\"name\":\"useAjax\",\"label\":\"@@form.defaultformoptionbinder.useAjax@@\",\"description\":\"@@form.jsonapibinder.options.ajax.desc@@\",\"type\":\"checkbox\",\"value\" :\"false\",\"options\" :[{\"value\" :\"true\",\"label\" :\"\"}]}";
        }
        
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/jsonApiFormOptionsBinder.json", new String[]{useAjax}, true, null);
    }

    @Override
    public FormRowSet load(Element arg0, String arg1, FormData arg2) {
        return loadAjaxOptions(null);
    }

    @Override
    public boolean useAjax() {
        return "true".equalsIgnoreCase(getPropertyString("useAjax"));
    }

    @Override
    public FormRowSet loadAjaxOptions(String[] values) {
        FormRowSet options = new FormRowSet();
        options.setMultiRow(true);
        
        Map<String, String> params = new HashMap<String, String>();
        String valuesStr = "";
        if (values != null && values.length > 0) {
            valuesStr =  StringUtils.join(values);
        }
        params.put("values", valuesStr);
        
        for (int i = 0; i < values.length; i++) {
            params.put("values["+i+"]", values[i]);
        }
        
        if ("true".equals(getPropertyString("addEmptyOption"))) {
            FormRow emptyRow = new FormRow();
            emptyRow.setProperty(FormUtil.PROPERTY_VALUE, "");
            emptyRow.setProperty(FormUtil.PROPERTY_LABEL, getPropertyString("emptyLabel"));
            options.add(emptyRow);
        }
        
        Map<String,Object> results = JsonApiUtil.callApi(getProperties(), params);
        if (results != null) {
            String multirowBaseObjectName = getPropertyString("multirowBaseObject");
            if (!multirowBaseObjectName.isEmpty()) {
                String idField = getPropertyString("idColumn").replace(multirowBaseObjectName + ".", "");
                String labelField = getPropertyString("labelColumn").replace(multirowBaseObjectName + ".", "");
                String groupingField = getPropertyString("groupingColumn").replace(multirowBaseObjectName + ".", "");
                
                Object temp = JsonApiUtil.getObjectFromMap(multirowBaseObjectName, results);
                if (temp != null && temp.getClass().isArray()) {
                    Object[] baseObjectArray = (Object[]) temp;
                    if (baseObjectArray.length > 0) {
                        for (Object rowObj : baseObjectArray) {
                            FormRow r = new FormRow();
                            Object id = JsonApiUtil.getObjectFromMap(idField, (Map) rowObj);
                            Object label = JsonApiUtil.getObjectFromMap(labelField, (Map) rowObj);
                            if (id != null) {
                                r.put(FormUtil.PROPERTY_VALUE, id.toString());
                                r.put(FormUtil.PROPERTY_LABEL, (label != null)?label.toString():id.toString());
                            }
                            if (!groupingField.isEmpty()) {
                                Object grouping = JsonApiUtil.getObjectFromMap(groupingField, (Map) rowObj);
                                r.put(FormUtil.PROPERTY_GROUPING, (grouping != null)?grouping.toString():"");
                            }
                            options.add(r);
                        }
                    }
                }
            }
        }
        
        return options;
    }
    
    @Override
    public String getDeveloperMode() {
        return "advanced";
    }
}
