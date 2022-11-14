package org.kecak.apps.workflow.security;

import com.kinnarastudio.commons.Declutter;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.AuditTrailManager;
import org.joget.apps.app.web.LocalLocaleResolver;
import org.joget.commons.util.HostManager;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.directory.model.service.UserSecurity;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class JwtTokenProcessingFilter extends AbstractAuthenticationProcessingFilter implements Declutter {
    private WorkflowUserManager workflowUserManager;
    private AuditTrailManager auditTrailManager;
    private LocalLocaleResolver localeResolver;
    private DirectoryManager directoryManager;

    protected JwtTokenProcessingFilter() {
        super("/**");
    }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String header = request.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ");
//        return super.requiresAuthentication(request, response);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse httpServletResponse) throws AuthenticationException, ServletException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new JwtTokenMissingException("No JWT token found in request headers");
        }

        String authToken = header.substring(7);

        JwtAuthenticationToken authRequest = new JwtAuthenticationToken(authToken);

        Authentication authentication = getAuthenticationManager().authenticate(authRequest);
        if(authentication.isAuthenticated()) {
            UserDetails currnentUser = (UserDetails) authentication.getPrincipal();
            workflowUserManager.setCurrentThreadUser(currnentUser.getUsername());

            String loginAs = getOptionalParameter(request, "loginAs", "");
            if(isNotEmpty(loginAs) && workflowUserManager.isCurrentUserInRole(WorkflowUserManager.ROLE_ADMIN)) {
                workflowUserManager.setCurrentThreadUser(loginAs);
            }
        }
        return getAuthenticationManager().authenticate(authRequest);
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

    public void setWorkflowUserManager(WorkflowUserManager workflowUserManager) {
        this.workflowUserManager = workflowUserManager;
    }

    public void setAuditTrailManager(AuditTrailManager auditTrailManager) {
        this.auditTrailManager = auditTrailManager;
    }

    public void setLocaleResolver(LocalLocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    public void setDirectoryManager(DirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }


    /**
     * Get optional parameter for http request
     *
     * @param request
     * @param parameterName
     * @param defaultValue
     * @return
     */
    private String getOptionalParameter(HttpServletRequest request, String parameterName, String defaultValue) {
        return Optional.of(parameterName)
                .map(request::getParameter)
                .map(String::trim)
                .filter(not(String::isEmpty))
                .orElse(defaultValue);
    }
}
