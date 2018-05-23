package org.enhydra.shark.repositorypersistence.data;

import com.lutris.appserver.server.sql.DBConnection;
import com.lutris.appserver.server.sql.DBTransaction;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomXPDLDataQuery extends XPDLDataQuery {
    
    private ResultSet customResultSet = null;
    
    public CustomXPDLDataQuery(DBTransaction dbTrans) {
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
