package org.joget.commons.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ServerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServerUtil.registerServer();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServerUtil.unregisterServer();
    }
}