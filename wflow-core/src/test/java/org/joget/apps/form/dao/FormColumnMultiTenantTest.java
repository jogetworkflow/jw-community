package org.joget.apps.form.dao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.cache.Cache;
import javax.cache.CacheManager;
import org.joget.apps.app.dao.BuilderDefinitionDao;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.BuilderDefinition;
import org.joget.apps.form.model.FormColumnCache;
import org.joget.apps.form.service.FormService;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LongTermCache;
import org.joget.commons.util.SetupManager;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Two tenants driven through the REAL caching stack — real {@link FormColumnResolver}, real
 * {@link FormColumnCache}, real {@link LongTermCache} — rather than a mocked cache.
 *
 * <p>The other suites mock {@code FormColumnCache}, so they can only assert which method the DAO
 * called, never that the key it was called with actually isolates tenants. That leaves the entire
 * key derivation — {@code getCacheKey}, the incremental base, the reset stamp, the manifest
 * backoff — unexercised for multitenancy. This closes that at the JVM level.</p>
 *
 * <p>Deliberately faithful to how production shares and separates things:</p>
 * <ul>
 *   <li>ONE javax.cache instance for all tenants (production shares {@code RBM_CACHE}), so a
 *       missing profile in a cache key shows up as a cross-tenant hit rather than being hidden by
 *       per-tenant storage.</li>
 *   <li>A SEPARATE {@code wf_setup} map per profile, because each tenant has its own database — that
 *       is where {@code CACHE_LAST_CLEAR_} rows live.</li>
 *   <li>Different form definitions per tenant, so a leak is visible as the WRONG COLUMNS, not just
 *       as a wrong count.</li>
 * </ul>
 *
 * <p>What this does NOT cover: two real datasources. Tenant routing itself is Hibernate's and
 * {@code DynamicDataSourceManager}'s job and needs the DB matrix rig; here the DAOs are stubbed per
 * profile. What is covered is everything this branch actually added.</p>
 */
public class FormColumnMultiTenantTest {

    private static final String TABLE = "app_fd_shared";
    private static final String UNPREFIXED = "shared";

    private static final String TENANT_A = "tenantA";
    private static final String TENANT_B = "tenantB";

    /** Shared across tenants, exactly like production's single RBM_CACHE. */
    private Map<Object, Object> sharedCache;

    /** Per-tenant wf_setup: profile -> (property -> value). */
    private Map<String, Map<String, String>> settingsByProfile;

    private FormColumnCache formColumnCache;
    private FormColumnResolver resolver;
    private FormDefinitionDao formDefinitionDao;
    private BuilderDefinitionDao builderDefinitionDao;

    /** profile -> the columns that tenant's builder definition declares. */
    private Map<String, String> builderJsonByProfile;

    @Before
    public void setUp() throws Exception {
        sharedCache = new LinkedHashMap<Object, Object>();
        settingsByProfile = new LinkedHashMap<String, Map<String, String>>();
        builderJsonByProfile = new LinkedHashMap<String, String>();

        LongTermCache longTermCache = new LongTermCache();
        longTermCache.setCacheManager(cacheManager(sharedCache));
        longTermCache.setSetupManager(perTenantSetupManager());

        formColumnCache = new FormColumnCache();
        formColumnCache.setCache(longTermCache);

        formDefinitionDao = mock(FormDefinitionDao.class);
        builderDefinitionDao = mock(BuilderDefinitionDao.class);
        // No form definitions anywhere; each tenant's columns come from its builder definition,
        // which needs no FormService or element tree to parse.
        when(formDefinitionDao.loadFormDefinitionByTableName(anyString())).thenReturn(new ArrayList());
        when(formDefinitionDao.getFormDefinitionManifestByTableName(anyString()))
                .thenReturn(new ArrayList<Object[]>());
        when(builderDefinitionDao.find(anyString(), any(), any(), any(), any(), any(), any()))
                .thenAnswer(inv -> builderDefsFor(currentProfile()));

        resolver = new FormColumnResolver();
        resolver.setFormColumnCache(formColumnCache);
        resolver.setFormDefinitionDao(formDefinitionDao);
        resolver.setBuilderDefinitionDao(builderDefinitionDao);
        resolver.setFormService(mock(FormService.class));

        clearManifestBackoff();
        HostManager.setCurrentProfile(TENANT_A);
    }

    @After
    public void tearDown() throws Exception {
        clearManifestBackoff();
        HostManager.setCurrentProfile(null);
    }

    // ------------------------------------------------------------------ column set isolation

    /**
     * THE POINT: the same table name in two tenants must resolve to each tenant's own columns.
     *
     * <p>Falsifiable: drop the profile from {@code FormColumnCache.getCacheKey} and tenant B is
     * served tenant A's columns straight out of the shared cache.</p>
     */
    @Test
    public void twoTenantsSharingATableNameGetTheirOwnColumns() {
        builderJsonByProfile.put(TENANT_A, "{\"columns\":{\"tenantAColumn\":{}}}");
        builderJsonByProfile.put(TENANT_B, "{\"columns\":{\"tenantBColumn\":{}}}");

        asTenant(TENANT_A);
        Collection<String> a = resolver.getColumnNames(TABLE);
        asTenant(TENANT_B);
        Collection<String> b = resolver.getColumnNames(TABLE);

        assertEquals("tenant A must see only its own column: " + a, 1, a.size());
        assertTrue(a.contains("tenantAColumn"));
        assertEquals("tenant B must see only its own column: " + b, 1, b.size());
        assertTrue("tenant B was served tenant A's column set: " + b, b.contains("tenantBColumn"));
    }

    /** A warm entry must stay warm per tenant: tenant B's build must not be served from A's entry. */
    @Test
    public void aWarmEntryForOneTenantIsNotAWarmEntryForAnother() {
        builderJsonByProfile.put(TENANT_A, "{\"columns\":{\"tenantAColumn\":{}}}");
        builderJsonByProfile.put(TENANT_B, "{\"columns\":{\"tenantBColumn\":{}}}");

        asTenant(TENANT_A);
        resolver.getColumnNames(TABLE);
        resolver.getColumnNames(TABLE);      // served warm, no second build

        asTenant(TENANT_B);
        Collection<String> b = resolver.getColumnNames(TABLE);

        assertTrue("tenant B must have built its own entry: " + b, b.contains("tenantBColumn"));
        // Two builder reads for A's first build and B's first build; A's second call was a cache hit.
        verify(builderDefinitionDao, times(2)).find(anyString(), any(), any(), any(), any(), any(), any());
    }

    // ------------------------------------------------------------------ index isolation

    @Test
    public void indexesAreIsolatedPerTenant() {
        builderJsonByProfile.put(TENANT_A, "{\"columns\":{\"c\":{}},\"indexes\":{\"idxA\":{}}}");
        builderJsonByProfile.put(TENANT_B, "{\"columns\":{\"c\":{}},\"indexes\":{\"idxB\":{}}}");

        asTenant(TENANT_A);
        resolver.getColumnNames(TABLE);
        asTenant(TENANT_B);
        resolver.getColumnNames(TABLE);

        asTenant(TENANT_A);
        assertTrue(formColumnCache.getIndexes(UNPREFIXED).contains("idxA"));
        asTenant(TENANT_B);
        assertTrue("tenant B was served tenant A's index set",
                formColumnCache.getIndexes(UNPREFIXED).contains("idxB"));
    }

    // ------------------------------------------------------------------ invalidation isolation

    /** One tenant invalidating a table must not evict another tenant's entry for that table. */
    @Test
    public void removeIsScopedToTheCurrentTenant() {
        builderJsonByProfile.put(TENANT_A, "{\"columns\":{\"tenantAColumn\":{}}}");
        builderJsonByProfile.put(TENANT_B, "{\"columns\":{\"tenantBColumn\":{}}}");

        asTenant(TENANT_A);
        resolver.getColumnNames(TABLE);
        asTenant(TENANT_B);
        resolver.getColumnNames(TABLE);

        asTenant(TENANT_A);
        formColumnCache.remove(UNPREFIXED);

        assertNull("tenant A's entry must be gone", formColumnCache.get(UNPREFIXED));
        asTenant(TENANT_B);
        assertNotNull("tenant B's entry must survive tenant A's invalidation",
                formColumnCache.get(UNPREFIXED));
    }

    /** And the heavier "forget everything" must be scoped too — including the incremental base. */
    @Test
    public void resetIsScopedToTheCurrentTenant() {
        builderJsonByProfile.put(TENANT_A, "{\"columns\":{\"tenantAColumn\":{}}}");
        builderJsonByProfile.put(TENANT_B, "{\"columns\":{\"tenantBColumn\":{}}}");

        asTenant(TENANT_A);
        resolver.getColumnNames(TABLE);
        asTenant(TENANT_B);
        resolver.getColumnNames(TABLE);

        asTenant(TENANT_A);
        formColumnCache.reset(UNPREFIXED);

        assertNull(formColumnCache.getBase(UNPREFIXED));
        asTenant(TENANT_B);
        assertNotNull("tenant B's incremental base must survive tenant A's reset",
                formColumnCache.getBase(UNPREFIXED));
    }

    /** {@code clear()} is the operator's big hammer; it must still land on one tenant only. */
    @Test
    public void clearIsScopedToTheCurrentTenant() {
        builderJsonByProfile.put(TENANT_A, "{\"columns\":{\"tenantAColumn\":{}}}");
        builderJsonByProfile.put(TENANT_B, "{\"columns\":{\"tenantBColumn\":{}}}");

        asTenant(TENANT_A);
        resolver.getColumnNames(TABLE);
        asTenant(TENANT_B);
        resolver.getColumnNames(TABLE);

        asTenant(TENANT_A);
        formColumnCache.clear();

        assertNull(formColumnCache.get(UNPREFIXED));
        assertNull(formColumnCache.getBase(UNPREFIXED));
        asTenant(TENANT_B);
        assertNotNull("tenant B's entry must survive tenant A's clear",
                formColumnCache.get(UNPREFIXED));
        assertNotNull("tenant B's base must survive tenant A's clear",
                formColumnCache.getBase(UNPREFIXED));
    }

    // ------------------------------------------------------------------ cross-node flag isolation

    /**
     * The {@code CACHE_LAST_CLEAR_} row lives in the tenant's OWN database, and the verified read
     * must consult that tenant's copy.
     *
     * <p>A shared or mis-keyed lookup would let one tenant's invalidation permanently condemn
     * another tenant's entry — the cache would appear to never warm up, for no visible reason.</p>
     */
    @Test
    public void anotherNodesInvalidationForOneTenantDoesNotCondemnAnother() {
        builderJsonByProfile.put(TENANT_A, "{\"columns\":{\"tenantAColumn\":{}}}");
        builderJsonByProfile.put(TENANT_B, "{\"columns\":{\"tenantBColumn\":{}}}");

        asTenant(TENANT_A);
        resolver.getColumnNames(TABLE);
        asTenant(TENANT_B);
        resolver.getColumnNames(TABLE);

        // Another NODE invalidated tenant A's entry: the flag row appears in tenant A's wf_setup
        // only, and the local cache entry is untouched.
        settings(TENANT_A).put("CACHE_LAST_CLEAR_" + TENANT_A + "_FORM_COLUMNS_" + UNPREFIXED,
                Long.toString(System.currentTimeMillis() + 1000L));

        asTenant(TENANT_A);
        assertNull("tenant A's entry must be condemned by its own flag",
                formColumnCache.getVerified(UNPREFIXED));
        asTenant(TENANT_B);
        assertNotNull("tenant B must not be condemned by tenant A's flag",
                formColumnCache.getVerified(UNPREFIXED));
    }

    /** The verification throttle is per tenant, so one tenant cannot suppress another's check. */
    @Test
    public void theVerificationThrottleIsPerTenant() {
        builderJsonByProfile.put(TENANT_A, "{\"columns\":{\"tenantAColumn\":{}}}");
        builderJsonByProfile.put(TENANT_B, "{\"columns\":{\"tenantBColumn\":{}}}");

        asTenant(TENANT_A);
        resolver.getColumnNames(TABLE);
        asTenant(TENANT_B);
        resolver.getColumnNames(TABLE);

        // A consumes its own throttle window.
        asTenant(TENANT_A);
        formColumnCache.getVerified(UNPREFIXED);

        // B's window must be untouched, so B's flag is still honoured immediately.
        settings(TENANT_B).put("CACHE_LAST_CLEAR_" + TENANT_B + "_FORM_COLUMNS_" + UNPREFIXED,
                Long.toString(System.currentTimeMillis() + 1000L));
        asTenant(TENANT_B);
        assertNull("tenant B's verification must not be throttled by tenant A's check",
                formColumnCache.getVerified(UNPREFIXED));
    }

    // ------------------------------------------------------------------ helpers

    private void asTenant(String profile) {
        HostManager.setCurrentProfile(profile);
    }

    private String currentProfile() {
        return HostManager.getCurrentProfile();
    }

    private Map<String, String> settings(String profile) {
        Map<String, String> tenantSettings = settingsByProfile.get(profile);
        if (tenantSettings == null) {
            tenantSettings = new LinkedHashMap<String, String>();
            settingsByProfile.put(profile, tenantSettings);
        }
        return tenantSettings;
    }

    private List<BuilderDefinition> builderDefsFor(String profile) {
        List<BuilderDefinition> defs = new ArrayList<BuilderDefinition>();
        String json = builderJsonByProfile.get(profile);
        if (json != null) {
            BuilderDefinition def = mock(BuilderDefinition.class);
            when(def.getJson()).thenReturn(json);
            defs.add(def);
        }
        return defs;
    }

    /** Reads and writes the CURRENT tenant's wf_setup, like a tenant-routed SetupDao would. */
    private SetupManager perTenantSetupManager() {
        return new SetupManager() {
            @Override
            public String getSettingValue(String property) {
                return settings(String.valueOf(currentProfile())).get(property);
            }

            @Override
            public java.util.Collection<String> getSettingValuesFresh(String property) {
                String v = settings(String.valueOf(currentProfile())).get(property);
                return (v == null) ? java.util.Collections.<String>emptyList()
                                   : java.util.Collections.singletonList(v);
            }

            @Override
            public void updateSetting(String property, String value) {
                settings(String.valueOf(currentProfile())).put(property, value);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static void clearManifestBackoff() throws Exception {
        java.lang.reflect.Field f = FormColumnResolver.class.getDeclaredField("MANIFEST_RETRY_AT");
        f.setAccessible(true);
        ((Map<Object, Object>) f.get(null)).clear();
    }

    private static Object entry(final Map<Object, Object> store, final Object key) {
        return Proxy.newProxyInstance(FormColumnMultiTenantTest.class.getClassLoader(),
                new Class[]{Cache.Entry.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                switch (method.getName()) {
                    case "getKey":
                        return key;
                    case "getValue":
                        return store.get(key);
                    default:
                        return null;
                }
            }
        });
    }

    /** One javax.cache instance for every tenant, as production shares RBM_CACHE. */
    private static CacheManager cacheManager(final Map<Object, Object> store) {
        final Cache jcache = (Cache) Proxy.newProxyInstance(
                FormColumnMultiTenantTest.class.getClassLoader(),
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
                        // removeAll() walks this and casts to Cache.Entry, then removes via the
                        // iterator — so the entries must be real Entry views over the live map.
                        List<Object> entries = new ArrayList<Object>();
                        for (Object key : new ArrayList<Object>(store.keySet())) {
                            entries.add(entry(store, key));
                        }
                        return new java.util.Iterator<Object>() {
                            private final java.util.Iterator<Object> delegate = entries.iterator();
                            private Object last;

                            @Override
                            public boolean hasNext() {
                                return delegate.hasNext();
                            }

                            @Override
                            public Object next() {
                                last = delegate.next();
                                return last;
                            }

                            @Override
                            public void remove() {
                                store.remove(((Cache.Entry) last).getKey());
                            }
                        };
                    default:
                        return null;
                }
            }
        });
        return (CacheManager) Proxy.newProxyInstance(
                FormColumnMultiTenantTest.class.getClassLoader(),
                new Class[]{CacheManager.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
                return "getCache".equals(method.getName()) ? jcache : null;
            }
        });
    }
}
