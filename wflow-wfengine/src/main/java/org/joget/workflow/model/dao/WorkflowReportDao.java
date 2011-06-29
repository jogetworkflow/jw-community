package org.joget.workflow.model.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.workflow.model.WorkflowReport;

public class WorkflowReportDao extends AbstractSpringDao {
    public static final String ENTITY_NAME = "WorkflowReport";
     
    public void addWorkflowReport(WorkflowReport workflowReport){
        save(ENTITY_NAME, workflowReport);
    }
    
    public void updateWorkflowReport(WorkflowReport workflowReport){
        merge(ENTITY_NAME, workflowReport);
    }

    public WorkflowReport getWorkflowProcessByActivityInstanceId(String activityInstanceId){
        WorkflowReport wfReport = (WorkflowReport) find(ENTITY_NAME, activityInstanceId);
        return (wfReport!=null ? wfReport : null);
    }

    public List getWorkflowReportsByActivityDefIdAndProcessDefId(String activityDefId, String processDefId){
        return this.getHibernateTemplate().find("select e.delay from WorkflowReport e where e.wfActivity.id=? and e.wfProcess.id=?", new Object[]{activityDefId, processDefId});
    }

    public int getTotalWorkflowReportsByActivityDefIdAndProcessDefId(String activityDefId, String processDefId){
        return count(ENTITY_NAME, "e where e.wfActivity.id=? and e.wfProcess.id=?", new Object[]{activityDefId, processDefId}).intValue();
    }

    public Collection<WorkflowReport> getWorkflowReportsByProcessDefinitionIdGroupByActivityDefId(String processDefinitionId){
       return find(ENTITY_NAME, "where e.wfProcess.id=? group by e.wfActivity.id", new Object[]{processDefinitionId}, null, null, null, null);
    }
    
    public void removeWorkflowReport(WorkflowReport workflowReport){
        delete(ENTITY_NAME, workflowReport);
    }

    public Collection<WorkflowReport> getWorkflowReportsByAcceptedUserWithAssignmentStatus(String username, String status, String packageId, String processId, String processName, String activityName, String sort, Boolean desc, Integer start, Integer rows){
        List<Object> params = new ArrayList<Object>();

        String hql = " where e.nameOfAcceptedUser like ? and e.status=?";
        params.add(username);
        params.add(status);

        if(packageId != null && packageId.trim().length() > 0){
            hql += " and e.wfPackage.packageId=?";
            params.add(packageId);
        }

        if(processId != null && processId.trim().length() > 0){
            hql += " and e.processInstanceId like ?";
            params.add("%"+processId+"%");
        }

        if(processName != null && processName.trim().length() > 0){
            hql += " and e.wfProcess.name like ?";
            params.add("%"+processName+"%");
        }

        if(activityName != null && activityName.trim().length() > 0){
            hql += " and e.wfActivity.name like ?";
            params.add("%"+activityName+"%");
        }

        return find(ENTITY_NAME, hql, params.toArray(), sort, desc, start, rows);
    }

    public int getWorkflowReportsSizeByAcceptedUserWithAssignmentStatus(String username, String status, String packageId, String processId, String processName, String activityName){
        List<Object> params = new ArrayList<Object>();

        String hql = "e where e.nameOfAcceptedUser like ? and e.status=?";
        params.add(username);
        params.add(status);

        if(packageId != null && packageId.trim().length() > 0){
            hql += " and e.wfPackage.packageId=?";
            params.add(packageId);
        }

        if(processId != null && processId.trim().length() > 0){
            hql += " and e.processInstanceId like ?";
            params.add("%"+processId+"%");
        }

        if(processName != null && processName.trim().length() > 0){
            hql += " and e.wfProcess.name like ?";
            params.add("%"+processName+"%");
        }

        if(activityName != null && activityName.trim().length() > 0){
            hql += " and e.wfActivity.name like ?";
            params.add("%"+activityName+"%");
        }
        
        return count(ENTITY_NAME, hql, params.toArray()).intValue();
    }

    public Collection<WorkflowReport> getWorkflowReportsByStatus(String processId, String status, Boolean notEquals, String sort, Boolean desc, Integer start, Integer rows){
        List<Object> params = new ArrayList<Object>();

        String hql = " where e.processInstanceId like ?";
        params.add(processId);

        if(notEquals){
            hql += " and e.status<>?";
        }else{
            hql += " and e.status=?";
        }
        params.add(status);

        return find(ENTITY_NAME, hql, params.toArray(), sort, desc, start, rows);
    }

    public int getWorkflowReportsSizeByStatus(String processId, String status, Boolean notEquals){
        List<Object> params = new ArrayList<Object>();

        String hql = " e where e.processInstanceId like ?";
        params.add(processId);

        if(notEquals){
            hql += " and e.status<>?";
        }else{
            hql += " and e.status=?";
        }
        params.add(status);

        return count(ENTITY_NAME, hql, params.toArray()).intValue();
    }
}
