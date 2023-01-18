package org.kecak.apps.scheduler.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Kecak Exclusive
 */
public class SchedulerDetails implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1881523080514688898L;

    private String id;

    private String jobName;
    private String groupJobName;
    private String triggerName;
    private String groupTriggerName;
    private String jobClassName;
    private String cronExpression;
    private Long interval;
    private TriggerTypes triggerTypes;
    private Date nextFireTime;

    private Date dateCreated;
    private Date dateModified;
    private String createdBy;
    private String modifiedBy;
    private Boolean deleted;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getGroupJobName() {
        return groupJobName;
    }

    public void setGroupJobName(String groupJobName) {
        this.groupJobName = groupJobName;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getGroupTriggerName() {
        return groupTriggerName;
    }

    public void setGroupTriggerName(String groupTriggerName) {
        this.groupTriggerName = groupTriggerName;
    }

    public String getJobClassName() {
        return jobClassName;
    }

    public void setJobClassName(String jobClassName) {
        this.jobClassName = jobClassName;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    public TriggerTypes getTriggerTypes() {
        return triggerTypes;
    }

    public void setTriggerTypes(TriggerTypes triggerTypes) {
        this.triggerTypes = triggerTypes;
    }

    public Date getNextFireTime() {
        return nextFireTime;
    }

    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = nextFireTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
