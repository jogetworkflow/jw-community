package org.joget.apps.form.model;

/**
 * A Form Store Binder stores data from an element and its children into a data source.
 */
public interface FormStoreBinder {

    /**
     * Stores data in the data source.
     * @param element The element from which the data originates.
     * @param rows This is the data to be stored. A Collection of Map objects. Each Map object contains property=value pairs to represent a data row.
     * @param formData Contains form data eg request parameters, primary key, foreign key, etc.
     * @return The rowset representing the data rows created/updated. Return null if nothing is stored.
     */
    public FormRowSet store(Element element, FormRowSet rows, FormData formData);
}
