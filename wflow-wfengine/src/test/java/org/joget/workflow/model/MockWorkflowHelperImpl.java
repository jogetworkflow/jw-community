package org.joget.workflow.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.springframework.stereotype.Service;

@Service("workflowHelper")
public class MockWorkflowHelperImpl implements WorkflowHelper {

    public boolean executeTool(WorkflowAssignment assignment) {
        return true;
    }
    
    public DecisionResult executeDecisionPlugin(String processDefId, String processId, String routeId, String routeActId, Map<String, String> variables) {
        return null;
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

    public Map<String, Collection<String>> getReplacementUsers(String username) {
        return null;
    }
    
    public Map<String, String> getPublishedPackageVersions() {
        return new HashMap<String, String>();
    }

    public void updateAppDefinitionForDeadline(String processId, String packageId, String packageVersion) {
        
    }

    @Override
    public String translateProcessLabel(String processId, String processDefId, String activityDefId, String defaultLabel) {
        return defaultLabel;
    }
    
    public void cleanDeadlineAppDefinitionCache(String packageId, String packageVersion) {
        
    }
    
    public void cleanForDeadline() {
    
    }
}
