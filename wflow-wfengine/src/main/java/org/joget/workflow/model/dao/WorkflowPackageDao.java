package org.joget.workflow.model.dao;

import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.workflow.model.WorkflowPackage;


public class WorkflowPackageDao extends AbstractSpringDao {
    public final static String ENTITY_NAME="WorkflowPackage";

    public void addWorkflowPackage(WorkflowPackage wfPackage){
        saveOrUpdate(ENTITY_NAME, wfPackage);
    }

    public void updateWorkflowPackage(WorkflowPackage wfPackage){
        merge(ENTITY_NAME, wfPackage);
    }
}
