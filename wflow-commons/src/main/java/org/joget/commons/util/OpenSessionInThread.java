package org.joget.commons.util;

import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateAccessor;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class OpenSessionInThread extends HibernateAccessor {
    
    public OpenSessionInThread() {
        setFlushMode(FLUSH_AUTO);
    }
    
    public void sessionStart() {
        if (!(TransactionSynchronizationManager.hasResource(getSessionFactory()) || SessionFactoryUtils.isDeferredCloseActive(getSessionFactory()))) {
            Session session = SessionFactoryUtils.getSession(getSessionFactory(), getEntityInterceptor(), getJdbcExceptionTranslator());
            applyFlushMode(session, false);
            TransactionSynchronizationManager.bindResource(getSessionFactory(), new SessionHolder(session));
        }
    }
    
    public void sessionStop() {
        SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.unbindResource(getSessionFactory());
	SessionFactoryUtils.closeSession(sessionHolder.getSession());
    }
}
