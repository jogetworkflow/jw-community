package org.joget.apps.form.model;

/**
 * Defines a form action (e.g. button) that performs an action when a form is submitted.
 */
public interface FormAction {

    /**
     * Checks to see whether or not this action was triggered e.g. button clicked
     * @param form
     * @param formData
     * @return
     */
    public boolean isActive(Form form, FormData formData);

    /**
     * Invoked when the action is triggered.
     * @param form
     * @param formData
     * @return
     */
    public FormData actionPerformed(Form form, FormData formData);
}
