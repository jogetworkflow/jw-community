package org.joget.apps.app.model;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import org.joget.apps.app.web.PluginWebFilterUtil;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.base.PluginWebFilter;

/**
 * Abstract class for PluginWebFilter implementation
 */
public abstract class PluginWebFilterAbstract extends ExtDefaultPlugin implements PluginWebFilter {

    @Override
    public void init(FilterConfig fc) throws ServletException {
        //not using, added to prevent OSGI plugin build error
    }
    
    @Override
    public void destroy() {
        //not using, added to prevent OSGI plugin build error
    }

    @Override
    public void afterRegister() {
        PluginWebFilterUtil.registerFilter(this);
    }

    @Override
    public void beforeUnregister() {
        PluginWebFilterUtil.unregisterFilter(this);
    }
    
    @Override
    public boolean isPositionAfterSecurityFilter() {
        return true;
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
}
