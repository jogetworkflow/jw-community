package org.joget.commons.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.collections.map.ListOrderedMap;

public class TimeZoneUtil {

    private static String serverTimeZone;
    public static ListOrderedMap list;

    private TimeZoneUtil() {
    }

    public static Map<String, String> getList() {
        if (list == null) {
            list = new ListOrderedMap();

            list.put("-12", "(GMT -12:00) Eniwetok, Kwajalein");
            list.put("-11", "(GMT -11:00) Midway Island, Samoa");
            list.put("-10", "(GMT -10:00) Hawaii");
            list.put("-9", "(GMT -09:00) Alaska");
            list.put("-8", "(GMT -08:00) Pacific Time (US & Canada), Tijuana");
            list.put("-7", "(GMT -07:00) Mountain Time (US & Canada), Arizona");
            list.put("-6", "(GMT -06:00) Central Time (US & Canada), Mexico City");
            list.put("-5", "(GMT -05:00) Eastern Time (US & Canada), Bogota, Lima, Quito");
            list.put("-4", "(GMT -04:00) Atlantic Time (Canada), Caracas, La Paz");
            list.put("-3.5", "(GMT -03:30) Newfoundland");
            list.put("-3", "(GMT -03:00) Brassila, Buenos Aires, Georgetown, Falkland Is");
            list.put("-2", "(GMT -02:00) Mid-Atlantic, Ascension Is., St. Helena");
            list.put("-1", "(GMT -01:00) Azores, Cape Verde Islands");
            list.put("0", "(GMT  00:00) Casablanca, Dublin, Edinburgh, London, Lisbon, Monrovia");
            list.put("1", "(GMT +01:00) Amsterdam, Berlin, Brussels, Madrid, Paris, Rome");
            list.put("2", "(GMT +02:00) Cairo, Helsinki, Kaliningrad, South Africa");
            list.put("3", "(GMT +03:00) Baghdad, Riyadh, Moscow, Nairobi");
            list.put("3.5", "(GMT +03:30) Tehran");
            list.put("4", "(GMT +04:00) Abu Dhabi, Baku, Muscat, Tbilisi");
            list.put("4.5", "(GMT +04:30) Kabul");
            list.put("5", "(GMT +05:00) Ekaterinburg, Islamabad, Karachi, Tashkent");
            list.put("5.5", "(GMT +05:30) Bombay, Calcutta, Madras, New Delhi");
            list.put("5.75", "(GMT +05:45) Katmandu");
            list.put("6", "(GMT +06:00) Almaty, Colombo, Dhaka, Novosibirsk");
            list.put("6.5", "(GMT +06:30) Rangoon");
            list.put("7", "(GMT +07:00) Bangkok, Hanoi, Jakarta");
            list.put("8", "(GMT +08:00) Beijing, Hong Kong, Perth, Kuala Lumpur, Singapore, Taipei");
            list.put("9", "(GMT +09:00) Osaka, Sapporo, Seoul, Tokyo, Yakutsk");
            list.put("9.5", "(GMT +09:30) Adelaide, Darwin");
            list.put("10", "(GMT +10:00) Canberra, Guam, Melbourne, Sydney, Vladivostok");
            list.put("11", "(GMT +11:00) Magadan, New Caledonia, Solomon Islands");
            list.put("12", "(GMT +12:00) Auckland, Wellington, Fiji, Marshall Island");
            
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

    public static String getServerTimeZone() {
        if (serverTimeZone == null) {
            serverTimeZone = Integer.toString(TimeZone.getDefault().getRawOffset() / (60 * 60 * 1000));
        }

        return serverTimeZone;
    }

    public static String convertToTimeZone(Date time, String gmt, String format) {

        if (format == null || format.trim().length() == 0) {
            format = "dd-MM-yyyy hh:mm aa";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);

        dateFormat.setTimeZone(TimeZone.getTimeZone(getTimeZoneByGMT(gmt)));

        return dateFormat.format(time);
    }

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
