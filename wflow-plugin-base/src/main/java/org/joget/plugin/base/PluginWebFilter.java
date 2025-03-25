package org.joget.plugin.base;

import javax.servlet.Filter;

/**
 * Interface for adding a web filter to filter chain
 */
public interface PluginWebFilter extends Filter, ActivationAwarePlugin {
    
    /**
     * Method returning the name of the filter
     * @return 
     */
    public String getName();
    
    /**
     * Method returning a URL patters array to apply the filter.
     * Using AntPathMatcher syntax
     * @return 
     */
    public String[] getUrlPatterns();
    
    /**
     * The filter is execute after security chain
     * @return 
     */
    public boolean isPositionAfterSecurityFilter();
    
    /**
     * The order of plugin web filter execution
     * @return 
     */
    public int getOrder();
}
