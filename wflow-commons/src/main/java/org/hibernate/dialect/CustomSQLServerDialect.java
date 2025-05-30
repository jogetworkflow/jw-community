package org.hibernate.dialect;

import org.hibernate.dialect.sequence.SequenceSupport;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.joget.commons.hibernate.DisableSequenceSupport;

import static org.hibernate.type.SqlTypes.CLOB;
import static org.hibernate.type.SqlTypes.VARCHAR;

public class CustomSQLServerDialect extends SQLServerDialect {
    
    private SequenceSupport sequenceSupport = new DisableSequenceSupport();
    
    public CustomSQLServerDialect(DialectResolutionInfo info) {
        super(info);
    }
    
    /**
     * CUSTOM: override TEXT mapping to NVARCHAR(MAX) to allow for query equality checking #753
     * CUSTOM: override VARCHAR mapping to NVARCHAR, MSSQL default using VARCHAR which does not support unicode
     * 
     * @param sqlTypeCode
     * @return 
     */
    @Override
    protected String columnType(int sqlTypeCode) {
        switch ( sqlTypeCode ) {
            case CLOB:
                return "nvarchar(max)";
            case VARCHAR:
		return "nvarchar($l)"; 
            default:
		return super.columnType( sqlTypeCode );
        }
    }
    
    /**
     * CUSTOM: Disable sequences to improve performance
     * @return 
     */
    @Override
    public SequenceSupport getSequenceSupport() {
        return sequenceSupport;
    }
}
