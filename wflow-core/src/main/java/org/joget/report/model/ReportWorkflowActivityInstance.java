package org.joget.report.model;

import java.io.Serializable;
import java.util.Date;

public class ReportWorkflowActivityInstance implements Serializable {

    private String instanceId;
    private ReportWorkflowActivity reportWorkflowActivity;
    private ReportWorkflowProcessInstance reportWorkflowProcessInstance;
    private String performer;
    private String state;
    private String status;
    private String nameOfAcceptedUser;
    private String assignmentUsers;
    private Long delay;
    private Date due;
    private Date createdTime;
    private Date startedTime;
    private Date finishTime;
    private Long timeConsumingFromCreatedTime;
    private Long timeConsumingFromStartedTime;

    public ReportWorkflowActivity getReportWorkflowActivity() {
        return reportWorkflowActivity;
    }

    public void setReportWorkflowActivity(ReportWorkflowActivity reportWorkflowActivity) {
        this.reportWorkflowActivity = reportWorkflowActivity;
    }

    public ReportWorkflowProcessInstance getReportWorkflowProcessInstance() {
        return reportWorkflowProcessInstance;
    }

    public void setReportWorkflowProcessInstance(ReportWorkflowProcessInstance reportWorkflowProcessInstance) {
        this.reportWorkflowProcessInstance = reportWorkflowProcessInstance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignmentUsers() {
        return assignmentUsers;
    }

    public void setAssignmentUsers(String assignmentUsers) {
        this.assignmentUsers = assignmentUsers;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Long getDelay() {
        return delay;
    }

    public void setDelay(Long delay) {
        this.delay = delay;
    }

    public Date getDue() {
        return due;
    }

    public void setDue(Date due) {
        this.due = due;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getNameOfAcceptedUser() {
        return nameOfAcceptedUser;
    }

    public void setNameOfAcceptedUser(String nameOfAcceptedUser) {
        this.nameOfAcceptedUser = nameOfAcceptedUser;
    }

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public Date getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(Date startedTime) {
        this.startedTime = startedTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getTimeConsumingFromCreatedTime() {
        return timeConsumingFromCreatedTime;
    }

    public void setTimeConsumingFromCreatedTime(Long timeConsumingFromCreatedTime) {
        this.timeConsumingFromCreatedTime = timeConsumingFromCreatedTime;
    }

    public Long getTimeConsumingFromStartedTime() {
        return timeConsumingFromStartedTime;
    }

    public void setTimeConsumingFromStartedTime(Long timeConsumingFromStartedTime) {
        this.timeConsumingFromStartedTime = timeConsumingFromStartedTime;
    }
}
