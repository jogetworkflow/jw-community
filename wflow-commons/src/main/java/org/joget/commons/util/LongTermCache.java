package org.joget.commons.util;

import java.util.Date;
import java.util.Iterator;
import javax.cache.Cache;
import javax.cache.CacheManager;

public class LongTermCache {
    private Cache cache;
    private SetupManager setupManager;
    
    public void setCacheManager(CacheManager cacheManager) {
        this.cache = cacheManager.getCache("org.joget.cache.RBM_CACHE");
    }
    
    public void setSetupManager(SetupManager setupManager) {
        this.setupManager = setupManager;
    }
    
    public Object get(String key) {
        Object value = null;
        CacheElement element = (CacheElement)cache.get(key);
        Long lastClear = getLastClearTime(key);
        if (element != null) {
            if (lastClear != null && element.creationTime < lastClear) {
                cache.remove(key);
                LogUtil.debug(LongTermCache.class.getName(), key + " need to refresh.");
            } else {
                value = element.value;
            }
        }
        return value;
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
            }
        }
        LogUtil.debug(LongTermCache.class.getName(), "All caches with `"+prefix+"` prefix are removed.");
    }
    
    public void put(String key, Object value) {
        CacheElement element = new CacheElement(value, System.currentTimeMillis());
        cache.put(key, element);
        LogUtil.debug(LongTermCache.class.getName(), key + " is refreshed.");
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
    
    class CacheElement {
        Object value;
        long creationTime;
        
        public CacheElement(Object value, long creationTime) {
            this.value = value;
            this.creationTime = creationTime;
        }
    }
}
