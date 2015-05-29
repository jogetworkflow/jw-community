package org.joget.apps.form.lib;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.SecurityUtil;

public class TextField extends Element implements FormBuilderPaletteElement {

    @Override
    public String getName() {
        return "Text Field";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
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
        
        dataModel.put("value", value);

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }
    
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        // get value
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        if (id != null) {
            String value = FormUtil.getElementPropertyValue(this, formData);
            if (value != null) {
                
                if ("true".equalsIgnoreCase(getPropertyString("encryption"))) {
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
        return "<label class='label'>TextField</label><input type='text' />";
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
        return "/plugin/org.joget.apps.form.lib.TextField/images/textField_icon.gif";
    }
}
