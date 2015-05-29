package org.joget.apps.app.lib;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import org.joget.apps.app.model.DefaultHashVariablePlugin;

public class DateHashVariable extends DefaultHashVariablePlugin {

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

    public String getName() {
        return "Date Hash Variable";
    }

    public String getPrefix() {
        return "date";
    }

    public String getVersion() {
        return "3.0.0";
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
        
        return syntax;
    }
}
