package org.joget.apps.datalist.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joget.apps.app.service.AppUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListFilter;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListBinder;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.json.JSONArray;

/**
 * Utility class containing methods to convert to/from JSON
 */
public class JsonUtil {

    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_PAGE_SIZE = "pageSize";
    public static final String PROPERTY_ORDER = "order";
    public static final String PROPERTY_ORDER_BY = "orderBy";
    public static final String PROPERTY_BINDER = "binder";
    public static final String PROPERTY_COLUMNS = "columns";
    public static final String PROPERTY_ROW_ACTIONS = "rowActions";
    public static final String PROPERTY_ACTION = "action";
    public static final String PROPERTY_ACTIONS = "actions";
    public static final String PROPERTY_FILTERS = "filters";
    public static final String PROPERTY_FORMAT = "format";

    /**
     * Create GSON object used to read/write JSON
     * @return
     */
    /*
    protected static Gson createGson() {
    GsonBuilder gsonBuilder = new GsonBuilder()
    .registerTypeAdapter(DataListBinder.class, new DataListJsonAdapter())
    .registerTypeAdapter(DataListAction.class, new DataListJsonAdapter())
    .registerTypeAdapter(DataListColumnFormat.class, new DataListJsonAdapter())
    .setPrettyPrinting();
    Gson gson = gsonBuilder.create();
    return gson;
    }
     */
    /**
     * Convert from JSON string into an object. Specifically to support data list model classes.
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
     * Convert from an object into JSON. Specifically to support data list model classes.
     * @param obj
     * @return
     */
    /*
    public static String toJson(Object obj) {
    Gson gson = createGson();
    return gson.toJson(obj);
    }
     */
    public static Object parseElementFromJson(String json) {
        try {
            // create json object
            JSONObject obj = new JSONObject(json);

            // parse json object
            Object object = JsonUtil.parseElementFromJsonObject(obj);

            return object;
        } catch (Exception ex) {
            Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Object parseElementFromJsonObject(JSONObject obj) throws Exception {
        DataList object = (DataList) new DataList();
        if (object != null) {

            if (obj.has(JsonUtil.PROPERTY_ID)) {
                object.setId(obj.getString(JsonUtil.PROPERTY_ID));
            }

            if (obj.has(JsonUtil.PROPERTY_NAME)) {
                object.setName(obj.getString(JsonUtil.PROPERTY_NAME));
            }

            if (obj.has(JsonUtil.PROPERTY_PAGE_SIZE)) {
                object.setPageSize(obj.getInt(JsonUtil.PROPERTY_PAGE_SIZE));
            }

//            //set order
//            objProperty = obj.getString(JsonUtil.PROPERTY_ORDER);
//            if(objProperty !=null)
//                object.se(objProperty);
//
//            //set orderBy
//            objProperty = obj.getString(JsonUtil.PROPERTY_ORDER_BY);
//            if(objProperty !=null)
//                object.setId(objProperty);

            //set columns
            Collection<DataListColumn> columns = JsonUtil.parseColumnsFromJsonObject(obj);
            DataListColumn[] temp = (DataListColumn[]) columns.toArray(new DataListColumn[columns.size()]);
            object.setColumns(temp);

            //set binder
            DataListBinder binder = JsonUtil.parseBinderFromJsonObject(obj);
            object.setBinder(binder);

            //set actions
            Collection<DataListAction> actions = JsonUtil.parseActionsFromJsonObject(obj);
            DataListAction[] temp2 = (DataListAction[]) actions.toArray(new DataListAction[actions.size()]);
            object.setActions(temp2);

            //set row actions
            Collection<DataListAction> rowActions = JsonUtil.parseRowActionsFromJsonObject(obj);
            DataListAction[] temp3 = (DataListAction[]) rowActions.toArray(new DataListAction[rowActions.size()]);
            object.setRowActions(temp3);

            //set filters
            Collection<DataListFilter> filters = JsonUtil.parseFiltersFromJsonObject(obj);
            DataListFilter[] temp4 = (DataListFilter[]) filters.toArray(new DataListFilter[filters.size()]);
            object.setFilters(temp4);

        }

        return object;
    }

    public static Collection<DataListFilter> parseFiltersFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        Collection<DataListFilter> property = new ArrayList<DataListFilter>();

        if (!obj.isNull(JsonUtil.PROPERTY_FILTERS)) {
            JSONArray filters = obj.getJSONArray(JsonUtil.PROPERTY_FILTERS);

            for (int i = 0; i < filters.length(); i++) {
                JSONObject filter = filters.getJSONObject(i);
                DataListFilter dataListFilter = new DataListFilter();

                if (filter.has("name")) {
                    dataListFilter.setName(filter.getString("name"));
                }
                if (filter.has("label")) {
                    dataListFilter.setLabel(filter.getString("label"));
                }

                property.add(dataListFilter);
            }
        }
        return property;
    }

    public static Collection<DataListAction> parseRowActionsFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        Collection<DataListAction> property = new ArrayList<DataListAction>();

        if (!obj.isNull(JsonUtil.PROPERTY_ROW_ACTIONS)) {
            JSONArray actions = obj.getJSONArray(JsonUtil.PROPERTY_ROW_ACTIONS);

            for (int i = 0; i < actions.length(); i++) {
                JSONObject action = actions.getJSONObject(i);
                if (action.has("className")) {
                    String className = action.getString("className");
                    DataListAction dataListAction = (DataListAction) loadPlugin(className);
                    if (dataListAction != null) {
                        Properties properties = new Properties();
                        for (int j = 0; j < action.names().length(); j++) {
                            String propertyName = action.names().getString(j);
                            if ("properties".equals(propertyName)) {
                                JSONObject propertiesObj = action.getJSONObject("properties");
                                if (propertiesObj.names() != null) {
                                    for (int k = 0; k < propertiesObj.names().length(); k++) {
                                        propertyName = propertiesObj.names().getString(k);
                                        properties.setProperty(propertyName, propertiesObj.getString(propertyName));
                                    }
                                }
                            } else if (!"className".equals(propertyName)) {
                                properties.setProperty(propertyName, action.getString(propertyName));
                            }
                        }
                        dataListAction.setProperties(properties);
                        property.add(dataListAction);
                    }
                }
            }
        }
        return property;
    }

    public static Collection<DataListAction> parseActionsFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        Collection<DataListAction> property = new ArrayList<DataListAction>();

        if (!obj.isNull(JsonUtil.PROPERTY_ACTIONS)) {
            JSONArray actions = obj.getJSONArray(JsonUtil.PROPERTY_ACTIONS);

            for (int i = 0; i < actions.length(); i++) {
                JSONObject action = actions.getJSONObject(i);
                if (action.has("className")) {
                    String className = action.getString("className");
                    DataListAction dataListAction = (DataListAction) loadPlugin(className);
                    if (dataListAction != null) {
                        Properties properties = new Properties();
                        for (int j = 0; j < action.names().length(); j++) {
                            String propertyName = action.names().getString(j);
                            if ("properties".equals(propertyName)) {
                                JSONObject propertiesObj = action.getJSONObject("properties");
                                if (propertiesObj.names() != null) {
                                    for (int k = 0; k < propertiesObj.names().length(); k++) {
                                        propertyName = propertiesObj.names().getString(k);
                                        properties.setProperty(propertyName, propertiesObj.getString(propertyName));
                                    }
                                }
                            } else if (!"className".equals(propertyName)) {
                                properties.setProperty(propertyName, action.getString(propertyName));
                            }
                        }
                        dataListAction.setProperties(properties);
                        property.add(dataListAction);
                    }
                }
            }
        }
        return property;
    }

    public static DataListBinder parseBinderFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        if (!obj.isNull(JsonUtil.PROPERTY_BINDER)) {
            JSONObject binderObj = obj.getJSONObject(JsonUtil.PROPERTY_BINDER);
            if (binderObj.has("className")) {
                String className = binderObj.getString("className");
                DataListBinder dataListBinder = (DataListBinder) loadPlugin(className);
                if (dataListBinder != null) {
                    if (binderObj.has("properties")) {
                        Properties properties = new Properties();
                        JSONObject propertiesObj = binderObj.getJSONObject("properties");
                        if (propertiesObj.names() != null) {
                            for (int j = 0; j < propertiesObj.names().length(); j++) {
                                String propertyName = propertiesObj.names().getString(j);
                                properties.setProperty(propertyName, propertiesObj.getString(propertyName));
                            }
                        }
                        dataListBinder.setProperties(properties);
                    }
                    return dataListBinder;
                }
            }
        }
        return null;
    }

    public static DataListAction parseActionFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        try {
            if (!obj.isNull(JsonUtil.PROPERTY_ACTION) && !"".equals(obj.getString(PROPERTY_ACTION))) {
                JSONObject actionObj = obj.getJSONObject(JsonUtil.PROPERTY_ACTION);
                if (actionObj.has("className")) {
                    String className = actionObj.getString("className");
                    DataListAction dataListAction = (DataListAction) loadPlugin(className);
                    if (dataListAction != null) {
                        if (actionObj.has("properties")) {
                            Properties properties = new Properties();
                            JSONObject propertiesObj = actionObj.getJSONObject("properties");
                            if (propertiesObj.names() != null) {
                                for (int j = 0; j < propertiesObj.names().length(); j++) {
                                    String propertyName = propertiesObj.names().getString(j);
                                    properties.setProperty(propertyName, propertiesObj.getString(propertyName));
                                }
                            }
                            dataListAction.setProperties(properties);
                        }
                        return dataListAction;
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(JsonUtil.class.getName()).log(Level.WARNING, "Invalid action for {0}", obj);
        }
        return null;
    }

    public static DataListColumnFormat parseFormatterFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        try {
            if (!obj.isNull(JsonUtil.PROPERTY_FORMAT) && !"".equals(obj.getString(PROPERTY_FORMAT))) {
                JSONObject formatterObj = obj.getJSONObject(JsonUtil.PROPERTY_FORMAT);
                if (formatterObj.has("className")) {
                    String className = formatterObj.getString("className");
                    DataListColumnFormat dataListColumnFormat = (DataListColumnFormat) loadPlugin(className);
                    if (dataListColumnFormat != null) {
                        if (formatterObj.has("properties")) {
                            Properties properties = new Properties();
                            JSONObject propertiesObj = formatterObj.getJSONObject("properties");
                            if (propertiesObj.names() != null) {
                                for (int j = 0; j < propertiesObj.names().length(); j++) {
                                    String propertyName = propertiesObj.names().getString(j);
                                    properties.setProperty(propertyName, propertiesObj.getString(propertyName));
                                }
                            }
                            dataListColumnFormat.setProperties(properties);
                        }
                        return dataListColumnFormat;
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(JsonUtil.class.getName()).log(Level.WARNING, "Invalid formater for {0}", obj);
        }
        return null;
    }

    public static Collection<DataListColumn> parseColumnsFromJsonObject(JSONObject obj) throws JSONException, InstantiationException, IllegalAccessException {
        Collection<DataListColumn> property = new ArrayList<DataListColumn>();

        if (!obj.isNull(JsonUtil.PROPERTY_COLUMNS)) {
            JSONArray columns = obj.getJSONArray(JsonUtil.PROPERTY_COLUMNS);

            for (int i = 0; i < columns.length(); i++) {
                JSONObject column = columns.getJSONObject(i);
                DataListColumn dataListColumn = new DataListColumn();

                if (column.has("name") && !column.isNull("name")) {
                    dataListColumn.setName(column.getString("name"));
                }
                if (column.has("label") && !column.isNull("label")) {
                    dataListColumn.setLabel(column.getString("label"));
                }
                if (column.has("filterable") && !column.isNull("filterable")) {
                    dataListColumn.setFilterable(column.getBoolean("filterable"));
                }
                if (column.has("href") && !column.isNull("href")) {
                    dataListColumn.setHref(column.getString("href"));
                }
                if (column.has("hrefParam") && !column.isNull("hrefParam")) {
                    dataListColumn.setHrefParam(column.getString("hrefParam"));
                }
                if (column.has("hrefColumn") && !column.isNull("hrefColumn")) {
                    dataListColumn.setHrefColumn(column.getString("hrefColumn"));
                }
                if (column.has("sortable") && !column.isNull("sortable")) {
                    dataListColumn.setSortable(column.getBoolean("sortable"));
                }
                if (column.has(JsonUtil.PROPERTY_ACTION) && !column.isNull(JsonUtil.PROPERTY_ACTION)) {
                    DataListAction action = JsonUtil.parseActionFromJsonObject(column);
                    dataListColumn.setAction(action);
                }

                if (column.has(JsonUtil.PROPERTY_FORMAT) && !column.isNull(PROPERTY_FORMAT)) {
                    Collection<DataListColumnFormat> formatCollection = new ArrayList<DataListColumnFormat>();
                    DataListColumnFormat format = JsonUtil.parseFormatterFromJsonObject(column);
                    formatCollection.add(format);

                    dataListColumn.setFormats(formatCollection);
                }
                property.add(dataListColumn);
            }
        }
        return property;
    }

    private static Plugin loadPlugin(String className) {
        Plugin plugin = null;
        if (className != null && !className.isEmpty()) {
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            plugin = pluginManager.getPlugin(className);
        }
        return plugin;
    }
}
