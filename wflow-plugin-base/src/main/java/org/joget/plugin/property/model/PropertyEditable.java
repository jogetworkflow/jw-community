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
     * @return
     */
    public String getLabel();

    /**
     * Return Class Name for the plugin.
     * @return
     */
    public abstract String getClassName();

    /**
     * Return the plugin properties options in JSON format.
     * @return
     */
    public String getPropertyOptions();
    
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
