package org.joget.apps.form.model;

/**
 * A Form Store Binder delete data from an element and its children into a data source.
 */
public interface FormDeleteBinder {
    
    /**
     * Delete data in the data source.
     * @param element The element from which the data originates.
     * @param rows This is the data to be deleted. A Collection of Map objects. Each Map object contains property=value pairs to represent a data row.
     * @param formData Contains form data eg request parameters, primary key, foreign key, etc.
     * @param deleteGrid
     * @param deleteSubform
     * @param abortProcess
     * @param deleteFiles
     * @param hardDelete
     */
    public void delete(Element element, FormRowSet rows, FormData formData, boolean deleteGrid, boolean deleteSubform, boolean abortProcess, boolean deleteFiles, boolean hardDelete);
}
