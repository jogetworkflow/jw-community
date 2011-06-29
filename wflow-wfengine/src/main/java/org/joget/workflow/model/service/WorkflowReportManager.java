package org.joget.workflow.model.service;

import java.util.Collection;
import java.util.List;
import org.joget.workflow.model.WorkflowReport;
import org.joget.workflow.model.WorkflowPackage;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.dao.WorkflowActivityDao;
import org.joget.workflow.model.dao.WorkflowPackageDao;
import org.joget.workflow.model.dao.WorkflowProcessDao;
import org.joget.workflow.model.dao.WorkflowReportDao;

public class WorkflowReportManager {
    private WorkflowReportDao workflowReportDao;
    private WorkflowPackageDao workflowPackageDao;
    private WorkflowProcessDao workflowProcessDao;
    private WorkflowActivityDao workflowActivityDao;
    
    public void addWorkflowReport(WorkflowReport workflowReport){
        workflowReportDao.addWorkflowReport(workflowReport);
    }

    public WorkflowReport getWorkflowProcessByActivityInstanceId(String activityInstanceId){
        return workflowReportDao.getWorkflowProcessByActivityInstanceId(activityInstanceId);
    }

    public List getWorkflowReportsByActivityDefIdAndProcessDefId(String activityDefId, String processDefId){
        return workflowReportDao.getWorkflowReportsByActivityDefIdAndProcessDefId(activityDefId, processDefId);
    }

    public int getTotalWorkflowReportsByActivityDefIdAndProcessDefId(String activityDefId, String processDefId){
        return workflowReportDao.getTotalWorkflowReportsByActivityDefIdAndProcessDefId(activityDefId, processDefId);
    }

    public Collection<WorkflowReport> getWorkflowReportsByProcessDefinitionIdGroupByActivityDefId(String processdefinitionId){
        return workflowReportDao.getWorkflowReportsByProcessDefinitionIdGroupByActivityDefId(processdefinitionId);
    }

    public Collection<WorkflowActivity> getActivityDefByProcessDefId(String processdefinitionId){
        return workflowActivityDao.getActivityDefByProcessDefId(processdefinitionId);
    }

    public Collection<WorkflowReport> getWorkflowReportsByAcceptedUserWithAssignmentStatus(String username, String status, String packageId, String processId, String processName, String activityName, String sort, Boolean desc, Integer start, Integer rows){
        return workflowReportDao.getWorkflowReportsByAcceptedUserWithAssignmentStatus(username, status, packageId, processId, processName, activityName, sort, desc, start, rows);
    }

    public int getWorkflowReportsSizeByAcceptedUserWithAssignmentStatus(String username, String status, String packageId, String processId, String processName, String activityName){
        return workflowReportDao.getWorkflowReportsSizeByAcceptedUserWithAssignmentStatus(username, status, packageId, processId, processName, activityName);
    }

    public Collection<WorkflowReport> getWorkflowReportsByStatus(String processId, String status, Boolean notEquals, String sort, Boolean desc, Integer start, Integer rows){
        return workflowReportDao.getWorkflowReportsByStatus(processId, status, notEquals, sort, desc, start, rows);
    }

    public int getWorkflowReportsSizeByStatus(String processId, String status, Boolean notEquals){
        return workflowReportDao.getWorkflowReportsSizeByStatus(processId, status, notEquals);
    }

    public Collection<WorkflowProcess> getAllProcessDefinitions(){
        return workflowProcessDao.getAllProcessDefinitions();
    }
    
    public void addWorkflowPackage(WorkflowPackage workflowPackage){
        workflowPackageDao.addWorkflowPackage(workflowPackage);
    }

    public void addWorkflowProcess(WorkflowProcess workflowProcess){
        workflowProcessDao.addWorkflowProcess(workflowProcess);
    }

    public void addWorkflowActivity(WorkflowActivity workflowActivity){
        workflowActivityDao.addWorkflowActivity(workflowActivity);
    }
    
    public void updateWorkflowPackage(WorkflowPackage workflowPackage){
        workflowPackageDao.updateWorkflowPackage(workflowPackage);
    }

    public void updateWorkflowProcess(WorkflowProcess workflowProcess){
        workflowProcessDao.updateWorkflowProcess(workflowProcess);
    }

    public void updateWorkflowActivity(WorkflowActivity workflowActivity){
        workflowActivityDao.updateWorkflowActivity(workflowActivity);
    }

    public void updateWorkflowReport(WorkflowReport workflowReport){
        workflowReportDao.updateWorkflowReport(workflowReport);
    }
    
    public void removeWorkflowReport(WorkflowReport workflowReport){
        workflowReportDao.removeWorkflowReport(workflowReport);
    }

    public WorkflowReportDao getWorkflowReportDao() {
        return workflowReportDao;
    }

    public void setWorkflowReportDao(WorkflowReportDao workflowReportDao) {
        this.workflowReportDao = workflowReportDao;
    }

    public WorkflowPackageDao getWorkflowPackageDao() {
        return workflowPackageDao;
    }

    public void setWorkflowPackageDao(WorkflowPackageDao workflowPackageDao) {
        this.workflowPackageDao = workflowPackageDao;
    }

    public WorkflowProcessDao getWorkflowProcessDao() {
        return workflowProcessDao;
    }

    public void setWorkflowProcessDao(WorkflowProcessDao workflowProcessDao) {
        this.workflowProcessDao = workflowProcessDao;
    }

    public WorkflowActivityDao getWorkflowActivityDao() {
        return workflowActivityDao;
    }

    public void setWorkflowActivityDao(WorkflowActivityDao workflowActivityDao) {
        this.workflowActivityDao = workflowActivityDao;
    }
}
