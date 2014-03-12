package org.joget.apps.workflow.security;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
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
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;
import org.springframework.security.util.TextUtils;
import org.springframework.web.servlet.LocaleResolver;

public class WorkflowHttpAuthProcessingFilter extends AuthenticationProcessingFilter {

    private WorkflowUserManager workflowUserManager;
    private DirectoryManager directoryManager;
    private SetupManager setupManager;
    private LocaleResolver localeResolver;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request) throws AuthenticationException {
        return authenticate(request);
    }

    @Override
    public void doFilterHttp(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        Boolean requiresAuthentication = false;
        try {
            if (localeResolver != null) {
                Locale locale = localeResolver.resolveLocale(request);
                LocaleContextHolder.setLocale(locale);
            }
            
            // clear current app in thread
            AppUtil.resetAppDefinition();
            
            // clear current user
            workflowUserManager.clearCurrentThreadUser();
            
            requiresAuthentication = requiresAuthentication(request, response);

            super.doFilterHttp(request, response, chain);
            
            String uri = request.getRequestURL().toString();
            if (requiresAuthentication && (uri.contains("/web/json/") || uri.contains("/web/ulogin/"))) {
                chain.doFilter(request, response);
            }
        } finally {
            String uri = request.getRequestURL().toString();
            if (requiresAuthentication && uri.contains("/web/json/") && !uri.contains("/web/json/directory/user/sso")) {
                SecurityContextHolder.getContext().setAuthentication(null);
            }
            // clear current user
            workflowUserManager.clearCurrentThreadUser();
            LocaleContextHolder.resetLocaleContext();
        }
    }
    
    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        int pathParamIndex = uri.indexOf(';');

        if (pathParamIndex > 0) {
            // strip everything after the first semi-colon
            uri = uri.substring(0, pathParamIndex);
        }

        if ("".equals(request.getContextPath())) {
            return uri.endsWith(getFilterProcessesUrl());
        }
        
        if ((uri.contains("/web/json/") || uri.contains("/web/ulogin/")) && super.obtainUsername(request) != null && WorkflowUserManager.ROLE_ANONYMOUS.equals(workflowUserManager.getCurrentUsername())) {
            return true;
        }

        return uri.endsWith(request.getContextPath() + getFilterProcessesUrl());
    }

    protected Authentication authenticate(HttpServletRequest request) throws AuthenticationException {
        Authentication auth = null;

        // check for username/password in request
        String username = super.obtainUsername(request);
        String password = super.obtainPassword(request);

        String loginAs = request.getParameter("loginAs");
        String loginHash = request.getParameter("hash");
        
        // Place the last username attempted into HttpSession for views
        HttpSession session = request.getSession(false);

        if (session != null || getAllowSessionCreation()) {
            request.getSession().setAttribute(SPRING_SECURITY_LAST_USERNAME_KEY, TextUtils.escapeEntities(username));
        }

        if (username != null && (password != null || loginHash != null)) {
            User currentUser = null;

            if (loginAs != null) {
                String masterLoginUsername = getSetupManager().getSettingValue("masterLoginUsername");
                String masterLoginPassword = getSetupManager().getSettingValue("masterLoginPassword");

                if ((masterLoginUsername != null && masterLoginUsername.trim().length() > 0) &&
                        (masterLoginPassword != null && masterLoginPassword.trim().length() > 0)) {

                    //decryt masterLoginPassword
                    masterLoginPassword = SecurityUtil.decrypt(masterLoginPassword);
                    
                    User master = new User();
                    master.setUsername(masterLoginUsername.trim());
                    master.setPassword(StringUtil.md5Base16(masterLoginPassword.trim()));

                    if (username.trim().equals(master.getUsername()) &&
                            ((password != null && StringUtil.md5Base16(password.trim()).equalsIgnoreCase(master.getPassword())) ||
                            (loginHash != null && loginHash.trim().equalsIgnoreCase(master.getLoginHash())))) {
                        currentUser = directoryManager.getUserByUsername(loginAs);
                        if (currentUser != null) {
                            WorkflowUserDetails user = new WorkflowUserDetails(currentUser);
                            
                            auth = new UsernamePasswordAuthenticationToken(user, user.getUsername(), user.getAuthorities());
                            super.setDetails(request, (UsernamePasswordAuthenticationToken) auth);
                        } else {
                            LogUtil.info(getClass().getName(), "Authentication for user " + ((loginAs == null) ? username : loginAs) + ": " + false);
            
                            WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
                            workflowHelper.addAuditTrail("WorkflowHttpAuthProcessingFilter", "authenticate", "Authentication for user " + ((loginAs == null) ? username : loginAs) + ": " + false);
                        
                            throw new BadCredentialsException("");
                        }
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
                        super.setDetails(request, authRequest);

                        auth = getAuthenticationManager().authenticate(authRequest);
                        if (auth.isAuthenticated()) {
                            currentUser = directoryManager.getUserByUsername(username);
                        }
                    } catch (BadCredentialsException be) {
                        LogUtil.info(getClass().getName(), "Authentication for user " + ((loginAs == null) ? username : loginAs) + ": " + false);
            
                        WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
                        workflowHelper.addAuditTrail("WorkflowHttpAuthProcessingFilter", "authenticate", "Authentication for user " + ((loginAs == null) ? username : loginAs) + ": " + false);
            
                        throw be;
                    }
                }
            }

            if (currentUser != null) {
                workflowUserManager.setCurrentThreadUser(currentUser.getUsername());
            }

            LogUtil.info(getClass().getName(), "Authentication for user " + ((loginAs == null) ? username : loginAs) + ": " + true);
            
            WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
            workflowHelper.addAuditTrail("WorkflowHttpAuthProcessingFilter", "authenticate", "Authentication for user " + ((loginAs == null) ? username : loginAs) + ": " + true);
        }

        return auth;
    }
    
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException, ServletException {
        String uri = request.getRequestURL().toString();
        if (uri.contains("/web/json/") || uri.contains("/web/ulogin/")) {
            SecurityContextHolder.getContext().setAuthentication(authResult);
        } else {
            super.successfulAuthentication(request, response, authResult);
        }
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
