package org.joget.report.model;

import java.io.Serializable;
import java.util.Collection;

public class ReportWorkflowProcess implements Serializable {

    private String uid;
    private String processDefId;
    private String processName;
    private ReportWorkflowPackage reportWorkflowPackage;
    private Collection<ReportWorkflowActivity> reportWorkflowActivityList;
    private Collection<ReportWorkflowProcessInstance> reportWorkflowProcessInstanceList;

    public Collection<ReportWorkflowActivity> getReportWorkflowActivityList() {
        return reportWorkflowActivityList;
    }

    public void setReportWorkflowActivityList(Collection<ReportWorkflowActivity> reportWorkflowActivityList) {
        this.reportWorkflowActivityList = reportWorkflowActivityList;
    }

    public ReportWorkflowPackage getReportWorkflowPackage() {
        return reportWorkflowPackage;
    }

    public void setReportWorkflowPackage(ReportWorkflowPackage reportWorkflowPackage) {
        this.reportWorkflowPackage = reportWorkflowPackage;
    }

    public Collection<ReportWorkflowProcessInstance> getReportWorkflowProcessInstanceList() {
        return reportWorkflowProcessInstanceList;
    }

    public void setReportWorkflowProcessInstanceList(Collection<ReportWorkflowProcessInstance> reportWorkflowProcessInstanceList) {
        this.reportWorkflowProcessInstanceList = reportWorkflowProcessInstanceList;
    }

    public String getProcessDefId() {
        return processDefId;
    }

    public void setProcessDefId(String processDefId) {
        this.processDefId = processDefId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
