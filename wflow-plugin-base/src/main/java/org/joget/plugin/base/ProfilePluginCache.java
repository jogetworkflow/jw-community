package org.joget.plugin.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;

/**
 * Class to cache plugin manager data
 */
public class ProfilePluginCache {

    private final Map<String, PluginManagerCache> profileCacheMap = new ConcurrentHashMap<>();

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
        return profileCacheMap.computeIfAbsent(profile, k -> new PluginManagerCache());
    }
}
