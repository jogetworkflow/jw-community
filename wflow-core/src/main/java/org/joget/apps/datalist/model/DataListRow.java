package org.joget.apps.datalist.model;

import java.util.HashMap;
import org.joget.apps.datalist.service.DataListService;

/**
 * A wrapper class to wrap the list row object into a map object, 
 * so that can add extra data to the row
 */
public class DataListRow extends HashMap<String, Object> {
    
    protected Object row; 
    
    public DataListRow(Object row) {
        super();
        this.row = row;
    }
    
    
    public Object get(String key) {
        if (containsKey(key)) {
            return super.get(key);
        } else {
            return DataListService.evaluateColumnValueFromRow(row, key); 
        }
    }
}
