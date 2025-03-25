package org.joget.plugin.base;

import javax.servlet.http.HttpServletRequest;

public interface UiHtmlInjectorPlugin {
    
    /**
     * Method returning a unique identifier of the injector
     * @return 
     */
    public String getName();
    
    /**
     * Method returning a URL patters array to inject HTML. Using AntPathMatcher syntax
     * @return 
     */
    public String[] getInjectUrlPatterns();
    
    /**
     * Method returning the HTML to inject to page footer
     * @param request
     * @return 
     */
    public String getHtml(HttpServletRequest request);
    
    /**
     * A flag to tell the HTML need to re-injecting when AJAX theme page switching
     * @return 
     */
    public boolean isIncludeForAjaxThemePageSwitching();
}
