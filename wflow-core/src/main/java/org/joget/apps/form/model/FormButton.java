package org.joget.apps.form.model;

import java.util.Map;
import org.joget.apps.form.service.FormUtil;

/**
 * Abstract base class for buttons in a form.
 */
public abstract class FormButton extends Element implements FormAction {

    public static final String DEFAULT_ID = "submit";

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "submitButton.ftl";

        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        return null;
    }

    @Override
    public boolean isActive(Form form, FormData formData) {
        boolean active = false;
        String value = FormUtil.getRequestParameter(this, formData);
        if (value != null && value.trim().length() > 0) {
            active = true;
        }
        return active;
    }
}
