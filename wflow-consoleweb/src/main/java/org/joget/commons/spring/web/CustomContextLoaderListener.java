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
            System.out.println("===== Initializing WebApplicationContext =====");            
            LogUtil.info(getClass().getName(), "===== Initializing WebApplicationContext =====");
            super.contextInitialized(event);
        } catch(Exception e) {
            if (LogUtil.isDebugEnabled(getClass().getName())) { //if debug then log everything
                LogUtil.error(getClass().getName(), e, "===== Error initializing WebApplicationContext =====");
            } else {
                Exception exceptionToLog = (e instanceof BeanCreationException) ? null : e;
                if (exceptionToLog != null) {
                    LogUtil.error(getClass().getName(), exceptionToLog, "===== Error initializing WebApplicationContext =====");
                    exceptionToLog.printStackTrace();
                } else {
                    Throwable cause = e;
                    while (cause.getCause() != null) {
                        cause = cause.getCause();
                    }
                    
                    System.out.println("===== Error initializing WebApplicationContext =====");
                    if (cause.getMessage().contains("hibernate.dialect")) {
                        System.out.println("ERROR : Fail to connect database");
                    } else {
                        System.out.println("ERROR : " + cause.getMessage());
                    }
                }
            }
            contextDestroyed(event);
        }
    }
    
}
