package org.joget.workflow.shark;

import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.enhydra.shark.client.utilities.LimitStruct;
import org.enhydra.shark.utilities.MiscUtilities;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.ServerUtil;
import org.joget.commons.util.SetupDao;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.shark.model.dao.DeadlineDao;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.LocaleContextResolver;

public class DeadlineChecker extends Thread {
    
    public static final String DEADLINE_PREFIX = "deadlinecheker_";

    protected String profile;
    protected long delay;
    protected int instancesPerTransaction;
    protected int failuresToIgnore;
    protected boolean stopped = true;
    protected List limitStructs;
    protected boolean isInitialized = false;
    protected boolean deadlineReeval = false;

    public DeadlineChecker(String profile,
            List limitStructs,
            long delay,
            int instPerTrans,
            int failToIgnore,
            boolean startImmediatelly) {
        this.profile = profile;
        this.limitStructs = limitStructs;
        this.delay = delay;
        this.setDaemon(true);
        this.instancesPerTransaction = instPerTrans;
        this.failuresToIgnore = failToIgnore;

        if (startImmediatelly) {
            startChecker();
        }

        LogUtil.info(getClass().getName(), "Deadline checking time initialized to " + delay + " ms. Deadline times: " + limitStructs + ". Checking " + instancesPerTransaction + " instances per transaction, ignoring " + failuresToIgnore + " failures for profile " + profile);
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public List getLimitStructs() {
        return this.limitStructs;
    }

    public void setLimitStructs(List ls) {
        this.limitStructs = ls;
    }

    public void setLimitStructsByString(String str) {
        List lcStrLst = new ArrayList();
        try {
            String[] lcStructs = MiscUtilities.tokenize(str, ",");
            if (lcStructs != null) {
                for (int i = 0; i < lcStructs.length; i++) {
                    String lcstruct = lcStructs[i];
                    try {
                        String[] hma = MiscUtilities.tokenize(lcstruct, ":");
                        String h = hma[0];
                        String m = hma[1];
                        String a = hma[2];
                        lcStrLst.add(new LimitStruct(Integer.parseInt(h),
                                Integer.parseInt(m),
                                Integer.parseInt(a)));
                    } catch (Exception ex) {
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("DeadlineChecker: Problem while setting limit structures through string!");
        }
        setLimitStructs(lcStrLst);
    }

    public int getInstancesPerTransaction() {
        return instancesPerTransaction;
    }

    public void setInstancesPerTransaction(int insPerTrans) {
        this.instancesPerTransaction = insPerTrans;
    }

    public int getFailuresToIgnore() {
        return failuresToIgnore;
    }

    public void setFailuresToIgnore(int failToIgnore) {
        this.failuresToIgnore = failToIgnore;
    }

    public void stopChecker() {
        stopped = true;
    }

    public void startChecker() {
        stopped = false;
        if (!isInitialized) {
            isInitialized = true;
            start();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                sleep(delay);
                if (!stopped) {
                    runMe();
                }
            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), ex, "");
            }
        }
    }
    
    /*
     * Move it to a private method so that security manager can whitelist this method without 
     * affecting other processing.
     */
    private void setProfile() {
        HostManager.setCurrentProfile(profile);
    }

    protected void runMe() {
        try {
            setProfile();

            // verify datasource
            if (DynamicDataSourceManager.getProperties().isEmpty()) {
                // datasource is no longer available, stop the deadline checker
                stopChecker();
                LogUtil.info(getClass().getName(), "Datasource not available, deadline checker stopped for profile " + profile);
                return;
            }
            
            LogUtil.debug(getClass().getName(), "Checking deadlines for profile " + profile);
            long start = System.currentTimeMillis();
            
            String serverName = ServerUtil.getServerName();

            WorkflowManager workflowManager = (WorkflowManager) WorkflowUtil.getApplicationContext().getBean("workflowManager");
            DeadlineDao deadlineDao = (DeadlineDao) WorkflowUtil.getApplicationContext().getBean("deadlineDao");
            SetupDao setupDao = (SetupDao) WorkflowUtil.getApplicationContext().getBean("setupDao");
            Setting setting = null;
            
            LocaleContextResolver localeResolver = (LocaleContextResolver) WorkflowUtil.getApplicationContext().getBean("localeResolver");
            if (localeResolver != null) {
                LocaleContext localeContext = localeResolver.resolveLocaleContext(null);
                LocaleContextHolder.setLocaleContext(localeContext, true);
            }
            
            try {
                int sizeToCheck = 0;
                List<String> instancesFailed2check = new ArrayList<String>();
                Collection<String> instancesToCheck = deadlineDao.getProcessIdsWithDeadlines(System.currentTimeMillis());

                if (instancesToCheck != null && !instancesToCheck.isEmpty()) {
                    sizeToCheck = instancesToCheck.size();
                    Iterator iterProcesses = instancesToCheck.iterator();
                    List<String> currentBatch = null;
                    Set<String> currentCheck = null;
                    do {
                        currentBatch = new ArrayList<String>(); 
                        currentCheck = new HashSet<String>();
                        
                        //find current deadline thread
                        Collection<Setting> deadlines = setupDao.find("WHERE property like '" + DEADLINE_PREFIX + "%'", null, "property", Boolean.TRUE, null, null);
                        if (deadlines != null) {
                            for (Setting s : deadlines) {
                                if (s.getProperty().equals(DEADLINE_PREFIX+serverName)) {
                                    setting = s;
                                }
                                if (s.getValue() != null && !s.getValue().isEmpty()) {
                                    currentCheck.addAll(Arrays.asList(s.getValue().split(";")));
                                }
                            }
                        }
                        
                        try {
                            for (int n = 0; n < this.instancesPerTransaction;) {
                                if (!iterProcesses.hasNext()) {
                                    break;
                                }
                                
                                String procId = (String) iterProcesses.next();
                                iterProcesses.remove();
                                
                                if (currentCheck.contains(procId)) {
                                    continue;
                                }
                                
                                currentBatch.add(procId);
                                n++;
                            }
                            String[] pids = new String[currentBatch.size()];
                            currentBatch.toArray(pids);
                            
                            if (setting == null) {
                                setting = new Setting();
                                setting.setProperty(DEADLINE_PREFIX+serverName);
                            }
                            setting.setValue(StringUtils.join(pids, ";"));
                            setupDao.saveOrUpdate(setting);
                            
                            workflowManager.internalCheckDeadlines(pids);
                        } catch (Exception ex) {
                            LogUtil.error(getClass().getName(), ex, "Profile : " + profile);
                            instancesFailed2check.addAll(currentBatch);
                        }
                    } while (instancesFailed2check.size() <= this.failuresToIgnore && iterProcesses.hasNext());
                }

                long end = System.currentTimeMillis();

                LogUtil.debug(getClass().getName(), "Deadline check lasted " + (end - start) + " ms for profile " + profile + ". Checked:" + sizeToCheck + ". Failed:" + instancesFailed2check.size());
            } finally {
                if (setting != null) {
                    setupDao.delete(setting);
                }
                
                WorkflowHelper workflowMapper = (WorkflowHelper) WorkflowUtil.getApplicationContext().getBean("workflowHelper");
                workflowMapper.cleanForDeadline();
            }
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "");
        }
    }

    public boolean isStopped(){
        return stopped;
    }
}
