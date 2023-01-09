package org.joget.apps.datalist.model;

import java.util.HashMap;
import java.util.Map;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.service.JsonUtil;
import org.joget.apps.userview.model.UserviewPermission;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.util.WorkflowUtil;

/**
 * Base class for a data list action
 */
public abstract class DataListActionDefault extends ExtDefaultPlugin implements DataListAction {
    private DataList datalist;
    private static Map<String, String> defaultPropertyValues = new HashMap<String, String>();

    public DataList getDatalist() {
        return datalist;
    }

    public void setDatalist(DataList datalist) {
        this.datalist = datalist;
    }
    
    /**
     * Flag that decide to show an action object or not when no record
     * 
     * Default to the value of property "visible".
     * 
     * @return 
     */
    public Boolean getVisibleOnNoRecord() {
        if (getPropertyString("visible") != null && "true".equalsIgnoreCase(getPropertyString("visible"))) {
            return true;
        } else {
            return false;
        }
    }
    
    public Boolean supportColumn() {
        return true;
    }
    
    public Boolean supportRow() {
        return true;
    }
    
    public Boolean supportList() {
        return true;
    }
    
    public String getIcon() {
        return "";
    }
    
    public String getDefaultPropertyValues(){
        if (!DataListActionDefault.defaultPropertyValues.containsKey(getClassName())) {
            DataListActionDefault.defaultPropertyValues.put(getClassName(), PropertyUtil.getDefaultPropertyValues(getPropertyOptions()));
        }
        return DataListActionDefault.defaultPropertyValues.get(getClassName());
    }

    @Override
    public boolean isPermitted() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        UserviewPermission permission = pluginManager.getPlugin((Map<String, Object>) getProperty(JsonUtil.PROPERTY_PERMISSION));
        if(WorkflowUtil.getCurrentUsername() == null || permission == null) {
            return true;
        }

        DirectoryManager directoryManager = (DirectoryManager) AppUtil.getApplicationContext().getBean("directoryManager");
        User user = directoryManager.getUserByUsername(WorkflowUtil.getCurrentUsername());
        permission.setCurrentUser(user);
        return permission.isAuthorize();
    }
}
