package org.joget.commons.util;

import org.apache.commons.dbcp2.BasicDataSource;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;

public class DataSourceCacheListener implements CacheEventListener {

    @Override
    public void onEvent(CacheEvent ce) {
        if (ce.getOldValue() instanceof BasicDataSource) {
            LogUtil.debug(DataSourceCacheListener.class.getName(), "Closing datasource.");
                            
            final BasicDataSource ds = (BasicDataSource) ce.getOldValue();
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
}