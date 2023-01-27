package org.kecak.apps.form.model;

import org.joget.apps.form.model.FormData;

import java.util.Map;

/**
 * AdminLTE Form Template
 */
public interface AdminLteFormElement extends BootstrapFormElement {
    String renderAdminLteTemplate(FormData formData, @SuppressWarnings("rawtypes") Map dataModel);
}
