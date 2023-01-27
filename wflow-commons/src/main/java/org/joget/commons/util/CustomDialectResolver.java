package org.joget.commons.util;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

/**
 * CUSTOM: Use Oracle10gDialect instead of Oracle8iDialect for unrecognized Oracle major version
 */
public class CustomDialectResolver extends StandardDialectResolver {

    @Override
    public Dialect resolveDialect(DialectResolutionInfo info) {
        Dialect dialect = null;
        final String databaseName = info.getDatabaseName();
        if ("Oracle".equals(databaseName)) {
            dialect = new Oracle10gDialect();
        } else if ("MariaDB".equals(databaseName)) {
            dialect = new MySQL5Dialect();
        } else {
            dialect = super.resolveDialect(info);
        }

        return dialect;
    }

}
