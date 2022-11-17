package org.joget.report.dao;

import java.util.Collection;
import java.util.List;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.report.model.ReportApp;

public class ReportAppDaoImpl extends AbstractSpringDao implements ReportAppDao {

    public static final String ENTITY_NAME = "ReportApp";

    public boolean saveReportApp(ReportApp reportApp) {
        try {
            saveOrUpdate(ENTITY_NAME, reportApp);
            return true;
        } catch (Exception e) {
            LogUtil.error(ReportAppDaoImpl.class.getName(), e, "saveReportApp Error!");
            return false;
        }
    }

    public ReportApp getReportApp(String appId, String appVersion) {
        ReportApp app = new ReportApp();
        app.setAppId(appId);
        app.setAppVersion(appVersion);
        List<ReportApp> apps = (List<ReportApp>) super.findByExample(ENTITY_NAME, app);

        if (apps != null && !apps.isEmpty()) {
            return apps.get(0);
        }
        return null;
    }

    public Collection<ReportApp> getReportAppList(String sort, Boolean desc, Integer start, Integer rows) {
        return (List<ReportApp>) super.find(ENTITY_NAME, "", new Object[]{}, sort, desc, start, rows);
    }

    public long getReportAppListSize() {
        return super.count(ENTITY_NAME, "", new Object[]{});
    }
}
