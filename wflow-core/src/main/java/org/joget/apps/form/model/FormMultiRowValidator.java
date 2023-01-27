package org.joget.apps.form.model;

/**
 * Base class for implementations to validate multi row data for an element e.g. Grid.
 * 
 */
public abstract class FormMultiRowValidator extends Validator {
    
    /**
     * Not using for now
     * @param element
     * @param data
     * @param values
     * @return 
     */
    public boolean validate(Element element, FormData data, String[] values) {
        FormRowSet rows = element.formatData(data);
  
        return validate(element, data, rows);
    }
    
    /**
     * Validate the submitted rows for an element
     * @param element
     * @param data
     * @param rows
     * @return true if the validation is successful
     */
    public abstract boolean validate(Element element, FormData data, FormRowSet rows);
}
