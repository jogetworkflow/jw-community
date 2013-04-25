package org.joget.report.model;

import java.util.Collection;

public class ReportWorkflowActivity {

    private String uid;
    private String activityDefId;
    private String activityName;
    private ReportWorkflowProcess reportWorkflowProcess;
    private Collection<ReportWorkflowActivityInstance> reportWorkflowActivityInstanceList;

    public Collection<ReportWorkflowActivityInstance> getReportWorkflowActivityInstanceList() {
        return reportWorkflowActivityInstanceList;
    }

    public void setReportWorkflowActivityInstanceList(Collection<ReportWorkflowActivityInstance> reportWorkflowActivityInstanceList) {
        this.reportWorkflowActivityInstanceList = reportWorkflowActivityInstanceList;
    }

    public ReportWorkflowProcess getReportWorkflowProcess() {
        return reportWorkflowProcess;
    }

    public void setReportWorkflowProcess(ReportWorkflowProcess reportWorkflowProcess) {
        this.reportWorkflowProcess = reportWorkflowProcess;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
