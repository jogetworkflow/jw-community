package org.joget.apps.form.model;

import java.io.Serializable;

/**
 * This interface indicate that a Form Options Binder support retrieving options
 * using AJAX
 * 
 */
public interface FormAjaxOptionsBinder extends Serializable {
    
    /**
     * Use to decide this field is using AJAX to load its options or not. 
     * @return 
     */
    public boolean useAjax();
    
    /**
     * Retrieve options based on dependency values
     * @return 
     */
    public FormRowSet loadAjaxOptions(String[] dependencyValues);
}
