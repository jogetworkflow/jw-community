package org.joget.apps.workflow.security;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.joget.commons.util.HostManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

public class SecurityHostFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest) || !(servletResponse instanceof HttpServletResponse)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        
        try {
            // Reset profile and set hostname
            HostManager.initHost();
            
            // Bind the request hostname before any OSGI plugin code runs (e.g. SSO callbacks),
            // so AuthenticationTokenWrapper captures the correct tenant host for all login paths.
            AuthenticationTokenWrapper.bindLoginHost(request.getServerName());
           
            if (!AuthenticationTokenWrapper.validateHost(request, response, getSessionAuthentication(request))) {
                return;
            }
            
            chain.doFilter(request, response);
        } finally {
            AuthenticationTokenWrapper.clearLoginHost();
        }    
    }

    private Authentication getSessionAuthentication(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object context = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (context instanceof SecurityContext) {
            return ((SecurityContext) context).getAuthentication();
        }
        return null;
    }
    
}
