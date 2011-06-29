package org.joget.apps.app.lib;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.ApplicationPlugin;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginProperty;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.util.WorkflowUtil;

public class DatabaseUpdateTool implements Plugin, ApplicationPlugin, PropertyEditable {

    public String getName() {
        return "DatabaseUpdateTool";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String getDescription() {
        return "Executes SQL INSERT and UPDATE statement on MySQL, Oracle or SQL Server database";
    }

    public PluginProperty[] getPluginProperties() {
        return null;
    }

    public Object execute(Map properties) {
        Object result = null;
        try {
            String driverClassName = (String) properties.get("driverClassName");
            String url = (String) properties.get("url");
            String username = (String) properties.get("username");
            String password = (String) properties.get("password");
            String query = (String) properties.get("query");

            WorkflowAssignment wfAssignment = (WorkflowAssignment) properties.get("workflowAssignment");

            Map<String, String> replace = new HashMap<String, String>();
            if (driverClassName.equalsIgnoreCase("com.mysql.jdbc.Driver")) {
                replace.put("\\\\", "\\\\");
                replace.put("'", "\\'");
            } else {
                replace.put("'", "''");
            }

            query = WorkflowUtil.processVariable(query, null, wfAssignment, "regex", replace);

            Properties props = new Properties();

            props.put("driverClassName", driverClassName);
            props.put("url", url);
            props.put("username", username);
            props.put("password", password);
            DataSource ds = createDataSource(props);
            result = executeQuery(ds, query);

            return result;
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error executing plugin", e);
            return null;
        }
    }

    protected DataSource createDataSource(Properties props) throws Exception {
        DataSource ds = BasicDataSourceFactory.createDataSource(props);
        return ds;
    }

    protected boolean executeQuery(DataSource ds, String sql) throws SQLException {
        Connection con = null;
        Statement stmt = null;
        try {
            con = ds.getConnection();
            stmt = con.createStatement();
            boolean result = stmt.execute(sql);
            return result;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }

    public String getLabel() {
        return "Database Update Tool";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/app/databaseUpdateTool.json", null, true, "message/app/databaseUpdateTool");
    }

    public String getDefaultPropertyValues() {
        return "";
    }
}
