package org.joget.commons.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.collections.map.ListOrderedMap;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Utility methods to deal with Time Zone
 * 
 */
public class TimeZoneUtil {

    private static String serverTimeZone;
    private static String serverTimeZoneId;
    public static ListOrderedMap list;

    private TimeZoneUtil() {
    }
    
    /**
     * Retrieve a list of Time Zone
     * @return a map of time zone id and its description
     */
    public static Map<String, String> getList() {
        if (list == null) {
            list = new ListOrderedMap();
            list.put("", "");
            list.put("-12", ResourceBundleUtil.getMessage("timezone.gmt.-12"));
            list.put("-11", ResourceBundleUtil.getMessage("timezone.gmt.-11"));
            list.put("-10", ResourceBundleUtil.getMessage("timezone.gmt.-10"));
            list.put("-9", ResourceBundleUtil.getMessage("timezone.gmt.-9"));
            list.put("-8", ResourceBundleUtil.getMessage("timezone.gmt.-8"));
            list.put("-7", ResourceBundleUtil.getMessage("timezone.gmt.-7"));
            list.put("-6", ResourceBundleUtil.getMessage("timezone.gmt.-6"));
            list.put("-5", ResourceBundleUtil.getMessage("timezone.gmt.-5"));
            list.put("-4", ResourceBundleUtil.getMessage("timezone.gmt.-4"));
            list.put("-3.5", ResourceBundleUtil.getMessage("timezone.gmt.-3.5"));
            list.put("-3", ResourceBundleUtil.getMessage("timezone.gmt.-3"));
            list.put("-2", ResourceBundleUtil.getMessage("timezone.gmt.-2"));
            list.put("-1", ResourceBundleUtil.getMessage("timezone.gmt.-1"));
            list.put("0", ResourceBundleUtil.getMessage("timezone.gmt.0"));
            list.put("1", ResourceBundleUtil.getMessage("timezone.gmt.1"));
            list.put("2", ResourceBundleUtil.getMessage("timezone.gmt.2"));
            list.put("3", ResourceBundleUtil.getMessage("timezone.gmt.3"));
            list.put("3.5", ResourceBundleUtil.getMessage("timezone.gmt.3.5"));
            list.put("4", ResourceBundleUtil.getMessage("timezone.gmt.4"));
            list.put("4.5", ResourceBundleUtil.getMessage("timezone.gmt.4.5"));
            list.put("5", ResourceBundleUtil.getMessage("timezone.gmt.5"));
            list.put("5.5", ResourceBundleUtil.getMessage("timezone.gmt.5.5"));
            list.put("5.75", ResourceBundleUtil.getMessage("timezone.gmt.5.75"));
            list.put("6", ResourceBundleUtil.getMessage("timezone.gmt.6"));
            list.put("6.5", ResourceBundleUtil.getMessage("timezone.gmt.6.5"));
            list.put("7", ResourceBundleUtil.getMessage("timezone.gmt.7"));
            list.put("8", ResourceBundleUtil.getMessage("timezone.gmt.8"));
            list.put("9", ResourceBundleUtil.getMessage("timezone.gmt.9"));
            list.put("9.5", ResourceBundleUtil.getMessage("timezone.gmt.9.5"));
            list.put("10", ResourceBundleUtil.getMessage("timezone.gmt.10"));
            list.put("11", ResourceBundleUtil.getMessage("timezone.gmt.11"));
            list.put("12", ResourceBundleUtil.getMessage("timezone.gmt.12"));
            
            String[] timezones = TimeZone.getAvailableIDs();
            List<String> sortedKeys=new ArrayList<String>(Arrays.asList(timezones));
            Collections.sort(sortedKeys);
            
            ListOrderedMap otherList = new ListOrderedMap();
            for (String tzid : sortedKeys) {
                TimeZone tz = TimeZone.getTimeZone(tzid);
                if (!tz.getDisplayName(true, TimeZone.SHORT).startsWith("GMT")) {
                    String display = "(" + tz.getDisplayName(true, TimeZone.SHORT) + ") ";
                    String tzname = tzid.replace("/", " - ");
                    tzname = tzname.replace("_", " ");
                    display += tzname;
                    otherList.put(tzid, display);
                }
            }
            
            list.putAll(otherList);
        }

        return list;
    }

    /**
     * Retrieve Server Time Zone in GMT format
     * @return GMT Time Zone 
     */
    public static String getServerTimeZone() {
        if (serverTimeZone == null) {
            serverTimeZone = Integer.toString(TimeZone.getDefault().getRawOffset() / (60 * 60 * 1000));
        }

        return serverTimeZone;
    }
    
    /**
     * Retrieve Server Time Zone ID 
     * @return 
     */
    public static String getServerTimeZoneID() {
        if (serverTimeZoneId == null) {
            serverTimeZoneId = TimeZone.getDefault().getID();
        }

        return serverTimeZoneId;
    }
    
    /**
     * Reset the default timezone back to its initial value
     */
    public static void resetDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(getServerTimeZoneID()));
    }

    /**
     * Convert Date to String based on GMT/Timezone ID and Date Format
     * @param time Datetime to convert
     * @param gmt GMT ("-12" to "12") or Timezone ID, NULL to use System/User selected timezone
     * @param format Date Format
     * @return Date in converted String 
     */
    public static String convertToTimeZone(Date time, String gmt, String format) {
        return convertToTimeZone(time, gmt, format, null);
    }
    
    /**
     * Convert Date to String based on GMT/Timezone ID and Date Format
     * @param time Datetime to convert
     * @param gmt GMT ("-12" to "12") or Timezone ID, NULL to use System/User selected timezone
     * @param format Date Format
     * @param locale
     * @return Date in converted String 
     */
    public static String convertToTimeZone(Date time, String gmt, String format, String locale) {
        if (time == null) {
            return "";
        }
        
        Locale userLocale = LocaleContextHolder.getLocale();
        if (locale != null && !locale.isEmpty()) {
            try {
                String[] temp = locale.split("_");
                switch (temp.length) {
                    case 1:
                        userLocale = new Locale(temp[0]);
                        break;
                    case 2:
                        userLocale = new Locale(temp[0], temp[1]);
                        break;
                    case 3:
                        userLocale = new Locale(temp[0], temp[1], temp[2]);
                        break;
                    default:
                        userLocale = LocaleContextHolder.getLocale();
                        break;
                }
            } catch (Exception e) {
                userLocale = LocaleContextHolder.getLocale();
            }
        }
        
        if (format == null || format.trim().length() == 0) {
            format = ResourceBundleUtil.getMessage("console.setting.general.default.systemDateFormat");
        }
        SimpleDateFormat dateFormat;
        try {
            dateFormat = new SimpleDateFormat(format, userLocale);
        } catch (Exception e) {
            dateFormat = new SimpleDateFormat(ResourceBundleUtil.getMessage("console.setting.general.default.systemDateFormat"), userLocale);
        }
        
        if (gmt != null && !gmt.isEmpty()) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(getTimeZoneByGMT(gmt)));
        } else {
            dateFormat.setTimeZone(LocaleContextHolder.getTimeZone());
        }
        
        return dateFormat.format(time);
    }

    /**
     * Get Time Zone ID by GMT 
     * @param gmt GMT ("-12" to "12")
     * @return Time Zone ID
     */
    public static String getTimeZoneByGMT(String gmt) {
        if (gmt != null && gmt.trim().length() > 0) {

            String tz = null;

            if (gmt.contains(".")) {
                Double rawoffset = Double.parseDouble(gmt) * 60 * 60 * 1000;
                tz = TimeZone.getAvailableIDs(rawoffset.intValue())[0];
            } else {
                try {
                    if (Integer.parseInt(gmt) > 0) {
                        gmt = "+" + gmt;
                    }
                    tz = TimeZone.getTimeZone("GMT" + gmt).getID();
                } catch (NumberFormatException e) {
                    tz = gmt;
                }
            }

            if (tz != null && tz.trim().length() > 0) {
                return tz;
            }
        }

        return TimeZone.getDefault().getID();
    }
}
