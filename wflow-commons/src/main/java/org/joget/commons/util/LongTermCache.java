package org.joget.commons.util;

import java.util.Date;
import java.util.Iterator;
import javax.cache.Cache;
import javax.cache.CacheManager;
import net.sf.ehcache.Element;

public class LongTermCache {
    private Cache cache;
    private SetupManager setupManager;
    
    // #2317: wrap in net.sf.ehcache.Cache (Joget's own JSR-107 shim, matching
    // EhCacheFactoryBean.getObject()) so MultiTenantPluginAspect's guards -- which match by
    // declaring type, not by name -- can see this cache at all; the raw JSR-107 provider object
    // cacheManager.getCache(..) alone returns is a different class the aspect's pointcuts never
    // match. Registered with UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE -- NOT the more
    // widely-shared LEGACY_UNDERSCORE_PREFIX_SCOPE PlatformCacheRegistrar's own comment
    // documents as its intended scope for this cache's dormant "longTermCacheObject" bean-based
    // registration path (that path never fires on 9.1 today because no such bean exists here,
    // so this class self-registers its own field instead of depending on it): independent
    // review (Sol) found LEGACY_UNDERSCORE_PREFIX_SCOPE unconditionally denies the exemption
    // whenever the CALLING tenant's own profile name contains "_", which is common in practice
    // (a Joget Cloud profile is frequently exactly the tenant's underlying database name, e.g.
    // "customer_eu" -- confirmed via SetupServlet, which writes the admin-typed "Database Name"
    // field straight through with no character restriction beyond
    // SecurityUtil.validateStringInput's permissive regex, which explicitly allows "_") -- this
    // would have newly broken every such tenant's ResourceBundleMessageDao-driven message
    // caching (profile+"_"+locale keys) and FormColumnCache-driven form-column caching
    // (profile+"_FORM_COLUMNS_"/"_FORM_INDEXES_" keys), where before this session's fix (an
    // unwrapped, unguarded raw cache) they always succeeded. See ManagedCacheRegistry
    // .UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE's own javadoc for why it does not have
    // this problem. Also registers the alias so AppUtil.getCache("org.joget.cache.RBM_CACHE") --
    // the accessor plugin/BeanShell code actually uses -- returns this SAME governed instance
    // instead of falling through to its own separate, ungoverned raw fetch.
    // Independent review (Sol) finding: constructs into a LOCAL variable and registers it BEFORE
    // assigning to the field, deliberately -- setCacheManager(..) is itself now guarded (see
    // MultiTenantPluginAspect.appDefAndLongTermCacheSetterGuardCheck), but ManagedCacheRegistry.
    // register(..) is ALSO independently guarded and can throw for a restricted caller who
    // somehow still reaches this method; assigning the field only after registration succeeds
    // means a thrown SecurityException here leaves the field completely untouched, never pointing
    // at an attacker-supplied, unregistered CacheManager's cache.
    public void setCacheManager(CacheManager cacheManager) {
        net.sf.ehcache.Cache wrapped = new net.sf.ehcache.Cache(cacheManager.getCache("org.joget.cache.RBM_CACHE"));
        org.joget.commons.util.ManagedCacheRegistry.register(wrapped,
                org.joget.commons.util.ManagedCacheRegistry.UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE);
        org.joget.commons.util.PlatformCacheRegistrar.registerAlias(wrapped);
        this.cache = wrapped;
    }
    
    public void setSetupManager(SetupManager setupManager) {
        this.setupManager = setupManager;
    }
    
    public Object getObject(String key) {
        Object value = null;
        Element element = get(key);
        if (element != null) {
            value = element.getObjectValue();
        }
        return value;
    }

    /**
     * Like {@link #getObject(String)}, but does not trust a CACHE_LAST_CLEAR_ value that was read
     * through the debounced setup cache when that value reports the entry as still fresh.
     *
     * <p>Why this exists: {@link #get(String)} resolves CACHE_LAST_CLEAR_ via
     * {@code SetupManager.getSettingValue}, which is served from a cache that refreshes on a ~20
     * second debounce. So for ~20-25 seconds after another node invalidates an entry, this cache
     * reports its OLD value as current. That is the widest window of #2085: a stale Hibernate
     * mapping matched a stale column set and was served.</p>
     *
     * @param key
     * @return the cached value, or null if absent or verified stale
     */
    public Object getObjectFresh(String key) {
        Element element = getFresh(key);
        return (element != null) ? element.getObjectValue() : null;
    }

    /**
     * Like {@link #get(String)}, but verifies a surviving entry against the database.
     *
     * <p>The cheap path runs first and usually wins. A cached CACHE_LAST_CLEAR_ can only ever be
     * BEHIND the database, never ahead of it — the value only moves forward, and this node either
     * has seen a given advance or has not. So a cached value that ALREADY condemns the entry is
     * conclusive and needs no round trip; only an entry that still looks fresh can be wrong, and
     * only that case pays for a read.</p>
     *
     * <p>Eviction is identical to {@link #get(String)}, so callers need no new invalidation
     * handling: a caller that rebuilds on null keeps working unchanged.</p>
     */
    public Element getFresh(String key) {
        Element element = get(key);
        if (element == null) {
            // Absent, or the cached CACHE_LAST_CLEAR_ already condemned it. Either way, nothing
            // a database read could tell us would change the answer.
            return null;
        }

        Long lastClear = getLastClearTimeFresh(key);
        if (lastClear != null && element.getCreationTime() <= lastClear) {
            cache.remove(key);
            LogUtil.debug(LongTermCache.class.getName(), key + " need to refresh (verified against database).");
            return null;
        }
        return element;
    }

    /**
     * Backward compatible method for getting cache
     *
     * @param key
     */
    public Element get(String key) {
        Element element = (Element)cache.get(key);
        Long lastClear = getLastClearTime(key);
        if (element != null) {
            if (lastClear != null && element.getCreationTime() <= lastClear) {
                cache.remove(key);
                LogUtil.debug(LongTermCache.class.getName(), key + " need to refresh.");
                return null;
            }
        }
        return element;
    }
    
    public void remove(String key) {
        cache.remove(key);
        setupManager.updateSetting("CACHE_LAST_CLEAR_" + key, Long.toString((new Date()).getTime()));
        LogUtil.debug(LongTermCache.class.getName(), key + " is removed.");
    }
    
    /**
     * #2261: now a thin, backward-compatible alias for {@link
     * #removeAllForCurrentProfile(String)} -- {@code prefix} is interpreted the SAME way that
     * method's own {@code localPrefix} is (profile-relative, e.g. {@code "FORM_COLUMNS_"}), NOT
     * as an already-profile-qualified full prefix the way this method used to require. The old
     * implementation trusted {@code prefix} at face value and iterated the whole cache directly,
     * which meant {@code MultiTenantPluginAspect.managedCacheBulkReadGuardCheck} had to block it
     * outright for any restricted (non-admin) caller regardless of prefix -- there was no way for
     * the guard to know a caller-supplied prefix would only ever match that caller's own tenant's
     * entries. Delegating here closes that gap for every caller of this method, not only new ones
     * written against {@link #removeAllForCurrentProfile(String)} directly: this method had no
     * remaining production caller by the time of this change (its one real caller,
     * {@code FormColumnCache.clear()}, already moved to calling
     * {@link #removeAllForCurrentProfile(String)} directly), so there was no reason to keep a
     * second, deliberately-unscoped implementation around as a standing foot-gun for a future
     * caller to reach for by habit.
     *
     * <p>Tolerates an OLD-style caller that still passes an already-profile-qualified full prefix
     * (i.e. one that already starts with {@code currentProfile + "_"}): that exact leading segment
     * is stripped before delegating, so it is not prepended a second time and silently turned into
     * a no-op match. Any prefix NOT already so qualified is passed through unchanged, as a local
     * prefix.
     *
     * @param prefix the cache owner's own prefix content -- either already-profile-qualified (the
     *               historical convention) or a bare, tenant-agnostic local prefix (see {@link
     *               #removeAllForCurrentProfile(String)}'s {@code localPrefix} parameter)
     */
    public void removeAll(String prefix) {
        String currentProfile = DynamicDataSourceManager.getCurrentProfile();
        String localPrefix = prefix;
        if (currentProfile != null && !currentProfile.isEmpty() && prefix != null) {
            String qualifiedMarker = currentProfile + "_";
            if (prefix.startsWith(qualifiedMarker)) {
                localPrefix = prefix.substring(qualifiedMarker.length());
            }
        }
        removeAllForCurrentProfile(localPrefix);
    }

    /**
     * The actual, self-validating implementation {@link #removeAll(String)} now delegates to --
     * scoped to the CALLING code's own current tenant profile instead of trusting a prefix at
     * face value.
     *
     * <p>Builds the full prefix as {@code currentProfile + "_" + localPrefix} -- this cache's own
     * existing, unchanged, already-in-production key convention (see {@code
     * setCacheManager(CacheManager)}'s own registration comment) -- then asks this cache's
     * registered {@code ManagedCacheRegistry.KeyScope} whether THAT prefix is provably safe to
     * bulk-remove by (see {@code UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE
     * #isCurrentTenantKeyPrefix(String)}'s own javadoc) BEFORE ever reaching {@code
     * cache.iterator()}. Only once that check passes does this method's own frame become eligible
     * for {@code MultiTenantSecurityManager
     * .isCalledDirectlyFromLongTermCacheRemoveAllForCurrentProfile()}'s identity-verified
     * exemption -- the guard trusts that THIS method, and no other, only ever asks for the
     * iterator after confirming it is safe; it does not itself re-derive that safety.
     *
     * <p>Silently does nothing (logs a warning, does not throw) when the current profile is unset,
     * empty, or the prefix cannot be proven collision-safe against another real tenant profile --
     * a cache is never authoritative, so refusing a bulk clear is exactly as safe as performing it
     * would have been had the caller's intent genuinely been self-scoped; the caller simply gets a
     * stale (not missing) cache, the ordinary failure mode this cache already tolerates elsewhere.
     * Returns whether the removal actually proceeded, for a caller (e.g. {@code
     * FormColumnCache.clear()}) that also maintains its own separate, node-local state derived
     * from these same entries and must keep that state consistent with whether this cache was
     * genuinely cleared, rather than clearing both unconditionally regardless of a refusal here.
     *
     * @param localPrefix the cache owner's own, tenant-agnostic prefix content (e.g.
     *                    {@code "FORM_COLUMNS_"}) -- never itself containing tenant-identifying
     *                    information, matching {@link ProfileScopedCacheKey#forCurrentProfile(String)}'s
     *                    own {@code localKey} contract
     * @return true if the prefix was proven self-scoped and the cache was actually searched (even
     *         if zero entries happened to match); false if the removal was refused entirely
     */
    public boolean removeAllForCurrentProfile(String localPrefix) {
        String currentProfile = DynamicDataSourceManager.getCurrentProfile();
        if (currentProfile == null || currentProfile.isEmpty()) {
            LogUtil.warn(LongTermCache.class.getName(),
                    "removeAllForCurrentProfile skipped: no current tenant profile to scope to.");
            return false;
        }
        String prefix = currentProfile + "_" + localPrefix;
        if (!ManagedCacheRegistry.isCurrentTenantKeyPrefix(cache, prefix)) {
            LogUtil.warn(LongTermCache.class.getName(),
                    "removeAllForCurrentProfile refused for prefix `" + prefix + "` -- not provably "
                            + "self-scoped to the current tenant profile alone (either this cache has "
                            + "no registered prefix-aware KeyScope, or another real tenant profile "
                            + "collides with it).");
            return false;
        }
        for (Iterator i=cache.iterator(); i.hasNext();) {
            Cache.Entry entry = (Cache.Entry)i.next();
            if (entry.getKey().toString().startsWith(prefix)) {
                i.remove();
                setupManager.updateSetting("CACHE_LAST_CLEAR_" + entry.getKey().toString(), Long.toString((new Date()).getTime()));
            }
        }
        LogUtil.debug(LongTermCache.class.getName(), "All caches with `"+prefix+"` prefix are removed for the current profile.");
        return true;
    }

    public void putObject(String key, Object value) {
        putObject(key, value, null);
    }
    
    /**
     * This is used for long processing time cache data. 
     * Keep its requesting time instead of cache creation time to prevent stale cache
     * 
     * @param key
     * @param value
     * @param requestTime 
     */
    public void putObject(String key, Object value, Date requestTime) {
        if (requestTime == null) {
            requestTime = new Date();
        }
        
        Element element = new Element(key, value, requestTime.getTime());
        put(element);
    }
    
    /**
     * Backward compatible method for adding cache 
     * 
     * @param element 
     */
    public void put(Element element) {
        cache.put(element.getKey(), element);
        LogUtil.debug(LongTermCache.class.getName(), element.getKey() + " is refreshed.");
    }
    
    public Long getLastClearTime(String key) {
        return parseLastClearTime(setupManager.getSettingValue("CACHE_LAST_CLEAR_" + key));
    }

    /**
     * The same value as {@link #getLastClearTime(String)}, read straight from the database instead
     * of through the debounced setup cache. One indexed read — see
     * {@code SetupManager.getSettingValuesFresh}.
     *
     * <p><b>Takes the MAXIMUM across duplicate rows.</b> {@code wf_setup.property} is not unique and
     * {@code updateSetting} resolves the existing row through the debounced cache, so two nodes
     * invalidating a given key for the first time can each insert their own row. Thereafter each
     * node keeps updating "its" row and an arbitrary pick can be permanently behind — the node would
     * never see the other's invalidations, which is #2085 with extra steps. The maximum is the
     * correct resolution rather than a heuristic: this value only ever moves forward, so the newest
     * duplicate is the only one that reflects every invalidation seen so far.</p>
     */
    public Long getLastClearTimeFresh(String key) {
        Long latest = null;
        for (String value : setupManager.getSettingValuesFresh("CACHE_LAST_CLEAR_" + key)) {
            Long parsed = parseLastClearTime(value);
            if (parsed != null && (latest == null || parsed.longValue() > latest.longValue())) {
                latest = parsed;
            }
        }
        return latest;
    }

    private Long parseLastClearTime(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                return Long.parseLong(value);
            } catch (Exception e) {
                LogUtil.error(LongTermCache.class.getName(), e, value);
            }
        }
        return null;
    }
}
