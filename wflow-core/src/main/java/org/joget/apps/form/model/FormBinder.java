package org.joget.apps.form.model;

import java.util.HashMap;
import java.util.Map;
import org.joget.plugin.base.DefaultPlugin;

/**
 * Base class for form load and store binders
 * @author julian
 */
public abstract class FormBinder extends DefaultPlugin {

    public static final String FORM_LOAD_BINDER = "loadBinder";
    public static final String FORM_OPTIONS_BINDER = "optionsBinder";
    public static final String FORM_STORE_BINDER = "storeBinder";
    private String className;
    private Map<String, Object> properties;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

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
}
