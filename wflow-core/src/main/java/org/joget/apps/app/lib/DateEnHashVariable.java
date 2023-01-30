package org.joget.apps.app.lib;

import java.util.ArrayList;
import java.util.Collection;
import org.joget.apps.app.service.AppUtil;

public class DateEnHashVariable extends DateHashVariable {
    
    @Override
    public String processHashVariable(String variableKey) {
        return getValue(variableKey, "en");
    }
    
    @Override
    public String getName() {
        return "Date English Locale Hash Variable";
    }

    @Override
    public String getPrefix() {
        return "dateEN";
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
        return "Date English Locale Hash Variable";
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
        syntax.add("dateEN.FORMAT");
        syntax.add("dateEN.FORMAT|TIMEZONE");
        syntax.add("dateEN.DAY+/-INTEGER.FORMAT");
        syntax.add("dateEN.MONTH+/-INTEGER.FORMAT");
        syntax.add("dateEN.YEAR+/-INTEGER.FORMAT");
        syntax.add("dateEN.FORMAT|TIMEZONE[DATE_VALUE|DATE_VALUE_FORMAT|TIMEZONE]");
        syntax.add("dateEN.DAY+/-INTEGER.FORMAT|TIMEZONE[DATE_VALUE|DATE_VALUE_FORMAT|TIMEZONE]");
        syntax.add("dateEN.MONTH+/-INTEGER.FORMAT|TIMEZONE[DATE_VALUE|DATE_VALUE_FORMAT|TIMEZONE]");
        syntax.add("dateEN.YEAR+/-INTEGER.FORMAT|TIMEZONE[DATE_VALUE|DATE_VALUE_FORMAT|TIMEZONE]");
        return syntax;
    }
    
    @Override
    public String getPropertyAssistantDefinition() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/assist/dateENHashVariable.json", null, true, null);
    }
}
