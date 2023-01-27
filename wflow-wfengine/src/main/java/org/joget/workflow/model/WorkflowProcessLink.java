package org.joget.workflow.model;

import java.io.Serializable;

public class WorkflowProcessLink implements Serializable {
    private String processId;
    private String parentProcessId;
    private String originProcessId;

    public String getOriginProcessId() {
        return originProcessId;
    }

    public void setOriginProcessId(String originProcessId) {
        this.originProcessId = originProcessId;
    }

    public String getParentProcessId() {
        return parentProcessId;
    }

    public void setParentProcessId(String parentProcessId) {
        this.parentProcessId = parentProcessId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }
}
