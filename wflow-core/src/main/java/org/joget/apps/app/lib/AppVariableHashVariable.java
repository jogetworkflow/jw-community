package org.joget.apps.app.lib;

import org.joget.apps.app.service.AppUtil;

public class AppVariableHashVariable extends EnvironmentVariableHashVariable{
    @Override
    public String getName() {
        return "App Variable Hash Variable";
    }

    @Override
    public String getPrefix() {
        return "appVariable";
    }
    
    @Override
    public String getPropertyAssistantDefinition() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/assist/environmentVariableHashVariable.json", null, true, null);
    }
}
