package org.joget.commons.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Thread implementation to used by plugin
 */
public final class PluginThread extends Thread {
    
    private final String profile;
    private HttpServletRequest request;
    
    public PluginThread(Runnable r) {
        super(r);
        profile = DynamicDataSourceManager.getCurrentProfile();
        ServletRequestAttributes sra = null;
        try {
            sra = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes());
        } catch (IllegalStateException e) {
        }
        if (sra != null) {
            request = new PluginThreadHttpRequest(sra.getRequest());
        } else {
            request = null;
        }
    }
    
    private void setProfile() {
        HostManager.setCurrentProfile(profile);
    }
    
    @Override
    public void run() {
        setProfile();
        
        if (request != null) {
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        }
        
        super.run();
        
        if (request != null) {
            RequestContextHolder.resetRequestAttributes();
            request = null;
        }
    }
    
    /**
     * A dummy request to copy the current HTTP request data to use by request hash variable in plugin thread
     */
    public final class PluginThreadHttpRequest implements HttpServletRequest {
        
        private Map<String, List<String>> headers = new HashMap<>();
        private String method;
        private String pathInfo;
        private String contextPath;
        private String queryString;
        private String requestedSessionId;
        private String requestURI;
        private StringBuffer requestURL;
        private String servletPath;
        private String characterEncoding;
        private Map<String, Object> attributes = new HashMap<>();
        private Map<String, String[]> parameterMap;
        private String protocol;
        private String schema;
        private String serverName;
        private int serverPort;
        private Locale locale;
        private String remoteAddr;
        
        public PluginThreadHttpRequest(HttpServletRequest request) {
            this.method = request.getMethod();
            this.pathInfo = request.getPathInfo();
            this.contextPath = request.getContextPath();
            this.queryString = request.getQueryString();
            this.requestedSessionId = request.getRequestedSessionId();
            this.requestURI = request.getRequestURI();
            this.requestURL = request.getRequestURL();
            this.servletPath = request.getServletPath();
            this.characterEncoding = request.getCharacterEncoding();
            this.parameterMap = Collections.unmodifiableMap(request.getParameterMap());
            this.protocol = request.getProtocol();
            this.schema = request.getScheme();
            this.serverName = request.getServerName();
            this.serverPort = request.getServerPort();
            this.locale = request.getLocale();
            this.remoteAddr = request.getRemoteAddr();
            
            // Copy all the headers from the original request to the new request
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                List<String> newHeaderValues = new ArrayList<>();
                
                Enumeration<String> headerValues = request.getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    String headerValue = headerValues.nextElement();
                    newHeaderValues.add(headerValue);
                }
                headers.put(headerName, newHeaderValues);
            }

            // Copy all the attributes from the original request to the new request
            Enumeration<String> attributeNames = request.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                Object attributeValue = request.getAttribute(attributeName);
                attributes.put(attributeName, attributeValue);
            }
        }

        @Override
        public String getAuthType() {
            return null;
        }

        @Override
        public Cookie[] getCookies() {
            return null;
        }

        @Override
        public long getDateHeader(String name) {
            String value = getHeader(name);
            if (value == null) {
                return -1L;
            } else {
                try {
                    return Date.parse(value);
                } catch (IllegalArgumentException ex) {
                    return -1L;
                }
            }
        }

        @Override
        public String getHeader(String name) {
            if (headers.containsKey(name)) {
                return headers.get(name).get(0);
            } else if (headers.containsKey(name.toLowerCase())) {
                return headers.get(name.toLowerCase()).get(0);
            }
            return null;
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            List<String> values = headers.get(name);
            if (values == null) {
                return Collections.emptyEnumeration();
            } else {
                return Collections.enumeration(values);
            }
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            return Collections.enumeration(headers.keySet());
        }

        @Override
        public int getIntHeader(String name) {
            String value = getHeader(name);
            if (value == null) {
                return -1;
            } else {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException ex) {
                    return -1;
                }
            }
        }

        @Override
        public String getMethod() {
            return method;
        }

        @Override
        public String getPathInfo() {
            return pathInfo;
        }

        @Override
        public String getPathTranslated() {
            return null;
        }

        @Override
        public String getContextPath() {
            return contextPath;
        }

        @Override
        public String getQueryString() {
            return queryString;
        }

        @Override
        public String getRemoteUser() {
            return null;
        }

        @Override
        public boolean isUserInRole(String role) {
            return false;
        }

        @Override
        public Principal getUserPrincipal() {
            return null;
        }

        @Override
        public String getRequestedSessionId() {
            return requestedSessionId;
        }

        @Override
        public String getRequestURI() {
            return requestURI;
        }

        @Override
        public StringBuffer getRequestURL() {
            return requestURL;
        }

        @Override
        public String getServletPath() {
            return servletPath;
        }

        @Override
        public HttpSession getSession(boolean create) {
            return null;
        }

        @Override
        public HttpSession getSession() {
            return null;
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromUrl() {
            return false;
        }

        @Override
        public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
            return false;
        }

        @Override
        public void login(String username, String password) throws ServletException {
            
        }

        @Override
        public void logout() throws ServletException {
            
        }

        @Override
        public Collection<Part> getParts() throws IOException, ServletException {
            return null;
        }

        @Override
        public Part getPart(String name) throws IOException, ServletException {
            return null;
        }

        @Override
        public Object getAttribute(String name) {
            if (attributes.containsKey(name)) {
                return attributes.get(name);
            } else if (attributes.containsKey(name.toLowerCase())) {
                return attributes.get(name.toLowerCase());
            }
            return null;
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return Collections.enumeration(attributes.keySet());
        }

        @Override
        public String getCharacterEncoding() {
            return characterEncoding;
        }

        @Override
        public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
            
        }

        @Override
        public int getContentLength() {
            return 0;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return null;
        }

        @Override
        public String getParameter(String name) {
            String[] values = getParameterValues(name);
            if ((values == null) || (values.length < 1)) {
                return null;
            }
            return values[0];
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(parameterMap.keySet());
        }

        @Override
        public String[] getParameterValues(String name) {
            return parameterMap.get(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return parameterMap;
        }

        @Override
        public String getProtocol() {
            return protocol;
        }

        @Override
        public String getScheme() {
            return schema;
        }

        @Override
        public String getServerName() {
            return serverName;
        }

        @Override
        public int getServerPort() {
            return serverPort;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return null;
        }

        @Override
        public String getRemoteAddr() {
            return remoteAddr;
        }

        @Override
        public String getRemoteHost() {
            return null;
        }

        @Override
        public void setAttribute(String name, Object o) {
            
        }

        @Override
        public void removeAttribute(String name) {
            
        }

        @Override
        public Locale getLocale() {
            return locale;
        }

        @Override
        public Enumeration<Locale> getLocales() {
            return null;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            return null;
        }

        @Override
        public String getRealPath(String path) {
            return null;
        }

        @Override
        public int getRemotePort() {
            return 0;
        }

        @Override
        public String getLocalName() {
            return null;
        }

        @Override
        public String getLocalAddr() {
            return null;
        }

        @Override
        public int getLocalPort() {
            return 0;
        }

        @Override
        public ServletContext getServletContext() {
            return null;
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            return null;
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
            return null;
        }

        @Override
        public boolean isAsyncStarted() {
            return false;
        }

        @Override
        public boolean isAsyncSupported() {
            return false;
        }

        @Override
        public AsyncContext getAsyncContext() {
            return null;
        }

        @Override
        public DispatcherType getDispatcherType() {
            return null;
        }
    }
}
