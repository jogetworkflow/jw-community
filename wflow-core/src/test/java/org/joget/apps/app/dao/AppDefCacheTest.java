package org.joget.apps.app.dao;

import java.lang.reflect.Field;
import java.util.Date;
import javax.cache.Cache;
import org.joget.apps.app.model.AppDefinition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Freshness of {@link AppDefCache} entries is decided by comparing an element's creation time
 * against the {@link AppDefinition#getDateModified()} seen on read. Stamping an entry with
 * wall-clock insertion time leaves a race: a reader holding pre-change data can repopulate the
 * cache <em>after</em> an eviction and stamp the stale value with a wall-clock time newer than the
 * post-change dateModified, so it is never detected as stale again (T8086 / #2196). Stamping with
 * the modification time of the data actually read closes that window.
 */
public class AppDefCacheTest {

    private Cache cache;
    private AppDefCache appDefCache;

    @Before
    public void setUp() throws Exception {
        cache = mock(Cache.class);
        appDefCache = new AppDefCache();
        Field cacheField = AppDefCache.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        cacheField.set(appDefCache, cache);
    }

    private AppDefinition appDef(long dateModifiedMillis) {
        AppDefinition appDef = new AppDefinition();
        appDef.setAppId("t8086");
        appDef.setVersion(1L);
        appDef.setDateModified(new Date(dateModifiedMillis));
        return appDef;
    }

    @Test
    public void testPutWithExplicitCreationTime_usesThatTimeNotWallClock() {
        AppDefinition appDef = appDef(5_000L);
        appDefCache.put("key", "value", appDef);

        ArgumentCaptor<AppDefCache.CacheElement> captor = ArgumentCaptor.forClass(AppDefCache.CacheElement.class);
        verify(cache).put(eq("key"), captor.capture());
        assertEquals("The entry must be stamped with the supplied modification time",
                appDef.getDateModified().getTime(), captor.getValue().creationTime);
    }

    /**
     * A value stamped with an older modification time (e.g. a stale entry repopulated after an
     * eviction) must be evicted once a read observes a newer dateModified.
     */
    @Test
    public void testGetObject_evictsEntryStampedBeforeDateModified() {
        when(cache.get("key")).thenReturn(appDefCache.new CacheElement("key", "stale", 1_000L));

        Object result = appDefCache.getObject("key", appDef(2_000L));

        assertNull("A stale-stamped entry must be treated as a miss", result);
        verify(cache).remove("key");
    }

    @Test
    public void testGetObject_keepsEntryStampedAtOrAfterDateModified() {
        when(cache.get("key")).thenReturn(appDefCache.new CacheElement("key", "fresh", 2_000L));

        Object result = appDefCache.getObject("key", appDef(2_000L));

        assertEquals("An entry as new as the data must be served", "fresh", result);
        verify(cache, never()).remove("key");
    }
}
