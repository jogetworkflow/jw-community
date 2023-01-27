package org.joget.apps.form.model;

/**
 * This interface indicate that the form data load by Form Load Binder is deletable
 * from Form Data table.
 * 
 */
public interface FormDataDeletableBinder {
    
    /**
     * Get Form Id of the loaded data
     * @return 
     */
    public String getFormId();
    
    /**
     * Get table name of the loaded data
     * @return 
     */
    public String getTableName();
}
