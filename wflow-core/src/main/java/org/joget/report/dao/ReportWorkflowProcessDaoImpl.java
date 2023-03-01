package org.joget.report.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.report.model.ReportWorkflowProcess;

public class ReportWorkflowProcessDaoImpl extends AbstractSpringDao implements ReportWorkflowProcessDao {

    public static final String ENTITY_NAME = "ReportWorkflowProcess";

    public boolean saveReportWorkflowProcess(ReportWorkflowProcess reportWorkflowProcess) {
        try {
            saveOrUpdate(ENTITY_NAME, reportWorkflowProcess);
            
            super.findSession().evict(reportWorkflowProcess);
            return true;
        } catch (Exception e) {
            LogUtil.error(ReportWorkflowProcessDaoImpl.class.getName(), e, "saveReportWorkflowProcess Error!");
            return false;
        }
    }

    public ReportWorkflowProcess getReportWorkflowProcess(String appId, String appVersion, String processDefId) {
        String condition = " WHERE e.reportWorkflowPackage.reportApp.appId = ? AND e.reportWorkflowPackage.reportApp.appVersion = ? AND e.processDefId = ?";
        Collection params = new ArrayList();
        params.add(appId);
        params.add(appVersion);
        params.add(processDefId);

        List<ReportWorkflowProcess> processes = (List<ReportWorkflowProcess>) super.find(ENTITY_NAME, condition, params.toArray(), null, null, 0, 1);

        if (processes != null && !processes.isEmpty()) {
            return processes.get(0);
        }
        return null;
    }

    public Collection<ReportWorkflowProcess> getReportWorkflowProcessList(String appId, String appVersion, String sort, Boolean desc, Integer start, Integer rows) {
        String condition = " WHERE 1=1";
        Collection params = new ArrayList();

        if (appId != null && !appId.isEmpty()) {
            condition += " AND e.reportWorkflowPackage.reportApp.appId = ?";
            params.add(appId);
        }

        if (appVersion != null && !appVersion.isEmpty()) {
            condition += " AND e.reportWorkflowPackage.reportApp.appVersion = ?";
            params.add(appVersion);
        }

        return (List<ReportWorkflowProcess>) super.find(ENTITY_NAME, condition, params.toArray(), sort, desc, start, rows);
    }

    public long getReportWorkflowProcessListSize(String appId, String appVersion) {
        String condition = " WHERE 1=1";
        Collection params = new ArrayList();

        if (appId != null && !appId.isEmpty()) {
            condition += " AND e.reportWorkflowPackage.reportApp.appId = ?";
            params.add(appId);
        }

        if (appVersion != null && !appVersion.isEmpty()) {
            condition += " AND e.reportWorkflowPackage.reportApp.appVersion = ?";
            params.add(appVersion);
        }

        return super.count(ENTITY_NAME, condition, params.toArray());
    }
}
