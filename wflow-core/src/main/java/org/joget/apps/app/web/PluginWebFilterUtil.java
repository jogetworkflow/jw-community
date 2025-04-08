package org.joget.apps.app.web;

import java.util.HashMap;
import java.util.Map;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginWebFilter;

/**
 * A utility class to manage proxies and filters
 */
public class PluginWebFilterUtil {

    protected static Map<String, PluginWebFilterChain> proxies = new HashMap<String, PluginWebFilterChain>();
    
    public static PluginWebFilterChain getPluginFilterChainProxy(boolean afterSecurity) {
        String profile = DynamicDataSourceManager.getCurrentProfile() + "::" + afterSecurity;
        if (!proxies.containsKey(profile)) {
            proxies.put(profile, new PluginWebFilterChain());
        }
        return proxies.get(profile);
    }
    
    /**
     * register plugin web filter. 
     * @param filter 
     */
    public static void registerFilter(PluginWebFilter filter) {
        getPluginFilterChainProxy(filter.isPositionAfterSecurityFilter()).addFilter(filter);
        LogUtil.info(PluginWebFilterUtil.class.getName(), "Registered filter : " + filter.getName());
    }
    
    /**
     * Unregister plugin web filter. 
     * @param filter 
     */
    public static void unregisterFilter(PluginWebFilter filter) {
        getPluginFilterChainProxy(filter.isPositionAfterSecurityFilter()).removeFilter(filter);
        LogUtil.info(PluginWebFilterUtil.class.getName(), "Unregistered filter : " + filter.getName());
    }
}