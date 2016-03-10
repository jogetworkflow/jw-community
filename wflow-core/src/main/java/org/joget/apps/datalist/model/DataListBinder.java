package org.joget.apps.datalist.model;

import java.util.Map;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * Interface to represent a data source for a data list
 */
public interface DataListBinder extends PropertyEditable {

    /**
     * Columns meta returned by the binder. 
     * 
     * Field "name", "label" and "sortable" are needed by system
     * 
     * @return
     */
    DataListColumn[] getColumns();

    /**
     * The primary key / identifier column for the data
     * @return The name of the column that represents the primary key
     */
    String getPrimaryKeyColumnName();
    
    /**
     * To get the actual column name
     * @return The name of the column 
     */
    String getColumnName(String name);

    /**
     * The data rows returned by the binder based on the current filter.
     * @param dataList
     * @param properties
     * @param filter
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    DataListCollection getData(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Total number of rows returned based on the current filter.
     * @param dataList
     * @param properties
     * @param filterName
     * @param filterValue
     * @return
     */
    int getDataTotalRowCount(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects);
}
