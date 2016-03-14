package org.joget.apps.datalist.model;

import org.joget.plugin.base.ExtDefaultPlugin;

/**
 * A base abstract class to develop a Datalist Filter Type plugin
 * 
 */
public abstract class DataListFilterTypeDefault extends ExtDefaultPlugin implements DataListFilterType {

    /**
     * Convenience method to get value from request parameters.
     * 
     * @param datalist
     * @param name
     * @return
     */
    public String getValue(DataList datalist, String name) {
        return getValue(datalist, name, null);
    }

    /**
     * Convenience method to get values from request parameters.
     * 
     * @param datalist
     * @param name
     * @return 
     */
    public String[] getValues(DataList datalist, String name) {
        return getValues(datalist, name, null);
    }
    
    /**
     * Convenience method to get value from request parameters.
     * 
     * @param datalist
     * @param name
     * @param defaultValue
     * @return default value if empty
     */
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

    /**
     * Convenience method to get values from request parameters.
     * 
     * @param datalist
     * @param name
     * @param defaultValue
     * @return default values if empty. default values can be separated by semicolon ";" if multiple value.
     */
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
