package org.joget.apps.userview.model;

import java.util.Map;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.model.PropertyEditable;

/**
 * A base abstract class used by Userview related plugin. Use the sub abstract class 
 * of corresponding plugin types in stead of this abstract class.
 * 
 */
public abstract class ExtElement extends ExtDefaultPlugin implements PropertyEditable {

    private Map requestParameters;
    
    /**
     * Gets request parameters
     * @return 
     */
    public Map getRequestParameters() {
        return requestParameters;
    }

    /**
     * Sets request parameters
     * @param requestParameters 
     */
    public void setRequestParameters(Map requestParameters) {
        this.requestParameters = requestParameters;
    }

    /**
     * Convenience method to get a parameter value
     * @param requestParameter
     * @return
     */
    public Object getRequestParameter(String requestParameter) {
        Object value = (requestParameters != null) ? requestParameters.get(requestParameter) : null;
        return value;
    }

    /**
     * Convenience method to get a parameter String value
     * @param requestParameter
     * @return Empty string instead of null.
     */
    public String getRequestParameterString(String requestParameter) {
        String result = null;
        Object value = (requestParameters != null) ? requestParameters.get(requestParameter) : "";
        if (value instanceof String) {
            result = (String)value;
        } else if (value instanceof String[] && ((String[])value).length > 0) {
            result = ((String[])value)[0];
        }
        return result;
    }
}
