package org.joget.apps.app.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.TimeZone;

import org.joget.workflow.model.service.WorkflowUserManager;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

/**
 * Regression tests for LocalLocaleResolver#determineDefaultLocale and
 * #determineDefaultTimeZone.
 *
 * Prior to the fix, both methods unconditionally called session.setAttribute()
 * with the current username on every invocation, even when the already-cached
 * value was unchanged. Under concurrent requests sharing one session (many
 * fallback-triggering @@key@@ token lookups per page render), this caused
 * ConcurrentHashMap contention on the session's attribute store. These tests
 * assert the write is skipped once the stored value already matches.
 */
public class LocalLocaleResolverTest {

    @Test
    public void determineDefaultLocaleShouldNotRewriteSessionWhenUsernameUnchanged() {
        WorkflowUserManager workflowUserManager = mock(WorkflowUserManager.class);
        when(workflowUserManager.getCurrentUsername()).thenReturn("testuser");

        LocalLocaleResolver resolver = new LocalLocaleResolver();
        resolver.setWorkflowUserManager(workflowUserManager);

        MockHttpSession realSession = new MockHttpSession();
        realSession.setAttribute(LocalLocaleResolver.LOCALE_OF_USER, "testuser");
        MockHttpSession session = spy(realSession);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        // Fast path: already-resolved locale cached on the request, so the
        // method returns without needing SetupManager at all.
        request.setAttribute(LocalLocaleResolver.DEFAULT_LOCALE_KEY, Locale.CANADA);

        clearInvocations(session);

        Locale result = resolver.determineDefaultLocale(request);

        org.junit.Assert.assertEquals(Locale.CANADA, result);
        verify(session, never()).setAttribute(eq(LocalLocaleResolver.LOCALE_OF_USER), any());
    }

    @Test
    public void determineDefaultLocaleShouldWriteSessionWhenUsernameChanged() {
        WorkflowUserManager workflowUserManager = mock(WorkflowUserManager.class);
        when(workflowUserManager.getCurrentUsername()).thenReturn("newuser");

        LocalLocaleResolver resolver = new LocalLocaleResolver();
        resolver.setWorkflowUserManager(workflowUserManager);

        MockHttpSession realSession = new MockHttpSession();
        realSession.setAttribute(LocalLocaleResolver.LOCALE_OF_USER, "olduser");
        MockHttpSession session = spy(realSession);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        request.setAttribute(LocalLocaleResolver.DEFAULT_LOCALE_KEY, Locale.CANADA);

        clearInvocations(session);

        resolver.determineDefaultLocale(request);

        verify(session).setAttribute(LocalLocaleResolver.LOCALE_OF_USER, "newuser");
    }

    @Test
    public void determineDefaultTimeZoneShouldNotRewriteSessionWhenUsernameUnchanged() {
        WorkflowUserManager workflowUserManager = mock(WorkflowUserManager.class);
        when(workflowUserManager.getCurrentUsername()).thenReturn("testuser");

        LocalLocaleResolver resolver = new LocalLocaleResolver();
        resolver.setWorkflowUserManager(workflowUserManager);

        MockHttpSession realSession = new MockHttpSession();
        realSession.setAttribute(LocalLocaleResolver.TIMEZONE_OF_USER, "testuser");
        MockHttpSession session = spy(realSession);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        // Fast path: already-resolved timezone cached as a request attribute.
        request.setAttribute(LocalLocaleResolver.SYSTEM_TIMEZONE, TimeZone.getTimeZone("America/Toronto"));

        clearInvocations(session);

        TimeZone result = resolver.determineDefaultTimeZone(request);

        org.junit.Assert.assertEquals(TimeZone.getTimeZone("America/Toronto"), result);
        verify(session, never()).setAttribute(eq(LocalLocaleResolver.TIMEZONE_OF_USER), any());
    }
}
