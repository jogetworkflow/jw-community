package org.joget.plugin.property.model;

import java.util.Map;

public interface PropertyEditable {
    /**
     * Label to be display
     * @return
     */
    public String getLabel();

    /**
     * Class name for the element.
     * @return
     */
    public abstract String getClassName();

    /**
     * JSON property options
     * @return
     */
    public String getPropertyOptions();
    
    public Map<String, Object> getProperties();
    
    public void setProperties(Map<String, Object> properties);
    
    public Object getProperty(String property);
    
    public String getPropertyString(String property);
    
    public void setProperty(String property, Object value);
}
