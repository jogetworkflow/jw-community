package org.joget.apps.app.lib;

import org.joget.apps.app.model.PropertyAssistant;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.ExtDefaultPlugin;

public class RegexPropertyAssistant extends ExtDefaultPlugin implements PropertyAssistant {

    @Override
    public String getName() {
        return "RegexPropertyAssistant";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getPropertyAssistantDefinition() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/assist/regex.json", null, true, null);
    }
    
    @Override
    public Type getPropertyAssistantType() {
        return Type.REGEX;
    }
}
