package org.kecak.webapi.json.controller;

import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.plugin.base.PluginManager;
import org.joget.report.service.ReportManager;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowPackage;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowProcessResult;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.kecak.apps.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Optional;

@Controller
public class WorkflowAdministrationJsonController {
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
    @Autowired
    private PluginManager pluginManager;

    @RequestMapping("/json/workflow/assignment/process/(*:processId)/transfer/(*:targetActivityDefId)")
    public void assignmentTransfer(final HttpServletRequest request, final HttpServletResponse response,
                                   @RequestParam("processId") final String processId,
                                   @RequestParam("targetActivityDefId") final String targetActivityDefId) throws JSONException, IOException {

        LogUtil.info(getClass().getName(), "Executing JSON Rest API [" + request.getRequestURI() + "] in method [" + request.getMethod() + "] as [" + WorkflowUtil.getCurrentUsername() + "]");

        try {
            if(!WorkflowUtil.isCurrentUserInRole(WorkflowUtil.ROLE_ADMIN)) {
                throw new ApiException(HttpServletResponse.SC_UNAUTHORIZED, "User is not administrator");
            }

            WorkflowProcessResult result = workflowManager.assignmentTransfer(processId, targetActivityDefId);

            String resultActivityId = Optional.ofNullable(result)
                    .map(WorkflowProcessResult::getActivities)
                    .orElse(Collections.emptyList())
                    .stream().findFirst()
                    .map(WorkflowActivity::getId)
                    .orElse("");

            String resultProcessId = Optional.ofNullable(result)
                    .map(WorkflowProcessResult::getProcess)
                    .map(WorkflowProcess::getInstanceId)
                    .orElse("");


            JSONObject jsonReturn = new JSONObject();
            jsonReturn.put("processId", resultProcessId);
            jsonReturn.put("activityId", resultActivityId);

            response.getWriter().write(jsonReturn.toString());
        } catch (ApiException e) {
            LogUtil.warn(getClass().getName(), e.getMessage());
            response.sendError(e.getErrorCode(), e.getMessage());
        }
    }

    @RequestMapping(value = "/json/workflow/monitoring/package/(*:packageId)/(*:fromVersion)/update", method = {RequestMethod.POST, RequestMethod.PUT})
    public void updateRunningProcesses(final HttpServletRequest request, final HttpServletResponse response, @RequestParam("packageId") String packageId, @RequestParam("fromVersion") Long fromVersion, @RequestParam(value = "callback", required = false) String callback) throws JSONException, IOException {
        String packageVersion = workflowManager.getCurrentPackageVersion(packageId);
        if(packageVersion != null) {
            WorkflowPackage workflowPackage = workflowManager.getPackage(packageId, packageVersion);
            if(workflowPackage != null) {
                WorkflowPackage fromWorkflowPackage = workflowManager.getPackage(workflowPackage.getPackageId(), String.valueOf(fromVersion));
                if(fromWorkflowPackage != null) {
                    String message = "Starting to update package [" + workflowPackage.getPackageId() + "] from version [" + fromVersion + "] to [" + workflowPackage.getVersion() + "]. This may take a while.";
                    appService.updateRunningProcesses(workflowPackage.getPackageId(), fromVersion, Long.parseLong(workflowPackage.getVersion()));

                    LogUtil.info(getClass().getName(), message);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("message", message);
                    AppUtil.writeJson(response.getWriter(), jsonObject, callback);
                } else {
                    String message = "Invalid package [" + workflowPackage.getPackageId() + "] [" + fromVersion + "]";
                    LogUtil.warn(getClass().getName(), message);
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
                }
            } else {
                String message = "Invalid package ["+packageId+"] ["+packageVersion+"]";
                LogUtil.warn(getClass().getName(), message);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
            }
        } else {
            String message = "Invalid package version ["+packageId+"]";
            LogUtil.warn(getClass().getName(), message);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
        }
    }

    /**
     * Get info of current kecak instance
     *
     * @param writer
     * @param callback
     * @throws IOException
     * @throws JSONException
     */
    @RequestMapping(value = "/json/build/info", method = RequestMethod.GET)
    public void getBuildInfo(Writer writer, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("buildNumber", ResourceBundleUtil.getMessage("build.number"));
        AppUtil.writeJson(writer, jsonObject, callback);
    }
}
