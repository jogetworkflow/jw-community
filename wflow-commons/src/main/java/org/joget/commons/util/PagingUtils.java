package org.joget.commons.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.support.PropertyComparator;

/**
 * Utility method used for sorting and paging an ordered list of data
 * 
 */
public class PagingUtils {

    private PagingUtils() {
    }

    /**
     * Sorts a list based on a field value of object in the list 
     * @param list
     * @param field
     * @param desc 
     */
    public static void sort(List list, String field, Boolean desc) {
        if (list != null && !list.isEmpty() && field != null) {
            boolean asc = (desc == null || !desc);
            
            if (list.get(0) instanceof Map) {
                final String f = field;
                final boolean d = desc;
                Collections.sort(list, new Comparator<Map<String, Object>>() {
                    public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                        if (m2.get(f) != null && m1.get(f) != null) {
                            if (d) {
                                return m2.get(f).toString().compareTo(m1.get(f).toString());
                            } else {
                                return m1.get(f).toString().compareTo(m2.get(f).toString());
                            }
                        } else if (d && m1.get(f) == null) {
                            return 1;
                        } else if (!d && m2.get(f) == null) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                });
            } else {
                Collections.sort(list, new PropertyComparator(field, true, asc));
            }
        }
    }
    
    /**
     * Sorts a map based on a value
     * @param map
     * @param desc 
     */
    public static Map sortMapByValue(Map map, Boolean desc) {
        // Convert Map to List
	List<Map.Entry> list = new LinkedList<Map.Entry>(map.entrySet());
        
        boolean asc = (desc == null || !desc);
        Collections.sort(list, new PropertyComparator("value", true, asc));
        
        // Convert sorted map back to a Map
        Map sortedMap = new LinkedHashMap();
        for (Map.Entry entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    /**
     * Gets the results of a page from a List
     * @param list
     * @param start
     * @param rows
     * @return 
     */
    public static List subList(List list, Integer start, Integer rows) {
        if (list == null) {
            return null;
        }
        int total = list.size();
        if (total > 0) {
            int begin = (start != null) ? start : 0;
            int end;
            if (begin < 0) {
                begin = 0;
            }
            if (rows == null || rows < 0) {
                end = total;
            } else {
                end = begin + rows;
            }
            if (end > total) {
                end = total;
            }
            List newList = list.subList(begin, end);
            return newList;
        } else {
            return new ArrayList();
        }
    }

    /**
     * Orders the list based on a field value of object in the list and returns 
     * the results of a page
     * @param list
     * @param field
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    public static List sortAndPage(List list, String field, Boolean desc, Integer start, Integer rows) {
        sort(list, field, desc);
        List newList = subList(list, start, rows);
        return newList;
    }
}
