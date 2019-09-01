package org.joget.apps.userview.model;

import java.util.Map;

public interface PwaOfflineValidation {
    static enum WARNING_TYPE 
    { 
        SUPPORTED, NOT_SUPPORTED, READONLY; 
    } 
    
    /**
     * Validate the PWA offline support of the element and return the warning type and message.
     * 
     * @return 
     */
    public Map<WARNING_TYPE, String[]> validation();
}
