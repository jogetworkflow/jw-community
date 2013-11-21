package org.joget.workflow.util;

import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;

public class XpdlImageThread extends Thread {
    private String profile;
    
    public XpdlImageThread(String profile) {
        super();
        this.profile = profile;
    }
    
    @Override
    public void run() {
        try {
            HostManager.setCurrentProfile(profile);
            XpdlImageUtil.generateXpdlImageFromQueue();
        } catch (Exception e) {
            LogUtil.error(XpdlImageThread.class.getName(), e, "Profile : " + profile);
        } finally {
            XpdlImageUtil.removeThread(profile);
        }
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
