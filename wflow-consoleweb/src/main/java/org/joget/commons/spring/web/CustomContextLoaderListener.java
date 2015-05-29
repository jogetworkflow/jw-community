package org.joget.commons.spring.web;

import javax.servlet.ServletContextEvent;
import org.joget.commons.util.LogUtil;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * Overrides Spring's ContextLoaderListener to support re-initialization of the 
 * ApplicationContext if previous attempts fail.
 */
public class CustomContextLoaderListener extends ContextLoaderListener {

    public CustomContextLoaderListener() {
    }

    public CustomContextLoaderListener(WebApplicationContext context) {
        super(context);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        super.contextDestroyed(event);
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            LogUtil.info(getClass().getName(), "===== Initializing WebApplicationContext =====");
            super.contextInitialized(event);
        } catch(Exception e) {
            Exception exceptionToLog = (e instanceof BeanCreationException) ? null : e;
            LogUtil.error(getClass().getName(), exceptionToLog, "===== Error initializing WebApplicationContext =====");
            contextDestroyed(event);
        }
    }
    
}
