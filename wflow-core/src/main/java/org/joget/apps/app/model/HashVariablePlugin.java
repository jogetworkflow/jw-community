package org.joget.apps.app.model;

import java.util.HashMap;
import java.util.Map;
import org.joget.plugin.base.DefaultPlugin;
import org.joget.plugin.base.PluginProperty;

public abstract class HashVariablePlugin extends DefaultPlugin {

    @Override
    public PluginProperty[] getPluginProperties() {
        return null;
    }

    @Override
    public Object execute(Map properties) {
        return null;
    }
    private Map<String, Object> properties = new HashMap<String, Object>();

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * Convenience method to get a property value
     * @param property
     * @return
     */
    public Object getProperty(String property) {
        Object value = (properties != null) ? properties.get(property) : null;
        return value;
    }

    /**
     * Convenience method to get a property String value
     * @param property
     * @return Empty string instead of null.
     */
    public String getPropertyString(String property) {
        String value = (properties != null) ? (String) properties.get(property) : "";
        return value;
    }

    /**
     * Convenience method to set a property value
     * @param property
     * @param value
     */
    public void setProperty(String property, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put(property, value);
    }

    public abstract String getPrefix();

    public abstract String processHashVariable(String variableKey);

    public String escapeHashVariable(String variable) {
        return variable;
    }
}
