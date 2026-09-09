package org.joget.commons.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link ManagedCacheRegistry#currentTenantKeyFor(Object, Object)}: a restricted caller's raw key is
 * only ever redirected to a key the cache's own scope confirms belongs to the current tenant.
 */
public class TestManagedCacheRegistryCurrentTenantKey {

    private final Object scopedCache = new Object();
    private final Object denyAllCache = new Object();
    private final Object unregisteredCache = new Object();

    @Before
    public void setUp() {
        ManagedCacheRegistry.invalidateProfileListCacheForTest();
        HostManager.setCurrentProfile(null);
        ManagedCacheRegistry.register(scopedCache, ProfileScopedCacheKey.SCOPE);
        ManagedCacheRegistry.register(denyAllCache, ManagedCacheRegistry.NO_TENANT_KEY_SCOPE);
    }

    @After
    public void tearDown() {
        HostManager.setCurrentProfile(null);
    }

    @Test
    public void testRawKeyIsMovedIntoCurrentTenantKeySpace() {
        HostManager.setCurrentProfile("acme");
        Object rewritten = ManagedCacheRegistry.currentTenantKeyFor(scopedCache, "raw less ::before {}");
        assertEquals("acme::raw less ::before {}", rewritten);
        assertTrue(ManagedCacheRegistry.isCurrentTenantKey(scopedCache, rewritten));
    }

    @Test
    public void testAnotherTenantsKeyStaysInsideCurrentTenantKeySpace() {
        HostManager.setCurrentProfile("acme");
        // naming another profile's key never yields that key back, only the caller's own form of it
        assertEquals("acme::other::secret", ManagedCacheRegistry.currentTenantKeyFor(scopedCache, "other::secret"));
    }

    @Test
    public void testNoRewriteWithoutAUsableScopeOrProfile() {
        HostManager.setCurrentProfile("acme");
        assertNull("no scope registered", ManagedCacheRegistry.currentTenantKeyFor(unregisteredCache, "k"));
        assertNull("deny-all scope builds nothing", ManagedCacheRegistry.currentTenantKeyFor(denyAllCache, "k"));
        assertNull("non-String keys are not rewritten", ManagedCacheRegistry.currentTenantKeyFor(scopedCache, Integer.valueOf(1)));
        HostManager.setCurrentProfile("bad:profile");
        assertNull("a profile that cannot be scoped safely builds nothing", ManagedCacheRegistry.currentTenantKeyFor(scopedCache, "k"));
    }

    @Test
    public void testCandidateKeysCoverEveryConventionOfTheCachesScope() {
        HostManager.setCurrentProfile("acme");
        assertEquals(java.util.Collections.singletonList("acme::raw"),
                ManagedCacheRegistry.candidateTenantKeysFor(scopedCache, "raw"));
        assertTrue(ManagedCacheRegistry.candidateTenantKeysFor(unregisteredCache, "raw").isEmpty());
        assertTrue(ManagedCacheRegistry.candidateTenantKeysFor(denyAllCache, "raw").isEmpty());

        Object formOptionsCache = new Object();
        ManagedCacheRegistry.register(formOptionsCache, ManagedCacheRegistry.FORM_OPTIONS_CACHE_SCOPE);
        java.util.List<Object> candidates = ManagedCacheRegistry.candidateTenantKeysFor(formOptionsCache, "raw");
        assertEquals(2, candidates.size());
        assertTrue(candidates.contains("FORMOPTIONS::acme::raw"));
        assertTrue(candidates.contains("LAST_ACTIVE_FORMOPTIONS::acme::raw"));
    }

    @Test
    public void testNonceScopeNeverAcceptsNulSeparatedKeysAndBuildsItsOwnPrefix() {
        HostManager.setCurrentProfile("acme");
        // the OIDC plugin's profile-scoped entries share the NONCE region and use NUL as separator
        assertTrue(!ManagedCacheRegistry.NONCE_GENERATOR_KEY_SCOPE.isCurrentTenantKey("acme_admin\u0000state"));
        assertTrue(!ManagedCacheRegistry.NONCE_GENERATOR_KEY_SCOPE.isCurrentTenantKey("acme_NonceGenerator_n\u0000x"));
        assertTrue("another profile's prefix is never the caller's",
                !ManagedCacheRegistry.NONCE_GENERATOR_KEY_SCOPE.isCurrentTenantKey("other_NonceGenerator_n_s"));
        assertEquals("acme_NonceGenerator_raw", ManagedCacheRegistry.NONCE_GENERATOR_KEY_SCOPE.keyForCurrentTenant("raw"));
        assertNull(ManagedCacheRegistry.NONCE_GENERATOR_KEY_SCOPE.keyForCurrentTenant("a\u0000b"));
    }

    @Test
    public void testBlocksAllTenantKeysOnlyForTheNoTenantScope() {
        assertTrue("a NO_TENANT_KEY_SCOPE cache blocks every tenant key",
                ManagedCacheRegistry.blocksAllTenantKeys(denyAllCache));
        assertTrue("a key-building scope does not block all tenant keys",
                !ManagedCacheRegistry.blocksAllTenantKeys(scopedCache));
        assertTrue("an unregistered cache does not block all tenant keys",
                !ManagedCacheRegistry.blocksAllTenantKeys(unregisteredCache));
    }

    /**
     * Lookups must not take the registry's class lock: the key-scope evaluation they trigger can stat
     * (or re-list) the profiles directory, and with the lock held every restricted cache read in the
     * JVM would queue behind that. A thread holding the lock here must not delay a lookup, and a
     * registration must still become visible to lookups once it completes.
     */
    @Test
    public void testLookupsDoNotBlockOnTheRegistryLock() throws Exception {
        HostManager.setCurrentProfile("acme");
        final java.util.concurrent.CountDownLatch locked = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        Thread holder = new Thread(() -> {
            synchronized (ManagedCacheRegistry.class) {
                locked.countDown();
                try {
                    release.await();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "registry-lock-holder");
        holder.start();
        assertTrue(locked.await(5, java.util.concurrent.TimeUnit.SECONDS));
        try {
            java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newSingleThreadExecutor();
            try {
                java.util.concurrent.Future<Boolean> lookup = pool.submit(() ->
                        ManagedCacheRegistry.isManaged(scopedCache)
                                && ManagedCacheRegistry.hasKeyScope(scopedCache)
                                && ManagedCacheRegistry.isCurrentTenantKey(scopedCache, "acme::k")
                                && "acme::k".equals(ManagedCacheRegistry.currentTenantKeyFor(scopedCache, "k")));
                assertTrue("lookups must complete while another thread holds the registry lock",
                        lookup.get(5, java.util.concurrent.TimeUnit.SECONDS));
            } finally {
                pool.shutdownNow();
            }
        } finally {
            release.countDown();
            holder.join(5000);
        }
        Object lateCache = new Object();
        ManagedCacheRegistry.register(lateCache, ProfileScopedCacheKey.SCOPE);
        assertTrue("a registration must be visible to the next lookup", ManagedCacheRegistry.isCurrentTenantKey(lateCache, "acme::k"));
        Object readScopedCache = new Object();
        ManagedCacheRegistry.registerReadScoped(readScopedCache, ProfileScopedCacheKey.SCOPE);
        assertTrue("a read-scoped registration must be visible as managed", ManagedCacheRegistry.isManaged(readScopedCache));
        assertTrue("a read-scoped registration must be visible as read-scoped", ManagedCacheRegistry.isReadScoped(readScopedCache));
        assertTrue("a read-scoped registration must carry its scope", ManagedCacheRegistry.hasKeyScope(readScopedCache));
        Object readSensitiveCache = new Object();
        ManagedCacheRegistry.registerReadSensitive(readSensitiveCache);
        assertTrue("a read-sensitive registration must be visible", ManagedCacheRegistry.isReadSensitive(readSensitiveCache));
        assertTrue(ManagedCacheRegistry.isManaged(readSensitiveCache));
    }
}
