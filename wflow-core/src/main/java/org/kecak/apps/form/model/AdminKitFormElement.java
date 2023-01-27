package org.kecak.apps.form.model;

import org.joget.apps.form.model.FormData;

import java.util.Map;

/**
 * Form AdminLTE Theme
 */
public interface AdminKitFormElement extends BootstrapFormElement {
    String renderAdminKitTemplate(FormData formData, @SuppressWarnings("rawtypes") Map dataModel);
}
