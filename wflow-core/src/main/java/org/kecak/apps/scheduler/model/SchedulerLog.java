package org.kecak.apps.scheduler.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Kecak Exclusive
 */
public class SchedulerLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8015768031764084825L;
	
	public static final String JOB_NAME= "jobName";
	public static final String FINISH_TIME = "finishTime";
	public static final int MESSAGE_MAX_LENGTH = 1000;
	
	private String id;
	
	private String jobName;
	private String jobClassName;
	private Date startTime;
	private Date finishTime;
	private JobStatus jobStatus;
	private String message;
	
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

	public String getJobClassName() {
		return jobClassName;
	}

	public void setJobClassName(String jobClassName) {
		this.jobClassName = jobClassName;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	public JobStatus getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(JobStatus jobStatus) {
		this.jobStatus = jobStatus;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
}
