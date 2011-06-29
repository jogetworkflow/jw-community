package org.joget.apps.app.lib;

import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.model.HashVariablePlugin;
import org.joget.workflow.util.WorkflowUtil;

public class RequestParameterHashVariable extends HashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

        String value = request.getParameter(variableKey);

        if (value != null) {
            return value;
        } else {
            return "";
        }
    }

    public String getName() {
        return "RequestParameterHashVariable";
    }

    public String getPrefix() {
        return "requestParam";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String getDescription() {
        return "";
    }
}
