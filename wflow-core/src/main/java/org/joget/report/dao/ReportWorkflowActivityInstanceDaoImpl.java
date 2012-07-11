package org.joget.report.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.report.model.ReportWorkflowActivityInstance;

public class ReportWorkflowActivityInstanceDaoImpl extends AbstractSpringDao implements ReportWorkflowActivityInstanceDao {

    public static final String ENTITY_NAME = "ReportWorkflowActivityInstance";

    public boolean saveReportWorkflowActivityInstance(ReportWorkflowActivityInstance reportWorkflowActivityInstance) {
        try {
            super.saveOrUpdate(ENTITY_NAME, reportWorkflowActivityInstance);
            return true;
        } catch (Exception e) {
            LogUtil.error(ReportWorkflowActivityInstanceDaoImpl.class.getName(), e, "saveReportWorkflowActivityInstance Error!");
            return false;
        }
    }

    public ReportWorkflowActivityInstance getReportWorkflowActivityInstance(String instanceId) {
        return (ReportWorkflowActivityInstance) super.find(ENTITY_NAME, instanceId);
    }

    public Collection<ReportWorkflowActivityInstance> getReportWorkflowActivityInstanceList(String appId, String appVersion, String processDefId, String activityDefId, String sort, Boolean desc, Integer start, Integer rows) {
        String condition = " WHERE 1=1";
        Collection params = new ArrayList();

        if (appId != null && !appId.isEmpty()) {
            condition += " AND e.reportWorkflowProcessInstance.reportWorkflowProcess.reportWorkflowPackage.reportApp.appId = ?";
            params.add(appId);
        }

        if (appVersion != null && !appVersion.isEmpty()) {
            condition += " AND e.reportWorkflowProcessInstance.reportWorkflowProcess.reportWorkflowPackage.reportApp.appVersion = ?";
            params.add(appVersion);
        }

        if (processDefId != null && !processDefId.isEmpty()) {
            condition += " AND e.reportWorkflowProcessInstance.reportWorkflowProcess.processDefId = ?";
            params.add(processDefId);
        }

        if (activityDefId != null && !activityDefId.isEmpty()) {
            condition += " AND e.reportWorkflowActivity.activityDefId = ?";
            params.add(activityDefId);
        }

        return (List<ReportWorkflowActivityInstance>) super.find(ENTITY_NAME, condition, params.toArray(), sort, desc, start, rows);
    }

    public long getReportWorkflowActivityInstanceListSize(String appId, String appVersion, String processDefId, String activityDefId) {
        String condition = " WHERE 1=1";
        Collection params = new ArrayList();

        if (appId != null && !appId.isEmpty()) {
            condition += " AND e.reportWorkflowProcessInstance.reportWorkflowProcess.reportWorkflowPackage.reportApp.appId = ?";
            params.add(appId);
        }

        if (appVersion != null && !appVersion.isEmpty()) {
            condition += " AND e.reportWorkflowProcessInstance.reportWorkflowProcess.reportWorkflowPackage.reportApp.appVersion = ?";
            params.add(appVersion);
        }

        if (processDefId != null && !processDefId.isEmpty()) {
            condition += " AND e.reportWorkflowProcessInstance.reportWorkflowProcess.processDefId = ?";
            params.add(processDefId);
        }

        if (activityDefId != null && !activityDefId.isEmpty()) {
            condition += " AND e.reportWorkflowActivity.activityDefId = ?";
            params.add(activityDefId);
        }

        return super.count(ENTITY_NAME, condition, params.toArray());
    }
}
