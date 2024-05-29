package org.joget.apps.app.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.service.JsonApiUtil;
import org.joget.apps.datalist.lib.JsonApiDatalistBinder;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
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
    
    @Test
    public void testJsonApiListBinderBreakQueryParts() throws IOException {
        List<String> queryParts = new ArrayList<>();
        
        String query = "(country like \"%abc%\" and id = \"abc\")";
        String operator = JsonApiDatalistBinder.breakQueryParts(query, queryParts);
        Assert.assertEquals(queryParts.get(0), "country like \"%abc%\" and id = \"abc\"");
        
        queryParts.clear();
        query = "country like \"%abc%\" and id = \"abc\"";
        operator = JsonApiDatalistBinder.breakQueryParts(query, queryParts);
        Assert.assertEquals(operator, "AND");
        Assert.assertEquals(queryParts.get(0), "country like \"%abc%\"");
        Assert.assertEquals(queryParts.get(1), "id = \"abc\"");
        
        queryParts.clear();
        query = "(country like \"%abc%\") or (id = \"abc\")";
        operator = JsonApiDatalistBinder.breakQueryParts(query, queryParts);
        Assert.assertEquals(operator, "OR");
        Assert.assertEquals(queryParts.get(0), "country like \"%abc%\"");
        Assert.assertEquals(queryParts.get(1), "id = \"abc\"");
        
        queryParts.clear();
        query = "((abc = \"123\") and (cdf <= 323) and (sss > 2321 or dsfsf = 24 or (asd = 123 and dsf < 234)) and sdf >= 234) and ddd = 3434";
        operator = JsonApiDatalistBinder.breakQueryParts(query, queryParts);
        Assert.assertEquals(operator, "AND");
        Assert.assertEquals(queryParts.get(0), "(abc = \"123\") and (cdf <= 323) and (sss > 2321 or dsfsf = 24 or (asd = 123 and dsf < 234)) and sdf >= 234");
        Assert.assertEquals(queryParts.get(1), "ddd = 3434");
        
        queryParts.clear();
        query = "(abc = \"123\") and (cdf <= 323) and (sss > 2321 or dsfsf = 24 or (asd = 123 and dsf < 234)) and sdf >= 234";
        operator = JsonApiDatalistBinder.breakQueryParts(query, queryParts);
        Assert.assertEquals(operator, "AND");
        Assert.assertEquals(queryParts.get(0), "abc = \"123\"");
        Assert.assertEquals(queryParts.get(1), "cdf <= 323");
        Assert.assertEquals(queryParts.get(2), "sss > 2321 or dsfsf = 24 or (asd = 123 and dsf < 234)");
        Assert.assertEquals(queryParts.get(3), "sdf >= 234");
        
        queryParts.clear();
        query = "sss > 2321 or dsfsf = 24 or (asd = 123 and dsf < 234)";
        operator = JsonApiDatalistBinder.breakQueryParts(query, queryParts);
        Assert.assertEquals(operator, "OR");
        Assert.assertEquals(queryParts.get(0), "sss > 2321");
        Assert.assertEquals(queryParts.get(1), "dsfsf = 24");
        Assert.assertEquals(queryParts.get(2), "asd = 123 and dsf < 234");
        
        queryParts.clear();
        query = "lower(name) = lower(?) and CONCAT(SUBSTRING(dateCreated, 0, 4), '-', SUBSTRING(dateCreated, 4, 2), '-', SUBSTRING(dateCreated, 6, 2), ' 00:00:00.0') >= ?";
        operator = JsonApiDatalistBinder.breakQueryParts(query, queryParts);
        Assert.assertEquals(operator, "AND");
        Assert.assertEquals(queryParts.get(0), "lower(name) = lower(?)");
        Assert.assertEquals(queryParts.get(1), "CONCAT(SUBSTRING(dateCreated, 0, 4), '-', SUBSTRING(dateCreated, 4, 2), '-', SUBSTRING(dateCreated, 6, 2), ' 00:00:00.0') >= ?");
    }
    
    @Test
    public void testJsonApiListBinderRecursiveGetData() throws IOException {
        DataListCollection resultList = new DataListCollection();
        Map<String, Object> sample = new HashMap<String, Object>();
        
        //normal mapping
        String json = TestUtil.readFile("/jsonresponse/standard.json");
        Map data = JsonApiUtil.getJsonObjectMap(new JSONObject(json));
        JsonApiDatalistBinder.recursiveGetData(data, resultList, new HashMap<String, Object>(), "", "data", false, sample);
        Assert.assertEquals(resultList.size(), 2);
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(0)).keySet(), ", "), "msg, country, error, cities, iso2, iso3");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(0)).values(), ", "), "countries and cities retrieved, Afghanistan, false, Herat;Kabul;Kandahar, AF, AFG");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(1)).values(), ", "), "countries and cities retrieved, Albania, false, Elbasan;Petran;Pogradec;Shkoder, AL, ALB");
        
        //get rows based inner array attribute
        resultList.clear();
        sample.clear();
        JsonApiDatalistBinder.recursiveGetData(data, resultList, new HashMap<String, Object>(), "", "data.cities", false, sample);
        Assert.assertEquals(resultList.size(), 7);
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(0)).keySet(), ", "), "msg, error, data.country, data.iso3, data.iso2, data.cities");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(0)).values(), ", "), "countries and cities retrieved, false, Afghanistan, AFG, AF, Herat");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(1)).values(), ", "), "countries and cities retrieved, false, Afghanistan, AFG, AF, Kabul");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(5)).values(), ", "), "countries and cities retrieved, false, Albania, ALB, AL, Pogradec");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(6)).values(), ", "), "countries and cities retrieved, false, Albania, ALB, AL, Shkoder");
        
        //get inner object array as column
        resultList.clear();
        sample.clear();
        json = TestUtil.readFile("/jsonresponse/nested.json");
        data = JsonApiUtil.getJsonObjectMap(new JSONObject(json));
        JsonApiDatalistBinder.recursiveGetData(data, resultList, new HashMap<String, Object>(), "", "data", false, sample);
        Assert.assertEquals(resultList.size(), 3);
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(0)).keySet(), ", "), "msg, name, error, iso2, iso3, states.name, states.state_code");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(0)).values(), ", "), "countries and states retrieved, Afghanistan, false, AF, AFG, Badakhshan;Badghis;Baghlan, BDS;BDG;BGL");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(1)).values(), ", "), "countries and states retrieved, Albania, false, AL, ALB, Berat County;Berat District, 01;BR");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(2)).values(), ", "), "countries and states retrieved, Algeria, false, DZ, DZA, Adrar Province, 02");
        
        //get inner object array as row
        resultList.clear();
        sample.clear();
        JsonApiDatalistBinder.recursiveGetData(data, resultList, new HashMap<String, Object>(), "", "data.states", false, sample);
        Assert.assertEquals(resultList.size(), 6);
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(0)).keySet(), ", "), "msg, name, error, state_code, data.iso3, data.name, data.iso2");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(0)).values(), ", "), "countries and states retrieved, Badakhshan, false, BDS, AFG, Afghanistan, AF");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(1)).values(), ", "), "countries and states retrieved, Badghis, false, BDG, AFG, Afghanistan, AF");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(5)).values(), ", "), "countries and states retrieved, Adrar Province, false, 02, DZA, Algeria, DZ");
        
        //loop object key as row
        resultList.clear();
        sample.clear();
        json = TestUtil.readFile("/jsonresponse/object.json");
        data = JsonApiUtil.getJsonObjectMap(new JSONObject(json));
        JsonApiDatalistBinder.recursiveGetData(data, resultList, new HashMap<String, Object>(), "", "data<>", true, sample);
        Assert.assertEquals(resultList.size(), 3);
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(0)).keySet(), ", "), "msg, name, states.KEY, error, states.VALUE, KEY");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(0)).values(), ", "), "countries and states retrieved, Afghanistan, BDS;BDG;BGL, false, Badakhshan;Badghis;Baghlan, AFG");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(1)).values(), ", "), "countries and states retrieved, Albania, BR;01, false, Berat District;Berat County, ALB");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(2)).values(), ", "), "countries and states retrieved, Algeria, 02, false, Adrar Province, DZA");
        
        //loop inner object key as row
        resultList.clear();
        sample.clear();
        JsonApiDatalistBinder.recursiveGetData(data, resultList, new HashMap<String, Object>(), "", "data<>.states<>", false, sample);
        Assert.assertEquals(resultList.size(), 6);
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(0)).keySet(), ", "), "msg, data.KEY, VALUE, error, data.name, KEY");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(0)).values(), ", "), "countries and states retrieved, AFG, Badakhshan, false, Afghanistan, BDS");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(1)).values(), ", "), "countries and states retrieved, AFG, Badghis, false, Afghanistan, BDG");
        Assert.assertEquals(StringUtils.join(((Map)resultList.get(5)).values(), ", "), "countries and states retrieved, DZA, Adrar Province, false, Algeria, 02");
    }
    
    @Test
    public void testJsonApiListBinderRecursiveGetColumns() throws IOException {
        Map<String, DataListColumn> columns = new HashMap<String, DataListColumn>();
        
        //normal mapping
        String json = TestUtil.readFile("/jsonresponse/standard.json");
        Map data = JsonApiUtil.getJsonObjectMap(new JSONObject(json));
        JsonApiDatalistBinder.recursiveGetColumns(data, columns, "", "data", false);
        Assert.assertEquals(StringUtils.join(columns.keySet(), ", "), "msg, country, cities, iso2, error, iso3");
        
        //get rows based inner array attribute
        columns.clear();
        JsonApiDatalistBinder.recursiveGetColumns(data, columns, "", "data.cities", false);
        Assert.assertEquals(StringUtils.join(columns.keySet(), ", "), "msg, data.cities, error, data.country, data.iso3, data.iso2");
        
        //get inner object array as column
        columns.clear();
        json = TestUtil.readFile("/jsonresponse/nested.json");
        data = JsonApiUtil.getJsonObjectMap(new JSONObject(json));
        JsonApiDatalistBinder.recursiveGetColumns(data, columns, "", "data", false);
        Assert.assertEquals(StringUtils.join(columns.keySet(), ", "), "msg, name, iso2, error, iso3, states.name, states.state_code");
        
        //get inner object array as row
        columns.clear();
        JsonApiDatalistBinder.recursiveGetColumns(data, columns, "", "data.states", false);
        Assert.assertEquals(StringUtils.join(columns.keySet(), ", "), "msg, name, state_code, error, data.iso3, data.name, data.iso2");
        
        //loop object key as row
        columns.clear();
        json = TestUtil.readFile("/jsonresponse/object.json");
        data = JsonApiUtil.getJsonObjectMap(new JSONObject(json));
        JsonApiDatalistBinder.recursiveGetColumns(data, columns, "", "data<>", true);
        Assert.assertEquals(StringUtils.join(columns.keySet(), ", "), "msg, states.VALUE, name, states.KEY, error, KEY");
        
        //loop inner object key as row
        columns.clear();
        JsonApiDatalistBinder.recursiveGetColumns(data, columns, "", "data<>.states<>", false);
        Assert.assertEquals(StringUtils.join(columns.keySet(), ", "), "msg, data.KEY, VALUE, error, data.name, KEY");
    }
    
    @Test
    public void testJsonApiListBinderFilterResult() throws IOException {
        DataListCollection resultList = new DataListCollection();
        Map<String, Object> sample = new HashMap<String, Object>();
        List<String> sampleKeys = new ArrayList<>();
                
        //preparing data
        String json = TestUtil.readFile("/jsonresponse/dataType.json");
        Map data = JsonApiUtil.getJsonObjectMap(new JSONObject(json));
        JsonApiDatalistBinder.recursiveGetData(data, resultList, new HashMap<String, Object>(), "", "data", false, sample);
        
        //sort by length
        sampleKeys.addAll(sample.keySet());
        Collections.sort(sampleKeys, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });
        
        Collection<DataListFilterQueryObject> filters = new ArrayList<DataListFilterQueryObject>();
        
        //stringVal contains 'b'
        DataListFilterQueryObject f1 = new DataListFilterQueryObject();
        f1.setQuery("lower(stringVal) like lower(?)");
        f1.setValues(new String[]{"%b%"});
        filters.add(f1);
        DataListCollection filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 2);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "233, 2311.23, 2024-04-01T15:25:43.511Z, Albania, false");
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(1)).values(), ", "), "33, 433.23, 2024-04-20T18:25:43.511Z, Berat County, true");
        
        //stringVal contains 'b' and booleanVal is true
        DataListFilterQueryObject f2 = new DataListFilterQueryObject();
        f2.setQuery("booleanVal = ?");
        f2.setValues(new String[]{"true"});
        filters.add(f2);
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 1);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "33, 433.23, 2024-04-20T18:25:43.511Z, Berat County, true");
        
        //stringVal contains 'b' and booleanVal is false
        f2.setValues(new String[]{"false"});
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 1);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "233, 2311.23, 2024-04-01T15:25:43.511Z, Albania, false");
        
        //stringVal contains 'b' and intVal = 33
        f2.setQuery("intVal = ?");
        f2.setValues(new String[]{"33"});
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 1);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "33, 433.23, 2024-04-20T18:25:43.511Z, Berat County, true");
        
        //stringVal contains 'b' and intVal > 100
        f2.setQuery("intVal > ?");
        f2.setValues(new String[]{"100"});
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 1);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "233, 2311.23, 2024-04-01T15:25:43.511Z, Albania, false");
        
        //stringVal contains 'b' and intVal < 100
        f2.setQuery("intVal > ?");
        f2.setValues(new String[]{"100"});
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 1);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "233, 2311.23, 2024-04-01T15:25:43.511Z, Albania, false");
        
        //stringVal contains 'a' and intVal <= 32
        f1.setValues(new String[]{"%a%"});
        f2.setQuery("intVal <= ?");
        f2.setValues(new String[]{"32"});
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 3);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "10, 345.23, 2024-04-23T18:25:43.511Z, Afghanistan, true");
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(2)).values(), ", "), "3, 1.23, 2024-04-01T18:25:43.511Z, Algeria, true");
        
        //stringVal contains 'a' and intVal >= 32
        f2.setQuery("intVal >= ?");
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 4);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "233, 2311.23, 2024-04-01T15:25:43.511Z, Albania, false");
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(3)).values(), ", "), "32, 12.23, 2024-04-25T18:25:43.511Z, Kandahar, false");
        
        //(stringVal contains 'f' or stringVal contains 'l') and intVal >= 32
        f1.setQuery("(lower(stringVal) like lower(?) or lower(stringVal) like lower(?))");
        f1.setValues(new String[]{"%f%", "%l%"});
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 1);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "233, 2311.23, 2024-04-01T15:25:43.511Z, Albania, false");
        
        //StringVal startWith `Al` and doubleVal is 2311.23
        f1.setQuery("lower(stringVal) like lower(?)");
        f1.setValues(new String[]{"Al%"});
        f2.setQuery("doubleVal = ?");
        f2.setValues(new String[]{"2311.23"});
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 1);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "233, 2311.23, 2024-04-01T15:25:43.511Z, Albania, false");
        
        //StringVal startWith `Al` and doubleVal is not 2311.23
        f1.setQuery("lower(stringVal) like lower(?)");
        f1.setValues(new String[]{"Al%"});
        f2.setQuery("doubleVal <> ?");
        f2.setValues(new String[]{"2311.23"});
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 1);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "3, 1.23, 2024-04-01T18:25:43.511Z, Algeria, true");
    
        //StringVal endWith `County` and doubleVal <= 500.20
        f1.setValues(new String[]{"%County"});
        f2.setQuery("doubleVal <= ?");
        f2.setValues(new String[]{"500.20"});
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 1);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "33, 433.23, 2024-04-20T18:25:43.511Z, Berat County, true");
        
        //StringVal = `Afghanistan` and cast doubleVal <= 500.20
        f1.setQuery("stringVal = ?");
        f1.setValues(new String[]{"Afghanistan"});
        f2.setQuery("cast(doubleVal as big_decimal) <= cast(? as big_decimal)");
        f2.setValues(new String[]{"500.20"});
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 1);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "10, 345.23, 2024-04-23T18:25:43.511Z, Afghanistan, true");
        
        //stringVal contains 'a' and dateVal range from 2024-04-22 00:00:00.0 to 2024-04-24 00:00:00.0
        f1.setQuery("lower(stringVal) like lower(?)");
        f1.setValues(new String[]{"%a%"});
        f2.setQuery("CONCAT(SUBSTRING(dateVal, 1, 4), '-', SUBSTRING(dateVal, 6, 2), '-', SUBSTRING(dateVal, 9, 2), ' ', SUBSTRING(dateVal, 12, 5), ':00.0') >= ? and CONCAT(SUBSTRING(dateVal, 1, 4), '-', SUBSTRING(dateVal, 6, 2), '-', SUBSTRING(dateVal, 9, 2), ' ', SUBSTRING(dateVal, 12, 5), ':00.0') <= ?");
        f2.setValues(new String[]{"2024-04-22 00:00:00.0", "2024-04-24 00:00:00.0"});
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 2);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "10, 345.23, 2024-04-23T18:25:43.511Z, Afghanistan, true");
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(1)).values(), ", "), "122, 0.23, 2024-04-22T18:25:43.511Z, Adrar Province, true");
        
        //stringVal contains 'a' and dateVal range from 2024-04-22 00:00:00.0 to 2024-04-24 00:00:00.0
        f1.setQuery("lower(stringVal) like lower(?)");
        f1.setValues(new String[]{"%a%"});
        f2.setQuery("CONCAT(SUBSTRING(dateVal, 1, 4), '-', SUBSTRING(dateVal, 6, 2), '-', SUBSTRING(dateVal, 9, 2), ' 00:00:00.0') >= ? and CONCAT(SUBSTRING(dateVal, 1, 4), '-', SUBSTRING(dateVal, 6, 2), '-', SUBSTRING(dateVal, 9, 2), ' 00:00:00.0') <= ?");
        f2.setValues(new String[]{"2024-04-22 00:00:00.0", "2024-04-24 00:00:00.0"});
        filtered = JsonApiDatalistBinder.filterResult(resultList, filters.toArray(new DataListFilterQueryObject[0]), sampleKeys, sample);
        Assert.assertEquals(filtered.size(), 2);
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(0)).values(), ", "), "10, 345.23, 2024-04-23T18:25:43.511Z, Afghanistan, true");
        Assert.assertEquals(StringUtils.join(((Map)filtered.get(1)).values(), ", "), "122, 0.23, 2024-04-22T18:25:43.511Z, Adrar Province, true");
        
        
    }
}
