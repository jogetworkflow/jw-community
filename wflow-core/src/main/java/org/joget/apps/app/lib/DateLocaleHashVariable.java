package org.joget.apps.app.lib;

import java.util.ArrayList;
import java.util.Collection;
import org.joget.apps.app.service.AppUtil;

public class DateLocaleHashVariable extends DateHashVariable {
    @Override
    public String processHashVariable(String variableKey) {
        return getValue(variableKey, AppUtil.getAppLocale());
    }
    
    @Override
    public String getName() {
        return "Date Locale Hash Variable";
    }

    @Override
    public String getPrefix() {
        return "dateLocale";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getLabel() {
        return "Date Hash Variable following user locale";
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return "";
    }
    
    @Override
    public Collection<String> availableSyntax() {
        Collection<String> syntax = new ArrayList<String>();
        syntax.add("dateLocale.FORMAT");
        syntax.add("dateLocale.FORMAT|TIMEZONE");
        syntax.add("dateLocale.DAY+/-INTEGER.FORMAT");
        syntax.add("dateLocale.MONTH+/-INTEGER.FORMAT");
        syntax.add("dateLocale.YEAR+/-INTEGER.FORMAT");
        syntax.add("dateLocale.FORMAT|TIMEZONE[DATE_VALUE|DATE_VALUE_FORMAT|TIMEZONE]");
        syntax.add("dateLocale.DAY+/-INTEGER.FORMAT|TIMEZONE[DATE_VALUE|DATE_VALUE_FORMAT|TIMEZONE]");
        syntax.add("dateLocale.MONTH+/-INTEGER.FORMAT|TIMEZONE[DATE_VALUE|DATE_VALUE_FORMAT|TIMEZONE]");
        syntax.add("dateLocale.YEAR+/-INTEGER.FORMAT|TIMEZONE[DATE_VALUE|DATE_VALUE_FORMAT|TIMEZONE]");
        return syntax;
    }
    
    @Override
    public String getPropertyAssistantDefinition() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/assist/dateLocaleHashVariable.json", null, true, null);
    }
}
