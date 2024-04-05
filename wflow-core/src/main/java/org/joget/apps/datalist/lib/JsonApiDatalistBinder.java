package org.joget.apps.datalist.lib;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.logical.And;
import com.googlecode.cqengine.query.logical.Or;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.displaytag.tags.TableTagParameters;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.JsonApiUtil;
import org.joget.apps.datalist.model.DataList;
import static org.joget.apps.datalist.model.DataList.CHECKBOX_POSITION_BOTH;
import static org.joget.apps.datalist.model.DataList.CHECKBOX_POSITION_LEFT;
import static org.joget.apps.datalist.model.DataList.ORDER_DESCENDING_VALUE;
import org.joget.apps.datalist.model.DataListBinderDefault;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListFilter;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListFilterTypeDefault;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PagingUtils;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.property.service.PropertyUtil;
import org.json.JSONObject;

public class JsonApiDatalistBinder extends DataListBinderDefault {
    
    private final Map<String, Object> sample = new HashMap<String, Object>();
    private final List<String> sampleKeys = new ArrayList<>();
    
    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getName() {
        return "JSON API Data Binder";
    }

    @Override
    public String getVersion() {
        return "8.0.0";
    }

    @Override
    public String getDescription() {
        return "Retrieves data rows from a JSON API.";
    }

    @Override
    public String getLabel() {
        return "JSON API Data Binder";
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/datalist/jsonApiDatalistBinder.json", null, true, null);
    }
    
    @Override
    public DataListColumn[] getColumns() {
        Map<String,Object> results = null;
        if (!getPropertyString("sampleResponse").isEmpty()) {
            String jsonResponse = getPropertyString("sampleResponse").trim();
            if (jsonResponse.startsWith("[") && jsonResponse.endsWith("]")) {
                jsonResponse = "{ \"response\" : " + jsonResponse + " }";
            } else if (!jsonResponse.startsWith("{") && !jsonResponse.endsWith("}")) {
                jsonResponse = "{ \"response\" : \"" + jsonResponse + "\" }";
            }
            if ("true".equalsIgnoreCase(getPropertyString("debugMode"))) {
                LogUtil.info(JsonApiUtil.class.getName(), jsonResponse);
            }
            try {
                results = PropertyUtil.getProperties(new JSONObject(jsonResponse));
            } catch (Exception e) {
                LogUtil.error(getClassName(), e, "");
            }
            if (results == null) {
                results = new HashMap<String, Object>();
            }
            setProperty("jsonResult", results);
        } else {
            results = call(null);
        }
        Map<String, DataListColumn> columns = new HashMap<String, DataListColumn>();
        
        if (results != null) {
            String multirowBaseObject = getPropertyString("multirowBaseObject");
            multirowBaseObject = multirowBaseObject.replaceAll("\\[\\d?\\]", "");
            
            Object o = results;
            String prefix = "";
            
            if (multirowBaseObject.startsWith("<>")) { //need to loop and create row based on the base object key
                Map<String, Object> base = new HashMap<>();
                base.put("data", o);
                o = base;
                multirowBaseObject = "data" + multirowBaseObject;
            }
            
            recursiveGetColumns(o, columns, prefix, multirowBaseObject, "true".equals(getPropertyString("joinObjectKeysAndValues")));
        }
        
        List<DataListColumn> temp = new ArrayList<DataListColumn>(columns.values());
        Collections.sort(temp, new Comparator<DataListColumn>() {
            @Override
            public int compare(DataListColumn a, DataListColumn b)
            {
                return a.getName().compareTo(b.getName());
            }
        });
        
        return temp.toArray(new DataListColumn[0]);
    }
    
    /**
     * Recursively loop the object attribute to create columns
     * @param o
     * @param columns
     * @param prefix
     * @param base
     * @param joinObjectKeysAndValues 
     */
    public static void recursiveGetColumns(Object o,  Map<String, DataListColumn> columns, String prefix, String base, boolean joinObjectKeysAndValues) {
        if (o != null) {
            if (o.getClass().isArray()) { //Looping array object
                Object[] array = (Object[]) o;
                if (array.length > 0) {
                    int max = array.length;  //to prevent empty object, loop a few data
                    if (max > 5) {
                        max = 5;
                    }
                    for (int i = 0; i < max; i++) {
                        String newPrefix = prefix;
                        if (prefix.equals(base)) {
                            newPrefix = "";
                        }
                        if (array[i] instanceof Map) { //it is an Object
                            recursiveGetColumns(array[i], columns, newPrefix, base, joinObjectKeysAndValues);
                        } else {
                            String value = array[i].toString();
                            recursiveGetColumns(value, columns, prefix, base, joinObjectKeysAndValues);
                            break;
                        }
                    }
                }
            } else if (o instanceof Map && base.startsWith(prefix + "<>")) { //loop object key as row
                Map m = (Map) o;
                int max = 0;
                for (Object k : m.keySet()) {
                    String newPrefix = prefix + "<>";
                    if (base.equals(prefix + "<>")) {
                        newPrefix = "";
                    }
                    
                    if (max > 5) { //to prevent empty object, loop a few data
                        break; 
                    }
                   
                    recursiveGetColumns(k, columns, (!newPrefix.isEmpty()?(newPrefix+"."):"") + "KEY", base, joinObjectKeysAndValues);//add key
                    
                    Object value = m.get(k);
                    if (!(value instanceof Map) && newPrefix.isEmpty()) {
                        newPrefix = "VALUE";
                    }
                    recursiveGetColumns(value, columns, newPrefix, base, joinObjectKeysAndValues);
                    
                    max++;
                }
            } else if (o instanceof Map) {
                if (!prefix.isEmpty()) {
                    prefix += ".";
                }

                Map m = (Map) o;
                for (Object k : m.keySet()) {
                    if (!base.startsWith(prefix) && joinObjectKeysAndValues) {
                        recursiveGetColumns(k, columns, prefix + "KEY", base, joinObjectKeysAndValues);
                        recursiveGetColumns(m.get(k), columns, prefix + "VALUE", base, joinObjectKeysAndValues);
                    } else {
                        recursiveGetColumns(m.get(k), columns, prefix + k.toString(), base, joinObjectKeysAndValues);
                    }
                }
            } else {
                String name = prefix.replaceAll(StringUtil.escapeRegex("<>"), "");
                columns.put(name, new DataListColumn(name, name, true));
            }
        } 
    }
    
    @Override
    public String getPrimaryKeyColumnName() {
        return getPropertyString("primaryKey");
    }
    
    @Override
    public DataListCollection getData(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects, String sort, Boolean desc, Integer start, Integer rows) {
        DataListCollection resultList = getJsonApiData(dataList, filterQueryObjects);
        
        if ("true".equals(getPropertyString("handlePaging"))) {
            List newResultList = PagingUtils.sortAndPage(resultList, sort, desc, start, rows);
            resultList = new DataListCollection();
            resultList.addAll(newResultList);
        }
                
        return resultList;
    }
    
    protected DataListCollection getJsonApiData(DataList dataList, DataListFilterQueryObject[] filterQueryObjects) {
        DataListCollection resultList = new DataListCollection();
        if (!getProperties().containsKey("jsonResultList")) {
            Map<String,Object> results = call(dataList);
        
            if (results != null) {
                String multirowBaseObject = getPropertyString("multirowBaseObject");
                multirowBaseObject = multirowBaseObject.replaceAll("\\[\\d?\\]", "");

                Object o = results;
                String prefix = "";
                
                if (multirowBaseObject.startsWith("<>")) { //need to loop and create row based on the base object key
                    Map<String, Object> base = new HashMap<>();
                    base.put("data", o);
                    o = base;
                    multirowBaseObject = "data" + multirowBaseObject;
                }

                recursiveGetData(o, resultList, new HashMap<String, Object>(), prefix, multirowBaseObject, "true".equals(getPropertyString("joinObjectKeysAndValues")), sample);
            }
            
            setProperty("jsonResultList", resultList);
        } else {
            resultList = (DataListCollection) getProperty("jsonResultList");
        }
        
        if ("true".equals(getPropertyString("handleFilters"))) {
            if (filterQueryObjects != null && filterQueryObjects.length > 0) {
                if (!getProperties().containsKey("filteredResults")) {
                    if (sampleKeys.isEmpty()) {
                        sampleKeys.addAll(sample.keySet());
                        
                        Collections.sort(sampleKeys, new Comparator<String>() {

                            @Override
                            public int compare(String o1, String o2) {
                                return o2.length() - o1.length();
                            }
                        });
                    }
                    
                    resultList = filterResult(resultList, filterQueryObjects, sampleKeys, sample);

                    setProperty("filteredResults", resultList);
                } else {
                    resultList = (DataListCollection) getProperty("filteredResults");
                }
            }
        }
        
        return resultList;
    }
    
    /**
     * Using CQ Engine to filter the result
     * 
     * @param resultList
     * @param filterQueryObjects
     * @param sampleKeys
     * @param sample
     * @return 
     */
    public static DataListCollection filterResult(DataListCollection resultList, DataListFilterQueryObject[] filterQueryObjects, List<String> sampleKeys, Map<String, Object> sample) {
        Query<Map> query = translateFiltersToQuery(filterQueryObjects, sampleKeys, sample);
        
        if (query != null) {
            IndexedCollection<Map> data = new ConcurrentIndexedCollection<Map>();
            data.addAll(resultList);
            resultList = new DataListCollection();
            try (ResultSet<Map> filtered = data.retrieve(query)) {
                Iterator it = filtered.iterator();
                while(it.hasNext()) {
                    resultList.add(it.next());
                }
            }
        }
        return resultList;
    }
    
    /**
     * Convert the filter query object to query understand by CQ Engine
     * @param filterQueryObjects
     * @param sampleKeys
     * @param sample
     * @return 
     */
    protected static Query<Map> translateFiltersToQuery(DataListFilterQueryObject[] filterQueryObjects, List<String> sampleKeys, Map<String, Object> sample) {
        Map<String, Attribute> attrs = new HashMap<>();
        
        List<Query<Map>> queries = new ArrayList<Query<Map>>();
        
        for (DataListFilterQueryObject f : filterQueryObjects) {
            Query<Map> q = translateQuery(f.getQuery(), f.getValues(), 0, sampleKeys, sample, attrs);
            if (q != null) {
                queries.add(q);
            }
        }
        
        if (queries.size() > 1) {
            return new And<Map>(queries);
        } else if (queries.size() == 1) {    
            return queries.get(0);
        } else {
            return null;
        }
    }
    
    /**
     * Break the query into the smaller part and translate it to CQ Engine query
     * @param query
     * @param values
     * @param index
     * @param sampleKeys
     * @param sample
     * @param attrs
     * @return 
     */
    protected static Query<Map> translateQuery(String query, String[] values, int index, List<String> sampleKeys, Map<String, Object> sample, Map<String, Attribute> attrs) {
        String lowercaseQuery = query.toLowerCase();
        
        if (lowercaseQuery.contains("and") || lowercaseQuery.contains("or")) {
            List<String> queryParts = new ArrayList<String>();
            String operation = breakQueryParts(query, queryParts);
            if (queryParts.size() > 1) {
                Collection<Query<Map>> queries = new ArrayList<Query<Map>>();
                for (String qp : queryParts) {
                    queries.add(translateQuery(qp, values, index, sampleKeys, sample, attrs));
                    index += StringUtils.countMatches(qp, "?"); //? is the placeholder for value
                }
                if ("AND".equals(operation)) {
                    return new And<Map>(queries);
                } else {
                    return new Or<Map>(queries);
                }
            } else if (queryParts.size() == 1) {
                return translateQuery(queryParts.get(0), values, index, sampleKeys, sample, attrs);
            }
        } else {
            //prepare attribute
            Attribute attr = createAttribute(query, lowercaseQuery, sampleKeys, sample, attrs);
            
            //prepare the compare value
            String value = values[index];
            Comparable cValue = value;
            if (lowercaseQuery.contains("lower(?)")) {
                value = value.toLowerCase();
            } else if (attr.getAttributeType().isAssignableFrom(Integer.class)) {
                cValue = Integer.valueOf(value);
            } else if (attr.getAttributeType().isAssignableFrom(BigDecimal.class)) {
                cValue = BigDecimal.valueOf(Double.valueOf(value));
            } else if (attr.getAttributeType().isAssignableFrom(Double.class)) {
                cValue = Double.valueOf(value);
            } else if (attr.getAttributeType().isAssignableFrom(Boolean.class)) {
                cValue = Boolean.valueOf(value);
            } else {
                cValue = value;
            }
            
            //create query based on operator
            if (lowercaseQuery.contains(" like ")) {
                if (value.startsWith("%") && value.endsWith("%")) {
                    return QueryFactory.contains(attr, value.substring(1, value.length() - 1));
                } else if (value.startsWith("%")) {
                    return QueryFactory.endsWith(attr, value.substring(1));
                } else if (value.endsWith("%")) {
                    return QueryFactory.startsWith(attr, value.substring(0, value.length() - 1));
                }
            } else if (lowercaseQuery.contains(" = ")) {
                return QueryFactory.equal(attr, cValue);
            } else if (lowercaseQuery.contains(" <> ")) {
                return QueryFactory.not(QueryFactory.equal(attr, cValue));
            } else if (lowercaseQuery.contains(" >= ")) {
                return QueryFactory.greaterThanOrEqualTo(attr, cValue);
            } else if (lowercaseQuery.contains(" <= ")) {
                return QueryFactory.lessThanOrEqualTo(attr, cValue);
            } else if (lowercaseQuery.contains(" > ")) {
                return QueryFactory.greaterThan(attr, cValue);
            } else if (lowercaseQuery.contains(" < ")) {
                return QueryFactory.lessThan(attr, cValue);
            }
        }
        return null;
    }
    
    /**
     * Break the query into smaller parts based on parentheses & and/or 
     * @param query
     * @param queryParts
     * @return 
     */
    public static String breakQueryParts(String query, Collection<String> queryParts) {
        int depth = 0;
        int start = 0;
        String operator = "AND";
        String lowercaseQuery = query.toLowerCase();
        
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth == 0 && (lowercaseQuery.startsWith("and", i) || lowercaseQuery.startsWith("or", i))) {
                addQueryParts(query.substring(start, i).trim(), queryParts);
                
                if (lowercaseQuery.startsWith("and", i)) {
                    operator = "AND";
                } else {
                    operator = "OR";
                }
                
                start = i + 3;
            }
        }

        // Add the last part
        addQueryParts(query.substring(start).trim(), queryParts);

        return operator;
    }
    
    public static void addQueryParts(String query, Collection<String> queryParts) {
        if (query.startsWith("(") && query.endsWith(")")) {
            query = query.substring(1, query.length() - 1);
        }
        queryParts.add(query);
    }
    
    /**
     * Create CQ Engine attribute used for query
     * @param query
     * @param lowercaseQuery
     * @param sampleKeys
     * @param sample
     * @param attrs
     * @return 
     */
    public static Attribute createAttribute(final String query, final String lowercaseQuery, List<String> sampleKeys, Map<String, Object> sample, Map<String, Attribute> attrs) {
        //find the name
        String attrname = "";
        
        for (String key : sampleKeys) {
            if (query.contains(key)) {
                attrname = key;
                break;
            }
        }
        final String name = attrname;
        Class type = String.class;
        if (sample.containsKey(name)) {
            type = sample.get(name).getClass();
        }
        
        if (lowercaseQuery.contains("lower(?)")) {
            attrname += "_lower";
        } else if (lowercaseQuery.contains("cast(? as big_decimal)")) {
            attrname += "_decimal";
            type = BigDecimal.class;
        } else if (lowercaseQuery.matches("concat\\(substring\\(.+, \\d+, 4\\), '-', substring\\(.+, \\d+, 2\\), '-', substring\\(.+, \\d+, 2\\), ' ', substring\\(.+, \\d+, 5\\), ':00.0'\\).+")) {
            attrname += "_datetime";
        } else if (lowercaseQuery.matches("concat\\(substring\\(.+, \\d+, 4\\), '-', substring\\(.+, \\d+, 2\\), '-', substring\\(.+, \\d+, 2\\), ' 00:00:00.0'\\).+")) {
            attrname += "_date";
        }
        
        //reuse created attribute for better performance
        if (attrs.containsKey(attrname)) {
            return attrs.get(attrname);
        }
        
        Attribute<Map, Object> attr = new SimpleAttribute<Map, Object>(Map.class, type, attrname) {
            @Override
            public Object getValue(Map map, QueryOptions queryOptions) {
                Object value = map.containsKey(name) ? map.get(name) : null;
                if (value != null) {
                    if (lowercaseQuery.contains("lower(?)")) {
                        value = value.toString().toLowerCase();
                    } else if (lowercaseQuery.contains("cast(? as big_decimal)")) {
                        value = BigDecimal.valueOf(Double.valueOf(value.toString()));
                    } else if (lowercaseQuery.matches("concat\\(substring\\(.+, \\d+, 4\\), '-', substring\\(.+, \\d+, 2\\), '-', substring\\(.+, \\d+, 2\\), ' ', substring\\(.+, \\d+, 5\\), ':00.0'\\).+")
                            || lowercaseQuery.matches("concat\\(substring\\(.+, \\d+, 4\\), '-', substring\\(.+, \\d+, 2\\), '-', substring\\(.+, \\d+, 2\\), ' 00:00:00.0'\\).+")) {
                        String dateValue = "";
                        String valueStr = value.toString();
                        
                        int count = 0;
                        Pattern pattern = Pattern.compile("substring\\([^,]+,\\s*(\\d+),\\s*(\\d+)\\)");
                        Matcher matcher = pattern.matcher(lowercaseQuery);
                        while (matcher.find()) {
                            int start = Integer.parseInt(matcher.group(1)) - 1; //substring in db start at index 1
                            int length = Integer.parseInt(matcher.group(2));
                            
                            if (count == 0) { //year
                                dateValue += valueStr.substring(start, start + length);
                            } else if (count == 1) { //month
                                dateValue += "-" + valueStr.substring(start, start + length);
                            } else if (count == 2) { //day
                                dateValue += "-" + valueStr.substring(start, start + length);
                            } else if (count == 3 && length == 5) { //time
                                dateValue += " " + valueStr.substring(start, start + length) + ":00.0";
                            } else if (count > 3) {
                                break;
                            }
                            
                            count++;
                        }
                        
                        //append time if not yet handle
                        if (!dateValue.contains(":00.0")) {
                            dateValue += " 00:00:00.0";
                        }
                        return dateValue;
                    }
                }
                return value;
            }
        };
        
        attrs.put(attrname, attr);
        
        return attr;
    }
    
    /**
     * Recursively loop all object attribute to create row
     * 
     * @param o
     * @param resultList
     * @param data
     * @param prefix
     * @param base
     * @param joinObjectKeysAndValues
     * @param sample 
     */
    public static void recursiveGetData(Object o, DataListCollection resultList, Map<String, Object> data, String prefix, String base, boolean joinObjectKeysAndValues, Map<String, Object> sample) {
        if (o != null) {
            if (o.getClass().isArray()) { //Looping array object
                Object[] array = (Object[]) o;
                if (array.length > 0) {
                    for (Object rowObj : array) {
                        String newPrefix = prefix;
                        Map<String, Object> rowData = data;
                        if (base.startsWith(prefix)) {
                            rowData = new HashMap<String, Object>();
                            rowData.putAll(data);
                            
                            if (prefix.equals(base)) {
                                newPrefix = "";
                                resultList.add(rowData);
                            }
                        }
            
                        if (rowObj instanceof Map) { //it is an Object
                            recursiveGetData(rowObj, resultList, rowData, newPrefix, base, joinObjectKeysAndValues, sample);
                        } else {
                            recursiveGetData(rowObj, resultList, rowData, prefix, base, joinObjectKeysAndValues, sample);
                        }
                    }
                }
            } else if (o instanceof Map && base.startsWith(prefix + "<>")) { //loop object key as row
                Map m = (Map) o;
                for (Object k : m.keySet()) {
                    String newPrefix = prefix + "<>";
                    Map<String, Object> rowData = new HashMap<String, Object>();
                    rowData.putAll(data);
                    
                    if (base.equals(prefix + "<>")) {
                        newPrefix = "";
                        resultList.add(rowData);
                    }
                   
                    recursiveGetData(k, resultList, rowData, (!newPrefix.isEmpty()?(newPrefix+"."):"") + "KEY", base, joinObjectKeysAndValues, sample); //add key
                    
                    Object value = m.get(k);
                    if (!(value instanceof Map) && newPrefix.isEmpty()) {
                        newPrefix = "VALUE";
                    }
                    recursiveGetData(value, resultList, rowData, newPrefix, base, joinObjectKeysAndValues, sample);
                }
            } else if (o instanceof Map) {
                if (!prefix.isEmpty()) {
                    prefix += ".";
                }

                Object last = null;
                String lastKey = "";
                Map m = (Map) o;
                for (Object k : m.keySet()) {
                    if (!base.startsWith(prefix) && joinObjectKeysAndValues) {
                        recursiveGetData(k, resultList, data, prefix + "KEY", base, joinObjectKeysAndValues, sample);
                        recursiveGetData(m.get(k), resultList, data, prefix + "VALUE", base, joinObjectKeysAndValues, sample);
                    } else if ((prefix + k.toString()).equals(base) || base.startsWith(prefix + k.toString())) {
                        last = m.get(k);
                        lastKey = k.toString();
                    } else {
                        recursiveGetData(m.get(k), resultList, data, prefix + k.toString(), base, joinObjectKeysAndValues, sample);
                    }
                }
                //this is to make sure all attribute are already included before run the looping to create row
                if (last != null) {
                    recursiveGetData(last, resultList, data, prefix + lastKey, base, joinObjectKeysAndValues, sample);
                }
            } else {
                String name = prefix.replaceAll(StringUtil.escapeRegex("<>"), "");
                if (data.containsKey(name)) {
                    data.put(name, data.get(name) + ";" + o);
                } else {
                    data.put(name, o);
                }
                if (!sample.containsKey(name)) {
                    sample.put(name, o);
                }
            }
        } 
    }
    
    @Override
    public int getDataTotalRowCount(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects) {
        int count = 0;
        
        if (getPropertyString("totalRowCountObject").isEmpty()) {
            count = getJsonApiData(dataList, filterQueryObjects).size();
        } else {
            Map<String,Object> results = call(dataList);
            Object c = JsonApiUtil.getObjectFromMap(getPropertyString("totalRowCountObject"), results);
            
            try {
                count = Integer.parseInt(c.toString());
            } catch (Exception e) {
                LogUtil.error(getClassName(), e, "");
            }
        }
        
        return count;
    }
    
    protected Map<String,Object> call(DataList dataList) {
        Map<String,Object> result = null;
        if (!getProperties().containsKey("jsonResult")) {
            Map<String, String> params = null;
            if (dataList != null) {
                params = getQueryParam(dataList);
            }
            result = JsonApiUtil.callApi(getProperties(), params);
            setProperty("jsonResult", result);
        } else {
            result = (Map<String,Object>) getProperty("jsonResult");
        }
        return result;
    }
    
    protected Map<String, String> getQueryParam(DataList dataList) {
        Map<String, String> param = new HashMap<String, String>();

        String page = dataList.getDataListParamString(TableTagParameters.PARAMETER_PAGE);
        String order = dataList.getDataListParamString(TableTagParameters.PARAMETER_ORDER);
        String sort = dataList.getDataListParamString(TableTagParameters.PARAMETER_SORT);
        
        // determine start and size
        Integer recordSize = dataList.getPageSize();
        Integer start = 0;
        
        if (dataList.getDataListParam(TableTagParameters.PARAMETER_EXPORTTYPE) != null && dataList.getDataListParam(TableTagParameters.PARAMETER_EXPORTING) != null) {
            // exporting, set full list
            page = "1";
            dataList.setPageSize(DataList.MAXIMUM_PAGE_SIZE);
            recordSize = DataList.MAXIMUM_PAGE_SIZE;
        }
        if (recordSize == -1) {
            recordSize = null;
        }
        if (page != null && page.trim().length() > 0 && recordSize != null) {
            start = (Integer.parseInt(page) - 1) * recordSize;
        }

        // determine sort column & order direction
        String sortColumn = null;
        Boolean desc = false;
        if (sort != null && !sort.trim().isEmpty()) {
            int sortIndex = Integer.parseInt(sort);

            if (dataList.getCheckboxPosition().equals(CHECKBOX_POSITION_LEFT) || dataList.getCheckboxPosition().equals(CHECKBOX_POSITION_BOTH)) {
                sortIndex = sortIndex - 1;
            }

            DataListColumn[] columns = getColumns();
            if (sortIndex < columns.length) {
                sortColumn = columns[sortIndex].getName();
            }
        } else if (dataList.getDefaultSortColumn() != null && !dataList.getDefaultSortColumn().isEmpty()) {
            sortColumn = dataList.getDefaultSortColumn();
            if (ORDER_DESCENDING_VALUE.equals(dataList.getDefaultOrder())) {
                desc = true;
            }
        }
        if (ORDER_DESCENDING_VALUE.equals(order)) {
            desc = true;
        }

        param.put("sort", sortColumn);
        param.put("desc", Boolean.toString(desc));
        param.put("size", (recordSize == null)?"":Integer.toString(recordSize));
        param.put("rows", (recordSize == null)?"":Integer.toString(recordSize));
        param.put("start", Integer.toString(start));
        param.put("end", Integer.toString(Integer.valueOf((String)param.get("start")) + Integer.valueOf((String)param.get("size"))));
        param.put("order", "false".equals(param.get("desc"))?"acs":"desc");
        param.put("orderUppercase", param.get("order").toUpperCase());
        
        //filter param
        DataListFilter[] filterList = dataList.getFilters();
        if (filterList != null) {
            for (int i = 0; i < filterList.length; i++) {
                DataListFilter filter = filterList[i];
                DataListFilterTypeDefault type = (DataListFilterTypeDefault) filter.getType();
                String[] values = type.getValues(dataList, filter.getName(), type.getPropertyString("defaultValue"));
                if (values != null && values.length > 0) {
                    param.put(filter.getName(), StringUtils.join(values, ";"));
                } else {
                    param.put(filter.getName(), "");
                }
            }
        }

        return param;
    }
    
    @Override
    public String getDeveloperMode() {
        return "advanced";
    }
}
