package org.joget.apps.app.lib;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.commons.spring.web.ParameterizedUrlHandlerMapping;
import org.joget.workflow.util.WorkflowUtil;

public class RequestParameterHashVariable extends DefaultHashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

        if (request != null) {
            String value = request.getParameter(variableKey);

            if (value != null) {
                return value;
            } else {
                //get path parameter
                Map<String, String> params = (Map) request.getAttribute(ParameterizedUrlHandlerMapping.PATH_PARAMETERS);
                if (params != null && params.containsKey(variableKey)) {
                    return params.get(variableKey);
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
        return "3.0.0";
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
