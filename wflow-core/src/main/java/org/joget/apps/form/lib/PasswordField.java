package org.joget.apps.form.lib;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPalette;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormUtil;

public class PasswordField extends Element implements FormBuilderPaletteElement {

    @Override
    public String getName() {
        return "Password Field";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
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
        return "/plugin/org.joget.apps.form.lib.PasswordField/images/textField_icon.gif";
    }
}
