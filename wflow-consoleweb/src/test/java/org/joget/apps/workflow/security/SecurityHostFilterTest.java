package org.joget.apps.workflow.security;

import java.util.Collections;
import org.joget.commons.util.HostManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import static org.junit.Assert.assertEquals;

public class SecurityHostFilterTest {

    private String virtualHostProperty;

    @Before
    public void setUp() {
        virtualHostProperty = System.getProperty(HostManager.SYSTEM_PROPERTY_VIRTUALHOST);
        System.setProperty(HostManager.SYSTEM_PROPERTY_VIRTUALHOST, "true");
        HostManager.setCurrentProfile(null);
    }

    @After
    public void tearDown() {
        HostManager.setCurrentProfile(null);
        if (virtualHostProperty == null) {
            System.clearProperty(HostManager.SYSTEM_PROPERTY_VIRTUALHOST);
        } else {
            System.setProperty(HostManager.SYSTEM_PROPERTY_VIRTUALHOST, virtualHostProperty);
        }
    }

    @Test
    public void rejectsMismatchedSessionHostBeforeSecurityHostFilterDoFilter() throws Exception {
        HostManager.setCurrentProfile("tenant-a");
        AuthenticationTokenWrapper authentication = wrapUserAuthentication();

        HostManager.setCurrentProfile("tenant-b");
        
        MockHttpServletRequest request = requestWithSessionAuthentication("tenant-b.example.test", authentication);
        MockHttpServletResponse response = new MockHttpServletResponse();

        new SecurityHostFilter().doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
    }

    @Test
    public void allowsMatchingSessionHostAndRunsPreSecurityHostFilter() throws Exception {
        HostManager.setCurrentProfile("tenant-b");
        AuthenticationTokenWrapper authentication = wrapUserAuthentication();

        MockHttpServletRequest request = requestWithSessionAuthentication("tenant-b.example.test", authentication);
        MockHttpServletResponse response = new MockHttpServletResponse();

        new SecurityHostFilter().doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
    }

    private AuthenticationTokenWrapper wrapUserAuthentication() {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        return new AuthenticationTokenWrapper(authentication);
    }

    private MockHttpServletRequest requestWithSessionAuthentication(String serverName, AuthenticationTokenWrapper authentication) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setServerName(serverName);
        request.setRequestURI("/web/test");

        SecurityContextImpl securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authentication);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        return request;
    }
}
