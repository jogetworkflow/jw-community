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

    /**
     * Runs {@code task} with the CALLING THREAD's audit-trail plugin dispatch (see {@link
     * #addAuditTrail(String, String, String, Class[], Object[], Object)}'s internal {@code
     * executePlugin(..)}) unconditionally suppressed for its duration -- no {@code
     * AuditTrailPlugin}, of any app or tenant, is resolved or executed on THIS thread while
     * {@code task} runs, regardless of how {@code executePlugin(..)} would otherwise have
     * resolved an app definition (ambient, from call args, or from the request's referer/
     * saved-URL fallback). Restores whatever suppression state was already in effect before the
     * call once {@code task} completes (normally or by exception). Does NOT extend to a thread
     * {@code task} itself spawns (e.g. an asynchronous cache-refresh worker) -- such a thread is
     * outside this suppression window even if it inherits the caller's ambient tenant profile.
     *
     * <p>Restricted to one trusted direct caller (the implementation decides which); any other
     * caller must be denied, not silently no-op-ed -- this suppresses a security- and
     * compliance-relevant dispatch, not a performance optimization.
     *
     * @param task the work to run with plugin dispatch suppressed
     * @throws SecurityException if the caller is not the implementation's one trusted caller
     */
    void withPluginDispatchSuppressed(Runnable task);
}
