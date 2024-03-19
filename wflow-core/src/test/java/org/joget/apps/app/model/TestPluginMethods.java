package org.joget.apps.app.model;

import java.io.IOException;
import java.util.Map;
import org.joget.apps.app.service.JsonApiUtil;
import org.joget.apps.form.lib.JsonApiFormOptionsBinder;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.property.service.PropertyUtil;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:testAppsApplicationContext.xml"})
public class TestPluginMethods {
    
    @Test
    public void testJsonApiFormOptionsBinderRecursiveAddOptions() throws IOException {
        FormRowSet options = new FormRowSet();
        options.setMultiRow(true);
        
        //test standard json
        String standardJson = TestUtil.readFile("/jsonresponse/standard.json");
        Map data = PropertyUtil.getProperties(new JSONObject(standardJson));
        
        //without base object
        JsonApiFormOptionsBinder.recursiveAddOptions(data, options, "data[].iso3", null, "data[].country", null, "", null, null);
        Assert.assertTrue(options.size() == 2);
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_VALUE), "AFG");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_LABEL), "Afghanistan");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_GROUPING), null);
        options.clear();
        
        Object baseObject = JsonApiUtil.getObjectFromMap("data", data);
        
        //standard mapping value and lable to object property and no grouping
        JsonApiFormOptionsBinder.recursiveAddOptions(baseObject, options, "iso3", null, "country", null, "", null, "data");
        Assert.assertTrue(options.size() == 2);
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_VALUE), "AFG");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_LABEL), "Afghanistan");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_GROUPING), null);
        options.clear();
        
        //mapping value & label to array string values
        JsonApiFormOptionsBinder.recursiveAddOptions(baseObject, options, "cities[]", null, "cities[]", null, "country", null, "data");
        Assert.assertTrue(options.size() == 7);
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_VALUE), "Herat");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_LABEL), "Herat");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_GROUPING), "Afghanistan");
        Assert.assertEquals(options.get(6).getProperty(FormUtil.PROPERTY_VALUE), "Shkoder");
        Assert.assertEquals(options.get(6).getProperty(FormUtil.PROPERTY_LABEL), "Shkoder");
        Assert.assertEquals(options.get(6).getProperty(FormUtil.PROPERTY_GROUPING), "Albania");
        options.clear();
        
        //mapping for mulitple grouping values
        JsonApiFormOptionsBinder.recursiveAddOptions(baseObject, options, "country", null, "country", null, "cities[]", null, "data");
        Assert.assertTrue(options.size() == 2);
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_VALUE), "Afghanistan");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_LABEL), "Afghanistan");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_GROUPING), "Herat;Kabul;Kandahar");
        options.clear();
        
        //test nested json
        String nestedJson = TestUtil.readFile("/jsonresponse/nested.json");
        data = PropertyUtil.getProperties(new JSONObject(nestedJson));
        
        //without base object
        JsonApiFormOptionsBinder.recursiveAddOptions(data, options, "data[].states[].state_code", null, "data[].states[].name", null, "data[].name", null, null);
        Assert.assertTrue(options.size() == 6);
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_VALUE), "BDS");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_LABEL), "Badakhshan");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_GROUPING), "Afghanistan");
        Assert.assertEquals(options.get(5).getProperty(FormUtil.PROPERTY_VALUE), "02");
        Assert.assertEquals(options.get(5).getProperty(FormUtil.PROPERTY_LABEL), "Adrar Province");
        Assert.assertEquals(options.get(5).getProperty(FormUtil.PROPERTY_GROUPING), "Algeria");
        options.clear();
        
        baseObject = JsonApiUtil.getObjectFromMap("data", data);
        
        //mapping value & label to nested array, grouping to parent object property
        JsonApiFormOptionsBinder.recursiveAddOptions(baseObject, options, "states[].state_code", null, "states[].name", null, "name", null, "data");
        Assert.assertTrue(options.size() == 6);
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_VALUE), "BDS");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_LABEL), "Badakhshan");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_GROUPING), "Afghanistan");
        Assert.assertEquals(options.get(5).getProperty(FormUtil.PROPERTY_VALUE), "02");
        Assert.assertEquals(options.get(5).getProperty(FormUtil.PROPERTY_LABEL), "Adrar Province");
        Assert.assertEquals(options.get(5).getProperty(FormUtil.PROPERTY_GROUPING), "Algeria");
        options.clear();
        
        //mapping grouping to nested array object property
        JsonApiFormOptionsBinder.recursiveAddOptions(baseObject, options, "iso3", null, "name", null, "states[].name", null, "data");
        Assert.assertTrue(options.size() == 3);
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_VALUE), "AFG");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_LABEL), "Afghanistan");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_GROUPING), "Badakhshan;Badghis;Baghlan");
        Assert.assertEquals(options.get(2).getProperty(FormUtil.PROPERTY_VALUE), "DZA");
        Assert.assertEquals(options.get(2).getProperty(FormUtil.PROPERTY_LABEL), "Algeria");
        Assert.assertEquals(options.get(2).getProperty(FormUtil.PROPERTY_GROUPING), "Adrar Province");
        options.clear();
        
        //test object json
        String objectJson = TestUtil.readFile("/jsonresponse/object.json");
        data = PropertyUtil.getProperties(new JSONObject(objectJson));
        
        //without base object
        JsonApiFormOptionsBinder.recursiveAddOptions(data, options, "data<>.KEY", null, "data<>.name", null, "", null, null);
        Assert.assertTrue(options.size() == 3);
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_VALUE), "AFG");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_LABEL), "Afghanistan");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_GROUPING), null);
        Assert.assertEquals(options.get(2).getProperty(FormUtil.PROPERTY_VALUE), "DZA");
        Assert.assertEquals(options.get(2).getProperty(FormUtil.PROPERTY_LABEL), "Algeria");
        Assert.assertEquals(options.get(2).getProperty(FormUtil.PROPERTY_GROUPING), null);
        options.clear();
        
        baseObject = JsonApiUtil.getObjectFromMap("data", data);
        
        //having base object which is not array
        JsonApiFormOptionsBinder.recursiveAddOptions(baseObject, options, "KEY", null, "name", null, "", null, "data<>");
        Assert.assertTrue(options.size() == 3);
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_VALUE), "AFG");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_LABEL), "Afghanistan");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_GROUPING), null);
        Assert.assertEquals(options.get(2).getProperty(FormUtil.PROPERTY_VALUE), "DZA");
        Assert.assertEquals(options.get(2).getProperty(FormUtil.PROPERTY_LABEL), "Algeria");
        Assert.assertEquals(options.get(2).getProperty(FormUtil.PROPERTY_GROUPING), null);
        options.clear();
        
        //mapping value and label to object property key and value. mapping to parent object key
        JsonApiFormOptionsBinder.recursiveAddOptions(baseObject, options, "states<>.KEY", null, "states<>.VALUE", null, "KEY", null, "data<>");
        Assert.assertTrue(options.size() == 6);
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_VALUE), "BDS");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_LABEL), "Badakhshan");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_GROUPING), "AFG");
        Assert.assertEquals(options.get(5).getProperty(FormUtil.PROPERTY_VALUE), "02");
        Assert.assertEquals(options.get(5).getProperty(FormUtil.PROPERTY_LABEL), "Adrar Province");
        Assert.assertEquals(options.get(5).getProperty(FormUtil.PROPERTY_GROUPING), "DZA");
        options.clear();
        
        //mapping value to object value, label to object key. grouping to parent object value
        JsonApiFormOptionsBinder.recursiveAddOptions(baseObject, options, "states<>.VALUE", null, "states<>.KEY", null, "name", null, "data<>");
        Assert.assertTrue(options.size() == 6);
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_VALUE), "Badakhshan");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_LABEL), "BDS");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_GROUPING), "Afghanistan");
        Assert.assertEquals(options.get(5).getProperty(FormUtil.PROPERTY_VALUE), "Adrar Province");
        Assert.assertEquals(options.get(5).getProperty(FormUtil.PROPERTY_LABEL), "02");
        Assert.assertEquals(options.get(5).getProperty(FormUtil.PROPERTY_GROUPING), "Algeria");
        options.clear();
        
        //mapping grouping to an object keys
        JsonApiFormOptionsBinder.recursiveAddOptions(baseObject, options, "KEY", null, "name", null, "states<>.KEY", null, "data<>");
        Assert.assertTrue(options.size() == 3);
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_VALUE), "AFG");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_LABEL), "Afghanistan");
        Assert.assertEquals(options.get(0).getProperty(FormUtil.PROPERTY_GROUPING), "BDS;BDG;BGL");
        Assert.assertEquals(options.get(2).getProperty(FormUtil.PROPERTY_VALUE), "DZA");
        Assert.assertEquals(options.get(2).getProperty(FormUtil.PROPERTY_LABEL), "Algeria");
        Assert.assertEquals(options.get(2).getProperty(FormUtil.PROPERTY_GROUPING), "02");
        options.clear();
    }
}
