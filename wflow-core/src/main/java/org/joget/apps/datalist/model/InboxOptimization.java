package org.joget.apps.datalist.model;

import java.util.Map;

public interface InboxOptimization {
    
    /**
     * Check is optimization supported
     * @return 
     */
    public boolean isOptimizationSupported();
    
    /**
     * The data rows returned by the binder based on the current filter.
     * 
     * @param dataList
     * @param properties
     * @param filterQueryObjects
     * @param filterType
     * @param processDefId
     * @param activityDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    DataListCollection getInboxData(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects, String filterType, String processDefId, String activityDefId, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Total number of rows returned based on the current filter.
     * @param dataList
     * @param properties
     * @param filterQueryObjects
     * @param filterType
     * @param processDefId
     * @param activityDefId
     * @return
     */
    int getInboxDataTotalRowCount(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects, String filterType, String processDefId, String activityDefId);
}
