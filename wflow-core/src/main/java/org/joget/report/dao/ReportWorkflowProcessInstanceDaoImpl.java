package org.joget.report.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.report.model.ReportWorkflowProcessInstance;

public class ReportWorkflowProcessInstanceDaoImpl extends AbstractSpringDao implements ReportWorkflowProcessInstanceDao {

    public static final String ENTITY_NAME = "ReportWorkflowProcessInstance";

    public boolean saveReportWorkflowProcessInstance(ReportWorkflowProcessInstance reportWorkflowProcessInstance) {
        try {
            saveOrUpdate(ENTITY_NAME, reportWorkflowProcessInstance);
            
            super.findSession().evict(reportWorkflowProcessInstance);
            return true;
        } catch (Exception e) {
            LogUtil.error(ReportWorkflowProcessInstanceDaoImpl.class.getName(), e, "saveReportWorkflowProcessInstance Error!");
            return false;
        }
    }

    public ReportWorkflowProcessInstance getReportWorkflowProcessInstance(String instanceId) {
        return (ReportWorkflowProcessInstance) super.find(ENTITY_NAME, instanceId);
    }
    
    public Collection<ReportWorkflowProcessInstance> getReportWorkflowProcessInstanceList(String appId, String appVersion, String processDefId, String sort, Boolean desc, Integer start, Integer rows) {
        return getReportWorkflowProcessInstanceList(appId, appVersion, processDefId, false, sort, desc, start, rows);
    }

    public Collection<ReportWorkflowProcessInstance> getReportWorkflowProcessInstanceList(String appId, String appVersion, String processDefId, boolean hasSlaOnly, String sort, Boolean desc, Integer start, Integer rows) {
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
        
        if (hasSlaOnly) {
            condition += " AND e.due IS NOT NULL";
        }

        return (List<ReportWorkflowProcessInstance>) super.find(ENTITY_NAME, condition, params.toArray(), sort, desc, start, rows);
    }

    public long getReportWorkflowProcessInstanceListSize(String appId, String appVersion, String processDefId) {
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
