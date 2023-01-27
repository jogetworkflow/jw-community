package org.joget.workflow.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import org.joget.workflow.util.WorkflowUtil;

public class WorkflowAssignment implements Serializable {

    private String processId;
    private String processName;
    private String processVersion;
    private String processDefId;
    private String processRequesterId;
    private boolean subflow;
    private String activityId;
    private String activityName;
    private String activityDefId;
    private String priority;
    private String description;
    private Date dateCreated;
    private boolean accepted;
    private String assigneeId;
    private String assigneeName;
    private String participant;
    private Date dueDate;
    private List<WorkflowVariable> processVariableList;
    private List<String> assigneeList;
    private double serviceLevelValue;

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

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return WorkflowUtil.translateProcessLabel(processId, processDefId, null, processName);
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return WorkflowUtil.translateProcessLabel(processId, processDefId, activityDefId, activityName);
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
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

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String version) {
        this.processVersion = version;
    }

    public String getProcessDefId() {
        return processDefId;
    }

    public void setProcessDefId(String processDefId) {
        this.processDefId = processDefId;
    }

    public String getProcessRequesterId() {
        return processRequesterId;
    }

    public void setProcessRequesterId(String processRequesterId) {
        this.processRequesterId = processRequesterId;
    }

    public boolean isSubflow() {
        return subflow;
    }

    public void setSubflow(boolean subflow) {
        this.subflow = subflow;
    }

    public String getActivityDefId() {
        return activityDefId;
    }

    public void setActivityDefId(String activityDefId) {
        this.activityDefId = activityDefId;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public List<WorkflowVariable> getProcessVariableList() {
        return processVariableList;
    }

    public void setProcessVariableList(List<WorkflowVariable> processVariableList) {
        this.processVariableList = processVariableList;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public List<String> getAssigneeList() {
        return assigneeList;
    }

    public void setAssigneeList(List<String> assigneeList) {
        this.assigneeList = assigneeList;
    }

    public double getServiceLevelValue() {
        return serviceLevelValue;
    }

    public void setServiceLevelValue(double serviceLevelValue) {
        this.serviceLevelValue = serviceLevelValue;
    }
}
