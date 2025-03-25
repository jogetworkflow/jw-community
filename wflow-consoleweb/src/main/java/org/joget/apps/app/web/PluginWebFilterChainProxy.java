package org.joget.apps.app.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
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
