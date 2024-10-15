package org.joget.report.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.report.model.ReportWorkflowPackage;

public class ReportWorkflowPackageDaoImpl extends AbstractSpringDao implements ReportWorkflowPackageDao {

    public static final String ENTITY_NAME = "ReportWorkflowPackage";

    public boolean saveReportWorkflowPackage(ReportWorkflowPackage reportWorkflowPackage) {
        try {
            saveOrUpdate(ENTITY_NAME, reportWorkflowPackage);
            return true;
        } catch (Exception e) {
            LogUtil.error(ReportWorkflowPackageDaoImpl.class.getName(), e, "saveReportWorkflowPackage Error!");
            return false;
        }
    }
    
    public ReportWorkflowPackage getReportWorkflowPackage(String appId, String appVersion, String packageId, String packageVersion) {
        String condition = " WHERE e.reportApp.appId = ? AND e.reportApp.appVersion = ? AND e.packageId = ? AND e.packageVersion = ?";
        Collection params = new ArrayList();
        params.add(appId);
        params.add(appVersion);
        params.add(packageId);
        params.add(packageVersion);

        List<ReportWorkflowPackage> packages = (List<ReportWorkflowPackage>) super.find(ENTITY_NAME, condition, params.toArray(), null, null, null, null);

        if (packages != null && !packages.isEmpty()) {
            return packages.get(0);
        }
        return null;
    }
}
