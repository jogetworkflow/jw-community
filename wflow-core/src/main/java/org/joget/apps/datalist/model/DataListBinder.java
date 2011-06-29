package org.joget.apps.datalist.model;

import java.util.Properties;

/**
 * Interface to represent a data source for a data list
 */
public interface DataListBinder extends DataListConfigurable {

    /**
     * Properties that to be configured for the binder in the builder
     * @return
     */
    DataListBuilderProperty[] getBuilderProperties();

    /**
     * Column types returned by the binder
     * @return
     */
    DataListColumn[] getColumns();

    /**
     * The primary key / identifier for the data
     * @return The name of the column that represents the primary key
     */
    String getPrimaryKeyColumnName();

    /**
     * The data rows returned by the binder.
     * @param dataList
     * @param properties
     * @param filter
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    DataListCollection getData(DataList dataList, Properties properties, String filterName, String filterValue, String sort, Boolean desc, int start, int rows);

    /**
     * Total number of rows returned based on the current filter.
     * @param dataList
     * @param properties
     * @param filterName
     * @param filterValue
     * @return
     */
    int getDataTotalRowCount(DataList dataList, Properties properties, String filterName, String filterValue);
}
