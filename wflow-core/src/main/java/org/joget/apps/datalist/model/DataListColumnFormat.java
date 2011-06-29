package org.joget.apps.datalist.model;

/**
 * Formatter for columns
 */
public interface DataListColumnFormat extends DataListConfigurable {

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
