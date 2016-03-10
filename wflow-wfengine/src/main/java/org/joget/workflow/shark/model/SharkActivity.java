package org.joget.workflow.shark.model;

public class SharkActivity {
    private String activityId;
    private String activityName;
    private String activityDefId;
    private Long activated;
    
    private SharkActivityState state;

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityDefId() {
        return activityDefId;
    }

    public void setActivityDefId(String activityDefId) {
        this.activityDefId = activityDefId;
    }

    public SharkActivityState getState() {
        return state;
    }

    public void setState(SharkActivityState state) {
        this.state = state;
    }

    public Long getActivated() {
        return activated;
}

    public void setActivated(Long activated) {
        this.activated = activated;
    }
}
