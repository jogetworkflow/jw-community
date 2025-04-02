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
    
    public Object getObject(String key, AppDefinition appDef) {
        Object value = null;
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
                value = element.value;
            }
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
    
    public void put(String key, Object value, AppDefinition appDef) {
        CacheElement element = new CacheElement(key, value, System.currentTimeMillis());
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
        Object value = getObject(key, appDef);
        if (value != null) {
            if (value instanceof Element) {
                return (Element) value;
            } else {
                Element newValue = new Element(key, (Serializable) value);
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
