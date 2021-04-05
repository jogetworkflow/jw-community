package org.joget.apps.app.service;

import java.util.Map;
import org.joget.apps.app.model.HashVariableSupportedMapImpl;
import org.joget.plugin.base.HashVariableSupportedMap;
import org.joget.plugin.property.service.PluginHashVariableHelper;
import org.springframework.stereotype.Service;

@Service("pluginHashVariableHelper")
public class PluginHashVariableHelperImpl implements PluginHashVariableHelper {
    static ThreadLocal disableHashVariable = new ThreadLocal();
    
    @Override
    public Map<String, Object> getHashVariableSupportedMap(Map<String, Object> map) {
        if (disableHashVariable.get() == null && !(map instanceof HashVariableSupportedMap)) {
            map = new HashVariableSupportedMapImpl(map);
        }
        return map;
    }
    
    @Override
    public void setDisableHashVariable(Boolean disable) {
        if (disable) {
            disableHashVariable.set(true);
        } else {
            disableHashVariable.set(null);
        }
    }
}
