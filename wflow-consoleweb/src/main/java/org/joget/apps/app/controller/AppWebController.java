package org.joget.apps.app.controller;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.dao.AppResourceDao;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AppResource;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppResourceUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FileDownloadSecurity;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.CustomFormDataTableUtil;
import org.joget.apps.form.service.FileUtil;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.lib.RunProcess;
import org.joget.apps.userview.model.Permission;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.service.UserviewService;
import org.joget.apps.userview.service.UserviewThemeProcesser;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.apps.workflow.lib.AssignmentCompleteButton;
import org.joget.apps.workflow.lib.AssignmentWithdrawButton;
import org.joget.commons.util.FileManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowProcessResult;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    @Autowired
    AppResourceDao appResourceDao;
    @Autowired
    PluginManager pluginManager;
    @Autowired
    private WorkflowUserManager workflowUserManager;
    @Autowired
    UserviewService userviewService;
    @Autowired
    UserviewDefinitionDao userviewDefinitionDao;
    @Autowired
    BuilderDefinitionDao builderDefinitionDao;
    @Autowired
    FormDataDao formDataDao;
    
    protected static String ORIGIN_FROM_PARAM = "_orifrom";
    protected static String ORIGIN_FROM_RUNPROCESS = "runProcess";

    @RequestMapping("/client/app/(*:appId)/(~:version)/process/(*:processDefId)")
    public String clientProcessView(HttpServletRequest request, ModelMap model, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam String processDefId, @RequestParam(required = false) String recordId, @RequestParam(required = false) String start) {

        // clean process def
        appId = SecurityUtil.validateStringInput(appId);        
        processDefId = SecurityUtil.validateStringInput(processDefId);        
        recordId = SecurityUtil.validateStringInput(recordId);        
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        WorkflowProcess processDef = appService.getWorkflowProcessForApp(appId, appDef.getVersion().toString(), processDefId);

        // check for permission
        if (!workflowManager.isUserInWhiteList(processDef.getId())) {
            return "client/app/processUnauthorized";
        }

        // set app and process details
        model.addAttribute("appId", appDef.getId());
        model.addAttribute("appVersion", appDef.getVersion());
        model.addAttribute("appDefinition", appDef);
        model.addAttribute("process", processDef);
        model.addAttribute("queryString", request.getQueryString());

        // check for start mapped form
        FormData formData = new FormData();
        formData = formService.retrieveFormDataFromRequest(formData, request);
        formData.setPrimaryKeyValue(recordId);
        
        String formUrl = "/web/client/app/" + appDef.getId() + "/" + appDef.getVersion() + "/process/" + StringEscapeUtils.escapeHtml(processDefId) + "/start";
        if (recordId != null) {
            formUrl += "?recordId=" + StringEscapeUtils.escapeHtml(recordId);
        }
        String formUrlWithContextPath = AppUtil.getRequestContextPath() + formUrl;
        
        PackageActivityForm startFormDef = appService.viewStartProcessForm(appDef.getId(), appDef.getVersion().toString(), processDefId, formData, formUrlWithContextPath);
        if (startFormDef != null && startFormDef.getForm() != null) {
            Form startForm = startFormDef.getForm();

            // generate form HTML
            String formHtml = formService.retrieveFormHtml(startForm, formData);

            // show form
            model.addAttribute("form", startForm);
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
                model.addAttribute("formUrl", formUrl);
                // redirect to start URL
                return "client/app/processStartRedirect";
            } else {
                // empty start page
                return "client/app/processStart";
            }
        }
    }

    @RequestMapping(value = "/client/app/(*:appId)/(~:version)/process/(*:processDefId)/start", method = RequestMethod.POST)
    public String clientProcessStart(HttpServletRequest request, ModelMap model, @RequestParam("appId") String appId, @RequestParam(required = false) String version, @RequestParam(required = false) String recordId, @RequestParam String processDefId) {

        // clean process def
        appId = SecurityUtil.validateStringInput(appId);        
        recordId = SecurityUtil.validateStringInput(recordId);        
        processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);

        // set app and process details
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        WorkflowProcess processDef = appService.getWorkflowProcessForApp(appId, appDef.getVersion().toString(), processDefId);
        String processDefIdWithVersion = processDef.getId();
        model.addAttribute("appId", appDef.getId());
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
        String formUrl = AppUtil.getRequestContextPath() + "/web/client/app/" + appDef.getId() + "/" + appDef.getVersion() + "/process/" + processDefId + "/start";
        if (recordId != null) {
            formUrl += "?recordId=" + recordId;
        }
        PackageActivityForm startFormDef = appService.viewStartProcessForm(appDef.getId(), appDef.getVersion().toString(), processDefId, formData, formUrl);
        WorkflowProcessResult result = appService.submitFormToStartProcess(appDef.getId(), appDef.getVersion().toString(), processDefId, formData, variableMap, recordId, formUrl);
        if (startFormDef != null && (startFormDef.getForm() != null || PackageActivityForm.ACTIVITY_FORM_TYPE_EXTERNAL.equals(startFormDef.getType()))) {
            if (result == null) {
                // validation error, get form
                Form startForm = startFormDef.getForm();

                // generate form HTML
                String formHtml = formService.retrieveFormErrorHtml(startForm, formData);

                // show form
                model.addAttribute("form", startForm);
                model.addAttribute("formHtml", formHtml);
                model.addAttribute("stay", formData.getStay());
                model.addAttribute("errorCount", formData.getFormErrors().size());
                model.addAttribute("submitted", Boolean.TRUE);
                model.addAttribute("activityForm", startFormDef);
                model.addAttribute("appDef", appDef);
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
                String assignmentUrl = "/web/client/app/" + appDef.getId() + "/" + appDef.getVersion() + "/assignment/" + nextActivity.getId() + "?" + request.getQueryString();
                assignmentUrl = StringUtil.addParamsToUrl(assignmentUrl, ORIGIN_FROM_PARAM, ORIGIN_FROM_RUNPROCESS);
                return "redirect:" + assignmentUrl;
            }
        }

        return "client/app/processStarted";
    }

    @RequestMapping("/client/app/(~:appId)/(~:version)/assignment/(*:activityId)")
    public String clientAssignmentView(HttpServletRequest request, ModelMap model, @RequestParam(required = false) String appId, @RequestParam(required = false) String version, @RequestParam("activityId") String activityId) throws UnsupportedEncodingException {
        // check assignment
        appId = SecurityUtil.validateStringInput(appId);
        activityId = SecurityUtil.validateStringInput(activityId);
        
        if (request.getParameterValues(ORIGIN_FROM_PARAM) == null) {
            //redirect to default userview if inbox is available in default userview
            UserviewDefinition defaultUserview = userviewService.getDefaultUserview();
            if (UserviewUtil.checkUserviewInboxEnabled(defaultUserview)) {
                // redirect to app center userview
                String path = "redirect:/web/userview/" + defaultUserview.getAppId() + "/" +  defaultUserview.getId() + "/_/_ja_inbox?_mode=assignment&activityId=" + URLEncoder.encode(activityId, "UTF-8");
                return path;
            }
        }
        
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
            String formUrl = AppUtil.getRequestContextPath() + "/web/client/app/" + appDef.getId() + "/" + appVersion + "/assignment/" + activityId + "/submit";
            if (request.getParameterValues(ORIGIN_FROM_PARAM) != null) {
                formUrl = StringUtil.addParamsToUrl(formUrl, ORIGIN_FROM_PARAM, request.getParameterValues(ORIGIN_FROM_PARAM)[0]);
            }
            PackageActivityForm activityForm = appService.viewAssignmentForm(appDef.getId(), appVersion.toString(), activityId, formData, formUrl);
            Form form = activityForm.getForm();

            // generate form HTML
            String formHtml = formService.retrieveFormHtml(form, formData);

            model.addAttribute("appDef", appDef);
            model.addAttribute("assignment", assignment);
            model.addAttribute("activityForm", activityForm);
            model.addAttribute("form", form);
            model.addAttribute("formHtml", formHtml);
        } catch (Exception e) {
            LogUtil.error(AppWebController.class.getName(), e, "");
        }

        return "client/app/assignmentView";
    }

    @RequestMapping(value = "/client/app/(~:appId)/(~:version)/assignment/(*:activityId)/submit", method = RequestMethod.POST)
    public String clientAssignmentSubmit(HttpServletRequest request, ModelMap model, @RequestParam(required = false) String appId, @RequestParam(required = false) String version, @RequestParam("activityId") String activityId) {
        // check assignment
        WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
        if (assignment == null) {
            return "client/app/assignmentUnavailable";
        }

        // get app
        SecurityUtil.validateStringInput(appId);
        SecurityUtil.validateStringInput(activityId);
        AppDefinition appDef = null;
        if (appId != null && !appId.isEmpty()) {
            appDef = appService.getAppDefinition(appId, version);
        } else {
            appDef = appService.getAppDefinitionForWorkflowActivity(activityId);
        }
        if (appDef == null) {
            return "client/app/assignmentUnavailable";
        }

        // extract form values from request
        FormData formData = new FormData();
        formData = formService.retrieveFormDataFromRequest(formData, request);

        // set process instance ID as primary key
        String processId = assignment.getProcessId();

        // load form
        Long appVersion = appDef.getVersion();
        String formUrl = AppUtil.getRequestContextPath() + "/web/client/app/" + appDef.getId() + "/" + appVersion + "/assignment/" + activityId + "/submit";
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
            if (!formResult.getStay() && (errors == null || errors.isEmpty()) && activityForm.isAutoContinue()) {
                // redirect to next activity if available
                WorkflowAssignment nextActivity = workflowManager.getNextAssignmentByCurrentAssignment(assignment);
                if (nextActivity != null) {
                    String assignmentUrl = "/web/client/app/" + appDef.getId() + "/" + appVersion + "/assignment/" + nextActivity.getActivityId();
                    if (request.getParameterValues(ORIGIN_FROM_PARAM) != null) {
                        assignmentUrl = StringUtil.addParamsToUrl(assignmentUrl, ORIGIN_FROM_PARAM, request.getParameterValues(ORIGIN_FROM_PARAM)[0]);
                    }
                    return "redirect:" + assignmentUrl;
                }
            }
        }

        String html = null;

        // check for validation errors
        Map<String, String> errors = formResult.getFormErrors();
        int errorCount = 0;
        if (!formResult.getStay() && (errors == null || errors.isEmpty())) {
            // render normal template
            html = formService.generateElementHtml(form, formResult);
        } else {
            // render error template
            html = formService.generateElementErrorHtml(form, formResult);
            errorCount = errors.size();
        }

        model.addAttribute("assignment", assignment);
        model.addAttribute("form", form);
        model.addAttribute("formHtml", html);
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
    public void downloadUploadedFile(HttpServletRequest request, HttpServletResponse response, @RequestParam("formDefId") String formDefId, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("primaryKeyValue") String primaryKeyValue, @RequestParam("fileName") String fileName, @RequestParam(required = false) String attachment) throws IOException {
        boolean isAuthorize = false;
        
        Form form = null;
        String tableName = null;
        AppDefinition appDef;
        
        try {
            if (appId != null && !appId.isEmpty()
                    && formDefId != null && !formDefId.isEmpty() 
                    && primaryKeyValue != null && !primaryKeyValue.isEmpty() 
                    && fileName != null && !fileName.isEmpty()) {
                
                appDef = appService.getAppDefinition(appId, version);
                FormDefinition formDef = formDefinitionDao.loadById(formDefId, appDef);
                
                // remove last dot in filename for Spring 5
                if (fileName.endsWith(".")) {
                    fileName = fileName.substring(0, fileName.length()-1);
                }
                
                if (formDef != null) {
                    String json = formDef.getJson();
                    form = (Form) formService.createElementFromJson(json);

                    if (form != null && form.getLoadBinder() != null) {
                        tableName = form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME);
                        FormData formData = new FormData();
                        FormRowSet rows = form.getLoadBinder().load(form, primaryKeyValue, formData);
                        if (rows != null && !rows.isEmpty()) {
                            FormRow row = rows.get(0);
                            for (Object fieldId : row.keySet()) {
                                String compareValue = fileName;
                                if (compareValue.endsWith(FileManager.THUMBNAIL_EXT)) {
                                    compareValue = compareValue.replace(FileManager.THUMBNAIL_EXT, "");
                                }
                                
                                String value = row.getProperty(fieldId.toString());
                                
                                if (value.equals(compareValue)
                                        || (value.contains(";") 
                                            && (value.startsWith(compareValue + ";") 
                                                || value.contains(";" + compareValue + ";")
                                                || value.endsWith(";" + compareValue)))
                                        || (value.contains(formDefId+"/"+primaryKeyValue+"/"+compareValue))
                                        || (value.contains(FileUtil.PATH_VARIABLE+compareValue))) {
                                    if (fieldId.toString().startsWith("t__")) {
                                        fieldId = fieldId.toString().substring(3);
                                    }
                                    Element field = FormUtil.findElement(fieldId.toString(), form, formData);
                                    if (field instanceof FileDownloadSecurity) {
                                        FileDownloadSecurity security = (FileDownloadSecurity) field;
                                        isAuthorize = security.isDownloadAllowed(request.getParameterMap());
                                        
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Set<String> customFields = CustomFormDataTableUtil.getColumns(appDef, formDefId);
                    if (customFields != null && !customFields.isEmpty()) {
                        tableName = formDefId;
                        FormRow row = formDataDao.load(formDefId, formDefId, primaryKeyValue);
                        if (row != null) {
                            for (Object fieldId : row.keySet()) {
                                String compareValue = fileName;
                                if (compareValue.endsWith(FileManager.THUMBNAIL_EXT)) {
                                    compareValue = compareValue.replace(FileManager.THUMBNAIL_EXT, "");
                                }
                                
                                String value = row.getProperty(fieldId.toString());
                                
                                if (value.equals(compareValue)
                                        || (value.contains(";") 
                                            && (value.startsWith(compareValue + ";") 
                                                || value.contains(";" + compareValue + ";")
                                                || value.endsWith(";" + compareValue)))
                                        || (value.contains(formDefId+"/"+primaryKeyValue+"/"+compareValue))
                                        || (value.contains(FileUtil.PATH_VARIABLE+compareValue))) {
                                    
                                    if (customFields.contains(fieldId.toString())) {
                                        isAuthorize = !WorkflowUtil.isCurrentUserAnonymous();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e){}
        
        if (!isAuthorize) {
            error404(request, response);
            return;
        }
        
        ServletOutputStream stream = response.getOutputStream();
        String decodedFileName = fileName;
        try {
            decodedFileName = URLDecoder.decode(fileName, "UTF8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        File file = FileUtil.getFile(decodedFileName, tableName, primaryKeyValue);
        if (file.isDirectory() || !file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            response.setDateHeader("Expires", System.currentTimeMillis() + 0);
            response.setHeader("Cache-Control", "no-cache, no-store");
            return;
        }
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        byte[] bbuf = new byte[65536];

        try {
            String contentType = request.getSession().getServletContext().getMimeType(decodedFileName);
            if (contentType != null) {
                response.setContentType(contentType);
            }
            
            // set attachment filename
            String name = URLEncoder.encode(decodedFileName, "UTF8").replaceAll("\\+", "%20");
            if (Boolean.valueOf(attachment).booleanValue()) {
                response.setHeader("Content-Disposition", "attachment; filename=" + name + "; filename*=UTF-8''" + name);
            } else {
                response.setHeader("Content-Disposition", "inline; filename=" + name + "; filename*=UTF-8''" + name);
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
    
    /**
     * Download app resource files.
     * @param request
     * @param response
     * @param appId
     * @param version
     * @param fileName
     * @param attachment
     * @throws IOException
     */
    @RequestMapping("/app/(*:appId)/(~:version)/resources/(*:fileName)")
    public void downloadAppResource(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("fileName") String fileName, @RequestParam(required = false) String attachment) throws IOException {
        boolean isAuthorize = false;
        
        fileName = getFilename(fileName, request.getRequestURL().toString());
        
        AppDefinition appDef;
        String decodedFileName = fileName;
        try {
            decodedFileName = URLDecoder.decode(fileName, "UTF8");
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        
        try {
            if (appId != null && !appId.isEmpty()) {
                if (version == null || version.isEmpty()) {
                    Long appVersion = appService.getPublishedVersion(appId);
                    if (appVersion != null) {
                        version = appVersion.toString();
                    }
                }
                
                appDef = appService.getAppDefinition(appId, version);
                version = appDef.getVersion().toString();
                AppResource appResource = appResourceDao.loadById(decodedFileName, appDef);
                
                Map<String, Object> value = PropertyUtil.getPropertiesValueFromJson(appResource.getPermissionProperties());
                if (value.containsKey("permission") && value.get("permission") instanceof Map && ((Map) value.get("permission")).containsKey("className")) {
                    Map permission = (Map) value.get("permission");
                    if (!permission.get("className").toString().isEmpty()) {
                        Plugin plugin = pluginManager.getPlugin(permission.get("className").toString());
                        if (plugin != null && plugin instanceof Permission) {
                            Permission up = (Permission) plugin;
                            up.setProperties((Map) permission.get("properties"));
                            up.setCurrentUser(workflowUserManager.getCurrentUser());
                            up.setRequestParameters(request.getParameterMap());
                            isAuthorize = up.isAuthorize();
                        }
                    } else {
                        isAuthorize = true;
                    }
                }
            }
        } catch (Exception e){}
        
        if (!isAuthorize) {
            error404(request, response);
            return;
        }
        
        ServletOutputStream stream = response.getOutputStream();
        File file = AppResourceUtil.getFile(appId, version, decodedFileName);
        if (file == null || file.isDirectory() || !file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            response.setDateHeader("Expires", System.currentTimeMillis() + 0);
            response.setHeader("Cache-Control", "no-cache, no-store");
            return;
        }
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        byte[] bbuf = new byte[65536];

        try {
            String contentType = request.getSession().getServletContext().getMimeType(decodedFileName);
            if (contentType != null) {
                response.setContentType(contentType);
            }
            
            // set attachment filename
            String name = URLEncoder.encode(decodedFileName, "UTF8").replaceAll("\\+", "%20");
            if (Boolean.valueOf(attachment).booleanValue()) {
                response.setHeader("Content-Disposition", "attachment; filename=" + name + "; filename*=UTF-8''" + name);
            } else {
                response.setHeader("Content-Disposition", "inline; filename=" + name + "; filename*=UTF-8''" + name);
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
    
    protected String getFilename(String filename, String url) {
        if (!url.endsWith(".")) {
            try {
                url = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException ex) {}
            int index = url.lastIndexOf(filename+".");
            if (index > 0) {
                filename = url.substring(url.lastIndexOf(filename+"."));
            }
        }
        
        return filename;
    }
    
    protected void error404(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setDateHeader("Expires", System.currentTimeMillis() + 0);
        response.setHeader("Cache-Control", "no-cache, no-store");
        response.setContentType("text/html;charset=UTF-8");

        //render userview 404 page if available
        UserviewDefinition userviewDef = null;
        String referer = request.getHeader("referer");
        if (referer != null && !referer.isEmpty() && (referer.contains("/web/userview") || referer.contains("/web/embed/userview"))) {
            referer = referer.substring(referer.indexOf("/userview/") + 10);
            String[] temp = referer.split("/");
            String appId = temp[0];
            String userviewId = temp[1];
            if (appId != null && userviewId != null) {
                AppDefinition appDef = appService.getPublishedAppDefinition(appId);
                if (appDef != null) {
                    userviewDef = userviewDefinitionDao.loadById(userviewId, appDef);
                }
            }   
        }
        if (userviewDef == null) {
            userviewDef = userviewService.getDefaultUserview();
        }
        if (userviewDef != null) {
            AppDefinition appDef = userviewDef.getAppDefinition();
            AppUtil.setCurrentAppDefinition(appDef);
            Map requestParams = new HashMap();
            requestParams.put("menuId", new String[]{"error404"});
            Userview userview = userviewService.createUserview(appDef, userviewDef.getJson(), "error404", false, request.getContextPath(), requestParams, null, false);
            UserviewThemeProcesser processer = new UserviewThemeProcesser(userview, request);
            String view = processer.getView();
            if (view != null && !view.startsWith("redirect:")) {
                response.getWriter().write(processer.getHtml());
            } else {
                response.getWriter().write(UserviewUtil.renderJspAsString("error404.jsp", new HashMap()));
            }
        } else {
            response.getWriter().write(UserviewUtil.renderJspAsString("error404.jsp", new HashMap()));
        }
    }
}
