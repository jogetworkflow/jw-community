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

import org.joget.commons.util.SetupManager;
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

    @Test
    public void parseLocaleAcceptsWellFormedCodes() {
        org.junit.Assert.assertEquals(new Locale("ar"), LocalLocaleResolver.parseLocale("ar"));
        org.junit.Assert.assertEquals(new Locale("en", "US"), LocalLocaleResolver.parseLocale("en_US"));
        org.junit.Assert.assertEquals(new Locale("zh", "CN"), LocalLocaleResolver.parseLocale("zh_CN"));
        org.junit.Assert.assertEquals(new Locale("th", "TH", "TH"), LocalLocaleResolver.parseLocale("th_TH_TH"));
    }

    @Test
    public void parseLocaleRejectsScriptBreakoutPayload() {
        // #2348: single-quote break-out out of UI.locale = '<value>';
        org.junit.Assert.assertNull(LocalLocaleResolver.parseLocale("ar';alert(1); //x"));
    }

    @Test
    public void parseLocaleRejectsAttributeBreakoutPayload() {
        // #2348: double-quote break-out out of <html lang="<value>">
        org.junit.Assert.assertNull(LocalLocaleResolver.parseLocale("ar\" data-x=\"y"));
    }

    @Test
    public void parseLocaleRejectsMalformedCodes() {
        org.junit.Assert.assertNull(LocalLocaleResolver.parseLocale(null));
        org.junit.Assert.assertNull(LocalLocaleResolver.parseLocale(""));
        // 4-subtag extended locale string stays unparseable, as before the fix
        org.junit.Assert.assertNull(LocalLocaleResolver.parseLocale("ja_JP_JP_#u-ca-japanese"));
    }

    @Test
    public void parseLocalePreservesScriptVariantForms() {
        // JVM script/extension forms (from Locale.getAvailableLocales().toString(),
        // offered by the systemLocale picker) must still be accepted - the '#' and
        // '-' they contain are harmless in the sink contexts.
        org.junit.Assert.assertEquals(new Locale("th", "TH", "#Thai"),
                LocalLocaleResolver.parseLocale("th_TH_#Thai"));
        org.junit.Assert.assertEquals(new Locale("sr", "RS", "#Cyrl"),
                LocalLocaleResolver.parseLocale("sr_RS_#Cyrl"));
        // ...but a quote in the variant is still rejected
        org.junit.Assert.assertNull(LocalLocaleResolver.parseLocale("th_TH_#Thai';alert(1)//"));
    }

    @Test
    public void parseLocaleAcceptsEmptyCountrySubtag() {
        // Locale.toString() leaves the country empty for script-only and
        // country-less variant locales, and the locale pickers offer these codes.
        org.junit.Assert.assertEquals(new Locale("zh", "", "#Hans"), LocalLocaleResolver.parseLocale("zh__#Hans"));
        org.junit.Assert.assertEquals(new Locale("sr", "", "#Latn"), LocalLocaleResolver.parseLocale("sr__#Latn"));
        org.junit.Assert.assertEquals(new Locale("be", "", "TARASK"), LocalLocaleResolver.parseLocale("be__TARASK"));
        // trailing empty subtags collapse, as split("_") always did
        org.junit.Assert.assertEquals(new Locale("ar"), LocalLocaleResolver.parseLocale("ar_"));
        org.junit.Assert.assertEquals(new Locale("ar"), LocalLocaleResolver.parseLocale("ar__"));
        // ...but break-out characters and a fourth subtag are still rejected
        org.junit.Assert.assertNull(LocalLocaleResolver.parseLocale("zh__#Hans';alert(1)//"));
        org.junit.Assert.assertNull(LocalLocaleResolver.parseLocale("ar__\" data-x=\"y"));
        org.junit.Assert.assertNull(LocalLocaleResolver.parseLocale("ar___"));
    }

    @Test
    public void parseLocaleAcceptsEveryPickerLocale() {
        // Every code the locale pickers offer (Locale.getAvailableLocales()) must
        // parse back to the same locale. The 4-subtag extension forms
        // (ja_JP_JP_#u-ca-japanese, th_TH_TH_#u-nu-thai) were never supported.
        for (Locale l : Locale.getAvailableLocales()) {
            String code = l.toString();
            if (code.isEmpty() || code.split("_").length > 3) {
                continue;
            }
            Locale parsed = LocalLocaleResolver.parseLocale(code);
            org.junit.Assert.assertNotNull(code, parsed);
            org.junit.Assert.assertEquals(code, parsed.toString());
        }
    }

    @Test
    public void resolveLocaleDoesNotPersistMaliciousLangParam() {
        WorkflowUserManager workflowUserManager = mock(WorkflowUserManager.class);
        when(workflowUserManager.getCurrentUsername()).thenReturn("testuser");
        when(workflowUserManager.isCurrentUserAnonymous()).thenReturn(true);

        SetupManager setupManager = mock(SetupManager.class);

        LocalLocaleResolver resolver = new LocalLocaleResolver();
        resolver.setWorkflowUserManager(workflowUserManager);
        resolver.setSetupManager(setupManager);

        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        request.setParameter(LocalLocaleResolver.PARAM_NAME, "ar';alert(1); //x");

        Locale result = resolver.resolveLocale(request);

        // malicious value must not survive as the resolved locale nor in the session
        org.junit.Assert.assertEquals(LocalLocaleResolver.DEFAULT, result);
        org.junit.Assert.assertNull(session.getAttribute(
                LocalLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME));
    }
}
