package org.joget.apps.workflow.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Properties;
import javax.security.auth.Subject;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.HostThreadLocal;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.joget.workflow.model.dao.WorkflowHelper;
import org.joget.workflow.model.service.WorkflowUserManager;
import static org.joget.workflow.model.service.WorkflowUserManager.ROLE_ADMIN;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AuthenticationTokenWrapper implements Authentication {

    public static final String HOST_SESSION_SIGNING_KEY_PROPERTY = "hostSessionSigningKey";
    public static final String SESSION_FILE = "app_session.properties";
    private static final String HOST_SESSION_SIGNING_KEY_ALGORITHM = "HmacSHA256";
    private static final String HOST_SESSION_BINDING_VERSION = "v1";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final StackWalker SIGNED_SESSION_STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    // SHOW_REFLECT_FRAMES is required so that reflection-based attacks (Method.invoke) are visible
    // in the stack and correctly treated as a boundary after ATW internal frames.
    private static final StackWalker INTERNAL_CALLER_STACK_WALKER = StackWalker.getInstance(
            java.util.EnumSet.of(StackWalker.Option.RETAIN_CLASS_REFERENCE, StackWalker.Option.SHOW_REFLECT_FRAMES));
    private static volatile byte[] hostSessionSigningKey;
    private static final ThreadLocal<Boolean> hostSessionSigningKeyAccess = new ThreadLocal<>();

    Authentication authentication;
    private final String loginHost;
    private final String loginHostSignature;

    // Set by WorkflowHttpAuthProcessingFilter after initHost() to preserve the hostname
    // before enterprise directory's setCurrentProfile() clears getCurrentHost().
    // Package-private: BeanShell (different package) cannot call this directly.
    // HostThreadLocal guards set()/remove() with HostPermission, blocking BeanShell/OSGI
    // even if they obtain a reference to the ThreadLocal via reflection.
    private static final ThreadLocal<String> loginHostHolder = new HostThreadLocal<>();

    static void bindLoginHost(String host) {
        if (!isBlank(host)) loginHostHolder.set(host);
        else loginHostHolder.remove();
    }

    static void clearLoginHost() {
        loginHostHolder.remove();
    }

    /**
     * Binds the current request's server name into the ThreadLocal if not already set.
     * Called by JogetSecurityContextImpl before wrapping a plain token (SSO / programmatic login),
     * so that the resulting AuthenticationTokenWrapper captures the correct tenant hostname
     * instead of falling back to the enterprise directory's internal profile name.
     * Safe to expose publicly: it reads from the actual HttpServletRequest, accepts no argument,
     * and is a no-op when the ThreadLocal is already populated.
     */
    public static void bindLoginHostFromCurrentRequest() {
        if (!isBlank(loginHostHolder.get())) {
            return;
        }
        try {
            HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String host = req.getServerName();
            if (!isBlank(host)) {
                loginHostHolder.set(host);
            }
        } catch (SecurityException se) {
            throw se; // propagate - BeanShell/OSGI attempting to spoof via RequestContextHolder
        } catch (Exception e) {
            // no request context available - token will fall back to currentTenantKey()
        }
    }

    public AuthenticationTokenWrapper(Authentication auth) {
        this.authentication = auth;
        if (auth instanceof AuthenticationTokenWrapper) {
            AuthenticationTokenWrapper wrapper = (AuthenticationTokenWrapper) auth;
            String name = getAuthenticationName(auth);
            if (isValidSignedBinding(wrapper.getLoginHost(), wrapper.loginHostSignature, name)) {
                this.loginHost = wrapper.getLoginHost();
                this.loginHostSignature = wrapper.loginHostSignature;
                return;
            }
            // Invalid wrapper: treat like a non-wrapper — re-signing requires a trusted caller.
        }
        requireTrustedSignedSessionCreationCaller();
        String name = getAuthenticationName(auth);
        this.loginHost = resolveLoginHost();
        this.loginHostSignature = signLoginHost(this.loginHost, name);
    }

    private AuthenticationTokenWrapper(Authentication auth, String loginHost, String loginHostSignature) {
        requireSignedSessionInternalCaller("restore signed host session binding");
        this.authentication = auth;
        this.loginHost = loginHost;
        this.loginHostSignature = loginHostSignature;
    }

    public final String getLoginHost() {
        return loginHost;
    }

    /**
     * Validates that the tenant host bound to the session token matches the
     * current request host. Must be called after Spring Security has restored the
     * SecurityContext and before tenant plugin code runs.
     * Returns false (and sends 403) on mismatch so the caller can exit early.
     */
    public static boolean validateHost(HttpServletRequest request, HttpServletResponse response) {
        if (!HostManager.isVirtualHostEnabled()) {
            return true;
        }
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication auth = (ctx != null) ? ctx.getAuthentication() : null;
        return validateHost(request, response, auth);
    }

    /**
     * Validates an authentication loaded from a trusted server-side source, e.g.
     * the session before Spring Security has restored SecurityContextHolder.
     */
    public static boolean validateHost(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        if (!HostManager.isVirtualHostEnabled()) {
            return true;
        }
        if (request == null || response == null || auth == null || auth instanceof AnonymousAuthenticationToken) {
            return true;
        }
        try {
            boolean hostMismatch = true;
            boolean signatureMismatch = false;
            String loginHost = null;
            if (auth instanceof AuthenticationTokenWrapper) {
                AuthenticationTokenWrapper wrapper = (AuthenticationTokenWrapper) auth;
                loginHost = wrapper.getLoginHost();
                String currentKey = currentTenantKey();
                signatureMismatch = !wrapper.hasValidSignedBinding();
                hostMismatch = (isBlank(loginHost) || !loginHost.equals(currentKey));
            }
            if (signatureMismatch) {
                return reject(request, response, auth, loginHost, "Host session signature mismatch");
            }
            if (hostMismatch) {
                return reject(request, response, auth, loginHost, "Host header mismatch");
            }
        } catch (Exception e) {
            return reject(request, response, auth, null, "Host validation error");
        }
        return true;
    }

    /**
     * Returns a non-null, plugin-safe tenant key for the current thread.
     * Prefers the tenant profile (enterprise routing sets this) and falls back to the
     * current host (community path set by HostManager.initHost). Both are protected by
     * MultiTenantSecurityManager's HostPermission - plugins cannot spoof either value.
     * RequestContextHolder is intentionally NOT used here because setRequestAttributes()
     * is unblocked and a malicious plugin could supply a fake hostname.
     */
    private static String currentTenantKey() {
        String profile = HostManager.getCurrentProfile();
        if (!isBlank(profile)) {
            return profile;
        }
        return HostManager.getCurrentHost();
    }

    private static String resolveLoginHost() {
        // Prefer the hostname saved by WorkflowHttpAuthProcessingFilter before enterprise
        // directory's setCurrentProfile() clears getCurrentHost(). Falls back to
        // currentTenantKey() for unit tests where the ThreadLocal is not populated.
        String host = loginHostHolder.get();
        if (!isBlank(host)) return host;
        return currentTenantKey();
    }

    private boolean hasValidSignedBinding() {
        return isValidSignedBinding(loginHost, loginHostSignature, authentication);
    }

    private static boolean isValidSignedBinding(String loginHost, String signature, Authentication auth) {
        if (auth == null) {
            return false;
        }
        return isValidSignedBinding(loginHost, signature, getAuthenticationName(auth));
    }

    private static boolean isValidSignedBinding(String loginHost, String signature, String name) {
        if (isBlank(loginHost) || isBlank(signature)) {
            return false;
        }
        String expected = signLoginHost(loginHost, name);
        if (isBlank(expected)) {
            return false;
        }
        byte[] supplied = signature.getBytes(StandardCharsets.UTF_8);
        byte[] calculated = expected.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(supplied, calculated);
    }

    private static String signLoginHost(String loginHost, Authentication auth) {
        requireSignedSessionInternalCaller("sign host session binding");
        if (auth == null) {
            return null;
        }
        return signLoginHost(loginHost, getAuthenticationName(auth));
    }

    private static String signLoginHost(String loginHost, String name) {
        requireSignedSessionInternalCaller("sign host session binding");
        if (isBlank(loginHost)) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance(HOST_SESSION_SIGNING_KEY_ALGORITHM);
            mac.init(new SecretKeySpec(getHostSessionSigningKey(), HOST_SESSION_SIGNING_KEY_ALGORITHM));
            mac.update(HOST_SESSION_BINDING_VERSION.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) 0);
            mac.update(loginHost.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) 0);
            if (name != null) {
                mac.update(name.getBytes(StandardCharsets.UTF_8));
            }
            return Base64.getEncoder().encodeToString(mac.doFinal());
        } catch (Exception e) {
            LogUtil.error(AuthenticationTokenWrapper.class.getName(), e, "Unable to sign host session binding");
            return null;
        }
    }

    private static String getAuthenticationName(Authentication auth) {
        return (auth != null) ? auth.getName() : null;
    }

    private static byte[] getHostSessionSigningKey() {
        requireSignedSessionInternalCaller("read host session signing key");
        byte[] key = hostSessionSigningKey;
        if (key == null) {
            synchronized (AuthenticationTokenWrapper.class) {
                key = hostSessionSigningKey;
                if (key == null) {
                    key = loadOrCreateHostSessionSigningKey();
                    hostSessionSigningKey = key;
                }
            }
        }
        return key;
    }

    private static byte[] loadOrCreateHostSessionSigningKey() {
        requireSignedSessionInternalCaller("load host session signing key");
        boolean previous = Boolean.TRUE.equals(hostSessionSigningKeyAccess.get());
        hostSessionSigningKeyAccess.set(Boolean.TRUE);
        try {
            String configuredKey = System.getProperty(HOST_SESSION_SIGNING_KEY_PROPERTY);
            if (isBlank(configuredKey)) {
                configuredKey = readHostSessionSigningKey();
            }
            if (isBlank(configuredKey)) {
                configuredKey = generateHostSessionSigningKey();
                writeHostSessionSigningKey(configuredKey);
            }
            return configuredKey.getBytes(StandardCharsets.UTF_8);
        } finally {
            if (previous) {
                hostSessionSigningKeyAccess.set(Boolean.TRUE);
            } else {
                hostSessionSigningKeyAccess.remove();
            }
        }
    }

    private static String readHostSessionSigningKey() {
        requireSignedSessionInternalCaller("read host session signing key file");
        Properties properties = new Properties();
        File file = getSessionPropertiesFile();
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
            return properties.getProperty(HOST_SESSION_SIGNING_KEY_PROPERTY);
        } catch (Exception e) {
            LogUtil.warn(AuthenticationTokenWrapper.class.getName(), "Unable to read " + HOST_SESSION_SIGNING_KEY_PROPERTY + " from " + file + ": " + e.getMessage());
            return null;
        }
    }

    private static void writeHostSessionSigningKey(String signingKey) {
        requireSignedSessionInternalCaller("write host session signing key file");
        Properties properties = new Properties();
        File file = getSessionPropertiesFile();
        try {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            if (file.exists()) {
                try (FileInputStream input = new FileInputStream(file)) {
                    properties.load(input);
                }
            } else {
                file.createNewFile();
            }
            if (isBlank(properties.getProperty(HOST_SESSION_SIGNING_KEY_PROPERTY))) {
                properties.setProperty(HOST_SESSION_SIGNING_KEY_PROPERTY, signingKey);
                try (FileOutputStream output = new FileOutputStream(file)) {
                    properties.store(output, "");
                }
            }
        } catch (Exception e) {
            LogUtil.warn(AuthenticationTokenWrapper.class.getName(), "Generated in-memory " + HOST_SESSION_SIGNING_KEY_PROPERTY + " only; clustered nodes must be configured with the same key. Unable to write " + file + ": " + e.getMessage());
        }
    }

    private static String generateHostSessionSigningKey() {
        requireSignedSessionInternalCaller("generate host session signing key");
        byte[] random = new byte[32];
        SECURE_RANDOM.nextBytes(random);
        return Base64.getEncoder().encodeToString(random);
    }

    private static File getSessionPropertiesFile() {
        requireSignedSessionInternalCaller("resolve host session signing key file");
        return new File(SetupManager.getBaseSharedDirectory(), SESSION_FILE);
    }

    public static boolean isHostSessionSigningKeyAccessAllowed() {
        return Boolean.TRUE.equals(hostSessionSigningKeyAccess.get())
                && hasHostSessionSigningKeyAccessFrame();
    }

    public static boolean isSignedSessionRuntimeGuardAvailable() {
        return true;
    }

    static void resetHostSessionSigningKeyForTest() {
        synchronized (AuthenticationTokenWrapper.class) {
            hostSessionSigningKey = null;
        }
    }

    private static void requireSignedSessionInternalCaller(String operation) {
        boolean allowed = INTERNAL_CALLER_STACK_WALKER.walk(frames -> {
            java.util.Iterator<StackFrame> it = frames.iterator();
            boolean internalFrame = false;
            while (it.hasNext()) {
                Class<?> c = it.next().getDeclaringClass();
                if (c == SerializationProxy.class) {
                    return Boolean.TRUE;
                }
                if (c == AuthenticationTokenWrapper.class) {
                    internalFrame = true;
                    continue;
                }
                String name = c.getName();
                if (name.startsWith("java.lang.reflect.")
                        || name.startsWith("jdk.internal.reflect.")
                        || name.startsWith("java.lang.invoke.")) {
                    if (internalFrame) {
                        return Boolean.FALSE;
                    }
                    continue;
                }
                if (internalFrame) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
            return Boolean.FALSE;
        });
        if (!allowed) {
            throw new SecurityException("No permission to " + operation);
        }
    }

    private static void requireTrustedSignedSessionCreationCaller() {
        if (!HostManager.isVirtualHostEnabled()) {
            return;
        }
        if (hasUntrustedSignedSessionCreationFrame()) {
            throw new SecurityException("No permission to create signed session wrapper");
        }
    }

    private static boolean hasUntrustedSignedSessionCreationFrame() {
        // Check only the DIRECT caller (first non-ATW frame). Checking the entire stack
        // would block legitimate logins where a plugin calls a trusted framework service
        // (e.g. UserAuthenticationService.loginUser) that in turn creates ATW.
        return SIGNED_SESSION_STACK_WALKER.walk(frames -> frames
                .map(StackFrame::getDeclaringClass)
                .filter(c -> c != AuthenticationTokenWrapper.class)
                .findFirst()
                .map(AuthenticationTokenWrapper::isUntrustedSignedSessionCreationClass)
                .orElse(false));
    }

    private static boolean isUntrustedSignedSessionCreationClass(Class<?> clazz) {
        if (clazz == null || clazz == AuthenticationTokenWrapper.class) {
            return false;
        }
        String className = clazz.getName();
        if (className.startsWith("bsh.")) {
            return true;
        }
        ClassLoader loader = clazz.getClassLoader();
        while (loader != null) {
            if (isOsgiBundleClassLoader(loader)) {
                return true;
            }
            loader = loader.getParent();
        }
        return false;
    }

    private static boolean isOsgiBundleClassLoader(ClassLoader loader) {
        String loaderClassName = loader.getClass().getName();
        return loaderClassName.contains("BundleClassLoader")
                || loaderClassName.contains("BundleWiringImpl");
    }

    private static boolean hasHostSessionSigningKeyAccessFrame() {
        return INTERNAL_CALLER_STACK_WALKER.walk(frames -> frames.anyMatch(f -> {
            if (f.getDeclaringClass() != AuthenticationTokenWrapper.class) {
                return false;
            }
            String method = f.getMethodName();
            return "loadOrCreateHostSessionSigningKey".equals(method)
                    || "readHostSessionSigningKey".equals(method)
                    || "writeHostSessionSigningKey".equals(method)
                    || "getSessionPropertiesFile".equals(method);
        }));
    }

    private static boolean reject(HttpServletRequest request, HttpServletResponse response, Authentication auth, String loginHost, String reason) {
        String message = buildInvalidHostMessage(request, auth, loginHost, reason);
        LogUtil.warn(AuthenticationTokenWrapper.class.getName(), message);
        addAuditTrail(message);
        try {
            new SecurityContextLogoutHandler().logout(request, response, null);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        } catch (IOException ex) {
            // ignore
        }
        return false;
    }

    private static String buildInvalidHostMessage(HttpServletRequest request, Authentication auth, String loginHost, String reason) {
        String requestHost = (request != null) ? request.getServerName() : null;
        String remoteAddr = getRemoteAddr(request);
        String uri = (request != null) ? request.getRequestURI() : null;
        Object principal = (auth != null) ? auth.getPrincipal() : null;
        return "Invalid tenant host attempt"
                + " reason=" + reason
                + ", user=" + principal
                + ", loginHost=" + loginHost
                + ", requestHost=" + requestHost
                + ", ip=" + remoteAddr
                + ", uri=" + uri;
    }

    private static String getRemoteAddr(HttpServletRequest request) {
        return AppUtil.getClientIp(request);
    }

    private static void addAuditTrail(String message) {
        try {
            if (AppUtil.getApplicationContext() != null) {
                WorkflowHelper workflowHelper = (WorkflowHelper) AppUtil.getApplicationContext().getBean("workflowHelper");
                workflowHelper.addAuditTrail(AuthenticationTokenWrapper.class.getName(), "validateHost", message);
            }
        } catch (Exception e) {
            LogUtil.warn(AuthenticationTokenWrapper.class.getName(), "Unable to add audit trail for invalid tenant host attempt: " + e.getMessage());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    @Override
    public String getName() {
        return authentication.getName();
    }

    @Override
    public boolean implies(Subject subject) {
        return authentication.implies(subject);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection tempAuthorities = new ArrayList<>(authentication.getAuthorities());
        
        // check for sys admin role, add if not in db
        GrantedAuthority ga = new SimpleGrantedAuthority(ROLE_ADMIN);
        if (tempAuthorities.contains(ga) && !EnhancedWorkflowUserManager.isSysAdminRoleAvailable()) {
            tempAuthorities.add(new SimpleGrantedAuthority(EnhancedWorkflowUserManager.ROLE_SYSADMIN));
        }
        
        // add app admin role configured for specific users
        if (!tempAuthorities.contains(ga) && EnhancedWorkflowUserManager.checkCustomAppAdmin()) {
            tempAuthorities.add(ga);
        }
        if (EnhancedWorkflowUserManager.isAppAdminRole()) {
            tempAuthorities.add(new SimpleGrantedAuthority(EnhancedWorkflowUserManager.ROLE_APPADMIN));
        }
        if (tempAuthorities.isEmpty()) {
            tempAuthorities.add(new SimpleGrantedAuthority(WorkflowUserManager.ROLE_USER));
        }

        return tempAuthorities;
    }

    @Override
    public Object getCredentials() {
        return authentication.getCredentials();
    }

    @Override
    public Object getDetails() {
        return authentication.getDetails();
    }

    @Override
    public Object getPrincipal() {
        return authentication.getPrincipal();
    }

    @Override
    public boolean isAuthenticated() {
        return authentication.isAuthenticated();
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        authentication.setAuthenticated(isAuthenticated);
    }
 
    public void clearCredentials() {
        // no direct way in Spring Security, so use reflection to clear password in token
        Field field = null;
        try {
            field = authentication.getClass().getDeclaredField("credentials");
            field.setAccessible(true);
            field.set(authentication, null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new BadCredentialsException(ex.getMessage(), ex);
        } finally {
            if (field != null) {
                field.setAccessible(false);
            }
        }
    }

    /**
     * Serialization proxy (Effective Java item 90). The signed loginHost binding is
     * serialized with the inner Authentication so clustered session replication preserves
     * the original login tenant without trusting ambient HostManager thread state.
     *
     * Direct deserialization validates the HMAC signature before accepting the restored
     * fields, so a tampered or forged stream is rejected with InvalidObjectException.
     */
    private Object writeReplace() {
        return new SerializationProxy(this.authentication, this.loginHost, this.loginHostSignature);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (!isValidSignedBinding(loginHost, loginHostSignature, authentication)) {
            throw new java.io.InvalidObjectException("Invalid signed session binding in stream");
        }
    }

    private static final class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Authentication authentication;
        private final String loginHost;
        private final String loginHostSignature;

        SerializationProxy(Authentication authentication, String loginHost, String loginHostSignature) {
            this.authentication = authentication;
            this.loginHost = loginHost;
            this.loginHostSignature = loginHostSignature;
        }

        private Object readResolve() {
            return new AuthenticationTokenWrapper(authentication, loginHost, loginHostSignature);
        }
    }

}
