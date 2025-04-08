package org.joget.apps.app.web;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.joget.commons.util.HostManager;

/**
 * A filter act as a proxy to all the registered plugin web filters. This filter has the order of first filter
 */
public class PluginWebFilterChainProxy implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest != null) {
            // reset profile and set hostname
            HostManager.initHost();
        }
        
        PluginWebFilterChain pluginWebFilterChain = PluginWebFilterUtil.getPluginFilterChainProxy(false);
        if (pluginWebFilterChain != null && !pluginWebFilterChain.isEmpty()) {
            pluginWebFilterChain.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }
    
}
