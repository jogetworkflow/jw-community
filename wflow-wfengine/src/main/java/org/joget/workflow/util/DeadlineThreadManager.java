package org.joget.workflow.util;

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
import org.joget.workflow.shark.DeadlineChecker;

public class DeadlineThreadManager {

    public static final long INTERVAL_MINIMUM = 10000; // 10 seconds
    private static Map<String, DeadlineChecker> threadMap = Collections.synchronizedMap(new HashMap<String, DeadlineChecker>());

    public static void initThreads(WorkflowManager workflowManager) {

        // stop and clear all threads
        for (String profile : threadMap.keySet()) {
            stopThread(profile);
        }
        threadMap.clear();

        if (HostManager.isVirtualHostEnabled()) {
            // find all profiles and start threads
            Properties profiles = DynamicDataSourceManager.getProfileProperties();
            Set<String> profileSet = new HashSet(profiles.values());
            for (String profile : profileSet) {
                if (profile.contains(",")) {
                    continue;
                }
                HostManager.setCurrentProfile(profile);
                workflowManager.internalRecoverStuckToolActivities();
                workflowManager.internalUpdateDeadlineChecker();
            }
            HostManager.setCurrentProfile(null);
        }
        else {
            // start current profile
            workflowManager.internalRecoverStuckToolActivities();
            workflowManager.internalUpdateDeadlineChecker();
        }
    }

    public static void startThread(long interval) {
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
