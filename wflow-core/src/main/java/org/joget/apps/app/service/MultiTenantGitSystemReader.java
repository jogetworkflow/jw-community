package org.joget.apps.app.service;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileBasedConfig;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.StringUtils;
import org.eclipse.jgit.util.SystemReader;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;

/**
 * This class customized the SystemReader.Default to support multitenant git config reading within it profile dir
 */
public class MultiTenantGitSystemReader extends SystemReader {

    private volatile String hostname;
    private Map<String, AtomicReference<FileBasedConfig>> systemConfigs = new HashMap<>();
    private Map<String, AtomicReference<FileBasedConfig>> userConfigs = new HashMap<>();
    private Map<String, AtomicReference<FileBasedConfig>> jgitConfigs = new HashMap<>();

    @Override
    public String getenv(String variable) {
        return System.getenv(variable);
    }

    @Override
    public String getProperty(String key) {
        return System.getProperty(key);
    }

    @Override
    public FileBasedConfig openSystemConfig(Config parent, FS fs) {
        if (StringUtils
                .isEmptyOrNull(getenv(Constants.GIT_CONFIG_NOSYSTEM_KEY))) {
            File configFile = fs.getGitSystemConfig();
            if (configFile != null) {
                return new FileBasedConfig(parent, configFile, fs);
            }
        }
        return new FileBasedConfig(parent, null, fs) {
            @Override
            public void load() {
                // empty, do not load
            }

            @Override
            public boolean isOutdated() {
                // regular class would bomb here
                return false;
            }
        };
    }

    @Override
    public FileBasedConfig openUserConfig(Config parent, FS fs) {
        return new FileBasedConfig(parent, new File(AppDevUtil.getAppDevBaseDirectory(), ".gitconfig"), //$NON-NLS-1$
                fs);
    }

    private Path getXDGConfigHome(FS fs) {
        String configHomePath = getenv(Constants.XDG_CONFIG_HOME);
        if (StringUtils.isEmptyOrNull(configHomePath)) {
            configHomePath = new File(AppDevUtil.getAppDevBaseDirectory(), ".config") //$NON-NLS-1$
                    .getAbsolutePath();
        }
        try {
            return Paths.get(configHomePath);
        } catch (InvalidPathException e) {
            LogUtil.error(MultiTenantGitSystemReader.class.getName(), e, JGitText.get().logXDGConfigHomeInvalid);
        }
        return null;
    }

    @Override
    public FileBasedConfig openJGitConfig(Config parent, FS fs) {
        Path xdgPath = getXDGConfigHome(fs);
        if (xdgPath != null) {
            Path configPath = xdgPath.resolve("jgit") //$NON-NLS-1$
                    .resolve(Constants.CONFIG);
            return new FileBasedConfig(parent, configPath.toFile(), fs);
        }
        return new FileBasedConfig(parent,
                new File(AppDevUtil.getAppDevBaseDirectory(), ".jgitconfig"), fs); //$NON-NLS-1$
    }

    @Override
    public String getHostname() {
        if (hostname == null) {
            try {
                InetAddress localMachine = InetAddress.getLocalHost();
                hostname = localMachine.getCanonicalHostName();
            } catch (UnknownHostException e) {
                // we do nothing
                hostname = "localhost"; //$NON-NLS-1$
            }
            assert hostname != null;
        }
        return hostname;
    }

    @Override
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    @Override
    public int getTimezone(long when) {
        return getTimeZone().getOffset(when) / (60 * 1000);
    }
    
    /**
     * Get the git configuration found in the user home. The configuration will
     * be reloaded automatically if the configuration file was modified. Also
     * reloads the system config if the system config file was modified. If the
     * configuration file wasn't modified returns the cached configuration.
     *
     * @return the git configuration found in the user home
     * @throws ConfigInvalidException
     *             if configuration is invalid
     * @throws IOException
     *             if something went wrong when reading files
     * @since 5.1.9
     */
    @Override
    public StoredConfig getUserConfig() throws ConfigInvalidException, IOException {
        String profile = HostManager.getCurrentProfile();
        
        if (!userConfigs.containsKey(profile)) {
            userConfigs.put(profile, new AtomicReference<FileBasedConfig>());
        }
        
        FileBasedConfig c = userConfigs.get(profile).get();
        if (c == null) {
            userConfigs.get(profile).compareAndSet(null, openUserConfig(getSystemConfig(), FS.DETECTED));
            c = userConfigs.get(profile).get();
        }
        // on the very first call this will check a second time if the system
        // config is outdated
        updateAllConfig(c);
        return c;
    }
    
    /**
     * Get the jgit configuration located at $XDG_CONFIG_HOME/jgit/config. The
     * configuration will be reloaded automatically if the configuration file
     * was modified. If the configuration file wasn't modified returns the
     * cached configuration.
     *
     * @return the jgit configuration located at $XDG_CONFIG_HOME/jgit/config
     * @throws ConfigInvalidException
     *             if configuration is invalid
     * @throws IOException
     *             if something went wrong when reading files
     * @since 5.5.2
     */
    public StoredConfig getJGitConfig() throws ConfigInvalidException, IOException {
        String profile = HostManager.getCurrentProfile();
        
        if (!jgitConfigs.containsKey(profile)) {
            jgitConfigs.put(profile, new AtomicReference<FileBasedConfig>());
        }
        
        FileBasedConfig c = jgitConfigs.get(profile).get();
        if (c == null) {
            jgitConfigs.get(profile).compareAndSet(null, openJGitConfig(null, FS.DETECTED));
            c = jgitConfigs.get(profile).get();
        }
        updateAllConfig(c);
        return c;
    }

    /**
     * Get the gitconfig configuration found in the system-wide "etc" directory.
     * The configuration will be reloaded automatically if the configuration
     * file was modified otherwise returns the cached system level config.
     *
     * @return the gitconfig configuration found in the system-wide "etc"
     *         directory
     * @throws ConfigInvalidException
     *             if configuration is invalid
     * @throws IOException
     *             if something went wrong when reading files
     * @since 5.1.9
     */
    public StoredConfig getSystemConfig() throws ConfigInvalidException, IOException {
        String profile = HostManager.getCurrentProfile();
        
        if (!systemConfigs.containsKey(profile)) {
            systemConfigs.put(profile, new AtomicReference<FileBasedConfig>());
        }
        
        FileBasedConfig c = systemConfigs.get(profile).get();
        if (c == null) {
            systemConfigs.get(profile).compareAndSet(null, openSystemConfig(getJGitConfig(), FS.DETECTED));
            c = systemConfigs.get(profile).get();
        }
        updateAllConfig(c);
        return c;
    }
    
    /**
     * Update config and its parents if they seem modified
     *
     * @param config
     *            configuration to reload if outdated
     * @throws ConfigInvalidException
     *             if configuration is invalid
     * @throws IOException
     *             if something went wrong when reading files
     */
    private void updateAllConfig(Config config)
                    throws ConfigInvalidException, IOException {
        if (config == null) {
                return;
        }
        updateAllConfig(config.getBaseConfig());
        if (config instanceof FileBasedConfig) {
                FileBasedConfig cfg = (FileBasedConfig) config;
                if (cfg.isOutdated()) {
                        LogUtil.debug(MultiTenantGitSystemReader.class.getName(), "loading config"); //$NON-NLS-1$
                        cfg.load();
                }
        }
    }
}
