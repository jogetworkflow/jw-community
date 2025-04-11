package org.joget.commons.util;

import java.util.Properties;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;

/**
 * Use to close the datasource after the cache removed
 */
public class DataSourceCacheListenerFactory extends CacheEventListenerFactory {

    @Override
    public CacheEventListener createCacheEventListener(Properties properties) {
        return new DataSourceCacheListener();
    }
    
}