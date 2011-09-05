package org.joget.apps.form.model;

/**
 * Interface that describes meta information used for configuring in the Form Builder.
 */
public interface FormBuilderEditable {
    public String getDefaultPropertyValues();
    
    /**
     * HTML template used for display in the Form Builder
     * @return
     */
    public String getFormBuilderTemplate();
}
