package org.joget.commons.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * FORM_OPTIONS_CACHE_SCOPE/USERVIEW_MENU_CACHE_SCOPE exist because userviewMenuCache/
 * formOptionsCache were registered with LEGACY_UNDERSCORE_PREFIX_SCOPE even though neither cache's
 * real key builders use a leading "&lt;profile&gt;_" prefix (see
 * FormOptionsCacheAspect.getCacheKey(..)/UserviewCache.getCacheKey(..)), so a restricted caller's
 * own genuinely self-scoped put/remove against either was unconditionally blocked. This matters
 * more once MultiTenantPluginAspect's ambient-admin-profile widening (jw-enterprise/cloud-wfengine)
 * makes a wider set of admin-profile threads count as "restricted" than before -- without a
 * correctly-matching scope, a legitimate admin-profile mutation to either cache would newly hit the
 * same unconditional block a non-admin tenant already did. setupManagerCache is deliberately NOT
 * given its own KeyScope constant -- see the "setupManagerCache has NO KeyScope constant" comment
 * below for why.
 *
 * <p>The collision-safety cases below need DynamicDataSourceManager.getProfileList() to return a
 * real, controlled list rather than null (which it does whenever its backing directory does not
 * exist, making every collision check fail closed regardless of whether the logic under test is
 * actually correct). SetupManager.BASE_DIRECTORY is `static final`, resolved once at class load
 * from the wflow.home system property, so it cannot be redirected per-test without breaking other
 * tests in this module (e.g. TestStringUtil) that load a real Spring ApplicationContext depending
 * on the real ~/wflow content. Instead this class adds a few fixture app_datasource-*.properties
 * files directly into the REAL shared directory (whatever SetupManager.getBaseSharedDirectory()
 * already resolves to on this machine) and removes only the files it created, in {@link
 * #createFixtureProfiles()}/{@link #removeFixtureProfiles()}.
 */
public class TestManagedCacheRegistryPlatformCacheScopes {

    private static final List<File> FIXTURE_FILES_CREATED = new ArrayList<File>();

    @BeforeClass
    public static void createFixtureProfiles() throws IOException {
        // "tenantA" has no colliding sibling, used by the plain recognize-my-own-key/reject-a-
        // different-tenant's-key cases below. "acme"/"acme:" is the specific colliding pair the
        // collision-regression test needs -- kept separate from "tenantA" so it cannot affect the
        // other tests' fixture list.
        createFixtureProfileFile("tenantA");
        createFixtureProfileFile("acme");
        createFixtureProfileFile("acme:");
        // #2317: UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE's own colliding pair --
        // "custShort2317" is a real, literal prefix of "custShort2317_eu". Neither is a
        // configured admin profile in any test below, so this pair proves ordinary tenant-vs
        // -tenant collisions are granted in BOTH directions -- see
        // testUnderscorePrefixLongestRealProfileMatchScopeGrantsBothDirectionsOnCollidingPairWhenNeitherIsAdmin.
        createFixtureProfileFile("custShort2317");
        createFixtureProfileFile("custShort2317_eu");
    }

    @AfterClass
    public static void removeFixtureProfiles() {
        // Only ever deletes a file this class itself created (tracked, never inferred from name
        // alone) -- never touches a pre-existing real profile file this machine already had.
        for (File file : FIXTURE_FILES_CREATED) {
            file.delete();
        }
        FIXTURE_FILES_CREATED.clear();
    }

    private static void createFixtureProfileFile(String profileName) throws IOException {
        File file = new File(SetupManager.getBaseSharedDirectory(),
                DynamicDataSourceManager.FILE_PREFIX + profileName + DynamicDataSourceManager.FILE_EXTENSION);
        if (!file.exists() && file.createNewFile()) {
            FIXTURE_FILES_CREATED.add(file);
        }
    }

    // Not safe for concurrent invocation (swaps a JVM-wide static field) -- assumes this class
    // (and any other test touching DynamicDataSourceManager's static state) runs single-threaded,
    // the default JUnit4/surefire execution model this module uses; matches the same assumption
    // every HostManager.setCurrentProfile(..)-based test in this class already makes. (Ported
    // from the 8.2 line's own copy of this test class.)
    private static void withAdminProfile(String adminProfileValue, ThrowingRunnable body) throws Exception {
        java.lang.reflect.Field cacheField = DynamicDataSourceManager.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        Object originalCache = cacheField.get(null);
        // 9.1 port note: this line's net.sf.ehcache.Cache is Joget's own JSR-107-backed wrapper
        // (wraps a real javax.cache.Cache), not the real Ehcache 2.x library 8.2 uses -- there is
        // no net.sf.ehcache.CacheManager class here at all. Constructs the fixture cache the same
        // way this test module's own TestPlatformCacheRegistrar#newFixtureCache(..) already does.
        javax.cache.CacheManager jsrManager = javax.cache.Caching.getCachingProvider().getCacheManager();
        String fixtureCacheName = "probeDatasourcePropsCache-2317-" + System.nanoTime();
        try {
            javax.cache.Cache jsrCache = jsrManager.getCache(fixtureCacheName);
            if (jsrCache == null) {
                jsrCache = jsrManager.createCache(fixtureCacheName,
                        new javax.cache.configuration.MutableConfiguration<Object, Object>());
            }
            net.sf.ehcache.Cache fixtureDatasourceCache = new net.sf.ehcache.Cache(jsrCache);
            // Sets the static field directly via reflection rather than DynamicDataSourceManager's
            // own setCache(..) instance method -- that method's only constructor also
            // unconditionally overwrites the OTHER static field (profilePropertyManager), which
            // this helper has no need to touch and must not disturb for other tests sharing this
            // JVM.
            cacheField.set(null, fixtureDatasourceCache);
            java.util.Properties fixtureProps = new java.util.Properties();
            fixtureProps.setProperty("adminProfile", adminProfileValue);
            // 9.1 port note: getProfilePropertiesLive() here populates its cache entry with the
            // RAW 2-arg JSR-107 put(key, value) shape (cache.put(getCacheKey(filename),
            // properties) -- a plain Properties value, no net.sf.ehcache.Element wrapper at all),
            // unlike 8.2's real Ehcache 2.x line, which always wraps in an Element. Matching that
            // exactly is required -- getProfilePropertiesLive()'s own cache-hit branch casts
            // straight to Properties, so an Element-wrapped entry here would ClassCastException.
            fixtureDatasourceCache.put(
                    DynamicDataSourceManager.getCacheKey(DynamicDataSourceManager.DATASOURCE_FILE), fixtureProps);
            body.run();
        } finally {
            cacheField.set(null, originalCache);
            jsrManager.destroyCache(fixtureCacheName);
        }
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    @Before
    public void setUp() {
        ManagedCacheRegistry.invalidateProfileListCacheForTest();
        HostManager.setCurrentProfile(null);
    }

    @After
    public void tearDown() {
        HostManager.setCurrentProfile(null);
    }

    /**
     * An ordinary colliding pair, where NEITHER
     * profile is a configured admin profile, is now DENIED in BOTH directions. Here "custShort2317"
     * and "custShort2317_eu" are both real (see {@link #createFixtureProfiles()}). The underscore key
     * space cannot tell the shorter profile's own key apart from the longer sibling's own key -- they
     * can be byte-identical (profile "custShort2317" + app "eu_..." builds the same string as profile
     * "custShort2317_eu") -- so neither profile may claim a key that could belong to the other. Each
     * colliding write is dropped/redirected to a miss and rebuilt from source, rather than one tenant
     * reading or overwriting the other's entry. An EARLIER revision granted both directions,
     * protecting only the admin profile; independent review showed that still left a real cross-tenant
     * read/overwrite between underscore-sibling tenants, so the denial was restored to both-direction
     * for every colliding pair (a durable fix is an unambiguous key encoding).
     */
    @Test
    public void testUnderscorePrefixLongestRealProfileMatchScopeDeniesBothDirectionsOnCollidingPairEvenWhenNeitherIsAdmin() throws Exception {
        String shorter = "custShort2317";
        String longer = "custShort2317_eu";

        // No admin profile configured -- the denial no longer depends on admin status at all; both
        // profiles are ordinary tenants here.
        withAdminProfile("", () -> {
            HostManager.setCurrentProfile(longer);
            try {
                String ownKey = longer + "_1_someForm";
                assertFalse("the longer profile's own naturally-built key must be DENIED when it "
                                + "collides with a shorter real sibling, even an ordinary (non-admin) "
                                + "one -- the key is ambiguous across the two profiles, so it becomes "
                                + "an uncached miss rather than a cross-tenant-readable entry",
                        ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE
                                .isCurrentTenantKey(ownKey));
            } finally {
                HostManager.setCurrentProfile(null);
            }

            HostManager.setCurrentProfile(shorter);
            try {
                String longerProfilesKey = longer + "_1_someForm";
                assertFalse("the shorter profile must ALSO be DENIED a key that collides with the "
                                + "longer sibling's namespace -- both-direction denial for every "
                                + "colliding pair, not just admin collisions",
                        ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE
                                .isCurrentTenantKey(longerProfilesKey));
            } finally {
                HostManager.setCurrentProfile(null);
            }
        });
    }

    /**
     * #2317 (ported from the 8.2 line): proves the one collision that IS still denied, in both
     * directions, using a comma-separated multi-admin-profile value (this deployment's
     * {@code "adminProfile"} property already supports more than one configured admin profile,
     * comma/space-separated -- confirmed handled correctly here). Manipulates the live {@code
     * "adminProfile"} property directly (same property/mechanism {@code
     * MultiTenantSecurityManager.isAdminProfile()} reads in the enterprise module) and restores it
     * in a {@code finally} block via {@link #withAdminProfile(String, ThrowingRunnable)}.
     */
    @Test
    public void testUnderscorePrefixLongestRealProfileMatchScopeDeniesCollidingClaimAgainstAdminProfile() throws Exception {
        String shorterAdmin = "adm2317short";
        String longerNonAdminExtendingShorterAdmin = shorterAdmin + "_eu";
        String longerAdmin = "adm2317long_eu";
        String shorterNonAdminExtendedByLongerAdmin = "adm2317long";
        createFixtureProfileFile(shorterAdmin);
        createFixtureProfileFile(longerNonAdminExtendingShorterAdmin);
        createFixtureProfileFile(longerAdmin);
        createFixtureProfileFile(shorterNonAdminExtendedByLongerAdmin);

        withAdminProfile(shorterAdmin + "," + longerAdmin, () -> {
            // Direction 1: a LONGER non-admin profile must NOT be able to write its own key when
            // it collides with a SHORTER real profile that is the configured admin.
            HostManager.setCurrentProfile(longerNonAdminExtendingShorterAdmin);
            try {
                String ownKey = longerNonAdminExtendingShorterAdmin + "_1_someForm";
                assertFalse("a non-admin tenant's own naturally-built key must be denied when it "
                                + "collides with the configured ADMIN profile's namespace, even "
                                + "though an equivalent collision against an ordinary tenant is "
                                + "now granted",
                        ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE
                                .isCurrentTenantKey(ownKey));
            } finally {
                HostManager.setCurrentProfile(null);
            }

            // Direction 2: a SHORTER non-admin profile must NOT be able to claim a key that
            // actually belongs to a LONGER real profile that is the configured admin.
            HostManager.setCurrentProfile(shorterNonAdminExtendedByLongerAdmin);
            try {
                String adminsKey = longerAdmin + "_1_someForm";
                assertFalse("a non-admin tenant must not be able to claim a key belonging to a "
                                + "LONGER real profile that is the configured ADMIN profile",
                        ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE
                                .isCurrentTenantKey(adminsKey));
            } finally {
                HostManager.setCurrentProfile(null);
            }

            // Sanity: the admin profile itself writing its own, non-colliding key is unaffected.
            HostManager.setCurrentProfile(shorterAdmin);
            try {
                String adminOwnKey = shorterAdmin + "_1_someForm";
                assertTrue("the admin profile's own naturally-built key must remain granted",
                        ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE
                                .isCurrentTenantKey(adminOwnKey));
            } finally {
                HostManager.setCurrentProfile(null);
            }
        });
    }

    /**
     * #2317 (ported from the 8.2 line, independent review, Opus): proves the fail-open
     * multi-admin-list parsing gap that review found is actually closed. A SPACE-only (no comma)
     * {@code "adminProfile"} value previously matched NEITHER configured admin at all (the whole
     * value was compared as one literal string), so a non-admin tenant colliding with either
     * admin profile was WRONGLY GRANTED the write, silently defeating this scope's one security
     * property.
     */
    @Test
    public void testUnderscorePrefixLongestRealProfileMatchScopeDeniesCollidingClaimAgainstSpaceSeparatedAdminList() throws Exception {
        String shorterAdmin = "adm2317space";
        String longerNonAdminExtendingShorterAdmin = shorterAdmin + "_eu";
        String otherAdmin = "adm2317spaceOther";
        createFixtureProfileFile(shorterAdmin);
        createFixtureProfileFile(longerNonAdminExtendingShorterAdmin);
        createFixtureProfileFile(otherAdmin);

        withAdminProfile(shorterAdmin + " " + otherAdmin, () -> {
            HostManager.setCurrentProfile(longerNonAdminExtendingShorterAdmin);
            try {
                String ownKey = longerNonAdminExtendingShorterAdmin + "_1_someForm";
                assertFalse("a SPACE-separated (no comma) multi-admin list must still be parsed "
                                + "correctly -- a non-admin tenant colliding with an admin profile "
                                + "listed only via a space separator must still be denied, not "
                                + "silently granted",
                        ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE
                                .isCurrentTenantKey(ownKey));
            } finally {
                HostManager.setCurrentProfile(null);
            }
        });
    }

    /**
     * #2317: the non-colliding case -- "tenantA" has no real sibling profile that is a prefix or
     * extension of it, so a key shaped like AppDefCache/LongTermCache's own
     * "&lt;profile&gt;_..." format is granted normally.
     */
    @Test
    public void testUnderscorePrefixLongestRealProfileMatchScopeGrantsNonCollidingOwnKey() {
        HostManager.setCurrentProfile("tenantA");
        try {
            String ownKey = "tenantA_myApp_1_someForm";
            assertTrue("a profile with no colliding real sibling must be granted its own key",
                    ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE
                            .isCurrentTenantKey(ownKey));
        } finally {
            HostManager.setCurrentProfile(null);
        }
    }

    @Test
    public void testFormOptionsCacheScopeRecognizesOwnKey() {
        HostManager.setCurrentProfile("tenantA");
        String ownKey = "FORMOPTIONS::tenantA::org.joget.plugin.enterprise.JdbcFormBinder::{}";
        assertTrue(ManagedCacheRegistry.FORM_OPTIONS_CACHE_SCOPE.isCurrentTenantKey(ownKey));
    }

    @Test
    public void testFormOptionsCacheScopeRejectsOtherTenantKey() {
        HostManager.setCurrentProfile("tenantA");
        String otherTenantKey = "FORMOPTIONS::tenantB::org.joget.plugin.enterprise.JdbcFormBinder::{}";
        assertFalse(ManagedCacheRegistry.FORM_OPTIONS_CACHE_SCOPE.isCurrentTenantKey(otherTenantKey));
    }

    @Test
    public void testFormOptionsCacheScopeRecognizesLastActiveVariantOwnKey() {
        // FormOptionsCacheAspect.updateLastActive(..) also writes "LAST_ACTIVE_" + getCacheKey(..)
        // -- the scope's own second real key shape (see its javadoc) -- via a direct cache.put(..)
        // with no try/catch. Must be recognized for the caller's own tenant, or a restricted
        // caller loading a form with a cached options binder hits an uncaught SecurityException on
        // ordinary, non-attack usage.
        HostManager.setCurrentProfile("tenantA");
        String lastActiveKey = "LAST_ACTIVE_FORMOPTIONS::tenantA::org.joget.plugin.enterprise.JdbcFormBinder::{}";
        assertTrue(ManagedCacheRegistry.FORM_OPTIONS_CACHE_SCOPE.isCurrentTenantKey(lastActiveKey));
    }

    @Test
    public void testFormOptionsCacheScopeRejectsLastActiveVariantOtherTenantKey() {
        HostManager.setCurrentProfile("tenantA");
        String otherTenantsLastActiveKey =
                "LAST_ACTIVE_FORMOPTIONS::tenantB::org.joget.plugin.enterprise.JdbcFormBinder::{}";
        assertFalse(ManagedCacheRegistry.FORM_OPTIONS_CACHE_SCOPE.isCurrentTenantKey(otherTenantsLastActiveKey));
    }

    /**
     * Regression test for a collision-safety bug in profileScope(..): its cross-check tested
     * whether another real profile started with {@code currentProfile + separator} IN FULL. For a
     * multi-character separator ("::"), a profile named "acme:" (one colon) builds a real key
     * "FORMOPTIONS::acme:::realsuffix" (its own "acme:" + "::" + suffix) -- which already starts
     * with "FORMOPTIONS::acme::" (caller "acme"'s own check prefix), so a caller running as plain
     * "acme" would satisfy the startsWith match, while checking whether "acme:".startsWith("acme::")
     * is false (only one colon where two were required), so the old check failed to flag the
     * collision. Keying the collision check off only the separator's first character catches this.
     * With the "acme"/"acme:" fixture profiles both present (see createFixtureProfiles()), caller
     * "acme" must NOT be able to claim a key shaped like "acme:"'s own real key.
     */
    @Test
    public void testFormOptionsCacheScopeRejectsPartialSeparatorCollision() {
        // On NTFS, a colon in a filename is an Alternate Data Stream separator, not a literal
        // character -- createFixtureProfileFile("acme:") silently fails to produce a real,
        // listable "acme:" profile there, so DynamicDataSourceManager.getProfileList() (which
        // profileScope(..)'s collision check relies on -- see ManagedCacheRegistry
        // .crossCheckProfileList()) can never see it. Skip on filesystems that can't represent
        // this fixture instead of failing on an environment gap unrelated to the logic under test.
        Assume.assumeTrue("\"acme:\" fixture profile is not listable on this filesystem (likely "
                + "NTFS colon/ADS reinterpretation) -- skipping collision regression check",
                DynamicDataSourceManager.getProfileList().contains("acme:"));
        HostManager.setCurrentProfile("acme");
        String victimsRealKey = "FORMOPTIONS::acme:::org.joget.plugin.enterprise.JdbcFormBinder::{}";
        assertFalse("caller \"acme\" must not be able to claim \"acme:\"'s own real key",
                ManagedCacheRegistry.FORM_OPTIONS_CACHE_SCOPE.isCurrentTenantKey(victimsRealKey));
    }

    @Test
    public void testUserviewMenuCacheScopeRecognizesOwnKey() {
        HostManager.setCurrentProfile("tenantA");
        String ownKey = "USERVIEW:tenantA:app1:userview1:menu1:scope1:key1:type1:{}";
        assertTrue(ManagedCacheRegistry.USERVIEW_MENU_CACHE_SCOPE.isCurrentTenantKey(ownKey));
    }

    @Test
    public void testUserviewMenuCacheScopeRejectsOtherTenantKey() {
        HostManager.setCurrentProfile("tenantA");
        String otherTenantKey = "USERVIEW:tenantB:app1:userview1:menu1:scope1:key1:type1:{}";
        assertFalse(ManagedCacheRegistry.USERVIEW_MENU_CACHE_SCOPE.isCurrentTenantKey(otherTenantKey));
    }

    /**
     * A prior version of this scope also recognized {@code "&lt;profile&gt;_dirLastModified_..."}
     * (via {@code anyOf(profileScope("USERVIEW:", ":"), LEGACY_UNDERSCORE_PREFIX_SCOPE)}), on the
     * mistaken belief that {@code AppDevUtil.dirLastModified(..)} writes this shape into the SAME
     * {@code userviewMenuCache} bean {@code UserviewCache} does. It actually reads {@code
     * "org.joget.cache.APP_CACHE"} -- a different, currently-unregistered cache -- so that
     * combinator had no real caller behind it and was removed. {@code USERVIEW_MENU_CACHE_SCOPE} is
     * {@code profileScope("USERVIEW:", ":")} alone now; this shape must be REJECTED even for the
     * caller's own tenant, confirming the combinator's removal actually took effect.
     */
    @Test
    public void testUserviewMenuCacheScopeRejectsDirLastModifiedShapeEvenForOwnTenant() {
        HostManager.setCurrentProfile("tenantA");
        String key = "tenantA_dirLastModified_myAppId";
        assertFalse(ManagedCacheRegistry.USERVIEW_MENU_CACHE_SCOPE.isCurrentTenantKey(key));
    }

    // setupManagerCache has NO KeyScope constant to test -- an earlier candidate scope
    // (profileScope("", ":")) exempted a restricted caller's put/remove against its own profile's
    // ENTIRE "<profile>:*" namespace in that cache, not just the two shapes it was meant to match,
    // reopening a real workflow-task-delegation/license-enforcement escalation. See
    // PlatformCacheRegistrar's own registration comment for the full reasoning; it stays
    // unconditionally blocked for every restricted caller instead.

    // No "current profile is null" case here: DynamicDataSourceManager.getCurrentProfile() falls
    // back to getProfileProperties() whenever HostManager's own profile is unset, which needs a
    // Spring-wired cache this bare unit test never initializes (NullPointerException, unrelated to
    // this scope's own logic) -- a pre-existing limitation of getCurrentProfile() itself, shared by
    // every KeyScope in this class, not something specific to these three. The null/empty-profile
    // guard below is the same pattern LEGACY_UNDERSCORE_PREFIX_SCOPE already uses.

    @Test
    public void testBothScopesRejectNonStringKeys() {
        HostManager.setCurrentProfile("tenantA");
        Object nonStringKey = new Object();
        assertFalse(ManagedCacheRegistry.FORM_OPTIONS_CACHE_SCOPE.isCurrentTenantKey(nonStringKey));
        assertFalse(ManagedCacheRegistry.USERVIEW_MENU_CACHE_SCOPE.isCurrentTenantKey(nonStringKey));
    }
}
