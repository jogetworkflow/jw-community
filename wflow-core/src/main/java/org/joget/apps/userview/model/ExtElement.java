package org.joget.apps.userview.model;

import java.util.HashMap;
import java.util.Map;
import org.joget.plugin.base.DefaultPlugin;
import org.joget.plugin.base.PluginProperty;
import org.joget.plugin.property.model.PropertyEditable;

public abstract class ExtElement extends DefaultPlugin implements PropertyEditable {

    private Map requestParameters;
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

    public Map getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(Map requestParameters) {
        this.requestParameters = requestParameters;
    }

    /**
     * Convenience method to get a parameter value
     * @param parameter
     * @return
     */
    public Object getRequestParameter(String requestParameter) {
        Object value = (requestParameters != null) ? requestParameters.get(requestParameter) : null;
        return value;
    }

    /**
     * Convenience method to get a parameter String value
     * @param parameter
     * @return Empty string instead of null.
     */
    public String getRequestParameterString(String requestParameter) {
        String value = (requestParameters != null) ? (String) requestParameters.get(requestParameter) : "";
        return value;
    }

    @Override
    public PluginProperty[] getPluginProperties() {
        return null;
    }

    @Override
    public Object execute(Map properties) {
        return null;
    }
}
