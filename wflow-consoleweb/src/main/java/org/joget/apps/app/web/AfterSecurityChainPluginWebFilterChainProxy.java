package org.joget.apps.app.web;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * A filter act as a proxy to all the registered plugin web filters. This filter place after security chain
 */
public class AfterSecurityChainPluginWebFilterChainProxy implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        PluginWebFilterChain pluginWebFilterChain = PluginWebFilterUtil.getPluginFilterChainProxy(true);
        if (pluginWebFilterChain != null && !pluginWebFilterChain.isEmpty()) {
            pluginWebFilterChain.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }
    
}