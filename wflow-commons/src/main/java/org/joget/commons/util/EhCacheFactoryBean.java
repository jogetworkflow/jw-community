package org.joget.commons.util;

import net.sf.ehcache.Cache;
import javax.cache.CacheManager;
import org.springframework.beans.factory.FactoryBean;

public class EhCacheFactoryBean implements FactoryBean<Cache> {
    
    private Cache cache;
    private CacheManager cacheManager;
    private String cacheName = "default";
    
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    
    /**
     * Set a name for which to retrieve a cache instance.
     */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    @Override
    public Cache getObject() throws Exception {
        if (cache == null) {
            cache = new Cache(cacheManager.getCache(cacheName));
        }
        
        return cache;
    }

    @Override
    public Class<?> getObjectType() {
        return Cache.class;
    }

    @Override
    public boolean isSingleton() {
        return true;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
    }
}
