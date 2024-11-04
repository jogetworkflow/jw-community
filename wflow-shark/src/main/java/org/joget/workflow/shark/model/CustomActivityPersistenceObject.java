package org.joget.workflow.shark.model;

import org.enhydra.shark.api.internal.instancepersistence.ActivityPersistenceObject;

public class CustomActivityPersistenceObject {
    
    private long oid;
    private String id;
    private String name;
    private String description;
    private String activitySetDefinitionId;
    private String activityDefinitionId;
    private String managerName;
    private String processId;
    private String resourceUsername;
    private String blockActivityId;
    private String subflowProcessId;
    private boolean subflowAsynchronous;
    private short priority;
    private long activatedTime;
    private long acceptedTime;
    private long lastStateTime;
    private long limitTime;
    
    private SharkActivityState state;
    
    private ActivityPersistenceObject activityPersistenceObject;

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActivitySetDefinitionId() {
        return activitySetDefinitionId;
    }

    public void setActivitySetDefinitionId(String activitySetDefinitionId) {
        this.activitySetDefinitionId = activitySetDefinitionId;
    }

    public String getActivityDefinitionId() {
        return activityDefinitionId;
    }

    public void setActivityDefinitionId(String activityDefinitionId) {
        this.activityDefinitionId = activityDefinitionId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getResourceUsername() {
        return resourceUsername;
    }

    public void setResourceUsername(String resourceUsername) {
        this.resourceUsername = resourceUsername;
    }

    public String getBlockActivityId() {
        return blockActivityId;
    }

    public void setBlockActivityId(String blockActivityId) {
        this.blockActivityId = blockActivityId;
    }

    public String getSubflowProcessId() {
        return subflowProcessId;
    }

    public void setSubflowProcessId(String subflowProcessId) {
        this.subflowProcessId = subflowProcessId;
    }

    public boolean isSubflowAsynchronous() {
        return subflowAsynchronous;
    }

    public void setSubflowAsynchronous(boolean subflowAsynchronous) {
        this.subflowAsynchronous = subflowAsynchronous;
    }

    public short getPriority() {
        return priority;
    }

    public void setPriority(short priority) {
        this.priority = priority;
    }

    public long getActivatedTime() {
        return activatedTime;
    }

    public void setActivatedTime(long activatedTime) {
        this.activatedTime = activatedTime;
    }

    public long getAcceptedTime() {
        return acceptedTime;
    }

    public void setAcceptedTime(long acceptedTime) {
        this.acceptedTime = acceptedTime;
    }

    public long getLastStateTime() {
        return lastStateTime;
    }

    public void setLastStateTime(long lastStateTime) {
        this.lastStateTime = lastStateTime;
    }

    public long getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(long limitTime) {
        this.limitTime = limitTime;
    }

    public SharkActivityState getState() {
        return state;
    }

    public void setState(SharkActivityState state) {
        this.state = state;
    }
    
    public boolean equals(CustomActivityPersistenceObject obj) {
        return id.equals(obj.getId());
    }
    
    public ActivityPersistenceObject getActivityPersistenceObject() {
        if (activityPersistenceObject == null) {
            activityPersistenceObject = new ActivityPersistenceObject();
            activityPersistenceObject.setId(id);
            activityPersistenceObject.setAcceptedTime(acceptedTime);
            activityPersistenceObject.setActivatedTime(activatedTime);
            activityPersistenceObject.setActivityDefinitionId(activityDefinitionId);
            activityPersistenceObject.setActivitySetDefinitionId(activitySetDefinitionId);
            activityPersistenceObject.setBlockActivityId(blockActivityId);
            activityPersistenceObject.setDescription(description);
            activityPersistenceObject.setLastStateTime(lastStateTime);
            activityPersistenceObject.setLimitTime(limitTime);
            activityPersistenceObject.setName(name);
            activityPersistenceObject.setPriority(priority);
            activityPersistenceObject.setProcessId(processId);
            activityPersistenceObject.setProcessMgrName(managerName);
            activityPersistenceObject.setResourceUsername(resourceUsername);
            activityPersistenceObject.setState(state.getName());
            activityPersistenceObject.setSubflowAsynchronous(subflowAsynchronous);
            activityPersistenceObject.setSubflowProcessId(subflowProcessId);                                                                
        }
        return activityPersistenceObject;
    }
}
