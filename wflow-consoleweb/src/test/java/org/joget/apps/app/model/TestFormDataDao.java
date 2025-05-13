package org.joget.apps.app.model;

import java.io.IOException;
import java.util.Collection;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.FormColumnCache;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
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
    
    @Test
    public void testPartiallyStoreData() throws IOException {
        String tableName = "sametable";
        
        TestUtil.deleteAllVersions("testFormDefinition");
        
        try {
            FormColumnCache cache = (FormColumnCache) AppUtil.getApplicationContext().getBean("formColumnCache");
            cache.remove(tableName);
            Assert.assertNull(cache.get(tableName));

            AppDefinition appDef = TestUtil.createAppDefinition("testFormDefinition", 1l);
            TestUtil.createFormDefinition(appDef, "sameTable1", tableName, "sameTable1");
            TestUtil.createFormDefinition(appDef, "sameTable2", tableName, "sameTable2");
            TestUtil.createFormDefinition(appDef, "sameTable3", tableName, "sameTable3");

            String id = "dummy_record_id_for_testing";
            String formDefId = "sameTable1";
            formDataDao.delete(formDefId, tableName, new String[]{id});
            
            //make sure record is not exist
            FormRow row = formDataDao.load(formDefId, tableName, id);
            Assert.assertNull(row);
            
            //create a record
            FormRowSet rows = new FormRowSet();
            row = new FormRow();
            row.setId(id);
            row.setProperty("field1", "field1");
            row.setProperty("field2", "field2");
            rows.add(row);
            formDataDao.saveOrUpdate(formDefId, tableName, rows);
            row = formDataDao.load(formDefId, tableName, id);
            Assert.assertNotNull(row);
            Assert.assertEquals(id, row.getId());
            Assert.assertEquals("field1", row.getProperty("field1"));
            Assert.assertEquals("field2", row.getProperty("field2"));
            Assert.assertNull("field3", row.getProperty("field3"));
            
            //partially update the record and make sure the other field data is still merged correctly
            rows = new FormRowSet();
            FormRow row2 = new FormRow();
            row2.setId(id);
            row2.setProperty("field3", "field3");
            row2.setProperty("field4", "field4");
            rows.add(row2);
            formDataDao.saveOrUpdate(formDefId, tableName, rows);
            row = formDataDao.load(formDefId, tableName, id);
            Assert.assertNotNull(row);
            Assert.assertEquals(id, row.getId());
            Assert.assertEquals("field1", row.getProperty("field1"));
            Assert.assertEquals("field2", row.getProperty("field2"));
            Assert.assertEquals("field3", row.getProperty("field3"));
            Assert.assertEquals("field4", row.getProperty("field4"));
            
            //remove the record
            formDataDao.delete(formDefId, tableName, new String[]{id});
        } finally {
            TestUtil.deleteAllVersions("testFormDefinition");
        }
    }
}
