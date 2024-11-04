package org.joget.apps.datalist.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.sql.DataSource;
import org.apache.commons.beanutils.BeanUtils;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.workflow.model.service.WorkflowManager;

/**
 * A base abstract class to develop a Datalist Binder Plugin
 */
public abstract class DataListBinderDefault extends ExtDefaultPlugin implements DataListBinder {

    public static final String USERVIEW_KEY_SYNTAX = "#userviewKey#";
    private DataList datalist;
    protected DataListInboxSetting inboxSetting;
    protected String driver;

    public DataList getDatalist() {
        return datalist;
    }

    public void setDatalist(DataList datalist) {
        this.datalist = datalist;
    }
    
    /**
     * To get the actual column name
     * @param name
     * @return 
     */
    @Override
    public String getColumnName(String name) {
        return name;
    }
    
    /**
     * Construct filter conditions
     * 
     * @param filterQueryObjects
     * @return 
     */
    public DataListFilterQueryObject processFilterQueryObjects(DataListFilterQueryObject[] filterQueryObjects) {
        DataListFilterQueryObject obj = new DataListFilterQueryObject();
        String condition = "";
        Collection<String> values = new ArrayList<String>();
        for (int i = 0; i < filterQueryObjects.length; i++) {
            if (condition.isEmpty()) {
                obj.setOperator(filterQueryObjects[i].getOperator());
            } else {
                condition += " " + filterQueryObjects[i].getOperator() + " ";
            }
            condition += filterQueryObjects[i].getQuery();
            if (filterQueryObjects[i].getValues() != null && filterQueryObjects[i].getValues().length > 0) {
                values.addAll(Arrays.asList(filterQueryObjects[i].getValues()));
            }
        }
        obj.setQuery(condition);
        if (values.size() > 0){
            obj.setValues((String[]) values.toArray(new String[0]));
        }
        return obj;
    }
    
    public void setDataListInboxSetting(DataListInboxSetting inboxSetting) {
        WorkflowManager wm = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
        this.inboxSetting = wm.processInboxSetting(inboxSetting);
    }

    public boolean isInbox() {
        return inboxSetting != null;
    }
    
    /**
     * Build the condition to retrieve assignments process id based on current user & delegated users
     * @return 
     */
    public DataListFilterQueryObject buildInboxCondition() {
        if (inboxSetting != null) {
            WorkflowManager wm = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
            return (DataListFilterQueryObject) wm.buildInboxCondition(inboxSetting, new DataListFilterQueryObject(), getDriver());
        }
        return null;
    }
    
    protected String getDriver() {
        if (driver == null) {
            try {
                DataSource ds = (DataSource) AppUtil.getApplicationContext().getBean("setupDataSource");
                driver = BeanUtils.getProperty(ds, "driverClassName");
            } catch (Exception e) {}
        } 
        return driver;
    }
}
