package org.joget.apps.form.lib;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormContainer;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;

/**
 * Columns layout container for grouping child fields into configurable columns.
 * Uses calc() for automatic space distribution like legacy columns.
 */
public class Columns extends Element implements FormBuilderPaletteElement, FormContainer {

    @Override
    public String getName() {
        return "Columns";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "Columns Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "columns.ftl";
       
        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/columns_template.json", null, true, null);
    }

    @Override
    public String getLabel() {
        return "Columns";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/columns.json", null, true, "message/form/Columns");
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public int getFormBuilderPosition() {
        return 1200;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class=\"las la-columns\"></i>";
    }
}
