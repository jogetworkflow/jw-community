package org.joget.commons.util;

import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp.managed.BasicManagedDataSource;

public class DynamicDataSource extends BasicManagedDataSource {

    public static final String URL = "Url";
    public static final String USER = "User";
    public static final String PASSWORD = "Password";
    public static final String DRIVER = "Driver";
    private String datasourceName;

    @Override
    protected synchronized DataSource createDataSource() throws SQLException {

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

        if (!this.url.equals(tempUrl)) {
            //close old datasource
            super.close();
            super.closed = false;

            // set new settings
            this.driverClassName = tempDriver;
            this.url = tempUrl;
            this.username = tempUser;
            this.password = tempPassword;
            LogUtil.info(getClass().getName(), "datasourceName=" + getDatasourceName() + ", url=" + url + ", user=" + username);
        }
        return super.createDataSource();
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
