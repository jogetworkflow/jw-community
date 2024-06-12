package org.joget.apps.datalist.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.workflow.util.WorkflowUtil;

/**
 * A base abstract class to develop a Datalist Binder Plugin
 */
public abstract class DataListBinderDefault extends ExtDefaultPlugin implements DataListBinder {

    public static final String USERVIEW_KEY_SYNTAX = "#userviewKey#";
    private DataList datalist;
    protected DataListInboxSetting inboxSetting;

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
        this.inboxSetting = inboxSetting;
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
            DataListFilterQueryObject queryObj = new DataListFilterQueryObject();
            Collection<String> values = new ArrayList<String>();
            
            String conds = "";
            if (inboxSetting.getActvityDefIds() != null && inboxSetting.getActvityDefIds().length > 0) {
                String prefix = "_";
                String actIdConds = "";
                if (inboxSetting.getAppId() != null && !inboxSetting.getAppId().isEmpty()) {
                    prefix += inboxSetting.getAppId() + "_";
                }
                if (inboxSetting.getProcessDefId() != null && !inboxSetting.getProcessDefId().isEmpty()) {
                    prefix += inboxSetting.getProcessDefId() + "_";
                }
                for (String actId : inboxSetting.getActvityDefIds()) {
                    if (!actIdConds.isEmpty()) {
                        actIdConds += " OR ";
                    }
                    actIdConds += "ass.ActivityId LIKE ?";
                    values.add("%"+prefix+actId);
                }
                conds += " AND ("+actIdConds+") ";
            } else {
                String value = "";
                if (inboxSetting.getAppId() != null && !inboxSetting.getAppId().isEmpty()) {
                    value = inboxSetting.getAppId() + "#%";
                }
                if (inboxSetting.getProcessDefId() != null && !inboxSetting.getProcessDefId().isEmpty()) {
                    if (value.isEmpty()) {
                        value = "%";
                    }
                    value += "#" + inboxSetting.getProcessDefId();
                }
                if (!value.isEmpty()) {
                    conds += " AND ass.ActivityProcessDefName LIKE ?";
                    values.add(value);
                }
            }

            if (inboxSetting.getUsername() != null && !inboxSetting.getUsername().isEmpty()) {
                Map<String, Collection<String>> replacementUsers = WorkflowUtil.getReplacementUsers(inboxSetting.getUsername());

                //handle task dalegation
                if (replacementUsers == null || replacementUsers.isEmpty()) {
                    conds += " AND ass.ResourceId = ?";
                    values.add(inboxSetting.getUsername());
                } else {
                    String temCond = "";
                    for (String u : replacementUsers.keySet()) {
                        String replaceCond = "";
                        Collection<String> processes = replacementUsers.get(u);
                        for (String p : processes) {
                            String[] tempPid = p.split(":");  //appId:processId
                            if (tempPid.length > 0 && !tempPid[0].isEmpty()) {
                                if (!replaceCond.isEmpty()) {
                                    replaceCond += " OR ";
                                }
                                String value = tempPid[0] + "#%";
                                if (tempPid.length > 1 && !tempPid[1].isEmpty()) {
                                    value += "#" + tempPid[1];
                                }
                                replaceCond += "ass.ActivityProcessDefName LIKE ?";
                                values.add(value);
                            }
                        }

                        if (!temCond.isEmpty()) {
                            temCond += "OR ";
                        }
                        if (!replaceCond.isEmpty()) {
                            replaceCond = "(" + replaceCond + ") AND "; //appId & processId matching
                        }
                        temCond += "(" + replaceCond + "ass.ResourceId = ?)"; //processIds belong to the replacement user
                        values.add(u);
                    }

                    conds += " AND (("+temCond+") OR ass.ResourceId = ?)"; //or the current user
                    values.add(inboxSetting.getUsername());
                }
            }
            
            conds += " AND ass.IsValid = 1";
            
            queryObj.setOperator("AND");
            queryObj.setQuery(conds);
            queryObj.setValues(values.toArray(new String[0]));
            return queryObj;
        }
        return null;
    }
}
