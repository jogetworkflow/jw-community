package org.joget.apps.datalist.model;

public class DataListInboxSetting {
    
    private String appId; 
    private String processDefId;
    private String[] actvityDefIds;
    private String username;

    public DataListInboxSetting(String appId, String processDefId, String[] actvityDefIds, String username) {
        this.appId = appId;
        this.processDefId = processDefId;
        this.actvityDefIds = actvityDefIds;
        this.username = username;
    }
    
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getProcessDefId() {
        return processDefId;
    }

    public void setProcessDefId(String processDefId) {
        this.processDefId = processDefId;
    }

    public String[] getActvityDefIds() {
        return actvityDefIds;
    }

    public void setActvityDefIds(String[] actvityDefIds) {
        this.actvityDefIds = actvityDefIds;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
