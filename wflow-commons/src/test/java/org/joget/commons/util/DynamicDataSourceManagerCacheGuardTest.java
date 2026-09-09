package org.joget.commons.util;

import java.lang.reflect.Field;
import javax.cache.Cache;
import javax.cache.CacheManager;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * #2317: {@code setCacheManager(..)} used to assign the raw JSR-107 provider object
 * ({@code cacheManager.getCache(..)}) directly, which is NOT an instance of
 * {@code net.sf.ehcache.Cache} -- the type {@code MultiTenantPluginAspect}'s cache guards match
 * by declaring class (an {@code execution(* net.sf.ehcache.Cache.<method>(..))} pointcut). On
 * 9.1 this silently left DATASOURCE_CACHE (every tenant's live datasource {@code Properties},
 * credentials included) entirely unguarded for every non-admin tenant: {@code isManagedCache(..)}
 * below is never even reached, since the guard's own {@code execution(..)} pointcut never
 * matches a call on the raw provider object in the first place.
 *
 * <p>Confirms the fix: the field now holds a {@code net.sf.ehcache.Cache} wrapper -- the exact
 * same wrap {@code org.joget.commons.util.EhCacheFactoryBean.getObject()} already performs for
 * every other correctly-guarded platform cache -- so {@code isManagedCache(..)} correctly
 * recognizes it.</p>
 */
public class DynamicDataSourceManagerCacheGuardTest {

    @Test
    public void testSetCacheManager_wrapsRawProviderCacheSoIsManagedCacheRecognizesIt() throws Exception {
        // DynamicDataSourceManager.profilePropertyManager and .cache are static, shared across
        // the whole JVM/test fork -- the constructor and setCacheManager(..) under test both
        // assign them. Every OTHER test that relies on the real, Spring-wired instance (e.g.
        // TestCache, which calls DynamicDataSourceManager.getCurrentProfile() transitively via
        // SetupManagerCache.get(..)) would silently break if this test's stand-ins leaked past
        // it, so both fields are captured and restored, regardless of outcome.
        Field profilePropertyManagerField = DynamicDataSourceManager.class.getDeclaredField("profilePropertyManager");
        profilePropertyManagerField.setAccessible(true);
        Object originalProfilePropertyManager = profilePropertyManagerField.get(null);

        Field cacheField = DynamicDataSourceManager.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        Object originalCache = cacheField.get(null);

        try {
            CacheManager cacheManager = mock(CacheManager.class);
            Cache<?, ?> rawProviderCache = mock(Cache.class);
            when(cacheManager.getCache(anyString())).thenReturn((Cache) rawProviderCache);

            DynamicDataSourceManager manager =
                    new DynamicDataSourceManager(mock(DatasourceProfilePropertyManager.class));
            manager.setCacheManager(cacheManager);

            Object wired = cacheField.get(null);

            assertTrue("setCacheManager(..) must wrap the raw provider cache in net.sf.ehcache.Cache "
                            + "so MultiTenantPluginAspect's guards can see it",
                    wired instanceof net.sf.ehcache.Cache);
            assertTrue("isManagedCache(..) must recognize the wrapped cache as this class's own",
                    DynamicDataSourceManager.isManagedCache(wired));
        } finally {
            profilePropertyManagerField.set(null, originalProfilePropertyManager);
            cacheField.set(null, originalCache);
        }
    }
}
