package org.joget.workflow.shark;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.DeadlineThreadManager;

public class SharkDeadlineThreadManager extends DeadlineThreadManager{

    public static final long INTERVAL_MINIMUM = 10000; // 10 seconds
    private static Map<String, DeadlineChecker> threadMap = Collections.synchronizedMap(new HashMap<String, DeadlineChecker>());

    public void init(WorkflowManager workflowManager) {

        // stop and clear all threads
        for (String profile : threadMap.keySet()) {
            stopThread(profile);
        }
        threadMap.clear();

        if (HostManager.isVirtualHostEnabled()) {
            // find all profiles and start threads
            Properties profiles = DynamicDataSourceManager.getProfileProperties();
            Set<String> profileSet = new HashSet(profiles.values());
            // Per-profile try/catch/finally, matching GovHealthCheckManager's own constructor --
            // two reasons this matters here, not just style: (1) a single unreachable/not-yet-
            // provisioned tenant database must not abort every OTHER tenant's own deadline-checker
            // start-up (the previous unconditional setCurrentProfile(profile) with no per-iteration
            // reset meant one exception here propagated out of the whole loop, and out of this
            // bean's own construction, since init(..) is called from WorkflowManagerImpl
            // .setSetupManager(..)); (2) now that WorkflowManagerImpl's own bare whitelist entry has
            // been removed (see MultiTenantSecurityManager's own comment on why), a restricted
            // caller's setCurrentProfile(profile) call here is refused with a SecurityException for
            // any profile it isn't entitled to -- that must be caught and skipped per profile, not
            // let to abort every remaining tenant's legitimate initialization too.
            //
            // `switched` guards the finally's own resetProfile() call: HostManager.setCurrentProfile
            // only pushes a restore entry AFTER its own permission check succeeds, so a REFUSED
            // setCurrentProfile leaves nothing on the stack for this iteration at all. Calling
            // resetProfile() unconditionally in that case would pop/restore whatever entry some
            // OUTER, unrelated scope had already pushed -- corrupting that caller's own restore
            // chain instead of doing nothing, exactly the "unpaired reset" shape this whole file's
            // own fix elsewhere in this codebase exists to prevent.
            for (String profile : profileSet) {
                if (profile.contains(",")) {
                    continue;
                }
                boolean switched = false;
                try {
                    HostManager.setCurrentProfile(profile);
                    switched = true;
                    workflowManager.internalRecoverStuckToolActivities();
                    workflowManager.internalUpdateDeadlineChecker();
                } catch (Exception e) {
                    LogUtil.error(SharkDeadlineThreadManager.class.getName(), e, "Error initializing deadline checker for " + profile);
                } finally {
                    if (switched) {
                        HostManager.resetProfile();
                    }
                }
            }
        }
        else if (DynamicDataSourceManager.getCurrentProfile() == null) {
            // no datasource profile configured yet (fresh install before /setup): touching the
            // workflow engine now would initialize Shark without a database and leave it broken
            // after the setup wizard rebuilds the application context
            LogUtil.info(SharkDeadlineThreadManager.class.getName(), "No datasource profile configured, deadline checker not started");
        }
        else {
            // start current profile
            workflowManager.internalRecoverStuckToolActivities();
            workflowManager.internalUpdateDeadlineChecker();
        }
    }

    public void start(long interval) {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        if (interval <= 0) {
            stopThread(profile);
        } else {
            if (interval < INTERVAL_MINIMUM) {
                interval = INTERVAL_MINIMUM;
            }
            DeadlineChecker thread = getThread(profile);
            if (thread == null) {
                thread = new DeadlineChecker(profile,
                    null,
                    interval,
                    10,
                    10,
                    true);
                threadMap.put(profile, thread);
            }
            else {
                if (interval != thread.getDelay()){
                    LogUtil.info(DeadlineThreadManager.class.getName(), "Deadline checking time initialized to " + interval + " ms. Deadline times: " + thread.getLimitStructs() + ". Checking " + thread.getInstancesPerTransaction() + " instances per transaction, ignoring " + thread.getFailuresToIgnore() + " failures for profile " + thread.getProfile());
                }
                thread.setDelay(interval);
                if (thread.isStopped()) {
                    LogUtil.info(DeadlineThreadManager.class.getName(), "Starting DeadlineChecker for profile " + profile);
                    thread.startChecker();
                }
            }
        }
    }
    
    public boolean isCheckerRunning() {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        DeadlineChecker thread = getThread(profile);
        if (thread != null && !thread.isStopped()) {
            return true;
        }
        return false;
    }

    protected static DeadlineChecker getThread(String profile) {
        DeadlineChecker thread = (DeadlineChecker)threadMap.get(profile);
        return thread;
    }

    protected static void stopThread(String profile) {
        DeadlineChecker thread = getThread(profile);
        if (thread != null) {
            LogUtil.info(DeadlineThreadManager.class.getName(), "Stopping DeadlineChecker for profile " + profile);
            thread.stopChecker();
            threadMap.remove(profile);
        }
    }

}
