package org.joget.apps.datalist.model;

import org.joget.plugin.base.ExtDefaultPlugin;

public abstract class DataListFilterTypeDefault extends ExtDefaultPlugin implements DataListFilterType {

    public String getValue(DataList datalist, String name) {
        if (datalist != null) {
            return datalist.getDataListParamString(DataList.PARAMETER_FILTER_PREFIX + name);
        }
        return null;
    }

    public String[] getValues(DataList datalist, String name) {
        if (datalist != null) {
            return datalist.getDataListParam(DataList.PARAMETER_FILTER_PREFIX + name);
        }
        return null;
    }
}
