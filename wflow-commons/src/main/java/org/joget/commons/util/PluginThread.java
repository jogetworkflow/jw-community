package org.joget.commons.util;

import io.undertow.servlet.handlers.ServletRequestContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConnection;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.Part;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Thread implementation to used by plugin
 */
public final class PluginThread extends Thread {
    
    private final String profile;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletRequestContext wildflyServletRequestContext; // for jboss eap and wildfly
    private Object websphereRequest; // for websphere liberty IRequest https://github.com/OpenLiberty/open-liberty/blob/gm-24.0.0.10/dev/com.ibm.ws.webcontainer/src/com/ibm/websphere/servlet/request/IRequest.java
    private Object tomcatConnector; // for tomcat
    
    /**
     * Default timeout for async request in milliseconds, 0 to disable.
     */
    public static long DEFAULT_ASYNC_REQUEST_TIMEOUT = 0;
    
    /**
     * Timeout for async request in milliseconds, 0 to disable
     * e.g. -Dwflow.asyncRequestTimeout=3000
     * Need to also set Tomcat configuration property 
     * -Dorg.apache.catalina.connector.RECYCLE_FACADES=false
     * to prevent exception "The request object has been recycled and is no longer associated with this facade".
     */
    public static String SYSTEM_PROPERTY_ASYNC_REQUEST_TIMEOUT = "wflow.asyncRequestTimeout";
    
    public PluginThread(Runnable r) {
        super(r);
        profile = DynamicDataSourceManager.getCurrentProfile();
        ServletRequestAttributes sra = null;
        try {
            sra = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes());
        } catch (IllegalStateException e) {
        }
        if (sra != null) {
            HttpServletRequest origRequest = sra.getRequest();
            String servletContextClassName = origRequest.getServletContext().getClass().getName();

            // The Servlet specification requires applications to only wrap the request/response using wrapper classes 
            // that extend from the ServletRequestWrapper and ServletResponseWrapper classes.
            // This is enforced by some app servers like JBoss EAP.
            if (servletContextClassName.contains("catalina")) {
                // for tomcat
                try {
                    // get internal tomcat connector from the request
                    ServletRequest wrappedRequest = getWrappedRequest(origRequest);
                    Field field = wrappedRequest.getClass().getDeclaredField("request");
                    field.setAccessible(true);
                    Object tomcatRequest = field.get(wrappedRequest);
                    Field connectorField = tomcatRequest.getClass().getDeclaredField("connector");
                    connectorField.setAccessible(true);
                    
                    // Set the disableFacades flag to false to prevent
                    // "java.lang.IllegalStateException: The request object has been recycled and is no longer associated with this facade"
                    // in tomcat 10.1 and above
                    tomcatConnector = connectorField.get(tomcatRequest);
                    MethodUtils.invokeMethod(tomcatConnector, true, "setDiscardFacades", false);
                } catch (NoSuchFieldException ex) {
                    // ignore
                } catch (Exception ex) {
                    LogUtil.warn(getClass().getName(), ex.toString());
                }
                
                request = new HttpServletRequestWrapper(new PluginThreadHttpRequest(origRequest));
            } else if (servletContextClassName.contains("ibm.ws.webcontainer")) {
                // for websphere liberty
                try {
                    ServletRequest wrappedRequest = getWrappedRequest(origRequest);
                    
                    // clone request using clone() method (https://github.com/OpenLiberty/open-liberty/blob/gm-24.0.0.10/dev/com.ibm.ws.webcontainer/src/com/ibm/ws/webcontainer/srt/SRTServletRequest.java#L1512)
                    // requires commons-lang3 upgrade to 3.15.0 for bug in version 3.12.0 https://issues.apache.org/jira/browse/LANG-1694
                    HttpServletRequest clonedRequest = (HttpServletRequest)MethodUtils.invokeMethod(wrappedRequest, true, "clone");
                    request = new HttpServletRequestWrapper(clonedRequest);

                    // get reference to class com.ibm.ws.webcontainer.srt.SRTServletRequest
                    Class requestClass = wrappedRequest.getClass().getSuperclass().getSuperclass().getSuperclass();
                    
                    // obtain internal _request for later initialization (https://github.com/OpenLiberty/open-liberty/blob/gm-24.0.0.10/dev/com.ibm.ws.webcontainer/src/com/ibm/ws/webcontainer/srt/SRTServletRequest.java#L180)
                    Field field = requestClass.getDeclaredField("_request");
                    field.setAccessible(true);
                    websphereRequest = field.get(wrappedRequest);
                } catch (Exception ex) {
                    LogUtil.warn(getClass().getName(), ex.toString());
                }
                
            } else {
                // for other application servers
                request = new HttpServletRequestWrapper(origRequest);
                if (servletContextClassName.contains("undertow")) {
                    // required for jboss eap and wildfly
                    wildflyServletRequestContext = ServletRequestContext.current();
                }
            }
            response = sra.getResponse();
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
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
            
            ServletRequest wrappedRequest = getWrappedRequest(request);
            if (websphereRequest != null && wrappedRequest.getClass().getName().contains("SRTServletRequest")) {
                // for websphere liberty, initialize request using initForNextRequest (https://github.com/OpenLiberty/open-liberty/blob/gm-24.0.0.7/dev/com.ibm.ws.webcontainer/src/com/ibm/ws/webcontainer/srt/SRTServletRequest.java#L320)
                try {
                    MethodUtils.invokeMethod(wrappedRequest, true, "initForNextRequest", new Object[] { websphereRequest });
                } catch (Exception ex) {
                    LogUtil.warn(getClass().getName(), ex.toString());
                }
            }
        }
        if (wildflyServletRequestContext != null) {
            // for jboss eap and wildfly
            ServletRequestContext.setCurrentRequestContext(wildflyServletRequestContext);
        }
        try {
            super.run();
        } finally {
            if (request != null) {
                RequestContextHolder.resetRequestAttributes();
                request = null;
            }
            if (wildflyServletRequestContext != null) {
                // clear wildfly servlet attachments
                ServletRequestContext.clearCurrentServletAttachments();
            }
        }        
    }

    /**
     * Return the original request within servlet request wrappers.
     * @param req
     * @return 
     */
    protected ServletRequest getWrappedRequest(ServletRequest req) {
        while (req instanceof ServletRequestWrapper) {
            req = ((ServletRequestWrapper)req).getRequest();
        }
        return req;
    }
    
    /**
     * Returns an asynchronous executor service to run in a background thread.
     * @return 
     */
    public static ExecutorService getAsyncExecutorService() {
        ExecutorService asyncExecutorService = Executors.newSingleThreadExecutor((Runnable r) -> {
            Thread t = new PluginThread(r);
            t.setDaemon(false);
            return t;
        });
        return asyncExecutorService;
    }
    
    /**
     * Returns the configured timeout for asynchronous request calls based on the system property wflow.asyncRequestTimeout.
     * Zero disables the timeout and makes the call synchronous.
     * @return 
     */
    public static long getAsyncRequestTimeout() {
        // check for supported app servers
        HttpServletRequest request = null;
        try {
            request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch(IllegalStateException e) {
            // ignore if servlet request is not available, e.g. when triggered from a deadline            
        }
        if (request != null) {
            String servletContextClassName = request.getServletContext().getClass().getName();
            if (!servletContextClassName.contains("catalina") // tomcat
                    && !servletContextClassName.contains("undertow") // jboss eap and wildfly
                    && !servletContextClassName.contains("ibm.ws.webcontainer")) { // websphere liberty
                // unsupported app server, disable async
                return 0L; 
            }
        } else {
            // request not available, disable async
            return 0L;
        }
        
        // get timeout setting from system property, default to DEFAULT_ASYNC_REQUEST_TIMEOUT.
        // TODO: can be enhanced to read from the System Settings
        long timeout = DEFAULT_ASYNC_REQUEST_TIMEOUT;
        String timeoutStr = System.getProperty(SYSTEM_PROPERTY_ASYNC_REQUEST_TIMEOUT);
        if (timeoutStr != null) {
            try {
                timeout = Long.parseLong(timeoutStr);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return timeout;
    }
    
    /**
     * A dummy request to copy the current HTTP request data to use by request hash variable in plugin thread.
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
        private String localAddr;
        private String localName;
        private int localPort;
        private HttpSession session;
        private ServletContext servletContext;
        private DispatcherType dispatcherType;

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
            this.parameterMap = new TreeMap<>(request.getParameterMap());
            this.protocol = request.getProtocol();
            this.schema = request.getScheme();
            this.serverName = request.getServerName();
            this.serverPort = request.getServerPort();
            this.localAddr = request.getLocalAddr();
            this.localName = request.getLocalName();
            this.localPort = request.getLocalPort();
            this.locale = request.getLocale();
            this.remoteAddr = request.getRemoteAddr();
            this.servletContext = request.getServletContext();
            this.dispatcherType = request.getDispatcherType();
            this.session = request.getSession();
            
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
                
                //skip cache attribute for process, no way to clear it after plugin thread created
                if (attributeName.startsWith("RequestCacheKey_processId_") || attributeName.startsWith("RequestCacheKey_activityId_")) {
                    continue;
                }
                
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
            return session;
        }

        @Override
        public HttpSession getSession() {
            return session;
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
            return Collections.enumeration(new LinkedHashSet<>(attributes.keySet()));
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
            return new TreeMap<>(parameterMap);
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
            attributes.put(name, o);
        }

        @Override
        public void removeAttribute(String name) {
            attributes.remove(name);
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
            return request.getRequestDispatcher(path);
        }

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
            return this.servletContext;
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
            return dispatcherType;
        }

        @Override
        public String changeSessionId() {
            return request.changeSessionId();
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> type) throws IOException, ServletException {
            return null;
        }

        @Override
        public long getContentLengthLong() {
            return (long)getContentLength();
        }

        @Override
        public String getRequestId() {
            return request.getRequestId();
        }

        @Override
        public String getProtocolRequestId() {
            return request.getProtocolRequestId();
        }

        @Override
        public ServletConnection getServletConnection() {
            return request.getServletConnection();
        }
    }
}
