package org.joget.apps.app.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.displaytag.util.ParamEncoder;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListBinder;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListFilter;
import org.joget.apps.datalist.model.DataListFilterType;
import org.joget.apps.datalist.service.DataListService;
import org.joget.commons.util.CsvUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONException;
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
    @Autowired
    PluginManager pluginManager;

    @RequestMapping("/console/app/(*:appId)/(~:version)/datalist/builder/(*:id)")
    public String builder(ModelMap map, @RequestParam("appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("id") String id, @RequestParam(required = false) String json) throws Exception {
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

    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/datalist/builderSave/(*:id)", method = RequestMethod.POST)
    public void save(Writer writer, @RequestParam("appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("id") String id, @RequestParam("json") String json) throws Exception {
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

    @RequestMapping(value = {"/console/app/(*:appId)/(~:appVersion)/datalist/builderPreview/(*:id)", "/client/app/(*:appId)/(*:appVersion)/datalist/(*:id)"})
    public String preview(ModelMap map, HttpServletRequest request, @RequestParam("appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam("id") String id, @RequestParam(required = false) String json) throws Exception {
        String view = "dbuilder/view";

        // get current app to set into thread
        appService.getAppDefinition(appId, appVersion);

        try {
            // get data list
            DataList dataList = new DataList();
            if (json != null && !json.trim().isEmpty()) {
                dataList = dataListService.fromJson(json);
                map.addAttribute("json", json);
            } else {
                dataList = parseFromJsonParameter(map, dataList, id, request);
            }

            map.addAttribute("dataList", dataList);

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

    @RequestMapping("/json/console/app/(*:appId)/(~:appVersion)/builder/actions")
    public void getBuilderDataActionList(ModelMap map, Writer writer, @RequestParam("appId") String appId, @RequestParam(required = false) String appVersion, HttpServletRequest request) throws Exception {
        appService.getAppDefinition(appId, appVersion);
        JSONObject jsonObject = new JSONObject();

        // get available binders
        DataListAction[] actions = dataListService.getAvailableActions();

        Collection<Object> collection = new ArrayList<Object>();
        for (DataListAction action : actions) {
            Plugin p = (Plugin) action;
            HashMap hm = new HashMap();
            hm.put("name", p.getName());
            hm.put("label", action.getLinkLabel());
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

    @RequestMapping(value = "/json/console/app/(*:appId)/(~:appVersion)/builder/binder/columns", method = RequestMethod.POST)
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
 
        // sort columns by label
        List<DataListColumn> binderColumnList = Arrays.asList(sourceColumns);
        Collections.sort(binderColumnList, new Comparator<DataListColumn>() {

            public int compare(DataListColumn o1, DataListColumn o2) {
                return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
            }
        });
        
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

    @RequestMapping("/dbuilder/getFilterTemplate")
    public String getBuilderFilterTemplate(ModelMap model, @RequestParam("json") String json) throws Exception {
        Map<String, Object> obj = PropertyUtil.getPropertiesValueFromJson(json);
        DataListFilter filter = new DataListFilter();
        filter.setLabel(obj.get("label").toString());
        filter.setName(obj.get("name").toString());
        if (obj.get("operator") != null) {
            filter.setOperator(obj.get("operator").toString());
        }
        if (obj.get("type") != null) {
            Map typeMap = (Map) obj.get("type");
            DataListFilterType type = (DataListFilterType) pluginManager.getPlugin(typeMap.get("className").toString());
            if (type != null) {
                if (!"{}".equals(typeMap.get("properties"))) {
                    type.setProperties((Map) typeMap.get("properties"));
                }
                filter.setType(type);
            }
        }

        model.addAttribute("template", filter.getType().getTemplate(new DataList(), filter.getName(), filter.getLabel()));
        return "dbuilder/filterTmplate";
    }

    protected DataListBinder createDataListBinderFromRequestInternal(String binderId, HttpServletRequest request) {
        DataListBinder binder = null;
        if (binderId != null && binderId.trim().length() > 0) {
            // create binder
            binder = dataListService.getBinder(binderId);

            if (request != null) {
                // get request params
                Enumeration e = request.getParameterNames();
                while (e.hasMoreElements()) {
                    String paramName = (String) e.nextElement();
                    if (paramName.startsWith(PREFIX_BINDER_PROPERTY)) {
                        String[] paramValue = (String[]) request.getParameterValues(paramName);
                        String propName = paramName.substring(PREFIX_BINDER_PROPERTY.length());
                        binder.setProperty(propName, WorkflowUtil.processVariable(CsvUtil.getDeliminatedString(paramValue), null, null));
                    }
                }
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
    
    @RequestMapping("/app/(*:appId)/(~:appVersion)/datalist/embed")
    public String embedDatalist(ModelMap model, @RequestParam("appId") String appId, @RequestParam(value = "version", required = false) String version, HttpServletRequest request, @RequestParam("_submitButtonLabel") String buttonLabel, @RequestParam("_callback") String callback, @RequestParam("_setting") String callbackSetting, @RequestParam(required = false) String id, @RequestParam(value = "_listId", required = false) String listId, @RequestParam(value = "_type", required = false) String selectionType) throws JSONException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(listId, appDef);
        String json = datalistDefinition.getJson();
        DataList dataList = dataListService.fromJson(json);
        dataList.setSelectionType(selectionType);
        
        model.addAttribute("id", id);
        model.addAttribute("json", json);
        model.addAttribute("buttonLabel", buttonLabel);
        model.addAttribute("dataList", dataList);
        model.addAttribute("setting", callbackSetting);
        model.addAttribute("callback", callback);
        return "dbuilder/embedDatalist";
    }
}
