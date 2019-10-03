package org.joget.workflow.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import org.joget.workflow.util.WorkflowUtil;

public class WorkflowProcess implements Serializable {

    private String recordId;
    private String id; // process definition ID
    private String instanceId;
    private String packageId;
    private String packageName;
    private String name;
    private String version;
    private String description;
    private String category;
    private Date createdTime;
    private Date startedTime;
    private String limit;
    private Date due;
    private String delay;
    private long delayInSeconds;
    private Date finishTime;
    private String timeConsumingFromDateCreated;
    private long timeConsumingFromDateCreatedInSeconds;
    private String timeConsumingFromDateStarted;
    private long timeConsumingFromDateStartedInSeconds;
    private String state;
    private String requesterId;
    boolean latest;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }
    
    public String getEncodedId() {
        if (id == null) {
            return null;
        } else {
            try {
                return URLEncoder.encode(id, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return id;
            }
        }
    }

    public String getIdWithoutVersion() {
        return WorkflowUtil.getProcessDefIdWithoutVersion(getId());
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return WorkflowUtil.translateProcessLabel(instanceId, id, null, name);
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public boolean isLatest() {
        return latest;
    }

    public void setLatest(boolean latest) {
        this.latest = latest;
    }

    public Date getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(Date startedTime) {
        this.startedTime = startedTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public Date getDue() {
        return due;
    }

    public void setDue(Date due) {
        this.due = due;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getTimeConsumingFromDateCreated() {
        return timeConsumingFromDateCreated;
    }

    public void setTimeConsumingFromDateCreated(String timeConsumingFromDateCreated) {
        this.timeConsumingFromDateCreated = timeConsumingFromDateCreated;
    }

    public String getTimeConsumingFromDateStarted() {
        return timeConsumingFromDateStarted;
    }

    public void setTimeConsumingFromDateStarted(String timeConsumingFromDateStarted) {
        this.timeConsumingFromDateStarted = timeConsumingFromDateStarted;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public long getDelayInSeconds() {
        return delayInSeconds;
    }

    public void setDelayInSeconds(long delayInSeconds) {
        this.delayInSeconds = delayInSeconds;
    }

    public long getTimeConsumingFromDateCreatedInSeconds() {
        return timeConsumingFromDateCreatedInSeconds;
    }

    public void setTimeConsumingFromDateCreatedInSeconds(long timeConsumingFromDateCreatedInSeconds) {
        this.timeConsumingFromDateCreatedInSeconds = timeConsumingFromDateCreatedInSeconds;
    }

    public long getTimeConsumingFromDateStartedInSeconds() {
        return timeConsumingFromDateStartedInSeconds;
    }

    public void setTimeConsumingFromDateStartedInSeconds(long timeConsumingFromDateStartedInSeconds) {
        this.timeConsumingFromDateStartedInSeconds = timeConsumingFromDateStartedInSeconds;
    }
}
