package org.joget.commons.util;

import au.com.bytecode.opencsv.CSVReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvUtil {

    public static final String DEFAULT_DELIMINATOR = ",";

    public static String getDeliminatedString(String[] array) {
        return getDeliminatedString(array, DEFAULT_DELIMINATOR);
    }

    public static String getDeliminatedString(String[] array, String deliminator) {
        return getDeliminatedString(array, deliminator, false);
    }

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
