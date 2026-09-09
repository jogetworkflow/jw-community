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
import javax.cache.Cache;
import javax.cache.CacheManager;

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
    private static Cache cache;

    // #2317: cacheManager.getCache(..) alone returns the raw JSR-107 provider object (e.g.
    // org.ehcache.jsr107.Eh107Cache), which is NOT an instance of net.sf.ehcache.Cache -- the
    // class MultiTenantPluginAspect's cache guards match by declaring type
    // (execution(* net.sf.ehcache.Cache.<method>(..))). Calling put/get/remove on the raw object
    // therefore never executes inside that guarded type at all, so isManagedCache(..) below is
    // never even reached: the whole tenant-isolation model for this cache (it holds every
    // tenant's live datasource Properties, credentials included) was silently unenforced on this
    // line. Wrapping in net.sf.ehcache.Cache -- Joget's own JSR-107 compatibility shim, the exact
    // same wrap org.joget.commons.util.EhCacheFactoryBean.getObject() already performs for every
    // other correctly-guarded platform cache -- makes the SAME field, with no other code change,
    // dispatch through that guarded type instead.
    public void setCacheManager(CacheManager cacheManager) {
        DynamicDataSourceManager.cache =
                new net.sf.ehcache.Cache(cacheManager.getCache("org.joget.cache.DATASOURCE_CACHE"));
    }

    /**
     * Lets multi-tenant security code (in the Cloud/Enterprise edition) recognize the live cache
     * backing this class without ever handing out a reference to it -- the cache is also reachable
     * as an ordinary Spring bean ({@code dynamicDataSourceManagerCache}), so mutating it directly
     * (e.g. {@code Cache.put(Element)}) achieves the same effect as calling {@link #setCacheManager}
     * without ever calling that guarded method at all. An identity check that never leaves this
     * class lets a guard recognize and block that direct mutation without exposing the cache
     * itself more broadly than it already is.
     * @param candidate the object to test
     * @return true if candidate is exactly this class's own managed cache
     */
    public static boolean isManagedCache(Object candidate) {
        return cache != null && cache == candidate;
    }

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
    
    public static String getCacheKey(String profile) {
        return "DATASOURCE-CACHE-" + profile;
    }

    public static Properties getProperties() {
        Properties properties = getPropertiesLive();
        // Defensive copy -- same reasoning as getProfileProperties() above: this method is also
        // on the security manager's restricted-class whitelist and returns the exact cached
        // instance, which would otherwise let any caller rewrite the current profile's own
        // datasource credentials/settings in place.
        Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }

    // Package-private: the LIVE, cached Properties instance, with NO defensive copy, for internal
    // hot callers only (e.g. MultiTenantDataSource.findDataSource(), called on every JDBC
    // connection checkout). Callers MUST treat the returned instance as read-only (only ever call
    // getProperty(..) on it) -- it is the exact same object every future caller (including this
    // method's public, defensive-copying sibling) sees. Package visibility is a real trust
    // boundary here: JVM package-private access requires the SAME defining classloader, and an
    // OSGi-bundle-loaded class never shares a classloader with this class or its legitimate
    // internal callers (MultiTenantSecurityManager, MultiTenantDataSource), which all load under
    // the webapp's own classloader.
    static Properties getPropertiesLive() {
        String profile = getCurrentProfile();
        Properties properties = (Properties)cache.get(getCacheKey(profile));
        if (properties == null) {
            properties = profilePropertyManager.newInstance();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(new File(determineFilePath(profile)));
                properties.load(fis);

                cache.put(getCacheKey(profile), properties);
                LogUtil.debug(DynamicDataSourceManager.class.getName(), "Updated app_datasource-"+profile+".properties cache");
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
        }
        return properties;
    }

    public static String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    // Cached list of profile names, refreshed from disk. The name set changes only when a profile file
    // is added/removed under FILE_PATH, which bumps that directory's mtime. The cache {stamp,list} is one
    // volatile object so a concurrent refresh can never pair a stale list with a current stamp, and it is
    // trusted only while the directory mtime is unchanged AND has been stable for RACY_PROFILE_DIR_SLACK_MS
    // -- the git "racy mtime" rule: File.lastModified() is millisecond-truncated (coarser on some
    // filesystems), so a directory touched within the last tick could have had a second change in the same
    // tick; it is re-listed (never cached) until it settles. In steady state this is a single stat instead
    // of a listFiles() on the ownership check's hot read path (ManagedCacheRegistry.crossCheckProfileList,
    // per restricted fluCache/userview/formOptions read); a newly provisioned or removed sibling profile is
    // visible immediately (its dir change bumps the stamp -> miss -> re-list). The stat and listFiles run
    // in this method's own frame, which carries the bare-class file-permission whitelist entry, so a
    // restricted (BeanShell/OSGi) caller reaches it as before. A fresh copy is returned each call (callers
    // may mutate it); a null (IO error) is not cached. As a backstop against filesystems where the
    // directory mtime does not track adds/removes reliably (object-store FUSE mounts with a constant dir
    // mtime; a file server whose clock trails this JVM; an mtime that recurs after a clock step or an
    // archive restore), a cached snapshot is also trusted only for MAX_PROFILE_SNAPSHOT_AGE_NANOS -- a
    // monotonic System.nanoTime() age, immune to wall-clock steps -- so any such stale snapshot is bounded
    // to that age (then re-listed), never served for the JVM's lifetime.
    private static final long RACY_PROFILE_DIR_SLACK_MS = 2500L;
    private static final long MAX_PROFILE_SNAPSHOT_AGE_NANOS = 30_000_000_000L; // 30s
    private static final class ProfileListSnapshot {
        private final long dirStamp;
        private final long cachedAtNanos;
        private final List<String> list;
        private ProfileListSnapshot(long dirStamp, long cachedAtNanos, List<String> list) {
            this.dirStamp = dirStamp;
            this.cachedAtNanos = cachedAtNanos;
            this.list = list;
        }
    }
    private static volatile ProfileListSnapshot cachedProfileListSnapshot;
    // Bumped by every in-JVM membership change; a listing that started before the change must not
    // publish its (now stale) result, even on a filesystem whose directory mtime did not move.
    private static final java.util.concurrent.atomic.AtomicLong profileListGeneration = new java.util.concurrent.atomic.AtomicLong();

    public static List<String> getProfileList() {
        try {
            long dirStamp = new File(FILE_PATH).lastModified();
            boolean dirSettled = dirStamp != 0L && (System.currentTimeMillis() - dirStamp) > RACY_PROFILE_DIR_SLACK_MS;
            long generation = profileListGeneration.get();
            ProfileListSnapshot snap = cachedProfileListSnapshot;
            if (snap != null && dirSettled && snap.dirStamp == dirStamp
                    && (System.nanoTime() - snap.cachedAtNanos) < MAX_PROFILE_SNAPSHOT_AGE_NANOS) {
                return new ArrayList<String>(snap.list);
            }
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
            // Only cache once the directory has settled; a racy (recently-touched) directory is served
            // fresh and left uncached, so a second change in the same mtime tick is never frozen in.
            cachedProfileListSnapshot = (dirSettled && generation == profileListGeneration.get())
                    ? new ProfileListSnapshot(dirStamp, System.nanoTime(), java.util.Collections.unmodifiableList(new ArrayList<String>(profileList)))
                    : null;
            return profileList;
        } catch (Exception e) {
            LogUtil.error(DynamicDataSourceManager.class.getName(), e, "");
        }
        return null;
    }

    // Test hook: drops the cached snapshot so a unit test that adds/removes profile files between cases
    // is never served a stale list. Not needed in production (a dir change bumps the mtime stamp).
    static void invalidateProfileListCacheForTest() {
        profileListGeneration.incrementAndGet();
        cachedProfileListSnapshot = null;
    }

    public static Properties getProfileProperties() {
        Properties properties = getProfilePropertiesLive();
        // Defensive copy: the cache holds this exact Properties instance across every future
        // call, including from OSGI/BeanShell code (this method is on the security manager's
        // own restricted-class whitelist, so it is reachable by design). Handing out
        // the live instance would let any caller permanently rewrite adminProfile/currentProfile/
        // the hostname routing table for every tenant by calling setProperty() on it directly --
        // no SecurityManager hook exists for a plain Properties mutation. The sanctioned update
        // path (changeProfile()/createDefaultProfile()) always loads its own fresh instance,
        // writes it to disk, then evicts this cache entry, so callers never need the live
        // instance to see an update -- the next call after eviction reloads from the file.
        Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }

    // Package-private: the LIVE, cached Properties instance, with NO defensive copy, for internal
    // hot callers only. isAdminProfile() (called on every checkRead/checkPermission/checkExec, and
    // via BshReflectionGuardAspect on every method call inside every BeanShell script) and
    // getCurrentProfile() below both only ever read a single value via getProperty(..) -- copying
    // the entire hostname-to-profile routing table (one entry per tenant) on every call would be
    // pure waste for those callers. Same read-only contract and classloader-based trust boundary
    // as getPropertiesLive() above.
    static Properties getProfilePropertiesLive() {
        String filename = DynamicDataSourceManager.DATASOURCE_FILE;
        Properties properties = (Properties)cache.get(getCacheKey(filename));
        if (properties == null) {
            // not in cache, load from file
            properties = loadProfileProperties();

            // add into cache
            cache.put(getCacheKey(filename), properties);
            LogUtil.debug(DynamicDataSourceManager.class.getName(), "Updated app_datasource.properties cache");
        }
        return properties;
    }

    public static Properties loadProfileProperties() {
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
        // look for profile or hostname set by HostManager in thread
        String currentProfile = HostManager.getCurrentProfile();
        
        if (currentProfile == null || currentProfile.trim().length() == 0) {
            // Only ever reads a single value below via getProperty(..) -- the live, uncopied
            // instance is safe here since this is the same class that owns the cache.
            Properties properties = getProfilePropertiesLive();

            if (properties != null) {
                
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

                if (currentProfile == null || currentProfile.trim().length() == 0) {
                    // default profile
                    currentProfile = properties.getProperty(CURRENT_PROFILE_KEY);
                }

                // Memoize the resolved profile for the rest of this thread's life -- not a
                // scoped override with a matching resetProfile() call, so it must not claim
                // HostManager's one-shot "value to restore" slot (see
                // setCurrentProfileWithoutTracking's javadoc for why that would be unsafe).
                HostManager.setCurrentProfileWithoutTracking(currentProfile);
                return currentProfile;
            }
        } else {
            return currentProfile;
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
            // Permanently changes the platform's configured default profile -- no matching
            // resetProfile() call of its own, so this must not claim HostManager's one-shot
            // "value to restore" slot either -- see setCurrentProfileWithoutTracking's javadoc.
            HostManager.setCurrentProfileWithoutTracking(profileName);
            
            String filename = DynamicDataSourceManager.DATASOURCE_FILE;
            cache.remove(getCacheKey(filename));
            LogUtil.debug(DynamicDataSourceManager.class.getName(), "Removed app_datasource.properties cache");
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
    
    public static void removeProfilesCache() {
        String filename = DynamicDataSourceManager.DATASOURCE_FILE;
        cache.remove(getCacheKey(filename));
        LogUtil.debug(DynamicDataSourceManager.class.getName(), "Removed app_datasource.properties cache");
    }

    public static boolean createProfile(String profileName) {
        try {
            profileName = SecurityUtil.validateStringInput(profileName);
            File file = new File(determineFilePath(profileName));
            if (file.exists()) {
                return false;
            }

            file.createNewFile();
            // This JVM just changed the membership: do not let a settled, still-fresh snapshot keep
            // answering with the old list (the directory mtime check covers other nodes).
            profileListGeneration.incrementAndGet();
            cachedProfileListSnapshot = null;
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
            profileListGeneration.incrementAndGet();
            cachedProfileListSnapshot = null;
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
            
            cache.remove(getCacheKey(currentProfile));
            LogUtil.debug(DynamicDataSourceManager.class.getName(), "Removed app_datasource-"+currentProfile+".properties cache");
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
        Properties properties = getProperties();
        if (properties != null) {
            return properties.keySet();
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
