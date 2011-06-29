package org.joget.apps.app.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
import org.joget.apps.workflow.lib.AssignmentCompleteButton;
import org.joget.commons.util.FileStore;
import org.joget.commons.util.LogUtil;
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

        WorkflowAssignment assignment = workflowManager.getAssignment(activityId);
        if (assignment != null) {
            String processDefId = assignment.getProcessDefId();
            String activityDefId = assignment.getActivityDefId();
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
        String processId = assignment.getProcessId();
        String processDefId = assignment.getProcessDefId();
        String activityDefId = assignment.getActivityDefId();
        PackageActivityForm activityForm = retrieveMappedForm(appId, version, processDefId, activityDefId);

        // get origin process id
        String originProcessId = getOriginProcessId(processId);

        // get mapped form
        if (formData == null) {
            formData = new FormData();
            formData.setPrimaryKeyValue(originProcessId);
        }
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
            saveButton.setProperty("label", "Save As Draft");
            formActionList.add((FormAction) saveButton);
        }
        Element completeButton = (Element) pluginManager.getPlugin(AssignmentCompleteButton.class.getName());
        completeButton.setProperty(FormUtil.PROPERTY_ID, AssignmentCompleteButton.DEFAULT_ID);
        completeButton.setProperty("label", "Complete");
        formActionList.add((FormAction) completeButton);
        FormAction[] formActions = formActionList.toArray(new FormAction[0]);
        form = decorateFormActions(form, formActions);

        // set to definition
        if (activityForm == null) {
            activityForm = new PackageActivityForm();
        }
        activityForm.setForm(form);

        return activityForm;
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
                formData = submitForm(appId, version, formDefId, formData, false);
            }
        }

        Map<String, String> errors = formData.getFormErrors();
        if (errors == null || errors.isEmpty()) {
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
        if (startFormDef != null && startFormDef.getFormId() != null && !startFormDef.getFormId().isEmpty()) {
            // get mapped form
            Form startForm = retrieveForm(appDef, startFormDef, formData, null);
            if (startForm != null) {
                // set action URL
                startForm.setProperty("url", formUrl);

                // decorate form with actions
                Element submitButton = (Element) pluginManager.getPlugin(SubmitButton.class.getName());
                submitButton.setProperty(FormUtil.PROPERTY_ID, "submit");
                submitButton.setProperty("label", "Submit");
                FormAction[] formActions = {(FormAction) submitButton};
                startForm = decorateFormActions(startForm, formActions);

                // set to definition
                startFormDef.setForm(startForm);
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

            // validate form
            FormData formResult = formService.validateFormData(startForm, formData);
            Map<String, String> errors = formResult.getFormErrors();
            if (errors == null || errors.isEmpty()) {
                // start process
                result = workflowManager.processStart(processDefIdWithVersion, null, workflowVariableMap, null, originProcessId, false);
                String processId = result.getProcess().getInstanceId();
                String originId = (originProcessId != null && originProcessId.trim().length() > 0) ? originProcessId : processId;
                originId = getOriginProcessId(originId);

                // set next activity if configured
                boolean autoContinue = (startFormDef != null) && startFormDef.isAutoContinue();
                if (!autoContinue) {
                    // clear next activities
                    result.setActivities(new ArrayList<WorkflowActivity>());
                }

                // set primary key
                formResult.setPrimaryKeyValue(originId);

                // submit form
                formResult = formService.submitForm(startForm, formData, true);
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
    protected PackageActivityForm retrieveMappedForm(String appId, String version, String processDefId, String activityDefId) {
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
        Collection<Element> formFields = form.getChildren();
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
     * Finds the app definition based on the appId and version
     * @param appId
     * @param version If null, empty or equals to AppDefinition.VERSION_LATEST, the latest version is returned.
     * @return null if the specific app definition is not found
     */
    @Override
    public AppDefinition getAppDefinition(String appId, String version) {
        // get app
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
        Long version = appDefinitionDao.getLatestVersion(appId);
        AppDefinition appDef = appDefinitionDao.loadVersion(appId, version);

        Serializer serializer = new Persister();
        AppDefinition newAppDef = null;

        try {
            newAppDef = serializer.read(AppDefinition.class, new ByteArrayInputStream(getAppDefinitionXml(appId, version)));
        } catch (Exception e) {
            LogUtil.error(AppServiceImpl.class.getName(), e, appId);
        }

        PackageDefinition packageDef = appDef.getPackageDefinition();
        byte[] xpdl = workflowManager.getPackageContent(packageDef.getId(), packageDef.getVersion().toString());

        Long newAppVersion = newAppDef.getVersion() + 1;
        return importingAppDefinition(newAppDef, newAppVersion, xpdl);
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
        String packageId = workflowManager.parsePackageIdFromDefinition(packageXpdl);

        // get app version
        if (appId != null && !appId.isEmpty()) {
            appDef = getAppDefinition(appId, version);

            // verify packageId
            if (appDef != null && !packageId.equals(appDef.getAppId())) {
                throw new UnsupportedOperationException("Package ID does not match App ID");
            }
        } else {
            appDef = getAppDefinition(packageId, null);
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
            if (packageDef == null) {
                packageDef = packageDefinitionDao.createPackageDefinition(appDef);
            } else {
                originalVersion = packageDef.getVersion();
            }

            // update package version
            Long packageVersion = Long.parseLong(versionStr);
            packageDefinitionDao.updatePackageDefinitionVersion(packageDef, packageVersion);

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
                formJson = AppUtil.processHashVariable(formJson, wfAssignment, null, null);
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
                Logger.getLogger(getClass().getName()).log(Level.INFO, "  -- Loaded form data row [{0}] for form [{1}] from table [{2}]", new Object[]{primaryKeyValue, form.getProperty(FormUtil.PROPERTY_ID), form.getProperty(FormUtil.PROPERTY_TABLE_NAME)});
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

            // save data
            formDataDao.saveOrUpdate(form, results);
            Logger.getLogger(getClass().getName()).log(Level.INFO, "  -- Saved form data row [{0}] for form [{1}] into table [{2}]", new Object[]{primaryKeyValue, form.getProperty(FormUtil.PROPERTY_ID), form.getProperty(FormUtil.PROPERTY_TABLE_NAME)});

            if (!rows.isMultiRow()) {
                // handle file uploads (only for a single row)
                Map<String, MultipartFile> uploadedFileMap = FileStore.getFileMap();
                if (uploadedFileMap != null) {
                    for (Iterator<String> i = uploadedFileMap.keySet().iterator(); i.hasNext();) {
                        String fileName = i.next();
                        MultipartFile file = uploadedFileMap.get(fileName);
                        if (file != null && !file.isEmpty()) {
                            // save file in folder
                            FileUtil.storeFile(file, form, primaryKeyValue);
                            Logger.getLogger(getClass().getName()).log(Level.INFO, "  -- Uploaded file: {0}; {1}", new Object[]{fileName, file.getOriginalFilename()});
                        }
                    }
                }
            }

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
        try {
            baos = new ByteArrayOutputStream();

            AppDefinition appDef = getAppDefinition(appId, Long.toString(version));

            Serializer serializer = new Persister();
            serializer.write(appDef, baos);

            appDefinitionXml = baos.toByteArray();
            baos.close();
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
        }
        String value = new String(appDefinitionXml);
        value = value.replaceAll("org\\.hibernate\\.collection\\.PersistentBag", "java.util.ArrayList");
        value = value.replaceAll("org\\.hibernate\\.collection\\.PersistentMap", "java.util.HashMap");

        return value.getBytes();
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
            AppDefinition appDef = getAppDefinition(appId, version);
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
        try {
            byte[] appData = getAppDataXmlFromZip(zip);
            byte[] xpdl = getXpdlFromZip(zip);

            Serializer serializer = new Persister();
            AppDefinition appDef = serializer.read(AppDefinition.class, new ByteArrayInputStream(appData));

            long appVersion = appDefinitionDao.getLatestVersion(appDef.getAppId());

            //Store appDef
            long newAppVersion = appVersion + 1;
            AppDefinition newAppDef = importingAppDefinition(appDef, newAppVersion, xpdl);

            importingPlugin(zip);

            return newAppDef;
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
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
        Thread backgroundThread = new Thread(new Runnable() {

            public void run() {
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

    protected AppDefinition importingAppDefinition(AppDefinition appDef, Long appVersion, byte[] xpdl) {
        AppDefinition newAppDef = new AppDefinition();
        newAppDef.setAppId(appDef.getAppId());
        newAppDef.setVersion(appVersion);
        newAppDef.setId(appDef.getId());
        newAppDef.setName(appDef.getName());
        newAppDef.setPublished(Boolean.FALSE);
        newAppDef.setDateCreated(new Date());
        newAppDef.setDateModified(new Date());
        appDefinitionDao.saveOrUpdate(newAppDef);

        if (appDef.getDatalistDefinitionList() != null) {
            for (DatalistDefinition o : appDef.getDatalistDefinitionList()) {
                o.setAppDefinition(newAppDef);
                datalistDefinitionDao.add(o);
            }
        }

        if (appDef.getFormDefinitionList() != null) {
            for (FormDefinition o : appDef.getFormDefinitionList()) {
                o.setAppDefinition(newAppDef);
                formDefinitionDao.add(o);
            }
        }

        if (appDef.getUserviewDefinitionList() != null) {
            for (UserviewDefinition o : appDef.getUserviewDefinitionList()) {
                o.setAppDefinition(newAppDef);
                userviewDefinitionDao.add(o);
            }
        }

        if (appDef.getEnvironmentVariableList() != null) {
            for (EnvironmentVariable o : appDef.getEnvironmentVariableList()) {
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
                o.setAppDefinition(newAppDef);
                pluginDefaultPropertiesDao.add(o);
            }
        }

        try {
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
                for (WorkflowProcess process : processList) {
                    XpdlImageUtil.generateXpdlImage(getDesignerwebBaseUrl(WorkflowUtil.getHttpServletRequest()), process.getId(), true);
                }
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Error deploying package for " + appDef.getAppId());
        }

        // reload app from DB
        newAppDef = getAppDefinition(newAppDef.getAppId(), newAppDef.getVersion().toString());
        
        return newAppDef;
    }

    protected String getDesignerwebBaseUrl(HttpServletRequest request) {
        String designerwebBaseUrl = "http://" + request.getServerName() + ":" + request.getServerPort();
        if (WorkflowUtil.getSystemSetupValue("designerwebBaseUrl") != null && WorkflowUtil.getSystemSetupValue("designerwebBaseUrl").length() > 0) {
            designerwebBaseUrl = WorkflowUtil.getSystemSetupValue("designerwebBaseUrl");
        }
        if (designerwebBaseUrl.endsWith("/")) {
            designerwebBaseUrl = designerwebBaseUrl.substring(0, designerwebBaseUrl.length() - 1);
        }

        return designerwebBaseUrl;
    }

    protected void importingPlugin(byte[] zip) throws Exception {
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

    protected byte[] getAppDataXmlFromZip(byte[] zip) throws Exception {
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

    protected byte[] getXpdlFromZip(byte[] zip) throws Exception {
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
}
