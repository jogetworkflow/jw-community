package org.joget.apps.datalist.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.joget.plugin.base.ExtDefaultPlugin;

/**
 * Convenient abstract base class for binders to inherit
 */
public abstract class DataListBinderDefault extends ExtDefaultPlugin implements DataListBinder {

    public static String USERVIEW_KEY_SYNTAX = "#userviewKey#";
    
    @Override
    public String getColumnName(String name) {
        return name;
    }
    
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
