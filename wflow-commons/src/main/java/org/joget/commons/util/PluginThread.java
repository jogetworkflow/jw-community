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
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sun.misc.Unsafe;

/**
 * Thread implementation to used by plugin
 */
public final class PluginThread extends Thread {
      
    // captured once at registration time: this.getName() is mutable (callers such as
    // getAsyncExecutorService() rename the thread after construction), so removal must
    // use the same key that was registered, not a fresh call to getName()
    private final String activeThreadKey;
    private final String profile;
    private final Locale currentLocale;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletRequestContext wildflyServletRequestContext; // for jboss eap and wildfly
    private Object websphereRequest; // for websphere liberty IRequest https://github.com/OpenLiberty/open-liberty/blob/gm-24.0.0.10/dev/com.ibm.ws.webcontainer/src/com/ibm/websphere/servlet/request/IRequest.java
    private Object websphereRequestHelper; // for websphere liberty SRTServletRequestHelper https://github.com/OpenLiberty/open-liberty/blob/gm-24.0.0.10/dev/com.ibm.ws.webcontainer/src/com/ibm/ws/webcontainer/srt/SRTServletRequest.java
    private Object tomcatConnector; // for tomcat
    
    // internal thread fields for Java 21+, need to clear holder.task field to prevent memory leak 
    // https://github.com/openjdk/jdk21/blob/master/src/java.base/share/classes/java/lang/Thread.java#L256
    private static Unsafe unsafe;
    private static Field threadHolderField;
    private static Field threadTaskField;
    
    //clean the threads (wait 1 mins to complete) when server shutdown
    private final static ConcurrentHashMap<String, Thread> ACTIVE_THREADS = new ConcurrentHashMap<>();
    private static final Runnable cleaning = new Runnable() {
        @Override
        public void run() {
            for (Thread thread : ACTIVE_THREADS.values()) {
                try {
                    LogUtil.info(getClass().getName(), "Waiting thread " + thread + " for max 1 min to complete.");
                    if (!thread.isAlive()) {
                        thread.start(); //start it if it is not running
                    }
                    thread.join(60 * 1000); // Wait up to 1 minute for thread to run
                } catch (Exception e) {
                    LogUtil.debug(getClass().getName(), "Fail to clean thread " + thread);
                }
            }
            ACTIVE_THREADS.clear();
        }
    };
    
    static {
        try {
            // get internal thread holder and task fields using reflection
            threadHolderField = Thread.class.getDeclaredField("holder");
            threadHolderField.setAccessible(true);
            Object holder = threadHolderField.get(new Thread());
            threadTaskField = holder.getClass().getDeclaredField("task");                
            threadTaskField.setAccessible(true);

            // get low-level Unsafe object that is required to set the value for holder.task which is declared final
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            unsafe = (Unsafe) unsafeField.get(null);
            
            ServerUtil.addServerShutdownCleaningTask("cleanPluginThread", cleaning);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex) {
            // ignore if fields do not exist
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Default timeout for async request in milliseconds, 0 to disable.
     */
    public static long DEFAULT_ASYNC_REQUEST_TIMEOUT = 0;
    
    /**
     * Timeout for async request in milliseconds, 0 to disable
     * e.g. -Dwflow.asyncRequestTimeout=3000
     */
    public static String SYSTEM_PROPERTY_ASYNC_REQUEST_TIMEOUT = "wflow.asyncRequestTimeout";
    
    public PluginThread(Runnable r) {
        super(wrapWithCurrentUserIdentity(r));
        activeThreadKey = this.getName();
        ACTIVE_THREADS.put(activeThreadKey, this);
        currentLocale = LocaleContextHolder.getLocale();
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

                    // obtain internal _srtRequestHelper for later reuse (https://github.com/OpenLiberty/open-liberty/blob/gm-24.0.0.10/dev/com.ibm.ws.webcontainer/src/com/ibm/ws/webcontainer/srt/SRTServletRequest.java#L162)
                    Field helperField = requestClass.getDeclaredField("_srtRequestHelper");
                    helperField.setAccessible(true);
                    websphereRequestHelper = helperField.get(wrappedRequest);
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
        try {
            setProfile();
            // set locale for the current thread
            LocaleContextHolder.setLocale(currentLocale);
            if (request != null) {
                RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

                ServletRequest wrappedRequest = getWrappedRequest(request);
                if (websphereRequest != null && wrappedRequest.getClass().getName().contains("SRTServletRequest")) {
                    // for websphere liberty
                    try {
                        // initialize request using initForNextRequest (https://github.com/OpenLiberty/open-liberty/blob/gm-24.0.0.10/dev/com.ibm.ws.webcontainer/src/com/ibm/ws/webcontainer/srt/SRTServletRequest.java#L320)
                        MethodUtils.invokeMethod(wrappedRequest, true, "initForNextRequest", new Object[] { websphereRequest });

                        // get reference to com.ibm.ws.webcontainer.srt.SRTServletRequest and set current _srtRequestHelper
                        Class requestClass = wrappedRequest.getClass().getSuperclass().getSuperclass().getSuperclass();
                        Field helperField = requestClass.getDeclaredField("_srtRequestHelper");
                        helperField.setAccessible(true);
                        helperField.set(wrappedRequest, websphereRequestHelper);
                    } catch (Exception ex) {
                        LogUtil.warn(getClass().getName(), ex.toString());
                    }
                }
            }
            if (wildflyServletRequestContext != null) {
                // for jboss eap and wildfly
                ServletRequestContext.setCurrentRequestContext(wildflyServletRequestContext);
            }
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
            
            // clear attributes
            response = null;
            wildflyServletRequestContext = null;
            websphereRequest = null;
            websphereRequestHelper = null;
            tomcatConnector = null;        

            try {
                if (threadHolderField != null && threadTaskField != null && unsafe != null) {
                    // Java 21+, clear holder.task field to avoid memory leak
                    Object holder = threadHolderField.get(this);
                    if (holder != null) {
                        unsafe.putObject(holder, unsafe.objectFieldOffset(threadTaskField), null);
                    }
                }
            } catch (IllegalAccessException ex) {
                LogUtil.warn(getClass().getName(), ex.toString());
            }    
            
            ACTIVE_THREADS.remove(activeThreadKey);

            // super.run() executes arbitrary plugin code that can throw. Without this, an
            // exception (or the ordinary non-exceptional path) leaves HostManager's
            // previousProfileStack ThreadLocal leaked, recoverable via a bare HostManager.resetProfile()
            // call (unguarded, no SecurityManager check applies to it) -- matches the same
            // setProfile()/no-cleanup idiom this class is the origin of, copied into
            // ReportTask/DeadlineChecker.
            HostManager.resetProfile();
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
     *
     * <p>GitLab #2261: unlike a one-shot {@code new PluginThread(runnable); thread.start();}, this
     * executor's single underlying worker thread is long-lived and reused across every task ever
     * submitted to it (this project's own {@code PresenceManager} holds one for its whole lifetime).
     * Capturing identity once, at thread-construction time, would be wrong here: whichever caller
     * happens to be first to trigger this executor's creation would have every LATER task -- possibly
     * submitted by a completely different user -- run with that first caller's identity. Each
     * submitted task must capture/restore/clear its OWN identity independently, so the returned
     * {@code ExecutorService} is wrapped to apply {@link #wrapWithCurrentUserIdentity(Runnable)} to
     * every task at the moment it is actually submitted, not once when the executor itself is built.
     * @return
     */
    public static ExecutorService getAsyncExecutorService() {
        ExecutorService asyncExecutorService = Executors.newSingleThreadExecutor((Runnable r) -> {
            Thread t = new PluginThread(r);
            t.setName("AsyncExecutor-" + t.getName());
            t.setDaemon(false);
            return t;
        });
        return new IdentityPropagatingExecutorService(asyncExecutorService);
    }

    /**
     * Wraps {@code task} so that whichever thread eventually runs it inherits the identity AND
     * tenant profile already established on THIS (the calling/submitting) thread at the moment of
     * this call -- both captured now, restored immediately before {@code task.run()} and cleared
     * immediately after, regardless of whether the executing thread is a fresh one-shot {@link
     * PluginThread} or a long-lived, reused worker.
     *
     * <p>Profile is captured/restored here UNCONDITIONALLY (not gated on an identity being present)
     * specifically for the reused-worker case: {@link #getAsyncExecutorService()}'s single
     * underlying worker thread is itself a {@code PluginThread}, whose OWN constructor captures
     * {@code profile} exactly ONCE, at construction time, and restores that SAME captured value for
     * the worker's ENTIRE lifetime (see this class's own constructor/{@code run()}) -- correct for a
     * one-shot {@code new PluginThread(runnable); thread.start();}, but wrong for a reused executor:
     * whichever tenant happens to be first to trigger the executor's creation would have every
     * LATER task, submitted by a completely different tenant, run under the FIRST tenant's profile.
     * Capturing/restoring profile again HERE, per task, corrects that for the reused-executor case;
     * for the one-shot case it is redundant with (but harmless alongside) the outer PluginThread's
     * own construction-time capture, since {@link HostManager#setCurrentProfile(String)}/{@link
     * HostManager#resetProfile()} push/pop a stack, so nesting the same value twice composes safely.
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

    /**
     * @see #restoreCapturedIdentity(ThreadUserIdentitySource, String)
     */
    private static void clearCapturedIdentity(ThreadUserIdentitySource provider) {
        provider.clearCurrentThreadUser();
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

    /**
     * Wraps a real {@code ExecutorService} so every task submitted through ANY of its methods
     * (all of which {@code AbstractExecutorService} implements in terms of {@link #execute(Runnable)}
     * below) gets {@link #wrapWithCurrentUserIdentity(Runnable)} applied at the moment it is
     * submitted -- see {@link #getAsyncExecutorService()}'s own comment for why per-task wrapping,
     * not per-worker-thread-construction wrapping, is required here.
     */
    private static final class IdentityPropagatingExecutorService extends java.util.concurrent.AbstractExecutorService {
        private final ExecutorService delegate;

        IdentityPropagatingExecutorService(ExecutorService delegate) {
            this.delegate = delegate;
        }

        @Override
        public void execute(Runnable command) {
            delegate.execute(wrapWithCurrentUserIdentity(command));
        }

        @Override
        public void shutdown() {
            delegate.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return delegate.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return delegate.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }
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
