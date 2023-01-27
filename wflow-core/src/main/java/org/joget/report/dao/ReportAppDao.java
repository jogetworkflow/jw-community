package org.joget.report.dao;

import java.util.Collection;
import org.joget.report.model.ReportApp;

public interface ReportAppDao {

    public boolean saveReportApp(ReportApp reportApp);

    public ReportApp getReportApp(String appId, String appVersion);

    public Collection<ReportApp> getReportAppList(String sort, Boolean desc, Integer start, Integer rows);

    public long getReportAppListSize();
}
