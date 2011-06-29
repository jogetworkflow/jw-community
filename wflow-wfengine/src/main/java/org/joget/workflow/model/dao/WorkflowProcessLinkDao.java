package org.joget.workflow.model.dao;

import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.workflow.model.WorkflowProcessLink;

public class WorkflowProcessLinkDao extends AbstractSpringDao {

    public final static String ENTITY_NAME="WorkflowProcessLink";

    public void addWorkflowProcessLink(WorkflowProcessLink wfProcessLink){
        saveOrUpdate(ENTITY_NAME, wfProcessLink);
    }

    public WorkflowProcessLink getWorkflowProcessLink(String processId){
        return (WorkflowProcessLink) find(ENTITY_NAME, processId);
    }

    public void delete(WorkflowProcessLink wfProcessLink) {
        super.delete(ENTITY_NAME, wfProcessLink);
    }
}
