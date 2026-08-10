package org.joget.commons.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.cache.Cache;
import javax.cache.CacheManager;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * The {@code requestTime} contract of {@link LongTermCache#putObject(String, Object, Date)}:
 * "keep its requesting time instead of cache creation time to prevent stale cache".
 *
 * <p>That exists for a specific race. A slow build reads its inputs at T0 and writes its result at
 * T0+300ms; another node invalidates the entry at T0+100ms. Comparing the invalidation against the
 * WRITE time says the entry is newer and therefore fine, so the invalidation is masked and the stale
 * value is served until something else evicts it — the #2085 failure. Comparing it against the time
 * the inputs were READ correctly condemns it.</p>
 *
 * <p>No Mockito in this module's test scope, hence the proxies.</p>
 */
public class TestLongTermCacheRequestTime {

    private static final String KEY = "profile1_FORM_COLUMNS_t1";

    private Map<Object, Object> store;
    private Map<String, String> settings;
    private LongTermCache cache;

    @Before
    public void setUp() {
        store = new LinkedHashMap<Object, Object>();
        settings = new LinkedHashMap<String, String>();

        cache = new LongTermCache();
        cache.setCacheManager(cacheManager(store));
        cache.setSetupManager(new SetupManager() {
            @Override
            public String getSettingValue(String property) {
                return settings.get(property);
            }

            @Override
            public java.util.Collection<String> getSettingValuesFresh(String property) {
                String v = settings.get(property);
                return (v == null) ? java.util.Collections.<String>emptyList()
                                   : java.util.Collections.singletonList(v);
            }

            @Override
            public void updateSetting(String property, String value) {
                settings.put(property, value);
            }
        });
    }

    /**
     * An invalidation that lands DURING a build must condemn that build's result.
     *
     * <p>Red before the fix: {@code putObject} passed {@code requestTime} to
     * {@code Element(key, value, long version)}, whose third argument is the element VERSION — that
     * constructor sets the creation time to {@code System.currentTimeMillis()} regardless. So the
     * whole requestTime mechanism was inert, and every caller that carefully threaded a requestTime
     * through got nothing for it. The form column build threads one through four methods.</p>
     */
    @Test
    public void anInvalidationDuringTheBuildCondemnsTheResult() {
        long now = System.currentTimeMillis();

        // Another node invalidated 100ms after this build read its inputs.
        settings.put("CACHE_LAST_CLEAR_" + KEY, Long.toString(now - 200L));

        cache.putObject(KEY, "columnsFromStaleInputs", new Date(now - 300L));

        assertNull("an entry built from inputs read BEFORE an invalidation must not be served",
                cache.getObject(KEY));
    }

    /** The mirror: an invalidation that predates the build's inputs must not condemn it. */
    @Test
    public void anInvalidationBeforeTheBuildDoesNotCondemnTheResult() {
        long now = System.currentTimeMillis();
        settings.put("CACHE_LAST_CLEAR_" + KEY, Long.toString(now - 5000L));

        cache.putObject(KEY, "freshColumns", new Date(now - 300L));

        assertEquals("an entry built after the last invalidation is current",
                "freshColumns", cache.getObject(KEY));
    }

    /** Same rule on the database-verified path, which is what the form column cache now uses. */
    @Test
    public void theVerifiedReadAppliesTheSameRequestTimeRule() {
        long now = System.currentTimeMillis();
        settings.put("CACHE_LAST_CLEAR_" + KEY, Long.toString(now - 200L));

        cache.putObject(KEY, "columnsFromStaleInputs", new Date(now - 300L));

        assertNull(cache.getObjectFresh(KEY));
    }

    /** No requestTime means "now", the ordinary case. */
    @Test
    public void withoutARequestTimeTheEntryIsTreatedAsWrittenNow() {
        settings.put("CACHE_LAST_CLEAR_" + KEY, Long.toString(System.currentTimeMillis() - 1000L));

        cache.putObject(KEY, "columns");

        assertEquals("columns", cache.getObject(KEY));
    }

    // ------------------------------------------------------------------ helpers

    private static CacheManager cacheManager(final Map<Object, Object> store) {
        final Cache jcache = (Cache) Proxy.newProxyInstance(
                TestLongTermCacheRequestTime.class.getClassLoader(),
                new Class[]{Cache.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                switch (method.getName()) {
                    case "get":
                        return store.get(args[0]);
                    case "put":
                        store.put(args[0], args[1]);
                        return null;
                    case "remove":
                        return Boolean.valueOf(store.remove(args[0]) != null);
                    case "iterator":
                        return store.entrySet().iterator();
                    default:
                        return null;
                }
            }
        });
        return (CacheManager) Proxy.newProxyInstance(
                TestLongTermCacheRequestTime.class.getClassLoader(),
                new Class[]{CacheManager.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                return "getCache".equals(method.getName()) ? jcache : null;
            }
        });
    }
}
