package org.joget.apps.app.service;

public interface AuditTrailManager {

    void addAuditTrail(String clazz, String method, String message);
    
    void addAuditTrail(String clazz, String method, String message, Class[] paramTypes, Object[] args, Object returnObject);
    
    void clean();
}
