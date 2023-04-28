package org.kecak.apps.app.web;

import javax.servlet.*;
import java.io.IOException;

/**
 * Prepared for next development improvement
 */
public class WebSocketFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // do nothing
        filterChain.doFilter(servletRequest, servletResponse);
    }

}
