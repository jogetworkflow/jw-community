package org.joget.workflow.shark.model;

import java.util.Date;
import org.joget.workflow.model.WorkflowProcessLink;

public class SharkProcessHistory {
    
    //SHKProcesses data
    private String processId;
    private String processName;
    private String processRequesterId;
    private String resourceRequesterId;
    private String version;
    private String processDefId;
    private Long started;
    private Long created;
    private Long lastStateTime;
    private String limit;
    private String state;
    private Date due;
    
    //process link history
    private WorkflowProcessLink link;
    
    //SHKProcessData data
    private String variables;


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

    public Long getLastStateTime() {
        return lastStateTime;
    }

    public void setLastStateTime(Long lastStateTime) {
        this.lastStateTime = lastStateTime;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public WorkflowProcessLink getLink() {
        return link;
    }

    public void setLink(WorkflowProcessLink link) {
        this.link = link;
    }

    public Date getDue() {
        return due;
    }

    public void setDue(Date due) {
        this.due = due;
    }
}
