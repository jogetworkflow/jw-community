package org.joget.apps.app.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.joget.apps.form.dao.FormDataDao;
import org.springframework.transaction.annotation.Propagation;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Map;
import junit.framework.Assert;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.joget.apps.app.dao.PackageDefinitionDao;
import org.joget.apps.app.service.AppService;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.workflow.lib.AssignmentCompleteButton;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.WorkflowActivity;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.WorkflowProcessResult;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml", "classpath:appsApplicationContext.xml"})
public class TestAppService {

    protected final String TEST_APP_ID = "workflow_patterns";
    protected final Long TEST_APP_VERSION = new Long(1);
    protected final String TEST_PACKAGE_ID = "workflow_patterns";
    protected final String TEST_PROCESS_DEF_ID = "WfBCP1_Sequence";
    protected final String TEST_ACTIVITY_DEF_ID = "A";
    protected final String TEST_ACTIVITY_DEF_ID_2 = "B";
    protected final String TEST_FORM_ID = "workflow_patterns_form0";
    protected final String TEST_FORM_ID_1 = "workflow_patterns_form1";
    protected final String TEST_FORM_ID_2 = "workflow_patterns_form2";
    protected final String TEST_XPDL = "/workflow_patterns.xpdl";
    protected final String TEST_FORM_A = "testFormA";
    protected final String TEST_FORM_B = "testFormB";
    protected final String TEST_FORM_C = "testFormC";
    protected final String TEST_FORM_D = "testFormD";
    protected final String TEST_FORM_E = "testFormE";
    
    @Autowired
    private AppService appService;
    @Autowired
    private PackageDefinitionDao packageDefinitionDao;
    @Autowired
    private WorkflowManager workflowManager;
    @Autowired
    private FormDataDao formDataDao;

    public TestAppService() {
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testDeployPackage() throws Exception {
        try {
            AppDefinition appDef = null;
            PackageDefinition packageDef = null;
            String packageXpdlStr = readFile(TEST_XPDL);
            byte[] packageXpdl = packageXpdlStr.getBytes();

            // existing delete package
            deleteXpdlPackage(TEST_APP_ID);

            // delete app
            deleteAppDefinition(TEST_APP_ID);

            // create wrong app definition
            String wrongAppId = TEST_APP_ID + TEST_APP_ID;
            appDef = createAppDefinition(wrongAppId, TEST_APP_VERSION);

            // check for package mismatch
            boolean packageMismatch = false;
            try {
                packageDef = appService.deployWorkflowPackage(wrongAppId, TEST_APP_VERSION.toString(), packageXpdl, true);
            } catch (UnsupportedOperationException e) {
                packageMismatch = true;
            } finally {
                // delete app
                deleteAppDefinition(wrongAppId);
            }
            assertTrue(packageMismatch);

            // create correct app definition
            appDef = createAppDefinition(TEST_APP_ID, TEST_APP_VERSION);

            // deploy package
            packageDef = appService.deployWorkflowPackage(TEST_APP_ID, TEST_APP_VERSION.toString(), packageXpdl, true);
            assertTrue(packageDef != null);

            // verify package versions
            String currentVersion = workflowManager.getCurrentPackageVersion(TEST_APP_ID);
            AppDefinition loadedApp = appService.loadAppDefinition(TEST_APP_ID, TEST_APP_VERSION.toString());
            PackageDefinition loadedPackage = loadedApp.getPackageDefinition();
            assertTrue(currentVersion.equals(loadedPackage.getVersion().toString()));

            // assign form mapping and save
            PackageActivityForm paf = new PackageActivityForm();
            paf.setPackageDefinition(packageDef);
            paf.setFormId(TEST_FORM_ID_1);
            paf.setProcessDefId(TEST_PROCESS_DEF_ID);
            paf.setActivityDefId(TEST_ACTIVITY_DEF_ID);
            paf.setAutoContinue(true);
            packageDef.addPackageActivityForm(paf);
            packageDefinitionDao.saveOrUpdate(packageDef);

            // redeploy package
            packageDef = appService.deployWorkflowPackage(TEST_APP_ID, TEST_APP_VERSION.toString(), packageXpdl, true);

            // verify new package version
            currentVersion = workflowManager.getCurrentPackageVersion(TEST_APP_ID);
            loadedApp = appService.loadAppDefinition(TEST_APP_ID, TEST_APP_VERSION.toString());
            loadedPackage = loadedApp.getPackageDefinition();
            assertTrue(currentVersion.equals(loadedPackage.getVersion().toString()));

            // verify updated form mapping
            PackageActivityForm loadedPaf = loadedPackage.getPackageActivityForm(TEST_PROCESS_DEF_ID, TEST_ACTIVITY_DEF_ID);
            assertTrue(loadedPaf != null && TEST_FORM_ID_1.equals(loadedPaf.getFormId()));

            // verify loading by process and activity definition
            packageDef = packageDefinitionDao.loadPackageDefinitionByProcess(loadedPackage.getId(), loadedPackage.getVersion(), TEST_PROCESS_DEF_ID);
            assertTrue(packageDef != null && currentVersion.equals(packageDef.getVersion().toString()));

            // verify auto continue
            boolean autoContinue = appService.isActivityAutoContinue(packageDef.getId(), currentVersion, TEST_PROCESS_DEF_ID, TEST_ACTIVITY_DEF_ID);
            assertTrue(autoContinue);

        } finally {

            // existing delete package
            deleteXpdlPackage(TEST_APP_ID);

            // delete app
            deleteAppDefinition(TEST_APP_ID);
        }
    }
    
    @Test
    @Transactional
    @Rollback(true)
    public void testImportExport() throws Exception {
        try {
            AppDefinition appDef = null;
            PackageDefinition packageDef = null;
            String packageXpdlStr = readFile(TEST_XPDL);
            byte[] packageXpdl = packageXpdlStr.getBytes();

            // existing delete package
            deleteXpdlPackage(TEST_APP_ID);

            // delete app
            deleteAppDefinition(TEST_APP_ID);

            // create correct app definition
            appDef = createAppDefinition(TEST_APP_ID, TEST_APP_VERSION);

            // create forms
            createFormDefinition(appDef, TEST_FORM_ID, TEST_FORM_ID, TEST_APP_VERSION);
            
            // deploy package
            packageDef = appService.deployWorkflowPackage(TEST_APP_ID, TEST_APP_VERSION.toString(), packageXpdl, true);
            assertTrue(packageDef != null);

            // assign form mapping and save
            PackageActivityForm paf = new PackageActivityForm();
            paf.setPackageDefinition(packageDef);
            paf.setFormId(TEST_FORM_ID_1);
            paf.setProcessDefId(TEST_PROCESS_DEF_ID);
            paf.setActivityDefId(TEST_ACTIVITY_DEF_ID);
            paf.setAutoContinue(true);
            packageDef.addPackageActivityForm(paf);
            packageDefinitionDao.saveOrUpdate(packageDef);

            // export app
            ByteArrayOutputStream contentToExport = new ByteArrayOutputStream();
            appService.exportApp(TEST_APP_ID, TEST_APP_VERSION.toString(), contentToExport);
            
            // import app
            byte[] contentToImport = contentToExport.toByteArray();
            AppDefinition importedApp = appService.importApp(contentToImport);
            
            // verify imported app version
            assertTrue(importedApp.getVersion() == TEST_APP_VERSION + 1);
            
            // verify form
            Collection<FormDefinition> formDefList = importedApp.getFormDefinitionList();
            FormDefinition importedFormDef = formDefList.iterator().next();
            assertTrue(TEST_FORM_ID.equals(importedFormDef.getId()));
            
            // verify package version
            String currentVersion = workflowManager.getCurrentPackageVersion(TEST_APP_ID);
            PackageDefinition importedPackage = importedApp.getPackageDefinition();
            assertTrue(importedPackage != null && currentVersion.equals(importedPackage.getVersion().toString()));

            // verify updated form mapping
            PackageActivityForm loadedPaf = importedPackage.getPackageActivityForm(TEST_PROCESS_DEF_ID, TEST_ACTIVITY_DEF_ID);
            assertTrue(loadedPaf != null && TEST_FORM_ID_1.equals(loadedPaf.getFormId()));

        } finally {

            // existing delete package
            deleteXpdlPackage(TEST_APP_ID);

            // delete app
            deleteAppDefinition(TEST_APP_ID);
        }
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testRunProcess() throws Exception {
        String processId = null;
        
        try {
            AppDefinition appDef = null;
            PackageDefinition packageDef = null;
            byte[] packageXpdl = readFile(TEST_XPDL).getBytes();

            // existing delete package
            deleteXpdlPackage(TEST_APP_ID);

            // delete app
            deleteAppDefinition(TEST_APP_ID);

            // create app definition
            appDef = createAppDefinition(TEST_APP_ID, TEST_APP_VERSION);

            // deploy package
            packageDef = appService.deployWorkflowPackage(TEST_APP_ID, TEST_APP_VERSION.toString(), packageXpdl, true);
            assertTrue(packageDef != null);

            // verify package versions
            String currentVersion = workflowManager.getCurrentPackageVersion(TEST_APP_ID);
            AppDefinition loadedApp = appService.loadAppDefinition(TEST_APP_ID, TEST_APP_VERSION.toString());
            PackageDefinition loadedPackage = loadedApp.getPackageDefinition();
            assertTrue(currentVersion.equals(loadedPackage.getVersion().toString()));

            // create forms
            createFormDefinition(appDef, TEST_FORM_ID, TEST_FORM_ID, TEST_APP_VERSION);
            createFormDefinition(appDef, TEST_FORM_ID_1, TEST_FORM_ID_1, TEST_APP_VERSION);
            createFormDefinition(appDef, TEST_FORM_ID_2, TEST_FORM_ID_2, TEST_APP_VERSION);

            // assign form mappings and save
            PackageActivityForm paf = new PackageActivityForm();
            paf.setPackageDefinition(packageDef);
            paf.setFormId(TEST_FORM_ID);
            paf.setProcessDefId(TEST_PROCESS_DEF_ID);
            paf.setActivityDefId(WorkflowUtil.ACTIVITY_DEF_ID_RUN_PROCESS);
            paf.setAutoContinue(true);
            packageDefinitionDao.addAppActivityForm(TEST_APP_ID, TEST_APP_VERSION, paf);
            paf = new PackageActivityForm();
            paf.setPackageDefinition(packageDef);
            paf.setFormId(TEST_FORM_ID_1);
            paf.setProcessDefId(TEST_PROCESS_DEF_ID);
            paf.setActivityDefId(TEST_ACTIVITY_DEF_ID);
            packageDefinitionDao.addAppActivityForm(TEST_APP_ID, TEST_APP_VERSION, paf);
            paf = new PackageActivityForm();
            paf.setPackageDefinition(packageDef);
            paf.setFormId(TEST_FORM_ID_2);
            paf.setProcessDefId(TEST_PROCESS_DEF_ID);
            paf.setActivityDefId(TEST_ACTIVITY_DEF_ID_2);
            packageDefinitionDao.addAppActivityForm(TEST_APP_ID, TEST_APP_VERSION, paf);

            // get runProcess start form
            PackageActivityForm startForm = appService.viewStartProcessForm(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_PROCESS_DEF_ID, null, null);
            assertTrue(TEST_FORM_ID.equals(startForm.getFormId()));

            // start process
            FormData data = new FormData();
            data.addRequestParameterValues(AssignmentCompleteButton.DEFAULT_ID, new String[]{AssignmentCompleteButton.DEFAULT_ID});
            
            WorkflowProcessResult result = appService.submitFormToStartProcess(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_PROCESS_DEF_ID, data, null, null, null);
            processId = result.getProcess().getInstanceId();
            Collection<WorkflowActivity> activityList = result.getActivities();
            assertTrue(activityList != null && activityList.size() == 1);

            // get first activity form
            WorkflowActivity firstActivity = activityList.iterator().next();
            String firstActivityId = firstActivity.getId();
            PackageActivityForm firstForm = appService.viewAssignmentForm(TEST_APP_ID, TEST_APP_VERSION.toString(), firstActivityId, null, null);
            assertTrue(TEST_FORM_ID_1.equals(firstForm.getFormId()));

            // complete first task
            FormData formResult = appService.completeAssignmentForm(TEST_APP_ID, TEST_APP_VERSION.toString(), firstActivityId, null, null);
            assertTrue(formResult.getFormErrors().isEmpty());

            // get assignments
            WorkflowAssignment assignment = workflowManager.getAssignmentByProcess(processId);
            String secondActivityId = assignment.getActivityId();
            PackageActivityForm secondForm = appService.viewAssignmentForm(TEST_APP_ID, TEST_APP_VERSION.toString(), secondActivityId, null, null);
            assertTrue(TEST_FORM_ID_2.equals(secondForm.getFormId()));

            // complete second task
            formResult = appService.completeAssignmentForm(TEST_APP_ID, TEST_APP_VERSION.toString(), secondActivityId, null, null);
            assertTrue(formResult.getFormErrors().isEmpty());

            // complete third task
            assignment = workflowManager.getAssignmentByProcess(processId);
            String thirdActivityId = assignment.getActivityId();
            formResult = appService.completeAssignmentForm(TEST_APP_ID, TEST_APP_VERSION.toString(), thirdActivityId, null, null);
            assertTrue(formResult.getFormErrors().isEmpty());

        } finally {
            // existing delete package
            deleteXpdlPackage(TEST_APP_ID);

            // delete app
            deleteAppDefinition(TEST_APP_ID);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Rollback(false)
    public void testFormData() throws Exception {
        try {
            AppDefinition appDef = null;

            // delete app
            deleteAppDefinition(TEST_APP_ID);

            // create app definition
            appDef = createAppDefinition(TEST_APP_ID, TEST_APP_VERSION);

            // create forms
            createFormDefinition(appDef, TEST_FORM_ID, TEST_FORM_ID, TEST_APP_VERSION);

            // store single row data
            String id = "row1";
            String name = "name";
            String state = "state";
            FormRow row = new FormRow();
            row.setId(id);
            row.setProperty("name", name);
            row.setProperty("state", state);
            FormRowSet rowSet = new FormRowSet();
            rowSet.add(row);
            appService.storeFormData(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_ID, rowSet, null);

            // load single row data
            FormRowSet loadedRowSet = appService.loadFormData(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_ID, id);
            FormRow loadedRow = loadedRowSet.get(0);
            assertTrue(name.equals(loadedRow.getProperty("name")));
            
            // store updated row data using request
            String updatedName = "updated name";
            String updatedState = "updated state";
            FormData formData = new FormData();
            formData.setPrimaryKeyValue(id);
            formData.addRequestParameterValues("name", new String[] { updatedName });
            formData.addRequestParameterValues("state", new String[] { updatedState });
            appService.submitForm(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_ID, formData, false);

            // load updated row data
            loadedRowSet = appService.loadFormData(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_ID, id);
            loadedRow = loadedRowSet.get(0);
            assertTrue(updatedName.equals(loadedRow.getProperty("name")) && updatedState.equals(loadedRow.getProperty("state")));
        } finally {
            // delete form data
            formDataDao.delete(TEST_FORM_ID, TEST_FORM_ID, new String[]{"row1"});

            // delete app
            deleteAppDefinition(TEST_APP_ID);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Rollback(false)
    public void testSubForms() throws Exception {
        String aId = null;
        String cId = null;
        String dId = null;
        String valueE1 = "e001";
        String valueE2 = "e002";
        try {
            AppDefinition appDef = null;

            // delete app
            deleteAppDefinition(TEST_APP_ID);

            // create app definition
            appDef = createAppDefinition(TEST_APP_ID, TEST_APP_VERSION);

            // create forms - Form D contains Form C, which contains Form B, which contains Form A. Each form points to different tables, except Form B which points to the same table as Form C.
            createFormDefinition(appDef, TEST_FORM_E, TEST_FORM_E, TEST_APP_VERSION);
            createFormDefinition(appDef, TEST_FORM_A, TEST_FORM_A, TEST_APP_VERSION);
            createFormDefinition(appDef, TEST_FORM_B, TEST_FORM_C, TEST_APP_VERSION);
            createFormDefinition(appDef, TEST_FORM_C, TEST_FORM_C, TEST_APP_VERSION);
            createFormDefinition(appDef, TEST_FORM_D, TEST_FORM_D, TEST_APP_VERSION);

            // add values for reference form E
            FormData dataE = new FormData();
            dataE.addRequestParameterValues("id", new String[]{valueE1});
            dataE.addRequestParameterValues("e1", new String[]{valueE1});
            dataE.addRequestParameterValues("e2", new String[]{valueE1});
            dataE.addRequestParameterValues("e3", new String[]{valueE1});
            appService.submitForm(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_E, dataE, true);
            FormData dataE2 = new FormData();
            dataE2.addRequestParameterValues("id", new String[]{valueE2});
            dataE2.addRequestParameterValues("e1", new String[]{valueE2});
            dataE2.addRequestParameterValues("e2", new String[]{valueE2});
            dataE2.addRequestParameterValues("e3", new String[]{valueE2});
            appService.submitForm(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_E, dataE2, true);

            // create data to store
            FormData data = new FormData();
            
            // add values for root form D
            String valueD = "d001";
            data.addRequestParameterValues("id", new String[]{valueD});
            data.addRequestParameterValues("d1", new String[]{valueD});
            data.addRequestParameterValues("d2", new String[]{valueD});

            // add values for subform C
            String valueC = "c001";
            data.addRequestParameterValues("formC_testFormC_c1", new String[]{valueC});
            data.addRequestParameterValues("formC_testFormC_c2", new String[]{valueC});
            data.addRequestParameterValues("formC_testFormC_eref", new String[]{valueE1, valueE2});

            // add values for subform B
            String valueB = "b001";
            data.addRequestParameterValues("formC_testFormC_formB_testFormB_b1", new String[]{valueB});
            data.addRequestParameterValues("formC_testFormC_formB_testFormB_b2", new String[]{valueB});

            // add values for subform A
            String valueA = "a001";
            data.addRequestParameterValues("formC_testFormC_formB_testFormB_formA_testFormA_id", new String[]{""}); // empty ID, to be automatically generated
            data.addRequestParameterValues("formC_testFormC_formB_testFormB_formA_testFormA_a1", new String[]{valueA});
            data.addRequestParameterValues("formC_testFormC_formB_testFormB_formA_testFormA_a2", new String[]{valueA});

            // submit form
            appService.submitForm(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_D, data, true);

            // load and verify data for form D
            FormRowSet rowSetD = appService.loadFormData(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_D, valueD);
            FormRow rowD = rowSetD.iterator().next();
            boolean verifyD = valueD.equals(rowD.get("id")) && valueD.equals(rowD.get("d1")) && valueD.equals(rowD.get("d2"));
            dId = rowD.get("id").toString();
                
            // load and verify data for form C
            String generatedIdForC = (String) rowD.get("cid");
            FormRowSet rowSetC = appService.loadFormData(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_C, generatedIdForC);
            FormRow rowC = rowSetC.iterator().next();
            boolean verifyC = generatedIdForC.equals(rowC.get("id")) && valueC.equals(rowC.get("c1")) && generatedIdForC.equals(rowD.get("cid"));
            cId = rowC.get("id").toString();
            
            // load and verify data for form A
            String generatedIdForA = (String) rowC.get("aid");
            FormRowSet rowSetA = appService.loadFormData(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_A, generatedIdForA);
            FormRow rowA = rowSetA.iterator().next();
            boolean verifyA = generatedIdForA.equals(rowA.get("id")) && valueA.equals(rowA.get("a1")) && generatedIdForC.equals(rowA.get("bid"));
            aId = rowA.get("id").toString();
            
            // verify form data
            assertTrue(verifyA && verifyC && verifyD);
             
            // load using form data util
            boolean includeSubformData = true;
            boolean includeReferenceElements = true;
            boolean flatten = false;
            Map<String, Object> result = FormUtil.loadFormData(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_D, dId, includeSubformData, includeReferenceElements, flatten, null);
            String resultOutput = result.toString();        
            LogUtil.info(FormUtil.class.getName(), "Form Data Output: " + resultOutput);
            String resultJson = FormUtil.loadFormDataJson(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_D, dId, includeSubformData, includeReferenceElements, flatten, null);
            LogUtil.info(FormUtil.class.getName(), "Form Data JSON: " + resultJson);
            
            // verify form data util output
            String resultValueD = (String)result.get("d1");
            String resultValueC = (String)((Map<String,Object>)result.get("formC")).get("c1");
            Collection<Map<String, Object>> resultE = (Collection<Map<String, Object>>)((Map<String,Object>)result.get("formC")).get("eref");
            String resultValueE1 = (String)resultE.iterator().next().get("e1");
            boolean verifyFormData = 
                    resultValueD.equals(valueD) &&
                    resultValueC.equals(valueC) &&
                    resultValueE1.equals(valueE1);
            Assert.assertTrue(verifyFormData);
            
        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            try {
                // delete form data
                formDataDao.delete(TEST_FORM_A, TEST_FORM_A, new String[]{aId});
                formDataDao.delete(TEST_FORM_C, TEST_FORM_C, new String[]{cId});
                formDataDao.delete(TEST_FORM_D, TEST_FORM_D, new String[]{dId});
                formDataDao.delete(TEST_FORM_E, TEST_FORM_E, new String[]{valueE1});
                formDataDao.delete(TEST_FORM_E, TEST_FORM_E, new String[]{valueE2});
            
            } catch(Exception e) {
                e.printStackTrace();
            }
            
            // delete app
            deleteAppDefinition(TEST_APP_ID);
        }
    }

    protected String readFile(String filePath) throws IOException {
        // deploy package
        BufferedReader reader = null;
        String fileContents = "";
        String line;
        try {
            InputStream in = getClass().getResourceAsStream(filePath);
            if (in != null) {
                reader = new BufferedReader(new InputStreamReader(in));
                while ((line = reader.readLine()) != null) {
                    fileContents += line + "\n";
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return fileContents;
    }

    protected void deleteXpdlPackage(String packageId) {
        workflowManager.processDeleteAndUnload(packageId);
    }

    protected AppDefinition createAppDefinition(String id, Long version) {
        // create test app
        AppDefinition appDef = new AppDefinition();
        appDef.setId(id);
        appDef.setVersion(version);
        appDef.setName(id);

        // save test app
        appService.createAppDefinition(appDef);

        return appDef;
    }

    protected AppDefinition loadAppDefinitionVersion(String id, Long version) {
        AppDefinition appDef = appService.loadAppDefinition(id, version.toString());
        return appDef;
    }

    protected AppDefinition loadAppDefinition(String id) {
        AppDefinition appDef = appService.loadAppDefinition(id, null);
        return appDef;
    }

    protected void deleteAppDefinition(String id) {
        appService.deleteAllAppDefinitionVersions(id);
    }

    protected FormDefinition createFormDefinition(AppDefinition appDef, String formId, String tableName, Long formVersion) throws IOException {
        // create test form
        FormDefinition formDef = new FormDefinition();
        formDef.setId(formId);
        formDef.setAppDefinition(appDef);
        formDef.setName(formId);
        formDef.setTableName(tableName);
        String jsonFileName = "/" + formId + ".json";
        String formJson = readFile(jsonFileName);
        if (formJson == null || formJson.trim().isEmpty()) {
            formJson = "{ \"className\":\"org.joget.apps.form.model.Form\" }";
        }
        formDef.setJson(formJson);

        // save test form
        appService.createFormDefinition(appDef, formDef);

        return formDef;
    }
}
