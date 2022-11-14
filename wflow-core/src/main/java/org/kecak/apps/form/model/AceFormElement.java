package org.kecak.apps.form.model;

import org.joget.apps.form.model.FormData;

import java.util.Map;

/**
 * Form AdminLTE Theme
 */
public interface AceFormElement extends BootstrapFormElement {
    String renderAceTemplate(FormData formData, @SuppressWarnings("rawtypes") Map dataModel);
}
