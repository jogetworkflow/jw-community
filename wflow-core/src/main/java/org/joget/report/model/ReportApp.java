package org.joget.report.model;

import java.io.Serializable;
import java.util.Collection;

public class ReportApp implements Serializable {

    private String uid;
    private String appId;
    private String appVersion;
    private String appName;
    private Collection<ReportWorkflowPackage> reportWorkflowPackageList;

    public Collection<ReportWorkflowPackage> getReportWorkflowPackageList() {
        return reportWorkflowPackageList;
    }

    public void setReportWorkflowPackageList(Collection<ReportWorkflowPackage> reportWorkflowPackageList) {
        this.reportWorkflowPackageList = reportWorkflowPackageList;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
