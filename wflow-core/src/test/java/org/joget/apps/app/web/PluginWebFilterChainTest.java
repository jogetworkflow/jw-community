package org.joget.apps.app.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.joget.plugin.base.PluginWebFilter;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Regression tests for PluginWebFilterChain#getFilters(HttpServletRequest).
 *
 * Prior to the fix, getFilters() returned on the first UrlPatternFiltersHolder
 * whose pattern matched the request, silently discarding every other holder
 * that also matched. These tests register two filters under overlapping URL
 * patterns and assert that both run for a request matching the overlap.
 */
public class PluginWebFilterChainTest {

    @Test
    public void allFiltersWithOverlappingMatchingPatternsShouldRun() throws IOException, ServletException {
        List<String> executionOrder = new ArrayList<String>();
        RecordingFilter webFilter = new RecordingFilter("webFilter", new String[]{"/web/**"}, 1, executionOrder);
        RecordingFilter shareFilter = new RecordingFilter("shareFilter", new String[]{"/web/joom/share/**"}, 2, executionOrder);

        PluginWebFilterChain filterChain = new PluginWebFilterChain();
        // registered in an order that would previously cause the "shareFilter"
        // bucket to be favoured by HashMap iteration, to prove ordering no
        // longer determines which bucket "wins"
        filterChain.addFilter(shareFilter);
        filterChain.addFilter(webFilter);

        MockHttpServletRequest request = newRequest("/web/joom/share/abc");
        MockHttpServletResponse response = new MockHttpServletResponse();
        TerminalFilterChain terminalChain = new TerminalFilterChain();

        filterChain.doFilter(request, response, terminalChain);

        assertEquals(Arrays.asList("webFilter", "shareFilter"), executionOrder);
        assertTrue("request should still reach the terminal chain", terminalChain.invoked);
    }

    @Test
    public void filterRegisteredUnderMultipleMatchingPatternsShouldRunOnlyOnce() throws IOException, ServletException {
        List<String> executionOrder = new ArrayList<String>();
        RecordingFilter dualPatternFilter = new RecordingFilter(
                "dualPatternFilter", new String[]{"/web/**", "/web/joom/**"}, 1, executionOrder);

        PluginWebFilterChain filterChain = new PluginWebFilterChain();
        filterChain.addFilter(dualPatternFilter);

        MockHttpServletRequest request = newRequest("/web/joom/share/abc");
        MockHttpServletResponse response = new MockHttpServletResponse();
        TerminalFilterChain terminalChain = new TerminalFilterChain();

        filterChain.doFilter(request, response, terminalChain);

        assertEquals(Arrays.asList("dualPatternFilter"), executionOrder);
        assertTrue(terminalChain.invoked);
    }

    @Test
    public void requestNotMatchingAnyPatternShouldSkipAllFilters() throws IOException, ServletException {
        List<String> executionOrder = new ArrayList<String>();
        RecordingFilter webFilter = new RecordingFilter("webFilter", new String[]{"/web/**"}, 1, executionOrder);

        PluginWebFilterChain filterChain = new PluginWebFilterChain();
        filterChain.addFilter(webFilter);

        MockHttpServletRequest request = newRequest("/other/path");
        MockHttpServletResponse response = new MockHttpServletResponse();
        TerminalFilterChain terminalChain = new TerminalFilterChain();

        filterChain.doFilter(request, response, terminalChain);

        assertTrue(executionOrder.isEmpty());
        assertTrue(terminalChain.invoked);
    }

    private static MockHttpServletRequest newRequest(String servletPath) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", servletPath);
        request.setServletPath(servletPath);
        return request;
    }

    private static final class RecordingFilter implements PluginWebFilter {
        private final String name;
        private final String[] urlPatterns;
        private final int order;
        private final List<String> executionOrder;

        RecordingFilter(String name, String[] urlPatterns, int order, List<String> executionOrder) {
            this.name = name;
            this.urlPatterns = urlPatterns;
            this.order = order;
            this.executionOrder = executionOrder;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String[] getUrlPatterns() {
            return urlPatterns;
        }

        @Override
        public boolean isPositionAfterSecurityFilter() {
            return false;
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            executionOrder.add(name);
            chain.doFilter(request, response);
        }

        @Override
        public void destroy() {
        }

        @Override
        public void afterRegister() {
        }

        @Override
        public void beforeUnregister() {
        }
    }

    private static final class TerminalFilterChain implements FilterChain {
        private boolean invoked = false;

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            invoked = true;
        }
    }
}
