package org.joget.workflow.model.dao;

import java.util.Collection;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.workflow.model.WorkflowProcess;

public class WorkflowProcessDao extends AbstractSpringDao {

    public final static String ENTITY_NAME="WorkflowProcess";

    public void addWorkflowProcess(WorkflowProcess wfProcess){
        saveOrUpdate(ENTITY_NAME, wfProcess);
    }

    public void updateWorkflowProcess(WorkflowProcess wfProcess){
        merge(ENTITY_NAME, wfProcess);
    }

    public Collection<WorkflowProcess> getAllProcessDefinitions(){
        return find(ENTITY_NAME, "", null, "name", false, 0, 1000);
    }
}
