package org.joget.apps.datalist.model;

import java.util.Map;

public interface DataListInboxBinder {
    
    /**
     * Set the inbox setting
     * @param inboxSetting
     */
    void setDataListInboxSetting(DataListInboxSetting inboxSetting);
    
    /**
     * Flag used to decide using inbox method to retrieve data
     * @return 
     */
    boolean isInbox();
    
    /**
     * The data rows returned by the binder based on the current filter.
     * @param dataList
     * @param properties
     * @param filterQueryObjects
     * @param inboxSetting
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    DataListCollection getInboxData(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Total number of rows returned based on the current filter.
     * @param dataList
     * @param properties
     * @param filterQueryObjects
     * @param inboxSetting
     * @return
     */
    int getInboxDataTotalRowCount(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects);
}
