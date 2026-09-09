package org.joget.apps.app.dao;

import java.io.Serializable;
import java.util.Iterator;
import javax.cache.Cache;
import javax.cache.CacheManager;
import net.sf.ehcache.Element;
import org.joget.apps.app.model.AppDefinition;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;

public class AppDefCache {
    private Cache cache;
    
    // #2317: wrap in net.sf.ehcache.Cache (Joget's own JSR-107 shim, matching
    // EhCacheFactoryBean.getObject()) so MultiTenantPluginAspect's guards -- which match by
    // declaring type, not by name -- can see this cache at all; the raw JSR-107 provider object
    // cacheManager.getCache(..) alone returns is a different class the aspect's pointcuts never
    // match. Registered with UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE, matching this
    // class's own real key shape (removeAll(..) above: profile+"_"+appId+...) -- NOT the more
    // widely-shared LEGACY_UNDERSCORE_PREFIX_SCOPE (PlatformCacheRegistrar's own intended scope
    // for this cache's dormant "fluCache" bean-based registration path -- that path never fires
    // on 9.1 today because nothing wires this class's cache to that bean, so this class
    // self-registers its own field instead of depending on it): independent review (Sol) found
    // LEGACY_UNDERSCORE_PREFIX_SCOPE unconditionally denies the exemption whenever the CALLING
    // tenant's own profile name contains "_", which is common in practice (a Joget Cloud
    // profile is frequently exactly the tenant's underlying database name, e.g. "customer_eu"
    // -- confirmed via SetupServlet, which writes the admin-typed "Database Name" field straight
    // through with no character restriction beyond SecurityUtil.validateStringInput's
    // permissive regex, which explicitly allows "_") -- this would have newly broken every such
    // tenant's own AppDefCache writes (used directly by real third-party OSGi plugins, e.g.
    // addon_src's ai-designer GenerationPage.java, not just this platform's own code), where
    // before this session's fix (an unwrapped, unguarded raw cache) they always succeeded. See
    // ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE's own javadoc for
    // why it does not have this problem. Also registers the alias so
    // AppUtil.getCache("org.joget.cache.FLU_CACHE") returns this SAME governed instance instead
    // of falling through to a separate, ungoverned raw fetch.
    // Independent review (Sol) finding: constructs into a LOCAL variable and registers it BEFORE
    // assigning to the field -- see MultiTenantPluginAspect.appDefAndLongTermCacheSetterGuardCheck
    // and LongTermCache.setCacheManager's identical comment for why.
    public void setCacheManager(CacheManager cacheManager) {
        net.sf.ehcache.Cache wrapped = new net.sf.ehcache.Cache(cacheManager.getCache("org.joget.cache.FLU_CACHE"));
        // Read-scoped (not plain register): a restricted caller reaching this wrapper directly must
        // not read another tenant's app-definition entry. Every real key is "<profile>_<appId>_...",
        // matched by the LONGEST scope, so a tenant's own read passes through and AppDefCache rebuilds
        // from the DB on a redirected miss. Matches the fluCache alias registration in PlatformCacheRegistrar.
        org.joget.commons.util.ManagedCacheRegistry.registerReadScoped(wrapped,
                org.joget.commons.util.ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE);
        org.joget.commons.util.PlatformCacheRegistrar.registerAlias(wrapped);
        this.cache = wrapped;
    }
    
    protected CacheElement getCacheElement(String key, AppDefinition appDef) {
        CacheElement element = (CacheElement)cache.get(key);
        if (element != null) {
            Long lastModified = null;
            if (appDef.getDateModified() != null) {
                lastModified = appDef.getDateModified().getTime();
            }
            if (lastModified != null && element.creationTime < lastModified) {
                cache.remove(key);
                LogUtil.debug(AppDefCache.class.getName(), key + " need to refresh.");
            } else {
                return element;
            }
        }
        return null;
    }
    
    public Object getObject(String key, AppDefinition appDef) {
        Object value = null;
        CacheElement element = getCacheElement(key, appDef);
        if (element != null) {
            value = element.value;
        }
        return value;
    }
    
    public void remove(String key, AppDefinition appDef) {
        CacheElement element = (CacheElement)cache.get(key);
        if (element != null) {
             cache.remove(key);
             LogUtil.debug(AppDefCache.class.getName(), key + " is removed.");
        }
    }
    
    public void removeAll(AppDefinition appDef) {
        String cacheKey = DynamicDataSourceManager.getCurrentProfile()+"_"+appDef.getAppId()+"_"+appDef.getVersion().toString()+"_";
        for (Iterator i=cache.iterator(); i.hasNext();) {
            Cache.Entry entry = (Cache.Entry)i.next();
            if (entry.getKey().toString().startsWith(cacheKey)) {
                i.remove();
            }
        }
        LogUtil.debug(AppDefCache.class.getName(), "All caches with `"+cacheKey+"` prefix are removed.");
    }
    
    /**
     * Puts a value stamped with an explicit app modified time instead of wall-clock insertion time.
     * <p>
     * {@link #getCacheElement} treats an element as stale once {@code creationTime < dateModified}
     * of the {@link AppDefinition} passed on read. Stamping with {@link System#currentTimeMillis()}
     * (see {@link #put(String, Object, AppDefinition)}) leaves a race: a reader that loaded
     * pre-change data can repopulate the cache <em>after</em> an eviction, stamping the stale value
     * with a wall-clock time newer than the new {@code dateModified}, so it is never detected as
     * stale and outlives the change. Callers that know the modification time of the data actually
     * read should pass it here: a stale value then carries the old {@code dateModified} and is
     * evicted on the next read that sees the newer one.
     * </p>
     * @param key cache key
     * @param value value to cache
     * @param appDef app definition the value belongs to
     */
    public void put(String key, Object value, AppDefinition appDef) {
        CacheElement element = new CacheElement(key, value, appDef.getDateModified().getTime());
        cache.put(key, element);
        LogUtil.debug(AppDefCache.class.getName(), key + " is refreshed.");
    }

    class CacheElement {
        String key;
        Object value;
        long creationTime;
        
        public CacheElement(String key, Object value, long creationTime) {
            this.key = key;
            this.value = value;
            this.creationTime = creationTime;
        }
    }

    /**
     * Backward compatible method to get a cache element
     * @param key
     * @param appDef
     * @return 
     */
    public Element get(String key, AppDefinition appDef) {
        CacheElement element = getCacheElement(key, appDef);
        if (element != null) {
            if (element.value instanceof Element) {
                return (Element) element.value;
            } else {
                Element newValue = new Element(key, (Serializable) element.value);
                return newValue;
            }
        }
        return null;
    }
    
    /**
     * Backward compatible method to put a cache element
     * @param value
     * @param appDef 
     */
    public void put(Element value, AppDefinition appDef) {
        put(value.getKey(), value.getObjectValue(), appDef);
    }
}
