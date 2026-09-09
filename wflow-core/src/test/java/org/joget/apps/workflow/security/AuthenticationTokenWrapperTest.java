package org.joget.apps.workflow.security;

import org.joget.commons.util.HostManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AuthenticationTokenWrapperTest {

    private String virtualHostProperty;
    private String hostSessionSigningKeyProperty;

    @Before
    public void setUp() {
        virtualHostProperty = System.getProperty(HostManager.SYSTEM_PROPERTY_VIRTUALHOST);
        hostSessionSigningKeyProperty = System.getProperty(AuthenticationTokenWrapper.HOST_SESSION_SIGNING_KEY_PROPERTY);
        System.setProperty(HostManager.SYSTEM_PROPERTY_VIRTUALHOST, "true");
        System.setProperty(AuthenticationTokenWrapper.HOST_SESSION_SIGNING_KEY_PROPERTY, "test-host-session-signing-key");
        AuthenticationTokenWrapper.resetHostSessionSigningKeyForTest();
        SecurityContextHolder.clearContext();
        HostManager.setCurrentProfile(null);
        AuthenticationTokenWrapper.clearLoginHost();
        RequestContextHolder.resetRequestAttributes();
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
        HostManager.setCurrentProfile(null);
        AuthenticationTokenWrapper.clearLoginHost();
        RequestContextHolder.resetRequestAttributes();
        if (virtualHostProperty == null) {
            System.clearProperty(HostManager.SYSTEM_PROPERTY_VIRTUALHOST);
        } else {
            System.setProperty(HostManager.SYSTEM_PROPERTY_VIRTUALHOST, virtualHostProperty);
        }
        if (hostSessionSigningKeyProperty == null) {
            System.clearProperty(AuthenticationTokenWrapper.HOST_SESSION_SIGNING_KEY_PROPERTY);
        } else {
            System.setProperty(AuthenticationTokenWrapper.HOST_SESSION_SIGNING_KEY_PROPERTY, hostSessionSigningKeyProperty);
        }
        AuthenticationTokenWrapper.resetHostSessionSigningKeyForTest();
    }

    // --- validateHost tests (original suite) ---

    @Test
    public void validateHostAllowsSameLoginHost() {
        HostManager.setCurrentProfile("tenant-a");
        SecurityContextHolder.getContext().setAuthentication(wrapUserAuthentication());

        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = AuthenticationTokenWrapper.validateHost(request("tenant-a.example.test"), response);

        assertTrue(result);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void validateHostAllowsSameLoginHostFromExplicitAuthentication() {
        HostManager.setCurrentProfile("tenant-a");
        AuthenticationTokenWrapper authentication = wrapUserAuthentication();

        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = AuthenticationTokenWrapper.validateHost(request("tenant-a.example.test"), response, authentication);

        assertTrue(result);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void validateHostRejectsDifferentHost() {
        HostManager.setCurrentProfile("tenant-a");
        SecurityContextHolder.getContext().setAuthentication(wrapUserAuthentication());

        HostManager.setCurrentProfile("tenant-b");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = AuthenticationTokenWrapper.validateHost(request("tenant-b.example.test"), response);

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    public void validateHostRejectsDifferentHostFromExplicitAuthentication() {
        HostManager.setCurrentProfile("tenant-a");
        AuthenticationTokenWrapper authentication = wrapUserAuthentication();

        HostManager.setCurrentProfile("tenant-b");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = AuthenticationTokenWrapper.validateHost(request("tenant-b.example.test"), response, authentication);

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    public void validateHostRejectsMissingLoginHost() {
        // No profile set - loginHost will be null fail-closed
        SecurityContextHolder.getContext().setAuthentication(wrapUserAuthentication());

        HostManager.setCurrentProfile("tenant-a");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = AuthenticationTokenWrapper.validateHost(request("tenant-a.example.test"), response);

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    public void validateHostRejectsUnwrappedAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(userAuthentication());

        HostManager.setCurrentProfile("tenant-a");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = AuthenticationTokenWrapper.validateHost(request("tenant-a.example.test"), response);

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    public void validateHostAllowsAnonymousAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        HostManager.setCurrentProfile("tenant-a");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = AuthenticationTokenWrapper.validateHost(request("tenant-a.example.test"), response);

        assertTrue(result);
        assertEquals(200, response.getStatus());
    }

    // --- bindLoginHost() - form-login capture path ---

    @Test
    public void bindLoginHostCapturesHostnameInToken() {
        // Simulates WorkflowHttpAuthProcessingFilter.authenticate() calling
        // bindLoginHost(request.getServerName()) before getAuthenticationManager().authenticate()
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());

        assertEquals("tenant-a.example.test", token.getLoginHost());
    }

    @Test
    public void bindLoginHostTokenPassesValidation() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());

        // Simulate initHost() at validation time: profile=null, currentHost="tenant-a.example.test"
        // currentTenantKey() falls through to getCurrentHost() = "tenant-a.example.test"
        HostManager.setCurrentHost("tenant-a.example.test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = AuthenticationTokenWrapper.validateHost(request("tenant-a.example.test"), response, token);

        assertTrue(result);
        assertEquals(200, response.getStatus());
    }

    // --- bindLoginHostFromCurrentRequest() - SSO / programmatic-login path ---

    @Test
    public void bindLoginHostFromCurrentRequestCapturesServerName() {
        // Simulates EnterpriseJogetSecurityContextImpl.setAuthentication() calling
        // bindLoginHostFromCurrentRequest() before new AuthenticationTokenWrapper(auth)
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setServerName("tenant-a.example.test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        AuthenticationTokenWrapper.bindLoginHostFromCurrentRequest();
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());

        assertEquals("tenant-a.example.test", token.getLoginHost());
    }

    @Test
    public void bindLoginHostFromCurrentRequestTokenPassesValidation() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setServerName("tenant-a.example.test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        AuthenticationTokenWrapper.bindLoginHostFromCurrentRequest();
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());

        // Simulate initHost() at validation time: profile=null, currentHost="tenant-a.example.test"
        HostManager.setCurrentHost("tenant-a.example.test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = AuthenticationTokenWrapper.validateHost(request("tenant-a.example.test"), response, token);

        assertTrue(result);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void bindLoginHostFromCurrentRequestWithNoContextFallsBackToProfile() {
        // No RequestContextHolder set - bindLoginHostFromCurrentRequest() silently does nothing.
        // resolveLoginHost() falls back to currentTenantKey() = currentProfile.
        HostManager.setCurrentProfile("tenant-a");

        AuthenticationTokenWrapper.bindLoginHostFromCurrentRequest();
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());

        assertEquals("tenant-a", token.getLoginHost());
    }

    // --- Spoofing-prevention: bindLoginHostFromCurrentRequest() must not override an already-bound host ---

    @Test
    public void spoofingViaRequestContextHolderIsBlockedWhenHostAlreadyBound() {
        // Simulates: form login correctly bound the real host.
        // A BeanShell plugin then calls RequestContextHolder.setRequestAttributes() with a fake request
        // and triggers bindLoginHostFromCurrentRequest() (e.g. via loginUser(User user) - setAuthentication()).
        // The ThreadLocal guard must prevent the override.
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");

        // Attacker injects a fake request pointing to tenant-b
        MockHttpServletRequest fakeRequest = new MockHttpServletRequest();
        fakeRequest.setServerName("tenant-b.example.test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(fakeRequest));

        AuthenticationTokenWrapper.bindLoginHostFromCurrentRequest(); // must be no-op
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());

        assertEquals("tenant-a.example.test", token.getLoginHost());
    }

    @Test
    public void spoofedTokenIsRejectedByValidateHost() {
        // Even if a spoofed token were created (loginHost = "tenant-b") and presented on tenant-a,
        // validateHost must reject it.
        HostManager.setCurrentProfile("tenant-b");
        AuthenticationTokenWrapper spoofedToken = new AuthenticationTokenWrapper(userAuthentication());

        HostManager.setCurrentProfile("tenant-a");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = AuthenticationTokenWrapper.validateHost(request("tenant-a.example.test"), response, spoofedToken);

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    public void validateHostRejectsTamperedLoginHostEvenWhenHostMatches() throws Exception {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());

        Field loginHost = AuthenticationTokenWrapper.class.getDeclaredField("loginHost");
        loginHost.setAccessible(true);
        loginHost.set(token, "tenant-b.example.test");

        HostManager.setCurrentHost("tenant-b.example.test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = AuthenticationTokenWrapper.validateHost(request("tenant-b.example.test"), response, token);

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    public void serializedTokenPreservesSignedLoginHostWithoutCurrentHostContext() throws Exception {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());

        byte[] serialized = serialize(token);
        AuthenticationTokenWrapper.clearLoginHost();
        HostManager.setCurrentHost(null);
        HostManager.setCurrentProfile(null);

        AuthenticationTokenWrapper restored = (AuthenticationTokenWrapper) deserialize(serialized);
        assertEquals("tenant-a.example.test", restored.getLoginHost());

        HostManager.setCurrentHost("tenant-a.example.test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = AuthenticationTokenWrapper.validateHost(request("tenant-a.example.test"), response, restored);

        assertTrue(result);
        assertEquals(200, response.getStatus());
    }

    /**
     * Deserialization verifies the signature as soon as {@code SerializationProxy.readResolve()}
     * reconstructs the wrapper (see the private 3-arg constructor), rather than only at the next
     * {@code validateHost(..)} call -- so a stale-key-signed stream is rejected at deserialize
     * time, closer to the point the signing key changed, instead of surfacing only as a later
     * 403.
     */
    @Test
    public void serializedTokenRejectsAfterSigningKeyChange() throws Exception {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());
        byte[] serialized = serialize(token);

        System.setProperty(AuthenticationTokenWrapper.HOST_SESSION_SIGNING_KEY_PROPERTY, "different-test-host-session-signing-key");
        AuthenticationTokenWrapper.resetHostSessionSigningKeyForTest();

        try {
            deserialize(serialized);
            fail("stale-key-signed stream should be rejected at deserialize time");
        } catch (SecurityException expected) {
            assertEquals("Invalid signed host session binding", expected.getMessage());
        }
    }

    @Test
    public void directReflectionCannotInvokeSigner() throws Exception {
        Method signer = AuthenticationTokenWrapper.class.getDeclaredMethod(
                "signLoginHost", String.class, org.springframework.security.core.Authentication.class);
        signer.setAccessible(true);

        try {
            signer.invoke(null, "tenant-b.example.test", userAuthentication());
            fail("Expected signer reflection to be blocked");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof SecurityException);
        }
    }

    // --- Double-wrapping: enterprise pattern (AuthenticationTokenWrapper inside another AuthenticationTokenWrapper) ---

    @Test
    public void doubleWrappingPreservesLoginHostFromThreadLocal() {
        // Simulates EnterpriseJogetSecurityContextImpl.setAuthentication():
        //   bindLoginHostFromCurrentRequest()                       // our fix
        //   authentication = new AuthenticationTokenWrapper(auth)   // line 135
        //   authentication = new LicenseAutheticationTokenWrapper(authentication) // line 138, calls super(auth)
        //
        // LicenseAutheticationTokenWrapper extends AuthenticationTokenWrapper and calls super(auth),
        // which again invokes resolveLoginHost(). The ThreadLocal must still hold the correct host.
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");

        // First wrap (line 135)
        AuthenticationTokenWrapper firstWrap = new AuthenticationTokenWrapper(userAuthentication());
        assertEquals("tenant-a.example.test", firstWrap.getLoginHost());

        // Enterprise directory clears getCurrentHost() (simulated by profile switch)
        HostManager.setCurrentProfile("jwdb9");

        // Second wrap (line 138: super(firstWrap) in LicenseAutheticationTokenWrapper)
        // ThreadLocal is still "tenant-a.example.test" - enterprise directory call doesn't affect it
        AuthenticationTokenWrapper secondWrap = new AuthenticationTokenWrapper(firstWrap);
        assertEquals("tenant-a.example.test", secondWrap.getLoginHost());
    }

    @Test
    public void doubleWrappingWithoutBindPreservesFirstSignedWrapperHost() {
        // Without an explicit request-host bind, the first wrapper still signs the
        // current profile, and the second wrapper must preserve that signed binding.
        HostManager.setCurrentProfile("tenant-a");
        AuthenticationTokenWrapper firstWrap = new AuthenticationTokenWrapper(userAuthentication());
        assertEquals("tenant-a", firstWrap.getLoginHost());

        HostManager.setCurrentProfile("jwdb9");
        AuthenticationTokenWrapper secondWrap = new AuthenticationTokenWrapper(firstWrap);
        assertEquals("tenant-a", secondWrap.getLoginHost());
    }

    // --- clearLoginHost() ---

    @Test
    public void clearLoginHostResetsThreadLocal() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        AuthenticationTokenWrapper.clearLoginHost();

        HostManager.setCurrentProfile("tenant-b");
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());

        // After clear, falls back to currentTenantKey() = "tenant-b"
        assertEquals("tenant-b", token.getLoginHost());
    }

    // --- provenance accessors ---

    /**
     * The enterprise SecurityContext guard decides whether to accept a programmatic login on the
     * PROVENANCE of the incoming token rather than on who is calling, so that a plugin-driven SSO
     * login keeps working while a self-assembled token does not. These two accessors are what it
     * asks; they must answer for the signed binding and nothing else.
     */
    @Test
    public void genuinelySignedSessionReportsTrueForATokenThisClassSigned() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());

        assertTrue(token.isGenuinelySignedSession());
        assertTrue(AuthenticationTokenWrapper.isTrustedAuthentication(token));
    }

    @Test
    public void genuinelySignedSessionReportsFalseWhenTheSignatureIsTampered() throws Exception {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());

        Field signature = AuthenticationTokenWrapper.class.getDeclaredField("loginHostSignature");
        signature.setAccessible(true);
        signature.set(token, "not-a-real-signature");

        assertFalse(token.isGenuinelySignedSession());
        assertFalse(AuthenticationTokenWrapper.isTrustedAuthentication(token));
    }

    /**
     * Regression test: the host-session signature must cover the wrapped {@link Authentication}'s
     * authorities, not just {@code loginHost} and the authenticated NAME. Since {@code getName()}
     * is unaffected by an authority swap, signing only name/loginHost would let an attacker holding
     * a legitimately-signed session replace the wrapped Authentication's authorities (e.g. via
     * tampering a serialized stream in transit/at rest, or any other means of substituting the
     * wrapped object) with {@code ROLE_ADMIN} while keeping the SAME name/loginHost/signature --
     * {@code isValidSignedBinding(..)} would recompute the expected signature from the unchanged
     * name and loginHost, so it would match, and the forged-privilege token would be accepted as
     * "genuinely signed."
     *
     * <p>Reproduces this directly: signs a real token for "user"/ROLE_USER, then substitutes the
     * WRAPPED (package-private) {@code authentication} field for a DIFFERENT token with the SAME
     * name "user" but ROLE_ADMIN authorities instead -- {@code loginHost}/{@code loginHostSignature}
     * are left completely untouched, exactly modeling the tamper. Before the fix (authorities not
     * signed), this reports {@code true} (a real, live-confirmed escalation); after it, {@code
     * false}, since the recomputed signature no longer matches once authorities are part of the
     * MAC input.
     */
    @Test
    public void genuinelySignedSessionReportsFalseWhenAuthoritiesAreSwappedWithNameUnchanged() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());
        assertTrue("sanity: the token must start out genuinely signed", token.isGenuinelySignedSession());

        // Same name ("user") as the originally-signed authentication -- only the authorities
        // differ. loginHost/loginHostSignature are untouched.
        UsernamePasswordAuthenticationToken sameNameEscalated = new UsernamePasswordAuthenticationToken(
                "user", "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        token.authentication = sameNameEscalated;

        assertFalse("a token whose wrapped authorities were swapped to ROLE_ADMIN after signing, "
                        + "with the name/loginHost/signature left byte-identical, must NOT be "
                        + "reported as genuinely signed -- the signature must cover authorities, "
                        + "not just loginHost/name",
                token.isGenuinelySignedSession());
        assertFalse(AuthenticationTokenWrapper.isTrustedAuthentication(token));
    }

    /**
     * Companion to the escalation test above: swapping to a DIFFERENT, but still low-privilege,
     * authority set must ALSO invalidate the signature -- confirms the fix reacts to ANY authority
     * change, not just an admin-specific one (i.e. it is genuinely binding the authority set into
     * the MAC, not special-casing ROLE_ADMIN).
     */
    @Test
    public void genuinelySignedSessionReportsFalseWhenAuthoritiesAreChangedToADifferentNonAdminSet() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());

        UsernamePasswordAuthenticationToken sameNameDifferentRole = new UsernamePasswordAuthenticationToken(
                "user", "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_SOME_OTHER_ROLE")));
        token.authentication = sameNameDifferentRole;

        assertFalse(token.isGenuinelySignedSession());
    }

    /**
     * Confirms the fix does not just react to authority PRESENCE/ABSENCE but to the actual
     * authority SET -- reordering (a Collection with no guaranteed iteration order) must not
     * accidentally invalidate a genuine signature, and adding/removing a single entry from an
     * otherwise-identical set must invalidate it. Exercises {@code canonicalizeAuthorities(..)}'s
     * own sorting indirectly (through the public API only, matching this test file's convention of
     * not reaching into private helpers where the public contract already proves the behavior).
     */
    @Test
    public void genuinelySignedSessionReportsTrueWhenAuthoritiesAreReorderedButUnchanged() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        java.util.List<SimpleGrantedAuthority> original = java.util.Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_OTHER"));
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(
                new UsernamePasswordAuthenticationToken("user", "password", original));
        assertTrue(token.isGenuinelySignedSession());

        // Same two authorities, reversed order, in a freshly-constructed List (a different
        // Collection instance/iteration order than what was originally signed).
        java.util.List<SimpleGrantedAuthority> reordered = java.util.Arrays.asList(
                new SimpleGrantedAuthority("ROLE_OTHER"), new SimpleGrantedAuthority("ROLE_USER"));
        token.authentication = new UsernamePasswordAuthenticationToken("user", "", reordered);

        assertTrue("reordering the SAME authority set must not invalidate a genuine signature -- "
                        + "the signature must be computed over a canonical (sorted) form",
                token.isGenuinelySignedSession());
    }

    /**
     * Regression test: the "v2" authorities fix covers {@code auth.getName()}/{@code
     * auth.getAuthorities()}, but {@code WorkflowUserManager.getCurrentUser()}/{@code
     * getCurrentRoles()} -- the ACTUAL identity/role source the rest of the application reads --
     * prefer {@code auth.getDetails()} over {@code getAuthorities()} whenever {@code getDetails()}
     * is a {@code UserDetails} (which the real login path, {@code WorkflowAuthenticationProvider},
     * always sets it to). Without also signing {@code getDetails()}, an attacker could leave
     * {@code getName()}/{@code getAuthorities()} untouched -- keeping the "v2" signature valid --
     * while tampering the wrapped {@code details} object's own username, and every caller
     * resolving identity through {@code WorkflowUserManager} would use the forged value instead.
     *
     * <p>Reproduces this directly: signs a real token for "user"/ROLE_USER with matching details,
     * then replaces ONLY the wrapped authentication's {@code details} with a {@code UserDetails}
     * claiming a DIFFERENT username ("admin") but the SAME authorities -- {@code getName()}/{@code
     * getAuthorities()} on the wrapped authentication are completely untouched. Before the "v3"
     * fix (details not signed), this reports {@code true} (a real escalation); after it, {@code
     * false}.
     */
    @Test
    public void genuinelySignedSessionReportsFalseWhenDetailsUsernameIsForgedWithNameAndAuthoritiesUnchanged() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        UsernamePasswordAuthenticationToken genuine = userAuthentication();
        genuine.setDetails(new StubUserDetails("user",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(genuine);
        assertTrue("sanity: the token must start out genuinely signed", token.isGenuinelySignedSession());

        genuine.setDetails(new StubUserDetails("admin",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));

        assertFalse("a token whose details-object username was forged after signing, with the "
                        + "top-level name/authorities/loginHost/signature left byte-identical, "
                        + "must NOT be reported as genuinely signed -- WorkflowUserManager reads "
                        + "identity from getDetails() in preference to getName(), so the "
                        + "signature must cover it too",
                token.isGenuinelySignedSession());
        assertFalse(AuthenticationTokenWrapper.isTrustedAuthentication(token));
    }

    /**
     * Companion to the details-username test above: forging the details object's OWN authorities
     * (while its username and the top-level name/authorities stay unchanged) must ALSO invalidate
     * the signature, since {@code WorkflowUserManager.getCurrentRoles()} reads roles from {@code
     * ((UserDetails) auth.getDetails()).getAuthorities()} in preference to {@code
     * auth.getAuthorities()}.
     */
    @Test
    public void genuinelySignedSessionReportsFalseWhenDetailsAuthoritiesAreForgedWithNameUnchanged() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        UsernamePasswordAuthenticationToken genuine = userAuthentication();
        genuine.setDetails(new StubUserDetails("user",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(genuine);
        assertTrue(token.isGenuinelySignedSession());

        genuine.setDetails(new StubUserDetails("user",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        assertFalse("a token whose details-object authorities were forged to ROLE_ADMIN after "
                        + "signing, with the username and top-level name/authorities left "
                        + "byte-identical, must NOT be reported as genuinely signed",
                token.isGenuinelySignedSession());
    }

    /**
     * Sanity/non-regression companion: a details object that genuinely matches the top-level
     * name/authorities (the shape {@code WorkflowAuthenticationProvider}'s real login path
     * produces) must NOT be treated as tampered -- confirms {@code canonicalizeDetails(..)} does
     * not spuriously invalidate an ordinary, legitimately-signed session.
     */
    @Test
    public void genuinelySignedSessionReportsTrueWhenDetailsGenuinelyMatches() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        UsernamePasswordAuthenticationToken genuine = userAuthentication();
        genuine.setDetails(new StubUserDetails("user",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(genuine);

        assertTrue("a details object matching the top-level name/authorities, exactly as a "
                        + "genuine login produces, must verify as genuinely signed",
                token.isGenuinelySignedSession());
    }

    /**
     * A bare {@code org.joget.directory.model.User} details object -- the shape {@code
     * getCurrentUser()}'s own {@code instanceof User} branch trusts directly, reading its roles
     * via {@code user.getRoles()} -- must also be covered by the signature, not canonicalized to
     * the same empty string a {@code null} details would produce.
     */
    @Test
    public void genuinelySignedSessionReportsFalseWhenBareUserDetailsRolesAreForged() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        UsernamePasswordAuthenticationToken genuine = userAuthentication();
        org.joget.directory.model.User user = new org.joget.directory.model.User();
        user.setUsername("user");
        user.setRoles(rolesOf("ROLE_USER"));
        genuine.setDetails(user);
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(genuine);
        assertTrue("sanity: the token must start out genuinely signed", token.isGenuinelySignedSession());

        user.setRoles(rolesOf("ROLE_ADMIN"));

        assertFalse("a bare User object's roles field was forged after signing (its own setter, "
                        + "no re-wrap needed) -- getCurrentUser() reads this field directly, so the "
                        + "signature must cover it too",
                token.isGenuinelySignedSession());
    }

    /**
     * Companion sanity check: a bare {@code User} details object whose roles genuinely match must
     * verify as genuinely signed -- confirms the new branch does not spuriously invalidate an
     * ordinary session using this shape.
     */
    @Test
    public void genuinelySignedSessionReportsTrueWhenBareUserDetailsGenuinelyMatches() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        UsernamePasswordAuthenticationToken genuine = userAuthentication();
        org.joget.directory.model.User user = new org.joget.directory.model.User();
        user.setUsername("user");
        user.setRoles(rolesOf("ROLE_USER"));
        genuine.setDetails(user);
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(genuine);

        assertTrue("a bare User details object matching what was signed must verify as genuinely "
                        + "signed", token.isGenuinelySignedSession());
    }

    /**
     * {@code getCurrentUser()}/{@code getCurrentRoles()} fall back to {@code auth.getPrincipal()}
     * whenever {@code getDetails()} is {@code null} -- that fallback principal must be covered by
     * the signature too, or its identity/roles could be forged while details stays null.
     */
    @Test
    public void genuinelySignedSessionReportsFalseWhenPrincipalRolesAreForgedWithNullDetails() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        StubUserDetails principal = new StubUserDetails("user",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken genuine = new UsernamePasswordAuthenticationToken(
                principal, "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        genuine.setDetails(null);
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(genuine);
        assertTrue("sanity: the token must start out genuinely signed", token.isGenuinelySignedSession());

        genuine = new UsernamePasswordAuthenticationToken(
                new StubUserDetails("user", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))),
                "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        genuine.setDetails(null);
        token.authentication = genuine;

        assertFalse("with getDetails() null, getCurrentUser()/getCurrentRoles() fall back to "
                        + "getPrincipal() -- forging the principal's authorities must invalidate "
                        + "the signature the same way forging details would",
                token.isGenuinelySignedSession());
    }

    /**
     * {@code getCurrentUser()}'s {@code ExtUserDetails} branch does NOT read {@code
     * getAuthorities()} -- it reads {@code getUser().getRoles()} directly. Signing {@code
     * getAuthorities()} for this shape (as the generic {@code UserDetails} branch would, since
     * {@code ExtUserDetails extends UserDetails}) covers a value {@code WorkflowUserDetails
     * .getAuthorities()} (the real implementation) derives from a separate, LIVE directory lookup
     * unrelated to {@code getUser().getRoles()} -- leaving the embedded {@code User}'s roles field
     * itself freely tamperable. The signature must cover {@code getUser().getRoles()} directly.
     */
    @Test
    public void genuinelySignedSessionReportsFalseWhenExtUserDetailsGetUserRolesAreForged() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        org.joget.directory.model.User user = new org.joget.directory.model.User();
        user.setUsername("user");
        user.setRoles(rolesOf("ROLE_USER"));
        UsernamePasswordAuthenticationToken genuine = userAuthentication();
        genuine.setDetails(new StubExtUserDetails(user,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(genuine);
        assertTrue("sanity: the token must start out genuinely signed", token.isGenuinelySignedSession());

        // Forge the embedded User's OWN roles while leaving getAuthorities() (a separately-held
        // field on the stub, standing in for WorkflowUserDetails' own live directory lookup)
        // completely untouched -- exactly the shape that made "v3" sign the wrong value.
        user.setRoles(rolesOf("ROLE_ADMIN"));

        assertFalse("an ExtUserDetails' embedded User.roles was forged after signing, with "
                        + "getAuthorities() left byte-identical -- getCurrentUser()'s ExtUserDetails "
                        + "branch reads getUser().getRoles() directly, so the signature must cover "
                        + "that, not getAuthorities()",
                token.isGenuinelySignedSession());
    }

    /**
     * Companion gap to the test above: {@code getCurrentRoles()} -- unlike {@code
     * getCurrentUser()} -- does NOT read {@code getUser().getRoles()} for an {@code
     * ExtUserDetails}; since {@code ExtUserDetails extends UserDetails}, {@code getCurrentRoles()}
     * takes its OWN {@code instanceof UserDetails} branch and reads {@code getAuthorities()}
     * instead. Signing only {@code getUser().getRoles()} for the {@code ExtUserDetails} branch
     * leaves {@code getAuthorities()} -- a plain, non-transient, deserialization-writable field on
     * {@code WorkflowUserDetails}, the one real implementation -- completely uncovered, worse than
     * the generic {@code UserDetails} branch (which signs {@code getAuthorities()} correctly for
     * this exact object, since an {@code ExtUserDetails} satisfies that {@code instanceof} too).
     * Both fields must be covered.
     */
    @Test
    public void genuinelySignedSessionReportsFalseWhenExtUserDetailsAuthoritiesAreForgedWithUserRolesUnchanged() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        org.joget.directory.model.User user = new org.joget.directory.model.User();
        user.setUsername("user");
        user.setRoles(rolesOf("ROLE_USER"));
        java.util.List<org.springframework.security.core.GrantedAuthority> authorities =
                new java.util.ArrayList<>(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        UsernamePasswordAuthenticationToken genuine = userAuthentication();
        genuine.setDetails(new StubExtUserDetails(user, authorities));
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(genuine);
        assertTrue("sanity: the token must start out genuinely signed", token.isGenuinelySignedSession());

        // Forge ONLY the authorities list in place (same object reference the stub already
        // returns), leaving User.roles completely untouched -- the exact shape that made "v4"
        // sign the wrong value.
        authorities.clear();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        assertFalse("an ExtUserDetails' getAuthorities() was forged after signing, with "
                        + "getUser().getRoles() left byte-identical -- getCurrentRoles() reads "
                        + "getAuthorities() directly for this shape (via its own instanceof "
                        + "UserDetails branch), so the signature must cover that too, not just "
                        + "getUser().getRoles()",
                token.isGenuinelySignedSession());
    }

    private static java.util.Set<org.joget.directory.model.Role> rolesOf(String... roleIds) {
        java.util.Set<org.joget.directory.model.Role> roles = new java.util.HashSet<>();
        for (String roleId : roleIds) {
            org.joget.directory.model.Role role = new org.joget.directory.model.Role();
            role.setId(roleId);
            roles.add(role);
        }
        return roles;
    }

    @Test
    public void isTrustedAuthenticationRejectsBareAndNullTokens() {
        assertFalse(AuthenticationTokenWrapper.isTrustedAuthentication(null));
        assertFalse(AuthenticationTokenWrapper.isTrustedAuthentication(userAuthentication()));
        assertFalse(AuthenticationTokenWrapper.isTrustedAuthentication(
                new AnonymousAuthenticationToken("k", "anonymousUser",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")))));
    }

    // --- signing-shield factories ---

    /**
     * FACTORY A. A public {@link AuthenticationTokenWrapper} subclass whose constructor just calls
     * {@code super(auth)} used to shield its caller: the creation-time stack check looked for the
     * first frame whose declaring class was not exactly {@code AuthenticationTokenWrapper}, found
     * the subclass' own {@code <init>}, judged THAT (webapp, trusted) and stopped. BeanShell got a
     * validly signed wrapper for a token it assembled itself.
     *
     * <p>Driven from a real {@code bsh.Interpreter}, not from this (trusted) test class -- a call
     * made directly from the test class would run as trusted (webapp-classloader) code and never
     * exercise the untrusted-caller path this test is meant to prove is blocked.
     */
    @Test
    public void beanShellCannotMintASignedWrapperThroughASubclassFactory() throws Exception {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        bsh.Interpreter interpreter = new bsh.Interpreter();
        interpreter.set("forged", forgedAdminAuthentication());

        Object result = evalExpectingSecurityException(interpreter,
                "new org.joget.apps.workflow.security.SigningShieldSubclassWrapper(forged)",
                "constructing an AuthenticationTokenWrapper SUBCLASS from BeanShell must be "
                        + "refused -- the subclass frame is a pass-through, not the caller");

        assertFalse("even if construction were allowed, the result must not report a valid signed "
                + "binding", AuthenticationTokenWrapper.isTrustedAuthentication(
                        (org.springframework.security.core.Authentication) result));
    }

    /**
     * FACTORY B. {@code JogetSecurityContextImpl.setAuthentication(..)} is public and unguarded and
     * wraps any non-anonymous, not-already-wrapped token in a fresh
     * {@code AuthenticationTokenWrapper}. Its frame shielded its caller exactly like factory A's.
     */
    @Test
    public void beanShellCannotMintASignedWrapperThroughJogetSecurityContextImpl() throws Exception {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        bsh.Interpreter interpreter = new bsh.Interpreter();
        interpreter.set("forged", forgedAdminAuthentication());
        interpreter.eval("ctx = new org.joget.apps.util.JogetSecurityContextImpl();");

        Object result = evalExpectingSecurityException(interpreter,
                "ctx.setAuthentication(forged); ctx.getAuthentication()",
                "JogetSecurityContextImpl.setAuthentication(..) must not sign on behalf of a "
                        + "BeanShell caller");

        assertFalse("no signed wrapper may be reachable through the security context factory",
                AuthenticationTokenWrapper.isTrustedAuthentication(
                        (org.springframework.security.core.Authentication) result));
    }

    /**
     * Non-regression for the two above: making those frames transparent must not make the pass-
     * through classes themselves unusable. A trusted (webapp-classloader) originator going through
     * the very same {@code JogetSecurityContextImpl} factory still gets a signed wrapper.
     */
    @Test
    public void trustedCallerStillGetsASignedWrapperThroughJogetSecurityContextImpl() {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        org.joget.apps.util.JogetSecurityContextImpl context =
                new org.joget.apps.util.JogetSecurityContextImpl();

        context.setAuthentication(userAuthentication());

        assertTrue("a trusted caller must still be able to reach the signing constructor through "
                + "the pass-through classes", AuthenticationTokenWrapper.isTrustedAuthentication(
                        context.getAuthentication()));
    }

    /**
     * A third route: rather than shielding itself behind a known class, untrusted code defines a
     * helper of its own through a classloader it built by hand -- {@code new ClassLoader(null)}
     * that delegates the shared types manually.
     * The helper's defining loader is not named like a Felix bundle loader and has no parent to
     * walk, so a negative "does this loader look untrusted" test judged it trusted and let it reach
     * the signing constructor. A positive "is this loader in OUR delegation chain" test does not.
     */
    @Test
    public void aClassDefinedOutsideThePlatformClassloaderChainCannotSign() throws Exception {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        Class<?> shadow = defineOutsideThePlatformChain(
                "org.joget.apps.workflow.security.CustomLoaderSigningHelper");

        try {
            shadow.getMethod("sign", org.springframework.security.core.Authentication.class)
                    .invoke(shadow.getDeclaredConstructor().newInstance(), userAuthentication());
            fail("a class defined outside the platform classloader chain must not be able to "
                    + "produce a signed wrapper");
        } catch (InvocationTargetException ite) {
            assertTrue("expected a SecurityException, got " + ite.getTargetException(),
                    ite.getTargetException() instanceof SecurityException);
        }
    }

    // --- helpers ---

    /**
     * Redefines an existing class' bytecode under a null-parent classloader that hand-delegates
     * every other name to the application loader: same fully-qualified name, same shared types,
     * but a defining loader outside this class' delegation chain.
     */
    private Class<?> defineOutsideThePlatformChain(String className) throws Exception {
        String resource = className.replace('.', '/') + ".class";
        byte[] bytecode;
        try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            for (int read; (read = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, read);
            }
            bytecode = out.toByteArray();
        }
        final ClassLoader application = getClass().getClassLoader();
        ClassLoader outside = new ClassLoader(null) {
            private Class<?> defined;
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (className.equals(name)) {
                    if (defined == null) {
                        defined = defineClass(name, bytecode, 0, bytecode.length);
                    }
                    return defined;
                }
                return application.loadClass(name);
            }
        };
        return outside.loadClass(className);
    }

    /**
     * Evaluates {@code script} and requires it to fail with a {@link SecurityException}. Returns
     * null on the expected failure so the caller can also assert on "and nothing usable came back";
     * returns the script's value if it (wrongly) succeeded, so the caller's follow-up assertion
     * reports the real observable outcome rather than just the absence of an exception.
     */
    private Object evalExpectingSecurityException(bsh.Interpreter interpreter, String script,
            String message) throws Exception {
        try {
            Object value = interpreter.eval(script);
            fail(message + " -- but it succeeded and returned: " + value);
            return value;
        } catch (bsh.TargetError te) {
            Throwable target = te.getTarget();
            if (!(target instanceof SecurityException)) {
                throw new AssertionError(message + " -- failed, but with " + target, target);
            }
            return null;
        }
    }

    private UsernamePasswordAuthenticationToken forgedAdminAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                "admin",
                "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private AuthenticationTokenWrapper wrapUserAuthentication() {
        return new AuthenticationTokenWrapper(userAuthentication());
    }

    private UsernamePasswordAuthenticationToken userAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                "user",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private MockHttpServletRequest request(String serverName) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName(serverName);
        request.getSession(true);
        return request;
    }

    private byte[] serialize(Object value) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(baos)) {
            output.writeObject(value);
        }
        return baos.toByteArray();
    }

    private Object deserialize(byte[] value) throws Exception {
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(value))) {
            return input.readObject();
        }
    }

    /**
     * Minimal stand-in for {@code WorkflowUserDetails} (real class needs a Spring
     * {@code DirectoryManager} bean this bare unit test has no ApplicationContext to provide) --
     * models exactly the shape {@code canonicalizeDetails(..)} reads: a plain {@code UserDetails}
     * with an independently-settable username and authorities. See the {@code
     * genuinelySignedSessionReportsFalseWhenDetails*} tests above.
     */
    private static final class StubUserDetails
            implements org.springframework.security.core.userdetails.UserDetails {
        private final String username;
        private final java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities;

        private StubUserDetails(String username,
                java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
            this.username = username;
            this.authorities = authorities;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    /**
     * Minimal stand-in for {@code WorkflowUserDetails}: models the exact divergence this fix
     * closes -- {@code getUser()} returns the SAME embedded {@code User} instance every call (so a
     * test can mutate its roles in place, exactly like a real deserialized/tampered {@code User}),
     * while {@code getAuthorities()} is an independently-held field standing in for {@code
     * WorkflowUserDetails.getAuthorities()}'s own separate, live directory lookup -- deliberately
     * NOT derived from {@code user.getRoles()}, so a test can prove the signature must track
     * {@code getUser().getRoles()} and not this field.
     */
    private static final class StubExtUserDetails
            implements org.joget.directory.model.service.ExtUserDetails {
        private final org.joget.directory.model.User user;
        private final java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities;

        private StubExtUserDetails(org.joget.directory.model.User user,
                java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
            this.user = user;
            this.authorities = authorities;
        }

        @Override
        public org.joget.directory.model.User getUser() {
            return user;
        }

        @Override
        public void updateUser(org.joget.directory.model.User updatedUser) {
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return null;
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
