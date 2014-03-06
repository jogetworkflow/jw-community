package org.joget.apps.app.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JsonResponseFilter implements Filter {
    
    public void init(FilterConfig fc) throws ServletException {
        // nothing to init
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse)) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            String callback = httpRequest.getParameter("callback");
            
            if (callback != null && !callback.isEmpty()) {
                httpResponse.setContentType("application/javascript; charset=utf-8");
            } else {
                httpResponse.setContentType("application/json; charset=utf-8");
            }
            
            filterChain.doFilter(httpRequest, httpResponse);	
        }
    }

    public void destroy() {
        // nothing to destroy
    }
}
