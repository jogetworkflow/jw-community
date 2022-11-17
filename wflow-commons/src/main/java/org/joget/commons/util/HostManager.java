package org.joget.commons.util;

import java.io.File;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility methods used by the system to manage cloud profile
 * 
 */
@Service
public class HostManager implements ApplicationContextAware {

    public static final String SYSTEM_PROPERTY_VIRTUALHOST = "wflow.virtualhost";
    public static final String REQUEST_HOST_INITIALIZED = "REQUEST_HOST_INITIALIZED";

    protected static final ThreadLocal currentHost = new ThreadLocal();
    protected static final ThreadLocal currentProfile = new InheritableThreadLocal();
    protected static final ThreadLocal previousProfile = new ThreadLocal();
    protected static String contextPath;

    /**
     * Sets the Host of current HTTP request.
     * This method is security protected in Cloud installation.
     * @param hostname 
     */
    public static void setCurrentHost(String hostname) {
        currentHost.set(hostname);
    }

    /**
     * Gets the current Host of HTTP request
     * @return 
     */
    public static String getCurrentHost() {
        if (isVirtualHostEnabled()) {
            String hostname = (String)currentHost.get();
            return hostname;
        }
        else {
            return null;
        }
    }

    /**
     * Sets the profile of current HTTP request.
     * This method is security protected in Cloud installation.
     * @param profile 
     */
    public static void setCurrentProfile(String profile) {
        profile = SecurityUtil.validateStringInput(profile);
        if (profile == null) {
            previousProfile.set(null);
        } else {
            String previous = (String)previousProfile.get();
            if (previous == null) {
                String current = getCurrentProfile();
                if (current != null) {
                    previousProfile.set(current);
                }
            }
        }
        currentProfile.set(profile);
        currentHost.set(null);
    }

    /**
     * Gets the current cloud profile of HTTP request
     * @return 
     */
    public static String getCurrentProfile() {
        String profile = (String) currentProfile.get();
        return profile;
    }

    /**
     * Reset the profile of the current HTTP request.
     * @param profile 
     */
    public static void resetProfile() {
        String previous = (String)previousProfile.get();
        if (previous != null) {
            currentProfile.set(previous);
            previousProfile.set(null);
            currentHost.set(null);
        }
    }
    
    /**
     * Initials the host for the current request
     */
    public static void initHost() {
        // reset profile and set hostname
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            if (request != null && !"true".equals(request.getAttribute(REQUEST_HOST_INITIALIZED))) {
                HostManager.setCurrentProfile(null);
                String hostname = request.getServerName();
                HostManager.setCurrentHost(hostname);
                request.setAttribute(REQUEST_HOST_INITIALIZED, "true");
            }
        } catch (Exception e) {
            // ignore if servlet request is not available
        }
    }

    /**
     * Flag to indicate it is a Cloud installation
     * @return 
     */
    public static boolean isVirtualHostEnabled() {
        boolean enabled = Boolean.valueOf(System.getProperty(SYSTEM_PROPERTY_VIRTUALHOST));
        return enabled;
    }

    /**
     * Gets the context path of the HTTP request
     * @return 
     */
    public static String getContextPath() {
        return contextPath;
    }

    /**
     * Method used by system to set Application Context. 
     * This method is security protected in Cloud installation.
     * @param appContext
     * @throws BeansException 
     */
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        if (appContext instanceof WebApplicationContext) {
            String realContextPath = ((WebApplicationContext)appContext).getServletContext().getRealPath("/");
            String cPath = "/jw";
            if (realContextPath != null) {
                File contextPathFile = new File(realContextPath);
                cPath = contextPathFile.getName();
                if (!cPath.startsWith("/")) {
                    cPath = "/" + cPath;
                }
            }
            HostManager.contextPath = cPath;
        }
    }

}
