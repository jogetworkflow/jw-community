package org.hibernate.engine.spi;

import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.internal.DisabledCaching;
import org.hibernate.cache.internal.EnabledCaching;
import org.hibernate.cache.internal.NoCachingRegionFactory;
import org.hibernate.cache.spi.CacheImplementor;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceInitiator;
import org.joget.commons.ignite.IgniteCacheManager;

/**
 * Initiator for second level cache support
 *
 * @author Steve Ebersole
 * @author Strong Liu
 */
public class CacheInitiator implements SessionFactoryServiceInitiator<CacheImplementor> {
	public static final CacheInitiator INSTANCE = new CacheInitiator();

	@Override
	public CacheImplementor initiateService(
			SessionFactoryImplementor sessionFactory,
			SessionFactoryOptions sessionFactoryOptions,
			ServiceRegistryImplementor registry) {
		final RegionFactory regionFactory = registry.getService( RegionFactory.class );
		return ( ! NoCachingRegionFactory.class.isInstance( regionFactory ) && IgniteCacheManager.isStarted() ) // CUSTOM: check for Ignite cache availability
				? new EnabledCaching( sessionFactory )
				: new DisabledCaching( sessionFactory );
	}

	@Override
	public Class<CacheImplementor> getServiceInitiated() {
		return CacheImplementor.class;
	}
}
