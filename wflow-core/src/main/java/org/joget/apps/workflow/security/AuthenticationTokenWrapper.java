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
    // Version history of what this MAC covers: "v1" -> "v2" added authorities; "v2" -> "v3" added
    // a canonical form of getDetails() (WorkflowUserManager reads identity/roles from getDetails()
    // in preference to getAuthorities(), which "v2" alone left uncovered -- see
    // canonicalizeDetails(..)'s own comment); "v3" -> "v4" closed three more getDetails() shapes;
    // "v4" -> "v5" closed a gap "v4" itself introduced in its ExtUserDetails branch (covered
    // getUser().getRoles() but not getAuthorities(), which is what getCurrentRoles() actually
    // reads for that shape). Each bump is deliberate, not incidental: a session signed under an
    // older version recomputes its expected signature with the CURRENT version in the version
    // byte, which can never match a signature actually computed under an older one, so every
    // pre-upgrade session fails closed (forces re-login) instead of being silently accepted as if
    // it had already been checked under the newer scheme.
    private static final String HOST_SESSION_BINDING_VERSION = "v5";
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
        if (!HostManager.isVirtualHostEnabled()) {
            // skip host-session binding when multitenancy is disabled
            this.loginHost = null;
            this.loginHostSignature = null;
            return;
        }
        if (auth instanceof AuthenticationTokenWrapper) {
            AuthenticationTokenWrapper wrapper = (AuthenticationTokenWrapper) auth;
            if (isValidSignedBinding(wrapper.getLoginHost(), wrapper.loginHostSignature, wrapper.authentication)) {
                this.loginHost = wrapper.getLoginHost();
                this.loginHostSignature = wrapper.loginHostSignature;
                return;
            }
            // Invalid wrapper: treat like a non-wrapper — re-signing requires a trusted caller.
        }
        requireTrustedSignedSessionCreationCaller();
        this.loginHost = resolveLoginHost();
        this.loginHostSignature = signLoginHost(this.loginHost, auth);
    }

    private AuthenticationTokenWrapper(Authentication auth, String loginHost, String loginHostSignature) {
        requireSignedSessionInternalCaller("restore signed host session binding");
        // This is the constructor SerializationProxy.readResolve() actually calls -- the outer
        // class's readObject(..) below never runs for a normal deserialization, since
        // writeReplace() unconditionally substitutes the proxy. Verifying HERE, not just there,
        // rejects an untrusted stream as soon as the wrapper is reconstructed, rather than relying
        // solely on the next validateHost(..) call to catch it downstream.
        if (!isValidSignedBinding(loginHost, loginHostSignature, auth)) {
            throw new SecurityException("Invalid signed host session binding");
        }
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

    /**
     * Reports whether this token carries a host-session binding that verifies against the server's
     * signing key -- i.e. whether it is the product of a real, credential-verifying login rather
     * than something assembled by a caller.
     *
     * <p>This is a provenance statement, and it is stronger than it may look. The public
     * constructor calls {@code requireTrustedSignedSessionCreationCaller()} whenever virtual
     * hosting is on, so BeanShell and OSGI bundle code cannot construct a signed wrapper at all,
     * and {@code signLoginHost(..)} is unreachable from outside this class, so the
     * signature cannot be forged either. A token that answers {@code true} here therefore came
     * from a trusted creator such as {@code WorkflowAuthenticationProvider}, whatever code is
     * currently holding it. Callers that must decide whether to trust an {@link Authentication}
     * they were handed can use that instead of guessing from the identity of their caller.
     *
     * <p>Returns {@code true} unconditionally when virtual hosting is disabled, matching the rest
     * of this class: host-session binding only exists in a multi-tenant deployment.
     *
     * <p>Final on purpose -- this class is not final, and a subclass that could override this
     * would be able to assert a provenance it does not have. {@link #getLoginHost()} is final for
     * the same reason.
     *
     * @return true if the signed host-session binding verifies, or virtual hosting is off
     */
    public final boolean isGenuinelySignedSession() {
        return hasValidSignedBinding();
    }

    /**
     * Convenience form of {@link #isGenuinelySignedSession()} for callers holding a plain
     * {@link Authentication}: true only when the given object is an
     * {@code AuthenticationTokenWrapper} whose signed host-session binding verifies. Null-safe,
     * and false for any bare or self-assembled token.
     *
     * <p>The concrete class must also come from this class's own classloader. This class is not
     * final, and while a subclass cannot get itself constructed from BeanShell or an OSGI bundle
     * (see the constructor), a subclass CAN override the un-final parts of the
     * {@link Authentication} contract -- {@code getPrincipal()} in particular, which the binding
     * is not computed over. {@code getName()} and {@code getAuthorities()} ARE covered (see
     * {@link #signLoginHost(String, Authentication)}'s own comment for why authorities needed to
     * be added). Requiring the same classloader keeps any script- or bundle-defined subclass out
     * of this answer regardless. The only in-tree subclass,
     * {@code LicenseAutheticationTokenWrapper}, ships in the same WAR and so shares this
     * classloader.
     *
     * @param authentication the authentication to test, may be null
     * @return true if the authentication provably originated from a trusted login
     */
    public static boolean isTrustedAuthentication(Authentication authentication) {
        return authentication instanceof AuthenticationTokenWrapper
                && authentication.getClass().getClassLoader() == AuthenticationTokenWrapper.class.getClassLoader()
                && ((AuthenticationTokenWrapper) authentication).isGenuinelySignedSession();
    }

    private static boolean isValidSignedBinding(String loginHost, String signature, Authentication auth) {
        if (!HostManager.isVirtualHostEnabled()) {
            return true;
        }
        // Unwrapped HERE, at the method boundary, rather than trusting every caller to have done
        // it already -- see unwrapRaw(..)'s own javadoc for why signing/verifying against a
        // (possibly invalidly-signed) wrapper directly, instead of the raw inner Authentication it
        // wraps, would be wrong. this.authentication (what hasValidSignedBinding()/readObject(..)
        // pass in here) is set unconditionally in the constructor before the wrapper-validity
        // check runs, so it CAN itself be a wrapper in the "invalid nested wrapper" fallthrough
        // case -- defense in depth, not a redundant safety net.
        auth = unwrapRaw(auth);
        if (auth == null) {
            return false;
        }
        if (isBlank(loginHost) || isBlank(signature)) {
            return false;
        }
        String expected = signLoginHost(loginHost, auth);
        if (isBlank(expected)) {
            return false;
        }
        byte[] supplied = signature.getBytes(StandardCharsets.UTF_8);
        byte[] calculated = expected.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(supplied, calculated);
    }

    /**
     * Unwraps to the innermost, non-{@code AuthenticationTokenWrapper} {@link Authentication} --
     * the object whose {@code getName()}/{@code getAuthorities()} this class actually signs and
     * verifies against. Signing/verifying against {@code auth} directly when it might itself be a
     * (possibly invalidly-signed) wrapper would instead cover THIS class's own {@link
     * #getAuthorities()} override, which injects synthetic roles (sysadmin/app-admin) derived from
     * OTHER, separately-mutable server state ({@code EnhancedWorkflowUserManager}) -- signing that
     * would make the signature unstable across calls (a legitimately-signed token could start
     * failing verification later purely because that unrelated state changed), not just wrong in
     * principle. Terminates on a non-wrapper (including {@code null}), so it cannot loop forever
     * even if wrapping were ever nested more than one level deep.
     */
    private static Authentication unwrapRaw(Authentication auth) {
        while (auth instanceof AuthenticationTokenWrapper) {
            auth = ((AuthenticationTokenWrapper) auth).authentication;
        }
        return auth;
    }

    /**
     * A canonical, deterministic string form of {@code auth}'s authorities -- sorted (so the same
     * conceptual authority SET always produces the same signature input regardless of the wrapped
     * {@code Authentication} implementation's own iteration order) and joined with a delimiter no
     * legal Spring Security authority string contains on its own (each entry is additionally
     * length-prefixed below to remove any ambiguity from a delimiter appearing inside an authority
     * string regardless).
     *
     * <p>{@code auth} is expected to already be the result of {@link #unwrapRaw(Authentication)}
     * -- this method does not unwrap itself, so a caller that passes a wrapper here would sign
     * that wrapper's own derived authorities instead, exactly the mistake {@link
     * #unwrapRaw(Authentication)}'s own javadoc explains.
     */
    private static String canonicalizeAuthorities(Authentication auth) {
        if (auth == null) {
            return "";
        }
        return canonicalizeGrantedAuthorities(auth.getAuthorities());
    }

    /**
     * Shared by {@link #canonicalizeAuthorities(Authentication)} and
     * {@link #canonicalizeDetails(Authentication)} so an authorities collection read from
     * {@code getDetails()} is bound the exact same collision-free way as {@code
     * auth.getAuthorities()}, instead of via a second, independently-written encoding that could
     * drift out of sync.
     */
    private static String canonicalizeGrantedAuthorities(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null) {
            return "";
        }
        java.util.List<String> names = new ArrayList<>();
        for (GrantedAuthority ga : authorities) {
            if (ga != null && ga.getAuthority() != null) {
                names.add(ga.getAuthority());
            }
        }
        java.util.Collections.sort(names);
        StringBuilder sb = new StringBuilder();
        for (String n : names) {
            sb.append(n.length()).append(':').append(n).append(';');
        }
        return sb.toString();
    }

    /**
     * Canonicalizes whatever {@code WorkflowUserManager.getCurrentUser()}/{@code
     * getCurrentRoles()} would actually read as identity/roles for this {@code auth} -- which is
     * {@code auth.getDetails()} (falling back to {@code getPrincipal()} when details is null), NOT
     * {@code auth.getAuthorities()}. {@link #canonicalizeAuthorities(Authentication)} alone leaves
     * this uncovered: an attacker holding a legitimately-signed low-privilege session could leave
     * {@code getName()}/{@code getAuthorities()} untouched while tampering the serialized {@code
     * details} object's embedded username/authorities, and every caller that resolves identity/
     * roles through {@code WorkflowUserManager} would use the forged values instead.
     *
     * <p>The branches below deliberately mirror {@code getCurrentUser()}/{@code getCurrentRoles()}
     * 's own {@code instanceof} handling, since what gets signed must be exactly what authorization
     * actually reads:
     * <ul>
     * <li>A bare {@code String} or {@code org.joget.directory.model.User} details/principal is
     * signed directly.</li>
     * <li>{@code ExtUserDetails} is checked before plain {@code UserDetails} (it extends it) and
     * signs both {@code getUser()}'s username/roles -- what {@code getCurrentUser()} reads -- and
     * {@code getAuthorities()} -- what {@code getCurrentRoles()} reads instead, since {@code
     * WorkflowUserDetails.getAuthorities()} does an independent, live directory lookup unrelated to
     * {@code getUser().getRoles()}. Both must be covered, or the uncovered one stays freely
     * tamperable without invalidating the signature.</li>
     * <li>A plain {@code UserDetails} signs its username and {@code getAuthorities()}.</li>
     * <li>Any other shape -- including {@code null} with no usable {@code getPrincipal()} fallback
     * -- canonicalizes to the same empty string, consistent with authorization never reading an
     * unrecognized details object either.</li>
     * </ul>
     */
    private static String canonicalizeDetails(Authentication auth) {
        if (auth == null) {
            return "";
        }
        Object userObj = auth.getDetails();
        if (userObj == null) {
            userObj = auth.getPrincipal();
        }
        if (userObj instanceof String) {
            String s = (String) userObj;
            return "S:" + s.length() + ':' + s;
        } else if (userObj instanceof org.joget.directory.model.service.ExtUserDetails
                && ((org.joget.directory.model.service.ExtUserDetails) userObj).getUser() != null) {
            org.joget.directory.model.service.ExtUserDetails extUserDetails =
                    (org.joget.directory.model.service.ExtUserDetails) userObj;
            return "EU:" + canonicalizeDirectoryUser(extUserDetails.getUser())
                    + ";" + canonicalizeGrantedAuthorities(extUserDetails.getAuthorities());
        } else if (userObj instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails =
                    (org.springframework.security.core.userdetails.UserDetails) userObj;
            String username = userDetails.getUsername();
            StringBuilder sb = new StringBuilder("U:");
            if (username != null) {
                sb.append(username.length()).append(':').append(username);
            }
            sb.append(';').append(canonicalizeGrantedAuthorities(userDetails.getAuthorities()));
            return sb.toString();
        } else if (userObj instanceof org.joget.directory.model.User) {
            return "PU:" + canonicalizeDirectoryUser((org.joget.directory.model.User) userObj);
        }
        return "";
    }

    /**
     * Canonicalizes a raw {@code org.joget.directory.model.User}'s username and roles -- the same
     * fields {@code getCurrentUser()}/{@code getCurrentRoles()} read directly off this object for
     * both the bare-{@code User} and {@code ExtUserDetails.getUser()} shapes (see {@link
     * #canonicalizeDetails(Authentication)}'s own comment). {@code User.getRoles()} returns a raw
     * {@code Set} (no generic element type declared on the class itself); non-{@code Role} entries
     * are skipped rather than thrown on, since a forged collection could otherwise crash signing
     * instead of just failing to canonicalize the bogus entries -- either way, a role-forging
     * attacker gets no signature they can compute themselves.
     */
    private static String canonicalizeDirectoryUser(org.joget.directory.model.User user) {
        if (user == null) {
            return "";
        }
        String username = user.getUsername();
        StringBuilder sb = new StringBuilder();
        if (username != null) {
            sb.append(username.length()).append(':').append(username);
        }
        sb.append(';');
        java.util.Set<?> roles = user.getRoles();
        if (roles != null) {
            java.util.List<String> ids = new ArrayList<>();
            for (Object o : roles) {
                if (o instanceof org.joget.directory.model.Role) {
                    String id = ((org.joget.directory.model.Role) o).getId();
                    if (id != null) {
                        ids.add(id);
                    }
                }
            }
            java.util.Collections.sort(ids);
            for (String id : ids) {
                sb.append(id.length()).append(':').append(id).append(';');
            }
        }
        return sb.toString();
    }

    private static String signLoginHost(String loginHost, Authentication auth) {
        requireSignedSessionInternalCaller("sign host session binding");
        // Unwrapped HERE too (same reasoning as isValidSignedBinding(String, String,
        // Authentication)'s own comment) so this entry point is safe regardless of whether every
        // future caller remembers to unwrap first.
        auth = unwrapRaw(auth);
        if (auth == null) {
            return null;
        }
        return signLoginHost(loginHost, getAuthenticationName(auth), canonicalizeAuthorities(auth),
                canonicalizeDetails(auth));
    }

    /**
     * The MAC covers {@code authorities} in addition to {@code loginHost}/{@code name}: since
     * {@code getName()} is unaffected by an authority swap, a signature computed only over
     * name/loginHost would let an attacker holding a legitimately-signed low-privilege session
     * tamper the wrapped {@code Authentication}'s serialized authorities to {@code ROLE_ADMIN}
     * while name/loginHost/signature stay byte-identical, and verification would still match.
     * Binding the authorities into the same MAC means any authority change also changes the MAC
     * input, so a tampered signature no longer matches and verification fails closed. {@code auth}
     * MUST be the result of {@link #unwrapRaw(Authentication)} -- see that method's own javadoc
     * for why signing a wrapper's own (possibly injected, possibly unstable) authorities instead
     * would be wrong.
     */
    private static String signLoginHost(String loginHost, String name, String authoritiesCanonical,
            String detailsCanonical) {
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
            mac.update((byte) 0);
            mac.update(authoritiesCanonical.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) 0);
            mac.update(detailsCanonical.getBytes(StandardCharsets.UTF_8));
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
            boolean internalReadObjectFrame = false;
            boolean reflectiveReadObjectDispatch = false;
            while (it.hasNext()) {
                StackFrame frame = it.next();
                Class<?> c = frame.getDeclaringClass();
                if (c == SerializationProxy.class) {
                    return Boolean.TRUE;
                }
                if (c == AuthenticationTokenWrapper.class) {
                    internalFrame = true;
                    if ("readObject".equals(frame.getMethodName())) {
                        internalReadObjectFrame = true;
                    }
                    continue;
                }
                String name = c.getName();
                if (isReflectionDispatchClass(name)) {
                    if (internalFrame) {
                        if (!internalReadObjectFrame) {
                        return Boolean.FALSE;
                    }
                        reflectiveReadObjectDispatch = true;
                    continue;
                }
                    continue;
                }
                if (internalFrame) {
                    if (reflectiveReadObjectDispatch) {
                        return isTrustedJavaDeserializationClass(c);
                    }
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

    private static boolean isReflectionDispatchClass(String className) {
        return className.startsWith("java.lang.reflect.")
                || className.startsWith("jdk.internal.reflect.")
                || className.startsWith("java.lang.invoke.");
    }

    private static boolean isTrustedJavaDeserializationClass(Class<?> clazz) {
        return clazz == ObjectInputStream.class
                || clazz == java.io.ObjectStreamClass.class;
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
        // Check only the DIRECT caller (first frame that is not one of our own pass-through
        // frames). Checking the entire stack would block legitimate logins where a plugin calls a
        // trusted framework service (e.g. UserAuthenticationService.loginUser) that in turn
        // creates ATW.
        return SIGNED_SESSION_STACK_WALKER.walk(frames -> frames
                .map(StackFrame::getDeclaringClass)
                .filter(c -> !isSignedSessionPassThroughClass(c))
                .findFirst()
                .map(AuthenticationTokenWrapper::isUntrustedSignedSessionCreationClass)
                .orElse(false));
    }

    /**
     * Frames that must be walked PAST before asking "is my caller untrusted?", because they are
     * trusted call <em>sites</em> but never trusted call <em>origins</em>.
     *
     * <p>The distinction matters. This class' own frames are obviously transparent -- they are the
     * implementation of the check itself. The other two are transparent for a security reason
     * rather than a mechanical one: each is a public, reachable-by-anyone wrapper that ends up
     * invoking this constructor on its caller's behalf, so if the walk stops at one of them it
     * reports the wrapper's own (webapp, therefore trusted) classloader and the real originator --
     * which may be BeanShell or an OSGI bundle -- is never inspected at all. That turns each of
     * them into a signing oracle: call the wrapper, get back a validly signed
     * AuthenticationTokenWrapper you could not have constructed directly.
     *
     * <ul>
     *   <li><b>Subclasses of this class.</b> {@code LicenseAutheticationTokenWrapper} (enterprise)
     *       is public with a public {@code (Authentication)} constructor whose only statement is
     *       {@code super(auth)}. Its {@code <init>} frame sits between the attacker and this check
     *       and is a DIFFERENT class from {@code AuthenticationTokenWrapper}, so an exact-class
     *       filter treats it as "the caller" and finds it trusted.</li>
     *   <li><b>{@link org.joget.apps.util.JogetSecurityContextImpl} and its subclasses.</b> Its
     *       {@code setAuthentication(..)} is public and unguarded and wraps any non-anonymous,
     *       not-already-wrapped token in a fresh {@code AuthenticationTokenWrapper}, so the same
     *       shielding applies to anyone who can reach a {@code SecurityContext}.</li>
     * </ul>
     *
     * <p>This is the same confused-deputy shape, and the same remedy, as
     * {@code MultiTenantSecurityManager.isNativeLibraryLoadOriginatedFromUntrustedCode()}: skip the
     * known-internal frames that any caller can reach and judge the first genuine application
     * frame behind them.
     *
     * <p>Not treated as transparent: enterprise's {@code EnterpriseJogetSecurityContextImpl}. It
     * also wraps, but only after its own {@code setAuthentication(..)} has already made the trust
     * decision for that call earlier in the same invocation, so it is not an unguarded oracle the
     * way the community {@code JogetSecurityContextImpl} is. It is also not a subclass of
     * {@code JogetSecurityContextImpl} (it extends Spring's {@code SecurityContextImpl} directly),
     * so the assignability test below does not reach it.
     */
    private static boolean isSignedSessionPassThroughClass(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return AuthenticationTokenWrapper.class.isAssignableFrom(clazz)
                || org.joget.apps.util.JogetSecurityContextImpl.class.isAssignableFrom(clazz);
    }

    private static boolean isUntrustedSignedSessionCreationClass(Class<?> clazz) {
        if (clazz == null || clazz == AuthenticationTokenWrapper.class) {
            return false;
        }
        // BeanShell's own interpreter classes ship inside the WAR, so they ARE framework-loadable
        // and have to be named explicitly; everything else is decided by where the class was
        // defined rather than by what it is called.
        if (clazz.getName().startsWith("bsh.")) {
            return true;
        }
        return !isFrameworkLoadable(clazz.getClassLoader());
    }

    /**
     * True when {@code loader} is this class' own (application/webapp) classloader or one of its
     * ancestors, up to and including the bootstrap classloader.
     *
     * <p>A POSITIVE membership test, deliberately, rather than the negative
     * "is the loader's class name one we recognise as a Felix bundle loader" test this used to do.
     * A negative test only rejects the loaders it has been told about: an OSGI bundle can define a
     * helper class through a classloader of its own making -- {@code new ClassLoader(null){..}}
     * delegating the platform APIs by hand -- whose class name matches no known pattern and whose
     * parent chain is empty, and that helper would then have been judged trusted and allowed to
     * reach the signing constructor. Anchoring on "is it in OUR delegation chain" cannot be escaped
     * that way: a class defined outside the chain is by definition not platform code.
     *
     * <p>This mirrors {@code MultiTenantSecurityManager.isFrameworkLoadable(..)}, which the
     * enterprise security manager already uses for exactly this question (and which its
     * {@code isFelixSecureAction(..)} pairs with a name test in the same "name AND framework-loaded"
     * shape). It is duplicated rather than shared because {@code wflow-core} cannot depend on
     * {@code cloud-wfengine}.
     */
    private static boolean isFrameworkLoadable(ClassLoader loader) {
        if (loader == null) {
            return true; // bootstrap classloader is an ancestor of every classloader
        }
        for (ClassLoader l = AuthenticationTokenWrapper.class.getClassLoader(); l != null; l = l.getParent()) {
            if (l == loader) {
                return true;
            }
        }
        return false;
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
