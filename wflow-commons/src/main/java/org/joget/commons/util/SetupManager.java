package org.joget.commons.util;

import org.joget.commons.spring.model.Setting;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.cache.Cache;
import javax.cache.CacheManager;

/**
 * Service method used to manage system settings
 * 
 */
public class SetupManager {

    public static final String SYSTEM_PROPERTY_WFLOW_HOME = "wflow.home";
    public static final String SYSTEM_PROPERTY_WFLOW_SECURE = "wflow.secure";
    public static final String SYSTEM_PROPERTY_SETUP_STALE_CACHE = "wflow.setupStaleCache";
    public static final String DIRECTORY_PROFILES = "app_profiles";
    public static final String MASTER_LOGIN_PASSWORD = "masterLoginPassword";
    public static final String SMTP_PASSWORD = "smtpPassword";
    public static final String SECURE_VALUE = "****SECURE VALUE*****";

    private static final String BASE_DIRECTORY;
    private static boolean STATUS_REFRESHING = false;

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
    private Cache cache;
    private Cache staleCache;    

    /**
     * Method used by system to set cache object
     * @param cacheManager 
     */
    public void setCacheManager(CacheManager cacheManager) {
        this.cache = cacheManager.getCache("org.joget.cache.SETUP_CACHE");
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
    
    protected String getCacheKey() {
        return "setup_" + DynamicDataSourceManager.getCurrentProfile();
    }
    
    /**
     * Method used by system to clear cache
     */
    public void clearCache() {
        String cacheKey = getCacheKey();
        if (cache != null) {
            cache.remove(cacheKey);
        }
        if (staleCache != null) {
            staleCache.remove(cacheKey);
        }
    }

    /**
     * Method used by system to update cache by property
     * @param setting
     */
    protected void updateCache(Setting setting) {
        String cacheKey = getCacheKey();
        if (cache != null) {
            Map<String, Setting> settingMap = (Map<String, Setting>)cache.get(cacheKey);
            if (settingMap != null) {
                String property = setting.getProperty();
                if (setting.getValue() == null) {
                    settingMap.remove(property);
                } else {
                    settingMap.put(property, setting);
                }
            }
        }
        if (staleCache != null) {
            Map<String, Setting> settingMap = (Map<String, Setting>)staleCache.get(cacheKey);
            if (settingMap != null) {
                String property = setting.getProperty();
                if (setting.getValue() == null) {
                    settingMap.remove(property);
                } else {
                    settingMap.put(property, setting);
                }
            }
        }
    }
    
    public SetupManagerHelper getSetupManagerHelper() {
        return setupManagerHelper;
    }

    public void setSetupManagerHelper(SetupManagerHelper setupManagerHelper) {
        this.setupManagerHelper = setupManagerHelper;
    }
    
    /**
     * Method used by system to refresh cache 
     */
    public void refreshCache() {
        if (cache != null) {
            String cacheKey = getCacheKey();
            boolean inCache = (staleCache != null) ? staleCache.containsKey(cacheKey) : cache.containsKey(cacheKey);
            if (!inCache) {
                refreshCacheFromDataSource();                                     
            } else if (!STATUS_REFRESHING) {
                STATUS_REFRESHING = true;
                new PluginThread(() -> {
                    refreshCacheFromDataSource();                     
                }).start();
            }
        }
    }
    
    /**
     * Refresh cache from the datasource
     */
    protected void refreshCacheFromDataSource() {
        try {
            String cacheKey = getCacheKey();
            LogUtil.debug(getClass().getName(), "Refreshing setup cache for " + cacheKey);
            Collection<Setting> settings = getSetupDao().find("", null, null, null, null, null);
            Map<String, Setting> settingMap = new HashMap<String, Setting>();
            for (Setting setting: settings) {
                settingMap.put(setting.getProperty(), setting);
            }

            getSetupManagerHelper().checkSettingChanges(settingMap);

            cache.put(cacheKey, settingMap);
            if (staleCache != null) {
                staleCache.put(cacheKey, settingMap);
            }
        } finally {
            STATUS_REFRESHING = false;
        }
    }
    
    /**
     * Create or update a system setting
     * @param setting 
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
        updateCache(setting);
        
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

        if (propertyFilter != null && propertyFilter.trim().length() > 0) {
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
            Setting setting = null;
            String cacheKey = getCacheKey();
            Map<String, Setting> settingMap = (Map<String, Setting>)cache.get(cacheKey);
            if (settingMap == null) {
                refreshCache();
                if (staleCache != null) {
                    settingMap = (Map<String, Setting>)staleCache.get(cacheKey);
                }
            }
            if (settingMap != null) {
                setting = settingMap.get(property);
            }
            return setting;
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
            updateCache(setting);
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
}
