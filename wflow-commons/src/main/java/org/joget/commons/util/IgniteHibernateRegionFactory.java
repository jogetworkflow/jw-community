package org.joget.commons.util;

import java.util.Map;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.hibernate.HibernateRegionFactory;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.internal.IgniteKernal;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.cfg.spi.DomainDataRegionBuildingContext;
import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.spi.DomainDataRegion;
import org.hibernate.cache.spi.QueryResultsRegion;
import org.hibernate.cache.spi.TimestampsRegion;
import org.hibernate.engine.spi.SessionFactoryImplementor;

/**
 * Custom HibernateRegionFactory for Apache Ignite to allow ignite to be enabled or disabled at startup.
 */
public class IgniteHibernateRegionFactory extends HibernateRegionFactory {

    private IgniteKernal ignite;
    
    @Override 
    public void start(SessionFactoryOptions options, Map cfgValues) throws CacheException {
        // only call super method if ignite cache is available
        if (IgniteCacheManager.isStarted()) {
            super.start(options, cfgValues);
            ignite = (IgniteKernal) Ignition.ignite("ignite-grid");
        }
    }
    
    @Override
    public DomainDataRegion buildDomainDataRegion(
                    DomainDataRegionConfig regionConfig, DomainDataRegionBuildingContext buildingContext) {
        // only call super method if ignite cache is available
        if (IgniteCacheManager.isStarted()) {
            String regionName = regionConfig.getRegionName();
            CacheConfiguration cacheConfiguration = (CacheConfiguration)SecurityUtil.getApplicationContext().getBean("igniteAtomicCache");
            cacheConfiguration.setName(regionName);
            ignite.getOrCreateCache(cacheConfiguration);
            DomainDataRegion domainDataRegion = super.buildDomainDataRegion(regionConfig, buildingContext);
            return domainDataRegion;
        } else {
            return null;
        }
    }

    @Override
    public QueryResultsRegion buildQueryResultsRegion(
                    String regionName, SessionFactoryImplementor sessionFactory) {
        // only call super method if ignite cache is available
        if (IgniteCacheManager.isStarted()) {
            CacheConfiguration cacheConfiguration = (CacheConfiguration)SecurityUtil.getApplicationContext().getBean("igniteAtomicCache");
            cacheConfiguration.setName(regionName);
            ignite.getOrCreateCache(cacheConfiguration);
            QueryResultsRegion queryResultsRegion = super.buildQueryResultsRegion(regionName, sessionFactory);
            return queryResultsRegion;
        } else {
            return null;
        }
    }

    @Override
    public TimestampsRegion buildTimestampsRegion(
                    String regionName, SessionFactoryImplementor sessionFactory) {
        // only call super method if ignite cache is available
        if (IgniteCacheManager.isStarted()) {
            CacheConfiguration cacheConfiguration = (CacheConfiguration)SecurityUtil.getApplicationContext().getBean("igniteAtomicCache");
            cacheConfiguration.setName(regionName);
            ignite.getOrCreateCache(cacheConfiguration);
            TimestampsRegion timestampsRegion = super.buildTimestampsRegion(regionName, sessionFactory);
            return timestampsRegion;
        } else {
            return null;
        }
    }

    /**
     * CUSTOM: Override timeout value from 60s to 1s to prevent queries hitting 
     * the DB all the time
     * https://developer.jboss.org/message/990403#990403
     * 
     * When an entity is updated, in TimestampsCacheEnabledImpl.preInvalidate
     * (TimestampsCacheEnabledImpl.java:50) 
     * final Long ts = regionFactory.nextTimestamp() + regionFactory.getTimeout(); 
     * the default 60s timeout gets added to the cache timestamp.
     *
     * When an item is put into the cache at QueryResultsCacheImpl.put
     * (QueryResultsCacheImpl.java:92), 
     * cacheRegion.putIntoCache( key, cacheItem, session) 
     * the cacheItem.timestamp is the current instant, which
     * is less the the last updated timestamp because of the added timeout. 
     * Thus the cache is considered not-up-to-date, which results in a DB hit.
     *
     * Solution is to reduce the default getTimeout() value in the
     * RegionFactory.
     * @return
     */
    @Override
    public long getTimeout() {
        return 1000L;
    }
    
}
