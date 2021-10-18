package org.joget.commons.util;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

public class SessionFactoryCacheListener implements CacheEventListener {

    public void notifyElementExpired(Ehcache cache, Element element) {
        closeSessionFactory(element);
    }

    public void notifyElementEvicted(Ehcache cache, Element element) {
        closeSessionFactory(element);
    }

    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        closeSessionFactory(element);
    }

    protected void closeSessionFactory(Element element) throws HibernateException {
        if (element.getObjectValue() instanceof SessionFactory) {
            final SessionFactory sf = (SessionFactory)element.getObjectValue();
            if (!sf.isClosed()) {
                new Thread() {
                    @Override
                    public void run() {
                        // check for open sessions
                        int retryCount = 0;
                        int sleepDuration = 10000; // 10s
                        Statistics stats = sf.getStatistics();
                        long openSessionCount = stats.getSessionOpenCount() - stats.getSessionCloseCount();
                        long retryLimit = openSessionCount * 60; // delay up to 10 minutes for each open session to prevent closing before transaction is completed
                        while (openSessionCount > 0 && retryCount < retryLimit) {
                            try {
                                Thread.sleep(sleepDuration);
                            } catch (InterruptedException ex) {
                                // ignore
                            }
                            openSessionCount = stats.getSessionOpenCount() - stats.getSessionCloseCount();
                            retryCount++;
                        }
                        
                        // close session factory
                        sf.close();
                    }
                }.start();
            }
        }
    }

    public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
    }

    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
    }

    public void notifyRemoveAll(Ehcache cache) {
    }

    public void dispose() {
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
}
