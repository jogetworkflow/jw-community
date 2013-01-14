package org.joget.apps.app.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections.map.ListOrderedMap;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.dao.EnvironmentVariableDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.dao.MessageDao;
import org.joget.apps.app.dao.PackageDefinitionDao;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.EnvironmentVariable;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.Message;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.lib.LinkButton;
import org.joget.apps.form.lib.SaveAsDraftButton;
import org.joget.apps.form.lib.SubmitButton;
import org.joget.apps.form.lib.TextField;
import org.joget.apps.form.lib.WorkflowFormBinder;
import org.joget.apps.form.model.Column;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormAction;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.model.FormStoreBinder;
import org.joget.apps.form.model.Section;
import org.joget.apps.form.service.FileUtil;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.service.UserviewService;
import org.joget.apps.workflow.lib.AssignmentCompleteButton;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowPackage;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowProcessLink;
import org.joget.workflow.model.WorkflowProcessResult;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.joget.workflow.util.XpdlImageUtil;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service("appService")
public class AppServiceImpl implements AppService {

    @Autowired
    FormService formService;
    @Autowired
    WorkflowManager workflowManager;
    @Autowired
    AppDefinitionDao appDefinitionDao;
    @Autowired
    DatalistDefinitionDao datalistDefinitionDao;
    @Autowired
    UserviewDefinitionDao userviewDefinitionDao;
    @Autowired
    MessageDao messageDao;
    @Autowired
    EnvironmentVariableDao environmentVariableDao;
    @Autowired
    PluginDefaultPropertiesDao pluginDefaultPropertiesDao;
    @Autowired
    PackageDefinitionDao packageDefinitionDao;
    @Autowired
    PluginManager pluginManager;
    @Autowired
    FormDataDao formDataDao;
    @Autowired
    UserviewService userviewService;
    //----- Workflow use cases ------

    /**
     * Retrieves the workflow process definition for a specific app version.
     * @param appId
     * @param version
     * @param processDefId
     * @return
     */
    @Override
    public WorkflowProcess getWorkflowProcessForApp(String appId, String version, String processDefId) {
        AppDefinition appDef = getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        String processDefIdWithVersion = AppUtil.getProcessDefIdWithVersion(packageDef.getId(), packageDef.getVersion().toString(), processDefId);
        WorkflowProcess process = workflowManager.getProcess(processDefIdWithVersion);
        return process;
    }

//    /**
//     * Retrieves the app definition for a specific workflow process.
//     * @param activityId
//     * @return
//     */
//    public AppDefinition getAppDefinitionForWorkflowProcess(String processId) {
//        AppDefinition appDef = null;
//        String processDefId = workflowManager.getProcessDefIdByInstanceId(processId);
//        String packageId = WorkflowUtil.getProcessDefPackageId(processDefId);
//        Long packageVersion = new Long(WorkflowUtil.getProcessDefVersion(processDefId));
//        PackageDefinition packageDef = packageDefinitionDao.loadPackageDefinitionByProcess(packageId, packageVersion, processDefId);
//        appDef = packageDef.getAppDefinition();
//        return appDef;
//    }
//
    /**
     * Retrieves the app definition for a specific workflow activity assignment.
     * @param activityId
     * @return
     */
    @Override
    public AppDefinition getAppDefinitionForWorkflowActivity(String activityId) {
        AppDefinition appDef = null;

        WorkflowActivity activity = workflowManager.getActivityById(activityId);
        if (activity != null) {
            String processDefId = activity.getProcessDefId();
            WorkflowProcess process = workflowManager.getProcess(processDefId);
            if (process != null) {
                String packageId = process.getPackageId();
                Long packageVersion = Long.parseLong(process.getVersion());
                PackageDefinition packageDef = packageDefinitionDao.loadPackageDefinition(packageId, packageVersion);
                if (packageDef != null) {
                    appDef = packageDef.getAppDefinition();
                }
            }
        }
        // set into thread
        AppUtil.setCurrentAppDefinition(appDef);
        return appDef;
    }
    
    /**
     * Retrieves the app definition for a specific workflow process.
     * @param processId
     * @return
     */
    @Override
    public AppDefinition getAppDefinitionForWorkflowProcess(String processId) {
        AppDefinition appDef = null;

        WorkflowProcess process = workflowManager.getRunningProcessById(processId);
        if (process != null) {
            String packageId = process.getPackageId();
            Long packageVersion = Long.parseLong(process.getVersion());
            PackageDefinition packageDef = packageDefinitionDao.loadPackageDefinition(packageId, packageVersion);
            if (packageDef != null) {
                appDef = packageDef.getAppDefinition();
            }
        }
        // set into thread
        AppUtil.setCurrentAppDefinition(appDef);
        return appDef;
    }
    
    /**
     * Retrieves the app definition for a specific workflow process definition id.
     * @param processDefId
     * @return
     */
    @Override
    public AppDefinition getAppDefinitionWithProcessDefId(String processDefId) {
        AppDefinition appDef = null;

        processDefId = workflowManager.getConvertedLatestProcessDefId(processDefId);
        String[] params = processDefId.split("#");
        String packageId = params[0];
        Long packageVersion = Long.parseLong(params[1]);
        
        PackageDefinition packageDef = packageDefinitionDao.loadPackageDefinition(packageId, packageVersion);
        if (packageDef != null) {
            appDef = packageDef.getAppDefinition();
        }
        
        // set into thread
        AppUtil.setCurrentAppDefinition(appDef);
        return appDef;
    }

    /**
     * Retrieve a form for a specific activity instance
     * @param appId
     * @param version
     * @param activityId
     * @param formData
     * @param formUrl
     * @return
     */
    @Override
    public PackageActivityForm viewAssignmentForm(String appId, String version, String activityId, FormData formData, String formUrl) {
        AppDefinition appDef = getAppDefinition(appId, version);
        WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
        return viewAssignmentForm(appDef, assignment, formData, formUrl);
    }
    
    /**
     * Retrieve a form for a specific activity instance
     * @param appId
     * @param version
     * @param activityId
     * @param formData
     * @param formUrl
     * @return
     */
    @Override
    public PackageActivityForm viewAssignmentForm(AppDefinition appDef, WorkflowAssignment assignment, FormData formData, String formUrl) {
        String processId = assignment.getProcessId();
        String processDefId = assignment.getProcessDefId();
        String activityDefId = assignment.getActivityDefId();
        PackageActivityForm activityForm = retrieveMappedForm(appDef.getAppId(), appDef.getVersion().toString(), processDefId, activityDefId);

        // get origin process id
        String originProcessId = getOriginProcessId(processId);

        // get mapped form
        if (formData == null) {
            formData = new FormData();
        }
        formData.setProcessId(processId);
        formData.setPrimaryKeyValue(originProcessId);
        
        Form form = retrieveForm(appDef, activityForm, formData, assignment);
        if (form == null) {
            form = createDefaultForm(processId, formData);
        }

        // set action URL
        form.setProperty("url", formUrl);

        // decorate form with actions
        Collection<FormAction> formActionList = new ArrayList<FormAction>();
        if (activityForm != null && activityForm.getFormId() != null && !activityForm.getFormId().isEmpty()) {
            Element saveButton = (Element) pluginManager.getPlugin(SaveAsDraftButton.class.getName());
            saveButton.setProperty(FormUtil.PROPERTY_ID, "saveAsDraft");
            saveButton.setProperty("label", ResourceBundleUtil.getMessage("form.button.saveAsDraft"));
            formActionList.add((FormAction) saveButton);
        }
        Element completeButton = (Element) pluginManager.getPlugin(AssignmentCompleteButton.class.getName());
        completeButton.setProperty(FormUtil.PROPERTY_ID, AssignmentCompleteButton.DEFAULT_ID);
        completeButton.setProperty("label", ResourceBundleUtil.getMessage("form.button.complete"));
        formActionList.add((FormAction) completeButton);
        FormAction[] formActions = formActionList.toArray(new FormAction[0]);
        form = decorateFormActions(form, formActions);

        // set to definition
        if (activityForm == null) {
            activityForm = new PackageActivityForm();
        }
        activityForm.setForm(form);

        if (PackageActivityForm.ACTIVITY_FORM_TYPE_EXTERNAL.equals(activityForm.getType())) {
            // set external URL
            String externalUrl = AppUtil.processHashVariable(activityForm.getFormUrl(), assignment, null, null);
            if (externalUrl.indexOf("?") >= 0) {
                if (!externalUrl.endsWith("?") && !externalUrl.endsWith("&")) {
                    externalUrl += "&";
                }
            } else {
                externalUrl += "?";
            }
            activityForm.setFormUrl(externalUrl);
        }        
        
        return activityForm;
    }
    
    @Override
    public FormData completeAssignmentForm(Form form, WorkflowAssignment assignment, FormData formData, Map<String, String> workflowVariableMap) {
        if (formData == null) {
            formData = new FormData();
        }

        // get assignment
        String activityId = assignment.getActivityId();
        String processId = assignment.getProcessId();
        String processDefId = assignment.getProcessDefId();
        String activityDefId = assignment.getActivityDefId();

        // accept assignment if necessary
        if (!assignment.isAccepted()) {
            workflowManager.assignmentAccept(activityId);
        }

        // get and submit mapped form
        if (form != null) {
            formData = submitForm(form, formData, false);
        }

        Map<String, String> errors = formData.getFormErrors();
        if (!formData.getStay() && errors == null || errors.isEmpty()) {
            // complete assignment
            workflowManager.assignmentComplete(activityId, workflowVariableMap);
        }
        return formData;
    }

    /**
     * Process a submitted form to complete an assignment
     * @param appId
     * @param version
     * @param activityId
     * @param formData
     * @param workflowVariableMap
     * @return
     */
    @Override
    public FormData completeAssignmentForm(String appId, String version, String activityId, FormData formData, Map<String, String> workflowVariableMap) {
        if (formData == null) {
            formData = new FormData();
        }

        // get assignment
        WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
        String processId = assignment.getProcessId();
        String processDefId = assignment.getProcessDefId();
        String activityDefId = assignment.getActivityDefId();

        // accept assignment if necessary
        if (!assignment.isAccepted()) {
            workflowManager.assignmentAccept(activityId);
        }

        // get and submit mapped form
        PackageActivityForm paf = retrieveMappedForm(appId, version, processDefId, activityDefId);
        if (paf != null) {
            String formDefId = paf.getFormId();
            if (formDefId != null && !formDefId.isEmpty()) {
                String originProcessId = getOriginProcessId(processId);
                formData.setPrimaryKeyValue(originProcessId);
                formData.setProcessId(processId);
                
                AppDefinition appDef = getAppDefinition(appId, version);
                Form form = retrieveForm(appDef, paf, formData, assignment);
                formData = submitForm(form, formData, false);
            }
        }

        Map<String, String> errors = formData.getFormErrors();
        if (!formData.getStay() && errors == null || errors.isEmpty()) {
            // complete assignment
            workflowManager.assignmentComplete(activityId, workflowVariableMap);
        }
        return formData;
    }

    /**
     * Retrieve form mapped to start a process
     * @param appId
     * @param version
     * @param processDefId
     * @param formData
     * @param formUrl
     * @return
     */
    @Override
    public PackageActivityForm viewStartProcessForm(String appId, String version, String processDefId, FormData formData, String formUrl) {
        AppDefinition appDef = getAppDefinition(appId, version);
        PackageActivityForm startFormDef = retrieveMappedForm(appId, version, processDefId, WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
        if (startFormDef != null) {
            if (startFormDef.getFormId() != null && !startFormDef.getFormId().isEmpty()) {
                // get mapped form
                Form startForm = retrieveForm(appDef, startFormDef, formData, null);
                if (startForm != null) {
                    // set action URL
                    startForm.setProperty("url", formUrl);

                    // decorate form with actions
                    Element submitButton = (Element) pluginManager.getPlugin(AssignmentCompleteButton.class.getName());
                    submitButton.setProperty(FormUtil.PROPERTY_ID, AssignmentCompleteButton.DEFAULT_ID);
                    submitButton.setProperty("label",  ResourceBundleUtil.getMessage("form.button.submit"));
                    FormAction[] formActions = {(FormAction) submitButton};
                    startForm = decorateFormActions(startForm, formActions);

                    // set to definition
                    startFormDef.setForm(startForm);
                }
            }
            if (PackageActivityForm.ACTIVITY_FORM_TYPE_EXTERNAL.equals(startFormDef.getType())) {
                // set external URL
                String externalUrl = AppUtil.processHashVariable(startFormDef.getFormUrl(), null, null, null);
                if (externalUrl.indexOf("?") >= 0) {
                    if (!externalUrl.endsWith("?") && !externalUrl.endsWith("&")) {
                        externalUrl += "&";
                    }
                } else {
                    externalUrl += "?";
                }
                startFormDef.setFormUrl(externalUrl);
            }        
        }
        return startFormDef;
    }

    /**
     * Start a process through a form submission
     * @param appId
     * @param version
     * @param processDefId
     * @param formData
     * @param workflowVariableMap
     * @param originProcessId
     * @param formUrl
     * @return
     */
    @Override
    public WorkflowProcessResult submitFormToStartProcess(String appId, String version, String processDefId, FormData formData, Map<String, String> workflowVariableMap, String originProcessId, String formUrl) {
        WorkflowProcessResult result = null;
        if (formData == null) {
            formData = new FormData();
        }

        AppDefinition appDef = getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        String processDefIdWithVersion = AppUtil.getProcessDefIdWithVersion(packageDef.getId(), packageDef.getVersion().toString(), processDefId);

        // get form
        PackageActivityForm startFormDef = viewStartProcessForm(appId, appDef.getVersion().toString(), processDefId, formData, formUrl);
        if (startFormDef != null && startFormDef.getForm() != null) {
            Form startForm = startFormDef.getForm();

            FormData formResult = formService.executeFormActions(startForm, formData);
            if (formResult.getFormResult(AssignmentCompleteButton.DEFAULT_ID) != null) {
                // validate form
                formData = FormUtil.executeElementFormatDataForValidation(startForm, formData);
                formResult = formService.validateFormData(startForm, formData);

                Map<String, String> errors = formResult.getFormErrors();
                if (!formResult.getStay() && (errors == null || errors.isEmpty())) {
                    if (originProcessId == null && formResult.getRequestParameter(FormUtil.FORM_META_ORIGINAL_ID) != null && !formResult.getRequestParameter(FormUtil.FORM_META_ORIGINAL_ID).isEmpty()) {
                        originProcessId = formResult.getRequestParameter(FormUtil.FORM_META_ORIGINAL_ID);
                    }

                    // start process
                    result = workflowManager.processStart(processDefIdWithVersion, null, workflowVariableMap, null, originProcessId, true);
                    String processId = result.getProcess().getInstanceId();
                    String originId = (originProcessId != null && originProcessId.trim().length() > 0) ? originProcessId : processId;
                    originId = getOriginProcessId(originId);

                    // set primary key
                    formResult.setPrimaryKeyValue(originId);
                    formResult.setProcessId(processId);

                    // submit form
                    formResult = formService.submitForm(startForm, formData, true);
                    result = workflowManager.processStartWithInstanceId(processDefIdWithVersion, processId, workflowVariableMap);

                    // set next activity if configured
                    boolean autoContinue = (startFormDef != null) && startFormDef.isAutoContinue();
                    if (!autoContinue) {
                        // clear next activities
                        result.setActivities(new ArrayList<WorkflowActivity>());
                    }
                }
            }
        }
        return result;
    }

    /**
     * Retrieves ID of the form data row that is created or updated upon form submission.
     * @param formResult
     * @return
     */
    protected String retrieveFormRowId(FormData formResult) {
        String formRowId = null;
        Collection<FormStoreBinder> binders = formResult.getStoreBinders();
        for (FormStoreBinder binder : binders) {
            if (binder instanceof FormStoreBinder) {
                FormRowSet rowSet = formResult.getStoreBinderData(binder);
                if (!rowSet.isEmpty()) {
                    FormRow row = rowSet.get(0);
                    formRowId = row.getProperty("FORM_ID"); // TODO: use constant for form ID field
                }
                break;
            }
        }
        return formRowId;
    }

    /**
     * Returns the form definition ID for the form mapped to the specified activity definition ID.
     * @param activityDefId
     * @return
     */
    public PackageActivityForm retrieveMappedForm(String appId, String version, String processDefId, String activityDefId) {
        String processDefIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        AppDefinition appDef = getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        PackageActivityForm paf = packageDef.getPackageActivityForm(processDefIdWithoutVersion, activityDefId);
        return paf;
    }

    protected FormDefinition retrieveFormDefinition(AppDefinition appDef, PackageActivityForm activityForm) {
        FormDefinition formDef = null;
        if (activityForm != null) {
            String formId = activityForm.getFormId();
            if (formId != null && !formId.isEmpty()) {
                formDef = formDefinitionDao.loadById(formId, appDef);
            }
        }
        return formDef;
    }

    protected Form retrieveForm(AppDefinition appDef, PackageActivityForm activityForm, FormData formData, WorkflowAssignment wfAssignment) {
        Form form = null;
        if (appDef != null && activityForm != null) {
            String formId = activityForm.getFormId();
            if (formId != null && !formId.isEmpty()) {
                // retrieve form HTML
                form = loadFormByFormDefId(appDef.getId(), appDef.getVersion().toString(), formId, formData, wfAssignment);
            }
        }
        return form;
    }

    /**
     * Create a default empty form containing buttons for submission and fields for workflow variables
     * @return 
     */
    protected Form createDefaultForm(String processId, FormData formData) {
        // create default empty form
        Form form = new Form();
        form.setProperty(FormUtil.PROPERTY_ID, "assignmentForm");
        form.setLoadBinder(new WorkflowFormBinder());
        form.setStoreBinder(new WorkflowFormBinder());

        // add textfields for workflow variables
        Collection<Element> children = new ArrayList<Element>();
        Collection<WorkflowVariable> variableList = workflowManager.getProcessVariableList(processId);
        for (WorkflowVariable variable : variableList) {
            String varId = variable.getId();
            String varName = variable.getName();
            TextField tf = new TextField();
            tf.setProperty(FormUtil.PROPERTY_ID, varId);
            tf.setProperty(FormUtil.PROPERTY_LABEL, varName);
            tf.setProperty(AppUtil.PROPERTY_WORKFLOW_VARIABLE, varId);
            children.add(tf);
        }
        form.setChildren(children);

        // load form
        String json = formService.generateElementJson(form);
        form = formService.loadFormFromJson(json, formData);

        // set workflow variable parameter names
        Collection<Element> formFields = form.getChildren(formData);
        for (Element element : formFields) {
            if (element instanceof TextField) {
                element.setCustomParameterName(AppUtil.PREFIX_WORKFLOW_VARIABLE + element.getProperty(FormUtil.PROPERTY_ID));
            }
        }

        return form;
    }

    /**
     * Returns the origin process ID for a process instance.
     * The origin process ID is the top-most process that is started that possibly triggers other sub-processes.
     * @param processId
     * @return
     */
    @Override
    public String getOriginProcessId(String processId) {
        WorkflowProcessLink link = workflowManager.getWorkflowProcessLink(processId);
        String originId = (link != null) ? link.getOriginProcessId() : processId;
        return originId;
    }

    /**
     * Check to see whether an activity is configured to automatically continue on to the next activity.
     * @param packageId
     * @param packageVersion
     * @param processDefId
     * @param activityDefId
     * @return
     */
    @Override
    public boolean isActivityAutoContinue(String packageId, String packageVersion, String processDefId, String activityDefId) {
        boolean autoContinue = false;
        Long version = null;
        try {
            version = Long.parseLong(packageVersion);
        } catch (Exception e) {
            // invalid number, ignore
        }
        if (version != null) {
            processDefId = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
            PackageDefinition packageDef = packageDefinitionDao.loadPackageDefinition(packageId, version);
            if (packageDef != null) {
                PackageActivityForm paf = packageDef.getPackageActivityForm(processDefId, activityDefId);
                if (paf != null) {
                    autoContinue = paf.isAutoContinue();
                }
            }
        }
        return autoContinue;
    }

    /**
     * Retrieve a data form
     * @param appId
     * @param version
     * @param formDefId
     * @param saveButtonLabel
     * @param submitButtonLabel
     * @param cancelButtonLabel
     * @param formData
     * @param formUrl
     * @param cancelUrl
     * @return
     */
    @Override
    public Form viewDataForm(String appId, String version, String formDefId, String saveButtonLabel, String submitButtonLabel, String cancelButtonLabel, FormData formData, String formUrl, String cancelUrl) {
        return viewDataForm(appId, version, formDefId, saveButtonLabel, submitButtonLabel, cancelButtonLabel, null, formData, formUrl, cancelUrl);
    }
    
    @Override
    public Form viewDataForm(String appId, String version, String formDefId, String saveButtonLabel, String submitButtonLabel, String cancelButtonLabel, String cancelButtonTarget, FormData formData, String formUrl, String cancelUrl) {
        AppDefinition appDef = getAppDefinition(appId, version);

        if (formData == null) {
            formData = new FormData();
        }

        // get form
        Form form = loadFormByFormDefId(appDef.getId(), appDef.getVersion().toString(), formDefId, formData, null);

        // set action URL
        form.setProperty("url", formUrl);

        // decorate form with actions
        Collection<FormAction> formActionList = new ArrayList<FormAction>();
        if (saveButtonLabel != null) {
            if (saveButtonLabel.isEmpty()) {
                saveButtonLabel = "Save As Draft";
            }
            Element saveButton = (Element) pluginManager.getPlugin(SaveAsDraftButton.class.getName());
            saveButton.setProperty(FormUtil.PROPERTY_ID, "saveAsDraft");
            saveButton.setProperty("label", saveButtonLabel);
            formActionList.add((FormAction) saveButton);
        }
        if (submitButtonLabel != null) {
            if (submitButtonLabel.isEmpty()) {
                submitButtonLabel = "Submit";
            }
            Element submitButton = (Element) pluginManager.getPlugin(SubmitButton.class.getName());
            submitButton.setProperty(FormUtil.PROPERTY_ID, "submit");
            submitButton.setProperty("label", submitButtonLabel);
            formActionList.add((FormAction) submitButton);
        }
        if (cancelButtonLabel != null) {
            if (cancelButtonLabel.isEmpty()) {
                cancelButtonLabel = "Cancel";
            }
            Element cancelButton = (Element) pluginManager.getPlugin(LinkButton.class.getName());
            cancelButton.setProperty(FormUtil.PROPERTY_ID, "cancel");
            cancelButton.setProperty("label", cancelButtonLabel);
            cancelButton.setProperty("url", cancelUrl);
            if (cancelButtonTarget != null) {
                cancelButton.setProperty("target", cancelButtonTarget);
            }
            formActionList.add((FormAction) cancelButton);
        }
        FormAction[] formActions = formActionList.toArray(new FormAction[0]);
        form = decorateFormActions(form, formActions);

        return form;
    }

    /**
     * Returns a Collection of form data for a process based on criteria
     * @param formDefId
     * @param processId
     * @param query
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return
     */
    @Override
    public Collection<Form> listProcessFormData(String formDefId, String processId, String query, String sort, Boolean desc, int start, int rows) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns the total number of form data rows for a process based on criteria
     * @param formDefId
     * @param query
     * @return
     */
    @Override
    public int countProcessFormData(String formDefId, String query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //----- Console app management use cases ------
    /**
     * Finds the app definition based on the appId and version, cached where possible
     * @param appId
     * @param version If null, empty or equals to AppDefinition.VERSION_LATEST, the latest version is returned.
     * @return null if the specific app definition is not found
     */
    @Override
    public AppDefinition getAppDefinition(String appId, String version) {
        // get app from thread
        boolean isAppDefReset = AppUtil.isAppDefinitionReset();
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        Long versionLong = AppUtil.convertVersionToLong(version);
        if (isAppDefReset || appDef == null || !appDef.getId().equals(appId) || (versionLong != null && !appDef.getVersion().equals(versionLong))) {
            // no matching app in thread, load from DAO
            appDef = loadAppDefinition(appId, version);
        }
        return appDef;
    }

    /**
     * Finds the app definition based on the appId and version
     * @param appId
     * @param version If null, empty or equals to AppDefinition.VERSION_LATEST, the latest version is returned.
     * @return null if the specific app definition is not found
     */
    @Override
    public AppDefinition loadAppDefinition(String appId, String version) {
        // get app from thread
        AppDefinition appDef = null;
        Long versionLong = AppUtil.convertVersionToLong(version);
        if (versionLong == null) {
            // load latest
            appDef = appDefinitionDao.loadById(appId);
        } else {
            // load specific version
            try {
                appDef = appDefinitionDao.loadVersion(appId, versionLong);
            } catch (NumberFormatException e) {
                // TODO: handle exception
            } catch (NullPointerException e) {
                // TODO: handle exception
            }
        }

        // set into thread
        AppUtil.setCurrentAppDefinition(appDef);
        return appDef;
    }
    
    /**
     *
     * @param appDefinition
     * @return A Collection of errors (if any).
     */
    @Override
    public Collection<String> createAppDefinition(AppDefinition appDefinition) {
        Collection<String> errors = new ArrayList<String>();

        // check for duplicate
        String appId = appDefinition.getId();
        AppDefinition appDef = appDefinitionDao.loadById(appId);
        if (appDef != null) {
            errors.add("console.app.error.label.idExists");
        } else {
            // create app
            appDefinitionDao.saveOrUpdate(appDefinition);
        }

        return errors;
    }

    @Override
    public AppDefinition createNewAppDefinitionVersion(String appId) {
        TimeZone current = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT 0"));
        
        Long version = appDefinitionDao.getLatestVersion(appId);
        AppDefinition appDef = appDefinitionDao.loadVersion(appId, version);

        Serializer serializer = new Persister();
        AppDefinition newAppDef = null;

        try {
            newAppDef = serializer.read(AppDefinition.class, new ByteArrayInputStream(getAppDefinitionXml(appId, version)), "UTF-8");
        } catch (Exception e) {
            LogUtil.error(AppServiceImpl.class.getName(), e, appId);
        } finally {
            TimeZone.setDefault(current);
        }

        PackageDefinition packageDef = appDef.getPackageDefinition();
        byte[] xpdl = null;
        
        if (packageDef != null) {
            xpdl = workflowManager.getPackageContent(packageDef.getId(), packageDef.getVersion().toString());
        }
        
        Long newAppVersion = newAppDef.getVersion() + 1;
        return importAppDefinition(newAppDef, newAppVersion, xpdl);
    }

    @Override
    public void deleteAppDefinitionVersion(String appId, Long version) {
        AppDefinition appDef = appDefinitionDao.loadVersion(appId, version);

        appDefinitionDao.delete(appDef);
    }

    @Override
    public void deleteAllAppDefinitionVersions(String appId) {
        // delete app
        appDefinitionDao.deleteAllVersions(appId);

        // TODO: delete processes

    }

    //----- Console workflow management use cases ------
    @Override
    public PackageDefinition deployWorkflowPackage(String appId, String version, byte[] packageXpdl, boolean createNewApp) throws Exception {

        PackageDefinition packageDef = null;
        AppDefinition appDef = null;
        String packageId = workflowManager.getPackageIdFromDefinition(packageXpdl);

        // get app version
        if (appId != null && !appId.isEmpty()) {
            appDef = loadAppDefinition(appId, version);

            // verify packageId
            if (appDef != null && !packageId.equals(appDef.getAppId())) {
                throw new UnsupportedOperationException("Package ID does not match App ID");
            }
        } else {
            appDef = loadAppDefinition(packageId, null);
        }

        if (appDef != null || createNewApp) {
            Long originalVersion = null;

            // deploy package
            String versionStr = workflowManager.getCurrentPackageVersion(packageId);
            String packageIdToUpload = (versionStr != null && !versionStr.isEmpty()) ? packageId : null;
            workflowManager.processUpload(packageIdToUpload, packageXpdl);

            // load package
            versionStr = workflowManager.getCurrentPackageVersion(packageId);
            WorkflowPackage workflowPackage = workflowManager.getPackage(packageId, versionStr);

            // create app from package if not specified
            if (appDef == null) {
                appDef = new AppDefinition();
                appDef.setAppId(packageId);
                appDef.setName(workflowPackage.getPackageName());
                appDef.setVersion(new Long(1));
                createAppDefinition(appDef);
            }

            // get package definition
            packageDef = appDef.getPackageDefinition();
            Long packageVersion = Long.parseLong(versionStr);
            if (packageDef == null) {
                packageDef = packageDefinitionDao.createPackageDefinition(appDef, packageVersion);
            } else {
                originalVersion = packageDef.getVersion();
                packageDefinitionDao.updatePackageDefinitionVersion(packageDef, packageVersion);
            }

            if (originalVersion != null) {
                updateRunningProcesses(packageId, originalVersion, packageVersion);
            }
        }
        return packageDef;
    }
    //----- Console form management use cases ------
    @Resource
    FormDefinitionDao formDefinitionDao;

    @Override
    public Collection<String> createFormDefinition(AppDefinition appDefinition, FormDefinition formDefinition) {
        Collection<String> errors = new ArrayList<String>();

        // check for duplicate
        String formId = formDefinition.getId();
        FormDefinition formDef = formDefinitionDao.loadById(formId, appDefinition);
        if (formDef != null) {
            errors.add("console.form.error.label.idExists");
        } else {
            // set app to form
            formDefinition.setAppDefinition(appDefinition);

            // create app
            formDefinitionDao.add(formDefinition);
        }

        return errors;
    }

    //---- form data use cases
    /**
     * Loads a Form based on a specific form definition ID
     * @param appId
     * @param version
     * @param formDefId
     * @param primaryKeyValue
     * @return
     */
    protected Form loadFormByFormDefId(String appId, String version, String formDefId, FormData formData, WorkflowAssignment wfAssignment) {
        Form form = null;
        try {
            AppDefinition appDef = getAppDefinition(appId, version);
            FormDefinition formDef = formDefinitionDao.loadById(formDefId, appDef);
            String formJson = formDef.getJson();

            if (formJson != null) {
                formJson = AppUtil.processHashVariable(formJson, wfAssignment, StringUtil.TYPE_JSON, null);
                form = (Form) formService.loadFormFromJson(formJson, formData);
            }
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return form;
    }

    /**
     * Decorates a Form by adding a horizontal row of FormAction buttons in a new section.
     * @param form
     * @param formActions
     * @return
     */
    protected Form decorateFormActions(Form form, FormAction[] formActions) {
        if (form != null && formActions != null) {
            // create new section for buttons
            Section section = new Section();
            section.setProperty(FormUtil.PROPERTY_ID, "section-actions");
            Collection<Element> sectionChildren = new ArrayList<Element>();
            section.setChildren(sectionChildren);
            Collection<Element> formChildren = form.getChildren();
            if (formChildren == null) {
                formChildren = new ArrayList<Element>();
            }
            formChildren.add(section);

            // add new horizontal column to section
            Column column = new Column();
            column.setProperty("horizontal", "true");
            Collection<Element> columnChildren = new ArrayList<Element>();
            column.setChildren(columnChildren);
            sectionChildren.add(column);

            // add actions to column
            for (FormAction formAction : formActions) {
                if (formAction != null && formAction instanceof Element) {
                    columnChildren.add((Element) formAction);
                }
            }
        }
        return form;
    }

    /**
     * Use case for form submission by ID
     * @param formDefId
     * @param formData
     * @param ignoreValidation
     * @return
     */
    @Override
    public FormData submitForm(String appId, String version, String formDefId, FormData formData, boolean ignoreValidation) {
        Form form = loadFormByFormDefId(appId, version, formDefId, formData, null);
        if (form != null) {
            return formService.submitForm(form, formData, ignoreValidation);
        } else {
            return formData;
        }
    }
    
    /**
     * Use case for form submission by ID
     * @param form
     * @param formData
     * @param ignoreValidation
     * @return
     */
    @Override
    public FormData submitForm(Form form, FormData formData, boolean ignoreValidation) {
        if (form != null) {
            return formService.submitForm(form, formData, ignoreValidation);
        } else {
            return formData;
        }
    }

    /**
     * Load specific data row (record) by primary key value
     * @param appId
     * @param version
     * @param formDefId
     * @param primaryKeyValue
     * @return null if the form is not available, empty FormRowSet if the form is available but record is not found.
     */
    @Override
    public FormRowSet loadFormData(String appId, String version, String formDefId, String primaryKeyValue) {
        FormRowSet results = null;
        Form form = viewDataForm(appId, version, formDefId, null, null, null, null, null, null);
        if (form != null) {
            results = loadFormData(form, primaryKeyValue);
        }
        return results;
    }

    /**
     * Load specific data row (record) by primary key value for a specific form
     * @param form
     * @param primaryKeyValue
     * @return null if the form is not available, empty FormRowSet if the form is available but record is not found.
     */
    @Override
    public FormRowSet loadFormData(Form form, String primaryKeyValue) {
        return internalLoadFormData(form, primaryKeyValue, true);
    }

    /**
     * Method to load specific data row (record) by primary key value for a specific form.
     * This method is non-transactional to support hibernate's auto update of DB schemas.
     * @param form
     * @param primaryKeyValue
     * @return null if the form is not available, empty FormRowSet if the form is available but record is not found.
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public FormRowSet loadFormDataWithoutTransaction(Form form, String primaryKeyValue) {
        return internalLoadFormData(form, primaryKeyValue, false);
    }

    /**
     * Load specific data row (record) by primary key value for a specific form
     * @param form
     * @param primaryKeyValue
     * @param transactional Determines whether the DAO method to call i.e. transactional or non-transactional
     * @return null if the form is not available, empty FormRowSet if the form is available but record is not found.
     */
    protected FormRowSet internalLoadFormData(Form form, String primaryKeyValue, boolean transactional) {
        FormRowSet results = null;
        if (form != null) {
            results = new FormRowSet();
            results.setMultiRow(false);
            if (primaryKeyValue != null && primaryKeyValue.trim().length() > 0) {
                FormRow row = (transactional) ? formDataDao.load(form, primaryKeyValue) : formDataDao.loadWithoutTransaction(form, primaryKeyValue);
                if (row != null) {
                    results.add(row);
                }
                Logger.getLogger(getClass().getName()).log(Level.FINE, "  -- Loaded form data row [{0}] for form [{1}] from table [{2}]", new Object[]{primaryKeyValue, form.getProperty(FormUtil.PROPERTY_ID), form.getProperty(FormUtil.PROPERTY_TABLE_NAME)});
            }
        }
        return results;
    }

    /**
     * Store specific data row (record). 
     * @param appId
     * @param version
     * @param formDefId
     * @param rows
     * @param primaryKeyValue
     * @return 
     */
    @Override
    public FormRowSet storeFormData(String appId, String version, String formDefId, FormRowSet rows, String primaryKeyValue) {
        FormRowSet results = null;
        Form form = viewDataForm(appId, version, formDefId, null, null, null, null, null, null);
        if (form != null) {
            results = storeFormData(form, rows, primaryKeyValue);
        }
        return results;
    }

    /**
     * Store specific data row (record) for a form. 
     * @param form
     * @param rows
     * @param primaryKeyValue For single-row data. If null, a UUID will be generated. For multi-row data, this value is not used.
     * @return
     */
    @Override
    public FormRowSet storeFormData(Form form, FormRowSet rows, String primaryKeyValue) {
        FormRowSet results = null;
        if (form != null && rows != null && !rows.isEmpty()) {

            // determine rows to store
            results = new FormRowSet();
            if (!rows.isMultiRow()) {
                results.add(rows.get(0));
            } else {
                primaryKeyValue = null;
                results.addAll(rows);
            }

            // iterate through rows
            for (int i = 0; i < results.size(); i++) {
                FormRow row = results.get(i);
                String rowPrimaryKeyValue = row.getId();

                // set id
                if (rowPrimaryKeyValue == null || rowPrimaryKeyValue.trim().length() == 0) {
                    rowPrimaryKeyValue = primaryKeyValue;
                }
                if (rowPrimaryKeyValue == null || rowPrimaryKeyValue.trim().length() == 0) {
                    // no primary key value specified, generate new primary key value
                    rowPrimaryKeyValue = UuidGenerator.getInstance().getUuid();
                }
                row.setId(rowPrimaryKeyValue);
                if (!rows.isMultiRow() && (primaryKeyValue == null || primaryKeyValue.trim().isEmpty())) {
                    primaryKeyValue = rowPrimaryKeyValue;
                }

                // set meta data
                Date currentDate = new Date();
                row.setDateModified(currentDate);
                Date dateCreated = null;
                FormRowSet loadedRow = loadFormDataWithoutTransaction(form, rowPrimaryKeyValue);
                if (loadedRow != null && loadedRow.iterator().hasNext()) {
                    dateCreated = loadedRow.iterator().next().getDateCreated();
                }
                if (dateCreated == null) {
                    dateCreated = currentDate;
                }
                row.setDateCreated(dateCreated);
            }

            // update DB schema
            formDataDao.updateSchema(form, rows);
            
            // save data
            formDataDao.saveOrUpdate(form, results);
            Logger.getLogger(getClass().getName()).log(Level.FINE, "  -- Saved form data row [{0}] for form [{1}] into table [{2}]", new Object[]{primaryKeyValue, form.getProperty(FormUtil.PROPERTY_ID), form.getProperty(FormUtil.PROPERTY_TABLE_NAME)});

            FileUtil.storeFileFromFormRowSet(results, form, primaryKeyValue);
        }
        return results;
    }

    @Override
    public Long getPublishedVersion(String appId) {
        try {
            return appDefinitionDao.getPublishedVersion(appId);
        } catch (Exception e) {
        }
        return null;
    }

    public byte[] getAppDefinitionXml(String appId, Long version) {
        byte[] appDefinitionXml = null;

        ByteArrayOutputStream baos = null;
        
        TimeZone current = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT 0"));
        
        try {
            baos = new ByteArrayOutputStream();

            AppDefinition appDef = getAppDefinition(appId, Long.toString(version));

            Serializer serializer = new Persister();
            serializer.write(appDef, baos, "UTF-8");

            appDefinitionXml = baos.toByteArray();
            baos.close();
            
            String value = new String(appDefinitionXml, "UTF-8");
            value = value.replaceAll("org\\.hibernate\\.collection\\.PersistentBag", "java.util.ArrayList");
            value = value.replaceAll("org\\.hibernate\\.collection\\.PersistentMap", "java.util.HashMap");

            return value.getBytes("UTF-8");
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                    LogUtil.error(getClass().getName(), e, "");
                }
            }
            
            TimeZone.setDefault(current);
        }
        return null;
    }

    /**
     * Export an app version in ZIP format into an OutputStream
     * @param appId
     * @param version If null, the latest app version will be used.
     * @param output The OutputStream the ZIP content will be streamed into
     * @return Returns the OutputStream object parameter passed in. If null, a ByteArrayOutputStream will be created and returned. 
     * @throws IOException 
     */
    @Override
    public OutputStream exportApp(String appId, String version, OutputStream output) throws IOException {
        ZipOutputStream zip = null;
        if (output == null) {
            output = new ByteArrayOutputStream();
        }
        try {
            AppDefinition appDef = loadAppDefinition(appId, version);
            if (appDef != null && output != null) {
                zip = new ZipOutputStream(output);

                // write zip entry for app XML
                byte[] data = getAppDefinitionXml(appId, appDef.getVersion());
                zip.putNextEntry(new ZipEntry("appDefinition.xml"));
                zip.write(data);
                zip.closeEntry();

                // write zip entry for app XML
                PackageDefinition packageDef = appDef.getPackageDefinition();
                byte[] xpdl = workflowManager.getPackageContent(packageDef.getId(), packageDef.getVersion().toString());
                zip.putNextEntry(new ZipEntry("package.xpdl"));
                zip.write(xpdl);
                zip.closeEntry();
                
                // finish the zip
                zip.finish();
            }
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            if (zip != null) {
                zip.flush();
            }
        }
        return output;
    }
    
    @Override
    public AppDefinition importApp(byte[] zip) {
        TimeZone current = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT 0"));
        
        try {
            byte[] appData = getAppDataXmlFromZip(zip);
            byte[] xpdl = getXpdlFromZip(zip);

            Serializer serializer = new Persister();
            AppDefinition appDef = serializer.read(AppDefinition.class, new ByteArrayInputStream(appData), "UTF-8");

            long appVersion = appDefinitionDao.getLatestVersion(appDef.getAppId());

            //Store appDef
            long newAppVersion = appVersion + 1;
            AppDefinition newAppDef = importAppDefinition(appDef, newAppVersion, xpdl);

            importPlugins(zip);

            return newAppDef;
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        } finally {
            TimeZone.setDefault(current);
        }
        return null;
    }

    @Override
    public String getPrimaryKeyWithForeignKey(String appId, String appVersion, String formDefId, String foreignKeyName, String foreignKeyValue) {
        Form form = loadFormByFormDefId(appId, appVersion, formDefId, null, null);

        return formDataDao.findPrimaryKey(form, foreignKeyName, foreignKeyValue);
    }

    /**
     * Update running processes for a package from a version to another.
     * The update is run in a background thread.
     * @param packageId
     * @param fromVersion
     * @param toVersion 
     */
    protected void updateRunningProcesses(final String packageId, final Long fromVersion, final Long toVersion) {
        final String profile = DynamicDataSourceManager.getCurrentProfile();
        Thread backgroundThread = new Thread(new Runnable() {

            public void run() {
                HostManager.setCurrentProfile(profile);
                LogUtil.info(getClass().getName(), "Updating running processes for " + packageId + " from " + fromVersion + " to " + toVersion);
                Collection<WorkflowProcess> runningProcessList = workflowManager.getRunningProcessList(packageId, null, null, fromVersion.toString(), null, null, 0, null);

                for (WorkflowProcess process : runningProcessList) {
                    String processId = null;
                    try {
                        processId = process.getInstanceId();
                        String processDefId = process.getId();
                        processDefId = processDefId.replace("#" + fromVersion.toString() + "#", "#" + toVersion.toString() + "#");
                        workflowManager.processCopyFromInstanceId(processId, processDefId, true);
                    } catch (Exception e) {
                        LogUtil.error(getClass().getName(), e, "Error updating process " + processId);
                    }
                }
                LogUtil.info(getClass().getName(), "Completed updating running processes for " + packageId + " from " + fromVersion + " to " + toVersion);
            }
        });
        backgroundThread.setDaemon(false);
        backgroundThread.start();
    }

    /**
     * Import an app definition object and XPDL content into the system.
     * @param appDef
     * @param appVersion
     * @param xpdl
     * @return 
     */
    @Override
    public AppDefinition importAppDefinition(AppDefinition appDef, Long appVersion, byte[] xpdl) {
        Boolean overrideEnvVariable = false;
        Boolean overridePluginDefault = false;
        
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null && request.getParameterValues("overrideEnvVariable") != null) {
            overrideEnvVariable = true;
        }
        if (request != null && request.getParameterValues("overridePluginDefault") != null) {
            overridePluginDefault = true;
        }
        
        AppDefinition orgAppDef = null;
        if (!overrideEnvVariable || !overridePluginDefault) {
            orgAppDef = loadAppDefinition(appDef.getAppId(), null);
        }

        LogUtil.debug(getClass().getName(), "Importing app " + appDef.getId());        
        AppDefinition newAppDef = new AppDefinition();
        newAppDef.setAppId(appDef.getAppId());
        newAppDef.setVersion(appVersion);
        newAppDef.setId(appDef.getId());
        newAppDef.setName(appDef.getName());
        newAppDef.setPublished(Boolean.FALSE);
        newAppDef.setDateCreated(new Date());
        newAppDef.setDateModified(new Date());
        newAppDef.setLicense(appDef.getLicense());
        appDefinitionDao.saveOrUpdate(newAppDef);

        if (appDef.getDatalistDefinitionList() != null) {
            for (DatalistDefinition o : appDef.getDatalistDefinitionList()) {
                o.setAppDefinition(newAppDef);
                datalistDefinitionDao.add(o);
                LogUtil.debug(getClass().getName(), "Added list " + o.getId());
            }
        }

        if (appDef.getFormDefinitionList() != null) {
            for (FormDefinition o : appDef.getFormDefinitionList()) {
                o.setAppDefinition(newAppDef);
                formDefinitionDao.add(o);
                
                // initialize db table by making a dummy load
                String dummyKey = "xyz123";
                formDataDao.loadWithoutTransaction(o.getId(), o.getTableName(), dummyKey);
                LogUtil.debug(getClass().getName(), "Initialized form " + o.getId() + " with table " + o.getTableName());
            }
        }

        if (appDef.getUserviewDefinitionList() != null) {
            for (UserviewDefinition o : appDef.getUserviewDefinitionList()) {
                o.setAppDefinition(newAppDef);
                userviewDefinitionDao.add(o);
                LogUtil.debug(getClass().getName(), "Added userview " + o.getId());
            }
        }

        if (appDef.getEnvironmentVariableList() != null) {
            for (EnvironmentVariable o : appDef.getEnvironmentVariableList()) {
                if (!overrideEnvVariable && orgAppDef != null && orgAppDef.getEnvironmentVariableList() != null) {
                    EnvironmentVariable temp = environmentVariableDao.loadById(o.getId(), orgAppDef);
                    if (temp != null) {
                        o.setValue(temp.getValue());
                    }
                }
                
                if (o.getValue() == null) {
                    o.setValue("");
                }
                o.setAppDefinition(newAppDef);
                
                environmentVariableDao.add(o);
            }
        }
        
        if (appDef.getMessageList() != null) {
            for (Message o : appDef.getMessageList()) {
                o.setAppDefinition(newAppDef);
                messageDao.add(o);
            }
        }

        if (appDef.getPluginDefaultPropertiesList() != null) {
            for (PluginDefaultProperties o : appDef.getPluginDefaultPropertiesList()) {
                if (!overridePluginDefault && orgAppDef != null && orgAppDef.getPluginDefaultPropertiesList() != null) {
                    PluginDefaultProperties temp = pluginDefaultPropertiesDao.loadById(o.getId(), orgAppDef);
                    if (temp != null) {
                        o.setPluginProperties(temp.getPluginProperties());
                    }
                }
                
                o.setAppDefinition(newAppDef);
                pluginDefaultPropertiesDao.add(o);
            }
        }

        try {
            if (xpdl != null) {
                PackageDefinition oldPackageDef = appDef.getPackageDefinition();

                //deploy package
                PackageDefinition packageDef = deployWorkflowPackage(newAppDef.getAppId(), newAppDef.getVersion().toString(), xpdl, false);

                if (packageDef != null) {
                    if (oldPackageDef != null) {
                        if (oldPackageDef.getPackageActivityFormMap() != null) {
                            for (Entry e : oldPackageDef.getPackageActivityFormMap().entrySet()) {
                                PackageActivityForm form = (PackageActivityForm) e.getValue();
                                form.setPackageDefinition(packageDef);
                                packageDefinitionDao.addAppActivityForm(newAppDef.getAppId(), appVersion, form);
                            }
                        }

                        if (oldPackageDef.getPackageActivityPluginMap() != null) {
                            for (Entry e : oldPackageDef.getPackageActivityPluginMap().entrySet()) {
                                PackageActivityPlugin plugin = (PackageActivityPlugin) e.getValue();
                                plugin.setPackageDefinition(packageDef);
                                packageDefinitionDao.addAppActivityPlugin(newAppDef.getAppId(), appVersion, plugin);
                            }
                        }

                        if (oldPackageDef.getPackageParticipantMap() != null) {
                            for (Entry e : oldPackageDef.getPackageParticipantMap().entrySet()) {
                                PackageParticipant participant = (PackageParticipant) e.getValue();
                                participant.setPackageDefinition(packageDef);
                                packageDefinitionDao.addAppParticipant(newAppDef.getAppId(), appVersion, participant);
                            }
                        }
                    }

                    // generate image for each process
                    List<WorkflowProcess> processList = workflowManager.getProcessList("", Boolean.TRUE, 0, 10000, packageDef.getId(), Boolean.FALSE, Boolean.FALSE);
                    String designerBaseUrl = getDesignerwebBaseUrl(WorkflowUtil.getHttpServletRequest());
                    if (designerBaseUrl != null && !designerBaseUrl.isEmpty()) {
                        for (WorkflowProcess process : processList) {
                            XpdlImageUtil.generateXpdlImage(designerBaseUrl, process.getId(), true);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Error deploying package for " + appDef.getAppId());
        }

        // reload app from DB
        newAppDef = loadAppDefinition(newAppDef.getAppId(), newAppDef.getVersion().toString());
        LogUtil.debug(getClass().getName(), "Finished importing app " + newAppDef.getId() + " version " + newAppDef.getVersion());
        
        return newAppDef;
    }

    protected String getDesignerwebBaseUrl(HttpServletRequest request) {
        String designerwebBaseUrl = null;
        if (request != null) {
            designerwebBaseUrl = "http://" + request.getServerName() + ":" + request.getServerPort();
        }
        if (WorkflowUtil.getSystemSetupValue("designerwebBaseUrl") != null && WorkflowUtil.getSystemSetupValue("designerwebBaseUrl").length() > 0) {
            designerwebBaseUrl = WorkflowUtil.getSystemSetupValue("designerwebBaseUrl");
        }
        if (designerwebBaseUrl != null && designerwebBaseUrl.endsWith("/")) {
            designerwebBaseUrl = designerwebBaseUrl.substring(0, designerwebBaseUrl.length() - 1);
        }

        return designerwebBaseUrl;
    }

    /**
     * Import plugins (JAR) from within a zip content.
     * @param zip
     * @throws Exception 
     */
    @Override
    public void importPlugins(byte[] zip) throws Exception {
        ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ZipEntry entry = null;

        while ((entry = in.getNextEntry()) != null) {
            if (entry.getName().endsWith(".jar")) {
                int length;
                byte[] temp = new byte[1024];
                while ((length = in.read(temp, 0, 1024)) != -1) {
                    out.write(temp, 0, length);
                }

                pluginManager.upload(entry.getName(), new ByteArrayInputStream(out.toByteArray()));
            }
            out.flush();
            out.close();
        }
        in.close();
    }

    /**
     * Reads app XML from zip content.
     * @param zip
     * @return 
     */
    @Override
    public byte[] getAppDataXmlFromZip(byte[] zip) throws Exception {
        ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ZipEntry entry = null;

        while ((entry = in.getNextEntry()) != null) {
            if (entry.getName().contains("appDefinition.xml")) {
                int length;
                byte[] temp = new byte[1024];
                while ((length = in.read(temp, 0, 1024)) != -1) {
                    out.write(temp, 0, length);
                }

                return out.toByteArray();
            }
            out.flush();
            out.close();
        }
        in.close();

        return null;
    }

    /**
     * Reads XPDL from zip content.
     * @param zip
     * @return
     * @throws Exception 
     */
    @Override
    public byte[] getXpdlFromZip(byte[] zip) throws Exception {
        ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        ZipEntry entry = null;

        while ((entry = in.getNextEntry()) != null) {
            if (entry.getName().endsWith(".xpdl")) {
                int length;
                byte[] temp = new byte[1024];
                while ((length = in.read(temp, 0, 1024)) != -1) {
                    out.write(temp, 0, length);
                }

                return out.toByteArray();
            }
            out.flush();
            out.close();
        }
        in.close();

        return null;
    }
    
    public String getFormTableName(String appId, String appVersion, String formDefId) {
        AppDefinition appDef = getAppDefinition(appId, appVersion);
        return getFormTableName(appDef, formDefId);
    }
    
    public String getFormTableName(AppDefinition appDef, String formDefId) {
        FormDefinition formDef = formDefinitionDao.loadById(formDefId, appDef);
        if (formDef != null) {
            return formDef.getTableName();
        }
        return null;
    }

    /**
     * Retrieve list of published apps available to the current user
     * @param appId Optional filter by appId
     * @return 
     */
    public Collection<AppDefinition> getPublishedApps(String appId) {
        Collection<AppDefinition> resultAppDefinitionList = new ArrayList<AppDefinition>();
        Collection<AppDefinition> appDefinitionList = null;
        if (appId == null || appId.trim().isEmpty()) {
            // get list of published apps.
            appDefinitionList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);
        } else {
            // get specific app
            appDefinitionList = new ArrayList<AppDefinition>();
            Long version = getPublishedVersion(appId);
            if (version != null && version > 0) {
                AppDefinition appDef = getAppDefinition(appId, version.toString());
                if (appDef != null) {
                    appDefinitionList.add(appDef);
                }
            }
        }

        // filter based on availability and permission of userviews to run.
        for (Iterator<AppDefinition> i = appDefinitionList.iterator(); i.hasNext();) {
            AppDefinition appDef = i.next();
            
            Collection<UserviewDefinition> uvDefList = appDef.getUserviewDefinitionList();
            Collection<UserviewDefinition> newUvDefList = new ArrayList<UserviewDefinition>();
            
            for (UserviewDefinition uvDef : uvDefList) {
                Userview userview = userviewService.createUserview(appDef, uvDef.getJson(), null, false, null, null, null, false);
                if (userview != null && (userview.getSetting().getPermission() == null || (userview.getSetting().getPermission() != null && userview.getSetting().getPermission().isAuthorize()))) {
                    newUvDefList.add(uvDef);
                }
            }

            if (newUvDefList != null && !newUvDefList.isEmpty()) {
                AppDefinition tempAppDef = new AppDefinition();
                tempAppDef.setAppId(appDef.getId());
                tempAppDef.setVersion(appDef.getVersion());
                tempAppDef.setName(appDef.getName());
                tempAppDef.setUserviewDefinitionList(newUvDefList);
                resultAppDefinitionList.add(tempAppDef);
            }
        }
        return resultAppDefinitionList;
    }

    /**
     * Retrieve list of published processes available to the current user
     * @param appId Optional filter by appId
     * @return 
     */
    public Map<AppDefinition, Collection<WorkflowProcess>> getPublishedProcesses(String appId) {
        Map<AppDefinition, Collection<WorkflowProcess>> appProcessMap = new ListOrderedMap();

        // get list of published apps.
        Collection<AppDefinition> appDefinitionList = null;
        if (appId == null || appId.trim().isEmpty()) {
            // get list of published apps.
            appDefinitionList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);
        } else {
            // get specific app
            appDefinitionList = new ArrayList<AppDefinition>();
            Long version = getPublishedVersion(appId);
            if (version != null && version > 0) {
                AppDefinition appDef = getAppDefinition(appId, version.toString());
                if (appDef != null) {
                    appDefinitionList.add(appDef);
                }
            }
        }

        // filter based on availability of processes to run.
        for (Iterator<AppDefinition> i = appDefinitionList.iterator(); i.hasNext();) {
            AppDefinition appDef = i.next();
            Collection<PackageDefinition> packageDefList = appDef.getPackageDefinitionList();
            if (packageDefList != null && !packageDefList.isEmpty()) {
                PackageDefinition packageDef = packageDefList.iterator().next();
                Collection<WorkflowProcess> processList = workflowManager.getProcessList(packageDef.getId(), packageDef.getVersion().toString());
                
                Collection<WorkflowProcess> processListWithPermission = new ArrayList<WorkflowProcess>();
                
                for (WorkflowProcess process : processList) {
                    if (workflowManager.isUserInWhiteList(process.getId())) {
                        processListWithPermission.add(process);
                    }
                }
                
                if (!processListWithPermission.isEmpty()) {
                    appProcessMap.put(appDef, processListWithPermission);
                }
            } else {
                i.remove();
            }
        }
        
        return appProcessMap;
    }
    
    public void generatePO(String appId, String version, String locale, OutputStream output) throws IOException {
        PrintWriter pw = new PrintWriter(output);
        
        try {
            pw.println("# This file was generated by Joget Workflow");
            pw.println("# http://www.joget.org");
            pw.println("msgid \"\"");
            pw.println("msgstr \"\"");
            pw.println("\"Content-Type: text/plain; charset=utf-8\\n\"");
            pw.println("\"Content-Transfer-Encoding: 8bit\\n\"");
            pw.println("\"Project-Id-Version: " + appId + "\\n\"");
            pw.println("\"POT-Creation-Date: \\n\"");
            pw.println("\"PO-Revision-Date: \\n\"");
            pw.println("\"Last-Translator: \\n\"");
            pw.println("\"Language-Team: \\n\"");
            pw.println("\"Language: " + locale + "\\n\"");
            pw.println("\"MIME-Version: 1.0\\n\"\n");
            
            Map<String, String> messages = getMessages(appId, version, locale);
            for (String key : messages.keySet()) {
                String value = messages.get(key);
                pw.println("msgid \"" + key + "\"");
                pw.println("msgstr \"" + value + "\"\n");
            }
        } catch(Exception e){
        } finally {
            if (pw != null) {
                pw.flush();
                pw.close();
            }
        }
    }
    
    public void importPO(String appId, String version, String locale, MultipartFile multipartFile) throws IOException {
        InputStream inputStream = multipartFile.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        AppDefinition appDef = getAppDefinition(appId, version);
        
        String line = null, key = null, translated = null;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("\"Language: ") && line.endsWith("\\n\"")) {
                    locale = line.substring(11, line.length() - 3);
                } else if (line.startsWith("msgid \"") && !line.equals("msgid \"\"")) {
                    key = line.substring(7, line.length() - 1);
                    translated = null;
                } else if (line.startsWith("msgstr \"")) {
                    translated = line.substring(8, line.length() - 1);
                }
                
                if (key != null && translated != null) {
                    Message message = messageDao.loadByMessageKey(key, locale, appDef);
                    if (message == null) {
                        message = new Message();
                        message.setLocale(locale);
                        message.setMessageKey(key);
                        message.setAppDefinition(appDef);
                        message.setMessage(translated);
                        messageDao.add(message);
                    } else {
                        message.setMessage(translated);
                        messageDao.update(message);
                    }
                    key = null;
                    translated = null;
                }
            }
        } catch(Exception e){
        } finally {
            bufferedReader.close();
            inputStream.close();
        }
    }
    
    protected Map<String, String> getMessages(String appId, String version, String locale) {
        Map<String, String> messages = new HashMap<String, String>();
        
        AppDefinition appDef = getAppDefinition(appId, version);
        if (appDef != null) {
            Collection<DatalistDefinition> dList = appDef.getDatalistDefinitionList();
            if (dList != null && !dList.isEmpty()) {
                for (DatalistDefinition def : dList) {
                    messages.putAll(getMessages(def.getJson()));
                }
            }
            
            Collection<FormDefinition> fList = appDef.getFormDefinitionList();
            if (fList != null && !fList.isEmpty()) {
                for (FormDefinition def : fList) {
                    messages.putAll(getMessages(def.getJson()));
                }
            }
            
            Collection<UserviewDefinition> uList = appDef.getUserviewDefinitionList();
            if (uList != null && !uList.isEmpty()) {
                for (UserviewDefinition def : uList) {
                    messages.putAll(getMessages(def.getJson()));
                }
            }
            
            PackageDefinition packageDefinition = appDef.getPackageDefinition();
            if (packageDefinition != null) {
                Collection<WorkflowProcess> processList = workflowManager.getProcessList(appId, packageDefinition.getVersion().toString());
                if (processList != null && !processList.isEmpty()) {
                    for (WorkflowProcess wp : processList) {
                        //get activity list
                        Collection<WorkflowActivity> activityList = workflowManager.getProcessActivityDefinitionList(wp.getId());
                        if (activityList != null && !activityList.isEmpty()) {
                            for (WorkflowActivity activity : activityList) {
                                messages.putAll(getMessages(activity.getName()));
                            }
                        }
                    }
                }
            }
            
            Collection<Message> mList = messageDao.getMessageList(null, locale, appDef, null, null, null, null);
            if (mList != null && !mList.isEmpty()) {
                for (Message m : mList) {
                    messages.put(m.getMessageKey(), m.getMessage());
                }
            }
        }
        
        return messages;
    }
    
    protected Map<String, String> getMessages(String content) {
        Map<String, String> messages = new HashMap<String, String>();
        
        // check for hash # to avoid unnecessary processing
        if (!AppUtil.containsHashVariable(content)) {
            return messages;
        }
        
        //parse content
        if (content != null) {
            Pattern pattern = Pattern.compile("#i18n\\.([^#^\"]*)#");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                messages.put(matcher.group(1), "");
            }
        }
        
        return messages;
    }
}
