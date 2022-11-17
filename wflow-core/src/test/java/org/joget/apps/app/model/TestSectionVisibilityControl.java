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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class TestSectionVisibilityControl {
    @Autowired
    private FormService formService;
    
    @Test
    public void testFirstLevel() throws IOException {
        FormData formData = new FormData();
        Form form = TestUtil.getForm("sectionVisibility", formData);
        
        //test valiadtions
        formData.addRequestParameterValues("field1", new String[]{""});
        formData.addRequestParameterValues("field2", new String[]{""});
        
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.validateFormData(form, formData);
        Map<String, String> errors = formData.getFormErrors();
        
        //normal section - field 1 & 2
        Assert.assertTrue(errors.containsKey("field1"));
        Assert.assertTrue(errors.containsKey("field2"));
        
        //section show when field1 = first & field2 matches non empty string
        Assert.assertTrue(!errors.containsKey("field3"));
        Assert.assertTrue(!errors.containsKey("field4"));
        
        //section show when field1 = second & field2 matches digit
        Assert.assertTrue(!errors.containsKey("field5"));
        Assert.assertTrue(!errors.containsKey("field6"));
        
        //section show when field3 = third & field4 matches alphanumeric
        Assert.assertTrue(!errors.containsKey("field7"));
        Assert.assertTrue(!errors.containsKey("field8"));
        
        //section show when field5 = fourth & field6 matches symbol
        Assert.assertTrue(!errors.containsKey("field9"));
        Assert.assertTrue(!errors.containsKey("field10"));
        
        //populate values which hided all others sections
        formData.addRequestParameterValues("field1", new String[]{"hide"});
        formData.addRequestParameterValues("field2", new String[]{"hide"});
        
        //test store
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.executeFormStoreBinders(form, formData);
        FormRowSet rows = formData.getStoreBinderData(form.getStoreBinder());
        FormRow row = rows.get(0);
        
        //normal section - field 1 & 2
        Assert.assertTrue("hide".equals(row.getProperty("field1")));
        Assert.assertTrue("hide".equals(row.getProperty("field2")));
        
        //section show when field1 = first & field2 matches non empty string
        Assert.assertNull(row.getProperty("field3"));
        Assert.assertNull(row.getProperty("field4"));
        
        //section show when field1 = second & field2 matches digit
        Assert.assertNull(row.getProperty("field5"));
        Assert.assertNull(row.getProperty("field6"));
        
        //section show when field3 = third & field4 matches alphanumeric
        Assert.assertNull(row.getProperty("field7"));
        Assert.assertNull(row.getProperty("field8"));
        
        //section show when field5 = fourth & field6 matches symbol
        Assert.assertNull(row.getProperty("field9"));
        Assert.assertNull(row.getProperty("field10"));
        
        //test load based on submitted values
        form.render(formData, false); //run it once to make sure everything is set
        
        //normal section - field 1 & 2
        Element section1 = FormUtil.findElement("section1", form, formData);
        Assert.assertTrue(!isSectionVisibilityHidden(section1, formData));
        
        //section show when field1 = first & field2 matches non empty string
        Element section2 = FormUtil.findElement("section2", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section2, formData));
        
        //section show when field1 = second & field2 matches digit
        Element section3 = FormUtil.findElement("section3", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section3, formData));
        
        //section show when field3 = third & field4 matches alphanumeric
        Element section4 = FormUtil.findElement("section4", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section4, formData));
        
        //section show when field5 = fourth & field6 matches symbol
        Element section5 = FormUtil.findElement("section5", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section5, formData));
    }
    
    @Test
    public void testSecondlevel1() throws IOException {
        FormData formData = new FormData();
        Form form = TestUtil.getForm("sectionVisibility", formData);
        
        //test valiadtions
        formData.addRequestParameterValues("field1", new String[]{"first"});
        formData.addRequestParameterValues("field2", new String[]{"test"});
        
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.validateFormData(form, formData);
        Map<String, String> errors = formData.getFormErrors();
        
        //normal section - field 1 & 2
        Assert.assertTrue(!errors.containsKey("field1"));
        Assert.assertTrue(!errors.containsKey("field2"));
        
        //section show when field1 = first & field2 matches non empty string
        Assert.assertTrue(errors.containsKey("field3"));
        Assert.assertTrue(errors.containsKey("field4"));
        
        //section show when field1 = second & field2 matches digit
        Assert.assertTrue(!errors.containsKey("field5"));
        Assert.assertTrue(!errors.containsKey("field6"));
        
        //section show when field3 = third & field4 matches alphanumeric
        Assert.assertTrue(!errors.containsKey("field7"));
        Assert.assertTrue(!errors.containsKey("field8"));
        
        //section show when field5 = fourth & field6 matches symbol
        Assert.assertTrue(!errors.containsKey("field9"));
        Assert.assertTrue(!errors.containsKey("field10"));
        
        //populate values which show section 1 & 2
        formData.addRequestParameterValues("field1", new String[]{"first"});
        formData.addRequestParameterValues("field2", new String[]{"test"});
        formData.addRequestParameterValues("field3", new String[]{"hide"});
        formData.addRequestParameterValues("field4", new String[]{"hide"});
        
        //test store
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.executeFormStoreBinders(form, formData);
        FormRowSet rows = formData.getStoreBinderData(form.getStoreBinder());
        FormRow row = rows.get(0);
        
        //normal section - field 1 & 2
        Assert.assertTrue("first".equals(row.getProperty("field1")));
        Assert.assertTrue("test".equals(row.getProperty("field2")));
        
        //section show when field1 = first & field2 matches non empty string
        Assert.assertTrue("hide".equals(row.getProperty("field3")));
        Assert.assertTrue("hide".equals(row.getProperty("field4")));
        
        //section show when field1 = second & field2 matches digit
        Assert.assertNull(row.getProperty("field5"));
        Assert.assertNull(row.getProperty("field6"));
        
        //section show when field3 = third & field4 matches alphanumeric
        Assert.assertNull(row.getProperty("field7"));
        Assert.assertNull(row.getProperty("field8"));
        
        //section show when field5 = fourth & field6 matches symbol
        Assert.assertNull(row.getProperty("field9"));
        Assert.assertNull(row.getProperty("field10"));
        
        //test load based on submitted values
        form.render(formData, false); //run it once to make sure everything is set
        
        //normal section - field 1 & 2
        Element section1 = FormUtil.findElement("section1", form, formData);
        Assert.assertTrue(!isSectionVisibilityHidden(section1, formData));
        
        //section show when field1 = first & field2 matches non empty string
        Element section2 = FormUtil.findElement("section2", form, formData);
        Assert.assertTrue(!isSectionVisibilityHidden(section2, formData));
        
        //section show when field1 = second & field2 matches digit
        Element section3 = FormUtil.findElement("section3", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section3, formData));
        
        //section show when field3 = third & field4 matches alphanumeric
        Element section4 = FormUtil.findElement("section4", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section4, formData));
        
        //section show when field5 = fourth & field6 matches symbol
        Element section5 = FormUtil.findElement("section5", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section5, formData));
    }
    
    @Test
    public void testSecondlevel2() throws IOException {
        FormData formData = new FormData();
        Form form = TestUtil.getForm("sectionVisibility", formData);
        
        //test valiadtions
        formData.addRequestParameterValues("field1", new String[]{"second"});
        formData.addRequestParameterValues("field2", new String[]{"123"});
        
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.validateFormData(form, formData);
        Map<String, String> errors = formData.getFormErrors();
        
        //normal section - field 1 & 2
        Assert.assertTrue(!errors.containsKey("field1"));
        Assert.assertTrue(!errors.containsKey("field2"));
        
        //section show when field1 = first & field2 matches non empty string
        Assert.assertTrue(!errors.containsKey("field3"));
        Assert.assertTrue(!errors.containsKey("field4"));
        
        //section show when field1 = second & field2 matches digit
        Assert.assertTrue(errors.containsKey("field5"));
        Assert.assertTrue(errors.containsKey("field6"));
        
        //section show when field3 = third & field4 matches alphanumeric
        Assert.assertTrue(!errors.containsKey("field7"));
        Assert.assertTrue(!errors.containsKey("field8"));
        
        //section show when field5 = fourth & field6 matches symbol
        Assert.assertTrue(!errors.containsKey("field9"));
        Assert.assertTrue(!errors.containsKey("field10"));
        
        //populate values which show section 1 & 2
        formData.addRequestParameterValues("field1", new String[]{"second"});
        formData.addRequestParameterValues("field2", new String[]{"123"});
        formData.addRequestParameterValues("field5", new String[]{"hide"});
        formData.addRequestParameterValues("field6", new String[]{"hide"});
        
        //test store
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.executeFormStoreBinders(form, formData);
        FormRowSet rows = formData.getStoreBinderData(form.getStoreBinder());
        FormRow row = rows.get(0);
        
        //normal section - field 1 & 2
        Assert.assertTrue("second".equals(row.getProperty("field1")));
        Assert.assertTrue("123".equals(row.getProperty("field2")));
        
        //section show when field1 = first & field2 matches non empty string
        Assert.assertNull(row.getProperty("field3"));
        Assert.assertNull(row.getProperty("field4"));
        
        //section show when field1 = second & field2 matches digit
        Assert.assertTrue("hide".equals(row.getProperty("field5")));
        Assert.assertTrue("hide".equals(row.getProperty("field6")));
        
        //section show when field3 = third & field4 matches alphanumeric
        Assert.assertNull(row.getProperty("field7"));
        Assert.assertNull(row.getProperty("field8"));
        
        //section show when field5 = fourth & field6 matches symbol
        Assert.assertNull(row.getProperty("field9"));
        Assert.assertNull(row.getProperty("field10"));
        
        //test load based on submitted values
        form.render(formData, false); //run it once to make sure everything is set
        
        //normal section - field 1 & 2
        Element section1 = FormUtil.findElement("section1", form, formData);
        Assert.assertTrue(!isSectionVisibilityHidden(section1, formData));
        
        //section show when field1 = first & field2 matches non empty string
        Element section2 = FormUtil.findElement("section2", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section2, formData));
        
        //section show when field1 = second & field2 matches digit
        Element section3 = FormUtil.findElement("section3", form, formData);
        Assert.assertTrue(!isSectionVisibilityHidden(section3, formData));
        
        //section show when field3 = third & field4 matches alphanumeric
        Element section4 = FormUtil.findElement("section4", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section4, formData));
        
        //section show when field5 = fourth & field6 matches symbol
        Element section5 = FormUtil.findElement("section5", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section5, formData));
    }
    
    @Test
    public void testThirdlevel1() throws IOException {
        FormData formData = new FormData();
        Form form = TestUtil.getForm("sectionVisibility", formData);
        
        //test valiadtions
        formData.addRequestParameterValues("field1", new String[]{"first"});
        formData.addRequestParameterValues("field2", new String[]{"test"});
        formData.addRequestParameterValues("field3", new String[]{"third"});
        formData.addRequestParameterValues("field4", new String[]{"123abc"});
        
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.validateFormData(form, formData);
        Map<String, String> errors = formData.getFormErrors();
        
        //normal section - field 1 & 2
        Assert.assertTrue(!errors.containsKey("field1"));
        Assert.assertTrue(!errors.containsKey("field2"));
        
        //section show when field1 = first & field2 matches non empty string
        Assert.assertTrue(!errors.containsKey("field3"));
        Assert.assertTrue(!errors.containsKey("field4"));
        
        //section show when field1 = second & field2 matches digit
        Assert.assertTrue(!errors.containsKey("field5"));
        Assert.assertTrue(!errors.containsKey("field6"));
        
        //section show when field3 = third & field4 matches alphanumeric
        Assert.assertTrue(errors.containsKey("field7"));
        Assert.assertTrue(errors.containsKey("field8"));
        
        //section show when field5 = fourth & field6 matches symbol
        Assert.assertTrue(!errors.containsKey("field9"));
        Assert.assertTrue(!errors.containsKey("field10"));
        
        //populate values which show section 1 & 2
        formData.addRequestParameterValues("field1", new String[]{"first"});
        formData.addRequestParameterValues("field2", new String[]{"test"});
        formData.addRequestParameterValues("field3", new String[]{"third"});
        formData.addRequestParameterValues("field4", new String[]{"123abc"});
        formData.addRequestParameterValues("field7", new String[]{"test"});
        formData.addRequestParameterValues("field8", new String[]{"test"});
        
        //test store
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.executeFormStoreBinders(form, formData);
        FormRowSet rows = formData.getStoreBinderData(form.getStoreBinder());
        FormRow row = rows.get(0);
        
        //normal section - field 1 & 2
        Assert.assertTrue("first".equals(row.getProperty("field1")));
        Assert.assertTrue("test".equals(row.getProperty("field2")));
        
        //section show when field1 = first & field2 matches non empty string
        Assert.assertTrue("third".equals(row.getProperty("field3")));
        Assert.assertTrue("123abc".equals(row.getProperty("field4")));
        
        //section show when field1 = second & field2 matches digit
        Assert.assertNull(row.getProperty("field5"));
        Assert.assertNull(row.getProperty("field6"));
        
        //section show when field3 = third & field4 matches alphanumeric
        Assert.assertTrue("test".equals(row.getProperty("field7")));
        Assert.assertTrue("test".equals(row.getProperty("field8")));
        
        //section show when field5 = fourth & field6 matches symbol
        Assert.assertNull(row.getProperty("field9"));
        Assert.assertNull(row.getProperty("field10"));
        
        //test load based on submitted values
        form.render(formData, false); //run it once to make sure everything is set
        
        //normal section - field 1 & 2
        Element section1 = FormUtil.findElement("section1", form, formData);
        Assert.assertTrue(!isSectionVisibilityHidden(section1, formData));
        
        //section show when field1 = first & field2 matches non empty string
        Element section2 = FormUtil.findElement("section2", form, formData);
        Assert.assertTrue(!isSectionVisibilityHidden(section2, formData));
        
        //section show when field1 = second & field2 matches digit
        Element section3 = FormUtil.findElement("section3", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section3, formData));
        
        //section show when field3 = third & field4 matches alphanumeric
        Element section4 = FormUtil.findElement("section4", form, formData);
        Assert.assertTrue(!isSectionVisibilityHidden(section4, formData));
        
        //section show when field5 = fourth & field6 matches symbol
        Element section5 = FormUtil.findElement("section5", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section5, formData));
    }
    
    @Test
    public void testThirdlevel2() throws IOException {
        FormData formData = new FormData();
        Form form = TestUtil.getForm("sectionVisibility", formData);
        
        //test valiadtions
        formData.addRequestParameterValues("field1", new String[]{"second"});
        formData.addRequestParameterValues("field2", new String[]{"123"});
        formData.addRequestParameterValues("field5", new String[]{"fourth"});
        formData.addRequestParameterValues("field6", new String[]{"!@#$%"});
        
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.validateFormData(form, formData);
        Map<String, String> errors = formData.getFormErrors();
        
        //normal section - field 1 & 2
        Assert.assertTrue(!errors.containsKey("field1"));
        Assert.assertTrue(!errors.containsKey("field2"));
        
        //section show when field1 = first & field2 matches non empty string
        Assert.assertTrue(!errors.containsKey("field3"));
        Assert.assertTrue(!errors.containsKey("field4"));
        
        //section show when field1 = second & field2 matches digit
        Assert.assertTrue(!errors.containsKey("field5"));
        Assert.assertTrue(!errors.containsKey("field6"));
        
        //section show when field3 = third & field4 matches alphanumeric
        Assert.assertTrue(!errors.containsKey("field7"));
        Assert.assertTrue(!errors.containsKey("field8"));
        
        //section show when field5 = fourth & field6 matches symbol
        Assert.assertTrue(errors.containsKey("field9"));
        Assert.assertTrue(errors.containsKey("field10"));
        
        //populate values which show section 1 & 2
        formData.addRequestParameterValues("field1", new String[]{"second"});
        formData.addRequestParameterValues("field2", new String[]{"123"});
        formData.addRequestParameterValues("field5", new String[]{"fourth"});
        formData.addRequestParameterValues("field6", new String[]{"!@#$%"});
        formData.addRequestParameterValues("field9", new String[]{"test"});
        formData.addRequestParameterValues("field10", new String[]{"test"});
        
        //test store
        formData = FormUtil.executeElementFormatDataForValidation(form, formData);
        formData = formService.executeFormStoreBinders(form, formData);
        FormRowSet rows = formData.getStoreBinderData(form.getStoreBinder());
        FormRow row = rows.get(0);
        
        //normal section - field 1 & 2
        Assert.assertTrue("second".equals(row.getProperty("field1")));
        Assert.assertTrue("123".equals(row.getProperty("field2")));
        
        //section show when field1 = first & field2 matches non empty string
        Assert.assertNull(row.getProperty("field3"));
        Assert.assertNull(row.getProperty("field4"));
        
        //section show when field1 = second & field2 matches digit
        Assert.assertTrue("fourth".equals(row.getProperty("field5")));
        Assert.assertTrue("!@#$%".equals(row.getProperty("field6")));
        
        //section show when field3 = third & field4 matches alphanumeric
        Assert.assertNull(row.getProperty("field7"));
        Assert.assertNull(row.getProperty("field8"));
        
        //section show when field5 = fourth & field6 matches symbol
        Assert.assertTrue("test".equals(row.getProperty("field9")));
        Assert.assertTrue("test".equals(row.getProperty("field10")));
        
        //test load based on submitted values
        form.render(formData, false); //run it once to make sure everything is set
        
        //normal section - field 1 & 2
        Element section1 = FormUtil.findElement("section1", form, formData);
        Assert.assertTrue(!isSectionVisibilityHidden(section1, formData));
        
        //section show when field1 = first & field2 matches non empty string
        Element section2 = FormUtil.findElement("section2", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section2, formData));
        
        //section show when field1 = second & field2 matches digit
        Element section3 = FormUtil.findElement("section3", form, formData);
        Assert.assertTrue(!isSectionVisibilityHidden(section3, formData));
        
        //section show when field3 = third & field4 matches alphanumeric
        Element section4 = FormUtil.findElement("section4", form, formData);
        Assert.assertTrue(isSectionVisibilityHidden(section4, formData));
        
        //section show when field5 = fourth & field6 matches symbol
        Element section5 = FormUtil.findElement("section5", form, formData);
        Assert.assertTrue(!isSectionVisibilityHidden(section5, formData));
    }
    
    @Test
    public void testSameFieldId1() throws IOException  {
        try {
            FormData formData = new FormData();
            formData.addRequestParameterValues("id", new String[]{"admin"});
            Form form = TestUtil.getForm("samefieldid", formData);

            Element id = FormUtil.findElement("id", form, formData);
            Assert.assertEquals("admin", FormUtil.getElementPropertyValue(id, formData));

            Element section4 = FormUtil.findElement("section4", form, formData);
            Assert.assertTrue(!isSectionVisibilityHidden(section4, formData));

            Element section5 = FormUtil.findElement("section5", form, formData);
            Assert.assertTrue(isSectionVisibilityHidden(section5, formData));

            Element section6 = FormUtil.findElement("section6", form, formData);
            Assert.assertTrue(!isSectionVisibilityHidden(section6, formData));

            Element section7 = FormUtil.findElement("section7", form, formData);
            Assert.assertTrue(isSectionVisibilityHidden(section7, formData));

            Element field2 = FormUtil.findElement("field2", form, formData);
            Assert.assertEquals("admin", FormUtil.getElementPropertyValue(field2, formData));

            Element field3 = FormUtil.findElement("field3", form, formData);
            Assert.assertEquals("admin", FormUtil.getElementPropertyValue(field3, formData));

            //test store
            formData = FormUtil.executeElementFormatDataForValidation(form, formData);
            formData = formService.executeFormStoreBinders(form, formData);
            FormRowSet rows = formData.getStoreBinderData(form.getStoreBinder());
            FormRow row = rows.get(0);

            Assert.assertEquals("admin", row.getId());
            Assert.assertEquals("admin", row.get("field2"));
            Assert.assertEquals("admin", row.get("field3"));
        } finally {
            TestUtil.deleteAllData("test");
        }
    }
    
    @Test
    public void testSameFieldId2() throws IOException  {
        try {
            FormData formData = new FormData();
            formData.addRequestParameterValues("id", new String[]{"cat"});
            Form form = TestUtil.getForm("samefieldid", formData);

            Element id = FormUtil.findElement("id", form, formData);
            Assert.assertEquals("cat", FormUtil.getElementPropertyValue(id, formData));

            Element section4 = FormUtil.findElement("section4", form, formData);
            Assert.assertTrue(isSectionVisibilityHidden(section4, formData));

            Element section5 = FormUtil.findElement("section5", form, formData);
            Assert.assertTrue(!isSectionVisibilityHidden(section5, formData));

            Element section6 = FormUtil.findElement("section6", form, formData);
            Assert.assertTrue(isSectionVisibilityHidden(section6, formData));

            Element section7 = FormUtil.findElement("section7", form, formData);
            Assert.assertTrue(!isSectionVisibilityHidden(section7, formData));

            Element field2 = FormUtil.findElement("field2", form, formData);
            Assert.assertEquals("cat", FormUtil.getElementPropertyValue(field2, formData));

            Element field3 = FormUtil.findElement("field3", form, formData);
            Assert.assertEquals("cat", FormUtil.getElementPropertyValue(field3, formData));

            //test store
            formData = FormUtil.executeElementFormatDataForValidation(form, formData);
            formData = formService.executeFormStoreBinders(form, formData);
            FormRowSet rows = formData.getStoreBinderData(form.getStoreBinder());
            FormRow row = rows.get(0);

            Assert.assertEquals("cat", row.getId());
            Assert.assertEquals("cat", row.get("field2"));
            Assert.assertEquals("cat", row.get("field3"));
        } finally {
            TestUtil.deleteAllData("test");
        }
    }
    
    protected boolean isSectionVisibilityHidden(Element e, FormData formData) {
        String template = e.render(formData, false);
        
        return template.contains("style=\"display: none\"");
    }
}
