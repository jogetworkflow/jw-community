package org.joget.apps.app.service;

/**
 * Interface of service methods used to add audit trail and trigger audit trail event.
 * 
 */
public interface AuditTrailManager {

    /**
     * Simplify method to add audit trail and trigger audit trail event without 
     * passing method parameters and returned object
     * 
     * @param clazz
     * @param method
     * @param message 
     */
    void addAuditTrail(String clazz, String method, String message);
    
    /**
     * Method to add audit trail and trigger audit trail event
     * @param clazz
     * @param method
     * @param message
     * @param paramTypes
     * @param args
     * @param returnObject 
     */
    void addAuditTrail(String clazz, String method, String message, Class[] paramTypes, Object[] args, Object returnObject);
    
    /**
     * Used by system to clear audit trail plugin cache
     */
    void clean();
}
