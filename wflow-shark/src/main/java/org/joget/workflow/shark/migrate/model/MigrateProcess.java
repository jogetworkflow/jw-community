package org.joget.workflow.shark.migrate.model;

public class MigrateProcess {
    private Long oid;
    private String id;
    private Long processDefinition;
    private String name;
    private Long state;

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(Long processDefinition) {
        this.processDefinition = processDefinition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getState() {
        return state;
    }

    public void setState(Long state) {
        this.state = state;
    }
}
