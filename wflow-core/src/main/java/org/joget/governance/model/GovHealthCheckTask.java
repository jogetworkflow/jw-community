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
        HostManager.setCurrentProfile(profile);
        GovHealthCheckManager govHealthCheckManager = (GovHealthCheckManager) AppUtil.getApplicationContext().getBean("govHealthCheckManager");
        govHealthCheckManager.runCheck();
    }
}
