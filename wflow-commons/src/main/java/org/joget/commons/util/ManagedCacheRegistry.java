package org.joget.commons.util;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.sf.ehcache.Ehcache;

/**
 * Lets multi-tenant security code (in the Cloud/Enterprise edition) recognize a set of the
 * platform's own shared {@code Ehcache} instances without ever handing out a reference to them,
 * the same purpose {@link DynamicDataSourceManager#isManagedCache(Object)} and {@link
 * SetupManagerCache#isManagedCacheManager(Object)} already serve for their own single managed
 * object -- this one is a registry rather than a single field because more than one class needs
 * to register a cache it owns: {@code
 * SecureDataEncryptionImpl}'s decryption cache is also reachable directly as the plain {@code
 * nonceCache} Spring bean under a different name than the {@code dataEncryption} bean that owns
 * it, and {@code FormDataDaoImpl}'s three {@code SF_CACHE}-backed fields are equally reachable
 * directly as their own plain Spring beans -- both bypass the existing {@code cacheManager}/
 * {@code DataEncryption} guards entirely, which only recognize the specific bean used to reach
 * them, not the underlying cache object itself. Registering the identity here lets {@link
 * org.joget.plugin.base.MultiTenantPluginAspect}'s existing {@code Cache.put}/{@code remove}/
 * {@code get}/{@code getQuiet} guards recognize these caches too, regardless of which bean
 * reached them.
 *
 * <p><b>Two separate tiers, not one.</b> Every registered cache gets the mutation/bulk-read guard
 * -- poisoning or enumerating another tenant's entries is never legitimate for any of these
 * caches. But an outright block on a plain, single-key {@code get()}/{@code getQuiet()} is only
 * correct where the READ ITSELF is the harm, which is why {@link #registerReadSensitive(Ehcache)}
 * is separate from plain {@link #register(Ehcache)}: {@code SecureDataEncryptionImpl}'s
 * decryption cache (already-decrypted plaintext secrets) and {@code FormDataDaoImpl}'s {@code
 * SF_CACHE} (live {@code SessionFactory}/{@code PersistentClass} objects) use the read-sensitive
 * form. {@code PlatformCacheRegistrar}'s platform beans (application definitions, rendered CSS,
 * form options, message bundles) are ordinary content every tenant's own routine platform code
 * reads as part of normal operation (e.g. {@code AppDefCache}/{@code FormDefinitionDaoImpl}
 * reading {@code fluCache}) -- they use the plain form only, so single-key reads stay unguarded
 * while mutation/enumeration stays blocked. Registering them read-sensitive instead would make
 * ordinary, legitimate non-admin-tenant form/datalist/userview loading throw {@code
 * SecurityException} the moment any OSGi plugin frame is on the stack.
 */
public final class ManagedCacheRegistry {

    /**
     * Answers whether a given key, read from a cache registered with this scope via
     * {@link #register(Ehcache, KeyScope)}, belongs to the CURRENTLY active tenant profile.
     * Implementations decide this from the key's own content/shape -- they must never trust
     * anything other than the key itself (the calling code is, by construction, exactly the
     * untrusted-caller case this exists to gate). Registered per cache, by the cache's OWNER
     * class, at the point it registers the cache -- not guessed centrally from the key's shape by
     * {@code MultiTenantPluginAspect}, which has no reliable way to tell one cache's key
     * convention from another's and historically got exactly one shape (a leading
     * {@code profile + "_"} prefix) right for some caches and silently wrong for others.
     */
    public interface KeyScope {
        boolean isCurrentTenantKey(Object key);

        /**
         * #2261: answers whether a BULK removal by this literal prefix (every key
         * {@code k} such that {@code k.startsWith(prefix)}) is provably scoped to the CURRENTLY
         * active tenant profile alone -- a stricter question than {@link #isCurrentTenantKey(Object)}
         * answers for one specific key, since a single call could remove every matching entry in
         * the whole cache in one shot, not just the one key a caller happened to name. Default
         * {@code false}: a scope that only ever answers the single-key question must not silently
         * grant bulk-removal-by-prefix just because a caller started asking this question too --
         * each scope opts in explicitly, the same way {@link #register(Object, KeyScope)} itself
         * only ever grants what a scope actually implements.
         */
        default boolean isCurrentTenantKeyPrefix(String prefix) {
            return false;
        }

        /**
         * Builds the key the CURRENT tenant would use for {@code localKey} under this scope's own
         * convention, or {@code null} if this scope has none (the default) or the current profile
         * cannot be safely scoped. Used to move a restricted caller's raw (unscoped) key into its own
         * tenant's key space instead of refusing or dropping the call; callers must still confirm the
         * result with {@link #isCurrentTenantKey(Object)} (see
         * {@link ManagedCacheRegistry#currentTenantKeyFor(Object, Object)}), so a tenant is only ever
         * redirected to a key it could already have used itself.
         */
        default Object keyForCurrentTenant(Object localKey) {
            return null;
        }

        /**
         * The keys the current tenant would use for {@code localKey} under this scope, built
         * WITHOUT the ownership check, so cheap enough for a cache read's hot path (the check may
         * list every profile). Anything found under one of them must still be confirmed with
         * {@link #isCurrentTenantKey(Object)} before it is handed out. Defaults to
         * {@link #keyForCurrentTenant(Object)}, which no single-convention scope here checks itself.
         */
        default java.util.List<Object> candidateKeysForCurrentTenant(Object localKey) {
            Object key = keyForCurrentTenant(localKey);
            return (key == null) ? java.util.Collections.<Object>emptyList() : java.util.Collections.singletonList(key);
        }
    }

    /**
     * Matches the legacy {@code "<profile>_"} prefix convention already used by
     * {@code AppDefCache}/{@code FormDefinitionDaoImpl} and this platform's other pre-existing
     * {@code "_"}-prefixed cache key builders (confirmed by direct source read against
     * {@code AppDefCache.remove(..)}). New callers should prefer {@link ProfileScopedCacheKey}
     * instead of adopting this shape, which requires the same "profile name must not itself
     * contain the separator" fail-closed care {@link ProfileScopedCacheKey} handles by
     * construction.
     *
     * <p>#2317/#2261 (independent review, Opus): no cache is registered with this scope any more,
     * directly OR as part of a union -- {@code fluCache}/{@code longTermCacheObject} (see {@code
     * PlatformCacheRegistrar}) were switched to {@link #UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE},
     * which matches the identical key shape without this scope's own blanket {@code
     * currentProfile.contains("_")} rejection (see that scope's own javadoc for the live incident
     * this closed); {@link #USERVIEW_MENU_CACHE_SCOPE} was LATER switched to union with that same
     * new scope too (see its own javadoc), not with this one -- a prior version of this comment
     * said this scope "survives only as one half of {@code USERVIEW_MENU_CACHE_SCOPE}'s {@code
     * anyOf(..)}", which independent review (Opus, round 3) found to be stale: a repo-wide search
     * confirms every remaining reference to this constant is in test code only. Kept, not removed,
     * only because the test suite still exercises it directly as a deliberate "what the old,
     * broken behavior looked like" comparison baseline.
     */
    public static final KeyScope LEGACY_UNDERSCORE_PREFIX_SCOPE = new KeyScope() {
        @Override
        public Object keyForCurrentTenant(Object localKey) {
            return underscorePrefixedKey(localKey);
        }

        @Override
        public boolean isCurrentTenantKey(Object key) {
            if (!(key instanceof String)) {
                return false;
            }
            String currentProfile = DynamicDataSourceManager.getCurrentProfile();
            if (currentProfile == null || currentProfile.isEmpty() || currentProfile.contains("_")
                    || !((String) key).startsWith(currentProfile + "_")) {
                return false;
            }
            // A leading "<profile>_" match alone is ambiguous: if another real profile is itself
            // a "_"-delimited extension of this one (e.g. "acme" vs "acme_dev"), a key starting
            // with "acme_" could genuinely belong to "acme_dev" instead. Checking the CURRENT
            // profile's own name for "_" (above) only protects a longer-named profile from being
            // impersonated by a shorter one that already contains "_" -- it does nothing to stop
            // the shorter-named profile itself from claiming a longer one's key. Cross-checked
            // against every other known profile here; fails closed (denies the exemption) both
            // when a collision is possible AND when the profile list itself cannot be read, rather
            // than trusting the shorter name's claim over an unverifiable one. None of this scope's
            // remaining callers are read-scoped (see PlatformCacheRegistrar), so the filesystem cost
            // of DynamicDataSourceManager.getProfileList() is paid on Cache.put/remove, and on a read
            // only when a restricted caller's raw key misses or finds its own redirected entry
            // (MultiTenantPluginAspect.managedCacheRegistryReadGuardCheck), never on an ordinary hit.
            java.util.List<String> profiles = crossCheckProfileList();
            if (profiles == null) {
                return false;
            }
            for (String profile : profiles) {
                if (!currentProfile.equals(profile) && profile.startsWith(currentProfile + "_")) {
                    return false;
                }
            }
            return true;
        }
    };

    /**
     * Scope for the {@code "<profile>_..."} key shape (AppDefCache / LongTermCache and third-party
     * plugins that reuse it), like {@link #LEGACY_UNDERSCORE_PREFIX_SCOPE} but without its blanket
     * rejection of a current profile whose own name contains {@code "_"} -- Joget Cloud tenant names are
     * often underscore-separated database names (e.g. {@code customer_eu}).
     *
     * <p>A key is the current tenant's own only if it starts with {@code "<currentProfile>_"} AND no
     * OTHER real profile (from {@link #crossCheckProfileList()}) collides with it in either direction
     * (that profile is an at-least-as-long {@code "<profile>_"} prefix of the key, or the current profile
     * is itself an underscore-extension of that shorter profile). ANY such collision is denied: the
     * underscore key space cannot tell {@code "<me>_<localkey>"} apart from a sibling's own key -- profile
     * {@code acme} + app {@code eu_sales} and profile {@code acme_eu} + app {@code sales} both build the
     * byte-identical {@code "acme_eu_sales_..."} -- so a key that could belong to another real profile is
     * never claimed as the current tenant's own; both siblings lose caching for that key (each rebuilds
     * from source) rather than one reading or overwriting the other's entry. (Fully separating such keys
     * needs an unambiguous key encoding across all writers and guards; this both-direction denial is the
     * fail-closed interim, matching {@link #isCurrentTenantKeyPrefix(String)}.)
     *
     * <p>Does not require the current profile itself to appear in the cross-check list (a just-created
     * profile or a test fixture may not be enumerated yet) -- it only checks for OTHER colliding names;
     * fails closed if the list cannot be read. The scope name is a historical artifact of an earlier
     * "longest match wins" design; read this javadoc, not the name, for the behavior.
     */
    public static final KeyScope UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE = new KeyScope() {
        @Override
        public Object keyForCurrentTenant(Object localKey) {
            return underscorePrefixedKey(localKey);
        }

        @Override
        public boolean isCurrentTenantKey(Object key) {
            if (!(key instanceof String)) {
                return false;
            }
            String currentProfile = DynamicDataSourceManager.getCurrentProfile();
            if (currentProfile == null || currentProfile.isEmpty()
                    || !((String) key).startsWith(currentProfile + "_")) {
                return false;
            }
            java.util.List<String> profiles = crossCheckProfileList();
            if (profiles == null) {
                return false;
            }
            String keyStr = (String) key;
            for (String profile : profiles) {
                if (profile == null || profile.equals(currentProfile)) {
                    continue;
                }
                boolean collides = profile.length() >= currentProfile.length()
                        ? keyStr.startsWith(profile + "_")
                        : currentProfile.startsWith(profile + "_");
                // Deny on ANY collision with another real profile, in EITHER direction, admin or
                // ordinary tenant alike. The underscore key space cannot tell "<me>_<localkey>"
                // apart from a sibling's own key -- profile "acme" + app "eu_sales" and profile
                // "acme_eu" + app "sales" both build the byte-identical "acme_eu_sales_..." -- so a
                // key that could belong to another real profile is never claimed as the current
                // tenant's own. The caller then gets a MISS (read redirected to its own, empty key
                // space) or a dropped/redirected write, never another profile's value. Both
                // colliding siblings lose caching for the ambiguous key (each rebuilds from source);
                // fully closing it needs an unambiguous key encoding. This
                // matches isCurrentTenantKeyPrefix(String)'s own both-direction rule.
                if (collides) {
                    return false;
                }
            }
            return true;
        }

        /**
         * #2261: unlike {@link #isCurrentTenantKey(Object)} above -- which accepts an ordinary
         * tenant-vs-tenant collision as a deliberate, already-reviewed residual risk for a single
         * put/remove, protecting only the configured ADMIN profile from collision -- a bulk
         * removal by prefix can wipe every one of a colliding tenant's matching entries in one
         * call, a materially larger blast radius than one key at a time. So this is deliberately
         * STRICTER: denied whenever ANY other real profile's own key-space could possibly overlap
         * with {@code prefix}, not only when that other profile is the admin profile.
         *
         * <p>Collision test operates on the FULL {@code prefix} (including whatever
         * caller-supplied local suffix it carries), not just the profile-name boundary: for each
         * other real profile {@code P}, compares {@code prefix} directly against {@code P}'s own
         * key-space boundary ({@code P + "_"}), in BOTH directions --
         * {@code prefix.startsWith(P + "_")} (my prefix already reaches into {@code P}'s
         * key-space) OR {@code (P + "_").startsWith(prefix)} (my prefix is itself a boundary every
         * one of {@code P}'s keys would satisfy). Either direction means the two key-spaces are
         * not provably disjoint, so the removal is refused. A profile-NAME-only comparison (does
         * {@code P} collide with {@code currentProfile}, ignoring the actual prefix) would both
         * miss real collisions (when {@code currentProfile} is itself an extension of a shorter
         * real {@code P} -- e.g. {@code currentProfile = "acme_en"}, {@code P = "acme"}: the full
         * prefix {@code "acme_en_" + localPrefix} can still fall inside {@code P}'s own key-space,
         * since some caches' real key shape is {@code profile + "_" + freeFormContent}, e.g. a
         * locale) and over-deny harmless ones (a legitimate, narrowly-suffixed caller like {@code
         * FormColumnCache.clear()}'s {@code "acme_FORM_COLUMNS_"} does not actually collide with a
         * real sibling {@code "acme_dev"}'s own boundary {@code "acme_dev_"} in either direction,
         * even though the bare profile names would).
         */
        @Override
        public boolean isCurrentTenantKeyPrefix(String prefix) {
            if (prefix == null || prefix.isEmpty()) {
                return false;
            }
            String currentProfile = DynamicDataSourceManager.getCurrentProfile();
            if (currentProfile == null || currentProfile.isEmpty()
                    || !prefix.startsWith(currentProfile + "_")) {
                return false;
            }
            java.util.List<String> profiles = crossCheckProfileList();
            if (profiles == null) {
                return false;
            }
            for (String profile : profiles) {
                if (profile == null || profile.equals(currentProfile)) {
                    continue;
                }
                String otherBoundary = profile + "_";
                if (prefix.startsWith(otherBoundary) || otherBoundary.startsWith(prefix)) {
                    return false;
                }
            }
            return true;
        }
    };

    /**
     * Builds a {@link KeyScope} for the "{@code <literalPrefix><profile><separator>...}" shape --
     * generalizes {@link #LEGACY_UNDERSCORE_PREFIX_SCOPE}'s own collision-safety reasoning (a
     * profile whose name is itself a {@code <separator>}-delimited extension of another real
     * profile must not be able to claim that other profile's key) to the several distinct
     * literal-prefix/separator conventions this platform's own cache key builders actually use.
     * Each caller below matches one specific cache's real key shape, confirmed by direct source
     * read of its key-building method (see {@code PlatformCacheRegistrar}'s registration comment
     * for the caller and shape each one corresponds to).
     *
     * <p>The safety checks (the current profile must not itself be ambiguous; no other real
     * profile may collide with it) key off only the separator's FIRST character, not the full
     * separator string, even for a multi-character separator like {@code "::"}. Checking the full
     * string is not conservative enough: with separator {@code "::"}, a profile literally named
     * {@code "acme:"} builds a real key {@code "acme:" + "::" + rest} = {@code "acme:::rest"},
     * which already starts with {@code "acme" + "::"} -- so a caller running as plain {@code
     * "acme"} would satisfy the {@code startsWith} match, and the OLD full-separator collision
     * loop (testing {@code profile.startsWith(currentProfile + separator)}, i.e. does "acme:"
     * start with "acme::") would fail to flag "acme:" as a collision, since "acme:" has only ONE
     * colon where two are required. Keying the guard off the separator's first character alone
     * (denying "acme" the exemption whenever ANY real profile starts with "acme:", one colon) is
     * strictly more conservative -- it can only ever deny an exemption this method would otherwise
     * have granted, never grant one it should not, so it cannot introduce a false positive; the
     * cost is a legitimately-named profile containing that character losing the self-scoped
     * exemption entirely (falls back to the pre-existing unconditional block, not a security
     * regression, just a missed optimization for an unusually-named profile).
     */
    private static KeyScope profileScope(final String literalPrefix, final String separator) {
        final char separatorGuardChar = separator.charAt(0);
        return new KeyScope() {
            @Override
            public boolean isCurrentTenantKey(Object key) {
                if (!(key instanceof String)) {
                    return false;
                }
                String currentProfile = DynamicDataSourceManager.getCurrentProfile();
                if (currentProfile == null || currentProfile.isEmpty()
                        || currentProfile.indexOf(separatorGuardChar) >= 0
                        || !((String) key).startsWith(literalPrefix + currentProfile + separator)) {
                    return false;
                }
                java.util.List<String> profiles = crossCheckProfileList();
                if (profiles == null) {
                    return false;
                }
                String collisionPrefix = currentProfile + separatorGuardChar;
                for (String profile : profiles) {
                    if (!currentProfile.equals(profile) && profile.startsWith(collisionPrefix)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public Object keyForCurrentTenant(Object localKey) {
                if (!(localKey instanceof String)) {
                    return null;
                }
                String currentProfile = DynamicDataSourceManager.getCurrentProfile();
                if (currentProfile == null || currentProfile.isEmpty()
                        || currentProfile.indexOf(separatorGuardChar) >= 0) {
                    return null;
                }
                return literalPrefix + currentProfile + separator + localKey;
            }
        };
    }

    /**
     * Answers true if any of the given scopes recognizes the key as belonging to the current
     * tenant -- for a cache whose real callers use more than one key convention (see {@link
     * #USERVIEW_MENU_CACHE_SCOPE}), so it is not registered with a scope that only recognizes one
     * of them and unconditionally blocks every restricted caller using the other.
     */
    private static KeyScope anyOf(final KeyScope... scopes) {
        return new KeyScope() {
            @Override
            public boolean isCurrentTenantKey(Object key) {
                for (KeyScope scope : scopes) {
                    if (scope != null && scope.isCurrentTenantKey(key)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Object keyForCurrentTenant(Object localKey) {
                for (KeyScope scope : scopes) {
                    Object candidate = (scope != null) ? scope.keyForCurrentTenant(localKey) : null;
                    if (candidate != null && scope.isCurrentTenantKey(candidate)) {
                        return candidate;
                    }
                }
                return null;
            }

            @Override
            public java.util.List<Object> candidateKeysForCurrentTenant(Object localKey) {
                java.util.List<Object> candidates = new java.util.ArrayList<Object>(scopes.length);
                for (KeyScope scope : scopes) {
                    if (scope != null) {
                        candidates.addAll(scope.candidateKeysForCurrentTenant(localKey));
                    }
                }
                return candidates;
            }
        };
    }

    /**
     * Matches {@code FormOptionsCacheAspect.getCacheKey(..)}'s real key shape
     * ({@code "FORMOPTIONS::" + profile + "::" + ...}) -- a leading literal prefix followed by the
     * profile, never a leading {@code profile + "_"}, so {@link #LEGACY_UNDERSCORE_PREFIX_SCOPE}
     * never matched it. Does not match the separate {@code "LAST_ACTIVE_FORMOPTIONS::..."} variant
     * the same class also writes; that shape is left unrecognized, same as before this scope
     * existed, rather than widen a `startsWith` check into a weaker `contains`.
     */
    public static final KeyScope FORM_OPTIONS_CACHE_SCOPE = profileScope("FORMOPTIONS::", "::");

    /**
     * Matches {@code UserviewCache.getCacheKey(..)}'s real key shape
     * ({@code "USERVIEW:" + profile + ":" + ...}) OR {@code AppDevUtil.dirLastModified(..)}'s
     * ({@code profile + "_dirLastModified_" + ...}, the SAME "&lt;profile&gt;_" convention
     * {@link #UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE} matches) -- both real callers
     * write into the same {@code userviewMenuCache} bean with genuinely different key conventions,
     * confirmed by direct source read of both. A scope recognizing only one of them would
     * unconditionally block the other's every restricted-caller write, which is exactly the
     * "silently wrong for others" failure mode this whole fix exists to close, not introduce for a
     * second convention.
     *
     * <p>#2317/#2261 (independent review, Opus): uses {@link
     * #UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE} here, not {@link
     * #LEGACY_UNDERSCORE_PREFIX_SCOPE} -- {@code AppDevUtil.dirLastModified(..)} is a REAL, live
     * caller of this exact bean on THIS line (confirmed by direct source read: {@code
     * AppUtil.getApplicationContext().getBean("userviewMenuCache")} then {@code cache.put(..)},
     * reachable whenever {@code !AppDevUtil.isGitDisabled()}), so it carries the identical
     * underscore-collision defect {@code fluCache}/{@code longTermCacheObject} were fixed for
     * elsewhere in this class -- {@code LEGACY_UNDERSCORE_PREFIX_SCOPE}'s blanket {@code
     * currentProfile.contains("_")} rejection would still unconditionally deny a {@code
     * "jwc_"}-style tenant's own genuine {@code dirLastModified} write, even with {@code
     * fluCache}/{@code longTermCacheObject} already fixed. (9.1 does not carry this same risk:
     * its own {@code AppDevUtil.dirLastModified(..)} reads a different cache, {@code
     * org.joget.cache.APP_CACHE}, not {@code userviewMenuCache} -- porting only 9.1's own fix for
     * this scope would have missed this 8.2-specific live caller entirely.)
     */
    public static final KeyScope USERVIEW_MENU_CACHE_SCOPE =
            anyOf(profileScope("USERVIEW:", ":"), UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE);

    // setupManagerCache deliberately has no KeyScope constant here -- see PlatformCacheRegistrar's
    // own registration comment for why: no prefix-based shape can safely match its real keys
    // without also exempting more of the cache's namespace than intended.

    // Named, package-visible extraction of the one DynamicDataSourceManager.getProfileList() call
    // LEGACY_UNDERSCORE_PREFIX_SCOPE needs, so MultiTenantSecurityManager
    // .WHITELIST_RESTRICTED_CLASSES (joget-enterprise/cloud-wfengine) can whitelist THIS specific
    // caller frame by name -- "org.joget.commons.util.ManagedCacheRegistry.crossCheckProfileList"
    // -- rather than either (a) excluding getProfileList() from cloud-wfengine's
    // adminProfileMethods() pointcut entirely, which would also un-guard a restricted caller
    // invoking it directly (must stay blocked, per
    // TestMultiTenantSecurityManager.testBeanShellSecurityManager), or (b) catching the resulting
    // SecurityException here and failing closed, which would leave the exemption permanently
    // unusable for the legitimate restricted caller it exists for. A bare, unqualified call to
    // this method from anywhere else in this class or a subclass would NOT match the
    // caller-qualified whitelist entry -- by design, it exists solely for this one call site.
    //
    // Delegates to DynamicDataSourceManager.getProfileList(), which caches the profile-name list
    // (invalidated by the profiles-directory mtime, with racy-mtime slack), so this ownership/collision
    // cross-check runs per restricted fluCache/userview/formOptions read without a per-read listFiles.
    // A null result (IO error) fails closed.
    private static java.util.List<String> crossCheckProfileList() {
        return DynamicDataSourceManager.getProfileList();
    }

    // Test hook: clears the profile-list cache (held in DynamicDataSourceManager) so a unit test that
    // adds/removes profile files between cases is never served a stale list.
    static void invalidateProfileListCacheForTest() {
        DynamicDataSourceManager.invalidateProfileListCacheForTest();
    }

    /**
     * Denies every key outright -- for a cache whose stated policy is "no exemption", registering
     * it with this scope (via {@link #registerReadScoped(Ehcache, KeyScope)}) makes single-key
     * reads blocked for a restricted caller the same as mutation already is, instead of falling
     * through to the unguarded default a {@code null}/absent scope leaves reads in.
     */
    public static final KeyScope NO_TENANT_KEY_SCOPE = new KeyScope() {
        @Override
        public boolean isCurrentTenantKey(Object key) {
            return false;
        }
    };

    private static final Set<Ehcache> MANAGED_CACHES =
            Collections.newSetFromMap(new IdentityHashMap<Ehcache, Boolean>());
    private static final Set<Ehcache> READ_SENSITIVE_CACHES =
            Collections.newSetFromMap(new IdentityHashMap<Ehcache, Boolean>());
    private static final Set<Ehcache> READ_SCOPED_CACHES =
            Collections.newSetFromMap(new IdentityHashMap<Ehcache, Boolean>());
    private static final java.util.Map<Ehcache, KeyScope> KEY_SCOPES =
            new IdentityHashMap<Ehcache, KeyScope>();

    /**
     * Immutable copy of the four registries above, rebuilt under the class lock by every
     * registration and read without any lock by the query methods. A lookup therefore never runs
     * inside this class's monitor -- and neither does the key-scope evaluation it triggers, which
     * can stat (or re-list) the profiles directory: with the monitor held, every restricted cache
     * read in the JVM would queue behind that filesystem call, the same shape as the
     * {@code MultiTenantDataSource} pool-lock bottleneck. Readers see either the previous or the
     * new snapshot, never a partially updated one.
     */
    private static final class Snapshot {
        final Set<Ehcache> managed;
        final Set<Ehcache> readSensitive;
        final Set<Ehcache> readScoped;
        final java.util.Map<Ehcache, KeyScope> keyScopes;

        Snapshot(Set<Ehcache> managed, Set<Ehcache> readSensitive, Set<Ehcache> readScoped, java.util.Map<Ehcache, KeyScope> keyScopes) {
            this.managed = copyOf(managed);
            this.readSensitive = copyOf(readSensitive);
            this.readScoped = copyOf(readScoped);
            this.keyScopes = Collections.unmodifiableMap(new IdentityHashMap<Ehcache, KeyScope>(keyScopes));
        }

        private static Set<Ehcache> copyOf(Set<Ehcache> source) {
            Set<Ehcache> copy = Collections.newSetFromMap(new IdentityHashMap<Ehcache, Boolean>());
            copy.addAll(source);
            return Collections.unmodifiableSet(copy);
        }
    }

    private static volatile Snapshot snapshot = new Snapshot(MANAGED_CACHES, READ_SENSITIVE_CACHES, READ_SCOPED_CACHES, KEY_SCOPES);

    /** Must be called with the class lock held, after any change to the registries above. */
    private static void publish() {
        snapshot = new Snapshot(MANAGED_CACHES, READ_SENSITIVE_CACHES, READ_SCOPED_CACHES, KEY_SCOPES);
    }

    private ManagedCacheRegistry() {
    }

    /**
     * Registers a cache as one this registry's callers should recognize. Safe to call more than
     * once for the same cache (e.g. if the owning bean's setter is invoked again).
     *
     * @param cache the cache to register; ignored when {@code null}
     */
    public static synchronized void register(Ehcache cache) {
        if (cache != null) {
            MANAGED_CACHES.add(cache);
        }
        publish();
    }

    /**
     * Registers a cache the same way {@link #register(Ehcache)} does, plus a {@link KeyScope} so
     * a non-admin caller's own single-key {@code put}/{@code remove}/{@code get} against a key it
     * genuinely owns (per that scope) can be exempted from the outright mutation/enumeration
     * block, instead of a caller-supplied key ever being trusted at face value. Safe to call more
     * than once for the same cache; the scope from the LAST call wins.
     *
     * @param cache the cache to register; ignored when {@code null}
     * @param scope how to recognize a key that belongs to the current tenant; ignored when
     *              {@code null} (equivalent to calling {@link #register(Ehcache)} alone)
     */
    public static synchronized void register(Ehcache cache, KeyScope scope) {
        registerWithoutPublishing(cache, scope);
        publish();
    }

    // Shared by register(cache, scope) and registerReadScoped so a read-scoped registration is
    // published exactly once, never in a scoped-but-not-yet-read-scoped intermediate state.
    private static void registerWithoutPublishing(Ehcache cache, KeyScope scope) {
        if (cache != null) {
            MANAGED_CACHES.add(cache);
            if (scope != null) {
                KEY_SCOPES.put(cache, scope);
            } else {
                // "the scope from the LAST call wins" (above) must hold for a null scope too --
                // otherwise a cache re-registered with no scope would silently keep whatever
                // scope an earlier call gave it, which is both surprising and, since a KeyScope
                // controls what gets treated as a caller's own key, fail-OPEN rather than closed.
                KEY_SCOPES.remove(cache);
            }
        }
    }

    /**
     * Registers a cache the same way {@link #register(Ehcache, KeyScope)} does, and additionally
     * makes it read-scoped: {@link org.joget.plugin.base.MultiTenantPluginAspect}'s single-key
     * {@code get()}/{@code getQuiet()} guard blocks a restricted caller's read outright unless
     * {@code scope} confirms the key belongs to the current tenant. Use this only for caches whose
     * key convention has actually been confirmed to carry reliable tenant-scoping (or for
     * {@link #NO_TENANT_KEY_SCOPE}, to block every read outright) -- registering a cache here whose
     * real keys don't match {@code scope} makes EVERY restricted caller's read of it throw,
     * regardless of whether the key was genuinely theirs. This is why it's a separate, explicit
     * opt-in from {@link #register(Ehcache, KeyScope)}: several platform caches (e.g.
     * {@code fluCache}/{@code userviewMenuCache}/{@code formOptionsCache}/{@code setupManagerCache})
     * are registered with a {@link KeyScope} purely for the mutation-guard exemption, but their
     * single-key reads are deliberately left unguarded (see this class's own top-level javadoc) --
     * scoping their reads too would silently break ordinary, legitimate tenant traffic for any
     * cache whose real key shape doesn't match the registered scope closely enough.
     *
     * @param cache the cache to register; ignored when {@code null}
     * @param scope how to recognize a key that belongs to the current tenant; required -- ignored
     *              (equivalent to calling {@link #register(Ehcache, KeyScope)} alone, i.e. NOT
     *              read-scoped) when {@code null}, since a read-scoped cache with no way to
     *              recognize an owned key would otherwise have no defined behavior
     */
    public static synchronized void registerReadScoped(Ehcache cache, KeyScope scope) {
        registerWithoutPublishing(cache, scope);
        if (cache != null && scope != null) {
            READ_SCOPED_CACHES.add(cache);
        }
        publish();
    }

    /**
     * Registers a cache as read-sensitive: in addition to the mutation/bulk-enumeration guard
     * {@link #register(Ehcache)} already provides, a plain single-key {@code get()}/{@code
     * getQuiet()} is blocked outright for non-admin callers too, because reading this cache's
     * contents directly (not just mutating them) exposes something no tenant should see. Also
     * registers via {@link #register(Ehcache)} -- a read-sensitive cache is always also managed
     * for mutation purposes, callers only need to call this one method.
     *
     * @param cache the cache to register; ignored when {@code null}
     */
    public static synchronized void registerReadSensitive(Ehcache cache) {
        if (cache != null) {
            MANAGED_CACHES.add(cache);
            READ_SENSITIVE_CACHES.add(cache);
        }
        publish();
    }

    /**
     * @param candidate the object to test
     * @return true if candidate is exactly one of this registry's registered caches
     */
    public static boolean isManaged(Object candidate) {
        return candidate instanceof Ehcache && snapshot.managed.contains(candidate);
    }

    /**
     * @param candidate the object to test
     * @return true if candidate was registered via {@link #registerReadSensitive(Ehcache)} --
     *         a plain single-key read of it should be blocked outright for non-admin callers,
     *         not just its mutation/enumeration methods
     */
    public static boolean isReadSensitive(Object candidate) {
        return candidate instanceof Ehcache && snapshot.readSensitive.contains(candidate);
    }

    /**
     * @param candidate the object to test
     * @return true if candidate was registered with a {@link KeyScope} via
     *         {@link #register(Ehcache, KeyScope)}
     */
    public static boolean hasKeyScope(Object candidate) {
        return candidate instanceof Ehcache && snapshot.keyScopes.containsKey(candidate);
    }

    /**
     * @param candidate the object to test
     * @return true if candidate was registered via {@link #registerReadScoped(Ehcache, KeyScope)}
     *         -- a plain single-key read of it should be blocked for a restricted caller unless
     *         its registered {@link KeyScope} confirms the key belongs to the current tenant
     */
    public static boolean isReadScoped(Object candidate) {
        return candidate instanceof Ehcache && snapshot.readScoped.contains(candidate);
    }

    /**
     * True only if {@code candidate}'s registered scope is {@link #NO_TENANT_KEY_SCOPE} -- a cache
     * that grants no tenant any key, so a restricted caller's read of it is refused outright rather
     * than redirected or read as a miss. Lets the read guard keep that "block" policy for such a
     * cache while returning a miss for a key-building scope that merely cannot scope one key.
     */
    public static boolean blocksAllTenantKeys(Object candidate) {
        return candidate != null && snapshot.keyScopes.get(candidate) == NO_TENANT_KEY_SCOPE;
    }

    /**
     * @param candidate the cache the key was read from or is being written to
     * @param key the key to test
     * @return true only if {@code candidate} was registered with a {@link KeyScope} AND that
     *         scope confirms {@code key} belongs to the current tenant. Fails closed: an
     *         unregistered scope, or a scope that itself answers false, both return false --
     *         there is no default-permissive fallback.
     */
    public static boolean isCurrentTenantKey(Object candidate, Object key) {
        if (!(candidate instanceof Ehcache)) {
            return false;
        }
        KeyScope scope = snapshot.keyScopes.get(candidate);
        return scope != null && scope.isCurrentTenantKey(key);
    }

    /**
     * #2261: the prefix-level counterpart to {@link #isCurrentTenantKey(Object, Object)}, for a
     * caller about to bulk-remove every entry matching {@code prefix} rather than read/write one
     * specific key. Fails closed exactly the same way: an unregistered scope, or a scope whose
     * {@link KeyScope#isCurrentTenantKeyPrefix(String)} itself answers false (including every
     * scope that does not override the default at all), both return false.
     *
     * @param candidate the cache the bulk removal is about to run against
     * @param prefix the literal prefix the removal would match every key against
     */
    public static boolean isCurrentTenantKeyPrefix(Object candidate, String prefix) {
        if (!(candidate instanceof Ehcache)) {
            return false;
        }
        KeyScope scope = snapshot.keyScopes.get(candidate);
        return scope != null && scope.isCurrentTenantKeyPrefix(prefix);
    }

    // The "<profile>_" builder shared by the two underscore-prefix scopes; same profile source as
    // their isCurrentTenantKey(..), so a key built here is checked against the same profile.
    private static Object underscorePrefixedKey(Object localKey) {
        if (!(localKey instanceof String)) {
            return null;
        }
        String currentProfile = DynamicDataSourceManager.getCurrentProfile();
        if (currentProfile == null || currentProfile.isEmpty()) {
            return null;
        }
        return currentProfile + "_" + localKey;
    }

    /**
     * The current tenant's own form of {@code key} under {@code candidate}'s registered
     * {@link KeyScope}, or {@code null} if the cache has no scope, the scope cannot build one, or the
     * built key is not confirmed by that same scope as belonging to the current tenant. Fails closed
     * like {@link #isCurrentTenantKey(Ehcache, Object)}: a non-null result is always a key the current
     * tenant was already allowed to read and write.
     */
    public static Object currentTenantKeyFor(Object candidate, Object key) {
        if (candidate == null) {
            return null;
        }
        KeyScope scope = snapshot.keyScopes.get(candidate);
        if (scope == null) {
            return null;
        }
        Object rewritten = scope.keyForCurrentTenant(key);
        return (rewritten != null && scope.isCurrentTenantKey(rewritten)) ? rewritten : null;
    }

    /**
     * The current tenant's possible own forms of {@code key} under {@code candidate}'s registered
     * {@link KeyScope}, NOT yet confirmed as the current tenant's (see
     * {@link KeyScope#candidateKeysForCurrentTenant(Object)}); empty if the cache has no scope. A
     * value read under one of them may only be returned after
     * {@link #isCurrentTenantKey(Object, Object)} confirms that key.
     */
    public static java.util.List<Object> candidateTenantKeysFor(Object candidate, Object key) {
        KeyScope scope = (candidate == null) ? null : snapshot.keyScopes.get(candidate);
        return (scope == null) ? java.util.Collections.<Object>emptyList() : scope.candidateKeysForCurrentTenant(key);
    }
}
