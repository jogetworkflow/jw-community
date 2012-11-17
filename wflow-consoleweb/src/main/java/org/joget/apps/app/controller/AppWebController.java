package org.joget.apps.app.controller;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FileUtil;
import org.joget.apps.form.service.FormService;
import org.joget.apps.userview.lib.RunProcess;
import org.joget.apps.workflow.lib.AssignmentCompleteButton;
import org.joget.apps.workflow.lib.AssignmentWithdrawButton;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowProcessResult;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AppWebController {

    @Autowired
    private AppService appService;
    @Autowired
    private FormService formService;
    @Autowired
    private WorkflowManager workflowManager;
    @Autowired
    FormDefinitionDao formDefinitionDao;

    @RequestMapping("/client/app/(*:appId)/(~:version)/process/(*:processDefId)")
    public String clientProcessView(HttpServletRequest request, ModelMap model, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam(required = false) String recordId, @RequestParam(required = false) String start) {

        // clean process def
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        WorkflowProcess processDef = appService.getWorkflowProcessForApp(appId, appDef.getVersion().toString(), processDefId);

        // check for permission
        if (!workflowManager.isUserInWhiteList(processDef.getId())) {
            return "client/app/processUnauthorized";
        }

        // set app and process details
        model.addAttribute("appId", appId);
        model.addAttribute("appVersion", appDef.getVersion());
        model.addAttribute("appDefinition", appDef);
        model.addAttribute("process", processDef);
        model.addAttribute("queryString", request.getQueryString());

        // check for start mapped form
        FormData formData = new FormData();
        formData = formService.retrieveFormDataFromRequest(formData, request);
        formData.setPrimaryKeyValue(recordId);
        
        String formUrl = "/web/client/app/" + appId + "/" + appDef.getVersion() + "/process/" + processDefId + "/start";
        if (recordId != null) {
            formUrl += "?recordId=" + recordId;
        }
        String formUrlWithContextPath = AppUtil.getRequestContextPath() + formUrl;
        
        PackageActivityForm startFormDef = appService.viewStartProcessForm(appId, appDef.getVersion().toString(), processDefId, formData, formUrlWithContextPath);
        if (startFormDef != null && startFormDef.getForm() != null) {
            Form startForm = startFormDef.getForm();

            // generate form HTML
            String formHtml = formService.retrieveFormHtml(startForm, formData);
            String formJson = formService.generateElementJson(startForm);

            // show form
            model.addAttribute("form", startForm);
            model.addAttribute("formJson", formJson);
            model.addAttribute("formHtml", formHtml);
            return "client/app/processFormStart";
        } else {
            if (Boolean.valueOf(start).booleanValue()) {
                Map requestParam = formData.getRequestParams();
                for (Object k : requestParam.keySet()) {
                    String key = (String) k;
                    if (key.startsWith(FormService.PREFIX_FOREIGN_KEY) || key.startsWith(FormService.PREFIX_FOREIGN_KEY_EDITABLE) || key.startsWith(AppUtil.PREFIX_WORKFLOW_VARIABLE)) {
                        try {
                            String[] values = (String[]) requestParam.get(k);
                            
                            for (String v : values) {
                                if (formUrl.contains("?")) {
                                    formUrl += "&";
                                } else {
                                    formUrl += "?";
                                }
                                
                                formUrl += key + "=" + URLEncoder.encode(v, "UTF-8");
                            }
                        } catch (Exception e) {
                            LogUtil.info(RunProcess.class.getName(), "Paramter:" + key + "cannot be append to URL");
                        }
                    }
                }
                // redirect to start URL
                return "redirect:" + formUrl;
            } else {
                // empty start page
                return "client/app/processStart";
            }
        }
    }

    @RequestMapping("/client/app/(*:appId)/(~:version)/process/(*:processDefId)/start")
    public String clientProcessStart(HttpServletRequest request, ModelMap model, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam(required = false) String recordId, @RequestParam String processDefId) {

        // clean process def
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);

        // set app and process details
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        WorkflowProcess processDef = appService.getWorkflowProcessForApp(appId, appDef.getVersion().toString(), processDefId);
        String processDefIdWithVersion = processDef.getId();
        model.addAttribute("appId", appId);
        model.addAttribute("appVersion", appDef.getVersion());
        model.addAttribute("appDefinition", appDef);
        model.addAttribute("process", processDef);

        // check for permission
        if (!workflowManager.isUserInWhiteList(processDef.getId())) {
            return "client/app/processUnauthorized";
        }

        // extract form values from request
        FormData formData = new FormData();
        formData.setPrimaryKeyValue(recordId);
        formData = formService.retrieveFormDataFromRequest(formData, request);

        // get workflow variables
        Map<String, String> variableMap = AppUtil.retrieveVariableDataFromRequest(request);
        String formUrl = AppUtil.getRequestContextPath() + "/web/client/app/" + appId + "/" + appDef.getVersion() + "/process/" + processDefId + "/start";
        if (recordId != null) {
            formUrl += "?recordId=" + recordId;
        }
        PackageActivityForm startFormDef = appService.viewStartProcessForm(appId, appDef.getVersion().toString(), processDefId, formData, formUrl);
        WorkflowProcessResult result = appService.submitFormToStartProcess(appId, version, processDefId, formData, variableMap, recordId, formUrl);
        if (startFormDef != null && (startFormDef.getForm() != null || PackageActivityForm.ACTIVITY_FORM_TYPE_EXTERNAL.equals(startFormDef.getType()))) {
            if (result == null) {
                // validation error, get form
                Form startForm = startFormDef.getForm();

                // generate form HTML
                String formHtml = formService.retrieveFormErrorHtml(startForm, formData);
                String formJson = formService.generateElementJson(startForm);

                // show form
                model.addAttribute("form", startForm);
                model.addAttribute("formJson", formJson);
                model.addAttribute("formHtml", formHtml);
                model.addAttribute("stay", formData.getStay());
                model.addAttribute("errorCount", formData.getFormErrors().size());
                model.addAttribute("submitted", Boolean.TRUE);
                model.addAttribute("activityForm", startFormDef);
                return "client/app/processFormStart";
            }
        } else {
            // start process - TODO: handle process linking
            result = workflowManager.processStart(processDefIdWithVersion, null, variableMap, null, recordId, false);
        }

        // set result
        if (result != null) {
            WorkflowProcess process = result.getProcess();
            model.addAttribute("process", process);

            // redirect to next activity if available
            Collection<WorkflowActivity> activities = result.getActivities();
            if (activities != null && !activities.isEmpty()) {
                WorkflowActivity nextActivity = activities.iterator().next();
                String assignmentUrl = "/web/client/app/" + appId + "/" + appDef.getVersion() + "/assignment/" + nextActivity.getId() + "?" + request.getQueryString();
                return "redirect:" + assignmentUrl;
            }
        }

        return "client/app/processStarted";
    }

    @RequestMapping("/client/app/(~:appId)/(~:version)/assignment/(*:activityId)")
    public String clientAssignmentView(HttpServletRequest request, ModelMap model, @RequestParam(required = false) String appId, @RequestParam(required = false) String version, @RequestParam("activityId") String activityId) {
        // check assignment
        WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
        if (assignment == null) {
            return "client/app/assignmentUnavailable";
        }

        try {
            // get app
            AppDefinition appDef = null;
            if (appId != null && !appId.isEmpty()) {
                if (version == null || version.isEmpty()) {
                    version = appService.getPublishedVersion(appId).toString();
                }
                appDef = appService.getAppDefinition(appId, version);
            } else {
                appDef = appService.getAppDefinitionForWorkflowActivity(activityId);
                if (appDef != null) {
                    appId = appDef.getId();
                }
            }

            FormData formData = new FormData();
            formData = formService.retrieveFormDataFromRequest(formData, request);

            // get form
            String appVersion = (appDef != null) ? appDef.getVersion().toString() : "";
            String formUrl = AppUtil.getRequestContextPath() + "/web/client/app/" + appId + "/" + appVersion + "/assignment/" + activityId + "/submit";
            PackageActivityForm activityForm = appService.viewAssignmentForm(appId, appVersion.toString(), activityId, formData, formUrl);
            Form form = activityForm.getForm();

            // generate form HTML
            String formHtml = formService.retrieveFormHtml(form, formData);
            String formJson = formService.generateElementJson(form);

            model.addAttribute("appDef", appDef);
            model.addAttribute("assignment", assignment);
            model.addAttribute("activityForm", activityForm);
            model.addAttribute("form", form);
            model.addAttribute("formHtml", formHtml);
            model.addAttribute("formJson", formJson);
        } catch (Exception e) {
            Logger.getLogger(AppWebController.class.getName()).log(Level.SEVERE, null, e);
        }

        return "client/app/assignmentView";
    }

    @RequestMapping("/client/app/(~:appId)/(~:version)/assignment/(*:activityId)/submit")
    public String clientAssignmentSubmit(HttpServletRequest request, ModelMap model, @RequestParam(required = false) String appId, @RequestParam(required = false) String version, @RequestParam("activityId") String activityId) {
        // check assignment
        WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
        if (assignment == null) {
            return "client/app/assignmentUnavailable";
        }

        // get app
        AppDefinition appDef = null;
        if (appId != null && !appId.isEmpty()) {
            appDef = appService.getAppDefinition(appId, version);
        } else {
            appDef = appService.getAppDefinitionForWorkflowActivity(activityId);
        }

        // extract form values from request
        FormData formData = new FormData();
        formData = formService.retrieveFormDataFromRequest(formData, request);

        // set process instance ID as primary key
        String processId = assignment.getProcessId();

        // load form
        Long appVersion = (appDef != null) ? appDef.getVersion() : null;
        String formUrl = AppUtil.getRequestContextPath() + "/web/client/app/" + appId + "/" + appVersion + "/assignment/" + activityId + "/submit";
        PackageActivityForm activityForm = appService.viewAssignmentForm(appDef, assignment, formData, formUrl);
        Form form = activityForm.getForm();

        // submit form
        FormData formResult = formService.executeFormActions(form, formData);

        if (formResult.getFormResult(AssignmentWithdrawButton.DEFAULT_ID) != null) {
            // withdraw assignment
            workflowManager.assignmentWithdraw(activityId);
            return "client/app/dialogClose";

        } else if (formResult.getFormResult(AssignmentCompleteButton.DEFAULT_ID) != null) {
            // complete assignment
            Map<String, String> variableMap = AppUtil.retrieveVariableDataFromRequest(request);
            formResult = appService.completeAssignmentForm(form, assignment, formData, variableMap);

            Map<String, String> errors = formResult.getFormErrors();
            if (!formResult.getStay() && errors.isEmpty() && activityForm.isAutoContinue()) {
                // redirect to next activity if available
                WorkflowAssignment nextActivity = workflowManager.getAssignmentByProcess(processId);
                if (nextActivity != null) {
                    String assignmentUrl = "/web/client/app/" + appId + "/" + appVersion + "/assignment/" + nextActivity.getActivityId();
                    return "redirect:" + assignmentUrl;
                }
            }
        }

        String html = null;

        // check for validation errors
        Map<String, String> errors = formResult.getFormErrors();
        int errorCount = 0;
        if (!formResult.getStay() && errors == null || errors.isEmpty()) {
            // render normal template
            html = formService.generateElementHtml(form, formResult);
        } else {
            // render error template
            html = formService.generateElementErrorHtml(form, formResult);
            errorCount = errors.size();
        }
        String formJson = formService.generateElementJson(form);

        model.addAttribute("assignment", assignment);
        model.addAttribute("form", form);
        model.addAttribute("formHtml", html);
        model.addAttribute("formJson", formJson);
        model.addAttribute("formResult", formResult);
        model.addAttribute("stay", formResult.getStay());
        model.addAttribute("errorCount", errorCount);
        model.addAttribute("submitted", Boolean.TRUE);
        model.addAttribute("closeDialog", Boolean.TRUE);

        return "client/app/assignmentView";
    }

    /**
     * Download uploaded files.
     * @param response
     * @param fileName
     * @param processInstanceId
     * @throws IOException
     */
    @RequestMapping("/client/app/(*:appId)/(~:version)/form/download/(*:formDefId)/(*:primaryKeyValue)/(*:fileName)")
    public void downloadUploadedFile(HttpServletResponse response, @RequestParam("formDefId") String formDefId, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("primaryKeyValue") String primaryKeyValue, @RequestParam("fileName") String fileName, @RequestParam(required = false) String attachment) throws IOException {
        ServletOutputStream stream = response.getOutputStream();
        Form form = null;
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        FormDefinition formDef = formDefinitionDao.loadById(formDefId, appDef);
        if (formDef != null) {
            String json = formDef.getJson();
            form = (Form) formService.createElementFromJson(json);
        }
        String decodedFileName = fileName;
        try {
            decodedFileName = URLDecoder.decode(fileName, "UTF8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        File file = FileUtil.getFile(decodedFileName, form, primaryKeyValue);
        if (file.isDirectory() || !file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        byte[] bbuf = new byte[65536];

        try {
            // set attachment filename
            if (Boolean.valueOf(attachment).booleanValue()) {
                response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
            }

            // send output
            int length = 0;
            while ((in != null) && ((length = in.read(bbuf)) != -1)) {
                stream.write(bbuf, 0, length);
            }
        } finally {
            in.close();
            stream.flush();
            stream.close();
        }
    }
}
