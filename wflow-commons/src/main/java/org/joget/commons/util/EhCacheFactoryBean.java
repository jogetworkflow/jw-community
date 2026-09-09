package org.joget.commons.util;

import net.sf.ehcache.Cache;
import javax.cache.CacheManager;
import org.springframework.beans.factory.FactoryBean;

public class EhCacheFactoryBean implements FactoryBean<Cache> {
    
    private Cache cache;
    private CacheManager cacheManager;
    private String cacheName = "default";
    private boolean scopeByProfile = false;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Set a name for which to retrieve a cache instance.
     */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    /**
     * #2261 (A4, external security review): when true, wrap the cache in
     * {@link ProfileScopedNonceCache} so every key is transparently scoped to the current profile
     * and bulk operations cannot cross tenants. Used by the nonceCache bean, whose OIDC consumer
     * keys by opaque OAuth state tokens that no {@code ManagedCacheRegistry.KeyScope} could
     * recognise.
     */
    public void setScopeByProfile(boolean scopeByProfile) {
        this.scopeByProfile = scopeByProfile;
    }

    @Override
    public Cache getObject() throws Exception {
        if (cache == null) {
            javax.cache.Cache backing = cacheManager.getCache(cacheName);
            cache = scopeByProfile ? new ProfileScopedNonceCache(backing) : new Cache(backing);
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
