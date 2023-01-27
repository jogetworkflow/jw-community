package org.joget.report.dao;

import java.util.Collection;
import org.joget.report.model.ReportWorkflowActivityInstance;

public interface ReportWorkflowActivityInstanceDao {

    public boolean saveReportWorkflowActivityInstance(ReportWorkflowActivityInstance reportWorkflowActivityInstance);

    public ReportWorkflowActivityInstance getReportWorkflowActivityInstance(String instanceId);

    public Collection<ReportWorkflowActivityInstance> getReportWorkflowActivityInstanceList(String appId, String appVersion, String processDefId, String activityDefId, String sort, Boolean desc, Integer start, Integer rows);

    public Collection<ReportWorkflowActivityInstance> getReportWorkflowActivityInstanceList(String appId, String appVersion, String processDefId, String activityDefId, boolean hasSlaOnly, String sort, Boolean desc, Integer start, Integer rows);

    public Collection<ReportWorkflowActivityInstance> getReportWorkflowActivityInstanceList(String appId, String appVersion, String processDefId, String processInstanceId, String activityDefId, boolean hasSlaOnly, String sort, Boolean desc, Integer start, Integer rows);

    public long getReportWorkflowActivityInstanceListSize(String appId, String appVersion, String processDefId, String activityDefId);
    
    public long getReportWorkflowActivityInstanceListSize(String appId, String appVersion, String processDefId, String activityDefId, boolean hasSlaOnly);
    
    public long getReportWorkflowActivityInstanceListSize(String appId, String appVersion, String processDefId, String processInstanceId, String activityDefId, boolean hasSlaOnly);
}
