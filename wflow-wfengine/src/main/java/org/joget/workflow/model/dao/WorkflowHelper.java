package org.joget.workflow.model.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowDeadline;

/**
 * Helper methods required for processing during runtime
 * e.g. plugins to execute for a tool, assignees for a participant, processing hash variables, etc.
 */
public interface WorkflowHelper {

    /**
     * Execute a tool for a specific assignment.
     * @param assignment
     * @return
     */
    boolean executeTool(WorkflowAssignment assignment);

    /**
     * Retrieve a list of assignees for a participant in a process.
     * @param packageId
     * @param procDefId
     * @param procId
     * @param version
     * @param actId
     * @param requesterUsername
     * @param participantId
     * @return A List of usernames.
     */
    List<String> getAssignmentUsers(String packageId, String procDefId, String procId, String version, String actId, String requesterUsername, String participantId);

    /**
     * Processes a string to parse hash variables
     * @param content
     * @param wfAssignment
     * @param escapeFormat
     * @param replaceMap
     * @return
     */
    String processHashVariable(String content, WorkflowAssignment wfAssignment, String escapeFormat, Map<String, String> replaceMap);

    /**
     * add an audit trail record
     * @param clazz
     * @param method
     * @param message
     * @return
     */
    void addAuditTrail(String clazz, String method, String message);
    
    /**
     * add an audit trail record
     * @param clazz
     * @param method
     * @param message
     * @param paramTypes
     * @param args
     * @param returnObject
     * @return
     */
    void addAuditTrail(String clazz, String method, String message, Class[] paramTypes, Object[] args, Object returnObject);

    /**
     * Execute Deadline Plugin
     * @param processId
     * @param ActivityId
     * @param deadline
     * @param processStartedTime
     * @param activityAcceptedTime
     * @param activityActivatedTime
     * @return
     */
    WorkflowDeadline executeDeadlinePlugin(String processId, String activityId, WorkflowDeadline deadline, Date processStartedTime, Date activityAcceptedTime, Date activityActivatedTime);
    
    /**
     * Get published package version 
     * @param packageId
     * @return
     */
    String getPublishedPackageVersion(String packageId);
}
