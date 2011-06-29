package org.joget.workflow.model;

import java.util.Date;

public class WorkflowReport {

    private String activityInstanceId;
    private String processInstanceId;
    private String appId;
    private Long appVersion;
    private String priority;
    private Date createdTime;
    private Date startedTime;
    private Long limit;
    private Date due;
    private Long delay;
    private Date finishTime;
    private Long timeConsumingFromDateCreated;
    private Long timeConsumingFromDateStarted;
    private String performer;
    private String nameOfAcceptedUser;
    private String assignmentUsers;
    private String status;
    private String state;
    private WorkflowPackage wfPackage;
    private WorkflowProcess wfProcess;
    private WorkflowActivity wfActivity;
    private double minDelay;
    private double maxDelay;
    private double ratioWithDelay;
    private double ratioOnTime;

    public String getActivityInstanceId() {
        return activityInstanceId;
    }

    public void setActivityInstanceId(String activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(Date startedTime) {
        this.startedTime = startedTime;
    }

    public Long getLimit() {
        return limit;
    }

    public void setLimit(Long limit) {
        this.limit = limit;
    }

    public Date getDue() {
        return due;
    }

    public void setDue(Date due) {
        this.due = due;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public Long getTimeConsumingFromDateCreated() {
        return timeConsumingFromDateCreated;
    }

    public void setTimeConsumingFromDateCreated(Long timeConsumingFromDateCreated) {
        this.timeConsumingFromDateCreated = timeConsumingFromDateCreated;
    }

    public Long getTimeConsumingFromDateStarted() {
        return timeConsumingFromDateStarted;
    }

    public void setTimeConsumingFromDateStarted(Long timeConsumingFromDateStarted) {
        this.timeConsumingFromDateStarted = timeConsumingFromDateStarted;
    }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public String getNameOfAcceptedUser() {
        return nameOfAcceptedUser;
    }

    public void setNameOfAcceptedUser(String nameOfAcceptedUser) {
        this.nameOfAcceptedUser = nameOfAcceptedUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public WorkflowPackage getWfPackage() {
        return wfPackage;
    }

    public void setWfPackage(WorkflowPackage wfPackage) {
        this.wfPackage = wfPackage;
    }

    public WorkflowProcess getWfProcess() {
        return wfProcess;
    }

    public void setWfProcess(WorkflowProcess wfProcess) {
        this.wfProcess = wfProcess;
    }

    public WorkflowActivity getWfActivity() {
        return wfActivity;
    }

    public void setWfActivity(WorkflowActivity wfActivity) {
        this.wfActivity = wfActivity;
    }

    public double getMinDelay() {
        return minDelay;
    }

    public void setMinDelay(double minDelay) {
        this.minDelay = minDelay;
    }

    public double getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(double maxDelay) {
        this.maxDelay = maxDelay;
    }

    public double getRatioWithDelay() {
        return ratioWithDelay;
    }

    public void setRatioWithDelay(double ratioWithDelay) {
        this.ratioWithDelay = ratioWithDelay;
    }

    public double getRatioOnTime() {
        return ratioOnTime;
    }

    public void setRatioOnTime(double ratioOnTime) {
        this.ratioOnTime = ratioOnTime;
    }

    public String getAssignmentUsers() {
        return assignmentUsers;
    }

    public void setAssignmentUsers(String assignmentUsers) {
        this.assignmentUsers = assignmentUsers;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Long getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(Long appVersion) {
        this.appVersion = appVersion;
    }
}
