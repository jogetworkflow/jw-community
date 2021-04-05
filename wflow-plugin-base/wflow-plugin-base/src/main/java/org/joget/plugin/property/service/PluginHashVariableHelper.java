package org.joget.plugin.property.service;

import java.util.Map;

public interface PluginHashVariableHelper {
    
    public Map<String, Object> getHashVariableSupportedMap(Map<String, Object> properties);
    
    public void setDisableHashVariable(Boolean disable);
}
