package org.joget.apps.form.model;

/**
 * A Form Load Binder loads data from a data source for use in a form.
 */
public interface FormLoadBinder {

    /**
     * Loads data based on a specific row ID.
     * @param element The element to load the data into.
     * @param rowId
     * @return A Collection of Map objects. Each Map object contains property=value pairs to represent a data row.
     */
    public FormRowSet load(Element element, String primaryKey, FormData formData);
}
