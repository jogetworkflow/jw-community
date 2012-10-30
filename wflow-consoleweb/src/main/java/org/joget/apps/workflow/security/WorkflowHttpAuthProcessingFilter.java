package org.joget.apps.workflow.security;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;
import org.springframework.web.servlet.LocaleResolver;

public class WorkflowHttpAuthProcessingFilter extends AuthenticationProcessingFilter {

    private WorkflowUserManager workflowUserManager;
    private DirectoryManager directoryManager;
    private SetupManager setupManager;
    private LocaleResolver localeResolver;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request) throws AuthenticationException {
        return super.attemptAuthentication(request);
    }

    @Override
    public void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            if (localeResolver != null) {
                Locale locale = localeResolver.resolveLocale(request);
                LocaleContextHolder.setLocale(locale);
            }
            
            // clear current app in thread
            AppUtil.resetAppDefinition();
            
            // clear current user
            workflowUserManager.clearCurrentThreadUser();

            // authenticate from request
            authenticate(request);

            super.doFilterHttp(request, response, chain);
        } finally {
            // clear current user
            workflowUserManager.clearCurrentThreadUser();
            LocaleContextHolder.resetLocaleContext();
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
                    master.setPassword(StringUtil.md5Base16(masterLoginPassword.trim()));

                    if (username.trim().equals(master.getUsername()) &&
                            ((password != null && StringUtil.md5Base16(password.trim()).equalsIgnoreCase(master.getPassword())) ||
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
            
            WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
            workflowHelper.addAuditTrail("WorkflowHttpAuthProcessingFilter", "authenticate", "Authentication for user " + ((loginAs == null) ? username : loginAs) + ": " + authenticated);
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
    
    public LocaleResolver getLocaleResolver() {
        return localeResolver;
    }

    public void setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }
}
