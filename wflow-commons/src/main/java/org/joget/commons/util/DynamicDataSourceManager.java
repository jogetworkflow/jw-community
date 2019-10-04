package org.joget.commons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class DynamicDataSourceManager {

    public static final String DATASOURCE_FILE = "app_datasource.properties";
    public static final String FILE_PREFIX = "app_datasource-";
    public static final String FILE_EXTENSION = ".properties";
    public static final String FILE_PATH = SetupManager.getBaseSharedDirectory();
    public static final String CURRENT_PROFILE_KEY = "currentProfile";
    public static final String DEFAULT_PROFILE = "default";
    public static final String SECURE_VALUE = "****SECURE VALUE****";
    public static final String SECURE_FIELD = "workflowPassword";

    private static DatasourceProfilePropertyManager profilePropertyManager;
    
    /**
     * The property manager is initialized via spring injection.
     */
    public DynamicDataSourceManager(DatasourceProfilePropertyManager propertyManager) {
        DynamicDataSourceManager.profilePropertyManager = propertyManager;
    }
    
    public static boolean testConnection(String driver, String url, String user, String password) {
        Connection conn = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
            }
        }
    }

    public static Properties getProperties() {
        Properties properties = profilePropertyManager.newInstance();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(determineFilePath(getCurrentProfile())));
            properties.load(fis);
        } catch (FileNotFoundException e) {
        } catch (Exception e) {
            LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
            }
        }
        return properties;
    }

    public static String getProperty(String key) {
        Properties properties = profilePropertyManager.newInstance();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(determineFilePath(getCurrentProfile())));
            properties.load(fis);
            return properties.getProperty(key);
        } catch (FileNotFoundException e) {
        } catch (Exception e) {
            LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
            }
        }
        return null;
    }

    public static List<String> getProfileList() {
        try {
            File[] fileList = new File(FILE_PATH).listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    if (name.startsWith(FILE_PREFIX) && name.endsWith(FILE_EXTENSION) && name.length() > (FILE_PREFIX.length() + FILE_EXTENSION.length())) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            List<String> profileList = new ArrayList<String>();
            for (File file : fileList) {
                String fileName = file.getName();
                String profileName = fileName.replace(FILE_PREFIX, "").replace(FILE_EXTENSION, "");
                profileList.add(profileName);
            }
            return profileList;
        } catch (Exception e) {
            LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
        }
        return null;
    }

    public static Properties getProfileProperties() {
        Properties properties = profilePropertyManager.newInstance();
        FileInputStream fis = null;
        String defaultDataSourceFilename = determineDefaultDataSourceFilename();
        try {
            fis = new FileInputStream(new File(defaultDataSourceFilename));
            properties.load(fis);
        } catch (Exception e) {
            LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
            }
        }
        return properties;
    }

    public static String getCurrentProfile() {
        Properties properties = new Properties();
        FileInputStream fis = null;
        String defaultDataSourceFilename = determineDefaultDataSourceFilename();
        try {
            // look for profile or hostname set by HostManager in thread
            String currentProfile = HostManager.getCurrentProfile();
            if (currentProfile == null || currentProfile.trim().length() == 0) {
                // load from properties file
                fis = new FileInputStream(new File(defaultDataSourceFilename));
                properties.load(fis);

                String hostname = HostManager.getCurrentHost();
                if (hostname != null && hostname.trim().length() > 0) {
                    currentProfile = properties.getProperty(hostname);
                }
                if (currentProfile == null || currentProfile.trim().length() == 0) {
                    // look for matching context path
                    String contextPath = HostManager.getContextPath();
                    if (contextPath != null && contextPath.trim().length() > 0) {
                        currentProfile = properties.getProperty(contextPath);
                    }
                }
            }

            if (currentProfile == null || currentProfile.trim().length() == 0) {
                // default profile
                currentProfile = properties.getProperty(CURRENT_PROFILE_KEY);
            }

            // set profile in thread
            HostManager.setCurrentProfile(currentProfile);
            return currentProfile;
        } catch (FileNotFoundException e) {
            if (!(e.getMessage() != null && e.getMessage().contains("Too many open files"))) {
                // As of v5, don't automatically create default profile, to allow setup on first startup
                // createDefaultProfile();
                LogUtil.debug(DynamicDataSourceManager.class.getName(), defaultDataSourceFilename + " not found, using default datasource");
            }
        } catch (Exception e) {
            LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
            }
        }
        return null;
    }

    public static void changeProfile(String profileName) {
        Properties properties = new Properties();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        String defaultDataSourceFilename = determineDefaultDataSourceFilename();
        try {
            profileName = SecurityUtil.validateStringInput(profileName);
            File datasourceFile = new File(defaultDataSourceFilename);
            if (!datasourceFile.exists()) {
                new File(FILE_PATH).mkdirs();
                datasourceFile.createNewFile();
            }
            fis = new FileInputStream(datasourceFile);
            properties.load(fis);
            properties.setProperty(CURRENT_PROFILE_KEY, profileName);

            fos = new FileOutputStream(datasourceFile);
            properties.store(fos, "");
            HostManager.setCurrentProfile(profileName);
        } catch (Exception e) {
            LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
            }
        }
    }

    public static boolean createProfile(String profileName) {
        try {
            profileName = SecurityUtil.validateStringInput(profileName);
            File file = new File(determineFilePath(profileName));
            if (file.exists()) {
                return false;
            }

            file.createNewFile();
            return true;
        } catch (Exception e) {
            LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
        }
        return false;
    }

    public static boolean deleteProfile(String profileName) {
        try {
            profileName = SecurityUtil.validateStringInput(profileName);
            File file = new File(determineFilePath(profileName));
            file.delete();
        } catch (Exception e) {
            LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
        }
        return false;
    }

    public static void writeProperty(String key, String value) {
        Properties properties = profilePropertyManager.newInstance();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            String currentProfile = getCurrentProfile();
            fis = new FileInputStream(new File(determineFilePath(currentProfile)));
            properties.load(fis);
            properties.setProperty(key, value);

            fos = new FileOutputStream(new File(determineFilePath(currentProfile)));
            properties.store(fos, "");
        } catch (Exception e) {
            LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
            }
        }
    }

    public static Set getPropertyKeySet() {
        Properties properties = profilePropertyManager.newInstance();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(determineFilePath(getCurrentProfile())));
            properties.load(fis);
            return properties.keySet();
        } catch (Exception e) {
            LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
            }
        }
        return null;
    }

    protected static String determineFilePath(String currentProfile) {
        if (!FILE_PATH.endsWith(File.separator)) {
            return FILE_PATH + File.separator + FILE_PREFIX + currentProfile + FILE_EXTENSION;
        } else {
            return FILE_PATH + FILE_PREFIX + currentProfile + FILE_EXTENSION;
        }
    }

    protected static String determineDefaultDataSourceFilename() {
        if (!FILE_PATH.endsWith(File.separator)) {
            return FILE_PATH + File.separator + DATASOURCE_FILE;
        } else {
            return FILE_PATH + DATASOURCE_FILE;
        }
    }

    protected static void createDefaultProfile() {
        Properties properties = new Properties();
        FileOutputStream fos = null;
        String defaultDataSourceFilename = determineDefaultDataSourceFilename();
        try {
            //create datasource properties file
            File file = new File(defaultDataSourceFilename);
            new File(FILE_PATH).mkdirs();
            file.createNewFile();

            fos = new FileOutputStream(file);
            properties.setProperty(CURRENT_PROFILE_KEY, DEFAULT_PROFILE);
            properties.store(fos, "");

            //create default datasource properties file
            createProfile(DEFAULT_PROFILE);
            changeProfile(DEFAULT_PROFILE);

            writeProperty("workflowUser", "root");
            writeProperty("workflowPassword", "");
            writeProperty("workflowDriver", "com.mysql.jdbc.Driver");
            writeProperty("workflowUrl", "jdbc:mysql://localhost:3306/jwdb?characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true");
            writeProperty("profileName", "");
        } catch (Exception e) {
            LogUtil.error(DynamicDataSourceManager.class.getName(), e, "Error creating default profile");
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
            }
        }
    }
}
