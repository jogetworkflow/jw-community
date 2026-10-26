package org.joget.apps.form.service;

import java.util.concurrent.ScheduledFuture;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;

public class FormOptionsCacheTask implements Runnable {
    private final String profile;
    private String cacheKey;
    private AppDefinition appDef;
    private ScheduledFuture scheduledFuture;

    // The profile is captured from the ambient context at CONSTRUCTION, never accepted from the caller:
    // the task later restores it on a pooled scheduler thread, and this class is exempted from the
    // profile-mutation guard. A caller-supplied profile would let untrusted code (plugin/BeanShell)
    // instantiate the task with another tenant's profile and, by running it from this exempted frame,
    // switch the ambient profile to one it does not own. Capturing getCurrentProfile() here pins the
    // task to the profile of whoever constructs it, so it can only ever run as that same tenant.
    public FormOptionsCacheTask(String cacheKey, AppDefinition appDef) {
        this.profile = DynamicDataSourceManager.getCurrentProfile();
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
                // This task is rescheduled on a ScheduledFuture/pooled scheduler thread. Without
                // try/finally here, an exception from syncOptionsCache() leaves HostManager's
                // previousProfileStack ThreadLocal leaked on that pooled thread, recoverable by a
                // later, unrelated task via a bare HostManager.resetProfile() call (unguarded, no
                // SecurityManager check applies to it).
                try {
                    HostManager.setCurrentProfile(profile);
                    FormOptionsCacheAspect.syncOptionsCache(cacheKey);
                } finally {
                    HostManager.resetProfile();
                }
            }
        } catch (Exception e) {
            LogUtil.error(FormOptionsCacheTask.class.getName(), e, "Profile : " + profile);
        }
    }
}