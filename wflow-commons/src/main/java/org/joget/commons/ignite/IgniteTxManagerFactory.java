package org.joget.commons.ignite;

import javax.cache.configuration.Factory;
import jakarta.transaction.TransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * Custom transaction manager factory using the JTA transaction manager.
 */
public class IgniteTxManagerFactory implements Factory<TransactionManager> {

    private JtaTransactionManager transactionManager;

    public JtaTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(JtaTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    @Override
    public TransactionManager create() {
        return transactionManager.getTransactionManager();
    }
    
}
