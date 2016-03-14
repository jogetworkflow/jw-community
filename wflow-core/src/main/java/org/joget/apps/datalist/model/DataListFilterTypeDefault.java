package org.joget.apps.datalist.model;

import org.joget.plugin.base.ExtDefaultPlugin;

public abstract class DataListFilterTypeDefault extends ExtDefaultPlugin implements DataListFilterType {

    public String getValue(DataList datalist, String name) {
        return getValue(datalist, name, null);
    }

    public String[] getValues(DataList datalist, String name) {
        return getValues(datalist, name, null);
    }
    
    public String getValue(DataList datalist, String name, String defaultValue) {
        if (datalist != null) {
            String value = datalist.getDataListParamString(DataList.PARAMETER_FILTER_PREFIX + name);
            if (value == null && defaultValue != null && !defaultValue.isEmpty()) {
                return defaultValue;
            } else {
                return value;
            }
        }
        return null;
    }

    public String[] getValues(DataList datalist, String name, String defaultValue) {
        if (datalist != null) {
            String value[] = datalist.getDataListParam(DataList.PARAMETER_FILTER_PREFIX + name);
            if ((value == null || value.length == 0) && defaultValue != null && !defaultValue.isEmpty()) {
                return defaultValue.split(";");
            } else {
                return value;
            }
        }
        return null;
    }
}
