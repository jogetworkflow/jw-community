package org.joget.apps.app.service;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.apache.tomcat.jdbc.pool.PooledConnection;
import org.joget.commons.util.DynamicDataSource;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;

/**
 * Utility method for JDBC related operations
 */
public class JdbcUtil {
    
    private static CacheManager cacheManager;
    private static Cache cache;

    static {
        cacheManager = (CacheManager) AppUtil.getApplicationContext().getBean("cacheManager");
        cache = cacheManager.getCache("org.joget.cache.DATASOURCE_CACHE");
    }

    /**
     * Used for create databasource based on plugin properties. Either using default data source or custom data source
     * 
     * @param pluginProps
     * @return 
     */
        public static DataSource createDataSource(Map<String, Object> pluginProps) throws SQLException {
        DataSource ds = null;
        
        String datasource = (pluginProps.containsKey("jdbcDatasource")?((String) pluginProps.get("jdbcDatasource")):((String) pluginProps.get("datasource")));
        
        if (datasource != null && "default".equals(datasource)) {
            // use current datasource
             ds = (DataSource)AppUtil.getApplicationContext().getBean("setupDataSource");
        } else {
            //fallback checking for some old JDBC plugins
            String driver = (pluginProps.containsKey("jdbcDriver")?((String) pluginProps.get("jdbcDriver")):((String) pluginProps.get("driverClassName")));
            String url = (pluginProps.containsKey("jdbcUrl")?((String) pluginProps.get("jdbcUrl")):((String) pluginProps.get("url")));
            String user = (pluginProps.containsKey("jdbcUser")?((String) pluginProps.get("jdbcUser")):((String) pluginProps.get("username")));
            String password = (pluginProps.containsKey("jdbcPassword")?((String) pluginProps.get("jdbcPassword")):((String) pluginProps.get("password")));
            String customProps = (pluginProps.containsKey("jdbcProps")?((String) pluginProps.get("jdbcProps")):"");
            
            // use custom datasource
            ds = createCachedCustomDataSource(driver, url, user, password, customProps);
        }
        return ds;
    }
    
    /**
     * Create a custom data source or retrieve it from cache
     * 
     * @param driver
     * @param url
     * @param username
     * @param password
     * @param customProps
     * @return 
     * @throws java.sql.SQLException 
     */
    public static DataSource createCachedCustomDataSource(String driver, String url, String username, String password, String customProps) throws SQLException {
        String cacheKey = "DATASOURCE-CACHE-" + DynamicDataSourceManager.getCurrentProfile() + "::" + driver + "::" + url + "::" + username + "::" + password + "::" + (customProps != null?customProps:"");
        
        BasicDataSource dataSource = (BasicDataSource) cache.get(cacheKey);
        if (dataSource == null) {
            url = DynamicDataSource.convertSqlServerUrlWithEncryptParam(driver, url);
            
            Properties props = new Properties();
            props.put("driverClassName", driver);
            props.put("url", url);
            props.put("username", username);
            props.put("password", password);
            props.put("removeAbandonedOnBorrow", "true");
            props.put("removeAbandonedOnMaintenance", "true");

            // Fail fast on pool exhaustion instead of blocking forever. DBCP2 defaults
            // maxWaitMillis to -1 (block indefinitely); under load that turns a pool
            // shortage into a permanent hang that only a restart clears. A finite wait
            // makes a borrow throw a pool-timeout SQLException so the container recovers.
            props.put("maxWaitMillis", (10*1000) + "");

            // Validate connections so stale/dead handles (DB restart, firewall idle
            // timeout) are detected and replaced instead of handed to callers. With no
            // validationQuery set, DBCP2 uses Connection.isValid(), which is driver-agnostic.
            // Cap the validation at 3s (isValid(0) has no timeout) so a dead network can't
            // make the borrow-time check block and undo the fail-fast intent above.
            props.put("testOnBorrow", "true");
            props.put("testWhileIdle", "true");
            props.put("validationQueryTimeout", "3");

            // Idle connections older than x mins * 60 seconds * 1000 millis will be evicted
            props.put("minEvictableIdleTimeMillis", (15*60*1000) + "");

            // Run evictor every x hour * 60 mins * 60 seconds * 1000 millis
            props.put("timeBetweenEvictionRunsMillis", (1*60*60*1000) + "");

            //support custom properties from plugin config (may override the defaults above)
            if (customProps != null) {
                try {
                    props.load(new StringReader(customProps));
                } catch (Exception e) {
                    LogUtil.debug(JdbcUtil.class.getName(), "Fail to load custom JDBC props : " + customProps);
                }
            }

            dataSource = BasicDataSourceFactory.createDataSource(props);
            cache.put(cacheKey, dataSource);
            LogUtil.info(JdbcUtil.class.getName(), "Create datasource.");
              
        }
        
        if (dataSource == null || dataSource.isClosed()) {
            LogUtil.info(JdbcUtil.class.getName(), "Cached connection is closed");

            //clear cache and retrieve it again
            cache.remove(cacheKey);
            dataSource = (BasicDataSource) createCachedCustomDataSource(driver, url, username, password, customProps);
        }

        return dataSource;
    }

    /**
     * Cleans up a connection whose query has just failed, BEFORE it is handed back to the
     * connection pool via {@link Connection#close()}.
     *
     * <p>This exists because a failed statement can leave a connection in a "poisoned" state -
     * most notably on MSSQL, where an aborted statement can leave a server-side transaction open
     * (<code>@@TRANCOUNT &gt; 0</code>) even though JDBC still reports auto-commit. When such a
     * connection is returned to the shared default datasource pool, the next borrower (e.g. Joget's
     * own <code>AppVersion</code> check on any page) inherits the bad state and fails immediately,
     * effectively locking down the whole instance until a restart.</p>
     *
     * <p>Simply calling <code>con.rollback()</code> is not enough: <code>rollback()</code> throws
     * when the connection is in auto-commit mode (the default for the shared pool), so the rollback
     * silently does nothing. This method therefore does two things:</p>
     * <ol>
     *   <li>best-effort rollback of any explicit (non auto-commit) transaction; and</li>
     *   <li>guarantees the physical connection is <b>not</b> reused by marking the Tomcat JDBC
     *       {@link PooledConnection} as discarded, so the subsequent <code>close()</code> destroys
     *       it and the pool creates a fresh one.</li>
     * </ol>
     *
     * @param con the connection that just failed; may be null
     */
    public static void handleFailedConnection(Connection con) {
        if (con == null) {
            return;
        }

        // 1) best-effort rollback to clear any lingering explicit transaction.
        //    rollback() is illegal (throws) in auto-commit mode, so guard on it.
        try {
            if (!con.isClosed() && !con.getAutoCommit()) {
                con.rollback();
            }
        } catch (Exception e) {
            LogUtil.warn(JdbcUtil.class.getName(), "Unable to roll back failed connection: " + e.getMessage());
        }

        // 2) guarantee the (possibly poisoned) physical connection is never reused.
        //    For a Tomcat JDBC pooled connection (used by the shared default datasource),
        //    flagging it as discarded makes close() destroy it instead of returning it to the pool.
        try {
            PooledConnection pooled = con.unwrap(PooledConnection.class);
            if (pooled != null) {
                pooled.setDiscarded(true);
            }
        } catch (Throwable t) {
            // not a Tomcat JDBC pooled connection (e.g. a custom DBCP2 datasource) - nothing to discard
            LogUtil.debug(JdbcUtil.class.getName(), "Connection is not a Tomcat JDBC pooled connection, skipping discard.");
        }
    }
}
