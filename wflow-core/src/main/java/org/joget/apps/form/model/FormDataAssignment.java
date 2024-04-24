package org.joget.apps.form.model;

import org.joget.workflow.model.WorkflowProcessLink;

public class FormDataAssignment {
    private long id;
    private String ResourceId;
    private String ActivityId;
    private String ActivityProcessDefName;
    private String ActivityProcessId;
    private Boolean IsValid;
    private WorkflowProcessLink link;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResourceId() {
        return ResourceId;
    }

    public void setResourceId(String ResourceId) {
        this.ResourceId = ResourceId;
    }

    public String getActivityId() {
        return ActivityId;
    }

    public void setActivityId(String ActivityId) {
        this.ActivityId = ActivityId;
    }

    public String getActivityProcessDefName() {
        return ActivityProcessDefName;
    }

    public void setActivityProcessDefName(String ActivityProcessDefName) {
        this.ActivityProcessDefName = ActivityProcessDefName;
    }

    public String getActivityProcessId() {
        return ActivityProcessId;
    }

    public void setActivityProcessId(String ActivityProcessId) {
        this.ActivityProcessId = ActivityProcessId;
    }

    public Boolean getIsValid() {
        return IsValid;
    }

    public void setIsValid(Boolean IsValid) {
        this.IsValid = IsValid;
    }

    public WorkflowProcessLink getLink() {
        return link;
    }

    public void setLink(WorkflowProcessLink link) {
        this.link = link;
    }
}
