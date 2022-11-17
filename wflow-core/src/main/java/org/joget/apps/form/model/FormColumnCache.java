package org.joget.apps.form.model;

import java.util.Collection;
import net.sf.ehcache.Cache;
import org.joget.commons.util.DynamicDataSourceManager;

/**
 * Cache to store list of form column definitions for each table name i.e. tableName -> collection of columns
 * 
 */
public class FormColumnCache {
 
        
    private Cache cache;
    
    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    public Collection<String> get(String tableName) {
        Collection<String> columnList = null;
        String cacheKey = getCacheKey(tableName);
        net.sf.ehcache.Element element = cache.get(cacheKey);
        if (element != null) {
            columnList = (Collection<String>)element.getValue();
        }
        return columnList;
    }

    public void put(String tableName, Collection<String> columnList) {
        if (columnList != null) {
            String cacheKey = getCacheKey(tableName);
            net.sf.ehcache.Element element = new net.sf.ehcache.Element(cacheKey, columnList);
            cache.put(element);
        } else {
            remove(tableName);
        }
    }
    
    public void remove(String tableName) {
        String cacheKey = getCacheKey(tableName);
        cache.remove(cacheKey);
    }
    
    protected String getCacheKey(String tableName) {
        String cacheKey = DynamicDataSourceManager.getCurrentProfile() + "_" + tableName;
        return cacheKey;
    }
   
    public void clear() {
        cache.removeAll();
    }
}
