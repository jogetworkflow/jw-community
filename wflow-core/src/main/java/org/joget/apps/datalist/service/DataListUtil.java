package org.joget.apps.datalist.service;

import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.datalist.lib.StaleCacheDataListBinder;
import org.joget.apps.datalist.model.DataListBinder;
import org.joget.apps.datalist.model.DataListBinderDefault;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListInboxBinder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataListUtil {

    /**
     * Returns a hash as required for {@link StaleCacheDataListBinder} to be used as a unique identifier.
     *
     * <p>This implementation should be sufficient for most List Binders that only construct queries based on its
     * properties.
     *
     * @return a hash String
     */
    public static String getStaleCacheKey(AppDefinition appDef, DataListBinder binder, Object[] additionalParams) {
        String appId = "";
        if (appDef != null) {
            appId = appDef.getId();
        }

        DataListFilterQueryObject inboxFilter = null;
        if (binder instanceof DataListInboxBinder) {
            inboxFilter = ((DataListBinderDefault) binder).buildInboxCondition();
        }

        Object[] arr = new Object[]{appId, binder.getProperties(), inboxFilter, additionalParams};
        int hash = getHash(arr);
        return String.valueOf(hash);
    }

    /**
     * Returns a hash of an object that contains nested Arrays, Maps, and Iterables.
     *
     * <p>Note: This method uses Java's {@link Object#hashCode} implementation. Therefore, in order for an object to be
     * properly and accurately hashed, they should have a concrete implementation of the {@code hashCode()} method.
     *
     * @param o the object that contains supported nested types
     * @return a hash code of the object
     */
    public static int getHash(Object o) {
        return getHashInternal(o, null);
    }

    private static int getHashInternal(Object o, Set<Object> visited) {
        int hash = 1;
        if (o == null) {
            return 0;
        }

        // check if o is visited to prevent cyclic references
        if (visited == null) {
            visited = new HashSet<>();
        }
        if (visited.contains(o)) {
            // return the object's native hashcode to denote it is found cyclically
            return o.hashCode();
        }
        visited.add(o);

        // use prime multiplication for better hash code distribution
        if (o.getClass().isArray()) {
            Class<?> componentType = o.getClass().getComponentType();
            if (componentType.isPrimitive()) {
                // Handle primitive arrays
                if (o instanceof int[]) {
                    for (int i : (int[]) o) {
                        hash = 31 * hash + Integer.hashCode(i);
                    }
                } else if (o instanceof long[]) {
                    for (long l : (long[]) o) {
                        hash = 31 * hash + Long.hashCode(l);
                    }
                } else if (o instanceof double[]) {
                    for (double d : (double[]) o) {
                        hash = 31 * hash + Double.hashCode(d);
                    }
                } else if (o instanceof char[]) {
                    for (char c : (char[]) o) {
                        hash = 31 * hash + Character.hashCode(c);
                    }
                } else if (o instanceof boolean[]) {
                    for (boolean b : (boolean[]) o) {
                        hash = 31 * hash + Boolean.hashCode(b);
                    }
                } else if (o instanceof byte[]) {
                    for (byte b : (byte[]) o) {
                        hash = 31 * hash + Byte.hashCode(b);
                    }
                } else if (o instanceof short[]) {
                    for (short s : (short[]) o) {
                        hash = 31 * hash + Short.hashCode(s);
                    }
                } else if (o instanceof float[]) {
                    for (float f : (float[]) o) {
                        hash = 31 * hash + Float.hashCode(f);
                    }
                }
            } else {
                // Handle object arrays
                for (Object arrObj : (Object[]) o) {
                    hash = 31 * hash + getHashInternal(arrObj, visited);
                }
            }
        } else if (o instanceof Map) {
            for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
                hash = 31 * hash + (31 * getHashInternal(e.getKey(), visited) + getHashInternal(e.getValue(), visited));
            }
        } else if (o instanceof Iterable) {
            for (Object iterObj : (Iterable<?>) o) {
                hash = 31 * hash + getHashInternal(iterObj, visited);
            }
        } else {
            hash = o.hashCode();
        }
        return hash;
    }
}
