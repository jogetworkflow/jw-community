package org.joget.apps.datalist.model;

import org.joget.plugin.property.model.PropertyEditable;
import org.kecak.apps.datalist.DataJsonControllerDataListColumnHandler;

/**
 * Interface of Datalist Column Formatter plugin
 */
public interface DataListColumnFormat extends PropertyEditable, DataJsonControllerDataListColumnHandler {

    /**
     * Format column value 
     * 
     * @param dataList
     * @param column
     * @param row
     * @param value
     * @return
     */
    String format(DataList dataList, DataListColumn column, Object row, Object value);
}
