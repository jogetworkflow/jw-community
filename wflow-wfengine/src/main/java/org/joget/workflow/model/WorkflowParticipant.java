package org.joget.workflow.model;

import java.io.Serializable;

public class WorkflowParticipant implements Serializable {

    private String id;
    private String name;
    private boolean packageLevel;

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

    public boolean isPackageLevel() {
        return packageLevel;
    }

    public void setPackageLevel(boolean packageLevel) {
        this.packageLevel = packageLevel;
    }
}
