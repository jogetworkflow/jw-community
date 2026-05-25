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

    @Test
    public void serializedTokenRejectsAfterSigningKeyChange() throws Exception {
        AuthenticationTokenWrapper.bindLoginHost("tenant-a.example.test");
        AuthenticationTokenWrapper token = new AuthenticationTokenWrapper(userAuthentication());
        byte[] serialized = serialize(token);

        System.setProperty(AuthenticationTokenWrapper.HOST_SESSION_SIGNING_KEY_PROPERTY, "different-test-host-session-signing-key");
        AuthenticationTokenWrapper.resetHostSessionSigningKeyForTest();

        AuthenticationTokenWrapper restored = (AuthenticationTokenWrapper) deserialize(serialized);
        HostManager.setCurrentHost("tenant-a.example.test");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = AuthenticationTokenWrapper.validateHost(request("tenant-a.example.test"), response, restored);

        assertFalse(result);
        assertEquals(403, response.getStatus());
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

    // --- helpers ---

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
}
