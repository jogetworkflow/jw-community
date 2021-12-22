package org.joget.commons.util;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Thread implementation to used by plugin
 */
public final class PluginThread extends Thread {
    
    private final String profile;
    private final HttpServletRequest request;
    
    public PluginThread(Runnable r) {
        super(r);
        profile = DynamicDataSourceManager.getCurrentProfile();
        ServletRequestAttributes sra = null;
        try {
            sra = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes());
        } catch (IllegalStateException e) {
        }
        if (sra != null) {
            request = sra.getRequest();
        } else {
            request = null;
        }
    }
    
    private void setProfile() {
        HostManager.setCurrentProfile(profile);
    }
    
    @Override
    public void run() {
        setProfile();
        
        if (request != null) {
            RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        }
        
        super.run();
        
        if (request != null) {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
