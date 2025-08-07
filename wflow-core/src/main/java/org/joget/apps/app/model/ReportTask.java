package org.joget.apps.app.model;

import java.util.ArrayList;
import java.util.List;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.report.model.ReportApp;
import org.joget.report.model.ReportWorkflowActivity;
import org.joget.report.model.ReportWorkflowActivityInstance;
import org.joget.report.model.ReportWorkflowPackage;
import org.joget.report.model.ReportWorkflowProcess;
import org.joget.report.model.ReportWorkflowProcessInstance;
import org.joget.report.service.ReportManager;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.transaction.annotation.Transactional;

public final class ReportTask implements Runnable {

    String profile;
    String appId;
    String appVersion;
    WorkflowProcess wfProcess;
    WorkflowProcess wfTrackProcess;
    WorkflowActivity wfActivity;
    WorkflowActivity wfTrackActivity;
    List<String> users;

    public ReportTask(WorkflowActivity wfActivity, WorkflowActivity wfTrackActivity, WorkflowProcess wfProcess, WorkflowProcess wfTrackProcess, List<String> users, String appId, String appVersion) {
        this.profile = DynamicDataSourceManager.getCurrentProfile();
        this.wfActivity = wfActivity;
        this.wfTrackActivity = wfTrackActivity;
        this.wfProcess = wfProcess;
        this.wfTrackProcess = wfTrackProcess;
        this.users = users;
        this.appId = appId;
        this.appVersion = appVersion;
    }
    
    private void setProfile() {
        HostManager.setCurrentProfile(profile);
    }

    @Transactional
    @Override
    public void run() {
        setProfile();
        if (wfActivity != null) {
            updateActivityData(wfActivity, wfTrackActivity, wfProcess, wfTrackProcess, users, appId, appVersion);
        } else {
            updateProcessData(wfProcess, wfTrackProcess, appId, appVersion);
        }
    }

    protected ReportWorkflowProcessInstance updateProcessData(WorkflowProcess wfProcess, WorkflowProcess wfTrackProcess, String appId, String appVersion) {
        String processInstanceId = wfProcess.getInstanceId();

        WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
        ReportManager reportManager = (ReportManager) AppUtil.getApplicationContext().getBean("reportManager");
        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");

        if (wfTrackProcess != null) {
            ReportWorkflowProcessInstance pInstance = reportManager.getReportWorkflowProcessInstance(processInstanceId);
            if (pInstance == null) {
                pInstance = new ReportWorkflowProcessInstance();
                pInstance.setInstanceId(processInstanceId);

                //get app
                AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
                ReportApp reportApp = reportManager.getReportApp(appId, appVersion, appDef.getName());

                //get package
                ReportWorkflowPackage reportPackage = reportManager.getReportWorkflowPackage(reportApp, wfProcess.getPackageId(), wfProcess.getVersion(), wfProcess.getName());

                //get process
                ReportWorkflowProcess reportProcess = reportManager.getReportWorkflowProcess(reportPackage, wfProcess.getIdWithoutVersion(), wfProcess.getName());
                pInstance.setReportWorkflowProcess(reportProcess);
            }

            pInstance.setRequester(wfProcess.getRequesterId());
            pInstance.setState(wfProcess.getState());
            pInstance.setDue(wfTrackProcess.getDue());
            pInstance.setStartedTime(wfTrackProcess.getStartedTime());
            pInstance.setFinishTime(wfTrackProcess.getFinishTime());
            pInstance.setDelay(wfTrackProcess.getDelayInSeconds());
            pInstance.setTimeConsumingFromStartedTime(wfTrackProcess.getTimeConsumingFromDateStartedInSeconds());
            pInstance.setReportWorkflowActivityInstanceList(null); //to fix session issue. mapping set to no update
            reportManager.saveReportWorkflowProcessInstance(pInstance);

            return reportManager.getReportWorkflowProcessInstance(processInstanceId);
        }
        return null;
    }

    protected ReportWorkflowActivityInstance updateActivityData(WorkflowActivity wfActivity, WorkflowActivity wfTrackActivity, WorkflowProcess wfProcess, WorkflowProcess wfTrackProcess, List<String> users, String appId, String appVersion) {
        String activityInstanceId = wfActivity.getId();

        WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
        ReportManager reportManager = (ReportManager) AppUtil.getApplicationContext().getBean("reportManager");

        if (wfActivity != null) {
            ReportWorkflowActivityInstance aInstance = reportManager.getReportWorkflowActivityInstance(activityInstanceId);
            List<String> userList = new ArrayList<String>();
            if (aInstance == null) {
                aInstance = new ReportWorkflowActivityInstance();
                aInstance.setInstanceId(activityInstanceId);

                //set process Instance
                String processInstanceId = wfActivity.getProcessId();

                ReportWorkflowProcessInstance processInstance = reportManager.getReportWorkflowProcessInstance(processInstanceId);
                if (processInstance == null) {
                    processInstance = updateProcessData(wfProcess, wfTrackProcess, appId, appVersion);
                }
                aInstance.setReportWorkflowProcessInstance(processInstance);

                //set activity
                ReportWorkflowActivity reportActivtiy = reportManager.getReportWorkflowActivity(processInstance.getReportWorkflowProcess(), wfActivity.getActivityDefId(), wfActivity.getName());
                aInstance.setReportWorkflowActivity(reportActivtiy);

                if (!wfActivity.getState().startsWith("closed")) {
                    //get assignment users
                    try {
                        if (users == null) {
                            int numOfAttempt = 0;
                            do {
                                LogUtil.debug(getClass().getName(), "Attempting to get resource ids....");
                                userList = workflowManager.getAssignmentResourceIds(wfActivity.getProcessDefId(), wfActivity.getProcessId(), activityInstanceId);

                                if (userList == null) {
                                    Thread.sleep(2000); //wait for assignment creation
                                }
                                numOfAttempt++;
                            } while (userList == null && numOfAttempt < 5); // try max 5 times
                        } else {
                            userList = users;
                        }

                        LogUtil.debug(getClass().getName(), "Resource ids=" + userList);
                    } catch (Exception e) {
                        LogUtil.error(getClass().getName(), e, "Error executing report plugin");
                    }
                }
            } else {
                if (users == null && !wfActivity.getState().startsWith("closed")) { // userList is not used when state is closed, also it caused exception when archive feature is used
                    userList = workflowManager.getAssignmentResourceIds(wfActivity.getProcessDefId(), wfActivity.getProcessId(), activityInstanceId);
                } else {
                    userList = users;
                }
            }

            if (!wfActivity.getState().startsWith("closed")) {
                String assignmentUsers = "";
                if (userList != null) {
                    for (String username : userList) {
                        assignmentUsers += username + ",";
                    }
                }
                if (assignmentUsers.endsWith(",")) {
                    assignmentUsers = assignmentUsers.substring(0, assignmentUsers.length() - 1);
                }
                aInstance.setAssignmentUsers(assignmentUsers);
            }

            aInstance.setPerformer(wfTrackActivity.getPerformer());
            aInstance.setNameOfAcceptedUser(wfTrackActivity.getNameOfAcceptedUser());
            aInstance.setState(wfActivity.getState());
            aInstance.setStatus(wfTrackActivity.getStatus());
            aInstance.setDue(wfTrackActivity.getDue());
            aInstance.setCreatedTime(wfTrackActivity.getCreatedTime());
            aInstance.setStartedTime(wfTrackActivity.getStartedTime());
            aInstance.setFinishTime(wfTrackActivity.getFinishTime());
            aInstance.setDelay(wfTrackActivity.getDelayInSeconds());
            aInstance.setTimeConsumingFromCreatedTime(wfTrackActivity.getTimeConsumingFromDateCreatedInSeconds());
            aInstance.setTimeConsumingFromStartedTime(wfTrackActivity.getTimeConsumingFromDateStartedInSeconds());

            reportManager.saveReportWorkflowActivityInstance(aInstance);

            return null;
        }
        return null;
    }
}
