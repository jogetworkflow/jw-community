package org.joget.apps.datalist.model;

/**
 * Interface to represent an action taken on a data list e.g. Edit, Delete, etc.
 */
public interface DataListAction extends DataListConfigurable {

    /**
     * Descriptive name for the action
     */
    String getLabel();

    /**
     * Optional link to a URL
     */
    String getHref();

    /**
     * Optional target to a URL
     */
    String getTarget();

    /**
     * Name of the parameter if linked, defaults to the current column name if not specified.
     */
    String getHrefParam();

    /**
     * Name of the column to use for the param value, defaults to the current column name if not specified.
     */
    String getHrefColumn();

    /**
     * Message for confirmation
     */
    String getConfirmation();

    /**
     * Method to perform the action on selected rows
     * @param dataList
     * @param rowKeys
     * @return
     */
    DataListActionResult executeAction(DataList dataList, String[] rowKeys);
}
