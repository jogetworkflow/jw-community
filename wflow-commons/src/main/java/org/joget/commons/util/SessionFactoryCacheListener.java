package org.joget.commons.util;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

public class SessionFactoryCacheListener implements CacheEventListener {

    @Override
    public void onEvent(CacheEvent ce) {
        if (ce.getOldValue() instanceof SessionFactory) {
            final SessionFactory sf = (SessionFactory)ce.getOldValue();
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
}
