package org.joget.commons.util;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;
import com.atomikos.icatch.jta.UserTransactionManager;

public class AtomikosJtaPlatform extends AbstractJtaPlatform {

    private static final long serialVersionUID = 1L;
    private UserTransactionManager utm;

    public AtomikosJtaPlatform() {
        utm = new UserTransactionManager();
    }

    @Override
    protected TransactionManager locateTransactionManager() {
        return utm;
    }

    @Override
    protected UserTransaction locateUserTransaction() {
        return utm;
    }
}
