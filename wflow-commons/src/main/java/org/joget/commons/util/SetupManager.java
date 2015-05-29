package org.joget.commons.util;

import org.joget.commons.spring.model.Setting;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

public class SetupManager {

    public static final String SYSTEM_PROPERTY_WFLOW_HOME = "wflow.home";
    public static final String DIRECTORY_PROFILES = "app_profiles";
    public static final String MASTER_LOGIN_PASSWORD = "masterLoginPassword";
    public static final String SECURE_VALUE = "****SECURE VALUE*****";

    private static final String BASE_DIRECTORY;

    static {
        String baseDirectory = System.getProperty(SYSTEM_PROPERTY_WFLOW_HOME, System.getProperty("user.home") + File.separator + "wflow" + File.separator);
        BASE_DIRECTORY = baseDirectory;
        LogUtil.info(SetupManager.class.getName(), "Using base directory: " + BASE_DIRECTORY);
        if (HostManager.isVirtualHostEnabled()) {
            LogUtil.info(SetupManager.class.getName(), "Virtual host support enabled");
        }
    }

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
    
    public static String getBaseSharedDirectory() {
        // shared directory e.g. profiles, plugins. This is also the default if virtual host feature is turned off.
        return BASE_DIRECTORY;
    }

    private SetupDao setupDao;

    private Cache cache;

    public void setCache(Cache cache) {
        this.cache = cache;
        if (cache != null) {
            LogUtil.info(getClass().getName(), "Initializing setup cache");
        }
    }
    
    public void clearCache() {
        if (cache != null) {
            synchronized(cache) {
                String profile = DynamicDataSourceManager.getCurrentProfile();
                cache.remove(profile);
            }
        }
    }
    
    public void refreshCache() {
        if (cache != null) {
            synchronized(cache) {
                String profile = DynamicDataSourceManager.getCurrentProfile();
                LogUtil.debug(getClass().getName(), "Refreshing setup cache for " + profile);
                cache.remove(profile);
                Collection<Setting> settings = getSetupDao().find("", null, null, null, null, null);
                Map<String, Setting> settingMap = new HashMap<String, Setting>();
                for (Setting setting: settings) {
                    settingMap.put(setting.getProperty(), setting);
                }
                Element element = new Element(profile, settingMap);
                cache.put(element);
            }
        }
    }
    
    public void saveSetting(Setting setting) {
        getSetupDao().saveOrUpdate(setting);
        clearCache();
    }

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

    public Setting getSettingByProperty(String property) {
        if (cache != null) {
            Setting setting = null;
            synchronized(cache) {
                Element element = null;
                String profile = DynamicDataSourceManager.getCurrentProfile();
                element = cache.get(profile);
                if (element == null) {
                    refreshCache();
                    element = cache.get(profile);
                }
                if (element != null) {
                    Map<String, Setting> settingMap = (Map<String, Setting>)element.getValue();
                    setting = settingMap.get(property);
                }
            }
            return setting;
        } else {
            Collection<Setting> result = getSetupDao().find("WHERE property = ?",
                    new String[]{property},
                    null, null, null, null);
            return (result.isEmpty()) ? null : result.iterator().next();
        }
    }

    public String getSettingValue(String property) {        
        Setting setting = getSettingByProperty(property);
        String value = (setting != null) ? setting.getValue() : null;
        return value;
    }

    public void deleteSetting(String property) {
        Setting setting = getSettingByProperty(property);
        if (setting != null) {
            getSetupDao().delete(setting);
        }
    }

    public SetupDao getSetupDao() {
        return setupDao;
    }

    public void setSetupDao(SetupDao setupDao) {
        this.setupDao = setupDao;
    }
}
