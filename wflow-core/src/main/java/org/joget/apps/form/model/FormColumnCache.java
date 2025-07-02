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
        String cacheKey = getCacheKey(tableName);
        Collection<String> columnList = (Collection<String>)cache.getObject(cacheKey);
        return columnList;
    }

    public void put(String tableName, Collection<String> columnList) {
        if (columnList != null) {
            String cacheKey = getCacheKey(tableName);
            cache.putObject(cacheKey, columnList);
        } else {
            remove(tableName);
        }
    }
    
    public Collection<String> getIndexes(String tableName) {
        String cacheKey = getIndexCacheKey(tableName);
        Collection<String> indexesList = (Collection<String>)cache.getObject(cacheKey);
        return indexesList;
    }
    
    public void putIndexes(String tableName, Collection<String> indexesList) {
        if (indexesList != null) {
            String cacheKey = getIndexCacheKey(tableName);
            cache.putObject(cacheKey, indexesList);
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
