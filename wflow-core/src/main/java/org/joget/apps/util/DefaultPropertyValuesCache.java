package org.joget.apps.util;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.base.Plugin;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;



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
            cache = (Cache) AppUtil.getApplicationContext().getBean("defaultPropertyValuesCache");
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
            // Check cache first
            Element element = cache.get(cacheKey);
            if (element != null) {
                return (String) element.getObjectValue();
            }
            // Compute and store in cache
            String computed = PropertyUtil.getDefaultPropertyValues(propertyOptions);

            cache.put(new Element(cacheKey, computed));
            return computed;

        } catch (Exception e) {
            LogUtil.error(DefaultPropertyValuesCache.class.getName(), e, "Error getting default property values");
            return PropertyUtil.getDefaultPropertyValues(propertyOptions);
        }
    }

    /**
     * Builds a cache key that is unique for class, version, locale, and profile.
     */
    private static String buildCacheKey(String className, String version) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(className)
                  .append(":")
                  .append(version != null ? version : "1.0")
                  .append(":")
                  .append(AppUtil.getAppLocale());
        String profileId = getCurrentProfileId();
        if (profileId != null) {
            keyBuilder.append(":profile:").append(profileId);
        }
        return keyBuilder.toString();
    }

    /**
     * Gets the current profile ID from {@link DynamicDataSourceManager}.
     */
    private static String getCurrentProfileId() {
        try {
            String profile = DynamicDataSourceManager.getCurrentProfile();
            if (profile != null && !profile.trim().isEmpty()) {
                return profile.trim();
            }
        } catch (Exception e) {
            LogUtil.info(DefaultPropertyValuesCache.class.getName(), "Could not determine profile: " + e.getMessage());
        }
        return DynamicDataSourceManager.DEFAULT_PROFILE;
    }

    /**
     * Evicts the cache if PluginManager has cleared its cache since the last check.
     */
    private static void clearCacheIfNeeded() {
        try {
            PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
            Date lastCleared = pluginManager.lastClearedCache();
            Date prev = lastClearCache.get();

            if (lastCleared != null && (prev == null || prev.before(lastCleared)) && lastClearCache.compareAndSet(prev, lastCleared)) {
                cache.removeAll();
                LogUtil.info(DefaultPropertyValuesCache.class.getName(), "Cleared default property values cache");
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