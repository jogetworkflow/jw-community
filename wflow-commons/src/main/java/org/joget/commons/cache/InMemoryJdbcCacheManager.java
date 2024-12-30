package org.joget.commons.cache;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.hibernate.cache.spi.RegionFactory;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;

/**
 * Manage caching for JDBC select queries.
 */
public class InMemoryJdbcCacheManager {

    private final static String SQL_PREFIX = "sql : '"; // prefix to locate SQL statement within PreparedStatement string
    private final static String SQL_SUFFIX = "', parameters"; // suffix to locate SQL statement within PreparedStatement string   
    private final static String SQL_TABLE_NAMES_PREFIX = "SQL_TBL_";
    private final static long DEFAULT_EXPIRY_MILLIS = 5*60*1000; // 5 minutes
    public static final String SYSTEM_PROPERTY_CACHE_JDBC = "wflow.cacheJdbc";
    public static final String SYSTEM_PROPERTY_CACHE_JDBC_EXPIRY = "wflow.cacheJdbcExpiry";
    
    private static Cache jdbcQueryCache;
    
    /**
     * Get the JDBC Query Cache as a singleton object.
     * @return 
     */
    public static Cache getJdbcQueryCache() {        
        if (jdbcQueryCache == null && isJdbcQueryCacheEnabled()) {
            String cacheName = "jdbc-query-results-region";
            InMemoryCacheManager cacheManager = InMemoryCacheManager.getInMemoryCacheManager();
            jdbcQueryCache = cacheManager.getCache(cacheName, getConfigJdbcCacheExpiry());
        }
        return jdbcQueryCache;
    }
    
    /**
     * Checks to see whether the JDBC query cache is enabled.
     * @return true if the cache is enabled.
     */
    public static boolean isJdbcQueryCacheEnabled() {
        boolean enabled = Boolean.parseBoolean(System.getProperty(SYSTEM_PROPERTY_CACHE_JDBC));
        return enabled;
    }    
    
    /**
     * Get the default timestamps cache used by Hibernate.
     * @return 
     */
    public static Cache getTimestampsCache() {
        String cacheName = RegionFactory.DEFAULT_UPDATE_TIMESTAMPS_REGION_UNQUALIFIED_NAME;
        InMemoryCacheManager cacheManager = InMemoryCacheManager.getInMemoryCacheManager();
        Cache timestampsCache = cacheManager.getCache(cacheName, 0);
        return timestampsCache;
    }
        
    /**
     * Retrieve a unique cache key for the query.
     * @param target
     * @param args
     * @return 
     */
    public static String generateCacheKey(Object target, Object[] args) {
        String cacheKey = null;

        Cache queryCache = getJdbcQueryCache();
        if (queryCache != null && target instanceof PreparedStatement) {
            String queryStr = target.toString();
            
            if (queryStr.contains("com.lutris.appserver.server.sql.pscache.CachedPreparedStatement")) {
                // ignore shark CachedPreparedStatement
                return null;
            }

            // extract query and parameters from string
            // string format example: org.apache.tomcat.jdbc.pool.StatementFacade$StatementProxy[Proxy=2147432954; Query=select appdefinit0_.appId as appid1_0_, appdefinit0_.appVersion as appversi2_0_, appdefinit0_.name as name3_0_, appdefinit0_.published as publishe4_0_, appdefinit0_.license as license5_0_, appdefinit0_.description as descript6_0_, appdefinit0_.meta as meta7_0_, appdefinit0_.dateCreated as datecrea8_0_, appdefinit0_.dateModified as datemodi9_0_ from app_app appdefinit0_ where appdefinit0_.published=1 and appdefinit0_.appId=? limit ?; Delegate=sql : 'select appdefinit0_.appId as appid1_0_, appdefinit0_.appVersion as appversi2_0_, appdefinit0_.name as name3_0_, appdefinit0_.published as publishe4_0_, appdefinit0_.license as license5_0_, appdefinit0_.description as descript6_0_, appdefinit0_.meta as meta7_0_, appdefinit0_.dateCreated as datecrea8_0_, appdefinit0_.dateModified as datemodi9_0_ from app_app appdefinit0_ where appdefinit0_.published=1 and appdefinit0_.appId=? limit ?', parameters : ['crm',1]]
            int idx = queryStr.indexOf(SQL_PREFIX);
            cacheKey = (idx >= 0) ? queryStr.substring(idx) : queryStr;
            int lastIndex = (cacheKey.contains(", parameters :")) ? cacheKey.lastIndexOf("]") : cacheKey.lastIndexOf("'");
            if (lastIndex > 0) {
                cacheKey = cacheKey.substring(0, lastIndex+1);
            }
        }
        return cacheKey;
    }

    /**
     * Retrieve cached ResultSet if relevant and up-to-date.
     * @param method
     * @param cacheKey
     * @param tableNames
     * @return 
     */
    public static ResultSet readQueryCache(String method, String cacheKey, Set<String> tableNames) {
        ResultSet cachedResultSet = null;
        if (method.equals("executeQuery")) {
            // read query from cache
            Cache queryCache = getJdbcQueryCache();
            Object result = queryCache.get(cacheKey);

            if (result != null && result instanceof CacheItem) {
                CacheItem cacheItem = (CacheItem)result;
                // check for cache validity
                boolean isCacheValid = isCacheUpToDate(tableNames, cacheItem);
                if (isCacheValid) {
                    cachedResultSet = getCachedResultSet(cacheKey, cacheItem);
                } else {
                    LogUtil.debug(InMemoryJdbcCacheManager.class.getName(), "Cached JDBC query out-of-date: " + cacheKey);
                }
            }
        }
        return cachedResultSet;
    }

    /**
     * Return the cache expiry in milliseconds.
     * Configurable via system property wflow.cacheJdbcExpiry, default CACHE_DEFAULT_EXPIRY.
     * @return 
     */
    public static long getConfigJdbcCacheExpiry() {
        long expiry = DEFAULT_EXPIRY_MILLIS;
        String expiryStr = System.getProperty(SYSTEM_PROPERTY_CACHE_JDBC_EXPIRY);
        if (expiryStr != null) {
            try {
                expiry = Long.parseLong(expiryStr);
            } catch(NumberFormatException e) {
                // ignore
            }
        }
        return expiry;
    }
    
    /**
     * Update cache with updated ResultSet and timestamps.
     * @param method
     * @param cacheKey
     * @param result
     * @param tableNames
     * @return
     * @throws SQLException 
     */
    public static ResultSet updateQueryCache(String method, String cacheKey, Object result, Set<String> tableNames) throws SQLException {
        ResultSet resultSet = null;
        
        // update cache
        if (result instanceof ResultSet) {
            // store in cache
            resultSet = storeCachedResultSet(cacheKey, result);
        } else {
            // check for PreparedStatement.execute select method as per JavaDoc i.e. true if results is a ResultSet.
            boolean isExecuteSelectQuery = ("execute".equals(method) && result != null && Boolean.parseBoolean(result.toString()));
            if (!isExecuteSelectQuery) {
                // not a select query, update timestamp
                updateCacheTimestamps(tableNames);
            }
        }
        return resultSet;
    }

    /**
     * Retrieve cached item based on the query key.
     * @param cacheKey
     * @param cacheItem
     * @return 
     */
    public static ResultSet getCachedResultSet(String cacheKey, CacheItem cacheItem) {
        LogUtil.debug(InMemoryJdbcCacheManager.class.getName(), "Returning cached JDBC query: " + cacheKey);
        ResultSet resultSet = cacheItem.resultSet;
        return resultSet;
    }

    /**
     * Extract the table names used within the SQL query.
     * @param cacheKey
     * @return
     * @throws JSQLParserException 
     */
    public static Set<String> extractTableNames(String cacheKey) throws JSQLParserException {
        Set<String> tableNames = new HashSet<>();

        // extract sql
        int lastIndex = cacheKey.indexOf(SQL_SUFFIX);
        if (lastIndex < 0) {
            lastIndex = cacheKey.lastIndexOf("'");
        }
        if (lastIndex > 0) {
            String sql = cacheKey.substring(cacheKey.indexOf(SQL_PREFIX) + SQL_PREFIX.length(), lastIndex);

            // extract table names from sql, using long term local cache for better performance
            String tableNamesCacheKey = SQL_TABLE_NAMES_PREFIX + sql;
            Cache longTermCache = ((CacheManager)SecurityUtil.getApplicationContext().getBean("cacheManager")).getCache("org.joget.cache.RBM_CACHE");
            tableNames = (Set<String>)longTermCache.get(tableNamesCacheKey);
            if (tableNames == null || tableNames.isEmpty()) {
                tableNames = TablesNamesFinder.findTables(sql);
                if (tableNames != null) {
                    longTermCache.put(tableNamesCacheKey, tableNames);
                }
            }                
        }
        return tableNames;
    }

    /**
     * Store the ResultSet as a disconnected copy in the cache.
     * @param cacheKey
     * @param result
     * @return
     * @throws SQLException
     */
    public static CachedRowSet storeCachedResultSet(String cacheKey, Object result) throws SQLException {
        // wrap in a disconnected result set
        CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet();
        rowSet.populate((ResultSet)result);
//        rowSet.setCacheKey(cacheKey);
        
        // store into cache
        Cache queryCache = getJdbcQueryCache();
        long timestamp = nextTimestamp();
        CacheItem cacheItem = new CacheItem(timestamp, rowSet);
        queryCache.put(cacheKey, cacheItem);
        LogUtil.debug(InMemoryJdbcCacheManager.class.getName(), "Stored JDBC query: " + cacheKey + " " + timestamp);
        return rowSet;
    }

    /**
     * Return the next timestamp in milliseconds.
     * @return 
     */
    public static long nextTimestamp() {
        return Instant.now().toEpochMilli();
    }

    /**
     * Check if the cache item is still valid, based on whether there have been any updates to the tables used.
     * @param tableNames
     * @param cacheItem
     * @return 
     */
    public static boolean isCacheUpToDate(Set<String> tableNames, CacheItem cacheItem) {
        boolean isUpToDate = true;
        
        if (tableNames != null) {
            long cachedTimestamp = cacheItem.timestamp;
            
            // check for expiry
            Cache timestampsCache = getTimestampsCache();
            Map<String, Long> resultMap = (Map<String, Long>)timestampsCache.get(tableNames);
            if (resultMap != null) {
                for (Long lastUpdate: resultMap.values()) {
                    if (lastUpdate != null && lastUpdate > cachedTimestamp) {
                        isUpToDate = false;
                        LogUtil.debug(InMemoryJdbcCacheManager.class.getName(), "isCacheUpToDate false: " + tableNames + " last updated " + lastUpdate + ", cached " + cachedTimestamp);            
                        break;
                    }
                }
            }
        }
        return isUpToDate;
    }
    
    /**
     * Update the last updated timestamps for tables used.
     * @param tableNames 
     */
    public static void updateCacheTimestamps(Set<String> tableNames) {
        if (tableNames != null) {
            long timestamp = nextTimestamp();
            Cache timestampsCache = getTimestampsCache();
            tableNames.forEach(tableName -> timestampsCache.put(tableName, timestamp));
            LogUtil.debug(InMemoryJdbcCacheManager.class.getName(), "updateCacheTimestamps: " + tableNames + " updated " + timestamp);            
        }
    }
        
    /**
     * Inner class to represent a cache item.
     */
    public static class CacheItem implements Serializable {
        private final long timestamp;
        private final ResultSet resultSet;

        CacheItem(long timestamp, ResultSet rs) {
            this.timestamp = timestamp;
            this.resultSet = rs;
        }
    }

}
