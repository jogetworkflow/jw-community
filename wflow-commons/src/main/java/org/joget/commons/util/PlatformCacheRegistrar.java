package org.joget.commons.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Registers every raw platform cache bean into {@link ManagedCacheRegistry} once, at application
 * startup, so the existing identity-based {@code Cache} guards in {@code MultiTenantPluginAspect}
 * (which already check {@code ManagedCacheRegistry.isManaged(..)}) cover them too, without needing
 * a separate registration call at every one of their many call sites.
 *
 * Unlike {@code nonceCache}/{@code SF_CACHE} (which
 * have one clear owning class that could register on its own setter), these beans are each fetched
 * fresh, directly by bean id, from many call sites throughout the platform's own code (e.g. {@code
 * FormOptionsCacheAspect}, {@code UserviewCache}, {@code AppWorkflowHelper}) -- there is no single
 * setter to hook. Listening for {@link ContextRefreshedEvent} instead: it fires only after the
 * entire application context (including anything imported from a separate XML file) has been fully
 * refreshed, so every bean below is guaranteed to exist by then regardless of which file defines it
 * or import order, and it fires once per context (webapp startup), well before any tenant traffic
 * -- registration itself never needs to be guarded.
 *
 * {@code fluCache}/{@code userviewMenuCache}/{@code formOptionsCache}/{@code cssCache}/{@code
 * rowCountCache} hold application definitions, rendered userview content, live {@code FormRowSet}
 * option data, and rendered CSS respectively -- a non-admin caller reaching one of these caches
 * directly (bypassing the platform's own accessor classes) could otherwise enumerate, evict, or
 * poison another tenant's entries by key. {@code resourceBundleMessageCache}/{@code
 * setupManagerCache}/{@code longTermCacheObject}/{@code defaultPropertyValuesCache} are lower-
 * profile but equally reachable the same way; {@code setupManagerCache} in particular holds
 * profile-prefixed workflow replacement-user maps that {@code AppWorkflowHelper} trusts.
 *
 * {@code dynamicDataSourceManagerCache} is registered SEPARATELY below (not via {@code
 * PLATFORM_CACHE_KEY_SCOPES}/{@code registerReadScoped} like the caches above): the guards' OTHER
 * identity check, {@code DynamicDataSourceManager.isManagedCache(..)}, recognizes only the RAW,
 * unwrapped {@code javax.cache.Cache} that class's own {@code setCacheManager(..)} stores, a
 * DIFFERENT object from this Spring-bean wrapper -- so neither identity check matches a caller
 * reaching this bean directly unless it is registered here too. Registered via {@link
 * ManagedCacheRegistry#registerReadSensitive(Object)} -- not {@code registerReadScoped} with a
 * {@link ManagedCacheRegistry.KeyScope} like the caches above -- because this cache holds every
 * tenant's live {@code Properties} (datasource connection strings, credentials): a single-key
 * {@code get()} of even a caller's OWN entry is still a real concern (there is no legitimate
 * non-admin reason to read connection credentials back out of this cache at all), unlike
 * fluCache/userviewMenuCache/etc. where a tenant reading its OWN scoped entry is expected,
 * legitimate traffic. {@code DynamicDataSourceManager}'s own reads go through the raw, unwrapped
 * cache (a different object this fix does not touch), so no internal/legitimate path is affected.
 */
public class PlatformCacheRegistrar implements ApplicationListener<ContextRefreshedEvent> {

    // Each cache's ManagedCacheRegistry.KeyScope, so a non-admin caller's own single-key
    // put/remove against a key it genuinely owns can be exempted from the outright mutation
    // block, instead of a caller-supplied key ever being trusted at face value -- null means no
    // exemption (the original, correctly-conservative default for a cache whose key convention
    // was never confirmed to carry tenant-scoping at all). dynamicDataSourceManagerCache is
    // deliberately NOT here -- see this class's own top-level javadoc for why it gets a stricter,
    // separate registration below instead.
    //
    // fluCache: registered with the legacy "<profile>_" prefix scope for the MUTATION guard only
    // (see READ_SCOPED_CACHE_NAMES below -- not read-scoped). Its real key builders
    // (AppDefCache.removeAll(..)/FormDefinitionDaoImpl.getCacheKey(..)) actually match that prefix.
    //
    // userviewMenuCache/formOptionsCache: each has at least one real key shape (found by grepping
    // every getBean("<cacheName>")/AppUtil.getCache("...") call site in both repos) that never
    // matched LEGACY_UNDERSCORE_PREFIX_SCOPE's leading "<profile>_" convention, so a restricted
    // caller's own genuinely self-scoped put/remove against either was unconditionally blocked
    // regardless of admin status. Each now uses a scope built for its own key shape (see
    // ManagedCacheRegistry's javadoc on each): userviewMenuCache ("USERVIEW:" + profile + ":..."
    // from UserviewCache.getCacheKey(..), the same shape joget-enterprise's
    // ProcessStatusDatalistFormatter also builds by reusing UserviewCache.CACHE_KEY_PREFIX -- the
    // only two real writers of "org.joget.cache.USERVIEW_CACHE"), formOptionsCache
    // ("FORMOPTIONS::" + profile + "::..." AND "LAST_ACTIVE_FORMOPTIONS::" + profile + "::..."
    // from FormOptionsCacheAspect's own two real key builders). Neither is read-scoped (see
    // READ_SCOPED_CACHE_NAMES below), so this only affects the mutation guard's self-scoped
    // exemption, never unconditionally blocks a legitimate reader.
    //
    // setupManagerCache: registered with NO scope (null) -- deliberately, not an oversight. A
    // non-admin tenant's own OSGi/BeanShell code could put(profile + ":USER_REPLACEMENT_" +
    // someRealUsername + "_" + today, forgedReplacementMap) -- AppWorkflowHelper
    // .getReplacementUsers(..) returns a cache hit VERBATIM with no DB fallback check at all,
    // forging workflow task delegation and redirecting someRealUsername's assignments to whoever
    // the forged map names. Unlike fluCache/formOptionsCache/userviewMenuCache, there is no
    // legitimate restricted-caller traffic to setupManagerCache an exemption would unblock, so
    // every restricted caller's put/remove against it stays unconditionally blocked -- the same
    // conservative default any cache with no confirmed-safe key convention gets. No narrower
    // "profile + \":USER_REPLACEMENT_\""-only scope can fix this either (see ManagedCacheRegistry's
    // own comment on this cache): the "username" half of that key is
    // AppWorkflowHelper.getReplacementUsers(..)'s own caller-supplied argument, not derived from
    // the acting identity, so no KeyScope this codebase's abstraction can express would stop a
    // restricted caller from forging a DIFFERENT tenant user's replacement map -- only the PROFILE
    // portion of a key is ever checked. AppWorkflowHelper's own two call sites catch the resulting
    // SecurityException and degrade (skip caching / leave a stale entry) instead, which is safe
    // there specifically because both are non-security-relevant cache housekeeping around an
    // already-correct DB read/write, not the mutation this comment's own escalation example
    // depends on.
    //
    // cssCache/rowCountCache/defaultPropertyValuesCache: their actual key formats (raw LESS
    // source with no tenant info at all; a literal "Profile::" + profile + "(hash)", where the
    // profile is NOT a leading prefix; and a "class:version:locale:profile:<id>" TRAILING
    // segment, respectively) never matched the legacy prefix convention either, so under the
    // original single central prefix-matching exemption these three caches' non-admin writes were
    // always blocked outright. Their key-building code (UniversalTheme/XadminTheme/
    // AjaxUniversalTheme, DataList, DefaultPropertyValuesCache) now builds keys via
    // ProfileScopedCacheKey instead, so this is the exemption that actually matches what they
    // write. NOTE (9.1-specific): every one of those real call sites reaches its cache via
    // AppUtil.getCache(String), which now consults REGISTERED_CACHES_BY_ALIAS below (keyed by the
    // cache's own getName() -- the alias every call site actually passes, e.g.
    // "org.joget.cache.CSS_CACHE" -- not the unrelated Spring bean id "cssCache") to return this
    // SAME registered, guarded instance, so this KeyScope wiring closes the enforcement gap for
    // cssCache/formOptionsCache/fluCache/userviewMenuCache/setupManagerCache/
    // resourceBundleMessageCache the same way it already does on 8.2. rowCountCache/
    // defaultPropertyValuesCache/longTermCacheObject remain the one carve-out -- see the note
    // below on why no Spring bean exists for them on 9.1 at all, so there is nothing for
    // REGISTERED_CACHES_BY_ALIAS to hold and AppUtil.getCache(..) still falls back to the raw,
    // unguarded fetch for those three specifically.
    //
    // resourceBundleMessageCache: NO_TENANT_KEY_SCOPE (deny-all, both mutation and read). Confirmed
    // to have no Java consumer anywhere in this repo -- the deny-all is inert, not a regression
    // risk, for what its stated "no exemption" policy is presumably guarding.
    //
    // longTermCacheObject is NOT registered NO_TENANT_KEY_SCOPE, despite superficially matching
    // resourceBundleMessageCache's "no exemption" policy -- unlike that cache, this one backs
    // org.joget.cache.RBM_CACHE (commonsApplicationContext.xml), the real cache behind
    // ResourceBundleMessageDao.getMessage(..) (reached from every ResourceBundleUtil.getMessage(..)
    // call in the platform) and FormColumnCache. A deny-all read guard there would throw for any
    // restricted (plugin/BeanShell) caller resolving ANY message key -- ResourceBundleUtil's own
    // blanket catch swallows the exception, but every such lookup would silently degrade to the raw
    // message code instead of the tenant's DB-stored translation, for entirely legitimate plugin
    // code, not just an attacker. Its actual key IS reliably tenant-scoped though
    // (ResourceBundleMessageDao.getCacheKey(..): getCurrentProfile() + "_" + locale -- the same
    // legacy prefix fluCache uses), so it is registered the same way fluCache is:
    // LEGACY_UNDERSCORE_PREFIX_SCOPE, mutation guard only, read left unguarded for the same "plain
    // legitimate non-admin-tenant reads must stay unguarded" reason fluCache's own read side does
    // (see READ_SCOPED_CACHE_NAMES below).
    //
    // NOTE: on 9.1, "rowCountCache"/"defaultPropertyValuesCache"/"longTermCacheObject" are not
    // real Spring bean ids (only "org.joget.cache.ROW_COUNT_CACHE" etc. exist as cache aliases,
    // fetched directly via AppUtil.getCache(String)) -- context.getBean(..) below throws and is
    // caught the same as any other absent bean, exactly as it already did before this port (see
    // PLATFORM_CACHE_BEAN_NAMES's pre-existing 9.1 list, unchanged by this port). Their KeyScope
    // entries are kept below anyway, matching 8.2's registration shape 1:1, so a future bean
    // rename/rewire does not silently lose the scope along with it. Since no bean ever exists for
    // these three, REGISTERED_CACHES_BY_ALIAS below never gets an entry for their aliases either,
    // so AppUtil.getCache(..) correctly falls back to the raw fetch for exactly these three and
    // no others -- a disclosed, pre-existing 9.1 architecture gap, not something this class's
    // registration loop can close without also introducing new bean definitions.
    private static final Map<String, ManagedCacheRegistry.KeyScope> PLATFORM_CACHE_KEY_SCOPES =
            new LinkedHashMap<String, ManagedCacheRegistry.KeyScope>();

    // Registered separately from PLATFORM_CACHE_KEY_SCOPES above -- via
    // ManagedCacheRegistry.registerReadSensitive(..), not registerReadScoped(..) -- see this
    // class's own top-level javadoc for why this one specific cache needs an unconditional read
    // block rather than a KeyScope-based "your own key is fine" exemption.
    private static final String DYNAMIC_DATASOURCE_MANAGER_CACHE_BEAN_NAME = "dynamicDataSourceManagerCache";

    // nonceCache and formPersistentClassCache were both EhCacheFactoryBeans with NO <cacheName>
    // property (commonsApplicationContext.xml), so each wrapped the shared "default" cache -- the
    // same cache SecureDataEncryptionImpl (joget-enterprise) fills with decrypted secrets. Unlike
    // resourceBundleMessageCache (also default-wrapping, but already registered by the KeyScope loop
    // above), neither of these two Spring-bean wrappers was registered anywhere: FormDataDaoImpl and
    // SecureDataEncryptionImpl each build and registerReadSensitive their OWN separate
    // net.sf.ehcache.Cache wrapper of "default" and never touch these bean instances, leaving the
    // exact wrapper object getBean("nonceCache")/getBean("formPersistentClassCache") returned
    // ungoverned. Because MultiTenantPluginAspect's Cache guards match by object identity, a
    // non-admin tenant fetching either bean by id could read and bulk-enumerate every "default"
    // entry.
    //
    // formPersistentClassCache (Hibernate PersistentClass metadata, no legitimate non-admin reader
    // -- confirmed by grepping every getBean("formPersistentClassCache") call site) is registered
    // here read-sensitive -- the same tier the other "default" wrappers use -- so even a
    // single-key get() of the caller's own key is blocked.
    //
    // nonceCache is NOT registered here at all -- an independent review found registering it the
    // same way breaks a real, legitimate consumer: OpenIDDirectoryManager (an OIDC
    // DirectoryManager plugin, run per-tenant) calls getBean("nonceCache").put(..)/.get(..)
    // directly with a plain, opaque OAuth `state` token as the key -- not itself
    // profile-prefixed, so no ManagedCacheRegistry.KeyScope could ever recognize it as "the
    // caller's own" key, and registerReadSensitive blocks EVERY restricted caller's put/get
    // unconditionally, with no exemption path at all (see MultiTenantPluginAspect
    // .recognizedPlatformCacheKeyMutationShape's own isReadSensitive(..) short-circuit). Instead,
    // nonceCache's own cacheName is now set to org.joget.cache.NONCE_CACHE
    // (commonsApplicationContext.xml) -- an already-existing, purpose-built region (ehcache.xml,
    // DynamicExpiryPolicy, matching this exact per-element-TTL use), separating it from the
    // secret-holding "default" cache entirely. This closes the SEVERE part of the original
    // exposure (reading decrypted secrets) by construction, without needing a guard at all. Bulk
    // enumeration of nonceCache's own, now-separate region (seeing other tenants' pending OIDC
    // state->nonce correlations, not secrets) remains open -- an accepted, disclosed, much
    // lower-severity residual risk, the same shape as the "ordinary tenant-vs-tenant collision"
    // ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE's own javadoc
    // already documents accepting elsewhere; closing it fully would need the OIDC plugin's own
    // key scheme to carry tenant scoping, out of scope for this fix.
    //
    // Forcing formPersistentClassCache's lazy-init bean to be created here (at context refresh, in
    // the system context) also removes the pre-existing window in which the FIRST caller to touch
    // the lazy bean was whoever's request happened to reach it -- nonceCache gets this SAME
    // eager-creation treatment too, just below, WITHOUT the registerReadSensitive call (see
    // onApplicationEvent's own comment on that separate step for why: live-confirmed that skipping
    // it entirely leaves nonceCache's own first-touch race open, and unlike a clean SecurityException
    // this specific race throws a raw BeanCreationException wrapping "no permission to access
    // Eh107CacheManager" when a non-admin tenant is the unlucky first caller). Deliberately NOT put into
    // REGISTERED_CACHES_BY_ALIAS (getName() == "default"), matching FormDataDaoImpl's own choice for
    // its "default" wrapper, to avoid a startup-order-dependent overwrite of another "default"
    // wrapper's alias for no behavioral benefit -- AppUtil.getCache("default") is already blocked for
    // a restricted caller by the cacheManager access guard, so only the getBean(..) path needs this.
    private static final java.util.Set<String> READ_SENSITIVE_DEFAULT_WRAPPER_BEAN_NAMES =
            new java.util.LinkedHashSet<String>(java.util.Arrays.asList("formPersistentClassCache"));

    // Keyed by each cache's own getName() (the ehcache.xml alias, e.g.
    // "org.joget.cache.CSS_CACHE") -- the string every real AppUtil.getCache(String) call site
    // actually passes -- NOT by the Spring bean id (e.g. "cssCache") used as this class's own
    // registration key above. The two are unrelated strings for the same cache (see
    // commonsApplicationContext.xml's <property name="cacheName" value="org.joget.cache...."/> on
    // each EhCacheFactoryBean) -- AppUtil.getCache(..) has no reason to know the bean id, only the
    // alias its own callers were already written against, so this is the map it consults.
    // ConcurrentHashMap: populated once at startup (see onApplicationEvent below, which fires
    // once per context, before any tenant traffic) but read from arbitrary request threads
    // afterward.
    private static final Map<String, net.sf.ehcache.Cache> REGISTERED_CACHES_BY_ALIAS =
            new ConcurrentHashMap<String, net.sf.ehcache.Cache>();

    /**
     * Returns the same registered, {@link ManagedCacheRegistry}-guarded {@link net.sf.ehcache.Cache}
     * instance this class registered for {@code cacheAlias} (the cache's own {@link
     * net.sf.ehcache.Cache#getName()}, e.g. {@code "org.joget.cache.CSS_CACHE"} -- NOT the Spring
     * bean id), or {@code null} if this class never registered one for that alias (either the
     * alias names a cache outside {@link #PLATFORM_CACHE_KEY_SCOPES}, or -- see the 9.1 note
     * above -- no Spring bean exists for it in this edition).
     */
    public static net.sf.ehcache.Cache getRegisteredCache(String cacheAlias) {
        return REGISTERED_CACHES_BY_ALIAS.get(cacheAlias);
    }

    /**
     * #2317: lets a cache's OWNING CLASS (one with no single Spring-bean-based registration path
     * this class's own {@link ContextRefreshedEvent} loop above can discover -- e.g. a class that
     * builds its own {@code net.sf.ehcache.Cache} wrapper directly inside a
     * {@code setCacheManager(CacheManager)} setter, rather than receiving an already-registered
     * bean by reference) still make {@link #getRegisteredCache(String)}/{@link
     * org.joget.apps.app.service.AppUtil#getCache(String)} return ITS SAME governed instance.
     * Without this, {@code AppUtil.getCache(cacheAlias)} -- the one cache accessor plugin/
     * BeanShell authors actually use -- falls through to a raw, ungoverned
     * {@code cacheManager.getCache(cacheAlias)} fetch for any alias this class's own {@link
     * #PLATFORM_CACHE_KEY_SCOPES}-driven loop never discovers a bean for, defeating that class's
     * own {@link ManagedCacheRegistry} registration entirely for anyone reaching the cache this
     * way instead of through the owning class's own accessor methods.
     *
     * <p>Callers must register with {@link ManagedCacheRegistry} themselves FIRST (this method
     * only affects {@code AppUtil.getCache(String)}'s routing, not
     * {@code MultiTenantPluginAspect}'s own guards, which key off {@link ManagedCacheRegistry}
     * directly).
     */
    public static void registerAlias(net.sf.ehcache.Cache cache) {
        // ConcurrentHashMap rejects a null key outright -- getName() can genuinely return null
        // (e.g. a test fixture/proxy Cache that never implements it), which must not crash the
        // caller's own setCacheManager(..); simply nothing to alias in that case.
        String name = cache.getName();
        if (name != null) {
            REGISTERED_CACHES_BY_ALIAS.put(name, cache);
        }
    }
    static {
        PLATFORM_CACHE_KEY_SCOPES.put("fluCache", ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("userviewMenuCache", ManagedCacheRegistry.USERVIEW_MENU_CACHE_SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("formOptionsCache", ManagedCacheRegistry.FORM_OPTIONS_CACHE_SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("cssCache", ProfileScopedCacheKey.SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("rowCountCache", ProfileScopedCacheKey.SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("resourceBundleMessageCache", ManagedCacheRegistry.NO_TENANT_KEY_SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("setupManagerCache", null);
        PLATFORM_CACHE_KEY_SCOPES.put("longTermCacheObject", ManagedCacheRegistry.LEGACY_UNDERSCORE_PREFIX_SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("defaultPropertyValuesCache", ProfileScopedCacheKey.SCOPE);
    }

    // Which of the caches above also get single-key get()/getQuiet() redirected to the caller's own
    // key space (or, when no own key can be built, read as a miss) for a restricted caller whose key
    // the registered KeyScope does not recognize as its own -- ManagedCacheRegistry.registerReadScoped(..)
    // rather than plain register(..).
    // fluCache/userviewMenuCache/formOptionsCache ARE read-scoped: every real key they hold leads with
    // the current profile (AppDefCache "<profile>_<appId>_...", UserviewCache "USERVIEW:<profile>:...",
    // FormOptionsCacheAspect "FORMOPTIONS::<profile>::..." / "LAST_ACTIVE_FORMOPTIONS::<profile>::..."),
    // each matched by that cache's own scope, so a tenant's own read passes through untouched and only
    // a key outside its own space is redirected. fluCache uses the LONGEST scope (not LEGACY) so that a
    // tenant whose profile name itself contains '_' (e.g. "customer_eu", ordinary on Joget Cloud) is
    // still recognized; every reader (AppDefCache/UserviewCache/FormOptionsCacheAspect) rebuilds on a
    // null miss, so a redirect that misses is safe.
    // cssCache/rowCountCache/defaultPropertyValuesCache are read-scoped because their real keys
    // (via ProfileScopedCacheKey) DO reliably carry tenant-scoping now.
    // resourceBundleMessageCache is read-scoped with NO_TENANT_KEY_SCOPE so its stated "no
    // exemption" policy is actually enforced on reads too, not just mutation/enumeration. It has no
    // real consumer, so this is inert.
    // setupManagerCache and longTermCacheObject are deliberately NOT here. setupManagerCache stays
    // null-scoped (see its registration comment above and ManagedCacheRegistry's setupManagerCache
    // comment): some of its real keys cannot be represented by any profile scope, and registering a
    // scope would also re-enable the self-scoped mutation exemption the null registration exists to
    // deny. longTermCacheObject wraps the same RBM region LongTermCache serves, whose read traffic
    // (incl. ResourceBundleUtil / LicenseManager) mixes profile-led and non-profile-led keys; closing
    // its cross-tenant read needs the same dedicated-scope treatment and is left as a reviewed follow-up.
    private static final java.util.Set<String> READ_SCOPED_CACHE_NAMES = new java.util.HashSet<String>(
            java.util.Arrays.asList("cssCache", "rowCountCache", "defaultPropertyValuesCache",
                    "resourceBundleMessageCache", "fluCache", "userviewMenuCache", "formOptionsCache"));

    // Cache aliases with NO Spring bean on 9.1 that platform code reaches only through
    // AppUtil.getCache(alias), from code that can run inside a tenant's plugin or BeanShell call
    // (DataList's row-count cache, AppDevUtil's git-sync lastModified cache, NonceGeneratorImpl's
    // nonce cache). With no registered instance, AppUtil.getCache(..) fell through to the shared
    // cacheManager, whose every method is refused for a restricted caller, so these features failed
    // (or silently ran uncached) for non-admin tenants only. Each gets its own plain pass-through
    // wrapper, registered with the KeyScope matching what its platform caller actually writes, and
    // aliased so AppUtil.getCache(..) returns that governed instance to every caller:
    // ROW_COUNT_CACHE keys are ProfileScopedCacheKey ("<profile>::...") and it is read-scoped like
    // rowCountCache. NONCE_CACHE ("<profile>_NonceGenerator_...", built from the same profile source as
    // the scope) is read-scoped with its own exact-prefix scope (NONCE_GENERATOR_KEY_SCOPE), so a
    // restricted caller can neither reach another profile's nonce nor the OIDC plugin's NUL-separated
    // entries in the same region. APP_CACHE ("<profile>_dirLastModified_...") is read-scoped as well;
    // AppDevUtil falls back to computing uncached if a key cannot be scoped for its caller. nonceCache
    // (the OIDC plugin's bean, a ProfileScopedNonceCache that re-wraps values with its own TTL) is a
    // different object over the same region and is deliberately not this alias.
    private static final Map<String, ManagedCacheRegistry.KeyScope> ALIAS_ONLY_CACHE_SCOPES =
            new LinkedHashMap<String, ManagedCacheRegistry.KeyScope>();
    private static final java.util.Set<String> ALIAS_ONLY_READ_SCOPED = new java.util.HashSet<String>(
            java.util.Arrays.asList("org.joget.cache.ROW_COUNT_CACHE", "org.joget.cache.NONCE_CACHE",
                    "org.joget.cache.APP_CACHE"));
    static {
        ALIAS_ONLY_CACHE_SCOPES.put("org.joget.cache.ROW_COUNT_CACHE", ProfileScopedCacheKey.SCOPE);
        ALIAS_ONLY_CACHE_SCOPES.put("org.joget.cache.APP_CACHE", ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE);
        ALIAS_ONLY_CACHE_SCOPES.put("org.joget.cache.NONCE_CACHE", ManagedCacheRegistry.NONCE_GENERATOR_KEY_SCOPE);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        for (Map.Entry<String, ManagedCacheRegistry.KeyScope> entry : PLATFORM_CACHE_KEY_SCOPES.entrySet()) {
            String beanName = entry.getKey();
            try {
                Object bean = context.getBean(beanName);
                // 9.1 port: no net.sf.ehcache.Ehcache interface exists here (see
                // ManagedCacheRegistry's own 9.1 port note) -- these beans are Joget's own
                // net.sf.ehcache.Cache backward-compat wrapper, wired via EhCacheFactoryBean.
                if (bean instanceof net.sf.ehcache.Cache) {
                    net.sf.ehcache.Cache cache = (net.sf.ehcache.Cache) bean;
                    if (READ_SCOPED_CACHE_NAMES.contains(beanName)) {
                        ManagedCacheRegistry.registerReadScoped(cache, entry.getValue());
                    } else {
                        ManagedCacheRegistry.register(cache, entry.getValue());
                    }
                    // AppUtil.getCache(String) callers pass the alias (cache.getName()), not this
                    // loop's own bean-id key -- see REGISTERED_CACHES_BY_ALIAS's javadoc above.
                    REGISTERED_CACHES_BY_ALIAS.put(cache.getName(), cache);
                }
            } catch (Exception e) {
                // Bean not present in this context/edition -- not every profile wires every one
                // of these, and a missing bean here is not itself a security concern.
                LogUtil.debug(getClass().getName(), "Platform cache bean not registered (not present in this context): " + beanName);
            }
        }
        // Alias-only caches (see ALIAS_ONLY_CACHE_SCOPES): built here, on the startup thread, from
        // the shared cacheManager, so no tenant ever needs the cacheManager itself. An alias already
        // registered by its owner (registerAlias) is left as is.
        try {
            javax.cache.CacheManager cacheManager = (javax.cache.CacheManager) context.getBean("cacheManager");
            for (Map.Entry<String, ManagedCacheRegistry.KeyScope> entry : ALIAS_ONLY_CACHE_SCOPES.entrySet()) {
                String alias = entry.getKey();
                if (REGISTERED_CACHES_BY_ALIAS.containsKey(alias)) {
                    continue;
                }
                try {
                    javax.cache.Cache backing = cacheManager.getCache(alias);
                    if (backing == null) {
                        continue;
                    }
                    net.sf.ehcache.Cache cache = new net.sf.ehcache.Cache(backing);
                    if (ALIAS_ONLY_READ_SCOPED.contains(alias)) {
                        ManagedCacheRegistry.registerReadScoped(cache, entry.getValue());
                    } else {
                        ManagedCacheRegistry.register(cache, entry.getValue());
                    }
                    REGISTERED_CACHES_BY_ALIAS.putIfAbsent(alias, cache);
                } catch (Exception e) {
                    LogUtil.warn(getClass().getName(), "Platform cache alias not registered: " + alias + ": " + e);
                }
            }
        } catch (Exception e) {
            LogUtil.warn(getClass().getName(), "Platform cache aliases not registered (no cacheManager): " + e);
        }
        // dynamicDataSourceManagerCache, separately -- see this class's own top-level javadoc for
        // why it gets registerReadSensitive(..) instead of a KeyScope-based registration like the
        // loop above.
        try {
            Object bean = context.getBean(DYNAMIC_DATASOURCE_MANAGER_CACHE_BEAN_NAME);
            if (bean instanceof net.sf.ehcache.Cache) {
                net.sf.ehcache.Cache cache = (net.sf.ehcache.Cache) bean;
                ManagedCacheRegistry.registerReadSensitive(cache);
                REGISTERED_CACHES_BY_ALIAS.put(cache.getName(), cache);
            }
        } catch (Exception e) {
            LogUtil.debug(getClass().getName(), "Platform cache bean not registered (not present in this context): "
                    + DYNAMIC_DATASOURCE_MANAGER_CACHE_BEAN_NAME);
        }
        // The remaining default-wrapping bean wrapper (see
        // READ_SENSITIVE_DEFAULT_WRAPPER_BEAN_NAMES's javadoc above). registerReadSensitive on the
        // exact instance getBean(..) returns, so a non-admin caller reaching this bean by id can
        // no longer read or enumerate the shared "default" cache.
        for (String beanName : READ_SENSITIVE_DEFAULT_WRAPPER_BEAN_NAMES) {
            try {
                Object bean = context.getBean(beanName);
                if (bean instanceof net.sf.ehcache.Cache) {
                    ManagedCacheRegistry.registerReadSensitive((net.sf.ehcache.Cache) bean);
                }
            } catch (Exception e) {
                LogUtil.debug(getClass().getName(), "Platform cache bean not registered (not present in this context): " + beanName);
            }
        }
        // #2261: nonceCache is intentionally NOT registered with ManagedCacheRegistry at all (see
        // READ_SENSITIVE_DEFAULT_WRAPPER_BEAN_NAMES's own javadoc for why -- registering it would
        // deny OpenIDDirectoryManager's own legitimate put/get). But it is still `lazy-init="true"`
        // (commonsApplicationContext.xml), and EhCacheFactoryBean#getObject() creating it for the
        // FIRST time calls the shared cacheManager bean's own getCache(..), which is ITSELF guarded
        // against a restricted caller -- confirmed live: a non-admin tenant being the very first
        // request to ever touch this lazy bean gets a BeanCreationException wrapping
        // "SecurityException: No permission to access org.ehcache.jsr107.Eh107CacheManager", not a
        // clean "SECURE" denial, and OIDC login fails outright for that request (a worse failure
        // mode than the read-sensitive block this fix was written to avoid in the first place).
        // This is the exact "first caller wins" window the original report already flagged for
        // every lazy-init bean in this class; the fix is the same one applied there: force creation
        // here, in the system/admin context this listener always runs in (well before any tenant
        // traffic), a plain getBean(..) touch with NO registration call at all -- unlike
        // formPersistentClassCache above, nonceCache must stay completely unguarded by design.
        try {
            context.getBean("nonceCache");
        } catch (Exception e) {
            // #2261: kept at warn (not debug like the loop above) -- live-verification (2026-09-26)
            // found this eager touch does not reliably close nonceCache's own first-caller race
            // (see this task's PLANS.md entry for the open investigation); if it fails here too,
            // that is operationally significant enough to want visible in a real deployment's logs.
            LogUtil.warn(getClass().getName(), "nonceCache eager touch failed at context-refresh "
                    + "time -- the first genuine caller may still hit a raw BeanCreationException "
                    + "instead of this being pre-empted: " + e);
        }
    }
}
