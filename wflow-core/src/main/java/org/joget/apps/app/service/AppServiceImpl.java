package org.joget.apps.app.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections.map.ListOrderedMap;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.AppResourceDao;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.dao.EnvironmentVariableDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.dao.MessageDao;
import org.joget.apps.app.dao.PackageDefinitionDao;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AppResource;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.app.model.CustomBuilder;
import org.joget.apps.app.model.CustomBuilderCallback;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.model.EnvironmentVariable;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.ImportAppException;
import org.joget.apps.app.model.Message;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.model.ProcessFormModifier;
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
import org.joget.apps.form.service.CustomFormDataTableUtil;
import org.joget.apps.form.service.FileUtil;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.UserviewSetting;
import org.joget.apps.userview.service.UserviewService;
import org.joget.apps.workflow.lib.AssignmentCompleteButton;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.UuidGenerator;
import org.joget.directory.model.User;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowPackage;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowProcessLink;
import org.joget.workflow.model.WorkflowProcessResult;
import org.joget.workflow.model.WorkflowVariable;
import org.joget.workflow.model.dao.WorkflowProcessLinkDao;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.shark.model.dao.WorkflowAssignmentDao;
import org.joget.workflow.util.WorkflowUtil;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of AppService interface
 * 
 */
@Service("appService")
public class AppServiceImpl implements AppService {

    @Autowired
    FormService formService;
    @Autowired
    WorkflowManager workflowManager;
    @Autowired
    WorkflowUserManager workflowUserManager;
    @Autowired
    AppDefinitionDao appDefinitionDao;
    @Autowired
    DatalistDefinitionDao datalistDefinitionDao;
    @Autowired
    UserviewDefinitionDao userviewDefinitionDao;
    @Autowired
    BuilderDefinitionDao builderDefinitionDao;
    @Autowired
    MessageDao messageDao;
    @Autowired
    EnvironmentVariableDao environmentVariableDao;
    @Autowired
    AppResourceDao appResourceDao;
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
    @Autowired
    WorkflowAssignmentDao workflowAssignmentDao;
    @Autowired
    WorkflowProcessLinkDao workflowProcessLinkDao;
    //----- Workflow use cases ------
    
    final protected Map<String, String> processMigration = new HashMap<String, String>();

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
        return viewAssignmentForm(appId, version, activityId, formData, formUrl, null);
    }

    /**
     * Retrieve a form for a specific activity instance
     * @param appId
     * @param version
     * @param activityId
     * @param formData
     * @param formUrl
     * @param cancelUrl
     * @return
     */
    @Override
    public PackageActivityForm viewAssignmentForm(String appId, String version, String activityId, FormData formData, String formUrl, String cancelUrl) {
        AppDefinition appDef = getAppDefinition(appId, version);
        WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
        return viewAssignmentForm(appDef, assignment, formData, formUrl, cancelUrl);
    }
    
    /**
     * Retrieve a form for a specific activity instance
     * @param appDef
     * @param assignment
     * @param formData
     * @param formUrl
     * @return
     */
    @Override
    public PackageActivityForm viewAssignmentForm(AppDefinition appDef, WorkflowAssignment assignment, FormData formData, String formUrl) {
        return viewAssignmentForm(appDef, assignment, formData, formUrl, null);
    }
    
    /**
     * Retrieve a form for a specific activity instance
     * @param appDef
     * @param assignment
     * @param formData
     * @param formUrl
     * @param cancelUrl
     * @return
     */
    @Override
    public PackageActivityForm viewAssignmentForm(AppDefinition appDef, WorkflowAssignment assignment, FormData formData, String formUrl, String cancelUrl) {
        String activityId = assignment.getActivityId();
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
        formData.setActivityId(activityId);
        formData.setProcessId(processId);
        formData.setPrimaryKeyValue(originProcessId);
        
        Collection<FormAction> formActions = new ArrayList<FormAction>();
        // decorate form with actions
        if (activityForm != null && activityForm.getFormId() != null && !activityForm.getFormId().isEmpty() && !activityForm.getDisableSaveAsDraft()) {
            Element saveButton = (Element) pluginManager.getPlugin(SaveAsDraftButton.class.getName());
            saveButton.setProperty(FormUtil.PROPERTY_ID, "saveAsDraft");
            saveButton.setProperty("label", ResourceBundleUtil.getMessage("form.button.saveAsDraft"));
            formActions.add((FormAction) saveButton);
        }
        Element completeButton = (Element) pluginManager.getPlugin(AssignmentCompleteButton.class.getName());
        completeButton.setProperty(FormUtil.PROPERTY_ID, AssignmentCompleteButton.DEFAULT_ID);
        completeButton.setProperty("label", ResourceBundleUtil.getMessage("form.button.complete"));
        formActions.add((FormAction) completeButton);
        if (cancelUrl != null && !cancelUrl.isEmpty()) {
            Element cancelButton = (Element) pluginManager.getPlugin(LinkButton.class.getName());
            cancelButton.setProperty(FormUtil.PROPERTY_ID, "cancel");
            cancelButton.setProperty("label", ResourceBundleUtil.getMessage("general.method.label.cancel"));
            cancelButton.setProperty("url", cancelUrl);
            cancelButton.setProperty("cssClass", cancelButton.getPropertyString("cssClass") + " btn-secondary");
            formActions.add((FormAction) cancelButton);
        }
        
        Form form = retrieveForm(appDef, activityForm, formData, assignment, formActions);
        if (form == null) {
            form = createDefaultForm(processId, formData);
            form.getActions().addAll(formActions);
        }

        // set action URL
        form.setProperty("url", formUrl);
        form = decorateFormActions(form);
        
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
    
    public void executeProcessFormModifier(Form form, FormData formData, WorkflowAssignment assignment, AppDefinition appDef) {
        if (assignment != null) {
            String processDefIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(assignment.getProcessDefId());
            PackageDefinition packageDef = appDef.getPackageDefinition();
            if (packageDef != null) {
                PackageActivityPlugin actPlugin = packageDef.getPackageActivityPlugin(processDefIdWithoutVersion, assignment.getActivityDefId());
                if (actPlugin != null) {
                    Plugin plugin = pluginManager.getPlugin(actPlugin.getPluginName());
                    if (plugin != null && plugin instanceof ProcessFormModifier) {
                        Map propertiesMap = AppPluginUtil.getDefaultProperties(plugin, actPlugin.getPluginProperties(), appDef, assignment);
                        if (plugin instanceof PropertyEditable) {
                            ((PropertyEditable) plugin).setProperties(propertiesMap);
                        }
                        ((ProcessFormModifier) plugin).modify(form, formData, assignment);
                    }
                }
            }
        }
    }
    
    /**
     * Process a submitted form to complete an assignment
     * @param form
     * @param assignment
     * @param formData
     * @param workflowVariableMap
     * @return
     */
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

        // get and submit mapped form
        if (form != null) {
            String originId = form.getPrimaryKeyValue(formData);
            boolean hasExistingRecord = true;
            if (formData.getLoadBinderData(form) != null && !formData.getLoadBinderData(form).isEmpty()) {
                String id = formData.getLoadBinderData(form).iterator().next().getId();
                if (id == null || id.isEmpty()) {
                    hasExistingRecord = false;
                }
            }
            
            formData = submitForm(form, formData, false);
            
            if (!hasExistingRecord && processId.equals(originId) && !originId.equalsIgnoreCase(form.getPrimaryKeyValue(formData))) {
                workflowProcessLinkDao.addWorkflowProcessLink(form.getPrimaryKeyValue(formData), processId);
            }
        }

        Map<String, String> errors = formData.getFormErrors();
        if (!formData.getStay() && (errors == null || errors.isEmpty())) {
            if (!executeProcessFormModifierSubmission(form, formData, assignment, AppUtil.getCurrentAppDefinition())) {
                // accept assignment if necessary
                if (!assignment.isAccepted()) {
                    workflowManager.assignmentAccept(activityId);
                }

                // complete assignment
                workflowManager.assignmentComplete(activityId, workflowVariableMap);
            }
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
        Form form = null;
        AppDefinition appDef = null;
        
        // get and submit mapped form
        PackageActivityForm paf = retrieveMappedForm(appId, version, processDefId, activityDefId);
        if (paf != null) {
            String formDefId = paf.getFormId();
            if (formDefId != null && !formDefId.isEmpty()) {
                String originProcessId = getOriginProcessId(processId);
                formData.setPrimaryKeyValue(originProcessId);
                formData.setProcessId(processId);
                
                appDef = getAppDefinition(appId, version);
                form = retrieveForm(appDef, paf, formData, assignment, null);
                
                String originId = form.getPrimaryKeyValue(formData);
                boolean hasExistingRecord = true;
                if (formData.getLoadBinderData(form) != null && !formData.getLoadBinderData(form).isEmpty()) {
                    String id = formData.getLoadBinderData(form).iterator().next().getId();
                    if (id == null || id.isEmpty()) {
                        hasExistingRecord = false;
                    }
                }
            
                formData = submitForm(form, formData, false);
                
                if (!hasExistingRecord && processId.equals(originId) && !originId.equalsIgnoreCase(form.getPrimaryKeyValue(formData))) {
                    workflowProcessLinkDao.addWorkflowProcessLink(form.getPrimaryKeyValue(formData), processId);
                }
            }
        }

        Map<String, String> errors = formData.getFormErrors();
        if (!formData.getStay() && (errors == null || errors.isEmpty())) {
            if (!executeProcessFormModifierSubmission(form, formData, assignment, appDef)) {
                // accept assignment if necessary
                if (!assignment.isAccepted()) {
                    workflowManager.assignmentAccept(activityId);
                }

                // complete assignment
                workflowManager.assignmentComplete(activityId, workflowVariableMap);
            }
        }
        return formData;
    }
    
    public boolean executeProcessFormModifierSubmission(Form form, FormData formData, WorkflowAssignment assignment, AppDefinition appDef) {
        if (form != null && assignment != null && appDef != null) {
            String processDefIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(assignment.getProcessDefId());
            PackageDefinition packageDef = appDef.getPackageDefinition();
            if (packageDef != null) {
                PackageActivityPlugin actPlugin = packageDef.getPackageActivityPlugin(processDefIdWithoutVersion, assignment.getActivityDefId());
                if (actPlugin != null) {
                    Plugin plugin = pluginManager.getPlugin(actPlugin.getPluginName());
                    if (plugin != null && plugin instanceof ProcessFormModifier) {
                        Map propertiesMap = AppPluginUtil.getDefaultProperties(plugin, actPlugin.getPluginProperties(), appDef, assignment);
                        if (plugin instanceof PropertyEditable) {
                            ((PropertyEditable) plugin).setProperties(propertiesMap);
                        }
                        return ((ProcessFormModifier) plugin).customSubmissionHandling(form, formData, assignment);
                    }
                }
            }
        }
        return false;
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
                Form startForm = retrieveForm(appDef, startFormDef, formData, null, null);
                if (startForm != null) {
                    // set action URL
                    startForm.setProperty("url", formUrl);

                    // decorate form with actions
                    Element submitButton = (Element) pluginManager.getPlugin(AssignmentCompleteButton.class.getName());
                    submitButton.setProperty(FormUtil.PROPERTY_ID, AssignmentCompleteButton.DEFAULT_ID);
                    submitButton.setProperty("label",  ResourceBundleUtil.getMessage("form.button.submit"));
                    startForm.addAction((FormAction) submitButton);
                    startForm = decorateFormActions(startForm);

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
        if (formData == null) {
            formData = new FormData();
        }

        // get form
        PackageActivityForm startFormDef = viewStartProcessForm(appId, version, processDefId, formData, formUrl);
        return submitFormToStartProcess(appId, version, startFormDef, processDefId, formData, workflowVariableMap, originProcessId);
    }

    /**
     * Start a process through a form submission
     * @param appId
     * @param version
     * @param startFormDef
     * @param processDefId
     * @param formData
     * @param workflowVariableMap
     * @param originProcessId
     * @return
     */
    @Override
    public WorkflowProcessResult submitFormToStartProcess(String appId, String version, PackageActivityForm startFormDef, String processDefId, FormData formData, Map<String, String> workflowVariableMap, String originProcessId) {
        WorkflowProcessResult result = null;
        if (formData == null) {
            formData = new FormData();
        }

        AppDefinition appDef = getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        String processDefIdWithVersion = AppUtil.getProcessDefIdWithVersion(packageDef.getId(), packageDef.getVersion().toString(), processDefId);

        // get form
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
                    } else if (startForm.getPrimaryKeyValue(formResult) != null) {
                        originProcessId = startForm.getPrimaryKeyValue(formResult);
                    }

                    // start process
                    result = workflowManager.processStart(processDefIdWithVersion, null, workflowVariableMap, null, originProcessId, true);
                    String processId = result.getProcess().getInstanceId();
                    String originId = result.getParentProcessId();

                    // set primary key
                    formResult.setPrimaryKeyValue(originId);
                    formResult.setProcessId(processId);

                    // submit form
                    formResult = submitForm(startForm, formResult, true);
                    errors = formResult.getFormErrors();
                    if (!formResult.getStay() && (errors == null || errors.isEmpty())) {
                        //if origin id is not equal to record id after submission, add linkage
                        if (!originId.equals(startForm.getPrimaryKeyValue(formData))) {
                            workflowProcessLinkDao.addWorkflowProcessLink(startForm.getPrimaryKeyValue(formData), originId);
                        }
                        
                        result = workflowManager.processStartWithInstanceId(processDefIdWithVersion, processId, workflowVariableMap);

                        // set next activity if configured
                        boolean autoContinue = (startFormDef != null) && startFormDef.isAutoContinue();
                        if (!autoContinue) {
                            // clear next activities
                            result.setActivities(new ArrayList<WorkflowActivity>());
                        }
                    } else {
                        workflowManager.removeProcessInstance(processId);
                        result = null;
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
     * @param appId
     * @param version
     * @param activityDefId
     * @param processDefId
     * @return
     */
    public PackageActivityForm retrieveMappedForm(String appId, String version, String processDefId, String activityDefId) {
        String processDefIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(processDefId);
        AppDefinition appDef = getAppDefinition(appId, version);
        PackageDefinition packageDef = appDef.getPackageDefinition();
        PackageActivityForm paf = packageDef.getPackageActivityForm(processDefIdWithoutVersion, activityDefId);
        if (paf != null) {
            try {
                paf = (PackageActivityForm)paf.clone();
            } catch (CloneNotSupportedException ex) {
                LogUtil.error(AppServiceImpl.class.getName(), ex, "Error cloning PackageActivityForm for " + activityDefId);
            }
        }
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

    protected Form retrieveForm(AppDefinition appDef, PackageActivityForm activityForm, FormData formData, WorkflowAssignment wfAssignment, Collection<FormAction> formActions) {
        Form form = null;
        if (appDef != null && activityForm != null) {
            String formId = activityForm.getFormId();
            if (formId != null && !formId.isEmpty()) {
                // retrieve form HTML
                form = loadFormByFormDefId(appDef.getId(), appDef.getVersion().toString(), formId, formData, wfAssignment);
            }
        }
        if (form != null) {
            if (formActions != null) {
                form.getActions().addAll(formActions);
            }
            executeProcessFormModifier(form, formData, wfAssignment, appDef);
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
     * Returns the origin process ID or recordId for a process instance.
     * The return value can be the process ID of the top-most process 
     * which is started that possibly triggers other sub-processes, or it is a record id
     * used to start the top-most process.
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
    
    /**
     * Retrieve a data form
     * @param appId
     * @param version
     * @param formDefId
     * @param saveButtonLabel
     * @param submitButtonLabel
     * @param cancelButtonLabel
     * @param cancelButtonTarget
     * @param formData
     * @param formUrl
     * @param cancelUrl
     * @return 
     */
    @Override
    public Form viewDataForm(String appId, String version, String formDefId, String saveButtonLabel, String submitButtonLabel, String cancelButtonLabel, String cancelButtonTarget, FormData formData, String formUrl, String cancelUrl) {
        return viewDataForm(appId, version, formDefId, saveButtonLabel, submitButtonLabel, cancelButtonLabel, cancelButtonTarget, formData, formUrl, cancelUrl, null);
    }
    
    /**
     * Retrieve a data form
     * @param appId
     * @param version
     * @param formDefId
     * @param saveButtonLabel
     * @param submitButtonLabel
     * @param cancelButtonLabel
     * @param cancelButtonTarget
     * @param formData
     * @param formUrl
     * @param cancelUrl
     * @param modifier
     * @return 
     */
    @Override
    public Form viewDataForm(String appId, String version, String formDefId, String saveButtonLabel, String submitButtonLabel, String cancelButtonLabel, String cancelButtonTarget, FormData formData, String formUrl, String cancelUrl, ProcessFormModifier modifier) {
        AppDefinition appDef = getAppDefinition(appId, version);

        if (formData == null) {
            formData = new FormData();
        }

        // get form
        Form form = loadFormByFormDefId(appDef.getId(), appDef.getVersion().toString(), formDefId, formData, null);
        return viewDataForm(form, saveButtonLabel, submitButtonLabel, cancelButtonLabel, cancelButtonTarget, formData, formUrl, cancelUrl, modifier);
    }
    
    /**
     * Retrieve a data form
     * @param form
     * @param saveButtonLabel
     * @param submitButtonLabel
     * @param cancelButtonLabel
     * @param cancelButtonTarget
     * @param formData
     * @param formUrl
     * @param cancelUrl
     * @return 
     */
    @Override
    public Form viewDataForm(Form form, String saveButtonLabel, String submitButtonLabel, String cancelButtonLabel, String cancelButtonTarget, FormData formData, String formUrl, String cancelUrl) {
        return viewDataForm(form, saveButtonLabel, submitButtonLabel, cancelButtonLabel, cancelButtonTarget, formData, formUrl, cancelUrl, null);
    }
    
    /**
     * Retrieve a data form
     * @param form
     * @param saveButtonLabel
     * @param submitButtonLabel
     * @param cancelButtonLabel
     * @param cancelButtonTarget
     * @param formData
     * @param formUrl
     * @param cancelUrl
     * @param modifier
     * @return 
     */
    @Override
    public Form viewDataForm(Form form, String saveButtonLabel, String submitButtonLabel, String cancelButtonLabel, String cancelButtonTarget, FormData formData, String formUrl, String cancelUrl, ProcessFormModifier modifier) {
        if (formData == null) {
            formData = new FormData();
        }

        // set action URL
        form.setProperty("url", formUrl);

        // decorate form with actions
        if (saveButtonLabel != null) {
            if (saveButtonLabel.isEmpty()) {
                saveButtonLabel = ResourceBundleUtil.getMessage("form.button.saveAsDraft");
            }
            Element saveButton = (Element) pluginManager.getPlugin(SaveAsDraftButton.class.getName());
            saveButton.setProperty(FormUtil.PROPERTY_ID, "saveAsDraft");
            saveButton.setProperty("label", saveButtonLabel);
            form.addAction((FormAction) saveButton);
        }
        if (submitButtonLabel != null) {
            if (submitButtonLabel.isEmpty()) {
                submitButtonLabel = ResourceBundleUtil.getMessage("general.method.label.submit");
            }
            Element submitButton = (Element) pluginManager.getPlugin(SubmitButton.class.getName());
            submitButton.setProperty(FormUtil.PROPERTY_ID, "submit");
            submitButton.setProperty("label", submitButtonLabel);
            form.addAction((FormAction) submitButton);
        }
        if (cancelButtonLabel != null) {
            if (cancelButtonLabel.isEmpty()) {
                cancelButtonLabel = ResourceBundleUtil.getMessage("general.method.label.cancel");
            }
            Element cancelButton = (Element) pluginManager.getPlugin(LinkButton.class.getName());
            cancelButton.setProperty(FormUtil.PROPERTY_ID, "cancel");
            cancelButton.setProperty("label", cancelButtonLabel);
            cancelButton.setProperty("url", cancelUrl);
            cancelButton.setProperty("cssClass", cancelButton.getPropertyString("cssClass") + " btn-secondary");
            if (cancelButtonTarget != null) {
                cancelButton.setProperty("target", cancelButtonTarget);
            }
            form.addAction((FormAction) cancelButton);
        }
        if (modifier != null) {
            modifier.modify(form, formData, null);
        }
        
        form = decorateFormActions(form);

        return form;
    }

    /**
     * Returns a Collection of form data for a process based on criteria
     * 
     * @Deprecated API used in v2. Not implemented since v3.
     * 
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
     * 
     * @Deprecated API used in v2. Not implemented since v3.
     * 
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
     * Loads the app definition based on the appId and version
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
        try {
            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
            boolean gitSyncAppDone = request != null && "true".equals(request.getAttribute(AppDevUtil.ATTRIBUTE_GIT_SYNC_APP + appId));
            if (!gitSyncAppDone) {
                AppDefinition newAppDef = AppDevUtil.dirSyncApp(appId, versionLong);
                if (newAppDef != null) {
                    appDef = newAppDef;
                }
                if (request != null) {
                    request.setAttribute(AppDevUtil.ATTRIBUTE_GIT_SYNC_APP + appId, "true");
                }
            }
        } catch (IOException | GitAPIException | URISyntaxException e) {
            if (appDef != null) {
                LogUtil.error(getClass().getName(), e, "Error sync app " + appDef);
            }
        }

        // set into thread
        AppUtil.setCurrentAppDefinition(appDef);
        return appDef;
    }
    
    /**
     * Create a new app definition
     * @param appDefinition
     * @return A Collection of errors (if any).
     */
    @Override
    @Transactional
    public Collection<String> createAppDefinition(AppDefinition appDefinition) {
        return createAppDefinition(appDefinition, null);
    }
    
    /**
     * Create a new app definition and duplicate the other app
     * @param appDefinition
     * @param copyAppDefinition
     * @return A Collection of errors (if any).
     */
    @Override
    @Transactional
    public Collection<String> createAppDefinition(AppDefinition appDefinition, AppDefinition copy) {
        Collection<String> errors = new ArrayList<String>();

        // check for duplicate
        String appId = appDefinition.getId();
        AppDefinition appDef = appDefinitionDao.loadById(appId);
        if (appDef != null) {
            errors.add("console.app.error.label.idExists");
        } else {
            if (copy != null) { 
                byte[] appDefinitionXml = null;
                byte[] xpdl = null;
                ByteArrayOutputStream baos = null;

                TimeZone current = TimeZone.getDefault();
                TimeZone.setDefault(TimeZone.getTimeZone("GMT 0"));

                try {
                    baos = new ByteArrayOutputStream();

                    Serializer serializer = new Persister();
                    serializer.write(copy, baos);

                    appDefinitionXml = baos.toByteArray();
                    baos.close();

                    String value = new String(appDefinitionXml, "UTF-8");
                    
                    //replace id and name
                    value = value.replaceAll("<id>"+copy.getAppId()+"</id>", "<id>"+appId+"</id>");
                    value = value.replaceAll("<name>"+copy.getName()+"</name>", "<name>"+appDefinition.getName()+"</name>");
                    value = value.replaceAll("<appId>"+copy.getAppId()+"</appId>", "<appId>"+appId+"</appId>");
                    
                    appDefinitionXml =  value.getBytes("UTF-8");
                    
                    PackageDefinition packageDef = copy.getPackageDefinition();
                    if (packageDef != null) {
                        xpdl = workflowManager.getPackageContent(packageDef.getId(), packageDef.getVersion().toString());
                        Map<String, String> replace = new HashMap<String, String>();
                        replace.put("Id=\""+copy.getAppId()+"\"", "Id=\""+appId+"\"");
                        replace.put("id=\""+copy.getAppId()+"\"", "id=\""+appId+"\"");
                        replace.put("Name=\""+copy.getName()+"\"", "Name=\""+appDefinition.getName()+"\"");
                        replace.put("name=\""+copy.getName()+"\"", "name=\""+appDefinition.getName()+"\"");
                        xpdl = StringUtil.searchAndReplaceByteContent(xpdl, replace);
                    }
                    
                    //import
                    appDef = serializer.read(AppDefinition.class, new ByteArrayInputStream(appDefinitionXml), false);
                    AppDefinition newAppDef = importAppDefinition(appDef, 1L, xpdl);
                    
                    AppResourceUtil.copyAppResources(copy.getAppId(), copy.getVersion().toString(), newAppDef.getAppId(), newAppDef.getVersion().toString());
                } catch (Exception ex) {
                    LogUtil.error(getClass().getName(), ex, "");
                    appDefinitionDao.saveOrUpdate(appDefinition);
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
                
            } else {
                // create app
                appDefinitionDao.saveOrUpdate(appDefinition);
            }
        }

        return errors;
    }
    
    /**
     * Create a new version of an app from an existing latest version
     * @param appId
     * @return
     */
    @Override
    @Transactional
    public AppDefinition createNewAppDefinitionVersion(String appId) {
        return createNewAppDefinitionVersion(appId, null);
    }

    /**
     * Create a new version of an app from an existing version
     * @param appId
     * @param version
     * @return
     */
    @Override
    @Transactional
    public AppDefinition createNewAppDefinitionVersion(String appId, Long version) {
        TimeZone current = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT 0"));
        
        if (version == null) {
            version = appDefinitionDao.getLatestVersion(appId);
        }
        AppDefinition appDef = appDefinitionDao.loadVersion(appId, version);
        appId = appDef.getAppId();

        Serializer serializer = new Persister();
        AppDefinition newAppDef = null;

        try {
            byte[] appData = getAppDefinitionXml(appId, version);
            
            //for backward compatible
            Map<String, String> replacement = new HashMap<String, String>();
            replacement.put("<!--disableSaveAsDraft>", "<disableSaveAsDraft>");
            replacement.put("</disableSaveAsDraft-->", "</disableSaveAsDraft>");
            replacement.put("<!--description>", "<description>");
            replacement.put("</description-->", "</description>");
            replacement.put("<!--meta>", "<meta>");
            replacement.put("</meta-->", "</meta>");
            replacement.put("<!--resourceList>", "<resourceList>");
            replacement.put("</resourceList-->", "</resourceList>");
            replacement.put("<!--resourceList/-->", "<resourceList/>");
            replacement.put("<!--builderDefinitionList>", "<builderDefinitionList>");
            replacement.put("</builderDefinitionList-->", "</builderDefinitionList>");
            appData = StringUtil.searchAndReplaceByteContent(appData, replacement);
            
            newAppDef = serializer.read(AppDefinition.class, new ByteArrayInputStream(appData));

            PackageDefinition packageDef = appDef.getPackageDefinition();
            byte[] xpdl = null;

            if (packageDef != null) {
                xpdl = workflowManager.getPackageContent(packageDef.getId(), packageDef.getVersion().toString());
            }

            Long newAppVersion = appDefinitionDao.getLatestVersion(appId) + 1;
            newAppDef = importAppDefinition(newAppDef, newAppVersion, xpdl);
            
            AppResourceUtil.copyAppResources(appId, version.toString(), appId, newAppDef.getVersion().toString());

            // save app def
            appDefinitionDao.saveOrUpdate(newAppDef);
            
            return newAppDef;
        } catch (Exception e) {
            LogUtil.error(AppServiceImpl.class.getName(), e, appId);
            return null;
        } finally {
            TimeZone.setDefault(current);
        }
    }

    /**
     * Delete a specific app version
     * @param appId
     * @param version
     */
    @Override
    @Transactional
    public void deleteAppDefinitionVersion(String appId, Long version) {
        AppDefinition appDef = appDefinitionDao.loadVersion(appId, version);

        appDefinitionDao.delete(appDef);
        
        AppResourceUtil.deleteAppResources(appDef.getAppId(), appDef.getVersion().toString());
    }

    /**
     * Delete all versions of an app
     * @param appId
     */
    @Override
    @Transactional
    public void deleteAllAppDefinitionVersions(String appId) {
        // delete app
        appDefinitionDao.deleteAllVersions(appId);

        // TODO: delete processes

        AppResourceUtil.deleteAppResourcesForAllVersion(appId);
    }

    //----- Console workflow management use cases ------
    /**
     * Deploy an XPDL package for an app.
     * @param appId
     * @param version
     * @param packageXpdl
     * @param createNewApp
     * @return
     * @throws Exception
     */
    @Override
    @Transactional
    public PackageDefinition deployWorkflowPackage(String appId, String version, byte[] packageXpdl, boolean createNewApp) throws Exception {

        PackageDefinition packageDef = null;
        AppDefinition appDef = null;
        String packageId = workflowManager.getPackageIdFromDefinition(packageXpdl);
        
        // get app version
        if (appId != null && !appId.isEmpty()) {
            appDef = loadAppDefinition(appId, version);

            // verify packageId
            if (appDef != null && !packageId.equalsIgnoreCase(appDef.getAppId())) {
                throw new UnsupportedOperationException("Package ID does not match App ID");
            }
        } else {
            appDef = loadAppDefinition(packageId, null);
        }

        if (appDef != null || createNewApp) {
            Long originalVersion = null;
            
            //to fix package id letter case issue
            if (appDef != null && !packageId.equals(appDef.getAppId())) {
                packageXpdl = StringUtil.searchAndReplaceByteContent(packageXpdl, packageId, appDef.getAppId());
                packageId = appDef.getAppId();
            }

            // deploy package
            String versionStr = workflowManager.getCurrentPackageVersion(packageId);
            String packageIdToUpload = (versionStr != null && !versionStr.isEmpty()) ? packageId : null;
            workflowManager.processUpload(packageIdToUpload, packageXpdl);

            // save to xpdl file for git commit
            if (appDef != null) {
                String xpdl = new String(packageXpdl, "UTF-8");
                String filename = "package.xpdl";
                String commitMessage = "Update xpdl " + appDef.getId();
                AppDevUtil.fileSave(appDef, filename, xpdl, commitMessage);
            }
        
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
                
                //if app version is the only version for the app and no package is found, set process start white list to admin user
                if (appDefinitionDao.countVersions(appId) == 1) {
                    Collection<WorkflowProcess> processList = workflowManager.getProcessList(appDef.getAppId(), packageVersion.toString());
                    for (WorkflowProcess wp : processList) {
                        String processIdWithoutVersion = WorkflowUtil.getProcessDefIdWithoutVersion(wp.getId());
                        PackageParticipant participant = new PackageParticipant();
                        participant.setProcessDefId(processIdWithoutVersion);
                        participant.setParticipantId(WorkflowUtil.PROCESS_START_WHITE_LIST);
                        participant.setType(PackageParticipant.TYPE_ROLE);
                        participant.setValue(PackageParticipant.VALUE_ROLE_ADMIN);
                        packageDefinitionDao.addAppParticipant(appDef.getAppId(), appDef.getVersion(), participant);
                    }
                }
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

    /**
     * Create a new form definition
     * @param appDefinition
     * @param formDefinition
     * @return A Collection of errors (if any).
     */
    @Override
    @Transactional
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
            
            if (formDef != null && formDef.getJson() != null) {
                String formJson = formDef.getJson();
                formJson = AppUtil.processHashVariable(formJson, wfAssignment, StringUtil.TYPE_JSON, null);
                form = (Form) formService.loadFormFromJson(formJson, formData);
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, e.getMessage());
        }
        return form;
    }

    /**
     * Decorates a Form by adding a horizontal row of FormAction buttons in a "section-actions" section.
     * @param form
     * @return
     */
    protected Form decorateFormActions(Form form) {
        if (form != null && form.getActions() != null) {
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
            for (FormAction formAction : form.getActions()) {
                if (formAction != null && formAction instanceof Element) {
                    columnChildren.add((Element) formAction);
                }
            }
        }
        return form;
    }

    /**
     * Use case for form submission by ID
     * @param appId
     * @param version
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
     * Use case for form submission by Form object
     * @param form
     * @param formData
     * @param ignoreValidation
     * @return
     */
    @Override
    public FormData submitForm(Form form, FormData formData, boolean ignoreValidation) {
        if (form != null) {
            try {
                formData = formService.submitForm(form, formData, ignoreValidation);
                FormUtil.executePostFormSubmissionProccessor(form, formData);
            } catch (Exception ex) {
                String formId = FormUtil.getElementParameterName(form);
                formData.addFormError(formId, "Error storing data: " + ex.getMessage());
                LogUtil.error(FormService.class.getName(), ex, "Error executing store binder");
            }
            return formData;
        } else {
            return formData;
        }
    }

    /**
     * Load specific data row (record) by primary key value for a specific form
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
        if (form != null) {
            String formDefId = form.getPropertyString(FormUtil.PROPERTY_ID);
            String tableName = form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME);
            return internalLoadFormData(formDefId, tableName, primaryKeyValue, true);
        }
        return null;
    }

    /**
     * Method to load specific data row (record) by primary key value for a specific form.
     * This method is transactional (since v5), but retains the method name for backward compatibility reasons.
     * @param form
     * @param primaryKeyValue
     * @return null if the form is not available, empty FormRowSet if the form is available but record is not found.
     */
    @Override
    public FormRowSet loadFormDataWithoutTransaction(Form form, String primaryKeyValue) {
        if (form != null) {
            String formDefId = form.getPropertyString(FormUtil.PROPERTY_ID);
            String tableName = form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME);
            return internalLoadFormData(formDefId, tableName, primaryKeyValue, false);
        }
        return null;
    }

    /**
     * Method to load specific data row (record) by primary key value for a specific form.
     * This method is transactional (since v5), but retains the method name for backward compatibility reasons.
     * @param formDefid
     * @param tableName
     * @param primaryKeyValue
     * @return null if the form is not available, empty FormRowSet if the form is available but record is not found.
     */
    @Override
    public FormRowSet loadFormDataWithoutTransaction(String formDefid, String tableName, String primaryKeyValue) {
        return internalLoadFormData(formDefid, tableName, primaryKeyValue, false);
    }

    /**
     * Load specific data row (record) by primary key value for a specific form
     * @param formDefId
     * @param tableName
     * @param primaryKeyValue
     * @param transactional Determines whether the DAO method to call i.e. transactional or non-transactional. No longer used in v5.
     * @return null if the form is not available, empty FormRowSet if the form is available but record is not found.
     */
    protected FormRowSet internalLoadFormData(String formDefId, String tableName, String primaryKeyValue, boolean transactional) {
        FormRowSet results = null;
        if (formDefId != null && tableName != null) {
            results = new FormRowSet();
            results.setMultiRow(false);
            if (primaryKeyValue != null && primaryKeyValue.trim().length() > 0) {
                FormRow row = (transactional) ? formDataDao.load(formDefId, tableName, primaryKeyValue) : formDataDao.loadWithoutTransaction(formDefId, tableName, primaryKeyValue);
                if (row != null) {
                    results.add(row);
                }
                LogUtil.debug(getClass().getName(), "  -- Loaded form data row [" + primaryKeyValue + "] for form [" + formDefId + "] from table [" + tableName + "]");
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
        if (form != null) {
            String formDefId = form.getPropertyString(FormUtil.PROPERTY_ID);
            String tableName = form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME);
            return storeFormData(formDefId, tableName, rows, primaryKeyValue);
        }
        return null;
    }

    /**
     * Store specific data row (record) for a form. 
     * @param formDefId
     * @param tableName
     * @param rows
     * @param primaryKeyValue For single-row data. If null, a UUID will be generated. For multi-row data, this value is not used.
     * @return
     */
    @Override
    public FormRowSet storeFormData(String formDefId, String tableName, FormRowSet rows, String primaryKeyValue) {
        FormRowSet results = null;
        if (formDefId != null && tableName != null && rows != null && !rows.isEmpty()) {

            // determine rows to store
            results = new FormRowSet();
            if (!rows.isMultiRow()) {
                results.add(rows.get(0));
            } else {
                primaryKeyValue = null;
                results.addAll(rows);
            }
            
            Date currentDate = new Date();

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
                row.setDateModified(currentDate);
                row.setModifiedBy(workflowUserManager.getCurrentUsername());
                User user = workflowUserManager.getCurrentUser();
                String name = null;
                if (user != null) {
                    name = user.getFirstName() + ((user.getLastName() != null)?(" "+ user.getLastName()):"");
                }
                row.setModifiedByName(name);
                Date dateCreated = null;
                String createdBy = null;
                String createdByName = null;
                FormRowSet loadedRow = loadFormDataWithoutTransaction(formDefId, tableName, rowPrimaryKeyValue);
                if (loadedRow != null && loadedRow.iterator().hasNext()) {
                    FormRow loadedrow = loadedRow.iterator().next();
                    dateCreated = loadedrow.getDateCreated();
                    createdBy = loadedrow.getCreatedBy();
                    createdByName = loadedrow.getCreatedByName();
                }
                if (dateCreated == null) {
                    dateCreated = currentDate;
                    createdBy = workflowUserManager.getCurrentUsername();
                    createdByName = name;
                    
                    if (rows.isMultiRow()) {
                        currentDate = new Date(currentDate.getTime() + 1000);
                    }
                } else {
                    if (rows.isMultiRow()) {
                        if (dateCreated.after(currentDate)) {
                            currentDate = new Date(dateCreated.getTime() + 1000);
                        }
                    }
                }
                row.setDateCreated(dateCreated);
                row.setCreatedBy(createdBy);
                row.setCreatedByName(createdByName);
            }

            // update DB schema
            formDataDao.updateSchema(formDefId, tableName, rows);
            
            FileUtil.checkAndUpdateFileName(results, tableName, primaryKeyValue);
            
            // save data
            formDataDao.saveOrUpdate(formDefId, tableName, results);
            LogUtil.debug(getClass().getName(), "  -- Saved form data row [" + primaryKeyValue + "] for form [" + formDefId + "] from table [" + tableName + "]");
            
            FileUtil.storeFileFromFormRowSet(results, tableName, primaryKeyValue);
        }
        return results;
    }

    /**
     * Get version of published app
     * @param appId
     * @return
     */
    @Override
    public Long getPublishedVersion(String appId) {
        try {
            return appDefinitionDao.getPublishedVersion(appId);
        } catch (Exception e) {
        }
        return null;
    }
    
    /**
     * Get published app
     * @param appId
     * @return
     */
    public AppDefinition getPublishedAppDefinition(String appId) {
        try {
            AppDefinition appDef = appDefinitionDao.getPublishedAppDefinition(appId);
            AppUtil.setCurrentAppDefinition(appDef);
            return appDef;
        } catch (Exception e) {
        }
        return null;
    }
    
    /**
     * Publish a specific app version
     * @param appId
     * @param version set null to publish the latest version
     * @return the published AppDefinition, null if not found
     */
    @Override
    public AppDefinition publishApp(String appId, String version) {
        // unset previous published version
        Long previousVersion = getPublishedVersion(appId);
        if (previousVersion != null && previousVersion != 0) {
            AppDefinition prevAppDef = appDefinitionDao.loadVersion(appId, previousVersion);
            prevAppDef.setPublished(Boolean.FALSE);
            appDefinitionDao.saveOrUpdate(prevAppDef);
        }
        // set published version
        AppDefinition appDef = null;
        Long versionLong = AppUtil.convertVersionToLong(version);
        if (versionLong == null) {
            // load latest
            appDef = appDefinitionDao.loadById(appId);
        } else {
            // load specific version
            appDef = appDefinitionDao.loadVersion(appId, versionLong);
        }
        if (appDef != null) {
            appDef.setPublished(Boolean.TRUE);
            appDefinitionDao.saveOrUpdate(appDef);
        }
        return appDef;
    }
    
    /**
     * Publish an app
     * @param appId
     * @return the unpublished AppDefinition, null if not found
     */
    @Override
    public AppDefinition unpublishApp(String appId) {
        AppDefinition prevAppDef = getPublishedAppDefinition(appId);
        // unset previous published version
        if (prevAppDef != null) {
            prevAppDef.setPublished(Boolean.FALSE);
            appDefinitionDao.saveOrUpdate(prevAppDef);
        }
        return prevAppDef;
    }

    /**
     * Get App definition XML
     * @param appId
     * @param version
     * @return
     */
    public byte[] getAppDefinitionXml(String appId, Long version) {
        byte[] appDefinitionXml = null;

        ByteArrayOutputStream baos = null;
        
        TimeZone current = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT 0"));
        
        try {
            baos = new ByteArrayOutputStream();

            AppDefinition appDef = getAppDefinition(appId, Long.toString(version));

            Serializer serializer = new Persister();
            serializer.write(appDef, baos);

            appDefinitionXml = baos.toByteArray();
            baos.close();
            
            String value = new String(appDefinitionXml, "UTF-8");
            value = value.replaceAll("org\\.hibernate\\.collection\\.PersistentBag", "java.util.ArrayList");
            value = value.replaceAll("org\\.hibernate\\.collection\\.PersistentMap", "java.util.HashMap");
            
            //for backward compatible
            value = commentTag(value, "disableSaveAsDraft");
            value = commentTag(value, "meta");
            if (value.indexOf("<formDefinitionList>") > 0) {
                int start = value.indexOf("<formDefinitionList>");
                int end = value.indexOf("</formDefinitionList>");
                value = value.substring(0, start - 1) + commentTag(value.substring(start, end-1), "description") + value.substring(end);
            }
            int afterMessagePos = 14;
            if (value.indexOf("<messageList/>") > 0) {
                afterMessagePos += value.indexOf("<messageList/>");
            }else{
                afterMessagePos += value.indexOf("</messageList>");
            }
            value = value.substring(0, afterMessagePos) + commentTag(value.substring(afterMessagePos+1), "description");
            
            value = value.replace("<resourceList>", "<!--resourceList>");
            value = value.replace("</resourceList>", "</resourceList-->");
            value = value.replace("<resourceList/>", "<!--resourceList/-->");
            value = value.replace("<builderDefinitionList>", "<!--builderDefinitionList>");
            value = value.replace("</builderDefinitionList>", "</builderDefinitionList-->");
            value = value.replace("<builderDefinitionList/>", "<!--builderDefinitionList/-->");
            
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
    
    private String commentTag(String content, String tag) {
        Pattern pattern = Pattern.compile("<"+tag+">([^<])*</"+tag+">");
        Matcher matcher = pattern.matcher(content);
        Set<String> foundList = new HashSet<String>();
        while (matcher.find()) {
            foundList.add(matcher.group());
        }
        
        for (String f : foundList) {
            String newf = f;
            newf = newf.replaceAll("-", "&#45;");
            newf = newf.replaceAll("<"+tag+">", "<!--"+tag+">");
            newf = newf.replaceAll("</"+tag+">", "</"+tag+"-->");
            
            content = content.replaceAll(StringUtil.escapeRegex(f), StringUtil.escapeRegex(newf));
        }
        
        return content;
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
                if (packageDef != null) {
                    byte[] xpdl = workflowManager.getPackageContent(packageDef.getId(), packageDef.getVersion().toString());
                    zip.putNextEntry(new ZipEntry("package.xpdl"));
                    zip.write(xpdl);
                    zip.closeEntry();
                }
                
                AppResourceUtil.addResourcesToZip(appId, version, zip);
                
                HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
                if (request != null && request.getParameterValues("exportplugins") != null && !SetupManager.isSecureMode()) {
                    AppDevUtil.addPluginsToZip(appDef, zip);
                }
                if (request != null && request.getParameterValues("tablenames") != null) {
                    exportFormData(appId, version, zip, request.getParameterValues("tablenames"));
                }
                
                for (CustomBuilder builder : CustomBuilderUtil.getBuilderList().values()) {
                    if (builder instanceof CustomBuilderCallback) {
                        ((CustomBuilderCallback) builder).exportAppPostProcessing(appDef, zip);
                    }
                }
                
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
    
    /**
     * Export form data of an app to ZioOutputStream
     * @param appId
     * @param version
     * @param zip
     * @param formTables
     * @throws java.io.UnsupportedEncodingException
     * @throws java.io.IOException
     */
    @Override
    public void exportFormData(String appId, String version, ZipOutputStream zip, String[] formTables) throws UnsupportedEncodingException, IOException {
        if (formTables != null && formTables.length > 0) {
            for (String formTable : formTables) {
                formTable = SecurityUtil.validateStringInput(formTable);
                FormRowSet rows = formDataDao.find(formTable, formTable, null, null, null, null, null, null);
                if (!rows.isEmpty()) {
                    String json = FormUtil.formRowSetToJson(rows);
                    byte[] byteData = json.getBytes("UTF-8");
                    zip.putNextEntry(new ZipEntry("data/"+formTable+".json"));
                    zip.write(byteData);
                    zip.closeEntry();
                    
                    //file uploads
                    for (FormRow row : rows) {
                        File targetDir = new File(FileUtil.getUploadPath(formTable, row.getId()));
                        if (targetDir.exists()) {
                            File[] files = targetDir.listFiles();
                            for (File file : files)
                            {
                                if (file.canRead())
                                {
                                    FileInputStream fis = null;
                                    try {
                                        zip.putNextEntry(new ZipEntry("app_formuploads/" + formTable + "/" + row.getId() + "/" + file.getName()));
                                        fis = new FileInputStream(file);
                                        byte[] buffer = new byte[4092];
                                        int byteCount = 0;
                                        while ((byteCount = fis.read(buffer)) != -1)
                                        {
                                            zip.write(buffer, 0, byteCount);
                                        }
                                        zip.closeEntry();
                                    } finally {
                                        if (fis != null) {
                                            fis.close();
                                        }
                                    }  
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Import app from zip file
     * @param zip
     * @return
     */
    @Override
    @Transactional
    public AppDefinition importApp(byte[] zip) throws ImportAppException {
        TimeZone current = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("GMT 0"));
        
        try {
            byte[] appData = getAppDataXmlFromZip(zip);
            byte[] xpdl = getXpdlFromZip(zip);
            
            //for backward compatible
            Map<String, String> replacement = new HashMap<String, String>();
            replacement.put("<!--disableSaveAsDraft>", "<disableSaveAsDraft>");
            replacement.put("</disableSaveAsDraft-->", "</disableSaveAsDraft>");
            replacement.put("<!--description>", "<description>");
            replacement.put("</description-->", "</description>");
            replacement.put("<!--meta>", "<meta>");
            replacement.put("</meta-->", "</meta>");
            replacement.put("<!--resourceList>", "<resourceList>");
            replacement.put("</resourceList-->", "</resourceList>");
            replacement.put("<!--builderDefinitionList>", "<builderDefinitionList>");
            replacement.put("</builderDefinitionList-->", "</builderDefinitionList>");
            appData = StringUtil.searchAndReplaceByteContent(appData, replacement);

            Serializer serializer = new Persister();
            AppDefinition appDef = serializer.read(AppDefinition.class, new ByteArrayInputStream(appData), false);

            long appVersion = appDefinitionDao.getLatestVersion(appDef.getAppId());

            //Store appDef
            long newAppVersion = appVersion + 1;
            AppDefinition newAppDef = importAppDefinition(appDef, newAppVersion, xpdl);
            
            AppResourceUtil.importFromZip(newAppDef.getAppId(), newAppDef.getVersion().toString(), zip);

            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
            if (request != null && request.getParameterValues("doNotImportPlugins") == null) {
                importPlugins(zip);
            }
            if (request != null && request.getParameterValues("doNotImportFormDatas") == null) {
                importFormData(zip);
            }
            
            for (CustomBuilder builder : CustomBuilderUtil.getBuilderList().values()) {
                if (builder instanceof CustomBuilderCallback) {
                    ((CustomBuilderCallback) builder).importAppPostProcessing(newAppDef, zip);
                }
            }
            
            return newAppDef;
        } catch (ImportAppException e) {
            throw e;
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        } finally {
            TimeZone.setDefault(current);
        }
        return null;
    }

    /**
     * Find a form data record id based a field name and value
     * @param appId
     * @param appVersion
     * @param formDefId
     * @param foreignKeyName
     * @param foreignKeyValue
     * @return 
     */
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
        final AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        final User currentUser = workflowUserManager.getCurrentUser();
        
        Thread backgroundThread = new Thread(new Runnable() {

            public void run() {
                HostManager.setCurrentProfile(profile);
                AppUtil.setCurrentAppDefinition(appDef);
                workflowUserManager.setCurrentThreadUser(currentUser);
                
                processMigration.put(profile + "::" + packageId + "::" + fromVersion, toVersion.toString());
                
                LogUtil.info(getClass().getName(), "Updating running processes for " + packageId + " from " + fromVersion + " to " + toVersion.toString());
                Collection<WorkflowProcess> runningProcessList = workflowManager.getRunningProcessList(packageId, null, null, fromVersion.toString(), null, null, 0, null);

                migrateProcessInstance(runningProcessList, profile, packageId, fromVersion.toString(), toVersion.toString());
                
                processMigration.remove(profile + "::" + packageId + "::" + fromVersion);
                LogUtil.info(getClass().getName(), "Completed updating running processes for " + packageId + " from " + fromVersion + " to " + toVersion.toString());
                removeUnusedXpdl(profile, packageId);
            }
        });
        backgroundThread.setDaemon(false);
        backgroundThread.start();
    }
    
    protected void migrateProcessInstance(Collection<WorkflowProcess> runningProcesses, String profile, String packageId, String fromVersion, String toVersion) {
        if (runningProcesses.isEmpty() || toVersion == null) {
            return;
        }
        
        String newVersion = toVersion;
        
        Collection<String> newProcessDefIds = null;
        Collection<WorkflowProcess> processInstanceNeedReview = new ArrayList<WorkflowProcess>();
        WorkflowProcess lastMigratedProcessInstance = null;
        
        for (WorkflowProcess process : runningProcesses) {
            if (newProcessDefIds == null) {
                newProcessDefIds = new ArrayList<String>();
                Collection<WorkflowProcess> processes = workflowManager.getProcessList(packageId, newVersion);
                for (WorkflowProcess processDef : processes) {
                    newProcessDefIds.add(processDef.getId());
                }
            }
            String processId = null;
            try {
                processId = process.getInstanceId();
                String processDefId = process.getId();
                processDefId = processDefId.replaceAll("#[0-9]+#", "#" + newVersion + "#");

                if (newProcessDefIds.contains(processDefId)) {
                    WorkflowProcessResult result = workflowManager.processCopyFromInstanceId(processId, processDefId, true);
                    if (result != null && result.getProcess() != null) {
                        lastMigratedProcessInstance = result.getProcess();
                    }
                } else {
                    workflowManager.processAbort(processId);
                    LogUtil.info(getClass().getName(), "Process Def ID " + processDefId + " does not exist. Aborted process " + processId + ".");
                }
            } catch (Exception e) {
                LogUtil.error(getClass().getName(), e, "Error updating process " + processId);
            }

            if (processMigration.containsKey(profile + "::" + packageId + "::" + newVersion)) {
                newProcessDefIds = null;
                String tempVersion = processMigration.get(profile + "::" + packageId + "::" + newVersion);
                if (fromVersion != null) {
                    processMigration.put(profile + "::" + packageId + "::" + fromVersion, tempVersion);
                }
                LogUtil.info(getClass().getName(), "New update found when updating running processes for " + packageId + " from " + fromVersion + " to " + newVersion + ". Continue update remaining running processes to " + tempVersion);
                newVersion = tempVersion;
                processInstanceNeedReview.add(lastMigratedProcessInstance);
            }
        }
        
        migrateProcessInstance(processInstanceNeedReview, profile, packageId, null, newVersion);
    }
    
    protected void removeUnusedXpdl(String profile, String packageId) {
        Collection<WorkflowProcess> existingProcesses = workflowManager.getProcessList(packageId);
        Set<String> versions = new HashSet<String>();
        for (WorkflowProcess p : existingProcesses) {
            versions.add(p.getVersion());
        }
        
        //removed version of latest package used by each app version
        Collection<Long> allPackageVersion = packageDefinitionDao.getPackageVersions(packageId);
        for (Long l : allPackageVersion) {
            versions.remove(l.toString());
        }
        
        //removed version of package used by all existing assignment
        Collection<String> allAssignmentPackageDefIds = workflowAssignmentDao.getPackageDefIds(packageId);
        for (String id : allAssignmentPackageDefIds) {
            String[] part = id.split("#");
            versions.remove(part[1]);
        }
        
        for (String v : versions) {
            if (!processMigration.containsKey(profile + "::" + packageId + "::" + v)) {
                try {
                    LogUtil.debug(getClass().getName(), "Trying to remove package " + packageId + " version " + v);
                    workflowManager.processDeleteAndUnloadVersion(packageId, v);
                } catch (Exception e) {
                    LogUtil.debug(getClass().getName(), "Fail to remove package " + packageId + " version " + v);
                }
            }
        }
    } 

    /**
     * Import an app definition object and XPDL content into the system.
     * @param appDef
     * @param appVersion
     * @param xpdl
     * @return 
     */
    @Override
    @Transactional
    public AppDefinition importAppDefinition(AppDefinition appDef, Long appVersion, byte[] xpdl) throws ImportAppException {
        Boolean overrideEnvVariable = false;
        Boolean overridePluginDefault = false;
        Boolean doNotImportParticipant = false;
        Boolean doNotImportTool = false;
        
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null && request.getParameterValues("overrideEnvVariable") != null) {
            overrideEnvVariable = true;
        }
        if (request != null && request.getParameterValues("overridePluginDefault") != null) {
            overridePluginDefault = true;
        }
        if (request != null && request.getParameterValues("doNotImportParticipant") != null) {
            doNotImportParticipant = true;
        }
        if (request != null && request.getParameterValues("doNotImportTool") != null) {
            doNotImportTool = true;
        }
        
        //fix app id letter case issue during import
        AppDefinition orgAppDef = loadAppDefinition(appDef.getAppId(), null);
        String appId = appDef.getAppId();
        if (orgAppDef != null) {
            appId = orgAppDef.getAppId();
        }

        LogUtil.debug(getClass().getName(), "Importing app " + appDef.getId());        
        AppDefinition newAppDef = new AppDefinition();
        newAppDef.setAppId(appId);
        newAppDef.setVersion(appVersion);
        newAppDef.setId(appId);
        newAppDef.setName(appDef.getName());
        newAppDef.setPublished(Boolean.FALSE);
        newAppDef.setDateCreated(new Date());
        newAppDef.setDateModified(new Date());
        newAppDef.setLicense(appDef.getLicense());
        newAppDef.setDescription(appDef.getDescription());
        newAppDef.setMeta(appDef.getMeta());
        appDefinitionDao.saveOrUpdate(newAppDef);

        if (appDef.getFormDefinitionList() != null) {
            for (FormDefinition o : appDef.getFormDefinitionList()) {
                o.setAppDefinition(newAppDef);
                formDefinitionDao.add(o);
            }
            
            String currentTable = "";
            Collection<String> importedForms = new ArrayList<String>();
            try {
                for (FormDefinition o : appDef.getFormDefinitionList()) {
                    currentTable = o.getTableName();
                    // initialize db table by making a dummy load
                    String dummyKey = "xyz123";
                    formDataDao.loadWithoutTransaction(o.getId(), o.getTableName(), dummyKey);
                    importedForms.add(o.getId());
                    LogUtil.debug(getClass().getName(), "Initialized form " + o.getId() + " with table " + o.getTableName());
                }
            } catch (Exception e) {
                //error creating form data table, rollback
                for (String formId : importedForms) {
                    formDefinitionDao.delete(formId, newAppDef);
                }
                appDefinitionDao.delete(newAppDef);
                String errorMessage = "";
                if (currentTable.length() > 20) {
                    errorMessage = ": " + ResourceBundleUtil.getMessage("form.form.invalidId");
                }
                throw new ImportAppException(ResourceBundleUtil.getMessage("console.app.import.error.createTable", new Object[]{currentTable, errorMessage}), e);
            }
        }
        
        if (appDef.getDatalistDefinitionList() != null) {
            for (DatalistDefinition o : appDef.getDatalistDefinitionList()) {
                o.setAppDefinition(newAppDef);
                datalistDefinitionDao.add(o);
                LogUtil.debug(getClass().getName(), "Added list " + o.getId());
            }
        }

        if (appDef.getUserviewDefinitionList() != null) {
            for (UserviewDefinition o : appDef.getUserviewDefinitionList()) {
                o.setAppDefinition(newAppDef);
                
                //remove tempDisablePermissionChecking setting
                if (o.getJson().contains("\"tempDisablePermissionChecking\"")) {
                    o.setJson(o.getJson().replace("\"tempDisablePermissionChecking\"", "\"__\""));
                }
                
                userviewDefinitionDao.add(o);
                LogUtil.debug(getClass().getName(), "Added userview " + o.getId());
            }
        }
        
        if (appDef.getBuilderDefinitionList() != null) {
            for (BuilderDefinition o : appDef.getBuilderDefinitionList()) {
                o.setAppDefinition(newAppDef);
                builderDefinitionDao.add(o);
                
                if (CustomFormDataTableUtil.TYPE.equals(o.getType())) {
                    try {
                        String dummyKey = "xyz123";
                        formDataDao.loadWithoutTransaction(o.getId(), o.getId(), dummyKey);
                    } catch (Exception e) {
                        LogUtil.error(getClass().getName(), e, "");
                    }
                }
                
                LogUtil.debug(getClass().getName(), "Added " + o.getType() + " " + o.getId());
            }
        }

        if (!overrideEnvVariable && orgAppDef != null && orgAppDef.getEnvironmentVariableList() != null) {
            for (EnvironmentVariable o : orgAppDef.getEnvironmentVariableList()) {
                EnvironmentVariable temp = new EnvironmentVariable();
                temp.setAppDefinition(newAppDef);
                temp.setId(o.getId());
                temp.setValue(o.getValue());
                temp.setRemarks(o.getRemarks());
                environmentVariableDao.add(temp);
            }
        } else {
            if (appDef.getEnvironmentVariableList() != null) {
                for (EnvironmentVariable o : appDef.getEnvironmentVariableList()) {
                    if (o.getValue() == null) {
                        o.setValue("");
                    }
                    o.setAppDefinition(newAppDef);

                    environmentVariableDao.add(o);
                }
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
        
        if (appDef.getResourceList() != null) {
            for (AppResource o : appDef.getResourceList()) {
                o.setAppDefinition(newAppDef);
                appResourceDao.add(o);
            }
        }

        try {
            if (xpdl != null) {
                PackageDefinition orgPackageDef = null;
                if ((doNotImportParticipant || doNotImportTool) && orgAppDef != null) {
                    orgPackageDef = orgAppDef.getPackageDefinition();
                }
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
                                if (orgPackageDef != null && doNotImportTool) {
                                    PackageActivityPlugin tempPlugin = orgPackageDef.getPackageActivityPlugin(plugin.getProcessDefId(), plugin.getActivityDefId());
                                    if (tempPlugin != null) {
                                        plugin.setPluginName(tempPlugin.getPluginName());
                                        plugin.setPluginProperties(tempPlugin.getPluginProperties());
                                    }
                                }
                                plugin.setPackageDefinition(packageDef);
                                packageDefinitionDao.addAppActivityPlugin(newAppDef.getAppId(), appVersion, plugin);
                            }
                        }

                        if (oldPackageDef.getPackageParticipantMap() != null) {
                            for (Entry e : oldPackageDef.getPackageParticipantMap().entrySet()) {
                                PackageParticipant participant = (PackageParticipant) e.getValue();
                                if (orgPackageDef != null && doNotImportParticipant) {
                                    PackageParticipant tempParticipant = orgPackageDef.getPackageParticipant(participant.getProcessDefId(), participant.getParticipantId());
                                    if (tempParticipant != null) {
                                        participant.setType(tempParticipant.getType());
                                        participant.setValue(tempParticipant.getValue());
                                        participant.setPluginProperties(tempParticipant.getPluginProperties());
                                    }
                                }
                                participant.setPackageDefinition(packageDef);
                                packageDefinitionDao.addAppParticipant(newAppDef.getAppId(), appVersion, participant);
                            }
                        }
                        
                        // update app definition
                        appDefinitionDao.saveOrUpdate(newAppDef);
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
     * Import form data from within a zip content.
     * @param zip
     * @throws Exception 
     */
    @Override
    public void importFormData(byte[] zip) throws Exception {
        ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip));
        ZipEntry entry = null;

        while ((entry = in.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                if (entry.getName().startsWith("data/")) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int length;
                    byte[] temp = new byte[1024];
                    while ((length = in.read(temp, 0, 1024)) != -1) {
                        out.write(temp, 0, length);
                    }
                
                    String json = new String(out.toByteArray(), "UTF-8");
                    FormRowSet rows = FormUtil.jsonToFormRowSet(json, false);

                    String tablename = entry.getName().substring(5, entry.getName().indexOf(".json"));
                    formDataDao.saveOrUpdate(tablename+"_import", tablename, rows);
                    
                    out.flush();
                    out.close();
                } else if (entry.getName().startsWith("app_formuploads/")) {
                    FileOutputStream out = null;
                    String filename = entry.getName();
                    try {
                        filename = SecurityUtil.normalizedFileName(filename);
                        File file = new File(SetupManager.getBaseDirectory(), URLDecoder.decode(filename, "UTF-8"));
                        
                        if (file.exists()) {
                            file.delete();
                        } else {
                            File parentFolder = file.getParentFile();
                            if (!parentFolder.exists()) {
                                parentFolder.mkdirs();
                            }
                        }

                        out = new FileOutputStream(file);
                        int length;
                        byte[] temp = new byte[1024];
                        while ((length = in.read(temp, 0, 1024)) != -1) {
                            out.write(temp, 0, length);
                        }
                    } catch (Exception ex) {
                    } finally {
                        if (out != null) {
                            try {
                                out.flush();
                                out.close();
                            } catch (IOException iex) {}
                        }
                    }
                }
            }
        }
        in.close();
    }

    /**
     * Reads app XML from zip content.
     * @param zip
     * @return 
     * @throws java.lang.Exception 
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
    
    /**
     * Get table name of a form
     * @param appId
     * @param appVersion
     * @param formDefId
     * @return 
     */
    public String getFormTableName(String appId, String appVersion, String formDefId) {
        AppDefinition appDef = getAppDefinition(appId, appVersion);
        return getFormTableName(appDef, formDefId);
    }
    
    /**
     * Get table name of a form
     * @param appDef
     * @param formDefId
     * @return 
     */
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
        Collection<AppDefinition> resultAppDefinitionList = getPublishedApps(appId, false, false);
        return resultAppDefinitionList;
    }

    /**
     * Retrieve list of published apps available to the current user. Overloaded
     * to additionally filter by mobile view support.
     * @param appId Optional filter by appId
     * @param mobileView
     * @param mobileCache
     * @return
     */
    public Collection<AppDefinition> getPublishedApps(String appId, boolean mobileView, boolean mobileCache) {
        AppDefinition orgAppDef = AppUtil.getCurrentAppDefinition();
        Collection<AppDefinition> resultAppDefinitionList = new ArrayList<AppDefinition>();
        try {
            Collection<AppDefinition> appDefinitionList;
            if (appId == null || appId.trim().isEmpty()) {
                // get list of published apps.
                appDefinitionList = appDefinitionDao.findPublishedApps("name", Boolean.FALSE, null, null);
            } else {
                // get specific app
                appDefinitionList = new ArrayList<AppDefinition>();
                AppDefinition appDef = getPublishedAppDefinition(appId);
                if (appDef != null) {
                    appDefinitionList.add(appDef);
                }
            }

            // filter based on availability and permission of userviews to run.
            for (Iterator<AppDefinition> i = appDefinitionList.iterator(); i.hasNext();) {
                AppDefinition appDef = i.next();
                AppUtil.setCurrentAppDefinition(appDef);
                try {
                    Collection<UserviewDefinition> uvDefList = appDef.getUserviewDefinitionList();
                    Collection<UserviewDefinition> newUvDefList = new ArrayList<UserviewDefinition>();

                    for (UserviewDefinition uvDef : uvDefList) {
                        UserviewSetting userviewSetting = userviewService.getUserviewSetting(appDef, uvDef.getJson());
                        if (userviewSetting != null && (userviewSetting.getPermission() == null || (userviewSetting.getPermission() != null && userviewSetting.getPermission().isAuthorize()))
                                && (!mobileView || !userviewSetting.getTheme().isMobileViewDisabled())
                                && (!mobileCache || "true".equals(userviewSetting.getProperty("mobileCacheEnabled")))) {
                            if (!userviewSetting.getPropertyString("userview_thumbnail").isEmpty()) {
                                uvDef.setThumbnail(userviewSetting.getPropertyString("userview_thumbnail"));
                            }
                            uvDef.setName(AppUtil.processHashVariable(uvDef.getName(), null, null, null, appDef));
                            uvDef.setDescription(AppUtil.processHashVariable(uvDef.getDescription(), null, null, null, appDef));
                            newUvDefList.add(uvDef);
                        }
                    }

                    if (!newUvDefList.isEmpty()) {
                        AppDefinition tempAppDef = new AppDefinition();
                        tempAppDef.setAppId(appDef.getId());
                        tempAppDef.setVersion(appDef.getVersion());
                        tempAppDef.setName(appDef.getName());
                        tempAppDef.setUserviewDefinitionList(newUvDefList);
                        resultAppDefinitionList.add(tempAppDef);
                    }
                } catch(Exception e) {
                    LogUtil.error(AppServiceImpl.class.getName(), e, "Error generating userviews for  " + appDef.getId());
                }
            }
        } finally {
            AppUtil.setCurrentAppDefinition(orgAppDef);
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
            AppDefinition appDef = getPublishedAppDefinition(appId);
            if (appDef != null) {
                appDefinitionList.add(appDef);
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
    
    /**
     * Generate Message Bundle PO file to OutputStream
     * @param appId
     * @param version
     * @param locale
     * @param output
     * @throws IOException 
     */
    public void generatePO(String appId, String version, String locale, OutputStream output) throws IOException {
        Writer writer = new OutputStreamWriter(output, "UTF-8");
        
        try {
            writer.append("# This file was generated by Joget DX\r\n");
            writer.append("# http://www.joget.org\r\n");
            writer.append("msgid \"\"\r\n");
            writer.append("msgstr \"\"\r\n");
            writer.append("\"Content-Type: text/plain; charset=utf-8\\n\"\r\n");
            writer.append("\"Content-Transfer-Encoding: 8bit\\n\"\r\n");
            writer.append("\"Project-Id-Version: " + appId + "\\n\"\r\n");
            writer.append("\"POT-Creation-Date: \\n\"\r\n");
            writer.append("\"PO-Revision-Date: \\n\"\r\n");
            writer.append("\"Last-Translator: \\n\"\r\n");
            writer.append("\"Language-Team: \\n\"\r\n");
            writer.append("\"Language: " + locale + "\\n\"\r\n");
            writer.append("\"MIME-Version: 1.0\\n\"\r\n\r\n");
            
            Map<String, String> messages = getMessages(appId, version, locale);
            for (String key : messages.keySet()) {
                String value = messages.get(key);
                writer.append("msgid \"" + key + "\"\r\n");
                writer.append("msgstr \"" + value + "\"\r\n");
            }
        } catch(Exception e) {
            LogUtil.error(AppServiceImpl.class.getName(), e, "Error generating PO file for " + appId);
        } finally {
            writer.flush();
            writer.close();
        }
    }
    
    /**
     * Import Messages from a PO file
     * @param appId
     * @param version
     * @param locale
     * @param multipartFile
     * @throws IOException 
     */
    @Transactional
    public void importPO(String appId, String version, String locale, MultipartFile multipartFile) throws IOException {
        InputStream inputStream = null;
        
        String line = null, key = null, translated = null;
        try {
            inputStream = multipartFile.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            AppDefinition appDef = getAppDefinition(appId, version);
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("\"Language: ") && line.endsWith("\\n\"")) {
                    locale = line.substring(11, line.length() - 3);
                } else if (line.startsWith("msgid \"") && !line.equals("msgid \"\"")) {
                    key = line.substring(7, line.length() - 1);
                    translated = null;
                } else if (line.startsWith("msgstr \"") && line.endsWith("\"")) {
                    translated = line.substring(8, line.length() - 1);
                } else if (line.startsWith("msgstr \"")) {
                    translated = line.substring(8, line.length());
                    while ((line = bufferedReader.readLine()) != null) {
                        if (line.endsWith("\"")) {
                            translated += "\n" + line.substring(0, line.length() - 1);
                            break;
                        } else {
                            translated += "\n" + line;
                        }
                    }
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
            LogUtil.error(AppServiceImpl.class.getName(), e, "Error importing PO file " + e.getMessage());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
    
    /**
     * Retrieve all apps without check for permission
     * @return 
     */
    public Collection<AppDefinition> getUnprotectedAppList(){
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", false, null, null);
        return appDefinitionList;
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
            
            Collection<BuilderDefinition> bList = appDef.getBuilderDefinitionList();
            if (bList != null && !bList.isEmpty()) {
                for (BuilderDefinition def : bList) {
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
