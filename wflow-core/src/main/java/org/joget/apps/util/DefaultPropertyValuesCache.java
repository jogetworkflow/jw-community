package org.joget.apps.util;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.commons.util.ProfileScopedCacheKey;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.base.Plugin;

import javax.cache.Cache;

/**
 * A centralized cache for default property values of plugins.
 * 
 * <p>This implementation uses Ehcache for caching and supports:
 * - Automatic invalidation when PluginManager clears caches.
 * - Manual invalidation through {@link #clearCache()}.
 */
public class DefaultPropertyValuesCache {
    private static Cache cache;
    private static final AtomicReference<Date> lastClearCache = new AtomicReference<>();

    static{
        try {
            cache = AppUtil.getCache("org.joget.cache.DEFAULT_PROPERTY_VALUES_CACHE");
        } catch (Exception e) {
            LogUtil.error(DefaultPropertyValuesCache.class.getName(), e, "Error initializing cache");
        }
    }

    /**
     * Gets default property values for the given plugin instance, using cache.
     *
     * @param plugin the plugin instance implementing {@link PropertyEditable}
     * @return default property values as JSON string, never null
     */
    public static String getDefaultPropertyValues(PropertyEditable plugin) {
        if (plugin == null) return "{}";
        String className = plugin.getClass().getName();
        String version = (plugin instanceof Plugin) ? ((Plugin) plugin).getVersion() : "1.0";
        return getDefaultPropertyValues(className, version, plugin.getPropertyOptions());
    }

    /**
     * Gets default property values with explicit parameters, using cache.
     *
     * @param className the plugin class name
     * @param version the plugin version
     * @param propertyOptions the property options JSON string
     * @return default property values as JSON string, never null
     */
    private static String getDefaultPropertyValues(String className, String version, String propertyOptions) {
        try {
            // cache should never be null, but just in case
            if (cache == null) {
                LogUtil.warn(DefaultPropertyValuesCache.class.getName(), "defaultPropertyValuesCache is null");
                return PropertyUtil.getDefaultPropertyValues(propertyOptions);
            }
            clearCacheIfNeeded();
            String cacheKey = buildCacheKey(className, version);
            if (cacheKey == null) {
                // Current profile cannot be safely scoped into a key (see
                // ProfileScopedCacheKey.forCurrentProfile(..)'s own javadoc) -- compute without
                // caching rather than fall back to an unscoped key.
                return PropertyUtil.getDefaultPropertyValues(propertyOptions);
            }
            // Check cache first
            String cached = (String) cache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            // Compute and store in cache
            String computed = PropertyUtil.getDefaultPropertyValues(propertyOptions);

            cache.put(cacheKey, computed);
            return computed;

        } catch (Exception e) {
            LogUtil.error(DefaultPropertyValuesCache.class.getName(), e, "Error getting default property values");
            return PropertyUtil.getDefaultPropertyValues(propertyOptions);
        }
    }

    /**
     * Builds a cache key that is unique for class, version, locale, and profile -- profile-scoped
     * via {@link ProfileScopedCacheKey} (not a trailing ":profile:<id>" segment built by hand
     * here) so the multi-tenant cache-mutation guard (Cloud/Enterprise edition) can recognize a
     * non-admin caller's own entry via the same {@link ProfileScopedCacheKey#SCOPE} registered
     * for this cache in {@code PlatformCacheRegistrar}.
     *
     * @return the key, or {@code null} if the current profile cannot be safely scoped (see
     *         {@link ProfileScopedCacheKey#forCurrentProfile(String)})
     */
    private static String buildCacheKey(String className, String version) {
        String localKey = className
                + ":" + (version != null ? version : "1.0")
                + ":" + AppUtil.getAppLocale();
        return ProfileScopedCacheKey.forCurrentProfile(localKey);
    }

    /**
     * Evicts the cache if PluginManager has cleared its cache since the last check.
     */
    private static void clearCacheIfNeeded() {
        try {
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            Date lastCleared = pluginManager.lastClearedCache();
            Date prev = lastClearCache.get();

            if (lastCleared != null && (prev == null || prev.before(lastCleared))) {
                // Clear FIRST, advance the timestamp only after removeAll() actually succeeds --
                // if a non-admin/untrusted caller reaches this while cache.removeAll() is itself
                // guarded (a SecurityException from the mutation guard), the timestamp must stay
                // where it was so a later, legitimately-privileged call still sees invalidation as
                // still-pending, instead of believing (from an advanced timestamp) that a clear
                // which never actually happened already did.
                try {
                    cache.removeAll();
                    lastClearCache.compareAndSet(prev, lastCleared);
                    LogUtil.info(DefaultPropertyValuesCache.class.getName(), "Cleared default property values cache");
                } catch (SecurityException e) {
                    // Expected, not exceptional -- a restricted (non-admin/plugin) caller is
                    // correctly blocked from bulk-clearing a cache shared by every tenant, per the
                    // Cloud/Enterprise multi-tenant mutation guard. Because the timestamp stays
                    // pending (above), this retries on every such call until an admin-profile
                    // caller succeeds -- logging at ERROR every time would be a permanent log
                    // storm for normal restricted-tenant traffic, not a real error.
                    LogUtil.debug(DefaultPropertyValuesCache.class.getName(),
                            "Deferred default property values cache clear: restricted caller cannot bulk-clear a shared cache");
                }
            }
        } catch (Exception e) {
            LogUtil.error(DefaultPropertyValuesCache.class.getName(), e, "Error checking cache clear status");
        }
    }

    /**
     * To manually clear cache.
     */
    public static void clearCache() {
        try {
            cache.removeAll();
            lastClearCache.set(new Date());
            LogUtil.info(DefaultPropertyValuesCache.class.getName(), "Cache cleared manually");
        } catch (Exception e) {
            LogUtil.error(DefaultPropertyValuesCache.class.getName(), e, "Error clearing cache manually");
        }
    }
}