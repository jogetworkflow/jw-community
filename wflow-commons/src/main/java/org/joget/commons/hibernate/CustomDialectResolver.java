package org.joget.commons.hibernate;

import org.hibernate.dialect.*;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;

public class CustomDialectResolver implements DialectResolver {
    
    @Override
    public Dialect resolveDialect(DialectResolutionInfo info) {
        for (Database database : Database.values()) {
            if (database.matchesResolutionInfo(info)) {
                switch (database) {
                    case ORACLE:
                        return new CustomOracleDialect(info);
                    case SQLSERVER:
                        return new CustomSQLServerDialect(info);
                    case POSTGRESQL:
                        return new CustomPostgreSQLDialect(info);
                    case MARIADB:
                        return new CustomMariaDBDialect(info);
                    case MYSQL:
                        return new CustomMySQLDialect(info);
                    default:
                        return database.createDialect(info);
                }
            }
        }

        return null;
    }
}
