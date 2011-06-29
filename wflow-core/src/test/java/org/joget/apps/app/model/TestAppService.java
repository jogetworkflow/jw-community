package org.joget.apps.app.model;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.joget.apps.app.dao.PackageDefinitionDao;
import org.joget.apps.app.service.AppService;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
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
    @Autowired
    private AppService appService;
    @Autowired
    private PackageDefinitionDao packageDefinitionDao;
    @Autowired
    private WorkflowManager workflowManager;

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
            AppDefinition loadedApp = appService.getAppDefinition(TEST_APP_ID, TEST_APP_VERSION.toString());
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
            loadedApp = appService.getAppDefinition(TEST_APP_ID, TEST_APP_VERSION.toString());
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
            createFormDefinition(appDef, TEST_FORM_ID, TEST_APP_VERSION);
            
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
            AppDefinition loadedApp = appService.getAppDefinition(TEST_APP_ID, TEST_APP_VERSION.toString());
            PackageDefinition loadedPackage = loadedApp.getPackageDefinition();
            assertTrue(currentVersion.equals(loadedPackage.getVersion().toString()));

            // create forms
            createFormDefinition(appDef, TEST_FORM_ID, TEST_APP_VERSION);
            createFormDefinition(appDef, TEST_FORM_ID_1, TEST_APP_VERSION);
            createFormDefinition(appDef, TEST_FORM_ID_2, TEST_APP_VERSION);

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
            WorkflowProcessResult result = appService.submitFormToStartProcess(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_PROCESS_DEF_ID, null, null, null, null);
            String processId = result.getProcess().getInstanceId();
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
    @Transactional
    @Rollback(true)
    public void testFormData() throws Exception {
        try {
            AppDefinition appDef = null;

            // delete app
            deleteAppDefinition(TEST_APP_ID);

            // create app definition
            appDef = createAppDefinition(TEST_APP_ID, TEST_APP_VERSION);

            // create forms
            createFormDefinition(appDef, TEST_FORM_ID, TEST_APP_VERSION);

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

        } finally {

            // delete app
            deleteAppDefinition(TEST_APP_ID);
        }
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testSubForms() throws Exception {
        try {
            AppDefinition appDef = null;

            // delete app
            deleteAppDefinition(TEST_APP_ID);

            // create app definition
            appDef = createAppDefinition(TEST_APP_ID, TEST_APP_VERSION);

            // create forms - Form D contains Form C, which contains Form B, which contains Form A
            createFormDefinition(appDef, TEST_FORM_A, TEST_APP_VERSION);
            createFormDefinition(appDef, TEST_FORM_B, TEST_APP_VERSION);
            createFormDefinition(appDef, TEST_FORM_C, TEST_APP_VERSION);
            createFormDefinition(appDef, TEST_FORM_D, TEST_APP_VERSION);

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

            // add values for subform B
            String valueB = "b001";
            data.addRequestParameterValues("formC_testFormC_formB_testFormB_id", new String[]{valueB});
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

            // load and verify data for form C
            String generatedIdForC = (String) rowD.get("cid");
            FormRowSet rowSetC = appService.loadFormData(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_C, generatedIdForC);
            FormRow rowC = rowSetC.iterator().next();
            boolean verifyC = generatedIdForC.equals(rowC.get("id")) && valueC.equals(rowC.get("c1")) && valueB.equals(rowC.get("bid")) && generatedIdForC.equals(rowD.get("cid"));

            // load and verify values for form B
            FormRowSet rowSetB = appService.loadFormData(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_B, valueB);
            FormRow rowB = rowSetB.iterator().next();
            boolean verifyB = valueB.equals(rowB.get("id")) && valueB.equals(rowB.get("b1")) && generatedIdForC.equals(rowB.get("cid"));

            // load and verify data for form A
            String generatedIdForA = (String) rowB.get("aid");
            FormRowSet rowSetA = appService.loadFormData(TEST_APP_ID, TEST_APP_VERSION.toString(), TEST_FORM_A, generatedIdForA);
            FormRow rowA = rowSetA.iterator().next();
            boolean verifyA = generatedIdForA.equals(rowA.get("id")) && valueA.equals(rowA.get("a1")) && valueB.equals(rowA.get("bid"));

            assertTrue(verifyA && verifyB && verifyC && verifyD);

        } finally {

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
        AppDefinition appDef = appService.getAppDefinition(id, version.toString());
        return appDef;
    }

    protected AppDefinition loadAppDefinition(String id) {
        AppDefinition appDef = appService.getAppDefinition(id, null);
        return appDef;
    }

    protected void deleteAppDefinition(String id) {
        appService.deleteAllAppDefinitionVersions(id);
    }

    protected FormDefinition createFormDefinition(AppDefinition appDef, String formId, Long formVersion) throws IOException {
        // create test form
        FormDefinition formDef = new FormDefinition();
        formDef.setId(formId);
        formDef.setAppDefinition(appDef);
        formDef.setName(formId);
        formDef.setTableName(formId);
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
