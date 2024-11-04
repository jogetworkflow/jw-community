package org.joget.workflow.model.dao;

import java.util.Collection;
import java.util.Set;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;

public interface WorkflowAssignmentDao {
    
    public Collection<WorkflowProcess> getProcesses(String packageId, String processDefId, String processId, String processName, String version, String recordId, String username, String state, String sort, Boolean desc, Integer start, Integer rows);
    
    public long getProcessesSize(String packageId, String processDefId, String processId, String processName, String version, String recordId, String username, String state);
    
    /**
     * Returns all the id of running process instances by requester
     * @return 
     */
    public Collection<String> getProcessIdsByRequester(String packageId, String processDefId, String username, String state);
    
    public String getRunningActivityIdByRecordId(String id, String processDefId, String activityDefId, String username);
    
    public WorkflowAssignment getAssignmentByRecordId(String id, String processDefId, String activityDefId, String username);
    
    public Set<String> getAssignmentProcessIds(String packageId, String processDefId, String processId, String activityDefId, String username, String state);
    
    public Collection<WorkflowAssignment> getAssignmentsByProcessIds(Collection<String> processIds, String username, String state, String sort, Boolean desc, Integer start, Integer rows);
    
    public Collection<WorkflowAssignment> getAssignments(String packageId, String processDefId, String processId, String activityDefId, String username, String state, String sort, Boolean desc, Integer start, Integer rows);
    
    public Collection<WorkflowActivity> getClosedActivities(String packageId, String processDefId, String processId, String activityDefId, String username, String state, String sort, Boolean desc, Integer start, Integer rows);
    
    public Collection<WorkflowActivity> getArchivedActivities(String packageId, String processDefId, String processId, String actDefId, String username, String state, String sort, Boolean desc, Integer start, Integer rows);

    public int getAssignmentSize(String packageId, String processDefId, String processId, String activityDefId, String username, String state);
    
    public int getClosedActivitiesSize(String packageId, String processDefId, String processId, String activityDefId, String username, String state);
    
    public int getArchivedActivitiesSize(String packageId, String processDefId, String processId, String actDefId, String username, String state);
    
    /**
     * Only stuck tools having "open.running" status during startup
     * @return 
     */
    public Collection<Object[]> getStuckTools();
    
    public Collection<String> getPackageDefIds(String packageId);
    
    /**
     * Migrate the process instance from one version to another version
     * @param packageId 
     * @return  
     */
    public Collection<String> getMigrateProcessInstances(String packageId);
    
    public Set<String> getUsedVersion(String packageId);
    
    /**
     * Migrate the process instance from one version to another version
     * @param processId 
     * @param newVersion 
     * @return  
     */
    public boolean migrateProcessInstance(String processId, String newVersion);
    
    /**
     * Used to check is there any completed process in shark table
     */
    public boolean hasNonHistoryCompletedProcess();
    
    public Collection<WorkflowProcess> getProcessHistories(String packageId, String processDefId, String processId, String processName, String version, String recordId, String username, String sort, Boolean desc, Integer start, Integer rows);
    
    public long getProcessHistoriesSize(String packageId, String processDefId, String processId, String processName, String version, String recordId, String username);
    
    public Collection<WorkflowActivity> getActivityHistories(String processId, String actDefId, String username, String sort, Boolean desc, Integer start, Integer rows);
    
    public long getActivityHistoriesSize(String processId, String actDefId, String username);
    
    public WorkflowProcess getProcessHistoryById(String processId);
    
    public WorkflowActivity getActivityHistoryById(String activityId);
    
    public void deleteProcessHistory(String processId);
}
