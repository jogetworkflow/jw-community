package org.joget.workflow.shark.model;

import java.util.Date;

public class SharkActivityHistory {
    
    private String activityId;
    private String activityName;
    private String activityDefId;
    private Long activated;
    private Long accepted;
    private Long lastStateTime;
    private Date due;
    private String limit;
    private String performer;
    private String participantId;
    private String assignmentUsers;
    private String state;
    private String type;
    private String processId;
    
    private SharkProcessHistory process;
    
    //SHKActivityData data
    private String variables;

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

    public Long getActivated() {
        return activated;
    }

    public void setActivated(Long activated) {
        this.activated = activated;
    }

    public Long getAccepted() {
        return accepted;
    }

    public void setAccepted(Long accepted) {
        this.accepted = accepted;
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

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SharkProcessHistory getProcess() {
        return process;
    }

    public void setProcess(SharkProcessHistory process) {
        this.process = process;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public Date getDue() {
        return due;
    }

    public void setDue(Date due) {
        this.due = due;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public String getAssignmentUsers() {
        return assignmentUsers;
    }

    public void setAssignmentUsers(String assignmentUsers) {
        this.assignmentUsers = assignmentUsers;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }
}
