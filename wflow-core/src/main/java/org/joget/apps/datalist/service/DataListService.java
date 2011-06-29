package org.joget.apps.datalist.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListBinder;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class to manage data lists
 */
@Service
public class DataListService {

    //@Autowired
    //DataListDao dataListDao;
    @Autowired
    PluginManager pluginManager;

    /**
     * Retrieve a data list by ID
     * @param id
     * @return
     */
    /*
    public DataList getDataList(String id) {
    String json = dataListDao.loadDataListDefinition(id);
    DataList dataList = fromJson(json);
    dataList.setId(id);
    return dataList;
    }

    public String getDataListJson(String id) {
    return dataListDao.loadDataListDefinition(id);
    }
     */
    /**
     * Create a DataList object from JSON definition
     * @param json
     * @return
     */
    public DataList fromJson(String json) {
        json = AppUtil.processHashVariable(json, null, null, null);

        DataList dataList = JsonUtil.fromJson(json, DataList.class);
        return dataList;
    }

    /**
     * Converts a DataList object into corresponding JSON definition
     * @param dataList
     * @return
     */
    /*
    public String toJson(DataList dataList) {
    String json = JsonUtil.toJson(dataList);
    return json;
    }
     */
    /**
     * Retrieve a binder by ID. For now the ID is the class name
     * @param id
     * @return
     */
    public DataListBinder getBinder(String id) {
        DataListBinder binder = null;
        try {
            String className = id;
            binder = (DataListBinder) pluginManager.getPlugin(className);
        } catch (Exception ex) {
            Logger.getLogger(DataListService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return binder;
    }

    /**
     * Retrieve an action by class name.
     * @param className
     * @return
     */
    public DataListAction getAction(String className) {
        DataListAction action = null;
        try {
            action = (DataListAction) pluginManager.getPlugin(className);
        } catch (Exception ex) {
            Logger.getLogger(DataListService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return action;
    }

    /**
     * Returns an array of available binder IDs. For now, ID is the fully qualified class name. 
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
     * Returns an array of available actions. For now, ID is the fully qualified class name.
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
     * Returns an array of available formatters. For now, ID is the fully qualified class name.
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
}
