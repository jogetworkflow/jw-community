package org.kecak.commons.util;

import com.atomikos.icatch.jta.UserTransactionManager;
import org.hibernate.engine.transaction.jta.platform.internal.AbstractJtaPlatform;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

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
