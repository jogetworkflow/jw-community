package org.joget.workflow.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.springframework.stereotype.Service;

@Service("workflowHelper")
public class MockWorkflowHelperImpl implements WorkflowHelper {

    public boolean executeTool(WorkflowAssignment assignment) {
        return true;
    }

    public List<String> getAssignmentUsers(String packageId, String procDefId, String procId, String version, String actId, String requesterUsername, String participantId) {
        return new ArrayList<String>();
    }

    public String processHashVariable(String content, WorkflowAssignment wfAssignment, String escapeFormat, Map<String, String> replaceMap) {
        return content;
    }

    public void addAuditTrail(String clazz, String method, String message) {
    }
    
    public void addAuditTrail(String clazz, String method, String message, Class[] paramTypes, Object[] args, Object returnObject) {
    }

    public WorkflowDeadline executeDeadlinePlugin(String processId, String activityId, WorkflowDeadline deadline, Date processStartedTime, Date activityAcceptedTime, Date activityActivatedTime) {
        return deadline;
    }
    
    public String getPublishedPackageVersion(String packageId){
        return null;
    }
}
