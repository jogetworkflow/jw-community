package org.joget.commons.cache;

import java.util.Properties;
import javax.cache.Cache;
import org.hibernate.query.Query;

/**
 * Dummy implementation for InMemoryCacheManager
 */
public class NoInMemoryCacheManager implements InMemoryCacheManager {

    @Override
    public void clear(String cacheName) {
    }

    @Override
    public void clearAll() {
    }

    @Override
    public Cache getCache(String cacheName, long expiry) {
        return null;
    }

    @Override
    public long getConfigAsyncTimeout() {
        return 0;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public Query setCacheable(Query query, String regionName) {
        return query;
    }

    @Override
    public Properties getHibernateProperties() {
        return new Properties();
    }

    @Override
    public String getConsoleHtml() {
        return "";
    }

    @Override
    public String getConsoleJson() {
        return "";
    }
    
}
