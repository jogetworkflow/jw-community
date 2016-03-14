package org.joget.plugin.base;

import java.util.HashMap;
import java.util.Map;

public abstract class ExtDefaultPlugin extends DefaultPlugin {
    private Map<String, Object> properties;
    
    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
    public Object getProperty(String property) {
        Object value = (properties != null) ? properties.get(property) : null;
        return value;
    }
    
    public String getPropertyString(String property) {
        String value = (properties != null && properties.get(property) != null) ? (String) properties.get(property) : "";
        return value;
    }
    
    public void setProperty(String property, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put(property, value);
    }
    
    public PluginProperty[] getPluginProperties() {
        return null;
    }
    
    public Object execute(Map properties) {
        return null;
    }
}
