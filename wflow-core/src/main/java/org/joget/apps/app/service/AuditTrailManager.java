package org.joget.apps.app.service;

public interface AuditTrailManager {

    void addAuditTrail(String clazz, String method, String message);
}
