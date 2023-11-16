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
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;

public class IdGeneratorField extends Element implements FormBuilderPaletteElement {

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "idGeneratorField.ftl";

        String value = FormUtil.getElementPropertyValue(this, formData);
        dataModel.put("value", value);

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        // get value
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        if (id != null) {
            String value = FormUtil.getElementPropertyValue(this, formData);
            if ((value == null || value.trim().isEmpty()) && !FormUtil.isReadonly(this, formData)) {
                // generate new value
                value = getGeneratedValue(formData);
                
                String paramName = FormUtil.getElementParameterName(this);
                formData.addRequestParameterValues(paramName, new String[] {value});
            }
            if (value != null) {
                // set value into Properties and FormRowSet object
                FormRow result = new FormRow();
                result.setProperty(id, value);
                rowSet = new FormRowSet();
                rowSet.add(result);
            }
        }

        return rowSet;
    }

    protected String getGeneratedValue(FormData formData) {
        String value = "";
        if (formData != null) {
            try {
                value = FormUtil.getElementPropertyValue(this, formData);
                if (!(value != null && value.trim().length() > 0)) {
                    String envVariable = getPropertyString("envVariable");
                    String format = getPropertyString("format");
                    boolean isDistributedGeneration = "true".equalsIgnoreCase(getPropertyString("isDistributedGeneration"));
                    value = AppUtil.idGenerator(envVariable, format, isDistributedGeneration, getName());
                }
            } catch (Exception e) {
                LogUtil.error(IdGeneratorField.class.getName(), e, "");
            }
        }
        return value;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getName() {
        return "Id Generator Field";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "ID Generator Element";
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_CUSTOM;
    }

    @Override
    public int getFormBuilderPosition() {
        return 3000;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i><span>ID</span></i>";
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>" + ResourceBundleUtil.getMessage("org.joget.apps.form.lib.IdGeneratorField.pluginLabel") + "</label><span></span>";
    }

    @Override
    public String getLabel() {
        return "ID Generator Field";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/idGeneratorField.json", null, true, "message/form/IdGeneratorField");
    }
}
