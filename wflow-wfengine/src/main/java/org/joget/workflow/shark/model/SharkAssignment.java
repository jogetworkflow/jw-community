package org.joget.workflow.shark.model;

public class SharkAssignment{
    private long id;
    private SharkActivity activity;
    private SharkProcess process;
    private String assigneeName;
    private int isValid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public SharkActivity getActivity() {
        return activity;
    }

    public void setActivity(SharkActivity activity) {
        this.activity = activity;
    }

    public SharkProcess getProcess() {
        return process;
    }

    public void setProcess(SharkProcess process) {
        this.process = process;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public int getIsValid() {
        return isValid;
    }

    public void setIsValid(int isValid) {
        this.isValid = isValid;
    }
}
