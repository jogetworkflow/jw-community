package org.joget.apps.workflow.security;

import org.joget.directory.model.service.SessionInvalidationService;
import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SetupManager;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.directory.model.service.UserSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

public class SessionSecurityFilter extends GenericFilterBean {

    private SessionInvalidationService sessionInvalidationService;
    private SetupManager setupManager;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        UserSecurity us = DirectoryUtil.getUserSecurity();
        if (us != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    String username = authentication.getName();
                    boolean valid = sessionInvalidationService.validateUserSession(username, request);

                    if (!valid) {
                        session.invalidate();
                        SecurityContextHolder.clearContext();
                        
                        request.getSession(true).setAttribute("SPRING_SECURITY_LAST_EXCEPTION", new RuntimeException(ResourceBundleUtil.getMessage("authentication.failed.sessionTimeOut")));
                        
                        // Redirect to login error page
                        response.sendRedirect(request.getContextPath() + "/web/login?login_error=1");
                        return;
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }

    public void setSessionInvalidationService(SessionInvalidationService sessionInvalidationService) {
        this.sessionInvalidationService = sessionInvalidationService;
    }

    public void setSetupManager(SetupManager setupManager) {
        this.setupManager = setupManager;
    }
}
