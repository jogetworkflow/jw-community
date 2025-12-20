package org.joget.apps.app.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.service.IdentityProviderManager;
import org.joget.directory.model.service.IdpMfaUtil;
import org.joget.directory.model.service.SessionInvalidationService;
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

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        // log logout event for logged in users
        WorkflowUserManager workflowUserManager = (WorkflowUserManager)AppUtil.getApplicationContext().getBean("workflowUserManager");

        // clear current thread user to obtain the correct user from SecurityContextHolder
        workflowUserManager.clearCurrentThreadUser();
        if (!workflowUserManager.isCurrentUserAnonymous()) {
            logoutAuditTrail();

            // Perform IdP logout
            IdentityProviderManager identityProviderManager = IdpMfaUtil.getIdpManager();
            if (identityProviderManager != null) {
                HttpSession session = event.getSession();
                Object pluginUuid = session.getAttribute(IdentityProviderManager.LOGGED_IN_IDP_SESSION_KEY);
                if (pluginUuid != null && !pluginUuid.toString().trim().isEmpty()) {
                    identityProviderManager.logout(pluginUuid.toString(), session);
                }
            }

            // Remove active session
            try {
                SessionInvalidationService sessionInvalidationService = (SessionInvalidationService) AppUtil.getApplicationContext().getBean("sessionInvalidationService");
                HttpSession session = event.getSession();
                String sessionId = (String) session.getAttribute(SessionInvalidationService.KEY_CURRENT_SESSION_ID);
                sessionInvalidationService.removeActiveSession(workflowUserManager.getCurrentUsername(), sessionId);
            } catch (Exception e) {
                LogUtil.error(SessionListener.class.getName(), e, "");
            }
        }
    }

    /**
     * Logs logout in the audit trail
     */
    protected void logoutAuditTrail() {
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) AppUtil.getApplicationContext()
                .getBean("workflowUserManager");
        String username = workflowUserManager.getCurrentUsername();
        WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
        String ip = "";
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            ip = AppUtil.getClientIp(request);
        }
        workflowHelper.addAuditTrail(this.getClass().getName(), "logout",
                "Logout for user " + username + " (" + ip + ")", new Class[] { String.class },
                new Object[] { username }, false);
    }

}