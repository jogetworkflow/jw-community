package org.hibernate.dialect;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.service.ServiceRegistry;

public class CustomMariaDBDialect extends MariaDBDialect {
    public CustomMariaDBDialect(DialectResolutionInfo info) {
        super(info);
    }

    @Override
    protected void registerColumnTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.registerColumnTypes(typeContributions, serviceRegistry);
        CustomMySQLDialect.registerColumnTypesCustom(this, typeContributions, serviceRegistry);
    }
}
