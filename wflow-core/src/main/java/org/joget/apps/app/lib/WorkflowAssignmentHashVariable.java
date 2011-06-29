package org.joget.apps.app.lib;

import java.lang.reflect.Method;
import org.joget.apps.app.model.HashVariablePlugin;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowAssignment;

public class WorkflowAssignmentHashVariable extends HashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        WorkflowAssignment wfAssignment = (WorkflowAssignment) this.getProperty("workflowAssignment");
        if (wfAssignment != null) {
            char firstChar = variableKey.charAt(0);
            firstChar = Character.toUpperCase(firstChar);
            variableKey = firstChar + variableKey.substring(1, variableKey.length());

            try {
                Method method = WorkflowAssignment.class.getDeclaredMethod("get" + variableKey, new Class[]{});
                String returnResult = (String) method.invoke(wfAssignment, new Object[]{});
                if (returnResult != null) {
                    return returnResult;
                }
            } catch (Exception e) {
                LogUtil.error(WorkflowAssignmentHashVariable.class.getName(), e, "Error retrieving wfAssignment attribute ");
            }
        }
        return null;
    }

    public String getName() {
        return "WorkflowAssignmentHashVariable";
    }

    public String getPrefix() {
        return "assignment";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String getDescription() {
        return "";
    }
}
