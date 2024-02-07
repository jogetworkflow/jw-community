package org.joget.commons.util;

import java.util.Collection;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.query.Query;

/**
 * To initialize and manage the Apache Ignite cache.
 * 
 */
public class IgniteCacheManager {
    
    public static final String SYSTEM_PROPERTY_IGNITE_CACHE = "wflow.ignite";
    private static boolean started = false;
    static IgniteConfiguration igniteCfg;

    public IgniteCacheManager() {
        // default constructor        
    }
    
    public IgniteCacheManager(IgniteConfiguration igniteCfg) {
        if (!HostManager.isVirtualHostEnabled() && IgniteCacheManager.isIgniteCacheEnabled()) {
            // set ignite work directory
            String workDirectory = SetupManager.getBaseDirectory() + "/ignite/work";
            igniteCfg.setWorkDirectory(workDirectory);            
            Ignition.setClientMode(false);
            IgniteCacheManager.igniteCfg = igniteCfg;            
            Ignite ignite = Ignition.getOrStart(igniteCfg);
            ignite.cluster().state(ClusterState.ACTIVE);
            started = true;
        }
    }

    /**
     * Checks to see whether the Ignite cache is enabled.
     * @return true if the cache is enabled.
     */
    public static boolean isIgniteCacheEnabled() {
        boolean enabled = Boolean.parseBoolean(System.getProperty(SYSTEM_PROPERTY_IGNITE_CACHE));
        return enabled;
    }
    
    /**
     * Checks to see whether the Ignite cache is started.
     * @return true if the cache is started.
     */
    public static boolean isStarted() {
        return started;
    }
    
    /**
     * Obtain reference to the Ignite cache instance.
     * @return 
     */
    public static Ignite getIgnite() {
        Ignite ignite = null;
        if (started) {
            ignite = Ignition.getOrStart(igniteCfg);
        }
        return ignite;
    }
    
    /**
     * Clear all caches in the Ignite grid.
     */
    public void clearAll() {
        if (started) {
            Ignite ignite = Ignition.getOrStart(igniteCfg);
            Collection<String> cacheNames = ignite.cacheNames();
            for (String cacheName: cacheNames) {
                IgniteCache cache = ignite.getOrCreateCache(cacheName);
                cache.clear();
            }
        }
    }

    /**
     * Clear a specific cache in the Ignite grid.
     * @param cacheName 
     */
    public void clear(String cacheName) {
        if (started) {
            Ignite ignite = Ignition.getOrStart(igniteCfg);
            IgniteCache cache = ignite.getOrCreateCache(cacheName);
            cache.clear();
        }
    }
    
    /**
     * Sets a Hibernate query to be cacheable along with an optional custom region name.
     * @param query
     * @param regionName If no region name is specified, the default "default-query-results-region" query cache name is used.
     * @return 
     */
    public static Query setCacheable(Query query, String regionName) {
        String cacheRegionName = (regionName != null && !regionName.isEmpty()) ? "query.cache." + regionName : RegionFactory.DEFAULT_QUERY_RESULTS_REGION_UNQUALIFIED_NAME;
        query.setCacheable(true);
        query.setCacheRegion(cacheRegionName);
        return query;
    }
}
