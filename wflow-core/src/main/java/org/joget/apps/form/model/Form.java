package org.joget.apps.form.model;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormUtil;

public class Form extends Element implements FormBuilderEditable {

    @Override
    public String getName() {
        return "Form";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public String getDescription() {
        return "Form Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "form.ftl";
        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
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
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/form.json", null, true, "message/form/Form");
    }
    
    @Override
    public String getFormBuilderTemplate() {
        return "";
    }
}
