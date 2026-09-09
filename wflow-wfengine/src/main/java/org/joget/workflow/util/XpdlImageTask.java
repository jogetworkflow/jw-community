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
            // No matching resetProfile() call in this task, so this must not claim
            // HostManager's one-shot "value to restore" slot on this (possibly pooled) thread --
            // see setCurrentProfileWithoutTracking's javadoc.
            HostManager.setCurrentProfileWithoutTracking(profile);
            XpdlImageUtil.createXpdlImage(designerwebBaseUrl, processDefId);
        } catch (Exception e) {
            LogUtil.error(XpdlImageTask.class.getName(), e, "Profile : " + profile);
        }
    }
}
