package org.joget.apps.form.model;

/**
 * This interface indicate that a Form Field Element is a multi options field 
 * such as Select Box, Check Box & Radio Button. It can use Form Options Binder 
 * which implemented org.joget.apps.form.model.FormAjaxOptionsBinder 
 * to populate its options using AJAX.
 * 
 */
public interface FormAjaxOptionsElement extends FormOptionsElement {
    
    /**
     * Get dependent field element which use to control the options of this field
     * @param formData
     * @return 
     */
    public Element getControlElement(FormData formData);
}
