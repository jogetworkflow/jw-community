package org.joget.workflow.model.service;

import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowTool;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowParticipant;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowVariable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.joget.commons.util.PagedList;
import org.joget.workflow.model.WorkflowPackage;
import org.joget.workflow.model.WorkflowProcessLink;
import org.joget.workflow.model.WorkflowProcessResult;

public interface WorkflowManager {
    public static String LATEST = "latest";

    void activityVariable(String activityInstanceId, String variableId, Object variableValue);

    void processVariable(String processInstanceId, String variableId, Object variableValue);

    String getProcessVariable(String processInstanceId, String variableId);

    void assignmentAccept(String activityId);

    void assignmentComplete(String activityId);

    void assignmentComplete(String activityId, Map<String, String> variableMap);

    void activityAbort(String processId, String activityDefId);

    boolean activityStart(String processId, String activityDefId, boolean abortRunningActivities);

    void assignmentVariable(String activityId, String variableName, String variableValue);

    void assignmentVariables(String activityId, Map<String, String> variableMap);

    void assignmentWithdraw(String activityId);

    void assignmentReassign(String processDefId, String processId, String activityId, String username, String replaceUser);

    void assignmentForceComplete(String processDefId, String processId, String activityId, String username);

    WorkflowActivity getActivityById(String activityId);

    Collection<WorkflowActivity> getActivityList(String processId, Integer start, Integer rows, String sort, Boolean desc);

    int getActivitySize(String processId);

    Collection<WorkflowVariable> getActivityVariableList(String activityId);

    Collection<WorkflowVariable> getProcessVariableList(String processId);

    Boolean isAssignmentExist(String activityId);

    WorkflowAssignment getAssignment(String activityId);

    WorkflowAssignment getMockAssignment(String activityId); // TODO: VERIFY USAGE??

    WorkflowAssignment getAssignmentByProcess(String processId);

    Collection<WorkflowAssignment> getAssignmentList(Boolean accepted, String processDefId, String sort, Boolean desc, Integer start, Integer rows); // REFACTOR??

    Collection<WorkflowAssignment> getAssignmentList(String packageId, String processDefId, String processId, String sort, Boolean desc, Integer start, Integer rows); // REFACTOR??

    Collection<WorkflowAssignment> getAssignmentList(String packageId, String processDefId, String processId, String activityDefId, String sort, Boolean desc, Integer start, Integer rows); // REFACTOR??
    
    Collection<WorkflowAssignment> getAssignmentListLite(String packageId, String processDefId, String processId, String activityDefId, String sort, Boolean desc, Integer start, Integer rows); // REFACTOR??

    Collection<WorkflowAssignment> getAssignmentListFilterByProccessDefIds(String[] processDefIds, String sort, Boolean desc, Integer start, Integer rows); // REFACTOR??

    int getAssignmentSize(Boolean accepted, String processDefId); // TODO: REFACTOR??

    int getAssignmentSize(String packageId, String processDefId, String processId); // TODO: REFACTOR??
    
    int getAssignmentSize(String packageId, String processDefId, String processId, String activityDefId); // TODO: REFACTOR??

    int getAssignmentListFilterByProccessDefIdsSize(String[] processDefIds); // TODO: VERIFY USAGE??

    PagedList<WorkflowAssignment> getAssignmentPendingAndAcceptedList(String packageId, String processDefId, String processId, String sort, Boolean desc, Integer start, Integer rows);

    PagedList<WorkflowAssignment> getAssignmentPendingList(String processDefId, String sort, Boolean desc, Integer start, Integer rows);

    PagedList<WorkflowAssignment> getAssignmentAcceptedList(String processDefId, String sort, Boolean desc, Integer start, Integer rows);

    Map getActivityInstanceByProcessIdAndStatus(String processId, Boolean accepted); // TODO: VERIFY USAGE??

    Collection<WorkflowVariable> getAssignmentVariableList(String activityId);

    String getUserByProcessIdAndActivityDefId(String processDefId, String processId, String activityDefId);

    Boolean isPackageIdExist(String packageId);

    Collection<WorkflowPackage> getPackageList();

    WorkflowPackage getPackage(String packageId, String version);

    String getCurrentPackageVersion(String packageId);

    byte[] getPackageContent(String packageId, String version);

    Map<String, WorkflowParticipant> getParticipantMap(String processDefId);

    WorkflowProcess getProcess(String processDefId);

    WorkflowActivity getProcessActivityDefinition(String processDefId, String activityDefId);

    Collection<WorkflowActivity> getProcessActivityDefinitionList(String processDefId);

    Collection<WorkflowTool> getProcessToolDefinitionList(String processDefId);

    String getProcessDefIdByInstanceId(String instanceId); // TODO: VERIFY USAGE??

    Collection<WorkflowProcess> getProcessList(String packageId); // TODO: REFACTOR??

    Collection<WorkflowProcess> getProcessList(String packageId, String version); // TODO: REFACTOR??

    PagedList<WorkflowProcess> getProcessList(String sort, Boolean desc, Integer start, Integer rows, String packageId, Boolean all, Boolean checkWhiteList); // TODO: REFACTOR??

    Collection<WorkflowParticipant> getProcessParticipantDefinitionList(String processDefId);

    Collection<WorkflowVariable> getProcessVariableDefinitionList(String processDefId);

    WorkflowProcess getRunningProcessById(String processId);
    
    Collection<String> getRunningProcessIds();

    Collection<WorkflowProcess> getRunningProcessList(String packageId, String processId, String processName, String version, String sort, Boolean desc, Integer start, Integer rows);

    Collection<WorkflowProcess> getCompletedProcessList(String packageId, String processId, String processName, String version, String sort, Boolean desc, Integer start, Integer rows);

    int getRunningProcessSize(String packageId, String processId, String processName, String version);

    int getCompletedProcessSize(String packageId, String processId, String processName, String version);

    WorkflowUserManager getWorkflowUserManager();

    void processDeleteAndUnloadVersion(String packageId, String version);

    void processDeleteAndUnload(String packageId);

    String processCreateWithoutStart(String processDefId);

    WorkflowProcessResult processStart(String processDefId); // TODO: REFACTOR??

    WorkflowProcessResult processStart(String processDefId, Map<String, String> variables); // TODO: REFACTOR??

    WorkflowProcessResult processStart(String processDefId, Map<String, String> variables, String startProcUsername); // TODO: REFACTOR??

    WorkflowProcessResult processStart(String processDefId, String processId, Map<String, String> variables, String startProcUsername, String parentProcessId, boolean startManually); // TODO: REFACTOR??

    WorkflowProcessResult processStartWithInstanceId(String processDefId, String processId, Map<String, String> variables); // TODO: REFACTOR??

    WorkflowProcessResult processStartWithLinking(String processDefId, Map<String, String> variables, String startProcUsername, String parentProcessId); // TODO: REFACTOR??

    WorkflowProcessResult processCopyFromInstanceId(String currentProcessId, String newProcessDefId, boolean abortCurrentProcess);

    boolean processAbort(String processId);

    String processUploadWithoutUpdateMapping(String packageId, byte[] processDefinitionData) throws Exception; // TODO: VERIFY USAGE??

    String processUpload(String packageId, byte[] processDefinitionData) throws Exception;

    String getPackageIdFromDefinition(byte[] processDefinitionData);

    void reevaluateAssignmentsForActivity(String activityInstanceId);

    void reevaluateAssignmentsForProcess(String procInstanceId);

    void reevaluateAssignmentsForProcesses(String[] procInstanceId);

    void reevaluateAssignmentsForUser(String username);

    void removeProcessInstance(String procInstanceId);

    void internalRemoveProcessOnComplete(String procInstanceId);

    double getServiceLevelMonitorForRunningActivity(String activityInstanceId);

    double getServiceLevelMonitorForRunningProcess(String processInstanceId);

    void setWorkflowUserManager(WorkflowUserManager userManager);

    WorkflowActivity getRunningActivityInfo(String activityInstanceId);

    WorkflowProcess getRunningProcessInfo(String processInstanceId);

    List<String> getAssignmentResourceIds(String processId, String processInstanceId, String activityInstanceId); // TODO: VERIFY USAGE??

    void internalCheckDeadlines(int instancesPerTransaction, int failuresToIgnore);
    
    boolean internalCheckDeadlines(String[] pids);

    void internalUpdateDeadlineChecker();

    WorkflowProcessLink getWorkflowProcessLink(String processId); // TODO: VERIFY USAGE??

    void internalDeleteWorkflowProcessLink(WorkflowProcessLink wfProcessLink); // TODO: VERIFY USAGE??

    Boolean isUserInWhiteList(String processDefId); // TODO: VERIFY USAGE??

    String getConvertedLatestProcessDefId(String processDefId); // TODO: VERIFY USAGE??
    
    //for process version migration 
    List<String> getMigrationAssignmentUserList(String processId, String activityDefId);
}
