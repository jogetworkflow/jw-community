package org.joget.apps.app.web;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

public class CSRFTokenFilter implements Filter {
    private static final String OLD_TOKEN_NAME = "OWASP_CSRFTOKEN";
    private static final String NEW_TOKEN_NAME = "OWASP-CSRFTOKEN";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String reqOldHeader = httpRequest.getHeader(OLD_TOKEN_NAME);
        String reqNewHeader = httpRequest.getHeader(NEW_TOKEN_NAME);
        String reqOldParam = httpRequest.getParameter(OLD_TOKEN_NAME);
        String reqNewParam = httpRequest.getParameter(NEW_TOKEN_NAME);

        ServletRequest modifiedRequest = handleRequestTokens(httpRequest, reqOldHeader, reqNewHeader, reqOldParam, reqNewParam);

        chain.doFilter(modifiedRequest, response);

        String resNewHeader = httpResponse.getHeader(NEW_TOKEN_NAME);
        if (resNewHeader != null && reqOldHeader != null && reqNewHeader == null) {
            httpResponse.setHeader(OLD_TOKEN_NAME, resNewHeader);
        }
    }

    private ServletRequest handleRequestTokens(HttpServletRequest request, 
            String oldHeader, String newHeader, String oldParam, String newParam) {

        if (oldHeader != null && newHeader == null) {
            request = wrapRequestForHeader(request, oldHeader);
        }

        if (oldParam != null && newParam == null) {
            request = wrapRequestForParameter(request);
        }
        
        return request;
    }

    private HttpServletRequestWrapper wrapRequestForHeader(HttpServletRequest request, String oldHeader) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (NEW_TOKEN_NAME.equalsIgnoreCase(name)) {
                    return oldHeader;
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                List<String> names = Collections.list(super.getHeaderNames());
                if (!names.stream().anyMatch(n -> NEW_TOKEN_NAME.equalsIgnoreCase(n))) {
                    names.add(NEW_TOKEN_NAME);
                }
                return Collections.enumeration(names);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (NEW_TOKEN_NAME.equalsIgnoreCase(name)) {
                    return Collections.enumeration(Collections.singletonList(oldHeader));
                }
                return super.getHeaders(name);
            }
        };
    }

    private HttpServletRequestWrapper wrapRequestForParameter(HttpServletRequest request) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(String name) {
                if (NEW_TOKEN_NAME.equalsIgnoreCase(name)) {
                    return super.getParameter(OLD_TOKEN_NAME);
                }
                return super.getParameter(name);
            }
        };
    }
}