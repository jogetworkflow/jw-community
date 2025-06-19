package org.joget.plugin.property.model;

import java.util.Map;

/**
 * A interface that must be implemented by a plugin to provide admin interface to configure the plugin
 * 
 */
public interface PropertyEditable {
    /**
     * Return plugin label. This value will be used when a Resource Bundle 
     * Message Key "<i>plugin.className</i>.pluginlabel" is not found by getI18nLabel() method.
     * 
     * Default implementation added for old plugin that still using getPluginProperties method to provide plugin
     * configuration options and didn't implement PropertyEditable
     * 
     * @return
     */
    default public String getLabel() {
        return "";
    }

    /**
     * Return Class Name for the plugin.
     * @return
     */
    public abstract String getClassName();

    /**
     * Return the plugin properties options in JSON format.
     * 
     * Default implementation added for old plugin that still using getPluginProperties method to provide plugin
     * configuration options and didn't implement PropertyEditable
     * 
     * @return
     */
    default public String getPropertyOptions() {
        return "";
    }
    
    /**
     * Get plugin properties.
     * @return 
     */
    public Map<String, Object> getProperties();
    
    /**
     * Set plugin properties.
     * @param properties 
     */
    public void setProperties(Map<String, Object> properties);
    
    /**
     * Get a plugin property value by property key.
     * 
     * @param property 
     */
    public Object getProperty(String property);
    
    /**
     * Get a plugin property value by property key and return in java.lang.String. Non-exist key 
     * will return an empty string instead of NULL value.
     * 
     * @param property 
     */
    public String getPropertyString(String property);
    
    /**
     * Set a plugin property
     * 
     * @param property A property key
     * @param value 
     */
    public void setProperty(String property, Object value);
}
