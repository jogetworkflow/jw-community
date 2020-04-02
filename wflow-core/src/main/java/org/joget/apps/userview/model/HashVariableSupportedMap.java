package org.joget.apps.userview.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.service.AppUtil;

public class HashVariableSupportedMap<K,V> extends HashMap<K,V> {
    
    
    public HashVariableSupportedMap(Map<? extends K, ? extends V> m) {
        super(m);
    }
    
    @Override
    public V get(Object key) {
        V object = super.get(key);
        
        if (object != null) {
            object = (V) parseHashVariable(object);
            super.put((K) key, object);
        }
        
        return object;        
    }
    
    protected Object parseHashVariable(Object object) {
        if (object instanceof String) { 
            return (V) AppUtil.processHashVariable(object.toString(), null, null, null);
        } else if (Map.class.isAssignableFrom(object.getClass())) {
            return (V) (new HashVariableSupportedMap((Map) object));
        } else if (object.getClass().isArray()) {
            Collection<Object> array = new ArrayList<Object>();
            for (Object t : (Object[]) object) {
                array.add(parseHashVariable(t));
            }
            return array.toArray();
        }
        
        return object;
    }
}
