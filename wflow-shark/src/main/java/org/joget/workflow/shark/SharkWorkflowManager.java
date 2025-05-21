package org.joget.workflow.shark;

import java.util.Collection;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.shark.migrate.model.MigrateActivity;
import org.joget.workflow.shark.migrate.model.MigrateProcess;

/**
 * Extension of WorkflowManager with Shark engine specific methods.
 */
public interface SharkWorkflowManager extends WorkflowManager {

    /**
     * Internal method used to updates workflow variable and deadline of migrated process instance
     * @param process
     * @param acts
     */
    public void internalUpdateMigratedProcess(MigrateProcess process, Collection<MigrateActivity> acts);
    
}
