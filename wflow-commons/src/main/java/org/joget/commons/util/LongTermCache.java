package org.joget.commons.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

public class LongTermCache {
    private Cache cache;
    private SetupManager setupManager;
    
    public void setCacheObject(Cache cache) {
        this.cache = cache;
    }
    
    public void setSetupManager(SetupManager setupManager) {
        this.setupManager = setupManager;
    }
    
    public Element get(String key) {
        Element element = null;
        element = cache.get(key);
        Long lastClear = getLastClearTime(key);
        if (element != null && lastClear != null && element.getCreationTime() < lastClear) {
            cache.remove(key);
            LogUtil.debug(LongTermCache.class.getName(), key + " need to refresh.");
            element = null;
        }
        return element;
    }
    
    public void remove(String key) {
        Element element = cache.get(key);
        if (element != null) {
             cache.remove(key);
             setupManager.updateSetting("CACHE_LAST_CLEAR_" + key, Long.toString((new Date()).getTime()));
             LogUtil.debug(LongTermCache.class.getName(), key + " is removed.");
        }
    }
    
    public void removeAll(String prefix) {
        List<String> keys = new ArrayList<String>();
        keys.addAll(cache.getKeys());
        
        for (String k : keys) {
            if (k.startsWith(prefix)) {
                cache.remove(k);
            }
        }
        LogUtil.debug(LongTermCache.class.getName(), "All caches with `"+prefix+"` prefix are removed.");
    }
    
    public void put(Element element) {
        cache.put(element);
        LogUtil.debug(LongTermCache.class.getName(), element.getObjectKey() + " is refreshed.");
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
