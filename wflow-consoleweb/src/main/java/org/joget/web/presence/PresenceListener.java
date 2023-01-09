package org.joget.web.presence;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
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