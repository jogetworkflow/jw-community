package org.joget.report.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.report.model.ReportWorkflowActivity;

public class ReportWorkflowActivityDaoImpl extends AbstractSpringDao implements ReportWorkflowActivityDao {

    public static final String ENTITY_NAME = "ReportWorkflowActivity";

    public boolean saveReportWorkflowActivity(ReportWorkflowActivity reportWorkflowActivity) {
        try {
            saveOrUpdate(ENTITY_NAME, reportWorkflowActivity);
            
            super.findSession().evict(reportWorkflowActivity);
            return true;
        } catch (Exception e) {
            LogUtil.error(ReportWorkflowActivityDaoImpl.class.getName(), e, "saveReportWorkflowActivity Error!");
            return false;
        }
    }

    public ReportWorkflowActivity getReportWorkflowActivity(String appId, String appVersion, String processDefId, String activityDefId) {
        String condition = " WHERE e.reportWorkflowProcess.reportWorkflowPackage.reportApp.appId = ? AND e.reportWorkflowProcess.reportWorkflowPackage.reportApp.appVersion = ? AND e.reportWorkflowProcess.processDefId = ? AND e.activityDefId = ?";
        Collection params = new ArrayList();
        params.add(appId);
        params.add(appVersion);
        params.add(processDefId);
        params.add(activityDefId);

        List<ReportWorkflowActivity> activities = (List<ReportWorkflowActivity>) super.find(ENTITY_NAME, condition, params.toArray(), null, null, 0, 1);

        if (activities != null && !activities.isEmpty()) {
            return activities.get(0);
        }
        return null;
    }

    public Collection<ReportWorkflowActivity> getReportWorkflowActivityList(String appId, String appVersion, String processDefId, String sort, Boolean desc, Integer start, Integer rows) {
        String condition = " WHERE 1=1";
        Collection params = new ArrayList();

        if (appId != null && !appId.isEmpty()) {
            condition += " AND e.reportWorkflowProcess.reportWorkflowPackage.reportApp.appId = ?";
            params.add(appId);
        }

        if (appVersion != null && !appVersion.isEmpty()) {
            condition += " AND e.reportWorkflowProcess.reportWorkflowPackage.reportApp.appVersion = ?";
            params.add(appVersion);
        }

        if (processDefId != null && !processDefId.isEmpty()) {
            condition += " AND e.reportWorkflowProcess.processDefId = ?";
            params.add(processDefId);
        }

        return (List<ReportWorkflowActivity>) super.find(ENTITY_NAME, condition, params.toArray(), sort, desc, start, rows);
    }

    public long getReportWorkflowActivityListSize(String appId, String appVersion, String processDefId) {
        String condition = " WHERE 1=1";
        Collection params = new ArrayList();

        if (appId != null && !appId.isEmpty()) {
            condition += " AND e.reportWorkflowProcess.reportWorkflowPackage.reportApp.appId = ?";
            params.add(appId);
        }

        if (appVersion != null && !appVersion.isEmpty()) {
            condition += " AND e.reportWorkflowProcess.reportWorkflowPackage.reportApp.appVersion = ?";
            params.add(appVersion);
        }

        if (processDefId != null && !processDefId.isEmpty()) {
            condition += " AND e.reportWorkflowProcess.processDefId = ?";
            params.add(processDefId);
        }

        return super.count(ENTITY_NAME, condition, params.toArray());
    }
}
