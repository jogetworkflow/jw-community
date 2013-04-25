package org.joget.workflow.model;

import java.util.Collection;

/**
 * Class to represent the result when starting a process.
 */
public class WorkflowProcessResult {
    private WorkflowProcess process;
    private Collection<WorkflowActivity> activities;
    private String status;

    /**
     * @return the process
     */
    public WorkflowProcess getProcess() {
        return process;
    }

    /**
     * @param process the process to set
     */
    public void setProcess(WorkflowProcess process) {
        this.process = process;
    }

    /**
     * @return the activities
     */
    public Collection<WorkflowActivity> getActivities() {
        return activities;
    }

    /**
     * @param activities the activities to set
     */
    public void setActivities(Collection<WorkflowActivity> activities) {
        this.activities = activities;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    

}
