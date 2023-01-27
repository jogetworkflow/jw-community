package org.joget.apps.app.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.joget.apps.app.service.AppUtil;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;

/**
 * HTTP session listener to capture logout events.
 */
public class SessionListener implements HttpSessionListener {
 
    public void sessionCreated(HttpSessionEvent event) {
        // do nothing
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        // log logout event for logged in users
        WorkflowUserManager workflowUserManager = (WorkflowUserManager)AppUtil.getApplicationContext().getBean("workflowUserManager");
        if (!workflowUserManager.isCurrentUserAnonymous()) {
            logout();
        }
    }

    /**
     * Logs logout in the audit trail
     */
    protected void logout() {
        WorkflowUserManager workflowUserManager = (WorkflowUserManager)AppUtil.getApplicationContext().getBean("workflowUserManager");
        String username = workflowUserManager.getCurrentUsername();
        WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
        String ip = "";
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            ip = AppUtil.getClientIp(request);
        }
        workflowHelper.addAuditTrail(this.getClass().getName(), "logout", "Logout for user " + username + " ("+ip+")", new Class[]{String.class}, new Object[]{username}, false);
    }
 
}