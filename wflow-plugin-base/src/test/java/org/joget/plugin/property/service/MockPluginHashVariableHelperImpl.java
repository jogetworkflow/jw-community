package org.joget.plugin.property.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service("pluginHashVariableHelper")
public class MockPluginHashVariableHelperImpl implements PluginHashVariableHelper {

    @Override
    public Map<String, Object> getHashVariableSupportedMap(Map<String, Object> arg0) {
        return arg0;
    }

    @Override
    public void setDisableHashVariable(Boolean arg0) {
        
    }
    
}
