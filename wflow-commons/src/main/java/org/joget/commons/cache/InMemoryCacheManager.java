package org.joget.commons.cache;

import java.util.Properties;
import javax.cache.Cache;
import org.hibernate.query.Query;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.springframework.beans.BeansException;

/**
 * Generic interface to interact with an integrated in-memory data grid
 */
public interface InMemoryCacheManager {

    public static final String SYSTEM_PROPERTY_CACHE_MANAGER = "wflow.cacheManager";
    
    /**
     * Convenience method to retrieve the currently configured cache manager.
     * @return
     */
    public static InMemoryCacheManager getInMemoryCacheManager() {
        String cacheManagerName = System.getProperty(SYSTEM_PROPERTY_CACHE_MANAGER);
        if (cacheManagerName == null || cacheManagerName.trim().isEmpty()) {
            cacheManagerName = "inMemoryCacheManager";
        }
        try {
            InMemoryCacheManager cacheManager = (InMemoryCacheManager)SecurityUtil.getApplicationContext().getBean(cacheManagerName);
            return cacheManager;
        } catch(BeansException e) {
            LogUtil.error(InMemoryCacheManager.class.getName(), e, "Cache manager " + cacheManagerName + " not available: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convenience method to see if cache manager is enabled and started.
     * @return
     */
    public static boolean isAvailable() {
        boolean started = false;
        InMemoryCacheManager cacheManager = getInMemoryCacheManager();
        if (cacheManager != null) {
            started = cacheManager.isStarted();
        }
        return started;
    }
    
    /**
     * Clear a specific cache in the in-memory grid.
     * @param cacheName
     */
    void clear(String cacheName);

    /**
     * Clear all caches in the in-memory grid.
     */
    void clearAll();
    
    /**
     * Return a specific cache from the in-memory grid.
     * @param cacheName
     * @param expiry cache expiry in milliseconds.
     * @return 
     */
    Cache getCache(String cacheName, long expiry);

    /**
     * Return the timeout for asynchronous cache gets and puts in milliseconds.
     * @return
     */
    long getConfigAsyncTimeout();

    /**
     * Return HTML for APM console
     * @return 
     */
    String getConsoleHtml();

    /**
     * Return JSON for APM console
     * @return 
     */
    String getConsoleJson();
    
    /**
     * Return Hibernate session configuration properties for this cache implementation.
     * @return 
     */
    Properties getHibernateProperties();
    
    /**
     * Checks to see whether the cache is enabled.
     * @return true if the cache is enabled.
     */
    boolean isEnabled();

    /**
     * Checks to see whether the cache is started.
     * @return true if the cache is started.
     */
    boolean isStarted();

    /**
     * Sets a Hibernate query to be cacheable along with an optional custom region name.
     * @param query
     * @param regionName If no region name is specified, the default "default-query-results-region" query cache name is used.
     * @return
     */
    Query setCacheable(Query query, String regionName);
    
}
