package org.joget.apps.datalist.model;

import org.joget.plugin.property.model.PropertyEditable;

/**
 * Formatter for columns
 */
public interface DataListColumnFormat extends PropertyEditable {

    /**
     *
     * @param dataList
     * @param column
     * @param row
     * @param value
     * @return
     */
    String format(DataList dataList, DataListColumn column, Object row, Object value);
}
