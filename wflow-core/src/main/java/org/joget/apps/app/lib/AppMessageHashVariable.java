package org.joget.apps.app.lib;

import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;

public class AppMessageHashVariable extends DefaultHashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        String label = AppUtil.replaceAppMessage(variableKey);
        if (label != null) {
            return label;
        }
        return null;
    }

    public String getName() {
        return "App Message Hash Variable";
    }

    public String getPrefix() {
        return "i18n";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "App Message Hash Variable";
    }

    public String getClassName() {
        return this.getClass().getName();
    }
    
    public String getPropertyOptions() {
        return "";
    }
}
