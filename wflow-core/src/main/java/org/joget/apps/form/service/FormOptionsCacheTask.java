package org.joget.apps.form.service;

import java.util.concurrent.ScheduledFuture;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;

public class FormOptionsCacheTask implements Runnable {
    private String profile;
    private String cacheKey;
    private AppDefinition appDef;
    private ScheduledFuture scheduledFuture;
    
    public FormOptionsCacheTask(String profile, String cacheKey, AppDefinition appDef) {
        this.profile = profile;
        this.cacheKey = cacheKey;
        this.appDef = appDef;
    }

    public ScheduledFuture getScheduledFuture() {
        return scheduledFuture;
    }

    public void setScheduledFuture(ScheduledFuture scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }
    
    @Override
    public void run() {
        try {
            if (FormOptionsCacheAspect.syncPaused(cacheKey)) {
                scheduledFuture.cancel(true);
                if (LogUtil.isDebugEnabled(FormOptionsCacheAspect.class.getName())) {    
                    LogUtil.debug(FormOptionsCacheAspect.class.getName(), "Stop syncOptionsCache: " + cacheKey);
                }
            } else {
                AppUtil.setCurrentAppDefinition(appDef);
                HostManager.setCurrentProfile(profile);
                FormOptionsCacheAspect.syncOptionsCache(cacheKey);
            }
        } catch (Exception e) {
            LogUtil.error(FormOptionsCacheTask.class.getName(), e, "Profile : " + profile);
        }
    }
}