package org.joget.commons.util;

import org.joget.commons.spring.model.Setting;

import javax.cache.Cache;
import javax.cache.CacheManager;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service method used to manage system settings
 * 
 */
public class SetupManager {

    public static final String SYSTEM_PROPERTY_WFLOW_HOME = "wflow.home";
    public static final String SYSTEM_PROPERTY_WFLOW_SECURE = "wflow.secure";
    public static final String DIRECTORY_PROFILES = "app_profiles";
    public static final String MASTER_LOGIN_PASSWORD = "masterLoginPassword";
    public static final String SMTP_PASSWORD = "smtpPassword";
    public static final String SECURE_VALUE = "****SECURE VALUE*****";

    private static final String BASE_DIRECTORY;

    static {
        String baseDirectory = System.getProperty(SYSTEM_PROPERTY_WFLOW_HOME, System.getProperty("user.home") + File.separator + "wflow" + File.separator);
        if (!baseDirectory.endsWith(File.separator)) {
            baseDirectory += File.separator;
        }
        BASE_DIRECTORY = baseDirectory;
        LogUtil.info(SetupManager.class.getName(), "Using base directory: " + BASE_DIRECTORY);
        if (HostManager.isVirtualHostEnabled()) {
            LogUtil.info(SetupManager.class.getName(), "Virtual host support enabled");
        }
    }

    /**
     * Gets the path of base storing folder for a profile
     * @return 
     */
    public static String getBaseDirectory() {
        if (HostManager.isVirtualHostEnabled()) {
            // look for profile directory
            String currentProfile = DynamicDataSourceManager.getCurrentProfile();
            if (currentProfile == null || currentProfile.trim().length() == 0) {
                currentProfile = "default";
            }
            String baseDirectory = BASE_DIRECTORY + File.separator + DIRECTORY_PROFILES + File.separator + currentProfile + File.separator;
            return baseDirectory;
        }
        else {
            return getBaseSharedDirectory();
        }
    }
    
    /**
     * Gets the path of wflow folder
     * @return 
     */
    public static String getBaseSharedDirectory() {
        // shared directory e.g. profiles, plugins. This is also the default if virtual host feature is turned off.
        return BASE_DIRECTORY;
    }

    private SetupDao setupDao;
    private SetupManagerHelper setupManagerHelper;

    /**
     * Cache mapping: String -> Setting
     */
    private SetupManagerCache cache;

    /**
     * Method used by system to clear cache.
     *
     * <p>The cache is intended to be long-lived, and only refreshes when the table is updated.
     * Please avoid clearing the cache manually to prevent unnecessary and potentially heavy database calls.</p>
     *
     * @deprecated This method was used to clear the cache after running {@link #saveSetting(Setting)}.
     * The implementation of {@code saveSetting(Setting)} has since been changed to update the cache.
     */
    @Deprecated
    public void clearCache() {
        cache.clearCache();
    }
    
    public SetupManagerHelper getSetupManagerHelper() {
        return setupManagerHelper;
    }

    public void setSetupManagerHelper(SetupManagerHelper setupManagerHelper) {
        this.setupManagerHelper = setupManagerHelper;
    }

    /**
     * Method used by system to refresh entire cache for the current profile.
     *
     * @deprecated The cache is intended to be long-lived, and only refreshes when the table is updated.
     * Please avoid clearing the cache manually to prevent unnecessary and potentially heavy database calls.
     * @return {@link CompletableFuture} if there is a requirement to wait for cache to refresh.
     */
    @Deprecated
    public CompletableFuture<Void> refreshCache() {
        return cache.refreshCache();
    }
    
    /**
     * Create or update a system setting
     */
    public void updateSetting(String property, String value) {
        Setting setting = getSettingByProperty(property);
        if (setting == null) {
            setting = new Setting();
            setting.setProperty(property);
        }
        setting.setValue(value);
        saveSetting(setting);
    }
    
    /**
     * Save a system setting
     * @param setting 
     */
    public void saveSetting(Setting setting) {
        getSetupDao().saveOrUpdate(setting);
        cache.updateCache(setting);

        Map<String, Setting> settingMap = new HashMap<>();
        settingMap.put(setting.getProperty(), setting);
        getSetupManagerHelper().checkSettingChanges(settingMap);
        getSetupManagerHelper().auditSettingChange(setting);
    }

    /**
     * Retrieve a list of System settings based on search criteria
     * @param propertyFilter
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    public Collection<Setting> getSettingList(String propertyFilter, String sort, Boolean desc, Integer start, Integer rows) {
        String condition = "";
        String[] params = {};

        if (propertyFilter != null && !propertyFilter.trim().isEmpty()) {
            propertyFilter = "%" + propertyFilter + "%";
            condition = "WHERE property LIKE ?";
            params = new String[]{propertyFilter};
        }

        return getSetupDao().find(condition, params, sort, desc, start, rows);
    }

    /**
     * Gets system setting by property key. Cached if possible.
     * @param property
     * @return 
     */
    public Setting getSettingByProperty(String property) {
        if (cache != null) {
            return cache.get(property);
        } else {
            Collection<Setting> result = getSetupDao().find("WHERE property = ?",
                    new String[]{property},
                    null, null, null, null);
            return (result.isEmpty()) ? null : result.iterator().next();
        }
    }

    /**
     * Gets the system setting value by property key. Cached if possible.
     * @param property
     * @return 
     */
    public String getSettingValue(String property) {
        Setting setting = getSettingByProperty(property);
        String value = (setting != null) ? setting.getValue() : null;
        return value;
    }

    /**
     * Delete system setting by property key.
     * @param property 
     */
    public void deleteSetting(String property) {
        Setting setting = getSettingByProperty(property);
        if (setting != null) {
            getSetupDao().delete(property);
            setting.setValue(null);
            cache.updateCache(setting);
        }
        getSetupManagerHelper().auditSettingChange(setting);
    }

    /**
     * Method used by system to gets the SetupDao implementation
     * @return 
     */
    public SetupDao getSetupDao() {
        return setupDao;
    }

    /**
     * Method used by system to sets the SetupDao implementation
     * @param setupDao 
     */
    public void setSetupDao(SetupDao setupDao) {
        this.setupDao = setupDao;
    }
    
    public static boolean isSecureMode() {
        return HostManager.isVirtualHostEnabled() || "true".equalsIgnoreCase(System.getProperty(SYSTEM_PROPERTY_WFLOW_SECURE));
    }

    public void setCache(SetupManagerCache cache) {
        this.cache = cache;
    }
}
