package org.joget.apps.datalist.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.displaytag.util.LookupUtil;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListBinder;
import org.joget.apps.datalist.model.DataListBinderDefault;
import static org.joget.apps.datalist.model.DataListBinderDefault.USERVIEW_KEY_SYNTAX;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.apps.datalist.model.DataListRow;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.userview.model.PwaOfflineNotSupported;
import org.joget.apps.userview.model.PwaOfflineReadonly;
import org.joget.apps.userview.model.PwaOfflineValidation;
import org.joget.apps.userview.model.PwaOfflineValidation.WARNING_TYPE;
import org.joget.apps.userview.model.Userview;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.plugin.base.HiddenPlugin;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class to manage data lists
 */
@Service
public class DataListService {

    @Autowired
    PluginManager pluginManager;

    /**
     * Create a DataList object from JSON definition
     * @param json
     * @return
     */
    public DataList fromJson(String json) {
        json = AppUtil.processHashVariable(json, null, StringUtil.TYPE_JSON, null);

        DataList dataList = JsonUtil.fromJson(json, DataList.class);
        return dataList;
    }

    /**
     * Retrieve a binder plugin by ID. For now the ID is the class name
     * @param id
     * @return
     */
    public DataListBinder getBinder(String id) {
        DataListBinder binder = null;
        try {
            String className = id;
            binder = (DataListBinder) pluginManager.getPlugin(className);
        } catch (Exception ex) {
            LogUtil.error(DataListService.class.getName(), ex, "");
        }
        return binder;
    }

    /**
     * Retrieve an action plugin by class name.
     * @param className
     * @return
     */
    public DataListAction getAction(String className) {
        DataListAction action = null;
        try {
            action = (DataListAction) pluginManager.getPlugin(className);
        } catch (Exception ex) {
            LogUtil.error(DataListService.class.getName(), ex, "");
        }
        return action;
    }

    /**
     * Returns an array of available binder plugins. For now, ID is the fully qualified class name. 
     * @return
     */
    public DataListBinder[] getAvailableBinders() {
        Collection<DataListBinder> list = new ArrayList<DataListBinder>();
        Collection<Plugin> pluginList = pluginManager.list(DataListBinder.class);
        for (Plugin plugin : pluginList) {
            if (plugin instanceof DataListBinder) {
                list.add((DataListBinder) plugin);
            }
        }
        DataListBinder[] result = (DataListBinder[]) list.toArray(new DataListBinder[0]);
        return result;
    }

    /**
     * Returns an array of available action plugins. For now, ID is the fully qualified class name.
     * @return
     */
    public DataListAction[] getAvailableActions() {
        Collection<DataListAction> list = new ArrayList<DataListAction>();
        Collection<Plugin> pluginList = pluginManager.list(DataListAction.class);
        for (Plugin plugin : pluginList) {
            if (plugin instanceof DataListAction && !(plugin instanceof HiddenPlugin)) {
                list.add((DataListAction) plugin);
            }
        }
        DataListAction[] result = (DataListAction[]) list.toArray(new DataListAction[0]);
        return result;
    }

    /**
     * Returns an array of available formatter plugins. For now, ID is the fully qualified class name.
     * @return
     */
    public DataListColumnFormat[] getAvailableFormats() {
        Collection<DataListColumnFormat> list = new ArrayList<DataListColumnFormat>();
        Collection<Plugin> pluginList = pluginManager.list(DataListColumnFormat.class);
        for (Plugin plugin : pluginList) {
            if (plugin instanceof DataListColumnFormat) {
                list.add((DataListColumnFormat) plugin);
            }
        }
        DataListColumnFormat[] result = (DataListColumnFormat[]) list.toArray(new DataListColumnFormat[0]);
        return result;
    }
    
    public static Object evaluateColumnValueFromRow(Object row, String propertyName) {
        if (row instanceof DataListRow) {
            return ((DataListRow) row).get(propertyName);
        }
        
        if (propertyName != null && !propertyName.isEmpty()) {
            try {
                Object value = LookupUtil.getBeanProperty(row, propertyName);
                
                //handle for lowercase propertyName
                if (value == null) {
                    value = LookupUtil.getBeanProperty(row, propertyName.toLowerCase());
                }
                
                //handle for numeric field name
                if (value == null && row instanceof FormRow && Character.isDigit(propertyName.charAt(0))) {
                    propertyName = "t__" + propertyName;
                    value = LookupUtil.getBeanProperty(row, propertyName);
                    
                    //handle for lowercase propertyName
                    if (value == null) {
                        value = LookupUtil.getBeanProperty(row, propertyName.toLowerCase());
                    }
                }
                
                if (value != null && value instanceof Date) {
                    value = TimeZoneUtil.convertToTimeZone((Date) value, null, AppUtil.getAppDateFormat());
                }
                return value;
            } catch (Exception e) {}
            
            if ((row instanceof Map) && propertyName.contains(".")) {
                Map rowMap = (Map) row;
                Object value = null;
                if (rowMap.containsKey(propertyName)) {
                    value = rowMap.get(propertyName);
                } else if (rowMap.containsKey(propertyName.toLowerCase())) {
                    //handle for lowercase propertyName
                    value = rowMap.get(propertyName.toLowerCase());
                }
                if (value != null && value instanceof Date) {
                    value = TimeZoneUtil.convertToTimeZone((Date) value, null, AppUtil.getAppDateFormat());
                }
                return value;
            }
        }
        return null;
    }
    
    /**
     * Used to modify oracle datetime format
     */
    public static void alterOracleSession() {
        try {
            DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
            String driver = BeanUtils.getProperty(ds, "driverClassName");
            
            if (driver.equals("oracle.jdbc.driver.OracleDriver")) {
                Connection con = null;
                PreparedStatement pstmt = null;
                try {
                    con = ds.getConnection();
                    pstmt = con.prepareStatement("ALTER SESSION SET NLS_TIMESTAMP_FORMAT = 'YYYY-MM-DD HH:MI:SS.FF'");
                    pstmt.executeUpdate();
                } catch (Exception e) {
                } finally {
                    try {
                        if (pstmt != null) {
                            pstmt.close();
                        }
                    } catch(Exception e){}
                    try {
                        if (con != null) {
                            con.close();
                        }
                    } catch(Exception e){}
                }
            }
        } catch (Exception e) {
            LogUtil.error(DataListService.class.getName(), e, "");
        }
    }
    
    /**
     * Used to generate the where clause query object
     * 
     * @param binder
     * @param properties
     * @param filterQueryObjects
     * @return 
     */
    public static DataListFilterQueryObject processFilterQueryObjects(DataListBinderDefault binder, Map properties, DataListFilterQueryObject[] filterQueryObjects) {
        String condition = "";
        Collection<String> values = new ArrayList<String>();

        //Genarate the condition based on the filter properties in plugin
        Object[] filters = (Object[]) properties.get("filters");
        if (filters != null && filters.length > 0) {
            Collection<DataListFilterQueryObject> conditionObjects = new ArrayList<DataListFilterQueryObject>();

            for (Object o : filters) {
                Map filterMap = (HashMap) o;
                DataListFilterQueryObject obj = new DataListFilterQueryObject();
                obj.setOperator(filterMap.get("join").toString());

                String field = filterMap.get("field").toString();
                field = binder.getColumnName(field);
                String operator = filterMap.get("operator").toString();
                String value = filterMap.get("value").toString();

                String query = "";
                String[] fvalues = null;

                if ("IS TRUE".equals(operator) || "IS FALSE".equals(operator) || "IS NULL".equals(operator) || "IS NOT NULL".equals(operator)) {
                    query = field + " " + operator;
                    fvalues = new String[0];
                } else if ("IN".equals(operator) || "NOT IN".equals(operator)) {
                    List<String> queries = new ArrayList<String>();
                    List<String> valuesList = new ArrayList<String>();
                    fvalues = value.split(";");
                    if (fvalues.length > 0) {
                        for (String v : fvalues) {
                            queries.add("("+field+" = ? or "+field+" like ? or "+field+" like ? or "+field+" like ?)");
                            valuesList.add(v);
                            valuesList.add(v + ";%");
                            valuesList.add("%;" + v + ";%");
                            valuesList.add("%;" + v);
                        }
                        if ("NOT IN".equals(operator)) {
                            query += "NOT ";
                        }
                        query += "(" + StringUtils.join(queries, " or ") + ")";
                        fvalues = valuesList.toArray(new String[0]);
                    }
                } else if ("=".equals(operator) || "<>".equals(operator)) {
                    query = field + " " + operator + " ?";
                    fvalues = new String[]{value};
                }else if (!value.isEmpty()) {
                    query = field + " " + operator + " ?";
                    fvalues = new String[]{value};
                }

                if (!query.isEmpty()) {
                    obj.setQuery(query);
                    obj.setValues(fvalues);
                    conditionObjects.add(obj);
                }
            }
            
            //processing the filter as single condition, then join it to `condition` & `filterValues` 
            DataListFilterQueryObject conditionFilterObj = binder.processFilterQueryObjects(conditionObjects.toArray(new DataListFilterQueryObject[0]));
            if (conditionFilterObj != null && !conditionFilterObj.getQuery().isEmpty()) {
                condition = "(" + conditionFilterObj.getQuery() + ")";
                if (conditionFilterObj.getValues() != null && conditionFilterObj.getValues().length > 0) {
                    values.addAll(Arrays.asList(conditionFilterObj.getValues()));
                }
            }
        }
        
        //This is the filter values from the list filter, loop to processing it
        if (filterQueryObjects != null && filterQueryObjects.length > 0) {
            for (int i = 0; i < filterQueryObjects.length; i++) {
                if (!condition.isEmpty()) {
                    condition += " " + filterQueryObjects[i].getOperator() + " ";
                }
                condition += filterQueryObjects[i].getQuery();
                if (filterQueryObjects[i].getValues() != null && filterQueryObjects[i].getValues().length > 0) {
                    values.addAll(Arrays.asList(filterQueryObjects[i].getValues()));
                }
            }
        }
        
        if (!condition.isEmpty()) {
            condition = " WHERE " + condition;
        }
        
        //if extra condition property or the userview key has value, add it to condition as well
        String extraCondition = (properties.get("extraCondition") != null) ? properties.get("extraCondition").toString() : null;
        String keyName = null;
        if (properties.get(Userview.USERVIEW_KEY_NAME) != null) {
            keyName = properties.get(Userview.USERVIEW_KEY_NAME).toString();
        }
        String keyValue = null;
        if (properties.get(Userview.USERVIEW_KEY_VALUE) != null) {
            keyValue = properties.get(Userview.USERVIEW_KEY_VALUE).toString();
        }

        if (extraCondition != null && extraCondition.contains(USERVIEW_KEY_SYNTAX)) {
            if (keyValue == null) {
                keyValue = "";
            }
            extraCondition = extraCondition.replaceAll(USERVIEW_KEY_SYNTAX, StringUtil.escapeRegex(keyValue));
        } else if (keyName != null && !keyName.isEmpty() && keyValue != null && !keyValue.isEmpty()) {
            if (condition.trim().length() > 0) {
                condition += " AND ";
            } else {
                condition += " WHERE ";
            }
            condition += binder.getColumnName(keyName) + " = ?";
            values.add(keyValue);
        }

        if (extraCondition != null && !extraCondition.isEmpty()) {
            if (condition.trim().length() > 0) {
                condition += " AND ";
            } else {
                condition += " WHERE ";
            }
            condition += extraCondition;
        }
        
        //return single filter query object after combined all query chunk
        DataListFilterQueryObject obj = new DataListFilterQueryObject();
        obj.setQuery(condition);
        if (values.size() > 0){
            obj.setValues((String[]) values.toArray(new String[0]));
        }
        
        return obj;
    }
    
    public static boolean pwaOfflineValidation(AppDefinition appDef, String listId, boolean checkAction) {
        if (listId != null && !listId.isEmpty()) {
            DataListService dataListService = (DataListService) AppUtil.getApplicationContext().getBean("dataListService");
            DatalistDefinitionDao datalistDefinitionDao = (DatalistDefinitionDao) AppUtil.getApplicationContext().getBean("datalistDefinitionDao");
            DatalistDefinition datalistDefinition = datalistDefinitionDao.loadById(listId, appDef);

            if (datalistDefinition != null) {
                return pwaOfflineValidation(dataListService.fromJson(datalistDefinition.getJson()), checkAction);
            }
        }
        return true;
    }
    
    public static boolean pwaOfflineValidation(DataList list, boolean checkAction) {
        if (list != null) {
            DataListColumn[] columns = list.getColumns();
            if (columns != null && columns.length > 0) {
                for (DataListColumn c : columns) {
                    Collection<DataListColumnFormat> formats = c.getFormats();
                    if (formats != null && !formats.isEmpty()) {
                        for (DataListColumnFormat f : formats) {
                            if (!pwaOfflineValidation(f)) {
                                return false;
                            }
                        }
                    }
                    if (checkAction) {
                        if (!pwaOfflineValidation(c.getAction())) {
                            return false;
                        }
                    }
                }
            }
            if (checkAction) {
                DataListAction[] actions = list.getActions();
                if (actions != null && actions.length > 0) {
                    for (DataListAction a : actions) {
                        if (!pwaOfflineValidation(a)) {
                            return false;
                        }
                    }
                }
                DataListAction[] rowActions = list.getRowActions();
                if (rowActions != null && rowActions.length > 0) {
                    for (DataListAction a : rowActions) {
                        if (!pwaOfflineValidation(a)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    
    protected static boolean pwaOfflineValidation(Object o) {
        if (o != null) {
            if (o instanceof PwaOfflineNotSupported || o instanceof PwaOfflineReadonly) {
                return false;
            } else if (o instanceof PwaOfflineValidation) {
                Map<WARNING_TYPE, String[]> results = ((PwaOfflineValidation) o).validation();
                if (results != null) {
                    if (results.containsKey(WARNING_TYPE.NOT_SUPPORTED) || results.containsKey(WARNING_TYPE.READONLY)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
