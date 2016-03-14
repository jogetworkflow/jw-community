package org.joget.designer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent event) {
        Thread myThread = Thread.currentThread();
        ClassLoader ccl = myThread.getContextClassLoader(); // PUSH 
        myThread.setContextClassLoader(ClassLoader.getSystemClassLoader());

        java.awt.Toolkit.getDefaultToolkit();
        
        myThread.setContextClassLoader(ccl); // POP 
    }

    public void contextDestroyed(ServletContextEvent event) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            try {
                if (thread.getName().startsWith("AWT-")) {
                    if (!thread.isInterrupted()) {
                        thread.interrupt();
                    }
                }
            } catch (Exception ex) {
            }
        }
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            try {
                if (thread.getName().startsWith("AWT-")) {
                    if (!thread.isInterrupted()) {
                        thread.interrupt();
                    }
                }
            } catch (Exception ex) {
            }
        }
    }
}
