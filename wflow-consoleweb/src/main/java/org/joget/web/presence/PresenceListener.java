package org.joget.web.presence;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class PresenceListener implements HttpSessionListener, ServletContextListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // ignore
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        String sessionId = se.getSession().getId();
        PresenceManager.leave(null, sessionId);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        PresenceManager.registerServer();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        PresenceManager.unregisterServer();
    }
 
}