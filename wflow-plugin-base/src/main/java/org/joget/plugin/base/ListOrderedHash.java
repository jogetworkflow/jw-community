package org.joget.plugin.base;

import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import java.util.Map;
import org.apache.commons.collections.SequencedHashMap;
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
        if (map instanceof ListOrderedMap || map.getClass().getName().equals(ListOrderedMap.class.getName())) {
            Map newMap = new ListOrderedMap();
            newMap.putAll(map);
            return newMap;
        } else if (map instanceof SequencedHashMap || map.getClass().getName().equals(SequencedHashMap.class.getName())) {
            Map newMap = new SequencedHashMap();
            newMap.putAll(map);
            return newMap;
        } else {
            return super.copyMap(map);
        }
    }
}
