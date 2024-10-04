package org.joget.commons.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ServerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServerUtil.registerServer();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}