package org.joget.apps.userview.model;

import java.util.HashMap;
import java.util.Map;
import org.joget.plugin.property.model.PropertyEditable;

public abstract class Element implements PropertyEditable {

    private Map<String, Object> properties;

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
        String value = (properties != null && properties.get(property) != null) ? (String) properties.get(property) : "";
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
    
    public String getLabel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getClassName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
