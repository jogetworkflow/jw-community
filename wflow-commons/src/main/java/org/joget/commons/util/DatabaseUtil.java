package org.joget.commons.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseUtil {
    public static final String COLLATION_CHECKING = "wflow.collationChecking";
    
    public static void checkAndFixMySqlDbCollation(Connection connection) {
        
        //provide option to disable the checking from JAVA_OPTS
        String collationChecking = System.getProperty(COLLATION_CHECKING, "true");
        if (!"true".equalsIgnoreCase(collationChecking)) {
            LogUtil.info(DatabaseUtil.class.getName(), "Collation auto fix is disabled");
            return;
        }
        
        String databaseCollation = null;
        String tableCharSet = null;
        String tableCollation = null;
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("SELECT @@collation_database AS DatabaseCollation")) {
                if (resultSet.next()) {
                    databaseCollation = resultSet.getString("DatabaseCollation");
                }
            }
            // Query to get character set and collation of the table
            String sql = "SELECT TABLE_NAME, TABLE_COLLATION FROM information_schema.TABLES " +
                         "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'SHKAssignmentsTable'";

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                if (resultSet.next()) {
                   tableCollation = resultSet.getString("TABLE_COLLATION");
                
                   // Extract character set from the collation (optional)
                   tableCharSet = tableCollation.split("_")[0];
                }
            }
            
            //make sure the db collation are same with shark tables
            if (databaseCollation != null && tableCollation != null && !databaseCollation.equals(tableCollation)) {
                LogUtil.info(DatabaseUtil.class.getName(), "Fixing database charset to "+tableCharSet+" and collation to " + tableCollation);
                
                //temporary disable foreign key checks
                statement.executeQuery("SET foreign_key_checks = 0");
                try {
                    //get db name
                    String dbname = "";
                    try (ResultSet resultSet = statement.executeQuery("SELECT DATABASE() AS CurrentDatabase")) {
                        if (resultSet.next()) {
                           dbname = resultSet.getString("CurrentDatabase");
                        }
                    }
                    
                    //alter charset and collation of database
                    statement.executeQuery("ALTER DATABASE "+dbname+" CHARACTER SET "+tableCharSet+" COLLATE " + tableCollation);

                    //fix the collation of all tables
                    // Query to get character set and collation of the table
                    String getTablesQuery = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_COLLATION != '"+tableCollation+"'";
                    String alterQuery = "";
                    try (ResultSet resultSet = statement.executeQuery(getTablesQuery)) {
                        while (resultSet.next()) {
                           String table = resultSet.getString("TABLE_NAME");
                           alterQuery = "ALTER TABLE "+table+" CONVERT TO CHARACTER SET "+tableCharSet+" COLLATE "+tableCollation+";";
                           LogUtil.info(DatabaseUtil.class.getName(), "Alter table "+table+" charset to "+tableCharSet+" and collation to " + tableCollation);
                           statement.executeQuery(alterQuery);
                        }
                    }
                } catch (Exception ex) {
                    LogUtil.error(DatabaseUtil.class.getName(), ex, "");
                } finally {
                    statement.executeQuery("SET foreign_key_checks = 1");
                }
            }
        } catch (Exception e) {
            LogUtil.info(DatabaseUtil.class.getName(), "Fail to fix db collation");
        }
        
        
    }
}