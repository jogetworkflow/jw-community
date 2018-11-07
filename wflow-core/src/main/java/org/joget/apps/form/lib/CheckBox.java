package org.joget.apps.form.lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;

public class CheckBox extends SelectBox implements FormBuilderPaletteElement {

    @Override
    public String getName() {
        return "Check Box";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Check Box Element";
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        // get value
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        if (id != null) {
            String[] values = FormUtil.getElementPropertyValues(this, formData);
            if (values != null && values.length > 0) {
                // check for empty submission via parameter
                String[] paramValues = FormUtil.getRequestParameterValues(this, formData);
                if ((paramValues == null || paramValues.length == 0) && FormUtil.isFormSubmitted(this, formData)) {
                    values = new String[]{""};
                }

                // formulate values
                String delimitedValue = FormUtil.generateElementPropertyValues(values);

                // set value into Properties and FormRowSet object
                FormRow result = new FormRow();
                result.setProperty(id, delimitedValue);
                rowSet = new FormRowSet();
                rowSet.add(result);
            }
        }

        return rowSet;
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "checkBox.ftl";
        
        dynamicOptions(formData);
        
        // set value
        String[] valueArray = FormUtil.getElementPropertyValues(this, formData);
        List<String> values = Arrays.asList(valueArray);
        dataModel.put("values", values);

        // set options
        Collection<Map> optionMap = getOptionMap(formData);
        dataModel.put("options", optionMap);

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>Check Box</label><label><input type='checkbox' value='Option' style='color:silver' />Option</label>";
    }

    @Override
    public String getLabel() {
        return "Check Box";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/checkbox.json", null, true, "message/form/CheckBox");
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public int getFormBuilderPosition() {
        return 400;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class=\"far fa-check-square\"></i>";
    }
}

