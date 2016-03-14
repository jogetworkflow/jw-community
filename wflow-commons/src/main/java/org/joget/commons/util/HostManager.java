package org.joget.commons.util;

import java.io.File;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

@Service
public class HostManager implements ApplicationContextAware {

    public static final String SYSTEM_PROPERTY_VIRTUALHOST = "wflow.virtualhost";

    protected static final ThreadLocal currentHost = new ThreadLocal();
    protected static final ThreadLocal currentProfile = new ThreadLocal();
    protected static String contextPath;

    public static void setCurrentHost(String hostname) {
        currentHost.set(hostname);
    }

    public static String getCurrentHost() {
        if (isVirtualHostEnabled()) {
            String hostname = (String)currentHost.get();
            return hostname;
        }
        else {
            return null;
        }
    }

    public static void setCurrentProfile(String profile) {
        currentProfile.set(profile);
        setCurrentHost(null);
    }

    public static String getCurrentProfile() {
        String profile = (String) currentProfile.get();
        return profile;
    }

    public static boolean isVirtualHostEnabled() {
        boolean enabled = Boolean.valueOf(System.getProperty(SYSTEM_PROPERTY_VIRTUALHOST));
        return enabled;
    }

    public static String getContextPath() {
        return contextPath;
    }

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
