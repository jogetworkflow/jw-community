package org.joget.workflow.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import org.joget.workflow.util.WorkflowUtil;

public class WorkflowActivity implements Serializable {

    public static final String TYPE_NORMAL = "normal";
    public static final String TYPE_TOOL = "tool";
    public static final String TYPE_ROUTE = "route";
    public static final String TYPE_SUBFLOW = "subflow";
    private String id;
    private String name;
    private String priority;
    private String description;
    private Date createdTime;
    private Date startedTime;
    private String limit;
    private long limitInSeconds;
    private Date due;
    private String delay;
    private long delayInSeconds;
    private Date finishTime;
    private String timeConsumingFromDateCreated;
    private long timeConsumingFromDateCreatedInSeconds;
    private String timeConsumingFromDateStarted;
    private long timeConsumingFromDateStartedInSeconds;
    private String performer;
    private String[] assignmentUsers;
    private String nameOfAcceptedUser;
    private String status;
    private String state;
    private String activityDefId;
    private String processId;
    private String processDefId;
    private String processName;
    private String processVersion;
    private String processStatus;
    private String type;
    private long latestActivityCount;

    public String getEncodedProcessDefId() {
        if (processDefId == null) {
            return null;
        } else {
            try {
                return URLEncoder.encode(processDefId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return processDefId;
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return WorkflowUtil.translateProcessLabel(processId, processDefId, activityDefId, name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessDefId() {
        return processDefId;
    }

    public void setProcessDefId(String processDefId) {
        this.processDefId = processDefId;
    }

    public String getActivityDefId() {
        return activityDefId;
    }

    public void setActivityDefId(String activityDefId) {
        this.activityDefId = activityDefId;
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

    public String getPerformer() {
        return performer;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNameOfAcceptedUser() {
        return nameOfAcceptedUser;
    }

    public void setNameOfAcceptedUser(String nameOfAcceptedUser) {
        this.nameOfAcceptedUser = nameOfAcceptedUser;
    }

    public String[] getAssignmentUsers() {
        return assignmentUsers;
    }

    public void setAssignmentUsers(String[] assignmentUsers) {
        this.assignmentUsers = assignmentUsers;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
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

    public long getLimitInSeconds() {
        return limitInSeconds;
    }

    public void setLimitInSeconds(long limitInSeconds) {
        this.limitInSeconds = limitInSeconds;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProcessName() {
        return WorkflowUtil.translateProcessLabel(processId, processDefId, null, processName);
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(String processStatus) {
        this.processStatus = processStatus;
    }

    public long getLatestActivityCount() {
        return latestActivityCount;
    }

    public void setLatestActivityCount(long latestActivityCount) {
        this.latestActivityCount = latestActivityCount;
    }
    
}
