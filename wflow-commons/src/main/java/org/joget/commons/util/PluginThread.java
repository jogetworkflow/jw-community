package org.joget.commons.util;

/**
 * Thread implementation to used by plugin
 */
public final class PluginThread extends Thread {
    
    private final String profile;
    
    public PluginThread(Runnable r) {
        super(r);
        profile = DynamicDataSourceManager.getCurrentProfile();
    }
    
    private void setProfile() {
        HostManager.setCurrentProfile(profile);
    }
    
    @Override
    public void run() {
        setProfile();
        super.run();
    }
}
