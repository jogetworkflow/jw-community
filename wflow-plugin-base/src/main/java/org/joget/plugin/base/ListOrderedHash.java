package org.joget.plugin.base;

import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import java.util.Map;
import org.apache.commons.collections.map.ListOrderedMap;

/**
 * Modified FreeMarker SimpleHash that supports ordering using ListOrderedMaps.
 */
public class ListOrderedHash extends SimpleHash {

    public ListOrderedHash() {
        super();
    }

    public ListOrderedHash(Map map) {
        super(map);
    }

    public ListOrderedHash(ObjectWrapper wrapper) {
        super(wrapper);
    }

    public ListOrderedHash(Map map, ObjectWrapper wrapper) {
        super(map, wrapper);
    }

    @Override
    protected Map copyMap(Map map) { 
        if (ListOrderedMap.class.isAssignableFrom(map.getClass())) {
            Map newMap = new ListOrderedMap();
            newMap.putAll(map);
            return newMap;
        } else if (HashVariableSupportedMap.class.isAssignableFrom(map.getClass())) {
            return (Map)((HashVariableSupportedMap) map).clone();
        } else {
            return super.copyMap(map);
        }
    }
}
