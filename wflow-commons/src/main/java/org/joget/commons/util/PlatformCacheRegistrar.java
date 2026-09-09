package org.joget.commons.util;

import java.util.LinkedHashMap;
import java.util.Map;
import net.sf.ehcache.Ehcache;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Registers every raw platform Ehcache bean into {@link ManagedCacheRegistry} once, at application
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
 */
public class PlatformCacheRegistrar implements ApplicationListener<ContextRefreshedEvent> {

    // Each cache's ManagedCacheRegistry.KeyScope, so a non-admin caller's own single-key
    // put/remove against a key it genuinely owns can be exempted from the outright mutation
    // block -- null means no exemption (the conservative default for a cache whose key convention
    // is not confirmed to carry tenant-scoping at all).
    //
    // fluCache: "<profile>_" prefix scope, mutation guard only (not read-scoped -- see
    // READ_SCOPED_CACHE_NAMES below). Its real key builders (AppDefCache.put(..)/
    // FormDefinitionDaoImpl.getCacheKey(..)) match that prefix -- NOT AppDefCache.removeAll(..),
    // which enumerates via cache.getKeys() first and so is gated by a SEPARATE, unconditional
    // bulk-enumeration pointcut this scope (or any KeyScope) has no effect on; still blocked for
    // every restricted caller regardless of this registration, a pre-existing, out-of-scope gap.
    //
    // userviewMenuCache/formOptionsCache: each is written with a key shape that never matched
    // LEGACY_UNDERSCORE_PREFIX_SCOPE's leading "<profile>_" convention, so each now uses a scope
    // built for its own real shape (see ManagedCacheRegistry's javadoc on each):
    // userviewMenuCache matches either "USERVIEW:" + profile + ":..." (UserviewCache) or the
    // legacy "<profile>_..." shape (AppDevUtil.dirLastModified(..)) -- both are real callers of
    // the same cache, so USERVIEW_MENU_CACHE_SCOPE recognizes either. formOptionsCache matches
    // "FORMOPTIONS::" + profile + "::...". Neither is read-scoped, so this only affects the
    // mutation guard's self-scoped exemption, never blocks a legitimate reader.
    //
    // setupManagerCache: registered with NO scope (null), deliberately. Its real writers
    // (AppWorkflowHelper.getReplacementUsers(..): "profile:USER_REPLACEMENT_...";
    // DefaultSystemKeyGenerator.isSystemKeyValid(..): "profile:networkInterfaces") share only a
    // "<profile>:" prefix, and a scope that recognized that prefix would exempt a restricted
    // caller's put/remove against its own profile's ENTIRE "<profile>:*" namespace in this cache,
    // not just those two shapes. That is a real escalation: getReplacementUsers(..) returns a
    // cache hit verbatim with no DB fallback check, so a forged "USER_REPLACEMENT_..." entry
    // redirects a real user's workflow task assignments outright, and a forged
    // "networkInterfaces" entry can influence DefaultSystemKeyGenerator's license-activation
    // check. There is no legitimate restricted-caller traffic to this cache that an exemption
    // would unblock, so every restricted caller's put/remove against it stays unconditionally
    // blocked.
    //
    // cssCache/rowCountCache/defaultPropertyValuesCache: their real key formats (raw LESS source
    // with no tenant info; a literal "Profile::" + profile + "(hash)", where profile is not a
    // leading prefix; and a trailing "class:version:locale:profile:<id>" segment, respectively)
    // never matched the legacy prefix convention, so every restricted caller's write to them was
    // unconditionally blocked -- and cssCache's own uncaught write site turned that into a hard
    // failure, not just a silent cache-disable, for any request with untrusted code on the stack.
    // Their key-building code (UniversalTheme/XadminTheme/AjaxUniversalTheme, DataList,
    // DefaultPropertyValuesCache) now builds keys via ProfileScopedCacheKey, so this scope
    // actually matches what they write.
    //
    // resourceBundleMessageCache: NO_TENANT_KEY_SCOPE, deny-all for both mutation and read. Has no
    // Java consumer anywhere in this codebase, so the deny-all is inert, not a regression risk.
    //
    // longTermCacheObject backs org.joget.cache.RBM_CACHE, the real cache behind
    // ResourceBundleMessageDao.getMessage(..) (reached from every ResourceBundleUtil.getMessage(..)
    // call) and FormColumnCache. Unlike resourceBundleMessageCache it has real, legitimate
    // restricted-caller read traffic, so it must NOT be deny-all or read-scoped: a blocked read
    // here would throw for any plugin/BeanShell code resolving a message key, which
    // ResourceBundleUtil's blanket catch would swallow -- silently degrading every such lookup to
    // the raw message code instead of the tenant's translation. Its actual key IS reliably
    // tenant-scoped (ResourceBundleMessageDao.getCacheKey(..): profile + "_" + locale, the same
    // convention fluCache uses), so it is registered the same way: mutation guard only, read left
    // unguarded (see fluCache's own registration comment below for which KeyScope and why).
    private static final Map<String, ManagedCacheRegistry.KeyScope> PLATFORM_CACHE_KEY_SCOPES =
            new LinkedHashMap<String, ManagedCacheRegistry.KeyScope>();
    static {
        // #2317/#2261 (live-reported): LEGACY_UNDERSCORE_PREFIX_SCOPE unconditionally denies the
        // self-scoped exemption whenever the CALLING tenant's own profile name contains "_" --
        // common in practice (a Joget Cloud profile is frequently exactly the tenant's own
        // database name, and this deployment's own tenant-naming convention is literally
        // "jwc_<name>"). That denial broke every AppDefCache.put(..) for such a tenant outright
        // (observed: PluginDefaultPropertiesDaoImpl.loadById(..), reached from ordinary
        // hash-variable processing), not just a genuine collision -- see
        // UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE's own javadoc for why the narrower
        // check is safe. The 9.1 line closed this same underscore-collision defect differently
        // (AppDefCache there self-registers its own wrapped cache directly, bypassing this
        // bean-name-keyed registration path entirely); on 8.2, fluCache is still wired into
        // AppDefCache purely through Spring XML (appsApplicationContext.xml: appFluCache's
        // cacheObject ref="fluCache") with no self-registration of its own, so THIS registration
        // is the only place a fix can land.
        PLATFORM_CACHE_KEY_SCOPES.put("fluCache", ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("userviewMenuCache", ManagedCacheRegistry.USERVIEW_MENU_CACHE_SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("formOptionsCache", ManagedCacheRegistry.FORM_OPTIONS_CACHE_SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("cssCache", ProfileScopedCacheKey.SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("rowCountCache", ProfileScopedCacheKey.SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("resourceBundleMessageCache", ManagedCacheRegistry.NO_TENANT_KEY_SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("setupManagerCache", null);
        // Same underscore-collision defect and same fix as fluCache immediately above -- this
        // cache's real key builder (ResourceBundleMessageDao.getCacheKey(..): profile + "_" +
        // locale) uses the identical "<profile>_..." shape.
        PLATFORM_CACHE_KEY_SCOPES.put("longTermCacheObject", ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE);
        PLATFORM_CACHE_KEY_SCOPES.put("defaultPropertyValuesCache", ProfileScopedCacheKey.SCOPE);
    }

    // Which of the caches above also get single-key get()/getQuiet() redirected to the caller's own
    // key space (or, when no own key can be built, read as a miss) for a restricted caller whose key
    // the registered KeyScope does not recognize as its own -- ManagedCacheRegistry.registerReadScoped(..)
    // rather than plain register(..).
    //
    // fluCache/userviewMenuCache/formOptionsCache ARE read-scoped: every real key they hold leads with
    // the current profile (AppDefCache "<profile>_<appId>_...", UserviewCache "USERVIEW:<profile>:...",
    // FormOptionsCacheAspect "FORMOPTIONS::<profile>::..." / "LAST_ACTIVE_FORMOPTIONS::<profile>::..."),
    // each matched by that cache's own scope (fluCache uses the LONGEST scope, so a profile name that
    // itself contains '_' is still recognized), so a tenant's own read passes through untouched and
    // only a key outside its own space is redirected; every reader rebuilds on a null miss.
    //
    // cssCache/rowCountCache/defaultPropertyValuesCache are read-scoped because their real keys
    // (via ProfileScopedCacheKey) reliably carry tenant-scoping. resourceBundleMessageCache is
    // read-scoped with NO_TENANT_KEY_SCOPE so its "no exemption" policy is enforced on reads too,
    // not just mutation -- it has no real consumer, so this is inert.
    //
    // setupManagerCache and longTermCacheObject are deliberately NOT here: setupManagerCache stays
    // null-scoped (some keys cannot be represented by any profile scope, and a scope would re-enable
    // the self-scoped mutation exemption the null registration denies), and longTermCacheObject's
    // RBM read traffic (ResourceBundleUtil / LicenseManager) mixes profile-led and non-profile-led
    // keys -- both need the same dedicated-scope treatment, left as a reviewed follow-up.
    private static final java.util.Set<String> READ_SCOPED_CACHE_NAMES = new java.util.HashSet<String>(
            java.util.Arrays.asList("cssCache", "rowCountCache", "defaultPropertyValuesCache",
                    "resourceBundleMessageCache", "fluCache", "userviewMenuCache", "formOptionsCache"));

    // #2261 H1 (ported from 9.1; DIFFERENT root cause on this line -- see below):
    // {@code nonceCache}/{@code formPersistentClassCache} (appsApplicationContext.xml,
    // {@code org.springframework.cache.ehcache.EhCacheFactoryBean}, no {@code cacheName}
    // property) were never registered with {@link ManagedCacheRegistry} by bean name here.
    //
    // Unlike 9.1 -- where Joget's OWN {@code EhCacheFactoryBean} hardcodes {@code cacheName =
    // "default"} as its Java field default, so an unset {@code cacheName} genuinely means "wraps
    // the one shared cache region literally named default", the same one
    // {@code SecureDataEncryptionImpl} fills with decrypted values -- Spring's OWN {@code
    // org.springframework.cache.ehcache.EhCacheFactoryBean#afterPropertiesSet()} falls back to
    // its OWN {@code beanName} when {@code cacheName} is unset (confirmed by inspecting its
    // bytecode: {@code getName() == null ? this.beanName : getName()}), so {@code nonceCache}/
    // {@code formPersistentClassCache} each get their OWN, distinct, separately-named cache
    // region on THIS line -- there is no shared-"default"-cache collision with
    // {@code SecureDataEncryptionImpl} here at all.
    //
    // The real 8.2 gap is narrower but still genuine: each bean's OWN cache region (nonces;
    // Hibernate {@code PersistentClass} metadata) was reachable, unguarded, by whichever caller
    // got the bean by id.
    // - {@code nonceCache} is ALREADY effectively covered as a SIDE EFFECT of Spring wiring: the
    //   {@code dataEncryption} bean (epApplicationContext.xml, joget-enterprise) has
    //   {@code <property name="cache" ref="nonceCache"/>}, and {@code
    //   SecureDataEncryptionImpl.setCache(..)} itself calls {@code
    //   ManagedCacheRegistry.registerReadSensitive(cache)} on that EXACT bean object. Registered
    //   here too anyway, for the same reason {@code dynamicDataSourceManagerCache} gets its own
    //   explicit registration on 9.1 rather than relying solely on a single owning setter: a
    //   dedicated, bean-name-keyed registration does not depend on an unrelated bean
    //   (joget-enterprise's {@code dataEncryption}, not even in this module) happening to be
    //   wired first, or at all, or continuing to reference the right bean id in some future
    //   refactor -- see the very next point for live proof that assumption breaks.
    // - {@code formPersistentClassCache} was genuinely UNPROTECTED: {@code formDataDao}'s own
    //   {@code formPersistentClassCache} property (appsApplicationContext.xml) is wired to
    //   {@code ref="formSessionFactoryCache"} -- a pre-existing, independently-discovered copy/
    //   paste bug (this property should reference the {@code formPersistentClassCache} bean, not
    //   {@code formSessionFactoryCache}, which is a DIFFERENT cache, {@code SF_CACHE}), fixed
    //   alongside this registration in the same commit. Because of that bug, {@code
    //   FormDataDaoImpl.setFormPersistentClassCache(..)}'s own {@code registerReadSensitive(..)}
    //   call was registering {@code formSessionFactoryCache}'s object a second time (redundant,
    //   harmless) instead of the real {@code formPersistentClassCache} bean, leaving THAT bean's
    //   own cache region completely unregistered -- exactly the kind of silent gap a dedicated,
    //   bean-name-keyed registration here is meant to be resilient against.
    //
    // Neither bean is {@code lazy-init} on this line (no such attribute on either bean
    // definition), so -- unlike 9.1's {@code nonceCache}/{@code formPersistentClassCache}, both
    // explicitly {@code lazy-init="true"} -- there is no "first caller wins" creation-order
    // window to close here either; both beans already exist by the time
    // {@link ContextRefreshedEvent} fires.
    private static final java.util.Set<String> READ_SENSITIVE_BEAN_NAMES = new java.util.HashSet<String>(
            java.util.Arrays.asList("nonceCache", "formPersistentClassCache"));

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        for (Map.Entry<String, ManagedCacheRegistry.KeyScope> entry : PLATFORM_CACHE_KEY_SCOPES.entrySet()) {
            String beanName = entry.getKey();
            try {
                Object bean = context.getBean(beanName);
                if (bean instanceof Ehcache) {
                    if (READ_SCOPED_CACHE_NAMES.contains(beanName)) {
                        ManagedCacheRegistry.registerReadScoped((Ehcache) bean, entry.getValue());
                    } else {
                        ManagedCacheRegistry.register((Ehcache) bean, entry.getValue());
                    }
                }
            } catch (Exception e) {
                // Bean not present in this context/edition -- not every profile wires every one
                // of these, and a missing bean here is not itself a security concern.
                LogUtil.debug(getClass().getName(), "Platform cache bean not registered (not present in this context): " + beanName);
            }
        }
        // The two read-sensitive-only beans above (see READ_SENSITIVE_BEAN_NAMES's own javadoc):
        // no KeyScope-based exemption -- a single-key get() of even the caller's own key is still
        // a real concern for nonces/persistent-class metadata, the same tier
        // dynamicDataSourceManagerCache (9.1) and SecureDataEncryptionImpl's own registration use.
        for (String beanName : READ_SENSITIVE_BEAN_NAMES) {
            try {
                Object bean = context.getBean(beanName);
                if (bean instanceof Ehcache) {
                    ManagedCacheRegistry.registerReadSensitive((Ehcache) bean);
                }
            } catch (Exception e) {
                LogUtil.debug(getClass().getName(), "Platform cache bean not registered (not present in this context): " + beanName);
            }
        }
    }
}
