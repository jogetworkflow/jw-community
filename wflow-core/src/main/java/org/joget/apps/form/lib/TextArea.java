package org.joget.apps.form.lib;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.ResourceBundleUtil;

public class TextArea extends Element implements FormBuilderPaletteElement {

    @Override
    public String getName() {
        return "Text Area";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
    }

    @Override
    public String getDescription() {
        return "Text Area Element";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "textArea.ftl";

        // set value
        String value = FormUtil.getElementPropertyValue(this, formData);
        dataModel.put("value", value);

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>" + ResourceBundleUtil.getMessage("org.joget.apps.form.lib.TextArea.pluginLabel") + "</label><textarea cols='20' rows='5'></textarea>";
    }

    @Override
    public String getLabel() {
        return "Text Area";
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/textArea.json", null, true, "message/form/TextArea");
    }

    @Override
    public String getFormBuilderCategory() {
        return FormBuilderPalette.CATEGORY_GENERAL;
    }

    @Override
    public int getFormBuilderPosition() {
        return 200;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<span class=\"fa-stack\"><i class=\"far fa-square fa-stack-2x\"></i><i><span style=\"font-weight: bold;\">A</span></i></span>";
    }
}
