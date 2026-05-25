package org.joget.plugin.base;

import freemarker.template.Template;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PluginManagerCache {

    private final Map<Class, Map<String, Plugin>> pluginCache = new ConcurrentHashMap<>();
    private final Map<String, Class> osgiPluginClassCache = new ConcurrentHashMap<>();
    private final List<String> noOsgiPluginClassCache = new CopyOnWriteArrayList<>();
    private final Map<String, Template> templateCache = new ConcurrentHashMap<>();
    private final List<String> noResourceBundleCache = new CopyOnWriteArrayList<>();
    private final Map<String, ResourceBundle> resourceBundleCache = new ConcurrentHashMap<>();
    private final Map<String, CustomPluginInterface> customPluginInterfaces = new ConcurrentHashMap<>();
    private volatile Date lastCleared = null;

    public Map<Class, Map<String, Plugin>> getPluginCache() {
        return pluginCache;
    }

    public Map<String, Class> getOsgiPluginClassCache() {
        return osgiPluginClassCache;
    }

    public List<String> getNoOsgiPluginClassCache() {
        return noOsgiPluginClassCache;
    }

    public Map<String, Template> getTemplateCache() {
        return templateCache;
    }

    public List<String> getNoResourceBundleCache() {
        return noResourceBundleCache;
    }

    public Map<String, ResourceBundle> getResourceBundleCache() {
        return resourceBundleCache;
    }

    public Map<String, CustomPluginInterface> getCustomPluginInterfaces() {
        return customPluginInterfaces;
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
        noResourceBundleCache.clear();
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
