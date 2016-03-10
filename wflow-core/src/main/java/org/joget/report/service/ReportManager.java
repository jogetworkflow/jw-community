package org.joget.report.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import org.joget.report.dao.ReportAppDao;
import org.joget.report.dao.ReportWorkflowActivityDao;
import org.joget.report.dao.ReportWorkflowActivityInstanceDao;
import org.joget.report.dao.ReportWorkflowPackageDao;
import org.joget.report.dao.ReportWorkflowProcessDao;
import org.joget.report.dao.ReportWorkflowProcessInstanceDao;
import org.joget.report.model.ReportApp;
import org.joget.report.model.ReportRow;
import org.joget.report.model.ReportWorkflowActivity;
import org.joget.report.model.ReportWorkflowActivityInstance;
import org.joget.report.model.ReportWorkflowPackage;
import org.joget.report.model.ReportWorkflowProcess;
import org.joget.report.model.ReportWorkflowProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service methods used to collect data for reporting purpose
 * 
 */
@Service("reportManager")
public class ReportManager {

    @Autowired
    ReportAppDao reportAppDao;
    @Autowired
    ReportWorkflowPackageDao reportWorkflowPackageDao;
    @Autowired
    ReportWorkflowProcessDao reportWorkflowProcessDao;
    @Autowired
    ReportWorkflowActivityDao reportWorkflowActivityDao;
    @Autowired
    ReportWorkflowProcessInstanceDao reportWorkflowProcessInstanceDao;
    @Autowired
    ReportWorkflowActivityInstanceDao reportWorkflowActivityInstanceDao;

    /**
     * Retrieves list of all app from report table
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    public Collection<ReportApp> getReportAppList(String sort, Boolean desc, Integer start, Integer rows) {
        return reportAppDao.getReportAppList(sort, desc, start, rows);
    }

    /**
     * Retrieves the number of app from report table
     * @return 
     */
    public long getReportAppListSize() {
        return reportAppDao.getReportAppListSize();
    }

    /**
     * Retrieves list of processes from report table
     * @param appId
     * @param appVersion
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    public Collection<ReportWorkflowProcess> getReportWorkflowProcessList(String appId, String appVersion, String sort, Boolean desc, Integer start, Integer rows) {
        return reportWorkflowProcessDao.getReportWorkflowProcessList(appId, appVersion, sort, desc, start, rows);
    }

    /**
     * Retrieves the number of processes from report table
     * @param appId
     * @param appVersion
     * @return 
     */
    public long getReportWorkflowProcessListSize(String appId, String appVersion) {
        return reportWorkflowProcessDao.getReportWorkflowProcessListSize(appId, appVersion);
    }

    /**
     * Retrieves list of activities from report table
     * @param appId
     * @param appVersion
     * @param processDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    public Collection<ReportWorkflowActivity> getReportWorkflowActivityList(String appId, String appVersion, String processDefId, String sort, Boolean desc, Integer start, Integer rows) {
        return reportWorkflowActivityDao.getReportWorkflowActivityList(appId, appVersion, processDefId, sort, desc, start, rows);
    }

    /**
     * Retrieves the number of activities from report table
     * @param appId
     * @param appVersion
     * @param processDefId
     * @return 
     */
    public long getReportWorkflowActivityListSize(String appId, String appVersion, String processDefId) {
        return reportWorkflowActivityDao.getReportWorkflowActivityListSize(appId, appVersion, processDefId);
    }

    /**
     * Retrieves list of process instances from report table
     * @param appId
     * @param appVersion
     * @param processDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    public Collection<ReportWorkflowProcessInstance> getReportWorkflowProcessInstanceList(String appId, String appVersion, String processDefId, String sort, Boolean desc, Integer start, Integer rows) {
        return reportWorkflowProcessInstanceDao.getReportWorkflowProcessInstanceList(appId, appVersion, processDefId, sort, desc, start, rows);
    }

    /**
     * Retrieves the number of process instances from report table
     * @param appId
     * @param appVersion
     * @param processDefId
     * @return 
     */
    public long getReportWorkflowProcessInstanceListSize(String appId, String appVersion, String processDefId) {
        return reportWorkflowProcessInstanceDao.getReportWorkflowProcessInstanceListSize(appId, appVersion, processDefId);
    }

    /**
     * Retrieves list of activity instances from report table
     * @param appId
     * @param appVersion
     * @param processDefId
     * @param activityDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    public Collection<ReportWorkflowActivityInstance> getReportWorkflowActivityInstanceList(String appId, String appVersion, String processDefId, String activityDefId, String sort, Boolean desc, Integer start, Integer rows) {
        return reportWorkflowActivityInstanceDao.getReportWorkflowActivityInstanceList(appId, appVersion, processDefId, activityDefId, sort, desc, start, rows);
    }

    /**
     * Retrieves the number of activity instances from report table
     * @param appId
     * @param appVersion
     * @param processDefId
     * @param activityDefId
     * @return 
     */
    public long getReportWorkflowActivityInstanceListSize(String appId, String appVersion, String processDefId, String activityDefId) {
        return reportWorkflowActivityInstanceDao.getReportWorkflowActivityInstanceListSize(appId, appVersion, processDefId, activityDefId);
    }

    /**
     * Retrieves a process SLA report 
     * @param appId
     * @param appVersion
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    public Collection<ReportRow> getWorkflowProcessSlaReport(String appId, String appVersion, String sort, Boolean desc, Integer start, Integer rows) {
        if (sort != null) {
            sort = "processName";
        }

        Collection<ReportRow> report = new ArrayList<ReportRow>();
        Collection<ReportWorkflowProcess> workflowProcessList = getReportWorkflowProcessList(appId, appVersion, sort, desc, start, rows);
        if (workflowProcessList != null && !workflowProcessList.isEmpty()) {
            for (ReportWorkflowProcess workflowProcess : workflowProcessList) {
                Collection<ReportWorkflowProcessInstance> processInstanceList = reportWorkflowProcessInstanceDao.getReportWorkflowProcessInstanceList(appId, appVersion, workflowProcess.getProcessDefId(), null, null, null, null);
                double[] values = new double[processInstanceList.size()];
                int i = 0;
                for (ReportWorkflowProcessInstance instance : processInstanceList) {
                    values[i] = getDelay(instance.getDue(), instance.getStartedTime(), instance.getFinishTime(), instance.getDelay());
                    i++;
                }
                double[] minMax = getMinMaxValues(values);
                double[] ratioDelayAndOnTime = getRatioWithDelayAndOnTime(values, processInstanceList.size());

                ReportRow row = new ReportRow();
                row.setId(workflowProcess.getProcessDefId());
                row.setName(workflowProcess.getProcessName());
                row.setMinDelay(minMax[0]);
                row.setMaxDelay(minMax[1]);
                row.setRatioWithDelay(ratioDelayAndOnTime[0]);
                row.setRatioOnTime(ratioDelayAndOnTime[1]);

                report.add(row);
            }
        }
        return report;
    }

    /**
     * Retrieves an activity SLA report 
     * @param appId
     * @param appVersion
     * @param processDefId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    public Collection<ReportRow> getWorkflowActivitySlaReport(String appId, String appVersion, String processDefId, String sort, Boolean desc, Integer start, Integer rows) {
        if (sort != null) {
            sort = "activityName";
        }

        Collection<ReportRow> report = new ArrayList<ReportRow>();
        Collection<ReportWorkflowActivity> workflowActivityList = getReportWorkflowActivityList(appId, appVersion, processDefId, sort, desc, start, rows);
        if (workflowActivityList != null && !workflowActivityList.isEmpty()) {
            for (ReportWorkflowActivity workflowActivity : workflowActivityList) {
                Collection<ReportWorkflowActivityInstance> activityInstanceList = reportWorkflowActivityInstanceDao.getReportWorkflowActivityInstanceList(appId, appVersion, workflowActivity.getReportWorkflowProcess().getProcessDefId(), workflowActivity.getActivityDefId(), null, null, null, null);
                double[] values = new double[activityInstanceList.size()];
                int i = 0;
                for (ReportWorkflowActivityInstance instance : activityInstanceList) {
                    values[i] = getDelay(instance.getDue(), instance.getCreatedTime(), instance.getFinishTime(), instance.getDelay());
                    i++;
                }
                double[] minMax = getMinMaxValues(values);
                double[] ratioDelayAndOnTime = getRatioWithDelayAndOnTime(values, activityInstanceList.size());

                ReportRow row = new ReportRow();
                row.setId(workflowActivity.getActivityDefId());
                row.setName(workflowActivity.getActivityName());
                row.setMinDelay(minMax[0]);
                row.setMaxDelay(minMax[1]);
                row.setRatioWithDelay(ratioDelayAndOnTime[0]);
                row.setRatioOnTime(ratioDelayAndOnTime[1]);

                report.add(row);
            }
        }
        return report;
    }

    /**
     * Gets an app data from report table
     * @param appId
     * @param appVersion
     * @param appName
     * @return 
     */
    public ReportApp getReportApp(String appId, String appVersion, String appName) {
        ReportApp reportApp = reportAppDao.getReportApp(appId, appVersion);
        if (reportApp == null) {
            reportApp = new ReportApp();
            reportApp.setAppId(appId);
            reportApp.setAppVersion(appVersion);
            reportApp.setAppName(appName);
            if (reportAppDao.saveReportApp(reportApp)) {
                return reportAppDao.getReportApp(appId, appVersion);
            } else {
                return null;
            }
        } else {
            return reportApp;
        }
    }

    /**
     * Gets workflow package data from report table
     * @param reportApp
     * @param packageId
     * @param packageVersion
     * @param packageName
     * @return 
     */
    public ReportWorkflowPackage getReportWorkflowPackage(ReportApp reportApp, String packageId, String packageVersion, String packageName) {
        ReportWorkflowPackage reportWorkflowPackage = reportWorkflowPackageDao.getReportWorkflowPackage(reportApp.getAppId(), reportApp.getAppVersion(), packageId, packageVersion);
        if (reportWorkflowPackage == null) {
            reportWorkflowPackage = new ReportWorkflowPackage();
            reportWorkflowPackage.setReportApp(reportApp);
            reportWorkflowPackage.setPackageId(packageId);
            reportWorkflowPackage.setPackageName(packageName);
            reportWorkflowPackage.setPackageVersion(packageVersion);
            if (reportWorkflowPackageDao.saveReportWorkflowPackage(reportWorkflowPackage)) {
                return reportWorkflowPackageDao.getReportWorkflowPackage(reportApp.getAppId(), reportApp.getAppVersion(), packageId, packageVersion);
            } else {
                return null;
            }
        } else {
            return reportWorkflowPackage;
        }
    }

    /**
     * Gets process data from report table
     * @param reportWorkflowPackage
     * @param processDefId
     * @param processName
     * @return 
     */
    public ReportWorkflowProcess getReportWorkflowProcess(ReportWorkflowPackage reportWorkflowPackage, String processDefId, String processName) {
        ReportWorkflowProcess reportWorkflowProcess = reportWorkflowProcessDao.getReportWorkflowProcess(reportWorkflowPackage.getReportApp().getAppId(), reportWorkflowPackage.getReportApp().getAppVersion(), processDefId);
        if (reportWorkflowProcess == null) {
            reportWorkflowProcess = new ReportWorkflowProcess();
            reportWorkflowProcess.setReportWorkflowPackage(reportWorkflowPackage);
            reportWorkflowProcess.setProcessDefId(processDefId);
            reportWorkflowProcess.setProcessName(processName);
            if (reportWorkflowProcessDao.saveReportWorkflowProcess(reportWorkflowProcess)) {
                return reportWorkflowProcessDao.getReportWorkflowProcess(reportWorkflowPackage.getReportApp().getAppId(), reportWorkflowPackage.getReportApp().getAppVersion(), processDefId);
            } else {
                return null;
            }
        } else {
            return reportWorkflowProcess;
        }
    }

    /**
     * Gets activity data from report table
     * @param reportWorkflowProcess
     * @param activityDefId
     * @param activityName
     * @return 
     */
    public ReportWorkflowActivity getReportWorkflowActivity(ReportWorkflowProcess reportWorkflowProcess, String activityDefId, String activityName) {
        ReportWorkflowActivity reportWorkflowActivity = reportWorkflowActivityDao.getReportWorkflowActivity(reportWorkflowProcess.getReportWorkflowPackage().getReportApp().getAppId(), reportWorkflowProcess.getReportWorkflowPackage().getReportApp().getAppVersion(), reportWorkflowProcess.getProcessDefId(), activityDefId);
        if (reportWorkflowActivity == null) {
            reportWorkflowActivity = new ReportWorkflowActivity();
            reportWorkflowActivity.setReportWorkflowProcess(reportWorkflowProcess);
            reportWorkflowActivity.setActivityDefId(activityDefId);
            reportWorkflowActivity.setActivityName(activityName);
            if (reportWorkflowActivityDao.saveReportWorkflowActivity(reportWorkflowActivity)) {
                return reportWorkflowActivityDao.getReportWorkflowActivity(reportWorkflowProcess.getReportWorkflowPackage().getReportApp().getAppId(), reportWorkflowProcess.getReportWorkflowPackage().getReportApp().getAppVersion(), reportWorkflowProcess.getProcessDefId(), activityDefId);
            } else {
                return null;
            }
        } else {
            return reportWorkflowActivity;
        }
    }

    /**
     * Gets process instance data from report table
     * @param processInstanceId
     * @return 
     */
    public ReportWorkflowProcessInstance getReportWorkflowProcessInstance(String processInstanceId) {
        return reportWorkflowProcessInstanceDao.getReportWorkflowProcessInstance(processInstanceId);
    }

    /**
     * Save a process instance data to report table
     * @param workflowProcessInstance
     * @return 
     */
    public boolean saveReportWorkflowProcessInstance(ReportWorkflowProcessInstance workflowProcessInstance) {
        return reportWorkflowProcessInstanceDao.saveReportWorkflowProcessInstance(workflowProcessInstance);
    }

    /**
     * Gets activity instance data from report table
     * @param activityInstanceId
     * @return 
     */
    public ReportWorkflowActivityInstance getReportWorkflowActivityInstance(String activityInstanceId) {
        return reportWorkflowActivityInstanceDao.getReportWorkflowActivityInstance(activityInstanceId);
    }

    /**
     * Save an activity instance data to report table 
     * @param workflowActivityInstance
     * @return 
     */
    public boolean saveReportWorkflowActivityInstance(ReportWorkflowActivityInstance workflowActivityInstance) {
        return reportWorkflowActivityInstanceDao.saveReportWorkflowActivityInstance(workflowActivityInstance);
    }

    protected double[] getRatioWithDelayAndOnTime(double[] values, int total) {
        int ratioWithDelay = 0;
        int ratioOnTime = 0;

        for (int i = 0; i < values.length; i++) {
            if (values[i] > 0) {
                ratioWithDelay++;
            } else {
                ratioOnTime++;
            }
        }

        return new double[]{(((double) ratioWithDelay / total) * 100), (((double) ratioOnTime / total) * 100)};

    }

    protected double[] getMinMaxValues(double[] values) {
        double min = (values.length > 0) ? values[0] : 0;
        double max = min;

        for (int i = 0; i < values.length; i++) {
            if (min > values[i] && values[i] > 0) {
                min = values[i];
            }

            if (max < values[i]) {
                max = values[i];
            }
        }

        return new double[]{min, max};
    }

    protected double getDelay(Date due, Date createdTime, Date finishTime, double delay) {
        if(finishTime == null){
            Date currentDate = new Date();
            if(due != null && currentDate.after(due)){
                Calendar currentCal = Calendar.getInstance();
                currentCal.setTime(currentDate);
                Calendar dueCal = Calendar.getInstance();
                dueCal.setTime(due);
                delay = (double) (currentCal.getTimeInMillis() - dueCal.getTimeInMillis()) /1000;
                return delay;
            }else{
                return 0;
            }
        }
        return delay;
    }
}
