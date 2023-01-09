package org.joget.apps.app.lib;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.userview.model.Userview;
import org.joget.commons.spring.web.ParameterizedUrlHandlerMapping;
import org.joget.workflow.util.WorkflowUtil;

public class RequestParameterHashVariable extends DefaultHashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

        if (request != null) {
            String separator = ", ";
            if (variableKey.contains("[") && variableKey.contains("]")) {
                separator = variableKey.substring(variableKey.indexOf("[") + 1, variableKey.indexOf("]"));
                variableKey = variableKey.substring(0, variableKey.indexOf("["));
            }
            
            String[] value = request.getParameterValues(variableKey);

            if (value != null && value.length > 0) {
                return StringUtils.join(value, separator);
            } else {
                //get path parameter
                Map<String, String> params = (Map) request.getAttribute(ParameterizedUrlHandlerMapping.PATH_PARAMETERS);
                if (params != null && params.containsKey(variableKey)) {
                    String pathValue = params.get(variableKey);
                    
                    if (variableKey.equals("key") && pathValue != null && pathValue.equals(Userview.USERVIEW_KEY_EMPTY_VALUE)) {
                        pathValue = "";
                    }
                    return pathValue;
                }

                return "";
            }
        }
        return "";
    }

    public String getName() {
        return "Request Parameter Hash Variable";
    }

    public String getPrefix() {
        return "requestParam";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Request Parameter Hash Variable";
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
}
