package org.joget.apps.app.service;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;

/**
 * Utility method for JDBC related operations
 */
public class JdbcUtil {
    
    private static CacheManager cacheManager;
    private static Cache cache;

    static {
        cacheManager = CacheManager.create();
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
        
        Element cachedDataSource = cache.get(cacheKey);

        BasicDataSource dataSource = null;
        if (cachedDataSource == null) {
            Properties props = new Properties();
            props.put("driverClassName", driver);
            props.put("url", url);
            props.put("username", username);
            props.put("password", password);
            props.put("removeAbandonedOnBorrow", "true");
            props.put("removeAbandonedOnMaintenance", "true");

            // Idle connections older than x mins * 60 seconds * 1000 millis will be evicted
            props.put("minEvictableIdleTimeMillis", (15*60*1000) + "");

            // Run evictor every x hour * 60 mins * 60 seconds * 1000 millis
            props.put("timeBetweenEvictionRunsMillis", (1*60*60*1000) + "");
            
            //support custom properties from plugin config
            if (customProps != null) {
                try {
                    props.load(new StringReader(customProps));
                } catch (Exception e) {
                    LogUtil.debug(JdbcUtil.class.getName(), "Fail to load custom JDBC props : " + customProps);
                }
            }

            dataSource = BasicDataSourceFactory.createDataSource(props);
            Element newDataSource = new Element(cacheKey, dataSource);
            cache.put(newDataSource);
            LogUtil.info(JdbcUtil.class.getName(), "Create datasource.");
              
        }
        else {
            dataSource = (BasicDataSource) cachedDataSource.getObjectValue();
        }
        
        if (dataSource == null || dataSource.isClosed()) {
            LogUtil.info(JdbcUtil.class.getName(), "Cached connection is closed");
            
            //clear cache and retrieve it again
            cache.remove(cacheKey);
            dataSource = (BasicDataSource) createCachedCustomDataSource(driver, url, username, password, customProps);
        }

        return dataSource;
    }
}
