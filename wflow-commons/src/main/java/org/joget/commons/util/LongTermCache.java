package org.joget.commons.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

public class LongTermCache {
    private Cache cache;
    private SetupManager setupManager;
    
    public void setCacheObject(Cache cache) {
        this.cache = cache;
    }
    
    public void setSetupManager(SetupManager setupManager) {
        this.setupManager = setupManager;
    }
    
    public Element get(String key) {
        Element element = null;
        element = cache.get(key);
        Long lastClear = getLastClearTime(key);
        if (element != null && lastClear != null && element.getCreationTime() <= lastClear) {
            cache.remove(key);
            LogUtil.debug(LongTermCache.class.getName(), key + " need to refresh.");
            element = null;
        }
        return element;
    }
    
    public void remove(String key) {
        Element element = cache.get(key);
        if (element != null) {
             cache.remove(key);
             setupManager.updateSetting("CACHE_LAST_CLEAR_" + key, Long.toString((new Date()).getTime()));
             LogUtil.debug(LongTermCache.class.getName(), key + " is removed.");
        }
    }
    
    /**
     * #2261: now a thin, backward-compatible alias for {@link #removeAllForCurrentProfile(String)}
     * -- {@code prefix} is interpreted the SAME way that method's own {@code localPrefix} is
     * (profile-relative, e.g. {@code "FORM_COLUMNS_"}), NOT as an already-profile-qualified full
     * prefix the way this method used to require. The old implementation trusted {@code prefix}
     * at face value and enumerated the whole cache directly via {@code cache.getKeys()}, which
     * meant {@code MultiTenantPluginAspect.managedCacheBulkReadGuardCheck} had to block it outright
     * for any restricted (non-admin) caller regardless of prefix -- there was no way for the guard
     * to know a caller-supplied prefix would only ever match that caller's own tenant's entries.
     * Delegating here closes that gap for every caller of this method, not only new ones written
     * against {@link #removeAllForCurrentProfile(String)} directly: this method had no remaining
     * production caller by the time of this change (its one real caller,
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
     * Scoped to the CALLING code's own current tenant profile instead of trusting a prefix at
     * face value.
     *
     * <p>Builds the full prefix as {@code currentProfile + "_" + localPrefix} -- this cache's own
     * existing, unchanged, already-in-production key convention -- then asks this cache's
     * registered {@code ManagedCacheRegistry.KeyScope} whether THAT prefix is provably safe to
     * bulk-remove by (see {@code UNDERSCORE_PREFIX_LONGEST_REAL_PROFILE_MATCH_SCOPE
     * #isCurrentTenantKeyPrefix(String)}'s own javadoc) BEFORE ever reaching {@code
     * cache.getKeys()}. Only once that check passes does this method's own frame become eligible
     * for {@code MultiTenantSecurityManager
     * .isCalledDirectlyFromLongTermCacheRemoveAllForCurrentProfile()}'s identity-verified
     * exemption -- the guard trusts that THIS method, and no other, only ever enumerates keys
     * after confirming it is safe; it does not itself re-derive that safety.
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
     *                    information
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
        List<String> keys = new ArrayList<String>();
        keys.addAll(cache.getKeys());
        for (String k : keys) {
            if (k.startsWith(prefix)) {
                cache.remove(k);
                setupManager.updateSetting("CACHE_LAST_CLEAR_" + k, Long.toString((new Date()).getTime()));
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
        Element element = new Element(key, value);
        
        if (requestTime != null) {
            element.getElementEvictionData().setCreationTime(requestTime.getTime());
        }
        
        put(element);
    }
   
    public void put(Element element) {
        cache.put(element);
        LogUtil.debug(LongTermCache.class.getName(), element.getObjectKey() + " is refreshed.");
    }
    
    public Long getLastClearTime(String key) {
        Long lastClear = null;
        String value = setupManager.getSettingValue("CACHE_LAST_CLEAR_" + key);
        if (value != null && !value.isEmpty()) {
            try {
                lastClear = Long.parseLong(value);
            } catch (Exception e) {
                LogUtil.error(LongTermCache.class.getName(), e, value);
            }
        }
        
        return lastClear;
    }
}
