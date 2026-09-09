package org.joget.governance.model;

import java.util.concurrent.ScheduledFuture;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.HostManager;
import org.joget.governance.service.GovHealthCheckManager;

public final class GovHealthCheckTask implements Runnable {
    private String profile;
    private long interval;
    private ScheduledFuture scheduledFuture;
    
    public GovHealthCheckTask(String profile, long interval) {
        this.profile = profile;
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
