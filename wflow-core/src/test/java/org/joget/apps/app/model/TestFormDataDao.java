package org.joget.apps.app.model;

import java.io.IOException;
import java.util.Collection;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.FormColumnCache;
import org.joget.apps.form.service.CustomFormDataTableUtil;
import org.joget.apps.form.service.FormService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class TestFormDataDao {
    
    @Autowired
    private FormDataDao formDataDao;
    
    @Autowired
    private FormService formService;
    
    @Autowired
    private AppService appService;
    
    @Autowired
    private FormDefinitionDao formDefinitionDao;
    
    @Autowired
    private AppDefinitionDao appDefinitionDao;
    
    @Test
    public void testGetFormDefinitionColumnNames() throws IOException {
        String tableName = "sametable";
        
        TestUtil.deleteAllVersions("testFormDefinition");
        TestUtil.deleteAllVersions("testFormDefinition2");
        
        try {
            FormColumnCache cache = (FormColumnCache) AppUtil.getApplicationContext().getBean("formColumnCache");
            cache.remove(tableName);
            Assert.assertNull(cache.get(tableName));

            AppDefinition appDef = TestUtil.createAppDefinition("testFormDefinition", 1l);
            TestUtil.createFormDefinition(appDef, "sameTable1", "sametable", "sameTable1");
            TestUtil.createFormDefinition(appDef, "sameTable2", "sametable", "sameTable2");
            TestUtil.createFormDefinition(appDef, "sameTable3", "sametable", "sameTable3");

            Collection<String> columns = formDataDao.getFormDefinitionColumnNames(tableName);
            while (columns == null) {
                columns = formDataDao.getFormDefinitionColumnNames(tableName);
            }
            Assert.assertEquals(11, columns.size());
            Assert.assertTrue(columns.contains("field1"));
            Assert.assertTrue(columns.contains("field2"));
            Assert.assertTrue(columns.contains("field3"));
            Assert.assertTrue(columns.contains("field4"));
            Assert.assertTrue(columns.contains("field5"));
            Assert.assertTrue(columns.contains("field6"));
            Assert.assertTrue(columns.contains("custom_field1"));
            Assert.assertTrue(columns.contains("custom_field2"));
            Assert.assertTrue(columns.contains("custom_field3"));
            Assert.assertTrue(columns.contains("custom_field4"));
            Assert.assertTrue(columns.contains("custom_field5"));

            cache.remove(tableName);
            Assert.assertNull(cache.get(tableName));

            AppDefinition appDef2 = appService.createNewAppDefinitionVersion("testFormDefinition", 1l);
            FormDefinition formDef = formDefinitionDao.loadById("sameTable3", appDef2);
            formDef.setJson(TestUtil.readFile("/forms/sameTable3a.json"));
            formDefinitionDao.update(formDef);

            columns = formDataDao.getFormDefinitionColumnNames(tableName);
            while (columns == null) {
                columns = formDataDao.getFormDefinitionColumnNames(tableName);
            }
            Assert.assertEquals(12, columns.size());
            Assert.assertTrue(columns.contains("field1"));
            Assert.assertTrue(columns.contains("field2"));
            Assert.assertTrue(columns.contains("field3"));
            Assert.assertTrue(columns.contains("field4"));
            Assert.assertTrue(columns.contains("field5"));
            Assert.assertTrue(columns.contains("fielD6")); //here is different lettercase
            Assert.assertTrue(columns.contains("field7"));
            Assert.assertTrue(columns.contains("custom_field1"));
            Assert.assertTrue(columns.contains("custom_field2"));
            Assert.assertTrue(columns.contains("custom_field3"));
            Assert.assertTrue(columns.contains("custom_field4"));
            Assert.assertTrue(columns.contains("custom_field5"));

            cache.remove(tableName);
            Assert.assertNull(cache.get(tableName));

            AppDefinition appDef3 = TestUtil.createAppDefinition("testFormDefinition2", 1l);
            TestUtil.createFormDefinition(appDef3, "sameTable4", "sametable", "sameTable4");

            columns = formDataDao.getFormDefinitionColumnNames(tableName);
            while (columns == null) {
                columns = formDataDao.getFormDefinitionColumnNames(tableName);
            }
            Assert.assertEquals(13, columns.size());
            Assert.assertTrue(columns.contains("field1"));
            Assert.assertTrue(columns.contains("field2"));
            Assert.assertTrue(columns.contains("field3"));
            Assert.assertTrue(columns.contains("field4"));
            Assert.assertTrue(columns.contains("field5"));
            Assert.assertTrue(columns.contains("fielD6")); //here is different lettercase
            Assert.assertTrue(columns.contains("field7"));
            Assert.assertTrue(columns.contains("custom_field1"));
            Assert.assertTrue(columns.contains("custom_field2"));
            Assert.assertTrue(columns.contains("custom_field3"));
            Assert.assertTrue(columns.contains("custom_field4"));
            Assert.assertTrue(columns.contains("custom_field5"));
            Assert.assertTrue(columns.contains("2field"));

            cache.remove(tableName);
            Assert.assertNull(cache.get(tableName));

            CustomFormDataTableUtil.createTable(appDef2, tableName, new String[]{"ctField1", "ctField2"});

            AppUtil.setCurrentAppDefinition(appDef2);
            columns = formDataDao.getFormDefinitionColumnNames(tableName);
            while (columns == null) {
                columns = formDataDao.getFormDefinitionColumnNames(tableName);
            }
            Assert.assertEquals(15, columns.size());
            Assert.assertTrue(columns.contains("field1"));
            Assert.assertTrue(columns.contains("field2"));
            Assert.assertTrue(columns.contains("field3"));
            Assert.assertTrue(columns.contains("field4"));
            Assert.assertTrue(columns.contains("field5"));
            Assert.assertTrue(columns.contains("fielD6")); //here is different lettercase
            Assert.assertTrue(columns.contains("field7"));
            Assert.assertTrue(columns.contains("custom_field1"));
            Assert.assertTrue(columns.contains("custom_field2"));
            Assert.assertTrue(columns.contains("custom_field3"));
            Assert.assertTrue(columns.contains("custom_field4"));
            Assert.assertTrue(columns.contains("custom_field5"));
            Assert.assertTrue(columns.contains("2field"));
            Assert.assertTrue(columns.contains("ctField1"));
            Assert.assertTrue(columns.contains("ctField2"));
        } finally {
            TestUtil.deleteAllVersions("testFormDefinition");
            TestUtil.deleteAllVersions("testFormDefinition2");
        }
    }
}
