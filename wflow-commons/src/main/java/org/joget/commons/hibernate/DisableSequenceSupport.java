package org.joget.commons.hibernate;

import org.hibernate.dialect.sequence.NextvalSequenceSupport;

/**
 * Used to disable sequences in oracle and MSSQL to improve performance
 * @author owenong
 */
public class DisableSequenceSupport extends NextvalSequenceSupport{
        
    @Override
    public boolean supportsSequences() {
        return false;
    }
}
