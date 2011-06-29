package org.joget.apps.app.lib;

import org.joget.apps.app.model.HashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;

public class WorkflowVariableHashVariable extends HashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        WorkflowAssignment wfAssignment = (WorkflowAssignment) this.getProperty("workflowAssignment");
        if (wfAssignment != null) {
            ApplicationContext appContext = AppUtil.getApplicationContext();
            WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");

            String varVal = workflowManager.getProcessVariable(wfAssignment.getProcessId(), variableKey);
            if (varVal != null) {
                return varVal;
            }
        }
        return null;
    }

    public String getName() {
        return "WorkflowVariableHashVariable";
    }

    public String getPrefix() {
        return "variable";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String getDescription() {
        return "";
    }
}
