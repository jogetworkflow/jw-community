package org.joget.apps.app.lib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joget.apps.app.model.AuditTrail;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultAuditTrailPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowPackage;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowReport;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowReportManager;

public class ProcessDataCollectorAuditTrail extends DefaultAuditTrailPlugin {

    public String getName() {
        return "ProcessDataCollectorAuditTrail";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String getDescription() {
        return "Save process data into wf_report* tables for reporting purposes";
    }

    public Object execute(Map properties) {
        Object result = null;
        try {
            final AuditTrail auditTrail = (AuditTrail) properties.get("auditTrail");
            final PluginManager pluginManager = (PluginManager) properties.get("pluginManager");
            final WorkflowManager workflowManager = (WorkflowManager) pluginManager.getBean("workflowManager");
            final WorkflowReportManager workflowReportManager = (WorkflowReportManager) pluginManager.getBean("workflowReportManager");

            if (validation(auditTrail)) {
                new Thread(new Runnable() {

                    public void run() {
                        String activityInstanceId = auditTrail.getMessage();
                        WorkflowActivity wfTrackActivity = workflowManager.getRunningActivityInfo(activityInstanceId);
                        //get workflow activity
                        WorkflowActivity wfActivity = workflowManager.getActivityById(activityInstanceId);

                        String processInstanceId = wfActivity.getProcessId();
                        String processDefId = workflowManager.getProcessDefIdByInstanceId(processInstanceId);

                        //get workflow process
                        WorkflowProcess wfProcess = (processDefId != null ? workflowManager.getProcess(processDefId) : null);
                        if (wfProcess != null) {
                            List<String> userList = new ArrayList<String>();
                            try {
                                Thread.sleep(2000);
                                int maxAttempt = 5;
                                int numOfAttempt = 0;
                                while (userList.size() == 0 && numOfAttempt < maxAttempt) {
                                    LogUtil.debug(getClass().getName(), "Attempting to get resource ids....");
                                    userList = workflowManager.getAssignmentResourceIds(wfActivity.getProcessDefId(), wfActivity.getProcessId(), activityInstanceId);
                                    Thread.sleep(2000);
                                    numOfAttempt++;
                                }

                                LogUtil.debug(getClass().getName(), "Resource ids=" + userList);
                            } catch (Exception e) {
                                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error executing report plugin", e);
                            }

                            //get assignment users
                            String assignmentUsers = "";
                            for (String username : userList) {
                                assignmentUsers += username + ",";
                            }
                            if (assignmentUsers.endsWith(",")) {
                                assignmentUsers = assignmentUsers.substring(0, assignmentUsers.length() - 1);
                            }

                            //set workflow package
                            WorkflowPackage wfPackage = new WorkflowPackage();
                            wfPackage.setPackageId(wfProcess.getPackageId());
                            wfPackage.setPackageName(wfProcess.getPackageName());

                            WorkflowReport updateWorkflowReport = workflowReportManager.getWorkflowProcessByActivityInstanceId(activityInstanceId);

                            if (updateWorkflowReport != null) {
                                //update workflow activity
                                workflowReportManager.updateWorkflowActivity(wfActivity);
                                //update workflow process
                                workflowReportManager.updateWorkflowProcess(wfProcess);
                                //update workflow package
                                workflowReportManager.updateWorkflowPackage(wfPackage);

                                //set workflow report
                                updateWorkflowReport.setActivityInstanceId(wfActivity.getId());
                                updateWorkflowReport.setWfPackage(wfPackage);
                                updateWorkflowReport.setWfProcess(wfProcess);
                                updateWorkflowReport.setWfActivity(wfActivity);
                                updateWorkflowReport.setAppId(auditTrail.getAppId());
                                updateWorkflowReport.setAppVersion((auditTrail.getAppVersion() != null)?Long.parseLong(auditTrail.getAppVersion()):null);
                                updateWorkflowReport.setProcessInstanceId(processInstanceId);
                                updateWorkflowReport.setPriority(wfTrackActivity.getPriority());
                                updateWorkflowReport.setCreatedTime(wfTrackActivity.getCreatedTime());
                                updateWorkflowReport.setStartedTime(wfTrackActivity.getStartedTime());
                                updateWorkflowReport.setLimit(wfTrackActivity.getLimitInSeconds());
                                updateWorkflowReport.setDue(wfTrackActivity.getDue());
                                updateWorkflowReport.setDelay(wfTrackActivity.getDelayInSeconds());
                                updateWorkflowReport.setFinishTime(wfTrackActivity.getFinishTime());
                                updateWorkflowReport.setTimeConsumingFromDateCreated(wfTrackActivity.getTimeConsumingFromDateCreatedInSeconds());
                                updateWorkflowReport.setTimeConsumingFromDateStarted(wfTrackActivity.getTimeConsumingFromDateStartedInSeconds());
                                updateWorkflowReport.setPerformer(wfTrackActivity.getPerformer());
                                updateWorkflowReport.setNameOfAcceptedUser(wfTrackActivity.getNameOfAcceptedUser());
                                //updateWorkflowReport.setAssignmentUsers(assignmentUsers);
                                updateWorkflowReport.setStatus(wfTrackActivity.getStatus());
                                updateWorkflowReport.setState(wfActivity.getState());

                                //update workflow report
                                workflowReportManager.updateWorkflowReport(updateWorkflowReport);
                            } else {
                                //Add or update workflow activity
                                wfActivity.setPriority(wfTrackActivity.getPriority());
                                workflowReportManager.updateWorkflowActivity(wfActivity);

                                //Add or update workflow process
                                workflowReportManager.updateWorkflowProcess(wfProcess);

                                //Add or update workflow package
                                workflowReportManager.updateWorkflowPackage(wfPackage);

                                //Add workflow report
                                WorkflowReport workflowReport = new WorkflowReport();
                                workflowReport.setActivityInstanceId(wfActivity.getId());
                                workflowReport.setWfPackage(wfPackage);
                                workflowReport.setWfProcess(wfProcess);
                                workflowReport.setWfActivity(wfActivity);
                                workflowReport.setProcessInstanceId(processInstanceId);
                                workflowReport.setAppId(auditTrail.getAppId());
                                workflowReport.setAppVersion((auditTrail.getAppVersion() != null)?Long.parseLong(auditTrail.getAppVersion()):null);
                                workflowReport.setPriority(wfTrackActivity.getPriority());
                                workflowReport.setCreatedTime(wfTrackActivity.getCreatedTime());
                                workflowReport.setStartedTime(wfTrackActivity.getStartedTime());
                                workflowReport.setLimit(wfTrackActivity.getLimitInSeconds());
                                workflowReport.setDue(wfTrackActivity.getDue());
                                workflowReport.setDelay(wfTrackActivity.getDelayInSeconds());
                                workflowReport.setFinishTime(wfTrackActivity.getFinishTime());
                                workflowReport.setTimeConsumingFromDateCreated(wfTrackActivity.getTimeConsumingFromDateCreatedInSeconds());
                                workflowReport.setTimeConsumingFromDateStarted(wfTrackActivity.getTimeConsumingFromDateStartedInSeconds());
                                workflowReport.setPerformer(wfTrackActivity.getPerformer());
                                workflowReport.setNameOfAcceptedUser(wfTrackActivity.getNameOfAcceptedUser());
                                workflowReport.setAssignmentUsers(assignmentUsers);
                                workflowReport.setStatus(wfTrackActivity.getStatus());
                                workflowReport.setState(wfActivity.getState());
                                //add workflow report
                                workflowReportManager.addWorkflowReport(workflowReport);
                            }
                        }
                    }
                }).start();
            }
            return result;
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error executing report plugin", e);
            return null;
        }
    }

    public String getLabel() {
        return "Process Data Collector";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }

    public String getDefaultPropertyValues() {
        return "";
    }

    public boolean validation(AuditTrail auditTrail){
        return auditTrail.getMethod().equals("getDefaultAssignments")
               || auditTrail.getMethod().equals("processAbort")
               || auditTrail.getMethod().equals("assignmentAccept")
               || auditTrail.getMethod().equals("assignmentComplete")
               || auditTrail.getMethod().equals("assignmentForceComplete");
    }
}
