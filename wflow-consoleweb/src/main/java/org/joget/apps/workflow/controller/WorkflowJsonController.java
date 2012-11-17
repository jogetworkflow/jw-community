package org.joget.apps.workflow.controller;

import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.FileStore;
import org.joget.commons.util.LogUtil;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.commons.util.PagedList;
import org.joget.directory.model.service.DirectoryManager;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.workflow.model.WorkflowPackage;
import org.joget.directory.model.User;
import org.joget.report.model.ReportRow;
import org.joget.report.service.ReportManager;
import org.joget.workflow.model.WorkflowProcessResult;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.joget.workflow.util.XpdlImageUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ui.ModelMap;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class WorkflowJsonController {

    @Autowired
    private WorkflowUserManager workflowUserManager;
    @Autowired
    @Qualifier("main")
    private DirectoryManager directoryManager;
    @Autowired
    private WorkflowManager workflowManager;
    @Autowired
    private AppService appService;
    @Autowired
    private ReportManager reportManager;

    @RequestMapping("/json/workflow/closeDialog")
    public String remoteCloseDialog() {
        return "remoteCloseDialog";
    }

    @RequestMapping("/json/workflow/package/list")
    public void packageList(Writer writer, @RequestParam(value = "callback", required = false) String callback) throws JSONException, IOException {
        Collection<WorkflowPackage> packageList = workflowManager.getPackageList();

        JSONObject jsonObject = new JSONObject();
        for (WorkflowPackage workflowPackage : packageList) {
            Map data = new HashMap();
            data.put("packageId", workflowPackage.getPackageId());
            data.put("packageName", workflowPackage.getPackageName());
            jsonObject.accumulate("data", data);
        }

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/process/list")
    public void processList(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "allVersion", required = false) String allVersion, @RequestParam(value = "packageId", required = false) String packageId, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows, @RequestParam(value = "checkWhiteList", required = false) Boolean checkWhiteList) throws JSONException, IOException {
        PagedList<WorkflowProcess> processList = null;

        if (allVersion != null && allVersion.equals("yes")) {
            processList = workflowManager.getProcessList(sort, desc, start, rows, packageId, true, checkWhiteList);
        } else {
            processList = workflowManager.getProcessList(sort, desc, start, rows, packageId, false, checkWhiteList);
        }

        Integer total = processList.getTotal();
        JSONObject jsonObject = new JSONObject();
        for (WorkflowProcess process : processList) {
            Map data = new HashMap();
            String label = process.getName() + " ver " + process.getVersion();
            data.put("id", process.getId());
            data.put("packageId", process.getPackageId());
            data.put("packageName", process.getPackageName());
            data.put("name", process.getName());
            data.put("version", process.getVersion());
            data.put("label", label);
            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", total);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/process/list/package")
    public void processPackageList(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "checkWhiteList", required = false) Boolean checkWhiteList) throws JSONException, IOException {
        PagedList<WorkflowProcess> processList = workflowManager.getProcessList(null, null, null, null, null, null, checkWhiteList);

        Map<String, Map> processMap = new TreeMap();
        // get process names and totals
        for (WorkflowProcess process : processList) {
            String label = process.getPackageName();
            Map data = (Map) processMap.get(label);
            if (data == null) {
                data = new HashMap();
                data.put("packageId", process.getPackageId());
                data.put("packageName", process.getPackageName());
                data.put("processId", process.getId());
                data.put("processName", process.getName());
                data.put("processVersion", process.getVersion());

                data.put("id", process.getPackageId());
                data.put("label", label);

                String url = "/json/workflow/process/list?packageId=" + process.getPackageId();
                if (callback != null && callback.trim().length() > 0) {
                    url += "&callback=" + callback;
                }
                data.put("url", url);
            }

            Integer count = (Integer) data.get("count");
            if (count == null) {
                count = new Integer(0);
            }
            ++count;
            data.put("count", count);
            processMap.put(label, data);
        }

        JSONObject jsonObject = new JSONObject();
        for (Iterator i = processMap.keySet().iterator(); i.hasNext();) {
            String processName = (String) i.next();
            Map data = (Map) processMap.get(processName);
            jsonObject.accumulate("data", data);
        }

        writeJson(writer, jsonObject, callback);

    }

    @RequestMapping("/json/workflow/process/latest/(*:processId)")
    public void getLatestProcessDefId(Writer writer, @RequestParam("processId") String processDefId, @RequestParam(value = "callback", required = false) String callback) throws JSONException, IOException {
        String id = workflowManager.getConvertedLatestProcessDefId(processDefId.replaceAll(":", "#").replaceAll("#[0-9]+#", "#latest#"));

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("id", id);
        jsonObject.accumulate("encodedId", id.replaceAll("#", ":"));

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/monitoring/running/process/list")
    public void runningProcessList(Writer writer, @RequestParam(value = "packageId", required = false) String packageId, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "processName", required = false) String processName, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException {
        if ("startedTime".equals(sort)) {
            sort = "Started";
        } else if ("createdTime".equals(sort)) {
            sort = "Created";
        }

        Collection<WorkflowProcess> processList = workflowManager.getRunningProcessList(packageId, processId, processName, version, sort, desc, start, rows);

        Integer total = workflowManager.getRunningProcessSize(packageId, processId, processName, version);
        JSONObject jsonObject = new JSONObject();
        for (WorkflowProcess workflowProcess : processList) {
            double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningProcess(workflowProcess.getInstanceId());

            Map data = new HashMap();
            data.put("id", workflowProcess.getInstanceId());
            data.put("name", workflowProcess.getName());
            data.put("state", workflowProcess.getState());
            data.put("version", workflowProcess.getVersion());
            data.put("startedTime", workflowProcess.getStartedTime());
            data.put("requesterId", workflowProcess.getRequesterId());
            data.put("due", workflowProcess.getDue() != null ? workflowProcess.getDue() : "-");

            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", total);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);
        jsonObject.write(writer);
    }

    @RequestMapping("/json/monitoring/completed/process/list")
    public void completedProcessList(Writer writer, @RequestParam(value = "packageId", required = false) String packageId, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "processName", required = false) String processName, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException {
        if ("startedTime".equals(sort)) {
            sort = "Started";
        } else if ("createdTime".equals(sort)) {
            sort = "Created";
        }
        Collection<WorkflowProcess> processList = workflowManager.getCompletedProcessList(packageId, processId, processName, version, sort, desc, start, rows);

        Integer total = workflowManager.getCompletedProcessSize(packageId, processId, processName, version);
        JSONObject jsonObject = new JSONObject();
        for (WorkflowProcess workflowProcess : processList) {
            double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningProcess(workflowProcess.getInstanceId());

            Map data = new HashMap();
            data.put("id", workflowProcess.getInstanceId());
            data.put("name", workflowProcess.getName());
            data.put("version", workflowProcess.getVersion());
            data.put("state", workflowProcess.getState());
            data.put("startedTime", workflowProcess.getStartedTime());
            data.put("requesterId", workflowProcess.getRequesterId());
            data.put("due", workflowProcess.getDue() != null ? workflowProcess.getDue() : "-");

            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", total);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);
        jsonObject.write(writer);
    }

    @RequestMapping("/json/monitoring/activity/list")
    public void activityList(Writer writer, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException {

        Collection<WorkflowActivity> activityList = workflowManager.getActivityList(processId, start, rows, sort, desc);

        Integer total = workflowManager.getActivitySize(processId);
        JSONObject jsonObject = new JSONObject();
        for (WorkflowActivity workflowActivity : activityList) {
            double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningActivity(workflowActivity.getId());
            Map data = new HashMap();
            data.put("id", workflowActivity.getId());
            data.put("name", workflowActivity.getName());
            data.put("state", workflowActivity.getState());
            data.put("dateCreated", workflowActivity.getCreatedTime());

            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", total);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);
        jsonObject.write(writer);
    }

    @RequestMapping(value = "/json/monitoring/activity/reevaluate", method = RequestMethod.POST)
    public void activityReevaluate(Writer writer, @RequestParam("activityId") String activityId) {
        appService.getAppDefinitionForWorkflowActivity(activityId);
        workflowManager.reevaluateAssignmentsForActivity(activityId);
    }

    @RequestMapping(value = "/json/monitoring/activity/abort/(*:processId)/(*:activityDefId)")
    public void activityAbort(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("processId") String processId, @RequestParam("activityDefId") String activityDefId) throws JSONException, IOException {
        appService.getAppDefinitionForWorkflowProcess(processId);
        workflowManager.activityAbort(processId, activityDefId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("processId", processId);
        jsonObject.accumulate("activityDefId", activityDefId);
        jsonObject.accumulate("status", "aborted");

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/json/monitoring/activity/start/(*:processId)/(*:activityDefId)")
    public void activityStart(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("processId") String processId, @RequestParam("activityDefId") String activityDefId, @RequestParam(value = "abortCurrent", required = false) Boolean abortCurrent) throws JSONException, IOException {
        appService.getAppDefinitionForWorkflowProcess(processId);
        boolean abortFlag = (abortCurrent != null) ? abortCurrent : false;
        boolean result = workflowManager.activityStart(processId, activityDefId, abortFlag);
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("processId", processId);
        jsonObject.accumulate("activityDefId", activityDefId);
        jsonObject.accumulate("result", result);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/json/monitoring/process/copy/(*:processId)/(*:processDefId)")
    public void processCopy(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("processId") String processId, @RequestParam("processDefId") String processDefId, @RequestParam(value = "abortCurrent", required = false) Boolean abortCurrent) throws JSONException, IOException {
        appService.getAppDefinitionForWorkflowProcess(processId);
        boolean abortFlag = (abortCurrent != null) ? abortCurrent : false;
        processDefId = processDefId.replaceAll(":", "#");
        WorkflowProcessResult processResult = workflowManager.processCopyFromInstanceId(processId, processDefId, abortFlag);
        String newProcessId = "";
        String[] startedActivities = new String[0];
        WorkflowProcess processStarted = processResult.getProcess();
        if (processStarted != null) {
            newProcessId = processStarted.getInstanceId();
            Collection<WorkflowActivity> activities = processResult.getActivities();
            Collection<String> activityDefIdList = new ArrayList<String>();
            for (WorkflowActivity act : activities) {
                activityDefIdList.add(act.getId());
            }
            startedActivities = (String[]) activityDefIdList.toArray(startedActivities);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("processDefId", processDefId);
        jsonObject.accumulate("processId", newProcessId);
        jsonObject.accumulate("activities", startedActivities);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/monitoring/user/reevaluate")
    public void userReevaluate(Writer writer, @RequestParam("username") String username) {
        workflowManager.reevaluateAssignmentsForUser(username);
    }

    @RequestMapping(value = "/json/monitoring/activity/variable/(*:activityId)/(*:variable)")
    public void activityVariable(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("activityId") String activityId, @RequestParam("variable") String variable, @RequestParam("value") String value) throws JSONException, IOException {
        appService.getAppDefinitionForWorkflowActivity(activityId);
        workflowManager.activityVariable(activityId, variable, value);
        LogUtil.info(getClass().getName(), "Activity variable " + variable + " set to " + value);
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("status", "variableSet");

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/json/monitoring/process/variable/(*:processId)/(*:variable)")
    public void processVariable(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("processId") String processId, @RequestParam("variable") String variable, @RequestParam("value") String value) throws JSONException, IOException {
        appService.getAppDefinitionForWorkflowProcess(processId);
        workflowManager.processVariable(processId, variable, value);
        LogUtil.info(getClass().getName(), "Activity variable " + variable + " set to " + value);
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("status", "variableSet");

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/monitoring/activity/view/(*:activityId)")
    public void activityView(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("activityId") String activityId) throws JSONException, IOException {
        WorkflowActivity activity = workflowManager.getActivityById(activityId);
        if (activity == null) {
            return;
        }
        WorkflowActivity activityInfo = workflowManager.getRunningActivityInfo(activityId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("activityId", activity.getId());
        jsonObject.accumulate("activityDefId", activity.getActivityDefId());
        jsonObject.accumulate("processId", activity.getProcessId());
        jsonObject.accumulate("processDefId", activity.getProcessDefId());
        jsonObject.accumulate("processVersion", activity.getProcessVersion());
        jsonObject.accumulate("processName", activity.getProcessName());
        jsonObject.accumulate("activityName", activity.getName());
        jsonObject.accumulate("description", activity.getDescription());
        jsonObject.accumulate("participant", activityInfo.getPerformer());
        jsonObject.accumulate("acceptedUser", activityInfo.getNameOfAcceptedUser());
        String[] assignmentUsers = activityInfo.getAssignmentUsers();
        for (String user : assignmentUsers) {
            jsonObject.accumulate("assignee", user);
        }
        Collection<WorkflowVariable> variableList = workflowManager.getActivityVariableList(activityId);
        for (WorkflowVariable variable : variableList) {
            JSONObject variableObj = new JSONObject();
            variableObj.accumulate(variable.getId(), variable.getVal());
            jsonObject.accumulate("variable", variableObj);
        }

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/json/workflow/process/variable/(*:processId)/(*:variable)")
    public void getProcessVariable(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("processId") String processId, @RequestParam("variable") String variable) throws JSONException, IOException {
        String variableValue = workflowManager.getProcessVariable(processId, variable);
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("variableValue", variableValue);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/json/workflow/process/sla/list")
    public void processSlaList(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "appId", required = false) String appId, @RequestParam(value = "appVersion", required = false) String appVersion) throws JSONException {
        Collection<ReportRow> processSla = reportManager.getWorkflowProcessSlaReport(appId, appVersion, null, null, null, null);

        JSONObject jsonObject = new JSONObject();
        for (ReportRow row : processSla) {
            Map data = new HashMap();
            data.put("processDefId", row.getId());
            data.put("processName", row.getName());
            data.put("minDelay", row.getMinDelay());
            data.put("maxDelay", row.getMaxDelay());
            data.put("ratioWithDelay", row.getRatioWithDelay());
            data.put("ratioOnTime", row.getRatioOnTime());
            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(row.getRatioOnTime()));

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", processSla.size());
        jsonObject.write(writer);
    }

    @RequestMapping(value = "/json/workflow/activity/sla/list")
    public void activitySlaList(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "appId", required = false) String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam(value = "processDefId", required = false) String processDefId) throws JSONException {
        Collection<ReportRow> activitySla = reportManager.getWorkflowActivitySlaReport(appId, appVersion, processDefId, null, null, null, null);

        JSONObject jsonObject = new JSONObject();
        for (ReportRow row : activitySla) {
            Map data = new HashMap();
            data.put("activityDefId", row.getId());
            data.put("activityName", row.getName());
            data.put("minDelay", row.getMinDelay());
            data.put("maxDelay", row.getMaxDelay());
            data.put("ratioWithDelay", row.getRatioWithDelay());
            data.put("ratioOnTime", row.getRatioOnTime());
            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(row.getRatioOnTime()));

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", activitySla.size());
        jsonObject.write(writer);
    }

    @RequestMapping("/json/workflow/process/view/(*:processId)")
    public void processView(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("processId") String processId) throws JSONException, IOException {

        //decode process def id (to default value)
        processId = processId.replaceAll(":", "#");

        WorkflowProcess process = workflowManager.getProcess(processId);
        JSONObject jsonObject = new JSONObject();
        if (process != null) {
            jsonObject.accumulate("processId", process.getId());
            jsonObject.accumulate("packageId", process.getPackageId());
            jsonObject.accumulate("packageName", process.getPackageName());
            jsonObject.accumulate("name", process.getName());
            jsonObject.accumulate("version", process.getVersion());
        }
        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/json/workflow/process/start/(*:processDefId)")
    public void processStart(Writer writer, ModelMap map, HttpServletRequest request, @RequestParam(value = "callback", required = false) String callback, @RequestParam("processDefId") String processDefId, @RequestParam(value = "processInstanceId", required = false) String processInstanceId) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject();
        String processId = "";
        String activityId = "";

        //decode process def id (to default value)
        processDefId = processDefId.replaceAll(":", "#");

        map.addAttribute("queryString", request.getQueryString());

        Enumeration enumeration = request.getParameterNames();
        Map<String, String> variables = new HashMap();

        //loop through all parameters to get the workflow variables
        while (enumeration.hasMoreElements()) {
            String paramName = String.valueOf(enumeration.nextElement());
            if (paramName.startsWith("var_")) {
                variables.put(paramName.replace("var_", ""), request.getParameter(paramName));
            }
        }

        if (workflowManager.isUserInWhiteList(processDefId)) {
            WorkflowProcessResult result;
            appService.getAppDefinitionWithProcessDefId(processDefId);
            if (processInstanceId != null && processInstanceId.trim().length() > 0) {
                result = workflowManager.processStartWithInstanceId(processDefId, processInstanceId, variables);
            } else {
                result = workflowManager.processStart(processDefId, variables);
            }

            if (result != null) {
                WorkflowProcess processStarted = result.getProcess();
                if (processStarted != null) {
                    processId = processStarted.getInstanceId();
                }
                // check for automatic continuation
                String packageId = WorkflowUtil.getProcessDefPackageId(processDefId);
                String packageVersion = WorkflowUtil.getProcessDefVersion(processDefId);
                boolean continueNextAssignment = appService.isActivityAutoContinue(packageId, packageVersion, processDefId, WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
                Collection<WorkflowActivity> activities = result.getActivities();
                if (continueNextAssignment && activities != null && activities.size() > 0) {
                    activityId = ((WorkflowActivity) activities.iterator().next()).getId();
                }
            }
        }

        jsonObject.accumulate("processId", processId);
        jsonObject.accumulate("activityId", activityId);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/json/workflow/process/abort/(*:processId)")
    public void processAbort(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("processId") String processId) throws JSONException, IOException {
        appService.getAppDefinitionForWorkflowProcess(processId);
        boolean aborted = workflowManager.processAbort(processId);
        LogUtil.info(getClass().getName(), "Process " + processId + " aborted: " + aborted);
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("processId", processId);
        jsonObject.accumulate("status", "aborted");

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/assignment/list/count")
    public void assignmentPendingAndAcceptedListCount(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "packageId", required = false) String packageId, @RequestParam(value = "processDefId", required = false) String processDefId, @RequestParam(value = "processId", required = false) String processId) throws JSONException, IOException {
        Integer total = new Integer(workflowManager.getAssignmentSize(packageId, processDefId, processId));

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("total", total);
        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/assignment/list/pending/count")
    public void assignmentPendingListCount(Writer writer, @RequestParam(value = "callback", required = false) String callback) throws JSONException, IOException {
        Integer total = new Integer(workflowManager.getAssignmentSize(Boolean.FALSE, null));

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("total", total);
        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/assignment/list/accepted/count")
    public void assignmentAcceptedListCount(Writer writer, @RequestParam(value = "callback", required = false) String callback) throws JSONException, IOException {
        Integer total = new Integer(workflowManager.getAssignmentSize(Boolean.TRUE, null));

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("total", total);
        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/assignment/list")
    public void assignmentPendingAndAcceptedList(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "packageId", required = false) String packageId, @RequestParam(value = "processDefId", required = false) String processDefId, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {
        PagedList<WorkflowAssignment> assignmentList = workflowManager.getAssignmentPendingAndAcceptedList(packageId, processDefId, processId, sort, desc, start, rows);
        Integer total = assignmentList.getTotal();
        JSONObject jsonObject = new JSONObject();

        User user = directoryManager.getUserByUsername(workflowUserManager.getCurrentUsername());
        String gmt = "";
        if (user != null) {
            gmt = user.getTimeZone();
        }
        String format = AppUtil.getAppDateFormat();
        for (WorkflowAssignment assignment : assignmentList) {
            Map data = new HashMap();
            data.put("processId", assignment.getProcessId());
            data.put("activityId", assignment.getActivityId());
            data.put("processName", assignment.getProcessName());
            data.put("activityName", assignment.getActivityName());
            data.put("processVersion", assignment.getProcessVersion());
            data.put("dateCreated", TimeZoneUtil.convertToTimeZone(assignment.getDateCreated(), gmt, format));
            data.put("acceptedStatus", assignment.isAccepted());
            data.put("due", assignment.getDueDate() != null ? TimeZoneUtil.convertToTimeZone(assignment.getDueDate(), gmt, format) : "-");

            double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningActivity(assignment.getActivityId());

            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

            data.put("id", assignment.getActivityId());
            data.put("label", assignment.getActivityName());
            data.put("description", assignment.getDescription());

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", total);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/assignment/list/pending")
    public void assignmentPendingList(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {
        PagedList<WorkflowAssignment> assignmentList = workflowManager.getAssignmentPendingList(processId, sort, desc, start, rows);
        Integer total = assignmentList.getTotal();
        JSONObject jsonObject = new JSONObject();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");

        for (WorkflowAssignment assignment : assignmentList) {
            Map data = new HashMap();
            data.put("processId", assignment.getProcessId());
            data.put("activityId", assignment.getActivityId());
            data.put("processName", assignment.getProcessName());
            data.put("activityName", assignment.getActivityName());
            data.put("processVersion", assignment.getProcessVersion());
            data.put("dateCreated", dateFormat.format(assignment.getDateCreated()));
            data.put("due", assignment.getDueDate() != null ? assignment.getDueDate() : "-");

            double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningActivity(assignment.getActivityId());

            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

            data.put("id", assignment.getActivityId());
            data.put("label", assignment.getActivityName());
            data.put("description", assignment.getDescription());

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", total);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/assignment/list/accepted")
    public void assignmentAcceptedList(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException, IOException {
        PagedList<WorkflowAssignment> assignmentList = workflowManager.getAssignmentAcceptedList(processId, sort, desc, start, rows);
        Integer total = assignmentList.getTotal();
        JSONObject jsonObject = new JSONObject();
        for (WorkflowAssignment assignment : assignmentList) {
            Map data = new HashMap();
            data.put("processId", assignment.getProcessId());
            data.put("activityId", assignment.getActivityId());
            data.put("processName", assignment.getProcessName());
            data.put("activityName", assignment.getActivityName());
            data.put("processVersion", assignment.getProcessVersion());
            data.put("dateCreated", assignment.getDateCreated());
            data.put("due", assignment.getDueDate() != null ? assignment.getDueDate() : "-");


            double serviceLevelMonitor = workflowManager.getServiceLevelMonitorForRunningActivity(assignment.getActivityId());

            data.put("serviceLevelMonitor", WorkflowUtil.getServiceLevelIndicator(serviceLevelMonitor));

            data.put("id", assignment.getActivityId());
            data.put("label", assignment.getActivityName());
            data.put("description", assignment.getDescription());

            jsonObject.accumulate("data", data);
        }

        jsonObject.accumulate("total", total);
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/assignment/list/pending/process")
    public void assignmentPendingProcessList(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "checkWhiteList", required = false) Boolean checkWhiteList) throws JSONException, IOException {
        Collection<WorkflowProcess> processList = workflowManager.getProcessList(null, null, null, null, null, true, checkWhiteList);

        Map<String, Map> processMap = new TreeMap();
        for (WorkflowProcess process : processList) {
            int size = workflowManager.getAssignmentSize(Boolean.FALSE, process.getId());
            if (size > 0) {
                String label = process.getName() + " ver " + process.getVersion() + " (" + size + ")";
                Map data = new HashMap();
                data.put("processDefId", process.getId());
                data.put("processName", process.getName());
                data.put("processVersion", process.getVersion());

                data.put("label", label);

                String url = "/json/workflow/assignment/list/pending?processId=" + process.getEncodedId();
                if (callback != null && callback.trim().length() > 0) {
                    url += "&callback=" + callback;
                }
                data.put("url", url);
                data.put("count", new Integer(size));
                processMap.put(label, data);
            }
        }


        JSONObject jsonObject = new JSONObject();
        for (Iterator i = processMap.keySet().iterator(); i.hasNext();) {
            String processName = (String) i.next();
            Map data = (Map) processMap.get(processName);
            jsonObject.accumulate("data", data);
        }

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/assignment/list/accepted/process")
    public void assignmentAcceptedProcessList(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "checkWhiteList", required = false) Boolean checkWhiteList) throws JSONException, IOException {
        Collection<WorkflowProcess> processList = workflowManager.getProcessList(null, null, null, null, null, true, checkWhiteList);

        Map<String, Map> processMap = new TreeMap();
        for (WorkflowProcess process : processList) {
            int size = workflowManager.getAssignmentSize(Boolean.TRUE, process.getId());
            if (size > 0) {
                String label = process.getName() + " ver " + process.getVersion() + " (" + size + ")";
                Map data = new HashMap();
                data.put("processDefId", process.getId());
                data.put("processName", process.getName());
                data.put("processVersion", process.getVersion());

                data.put("label", label);

                String url = "/json/workflow/assignment/list/accepted?processId=" + process.getEncodedId();
                if (callback != null && callback.trim().length() > 0) {
                    url += "&callback=" + callback;
                }
                data.put("url", url);
                data.put("count", new Integer(size));
                processMap.put(label, data);
            }
        }

        JSONObject jsonObject = new JSONObject();
        for (Iterator i = processMap.keySet().iterator(); i.hasNext();) {
            String processName = (String) i.next();
            Map data = (Map) processMap.get(processName);
            jsonObject.accumulate("data", data);
        }

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/assignment/view/(*:activityId)")
    public void assignmentView(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("activityId") String activityId) throws JSONException, IOException {
        WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
        if (assignment == null) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("activityId", assignment.getActivityId());
        jsonObject.accumulate("activityDefId", assignment.getActivityDefId());
        jsonObject.accumulate("processId", assignment.getProcessId());
        jsonObject.accumulate("processDefId", assignment.getProcessDefId());
        jsonObject.accumulate("processVersion", assignment.getProcessVersion());
        jsonObject.accumulate("processName", assignment.getProcessName());
        jsonObject.accumulate("activityName", assignment.getActivityName());
        jsonObject.accumulate("description", assignment.getDescription());
        jsonObject.accumulate("participant", assignment.getParticipant());
        jsonObject.accumulate("assigneeId", assignment.getAssigneeId());
        jsonObject.accumulate("assigneeName", assignment.getAssigneeName());

        User user = directoryManager.getUserByUsername(workflowUserManager.getCurrentUsername());
        String gmt = "";
        if (user != null) {
            gmt = user.getTimeZone();
        }
        String format = AppUtil.getAppDateFormat();
        jsonObject.accumulate("dateCreated", TimeZoneUtil.convertToTimeZone(assignment.getDateCreated(), gmt, format));
        if (assignment.getDueDate() != null) {
            jsonObject.accumulate("dueDate", TimeZoneUtil.convertToTimeZone(assignment.getDueDate(), gmt, format));
        }
        Collection<WorkflowVariable> variableList = workflowManager.getActivityVariableList(activityId);
        for (WorkflowVariable variable : variableList) {
            JSONObject variableObj = new JSONObject();
            variableObj.accumulate(variable.getId(), variable.getVal());
            jsonObject.accumulate("variable", variableObj);
        }

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/assignment/process/view/(*:processId)")
    public void assignmentViewByProcess(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("processId") String processId) throws JSONException, IOException {
        WorkflowAssignment assignment = workflowManager.getAssignmentByProcess(processId);
        if (assignment == null) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("activityId", assignment.getActivityId());
        jsonObject.accumulate("activityDefId", assignment.getActivityDefId());
        jsonObject.accumulate("processId", assignment.getProcessId());
        jsonObject.accumulate("processDefId", assignment.getProcessDefId());
        jsonObject.accumulate("processVersion", assignment.getProcessVersion());
        jsonObject.accumulate("processName", assignment.getProcessName());
        jsonObject.accumulate("activityName", assignment.getActivityName());
        jsonObject.accumulate("description", assignment.getDescription());
        jsonObject.accumulate("participant", assignment.getParticipant());
        jsonObject.accumulate("assigneeId", assignment.getAssigneeId());
        jsonObject.accumulate("assigneeName", assignment.getAssigneeName());

        User user = directoryManager.getUserByUsername(workflowUserManager.getCurrentUsername());
        String gmt = "";
        if (user != null) {
            gmt = user.getTimeZone();
        }
        String format = AppUtil.getAppDateFormat();
        jsonObject.accumulate("dateCreated", TimeZoneUtil.convertToTimeZone(assignment.getDateCreated(), gmt, format));
        if (assignment.getDueDate() != null) {
            jsonObject.accumulate("dueDate", TimeZoneUtil.convertToTimeZone(assignment.getDueDate(), gmt, format));
        }
        Collection<WorkflowVariable> variableList = workflowManager.getActivityVariableList(assignment.getActivityId());
        for (WorkflowVariable variable : variableList) {
            JSONObject variableObj = new JSONObject();
            variableObj.accumulate(variable.getId(), variable.getVal());
            jsonObject.accumulate("variable", variableObj);
        }

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/variable/list/(*:processId)")
    public void variableListByProcess(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("processId") String processId) throws JSONException, IOException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("processId", processId);

        Collection<WorkflowVariable> variableList = workflowManager.getProcessVariableList(processId);

        for (WorkflowVariable variable : variableList) {
            JSONObject variableObj = new JSONObject();
            variableObj.accumulate(variable.getId(), variable.getVal());
            jsonObject.accumulate("variable", variableObj);
        }

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/json/workflow/assignment/accept/(*:activityId)")
    public void assignmentAccept(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("activityId") String activityId) throws JSONException, IOException {
        appService.getAppDefinitionForWorkflowActivity(activityId);
        workflowManager.assignmentAccept(activityId);
        WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
        LogUtil.info(getClass().getName(), "Assignment " + activityId + " accepted");
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("assignment", assignment.getActivityId());
        jsonObject.accumulate("status", "accepted");

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/json/workflow/assignment/withdraw/(*:activityId)")
    public void assignmentWithdraw(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("activityId") String activityId) throws JSONException, IOException {
        appService.getAppDefinitionForWorkflowActivity(activityId);
        workflowManager.assignmentWithdraw(activityId);
        WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
        LogUtil.info(getClass().getName(), "Assignment " + activityId + " withdrawn");
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("assignment", assignment.getActivityId());
        jsonObject.accumulate("status", "withdrawn");

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/json/workflow/assignment/variable/(*:activityId)/(*:variable)")
    public void assignmentVariable(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("activityId") String activityId, @RequestParam("variable") String variable, @RequestParam("value") String value) throws JSONException, IOException {
        appService.getAppDefinitionForWorkflowActivity(activityId);
        workflowManager.assignmentVariable(activityId, variable, value);
        LogUtil.info(getClass().getName(), "Assignment variable " + variable + " set to " + value);
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("status", "variableSet");

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/json/workflow/assignment/completeWithVariable/(*:activityId)")
    public void assignmentCompleteWithVariable(HttpServletRequest request, Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("activityId") String activityId) throws JSONException, IOException {
        appService.getAppDefinitionForWorkflowActivity(activityId);
        WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
        String processId = (assignment != null) ? assignment.getProcessId() : "";
        
        if (assignment != null && !assignment.isAccepted()) {
            workflowManager.assignmentAccept(activityId);
        }

        Map<String, String> workflowVariableMap = AppUtil.retrieveVariableDataFromRequest(request);
        workflowManager.assignmentComplete(activityId, workflowVariableMap);
        LogUtil.info(getClass().getName(), "Assignment " + activityId + " completed");
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("assignment", assignment.getAssigneeId());
        jsonObject.accumulate("status", "completed");
        jsonObject.accumulate("processId", processId);
        jsonObject.accumulate("activityId", activityId);

        // check for automatic continuation
        String processDefId = assignment.getProcessDefId();
        String activityDefId = assignment.getActivityDefId();
        String packageId = WorkflowUtil.getProcessDefPackageId(processDefId);
        String packageVersion = WorkflowUtil.getProcessDefVersion(processDefId);
        boolean continueNextAssignment = appService.isActivityAutoContinue(packageId, packageVersion, processDefId, activityDefId);
        if (continueNextAssignment) {
            WorkflowAssignment nextAssignment = workflowManager.getAssignmentByProcess(processId);
            if (nextAssignment != null) {
                jsonObject.accumulate("nextActivityId", nextAssignment.getActivityId());
            }
        }

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping(value = "/json/workflow/assignment/complete/(*:activityId)")
    public void assignmentComplete(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("activityId") String activityId) throws JSONException, IOException {
        appService.getAppDefinitionForWorkflowActivity(activityId);
        WorkflowAssignment assignment = workflowManager.getAssignment(activityId);

        String processId = (assignment != null) ? assignment.getProcessId() : "";

        if (assignment != null && !assignment.isAccepted()) {
            workflowManager.assignmentAccept(activityId);
        }

        workflowManager.assignmentComplete(activityId);
        LogUtil.info(getClass().getName(), "Assignment " + activityId + " completed");
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("status", "completed");
        jsonObject.accumulate("processId", processId);
        jsonObject.accumulate("activityId", activityId);

        // check for auto continuation
        String processDefId = assignment.getProcessDefId();
        String activityDefId = assignment.getActivityDefId();
        String packageId = WorkflowUtil.getProcessDefPackageId(processDefId);
        String packageVersion = WorkflowUtil.getProcessDefVersion(processDefId);
        boolean continueNextAssignment = appService.isActivityAutoContinue(packageId, packageVersion, processDefId, activityDefId);
        if (continueNextAssignment) {
            WorkflowAssignment nextAssignment = workflowManager.getAssignmentByProcess(processId);
            if (nextAssignment != null) {
                jsonObject.accumulate("nextActivityId", nextAssignment.getActivityId());
            }
        }

        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/currentUsername")
    public void getCurrentUsername(Writer writer, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("username", workflowManager.getWorkflowUserManager().getCurrentUsername());
        writeJson(writer, jsonObject, callback);
    }

    @RequestMapping("/json/workflow/testConnection")
    public void testConnection(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam("datasource") String datasource, @RequestParam("driver") String driver, @RequestParam("url") String url, @RequestParam("user") String user, @RequestParam("password") String password) throws IOException, JSONException {
        boolean success = DynamicDataSourceManager.testConnection(driver, url, user, password);

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("datasource", datasource);
        jsonObject.accumulate("success", success);
        writeJson(writer, jsonObject, callback);
    }

    protected static void writeJson(Writer writer, JSONObject jsonObject, String callback) throws IOException, JSONException {
        if (callback != null && callback.trim().length() > 0) {
            writer.write(StringEscapeUtils.escapeHtml(callback) + "(");
        }
        jsonObject.write(writer);
        if (callback != null && callback.trim().length() > 0) {
            writer.write(")");
        }
    }

    @RequestMapping(value = "/monitoring/running/activity/reassign", method = RequestMethod.POST)
    public void assignmentReassign(Writer writer, @RequestParam("state") String state, @RequestParam("processDefId") String processDefId, @RequestParam("username") String username, @RequestParam("replaceUser") String replaceUser, @RequestParam("activityId") String activityId, @RequestParam("processId") String processId) {
        appService.getAppDefinitionForWorkflowActivity(activityId);
        workflowManager.assignmentReassign(processDefId, processId, activityId, username, replaceUser);
    }

    @RequestMapping("/monitoring/running/activity/complete")
    public void completeProcess(Writer writer, @RequestParam("state") String state, @RequestParam("processDefId") String processDefId, @RequestParam("activityId") String activityId, @RequestParam("processId") String processId) {
        String username = workflowUserManager.getCurrentUsername();
        appService.getAppDefinitionForWorkflowActivity(activityId);
        workflowManager.assignmentForceComplete(processDefId, processId, activityId, username);
    }
    
    @RequestMapping("/json/apps/published/userviews")
    public void publishedApps(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "appId", required = false) String appId) throws JSONException, IOException {
        Collection<AppDefinition> appDefinitionList = appService.getPublishedApps(appId);
        JSONObject root = new JSONObject();
        JSONArray apps = new JSONArray();
        for (AppDefinition appDef: appDefinitionList) {
            JSONObject app = new JSONObject();
            app.accumulate("id", appDef.getAppId());
            app.accumulate("name", appDef.getName());
            app.accumulate("version", appDef.getVersion());
            JSONArray userviews = new JSONArray();
            for (UserviewDefinition userviewDef: appDef.getUserviewDefinitionList()) {
                JSONObject userview = new JSONObject();
                userview.accumulate("id", userviewDef.getId());
                userview.accumulate("name", userviewDef.getName());
                userview.accumulate("version", userviewDef.getAppVersion());
                String url = WorkflowUtil.getHttpServletRequest().getContextPath() + "/web/userview/" + appDef.getId() + "/" + userviewDef.getId();
                userview.accumulate("url", url);
                userviews.put(userview);
            }
            app.put("userviews", userviews);
            apps.put(app);
        }
        root.put("apps", apps);
        writeJson(writer, root, callback);
    }

    @RequestMapping("/json/apps/published/processes")
    public void publishedProcesses(Writer writer, @RequestParam(value = "callback", required = false) String callback, @RequestParam(value = "appId", required = false) String appId) throws JSONException, IOException {
        // get list of published processes
        Map<AppDefinition, Collection<WorkflowProcess>> appProcessMap = appService.getPublishedProcesses(appId);
        JSONObject root = new JSONObject();
        JSONArray apps = new JSONArray();
        for (Iterator<AppDefinition> i=appProcessMap.keySet().iterator(); i.hasNext();) {
            AppDefinition appDef = i.next();
            Collection<WorkflowProcess> processList = appProcessMap.get(appDef);
            JSONObject app = new JSONObject();
            app.accumulate("id", appDef.getAppId());
            app.accumulate("name", appDef.getName());
            app.accumulate("version", appDef.getVersion());
            JSONArray processes = new JSONArray();
            for (WorkflowProcess processDef: processList) {
                JSONObject process = new JSONObject();
                process.accumulate("id", processDef.getId());
                process.accumulate("idWithoutVersion", processDef.getIdWithoutVersion());
                process.accumulate("name", processDef.getName());
                process.accumulate("processVersion", processDef.getVersion());
                process.accumulate("appVersion", appDef.getVersion());
                String url = WorkflowUtil.getHttpServletRequest().getContextPath() + "/web/client/app/" + appDef.getId() + "/" + appDef.getVersion() + "/process/" + processDef.getIdWithoutVersion() + "?start=true";
                process.accumulate("url", url);
                processes.put(process);
            }
            app.put("processes", processes);
            apps.put(app);
        }
        root.put("apps", apps);
        writeJson(writer, root, callback);
    }
    
}
