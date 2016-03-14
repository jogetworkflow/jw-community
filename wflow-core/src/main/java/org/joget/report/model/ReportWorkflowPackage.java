package org.joget.report.model;

import java.io.Serializable;
import java.util.Collection;

public class ReportWorkflowPackage implements Serializable {

    private String uid;
    private String packageId;
    private String packageName;
    private String packageVersion;
    private ReportApp reportApp;
    private Collection<ReportWorkflowProcess> reportWorkflowProcessList;

    public ReportApp getReportApp() {
        return reportApp;
    }

    public void setReportApp(ReportApp reportApp) {
        this.reportApp = reportApp;
    }

    public Collection<ReportWorkflowProcess> getReportWorkflowProcessList() {
        return reportWorkflowProcessList;
    }

    public void setReportWorkflowProcessList(Collection<ReportWorkflowProcess> reportWorkflowProcessList) {
        this.reportWorkflowProcessList = reportWorkflowProcessList;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
