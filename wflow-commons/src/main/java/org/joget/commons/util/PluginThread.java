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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
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
import javax.servlet.http.HttpUpgradeHandler;
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
        super(wrapWithCurrentUserIdentity(r));
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

    /**
     * GitLab #2261: Wraps {@code task} so that whichever thread eventually runs it inherits the
     * identity already established on THIS (the calling/submitting) thread at the moment of this
     * call -- captured now, restored immediately before {@code task.run()} and cleared immediately
     * after. Added so a plugin using {@link PluginThread} to spawn a background worker (e.g.
     * {@code process_enhancement}'s {@code AssignmentFormActions}) gets identity propagation as a
     * platform default, instead of needing its own {@code setCurrentThreadUser(..)} call
     * individually whitelisted by fully-qualified class name -- a whack-a-mole approach that also
     * required the genuine plugin's jar to live in the shared {@code wflow/app_plugins/} directory
     * (not a per-tenant {@code app_profiles/<profile>/app_plugins/} one) to survive {@code
     * MultiTenantPluginManager.verifyTrustedClasses()}'s anti-shadowing check, silently uninstalling
     * a non-admin tenant's own legitimate Marketplace install of the same plugin.
     *
     * <p>Also captures/restores tenant profile unconditionally (not gated on an identity being
     * present): this line's own {@code PluginThread} has no reusable-executor helper today, so this
     * is purely for consistency with the 9.1 line's own version of this method (which needs it to
     * correctly support a long-lived, reused worker thread) -- harmless here alongside the one-shot
     * constructor's own existing profile capture, since {@link HostManager#setCurrentProfile(String)}
     * /{@link HostManager#resetProfile()} push/pop a stack, so nesting the same value twice composes
     * safely.
     */
    public static Runnable wrapWithCurrentUserIdentity(final Runnable task) {
        final String capturedUsername = captureCurrentUsername();
        final String capturedProfile = DynamicDataSourceManager.getCurrentProfile();
        return new Runnable() {
            @Override
            public void run() {
                runWithIdentityPropagation(capturedUsername, capturedProfile, task);
            }
        };
    }

    private static String captureCurrentUsername() {
        try {
            ThreadUserIdentitySource provider = resolveIdentitySource();
            return provider == null ? null : provider.captureUsernameForPluginThreadPropagation();
        } catch (Exception ex) {
            return null;
        }
    }

    private static void runWithIdentityPropagation(String username, String profile, Runnable task) {
        ThreadUserIdentitySource provider = resolveIdentitySource();
        boolean restoredIdentity = false;
        if (username != null && provider != null) {
            try {
                // Deliberately a single-statement call into a DEDICATED, private method (see
                // restoreCapturedIdentity's own comment) -- this exact call is one of a small, fixed
                // set of platform-code call sites MultiTenantSecurityManager.WHITELIST_RESTRICTED_
                // CLASSES exempts from the setCurrentThreadUser guard, on the SAME
                // propagate-already-established-identity reasoning as every other entry there.
                restoreCapturedIdentity(provider, username);
                restoredIdentity = true;
            } catch (Exception ex) {
                LogUtil.warn(PluginThread.class.getName(), "Failed to restore thread user identity: " + ex);
            }
        }
        // See this method's caller (wrapWithCurrentUserIdentity) for why profile is restored
        // unconditionally, unlike identity. Deliberately a single-statement call into a DEDICATED
        // private method (see restoreCapturedProfile's own comment) -- NOT called inline here --
        // for the SAME reason restoreCapturedIdentity above is its own method: this method's OWN
        // frame remains on the stack for the ENTIRE DURATION of task.run() below (it is the direct
        // caller), so if the profile mutation happened directly in THIS method's body, a malicious
        // wrapped task could call HostManager.setCurrentProfile(otherTenant) itself and inherit
        // this frame's own trust by simply being a descendant of it -- an independent review found
        // exactly this gap in an earlier version of this fix. restoreCapturedProfile/
        // clearCapturedProfile's own frames, by contrast, each return before task.run() begins (or
        // only start after it ends), so they are never an ancestor of the wrapped task's own code.
        restoreCapturedProfile(profile);
        try {
            task.run();
        } finally {
            clearCapturedProfile();
            if (restoredIdentity) {
                try {
                    clearCapturedIdentity(provider);
                } catch (Exception ex) {
                    LogUtil.warn(PluginThread.class.getName(), "Failed to clear thread user identity: " + ex);
                }
            }
        }
    }

    /**
     * Backs {@link #runWithIdentityPropagation(String, Runnable)}'s own comment. Kept as its own
     * tiny, single-statement, private method (never called anywhere else in this class, and never
     * reachable from outside it without also defeating this project's separate, already-existing
     * reflection guards) specifically so its presence on the stack -- checked by
     * MultiTenantSecurityManager.checkAgainstWhitelist(..) via a whitelist entry naming this exact
     * method -- can only mean this class's own automatic identity propagation is genuinely in
     * progress. Always returns BEFORE task.run() (see the caller above) -- its own frame is never an
     * ancestor of the wrapped task's later execution, so a plugin's own, separate, direct call to
     * setCurrentThreadUser from within that task never inherits this trust by association.
     */
    private static void restoreCapturedIdentity(ThreadUserIdentitySource provider, String username) {
        provider.setCurrentThreadUser(username);
    }

    /**
     * @see #restoreCapturedIdentity(ThreadUserIdentitySource, String)
     */
    private static void clearCapturedIdentity(ThreadUserIdentitySource provider) {
        provider.clearCurrentThreadUser();
    }

    /**
     * Backs {@link #runWithIdentityPropagation(String, String, Runnable)}'s own comment on why
     * profile mutation must not happen inline in that method's body. Kept as its own tiny,
     * single-statement, private method (never called anywhere else in this class, and never
     * reachable from outside it without also defeating this project's separate, already-existing
     * reflection guards) specifically so its presence on the stack -- checked by
     * MultiTenantSecurityManager.checkAgainstWhitelist(..) via a whitelist entry naming this exact
     * method -- can only mean this class's own automatic profile propagation is genuinely in
     * progress. Always returns BEFORE task.run() (see the caller above) -- its own frame is never an
     * ancestor of the wrapped task's later execution, so a plugin's own, separate, direct call to
     * HostManager.setCurrentProfile from within that task never inherits this trust by association.
     */
    private static void restoreCapturedProfile(String profile) {
        HostManager.setCurrentProfile(profile);
    }

    /**
     * @see #restoreCapturedProfile(String)
     */
    private static void clearCapturedProfile() {
        HostManager.resetProfile();
    }

    // GitLab #2261 (07 Sep 2026, independent review): resolving the identity source via a live
    // SecurityUtil.getApplicationContext().getBean("workflowUserManager") lookup, even with that
    // context reference itself guarded against replacement, was found still exploitable -- a tenant
    // bundle can reach the SAME genuine ApplicationContext's own bean factory through entirely
    // PUBLIC Spring APIs (no reflection/setAccessible needed: ConfigurableApplicationContext
    // .getBeanFactory(), then ConfigurableListableBeanFactory.registerSingleton(..)), destroy the
    // real "workflowUserManager" singleton, and register an attacker-defined replacement -- which
    // this class's own NEXT lookup would then trust completely. Fixed by never querying the mutable
    // Spring context for this at all: the genuine WorkflowUserManager bean instance PINS itself here
    // exactly once, in its own constructor (see that class's own comment), strictly before any OSGi/
    // tenant plugin code can possibly execute (Felix containers are only created after the core
    // webapp Spring context finishes initializing) -- so no later bean-registry tampering can ever
    // reach this reference. Write-once (pinIdentitySourceIfAbsent below): even if some other object
    // later also implements ThreadUserIdentitySource and is (somehow) constructed, it cannot displace
    // the already-pinned genuine instance.
    private static volatile ThreadUserIdentitySource pinnedIdentitySource;

    /**
     * Called ONCE by the genuine {@code WorkflowUserManager} bean's own constructor -- see this
     * field's own comment above for why that timing is safe, and {@link #resolveIdentitySource()}
     * for how the pinned value is used.
     */
    public static void pinIdentitySourceIfAbsent(ThreadUserIdentitySource source) {
        if (pinnedIdentitySource == null && source != null) {
            synchronized (PluginThread.class) {
                if (pinnedIdentitySource == null) {
                    pinnedIdentitySource = source;
                }
            }
        }
    }

    // Test-only seam, matching MultiTenantSecurityManager.TEST_TRUSTED_PLUGIN_BUNDLE_LOCATOR_OVERRIDE
    // 's own established pattern in this codebase: consulted only as a FALLBACK after the real
    // pinned instance, never touched by production code -- lets a bare-JUnit test (no real Spring-
    // wired WorkflowUserManager bean to trigger pinning) supply a fake without weakening the
    // production write-once guarantee above.
    static volatile ThreadUserIdentitySource TEST_IDENTITY_SOURCE_OVERRIDE = null;

    private static ThreadUserIdentitySource resolveIdentitySource() {
        if (pinnedIdentitySource != null) {
            return pinnedIdentitySource;
        }
        return TEST_IDENTITY_SOURCE_OVERRIDE;
    }
    
    @Override
    public void run() {
        // super.run() executes arbitrary plugin code that can throw. Without try/finally, an
        // exception leaves HostManager's previousProfileStack ThreadLocal leaked, recoverable via a
        // bare HostManager.resetProfile() call (unguarded, no SecurityManager check applies to
        // it) -- matches the same setProfile()/no-cleanup idiom this class is the origin of,
        // copied into ReportTask/DeadlineChecker.
        try {
            setProfile();

            if (request != null) {
                RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
            }

            super.run();
        } finally {
            if (request != null) {
                RequestContextHolder.resetRequestAttributes();
                request = null;
            }
            HostManager.resetProfile();
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
            return this.localPort;
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
    }
}
