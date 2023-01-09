package org.joget.commons.util;

import au.com.bytecode.opencsv.CSVReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility method used to deal with comma separated value (CSV)
 * 
 */
public class CsvUtil {

    public static final String DEFAULT_DELIMINATOR = ",";

    /**
     * Convert a String array as CSV
     * @param array
     * @return 
     */
    public static String getDeliminatedString(String[] array) {
        return getDeliminatedString(array, DEFAULT_DELIMINATOR);
    }

    /**
     * Convert a String array as CSV with custom deliminator
     * @param array
     * @param deliminator
     * @return 
     */
    public static String getDeliminatedString(String[] array, String deliminator) {
        return getDeliminatedString(array, deliminator, false);
    }

    /**
     * Convert a String array as CSV with custom deliminator. 
     * Option to remove empty String from result.
     * @param array
     * @param deliminator
     * @param ignoreEmptyValue
     * @return 
     */
    public static String getDeliminatedString(String[] array, String deliminator, boolean ignoreEmptyValue) {
        String result = "";

        for (String temp : array) {
            if (ignoreEmptyValue) {
                if (temp.length() > 0) {
                    result += temp + deliminator;
                }
            } else {
                result += temp + deliminator;
            }
        }

        //remove trailing deliminator
        if (result.length() > 0) {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * Retrieve plugin properties from a CSV string
     * 
     * @deprecated method used by Joget v2 to parse plugin properties from a CSV string. Since Joget v3,
     * Joget introduced a better UI for plugin configuration, the properties are store in JSON format.
     * 
     * @param propertyString
     * @return
     * @throws IOException 
     */
    public static Map<String, String> getPluginPropertyMap(String propertyString) throws IOException {
        Map propertyMap = new HashMap();
        if (propertyString != null && propertyString.trim().length() > 0) {
            CSVReader reader = new CSVReader(new StringReader(propertyString));
            List entries = reader.readAll();

            for (int i = 0; i < entries.size(); i++) {
                String[] entry = (String[]) entries.get(i);
                propertyMap.put(entry[0], entry[1]);
            }
        }
        return propertyMap;
    }
}
