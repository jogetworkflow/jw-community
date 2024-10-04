package org.joget.web.presence;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.joget.commons.util.LogUtil;

@WebListener
public class PresenceListener implements HttpSessionListener{

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // ignore
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        try {
            String sessionId = se.getSession().getId();
            PresenceManager.leave(null, sessionId);
        } catch(Exception e) {
            LogUtil.debug(getClass().getName(), e.getMessage());
        }
    }
}