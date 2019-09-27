package org.joget.workflow.shark.model;

import org.joget.workflow.model.WorkflowProcessLink;

public class SharkProcess {
    private String processId;
    private String processName;
    private String processRequesterId;
    private String resourceRequesterId;
    private String version;
    private String processDefId;
    private Long started;
    private Long created;
    
    private SharkProcessState state;
    private WorkflowProcessLink link;

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessRequesterId() {
        return processRequesterId;
    }

    public void setProcessRequesterId(String processRequesterId) {
        this.processRequesterId = processRequesterId;
    }

    public String getResourceRequesterId() {
        return resourceRequesterId;
    }

    public void setResourceRequesterId(String resourceRequesterId) {
        this.resourceRequesterId = resourceRequesterId;
    }

    public String getProcessVersion() {
        return processDefId.split("#")[1];
    }
    
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProcessDefId() {
        return processDefId;
    }

    public void setProcessDefId(String processDefId) {
        this.processDefId = processDefId;
    }

    public SharkProcessState getState() {
        return state;
    }

    public void setState(SharkProcessState state) {
        this.state = state;
    }

    public WorkflowProcessLink getLink() {
        return link;
    }

    public void setLink(WorkflowProcessLink link) {
        this.link = link;
    }

    public Long getStarted() {
        return started;
    }

    public void setStarted(Long started) {
        this.started = started;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
