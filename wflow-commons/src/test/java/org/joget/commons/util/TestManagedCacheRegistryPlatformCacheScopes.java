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
 * FORM_OPTIONS_CACHE_SCOPE/USERVIEW_MENU_CACHE_SCOPE were added because userviewMenuCache/
 * formOptionsCache were registered with LEGACY_UNDERSCORE_PREFIX_SCOPE even though neither cache's
 * real key builders use a leading "&lt;profile&gt;_" prefix (confirmed by direct source read of
 * FormOptionsCacheAspect.getCacheKey(..)/UserviewCache.getCacheKey(..)), so a restricted caller's
 * own genuinely self-scoped put/remove against either was unconditionally blocked. This matters
 * more once MultiTenantPluginAspect's ambient-admin-profile widening (jw-enterprise/cloud-wfengine)
 * makes a wider set of admin-profile threads count as "restricted" than before -- without a
 * correctly-matching scope, a legitimate admin-profile mutation to either cache would newly hit the
 * same unconditional block a non-admin tenant already did. setupManagerCache has no such scope
 * and stays unconditionally blocked -- see the "setupManagerCache has NO KeyScope constant"
 * comment below for why.
 *
 * <p>The collision-safety cases below need DynamicDataSourceManager.getProfileList() to return a
 * real, controlled list rather than null (which it does whenever its backing directory does not
 * exist, making every collision check fail closed regardless of whether the logic under test is
 * actually correct). SetupManager.BASE_DIRECTORY is `static final`, resolved once at class load
 * from the wflow.home system property, so it cannot be redirected via surefire config without
 * breaking OTHER tests in this module (e.g. TestStringUtil) that load a real Spring
 * ApplicationContext depending on the real ~/wflow content. Instead this class adds a
 * few fixture app_datasource-*.properties files directly into the REAL shared directory
 * (whatever SetupManager.getBaseSharedDirectory() already resolves to on this machine) and removes
 * only the files it created, in {@link #createFixtureProfiles()}/{@link #removeFixtureProfiles()}.
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
        // UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE's own colliding pair -- "custShort2317"
        // is a real, literal prefix of "custShort2317_eu". Neither is a configured admin profile (no
        // test in this class sets the "adminProfile" property to either), so this pair proves an
        // ordinary tenant-vs-tenant collision is denied in BOTH directions -- see
        // testUnderscorePrefixLongestRealProfileMatchScopeDeniesBothDirectionsOnCollidingPairEvenWhenNeitherIsAdmin.
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

    /**
     * Runs {@code body} with {@code DynamicDataSourceManager}'s private static {@code cache} field
     * (normally Spring-wired via its {@code setCache(Cache)} instance method) temporarily replaced
     * by a fresh, empty fixture {@code net.sf.ehcache.Cache} pre-populated with a {@code
     * "adminProfile"} value at the exact cache key {@code getProfilePropertiesLive()} (used by
     * {@code ManagedCacheRegistry.isConfiguredAdminProfile(..)}) reads -- pre-populating that one
     * entry means {@code getProfilePropertiesLive()}'s cache-miss branch (which would otherwise
     * need a real {@code DatasourceProfilePropertyManager} AND a real {@code
     * app_datasource.properties} file, neither wired in this plain, non-Spring test class) is never
     * reached. Reflection, not the public setter, so the ORIGINAL field value (whatever this JVM's
     * other tests left it as -- {@code null} unless a Spring-context test ran first) is captured
     * and restored exactly, rather than merely set back to {@code null} unconditionally.
     */
    // Not safe for concurrent invocation (swaps a JVM-wide static field) -- assumes this class
    // (and any other test touching DynamicDataSourceManager's static state) runs single-threaded,
    // the default JUnit4/surefire execution model this module uses; matches the same assumption
    // every HostManager.setCurrentProfile(..)-based test in this class already makes.
    private static void withAdminProfile(String adminProfileValue, ThrowingRunnable body) throws Exception {
        java.lang.reflect.Field cacheField = DynamicDataSourceManager.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        Object originalCache = cacheField.get(null);
        net.sf.ehcache.CacheManager manager = net.sf.ehcache.CacheManager.getInstance();
        String fixtureCacheName = "probeDatasourcePropsCache-2317-" + System.nanoTime();
        try {
            // Independent-review correction (Sol): the swap, population, AND body must all be
            // inside this try -- an earlier version populated the fixture cache BEFORE entering
            // the try, so a failure in that setup (cache creation, property population) would
            // leave the static field pointed at the fixture permanently, never restored.
            if (!manager.cacheExists(fixtureCacheName)) {
                manager.addCache(fixtureCacheName);
            }
            net.sf.ehcache.Cache fixtureDatasourceCache = manager.getCache(fixtureCacheName);
            // Sets the static field directly via reflection rather than DynamicDataSourceManager's
            // own setCache(..) instance method -- that method's only constructor also
            // unconditionally overwrites the OTHER static field (profilePropertyManager), which
            // this helper has no need to touch and must not disturb for other tests sharing this
            // JVM.
            cacheField.set(null, fixtureDatasourceCache);
            java.util.Properties fixtureProps = new java.util.Properties();
            fixtureProps.setProperty("adminProfile", adminProfileValue);
            fixtureDatasourceCache.put(new net.sf.ehcache.Element(
                    DynamicDataSourceManager.getCacheKey(DynamicDataSourceManager.DATASOURCE_FILE), fixtureProps));
            body.run();
        } finally {
            cacheField.set(null, originalCache);
            // Independent-review correction (Sol): the fixture cache was never removed from the
            // shared CacheManager, leaking one Ehcache instance per test invocation for the life
            // of the JVM. The nanoTime()-suffixed name already made this harmless (never reused,
            // never collided), but still worth not leaking.
            if (manager.cacheExists(fixtureCacheName)) {
                manager.removeCache(fixtureCacheName);
            }
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
    public void testFormOptionsCacheScopeRejectsLastActiveVariant() {
        // FormOptionsCacheAspect also writes "LAST_ACTIVE_" + getCacheKey(..) -- a different shape
        // this scope deliberately does not recognize (see its own javadoc); confirms that
        // narrower-than-`contains` matching, not an accident.
        HostManager.setCurrentProfile("tenantA");
        String lastActiveKey = "LAST_ACTIVE_FORMOPTIONS::tenantA::org.joget.plugin.enterprise.JdbcFormBinder::{}";
        assertFalse(ManagedCacheRegistry.FORM_OPTIONS_CACHE_SCOPE.isCurrentTenantKey(lastActiveKey));
    }

    /**
     * Regression test for a partial-separator collision: the collision-safety check used to test
     * whether another real profile started with {@code currentProfile + separator} IN FULL. For a
     * multi-character separator ("::"), a profile named "acme:" (one colon) builds a real key
     * "FORMOPTIONS::acme:::realsuffix" (its own "acme:" + "::" + suffix) -- which already starts
     * with "FORMOPTIONS::acme::" (caller "acme"'s own check prefix), so a caller running as plain
     * "acme" would satisfy that startsWith match, while the old collision loop tested whether
     * "acme:".startsWith("acme::") -- false, only one colon where two were required -- and so
     * failed to flag the collision. Keying the collision check off only the separator's first
     * character fixes this. With the "acme"/"acme:" fixture profiles both present (see
     * createFixtureProfiles()), caller "acme" must NOT be able to claim a key shaped like "acme:"'s
     * own real key.
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
     * userviewMenuCache has a SECOND real caller besides UserviewCache -- AppDevUtil
     * .dirLastModified(..) builds "&lt;profile&gt;_dirLastModified_&lt;appDef&gt;", the SAME
     * "&lt;profile&gt;_" convention {@link ManagedCacheRegistry#UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE}
     * recognizes. Registering userviewMenuCache with only the "USERVIEW:" scope would newly block
     * this caller's own genuinely self-scoped put, so USERVIEW_MENU_CACHE_SCOPE must recognize
     * both shapes.
     */
    @Test
    public void testUserviewMenuCacheScopeRecognizesAppDevUtilDirLastModifiedKey() {
        HostManager.setCurrentProfile("tenantA");
        String ownKey = "tenantA_dirLastModified_myAppId";
        assertTrue(ManagedCacheRegistry.USERVIEW_MENU_CACHE_SCOPE.isCurrentTenantKey(ownKey));
    }

    @Test
    public void testUserviewMenuCacheScopeRejectsOtherTenantDirLastModifiedKey() {
        HostManager.setCurrentProfile("tenantA");
        String otherTenantKey = "tenantB_dirLastModified_myAppId";
        assertFalse(ManagedCacheRegistry.USERVIEW_MENU_CACHE_SCOPE.isCurrentTenantKey(otherTenantKey));
    }

    /**
     * Regression for a live-reported gap (#2317/#2261, independent review Opus): {@code
     * USERVIEW_MENU_CACHE_SCOPE} originally unioned {@code profileScope("USERVIEW:", ":")} with
     * {@code LEGACY_UNDERSCORE_PREFIX_SCOPE} -- the SAME scope {@code fluCache}/{@code
     * longTermCacheObject} were fixed away from elsewhere in this class, for the identical reason:
     * {@code AppDevUtil.dirLastModified(..)} is a real, live caller of THIS bean (confirmed by
     * direct source read: {@code getBean("userviewMenuCache")} then {@code cache.put(..)},
     * reachable whenever {@code !AppDevUtil.isGitDisabled()}), so a {@code "jwc_"}-style tenant's
     * own genuine {@code dirLastModified} key was ALSO unconditionally denied -- even after the
     * fluCache/longTermCacheObject fix, this one call site would still have broken for such a
     * tenant. Fixed by switching the union to {@code UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE}.
     */
    @Test
    public void testUserviewMenuCacheScopeRecognizesJwcStyleTenantsDirLastModifiedKey() {
        HostManager.setCurrentProfile("jwc_fatinrakib16");
        String ownKey = "jwc_fatinrakib16_dirLastModified_myAppId";
        try {
            assertFalse("the OLD union member (LEGACY_UNDERSCORE_PREFIX_SCOPE) must still deny "
                            + "ANY underscore-containing profile's own key -- confirms the "
                            + "regression this test guards against is real",
                    ManagedCacheRegistry.LEGACY_UNDERSCORE_PREFIX_SCOPE.isCurrentTenantKey(ownKey));
            assertTrue("USERVIEW_MENU_CACHE_SCOPE must recognize a \"jwc_\"-style tenant's own "
                            + "genuine dirLastModified key",
                    ManagedCacheRegistry.USERVIEW_MENU_CACHE_SCOPE.isCurrentTenantKey(ownKey));
        } finally {
            HostManager.setCurrentProfile(null);
        }
    }

    // setupManagerCache has NO KeyScope constant to test -- a first-attempt profileScope("", ":")
    // would exempt a restricted caller's put/remove against its own profile's ENTIRE
    // "<profile>:*" namespace in that cache, not just the two shapes it was meant to match,
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

    /**
     * Regression for a live-reported gap (#2317/#2261): {@code PlatformCacheRegistrar} registered
     * {@code fluCache}/{@code longTermCacheObject} (the caches {@code AppDefCache}/{@code
     * LongTermCache} are wired to, purely via Spring XML on this line -- see
     * {@code appsApplicationContext.xml}'s {@code appFluCache} bean) with {@code
     * LEGACY_UNDERSCORE_PREFIX_SCOPE}, which unconditionally denies the self-scoped exemption
     * whenever the CALLING tenant's own profile name contains {@code "_"} -- common in practice,
     * and this deployment's own tenant-naming convention is literally {@code "jwc_<name>"}.
     * Confirmed live: a tenant named this way had every {@code AppDefCache.put(..)} denied
     * outright (observed via {@code PluginDefaultPropertiesDaoImpl.loadById(..)}, reached from
     * ordinary hash-variable processing), not narrowed to a genuine collision -- denied
     * unconditionally, for every such tenant. Fixed by switching the registration to {@code
     * UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE} (ported from the 9.1 line, where the
     * identical defect was independently closed a different way -- see that scope's own javadoc).
     *
     * <p>This test drives the two {@link ManagedCacheRegistry.KeyScope} constants directly
     * (matching every other test in this class), not {@code PlatformCacheRegistrar} itself (its
     * registration map is private and only populated at Spring {@code ContextRefreshedEvent}
     * time) -- it proves the defect class this fix closes: the OLD scope denies a "{@code
     * jwc_}"-shaped tenant's own genuine key outright, while the NEW one (what {@code
     * PlatformCacheRegistrar} now actually registers both caches with) correctly recognizes it.
     */
    @Test
    public void testUnderscorePrefixLongestRealProfileMatchScopeRecognizesJwcStyleTenantsOwnKey() {
        HostManager.setCurrentProfile("jwc_fatinrakib16");
        String ownKey = "jwc_fatinrakib16_someAppId_1_PDP_someId";

        assertFalse("the OLD scope this fix replaces must still (by design, unrelated to this "
                        + "fix) unconditionally deny ANY underscore-containing profile's own key "
                        + "-- confirms the regression this test guards against is real",
                ManagedCacheRegistry.LEGACY_UNDERSCORE_PREFIX_SCOPE.isCurrentTenantKey(ownKey));
        assertTrue("the NEW scope PlatformCacheRegistrar now registers fluCache/longTermCacheObject "
                        + "with must recognize a \"jwc_\"-style tenant's own genuine key",
                ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE.isCurrentTenantKey(ownKey));
    }

    /**
     * Companion to the test above: a key that does not even start with {@code "<currentProfile>_"}
     * is rejected before the collision loop is ever reached -- the cheap, unambiguous case.
     * (Independent review, Opus: an EARLIER version of this test used two "jwc_"-prefixed
     * profiles for both the caller and the other tenant's key, e.g. caller {@code
     * "jwc_fatinrakib16"} against key {@code "jwc_someoneelse_..."} -- that key fails the leading
     * {@code startsWith(currentProfile + "_")} check and returns {@code false} before the
     * collision loop is ever entered, so it tested nothing this fix's own collision logic added;
     * the two tests below, ported from the 9.1 line, are what actually exercise the loop.)
     */
    @Test
    public void testUnderscorePrefixLongestRealProfileMatchScopeRejectsKeyWithDifferentPrefix() {
        HostManager.setCurrentProfile("jwc_fatinrakib16");
        String otherTenantKey = "jwc_someoneelse_someAppId_1_PDP_someId";
        assertFalse(ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE.isCurrentTenantKey(otherTenantKey));
    }

    /**
     * Direct, AspectJ/BeanShell-free proof that
     * an ordinary colliding pair, where NEITHER profile is a configured admin profile, is now DENIED
     * in BOTH directions. "custShort2317" and "custShort2317_eu" are both real (see {@link
     * #createFixtureProfiles()}); the underscore key space cannot tell the shorter profile's own key
     * apart from the longer sibling's own key (they can be byte-identical), so neither may claim a
     * key that could belong to the other -- each colliding write is dropped/redirected to a miss and
     * rebuilt. An earlier revision granted both directions (protecting only the admin profile);
     * review showed that still left a real cross-tenant read/overwrite between underscore-sibling
     * tenants, so the denial was restored to both-direction for every colliding pair (durable fix: an
     * unambiguous key encoding).
     */
    @Test
    public void testUnderscorePrefixLongestRealProfileMatchScopeDeniesBothDirectionsOnCollidingPairEvenWhenNeitherIsAdmin() throws Exception {
        String shorter = "custShort2317";
        String longer = "custShort2317_eu";

        // No admin profile configured at all -- withAdminProfile(..) is needed only because
        // isConfiguredAdminProfile(..) (consulted whenever a real collision is found) reads
        // DynamicDataSourceManager's cache, which this plain, non-Spring test class never
        // otherwise initializes; an empty value means "no admin profile", matching this test's
        // own premise.
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
     * #2317/#2261 SECOND follow-up (explicit user decision, prompted by independent Sol/Opus
     * findings that the FIRST follow-up's "longer profile always wins" relaxation was a genuine,
     * deliberately-exploitable spoofing vector against a real sibling's {@code
     * longTermCacheObject}/{@code ResourceBundleMessageDao} entries -- not merely an accidental
     * collision -- see this scope's own javadoc): proves the one collision that IS still denied --
     * a non-admin profile colliding with the deployment's CONFIGURED ADMIN PROFILE -- in both
     * directions, using two separate colliding pairs so each direction is unambiguous. Manipulates
     * the live {@code "adminProfile"} property directly (same property/mechanism {@code
     * MultiTenantSecurityManager.isAdminProfile()} reads in the enterprise module) and restores it
     * in a {@code finally} block.
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
     * #2317/#2261 THIRD follow-up (independent review, Opus round 3): proves the fail-open
     * multi-admin-list parsing gap that review found is actually closed. A SPACE-only (no comma)
     * {@code "adminProfile"} value -- e.g. {@code "adm2317short adm2317long"} -- previously matched
     * NEITHER configured admin at all (the whole value was compared as one literal string), so a
     * non-admin tenant colliding with either admin profile was WRONGLY GRANTED the write, silently
     * defeating this entire follow-up's one security property. Reuses the exact same fixture
     * profiles and collision shape as {@link
     * #testUnderscorePrefixLongestRealProfileMatchScopeDeniesCollidingClaimAgainstAdminProfile()}
     * above, only the separator in the configured value differs.
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
     * #2317/#2261 (ported verbatim from the 9.1 line): the non-colliding case -- "tenantA" has no
     * real sibling profile that is a prefix or extension of it, so a key shaped like
     * AppDefCache/LongTermCache's own "&lt;profile&gt;_..." format is granted normally, proving
     * the collision loop's absence-of-collision path (every iteration falls through the loop's
     * {@code return false} branch, which as of the second #2317 follow-up only fires when the
     * colliding profile is the configured admin profile -- "tenantA" has no colliding sibling at
     * all here, admin or otherwise) reaches the trailing {@code return true}.
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
}
