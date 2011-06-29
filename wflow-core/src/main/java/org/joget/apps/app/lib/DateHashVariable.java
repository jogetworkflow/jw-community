package org.joget.apps.app.lib;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.joget.apps.app.model.HashVariablePlugin;

public class DateHashVariable extends HashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        try {
            Calendar cal = Calendar.getInstance();

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

            return new SimpleDateFormat(variableKey).format(cal.getTime());
        } catch (IllegalArgumentException iae) {
            return new Date().toString();
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public String escapeHashVariable(String variable) {
        return variable.replace("+", "\\+");
    }

    public String getName() {
        return "DateHashVariable";
    }

    public String getPrefix() {
        return "date";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String getDescription() {
        return "";
    }
}
