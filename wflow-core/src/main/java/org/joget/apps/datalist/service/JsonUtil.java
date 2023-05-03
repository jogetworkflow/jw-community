package org.joget.apps.datalist.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringEscapeUtils;
import org.displaytag.util.LookupUtil;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilter;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListBinder;
import org.joget.apps.datalist.model.DataListDisplayColumnProxy;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.apps.datalist.model.DataListFilterType;
import org.joget.apps.userview.model.Permission;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.directory.model.User;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONArray;
import org.joget.apps.datalist.model.DataListDisplayColumn;

/**
 * Utility class containing methods to create datalist from JSON
 */
public class JsonUtil {
    
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_LABEL = "label";
    public static final String PROPERTY_DESC = "description";
    public static final String PROPERTY_PAGE_SIZE = "pageSize";
    public static final String PROPERTY_HIDE_PAGE_SIZE = "hidePageSize";
    public static final String PROPERTY_PAGE_SIZE_SELECTOR_OPTIONS = "pageSizeSelectorOptions";
    public static final String PROPERTY_CLASS_NAME = "className";
    public static final String PROPERTY_PROPERTIES = "properties";
    public static final String PROPERTY_ORDER = "order";
    public static final String PROPERTY_ORDER_BY = "orderBy";
    public static final String PROPERTY_BINDER = "binder";
    public static final String PROPERTY_COLUMNS = "columns";
    public static final String PROPERTY_SORTABLE = "sortable";
    public static final String PROPERTY_HIDDEN = "hidden";
    public static final String PROPERTY_WIDTH = "width";
    public static final String PROPERTY_STYLE = "style";
    public static final String PROPERTY_ALIGNMENT = "alignment";
    public static final String PROPERTY_HEADER_ALIGNMENT = "headerAlignment";
    public static final String PROPERTY_ROW_ACTIONS = "rowActions";
    public static final String PROPERTY_ACTION = "action";
    public static final String PROPERTY_ACTIONS = "actions";
    public static final String PROPERTY_FILTERS = "filters";
    public static final String PROPERTY_OPERATOR = "operator";
    public static final String PROPERTY_FILTER_TYPE = "type";
    public static final String PROPERTY_FORMAT = "format";
    public static final String PROPERTY_RENDER_HTML = "renderHtml";
    public static final String PROPERTY_USE_SESSION = "useSession";
    public static final String PROPERTY_SHOW_DATA_ONFILTER = "showDataWhenFilterSet";
    public static final String PROPERTY_CONSIDER_FILTER_WHEN_GET_TOTAL = "considerFilterWhenGetTotal";
    public static final String PROPERTY_DISABLE_RESPONSIVE = "disableResponsive";
    public static final String PROPERTY_RESPONSIVE = "responsiveView";
    public static final String PROPERTY_REPONSIVE_SEARCH_POPUP = "searchPopup";

    /**
     * Converts from JSON string into an object. Specifically to support data list model classes.
     * @param <T>
     * @param json
     * @param classOfT
     * @return
     */
    public static <T extends Object> T fromJson(String json, Class<T> classOfT) {
        if (json == null) {
            return null;
        }

        // strip enclosing brackets
        json = json.trim();
        if (json.startsWith("(")) {
            json = json.substring(1);
        }
        if (json.endsWith(")")) {
            json = json.substring(0, json.length() - 1);
        }
        
        Object object = parseElementFromJson(json);
        return (T) object;
    }

    /**
     * Converts from JSON string into an object. Specifically to support data list model classes.
     * @param json
     * @return
     */
    public static Object parseElementFromJson(String json) {
        try {
            // create json object
            json = AppUtil.replaceAppMessages(json, StringUtil.TYPE_JSON);
            JSONObject obj = new JSONObject(json);

            // parse json object
            Object object = parseElementFromJsonObject(obj);
            
            return object;
        } catch (Exception ex) {
            LogUtil.error(JsonUtil.class.getName(), ex, "");
        }
        return null;
    }
    
    /**
     * Used to creates Datalist object from JSON Object
     * @param obj
     * @return
     * @throws Exception 
     */
    public static Object parseElementFromJsonObject(JSONObject obj) throws Exception {
        String permissionKey = Permission.DEFAULT;
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        User currentUser = workflowUserManager.getCurrentUser();
        Map<String, Object> requestParameters = new HashMap<String, Object>();
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            requestParameters.putAll(convertRequestParamMap(request.getParameterMap()));
        }
        Boolean hasPermission = false;
        
        DataList object = (DataList) new DataList();
        if (object != null) {
            object.setProperties(PropertyUtil.getProperties(obj));
            
            if (object.getProperties().containsKey(PROPERTY_ID)) {
                object.setId(object.getPropertyString(PROPERTY_ID));
            }
            if (object.getProperties().containsKey(PROPERTY_NAME)) {
                object.setName(object.getPropertyString(PROPERTY_NAME));
            }
            if (object.getProperties().containsKey(PROPERTY_DESC)) {
                object.setDescription(object.getPropertyString(PROPERTY_DESC));
            }
            if (object.getProperties().containsKey(PROPERTY_PAGE_SIZE) && !object.getPropertyString(PROPERTY_PAGE_SIZE).isEmpty()) {
                object.setDefaultPageSize(Integer.parseInt(object.getPropertyString(PROPERTY_PAGE_SIZE)));
            }
            if (object.getProperties().containsKey(PROPERTY_HIDE_PAGE_SIZE) && !object.getPropertyString(PROPERTY_HIDE_PAGE_SIZE).isEmpty()) {
                object.setShowPageSizeSelector(!object.getPropertyString(PROPERTY_HIDE_PAGE_SIZE).equals("true"));
            }
            if (object.getProperties().containsKey(PROPERTY_PAGE_SIZE_SELECTOR_OPTIONS) && !object.getPropertyString(PROPERTY_PAGE_SIZE_SELECTOR_OPTIONS).isEmpty()) {
                object.setPageSizeList(object.getPropertyString(PROPERTY_PAGE_SIZE_SELECTOR_OPTIONS));
            }
            if (object.getProperties().containsKey(PROPERTY_ORDER)) {
                object.setDefaultOrder(object.getPropertyString(PROPERTY_ORDER));
            }
            if (object.getProperties().containsKey(PROPERTY_ORDER_BY)) {
                object.setDefaultSortColumn(object.getPropertyString(PROPERTY_ORDER_BY));
            }
            if (object.getProperties().containsKey(PROPERTY_USE_SESSION) && !object.getPropertyString(PROPERTY_USE_SESSION).isEmpty()) {
                object.setUseSession(object.getPropertyString(PROPERTY_USE_SESSION).equals("true"));
            }
            if (object.getProperties().containsKey(PROPERTY_SHOW_DATA_ONFILTER) && !object.getPropertyString(PROPERTY_SHOW_DATA_ONFILTER).isEmpty()) {
                object.setShowDataWhenFilterSet(object.getPropertyString(PROPERTY_SHOW_DATA_ONFILTER).equals("true"));
            }
            if (object.getProperties().containsKey(PROPERTY_CONSIDER_FILTER_WHEN_GET_TOTAL) && !object.getPropertyString(PROPERTY_CONSIDER_FILTER_WHEN_GET_TOTAL).isEmpty()) {
                object.setConsiderFilterWhenGetTotal(object.getPropertyString(PROPERTY_CONSIDER_FILTER_WHEN_GET_TOTAL).equals("true"));
            }
            if (object.getProperties().containsKey(PROPERTY_DISABLE_RESPONSIVE) && !object.getPropertyString(PROPERTY_DISABLE_RESPONSIVE).isEmpty()) {
                object.setDisableResponsive(object.getPropertyString(PROPERTY_DISABLE_RESPONSIVE).equals("true"));
            }
            if (obj.has(PROPERTY_RESPONSIVE) && !obj.isNull(PROPERTY_RESPONSIVE)) {
                object.setResponsiveJson(obj.getJSONArray(PROPERTY_RESPONSIVE).toString());
            }
            if (object.getProperties().containsKey(PROPERTY_REPONSIVE_SEARCH_POPUP) && !object.getPropertyString(PROPERTY_REPONSIVE_SEARCH_POPUP).isEmpty()) {
                object.setResponsiveSearchPopup(object.getPropertyString(PROPERTY_REPONSIVE_SEARCH_POPUP).equals("true"));
            }
            
            JsonUtil.generateBuilderProperties(object.getProperties(), new String[]{"", "filter-", "columns-header-", "columns-", "rowActions-header-", "rowActions-link-", "rowActions-", "action-", "card-"});
            
            if (obj.has("permission_rules")) {
                JSONArray permissionRules = obj.getJSONArray("permission_rules");
                if (permissionRules != null && permissionRules.length() > 0) {
                    for (int i = 0; i < permissionRules.length(); i++) {
                        JSONObject rule = permissionRules.getJSONObject(i);
                        if (rule.has("permission")) {
                            JSONObject permissionObj = rule.optJSONObject("permission");
                            hasPermission = UserviewUtil.getPermisionResult(permissionObj, requestParameters, currentUser);
                            if (hasPermission) {
                                permissionKey = rule.getString("permission_key");
                                break;
                            }
                        }
                    }
                }
            }
            if (!hasPermission) {
                if (obj.has("permission")) {
                    JSONObject permissionObj = obj.getJSONObject("permission");
                    hasPermission = UserviewUtil.getPermisionResult(permissionObj, requestParameters, currentUser);
                } else {
                    hasPermission = true;
                }
            }
            
            object.setIsAuthorized(hasPermission);
            if (object.getProperties().containsKey("noPermissionMessage") && !object.getPropertyString("noPermissionMessage").isEmpty()) {
                object.setUnauthorizedMsg(object.getPropertyString("noPermissionMessage"));
            }

            //set binder
            DataListBinder binder = parseBinderFromJsonObject(obj);
            object.setBinder(binder);
                
            if (hasPermission) {
                //set columns
                Collection<DataListColumn> columns = parseColumnsFromJsonObject(obj, permissionKey);
                DataListColumn[] temp = (DataListColumn[]) columns.toArray(new DataListColumn[columns.size()]);
                object.setColumns(temp);
                object.setColumnPlaceholder("columns", temp);

                //set actions
                Collection<DataListAction> actions = parseActionsFromJsonObject(obj, permissionKey);
                DataListAction[] temp2 = (DataListAction[]) actions.toArray(new DataListAction[actions.size()]);
                object.setActions(temp2);

                //set row actions
                Collection<DataListAction> rowActions = parseRowActionsFromJsonObject(obj, permissionKey);
                DataListAction[] temp3 = (DataListAction[]) rowActions.toArray(new DataListAction[rowActions.size()]);
                object.setRowActions(temp3);
                object.setRowActionPlaceholder("rowActions", temp3);

                //set filters
                Collection<DataListFilter> filters = parseFiltersFromJsonObject(obj, permissionKey);
                DataListFilter[] temp4 = (DataListFilter[]) filters.toArray(new DataListFilter[filters.size()]);
                object.setFilters(temp4);
                
                //set column placeholder
                Iterator<String> keys = obj.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    if (key.startsWith("column_")) {
                        Collection<DataListColumn> cols = parseColumnsFromJsonArray(obj.getJSONArray(key), permissionKey);
                        DataListColumn[] t = (DataListColumn[]) cols.toArray(new DataListColumn[cols.size()]);
                        object.setColumnPlaceholder(key, t);
                    } else if (key.startsWith("rowAction_")) {
                        Collection<DataListAction> acts = parseRowActionsFromJsonArray(obj.getJSONArray(key), permissionKey);
                        DataListAction[] t = (DataListAction[]) acts.toArray(new DataListAction[acts.size()]);
                        object.setRowActionPlaceholder(key, t);
                    }
                }
            } else {
                object.setColumns(new DataListColumn[0]);
                object.setActions(new DataListAction[0]);
                object.setRowActions(new DataListAction[0]);
                object.setFilters(new DataListFilter[0]);
            }
        }
        
        return object;
    }
    
    /**
     * Used to retrieves datalist filters from JSON Object
     * @param obj
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static Collection<DataListFilter> parseFiltersFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        return parseFiltersFromJsonObject(obj, Permission.DEFAULT);
    }
    
    /**
     * Used to retrieves datalist filters from JSON Object
     * @param obj
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static Collection<DataListFilter> parseFiltersFromJsonObject(JSONObject obj, String permissionKey) throws JSONException, InstantiationException, IllegalAccessException {
        Collection<DataListFilter> property = new ArrayList<DataListFilter>();
        
        if (!obj.isNull(PROPERTY_FILTERS)) {
            JSONArray filters = obj.getJSONArray(PROPERTY_FILTERS);
            
            for (int i = 0; i < filters.length(); i++) {
                JSONObject filter = filters.getJSONObject(i);
                DataListFilter dataListFilter = new DataListFilter();
                dataListFilter.setProperties(PropertyUtil.getProperties(filter));
                
                if (dataListFilter.getProperties().containsKey(PROPERTY_NAME)) {
                    dataListFilter.setName(dataListFilter.getPropertyString(PROPERTY_NAME));
                }
                if (dataListFilter.getProperties().containsKey(PROPERTY_LABEL)) {
                    dataListFilter.setLabel(dataListFilter.getPropertyString(PROPERTY_LABEL));
                }
                if (dataListFilter.getProperties().containsKey(PROPERTY_OPERATOR)) {
                    dataListFilter.setOperator(dataListFilter.getPropertyString(PROPERTY_OPERATOR));
                }
                if (filter.has(PROPERTY_FILTER_TYPE)) {
                    DataListFilterType type = parseFilterTypeFromJsonObject(filter);
                    dataListFilter.setType(type);
                }
                if (dataListFilter.getProperties().containsKey(PROPERTY_HIDDEN)) {
                    dataListFilter.setHidden("true".equalsIgnoreCase(dataListFilter.getPropertyString(PROPERTY_HIDDEN)));
                }
                
                if (!Permission.DEFAULT.equals(permissionKey)) {
                    if (filter.has("permission_rules") && filter.getJSONObject("permission_rules").has(permissionKey)) {
                        JSONObject rule = filter.getJSONObject("permission_rules").getJSONObject(permissionKey);
                        if (rule.has(PROPERTY_HIDDEN) && "true".equals(rule.get(PROPERTY_HIDDEN).toString())) {
                            dataListFilter.setHidden(true);
                        } else {
                            dataListFilter.setHidden(false);
                        }
                    } else {
                        dataListFilter.setHidden(false);
                    }
                }
                
                JsonUtil.generateBuilderProperties(dataListFilter.getProperties(), new String[]{""});
                
                property.add(dataListFilter);
            }
        }
        return property;
    }
    
    /**
     * Used to retrieves datalist row actions from JSON Object
     * @param obj
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static Collection<DataListAction> parseRowActionsFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        return parseRowActionsFromJsonObject(obj, Permission.DEFAULT);
    }
    
    /**
     * Used to retrieves datalist row actions from JSON Object
     * @param obj
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static Collection<DataListAction> parseRowActionsFromJsonObject(JSONObject obj, String permissionKey) throws JSONException, InstantiationException, IllegalAccessException {
        Collection<DataListAction> property = new ArrayList<DataListAction>();
        
        if (!obj.isNull(PROPERTY_ROW_ACTIONS)) {
            JSONArray actions = obj.getJSONArray(PROPERTY_ROW_ACTIONS);
            
            for (int i = 0; i < actions.length(); i++) {
                JSONObject action = actions.getJSONObject(i);
                boolean isHidden = false;
                if (!Permission.DEFAULT.equals(permissionKey)) {
                    if (action.has("permission_rules") && action.getJSONObject("permission_rules").has(permissionKey)) {
                        JSONObject rule = action.getJSONObject("permission_rules").getJSONObject(permissionKey);
                        if (rule.has(PROPERTY_HIDDEN) && "true".equals(rule.get(PROPERTY_HIDDEN).toString())) {
                            isHidden = true;
                        }
                    }
                } else if (action.has(PROPERTY_HIDDEN) && "true".equals(action.get(PROPERTY_HIDDEN).toString())) {
                    isHidden = true;
                }
                
                if (!isHidden && action.has(PROPERTY_CLASS_NAME)) {
                    String className = action.getString(PROPERTY_CLASS_NAME);
                    DataListAction dataListAction = (DataListAction) loadPlugin(className);
                    if (dataListAction != null) {
                        dataListAction.setProperties(PropertyUtil.getProperties(action.getJSONObject(PROPERTY_PROPERTIES)));
                        dataListAction.setProperty(PROPERTY_ID, action.getString(PROPERTY_ID));
                        
                        JsonUtil.generateBuilderProperties(dataListAction.getProperties(), new String[]{"", "header-", "link-"});
                        
                        property.add(dataListAction);
                    }
                }
            }
        }
        return property;
    }
    
    /**
     * Used to retrieves datalist row actions from JSON Array
     * @param Array
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static Collection<DataListAction> parseRowActionsFromJsonArray(JSONArray actions, String permissionKey) throws JSONException, InstantiationException, IllegalAccessException {
        Collection<DataListAction> property = new ArrayList<DataListAction>();
        
        for (int i = 0; i < actions.length(); i++) {
            JSONObject action = actions.getJSONObject(i);
            boolean isHidden = false;
            if (!Permission.DEFAULT.equals(permissionKey)) {
                if (action.has("permission_rules") && action.getJSONObject("permission_rules").has(permissionKey)) {
                    JSONObject rule = action.getJSONObject("permission_rules").getJSONObject(permissionKey);
                    if (rule.has(PROPERTY_HIDDEN) && "true".equals(rule.get(PROPERTY_HIDDEN).toString())) {
                        isHidden = true;
                    }
                }
            } else if (action.has(PROPERTY_HIDDEN) && "true".equals(action.get(PROPERTY_HIDDEN).toString())) {
                isHidden = true;
            }

            if (!isHidden && action.has(PROPERTY_CLASS_NAME)) {
                String className = action.getString(PROPERTY_CLASS_NAME);
                DataListAction dataListAction = (DataListAction) loadPlugin(className);
                if (dataListAction != null) {
                    dataListAction.setProperties(PropertyUtil.getProperties(action.getJSONObject(PROPERTY_PROPERTIES)));
                    dataListAction.setProperty(PROPERTY_ID, action.getString(PROPERTY_ID));

                    JsonUtil.generateBuilderProperties(dataListAction.getProperties(), new String[]{"", "header-", "link-"});

                    property.add(dataListAction);
                }
            }
        }
        
        return property;
    }
    
    /**
     * Used to retrieves datalist actions from JSON Object
     * @param obj
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static Collection<DataListAction> parseActionsFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        return parseActionsFromJsonObject(obj, Permission.DEFAULT);
    }
    
    /**
     * Used to retrieves datalist actions from JSON Object
     * @param obj
     * @param permissionKey
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static Collection<DataListAction> parseActionsFromJsonObject(JSONObject obj, String permissionKey) throws JSONException, InstantiationException, IllegalAccessException {
        Collection<DataListAction> property = new ArrayList<DataListAction>();
        
        if (!obj.isNull(PROPERTY_ACTIONS)) {
            JSONArray actions = obj.getJSONArray(PROPERTY_ACTIONS);
            
            for (int i = 0; i < actions.length(); i++) {
                JSONObject action = actions.getJSONObject(i);
                
                boolean isHidden = false;
                if (!Permission.DEFAULT.equals(permissionKey)) {
                    if (action.has("permission_rules") && action.getJSONObject("permission_rules").has(permissionKey)) {
                        JSONObject rule = action.getJSONObject("permission_rules").getJSONObject(permissionKey);
                        if (rule.has(PROPERTY_HIDDEN) && "true".equals(rule.get(PROPERTY_HIDDEN).toString())) {
                            isHidden = true;
                        }
                    }
                } else if (action.has(PROPERTY_HIDDEN) && "true".equals(action.get(PROPERTY_HIDDEN).toString())) {
                    isHidden = true;
                }
                
                if (!isHidden && action.has(PROPERTY_CLASS_NAME)) {
                    String className = action.getString(PROPERTY_CLASS_NAME);
                    DataListAction dataListAction = (DataListAction) loadPlugin(className);
                    if (dataListAction != null) {
                        dataListAction.setProperties(PropertyUtil.getProperties(action.getJSONObject(PROPERTY_PROPERTIES)));
                        dataListAction.setProperty(PROPERTY_ID, action.getString(PROPERTY_ID));
                        
                        JsonUtil.generateBuilderProperties(dataListAction.getProperties(), new String[]{""});
                        
                        property.add(dataListAction);
                    }
                }
            }
        }
        return property;
    }
    
    /**
     * Used to retrieves datalist binder from JSON Object
     * @param obj
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static DataListBinder parseBinderFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        if (!obj.isNull(PROPERTY_BINDER)) {
            JSONObject binderObj = obj.getJSONObject(PROPERTY_BINDER);
            if (binderObj.has(PROPERTY_CLASS_NAME)) {
                String className = binderObj.getString(PROPERTY_CLASS_NAME);
                DataListBinder dataListBinder = (DataListBinder) loadPlugin(className);
                if (dataListBinder != null) {
                    dataListBinder.setProperties(PropertyUtil.getProperties(binderObj.getJSONObject(PROPERTY_PROPERTIES)));
                    return dataListBinder;
                }
            }
        }
        return null;
    }
    
    /**
     * Used to retrieves datalist action from JSON Object 
     * @param obj
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static DataListAction parseActionFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        try {
            if (!obj.isNull(PROPERTY_ACTION) && obj.get(PROPERTY_ACTION) instanceof JSONObject) {
                JSONObject actionObj = obj.getJSONObject(PROPERTY_ACTION);
                if (actionObj.has(PROPERTY_CLASS_NAME)) {
                    String className = actionObj.getString(PROPERTY_CLASS_NAME);
                    DataListAction dataListAction = (DataListAction) loadPlugin(className);
                    if (dataListAction != null) {
                        dataListAction.setProperties(PropertyUtil.getProperties(actionObj.getJSONObject(PROPERTY_PROPERTIES)));
                        return dataListAction;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.warn(JsonUtil.class.getName(), "Invalid action for " + obj.toString());
        }
        return null;
    }
    
    /**
     * Used to retrieves datalist formatter from JSON Object
     * @param obj
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static DataListColumnFormat parseFormatterFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        try {
            if (!obj.isNull(PROPERTY_FORMAT) && obj.get(PROPERTY_FORMAT) instanceof JSONObject) {
                JSONObject formatterObj = obj.getJSONObject(PROPERTY_FORMAT);
                if (formatterObj.has(PROPERTY_CLASS_NAME)) {
                    String className = formatterObj.getString(PROPERTY_CLASS_NAME);
                    DataListColumnFormat dataListColumnFormat = (DataListColumnFormat) loadPlugin(className);
                    if (dataListColumnFormat != null) {
                        dataListColumnFormat.setProperties(PropertyUtil.getProperties(formatterObj.getJSONObject(PROPERTY_PROPERTIES)));
                        return dataListColumnFormat;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.warn(JsonUtil.class.getName(), "Invalid formater for " + obj.toString());
        }
        return null;
    }
    
    /**
     * Used to retrieves datalist column from JSON Object
     * @param obj
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static Collection<DataListColumn> parseColumnsFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        return parseColumnsFromJsonObject(obj, Permission.DEFAULT);
    }
    
    /**
     * Used to retrieves datalist column from JSON Object
     * @param obj
     * @param permissionKey
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static Collection<DataListColumn> parseColumnsFromJsonObject(JSONObject obj, String permissionKey) throws JSONException, InstantiationException, IllegalAccessException {
        Collection<DataListColumn> property = new ArrayList<DataListColumn>();
        
        if (!obj.isNull(PROPERTY_COLUMNS)) {
            JSONArray columns = obj.getJSONArray(PROPERTY_COLUMNS);
            
            for (int i = 0; i < columns.length(); i++) {
                JSONObject column = columns.getJSONObject(i);
                
                if (column.has(PROPERTY_CLASS_NAME)) {
                    DataListDisplayColumnProxy dataListColumn = parseDisplayColumnFromJsonObject(column, permissionKey);
                    if (dataListColumn != null) {
                        property.add(dataListColumn);
                    }
                } else {
                    DataListColumn dataListColumn = parseColumnFromJsonObject(column, permissionKey);
                    property.add(dataListColumn);
                }
            }
        }
        return property;
    }
    
    /**
     * Used to retrieves datalist column from JSON Object
     * @param obj
     * @param permissionKey
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static Collection<DataListColumn> parseColumnsFromJsonArray(JSONArray columns, String permissionKey) throws JSONException, InstantiationException, IllegalAccessException {
        Collection<DataListColumn> property = new ArrayList<DataListColumn>();
        
        for (int i = 0; i < columns.length(); i++) {
            JSONObject column = columns.getJSONObject(i);
            
            if (column.has(PROPERTY_CLASS_NAME)) {
                DataListDisplayColumnProxy dataListColumn = parseDisplayColumnFromJsonObject(column, permissionKey);
                if (dataListColumn != null) {
                    property.add(dataListColumn);
                }
            } else {
                DataListColumn dataListColumn = parseColumnFromJsonObject(column, permissionKey);
                property.add(dataListColumn);
            }
        }
        return property;
    }
    
    /**
     * Used to retrieves datalist column from JSON Object
     * @param obj
     * @param permissionKey
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static DataListColumn parseColumnFromJsonObject(JSONObject column, String permissionKey) throws JSONException, InstantiationException, IllegalAccessException {
        DataListColumn dataListColumn = new DataListColumn();
        dataListColumn.setProperties(PropertyUtil.getProperties(column));
        
        if (dataListColumn.getProperties().containsKey(PROPERTY_NAME) && !dataListColumn.getPropertyString(PROPERTY_NAME).isEmpty()) {
            dataListColumn.setName(dataListColumn.getPropertyString(PROPERTY_NAME));
        }
        if (dataListColumn.getProperties().containsKey(PROPERTY_LABEL) && !dataListColumn.getPropertyString(PROPERTY_LABEL).isEmpty()) {
            dataListColumn.setLabel(dataListColumn.getPropertyString(PROPERTY_LABEL));
        }
        if (dataListColumn.getProperties().containsKey(PROPERTY_SORTABLE) && !dataListColumn.getPropertyString(PROPERTY_SORTABLE).isEmpty()) {
            dataListColumn.setSortable(Boolean.parseBoolean(dataListColumn.getPropertyString(PROPERTY_SORTABLE)));
        }
        if (dataListColumn.getProperties().containsKey(PROPERTY_HIDDEN) && !dataListColumn.getPropertyString(PROPERTY_HIDDEN).isEmpty()) {
            dataListColumn.setHidden(Boolean.parseBoolean(dataListColumn.getPropertyString(PROPERTY_HIDDEN)));
        }
        if (dataListColumn.getProperties().containsKey(PROPERTY_WIDTH) && !dataListColumn.getPropertyString(PROPERTY_WIDTH).isEmpty()) {
            dataListColumn.setWidth(dataListColumn.getPropertyString(PROPERTY_WIDTH));
        }
        if (dataListColumn.getProperties().containsKey(PROPERTY_STYLE) && !dataListColumn.getPropertyString(PROPERTY_STYLE).isEmpty()) {
            dataListColumn.setStyle(dataListColumn.getPropertyString(PROPERTY_STYLE));
        }
        if (dataListColumn.getProperties().containsKey(PROPERTY_ALIGNMENT) && !dataListColumn.getPropertyString(PROPERTY_ALIGNMENT).isEmpty()) {
            dataListColumn.setAlignment(dataListColumn.getPropertyString(PROPERTY_ALIGNMENT));
        }
        if (dataListColumn.getProperties().containsKey(PROPERTY_HEADER_ALIGNMENT) && !dataListColumn.getPropertyString(PROPERTY_HEADER_ALIGNMENT).isEmpty()) {
            dataListColumn.setHeaderAlignment(dataListColumn.getPropertyString(PROPERTY_HEADER_ALIGNMENT));
        }
        if (column.has(PROPERTY_ACTION) && !column.isNull(PROPERTY_ACTION)) {
            DataListAction action = parseActionFromJsonObject(column);
            dataListColumn.setAction(action);
        }
        if (column.has(PROPERTY_FORMAT) && !column.isNull(PROPERTY_FORMAT)) {
            Collection<DataListColumnFormat> formatCollection = new ArrayList<DataListColumnFormat>();
            DataListColumnFormat format = parseFormatterFromJsonObject(column);
            formatCollection.add(format);

            dataListColumn.setFormats(formatCollection);
        }
        if (dataListColumn.getProperties().containsKey(PROPERTY_RENDER_HTML) && !dataListColumn.getPropertyString(PROPERTY_RENDER_HTML).isEmpty()) {
            dataListColumn.setRenderHtml(Boolean.parseBoolean(dataListColumn.getPropertyString(PROPERTY_RENDER_HTML)));
        }

        if (!Permission.DEFAULT.equals(permissionKey)) {
            if (column.has("permission_rules") && column.getJSONObject("permission_rules").has(permissionKey)) {
                JSONObject rule = column.getJSONObject("permission_rules").getJSONObject(permissionKey);
                if (rule.has(PROPERTY_HIDDEN) && "true".equals(rule.get(PROPERTY_HIDDEN).toString())) {
                    dataListColumn.setHidden(true);
                    if (rule.has("include_export") && "true".equals(rule.get("include_export").toString())) {
                        dataListColumn.setProperty("include_export", "true");
                        dataListColumn.setProperty("exclude_export", "");
                    } else {
                        dataListColumn.setProperty("include_export", "");
                        dataListColumn.setProperty("exclude_export", "");
                    }
                } else {
                    dataListColumn.setHidden(false);
                    if (rule.has("exclude_export") && "true".equals(rule.get("exclude_export").toString())) {
                        dataListColumn.setProperty("include_export", "");
                        dataListColumn.setProperty("exclude_export", "true");
                    } else {
                        dataListColumn.setProperty("include_export", "");
                        dataListColumn.setProperty("exclude_export", "");
                    }
                }
            } else {
                dataListColumn.setHidden(false);
                dataListColumn.setProperty("include_export", "");
                dataListColumn.setProperty("exclude_export", "");
            }
        }

        JsonUtil.generateBuilderProperties(dataListColumn.getProperties(), new String[]{"", "header-"});
                
        return dataListColumn;
    }
    
    /**
     * Used to retrieves datalist display column from JSON Object
     * @param obj
     * @param permissionKey
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static DataListDisplayColumnProxy parseDisplayColumnFromJsonObject(JSONObject column, String permissionKey) throws JSONException, InstantiationException, IllegalAccessException {
        String className = column.getString(PROPERTY_CLASS_NAME);
        DataListDisplayColumn displayColumn = (DataListDisplayColumn) loadPlugin(className);
        if (displayColumn != null) {
            displayColumn.setProperties(PropertyUtil.getProperties(column.getJSONObject(PROPERTY_PROPERTIES)));

            JsonUtil.generateBuilderProperties(displayColumn.getProperties(), new String[]{"", "header-"});
            
            DataListDisplayColumnProxy proxy = new DataListDisplayColumnProxy(displayColumn);
        
            if (!Permission.DEFAULT.equals(permissionKey)) {
                if (column.has("permission_rules") && column.getJSONObject("permission_rules").has(permissionKey)) {
                    JSONObject rule = column.getJSONObject("permission_rules").getJSONObject(permissionKey);
                    if (rule.has(PROPERTY_HIDDEN) && "true".equals(rule.get(PROPERTY_HIDDEN).toString())) {
                        proxy.setHidden(true);
                        if (rule.has("include_export") && "true".equals(rule.get("include_export").toString())) {
                            proxy.setProperty("include_export", "true");
                            proxy.setProperty("exclude_export", "");
                        } else {
                            proxy.setProperty("include_export", "");
                            proxy.setProperty("exclude_export", "");
                        }
                    } else {
                        proxy.setHidden(false);
                        if (rule.has("exclude_export") && "true".equals(rule.get("exclude_export").toString())) {
                            proxy.setProperty("include_export", "");
                            proxy.setProperty("exclude_export", "true");
                        } else {
                            proxy.setProperty("include_export", "");
                            proxy.setProperty("exclude_export", "");
                        }
                    }
                } else {
                    proxy.setHidden(false);
                    proxy.setProperty("include_export", "");
                    proxy.setProperty("exclude_export", "");
                }
            } else {
                proxy.setHidden("true".equalsIgnoreCase(displayColumn.getPropertyString(PROPERTY_HIDDEN)));
            }
            return proxy;
        }
        return null;
    }
    
    /**
     * Used to retrieves datalist filter type from JSON Object
     * @param obj
     * @return
     * @throws JSONException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    public static DataListFilterType parseFilterTypeFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        if (!obj.isNull(PROPERTY_FILTER_TYPE)) {
            JSONObject filterTypeObj = obj.getJSONObject(PROPERTY_FILTER_TYPE);
            if (filterTypeObj.has(PROPERTY_CLASS_NAME)) {
                String className = filterTypeObj.getString(PROPERTY_CLASS_NAME);
                DataListFilterType dataListFilterType = (DataListFilterType) loadPlugin(className);
                if (dataListFilterType != null) {
                    dataListFilterType.setProperties(PropertyUtil.getProperties(filterTypeObj.getJSONObject(PROPERTY_PROPERTIES)));
                    return dataListFilterType;                    
                }
            }
        }
        return null;
    }
    
    /**
     * Used to generate a datalist definition JSON
     * @param listId
     * @param datalistDef
     * @return 
     */
    public static String generateDefaultList(String listId, DatalistDefinition datalistDef) {
        return generateDefaultList(listId, datalistDef, null);
    }
    
    /**
     * Used to generate a datalist definition JSON based on another datalist definition
     * @param listId
     * @param datalistDef
     * @param copyDatalistDef
     * @return 
     */
    public static String generateDefaultList(String listId, DatalistDefinition datalistDef, DatalistDefinition copyDatalistDef) {
        String name = "";
        String desc = "";
        String json = "";

        if (datalistDef != null) {
            name = datalistDef.getName();
            desc = datalistDef.getDescription();
        }
        
        if (copyDatalistDef != null) {
            String copyJson = copyDatalistDef.getJson();
            try {
                JSONObject obj = new JSONObject(copyJson);
                obj.put("id", listId);
                obj.put("name", name);
                obj.put("description", desc);
                json = obj.toString();
            } catch (Exception e) {
            }
        }

        if (json.isEmpty()) {
            listId = StringUtil.escapeString(listId, StringUtil.TYPE_JSON, null);
            name = StringUtil.escapeString(name, StringUtil.TYPE_JSON, null);
            desc = StringUtil.escapeString(desc, StringUtil.TYPE_JSON, null);
            json = "{\"id\":\"" + listId + "\",\"name\":\"" + name + "\",\"pageSize\":\"0\",\"pageSizeSelectorOptions\":\"10,20,30,40,50,100\",\"order\":\"\",\"orderBy\":\"\",\"description\":\"" + desc + "\",\"actions\":[],\"rowActions\":[],\"filters\":[],\"binder\":{\"name\":\"\",\"className\":\"\",\"properties\":{}},\"columns\":[]}";
        }

        return json;
    }
    
    public static String buildMobileActionLink(Object actionObject, Object row, Object menuId) {
        String link = "";
        if (actionObject != null && actionObject instanceof DataListAction && row != null) {
            DataListAction action = (DataListAction) actionObject;
            String href = action.getHref();
            String hrefParam = (action.getHrefParam() != null && action.getHrefParam().trim().length() > 0) ? action.getHrefParam() : "";
            String hrefColumn = (action.getHrefColumn() != null && action.getHrefColumn().trim().length() > 0) ? action.getHrefColumn() : "";
            link = href;
            
            if (hrefParam != null && hrefColumn != null && !hrefColumn.isEmpty()) {
                String[] params = hrefParam.split(";");
                String[] columns = hrefColumn.split(";");
                
                for (int i = 0; i < columns.length; i++ ) {
                    if (columns[i] != null && !columns[i].isEmpty()) {
                        boolean isValid = false;
                        if (params.length > i && params[i] != null && !params[i].isEmpty()) {
                            if (link.contains("?")) {
                                link += "&";
                            } else {
                                link += "?";
                            }
                            link += StringEscapeUtils.escapeHtml(params[i]);
                            link += "=";
                            isValid = true;
                        } if (!link.contains("?")) {
                            if (!link.endsWith("/")) {
                                link += "/";
                            }
                            isValid = true;
                        }
                        
                        if (isValid) {
                            Object paramValue = "";
                            try {
                                paramValue = LookupUtil.getBeanProperty(row, columns[i]);
                                
                                //handle for lowercase propertyName
                                if (paramValue == null) {
                                    paramValue = LookupUtil.getBeanProperty(row, columns[i].toLowerCase());
                                }
                                if (paramValue != null && paramValue instanceof Date) {
                                    paramValue = TimeZoneUtil.convertToTimeZone((Date) paramValue, null, AppUtil.getAppDateFormat());
                                }
                            } catch (Exception e) { }
                            
                            if (paramValue == null) {
                                paramValue = StringEscapeUtils.escapeHtml(columns[i]);
                            }
                            try {
                                link += (paramValue != null) ? URLEncoder.encode(paramValue.toString(), "UTF-8") : null;
                            } catch (UnsupportedEncodingException ex) {
                                link += paramValue;
                            }
                        }
                    }
                }
            }
        }
        if (link.startsWith("?") && menuId != null) {
            link = menuId + link;
        }
        return link;
    }
    
    private static Plugin loadPlugin(String className) {
        Plugin plugin = null;
        if (className != null && !className.isEmpty()) {
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            plugin = pluginManager.getPlugin(className);
        }
        return plugin;
    }
    
    private static Map<String, Object> convertRequestParamMap(Map params) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (String key : (Set<String>) params.keySet()) {
            key = StringEscapeUtils.escapeHtml(key);
            String[] paramValue = (String[]) params.get(key);
            if (paramValue.length == 1) {
                result.put(key, paramValue[0]);
            } else {
                result.put(key, paramValue);
            }
        }
        return result;
    }
    
    public static void generateBuilderProperties(Map<String, Object> props, String[] prefixes) {
        for (String prefix : prefixes) {
            String propKey = "BUILDER_GENERATED_" + prefix.toUpperCase().replace("-", "_");
            if (!props.containsKey(propKey + "CSS")) {
                Map<String, String> styles = AppPluginUtil.generateAttrAndStyles(props, prefix);

                props.put(propKey + "CSS", styles.get("cssClass"));
                props.put(propKey + "ATTR", styles.get("attr"));
                props.put(propKey + "MOBILE_STYLE", styles.get("mobileStyle"));
                props.put(propKey + "TABLET_STYLE", styles.get("tabletStyle"));
                props.put(propKey + "STYLE", styles.get("desktopStyle"));
                props.put(propKey + "HOVER_MOBILE_STYLE", styles.get("hoverMobileStyle"));
                props.put(propKey + "HOVER_TABLET_STYLE", styles.get("hoverTabletStyle"));
                props.put(propKey + "HOVER_STYLE", styles.get("hoverDesktopStyle"));
            }
        }
    }
}