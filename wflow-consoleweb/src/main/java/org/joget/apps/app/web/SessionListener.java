package org.joget.apps.app.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.joget.apps.app.service.AppUtil;
import org.joget.directory.model.service.IdentityProviderManager;
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

        // clear current thread user to obtain the correct user from SecurityContextHolder
        workflowUserManager.clearCurrentThreadUser();
        if (!workflowUserManager.isCurrentUserAnonymous()) {
            logoutAuditTrail();

            // Perform IdP logout
            Object identityProviderManager = AppUtil.getApplicationContext().getBean("identityProviderManager");
            if (identityProviderManager instanceof IdentityProviderManager) {
                HttpSession session = event.getSession();
                Object pluginUuid = session.getAttribute(IdentityProviderManager.LOGGED_IN_IDP_SESSION_KEY);
                if (pluginUuid != null && !pluginUuid.toString().trim().isEmpty()) {
                    ((IdentityProviderManager) identityProviderManager).logout(pluginUuid.toString(), session);
                }
            }
        }
    }

    /**
     * Logs logout in the audit trail
     */
    protected void logoutAuditTrail() {
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