package org.joget.workflow.model.dao;

import java.util.Collection;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.workflow.model.WorkflowActivity;

public class WorkflowActivityDao extends AbstractSpringDao {

    public final static String ENTITY_NAME="WorkflowActivity";

    public void addWorkflowActivity(WorkflowActivity wfActivity){
        saveOrUpdate(ENTITY_NAME, wfActivity);
    }

    public void updateWorkflowActivity(WorkflowActivity wfActivity){
        merge(ENTITY_NAME, wfActivity);
    }

    public Collection<WorkflowActivity> getActivityDefByProcessDefId(String processDefId) {
       return find(ENTITY_NAME, "where e.activityDefId in (select distinct r.wfActivity.id from WorkflowReport r where r.wfProcess.id=?)", new Object[]{processDefId}, null, null, null, null);
    }

}
