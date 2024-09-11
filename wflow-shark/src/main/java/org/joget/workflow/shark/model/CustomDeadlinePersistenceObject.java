package org.joget.workflow.shark.model;

import org.enhydra.shark.api.internal.instancepersistence.DeadlinePersistenceObject;

public class CustomDeadlinePersistenceObject {
    
    private long oid;
    private long timeLimit;
    private String exceptionName;
    private boolean synchronous;
    private boolean executed;
    private long cnt;
    
    private CustomActivityPersistenceObject activity;
    
    private DeadlinePersistenceObject deadlinePersistenceObject;

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
    }

    public boolean isSynchronous() {
        return synchronous;
    }

    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public CustomActivityPersistenceObject getActivity() {
        return activity;
    }

    public void setActivity(CustomActivityPersistenceObject activity) {
        this.activity = activity;
    }

    public long getCnt() {
        return cnt;
    }

    public void setCnt(long cnt) {
        this.cnt = cnt;
    }
    
    public DeadlinePersistenceObject getDeadlinePersistenceObject() {
        if (deadlinePersistenceObject == null) {
            deadlinePersistenceObject = new DeadlinePersistenceObject();
            deadlinePersistenceObject.setActivityId(activity.getId());
            deadlinePersistenceObject.setExceptionName(exceptionName);
            deadlinePersistenceObject.setExecuted(executed);
            deadlinePersistenceObject.setProcessId(activity.getProcessId());
            deadlinePersistenceObject.setSynchronous(synchronous);
            deadlinePersistenceObject.setTimeLimit(timeLimit);
            deadlinePersistenceObject.setUniqueId(Long.toString(cnt));
        }
        return deadlinePersistenceObject;
    }
}
