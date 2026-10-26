package org.joget.plugin.base;

import freemarker.template.Template;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class PluginManagerCache {

    // Striped locks keep cold ResourceBundle loads out of pluginListLock and
    // ConcurrentHashMap compute callbacks while avoiding one lock per cache key.
    private static final int RESOURCE_BUNDLE_LOCK_STRIPES = 64;

    @SuppressWarnings("rawtypes")
    private final Map<Class, Map<String, Plugin>> pluginCache = new ConcurrentHashMap<>();
    @SuppressWarnings("rawtypes")
    private final Map<String, Class> osgiPluginClassCache = new ConcurrentHashMap<>();
    private final Set<String> noOsgiPluginClassCache = ConcurrentHashMap.newKeySet();
    private final Map<String, Template> templateCache = new ConcurrentHashMap<>();
    private final Map<String, ResourceBundle> resourceBundleCache = new ConcurrentHashMap<>();
    private final Map<String, CustomPluginInterface> customPluginInterfaces = new ConcurrentHashMap<>();
    private volatile Date lastCleared = null;
    // Per-profile lock guarding plugin-map loads. This cache is resolved per
    // profile, so loads for different tenants no longer serialize on a single
    // process-wide lock.
    private final ReentrantLock pluginListLock = new ReentrantLock();
    // OSGi plugin services of this profile, woven lazily per requested type by
    // PluginManager.getOsgiPlugins. Cleared together with the rest of the cache so
    // bundle install/uninstall/refresh rebuilds it.
    private volatile OsgiPluginIndex osgiPluginIndex = null;
    private volatile long refreshGeneration = 0;
    private final ReentrantLock[] resourceBundleLocks = createResourceBundleLocks();

    @SuppressWarnings("rawtypes")
    public Map<Class, Map<String, Plugin>> getPluginCache() {
        return pluginCache;
    }

    @SuppressWarnings("rawtypes")
    public Map<String, Class> getOsgiPluginClassCache() {
        return osgiPluginClassCache;
    }

    public Set<String> getNoOsgiPluginClassCache() {
        return noOsgiPluginClassCache;
    }

    public Map<String, Template> getTemplateCache() {
        return templateCache;
    }

    public Map<String, ResourceBundle> getResourceBundleCache() {
        return resourceBundleCache;
    }

    public Map<String, CustomPluginInterface> getCustomPluginInterfaces() {
        return customPluginInterfaces;
    }

    public ReentrantLock getPluginListLock() {
        return pluginListLock;
    }

    public ReentrantLock getResourceBundleLock(String cacheKey) {
        return resourceBundleLocks[(cacheKey.hashCode() & Integer.MAX_VALUE) % resourceBundleLocks.length];
    }

    public long getRefreshGeneration() {
        return refreshGeneration;
    }

    public void incrementRefreshGeneration() {
        refreshGeneration++;
    }

    OsgiPluginIndex getOsgiPluginIndex() {
        return osgiPluginIndex;
    }

    void setOsgiPluginIndex(OsgiPluginIndex osgiPluginIndex) {
        this.osgiPluginIndex = osgiPluginIndex;
    }

    public Date getLastCleared() {
        return lastCleared;
    }

    public void setLastCleared(Date lastCleared) {
        this.lastCleared = lastCleared;
    }

    public void clearCache() {
        pluginCache.clear();
        osgiPluginClassCache.clear();
        noOsgiPluginClassCache.clear();
        templateCache.clear();
        resourceBundleCache.clear();
        osgiPluginIndex = null;
        lastCleared = new Date();
    }

    public boolean isCleared(Date date) {
        Date clearedDate = lastCleared;
        if (clearedDate == null) {
            return false;
        }
        return clearedDate.after(date);
    }

    private static ReentrantLock[] createResourceBundleLocks() {
        ReentrantLock[] locks = new ReentrantLock[RESOURCE_BUNDLE_LOCK_STRIPES];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock();
        }
        return locks;
    }

    /**
     * Unwoven OSGi plugin service objects of one profile and the woven plugin of each
     * service woven so far. A null woven value records a failed weave so it is not
     * retried until the cache is cleared. Replaced as a whole by clearCache(), so a
     * reader holding an old index never sees it partly cleared. The woven map is
     * guarded by the profile's pluginListLock. Package-private so the unwoven
     * service objects are not reachable through the public cache API.
     */
    static class OsgiPluginIndex {
        private final List<Plugin> services;
        private final Map<Plugin, Plugin> woven = new IdentityHashMap<>();

        OsgiPluginIndex(List<Plugin> services) {
            this.services = Collections.unmodifiableList(services);
        }

        List<Plugin> getServices() {
            return services;
        }

        boolean isWoven(Plugin service) {
            return woven.containsKey(service);
        }

        Plugin getWoven(Plugin service) {
            return woven.get(service);
        }

        void putWoven(Plugin service, Plugin plugin) {
            woven.put(service, plugin);
        }
    }
}
