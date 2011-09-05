package org.joget.apps.userview.model;

import java.util.Map;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.property.model.PropertyEditable;

public abstract class ExtElement extends ExtDefaultPlugin implements PropertyEditable {

    private Map requestParameters;

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
}
