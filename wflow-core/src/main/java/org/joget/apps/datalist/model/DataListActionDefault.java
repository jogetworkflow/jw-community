package org.joget.apps.datalist.model;

import java.util.HashMap;
import java.util.Map;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.service.PropertyUtil;

/**
 * Base class for a data list action
 */
public abstract class DataListActionDefault extends ExtDefaultPlugin implements DataListAction {
    private DataList datalist;
    private static Map<String, String> defaultPropertyValues = new HashMap<String, String>();

    public DataList getDatalist() {
        return datalist;
    }

    public void setDatalist(DataList datalist) {
        this.datalist = datalist;
    }
    
    /**
     * Flag that decide to show an action object or not when no record
     * 
     * Default to the value of property "visible".
     * 
     * @return 
     */
    public Boolean getVisibleOnNoRecord() {
        if (getPropertyString("visible") != null && "true".equalsIgnoreCase(getPropertyString("visible"))) {
            return true;
        } else {
            return false;
        }
    }
    
    public Boolean supportColumn() {
        return true;
    }
    
    public Boolean supportRow() {
        return true;
    }
    
    public Boolean supportList() {
        return true;
    }
    
    public String getIcon() {
        return "";
    }
    
    public String getDefaultPropertyValues(){
        if (!DataListActionDefault.defaultPropertyValues.containsKey(getClassName())) {
            DataListActionDefault.defaultPropertyValues.put(getClassName(), PropertyUtil.getDefaultPropertyValues(getPropertyOptions()));
        }
        return DataListActionDefault.defaultPropertyValues.get(getClassName());
    }
}
