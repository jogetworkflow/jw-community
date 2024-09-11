package org.joget.workflow.util;

import org.joget.workflow.model.service.WorkflowManager;

public abstract class DeadlineThreadManager {
    
    public abstract void init(WorkflowManager workflowManager);
    
    public abstract void start(long interval);
    
    public abstract boolean isCheckerRunning();
    
    public static DeadlineThreadManager getInstance() {
        return (DeadlineThreadManager) WorkflowUtil.getApplicationContext().getBean("deadlineThreadManager");
    }
    
    public static void initThreads(WorkflowManager workflowManager) {
        getInstance().init(workflowManager);
    }
    
    public static void startThread(long interval) {
        getInstance().start(interval);
    }
    
    public static boolean isDeadlineChekerRunning() {
        return getInstance().isCheckerRunning();
    }
}
