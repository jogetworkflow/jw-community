package org.joget.apps.workflow.security;

import org.joget.commons.util.LogUtil;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.commons.util.SetupManager;
import org.joget.directory.model.User;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;

public class WorkflowHttpAuthProcessingFilter extends AuthenticationProcessingFilter {

    private WorkflowUserManager workflowUserManager;
    private DirectoryManager directoryManager;
    private SetupManager setupManager;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request) throws AuthenticationException {
        return super.attemptAuthentication(request);
    }

    @Override
    public void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            // clear current user
            workflowUserManager.clearCurrentThreadUser();

            // authenticate from request
            authenticate(request);

            super.doFilterHttp(request, response, chain);
        } finally {
            // clear current user
            workflowUserManager.clearCurrentThreadUser();
        }
    }

    protected boolean authenticate(HttpServletRequest request) {
        boolean authenticated = false;

        // check for username/password in request
        String username = super.obtainUsername(request);
        String password = super.obtainPassword(request);

        String loginAs = request.getParameter("loginAs");
        String loginHash = request.getParameter("hash");

        if (username != null && (password != null || loginHash != null)) {
            User currentUser = null;

            if (loginAs != null) {
                String masterLoginUsername = getSetupManager().getSettingValue("masterLoginUsername");
                String masterLoginPassword = getSetupManager().getSettingValue("masterLoginPassword");

                if ((masterLoginUsername != null && masterLoginUsername.trim().length() > 0) &&
                        (masterLoginPassword != null && masterLoginPassword.trim().length() > 0)) {

                    User master = new User();
                    master.setUsername(masterLoginUsername.trim());
                    master.setPassword(masterLoginPassword.trim());

                    if (username.trim().equals(master.getUsername()) &&
                            ((password != null && password.trim().equals(master.getPassword())) ||
                            (loginHash != null && loginHash.trim().equalsIgnoreCase(master.getLoginHash())))) {
                        currentUser = directoryManager.getUserByUsername(loginAs);
                    }
                }
            } else {
                if (username != null && (password != null || loginHash != null)) {
                    if (loginHash != null) {
                        password = loginHash;
                    }
                    // TODO: pluggable authentication, use existing authentication manager for now
                    try {
                        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username.trim(), password.trim());
                        Authentication auth = getAuthenticationManager().authenticate(authRequest);
                        if (auth.isAuthenticated()) {
                            currentUser = directoryManager.getUserByUsername(username);
                        }
                    } catch (BadCredentialsException be) {
                        // ignore
                    }
                }
            }

            if (currentUser != null) {
                workflowUserManager.setCurrentThreadUser(currentUser.getUsername());
                authenticated = true;
            }

            LogUtil.info(getClass().getName(), "Authentication for user " + ((loginAs == null) ? username : loginAs) + ": " + authenticated);
        }

        return authenticated;
    }

    public WorkflowUserManager getWorkflowUserManager() {
        return workflowUserManager;
    }

    public void setWorkflowUserManager(WorkflowUserManager workflowUserManager) {
        this.workflowUserManager = workflowUserManager;
    }

    public DirectoryManager getDirectoryManager() {
        return directoryManager;
    }

    public void setDirectoryManager(DirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    public SetupManager getSetupManager() {
        return setupManager;
    }

    public void setSetupManager(SetupManager setupManager) {
        this.setupManager = setupManager;
    }
}
