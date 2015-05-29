package org.joget.apps.app.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.joget.commons.util.LogUtil;

public class ContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent event) {
    }

    public void contextDestroyed(ServletContextEvent event) {
        try {
            ThreadCleaner cleaner = new ThreadCleaner();
            cleaner.cleanThreadLocals();
            cleaner.cleanThreads();
        } catch (Exception ex) {
            LogUtil.error(ContextListener.class.getName(), ex, "");
        }
    }
}
