package org.joget.apps.workflow.security;

import java.io.IOException;
import java.lang.reflect.Field;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.AuditTrailManager;
import org.joget.apps.app.web.LocalLocaleResolver;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.directory.model.service.UserSecurity;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.TextEscapeUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

public class WorkflowHttpAuthProcessingFilter extends UsernamePasswordAuthenticationFilter {

    private WorkflowUserManager workflowUserManager;
    private DirectoryManager directoryManager;
    private SetupManager setupManager;
    private LocalLocaleResolver localeResolver;
    private AuditTrailManager auditTrailManager;
    
    public WorkflowHttpAuthProcessingFilter() {
        super.setUsernameParameter("j_username");
        super.setPasswordParameter("j_password");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        return authenticate(request, response);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        Boolean requiresAuthentication;
        try {
            if (request != null) {
                // reset profile and set hostname
                HostManager.initHost();
            }
            if (localeResolver != null) {
                LocaleContext localeContext = localeResolver.resolveLocaleContext(request);
                LocaleContextHolder.setLocaleContext(localeContext, true);
            }
            
            // clear current app in thread
            AppUtil.resetAppDefinition();
            
            // clear current user
            workflowUserManager.clearCurrentThreadUser();
            
            requiresAuthentication = requiresAuthentication(request, response);

            super.doFilter(request, response, chain);
            
            String uri = request.getRequestURI();
            if (requiresAuthentication && !uri.startsWith(request.getContextPath() + "/j_spring_security_check") && !response.isCommitted()) {
                chain.doFilter(request, response);
            }
        } finally {
            /*
            // Uncomment this block to force JSON API requests to authenticate on every call
            String uri = request.getRequestURI();
            if (requiresAuthentication && uri.startsWith(request.getContextPath() + "/web/json") && !uri.contains("/web/json/directory/user/sso")) {
                // don't store authentication in session for json calls
                SecurityContextHolder.getContext().setAuthentication(null);
            }            
            */

            UserSecurity us = DirectoryUtil.getUserSecurity();
            if (us != null) {
                us.requestPostProcessing();
            }
            
            // clear current user
            workflowUserManager.clearCurrentThreadUser();
            LocaleContextHolder.resetLocaleContext();
            auditTrailManager.clean();
        }
    }
    
    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        boolean isAnonymous = workflowUserManager.isCurrentUserAnonymous();
        boolean requiresAuth = false;
        String uri = request.getRequestURI();
        int pathParamIndex = uri.indexOf(';');

        if (pathParamIndex > 0) {
            // strip everything after the first semi-colon
            uri = uri.substring(0, pathParamIndex);
        } 

        UserSecurity us = DirectoryUtil.getUserSecurity();
        if ((super.obtainUsername(request) != null)) {
            // request contains j_username, force authentication
            requiresAuth = true;
        } else if (us != null) {
            uri = uri.substring(request.getContextPath().length());
            if (us.getAuthenticateAllApi() && uri.startsWith("/web/json/") && (!uri.startsWith("/web/json/plugin") || uri.startsWith("/web/json/plugin/list")) && !uri.startsWith("/web/json/directory/user/sso") && !uri.startsWith("/web/json/workflow/currentUsername") && !uri.startsWith("/web/json/apps/published/userviews") && isAnonymous) {
                // authenticateAllApi flag is true, so force authentication for all json calls except for plugin, sso, and published userview calls
                requiresAuth = true;
            } else if (us.getForceSessionTimeout() && !isAnonymous) {
                // logged in, but timed out
                requiresAuth = true;
            }
        } 
        
        if (requiresAuth) {
            // generate new session to avoid session fixation vulnerability
            HttpSession session = request.getSession(false);
            if (session != null) {
                SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
                session.invalidate();
                session = request.getSession(true);
                if (savedRequest != null) {
                    new HttpSessionRequestCache().saveRequest(request, response);
                }
            }
        }
        return requiresAuth;
    }

    protected Authentication authenticate(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
        
        boolean isAnonymous = workflowUserManager.isCurrentUserAnonymous();
        UserSecurity us = DirectoryUtil.getUserSecurity();
        if (us != null && us.getForceSessionTimeout() && !isAnonymous) {
            throw new BadCredentialsException(ResourceBundleUtil.getMessage("authentication.failed.sessionTimeOut"));
        }
        
        Authentication auth = null;

        // check for username/password in request
        String username = super.obtainUsername(request);
        String password = super.obtainPassword(request);

        String loginAs = request.getParameter("loginAs");
        String loginHash = request.getParameter("hash");
        
        // Place the last username attempted into HttpSession for views
        HttpSession session = request.getSession(false);

        if (session != null || getAllowSessionCreation()) {
            request.getSession().setAttribute(SPRING_SECURITY_FORM_USERNAME_KEY, TextEscapeUtils.escapeEntities(username));
        }

        if (username != null && (password != null || loginHash != null)) {
            User currentUser = null;

            //diable master login based on UserSecurity
            if (us != null && us.getDisableHashLogin()) {
                loginAs = null;
            }
            
            if (loginAs != null) {
                String masterLoginUsername = getSetupManager().getSettingValue("masterLoginUsername");
                String masterLoginPassword = getSetupManager().getSettingValue("masterLoginPassword");
                
                //decryt masterLoginPassword
                masterLoginPassword = SecurityUtil.decrypt(masterLoginPassword);

                if ((masterLoginUsername != null && masterLoginUsername.trim().length() > 0) &&
                        (masterLoginPassword != null && masterLoginPassword.length() > 0)) {

                    User master = new User();
                    master.setUsername(masterLoginUsername.trim());
                    master.setPassword(StringUtil.md5Base16(masterLoginPassword));

                    if (username.trim().equals(master.getUsername()) &&
                            ((password != null && StringUtil.md5Base16(password).equalsIgnoreCase(master.getPassword())) ||
                            (loginHash != null && loginHash.trim().equalsIgnoreCase(master.getLoginHash())))) {
                        currentUser = directoryManager.getUserByUsername(loginAs);
                        if (currentUser != null) {
                            WorkflowUserDetails user = new WorkflowUserDetails(currentUser);
                            
                            auth = new UsernamePasswordAuthenticationToken(user, user.getUsername(), user.getAuthorities());
                            super.setDetails(request, (UsernamePasswordAuthenticationToken) auth);
                        } else {
                            LogUtil.info(getClass().getName(), "Authentication for user " + loginAs + ": " + false);
            
                            WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
                            workflowHelper.addAuditTrail(this.getClass().getName(), "authenticate", "Authentication for user " + loginAs + ": " + false, new Class[]{String.class}, new Object[]{loginAs}, false);
                        
                            throw new BadCredentialsException("");
                        }
                    }
                }
            } else {
                if (loginHash != null) {
                    password = loginHash.trim();
                }
                if (password != null) {
                    // use existing authentication manager
                    try {
                        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username.trim(), password);
                        super.setDetails(request, authRequest);

                        auth = getAuthenticationManager().authenticate(authRequest);

                        // no direct way in Spring Security 2, so use reflection to clear password in token
                        Field field = null;
                        try {
                            field = auth.getClass().getDeclaredField("credentials");
                            field.setAccessible(true);
                            field.set(auth, null);
                        } catch (Exception ex) {
                            LogUtil.error(getClass().getName(), ex, "Error clearing credentials in token");
                        } finally {
                            if (field != null) {
                                field.setAccessible(false);
                            }
                        }
                        
                        if (auth.isAuthenticated()) {
                            currentUser = directoryManager.getUserByUsername(username);
                        }
                    } catch (BadCredentialsException be) {
                        LogUtil.info(getClass().getName(), "Authentication for user " + ((loginAs == null) ? username : loginAs) + ": " + false);
            
                        WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
                        workflowHelper.addAuditTrail(this.getClass().getName(), "authenticate", "Authentication for user " + ((loginAs == null) ? username : loginAs) + ": " + false, new Class[]{String.class}, new Object[]{((loginAs == null) ? username : loginAs)}, false);
            
                        throw be;
                    }
                }
            }

            if (currentUser != null) {
                workflowUserManager.setCurrentThreadUser(currentUser.getUsername());
            }

            if (!"/WEB-INF/jsp/unauthorized.jsp".equals(request.getServletPath())) {
                LogUtil.info(getClass().getName(), "Authentication for user " + ((loginAs == null) ? username : loginAs) + ": " + true);
                WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
                workflowHelper.addAuditTrail(this.getClass().getName(), "authenticate", "Authentication for user " + ((loginAs == null) ? username : loginAs) + ": " + true, new Class[]{String.class}, new Object[]{((loginAs == null) ? username : loginAs)}, true);
            }
        } else {
            if (us != null && us.getAuthenticateAllApi()) {
                throw new BadCredentialsException("");
            }
        }

        return auth;
    }
    
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String uri = request.getRequestURI();
        if (!uri.startsWith(request.getContextPath() + "/j_spring_security_check")) {
            // set temporary per-request authentication
            SecurityContextHolder.getContext().setAuthentication(authResult);
            // clear system alert
            AppUtil.getSystemAlert();
        } else {
//            TargetUrlResolver resolver = getTargetUrlResolver();
//                    
//            if (resolver instanceof TargetUrlResolverImpl) {
//                ((TargetUrlResolverImpl)resolver).setJustUseSavedRequestOnGet(true);
//            }
//            
//            HttpSession session = request.getSession(false);
//            if (session != null) {
//                SavedRequest savedRequest = (SavedRequest) session.getAttribute(AbstractProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY);
//            }
            // default spring security login
            super.successfulAuthentication(request, response, chain, authResult);
        }
    }
    
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException ae) throws IOException, ServletException {
        String uri = request.getRequestURI();
        if (uri.startsWith(request.getContextPath() + "/web/json/")) {
            // return 401 for unauthorized JSON API calls
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            SimpleUrlAuthenticationFailureHandler failureHandler = (SimpleUrlAuthenticationFailureHandler) getFailureHandler();
            failureHandler.setDefaultFailureUrl("/web/login?login_error=1");
            
            super.unsuccessfulAuthentication(request, response, ae);
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

    public void setLocaleResolver(LocalLocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    public AuditTrailManager getAuditTrailManager() {
        return auditTrailManager;
    }

    public void setAuditTrailManager(AuditTrailManager auditTrailManager) {
        this.auditTrailManager = auditTrailManager;
    }
}
