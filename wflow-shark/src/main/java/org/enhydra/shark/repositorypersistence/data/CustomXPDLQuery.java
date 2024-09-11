package org.enhydra.shark.repositorypersistence.data;

import com.lutris.appserver.server.sql.DBConnection;
import com.lutris.appserver.server.sql.DBTransaction;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomXPDLQuery extends XPDLQuery {
    
    private ResultSet customResultSet = null;
    
    public CustomXPDLQuery(DBTransaction dbTrans) {
        super(dbTrans);
    }
    
    public ResultSet getResultSet() {
        return customResultSet;
    }
    
    @Override
    public ResultSet executeQuery(DBConnection conn) throws SQLException {
        this.customResultSet = super.executeQuery(conn);
        return this.customResultSet;
    }
}
