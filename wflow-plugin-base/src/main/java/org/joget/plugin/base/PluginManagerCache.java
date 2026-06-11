package org.joget.plugin.base;

import freemarker.template.Template;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class PluginManagerCache {

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
    private final ReentrantLock pluginListLoadLock = new ReentrantLock();
    // All woven OSGi plugins for this profile, built once and filtered by type
    // by PluginManager.internalLoadPluginMap. Cleared together with the rest of
    // the cache so bundle install/uninstall/refresh rebuilds it.
    private volatile Map<String, Plugin> allOsgiPlugins = null;

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

    public ReentrantLock getPluginListLoadLock() {
        return pluginListLoadLock;
    }

    public Map<String, Plugin> getAllOsgiPlugins() {
        return allOsgiPlugins;
    }

    public void setAllOsgiPlugins(Map<String, Plugin> allOsgiPlugins) {
        this.allOsgiPlugins = allOsgiPlugins;
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
        allOsgiPlugins = null;
        lastCleared = new Date();
    }

    public boolean isCleared(Date date) {
        Date clearedDate = lastCleared;
        if (clearedDate == null) {
            return false;
        }
        return clearedDate.after(date);
    }
}
