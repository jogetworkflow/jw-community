package org.joget.apps.userview.model;

import java.util.Map;
import java.util.Set;

/**
 * Userview themes to implement this interface to support service workers and progressive web apps (PWA)
 */
public interface UserviewPwaTheme extends PwaOfflineResources {
    
    public static final String PWA_OFFLINE_MENU_ID = "pwaoffline";
    public static final String PAGE_UNAVAILABLE_MENU_ID = "offline";

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
    
    /**
     * HTML template to handle PWA offline page.
     * 
     * @param data
     * @return 
     */
    String handlePwaOfflinePage(Map<String, Object> data);
    
    /**
     * HTML template to handle PWA unavailable page.
     * 
     * @param data
     * @return 
     */
    String handlePwaUnavailablePage(Map<String, Object> data);
}
