package org.joget.apps.datalist.lib;

import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListBinderDefault;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.commons.util.LogUtil;

public class BeanShellDatalistBinder extends DataListBinderDefault {
    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getName() {
        return "BeanShell Data Binder";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "Retrieves data rows using BeanShell script.";
    }

    @Override
    public String getLabel() {
        return "BeanShell Data Binder";
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/beanShellDataBinder.json", null, true, null);
    }
    
    @Override
    public DataListColumn[] getColumns() {
        Map properties = new HashMap();
        properties.put("plugin", this);
        return (DataListColumn[]) AppPluginUtil.executeScript(getPropertyString("getColumnScript"), properties);
    }
    
    @Override
    public String getPrimaryKeyColumnName() {
        return getPropertyString("primaryKey");
    }
    
    @Override
    public DataListCollection getData(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects, String sort, Boolean desc, Integer start, Integer rows) {
        properties.put("plugin", this);
        properties.put("dataList", dataList);
        properties.put("filterQueryObjects", filterQueryObjects);
        properties.put("sort", sort);
        properties.put("desc", desc);
        properties.put("start", start);
        properties.put("rows", rows);
        
        DataListCollection resultList = (DataListCollection) AppPluginUtil.executeScript(getPropertyString("getDataScript"), properties);
        return resultList;
    }
    
    @Override
    public int getDataTotalRowCount(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects) {
        properties.put("plugin", this);
        properties.put("dataList", dataList);
        properties.put("filterQueryObjects", filterQueryObjects);
        
        int total = 0;
        try {
            total = (int) AppPluginUtil.executeScript(getPropertyString("getTotalScript"), properties);
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "");
        }
        
        return total;
    }
    
    @Override
    public String getDeveloperMode() {
        return "advanced";
    }
}
