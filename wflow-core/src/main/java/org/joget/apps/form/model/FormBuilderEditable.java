package org.joget.apps.form.model;

/**
 * Interface that describes meta information used for configuring in the Form Builder.
 */
public interface FormBuilderEditable {
    
    /**
     * Set default Plugin Properties Options value to a new added Field in Form Builder. 
     * This method is implemented in org.joget.apps.form.model.Element
     * 
     * @return
     */
    public String getDefaultPropertyValues();
    
    /**
     * HTML template used for display a new added field in the Form Builder
     * @return
     */
    public String getFormBuilderTemplate();
}
