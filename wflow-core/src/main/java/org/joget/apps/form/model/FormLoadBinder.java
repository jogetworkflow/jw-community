package org.joget.apps.form.model;

/**
 * A Form Load Binder loads data from a data source for use of an element and 
 * its children in a form.
 */
public interface FormLoadBinder {

    /**
     * Loads data based on a primary key.
     * @param element The element to load the data into.
     * @param primaryKey
     * @param formData
     * @return A Collection of Map objects. Each Map object contains property=value pairs to represent a data row.
     */
    public FormRowSet load(Element element, String primaryKey, FormData formData);
}
