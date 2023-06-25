package org.joget.apps.datalist.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringEscapeUtils;
import org.displaytag.util.LookupUtil;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilter;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListBinder;
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
            
            if (obj.has(PROPERTY_ID)) {
                object.setId(obj.getString(PROPERTY_ID));
            }
            if (obj.has(PROPERTY_NAME)) {
                object.setName(obj.getString(PROPERTY_NAME));
            }
            if (obj.has(PROPERTY_DESC)) {
                object.setDescription(obj.getString(PROPERTY_DESC));
            }
            if (obj.has(PROPERTY_PAGE_SIZE)) {
                object.setDefaultPageSize(obj.getInt(PROPERTY_PAGE_SIZE));
            }
            if (obj.has(PROPERTY_HIDE_PAGE_SIZE) && !obj.isNull(PROPERTY_HIDE_PAGE_SIZE)) {
                object.setShowPageSizeSelector(!obj.getString(PROPERTY_HIDE_PAGE_SIZE).equals("true"));
            }
            if (obj.has(PROPERTY_PAGE_SIZE_SELECTOR_OPTIONS) && !obj.isNull(PROPERTY_PAGE_SIZE_SELECTOR_OPTIONS)) {
                object.setPageSizeList(obj.getString(PROPERTY_PAGE_SIZE_SELECTOR_OPTIONS));
            }
            if (obj.has(PROPERTY_ORDER)) {
                object.setDefaultOrder(obj.getString(PROPERTY_ORDER));
            }
            if (obj.has(PROPERTY_ORDER_BY)) {
                object.setDefaultSortColumn(obj.getString(PROPERTY_ORDER_BY));
            }
            if (obj.has(PROPERTY_USE_SESSION) && !obj.isNull(PROPERTY_USE_SESSION)) {
                object.setUseSession(obj.getString(PROPERTY_USE_SESSION).equals("true"));
            }
            if (obj.has(PROPERTY_SHOW_DATA_ONFILTER) && !obj.isNull(PROPERTY_SHOW_DATA_ONFILTER)) {
                object.setShowDataWhenFilterSet(obj.getString(PROPERTY_SHOW_DATA_ONFILTER).equals("true"));
            }
            if (obj.has(PROPERTY_CONSIDER_FILTER_WHEN_GET_TOTAL) && !obj.isNull(PROPERTY_CONSIDER_FILTER_WHEN_GET_TOTAL)) {
                object.setConsiderFilterWhenGetTotal(obj.getString(PROPERTY_CONSIDER_FILTER_WHEN_GET_TOTAL).equals("true"));
            }
            if (obj.has(PROPERTY_DISABLE_RESPONSIVE) && !obj.isNull(PROPERTY_DISABLE_RESPONSIVE)) {
                object.setDisableResponsive(obj.getString(PROPERTY_DISABLE_RESPONSIVE).equals("true"));
            }
            if (obj.has(PROPERTY_RESPONSIVE) && !obj.isNull(PROPERTY_RESPONSIVE)) {
                object.setResponsiveJson(obj.getJSONArray(PROPERTY_RESPONSIVE).toString());
            }
            if (obj.has(PROPERTY_REPONSIVE_SEARCH_POPUP) && !obj.isNull(PROPERTY_REPONSIVE_SEARCH_POPUP)) {
                object.setResponsiveSearchPopup(obj.getString(PROPERTY_REPONSIVE_SEARCH_POPUP).equals("true"));
            }
            
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
            if (obj.has("noPermissionMessage") && !obj.optString("noPermissionMessage").isEmpty()) {
                object.setUnauthorizedMsg(obj.optString("noPermissionMessage"));
            }

            //set binder
            DataListBinder binder = parseBinderFromJsonObject(obj);
            object.setBinder(binder);
                
            if (hasPermission) {
                //set columns
                Collection<DataListColumn> columns = parseColumnsFromJsonObject(obj, permissionKey);
                DataListColumn[] temp = (DataListColumn[]) columns.toArray(new DataListColumn[columns.size()]);
                object.setColumns(temp);

                //set actions
                Collection<DataListAction> actions = parseActionsFromJsonObject(obj, permissionKey);
                DataListAction[] temp2 = (DataListAction[]) actions.toArray(new DataListAction[actions.size()]);
                object.setActions(temp2);

                //set row actions
                Collection<DataListAction> rowActions = parseRowActionsFromJsonObject(obj, permissionKey);
                DataListAction[] temp3 = (DataListAction[]) rowActions.toArray(new DataListAction[rowActions.size()]);
                object.setRowActions(temp3);

                //set filters
                Collection<DataListFilter> filters = parseFiltersFromJsonObject(obj, permissionKey);
                DataListFilter[] temp4 = (DataListFilter[]) filters.toArray(new DataListFilter[filters.size()]);
                object.setFilters(temp4);
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
                
                if (filter.has(PROPERTY_NAME)) {
                    dataListFilter.setName(filter.getString(PROPERTY_NAME));
                }
                if (filter.has(PROPERTY_LABEL)) {
                    dataListFilter.setLabel(filter.getString(PROPERTY_LABEL));
                }
                if (filter.has(PROPERTY_OPERATOR)) {
                    dataListFilter.setOperator(filter.getString(PROPERTY_OPERATOR));
                }
                if (filter.has(PROPERTY_FILTER_TYPE)) {
                    DataListFilterType type = parseFilterTypeFromJsonObject(filter);
                    dataListFilter.setType(type);
                }
                if (filter.has(PROPERTY_HIDDEN)) {
                    dataListFilter.setHidden("true".equalsIgnoreCase(filter.getString(PROPERTY_HIDDEN)));
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
                        property.add(dataListAction);
                    }
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
                DataListColumn dataListColumn = new DataListColumn();
                
                if (column.has(PROPERTY_NAME) && !column.isNull(PROPERTY_NAME)) {
                    dataListColumn.setName(column.getString(PROPERTY_NAME));
                }
                if (column.has(PROPERTY_LABEL) && !column.isNull(PROPERTY_LABEL)) {
                    dataListColumn.setLabel(column.getString(PROPERTY_LABEL));
                }
                if (column.has(PROPERTY_SORTABLE) && !column.isNull(PROPERTY_SORTABLE)) {
                    dataListColumn.setSortable(column.getBoolean(PROPERTY_SORTABLE));
                }
                if (column.has(PROPERTY_HIDDEN) && !column.isNull(PROPERTY_HIDDEN)) {
                    dataListColumn.setHidden(column.getBoolean(PROPERTY_HIDDEN));
                }
                if (column.has(PROPERTY_WIDTH) && !column.isNull(PROPERTY_WIDTH)) {
                    dataListColumn.setWidth(column.getString(PROPERTY_WIDTH));
                }
                if (column.has(PROPERTY_STYLE) && !column.isNull(PROPERTY_STYLE)) {
                    dataListColumn.setStyle(column.getString(PROPERTY_STYLE));
                }
                if (column.has(PROPERTY_ALIGNMENT) && !column.isNull(PROPERTY_ALIGNMENT)) {
                    dataListColumn.setAlignment(column.getString(PROPERTY_ALIGNMENT));
                }
                if (column.has(PROPERTY_HEADER_ALIGNMENT) && !column.isNull(PROPERTY_HEADER_ALIGNMENT)) {
                    dataListColumn.setHeaderAlignment(column.getString(PROPERTY_HEADER_ALIGNMENT));
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
                if (column.has(PROPERTY_RENDER_HTML) && !column.isNull(PROPERTY_RENDER_HTML) && !column.getString(PROPERTY_RENDER_HTML).isEmpty()) {
                    dataListColumn.setRenderHtml(column.getBoolean(PROPERTY_RENDER_HTML));
                }
                
                dataListColumn.setProperties(PropertyUtil.getProperties(column));
                
                if (!Permission.DEFAULT.equals(permissionKey)) {
                    if (column.has("permission_rules") && column.getJSONObject("permission_rules").has(permissionKey)) {
                        JSONObject rule = column.getJSONObject("permission_rules").getJSONObject(permissionKey);
                        if (rule.has(PROPERTY_HIDDEN) && "true".equals(rule.getString(PROPERTY_HIDDEN))) {
                            dataListColumn.setHidden(true);
                            if (rule.has("include_export") && "true".equals(rule.getString("include_export"))) {
                                dataListColumn.setProperty("include_export", "true");
                                dataListColumn.setProperty("exclude_export", "");
                            } else {
                                dataListColumn.setProperty("include_export", "");
                                dataListColumn.setProperty("exclude_export", "");
                            }
                        } else {
                            dataListColumn.setHidden(false);
                            if (rule.has("exclude_export") && "true".equals(rule.getString("exclude_export"))) {
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
                
                property.add(dataListColumn);
            }
        }
        return property;
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
            json = "{\"id\":\"" + listId + "\",\"name\":\"" + name + "\",\"pageSize\":\"0\",\"order\":\"\",\"orderBy\":\"\",\"description\":\"" + desc + "\",\"actions\":[],\"rowActions\":[],\"filters\":[],\"binder\":{\"name\":\"\",\"className\":\"\",\"properties\":{}},\"columns\":[]}";
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
}