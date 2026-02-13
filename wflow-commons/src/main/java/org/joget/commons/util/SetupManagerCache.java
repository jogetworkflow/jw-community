package org.joget.commons.util;

import org.joget.commons.spring.model.Setting;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.util.Collection;
import java.util.concurrent.*;

public class SetupManagerCache {
    public static final String SYSTEM_PROPERTY_SETUP_STALE_CACHE = "wflow.setupStaleCache";

    protected static final String TABLE_LAST_UPDATED_KEY = "setup_lastUpdated";
    private static final int DEBOUNCE_MS = 20000; // 20 seconds
    private static final int MAX_DEBOUNCE_JITTER_MS = 5000; // 5 seconds
    private static final ConcurrentMap<String, Long> CACHE_LAST_UPDATED = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Long> CACHE_LAST_CHECKED = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, CompletableFuture<Void>> PROFILE_IS_REFRESHING = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> THREAD_IS_REFRESHING = ThreadLocal.withInitial(() -> false);

    /**
     * Cache mapping: {@code Profile string -> ConcurrentMap<String, Setting>}.
     */
    private Cache<String, ConcurrentMap<String, Setting>> cache;
    private Cache<String, ConcurrentMap<String, Setting>> staleCache;
    private SetupDao setupDao;
    private SetupManagerHelper setupManagerHelper;

    /**
     * Method used by system to clear cache of a property of the current profile
     */
    protected void clearCache() {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        assertProfileNotNull(profile);
        if (cache != null) {
            cache.remove(profile);
        }
        if (staleCache != null) {
            staleCache.remove(profile);
        }
    }

    /**
     * Method used by system to update cache by property
     *
     * @param setting the Setting to update
     * @throws NullPointerException if Setting's property value is null
     */
    protected void updateCache(Setting setting) {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        updateCacheInternal(profile, setting);
        updateTableModifiedTimestamp(profile);
    }

    /**
     * Method used by system to refresh entire cache for the current profile.
     *
     * <p>The cache refreshes in a separate {@link PluginThread}, and therefore the completion time is undetermined.
     * If there is a synchronous requirement, please use the return value.</p>
     *
     * <p><b>Note: This operation is potentially expensive, as it has to retrieve all rows from {@link SetupDao}.</b></p>
     *
     * @return {@link CompletableFuture} for the refresh operation
     */
    protected CompletableFuture<Void> refreshCache() {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        return refreshCacheInternal(profile, false);
    }

    /**
     * Refresh the cache for the current profile, with optional debounce.
     *
     * @param profile  the profile key
     * @param debounce whether to respect debounce rules
     * @return {@link CompletableFuture} for the refresh operation
     */
    private CompletableFuture<Void> refreshCacheInternal(String profile, boolean debounce) {
        if (cache == null) {
            return CompletableFuture.failedFuture(new NullPointerException("No cache available"));
        }

        if (profile == null) {
            return CompletableFuture.failedFuture(new NullPointerException("Cannot obtain current profile"));
        }

        // Debounce check
        if (debounce) {
            long now = System.currentTimeMillis();
            long randomDelay = ThreadLocalRandom.current().nextLong(0, MAX_DEBOUNCE_JITTER_MS);
            long lastCheck = CACHE_LAST_CHECKED.getOrDefault(profile, 0L);
            boolean shouldCheck = now > lastCheck + DEBOUNCE_MS + randomDelay;
            if (!shouldCheck) {
                return CompletableFuture.completedFuture(null);
            }
            CACHE_LAST_CHECKED.put(profile, now);
        }

        // Single flight caching of CompletableFuture to execute refreshCacheFromDataSource
        // The refresh will only run once if multiple threads access the same key while the refresh is running.
        // However, if the same thread reenters, throw an exception to be handled by caller
        if (THREAD_IS_REFRESHING.get()) {
            return CompletableFuture.failedFuture(new IllegalThreadStateException("Current thread is refreshing"));
        }

        CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture<Void> result = PROFILE_IS_REFRESHING.putIfAbsent(profile, future);
        if (result != null) {
            return result; // another thread is already refreshing
        }

        boolean inCache = cache.containsKey(profile);
        Runnable refresh = () -> {
            try {
                // mark this thread as refreshing to prevent self-deadlock
                THREAD_IS_REFRESHING.set(true);
                long cacheLastUpdated = CACHE_LAST_UPDATED.getOrDefault(profile, 0L);
                long tableLastUpdated = getTableModifiedTimestamp();
                if (!debounce || tableLastUpdated > cacheLastUpdated) {
                    // on server startup it is not yet cached, so we do not check setting changes
                    refreshCacheFromDataSource(profile, inCache);
                }
                future.complete(null);
            } catch (Exception e) {
                future.completeExceptionally(e);
            } finally {
                PROFILE_IS_REFRESHING.remove(profile, future);
                THREAD_IS_REFRESHING.remove();
            }
        };

        // if NOT in cache, refresh synchronously, else asynchronously
        if (!inCache) {
            refresh.run();
        } else {
            CompletableFuture.runAsync(
                    refresh,
                    command -> new PluginThread(command).start()
            );
        }
        return future;
    }

    /**
     * Refresh cache from the datasource
     *
     * @param profile             the profile key
     * @param checkSettingChanges whether to check for setting changes
     */
    private void refreshCacheFromDataSource(String profile, boolean checkSettingChanges) {
        assertProfileNotNull(profile);
        LogUtil.debug(getClass().getName(), "Refreshing setup cache for " + profile);

        Collection<Setting> settings = getSetupDao().find("", null, null, null, null, null);

        // put all rows in a ConcurrentMap to cache
        ConcurrentMap<String, Setting> settingMap = new ConcurrentHashMap<>();
        for (Setting setting : settings) {
            String settingProperty = setting.getProperty();
            settingMap.put(settingProperty, setting);
        }
        cache.put(profile, settingMap);
        if (staleCache != null) {
            staleCache.put(profile, settingMap);
        }
        // update the cached timestamp to now
        long now = System.currentTimeMillis();
        CACHE_LAST_UPDATED.put(profile, now);
        // since we updated cache, reset last checked timer so it will not check again soon
        CACHE_LAST_CHECKED.put(profile, now);

        if (checkSettingChanges) {
            getSetupManagerHelper().checkSettingChanges(settingMap);
        }
    }

    /**
     * Get value from cache or obtain from database
     *
     * @param property the value of the setting
     * @return a Setting object
     * @throws NullPointerException if property is null
     */
    protected Setting get(String property) {
        if (property == null) {
            throw new NullPointerException("Property must not be null");
        }
        String profile = DynamicDataSourceManager.getCurrentProfile();
        // get settingMap from cache, load from stale and refresh if not exist
        ConcurrentMap<String, Setting> settingMap = cache.get(profile);
        if (settingMap == null) {
            CompletableFuture<Void> refreshed = refreshCache();
            if (staleCache != null) {
                settingMap = staleCache.get(profile);
            }
            // wait synchronously and get refreshed value from cache
            if (settingMap == null) {
                try {
                    refreshed.get(5, TimeUnit.SECONDS);
                } catch (Exception e) {
                    // if current thread is refreshing or timeout, we get it directly from database
                    if (e instanceof TimeoutException || e.getCause() instanceof IllegalThreadStateException) {
                        if (LogUtil.isDebugEnabled(getClass().getName()) || LogUtil.isDebugEnabled(SetupManager.class.getName())) {
                            LogUtil.error(getClass().getName(), e, Thread.currentThread().getName() + " -- error getting property " + property + " for profile " + profile);
                        }
                        Collection<Setting> result = getSetupDao().find("WHERE property = ?",
                                new String[]{property},
                                null, null, null, null);
                        return (result.isEmpty()) ? null : result.iterator().next();
                    } else {
                        throw new RuntimeException(e);
                    }
                }
                settingMap = cache.get(profile);
            }
        } else {
            refreshCacheInternal(profile, true);
        }
        return settingMap.get(property);
    }

    private void updateTableModifiedTimestamp(String profile) {
        assertProfileNotNull(profile);

        Collection<Setting> settings = getSetupDao().find("WHERE property = ?", new String[]{TABLE_LAST_UPDATED_KEY}, null, null, null, null);
        Setting lastUpdated;
        if (settings.isEmpty()) {
            lastUpdated = new Setting();
            lastUpdated.setProperty(TABLE_LAST_UPDATED_KEY);
        } else {
            lastUpdated = settings.iterator().next();
        }
        long now = System.currentTimeMillis();
        lastUpdated.setValue(String.valueOf(now));

        // update database and cache
        getSetupDao().saveOrUpdate(lastUpdated);
        updateCacheInternal(profile, lastUpdated);
        CACHE_LAST_UPDATED.put(profile, now);
    }

    private long getTableModifiedTimestamp() {
        Collection<Setting> settings = getSetupDao().find("WHERE property = ?", new String[]{TABLE_LAST_UPDATED_KEY}, null, null, null, null);
        if (!settings.isEmpty()) {
            Setting setting = settings.iterator().next();
            try {
                return Long.parseLong(setting.getValue());
            } catch (Exception ignored) {}
        }
        return 0;
    }

    private void updateCacheInternal(String profile, Setting setting) {
        assertProfileNotNull(profile);
        ConcurrentMap<String, Setting> settingMap = cache.get(profile);
        if (settingMap == null) {
            try {
                refreshCacheInternal(profile, false).get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                // if current thread is refreshing or timeout, we refresh it directly from database
                if (e instanceof TimeoutException || e.getCause() instanceof IllegalThreadStateException) {
                    if (LogUtil.isDebugEnabled(getClass().getName()) || LogUtil.isDebugEnabled(SetupManager.class.getName())) {
                        LogUtil.error(getClass().getName(), e, Thread.currentThread().getName() + " -- error updating cache for profile " + profile);
                    }
                    refreshCacheFromDataSource(profile, false);
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
        settingMap = cache.get(profile);
        String settingProperty = setting.getProperty();
        settingMap.put(settingProperty, setting);
        cache.put(profile, settingMap);
    }

    /**
     * Method used by system to set cache object
     * @param cacheManager
     */
    public void setCacheManager(CacheManager cacheManager) {
        this.cache = cacheManager.getCache("org.joget.cache.LONG_TERM_SETUP_CACHE");
        if (cache != null) {
            LogUtil.info(getClass().getName(), "Initializing setup cache");
        }
        boolean setupStaleCache = Boolean.parseBoolean(System.getProperty(SYSTEM_PROPERTY_SETUP_STALE_CACHE, "true"));
        if (setupStaleCache) {
            this.staleCache = cacheManager.getCache("default");
            if (staleCache != null) {
                LogUtil.info(getClass().getName(), "Initializing setup stale cache");
            }
        }
    }

    public SetupManagerHelper getSetupManagerHelper() {
        return setupManagerHelper;
    }

    public void setSetupManagerHelper(SetupManagerHelper setupManagerHelper) {
        this.setupManagerHelper = setupManagerHelper;
    }

    public SetupDao getSetupDao() {
        return setupDao;
    }

    public void setSetupDao(SetupDao setupDao) {
        this.setupDao = setupDao;
    }

    private static void assertProfileNotNull(String profile) {
        if (profile == null) {
            throw new NullPointerException("Profile must not be null");
        }
    }
}
