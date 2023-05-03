package org.joget.apps.app.model;

import java.io.IOException;
import java.util.Map;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormService;
import org.joget.apps.form.service.FormUtil;
import org.joget.directory.model.User;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class TestFormPermission {
    @Autowired
    private WorkflowUserManager workflowUserManager;
    @Autowired
    private FormService formService;
    
    @Test
    public void testDefaultUserHasPermission() throws IOException {
        //user: cat
        User user = new User();
        user.setUsername("cat");
        workflowUserManager.setCurrentThreadUser(user);
        
        FormData formData = new FormData();
        Form form = TestUtil.getForm("normal", formData);
        //LOAD
        form.render(formData, false); //run it once to make sure everything is set
        
        //normal section - field 1 & 2
        Element section1 = FormUtil.findElement("section1", form, formData);
        Assert.assertTrue(!isSectionHidden(section1, formData));
        Element field1 = FormUtil.findElement("field1", form, formData);
        Assert.assertTrue(isTextFieldEditable(field1, formData));
        Element field2 = FormUtil.findElement("field2", form, formData);
        Assert.assertTrue(isSelectFieldEditable(field2, formData));
        
        //readonly section - field 3 & 4
        Element section2 = FormUtil.findElement("section2", form, formData);
        Assert.assertTrue(!isSectionHidden(section2, formData));
        Element field3 = FormUtil.findElement("field3", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field3, formData));
        Element field4 = FormUtil.findElement("field4", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field4, formData));
        
        //readonly & label section - field 5 & 6
        Element section3 = FormUtil.findElement("section3", form, formData);
        Assert.assertTrue(!isSectionHidden(section3, formData));
        Element field5 = FormUtil.findElement("field5", form, formData);
        Assert.assertTrue(isTextFieldReadonlyLabel(field5, formData));
        Element field6 = FormUtil.findElement("field6", form, formData);
        Assert.assertTrue(isSelectFieldReadonlyLabel(field6, formData));
        
        //permission readonly section - field 7 & 8 (cat has permission)
        Element section4 = FormUtil.findElement("section4", form, formData);
        Assert.assertTrue(!isSectionHidden(section4, formData));
        Element field7 = FormUtil.findElement("field7", form, formData);
        Assert.assertTrue(isTextFieldEditable(field7, formData));
        Element field8 = FormUtil.findElement("field8", form, formData);
        Assert.assertTrue(isSelectFieldEditable(field8, formData));
        
        //permission readonly & label section - field 9 & 10 (cat has permission)
        Element section5 = FormUtil.findElement("section5", form, formData);
        Assert.assertTrue(!isSectionHidden(section5, formData));
        Element field9 = FormUtil.findElement("field9", form, formData);
        Assert.assertTrue(isTextFieldEditable(field9, formData));
        Element field10 = FormUtil.findElement("field10", form, formData);
        Assert.assertTrue(isSelectFieldEditable(field10, formData));
        
        //permission hidden section - field 11 & 12 (cat has permission)
        Element section6 = FormUtil.findElement("section6", form, formData);
        Assert.assertTrue(!isSectionHidden(section6, formData));
        Element field11 = FormUtil.findElement("field11", form, formData);
        Assert.assertTrue(isTextFieldEditable(field11, formData));
        Element field12 = FormUtil.findElement("field12", form, formData);
        Assert.assertTrue(isSelectFieldEditable(field12, formData));
        
        //readonly fields - field 13 & 14
        Element section7 = FormUtil.findElement("section7", form, formData);
        Assert.assertTrue(!isSectionHidden(section7, formData));
        Element field13 = FormUtil.findElement("field13", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field13, formData));
        Element field14 = FormUtil.findElement("field14", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field14, formData));
        
        //readonly & label fields - field 15 & 16
        Element section8 = FormUtil.findElement("section8", form, formData);
        Assert.assertTrue(!isSectionHidden(section8, formData));
        Element field15 = FormUtil.findElement("field15", form, formData);
        Assert.assertTrue(isTextFieldReadonlyLabel(field15, formData));
        Element field16 = FormUtil.findElement("field16", form, formData);
        Assert.assertTrue(isSelectFieldReadonlyLabel(field16, formData));
        
        //hidden fields - field 17 & 18
        Element section9 = FormUtil.findElement("section9", form, formData);
        Assert.assertTrue(!isSectionHidden(section9, formData));
        Element field17 = FormUtil.findElement("field17", form, formData);
        Assert.assertTrue(isTextFieldHidden(field17, formData));
        Element field18 = FormUtil.findElement("field18", form, formData);
        Assert.assertTrue(isSelectFieldHidden(field18, formData));
        
        //normal section with store binder - field 19 & 20
        Element section10 = FormUtil.findElement("section10", form, formData);
        Assert.assertTrue(!isSectionHidden(section10, formData));
        Element field19 = FormUtil.findElement("field19", form, formData);
        Assert.assertTrue(isTextFieldEditable(field19, formData));
        Element field20 = FormUtil.findElement("field20", form, formData);
        Assert.assertTrue(isSelectFieldEditable(field20, formData));
        
        //readonly section with store binder - field 21 & 22
        Element section11 = FormUtil.findElement("section11", form, formData);
        Assert.assertTrue(!isSectionHidden(section11, formData));
        Element field21 = FormUtil.findElement("field21", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field21, formData));
        Element field22 = FormUtil.findElement("field22", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field22, formData));
        
        //permission hidden section with store binder - field 23 & 24 (cat has permission)
        Element section12 = FormUtil.findElement("section12", form, formData);
        Assert.assertTrue(!isSectionHidden(section12, formData));
        Element field23 = FormUtil.findElement("field23", form, formData);
        Assert.assertTrue(isTextFieldEditable(field23, formData));
        Element field24 = FormUtil.findElement("field24", form, formData);
        Assert.assertTrue(isSelectFieldEditable(field24, formData));
        
        //permission readonly section with store binder - field 25 & 26 (cat has permission)
        Element section13 = FormUtil.findElement("section13", form, formData);
        Assert.assertTrue(!isSectionHidden(section13, formData));
        Element field25 = FormUtil.findElement("field25", form, formData);
        Assert.assertTrue(isTextFieldEditable(field25, formData));
        Element field26 = FormUtil.findElement("field26", form, formData);
        Assert.assertTrue(isSelectFieldEditable(field26, formData));
        
        //VALIDATION
        populateSubmittedData(formData, "");
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.validateFormData(form, formData);
        Map<String, String> errors = formData.getFormErrors();
        
        //normal section - field 1 & 2
        Assert.assertTrue(errors.containsKey("field1"));
        Assert.assertTrue(errors.containsKey("field2"));
        
        //readonly section - field 3 & 4
        Assert.assertTrue(!errors.containsKey("field3"));
        Assert.assertTrue(!errors.containsKey("field4"));
        
        //readonly & label section - field 5 & 6
        Assert.assertTrue(!errors.containsKey("field5"));
        Assert.assertTrue(!errors.containsKey("field6"));
        
        //permission readonly section - field 7 & 8
        Assert.assertTrue(errors.containsKey("field7"));
        Assert.assertTrue(errors.containsKey("field8"));
        
        //permission readonly & label section - field 9 & 10
        Assert.assertTrue(errors.containsKey("field9"));
        Assert.assertTrue(errors.containsKey("field10"));
        
        //permission hidden section - field 11 & 12
        Assert.assertTrue(errors.containsKey("field11"));
        Assert.assertTrue(errors.containsKey("field12"));
        
        //readonly fields - field 13 & 14
        Assert.assertTrue(!errors.containsKey("field13"));
        Assert.assertTrue(!errors.containsKey("field14"));
        
        //readonly & label fields - field 15 & 16
        Assert.assertTrue(!errors.containsKey("field15"));
        Assert.assertTrue(!errors.containsKey("field16"));
        
        //hidden fields - field 17 & 18
        Assert.assertTrue(!errors.containsKey("field17"));
        Assert.assertTrue(!errors.containsKey("field18"));
        
        //normal section with store binder - field 19 & 20
        Assert.assertTrue(errors.containsKey("field19"));
        Assert.assertTrue(errors.containsKey("field20"));
        
        //readonly section with store binder - field 21 & 22
        Assert.assertTrue(!errors.containsKey("field21"));
        Assert.assertTrue(!errors.containsKey("field22"));
        
        //permission hidden section with store binder - field 23 & 24 (cat has permission)
        Assert.assertTrue(errors.containsKey("field23"));
        Assert.assertTrue(errors.containsKey("field24"));
        
        //permission readonly section with store binder - field 25 & 26 (cat has permission)
        Assert.assertTrue(errors.containsKey("field25"));
        Assert.assertTrue(errors.containsKey("field26"));
        
        //STORE
        populateSubmittedData(formData, "value1");
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.executeFormStoreBinders(form, formData);
        FormRowSet rows = formData.getStoreBinderData(form.getStoreBinder());
        FormRow row = rows.get(0);
        
        //normal section - field 1 & 2
        Assert.assertTrue("value1".equals(row.getProperty("field1")));
        Assert.assertTrue("value1".equals(row.getProperty("field2")));
        
        //readonly section - field 3 & 4
        Assert.assertTrue("value".equals(row.getProperty("field3")));
        Assert.assertTrue("value".equals(row.getProperty("field4")));
        
        //readonly & label section - field 5 & 6
        Assert.assertTrue("value".equals(row.getProperty("field5")));
        Assert.assertTrue("value".equals(row.getProperty("field6")));
        
        //permission readonly section - field 7 & 8
        Assert.assertTrue("value1".equals(row.getProperty("field7")));
        Assert.assertTrue("value1".equals(row.getProperty("field8")));
        
        //permission readonly & label section - field 9 & 10
        Assert.assertTrue("value1".equals(row.getProperty("field9")));
        Assert.assertTrue("value1".equals(row.getProperty("field10")));
        
        //permission hidden section - field 11 & 12
        Assert.assertTrue("value1".equals(row.getProperty("field11")));
        Assert.assertTrue("value1".equals(row.getProperty("field12")));
        
        //readonly fields - field 13 & 14
        Assert.assertTrue("value".equals(row.getProperty("field13")));
        Assert.assertTrue("value".equals(row.getProperty("field14")));
        
        //readonly & label fields - field 15 & 16
        Assert.assertTrue("value".equals(row.getProperty("field15")));
        Assert.assertTrue("value".equals(row.getProperty("field16")));
        
        //hidden fields - field 17 & 18
        Assert.assertNull(row.getProperty("field17"));
        Assert.assertNull(row.getProperty("field18"));
        
        //normal section with store binder - field 19 & 20
        rows = formData.getStoreBinderData(section10.getStoreBinder());
        row = rows.get(0);
        Assert.assertTrue("true".equals(row.getProperty("isExecuted")));
        Assert.assertTrue("value1".equals(row.getProperty("field19")));
        Assert.assertTrue("value1".equals(row.getProperty("field20")));
        
        //readonly section with store binder - field 21 & 22
        rows = formData.getStoreBinderData(section11.getStoreBinder());
        row = rows.get(0);
        Assert.assertNull(row.getProperty("isExecuted"));
        Assert.assertTrue("value".equals(row.getProperty("field21")));
        Assert.assertTrue("value".equals(row.getProperty("field22")));
        
        //permission hidden section with store binder - field 23 & 24 (cat has permission)
        rows = formData.getStoreBinderData(section12.getStoreBinder());
        row = rows.get(0);
        Assert.assertTrue("true".equals(row.getProperty("isExecuted")));
        Assert.assertTrue("value1".equals(row.getProperty("field23")));
        Assert.assertTrue("value1".equals(row.getProperty("field24")));
        
        //permission readonly section with store binder - field 25 & 26 (cat has permission)
        rows = formData.getStoreBinderData(section13.getStoreBinder());
        row = rows.get(0);
        Assert.assertTrue("true".equals(row.getProperty("isExecuted")));
        Assert.assertTrue("value1".equals(row.getProperty("field25")));
        Assert.assertTrue("value1".equals(row.getProperty("field26")));
    }
    
    @Test
    public void testDefaultUserNoPermission() throws IOException  {
        //user: admin
        User user = new User();
        user.setUsername("admin");
        workflowUserManager.setCurrentThreadUser(user);
        
        FormData formData = new FormData();
        Form form = TestUtil.getForm("normal", formData);
        //LOAD
        form.render(formData, false); //run it once to make sure everything is set
        
        //normal section - field 1 & 2
        Element section1 = FormUtil.findElement("section1", form, formData);
        Assert.assertTrue(!isSectionHidden(section1, formData));
        Element field1 = FormUtil.findElement("field1", form, formData);
        Assert.assertTrue(isTextFieldEditable(field1, formData));
        Element field2 = FormUtil.findElement("field2", form, formData);
        Assert.assertTrue(isSelectFieldEditable(field2, formData));
        
        //readonly section - field 3 & 4
        Element section2 = FormUtil.findElement("section2", form, formData);
        Assert.assertTrue(!isSectionHidden(section2, formData));
        Element field3 = FormUtil.findElement("field3", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field3, formData));
        Element field4 = FormUtil.findElement("field4", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field4, formData));
        
        //readonly & label section - field 5 & 6
        Element section3 = FormUtil.findElement("section3", form, formData);
        Assert.assertTrue(!isSectionHidden(section3, formData));
        Element field5 = FormUtil.findElement("field5", form, formData);
        Assert.assertTrue(isTextFieldReadonlyLabel(field5, formData));
        Element field6 = FormUtil.findElement("field6", form, formData);
        Assert.assertTrue(isSelectFieldReadonlyLabel(field6, formData));
        
        //permission readonly section - field 7 & 8 (admin no permission)
        Element section4 = FormUtil.findElement("section4", form, formData);
        Assert.assertTrue(!isSectionHidden(section4, formData));
        Element field7 = FormUtil.findElement("field7", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field7, formData));
        Element field8 = FormUtil.findElement("field8", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field8, formData));
        
        //permission readonly & label section - field 9 & 10 (admin no permission)
        Element section5 = FormUtil.findElement("section5", form, formData);
        Assert.assertTrue(!isSectionHidden(section5, formData));
        Element field9 = FormUtil.findElement("field9", form, formData);
        Assert.assertTrue(isTextFieldReadonlyLabel(field9, formData));
        Element field10 = FormUtil.findElement("field10", form, formData);
        Assert.assertTrue(isSelectFieldReadonlyLabel(field10, formData));
        
        //permission hidden section - field 11 & 12 (admin no permission)
        Element section6 = FormUtil.findElement("section6", form, formData);
        Assert.assertTrue(isSectionHidden(section6, formData));
        Element field11 = FormUtil.findElement("field11", form, formData);
        Assert.assertTrue(isTextFieldHidden(field11, formData));
        Element field12 = FormUtil.findElement("field12", form, formData);
        Assert.assertTrue(isSelectFieldHidden(field12, formData));
        
        //readonly fields - field 13 & 14
        Element section7 = FormUtil.findElement("section7", form, formData);
        Assert.assertTrue(!isSectionHidden(section7, formData));
        Element field13 = FormUtil.findElement("field13", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field13, formData));
        Element field14 = FormUtil.findElement("field14", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field14, formData));
        
        //readonly & label fields - field 15 & 16
        Element section8 = FormUtil.findElement("section8", form, formData);
        Assert.assertTrue(!isSectionHidden(section8, formData));
        Element field15 = FormUtil.findElement("field15", form, formData);
        Assert.assertTrue(isTextFieldReadonlyLabel(field15, formData));
        Element field16 = FormUtil.findElement("field16", form, formData);
        Assert.assertTrue(isSelectFieldReadonlyLabel(field16, formData));
        
        //hidden fields - field 17 & 18
        Element section9 = FormUtil.findElement("section9", form, formData);
        Assert.assertTrue(!isSectionHidden(section9, formData));
        Element field17 = FormUtil.findElement("field17", form, formData);
        Assert.assertTrue(isTextFieldHidden(field17, formData));
        Element field18 = FormUtil.findElement("field18", form, formData);
        Assert.assertTrue(isSelectFieldHidden(field18, formData));
        
        //normal section with store binder - field 19 & 20
        Element section10 = FormUtil.findElement("section10", form, formData);
        Assert.assertTrue(!isSectionHidden(section10, formData));
        Element field19 = FormUtil.findElement("field19", form, formData);
        Assert.assertTrue(isTextFieldEditable(field19, formData));
        Element field20 = FormUtil.findElement("field20", form, formData);
        Assert.assertTrue(isSelectFieldEditable(field20, formData));
        
        //readonly section with store binder - field 21 & 22
        Element section11 = FormUtil.findElement("section11", form, formData);
        Assert.assertTrue(!isSectionHidden(section11, formData));
        Element field21 = FormUtil.findElement("field21", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field21, formData));
        Element field22 = FormUtil.findElement("field22", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field22, formData));
        
        //permission hidden section with store binder - field 23 & 24 (admin no permission)
        Element section12 = FormUtil.findElement("section12", form, formData);
        Assert.assertTrue(isSectionHidden(section12, formData));
        Element field23 = FormUtil.findElement("field23", form, formData);
        Assert.assertTrue(isTextFieldHidden(field23, formData));
        Element field24 = FormUtil.findElement("field24", form, formData);
        Assert.assertTrue(isSelectFieldHidden(field24, formData));
        
        //permission readonly section with store binder - field 25 & 26 (admin no permission)
        Element section13 = FormUtil.findElement("section13", form, formData);
        Assert.assertTrue(!isSectionHidden(section13, formData));
        Element field25 = FormUtil.findElement("field25", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field25, formData));
        Element field26 = FormUtil.findElement("field26", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field26, formData));
        
        //VALIDATION
        populateSubmittedData(formData, "");
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.validateFormData(form, formData);
        Map<String, String> errors = formData.getFormErrors();
        
        //normal section - field 1 & 2
        Assert.assertTrue(errors.containsKey("field1"));
        Assert.assertTrue(errors.containsKey("field2"));
        
        //readonly section - field 3 & 4
        Assert.assertTrue(!errors.containsKey("field3"));
        Assert.assertTrue(!errors.containsKey("field4"));
        
        //readonly & label section - field 5 & 6
        Assert.assertTrue(!errors.containsKey("field5"));
        Assert.assertTrue(!errors.containsKey("field6"));
        
        //permission readonly section - field 7 & 8
        Assert.assertTrue(!errors.containsKey("field7"));
        Assert.assertTrue(!errors.containsKey("field8"));
        
        //permission readonly & label section - field 9 & 10
        Assert.assertTrue(!errors.containsKey("field9"));
        Assert.assertTrue(!errors.containsKey("field10"));
        
        //permission hidden section - field 11 & 12
        Assert.assertTrue(!errors.containsKey("field11"));
        Assert.assertTrue(!errors.containsKey("field12"));
        
        //readonly fields - field 13 & 14
        Assert.assertTrue(!errors.containsKey("field13"));
        Assert.assertTrue(!errors.containsKey("field14"));
        
        //readonly & label fields - field 15 & 16
        Assert.assertTrue(!errors.containsKey("field15"));
        Assert.assertTrue(!errors.containsKey("field16"));
        
        //hidden fields - field 17 & 18
        Assert.assertTrue(!errors.containsKey("field17"));
        Assert.assertTrue(!errors.containsKey("field18"));
        
        //normal section with store binder - field 19 & 20
        Assert.assertTrue(errors.containsKey("field19"));
        Assert.assertTrue(errors.containsKey("field20"));
        
        //readonly section with store binder - field 21 & 22
        Assert.assertTrue(!errors.containsKey("field21"));
        Assert.assertTrue(!errors.containsKey("field22"));
        
        //permission hidden section with store binder - field 23 & 24 (admin no permission)
        Assert.assertTrue(!errors.containsKey("field23"));
        Assert.assertTrue(!errors.containsKey("field24"));
        
        //permission readonly section with store binder - field 25 & 26 (admin no permission)
        Assert.assertTrue(!errors.containsKey("field25"));
        Assert.assertTrue(!errors.containsKey("field26"));
        
        
        //STORE
        populateSubmittedData(formData, "value1");
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.executeFormStoreBinders(form, formData);
        FormRowSet rows = formData.getStoreBinderData(form.getStoreBinder());
        FormRow row = rows.get(0);
        
        //normal section - field 1 & 2
        Assert.assertTrue("value1".equals(row.getProperty("field1")));
        Assert.assertTrue("value1".equals(row.getProperty("field2")));
        
        //readonly section - field 3 & 4
        Assert.assertTrue("value".equals(row.getProperty("field3")));
        Assert.assertTrue("value".equals(row.getProperty("field4")));
        
        //readonly & label section - field 5 & 6
        Assert.assertTrue("value".equals(row.getProperty("field5")));
        Assert.assertTrue("value".equals(row.getProperty("field6")));
        
        //permission readonly section - field 7 & 8
        Assert.assertTrue("value".equals(row.getProperty("field7")));
        Assert.assertTrue("value".equals(row.getProperty("field8")));
        
        //permission readonly & label section - field 9 & 10
        Assert.assertTrue("value".equals(row.getProperty("field9")));
        Assert.assertTrue("value".equals(row.getProperty("field10")));
        
        //permission hidden section - field 11 & 12
        Assert.assertNull(row.getProperty("field11"));
        Assert.assertNull(row.getProperty("field12"));
        
        //readonly fields - field 13 & 14
        Assert.assertTrue("value".equals(row.getProperty("field13")));
        Assert.assertTrue("value".equals(row.getProperty("field14")));
        
        //readonly & label fields - field 15 & 16
        Assert.assertTrue("value".equals(row.getProperty("field15")));
        Assert.assertTrue("value".equals(row.getProperty("field16")));
        
        //hidden fields - field 17 & 18
        Assert.assertNull(row.getProperty("field17"));
        Assert.assertNull(row.getProperty("field18"));
        
        //normal section with store binder - field 19 & 20
        rows = formData.getStoreBinderData(section10.getStoreBinder());
        row = rows.get(0);
        Assert.assertTrue("true".equals(row.getProperty("isExecuted")));
        Assert.assertTrue("value1".equals(row.getProperty("field19")));
        Assert.assertTrue("value1".equals(row.getProperty("field20")));
        
        //readonly section with store binder - field 21 & 22
        rows = formData.getStoreBinderData(section11.getStoreBinder());
        row = rows.get(0);
        Assert.assertNull(row.getProperty("isExecuted"));
        Assert.assertTrue("value".equals(row.getProperty("field21")));
        Assert.assertTrue("value".equals(row.getProperty("field22")));
        
        //permission hidden section with store binder - field 23 & 24 (admin no permission)
        rows = formData.getStoreBinderData(section12.getStoreBinder());
        Assert.assertTrue(rows.isEmpty());
        
        //permission readonly section with store binder - field 25 & 26 (admin no permission)
        rows = formData.getStoreBinderData(section13.getStoreBinder());
        row = rows.get(0);
        Assert.assertNull(row.getProperty("isExecuted"));
        Assert.assertTrue("value".equals(row.getProperty("field25")));
        Assert.assertTrue("value".equals(row.getProperty("field26")));
    }
    
    @Test
    public void testPermissionRule1() throws IOException {
        //user: clark
        User user = new User();
        user.setUsername("clark");
        workflowUserManager.setCurrentThreadUser(user);
        
        FormData formData = new FormData();
        Form form = TestUtil.getForm("permission", formData);
        //LOAD
        form.render(formData, false); //run it once to make sure everything is set
        
        //normal section - field 1 & 2
        Element section1 = FormUtil.findElement("section1", form, formData);
        Assert.assertTrue(!isSectionHidden(section1, formData));
        Element field1 = FormUtil.findElement("field1", form, formData);
        Assert.assertTrue(isTextFieldEditable(field1, formData));
        Element field2 = FormUtil.findElement("field2", form, formData);
        Assert.assertTrue(isSelectFieldEditable(field2, formData));
        
        //readonly section - field 3 & 4
        Element section2 = FormUtil.findElement("section2", form, formData);
        Assert.assertTrue(!isSectionHidden(section2, formData));
        Element field3 = FormUtil.findElement("field3", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field3, formData));
        Element field4 = FormUtil.findElement("field4", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field4, formData));
        
        //readonly & label section - field 5 & 6
        Element section3 = FormUtil.findElement("section3", form, formData);
        Assert.assertTrue(!isSectionHidden(section3, formData));
        Element field5 = FormUtil.findElement("field5", form, formData);
        Assert.assertTrue(isTextFieldReadonlyLabel(field5, formData));
        Element field6 = FormUtil.findElement("field6", form, formData);
        Assert.assertTrue(isSelectFieldReadonlyLabel(field6, formData));
        
        //permission readonly section - field 7 & 8 (clark no permission)
        Element section4 = FormUtil.findElement("section4", form, formData);
        Assert.assertTrue(!isSectionHidden(section4, formData));
        Element field7 = FormUtil.findElement("field7", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field7, formData));
        Element field8 = FormUtil.findElement("field8", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field8, formData));
        
        //permission readonly & label section - field 9 & 10 (clark no permission)
        Element section5 = FormUtil.findElement("section5", form, formData);
        Assert.assertTrue(!isSectionHidden(section5, formData));
        Element field9 = FormUtil.findElement("field9", form, formData);
        Assert.assertTrue(isTextFieldReadonlyLabel(field9, formData));
        Element field10 = FormUtil.findElement("field10", form, formData);
        Assert.assertTrue(isSelectFieldReadonlyLabel(field10, formData));
        
        //permission hidden section - field 11 & 12 (clark no permission)
        Element section6 = FormUtil.findElement("section6", form, formData);
        Assert.assertTrue(isSectionHidden(section6, formData));
        Element field11 = FormUtil.findElement("field11", form, formData);
        Assert.assertTrue(isTextFieldHidden(field11, formData));
        Element field12 = FormUtil.findElement("field12", form, formData);
        Assert.assertTrue(isSelectFieldHidden(field12, formData));
        
        //readonly fields - field 13 & 14
        Element section7 = FormUtil.findElement("section7", form, formData);
        Assert.assertTrue(!isSectionHidden(section7, formData));
        Element field13 = FormUtil.findElement("field13", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field13, formData));
        Element field14 = FormUtil.findElement("field14", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field14, formData));
        
        //readonly & label fields - field 15 & 16
        Element section8 = FormUtil.findElement("section8", form, formData);
        Assert.assertTrue(!isSectionHidden(section8, formData));
        Element field15 = FormUtil.findElement("field15", form, formData);
        Assert.assertTrue(isTextFieldReadonlyLabel(field15, formData));
        Element field16 = FormUtil.findElement("field16", form, formData);
        Assert.assertTrue(isSelectFieldReadonlyLabel(field16, formData));
        
        //hidden fields - field 17 & 18
        Element section9 = FormUtil.findElement("section9", form, formData);
        Assert.assertTrue(!isSectionHidden(section9, formData));
        Element field17 = FormUtil.findElement("field17", form, formData);
        Assert.assertTrue(isTextFieldHidden(field17, formData));
        Element field18 = FormUtil.findElement("field18", form, formData);
        Assert.assertTrue(isSelectFieldHidden(field18, formData));
        
        //normal section with store binder - field 19 & 20
        Element section10 = FormUtil.findElement("section10", form, formData);
        Assert.assertTrue(!isSectionHidden(section10, formData));
        Element field19 = FormUtil.findElement("field19", form, formData);
        Assert.assertTrue(isTextFieldEditable(field19, formData));
        Element field20 = FormUtil.findElement("field20", form, formData);
        Assert.assertTrue(isSelectFieldEditable(field20, formData));
        
        //readonly section with store binder - field 21 & 22
        Element section11 = FormUtil.findElement("section11", form, formData);
        Assert.assertTrue(!isSectionHidden(section11, formData));
        Element field21 = FormUtil.findElement("field21", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field21, formData));
        Element field22 = FormUtil.findElement("field22", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field22, formData));
        
        //permission hidden section with store binder - field 23 & 24 (clark no permission)
        Element section12 = FormUtil.findElement("section12", form, formData);
        Assert.assertTrue(isSectionHidden(section12, formData));
        Element field23 = FormUtil.findElement("field23", form, formData);
        Assert.assertTrue(isTextFieldHidden(field23, formData));
        Element field24 = FormUtil.findElement("field24", form, formData);
        Assert.assertTrue(isSelectFieldHidden(field24, formData));
        
        //permission readonly section with store binder - field 25 & 26 (clark no permission)
        Element section13 = FormUtil.findElement("section13", form, formData);
        Assert.assertTrue(!isSectionHidden(section13, formData));
        Element field25 = FormUtil.findElement("field25", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field25, formData));
        Element field26 = FormUtil.findElement("field26", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field26, formData));
        
        //VALIDATION
        populateSubmittedData(formData, "");
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.validateFormData(form, formData);
        Map<String, String> errors = formData.getFormErrors();
        
        //normal section - field 1 & 2
        Assert.assertTrue(errors.containsKey("field1"));
        Assert.assertTrue(errors.containsKey("field2"));
        
        //readonly section - field 3 & 4
        Assert.assertTrue(!errors.containsKey("field3"));
        Assert.assertTrue(!errors.containsKey("field4"));
        
        //readonly & label section - field 5 & 6
        Assert.assertTrue(!errors.containsKey("field5"));
        Assert.assertTrue(!errors.containsKey("field6"));
        
        //permission readonly section - field 7 & 8
        Assert.assertTrue(!errors.containsKey("field7"));
        Assert.assertTrue(!errors.containsKey("field8"));
        
        //permission readonly & label section - field 9 & 10
        Assert.assertTrue(!errors.containsKey("field9"));
        Assert.assertTrue(!errors.containsKey("field10"));
        
        //permission hidden section - field 11 & 12
        Assert.assertTrue(!errors.containsKey("field11"));
        Assert.assertTrue(!errors.containsKey("field12"));
        
        //readonly fields - field 13 & 14
        Assert.assertTrue(!errors.containsKey("field13"));
        Assert.assertTrue(!errors.containsKey("field14"));
        
        //readonly & label fields - field 15 & 16
        Assert.assertTrue(!errors.containsKey("field15"));
        Assert.assertTrue(!errors.containsKey("field16"));
        
        //hidden fields - field 17 & 18
        Assert.assertTrue(!errors.containsKey("field17"));
        Assert.assertTrue(!errors.containsKey("field18"));
        
        //normal section with store binder - field 19 & 20
        Assert.assertTrue(errors.containsKey("field19"));
        Assert.assertTrue(errors.containsKey("field20"));
        
        //readonly section with store binder - field 21 & 22
        Assert.assertTrue(!errors.containsKey("field21"));
        Assert.assertTrue(!errors.containsKey("field22"));
        
        //permission hidden section with store binder - field 23 & 24 (clark no permission)
        Assert.assertTrue(!errors.containsKey("field23"));
        Assert.assertTrue(!errors.containsKey("field24"));
        
        //permission readonly section with store binder - field 25 & 26 (clark no permission)
        Assert.assertTrue(!errors.containsKey("field25"));
        Assert.assertTrue(!errors.containsKey("field26"));
        
        //STORE
        populateSubmittedData(formData, "value1");
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.executeFormStoreBinders(form, formData);
        FormRowSet rows = formData.getStoreBinderData(form.getStoreBinder());
        FormRow row = rows.get(0);
        
        //normal section - field 1 & 2
        Assert.assertTrue("value1".equals(row.getProperty("field1")));
        Assert.assertTrue("value1".equals(row.getProperty("field2")));
        
        //readonly section - field 3 & 4
        Assert.assertTrue("value".equals(row.getProperty("field3")));
        Assert.assertTrue("value".equals(row.getProperty("field4")));
        
        //readonly & label section - field 5 & 6
        Assert.assertTrue("value".equals(row.getProperty("field5")));
        Assert.assertTrue("value".equals(row.getProperty("field6")));
        
        //permission readonly section - field 7 & 8
        Assert.assertTrue("value".equals(row.getProperty("field7")));
        Assert.assertTrue("value".equals(row.getProperty("field8")));
        
        //permission readonly & label section - field 9 & 10
        Assert.assertTrue("value".equals(row.getProperty("field9")));
        Assert.assertTrue("value".equals(row.getProperty("field10")));
        
        //permission hidden section - field 11 & 12
        Assert.assertNull(row.getProperty("field11"));
        Assert.assertNull(row.getProperty("field12"));
        
        //readonly fields - field 13 & 14
        Assert.assertTrue("value".equals(row.getProperty("field13")));
        Assert.assertTrue("value".equals(row.getProperty("field14")));
        
        //readonly & label fields - field 15 & 16
        Assert.assertTrue("value".equals(row.getProperty("field15")));
        Assert.assertTrue("value".equals(row.getProperty("field16")));
        
        //hidden fields - field 17 & 18
        Assert.assertNull(row.getProperty("field17"));
        Assert.assertNull(row.getProperty("field18"));
        
        //normal section with store binder - field 19 & 20
        rows = formData.getStoreBinderData(section10.getStoreBinder());
        row = rows.get(0);
        Assert.assertTrue("true".equals(row.getProperty("isExecuted")));
        Assert.assertTrue("value1".equals(row.getProperty("field19")));
        Assert.assertTrue("value1".equals(row.getProperty("field20")));
        
        //readonly section with store binder - field 21 & 22
        rows = formData.getStoreBinderData(section11.getStoreBinder());
        row = rows.get(0);
        Assert.assertNull(row.getProperty("isExecuted"));
        Assert.assertTrue("value".equals(row.getProperty("field21")));
        Assert.assertTrue("value".equals(row.getProperty("field22")));
        
        //permission hidden section with store binder - field 23 & 24 (clark no permission)
        rows = formData.getStoreBinderData(section12.getStoreBinder());
        Assert.assertTrue(rows.isEmpty());
        
        //permission readonly section with store binder - field 25 & 26 (clark no permission)
        rows = formData.getStoreBinderData(section13.getStoreBinder());
        row = rows.get(0);
        Assert.assertNull(row.getProperty("isExecuted"));
        Assert.assertTrue("value".equals(row.getProperty("field25")));
        Assert.assertTrue("value".equals(row.getProperty("field26")));
    }
    
    @Test
    public void testPermissionRule2() throws IOException  {
        //user: david
        User user = new User();
        user.setUsername("david");
        workflowUserManager.setCurrentThreadUser(user);
        
        FormData formData = new FormData();
        Form form = TestUtil.getForm("permission", formData);
        //LOAD
        form.render(formData, false); //run it once to make sure everything is set
        
        //normal section - field 1 & 2
        Element section1 = FormUtil.findElement("section1", form, formData);
        Assert.assertTrue(!isSectionHidden(section1, formData));
        Element field1 = FormUtil.findElement("field1", form, formData);
        Assert.assertTrue(isTextFieldEditable(field1, formData));
        Element field2 = FormUtil.findElement("field2", form, formData);
        Assert.assertTrue(isSelectFieldEditable(field2, formData));
        
        //readonly section - field 3 & 4
        Element section2 = FormUtil.findElement("section2", form, formData);
        Assert.assertTrue(!isSectionHidden(section2, formData));
        Element field3 = FormUtil.findElement("field3", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field3, formData));
        Element field4 = FormUtil.findElement("field4", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field4, formData));
        
        //readonly & label section - field 5 & 6
        Element section3 = FormUtil.findElement("section3", form, formData);
        Assert.assertTrue(!isSectionHidden(section3, formData));
        Element field5 = FormUtil.findElement("field5", form, formData);
        Assert.assertTrue(isTextFieldReadonlyLabel(field5, formData));
        Element field6 = FormUtil.findElement("field6", form, formData);
        Assert.assertTrue(isSelectFieldReadonlyLabel(field6, formData));
        
        //permission readonly section - field 7 & 8 (david has permission)
        Element section4 = FormUtil.findElement("section4", form, formData);
        Assert.assertTrue(!isSectionHidden(section4, formData));
        Element field7 = FormUtil.findElement("field7", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field7, formData));
        Element field8 = FormUtil.findElement("field8", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field8, formData));
        
        //permission readonly & label section - field 9 & 10 (david has permission)
        Element section5 = FormUtil.findElement("section5", form, formData);
        Assert.assertTrue(!isSectionHidden(section5, formData));
        Element field9 = FormUtil.findElement("field9", form, formData);
        Assert.assertTrue(isTextFieldReadonlyLabel(field9, formData));
        Element field10 = FormUtil.findElement("field10", form, formData);
        Assert.assertTrue(isSelectFieldReadonlyLabel(field10, formData));
        
        //permission hidden section - field 11 & 12 (david has permission)
        Element section6 = FormUtil.findElement("section6", form, formData);
        Assert.assertTrue(isSectionHidden(section6, formData));
        Element field11 = FormUtil.findElement("field11", form, formData);
        Assert.assertTrue(isTextFieldHidden(field11, formData));
        Element field12 = FormUtil.findElement("field12", form, formData);
        Assert.assertTrue(isSelectFieldHidden(field12, formData));
        
        //readonly fields - field 13 & 14
        Element section7 = FormUtil.findElement("section7", form, formData);
        Assert.assertTrue(!isSectionHidden(section7, formData));
        Element field13 = FormUtil.findElement("field13", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field13, formData));
        Element field14 = FormUtil.findElement("field14", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field14, formData));
        
        //readonly & label fields - field 15 & 16
        Element section8 = FormUtil.findElement("section8", form, formData);
        Assert.assertTrue(!isSectionHidden(section8, formData));
        Element field15 = FormUtil.findElement("field15", form, formData);
        Assert.assertTrue(isTextFieldReadonlyLabel(field15, formData));
        Element field16 = FormUtil.findElement("field16", form, formData);
        Assert.assertTrue(isSelectFieldReadonlyLabel(field16, formData));
        
        //hidden fields - field 17 & 18
        Element section9 = FormUtil.findElement("section9", form, formData);
        Assert.assertTrue(!isSectionHidden(section9, formData));
        Element field17 = FormUtil.findElement("field17", form, formData);
        Assert.assertTrue(isTextFieldHidden(field17, formData));
        Element field18 = FormUtil.findElement("field18", form, formData);
        Assert.assertTrue(isSelectFieldHidden(field18, formData));
        
        //normal section with store binder - field 19 & 20
        Element section10 = FormUtil.findElement("section10", form, formData);
        Assert.assertTrue(!isSectionHidden(section10, formData));
        Element field19 = FormUtil.findElement("field19", form, formData);
        Assert.assertTrue(isTextFieldEditable(field19, formData));
        Element field20 = FormUtil.findElement("field20", form, formData);
        Assert.assertTrue(isSelectFieldEditable(field20, formData));
        
        //readonly section with store binder - field 21 & 22
        Element section11 = FormUtil.findElement("section11", form, formData);
        Assert.assertTrue(!isSectionHidden(section11, formData));
        Element field21 = FormUtil.findElement("field21", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field21, formData));
        Element field22 = FormUtil.findElement("field22", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field22, formData));
        
        //permission hidden section with store binder - field 23 & 24 (david has permission)
        Element section12 = FormUtil.findElement("section12", form, formData);
        Assert.assertTrue(isSectionHidden(section12, formData));
        Element field23 = FormUtil.findElement("field23", form, formData);
        Assert.assertTrue(isTextFieldHidden(field23, formData));
        Element field24 = FormUtil.findElement("field24", form, formData);
        Assert.assertTrue(isSelectFieldHidden(field24, formData));
        
        //permission readonly section with store binder - field 25 & 26 (david has permission)
        Element section13 = FormUtil.findElement("section13", form, formData);
        Assert.assertTrue(!isSectionHidden(section13, formData));
        Element field25 = FormUtil.findElement("field25", form, formData);
        Assert.assertTrue(isTextFieldReadonly(field25, formData));
        Element field26 = FormUtil.findElement("field26", form, formData);
        Assert.assertTrue(isSelectFieldReadonly(field26, formData));
        
        //VALIDATION
        populateSubmittedData(formData, "");
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.validateFormData(form, formData);
        Map<String, String> errors = formData.getFormErrors();
        
        //normal section - field 1 & 2
        Assert.assertTrue(errors.containsKey("field1"));
        Assert.assertTrue(errors.containsKey("field2"));
        
        //readonly section - field 3 & 4
        Assert.assertTrue(!errors.containsKey("field3"));
        Assert.assertTrue(!errors.containsKey("field4"));
        
        //readonly & label section - field 5 & 6
        Assert.assertTrue(!errors.containsKey("field5"));
        Assert.assertTrue(!errors.containsKey("field6"));
        
        //permission readonly section - field 7 & 8
        Assert.assertTrue(!errors.containsKey("field7"));
        Assert.assertTrue(!errors.containsKey("field8"));
        
        //permission readonly & label section - field 9 & 10
        Assert.assertTrue(!errors.containsKey("field9"));
        Assert.assertTrue(!errors.containsKey("field10"));
        
        //permission hidden section - field 11 & 12
        Assert.assertTrue(!errors.containsKey("field11"));
        Assert.assertTrue(!errors.containsKey("field12"));
        
        //readonly fields - field 13 & 14
        Assert.assertTrue(!errors.containsKey("field13"));
        Assert.assertTrue(!errors.containsKey("field14"));
        
        //readonly & label fields - field 15 & 16
        Assert.assertTrue(!errors.containsKey("field15"));
        Assert.assertTrue(!errors.containsKey("field16"));
        
        //hidden fields - field 17 & 18
        Assert.assertTrue(!errors.containsKey("field17"));
        Assert.assertTrue(!errors.containsKey("field18"));
        
        //normal section with store binder - field 19 & 20
        Assert.assertTrue(errors.containsKey("field19"));
        Assert.assertTrue(errors.containsKey("field20"));
        
        //readonly section with store binder - field 21 & 22
        Assert.assertTrue(!errors.containsKey("field21"));
        Assert.assertTrue(!errors.containsKey("field22"));
        
        //permission hidden section with store binder - field 23 & 24 (david has permission)
        Assert.assertTrue(!errors.containsKey("field23"));
        Assert.assertTrue(!errors.containsKey("field24"));
        
        //permission readonly section with store binder - field 25 & 26 (david has permission)
        Assert.assertTrue(!errors.containsKey("field25"));
        Assert.assertTrue(!errors.containsKey("field26"));
        
        
        //STORE
        populateSubmittedData(formData, "value1");
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.executeFormStoreBinders(form, formData);
        FormRowSet rows = formData.getStoreBinderData(form.getStoreBinder());
        FormRow row = rows.get(0);
        
        //normal section - field 1 & 2
        Assert.assertTrue("value1".equals(row.getProperty("field1")));
        Assert.assertTrue("value1".equals(row.getProperty("field2")));
        
        //readonly section - field 3 & 4
        Assert.assertTrue("value".equals(row.getProperty("field3")));
        Assert.assertTrue("value".equals(row.getProperty("field4")));
        
        //readonly & label section - field 5 & 6
        Assert.assertTrue("value".equals(row.getProperty("field5")));
        Assert.assertTrue("value".equals(row.getProperty("field6")));
        
        //permission readonly section - field 7 & 8
        Assert.assertTrue("value".equals(row.getProperty("field7")));
        Assert.assertTrue("value".equals(row.getProperty("field8")));
        
        //permission readonly & label section - field 9 & 10
        Assert.assertTrue("value".equals(row.getProperty("field9")));
        Assert.assertTrue("value".equals(row.getProperty("field10")));
        
        //permission hidden section - field 11 & 12
        Assert.assertNull(row.getProperty("field11"));
        Assert.assertNull(row.getProperty("field12"));
        
        //readonly fields - field 13 & 14
        Assert.assertTrue("value".equals(row.getProperty("field13")));
        Assert.assertTrue("value".equals(row.getProperty("field14")));
        
        //readonly & label fields - field 15 & 16
        Assert.assertTrue("value".equals(row.getProperty("field15")));
        Assert.assertTrue("value".equals(row.getProperty("field16")));
        
        //hidden fields - field 17 & 18
        Assert.assertNull(row.getProperty("field17"));
        Assert.assertNull(row.getProperty("field18"));
        
        //normal section with store binder - field 19 & 20
        rows = formData.getStoreBinderData(section10.getStoreBinder());
        row = rows.get(0);
        Assert.assertTrue("true".equals(row.getProperty("isExecuted")));
        Assert.assertTrue("value1".equals(row.getProperty("field19")));
        Assert.assertTrue("value1".equals(row.getProperty("field20")));
        
        //readonly section with store binder - field 21 & 22
        rows = formData.getStoreBinderData(section11.getStoreBinder());
        row = rows.get(0);
        Assert.assertNull(row.getProperty("isExecuted"));
        Assert.assertTrue("value".equals(row.getProperty("field21")));
        Assert.assertTrue("value".equals(row.getProperty("field22")));
        
        //permission hidden section with store binder - field 23 & 24 (david has permission)
        rows = formData.getStoreBinderData(section12.getStoreBinder());
        Assert.assertTrue(rows.isEmpty());
        
        //permission readonly section with store binder - field 25 & 26 (david has permission)
        rows = formData.getStoreBinderData(section13.getStoreBinder());
        row = rows.get(0);
        Assert.assertNull(row.getProperty("isExecuted"));
        Assert.assertTrue("value".equals(row.getProperty("field25")));
        Assert.assertTrue("value".equals(row.getProperty("field26")));
    }
    
    @Test
    public void testSameFieldId1() throws IOException  {
        //user: admin
        User user = new User();
        user.setUsername("admin");
        workflowUserManager.setCurrentThreadUser(user);
        
        FormData formData = new FormData();
        Form form = TestUtil.getForm("samefieldid", formData);
        
        Element field1 = FormUtil.findElement("field1", form, formData);
        Assert.assertEquals("admin", FormUtil.getElementPropertyValue(field1, formData));
        
        formData.addRequestParameterValues("field1", new String[]{"test"});
        
        field1 = FormUtil.findElement("field1", form, formData);
        Assert.assertEquals("test", FormUtil.getElementPropertyValue(field1, formData));
    }
    
    @Test
    public void testSameFieldId2() throws IOException  {
        //user: admin
        User user = new User();
        user.setUsername("cat");
        workflowUserManager.setCurrentThreadUser(user);
        
        FormData formData = new FormData();
        Form form = TestUtil.getForm("samefieldid", formData);
        
        Element field1 = FormUtil.findElement("field1", form, formData);
        Assert.assertEquals("cat", FormUtil.getElementPropertyValue(field1, formData));
        
        formData.addRequestParameterValues("field1", new String[]{"test"});
        
        field1 = FormUtil.findElement("field1", form, formData);
        Assert.assertEquals("test", FormUtil.getElementPropertyValue(field1, formData));
    }
    
    protected void populateSubmittedData(FormData formData, String value){
        for (int i = 1; i <= 26; i++) {
            formData.addRequestParameterValues("field"+i, new String[]{value});
        }
    }
    
    protected boolean isSectionHidden(Element e, FormData formData) {
        String template = e.render(formData, false);
        boolean templateCorrect = template.isEmpty();
        
        return FormUtil.isHidden(e, formData) && templateCorrect;
    }
    
    protected boolean isTextFieldEditable(Element e, FormData formData) {
        String template = e.render(formData, false);
        boolean templateCorrect = template.matches("\\s*<div class=\"form-cell\" >\\s+<label field-tooltip=\"[^\"]+\" class=\"label\" for=\"[^\"]+\">TextField <span class=\"form-cell-validator\">\\*</span></label>\\s+<input id=\"[^\"]+\" name=\"[^\"]+\" class=\"[^\"]+\" type=\"text\" placeholder=\"\"  value=\"value\"    />\\s+</div>\\s*");
        
        return !FormUtil.isReadonly(e, formData) && !FormUtil.isHidden(e, formData) && templateCorrect;
    }
    
    protected boolean isTextFieldReadonly(Element e, FormData formData) {
        String template = e.render(formData, false);
        boolean templateCorrect = template.matches("\\s*<div class=\"form-cell\" >\\s+<label field-tooltip=\"[^\"]+\" class=\"label\" for=\"[^\"]+\">TextField <span class=\"form-cell-validator\">\\*</span></label>\\s+<input id=\"[^\"]+\" name=\"[^\"]+\" class=\"[^\"]+\" type=\"text\" placeholder=\"\"  value=\"value\"   readonly />\\s+</div>\\s*");
        
        return FormUtil.isReadonly(e, formData) && templateCorrect;
    }
    
    protected boolean isTextFieldReadonlyLabel(Element e, FormData formData) {
        String template = e.render(formData, false);
        boolean templateCorrect = template.matches("\\s*<div class=\"form-cell\" >\\s+<label field-tooltip=\"[^\"]+\" class=\"label\" for=\"[^\"]+\">TextField <span class=\"form-cell-validator\">\\*</span></label>\\s+<div class=\"form-cell-value\"><span>value</span></div>\\s+<input id=\"[^\"]+\" name=\"[^\"]+\" class=\"[^\"]+\" type=\"hidden\" value=\"value\" />\\s+</div>\\s*");
                
        return FormUtil.isReadonly(e, formData) && "true".equalsIgnoreCase(e.getPropertyString("readonlyLabel")) && templateCorrect;
    }
    
    protected boolean isTextFieldHidden(Element e, FormData formData) {
        String template = e.render(formData, false);
        boolean templateCorrect = template.isEmpty();
        
        return FormUtil.isHidden(e, formData) && templateCorrect;
    }
    
    protected boolean isSelectFieldEditable(Element e, FormData formData) {
        String template = e.render(formData, false);
        boolean templateCorrect = template.matches("\\s*<div class=\"form-cell\" >\\s+<label class=\"label\" for=\"[^\"]+\" field-tooltip=\"[^\"]+\">SelectBox <span class=\"form-cell-validator\">\\*</span></label>\\s+<select id=\"[^\"]+\" name=\"[^\"]+\"    >\\s+<option value=\"value\" grouping=\"\" selected >label</option>\\s+<option value=\"value1\" grouping=\"\"  >label1</option>\\s+</select>\\s+</div>\\s*");
        
        return !FormUtil.isReadonly(e, formData) && !FormUtil.isHidden(e, formData) && templateCorrect;
    }
    
    protected boolean isSelectFieldReadonly(Element e, FormData formData) {
        String template = e.render(formData, false);
        boolean templateCorrect = template.matches("\\s*<div class=\"form-cell\" >\\s+<label class=\"label\" for=\"[^\"]+\" field-tooltip=\"[^\"]+\">SelectBox <span class=\"form-cell-validator\">\\*</span></label>\\s+<select  name=\"[^\"]+\"     disabled >\\s+<option value=\"value\" grouping=\"\" selected disabled>label</option>\\s+<option value=\"value1\" grouping=\"\"  disabled>label1</option>\\s+</select>\\s+<input type=\"hidden\" id=\"[^\"]+\" name=\"[^\"]+\" value=\"value\" />\\s+</div>\\s*");
        
        return FormUtil.isReadonly(e, formData) && templateCorrect;
    }
    
    protected boolean isSelectFieldReadonlyLabel(Element e, FormData formData) {
        String template = e.render(formData, false);
        boolean templateCorrect = template.matches("\\s*<div class=\"form-cell\" >\\s+<label class=\"label\" for=\"[^\"]+\" field-tooltip=\"[^\"]+\">SelectBox <span class=\"form-cell-validator\">\\*</span></label>\\s+<div class=\"form-cell-value\">\\s+<label class=\"readonly_label\">\\s+<span>label</span>\\s+</label>\\s+</div>\\s+<div style=\"clear:both;\"></div>\\s+<input type=\"hidden\" id=\"[^\"]+\" name=\"[^\"]+\" value=\"value\" />\\s+</div>\\s*");
        
        return FormUtil.isReadonly(e, formData) && "true".equalsIgnoreCase(e.getPropertyString("readonlyLabel")) && templateCorrect;
    }
    
    protected boolean isSelectFieldHidden(Element e, FormData formData) {
        String template = e.render(formData, false);
        boolean templateCorrect = template.isEmpty();
        
        return FormUtil.isHidden(e, formData) && templateCorrect;
    }
}
