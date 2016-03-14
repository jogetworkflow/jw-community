package org.joget.apps.datalist.model;

import org.joget.plugin.property.model.PropertyEditable;

/**
 * Interface to represent an action taken on a data list e.g. Edit, Delete, etc.
 */
public interface DataListAction extends PropertyEditable {

    /**
     * Descriptive name for the action
     */
    String getLinkLabel();

    /**
     * Optional link to a URL
     */
    String getHref();

    /**
     * Optional target to a URL
     */
    String getTarget();

    /**
     * Name of the parameter if linked, separated by semicolon ";" if has multiple.
     */
    String getHrefParam();

    /**
     * Name of the column to use for the param value if linked, separated by semicolon ";" if has multiple.
     */
    String getHrefColumn();

    /**
     * Message for confirmation
     */
    String getConfirmation();

    /**
     * Flag that decide to show an action object or not when no record
     */
    Boolean getVisibleOnNoRecord();

    /**
     * Method to perform the action on selected rows
     * @param dataList
     * @param rowKeys
     * @return
     */
    DataListActionResult executeAction(DataList dataList, String[] rowKeys);
}
