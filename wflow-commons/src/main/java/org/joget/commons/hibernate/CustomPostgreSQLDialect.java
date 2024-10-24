package org.joget.commons.hibernate;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import static org.hibernate.type.SqlTypes.CLOB;
import static org.hibernate.type.SqlTypes.NVARCHAR;
import static org.hibernate.type.SqlTypes.VARCHAR;

public class CustomPostgreSQLDialect extends PostgreSQLDialect {
    
    public CustomPostgreSQLDialect(DialectResolutionInfo info) {
        super(info);
    }
    
    /**
     * CUSTOM: Fixed builder long JSON definition can't store correctly
     * 
     * @param sqlTypeCode
     * @return 
     */
    @Override
    protected String columnType(int sqlTypeCode) {
        switch ( sqlTypeCode ) {
            case NVARCHAR:
            case VARCHAR:    
            case CLOB:
                return "text";
            default:
		return super.columnType( sqlTypeCode );
        }
    }
}
