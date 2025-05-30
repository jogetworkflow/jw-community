package org.hibernate.dialect;

import org.hibernate.dialect.sequence.SequenceSupport;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.joget.commons.hibernate.DisableSequenceSupport;

import static org.hibernate.type.SqlTypes.CLOB;

public class CustomOracleDialect extends OracleDialect {
    
    private SequenceSupport sequenceSupport = new DisableSequenceSupport();
    
    public CustomOracleDialect(DialectResolutionInfo info) {
        super(info);
    }
    
    /**
     * CUSTOM: Fixed form data errors in Oracle (#734) by overriding clob mapping to varchar2
     * 
     * @param sqlTypeCode
     * @return 
     */
    @Override
    protected String columnType(int sqlTypeCode) {
        switch ( sqlTypeCode ) {
            case CLOB:
                return "varchar2(4000 char)";
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
