package org.hibernate.dialect;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.descriptor.sql.internal.CapacityDependentDdlType;
import org.hibernate.type.descriptor.sql.spi.DdlTypeRegistry;

import static org.hibernate.type.SqlTypes.*;

public class CustomMySQLDialect extends MySQLDialect {

    public CustomMySQLDialect(DialectResolutionInfo info) {
        super(info);
    }

    @Override
    protected void registerColumnTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.registerColumnTypes(typeContributions, serviceRegistry);
        registerColumnTypesCustom(this, typeContributions, serviceRegistry);
    }

    static void registerColumnTypesCustom(MySQLDialect mySQLDialect, TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        DdlTypeRegistry ddlTypeRegistry = typeContributions.getTypeConfiguration().getDdlTypeRegistry();

        //           ----- Copied from Hibernate 6.6.1 -----
        // Override parts of implementation of Hibernate 6's MySQLDialect
        // change type to `longtext` where maxLobLen is set to `text`

        final int maxTinyLobLen = 255;
        final int maxLobLen = 65_535;
        final int maxMediumLobLen = 16_777_215;

        final CapacityDependentDdlType.Builder varcharBuilder =
                CapacityDependentDdlType.builder(
                                VARCHAR,
                                CapacityDependentDdlType.LobKind.BIGGEST_LOB,
                                mySQLDialect.columnType(CLOB),
                                mySQLDialect.columnType(CHAR),
                                mySQLDialect.castType(CHAR),
                                mySQLDialect
                        )
                        .withTypeCapacity(mySQLDialect.getMaxVarcharLength(), "varchar($l)")
                        .withTypeCapacity(maxMediumLobLen, "mediumtext")
                        .withTypeCapacity(maxLobLen, "longtext"); // force longtext
        ddlTypeRegistry.addDescriptor(varcharBuilder.build());

        // do not use nchar/nvarchar/ntext because these
        // types use a deprecated character set on MySQL 8
        final CapacityDependentDdlType.Builder nvarcharBuilder =
                CapacityDependentDdlType.builder(
                                NVARCHAR,
                                CapacityDependentDdlType.LobKind.BIGGEST_LOB,
                                mySQLDialect.columnType(NCLOB),
                                mySQLDialect.columnType(NCHAR),
                                mySQLDialect.castType(NCHAR),
                                mySQLDialect
                        )
                        .withTypeCapacity(mySQLDialect.getMaxVarcharLength(), "varchar($l) character set utf8")
                        .withTypeCapacity(maxMediumLobLen, "mediumtext character set utf8")
                        .withTypeCapacity(maxLobLen, "longtext character set utf8"); // force longtext
        ddlTypeRegistry.addDescriptor(nvarcharBuilder.build());

        ddlTypeRegistry.addDescriptor(
                CapacityDependentDdlType.builder(CLOB,
                                mySQLDialect.columnType(CLOB), mySQLDialect.castType(CHAR), mySQLDialect)
                        .withTypeCapacity(maxTinyLobLen, "tinytext")
                        .withTypeCapacity(maxMediumLobLen, "mediumtext")
                        .withTypeCapacity(maxLobLen, "longtext") // force longtext
                        .build()
        );

        ddlTypeRegistry.addDescriptor(
                CapacityDependentDdlType.builder(NCLOB,
                                mySQLDialect.columnType(NCLOB), mySQLDialect.castType(NCHAR), mySQLDialect)
                        .withTypeCapacity(maxTinyLobLen, "tinytext character set utf8")
                        .withTypeCapacity(maxMediumLobLen, "mediumtext character set utf8")
                        .withTypeCapacity(maxLobLen, "longtext character set utf8") // force longtext
                        .build()
        );
    }
}
