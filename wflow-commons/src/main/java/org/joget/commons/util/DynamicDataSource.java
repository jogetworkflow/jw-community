package org.joget.commons.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.tomcat.jdbc.pool.XADataSource;

public class DynamicDataSource extends XADataSource {

    public static final String URL = "Url";
    public static final String USER = "User";
    public static final String PASSWORD = "Password";
    public static final String DRIVER = "Driver";
    private String datasourceName;
    
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

        if (!getUrl().equals(tempUrl)) {
            //close old datasource
            super.close();

            // set new settings
            setDriverClassName(tempDriver);
            setUrl(tempUrl);
            setUsername(tempUser);
            setPassword(tempPassword);
            setProperties(properties);
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
