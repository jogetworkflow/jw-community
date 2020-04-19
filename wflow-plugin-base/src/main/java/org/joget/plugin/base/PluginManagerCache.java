package org.joget.plugin.base;

import freemarker.template.Template;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class PluginManagerCache {
    
    private Map<Class, Map<String, Plugin>> pluginCache = new HashMap<Class, Map<String, Plugin>>();
    private Map<String, Class> osgiPluginClassCache = new HashMap<String, Class>();
    private List<String> noOsgiPluginClassCache = new ArrayList<String>();
    private Map<String, Template> templateCache = new HashMap<String, Template>();
    private List<String> noResourceBundleCache = new ArrayList<String>();
    private Map<String, ResourceBundle> resourceBundleCache = new HashMap<String, ResourceBundle>();
    private Map<String, CustomPluginInterface> customPluginInterfaces = new HashMap<String, CustomPluginInterface>();
    private Date lastCleared = null;

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
        customPluginInterfaces.clear();
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
