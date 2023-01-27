package org.joget.apps.datalist.service;

import java.util.*;
import java.util.stream.Stream;

import org.displaytag.util.LookupUtil;
import org.joget.apps.app.dao.DatalistDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DatalistDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListAction;
import org.joget.apps.datalist.model.DataListBinder;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListColumnFormat;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.userview.model.*;
import org.joget.apps.userview.model.PwaOfflineValidation.WARNING_TYPE;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.directory.model.service.ExtDirectoryManager;
import org.joget.plugin.base.HiddenPlugin;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

/**
 * Service class to manage data lists
 */
@Service
public class DataListService {

    @Autowired
    PluginManager pluginManager;


    @Autowired
    WorkflowUserManager workflowUserManager;

    @Autowired
    @Qualifier("main")
    ExtDirectoryManager directoryManager;


    /**
     * Create a DataList object from JSON definition
     * @param json
     * @return
     */
    public DataList fromJson(String json) {
        return fromJson(json, null);
    }

    /**
     * Create a DataList object from JSON definition
     * @param json
     * @param theme
     * @return
     */
    public DataList fromJson(String json, @Nullable UserviewTheme theme) {
        json = AppUtil.processHashVariable(json, null, StringUtil.TYPE_JSON, null);

        final DataList dataList = JsonUtil.fromJson(json, DataList.class);

        final Optional<UserviewPermission> optPermission = Optional.ofNullable(dataList)
                .map(DataList::getPermission);

        if (optPermission.isPresent()) {
            final UserviewPermission permission = optPermission.get();
            final DirectoryManager directoryManager = (DirectoryManager) AppUtil.getApplicationContext().getBean("directoryManager");
            final User user = directoryManager.getUserByUsername(WorkflowUtil.getCurrentUsername());

            permission.setCurrentUser(user);
            if (!permission.isAuthorize()) {
                LogUtil.info(getClass().getName(), "User [" + user.getUsername() + "] is unauthorized to access datalist [" + dataList.getId() + "]");
                return null;
            }
        }

        // check column permission
        if (dataList != null) {
            DataListColumn[] columns = Optional.of(dataList)
                    .map(DataList::getColumns)
                    .map(Arrays::stream)
                    .orElseGet(Stream::empty)
                    .filter(dataListColumn -> dataListColumn.isPermitted())
                    .toArray(DataListColumn[]::new);

            dataList.setColumns(columns);

            dataList.setTheme(theme);
        }

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

    /**
     * Check authorization using permission
     *
     * @param dataList
     * @return
     */
    public boolean isAuthorize(DataList dataList) {
        return Optional.ofNullable(dataList)
                .map(DataList::getPermission)
                .map(userviewPermission -> {
                    User user = Optional.of(workflowUserManager.getCurrentUsername())
                            .map(directoryManager::getUserByUsername)
                            .orElse(null);

                    userviewPermission.setCurrentUser(user);
                    return userviewPermission.isAuthorize();
                })
                .orElse(true);
    }
}
