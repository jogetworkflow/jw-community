package org.joget.workflow.util;

import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;

public class XpdlImageTask implements Runnable {
    private String profile;
    private String designerwebBaseUrl;
    private String processDefId;
    
    public XpdlImageTask(String profile, String designerwebBaseUrl, String processDefId) {
        this.profile = profile;
        this.designerwebBaseUrl = designerwebBaseUrl;
        this.processDefId = processDefId;
    }
    
    @Override
    public void run() {
        try {
            HostManager.setCurrentProfile(profile);
            XpdlImageUtil.createXpdlImage(designerwebBaseUrl, processDefId);
        } catch (Exception e) {
            LogUtil.error(XpdlImageTask.class.getName(), e, "Profile : " + profile);
        }
    }
}
