package org.joget.plugin.base;

import java.util.HashMap;
import java.util.Map;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;

/**
 * Class to cache plugin manager data
 */
public class ProfilePluginCache {

    private Map<String, PluginManagerCache> profileCacheMap = new HashMap<String, PluginManagerCache>();
    
    /**
     * Retrieve the cache for the current profile.
     * @return 
     */
    public PluginManagerCache getCache() {
        String profile = null;
        try {
            profile = DynamicDataSourceManager.getCurrentProfile();
        } catch (Exception e) {
            LogUtil.debug(ProfilePluginCache.class.getName(), profile);
        }
        if (profile == null) {
            profile = DynamicDataSourceManager.DEFAULT_PROFILE;
        }
        PluginManagerCache cache = profileCacheMap.get(profile);
        if (cache == null) {
            cache = new PluginManagerCache();
            profileCacheMap.put(profile, cache);
        }
        return cache;
    }
}
