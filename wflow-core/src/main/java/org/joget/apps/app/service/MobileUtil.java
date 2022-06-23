package org.joget.apps.app.service;

import javax.servlet.http.HttpServletRequest;
import org.joget.workflow.util.WorkflowUtil;

/**
 * Utility classes for mobile related features.
 */
public class MobileUtil {

    public static final String MOBILE_VIEW = "_mobileView_";
    
    private MobileUtil(){
    }

    private static Boolean mobileDisabled;
    
    /**
     * Flag to indicate the mobile support is disabled by system  
     * @return 
     */
    public static boolean isMobileDisabled() {
        return mobileDisabled != null && mobileDisabled;
    }
    
    /**
     * Sets to disable the mobile support
     * @param disabled 
     */
    public void setDisableMobile(boolean disabled) {
        // only allow setting once
        if (mobileDisabled == null) {
            mobileDisabled = disabled;
        }
    }
    
    /**
     * Checks whether the current thread is being called from a mobile browser
     * @return 
     */
    public static boolean isMobileUserAgent() {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        return isMobileUserAgent(request);
    }
    
    /**
     * Checks whether the current request is being called from a mobile browser
     * @return 
     */
    public static boolean isMobileUserAgent(HttpServletRequest request) {
        boolean isMobile = false;
        if (request != null) {
            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null) {
                // acording to https://developer.mozilla.org/en-US/docs/Web/HTTP/Browser_detection_using_the_user_agent 
                isMobile = userAgent.contains("Mobi");
            }
        }
        return isMobile;
    }
    
    /**
     * Checks whether the current request is being called from a IOS browser
     * @return 
     */
    public static boolean isIOS() {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        boolean isIOS = false;
        if (request != null) {
            String userAgent = request.getHeader("User-Agent");
            if (userAgent != null) {
                isIOS = userAgent.contains("Mobi") && (userAgent.contains("iPhone") || userAgent.contains("iPad") || userAgent.contains("iPod") || userAgent.contains("Mac"));
            }
        }
        return isIOS;
    }
    
    /**
     * Sets a flag to indicate whether or not the current request is a mobile view.
     * @param request 
     */
    public static void setMobileView(HttpServletRequest request, Boolean mobileView) {
        request.setAttribute(MOBILE_VIEW, Boolean.TRUE);
    }
    
    /**
     * Checks whether the current request is a mobile view
     * @return 
     */
    public static boolean isMobileView() {
        boolean isMobileView = false;
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            isMobileView = Boolean.TRUE.equals(request.getAttribute(MOBILE_VIEW));
        }
        return isMobileView;
    }
    
    public static boolean isIE() {
        String userAgent = WorkflowUtil.getHttpServletRequest().getHeader("User-Agent");
        return (userAgent.contains("MSIE") || userAgent.contains("Trident"));
    }
}
