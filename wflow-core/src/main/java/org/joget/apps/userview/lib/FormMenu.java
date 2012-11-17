package org.joget.apps.userview.lib;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.workflow.lib.AssignmentCompleteButton;
import org.joget.apps.workflow.lib.AssignmentWithdrawButton;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;

/**
 * Represents a menu item that displays a data form and handles form submission.
 */
public class FormMenu extends UserviewMenu {

    @Override
    public String getIcon() {
        return "/plugin/org.joget.apps.userview.lib.FormMenu/images/subForm_icon.gif";
    }

    @Override
    public String getName() {
        return "Form Menu";
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    @Override
    public String getDescription() {
        return "Userview Form Menu Item";
    }

    @Override
    public String getLabel() {
        return "Form";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String appId = appDef.getId();
        String appVersion = appDef.getVersion().toString();
        Object[] arguments = new Object[]{appId, appVersion};
        String json = AppUtil.readPluginResource(getClass().getName(), "/properties/userview/formMenu.json", arguments, true, "message/userview/formMenu");
        return json;
    }

    @Override
    public String getRenderPage() {
        return null;
    }

    @Override
    public boolean isHomePageSupported() {
        return true;
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

    @Override
    public String getJspPage() {
        if ("submit".equals(getRequestParameterString("_action"))) {
            // submit form
            submitForm();
        } else {
            displayForm();

        }
        return "userview/plugin/form.jsp";
    }

    @Override
    public String getCategory() {
        return UserviewBuilderPalette.CATEGORY_GENERAL;
    }

    /**
     * Display either an assignment form or data form, depending on availability.
     * If an "activityId" parameter is available, a matching assignment form is loaded.
     * Otherwise, based on the "id" parameter, a matching assignment for the processID is loaded.
     * If no assignment is available, a matching data form is loaded with a matching primary key.
     */
    protected void displayForm() {

        String id = getRequestParameterString(FormUtil.PROPERTY_ID);
        String processId = getRequestParameterString("processId");
        String activityId = getRequestParameterString("activityId");

        ApplicationContext ac = AppUtil.getApplicationContext();
        FormService formService = (FormService) ac.getBean("formService");
        WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");

        Form form = null;
        WorkflowAssignment assignment = null;
        FormData formData = new FormData();

        // get assignment by activity ID if available
        if (activityId != null && !activityId.trim().isEmpty()) {
            assignment = workflowManager.getAssignment(activityId);
        }
        if (assignment == null) {
            // assignment not available, attempt to load assignment by process ID
            assignment = workflowManager.getAssignmentByProcess(processId);
        }
        if (assignment != null) {
            // load assignment form
            PackageActivityForm activityForm = retrieveAssignmentForm(formData, assignment);
            form = activityForm.getForm();
            setProperty("activityForm", activityForm);
        } else {
            // load data form
            form = retrieveDataForm(formData, id);
        }

        if (form != null) {
            // generate form HTML
            String formHtml = formService.retrieveFormHtml(form, formData);
            String formJson = formService.generateElementJson(form);
            setProperty("view", "formView");
            setProperty("formHtml", formHtml);
            setProperty("formJson", formJson);
            if (assignment != null) {
                setProperty("headerTitle", assignment.getProcessName() + " - " + assignment.getActivityName());
            }
        } else {
            setProperty("headerTitle", "Assignment or Form Unavailable");
            setProperty("view", "assignmentFormUnavailable");
            setProperty("formHtml", "Assignment or Form Unavailable");
        }

    }

    /**
     * Handles assignment or data form submission.
     */
    protected void submitForm() {

        String id = getRequestParameterString(FormUtil.PROPERTY_ID);
        String processId = getRequestParameterString("processId");
        String activityId = getRequestParameterString("activityId");

        ApplicationContext ac = AppUtil.getApplicationContext();
        FormService formService = (FormService) ac.getBean("formService");
        WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");

        Form form = null;
        WorkflowAssignment assignment = null;
        FormData formData = new FormData();

        // get assignment by activity ID if available
        if (activityId != null && !activityId.trim().isEmpty()) {
            assignment = workflowManager.getAssignment(activityId);
        }
        if (assignment == null) {
            // assignment not available, attempt to load assignment by process ID
            assignment = workflowManager.getAssignmentByProcess(processId);
        }
        if (assignment != null) {
            // load assignment form
            PackageActivityForm activityForm = retrieveAssignmentForm(formData, assignment);

            // submit assignment form
            form = submitAssignmentForm(formData, assignment, activityForm);
        } else {
            // load data form
            form = retrieveDataForm(formData, id);

            // submit data form
            form = submitDataForm(formData, form);
        }

        // determine redirect URL
        String redirectUrl = getPropertyString("redirectUrlAfterComplete");

        if (redirectUrl != null && redirectUrl.trim().length() > 0 && getPropertyString("fieldPassover") != null && getPropertyString("fieldPassover").trim().length() > 0) {
            String passoverFieldName = getPropertyString("fieldPassover");
            Element passoverElement = FormUtil.findElement(passoverFieldName, form, formData);
            
            String passoverValue = "";
            
            if (passoverElement != null) {
                passoverValue = FormUtil.getElementPropertyValue(passoverElement, formData);
            } else if (FormUtil.PROPERTY_ID.equals(passoverFieldName)) {
                passoverValue = formData.getPrimaryKeyValue();
            }
            
            if ("append".equals(getPropertyString("fieldPassoverMethod"))) {
                if (!redirectUrl.endsWith("/")) {
                    redirectUrl += "/";
                }
                redirectUrl += passoverValue;
            } else {
                if (redirectUrl.contains("?")) {
                    redirectUrl += "&";
                } else {
                    redirectUrl += "?";
                }
                redirectUrl += getPropertyString("paramName") + "=" + passoverValue;
            }
        }

        if (form != null) {
            // generate form HTML
            String formHtml = null;

            // check for validation errors
            Map<String, String> errors = formData.getFormErrors();
            int errorCount = 0;
            if (!formData.getStay() && errors == null || errors.isEmpty()) {
                // render normal template
                formHtml = formService.generateElementHtml(form, formData);
                setAlertMessage(getPropertyString("messageShowAfterComplete"));
                boolean redirectToParent = "Yes".equals(getPropertyString("showInPopupDialog"));
                setRedirectUrl(redirectUrl, redirectToParent);
            } else {
                // render error template
                formHtml = formService.generateElementErrorHtml(form, formData);
                errorCount = errors.size();
            }
            
            if (formData.getStay()) {
                setAlertMessage("");
                setRedirectUrl("");
            }

            // show form
            String formJson = formService.generateElementJson(form);
            setProperty("view", "formView");
            setProperty("errorCount", errorCount);
            setProperty("stay", formData.getStay());
            setProperty("submitted", Boolean.TRUE);
            setProperty("formHtml", formHtml);
            setProperty("formJson", formJson);
            setProperty("redirectUrlAfterComplete", redirectUrl);
            if (assignment != null) {
                setProperty("headerTitle", assignment.getProcessName() + " - " + assignment.getActivityName());
            }
        } else if (assignment != null) {
            setProperty("headerTitle", assignment.getProcessName() + " - " + assignment.getActivityName());
            setProperty("errorCount", 0);
            setProperty("submitted", Boolean.TRUE);
            setAlertMessage(getPropertyString("messageShowAfterComplete"));
            if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
                setProperty("redirectUrlAfterComplete", redirectUrl);
                boolean redirectToParent = "Yes".equals(getPropertyString("showInPopupDialog"));
                setRedirectUrl(redirectUrl, redirectToParent);
            } else {
                setProperty("view", "assignmentUpdated");
            }
        } else {
            setProperty("headerTitle", "Assignment or Form Unavailable");
            setProperty("view", "assignmentFormUnavailable");
            setProperty("formHtml", "Assignment or Form Unavailable");
        }

    }

    /**
     * Retrieves form mapped to an assignment
     * @param formData
     * @param assignment
     * @return
     */
    protected PackageActivityForm retrieveAssignmentForm(FormData formData, WorkflowAssignment assignment) {
        String activityId = assignment.getActivityId();
        String formUrl = getUrl() + "?_action=submit&activityId=" + activityId;
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        FormService formService = (FormService) ac.getBean("formService");
        
        formData = formService.retrieveFormDataFromRequestMap(formData, getRequestParameters());
        
        PackageActivityForm activityForm = appService.viewAssignmentForm(appDef, assignment, formData, formUrl);
        return activityForm;
    }

    /**
     * Retrieves the form mapped to this menu item.
     * @param formData
     * @return
     */
    protected Form retrieveDataForm(FormData formData, String primaryKeyValue) {
        Form form = null;
        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        FormService formService = (FormService) ac.getBean("formService");
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String formId = getPropertyString("formId");

        formData = formService.retrieveFormDataFromRequestMap(formData, getRequestParameters());

        //if primary key is null, look up for primary key using userview key
        if (primaryKeyValue == null && getPropertyString("keyName") != null && (getPropertyString("loadDataWithKey") != null && "Yes".equalsIgnoreCase(getPropertyString("loadDataWithKey"))) && getPropertyString("keyName").trim().length() > 0 && getKey() != null) {
            if (FormUtil.PROPERTY_ID.equals(getPropertyString("keyName"))) {
                primaryKeyValue = getKey();
            } else {
                primaryKeyValue = appService.getPrimaryKeyWithForeignKey(appDef.getId(), appDef.getVersion().toString(), formId, getPropertyString("keyName"), getKey());
            }
        }
        // set primary key
        formData.setPrimaryKeyValue(primaryKeyValue);

        if (getPropertyString("keyName") != null && getPropertyString("keyName").trim().length() > 0 && getKey() != null) {
            formData.addRequestParameterValues(getPropertyString("keyName"), new String[]{getKey()});
        }

        // retrieve form
        String formUrl = getUrl() + "?_action=submit";
        if (primaryKeyValue != null) {
            try {
                formUrl += "&" + FormUtil.PROPERTY_ID + "=" + URLEncoder.encode(primaryKeyValue, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                // ignore
            }
        }

        String submitLabel = "Submit";

        if (getPropertyString("submitButtonLabel") != null && getPropertyString("submitButtonLabel").trim().length() > 0) {
            submitLabel = getPropertyString("submitButtonLabel");
        }

        String cancelUrl = getPropertyString("redirectUrlOnCancel");
        String cancelLabel = null;
        if (cancelUrl != null && !cancelUrl.isEmpty()) {
            if (getPropertyString("cancelButtonLabel") != null && getPropertyString("cancelButtonLabel").trim().length() > 0) {
                cancelLabel = getPropertyString("cancelButtonLabel");
            } else {
                cancelLabel = "Cancel";
            }
        }
        
        Boolean readonlyLabel = "true".equalsIgnoreCase(getPropertyString("readonlyLabel"));

        form = appService.viewDataForm(appDef.getId(), appDef.getVersion().toString(), formId, null, submitLabel, cancelLabel, getPropertyString("redirectTargetOnCancel"), formData, formUrl, cancelUrl);
        if (form != null) {

            // make primary key read-only
            Element el = FormUtil.findElement(FormUtil.PROPERTY_ID, form, formData);
            if (el != null) {
                String idValue = FormUtil.getElementPropertyValue(el, formData);
                if (idValue != null && !idValue.trim().isEmpty() && !"".equals(formData.getRequestParameter(FormUtil.FORM_META_ORIGINAL_ID))) {
                    FormUtil.setReadOnlyProperty(el, true, readonlyLabel);
                }
            }

            if (getPropertyString("keyName") != null && getPropertyString("keyName").trim().length() > 0 && getKey() != null) {
                el = FormUtil.findElement(getPropertyString("keyName"), form, formData);
                if (el != null) {
                    FormUtil.setReadOnlyProperty(el, true, readonlyLabel);
                }
            }
        }

        // set form to read-only if required
        Boolean readonly = "Yes".equalsIgnoreCase(getPropertyString("readonly"));
        if (readonly || readonlyLabel) {
            FormUtil.setReadOnlyProperty(form, readonly, readonlyLabel);
        }
        return form;
    }

    /**
     * Handles assignment form submission
     * @param formData
     * @param assignment
     * @param activityForm
     * @return
     */
    protected Form submitAssignmentForm(FormData formData, WorkflowAssignment assignment, PackageActivityForm activityForm) {
        ApplicationContext ac = AppUtil.getApplicationContext();
        AppService appService = (AppService) ac.getBean("appService");
        FormService formService = (FormService) ac.getBean("formService");
        WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");
        String activityId = assignment.getActivityId();
        String processId = assignment.getProcessId();
        
        // get form
        Form currentForm = activityForm.getForm();

        // submit form
        formData = formService.executeFormActions(currentForm, formData);

        if (formData.getFormResult(AssignmentWithdrawButton.DEFAULT_ID) != null) {
            // withdraw assignment
            workflowManager.assignmentWithdraw(activityId);
        } else if (formData.getFormResult(AssignmentCompleteButton.DEFAULT_ID) != null) {
            // complete assignment
            Map<String, String> variableMap = AppUtil.retrieveVariableDataFromMap(getRequestParameters());
            formData = appService.completeAssignmentForm(currentForm, assignment, formData, variableMap);

            Map<String, String> errors = formData.getFormErrors();
            
            setProperty("submitted", Boolean.TRUE);
            if (!formData.getStay() && errors.isEmpty() && activityForm.isAutoContinue()) {
                setProperty("redirectUrlAfterComplete", getPropertyString("redirectUrlAfterComplete"));
                setRedirectUrl(getPropertyString("redirectUrlAfterComplete"));
                // redirect to next activity if available
                WorkflowAssignment nextActivity = workflowManager.getAssignmentByProcess(processId);
                if (nextActivity != null) {
                    String redirectUrl = getUrl() + "?activityId=" + nextActivity.getActivityId();
                    setProperty("messageShowAfterComplete", "");
                    setProperty("redirectUrlAfterComplete", redirectUrl);
                    setAlertMessage("");
                    setRedirectUrl(redirectUrl);
                }
            }
        }
        return currentForm;
    }

    /**
     * Handles data form submission.
     */
    protected Form submitDataForm(FormData formData, Form form) {
        ApplicationContext ac = AppUtil.getApplicationContext();
        FormService formService = (FormService) ac.getBean("formService");
        formData = formService.retrieveFormDataFromRequestMap(formData, getRequestParameters());
        formData = formService.executeFormActions(form, formData);

        setProperty("submitted", Boolean.TRUE);
        setProperty("redirectUrlAfterComplete", getPropertyString("redirectUrlAfterComplete"));

        return form;
    }
}
