package org.joget.commons.util;

import net.sf.ehcache.Element;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * #2261 (round 14): regression coverage for the two P1s an external (Sol) review found in {@link
 * ProfileScopedNonceCache} after it was first introduced this round.
 */
public class ProfileScopedNonceCacheTest {

    @After
    public void tearDown() {
        // HostManager's profile ThreadLocal must not leak into other tests on this shared JVM fork.
        HostManager.setCurrentProfileWithoutTracking(null);
    }

    @Test
    public void testGetAndPut_failClosed_whenNoAmbientProfileSet() {
        HostManager.setCurrentProfileWithoutTracking(null);
        javax.cache.Cache backing = mock(javax.cache.Cache.class);
        ProfileScopedNonceCache cache = new ProfileScopedNonceCache(backing);

        try {
            cache.get("state123");
            fail("get(..) with no ambient profile must fail closed, not silently scope to a "
                    + "fallback/default profile");
        } catch (SecurityException expected) {
            // expected
        }

        // #2261 (Sol review finding, round 15 "MISSED" note): without this, an implementation
        // that resolved/memoized a fallback profile (the exact bug being fixed) and only THEN
        // threw could still pass this test for the wrong reason. Asserting the ambient profile is
        // still unset after the failed call rules that out.
        assertEquals(null, HostManager.getCurrentProfile());

        try {
            cache.put(new Element("state123", "nonceValue"));
            fail("put(..) with no ambient profile must fail closed the same way");
        } catch (SecurityException expected) {
            // expected
        }
    }

    @Test
    public void testGetAndPut_scopeByProfile_whenAmbientProfileSet() {
        HostManager.setCurrentProfileWithoutTracking("tenant1");
        javax.cache.Cache backing = mock(javax.cache.Cache.class);
        ProfileScopedNonceCache cache = new ProfileScopedNonceCache(backing);

        when(backing.get(any())).thenReturn(null);
        // Ordinary scoped access must NOT throw once a real ambient profile is set.
        assertEquals(null, cache.get("state123"));
        cache.put(new Element("state123", "nonceValue"));
    }

    @Test
    public void testGetCacheManagerAndGetConfiguration_alwaysDenied() {
        HostManager.setCurrentProfileWithoutTracking("tenant1");
        javax.cache.Cache backing = mock(javax.cache.Cache.class);
        ProfileScopedNonceCache cache = new ProfileScopedNonceCache(backing);

        try {
            cache.getCacheManager();
            fail("getCacheManager() must be refused -- it hands back the raw, unscoped backing's "
                    + "own CacheManager, from which the shared region is reachable directly");
        } catch (UnsupportedOperationException expected) {
            // expected
        }

        try {
            cache.getConfiguration(Object.class);
            fail("getConfiguration(..) must be refused -- its Eh107Configuration can be unwrapped "
                    + "to register a listener on the shared, unscoped region");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }
}
