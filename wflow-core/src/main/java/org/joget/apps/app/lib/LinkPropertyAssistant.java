package org.joget.apps.app.lib;

import org.joget.apps.app.model.PropertyAssistant;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.ExtDefaultPlugin;

public class LinkPropertyAssistant extends ExtDefaultPlugin implements PropertyAssistant {

    @Override
    public String getName() {
        return "LinkPropertyAssistant";
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
        return AppUtil.readPluginResource(getClass().getName(), "/properties/assist/link.json", null, true, null);
    }
    
    @Override
    public PropertyAssistant.Type getPropertyAssistantType() {
        return PropertyAssistant.Type.URL;
    }
}
