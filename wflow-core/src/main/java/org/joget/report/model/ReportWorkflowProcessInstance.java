package org.joget.report.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

public class ReportWorkflowProcessInstance implements Serializable {

    private String instanceId;
    private ReportWorkflowProcess reportWorkflowProcess;
    private String requester;
    private String state;
    private Date due;
    private Date startedTime;
    private Date finishTime;
    private long delay;
    private long timeConsumingFromStartedTime;
    private Collection<ReportWorkflowActivityInstance> reportWorkflowActivityInstanceList;

    public Collection<ReportWorkflowActivityInstance> getReportWorkflowActivityInstanceList() {
        return reportWorkflowActivityInstanceList;
    }

    public void setReportWorkflowActivityInstanceList(Collection<ReportWorkflowActivityInstance> reportWorkflowActivityInstanceList) {
        this.reportWorkflowActivityInstanceList = reportWorkflowActivityInstanceList;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
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

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
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

    public long getTimeConsumingFromStartedTime() {
        return timeConsumingFromStartedTime;
    }

    public void setTimeConsumingFromStartedTime(long timeConsumingFromStartedTime) {
        this.timeConsumingFromStartedTime = timeConsumingFromStartedTime;
    }

    public ReportWorkflowProcess getReportWorkflowProcess() {
        return reportWorkflowProcess;
    }

    public void setReportWorkflowProcess(ReportWorkflowProcess reportWorkflowProcess) {
        this.reportWorkflowProcess = reportWorkflowProcess;
    }
}
