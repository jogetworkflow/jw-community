package org.joget.apps.form.model;

public abstract class FormMultiRowValidator extends Validator {
    public boolean validate(Element element, FormData data, String[] values) {
        FormRowSet rows = element.formatData(data);
  
        return validate(element, data, rows);
    }
    
    /**
     * Validate the submitted values for an element/form
     * @param element
     * @param data
     * @param id ID of the element
     * @param values The values to validate
     * @return true if the validation is successful
     */
    public abstract boolean validate(Element element, FormData data, FormRowSet rows);
}
