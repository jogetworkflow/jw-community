package org.joget.workflow.shark.migrate.model;

public class MigrateProcessDefinition {
    private long oid;
    private String name;

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
