package org.joget.apps.app.lib;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.JdbcUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.util.WorkflowUtil;

public class DatabaseUpdateTool extends DefaultApplicationPlugin {

    public String getName() {
        return "Database Update Tool";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "Executes SQL INSERT and UPDATE statement on MySQL, Oracle or SQL Server database";
    }

    public Object execute(Map properties) {
        Object result = null;
        DataSource ds = null;
        
        try {
            String query = (String) properties.get("query");
            String driver = "";
            
            ds = JdbcUtil.createDataSource(getProperties());
            
            WorkflowAssignment wfAssignment = (WorkflowAssignment) properties.get("workflowAssignment");

            Map<String, String> replace = new HashMap<String, String>();
            if (driver.equalsIgnoreCase("com.mysql.jdbc.Driver")) {
                replace.put("\\\\", "\\\\");
                replace.put("'", "\\'");
            } else {
                replace.put("'", "''");
            }

            query = WorkflowUtil.processVariable(query, null, wfAssignment, "regex", replace);

            result = executeQuery(ds, query);

            return result;
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Error executing plugin");
            return null;
        } 
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
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch(Exception e) {
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch(Exception e) {
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
        return AppUtil.readPluginResource(getClass().getName(), "/properties/app/databaseUpdateTool.json", null, true, null);
    }
    
    @Override
    public String getDeveloperMode() {
        return "advanced";
    }
    
    @Override
    public String getPluginIcon() {
        return "<i class=\"fas fa-database\"></i>";
    }
}
