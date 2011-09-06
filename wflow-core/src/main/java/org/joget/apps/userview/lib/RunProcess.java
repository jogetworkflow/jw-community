package org.joget.apps.userview.lib;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormService;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.workflow.lib.AssignmentCompleteButton;
import org.joget.apps.workflow.lib.AssignmentWithdrawButton;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowProcessResult;
import org.joget.workflow.model.service.WorkflowManager;
import org.json.JSONArray;
import org.springframework.context.ApplicationContext;

public class RunProcess extends UserviewMenu implements PluginWebSupport {

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return "Run Process";
    }

    @Override
    public String getIcon() {
        return "/plugin/org.joget.apps.userview.lib.RunProcess/images/grid_icon.gif";
    }

    @Override
    public String getRenderPage() {
        return null;
    }

    public String getName() {
        return "Run Process Menu";
    }

    public String getVersion() {
        return "3.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getPropertyOptions() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String appId = appDef.getId();
        String appVersion = appDef.getVersion().toString();
        Object[] arguments = new Object[]{appId, appVersion};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/userview/runProcess.json", arguments, true, "message/userview/runProcess");
        return json;
    }

    @Override
    public String getDecoratedMenu() {
        if ("Yes".equals(getPropertyString("showInPopupDialog"))) {
            String menu = "<a onclick=\"menu_" + getPropertyString("id") + "_showDialog();return false;\" class=\"menu-link\"><span>" + getPropertyString("label") + "</span></a>";
            menu += "<script>\n";

            if ("Yes".equals(getPropertyString("showInPopupDialog"))) {
                String url = getUrl() + "?embed=true";

                menu += "var menu_" + getPropertyString("id") + "Dialog = new PopupDialog(\"" + url + "\",\"\");\n";
            }
            menu += "function menu_" + getPropertyString("id") + "_showDialog(){\n";
            if ("true".equals(getRequestParameter("isPreview"))) {
                menu += "alert('\\'Show in popup dialog?\\' feature disabled in Preview Mode.');\n";
            } else {
                menu += "menu_" + getPropertyString("id") + "Dialog.init();\n";
            }
            menu += "}\n</script>";
            return menu;
        }
        return null;
    }

    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("getOptions".equals(action)) {
            String appId = request.getParameter("appId");
            String appVersion = request.getParameter("appVersion");
            try {
                JSONArray jsonArray = new JSONArray();

                ApplicationContext ac = AppUtil.getApplicationContext();
                AppService appService = (AppService) ac.getBean("appService");
                WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");
                AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
                PackageDefinition packageDefinition = appDef.getPackageDefinition();
                Long packageVersion = (packageDefinition != null) ? packageDefinition.getVersion() : new Long(1);
                Collection<WorkflowProcess> processList = workflowManager.getProcessList(appId, packageVersion.toString());

                Map<String, String> empty = new HashMap<String, String>();
                empty.put("value", "");
                empty.put("label", "");
                jsonArray.put(empty);

                for (WorkflowProcess p : processList) {
                    Map<String, String> option = new HashMap<String, String>();
                    option.put("value", p.getIdWithoutVersion());
                    option.put("label", p.getName());
                    jsonArray.put(option);
                }

                jsonArray.write(response.getWriter());
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Get Run Process's options Error!");
            }
        }
    }

    @Override
    public String getJspPage() {
        if ("start".equals(getRequestParameterString("action"))) {
            startProcess();
        } else if ("assignmentSubmit".equals(getRequestParameterString("action"))) {
            assignmentSubmit();
        } else {
            ApplicationContext ac = AppUtil.getApplicationContext();
            AppService appService = (AppService) ac.getBean("appService");
            PackageActivityForm startFormDef = appService.viewStartProcessForm(getRequestParameterString("appId"), getRequestParameterString("appVersion"), getPropertyString("processDefId"), null, "");

            if ("Yes".equals(getPropertyString("runProcessDirectly")) && !(startFormDef != null && startFormDef.getForm() != null)) {
                if ("true".equals(getRequestParameter("isPreview"))) {
                    setProperty("view", "featureDisabled");
                } else {
                    startProcess();
                }
            } else {
                viewProcess();
            }
        }
        return "userview/plugin/runProcess.jsp";
    }

    private void viewProcess() {
        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        FormService formService = (FormService) ac.getBean("formService");

        WorkflowProcess process = appService.getWorkflowProcessForApp(getRequestParameterString("appId"), getRequestParameterString("appVersion"), getPropertyString("processDefId"));
        setProperty("process", process);

        if (isUnauthorized()) {
            // check for start mapped form
            String formUrl = getUrl() + "?action=start";
            if ("Yes".equals(getPropertyString("showInPopupDialog"))) {
                formUrl += "&embed=true";
            }
            FormData formData = new FormData();
            formData = formService.retrieveFormDataFromRequestMap(formData, getRequestParameters());

            String primaryKey = getRequestParameterString("recordId");
            if (primaryKey != null && primaryKey.trim().length() > 0) {
                formData.setPrimaryKeyValue(primaryKey);
            }

            if (getPropertyString("keyName") != null && getPropertyString("keyName").trim().length() > 0 && getKey() != null) {
                formData.addRequestParameterValues(getPropertyString("keyName"), new String[]{getKey()});
            }

            PackageActivityForm startFormDef = appService.viewStartProcessForm(getRequestParameterString("appId"), getRequestParameterString("appVersion"), getPropertyString("processDefId"), formData, formUrl);
            if (startFormDef != null && startFormDef.getForm() != null) {
                Form startForm = startFormDef.getForm();

                // generate form HTML
                String formHtml = formService.retrieveFormHtml(startForm, formData);

                // show form
                setProperty("headerTitle", process.getName());
                setProperty("view", "formView");
                setProperty("formHtml", formHtml);
                setProperty("activityForm", startFormDef);
            } else {
                // empty start page
                setProperty("headerTitle", process.getPackageName() + " (version " + process.getVersion() + ")");

                //append fk & fke parameter to url
                Map requestParam = getRequestParameters();
                for (Object k : requestParam.keySet()) {
                    String key = (String) k;
                    if (key.startsWith(FormService.PREFIX_FOREIGN_KEY) || key.startsWith(FormService.PREFIX_FOREIGN_KEY_EDITABLE)) {
                        try {
                            formUrl += "&" + key + "=" + URLEncoder.encode(requestParam.get(k).toString(), "UTF-8");
                        } catch (Exception e) {
                            LogUtil.info(RunProcess.class.getName(), "Paramter:" + key + "cannot be append to URL");
                        }
                    }
                }

                setProperty("startUrl", formUrl);
                setProperty("view", "processDetail");
            }
        }
    }

    private void startProcess() {
        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        FormService formService = (FormService) ac.getBean("formService");

        WorkflowProcess process = appService.getWorkflowProcessForApp(getRequestParameterString("appId"), getRequestParameterString("appVersion"), getPropertyString("processDefId"));
        setProperty("process", process);

        if (isUnauthorized()) {
            // extract form values from request
            FormData formData = new FormData();

            String recordId = getRequestParameterString("recordId");
            if (recordId != null && recordId.trim().length() == 0) {
                recordId = null;
            }

            formData = formService.retrieveFormDataFromRequestMap(formData, getRequestParameters());

            if (getPropertyString("keyName") != null && getPropertyString("keyName").trim().length() > 0 && getKey() != null) {
                formData.addRequestParameterValues(getPropertyString("keyName"), new String[]{getKey()});
            }

            // get workflow variables
            Map<String, String> variableMap = AppUtil.retrieveVariableDataFromMap(getRequestParameters());
            String formUrl = getUrl() + "?action=start";
            if ("Yes".equals(getPropertyString("showInPopupDialog"))) {
                formUrl += "&embed=true";
            }
            WorkflowProcessResult result = appService.submitFormToStartProcess(getRequestParameterString("appId"), getRequestParameterString("appVersion"), getPropertyString("processDefId"), formData, variableMap, recordId, formUrl);
            PackageActivityForm startFormDef = appService.viewStartProcessForm(getRequestParameterString("appId"), getRequestParameterString("appVersion"), getPropertyString("processDefId"), formData, formUrl);
            if (startFormDef != null && startFormDef.getForm() != null) {
                if (result == null) {
                    // validation error, get form
                    Form startForm = startFormDef.getForm();

                    // generate form HTML
                    String formHtml = formService.retrieveFormErrorHtml(startForm, formData);

                    // show form
                    setProperty("headerTitle", process.getName());
                    setProperty("view", "formView");
                    setProperty("formHtml", formHtml);
                    setProperty("activityForm", startFormDef);
                }
            } else {
                // start process - TODO: handle process linking
                WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");
                result = workflowManager.processStart(process.getId(), null, variableMap, null, recordId, false);
            }

            // set result
            if (result != null) {
                // Show next activity if available
                Collection<WorkflowActivity> activities = result.getActivities();
                if (activities != null && !activities.isEmpty()) {
                    WorkflowActivity nextActivity = activities.iterator().next();
                    assignmentView(nextActivity.getId());
                } else {
                    processStarted();
                }
            }
        }
    }

    private void assignmentView(String activityId) {
        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        FormService formService = (FormService) ac.getBean("formService");
        WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");

        if (isAssignmentExist(activityId)) {
            try {
                WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
                // set process instance ID as primary key
                FormData formData = new FormData();
                String processId = assignment.getProcessId();
                String primaryKey = appService.getOriginProcessId(processId);
                ;
                formData.setPrimaryKeyValue(primaryKey);

                formData = formService.retrieveFormDataFromRequestMap(formData, getRequestParameters());

                if (getPropertyString("keyName") != null && getPropertyString("keyName").trim().length() > 0 && getKey() != null) {
                    formData.addRequestParameterValues(getPropertyString("keyName"), new String[]{getKey()});
                }

                // get form
                String formUrl = getUrl() + "?action=assignmentSubmit&activityId=" + activityId;
                if ("Yes".equals(getPropertyString("showInPopupDialog"))) {
                    formUrl += "&embed=true";
                }
                PackageActivityForm activityForm = appService.viewAssignmentForm(getRequestParameterString("appId"), getRequestParameterString("appVersion"), activityId, formData, formUrl);
                Form form = activityForm.getForm();

                // generate form HTML
                String formHtml = formService.retrieveFormHtml(form, formData);

                // show form
                setProperty("headerTitle", assignment.getProcessName() + " - " + assignment.getActivityName());
                setProperty("view", "formView");
                setProperty("formHtml", formHtml);
                setProperty("activityForm", activityForm);
            } catch (Exception e) {
                Logger.getLogger(RunProcess.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    private void assignmentSubmit() {
        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        FormService formService = (FormService) ac.getBean("formService");
        WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");
        String activityId = getRequestParameterString("activityId");
        if (isAssignmentExist(activityId)) {
            try {
                WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
                // set process instance ID as primary key
                FormData formData = new FormData();
                formData = formService.retrieveFormDataFromRequestMap(formData, getRequestParameters());
                String processId = assignment.getProcessId();
                String primaryKey = processId;
                formData.setPrimaryKeyValue(primaryKey);

                // get form
                String formUrl = getUrl() + "?action=assignmentSubmit&activityId=" + activityId;
                if ("Yes".equals(getPropertyString("showInPopupDialog"))) {
                    formUrl += "&embed=true";
                }
                PackageActivityForm activityForm = appService.viewAssignmentForm(getRequestParameterString("appId"), getRequestParameterString("appVersion"), activityId, formData, formUrl);
                Form form = activityForm.getForm();

                // TODO: determine foreign key?
                String foreignKeyValue = null;
                formData.setForeignKeyValue(foreignKeyValue);

                // submit form
                FormData formResult = formService.executeFormActions(form, formData);

                // check for validation errors
                if (formResult.getFormResult(AssignmentWithdrawButton.DEFAULT_ID) != null) {
                    // withdraw assignment
                    workflowManager.assignmentWithdraw(activityId);
                } else if (formResult.getFormResult(AssignmentCompleteButton.DEFAULT_ID) != null) {
                    // complete assignment
                    Map<String, String> variableMap = AppUtil.retrieveVariableDataFromMap(getRequestParameters());
                    formResult = appService.completeAssignmentForm(getRequestParameterString("appId"), getRequestParameterString("appVersion"), activityId, formData, variableMap);

                    Map<String, String> errors = formResult.getFormErrors();
                    if (errors.isEmpty() && activityForm.isAutoContinue()) {
                        // redirect to next activity if available
                        WorkflowAssignment nextActivity = workflowManager.getAssignmentByProcess(processId);
                        if (nextActivity != null) {
                            assignmentView(nextActivity.getActivityId());
                            return;
                        }
                    }
                }

                String html = null;

                // check for validation errors
                Map<String, String> errors = formResult.getFormErrors();
                int errorCount = 0;
                if (errors == null || errors.isEmpty()) {
                    // render normal template
                    html = formService.generateElementHtml(form, formResult);
                } else {
                    // render error template
                    html = formService.generateElementErrorHtml(form, formResult);
                    errorCount = errors.size();
                }

                // show form
                setProperty("headerTitle", assignment.getProcessName() + " - " + assignment.getActivityName());
                if (errorCount == 0) {
                    if (getPropertyString("redirectUrlAfterComplete") != null && !getPropertyString("redirectUrlAfterComplete").isEmpty()) {
                        setProperty("view", "redirect");
                        setProperty("redirectURL", getPropertyString("redirectUrlAfterComplete"));
                    } else {
                        setProperty("view", "assignmentUpdated");
                    }
                } else {
                    setProperty("view", "formView");
                }
                setProperty("errorCount", errorCount);
                setProperty("submitted", Boolean.TRUE);
                setProperty("formHtml", html);
                return;

            } catch (Exception e) {
                Logger.getLogger(RunProcess.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    private boolean isUnauthorized() {
        // check for permission
        ApplicationContext ac = AppUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");
        if (workflowManager.isUserInWhiteList(getPropertyString("processDefId"))) {
            return true;
        } else {
            setProperty("headerTitle", "Unauthorized");
            setProperty("view", "unauthorized");
            return false;
        }
    }

    private boolean isAssignmentExist(String activityId) {
        // check for assignment
        ApplicationContext ac = AppUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");
        WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
        if (assignment != null) {
            return true;
        } else {
            setProperty("headerTitle", "Assignment Unavailable");
            setProperty("view", "assignmentUnavailable");
            return false;
        }
    }

    private void processStarted() {
        if (getPropertyString("redirectUrlAfterComplete") != null && !getPropertyString("redirectUrlAfterComplete").isEmpty()) {
            setProperty("view", "redirect");
            setProperty("redirectURL", getPropertyString("redirectUrlAfterComplete"));
        } else {
            setProperty("headerTitle", "Process Started");
            setProperty("view", "processStarted");
        }
    }

    @Override
    public boolean isHomePageSupported() {
        if (!"Yes".equals(getPropertyString("showInPopupDialog"))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getCategory() {
        return UserviewBuilderPalette.CATEGORY_GENERAL;
    }
}
