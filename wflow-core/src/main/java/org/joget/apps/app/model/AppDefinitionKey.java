package org.joget.apps.app.model;

/**
 * Key for an AppDefinition
 */
public class AppDefinitionKey extends AbstractVersionedObject {

    @Override
    public String toString() {
        return "{type=AppDefinition," + "id=" + getId() + ", version=" + getVersion() + "'}'";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return this.toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
    
    
}
