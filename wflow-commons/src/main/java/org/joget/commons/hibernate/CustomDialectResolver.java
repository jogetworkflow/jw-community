package org.joget.commons.hibernate;

import org.hibernate.dialect.Database;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;

public class CustomDialectResolver implements DialectResolver {
    
    @Override
    public Dialect resolveDialect(DialectResolutionInfo info) {

            for ( Database database : Database.values() ) {
                if ( database.matchesResolutionInfo( info ) ) {
                    if (database.name().equals(Database.ORACLE.name())) {
                        return new CustomOracleDialect(info);
                    } else if (database.name().equals(Database.SQLSERVER.name())) {
                        return new CustomSQLServerDialect(info);
                    } else if (database.name().equals(Database.POSTGRESQL.name())) {
                        return new CustomPostgreSQLDialect(info);
                    } else {
                        return database.createDialect( info );
                    }
                }
            }

            return null;
    }
}
