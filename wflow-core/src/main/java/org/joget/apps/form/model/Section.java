package org.joget.apps.form.model;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormUtil;

public class Section extends Element implements FormBuilderEditable {

    @Override
    public String getName() {
        return "Section";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public String getDescription() {
        return "Section Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "section.ftl";

        // set visibility attributes - currently working for textfield, textarea and selectbox. TODO: ensure it works for checkbox and radio.
        String visibilityControl = getPropertyString("visibilityControl");
        if (visibilityControl != null && !visibilityControl.isEmpty()) {
            Form rootForm = FormUtil.findRootForm(this);
            Element controlElement = FormUtil.findElement(visibilityControl, rootForm, formData);
            if (controlElement != null) {
                String visibilityControlParam = FormUtil.getElementParameterName(controlElement);
                dataModel.put("visibilityControlParam", visibilityControlParam);
            }
        }

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public boolean continueValidation(FormData formData) {
        boolean continueValidation = true;

        // get the control element (where value changes the target)
        String visibilityControl = getPropertyString("visibilityControl");

        // get the value in the control element which will trigger the change
        String visibilityValue = getPropertyString("visibilityValue");

        if (visibilityControl != null && !visibilityControl.isEmpty()) {
            // find the control element
            Form rootForm = FormUtil.findRootForm(this);
            Element controlElement = FormUtil.findElement(visibilityControl, rootForm, formData);
            if (controlElement != null) {
                // check for matching values
                String paramName = FormUtil.getElementParameterName(controlElement);
                String paramValue = formData.getRequestParameter(paramName);
                continueValidation = (paramValue != null && paramValue.equals(visibilityValue));
            }
        } else {
            continueValidation = super.continueValidation(formData);
        }
        return continueValidation;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/section.json", null, true, "message/form/Section");
    }

    @Override
    public String getFormBuilderTemplate() {
        return "";
    }
    
    @Override
    public FormRowSet formatData(FormData formData) {
        return null;
    }
}
