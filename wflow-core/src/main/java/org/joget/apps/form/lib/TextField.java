package org.joget.apps.form.lib;

import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;

public class TextField extends Element implements FormBuilderPaletteElement {
    protected Map<FormData, String> submittedValueHolder = new HashMap<FormData, String>();
    protected Map<FormData, String> validationValueHolder = new HashMap<FormData, String>();

    @Override
    public String getName() {
        return "Text Field";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Text Field Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "textField.ftl";

        // set value
        String value = FormUtil.getElementPropertyValue(this, formData);
        
        value = SecurityUtil.decrypt(value);
        
        if (FormUtil.isReadonly(this, formData) && "true".equalsIgnoreCase(getPropertyString("readonlyLabel"))) {
            String valueLabel = value;
            if (!getPropertyString("style").isEmpty() && "true".equalsIgnoreCase(getPropertyString("storeNumeric"))) {
                valueLabel = StringUtil.numberFormat(value, getPropertyString("style"), getPropertyString("prefix"), getPropertyString("postfix"), "true".equalsIgnoreCase(getPropertyString("useThousandSeparator")), getPropertyString("numOfDecimal"));
            }
            dataModel.put("valueLabel", valueLabel);
        }
        dataModel.put("value", value);

        //Get the validator type  
        Object element = getProperty("validator");
        
        if (element != null && element instanceof Map) {
            Map elementMap = (Map) element;
            Map<String, Object> properties = (Map<String, Object>) elementMap.get("properties");
            
            dataModel.put("validator", properties.get("type"));
        }
        

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }
    
    @Override
    public FormData formatDataForValidation(FormData formData) {
        if (!getPropertyString("style").isEmpty()) {
            String id = FormUtil.getElementParameterName(this);
            if (id != null) {
                String submittedValue = FormUtil.getElementPropertyValue(this, formData);
                String validationValue = submittedValue;
                
                validationValue = validationValue.replaceAll(" ", "");
                if ("EURO".equalsIgnoreCase(getPropertyString("style"))) {
                    validationValue = validationValue.replaceAll(StringUtil.escapeRegex("."), "");
                } else {
                    validationValue = validationValue.replaceAll(StringUtil.escapeRegex(","), "");
                }
                validationValue = validationValue.replaceAll(StringUtil.escapeRegex(getPropertyString("prefix")), "");
                validationValue = validationValue.replaceAll(StringUtil.escapeRegex(getPropertyString("postfix")), "");
                formData.addRequestParameterValues(id, new String[]{validationValue});
                
                submittedValueHolder.put(formData, submittedValue);
                validationValueHolder.put(formData, validationValue);
            }
        }
        return formData;
    }
    
    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        // get value
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        if (id != null) {
            String value = FormUtil.getElementPropertyValue(this, formData);
            if (value != null) {
                
                if (!getPropertyString("style").isEmpty()) {
                    String submittedValue = submittedValueHolder.get(formData);
                    String validationValue = validationValueHolder.get(formData);
                
                    if (validationValue == null) {
                        formatDataForValidation(formData);
                        submittedValue = submittedValueHolder.get(formData);
                        validationValue = validationValueHolder.get(formData);
                    }
                    if ("true".equalsIgnoreCase(getPropertyString("storeNumeric"))) {
                        value = validationValue;
                    } else {
                        value = submittedValue;
                    }
                }
                
                if (!FormUtil.isReadonly(this, formData) && "true".equalsIgnoreCase(getPropertyString("encryption"))) {
                    value = SecurityUtil.encrypt(value);
                }
                
                // set value into Properties and FormRowSet object
                FormRow result = new FormRow();
                result.setProperty(id, value);
                rowSet = new FormRowSet();
                rowSet.add(result);
            }
        }

        return rowSet;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>" + ResourceBundleUtil.getMessage("org.joget.apps.form.lib.TextField.pluginLabel") + "</label><input type='text' />";
    }

    @Override
    public String getLabel() {
        return "Text Field";
    }

    @Override
    public String getPropertyOptions() {
        String encryption = "";
        if (SecurityUtil.getDataEncryption() != null) {
            encryption = ",{name : 'encryption', label : '@@form.textfield.encryption@@', type : 'checkbox', value : 'false', ";
            encryption += "options : [{value : 'true', label : '' }]}";
        }
        
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/textField.json", new Object[]{encryption}, true, "message/form/TextField");
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public int getFormBuilderPosition() {
        return 100;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<span class=\"fa-stack\"><i class=\"far fa-square fa-stack-2x\"></i><i><span style=\"font-weight: bold;\">T</span></i></span>";
    }
}
