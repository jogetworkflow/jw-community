package org.joget.workflow.shark.model.dao;

import java.util.Collection;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.dao.WorkflowAssignmentDao;
import org.joget.workflow.model.dao.WorkflowProcessLinkDao;
import org.joget.workflow.shark.migrate.model.MigrateActivity;
import org.joget.workflow.shark.model.SharkActivityHistory;
import org.joget.workflow.shark.model.SharkProcessHistory;

/**
 * Additional DAO interface required for the Enhydra Shark workflow engine.
 */
public interface SharkWorkflowAssignmentDao extends WorkflowAssignmentDao {

    String ACTIVITY_ENTITY_NAME = "SharkActivity";
    String ACTIVITY_HISTORY_ENTITY_NAME = "SharkActivityHistory";
    String ENTITY_NAME = "SharkAssignment";
    String PROCESS_ENTITY_NAME = "SharkProcess";
    String PROCESS_HISTORY_ENTITY_NAME = "SharkProcessHistory";

    MigrateActivity getActivityProcessDefId(String actId);

    WorkflowProcessLinkDao getWorkflowProcessLinkDao();

    void saveActivityHistory(SharkActivityHistory history);

    void saveProcessHistory(SharkProcessHistory history);

    void setWorkflowProcessLinkDao(WorkflowProcessLinkDao workflowProcessLinkDao);
    
    Collection<WorkflowAssignment> loadAssignmentsByProcessIds(Collection<String> processIds, String username, String state, String sort, Boolean desc, Integer start, Integer rows);
    
}
