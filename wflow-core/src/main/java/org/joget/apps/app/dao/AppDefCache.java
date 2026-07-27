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
    
    public void setCacheManager(CacheManager cacheManager) {
        this.cache = cacheManager.getCache("org.joget.cache.FLU_CACHE");
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
