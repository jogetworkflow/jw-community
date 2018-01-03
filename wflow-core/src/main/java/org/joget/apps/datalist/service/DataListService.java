package org.joget.apps.datalist.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.displaytag.util.LookupUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListBinder;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.apps.form.model.FormRow;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.TimeZoneUtil;
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
            if (plugin instanceof DataListAction) {
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
        }
        return null;
    }
}
