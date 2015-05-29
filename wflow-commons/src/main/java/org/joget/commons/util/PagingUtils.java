package org.joget.commons.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.support.PropertyComparator;

public class PagingUtils {

    private PagingUtils() {
    }

    public static void sort(List list, String field, Boolean desc) {
        if (list != null && field != null) {
            boolean asc = (desc == null || !desc);
            Collections.sort(list, new PropertyComparator(field, true, asc));
        }
    }

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

    public static List sortAndPage(List list, String field, Boolean desc, Integer start, Integer rows) {
        sort(list, field, desc);
        List newList = subList(list, start, rows);
        return newList;
    }
}
