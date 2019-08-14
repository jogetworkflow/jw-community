package org.joget.apps.userview.model;

import java.util.Set;

/**
 * Userview themes to implement this interface to support service workers and progressive web apps (PWA)
 */
public interface UserviewPwaTheme extends PwaOfflineResources {

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
     * @param userviewKey
     * @return 
     */
    String getServiceWorker(String appId, String userviewId, String userviewKey);
    
    /**
     * Return the cache urls for the theme
     * @param appId
     * @param userviewId
     * @param userviewKey
     * @return 
     */
    Set<String> getCacheUrls(String appId, String userviewId, String userviewKey);
}
