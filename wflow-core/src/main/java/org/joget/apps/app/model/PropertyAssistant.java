package org.joget.apps.app.model;

public interface PropertyAssistant {
    
    public enum Type {
        HASH_VARIABLE,
        URL,
        SQL,
        REGEX,
        FORM_FIELD
    }
    
    /**
     * Retrieve the definition contains value option and syntax
     * @return 
     */
    public String getPropertyAssistantDefinition();
    
    /**
     * Retrieve the type of Property Assistant
     * @return 
     */
    public Type getPropertyAssistantType();
}
