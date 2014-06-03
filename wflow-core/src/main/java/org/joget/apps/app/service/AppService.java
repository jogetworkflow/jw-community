package org.joget.apps.app.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.ImportAppException;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRowSet;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.WorkflowProcessResult;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
public interface AppService {

    /**
     * Process a submitted form to complete an assignment
     * @param activityId
     * @param formData
     * @param workflowVariableMap
     * @return
     */
    FormData completeAssignmentForm(String appId, String version, String activityId, FormData formData, Map<String, String> workflowVariableMap);

    /**
     * Process a submitted form to complete an assignment
     * @param activityId
     * @param formData
     * @param workflowVariableMap
     * @return
     */
    FormData completeAssignmentForm(Form form, WorkflowAssignment assignment, FormData formData, Map<String, String> workflowVariableMap);

    /**
     * Create a new version of an app from an existing version
     * @param appId
     * @param version
     * @return
     */
    AppDefinition createNewAppDefinitionVersion(String appId);

    /**
     * Returns the total number of form data rows for a process based on criteria
     * @param formDefId
     * @param query
     * @return
     */
    int countProcessFormData(String formDefId, String query);

    /**
     * Create a new app definition
     * @param appDefinition
     * @return A Collection of errors (if any).
     */
    Collection<String> createAppDefinition(AppDefinition appDefinition);

    /**
     * Create a new form definition
     * @param formDefinition
     * @return A Collection of errors (if any).
     */
    Collection<String> createFormDefinition(AppDefinition appDefinition, FormDefinition formDefinition);

    /**
     * Delete all versions of an app
     * @param appId
     */
    void deleteAllAppDefinitionVersions(String appId);

    /**
     * Delete a specific app version
     * @param appId
     * @param version
     */
    void deleteAppDefinitionVersion(String appId, Long version);

    /**
     * Deploy an XPDL package for an app.
     * @param appId
     * @param version
     * @param packageXpdl
     * @param createNewApp
     * @return
     * @throws Exception
     */
    PackageDefinition deployWorkflowPackage(String appId, String version, byte[] packageXpdl, boolean createNewApp) throws Exception;

    /**
     * Finds the app definition based on the appId and version, cached where possible
     * @param appId
     * @param version If null, empty or equals to AppDefinition.VERSION_LATEST, the latest version is returned.
     * @return null if the specific app definition is not found
     */
    AppDefinition getAppDefinition(String appId, String version);

    /**
     * Loads the app definition based on the appId and version
     * @param appId
     * @param version If null, empty or equals to AppDefinition.VERSION_LATEST, the latest version is returned.
     * @return null if the specific app definition is not found
     */
    AppDefinition loadAppDefinition(String appId, String version);

    /**
     * Retrieves the workflow process definition for a specific app version.
     * @param appId
     * @param version
     * @param processDefId
     * @return
     */
    WorkflowProcess getWorkflowProcessForApp(String appId, String version, String processDefId);

    /**
     * Retrieves the app definition for a specific workflow activity assignment.
     * @param activityId
     * @return
     */
    AppDefinition getAppDefinitionForWorkflowActivity(String activityId);
    
    /**
     * Retrieves the app definition for a specific workflow process.
     * @param processId
     * @return
     */
    AppDefinition getAppDefinitionForWorkflowProcess(String processId);

    /**
     * Retrieves the app definition for a specific workflow process definition id.
     * @param processDefId
     * @return
     */
    AppDefinition getAppDefinitionWithProcessDefId(String processDefId); 
            
    /**
     * Check to see whether an activity is configured to automatically continue on to the next activity.
     * @param packageId
     * @param version
     * @param processDefId
     * @param activityDefId
     * @return
     */
    boolean isActivityAutoContinue(String packageId, String version, String processDefId, String activityDefId);

    /**
     * Returns the origin process ID for a process instance.
     * The origin process ID is the top-most process that is started that possibly triggers other sub-processes.
     * @param processId
     * @return
     */
    String getOriginProcessId(String processId);

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
    Form viewDataForm(String appId, String version, String formDefId, String saveButtonLabel, String submitButtonLabel, String cancelButtonLabel, FormData formData, String formUrl, String cancelUrl);
    
    Form viewDataForm(String appId, String version, String formDefId, String saveButtonLabel, String submitButtonLabel, String cancelButtonLabel, String cancelButtonTarget, FormData formData, String formUrl, String cancelUrl);

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
    Collection<Form> listProcessFormData(String formDefId, String processId, String query, String sort, Boolean desc, int start, int rows);

    /**
     * Start a process through a form submission
     * @param processDefId
     * @param formData
     * @param workflowVariableMap
     * @return
     */
    WorkflowProcessResult submitFormToStartProcess(String appId, String version, String processDefId, FormData formData, Map<String, String> workflowVariableMap, String originProcessId, String formUrl);

    /**
     * Retrieve a form for a specific activity instance
     * @param appId
     * @param version
     * @param activityId
     * @param formData
     * @param formUrl
     * @return
     */
    PackageActivityForm viewAssignmentForm(String appId, String version, String activityId, FormData formData, String formUrl);

    /**
     * Retrieve a form for a specific activity instance
     * @param appDef
     * @param assignment
     * @param formData
     * @param formUrl
     * @return
     */
    PackageActivityForm viewAssignmentForm(AppDefinition appDef, WorkflowAssignment assignment, FormData formData, String formUrl);
    
    /**
     * Retrieve form mapped to start a process
     * @param appId
     * @param version
     * @param processDefId
     * @param formData
     * @param formUrl
     * @return
     */
    PackageActivityForm viewStartProcessForm(String appId, String version, String processDefId, FormData formData, String formUrl);

    /**
     * Returns the form definition ID for the form mapped to the specified activity definition ID.
     * @param activityDefId
     * @return
     */
    PackageActivityForm retrieveMappedForm(String appId, String version, String processDefId, String activityDefId);
    
    /**
     * Use case for form submission by ID
     * @param formDefId
     * @param formData
     * @param ignoreValidation
     * @return
     */
    FormData submitForm(String appId, String version, String formDefId, FormData formData, boolean ignoreValidation);
    
    /**
     * Use case for form submission by ID
     * @param form
     * @param formData
     * @param ignoreValidation
     * @return
     */
    FormData submitForm(Form form, FormData formData, boolean ignoreValidation);

    /**
     * Load specific data row (record) by primary key value
     * @param appId
     * @param version
     * @param formDefId
     * @param primaryKeyValue
     * @return null if the form is not available, empty FormRowSet if the form is available but record is not found.
     */
    FormRowSet loadFormData(String appId, String version, String formDefId, String primaryKeyValue);

    /**
     * Load specific data row (record) by primary key value for a specific form
     * @param form
     * @param primaryKeyValue
     * @return null if the form is not available, empty FormRowSet if the form is available but record is not found.
     */
    FormRowSet loadFormData(Form form, String primaryKeyValue);

    /**
     * Method to load specific data row (record) by primary key value for a specific form.
     * This method is non-transactional to support hibernate's auto update of DB schemas.
     * @param form
     * @param primaryKeyValue
     * @return null if the form is not available, empty FormRowSet if the form is available but record is not found.
     */
    FormRowSet loadFormDataWithoutTransaction(Form form, String primaryKeyValue);

    /**
     * Store specific data row (record). 
     * @param appId
     * @param version
     * @param formDefId
     * @param rows
     * @param primaryKeyValue
     * @return 
     */
    FormRowSet storeFormData(String appId, String version, String formDefId, FormRowSet rows, String primaryKeyValue);

    /**
     * Store specific data row (record) for a form. 
     * @param form
     * @param rows
     * @param primaryKeyValue For single-row data. If null, a UUID will be generated. For multi-row data, this value is not used.
     * @return
     */
    FormRowSet storeFormData(Form form, FormRowSet rows, String primaryKeyValue);

    /**
     * Get App definition XML
     * @param appId
     * @param version
     * @return
     */
    byte[] getAppDefinitionXml(String appId, Long version);

    /**
     * Export an app version in ZIP format into an OutputStream
     * @param appId
     * @param version If null, the latest app version will be used.
     * @param output The OutputStream the ZIP content will be streamed into
     * @return Returns the OutputStream object parameter passed in. If null, a ByteArrayOutputStream will be created and returned. 
     * @throws IOException 
     */
    public OutputStream exportApp(String appId, String version, OutputStream output) throws IOException;
    
    /**
     * Import app from zip file
     * @param data
     * @return
     */
    AppDefinition importApp(byte[] zip) throws ImportAppException;

    /**
     * Reads app XML from zip content.
     * @param zip
     * @return 
     */
    byte[] getAppDataXmlFromZip(byte[] zip) throws Exception;    

    /**
     * Reads XPDL from zip content.
     * @param zip
     * @return
     * @throws Exception 
     */
    byte[] getXpdlFromZip(byte[] zip) throws Exception;
    
    /**
     * Import an app definition object and XPDL content into the system.
     * @param appDef
     * @param appVersion
     * @param xpdl
     * @return 
     */
    AppDefinition importAppDefinition(AppDefinition appDef, Long appVersion, byte[] xpdl) throws ImportAppException;
    
    /**
     * Import plugins (JAR) from within a zip content.
     * @param zip
     * @throws Exception 
     */
    void importPlugins(byte[] zip) throws Exception;
    
    /**
     * Get version of published app
     * @param appId
     * @return
     */
    public Long getPublishedVersion(String appId);

    public String getPrimaryKeyWithForeignKey(String appId, String appVersion, String formId, String foreignKeyName, String foreignKeyValue);

    public String getFormTableName(String appId, String appVersion, String formDefID);
    
    public String getFormTableName(AppDefinition appDef, String formDefID);

    /**
     * Retrieve list of published apps available to the current user
     * @param appId Optional filter by appId
     * @return 
     */
    public Collection<AppDefinition> getPublishedApps(String appId);

    /**
     * Retrieve list of published apps available to the current user. Overloaded
     * to additionally filter by mobile view support.
     * @param appId Optional filter by appId
     * @return
     */
    public Collection<AppDefinition> getPublishedApps(String appId, boolean mobileView, boolean mobileCache);
    
    /**
     * Retrieve list of published processes available to the current user
     * @param appId Optional filter by appId
     * @return 
     */
    public Map<AppDefinition, Collection<WorkflowProcess>> getPublishedProcesses(String appId);
    
    public void generatePO(String appId, String version, String locale, OutputStream output) throws IOException; 
    
    public void importPO(String appId, String version, String locale, MultipartFile multipartFile) throws IOException;   
}
