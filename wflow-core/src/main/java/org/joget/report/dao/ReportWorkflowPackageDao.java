package org.joget.report.dao;

import org.joget.report.model.ReportWorkflowPackage;

public interface ReportWorkflowPackageDao {

    public boolean saveReportWorkflowPackage(ReportWorkflowPackage reportWorkflowPackage);

    public ReportWorkflowPackage getReportWorkflowPackage(String appId, String appVersion, String packageId, String packageVersion);
}
