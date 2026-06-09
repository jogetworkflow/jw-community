package org.joget.apps.app.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
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
        String appId = null;
        if (request != null) {
            ip = AppUtil.getClientIp(request);

            // capture app id
            String referer = request.getHeader("referer");
            if (referer != null) {
                Pattern pattern = Pattern.compile("/web/ulogin/([^/]+)/"); // Regex to extract the segment after /web/ulogin/ and before the next /
                Matcher matcher = pattern.matcher(referer);
                if (matcher.find()) {
                    appId = matcher.group(1);
                } else {
                    pattern = Pattern.compile("/web/userview/([^/]+)/"); // Regex to extract the segment after /web/userview/ and before the next /
                    matcher = pattern.matcher(referer);
                    if (matcher.find()) {
                        appId = matcher.group(1);
                    }
                }
            }
        }

        if (appId != null) {
            AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
            AppDefinition appDef = appService.getPublishedAppDefinition(appId);
            AppUtil.setCurrentAppDefinition(appDef);
        }

        workflowHelper.addAuditTrail(this.getClass().getName(), "logout",
                "Logout for user " + username + " (" + ip + ")", new Class[] { String.class },
                new Object[] { username }, false);
    }

}