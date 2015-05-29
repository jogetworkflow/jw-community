package org.joget.apps.form.model;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormUtil;

public class Section extends Element implements FormBuilderEditable, FormContainer {
    private Boolean continueValidation = null;

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
        if (((Boolean) dataModel.get("includeMetaData") == true) || isAuthorize(formData)) {
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

            dataModel.put("visible", isMatch(formData));

            String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
            return html;
        } else {
            return "";
        }
    }

    @Override
    public boolean continueValidation(FormData formData) {
        if (continueValidation == null) {
            if (isAuthorize(formData)) {
                // get the control element (where value changes the target)
                String visibilityControl = getPropertyString("visibilityControl");

                if (visibilityControl != null && !visibilityControl.isEmpty()) {
                    continueValidation = isMatch(formData);
                } else {
                    continueValidation = super.continueValidation(formData);
                }
            } else {
                continueValidation = false;
            }
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
    
    protected Boolean isMatch(FormData formData) {
        // get the control element (where value changes the target)
        String visibilityControl = getPropertyString("visibilityControl");

        // get the value in the control element which will trigger the change
        String visibilityValue = getPropertyString("visibilityValue");
        
        String isRegex = getPropertyString("regex");
        
        if (visibilityControl != null && !visibilityControl.isEmpty() && visibilityValue != null && !visibilityValue.isEmpty()) {
            // find the control element
            Form rootForm = FormUtil.findRootForm(this);
            Element controlElement = FormUtil.findElement(visibilityControl, rootForm, formData);
            if (controlElement != null) {
                // check for matching values
                String[] paramValue = FormUtil.getElementPropertyValues(controlElement, formData);
                
                if (paramValue != null) {
                    for (String value : paramValue) {
                        if (isRegex != null && "true".equals(isRegex)) {
                            try {
                                if (value.matches(visibilityValue)) {
                                    return true;
                                }
                            } catch (Exception e){}
                        } else {
                            if (value.equals(visibilityValue)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
        }
        
        return true;
    }
}
