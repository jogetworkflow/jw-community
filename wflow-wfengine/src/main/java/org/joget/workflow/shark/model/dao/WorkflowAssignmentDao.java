package org.joget.workflow.shark.model.dao;

import java.util.ArrayList;
import java.util.Collection;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.shark.model.SharkAssignment;

public class WorkflowAssignmentDao extends AbstractSpringDao {
    
    public final static String ENTITY_NAME="SharkAssignment";
    
    public Collection<WorkflowAssignment> getAssignments(String packageId, String processDefId, String processId, String activityDefId, String username, String state, String sort, Boolean desc, Integer start, Integer rows) {
        //required to disable lazy loading 
        String condition = "join fetch e.process p join fetch e.activity a  join fetch a.state s";
        Collection<String> params = new ArrayList<String>();
        
        if (packageId != null || processDefId != null || processId != null || activityDefId != null || username != null || state != null) {
            condition += " where 1=1";
            
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and p.processDefId like ?";
                params.add(packageId + "#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                condition += " and p.processDefId = ?";
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and p.processId = ?";
                params.add(processId);
            }
            
            if (activityDefId != null && !activityDefId.isEmpty()) {
                condition += " and a.activityDefId = ?";
                params.add(activityDefId);
            }
            
            if (username != null && !username.isEmpty()) {
                condition += " and e.assigneeName = ?";
                params.add(username);
            }
            
            if (state != null && !state.isEmpty()) {
                condition += " and s.name = ?";
                params.add(state);
            }
        }
        Collection<SharkAssignment> shAss = find(ENTITY_NAME, condition, params.toArray(new String[0]), sort, desc, start, rows);
        Collection<WorkflowAssignment> ass = new ArrayList<WorkflowAssignment>();
        
        //transform to WorkflowAssignment
        if (shAss != null && !shAss.isEmpty()) {
            for (SharkAssignment s : shAss) {
                WorkflowAssignment a = new WorkflowAssignment();
                a.setActivityId(s.getActivity().getActivityId());
                a.setActivityDefId(s.getActivity().getActivityDefId());
                a.setActivityName(s.getActivity().getActivityName());
                a.setProcessDefId(s.getProcess().getProcessDefId());
                a.setProcessId(s.getProcess().getProcessId());
                a.setProcessName(s.getProcess().getProcessName());
                a.setProcessRequesterId(s.getProcess().getProcessRequesterId());
                a.setProcessVersion(s.getProcess().getProcessVersion());
                a.setAssigneeName(s.getAssigneeName());
                ass.add(a);
            }
        }
        
        return ass;
    }
    
    public int getAssignmentSize(String packageId, String processDefId, String processId, String activityDefId, String username, String state) {
        //required to disable lazy loading 
        String condition = "join fetch e.process p join fetch e.activity a  join fetch a.state s";
        Collection<String> params = new ArrayList<String>();
        
        if (packageId != null || processDefId != null || processId != null || activityDefId != null || username != null || state != null) {
            condition += " where 1=1";
            
            if (packageId != null && !packageId.isEmpty()) {
                condition += " and p.processDefId like ?";
                params.add(packageId + "#%");
            }
            
            if (processDefId != null && !processDefId.isEmpty()) {
                condition += " and p.processDefId = ?";
                params.add(processDefId);
            }
            
            if (processId != null && !processId.isEmpty()) {
                condition += " and p.processId = ?";
                params.add(processId);
            }
            
            if (activityDefId != null && !activityDefId.isEmpty()) {
                condition += " and a.activityDefId = ?";
                params.add(activityDefId);
            }
            
            if (username != null && !username.isEmpty()) {
                condition += " and e.assigneeName = ?";
                params.add(username);
            }
            
            if (state != null && !state.isEmpty()) {
                condition += " and s.name = ?";
                params.add(state);
            }
        }
        Long total = count(ENTITY_NAME, condition, params.toArray(new String[0]));
        
        if (total != null) {
            return total.intValue();
        }
        return 0;
    }
}
