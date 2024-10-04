package org.joget.commons.ignite;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteClientDisconnectedException;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.internal.processors.cache.CacheStoppedException;
import org.apache.ignite.lang.IgniteFuture;
import org.apache.ignite.lang.IgniteFutureTimeoutException;
import org.apache.ignite.transactions.TransactionException;
import org.hibernate.cache.spi.RegionFactory;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;

/**
 * Manage caching for JDBC select queries.
 */
public class IgniteJdbcCacheManager {

    private final static String SQL_PREFIX = "sql : '"; // prefix to locate SQL statement within PreparedStatement string
    private final static String SQL_SUFFIX = "', parameters"; // suffix to locate SQL statement within PreparedStatement string   
    private final static String SQL_TABLE_NAMES_PREFIX = "SQL_TBL_";
    private final static long DEFAULT_EXPIRY_MILLIS = 5*60*1000; // 5 minutes
    public static final String SYSTEM_PROPERTY_IGNITE_CACHE_JDBC = "wflow.igniteJdbc";
    public static final String SYSTEM_PROPERTY_IGNITE_CACHE_ASYNC_TIMEOUT = "wflow.igniteAsyncTimeout";
    public static final String SYSTEM_PROPERTY_IGNITE_CACHE_EXPIRY = "wflow.igniteExpiry";
    
    private static IgniteCache jdbcQueryCache;
    
    /**
     * Get the JDBC Query Cache as a singleton object.
     * @return 
     */
    public static IgniteCache getJdbcQueryCache() {        
        if (jdbcQueryCache == null && isIgniteJdbcQueryCacheEnabled()) {
            Ignite ignite = IgniteCacheManager.getIgnite();
            if (ignite != null) {
                String regionName = "jdbc-query-results-region";
                CacheConfiguration cacheConfiguration = (CacheConfiguration)SecurityUtil.getApplicationContext().getBean("igniteAtomicCache");
                cacheConfiguration.setName(regionName);
                cacheConfiguration.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MILLISECONDS, getConfigJdbcCacheExpiry())));
                IgniteCacheManager.setCacheMode(cacheConfiguration);
                jdbcQueryCache = ignite.getOrCreateCache(cacheConfiguration);
            }        
        }
        return jdbcQueryCache;
    }
    
    /**
     * Checks to see whether the Ignite JDBC query cache is enabled.
     * @return true if the cache is enabled.
     */
    public static boolean isIgniteJdbcQueryCacheEnabled() {
        boolean enabled = Boolean.parseBoolean(System.getProperty(SYSTEM_PROPERTY_IGNITE_CACHE_JDBC));
        return enabled;
    }    
    
    /**
     * Get the default timestamps cache used by Hibernate.
     * @return 
     */
    public static IgniteCache getTimestampsCache() {
        IgniteCache timestampsCache = null;
        Ignite ignite = IgniteCacheManager.getIgnite();
        if (ignite != null) {
            String regionName = RegionFactory.DEFAULT_UPDATE_TIMESTAMPS_REGION_UNQUALIFIED_NAME;
            timestampsCache = ignite.cache(regionName);
        }        
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
            try {
                // read query from cache
                IgniteCache queryCache = getJdbcQueryCache();
                Object result = getAsyncCacheResult(queryCache, cacheKey);

                if (result != null && result instanceof CacheItem) {
                    CacheItem cacheItem = (CacheItem)result;
                    // check for cache validity
                    boolean isCacheValid = isCacheUpToDate(tableNames, cacheItem);
                    if (isCacheValid) {
                        cachedResultSet = getCachedResultSet(cacheKey, cacheItem);
                    } else {
                        LogUtil.debug(IgniteJdbcCacheManager.class.getName(), "Cached JDBC query out-of-date: " + cacheKey);
                    }
                }
            } catch (IgniteFutureTimeoutException e) {
                LogUtil.debug(IgniteJdbcCacheManager.class.getName(), "readQueryCache timed out: " + cacheKey);   
                cachedResultSet = null;
            } catch(CacheException | IllegalStateException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IgniteClientDisconnectedException || cause instanceof CacheStoppedException) {
                    LogUtil.warn(IgniteJdbcCacheManager.class.getName(), "Error connecting to Ignite cache: " + e.toString());
                    // reconnect
                    jdbcQueryCache = null;
                } else {
                    throw e;
                }
            }
        }
        return cachedResultSet;
    }

    /**
     * Return the cache expiry in milliseconds.
     * Configurable via system property wflow.igniteExpiry, default CACHE_DEFAULT_EXPIRY.
     * @return 
     */
    public static long getConfigJdbcCacheExpiry() {
        long expiry = DEFAULT_EXPIRY_MILLIS;
        String expiryStr = System.getProperty(SYSTEM_PROPERTY_IGNITE_CACHE_EXPIRY);
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
     * Gets result from the cache asynchronously, timeout CACHE_ASYNC_TIMEOUT ms.
     * Defaults to synchronous get if async is not available.
     * @param cache
     * @param cacheKey
     * @return null if not in cache, or timed out.
     */
    protected static Object getAsyncCacheResult(IgniteCache cache, String cacheKey) throws IgniteFutureTimeoutException {
        if (cache == null) {
            return null;
        }
        Object result;
        long asyncTimeout = IgniteCacheManager.getConfigAsyncTimeout();
        if (asyncTimeout > 0) {
            IgniteFuture future = cache.getAsync(cacheKey);
            result = future.get(asyncTimeout);
        } else {
            result = cache.get(cacheKey);
        }
        return result;
    }

    /**
     * Gets result from the cache asynchronously, timeout CACHE_ASYNC_TIMEOUT ms.
     * Defaults to synchronous get if async is not available.
     * @param cache
     * @param cacheKeys
     * @return null if not in cache, or timed out.
     */
    protected static Object getAsyncCacheResult(IgniteCache cache, Set<String> cacheKeys) throws IgniteFutureTimeoutException {
        if (cache == null) {
            return null;
        }
        Object result;
        long asyncTimeout = IgniteCacheManager.getConfigAsyncTimeout();
        if (asyncTimeout > 0) {
            IgniteFuture future = cache.getAllAsync(cacheKeys);
            result = future.get(asyncTimeout);
        } else {
            result = cache.getAll(cacheKeys);
        }
        return result;
    }

    /**
     * Puts result into the cache asynchronously, timeout CACHE_ASYNC_TIMEOUT ms.
     * Defaults to synchronous put if async is not available.
     * If timed out, value is not put into the cache.
     * @param cache
     * @param cacheKey
     * @param value 
     */
    protected static void putAsyncCacheResult(IgniteCache cache, String cacheKey, Object value) throws IgniteFutureTimeoutException {
        if (cache == null) {
            return;
        }
        long asyncTimeout = IgniteCacheManager.getConfigAsyncTimeout();
        if (asyncTimeout > 0) {
            IgniteFuture future = cache.putAsync(cacheKey, value);
            future.get(asyncTimeout);
        } else {
            cache.put(cacheKey, value);
        }
    }
    
    /**
     * Update cache with updated ResultSet and timestamps.
     * @param method
     * @param cacheKey
     * @param result
     * @param tableNames
     * @return
     * @throws TransactionException
     * @throws SQLException 
     */
    public static ResultSet updateQueryCache(String method, String cacheKey, Object result, Set<String> tableNames) throws TransactionException, SQLException {
        ResultSet resultSet = null;
        
        try {
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
            
        } catch(IgniteClientDisconnectedException e) {            
            LogUtil.warn(IgniteJdbcCacheManager.class.getName(), "Error connecting to Ignite cache: " + e.toString());
        } catch(CacheException | IllegalStateException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IgniteClientDisconnectedException || cause instanceof CacheStoppedException) {
                LogUtil.warn(IgniteJdbcCacheManager.class.getName(), "Error connecting to Ignite cache: " + e.toString());
                jdbcQueryCache = null;
            } else {
                throw e;
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
        LogUtil.debug(IgniteJdbcCacheManager.class.getName(), "Returning cached JDBC query: " + cacheKey);
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
            javax.cache.Cache longTermCache = (javax.cache.Cache)SecurityUtil.getApplicationContext().getBean("longTermCacheObject");
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
     * @throws TransactionException 
     */
    public static CachedRowSet storeCachedResultSet(String cacheKey, Object result) throws SQLException, TransactionException {
        // wrap in a disconnected result set
        CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet();
        rowSet.populate((ResultSet)result);
//        rowSet.setCacheKey(cacheKey);
        
        // store into cache
        IgniteCache queryCache = getJdbcQueryCache();
        long timestamp = nextTimestamp();
        CacheItem cacheItem = new CacheItem(timestamp, rowSet);
        try {
            putAsyncCacheResult(queryCache, cacheKey, cacheItem);
            LogUtil.debug(IgniteJdbcCacheManager.class.getName(), "Stored JDBC query: " + cacheKey + " " + timestamp);
        } catch (IgniteFutureTimeoutException e) {
            LogUtil.debug(IgniteJdbcCacheManager.class.getName(), "storeCachedResultSet timed out: " + cacheKey + " " + timestamp);   
        }
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
            IgniteCache timestampsCache = getTimestampsCache();
            try {
                Map<String, Long> resultMap = (Map<String, Long>)getAsyncCacheResult(timestampsCache, tableNames);
                if (resultMap != null) {
                    for (Long lastUpdate: resultMap.values()) {
                        if (lastUpdate != null && lastUpdate > cachedTimestamp) {
                            isUpToDate = false;
                            LogUtil.debug(IgniteJdbcCacheManager.class.getName(), "isCacheUpToDate false: " + tableNames + " last updated " + lastUpdate + ", cached " + cachedTimestamp);            
                            break;
                        }
                    }
                }
            } catch (IgniteFutureTimeoutException e) {
                LogUtil.debug(IgniteJdbcCacheManager.class.getName(), "isCacheUpToDate timed out: " + tableNames + " " + cachedTimestamp);   
                return false;
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
            IgniteCache timestampsCache = getTimestampsCache();
            try {
                tableNames.forEach(tableName -> putAsyncCacheResult(timestampsCache, tableName, timestamp));
            } catch (IgniteFutureTimeoutException e) {
                LogUtil.debug(IgniteJdbcCacheManager.class.getName(), "updateCacheTimestamps timed out: " + tableNames + " " + timestamp);   
            }
            LogUtil.debug(IgniteJdbcCacheManager.class.getName(), "updateCacheTimestamps: " + tableNames + " updated " + timestamp);            
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
