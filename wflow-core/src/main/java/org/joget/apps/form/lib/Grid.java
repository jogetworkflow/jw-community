package org.joget.apps.form.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.map.ListOrderedMap;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.PwaOfflineResources;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Grid extends Element implements FormBuilderPaletteElement, FormContainer, PwaOfflineResources {
    protected Map<FormData, FormRowSet> cachedRowSet = new HashMap<FormData, FormRowSet>();

    @Override
    public String getName() {
        return "Grid";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Grid Element";
    }

    /**
     * Return a Map of value=label, each pair representing a grid column header.
     * @param formData
     * @return
     */
    protected Map<String, String> getHeaderMap(FormData formData) {
        Map<String, String> headerMap = new ListOrderedMap();
        Object optionProperty = getProperty(FormUtil.PROPERTY_OPTIONS);
        if (optionProperty != null && optionProperty instanceof Collection) {
            for (Map opt : (FormRowSet) optionProperty) {
                Object value = opt.get("value");
                Object label = opt.get("label");
                if (value != null && label != null) {
                    headerMap.put(value.toString(), StringUtil.stripHtmlRelaxed(label.toString()));
                }
            }
        }
        return headerMap;
    }

    /**
     * Return the grid data
     * @param formData
     * @return A FormRowSet containing the grid cell data.
     */
    protected FormRowSet getRows(FormData formData) {
        if (!cachedRowSet.containsKey(formData)) {
            String id = getPropertyString(FormUtil.PROPERTY_ID);
            String param = FormUtil.getElementParameterName(this);
            FormRowSet rowSet = new FormRowSet();
            rowSet.setMultiRow(true);

            // get headers
            Map<String, String> headerMap = getHeaderMap(formData);

            // read from 'value' property
            String json = getPropertyString(FormUtil.PROPERTY_VALUE);
            try {
                rowSet = parseFormRowSetFromJson(json);
            } catch (Exception ex) {
                LogUtil.error(Grid.class.getName(), ex, "Error parsing grid JSON");
            }

            // read from request if available.
            boolean hasSubmittedData = false;
            boolean continueLoop = true;
            int i = 0;
            while (continueLoop) {
                FormRow row = new FormRow();
                for (String header : headerMap.keySet()) {
                    String paramName = param + "_" + header + "_" + i;
                    String paramValue = formData.getRequestParameter(paramName);
                    if (paramValue != null) {
                        row.setProperty(header, paramValue);
                    }
                }
                i++;
                if (!row.isEmpty()) {
                    if (i == 0) {
                        // reset rowset
                        rowSet = new FormRowSet();
                    }
                    rowSet.add(row);
                    hasSubmittedData = true;
                } else {
                    // no more rows, stop looping
                    continueLoop = false;
                }
            }
            
            //checking for hidden section submission
            if (!hasSubmittedData) {
                for (String header : headerMap.keySet()) {
                    String paramName = param + "_" + header;
                    String paramValue = formData.getRequestParameter(paramName);
                    if (paramValue != null) {
                        hasSubmittedData = true;
                    }
                    break;
                }
            }

            if (!FormUtil.isFormSubmitted(this, formData) || FormUtil.isReadonly(this, formData) || !hasSubmittedData) {
                // load from binder if available
                FormRowSet binderRowSet = formData.getLoadBinderData(this);
                if (binderRowSet != null) {
                    if (!binderRowSet.isMultiRow()) {
                        // parse from String
                        if (!binderRowSet.isEmpty()) {
                            FormRow row = binderRowSet.get(0);
                            String jsonValue = row.getProperty(id);
                            try {
                                rowSet = parseFormRowSetFromJson(jsonValue);
                            } catch (Exception ex) {
                                LogUtil.error(Grid.class.getName(), ex, "Error parsing grid JSON");
                            }
                        }

                    } else {
                        rowSet = binderRowSet;
                    }
                    
                    if (rowSet != null) {
                        for (FormRow r : rowSet) {
                            for (Object k : r.keySet()) {
                                String value = r.getProperty(k.toString());
                                if (SecurityUtil.hasSecurityEnvelope(value)) {
                                    r.setProperty(k.toString(), SecurityUtil.decrypt(value));
                                }
                            }
                        }
                    }
                }

            }
            cachedRowSet.put(formData, rowSet);
        }

        return cachedRowSet.get(formData);
    }

    /**
     * Parses a JSON String into a FormRowSet
     * @param json
     * @return
     * @throws JSONException
     */
    protected FormRowSet parseFormRowSetFromJson(String json) throws JSONException {
        FormRowSet rowSet = new FormRowSet();
        rowSet.setMultiRow(false);

        if (json != null && json.trim().length() > 0) {
            // loop thru each row in json array
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonRow = (JSONObject) jsonArray.get(i);

                // create row and populate fields
                FormRow row = new FormRow();
                JSONArray fields = jsonRow.names();
                if (fields != null && fields.length() > 0) {
                    for (int k = 0; k < fields.length(); k++) {
                        String fieldName = fields.getString(k);
                        String value = jsonRow.get(fieldName).toString();
                        row.setProperty(fieldName, value);
                    }
                }
                rowSet.add(row);
            }
        }
        return rowSet;
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        // get form rowset
        FormRowSet rowSet = getRows(formData);
        rowSet.setMultiRow(true);

        // TODO: set foreign key?

        return rowSet;
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "grid.ftl";

        // set value
        String[] valueArray = FormUtil.getElementPropertyValues(this, formData);
        List<String> values = Arrays.asList(valueArray);
        dataModel.put("values", values);

        // set validator decoration
        String decoration = FormUtil.getElementValidatorDecoration(this, formData);
        dataModel.put("decoration", decoration);

        // set headers
        Map<String, String> headers = getHeaderMap(formData);
        dataModel.put("headers", headers);

        // set rows
        FormRowSet rows = getRows(formData);
        dataModel.put("rows", rows);
        
        dataModel.put("customDecorator", getDecorator());

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>" + ResourceBundleUtil.getMessage("org.joget.apps.form.lib.Grid.pluginLabel") + "</label><table cellspacing='0'><tr><th>" + ResourceBundleUtil.getMessage("form.grid.header") + "</th><th>" + ResourceBundleUtil.getMessage("form.grid.header") + "</th></tr><tr><td>" + ResourceBundleUtil.getMessage("form.grid.cell") + "</td><td>" + ResourceBundleUtil.getMessage("form.grid.cell") + "</td></tr></table>";
    }

    @Override
    public String getLabel() {
        return "Grid";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/grid.json", null, true, "message/form/Grid");
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_CUSTOM;
    }

    @Override
    public int getFormBuilderPosition() {
        return 1100;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class=\"fas fa-table\"></i>";
    }
     
    @Override
    public Boolean selfValidate(FormData formData) {
        Boolean valid = true;
        
        FormRowSet rowSet = getRows(formData);
        String id = FormUtil.getElementParameterName(this);
        String errorMsg = getPropertyString("errorMessage");
        
        String min = getPropertyString("validateMinRow");
        if (min != null && !min.isEmpty()) {
            try {
                int minNumber = Integer.parseInt(min);
                if (rowSet.size() < minNumber) {
                    valid = false;
                }
            } catch (Exception e) {}
        }
        
        String max = getPropertyString("validateMaxRow");
        if (max != null && !max.isEmpty()) {
            try {
                int maxNumber = Integer.parseInt(max);
                if (rowSet.size() > maxNumber) {
                    valid = false;
                }
            } catch (Exception e) {}
        }
        
        if (!valid) {
            formData.addFormError(id, errorMsg);
        }
        
        return valid;
    }
    
    protected String getDecorator() {
        String decorator = "";
        
        try {
            String min = getPropertyString("validateMinRow");
            
            if ((min != null && !min.isEmpty())) {
                int minNumber = Integer.parseInt(min);
                if (minNumber > 0) {
                    decorator = "*";
                }
            }
        } catch (Exception e) {}
        
        return decorator;
    }
    
    @Override
    public Collection<String> getDynamicFieldNames() {
        Collection<String> fieldNames = new ArrayList<String>();
        
        if (getStoreBinder() == null) {
            fieldNames.add(getPropertyString(FormUtil.PROPERTY_ID));
        }
        
        return fieldNames;
    }
    
    @Override
    public Set<String> getOfflineStaticResources() {
        Set<String> urls = new HashSet<String>();
        String contextPath = AppUtil.getRequestContextPath();
        urls.add(contextPath + "/js/jquery/jquery.jeditable.js");
        urls.add(contextPath + "/plugin/org.joget.apps.form.lib.Grid/js/jquery.formgrid.js");
        
        return urls;
    }
}

