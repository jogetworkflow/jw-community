package org.joget.apps.app.model;

import java.util.Collection;
import java.util.Map;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.dao.PackageDefinitionDao;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:appsApplicationContext.xml"})
public class TestAppDao {

    protected final String TEST_APP_ID = "awf_testApp";
    protected final Long TEST_APP_VERSION = new Long(1);
    protected final String TEST_FORM_ID = "awf_testForm";
    protected final String TEST_PACKAGE_ID = "awf_testPackage";
    protected final Long TEST_PACKAGE_VERSION = new Long(1);
    protected final String TEST_PROCESS_DEF_ID = "awf_testProcess";
    protected final String TEST_ACTIVITY_DEF_ID = "awf_testActivity";
    @Autowired
    private AppDefinitionDao appDefinitionDao;
    @Autowired
    private FormDefinitionDao formDefinitionDao;
    @Autowired
    private PackageDefinitionDao packageDefinitionDao;

    public TestAppDao() {
    }

    @Test
    @Transactional
    public void testCreateAppDefinition() {
        try {
            // create app definition
            AppDefinition appDef = createAppDefinition(TEST_APP_ID, TEST_APP_VERSION);

            // load and compare
            AppDefinition loadedDef = loadAppDefinitionVersion(TEST_APP_ID, TEST_APP_VERSION);
            assertTrue(appDef.getUid().equals(loadedDef.getUid()));

            // search
            Collection<AppDefinition> list = listAppDefinitions(TEST_APP_ID);
            assertTrue(list.size() > 0);

        } finally {
        }
    }

    @Test
    @Transactional
    public void testCreateFormDefinition() {
        try {
            // create app definition
            AppDefinition appDef = createAppDefinition(TEST_APP_ID, TEST_APP_VERSION);

            // create form definition
            FormDefinition formDef = createFormDefinition(TEST_FORM_ID, appDef);

            // load and compare
            FormDefinition loadedDef = loadFormDefinitionVersion(TEST_FORM_ID, appDef);
            assertTrue(formDef.getId().equals(loadedDef.getId()));

            // compare AppDefinition
            AppDefinition formAppDef = loadedDef.getAppDefinition();
            assertTrue(formAppDef.getUid().equals(appDef.getUid()));


        } finally {
        }
    }

    @Test
    @Transactional
    public void testAppVersions() {
        try {
            // create app versions
            AppDefinition appDef1 = createAppDefinition(TEST_APP_ID, TEST_APP_VERSION);
            AppDefinition appDef2 = createAppDefinition(TEST_APP_ID, TEST_APP_VERSION + 1);
            AppDefinition appDef3 = createAppDefinition(TEST_APP_ID, TEST_APP_VERSION + 2);

            // list latest version
            Collection<AppDefinition> list = listAppDefinitions(TEST_APP_ID);
            assertTrue(list.size() == 1);
            AppDefinition loadedDef = list.iterator().next();
            assertTrue(appDef3.getVersion().equals(loadedDef.getVersion()));

            // check version count
            Long versionCount = countAppDefinitionVersions(TEST_APP_ID);
            assertTrue(versionCount == 3);

        } finally {
        }
    }

    @Test
    @Transactional
    public void testPackageActivityFormMapping() {
        try {
            // create app definition
            AppDefinition appDef = createAppDefinition(TEST_APP_ID, TEST_APP_VERSION);

            // create form definition
            FormDefinition formDef = createFormDefinition(TEST_FORM_ID, appDef);

            // create package definition
            PackageDefinition packageDef = createPackageDefinition(appDef, TEST_PACKAGE_ID, TEST_PACKAGE_VERSION);

            // assign form and save
            PackageActivityForm paf = new PackageActivityForm();
            paf.setPackageDefinition(packageDef);
            paf.setFormId(TEST_PACKAGE_ID);
            paf.setProcessDefId(TEST_PROCESS_DEF_ID);
            paf.setActivityDefId(TEST_ACTIVITY_DEF_ID);
            packageDef.addPackageActivityForm(paf);
            updatePackageDefinition(packageDef);

            // load and verify
            AppDefinition loadedAppDef = loadAppDefinitionVersion(TEST_APP_ID, TEST_APP_VERSION);
            PackageDefinition loadedPackageDef = loadedAppDef.getPackageDefinition();
            Map<String, PackageActivityForm> activityFormMap = loadedPackageDef.getPackageActivityFormMap();
            String uid = paf.getUid();
            PackageActivityForm loadedPaf = activityFormMap.get(uid);
            assertTrue(paf.getFormId().equals(loadedPaf.getFormId()));

            // update form
            loadedPaf.setPackageDefinition(packageDef);
            loadedPaf.setFormId(TEST_FORM_ID);
            loadedPaf.setProcessDefId(TEST_PROCESS_DEF_ID);
            loadedPaf.setActivityDefId(TEST_ACTIVITY_DEF_ID);
            packageDef.addPackageActivityForm(loadedPaf);
            updatePackageDefinition(packageDef);

            // reload and verify
            loadedPackageDef = loadPackageDefinition(TEST_PACKAGE_ID);
            activityFormMap = loadedPackageDef.getPackageActivityFormMap();
            uid = paf.getUid();
            loadedPaf = activityFormMap.get(uid);
            assertTrue(TEST_FORM_ID.equals(loadedPaf.getFormId()));

            // remove mapping
            loadedPackageDef = loadPackageDefinition(TEST_PACKAGE_ID);
            loadedPackageDef.removePackageActivityForm(TEST_PROCESS_DEF_ID, TEST_ACTIVITY_DEF_ID);
            updatePackageDefinition(loadedPackageDef);
            loadedPackageDef = loadPackageDefinition(TEST_PACKAGE_ID);
            activityFormMap = loadedPackageDef.getPackageActivityFormMap();
            assertTrue(activityFormMap.isEmpty());

        } finally {
        }
    }

    @Test
    @Transactional
    public void deleteAppDefinition() {
        testPackageActivityFormMapping();

        // delete app
        deleteAppDefinition(TEST_APP_ID);

        // verify forms deleted
        AppDefinition appDef = new AppDefinition();
        appDef.setId(TEST_APP_ID);
        appDef.setVersion(TEST_APP_VERSION);
        FormDefinition loadedDef = loadFormDefinitionVersion(TEST_FORM_ID, appDef);
        assertTrue(loadedDef == null);

        // verify package deleted
        PackageDefinition loadedPackageDef = loadPackageDefinition(TEST_PACKAGE_ID);
        assertTrue(loadedPackageDef == null);
    }

    protected AppDefinition createAppDefinition(String id, Long version) {
        // create test app
        AppDefinition appDef = new AppDefinition();
        appDef.setId(id);
        appDef.setVersion(version);
        appDef.setName(id);

        // save test app
        appDefinitionDao.saveOrUpdate(appDef);

        return appDef;
    }

    protected AppDefinition loadAppDefinitionVersion(String id, Long version) {
        AppDefinition appDef = appDefinitionDao.loadVersion(id, version);
        return appDef;
    }

    protected AppDefinition loadAppDefinition(String id) {
        AppDefinition appDef = appDefinitionDao.loadById(id);
        return appDef;
    }

    protected Long countAppDefinitionVersions(String id) {
        Long count = appDefinitionDao.countVersions(id);
        return count;
    }

    protected Collection<AppDefinition> listAppDefinitions(String id) {
        // get substring to search for
        String nameFilter = id.substring(0, id.length() - 2);

        // query dao
        Collection<AppDefinition> resultList = appDefinitionDao.findLatestVersions(id, null, nameFilter, null, null, null, null);
        return resultList;
    }

    protected Long countAppDefinitions(String id) {
        // get substring to search for
        String nameFilter = id.substring(0, id.length() - 2);

        // query dao
        Long result = appDefinitionDao.countLatestVersions(id, null, nameFilter);
        return result;
    }

    protected void deleteAppDefinition(String id) {
        appDefinitionDao.deleteAllVersions(id);
    }

    protected FormDefinition createFormDefinition(String formId, AppDefinition appDef) {
        // create test form
        FormDefinition formDef = new FormDefinition();
        formDef.setId(formId);
        formDef.setAppDefinition(appDef);
        formDef.setName(formId);

        // save test form
        formDefinitionDao.add(formDef);

        return formDef;
    }

    protected FormDefinition loadFormDefinitionVersion(String id, AppDefinition appDef) {
        FormDefinition formDef = formDefinitionDao.loadById(id, appDef);
        return formDef;
    }

    protected Long countFormDefinition(AppDefinition appDef) {
        Long count = formDefinitionDao.getFormDefinitionListCount(null, appDef);
        return count;
    }

    protected void deleteFormDefinition(String id, AppDefinition appDef) {
        formDefinitionDao.delete(id, appDef);
    }

    protected PackageDefinition createPackageDefinition(AppDefinition appDef, String packageId, Long packageVersion) {
        // create test package
        PackageDefinition packageDef = new PackageDefinition();
        packageDef.setId(packageId);
        packageDef.setVersion(packageVersion);
        packageDef.setName(packageId);
        packageDef.setAppDefinition(appDef);

        // save test package
        packageDefinitionDao.saveOrUpdate(packageDef);

        return packageDef;
    }

    protected PackageDefinition updatePackageDefinition(PackageDefinition packageDef) {
        // update test package
        packageDefinitionDao.saveOrUpdate(packageDef);
        return packageDef;
    }

    protected PackageDefinition loadPackageDefinition(String id) {
        PackageDefinition packageDef = packageDefinitionDao.loadById(id);
        return packageDef;
    }

    protected void deletePackageDefinition(String packageId) {
        packageDefinitionDao.deleteAllVersions(packageId);
    }
}
