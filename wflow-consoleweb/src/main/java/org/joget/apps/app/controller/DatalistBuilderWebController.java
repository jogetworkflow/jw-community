package org.joget.apps.app.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections.map.ListOrderedMap;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListBinder;
import org.joget.apps.datalist.model.DataListBuilderProperty;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListFilter;
import org.joget.apps.datalist.service.DataListDecorator;
import org.joget.apps.datalist.service.DataListService;
import org.joget.commons.util.CsvUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ui.ModelMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DatalistBuilderWebController {

    public static final String PREFIX_SELECTED = "selected_";
    public static final String PREFIX_BINDER_PROPERTY = "binder_";
    @Autowired
    DataListService dataListService;
    @Autowired
    AppService appService;
    @Autowired
    DatalistDefinitionDao datalistDefinitionDao;

    @RequestMapping("/console/app/(*:appId)/(*:version)/datalist/builder/(*:id)")
    public String builder(ModelMap map, @RequestParam("appId") String appId, @RequestParam("version") String version, @RequestParam("id") String id, @RequestParam(required = false) String json) throws Exception {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        map.addAttribute("appId", appId);
        map.addAttribute("appVersion", appDef.getVersion());
        map.addAttribute("appDefinition", appDef);

        DatalistDefinition datalist = datalistDefinitionDao.loadById(id, appDef);
        String listJson = null;
        if (json != null && !json.trim().isEmpty()) {
            // read custom JSON from request
            listJson = json;
        } else {
            // get JSON from form definition
            listJson = datalist.getJson();
        }

        map.addAttribute("id", id);
        map.addAttribute("datalist", datalist);
        map.addAttribute("json", listJson);
        return "dbuilder/builder";
    }

    @RequestMapping(value = "/console/app/(*:appId)/(*:version)/datalist/builderSave/(*:id)", method = RequestMethod.POST)
    public void save(Writer writer, @RequestParam("appId") String appId, @RequestParam("version") String version, @RequestParam("id") String id, @RequestParam("json") String json) throws Exception {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        DatalistDefinition datalist = datalistDefinitionDao.loadById(id, appDef);
        DataList dlist = dataListService.fromJson(json);
        datalist.setName(dlist.getName());
        datalist.setDescription(dlist.getName());
        datalist.setJson(json);

        boolean success = datalistDefinitionDao.update(datalist);
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("success", success);
        jsonObject.write(writer);
    }

    @RequestMapping(value = {"/console/app/(*:appId)/(*:appVersion)/datalist/builderPreview/(*:id)", "/client/app/(*:appId)/(*:appVersion)/datalist/(*:id)"})
    public String preview(ModelMap map, HttpServletRequest request, @RequestParam("appId") String appId, @RequestParam("appVersion") String appVersion, @RequestParam("id") String id, @RequestParam(required = false) String json) throws Exception {
        String view = "dbuilder/view";

        // get current app to set into thread
        appService.getAppDefinition(appId, appVersion);

        // get parameters
        String sortParam = new ParamEncoder(id).encodeParameterName(TableTagParameters.PARAMETER_SORT);
        String orderParam = new ParamEncoder(id).encodeParameterName(TableTagParameters.PARAMETER_ORDER);
        String pageParam = new ParamEncoder(id).encodeParameterName(TableTagParameters.PARAMETER_PAGE);
        String filterNameParam = new ParamEncoder(id).encodeParameterName(DataListFilter.PARAMETER_FILTER_NAME);
        String filterValueParam = new ParamEncoder(id).encodeParameterName(DataListFilter.PARAMETER_FILTER_VALUE);
        String filterOptionParam = new ParamEncoder(id).encodeParameterName(DataListFilter.PARAMETER_FILTER_OPTION);
        String exportParam = new ParamEncoder(id).encodeParameterName(TableTagParameters.PARAMETER_EXPORTTYPE);
        String sort = request.getParameter(sortParam);
        String order = request.getParameter(orderParam);
        String filterName = request.getParameter(filterNameParam);
        String filterValue = request.getParameter(filterValueParam);
        String filterOption = request.getParameter(filterOptionParam);
        String export = request.getParameter(exportParam);

        try {
            // get data list
            DataList dataList = new DataList();
            if (json != null && !json.trim().isEmpty()) {
                dataList = dataListService.fromJson(json);
                map.addAttribute("json", json);
            } else {
                dataList = parseFromJsonParameter(map, dataList, id, request);
            }

            // determine sort column
            String sortColumn = null;
            if (sort != null && !sort.trim().isEmpty()) {
                int sortIndex = Integer.parseInt(sort) - 1;
                DataListColumn[] columns = dataList.getColumns();
                if (sortIndex < columns.length) {
                    sortColumn = columns[sortIndex].getName();
                }
            }

            // determine order
            String dir = null;
            if ("2".equals(order)) {
                dir = "asc";
            } else if ("1".equals(order)) {
                dir = "desc";
            }

            // determine start and size
            String page = request.getParameter(pageParam);
            int pageSize = dataList.getPageSize();
            int start = 0;
            if (export == null) {
                if (page != null && page.trim().length() > 0) {
                    start = (Integer.parseInt(page) - 1) * pageSize;
                }
            } else {
                // exporting, set full list
                pageSize = DataList.MAXIMUM_PAGE_SIZE;
            }

            // determine filter name and value
            if (filterValue == null || filterValue.trim().length() == 0) {
                if (filterOption != null && filterOption.trim().length() > 0) {
                    StringTokenizer st = new StringTokenizer(filterOption, DataListFilter.FILTER_OPTION_DELIMITER);
                    if (st.hasMoreTokens()) {
                        filterName = st.nextToken();
                        filterValue = (st.hasMoreTokens()) ? st.nextToken() : "";
                    }
                }
            }

            // set filter param names
            map.addAttribute("filterNameParam", filterNameParam);
            map.addAttribute("filterValueParam", filterValueParam);
            map.addAttribute("filterOptionParam", filterOptionParam);

            // set data list
            map.addAttribute("dataListId", dataList.getId());
            map.addAttribute("dataList", dataList);

            DataListBinder binder = dataList.getBinder();
            if (binder == null) {
                String message = "No binder or colums specified";
                map.addAttribute("error", message);
                return view;
            }
            Properties binderProperties = dataList.getBinder().getProperties();

            // set data rows
            Boolean desc = null;
            if (dir != null) {
                desc = ("desc".equals(dir)) ? Boolean.TRUE : Boolean.FALSE;
            }
            DataListCollection rows = binder.getData(dataList, binderProperties, filterName, filterValue, sortColumn, desc, start, pageSize);
            map.addAttribute("dataListRows", rows);
            map.addAttribute("dataListSize", rows.getFullListSize());
            map.addAttribute("dataListPageSize", pageSize);

            // set filters
            Collection<DataListFilter> filterList = new ArrayList<DataListFilter>();
            DataListFilter[] filters = dataList.getFilters();
            if (filters != null && filters.length > 0) {
                filterList.addAll(Arrays.asList(filters));
            }
            Map<String, String> textfieldFilterMap = new ListOrderedMap(); // filter with open options (textfield)
            Map<String, String> selectBoxFilterMap = new ListOrderedMap(); // filter with fixed options (selectbox)
            for (DataListFilter filter : filterList) {
                if (filter.getOptions() != null) {
                    // add selectbox option header
                    selectBoxFilterMap.put(DataListFilter.FILTER_HEADER, filter.getLabel());
                    String[][] options = filter.getOptions();
                    for (int i = 0; i < options.length; i++) {
                        String key = options[i][0];
                        String value = options[i][1];
                        selectBoxFilterMap.put(filter.getName() + DataListFilter.FILTER_OPTION_DELIMITER + key, value);
                    }
                } else {
                    textfieldFilterMap.put(filter.getName(), filter.getLabel());
                }
            }
            map.addAttribute("textfieldFilterMap", textfieldFilterMap);
            map.addAttribute("selectBoxFilterMap", selectBoxFilterMap);

            // set checkbox
            String key = binder.getPrimaryKeyColumnName();
            DataListDecorator decorator = new DataListDecorator();
            decorator.setId(key);
            decorator.setFieldName(PREFIX_SELECTED + key);
            map.addAttribute("decorator", decorator);
        } catch (Exception ex) {
            StringWriter out = new StringWriter();
            ex.printStackTrace(new PrintWriter(out));
            String message = ex.toString();
            message += "\r\n<pre class=\"stacktrace\">" + out.getBuffer() + "</pre>";
            map.addAttribute("error", message);
        }

        // set map into model to be used in the JSP template
        map.addAttribute("properties", new HashMap(map));
        return view;
    }

    @RequestMapping("/json/console/app/(*:appId)/(*:appVersion)/builder/binders")
    public void getBuilderDataBinderList(Writer writer, @RequestParam("appId") String appId, @RequestParam(required = false) String appVersion) throws Exception {
        appService.getAppDefinition(appId, appVersion);
        JSONObject jsonObject = new JSONObject();

        // get available binders
        DataListBinder[] binders = dataListService.getAvailableBinders();

        Collection<Object> collection = new ArrayList<Object>();
        for (DataListBinder binder : binders) {
            HashMap hm = new HashMap();
            hm.put("name", binder.getName());
            hm.put("className", binder.getClassName());
            hm.put("type", "text");
            collection.add(hm);
        }
        jsonObject.accumulate("binders", collection);
        jsonObject.write(writer);
    }

    @RequestMapping("/json//console/app/(*:appId)/(*:appVersion)/builder/binder/options")
    public void getBuilderDataBinderOption(ModelMap map, Writer writer, @RequestParam("appId") String appId, @RequestParam(required = false) String appVersion, @RequestParam("id") String id, @RequestParam String value, HttpServletRequest request) throws Exception {
        appService.getAppDefinition(appId, appVersion);
        JSONObject jsonObject = new JSONObject();

        // get data list
        DataList dataList = new DataList();// = dataListService.getDataList(id);

        // clear columns
        dataList.setColumns(null);

        // parse JSON from request if available
        dataList = parseFromJsonParameter(map, dataList, id, request);

        // get binder from request
        DataListBinder binder = createDataListBinderFromRequestInternal(value, request);
        if (binder != null) {
            dataList.setBinder(binder);
        }

        String jsonStr = "";

        // get binder properties
        binder = dataList.getBinder();
        if (binder instanceof PropertyEditable) {
            jsonStr = ((PropertyEditable) binder).getPropertyOptions();
        } else {
            DataListBuilderProperty[] options = null;
            try {
                options = binder.getBuilderProperties();
                Properties props = binder.getProperties();
                if (options != null && props != null) {
                    for (DataListBuilderProperty option : options) {
                        String val = props.getProperty(option.getName());
                        if (val != null) {
                            option.setValue(val);
                        }
                    }
                }
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "getBuilderDataOption Error!");
            }

            Collection<Object> propertyCollection = new ArrayList<Object>();
            for (DataListBuilderProperty option : options) {
                HashMap propertyHm = new HashMap();
                propertyHm.put("label", option.getLabel());
                propertyHm.put("name", option.getName());
                propertyHm.put("value", option.getValue());

                if (option.getOptions() != null) {
                    propertyHm.put("type", "selectbox");
                    Collection<Object> propertyOptions = new ArrayList<Object>();
                    for (String temp : option.getOptions()) {
                        HashMap tempHm = new HashMap();
                        tempHm.put("value", temp);
                        tempHm.put("label", temp);
                        propertyOptions.add(tempHm);
                    }
                    propertyHm.put("options", propertyOptions);
                } else if (DataListBuilderProperty.TYPE_TEXTAREA.equals(option.getType())) {
                    propertyHm.put("type", "textarea");
                    propertyHm.put("cols", "50");
                    propertyHm.put("rows", "10");
                } else {
                    propertyHm.put("type", "textfield");
                    propertyHm.put("size", "40");
                }
                propertyCollection.add(propertyHm);
            }

            HashMap page = new HashMap();
            page.put("title", "Configure Binder");
            page.put("properties", propertyCollection);

            Collection<Object> definition = new ArrayList<Object>();
            definition.add(page);

            jsonObject.accumulate("", definition);
            jsonStr = jsonObject.toString();
            jsonStr = jsonStr.substring(4, jsonStr.length() - 1);
        }

        writer.write(jsonStr);
    }

    @RequestMapping("/json/console/app/(*:appId)/(*:appVersion)/builder/column/actions")
    public void getBuilderDataColumnActionList(ModelMap map, Writer writer, @RequestParam("appId") String appId, @RequestParam(required = false) String appVersion, HttpServletRequest request) throws Exception {
        appService.getAppDefinition(appId, appVersion);
        JSONArray jsonArray = new JSONArray();

        // get available actions
        DataListAction[] actions = dataListService.getAvailableActions();

        HashMap none = new HashMap();
        none.put("label", "");
        none.put("value", "");
        jsonArray.put(none);
        for (DataListAction action : actions) {
            HashMap hm = new HashMap();
            hm.put("name", action.getName());
            hm.put("label", action.getLabel());
            hm.put("value", action.getClassName());
            hm.put("className", action.getClassName());
            hm.put("type", "text");
            jsonArray.put(hm);
        }
        jsonArray.write(writer);
    }

    @RequestMapping("/json/console/app/(*:appId)/(*:appVersion)/builder/column/action/properties")
    public void getBuilderDataColumnActionProperties(ModelMap map, Writer writer, @RequestParam("appId") String appId, @RequestParam(required = false) String appVersion, @RequestParam String value, HttpServletRequest request) throws Exception {
        appService.getAppDefinition(appId, appVersion);
        String json = "";

        if (value != null && !value.trim().isEmpty()) {
            DataListAction action = dataListService.getAction(value);
            if (action != null && action instanceof PropertyEditable) {
                json = ((PropertyEditable) action).getPropertyOptions();
            }
        }

        writer.write(json);
    }

    @RequestMapping("/json/console/app/(*:appId)/(*:appVersion)/builder/actions")
    public void getBuilderDataActionList(ModelMap map, Writer writer, @RequestParam("appId") String appId, @RequestParam(required = false) String appVersion, HttpServletRequest request) throws Exception {
        appService.getAppDefinition(appId, appVersion);
        JSONObject jsonObject = new JSONObject();

        // get available binders
        DataListAction[] actions = dataListService.getAvailableActions();

        Collection<Object> collection = new ArrayList<Object>();
        for (DataListAction action : actions) {
            HashMap hm = new HashMap();
            hm.put("name", action.getName());
            hm.put("label", action.getLabel());
            hm.put("className", action.getClassName());
            if (action instanceof PropertyEditable) {
                String propertyOptions = ((PropertyEditable) action).getPropertyOptions();
                if (propertyOptions != null && !propertyOptions.isEmpty()) {
                    hm.put("propertyOptions", propertyOptions);
                }
            }
            hm.put("type", "text");
            collection.add(hm);
        }
        jsonObject.accumulate("actions", collection);
        jsonObject.write(writer);
    }

    @RequestMapping("/json/console/app/(*:appId)/(*:appVersion)/builder/binder/columns")
    public void getBuilderDataColumnList(ModelMap map, Writer writer, @RequestParam("appId") String appId, @RequestParam(required = false) String appVersion, @RequestParam String id, @RequestParam String binderId, HttpServletRequest request) throws Exception {
        appService.getAppDefinition(appId, appVersion);
        JSONObject jsonObject = new JSONObject();

        // get data list
        DataList dataList = new DataList();

        // parse JSON from request if available
        dataList = parseFromJsonParameter(map, dataList, id, request);

        // get binder from request
        DataListBinder binder = createDataListBinderFromRequestInternal(binderId, request);
        if (binder != null) {
            dataList.setBinder(binder);
        }

        DataListColumn[] sourceColumns = binder.getColumns();
        Collection<DataListColumn> binderColumnList = new ArrayList<DataListColumn>(Arrays.asList(sourceColumns));
        Collection<String> columnNameList = new HashSet<String>();
        DataListColumn[] targetColumns = dataList.getColumns();
        if (targetColumns != null) {
            for (DataListColumn selectedColumn : targetColumns) {
                columnNameList.add(selectedColumn.getName());
            }
        }
        for (Iterator i = binderColumnList.iterator(); i.hasNext();) {
            DataListColumn column = (DataListColumn) i.next();
            if (columnNameList.contains(column.getName())) {
                i.remove();
            }
        }
        sourceColumns = (DataListColumn[]) binderColumnList.toArray(new DataListColumn[0]);
        DataList sourceDataList = new DataList();
        sourceDataList.setColumns(sourceColumns);
        //String sourceColumnsJson = "";//dataListService.toJson(sourceDataList);

        Collection<Object> collection = new ArrayList<Object>();
        for (DataListColumn sourceColumn : sourceColumns) {
            HashMap hm = new HashMap();
            hm.put("name", sourceColumn.getName());
            hm.put("label", sourceColumn.getLabel());
            hm.put("sortable", true);
            hm.put("filterable", true);
            hm.put("type", sourceColumn.getType());
            collection.add(hm);
        }
        jsonObject.accumulate("columns", collection);
        jsonObject.write(writer);
    }
    
    protected DataListBinder createDataListBinderFromRequestInternal(String binderId, HttpServletRequest request) {
        DataListBinder binder = null;
        if (binderId != null && binderId.trim().length() > 0) {
            // create binder
            binder = dataListService.getBinder(binderId);

            if (request != null) {
                // get request params
                Properties properties = new Properties();
                Enumeration e = request.getParameterNames();
                while (e.hasMoreElements()) {
                    String paramName = (String) e.nextElement();
                    if (paramName.startsWith(PREFIX_BINDER_PROPERTY)) {
                        String[] paramValue = (String[]) request.getParameterValues(paramName);
                        String propName = paramName.substring(PREFIX_BINDER_PROPERTY.length());
                        properties.put(propName, WorkflowUtil.processVariable(CsvUtil.getDeliminatedString(paramValue), null, null) );
                    }
                }
                binder.setProperties(properties);
            }
        }
        return binder;
    }

    protected DataList parseFromJsonParameter(ModelMap map, DataList dataList, String id, HttpServletRequest request) {
        // get parameters

        String jsonParam = new ParamEncoder(id).encodeParameterName("json");
        String json = request.getParameter(jsonParam);

        // use preview json if available
        if (json != null && json.trim().length() > 0) {
            try {
                dataList = dataListService.fromJson(json);
                dataList.setId(id);
            } catch (Exception ex) {
                map.addAttribute("dataListError", ex.toString());
            }
        }/* else {
        json = dataListService.toJson(dataList);
        }*/

        String jsonEncoded = null;
        try {
            if (json != null) {
                jsonEncoded = URLEncoder.encode(json, "UTF-8");
            }
        } catch (Exception ex) {
            LogUtil.error(this.getClass().getName(), ex, "parseFromJsonParameter Error!");
        }

        // set for view
        map.addAttribute("json", json);
        map.addAttribute("jsonEncoded", jsonEncoded);
        map.addAttribute("jsonParam", jsonParam);
        return dataList;
    }
}
