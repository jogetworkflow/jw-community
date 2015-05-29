package org.joget.commons.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    
    public static boolean validateDateFormat(String value, String format) {
        Boolean valid = true;

        if (value != null && !value.isEmpty() && format != null && !format.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                Date date = sdf.parse(value);
            } catch (Exception e) {
                valid = false;
            }
        }

        return valid;
    }
    
    public static boolean compare(String start, String end, String format) {
        Boolean valid = true;

        if (start != null && !start.isEmpty() && end != null && !end.isEmpty() && format != null && !format.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                Date startDate = sdf.parse(start);
                Date endDate = sdf.parse(end);
                
                if (startDate.after(endDate)) {
                    valid = false;
                }
                
            } catch (Exception e) {
                valid = false;
            }
        }

        return valid;
    }
}
