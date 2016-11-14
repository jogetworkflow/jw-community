package org.joget.apps.app.lib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.TimeZoneUtil;

public class DateHashVariable extends DefaultHashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        try {
            Calendar cal = Calendar.getInstance();
            
            if (variableKey.contains("[") && variableKey.contains("]")) {
                String date = variableKey.substring(variableKey.indexOf("[") + 1, variableKey.indexOf("]"));
                variableKey = variableKey.substring(0, variableKey.indexOf("["));

                if (!date.isEmpty()) {
                    try {
                        String format = "yyyy-MM-dd";
                        if (date.contains("|")) {
                            format = date.substring(date.indexOf("|") + 1);
                            date = date.substring(0, date.indexOf("|"));
                        }

                        DateFormat df = new SimpleDateFormat(format);
                        Date result =  df.parse(date);  
                        cal.setTime(result);
                    } catch (Exception e) {
                        LogUtil.error(DateHashVariable.class.getName(), e, "");
                    }
                }
            }

            int field = -1;
            if (variableKey.contains("YEAR")) {
                field = Calendar.YEAR;
                variableKey = variableKey.replace("YEAR", "");
            } else if (variableKey.contains("MONTH")) {
                field = Calendar.MONTH;
                variableKey = variableKey.replace("MONTH", "");
            } else if (variableKey.contains("DAY")) {
                field = Calendar.DATE;
                variableKey = variableKey.replace("DAY", "");
            }

            if (field != -1) {
                String amount = variableKey.substring(0, variableKey.indexOf("."));
                variableKey = variableKey.replace(amount + ".", "");

                amount = amount.replace("+", "");

                cal.add(field, Integer.parseInt(amount));
            }

            return TimeZoneUtil.convertToTimeZone(cal.getTime(), null, variableKey);
        } catch (IllegalArgumentException iae) {
            return new Date().toString();
        } catch (Exception ex) {
            return null;
        }
    }

    public String getName() {
        return "Date Hash Variable";
    }

    public String getPrefix() {
        return "date";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Date Hash Variable";
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
    
    @Override
    public Collection<String> availableSyntax() {
        Collection<String> syntax = new ArrayList<String>();
        syntax.add("date.FORMAT");
        syntax.add("date.DAY+INTEGER.FORMAT");
        syntax.add("date.DAY-INTEGER.FORMAT");
        syntax.add("date.MONTH+INTEGER.FORMAT");
        syntax.add("date.MONTH-INTEGER.FORMAT");
        syntax.add("date.YEAR+INTEGER.FORMAT");
        syntax.add("date.YEAR-INTEGER.FORMAT");
        syntax.add("date.FORMAT[DATE_VALUE|DATE_VALUE_FORMAT]");
        syntax.add("date.DAY+INTEGER.FORMAT[DATE_VALUE|DATE_VALUE_FORMAT]");
        syntax.add("date.DAY-INTEGER.FORMAT[DATE_VALUE|DATE_VALUE_FORMAT]");
        syntax.add("date.MONTH+INTEGER.FORMAT[DATE_VALUE|DATE_VALUE_FORMAT]");
        syntax.add("date.MONTH-INTEGER.FORMAT[DATE_VALUE|DATE_VALUE_FORMAT]");
        syntax.add("date.YEAR+INTEGER.FORMAT[DATE_VALUE|DATE_VALUE_FORMAT]");
        syntax.add("date.YEAR-INTEGER.FORMAT[DATE_VALUE|DATE_VALUE_FORMAT]");
        return syntax;
    }
}
