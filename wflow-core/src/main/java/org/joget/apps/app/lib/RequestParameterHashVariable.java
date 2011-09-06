package org.joget.apps.app.lib;

import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.workflow.util.WorkflowUtil;

public class RequestParameterHashVariable extends DefaultHashVariablePlugin {

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
