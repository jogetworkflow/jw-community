package org.joget.commons.util;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.beanutils.BeanUtils;
import org.joget.commons.cache.InMemoryCacheManager;

public class DynamicHikariDataSource extends HikariDataSource {

    public static final String URL = "Url";
    public static final String USER = "User";
    public static final String PASSWORD = "Password";
    public static final String DRIVER = "Driver";
    private String datasourceName;
    
    public String getUrl() {
        return super.getJdbcUrl();
    }
    
    public void setUrl(String url) {
        super.setJdbcUrl(url);
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        Properties properties = DynamicDataSourceManager.getProperties();
        String tempDriver = properties.getProperty(getDatasourceName() + DRIVER);
        String tempUrl = properties.getProperty(getDatasourceName() + URL);
        String tempUser = properties.getProperty(getDatasourceName() + USER);
        String tempPassword = properties.getProperty(getDatasourceName() + PASSWORD);

        if (tempDriver == null || tempDriver.length() == 0 ||
                tempUrl == null || tempUrl.length() == 0 ||
                tempUser == null || tempUser.length() == 0) {
            throw new SQLException("No database profile configured");
        }

        if (tempPassword == null) {
            tempPassword = "";
        }

        if (tempUrl.contains(":mysql")) {
            // replace jdbc:mysql with jdbc:mariadb for MariaDB Connector/J 3
            tempUrl = tempUrl.replace(":mysql", ":mariadb");
        }
        
        if (!getUrl().equals(tempUrl)) {
            if (getUrl() != null && !getUrl().isEmpty()) {
                // clear cache
                InMemoryCacheManager inMemoryCacheManager = InMemoryCacheManager.getInMemoryCacheManager();
                inMemoryCacheManager.clearAll();
            }
            
            //close old datasource
            super.close();

            // set new settings
            setDriverClassName(tempDriver);
            setUrl(tempUrl);
            setUsername(tempUser);
            setPassword(tempPassword);
            setProperties(properties);

            try {
                // reset shutdown flag
                Field shutdownField = HikariDataSource.class.getDeclaredField("isShutdown");
                shutdownField.setAccessible(true);
                Method setMethod = AtomicBoolean.class.getDeclaredMethod("set", boolean.class);
                Object shutdownFieldObj = shutdownField.get(this);
                setMethod.invoke(shutdownFieldObj, false);
                
                // recreate pool
                HikariPool pool = new HikariPool(this);
                Field poolField = HikariDataSource.class.getDeclaredField("pool");
                poolField.setAccessible(true);
                poolField.set(this, pool);
                Field fastPathPoolField = HikariDataSource.class.getDeclaredField("fastPathPool");
                fastPathPoolField.setAccessible(true);
                fastPathPoolField.set(this, pool);
            } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new SQLException(e);
            }            

            LogUtil.info(getClass().getName(), "profileName=" + HostManager.getCurrentProfile() + ", url=" + getUrl() + ", user=" + getUsername());
            
            if (tempDriver.contains("mysql")) {
                //run collation check after connection established for mysql
                DatabaseUtil.checkAndFixMySqlDbCollation(super.getConnection());
            } 
        }
        return super.getConnection();
    }
    
    protected void setProperties(Properties properties) {
        for (Map.Entry<Object, Object> e : properties.entrySet()) {
            String key = (String) e.getKey();
            String value = (String) e.getValue();
            
            if (key.endsWith(DRIVER) || key.endsWith(URL) || key.endsWith(USER) || key.endsWith(PASSWORD) || key.endsWith("profileName") || key.endsWith("encryption")) {
                continue;
            }
            
            try {
                BeanUtils.setProperty(this, key, value);
            } catch (Exception ex) {/*ignore*/}
        }
    }

    public String getConfigDataSourceUrl() {
        String configUrl = DynamicDataSourceManager.getProperty(getDatasourceName() + URL);
        return configUrl;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }
}
