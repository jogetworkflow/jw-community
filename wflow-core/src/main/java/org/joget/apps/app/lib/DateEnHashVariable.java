package org.joget.apps.app.lib;

/**
 * This class only for backward compatible for a Hash added during 8.0-BETA
 */
public class DateEnHashVariable extends DateHashVariable {
    
    @Override
    public String processHashVariable(String variableKey) {
        return getValue(variableKey, "en");
    }
    
    @Override
    public String getName() {
        return "Date Hash Variable";
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
}
