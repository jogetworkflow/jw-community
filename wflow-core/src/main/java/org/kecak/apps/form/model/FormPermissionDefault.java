package org.kecak.apps.form.model;

import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormPermission;
import org.joget.apps.userview.model.UserviewPermission;

/**
 * @author aristo
 *
 * Permission for form
 *
 */
public abstract class FormPermissionDefault extends UserviewPermission implements FormPermission {
    private FormData formData;
    private Element element;

    @Override
    public FormData getFormData() {
        return formData;
    }

    @Override
    public void setFormData(FormData formData) {
        this.formData = formData;
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public void setElement(Element element) {
        this.element = element;
    }
}
