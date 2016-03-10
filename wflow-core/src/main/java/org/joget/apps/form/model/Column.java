package org.joget.apps.form.model;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormUtil;

public class Column extends Element implements FormBuilderEditable, FormContainer {

    @Override
    public String getName() {
        return "Column";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Column Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "column.ftl";
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
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/column.json", null, true, "message/form/Column");
    }

    @Override
    public String getFormBuilderTemplate() {
        return "";
    }
}
