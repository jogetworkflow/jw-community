package org.joget.apps.form.model;

import org.joget.workflow.model.WorkflowProcessLink;

public class FormDataAssignment {
    private long id;
    private String resourceId;
    private String activityId;
    private String activityProcessDefName;
    private WorkflowProcessLink link;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityProcessDefName() {
        return activityProcessDefName;
    }

    public void setActivityProcessDefName(String activityProcessDefName) {
        this.activityProcessDefName = activityProcessDefName;
    }

    public WorkflowProcessLink getLink() {
        return link;
    }

    public void setLink(WorkflowProcessLink link) {
        this.link = link;
    }
}
