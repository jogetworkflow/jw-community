package org.joget.apps.datalist.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.joget.plugin.base.ExtDefaultPlugin;

/**
 * A base abstract class to develop a Datalist Binder Plugin
 */
public abstract class DataListBinderDefault extends ExtDefaultPlugin implements DataListBinder {

    public static final String USERVIEW_KEY_SYNTAX = "#userviewKey#";
    private DataList datalist;

    public DataList getDatalist() {
        return datalist;
    }

    public void setDatalist(DataList datalist) {
        this.datalist = datalist;
    }
    
    /**
     * To get the actual column name
     * @param name
     * @return 
     */
    @Override
    public String getColumnName(String name) {
        return name;
    }
    
    /**
     * Construct filter conditions
     * 
     * @param filterQueryObjects
     * @return 
     */
    public DataListFilterQueryObject processFilterQueryObjects(DataListFilterQueryObject[] filterQueryObjects) {
        DataListFilterQueryObject obj = new DataListFilterQueryObject();
        String condition = "";
        Collection<String> values = new ArrayList<String>();
        for (int i = 0; i < filterQueryObjects.length; i++) {
            if (condition.isEmpty()) {
                obj.setOperator(filterQueryObjects[i].getOperator());
            } else {
                condition += " " + filterQueryObjects[i].getOperator() + " ";
            }
            condition += filterQueryObjects[i].getQuery();
            if (filterQueryObjects[i].getValues() != null && filterQueryObjects[i].getValues().length > 0) {
                values.addAll(Arrays.asList(filterQueryObjects[i].getValues()));
            }
        }
        obj.setQuery(condition);
        if (values.size() > 0){
            obj.setValues((String[]) values.toArray(new String[0]));
        }
        return obj;
    }
}
