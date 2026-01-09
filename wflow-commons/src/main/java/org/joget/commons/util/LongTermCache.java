package org.joget.commons.util;

import java.util.Date;
import java.util.Iterator;
import javax.cache.Cache;
import javax.cache.CacheManager;
import net.sf.ehcache.Element;

public class LongTermCache {
    private Cache cache;
    private SetupManager setupManager;
    
    public void setCacheManager(CacheManager cacheManager) {
        this.cache = cacheManager.getCache("org.joget.cache.RBM_CACHE");
    }
    
    public void setSetupManager(SetupManager setupManager) {
        this.setupManager = setupManager;
    }
    
    public Object getObject(String key) {
        Object value = null;
        Element element = get(key);
        if (element != null) {
            value = element.getObjectValue();
        }
        return value;
    }
    
    /**
     * Backward compatible method for getting cache 
     * 
     * @param key 
     */
    public Element get(String key) {
        Element element = (Element)cache.get(key);
        Long lastClear = getLastClearTime(key);
        if (element != null) {
            if (lastClear != null && element.getCreationTime() <= lastClear) {
                cache.remove(key);
                LogUtil.debug(LongTermCache.class.getName(), key + " need to refresh.");
                return null;
            }
        }
        return element;
    }
    
    public void remove(String key) {
        cache.remove(key);
        setupManager.updateSetting("CACHE_LAST_CLEAR_" + key, Long.toString((new Date()).getTime()));
        LogUtil.debug(LongTermCache.class.getName(), key + " is removed.");
    }
    
    public void removeAll(String prefix) {
        for (Iterator i=cache.iterator(); i.hasNext();) {
            Cache.Entry entry = (Cache.Entry)i.next();
            if (entry.getKey().toString().startsWith(prefix)) {
                i.remove();
                setupManager.updateSetting("CACHE_LAST_CLEAR_" + entry.getKey().toString(), Long.toString((new Date()).getTime()));
            }
        }
        LogUtil.debug(LongTermCache.class.getName(), "All caches with `"+prefix+"` prefix are removed.");
    }

    public void putObject(String key, Object value) {
        putObject(key, value, null);
    }
    
    /**
     * This is used for long processing time cache data. 
     * Keep its requesting time instead of cache creation time to prevent stale cache
     * 
     * @param key
     * @param value
     * @param requestTime 
     */
    public void putObject(String key, Object value, Date requestTime) {
        if (requestTime == null) {
            requestTime = new Date();
        }
        
        Element element = new Element(key, value, requestTime.getTime());
        put(element);
    }
    
    /**
     * Backward compatible method for adding cache 
     * 
     * @param element 
     */
    public void put(Element element) {
        cache.put(element.getKey(), element);
        LogUtil.debug(LongTermCache.class.getName(), element.getKey() + " is refreshed.");
    }
    
    public Long getLastClearTime(String key) {
        Long lastClear = null;
        String value = setupManager.getSettingValue("CACHE_LAST_CLEAR_" + key);
        if (value != null && !value.isEmpty()) {
            try {
                lastClear = Long.parseLong(value);
            } catch (Exception e) {
                LogUtil.error(LongTermCache.class.getName(), e, value);
            }
        }
        
        return lastClear;
    }
}
