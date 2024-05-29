package org.joget.workflow.model.service;

import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowTool;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowParticipant;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowVariable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joget.commons.util.PagedList;
import org.joget.workflow.model.WorkflowPackage;
import org.joget.workflow.model.WorkflowProcessLink;
import org.joget.workflow.model.WorkflowProcessResult;
import org.joget.workflow.shark.migrate.model.MigrateActivity;
import org.joget.workflow.shark.migrate.model.MigrateProcess;

/**
 * Service methods to interact with workflow engine
 * 
 */
public interface WorkflowManager {
    public static String LATEST = "latest";

    /**
     * Set the workflow variable based on an activity instance ID.
     * @param activityInstanceId
     * @param variableId
     * @param variableValue 
     */
    void activityVariable(String activityInstanceId, String variableId, Object variableValue);

    /**
     * Set the workflow variable based on an process instance ID.
     * @param processInstanceId
     * @param variableId
     * @param variableValue 
     */
    void processVariable(String processInstanceId, String variableId, Object variableValue);
    
    /**
     * Set the workflow variables based on an activity instance ID.
     * @param activityInstanceId
     * @param variables 
     */
    void activityVariables(String activityInstanceId, Map<String, String> variables);

    /**
     * Set the workflow variables based on an process instance ID.
     * @param processInstanceId
     * @param variables 
     */
    void processVariables(String processInstanceId, Map<String, String> variables);

    /**
     * Returns the variable value based on a process instance ID.
     * @param processInstanceId
     * @param variableId
     * @return 
     */
    String getProcessVariable(String processInstanceId, String variableId);

    /**
     * Accept an assignment (for the current user) based on the activity instance ID.
     * 
     * @deprecated Since v3, the concept of accept & withdraw assignment is removed  
     * 
     * @param activityId 
     */
    void assignmentAccept(String activityId);

    /**
     * Complete an assignment (for the current user) based on the activity instance ID.
     * @param activityId 
     */
    void assignmentComplete(String activityId);

    /**
     * Complete an assignment (for the current user) while setting workflow variable values
     * @param activityId
     * @param variableMap 
     */
    void assignmentComplete(String activityId, Map<String, String> variableMap);

    /**
     * Force completes an activity
     * @param processDefId
     * @param activityId
     * @param processId
     */
    public void activityForceComplete(String processDefId, String processId, String activityId);
    
    /**
     * Abort an activity based on the process instance Id and activity definition ID.
     * @param processId
     * @param activityDefId 
     */
    void activityAbort(String processId, String activityDefId);

    /**
     * Start a specific activity for a running process instance.
     * @param processId
     * @param activityDefId
     * @param abortRunningActivities
     * @return 
     */
    boolean activityStart(String processId, String activityDefId, boolean abortRunningActivities);
    
    /**
     * Start a specific activity for a running process instance.
     * @param processId
     * @param activityDefId
     * @param usernames
     * @param abortRunningActivities
     * @return 
     */
    boolean activityStartAndAssignTo(String processId, String activityDefId, String[] usernames, boolean abortRunningActivities);

    /**
     * Set workflow variable value based on activity instance ID. 
     * This only works when the current user is assigned to the activity.
     * 
     * @param activityId
     * @param variableName
     * @param variableValue 
     */
    void assignmentVariable(String activityId, String variableName, String variableValue);

    /**
     * Set workflow variables value based on activity instance ID. 
     * This only works when the current user is assigned to the activity.
     * 
     * @param activityId
     * @param variableMap 
     */
    void assignmentVariables(String activityId, Map<String, String> variableMap);

    /**
     * Withdraw an assignment (for the current user) based on the activity instance ID.
     * 
     * @deprecated Since v3, the concept of accept & withdraw assignment is removed.  
     * 
     * @param activityId 
     */
    void assignmentWithdraw(String activityId);

    /**
     * Reassigns the assignment from a user to another user
     * @param processDefId
     * @param processId
     * @param activityId
     * @param username
     * @param replaceUser user to be replaced
     */
    void assignmentReassign(String processDefId, String processId, String activityId, String username, String replaceUser);
    
    /**
     * Complete current assignment with replaceUser and reassigns the new assignment to a user
     * @param processDefId
     * @param processId
     * @param activityId
     * @param username
     * @param replaceUser user to be replaced
     */
    void completeAssignmentAndReassign(String processDefId, String processId, String activityId, String username, String replaceUser);

    /**
     * Complete an assignment and start a activity
     * @param processDefId
     * @param processId
     * @param activityId
     * @param startActivityDefId
     * 
     */
    public void completeAssignmentAndStart(String processDefId, String processId, String activityId, String startActivityDefId);
    
    /**
     * Complete an assignment of a single assignee
     * @param processDefId
     * @param processId
     * @param activityId
     * 
     */
    public void completeSingleAssignment(String processDefId, String processId, String activityId);
        
    /**
     * Force completes an assignment of a user
     * @param processDefId
     * @param processId
     * @param activityId
     * @param username 
     */
    void assignmentForceComplete(String processDefId, String processId, String activityId, String username);

    /**
     * Returns an activity instance based on the activity instance ID.
     * @param activityId
     * @return 
     */
    WorkflowActivity getActivityById(String activityId);
    
    /**
     * Returns latest activity instance based on the process instance ID and activity definition id.
     * @param processId
     * @param actDefId
     * @return 
     */
    WorkflowActivity getActivityByProcess(String processId, String actDefId);

    /**
     * Returns a list of running or completed activities for a process instance ID.
     * @param processId
     * @param start
     * @param rows
     * @param sort
     * @param desc
     * @return 
     */
    Collection<WorkflowActivity> getActivityList(String processId, Integer start, Integer rows, String sort, Boolean desc);

    /**
     * Returns the number of running or completed activities for a process instance ID.
     * @param processId
     * @return 
     */
    int getActivitySize(String processId);

    /**
     * Returns a list of workflow variables for the specified activity instance ID (for any user)
     * @param activityId
     * @return 
     */
    Collection<WorkflowVariable> getActivityVariableList(String activityId);

    /**
     * Returns a list of workflow variables for the specified process instance ID (for any user)
     * @param processId
     * @return 
     */
    Collection<WorkflowVariable> getProcessVariableList(String processId);

    /**
     * Check an assignment is exist or not (for current user) based on an activity instance ID.
     * @param activityId
     * @return 
     */
    Boolean isAssignmentExist(String activityId);

    /**
     * Returns an assignment for the current user based on an activity instance ID.
     * @param activityId
     * @return 
     */
    WorkflowAssignment getAssignment(String activityId);

    /**
     * Returns a mock assignment based on an activity instance ID.
     * @param activityId
     * @return 
     */
    WorkflowAssignment getMockAssignment(String activityId); // TODO: VERIFY USAGE??

    /**
     * Returns the first assignment for the current user based on a process instance ID.
     * @param processId
     * @return 
     */
    WorkflowAssignment getAssignmentByProcess(String processId);
    
    /**
     * Returns a list of assignments for the current user.
     * @param accepted
     * @param processDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<WorkflowAssignment> getAssignmentList(Boolean accepted, String processDefId, String sort, Boolean desc, Integer start, Integer rows); // REFACTOR??

    /**
     * Returns a list of assignments for the current user.
     * @param packageId
     * @param processDefId
     * @param processId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<WorkflowAssignment> getAssignmentList(String packageId, String processDefId, String processId, String sort, Boolean desc, Integer start, Integer rows); // REFACTOR??

    /**
     * Returns a list of assignments for the current user.
     * @param packageId
     * @param processDefId
     * @param processId
     * @param activityDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<WorkflowAssignment> getAssignmentList(String packageId, String processDefId, String processId, String activityDefId, String sort, Boolean desc, Integer start, Integer rows); // REFACTOR??
    
    /**
     * Returns list of process id based on running assignments for the current user.
     * @param packageId
     * @param processDefId
     * @param processId
     * @param activityDefId
     * @return 
     */
    Set<String> getAssignmentProcessIds(String packageId, String processDefId, String processId, String activityDefId);
    
    /**
     * Returns list of running assignments for the current user based on process ids.
     * @param processIds
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<WorkflowAssignment> getAssignmentsByProcessIds(Collection<String> processIds, String sort, Boolean desc, Integer start, Integer rows);
    
    /**
     * Returns a list of assignments with lite info for the current user.
     * 
     * @param packageId
     * @param processDefId
     * @param processId
     * @param activityDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<WorkflowAssignment> getAssignmentListLite(String packageId, String processDefId, String processId, String activityDefId, String sort, Boolean desc, Integer start, Integer rows); // REFACTOR??

    /**
     * Returns a list of closed activities for the user by state.
     *
     * @param packageId
     * @param processDefId
     * @param processId
     * @param activityDefId
     * @param username
     * @param state the sub state of the closed state. If null or empty, all sub states are selected
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<WorkflowActivity> getClosedActivitiesList(String packageId, String processDefId, String processId, String activityDefId, String username, String state, String sort, Boolean desc, Integer start, Integer rows); // REFACTOR??

    /**
     * Returns a list of assignments for the current user filter by processDefIds.
     * @param processDefIds
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<WorkflowAssignment> getAssignmentListFilterByProccessDefIds(String[] processDefIds, String sort, Boolean desc, Integer start, Integer rows); // REFACTOR??

    /**
     * Returns the number of assignments for the current user.
     * @param accepted
     * @param processDefId
     * @return 
     */
    int getAssignmentSize(Boolean accepted, String processDefId); // TODO: REFACTOR??

    /**
     * Returns the number of assignments for the current user.
     * @param packageId
     * @param processDefId
     * @param processId
     * @return 
     */
    int getAssignmentSize(String packageId, String processDefId, String processId); // TODO: REFACTOR??
    
    /**
     * Returns the number of assignments for the current user.
     * @param packageId
     * @param processDefId
     * @param processId
     * @param activityDefId
     * @return 
     */
    int getAssignmentSize(String packageId, String processDefId, String processId, String activityDefId); // TODO: REFACTOR??
    
    /**
     * Returns the number of closed activities for the user by state.
     *
     * @param packageId
     * @param processDefId
     * @param processId
     * @param activityDefId
     * @param username
     * @param state the sub state of the closed state. If null or empty, all sub states are selected
     * @return 
     */ 
    int getClosedActivitiesListSize(String packageId, String processDefId, String processId, String activityDefId, String username, String state); 

    /**
     * Returns the number of assignments for the current user filter by processDefIds.
     * @param processDefIds
     * @return 
     */
    int getAssignmentListFilterByProccessDefIdsSize(String[] processDefIds); // TODO: VERIFY USAGE??

    /**
     * Returns the all (pending and accepted) assignments for the current user.
     * @param packageId
     * @param processDefId
     * @param processId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    PagedList<WorkflowAssignment> getAssignmentPendingAndAcceptedList(String packageId, String processDefId, String processId, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Returns pending assignments for the current user
     * @param processDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    PagedList<WorkflowAssignment> getAssignmentPendingList(String processDefId, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Returns accepted assignments for the current user
     * 
     * @deprecated Since v3, the concept of accept & withdraw assignment is removed.  
     * 
     * @param processDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    PagedList<WorkflowAssignment> getAssignmentAcceptedList(String processDefId, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Gets a map of active activities in a process instance.
     * 
     * @deprecated Since v3, the concept of accept & withdraw assignment is removed.  
     * 
     * @param processId
     * @param accepted
     * @return 
     */
    Map getActivityInstanceByProcessIdAndStatus(String processId, Boolean accepted); // TODO: VERIFY USAGE??

    /**
     * Returns a list of workflow variables for the specified activity instance ID (only if assigned to the current user)
     * @param activityId
     * @return 
     */
    Collection<WorkflowVariable> getAssignmentVariableList(String activityId);

    /**
     * Returns the name of the user that accepted/completed activity.
     * @param processDefId Unused for now
     * @param processId
     * @param activityDefId
     * @return
     */
    String getUserByProcessIdAndActivityDefId(String processDefId, String processId, String activityDefId);

    /**
     * Checks to see whether or not package exists.
     * @param packageId
     * @return true if the package exists, false otherwise.
     */
    Boolean isPackageIdExist(String packageId);

    /**
     * Returns a list of packages currently in the system.
     * @return
     */
    Collection<WorkflowPackage> getPackageList();

    /**
     * Retrieve a specific workflow package.
     * @param packageId
     * @param version
     * @return
     */
    WorkflowPackage getPackage(String packageId, String version);

    /**
     * Returns the latest package version for the given package ID
     * @param packageId
     * @return null if the package is not available.
     */
    String getCurrentPackageVersion(String packageId);

    /**
     * Returns the XPDL content for a package version.
     * @param packageId
     * @param version
     * @return
     */
    byte[] getPackageContent(String packageId, String version);

    /**
     * Returns the participant definitions for a process definition ID in a map.
     * @param processDefId
     * @return
     */
    Map<String, WorkflowParticipant> getParticipantMap(String processDefId);

    /**
     * Returns a process definition by its definition ID.
     * @param processDefId
     * @return
     */
    WorkflowProcess getProcess(String processDefId);

    /**
     * Returns the activity definitions for a process definition ID and actvity definition ID.
     * @param processDefId
     * @param activityDefId
     * @return
     */
    WorkflowActivity getProcessActivityDefinition(String processDefId, String activityDefId);

    /**
     * Returns the activity definitions for a process definition ID.
     * @param processDefId
     * @return
     */
    Collection<WorkflowActivity> getProcessActivityDefinitionList(String processDefId);

    /**
     * Returns the application definitions for a process definition ID.
     * @param processDefId
     * @return
     */
    Collection<WorkflowTool> getProcessToolDefinitionList(String processDefId);

    /**
     * Returns a process definition ID based on a process instance ID.
     * @param instanceId
     * @return
     */
    String getProcessDefIdByInstanceId(String instanceId); // TODO: VERIFY USAGE??

    /**
     * Returns a list of process definitions.
     * @param packageId Optional, to show only processes with the specified package ID
     * @return
     */
    Collection<WorkflowProcess> getProcessList(String packageId); // TODO: REFACTOR??

    /**
     * Returns a list of process definitions.
     * @param packageId Optional, to show only processes with the specified package ID
     * @param version Optional, to show only for the specified version
     * @return
     */
    Collection<WorkflowProcess> getProcessList(String packageId, String version); // TODO: REFACTOR??

    /**
     * Returns a list of process definitions
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @param packageId
     * @param all
     * @param checkWhiteList
     * @return
     */
    PagedList<WorkflowProcess> getProcessList(String sort, Boolean desc, Integer start, Integer rows, String packageId, Boolean all, Boolean checkWhiteList); // TODO: REFACTOR??

    /**
     * Returns the participant definitions for a process definition ID.
     * @param processDefId
     * @return
     */
    Collection<WorkflowParticipant> getProcessParticipantDefinitionList(String processDefId);

    /**
     * Returns the variable definitions for a process definition ID.
     * @param processId
     * @return
     */
    Collection<WorkflowVariable> getProcessVariableDefinitionList(String processDefId);

    /**
     * Returns a running process by process instance ID.
     * @param processId
     * @return
     */
    WorkflowProcess getRunningProcessById(String processId);
    
    /**
     * Returns a processDefId of a running process by process instance ID.
     * @param processId
     * @return
     */
    String getProcessDefId(String processId);
    
    /**
     * Returns all the id of running process instances
     * @return 
     */
    Collection<String> getRunningProcessIds();
    
    /**
     * Returns all the id of running process instances by requester
     * @param packageId
     * @param processDefId
     * @param username
     * @return 
     */
    public Collection<String> getRunningProcessIdsByRequester(String packageId, String processDefId, String username);

    /**
     * Returns a list of running processes, filtered by optional parameter values.
     * @param packageId
     * @param processId
     * @param processName
     * @param version
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<WorkflowProcess> getRunningProcessList(String packageId, String processId, String processName, String version, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Returns a list of running processes, filtered by optional parameter values.
     * @param packageId
     * @param processId
     * @param processName
     * @param version
     * @param recordId
     * @param requester
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    Collection<WorkflowProcess> getRunningProcessList(String packageId, String processId, String processName, String version, String recordId, String requester, String sort, Boolean desc, Integer start, Integer rows);
    
    /**
     * Returns a list of completed processes, filtered by optional parameter values.
     * @param packageId
     * @param processId
     * @param processName
     * @param version
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<WorkflowProcess> getCompletedProcessList(String packageId, String processId, String processName, String version, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Returns a list of completed processes, filtered by optional parameter values.
     * @param packageId
     * @param processId
     * @param processName
     * @param version
     * @param recordId
     * @param requester
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    Collection<WorkflowProcess> getCompletedProcessList(String packageId, String processId, String processName, String version, String recordId, String requester, String sort, Boolean desc, Integer start, Integer rows);
    
    /**
     * Returns the number of running processes, filtered by optional parameter values.
     * @param packageId
     * @param processId
     * @param processName
     * @param version
     * @return 
     */
    int getRunningProcessSize(String packageId, String processId, String processName, String version);
    
    /**
     * Returns the number of running processes, filtered by optional parameter values.
     * @param packageId
     * @param processId
     * @param processName
     * @param version
     * @param recordId
     * @param requester
     * @return 
     */
    int getRunningProcessSize(String packageId, String processId, String processName, String version, String recordId, String requester);

    /**
     * Returns the number of completed processes, filtered by optional parameter values.
     * @param packageId
     * @param processId
     * @param processName
     * @param version
     * @return 
     */
    int getCompletedProcessSize(String packageId, String processId, String processName, String version);
    
    /**
     * Returns the number of completed processes, filtered by optional parameter values.
     * @param packageId
     * @param processId
     * @param processName
     * @param version
     * @param recordId
     * @param requester
     * @return 
     */
    int getCompletedProcessSize(String packageId, String processId, String processName, String version, String recordId, String requester);

    /**
     * Method used by system to get WorkflowUserManager implementation
     * @return 
     */
    WorkflowUserManager getWorkflowUserManager();

    /**
     * Deletes a specific package version together with its process instances.
     * @param packageId
     * @param version
     */
    void processDeleteAndUnloadVersion(String packageId, String version);

    /**
     * Deletes all versions for a package together with its associated process instances.
     * @param packageId
     */
    void processDeleteAndUnload(String packageId);

    /**
     * Create a process instance without starting any activities.
     * @param processDefId
     * @return The created process instance ID
     */
    String processCreateWithoutStart(String processDefId);

    /**
     * Starts a process based on the process definition ID.
     * @param processDefId
     * @return
     */
    WorkflowProcessResult processStart(String processDefId); // TODO: REFACTOR??

    /**
     * Starts a process based on the process definition ID, while setting workflow variables values
     * @param processDefId
     * @param variables
     * @return
     */
    WorkflowProcessResult processStart(String processDefId, Map<String, String> variables); // TODO: REFACTOR??

    /**
     * Starts a process based on the process definition ID, while setting workflow variables values and start process username.
     * @param processDefId
     * @param variables
     * @param startProcUsername
     * @return
     */
    WorkflowProcessResult processStart(String processDefId, Map<String, String> variables, String startProcUsername); // TODO: REFACTOR??

    /**
     * Generic method to start a process with various options
     * @param processDefId The process definition ID of the process to start
     * @param processId The process instance ID of a current running process to start
     * @param variables Workflow variables values to set for the process
     * @param startProcUsername The username of the person starting the process
     * @param parentProcessId The process instance ID of a parent or calling process
     * @param startManually Set to true to prevent beginning activities from being started.
     * @return
     */
    WorkflowProcessResult processStart(String processDefId, String processId, Map<String, String> variables, String startProcUsername, String parentProcessId, boolean startManually); // TODO: REFACTOR??

    /**
     * Starts a process based on the process definition ID, while setting workflow variables values, start process username and parent process id.
     * @param processDefId
     * @param variables
     * @param startProcUsername
     * @param parentProcessId
     * @return
     */
    WorkflowProcessResult processStartWithInstanceId(String processDefId, String processId, Map<String, String> variables); // TODO: REFACTOR??

    /**
     * Starts a process based on the process definition ID, while setting workflow variables values, start process username and parent process id.
     * @param processDefId
     * @param variables
     * @param startProcUsername
     * @param parentProcessId
     * @return
     */
    WorkflowProcessResult processStartWithLinking(String processDefId, Map<String, String> variables, String startProcUsername, String parentProcessId); // TODO: REFACTOR??

    /**
     * Start a new process while copying variables, form data and running activities from a previous running process instance.
     * @param currentProcessId The current running process instance
     * @param newProcessDefId The new process definition ID to start
     * @param abortCurrentProcess Set to true to abort the current running process
     * @return
     */
    WorkflowProcessResult processCopyFromInstanceId(String currentProcessId, String newProcessDefId, boolean abortCurrentProcess);

    /**
     * Abort a process instance.
     * @param processId
     * @return
     */
    boolean processAbort(String processId);

    /**
     * Upload a package XPDL without updating mapping information.
     * @param packageId
     * @param processDefinitionData
     * @return
     * @throws Exception
     */
    String processUploadWithoutUpdateMapping(String packageId, byte[] processDefinitionData) throws Exception; // TODO: VERIFY USAGE??

    /**
     * Upload a package XPDL together with forms, participant and activity mapping information.
     * @param packageId
     * @param processDefinitionData
     * @return
     * @throws Exception
     */
    String processUpload(String packageId, byte[] processDefinitionData) throws Exception;

    /**
     * Reads package ID from XPDL definition
     * @param processDefinitionData
     * @return
     */
    String getPackageIdFromDefinition(byte[] processDefinitionData);

    /**
     * Reevaluate assignments for an activity based on an activity instance ID.
     * @param activityInstanceId
     */
    void reevaluateAssignmentsForActivity(String activityInstanceId);

    /**
     * Reevaluate assignments for a process based on an process instance ID.
     * @param procInstanceId
     */
    void reevaluateAssignmentsForProcess(String procInstanceId);

    /**
     * Reevaluate assignments for an array of processes based on the process instance IDs.
     * @param procInstanceId
     */
    void reevaluateAssignmentsForProcesses(String[] procInstanceId);

    /**
     * Reevaluate assignments for an user
     * @param username
     */
    void reevaluateAssignmentsForUser(String username);

    /**
     * Deletes a process instance.
     * @param procInstanceId
     */
    void removeProcessInstance(String procInstanceId);

    /**
     * Internal method used to delete a process instance only if it is completed.
     * @param procInstanceId 
     */
    void internalRemoveProcessOnComplete(String procInstanceId);

    /**
     * Gets the service level for a specific activity instance ID.
     * @param activityInstanceId
     * @return
     */
    double getServiceLevelMonitorForRunningActivity(String activityInstanceId);

    /**
     * Gets the service level for a specific process instance ID.
     * @param processInstanceId
     * @return
     */
    double getServiceLevelMonitorForRunningProcess(String processInstanceId);

    /**
     * Method used by system to sets WorkflowUserManager implementation
     * @param userManager 
     */
    void setWorkflowUserManager(WorkflowUserManager userManager);

    /**
     * Returns activity monitoring info (eg date creation, limit, due (creation + limit), delay and completion) for a process instance ID.
     * @param activityInstanceId
     * @return
     */
    WorkflowActivity getRunningActivityInfo(String activityInstanceId);

    /**
     * Returns process monitoring info (eg date creation, due dates, etc) for a process instance ID.
     * @param processInstanceId
     * @return
     */
    WorkflowProcess getRunningProcessInfo(String processInstanceId);

    /**
     * Returns a list of usernames that are assigned to a specific activity instance.
     * @param processId
     * @param processInstanceId
     * @param activityInstanceId
     * @return
     */
    List<String> getAssignmentResourceIds(String processId, String processInstanceId, String activityInstanceId); // TODO: VERIFY USAGE??

    /**
     * Internal method used to checks deadlines
     * @param instancesPerTransaction
     * @param failuresToIgnore 
     */
    void internalCheckDeadlines(int instancesPerTransaction, int failuresToIgnore);
    
    /**
     * Internal method used to checks deadlines for selected process instances 
     * @param pids
     * @return the status of the method call
     */
    boolean internalCheckDeadlines(String[] pids);

    /**
     * Internal method used to updates deadline checker time interval
     */
    void internalUpdateDeadlineChecker();
    
    /**
     * Internal method used to recover stuck tool activities due to improper shutdown
     */
    void internalRecoverStuckToolActivities();
    
    /**
     * Internal method used to updates workflow variable and deadline of migrated process instance
     * @param process
     * @param acts
     */
    void internalUpdateMigratedProcess(MigrateProcess process, Collection<MigrateActivity> acts);

    /**
     * Gets the parent process instance id of a process instance
     * @param processId
     * @return 
     */
    WorkflowProcessLink getWorkflowProcessLink(String processId); // TODO: VERIFY USAGE??

    /**
     * Internal method used to delete the processes link
     * @param wfProcessLink 
     */
    void internalDeleteWorkflowProcessLink(WorkflowProcessLink wfProcessLink); // TODO: VERIFY USAGE??

    /**
     * Checks the current user is allow to start a process
     * @param processDefId
     * @return 
     */
    Boolean isUserInWhiteList(String processDefId); // TODO: VERIFY USAGE??

    /**
     * Replaces the WorkflowManager.LATEST in process def id to the latest process def id 
     * @param processDefId
     * @return 
     */
    String getConvertedLatestProcessDefId(String processDefId); // TODO: VERIFY USAGE??
    
    /**
     * Gets the usernames of process activity for process version migration
     * @param processId
     * @param activityDefId
     * @return 
     */
    List<String> getMigrationAssignmentUserList(String processId, String activityDefId);
    
    /**
     * Gets activity definition limit in second
     * @param processDefId
     * @param activityDefId
     * @return 
     */
    long getActivityLimit(String processDefId, String activityDefId);
    
    /**
     * Gets due date return from the deadline plugin
     * @param processId
     * @param activityId
     * @param limitInSecond
     * @param createdTime
     * @param startTime
     * @return 
     */
    Date getDueDateProceedByPlugin(String processId, String activityId, long limitInSecond, Date createdTime, Date startTime);
    
    /**
     * Calculates service level value 
     * @param startedDate
     * @param finishDate
     * @param dueDate
     * @return 
     */
    double getServiceLevelValue(Date startedDate, Date finishDate, Date dueDate);
    
    /**
     * Gets previous activities already executed in the current process, up until and including the current active activity
     * @param activityId
     * @param includeTools Set to true to also include Tool elements in the results
     * @return null if specific activity not found
     */
    Collection<WorkflowActivity> getPreviousActivities(String activityId, boolean includeTools);

    /**
     * Gets the next possible activities
     * @param activityId
     * @param includeTools Set to true to also include Tool elements in the results
     * @return null if specified activity not found
     */
    public Collection<WorkflowActivity> getNextActivities(String activityId, boolean includeTools);

    /**
     * Gets outgoing transitions name/id of an activity
     * @param processDefId
     * @param actDefId
     * @return 
     */
    public Map<String, String> getNonExceptionalOutgoingTransitions(String processDefId, String actDefId);
    
    /**
     * Gets running activity id by using form record id
     * 
     * @param id
     * @param processDefId
     * @param activityDefId
     * @param username
     * @return 
     */
    public String getRunningActivityIdByRecordId(String id, String processDefId, String activityDefId, String username);
    
    /**
     * Gets assignment by using form record id
     * 
     * @param id
     * @param processDefId
     * @param activityDefId
     * @param username
     * @return 
     */
    public WorkflowAssignment getAssignmentByRecordId(String id, String processDefId, String activityDefId, String username);
    
    /**
     * Gets next assignment by current completed assignment
     * 
     * @param assignment
     * @return 
     */
    public WorkflowAssignment getNextAssignmentByCurrentAssignment(WorkflowAssignment assignment);
    
    /**
     * Check participant has activities
     * 
     * @param processDefId
     * @param participantId
     * @return 
     */
    public boolean participantHasActivities(String processDefId, String participantId);
    
    /**
     * To migrate process data to history tables
     */
    public void internalMigrateProcessHistories();
}
