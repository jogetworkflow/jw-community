package org.joget.apps.form.lib;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.SecurityUtil;

public class PasswordField extends Element implements FormBuilderPaletteElement {
    public static final String SECURE_VALUE = "****SECURE VALUE****";  

    @Override
    public String getName() {
        return "Password Field";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Password Field Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "passwordField.ftl";

        // set value
        String value = FormUtil.getElementPropertyValue(this, formData);
        String binderValue = getBinderValue(formData);
        
        if (value != null && !value.isEmpty() && (value.equals(binderValue) || (binderValue != null && value.equals(SecurityUtil.decrypt(binderValue))))) {
            value = SECURE_VALUE;
        }
        
        dataModel.put("value", value);

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }
    
    @Override
    public FormData formatDataForValidation(FormData formData) {
        String id = FormUtil.getElementParameterName(this);
        if (id != null) {
            String value = formData.getRequestParameter(id);
            if (value != null) {
                if (value.equals(SECURE_VALUE)) {
                    value = getBinderValue(formData);
                    
                    if (value != null) {
                        value = SecurityUtil.decrypt(value);
                        formData.addRequestParameterValues(id, new String[]{value});
                    } else {
                        formData.addRequestParameterValues(id, new String[]{""});
                    }
                }
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
                if (value.equals(SECURE_VALUE)) {
                    value = getPropertyString(FormUtil.PROPERTY_VALUE);
                    // load from binder if available
                    if (formData != null) {
                        String binderValue = formData.getLoadBinderDataProperty(this, id);
                        if (binderValue != null) {
                            value = binderValue;
                        }
                    } else {
                        value = SecurityUtil.encrypt(value);
                    }
                } else {
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
    
    protected String getBinderValue(FormData formData) {
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        String value = getPropertyString(FormUtil.PROPERTY_VALUE);
        
        // load from binder if available
        if (formData != null) {
            String binderValue = formData.getLoadBinderDataProperty(this, id);
            if (binderValue != null) {
                value = binderValue;
            }
        }
        
        return value;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>PasswordField</label><input type='password' />";
    }

    @Override
    public String getLabel() {
        return "Password Field";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/passwordField.json", null, true, "message/form/PasswordField");
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public int getFormBuilderPosition() {
        return 102;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class=\"fas fa-asterisk\"></i>";
    }
}
