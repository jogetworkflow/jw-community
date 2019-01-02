package org.joget.workflow.model.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowDeadline;

/**
 * Helper methods required by workflow engine for processing during runtime
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
     * Add an audit trail record and trigger audit trail event
     * @param clazz
     * @param method
     * @param message
     * @return
     */
    void addAuditTrail(String clazz, String method, String message);
    
    /**
     * Add an audit trail record and trigger audit trail event
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
    
    /**
     * Get replacement users replaced by an user
     * @param username
     * @return
     */
    Map<String, Collection<String>> getReplacementUsers(String username);
    
    /**
     * Get all package id and version 
     * @return
     */
    Map<String, String> getPublishedPackageVersions();
    
    /**
     * Clean the current app definition cache for deadline
     * @param packageId
     * @param packageVersion
     */
    void cleanDeadlineAppDefinitionCache(String packageId, String packageVersion);
    
    /**
     * Update current app definition based on process instance
     * @param processId
     * @param packageId
     * @param packageVersion
     */
    void updateAppDefinitionForDeadline(String processId, String packageId, String packageVersion);
    
}
