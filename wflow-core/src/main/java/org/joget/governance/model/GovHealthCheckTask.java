package org.joget.governance.model;

import java.util.concurrent.ScheduledFuture;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.governance.service.GovHealthCheckManager;

public final class GovHealthCheckTask implements Runnable {
    private final String profile;
    private long interval;
    private ScheduledFuture scheduledFuture;

    // Profile captured from the ambient context at construction, never from the caller: run() restores
    // it on a pooled scheduler thread and this class is exempted from the profile-mutation guard, so a
    // caller-supplied profile would let untrusted code switch the ambient profile to one it does not own
    // by running the task from this exempted frame. Capturing here pins the task to its constructor's
    // tenant.
    public GovHealthCheckTask(long interval) {
        this.profile = DynamicDataSourceManager.getCurrentProfile();
        this.interval = interval;
    }
    
    public ScheduledFuture getScheduledFuture() {
        return scheduledFuture;
    }

    public void setScheduledFuture(ScheduledFuture scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public long getInterval() {
        return interval;
    }
    
    @Override
    public void run() {
        // Runs on a pooled ThreadPoolTaskScheduler thread. Without try/finally, an exception from
        // runCheck() leaves HostManager's previousProfileStack ThreadLocal leaked on that pooled
        // thread, recoverable by a later, unrelated task via a bare HostManager.resetProfile()
        // call (unguarded, no SecurityManager check applies to it).
        try {
            HostManager.setCurrentProfile(profile);
            GovHealthCheckManager govHealthCheckManager = (GovHealthCheckManager) AppUtil.getApplicationContext().getBean("govHealthCheckManager");
            govHealthCheckManager.runCheck();
        } finally {
            HostManager.resetProfile();
        }
    }
}
