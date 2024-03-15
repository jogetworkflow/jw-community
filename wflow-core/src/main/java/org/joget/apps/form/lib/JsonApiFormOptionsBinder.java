package org.joget.apps.form.lib;

import java.util.ArrayList;
import java.util.Collection;
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
            
            //used for json api parameter passing using `{values[0]}`
            for (int i = 0; i < values.length; i++) {
                params.put("values["+i+"]", values[i]);
            }
        }
        params.put("values", valuesStr);
        
        if ("true".equals(getPropertyString("addEmptyOption"))) {
            FormRow emptyRow = new FormRow();
            emptyRow.setProperty(FormUtil.PROPERTY_VALUE, "");
            emptyRow.setProperty(FormUtil.PROPERTY_LABEL, getPropertyString("emptyLabel"));
            options.add(emptyRow);
        }
        
        Map<String,Object> results = null;
        if (!"true".equals(getPropertyString("emptyDependencyValueCheck")) || (values != null && values.length > 0 && !values[0].isEmpty())) {
            results = JsonApiUtil.callApi(getProperties(), params);
        }

        if (results != null) {
            String idField = getPropertyString("idColumn");
            String labelField = getPropertyString("labelColumn");
            String groupingField = getPropertyString("groupingColumn");
            String name = "";
            
            Object data = results;
            
            String multirowBaseObjectName = getPropertyString("multirowBaseObject");
            if (!multirowBaseObjectName.isEmpty()) {
                data = JsonApiUtil.getObjectFromMap(multirowBaseObjectName, results);
                
                if (data != null) {
                    idField = idField.replace(multirowBaseObjectName + ".", "");
                    labelField = labelField.replace(multirowBaseObjectName + ".", "");
                    groupingField = groupingField.replace(multirowBaseObjectName + ".", "");
                    name = multirowBaseObjectName;
                }
            }
            
            if (data != null) {
                recursiveAddOptions(data, options, idField, null, labelField, null, groupingField, null, name);
            }
        }
        
        return options;
    }
    
    /**
     * Recursively loop through data tree to add options
     * 
     * @param data
     * @param options
     * @param idField
     * @param idValue
     * @param labelField
     * @param labelValue
     * @param groupingField
     * @param groupingValue
     * @param name 
     */
    public static void recursiveAddOptions(Object data, FormRowSet options, String idField, String idValue, String labelField, String labelValue, String groupingField, String groupingValue, String name) {
        if (data != null) {
            if (data.getClass().isArray()) { //Looping array object
                Object[] array = (Object[]) data;
                if (array.length > 0) {
                    for (Object rowObj : array) {
                        if (rowObj instanceof Map) { //it is an Object
                            String cGroupingValue = tryRetrieveGroupingValue(rowObj, groupingField, groupingValue);

                            recursiveAddOptions(rowObj, options, idField, idValue, labelField, labelValue, groupingField, cGroupingValue, null);
                        } else {
                            String value = rowObj.toString();
                            recursiveAddOptions(rowObj, options, idField, value, labelField, value, groupingField, groupingValue, null);
                        }
                    }
                }
            } else if (data instanceof Map && name != null && name.contains("<>")) { //it is an object and there is <> syntax to loop it
                //support looping object properties
                Map mapData = (Map) data;
                for (Object key : mapData.keySet()) {
                    Object value = mapData.get(key);
                    
                    String cIdValue = idValue;
                    String cLabelValue = labelValue;
                    String cGroupingValue = groupingValue;
                    if (cIdValue == null && "KEY".equals(idField)) {
                        cIdValue = key.toString();
                    }
                    if (cLabelValue == null && "KEY".equals(labelField)) {
                        cLabelValue = key.toString();
                    }
                    if (cGroupingValue == null && "KEY".equals(groupingField)) {
                        cGroupingValue = key.toString();
                    }
                    if (cIdValue == null && "VALUE".equals(idField)) {
                        cIdValue = value.toString();
                    }
                    if (cLabelValue == null && "VALUE".equals(labelField)) {
                        cLabelValue = value.toString();
                    }
                    if (cGroupingValue == null && "VALUE".equals(groupingField)) {
                        cGroupingValue = value.toString();
                    }
                    
                    recursiveAddOptions(value, options, idField, cIdValue, labelField, cLabelValue, groupingField, cGroupingValue, null);
                }
            } else if (idField.contains("[].") || idField.contains("<>.")) {
                //support nested looping array or object
                
                int index = idField.contains("[].")?idField.indexOf("[]."):idField.indexOf("<>.");
                String baseName = idField.substring(0, index + 2);
                
                Object loopData = JsonApiUtil.getObjectFromMap(baseName, (Map) data);
                
                if (loopData != null) {
                    String cGroupingValue = tryRetrieveGroupingValue(data, groupingField, groupingValue);
                    
                    idField = idField.replace(baseName + ".", "");
                    labelField = labelField.replace(baseName + ".", "");
                    groupingField = groupingField.replace(baseName + ".", "");
                    
                    recursiveAddOptions(loopData, options, idField, idValue, labelField, labelValue, groupingField, cGroupingValue, baseName);
                }
            } else if (idField.endsWith("[]")) { //the value & label is just array value
                Object loopData = JsonApiUtil.getObjectFromMap(idField, (Map) data);
                recursiveAddOptions(loopData, options, "", idValue, "", labelValue, groupingField, groupingValue, null);
            } else {
                //it is data
                String cIdValue = idValue;
                String cLabelValue = labelValue;
                String cGroupingValue = groupingValue;
                    
                if (cIdValue == null) {
                    Object id = JsonApiUtil.getObjectFromMap(idField, (Map) data);
                    cIdValue = id != null?id.toString():cIdValue; 
                }
                if (cLabelValue == null) {
                    Object label = JsonApiUtil.getObjectFromMap(labelField, (Map) data);
                    cLabelValue = label != null?label.toString():cLabelValue; 
                }
                
                if (cIdValue != null && cLabelValue != null) {
                    FormRow r = new FormRow();
                    r.put(FormUtil.PROPERTY_VALUE, cIdValue);
                    r.put(FormUtil.PROPERTY_LABEL, cLabelValue);
                    
                    if (!groupingField.isEmpty() && cGroupingValue == null) {
                        if (groupingField.contains("[]") || groupingField.contains("<>")) { //support grouping is an array or Object
                            FormRowSet groupingValues = new FormRowSet();
                            groupingValues.setMultiRow(true);
                            
                            recursiveAddOptions(data, groupingValues, groupingField, null, groupingField, null, "", null, null);
                            
                            if (!groupingValues.isEmpty()) {
                                Collection<String> values = new ArrayList<String>();
                                for (FormRow g : groupingValues) {
                                    values.add(g.getProperty(FormUtil.PROPERTY_VALUE));
                                }
                                cGroupingValue = String.join(";", values);
                            }
                        } else {
                            Object grouping = JsonApiUtil.getObjectFromMap(groupingField, (Map) data);
                            
                            if (data.getClass().isArray()) { //grouping is just an array
                                Collection<String> values = new ArrayList<String>();
                                Object[] array = (Object[]) data;
                                if (array.length > 0) {
                                    for (Object row : array) {
                                        values.add(row.toString());
                                    }
                                    cGroupingValue = String.join(";", values);
                                }
                            } else {
                                cGroupingValue = grouping != null?grouping.toString():cGroupingValue; 
                            }
                        }
                    }
                    
                    if (cGroupingValue != null) {
                        r.put(FormUtil.PROPERTY_GROUPING, cGroupingValue);
                    }
                    
                    options.add(r);
                }
            }
        }
    }
    
    /**
     * Try retrieve the grouping data if it is exist in parent object
     * 
     * @param rowObj
     * @param groupingField
     * @param groupingValue
     * @return 
     */
    public static String tryRetrieveGroupingValue(Object rowObj, String groupingField, String groupingValue) {
        String cGroupingValue = groupingValue;
        //support getting grouping value from a parent level
        if (rowObj instanceof Map 
                && cGroupingValue == null && !groupingField.isEmpty() 
                && !(groupingField.contains("[]") || groupingField.contains("<>"))) {
            Object result = JsonApiUtil.getObjectFromMap(groupingField, (Map) rowObj);
            if (result != null) {
                cGroupingValue = result.toString();
            }
        }
        return cGroupingValue;
    }
    
    @Override
    public String getDeveloperMode() {
        return "advanced";
    }
}