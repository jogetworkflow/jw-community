package org.joget.apps.app.lib;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.joget.apps.app.model.AuditTrail;
import org.joget.apps.app.model.ReportTask;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultAuditTrailPlugin;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.core.task.TaskExecutor;

public class ProcessDataCollectorAuditTrail extends DefaultAuditTrailPlugin {
    
    public String getName() {
        return "Process Data Collector";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "Save process data into app_report_* tables for reporting purposes";
    }

    public Object execute(Map properties) {
        Object result = null;
        try {
            final AuditTrail auditTrail = (AuditTrail) properties.get("auditTrail");

            if (validation(auditTrail)) {
                String method = auditTrail.getMethod();
                WorkflowManager workflowManager = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
                WorkflowProcess process = null;
                WorkflowProcess trackProcess = null;
                WorkflowActivity activity = null;
                WorkflowActivity trackActivity = null;
                List<String> users = null;

                if (method.startsWith("process")) {
                    process = workflowManager.getRunningProcessById(auditTrail.getMessage());
                    trackProcess = workflowManager.getRunningProcessInfo(auditTrail.getMessage());
                } else {
                    String actId = null;
                    Object[] args = auditTrail.getArgs();
                    
                    if (method.equals("getDefaultAssignments") && args.length == 3) {
                        users = (List<String>) auditTrail.getReturnObject();
                        actId = (String) args[1];
                    } else if (method.equals("assignmentReassign") && args.length == 5) {
                        users = new ArrayList<String> ();
                        users.add((String) args[3]);
                        actId = (String) args[2];
                    } else if (method.equals("assignmentForceComplete") && args.length == 4) {
                        actId = (String) args[2];
                    } else if (method.startsWith("executeTool")) {
                        users = new ArrayList<String>();
                        actId = ((WorkflowAssignment) args[0]).getActivityId();
                    } else {
                        actId = auditTrail.getMessage();
                    }
                    
                    if (method.equals("activityAbort")) {
                        activity = workflowManager.getActivityByProcess((String) args[0], (String) args[1]);
                        trackActivity = workflowManager.getRunningActivityInfo(activity.getId());
                    } else {
                        activity = workflowManager.getActivityById(actId);
                        trackActivity = workflowManager.getRunningActivityInfo(actId);
                    }
                    process = workflowManager.getRunningProcessById(activity.getProcessId());
                    trackProcess = workflowManager.getRunningProcessInfo(activity.getProcessId());
                    
                    if (method.equals("executeTool")) {
                        trackActivity.setStartedTime(trackActivity.getCreatedTime());
                    } else if (method.equals("executeToolCompleted") || method.equals("executeActivity")) {
                        activity.setState("closed.completed");
                        trackActivity.setStatus("Completed");
                        trackActivity.setStartedTime(trackActivity.getCreatedTime());
                        trackActivity.setFinishTime(new Date());
                        
                        long timeTakenInSeconds = (trackActivity.getFinishTime().getTime() - trackActivity.getCreatedTime().getTime()) / 1000;
                        
                        trackActivity.setTimeConsumingFromDateCreatedInSeconds(timeTakenInSeconds);
                        trackActivity.setTimeConsumingFromDateStartedInSeconds(timeTakenInSeconds);
                    }
                }

                TaskExecutor executor = (TaskExecutor) AppUtil.getApplicationContext().getBean("reportExecutor");
                executor.execute(new ReportTask(activity, trackActivity, process, trackProcess, users, auditTrail.getAppId(), auditTrail.getAppVersion()));
            }
            return result;
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Error executing report plugin");
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

    public boolean validation(AuditTrail auditTrail) {
        return auditTrail != null 
                && auditTrail.getMethod() != null
                && (auditTrail.getMethod().equals("getDefaultAssignments")
                || auditTrail.getMethod().equals("processAbort")
                || auditTrail.getMethod().equals("processCompleted")
                || auditTrail.getMethod().equals("assignmentAbort")
                || auditTrail.getMethod().equals("assignmentComplete")
                || auditTrail.getMethod().equals("assignmentForceComplete")
                || auditTrail.getMethod().equals("assignmentReassign")
                || auditTrail.getMethod().equals("executeTool")
                || auditTrail.getMethod().equals("executeToolCompleted")
                || auditTrail.getMethod().equals("executeActivity")
                || auditTrail.getMethod().equals("runSubFlow")
                || auditTrail.getMethod().equals("finishSubFlow")
                || auditTrail.getMethod().equals("activityAbort"));
    }
}
