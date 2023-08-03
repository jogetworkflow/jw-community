package org.joget.apps.form.model;

import java.util.Collection;
import static org.joget.apps.form.dao.FormDataDaoImpl.FORM_PREFIX_TABLE_NAME;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LongTermCache;

/**
 * Cache to store list of form column definitions for each table name i.e. tableName -> collection of columns
 * 
 */
public class FormColumnCache {
 
        
    private LongTermCache cache;
    
    public void setCache(LongTermCache cache) {
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
    
    public Collection<String> getIndexes(String tableName) {
        Collection<String> columnList = null;
        String cacheKey = getIndexCacheKey(tableName);
        net.sf.ehcache.Element element = cache.get(cacheKey);
        if (element != null) {
            columnList = (Collection<String>)element.getValue();
        }
        return columnList;
    }
    
    public void putIndexes(String tableName, Collection<String> indexesList) {
        if (indexesList != null) {
            String cacheKey = getIndexCacheKey(tableName);
            net.sf.ehcache.Element element = new net.sf.ehcache.Element(cacheKey, indexesList);
            cache.put(element);
        } else {
            remove(tableName);
        }
    }
    
    public void remove(String tableName) {
        String cacheKey = getCacheKey(tableName);
        cache.remove(cacheKey);
        String indexCacheKey = getIndexCacheKey(tableName);
        cache.remove(indexCacheKey);
    }
    
    protected String getCacheKey(String tableName) {
        // strip table prefix
        if (tableName != null && tableName.startsWith(FORM_PREFIX_TABLE_NAME)) {
            tableName = tableName.substring(FORM_PREFIX_TABLE_NAME.length());
        }
        String cacheKey = DynamicDataSourceManager.getCurrentProfile() + "_" + "FORM_COLUMNS_" + tableName;
        return cacheKey;
    }
    
    protected String getIndexCacheKey(String tableName) {
        // strip table prefix
        if (tableName != null && tableName.startsWith(FORM_PREFIX_TABLE_NAME)) {
            tableName = tableName.substring(FORM_PREFIX_TABLE_NAME.length());
        }
        String cacheKey = DynamicDataSourceManager.getCurrentProfile() + "_" + "FORM_INDEXES_" + tableName;
        return cacheKey;
    }
    
    public void clear() {
        cache.removeAll(DynamicDataSourceManager.getCurrentProfile() + "_" + "FORM_COLUMNS_");
        cache.removeAll(DynamicDataSourceManager.getCurrentProfile() + "_" + "FORM_INDEXES_");
    }
}
