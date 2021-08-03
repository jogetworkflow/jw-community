package org.joget.apps.app.dao;

import java.util.ArrayList;
import java.util.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.joget.apps.app.model.AppDefinition;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;

public class AppDefCache {
    private Cache cache;
    
    public void setCacheObject(Cache cache) {
        this.cache = cache;
    }
    
    public Element get(String key, AppDefinition appDef) {
        Element element = null;
        element = cache.get(key);
        Long lastModified = null;
        if (appDef.getDateModified() != null) {
            lastModified = appDef.getDateModified().getTime();
        }
        if (element != null && lastModified != null && element.getCreationTime() < lastModified) {
            cache.remove(key);
            LogUtil.debug(AppDefCache.class.getName(), key + " need to refresh.");
            element = null;
        }
        return element;
    }
    
    public void remove(String key, AppDefinition appDef) {
        Element element = cache.get(key);
        if (element != null) {
             cache.remove(key);
             LogUtil.debug(AppDefCache.class.getName(), key + " is removed.");
        }
    }
    
    public void removeAll(AppDefinition appDef) {
        String cacheKey = DynamicDataSourceManager.getCurrentProfile()+"_"+appDef.getAppId()+"_"+appDef.getVersion().toString()+"_";
        
        List<String> keys = new ArrayList<String>();
        keys.addAll(cache.getKeys());
        
        for (String k : keys) {
            if (k.startsWith(cacheKey)) {
                cache.remove(k);
            }
        }
        LogUtil.debug(AppDefCache.class.getName(), "All caches with `"+cacheKey+"` prefix are removed.");
    }
    
    public void put(Element element, AppDefinition appDef) {
        cache.put(element);
        LogUtil.debug(AppDefCache.class.getName(), element.getObjectKey() + " is refreshed.");
    }
}
