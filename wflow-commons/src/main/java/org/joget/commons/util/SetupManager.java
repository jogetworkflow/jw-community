package org.joget.commons.util;

import org.joget.commons.spring.model.Setting;
import java.io.File;
import java.util.Collection;

public class SetupManager {

    public static final String SYSTEM_PROPERTY_WFLOW_HOME = "wflow.home";

    private static String BASE_DIRECTORY;

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
            String baseDirectory = BASE_DIRECTORY + File.separator + "app_profiles" + File.separator + currentProfile + File.separator;
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

    public void saveSetting(Setting setting) {
        getSetupDao().saveOrUpdate(setting);
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
        Collection<Setting> result = getSetupDao().find("WHERE property = ?",
                new String[]{property},
                null, null, null, null);
        return (result.isEmpty()) ? null : result.iterator().next();
    }

    public String getSettingValue(String property) {
        Collection<Setting> result = getSetupDao().find("WHERE e.property = ?", new String[]{property}, null, null, null, null);
        return (result.isEmpty()) ? null : result.iterator().next().getValue();
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
