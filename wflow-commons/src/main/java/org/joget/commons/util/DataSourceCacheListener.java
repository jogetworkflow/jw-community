package org.joget.commons.util;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.HibernateException;

public class DataSourceCacheListener implements CacheEventListener {

    public void notifyElementExpired(Ehcache cache, Element element) {
        closeDatasource(element);
    }

    public void notifyElementEvicted(Ehcache cache, Element element) {
        closeDatasource(element);
    }

    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        closeDatasource(element);
    }

    protected void closeDatasource(Element element) throws HibernateException {
        if (element.getObjectValue() instanceof BasicDataSource) {
            LogUtil.debug(DataSourceCacheListener.class.getName(), "Closing datasource.");
                            
            final BasicDataSource ds = (BasicDataSource) element.getObjectValue();
            if (!ds.isClosed()) {
                new Thread() {
                    @Override
                    public void run() {
                        // check for active connection
                        int retryCount = 0;
                        int sleepDuration = 10000; // 10s
                        int activeCount = ds.getNumActive();
                        long retryLimit = activeCount * 60; // delay up to 10 minutes for each active connection to prevent closing before transaction is completed
                        while (activeCount > 0 && retryCount < retryLimit) {
                            LogUtil.debug(DataSourceCacheListener.class.getName(), "There is active connection - " + activeCount + ". Can't close data source. Retry after " + sleepDuration + "s.");
                            try {
                                Thread.sleep(sleepDuration);
                            } catch (InterruptedException ex) {
                                // ignore
                            }
                            activeCount = ds.getNumActive();
                            retryCount++;
                        }
                        
                        // close it
                        try {
                            ds.close();
                        } catch (Exception e) {
                            //safe to ignore
                        }
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