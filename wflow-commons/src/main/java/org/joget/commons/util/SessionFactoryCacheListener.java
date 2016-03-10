package org.joget.commons.util;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

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
                        // delay close
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            // ignore
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
