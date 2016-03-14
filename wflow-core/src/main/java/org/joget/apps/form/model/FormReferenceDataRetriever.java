package org.joget.apps.form.model;

public interface FormReferenceDataRetriever {

    /**
     * Retrieve form data rows for an array of specified primary key values.
     * @param primaryKeyValues
     * @return 
     */
    public FormRowSet loadFormRows(String[] primaryKeyValues, FormData formData);
    
}
