package org.joget.report.dao;

import java.util.Collection;
import org.joget.report.model.ReportWorkflowActivity;

public interface ReportWorkflowActivityDao {

    public boolean saveReportWorkflowActivity(ReportWorkflowActivity reportWorkflowActivity);

    public ReportWorkflowActivity getReportWorkflowActivity(String appId, String appVersion, String processDefId, String activityDefId);

    public Collection<ReportWorkflowActivity> getReportWorkflowActivityList(String appId, String appVersion, String processDefId, String sort, Boolean desc, Integer start, Integer rows);

    public long getReportWorkflowActivityListSize(String appId, String appVersion, String processDefId);
}
