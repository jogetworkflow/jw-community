package org.joget.apps.userview.model;

/**
 * Userview themes to implement this interface to support service workers and progressive web apps (PWA)
 */
public interface UserviewPwaTheme {

    /**
     * Return the PWA manifest contents for the userview
     * @param appId
     * @param userviewId
     * @return 
     */
    String getManifest(String appId, String userviewId);

    /**
     * Return the service worker JS contents for the userview
     * @param appId
     * @param userviewId
     * @return 
     */
    String getServiceWorker(String appId, String userviewId);
    
}
