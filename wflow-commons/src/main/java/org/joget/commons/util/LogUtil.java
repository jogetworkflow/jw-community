package org.joget.commons.util;

import java.io.File;
import java.net.URLDecoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * Utility methods to log message to log file
 * 
 */
public class LogUtil {
    
    /**
     * System property to specify a custom class name for the Log implementation.
     */
    public static final String SYSTEM_PROPERTY_LOGGER = "wflow.logger";
    private static String logClassName = null;
    
    /**
     * Return the Log implementation specified by the wflow.logger system property. Defaults to commons-logging LogFactory.getLog(className).
     * @param className
     * @return
     */
    protected static Log getLog(String className) {
        Log logger = null;
        String loggerClassName = System.getProperty(SYSTEM_PROPERTY_LOGGER);
        if (loggerClassName != null && !loggerClassName.isEmpty()) {
            try {
                logger = (Log)Class.forName(loggerClassName).getDeclaredConstructor(String.class).newInstance(className);
                if (logClassName == null) {
                    logClassName = loggerClassName;
                    System.out.println("LogUtil Log implementation: " + logger.getClass().getName());
                }
            } catch(Exception e) {
                // ignore
            }
        }
        if (logger == null) {
            logger = LogFactory.getLog(className);
        }
        return logger;
    }
    
    /**
     * Log message with logger level is info
     * @param className
     * @param message 
     */
    public static void info(String className, String message) {
        message = getLogInfoHelper().prepareAdditionalLogMessage(className, "INFO", message);
        Log log = getLog(className);
        
        String clean = (message != null) ? message.replace( '\n', '_' ).replace( '\r', '_' ) : null;
        log.info(getHost() + clean);
    }

    /**
     * Log message with logger level is debug
     * @param className
     * @param message 
     */
    public static void debug(String className, String message) {
        message = getLogInfoHelper().prepareAdditionalLogMessage(className, "DEBUG", message);
        Log log = getLog(className);
        
        String clean = (message != null) ? message.replace( '\n', '_' ).replace( '\r', '_' ) : null;
        log.debug(getHost() + clean);
    }

    /**
     * Log message with logger level is warn
     * @param className
     * @param message 
     */
    public static void warn(String className, String message) {
        message = getLogInfoHelper().prepareAdditionalLogMessage(className, "WARN", message);
        Log log = getLog(className);
        
        String clean = (message != null) ? message.replace( '\n', '_' ).replace( '\r', '_' ) : null;
        log.warn(getHost() + clean);
    }

    /**
     * Log exception message with logger level is error
     * @param className
     * @param message 
     */
    public static void error(String className, Throwable e, String message) {
        message = getLogInfoHelper().prepareAdditionalLogMessage(className, "ERROR", message);
        Log log = getLog(className);
        
        if (message != null && message.trim().length() > 0) {
            String clean = message.replace( '\n', '_' ).replace( '\r', '_' );
            log.error(getHost() + clean, e);
        } else {
            log.error(getHost() + e.toString(), e);
        }
    }
    
    /**
     * Check is the info log is enabled
     * @param className
     * @return 
     */
    public static boolean isInfoEnabled(String className) {
        Log log = getLog(className);
        return log.isInfoEnabled();
    }
    
    /**
     * Check is the debug log is enabled
     * @param className
     * @return 
     */
    public static boolean isDebugEnabled(String className) {
        Log log = getLog(className);
        return log.isDebugEnabled();
    }
    
    /**
     * Check is the warn log is enabled
     * @param className
     * @return 
     */
    public static boolean isWarnEnabled(String className) {
        Log log = getLog(className);
        return log.isWarnEnabled();
    }
    
    /**
     * Check is the error log is enabled
     * @param className
     * @return 
     */
    public static boolean isErrorEnabled(String className) {
        Log log = getLog(className);
        return log.isErrorEnabled();
    }
    
    /**
     * Check is the current installation is deploy in Tomcat server
     * @return 
     */
    public static Boolean isDeployInTomcat() {
        if (System.getProperty("catalina.base") != null) {
            return true;
        }
        return false;
    }
    
    /**
     * Convenient method to retrieve all tomcat log files 
     * @return 
     */
    public static File[] tomcatLogFiles() {
        // Directory path here
        String path = System.getProperty("catalina.base"); 
        if (path != null) {
            File folder = new File(System.getProperty("catalina.base"), "logs");
            File[] listOfFiles = folder.listFiles(); 
            return listOfFiles;
        } 
        return null;
    }
    
    /**
     * Convenient method to retrieve all tomcat log file by file name 
     * @return 
     */
    public static File getTomcatLogFile(String filename) {
        // validate input
        String normalizedFileName = SecurityUtil.normalizedFileName(filename);
        
        String path = System.getProperty("catalina.base");
        if (path != null) {
            try {
                String pureFilename = (new File(URLDecoder.decode(normalizedFileName, "UTF-8"))).getName();
                String logPath =  path + File.separator + "logs";
                File file = new File(logPath, pureFilename);
                if (file.exists() && !file.isDirectory()) {
                    return file;
                }
            } catch (Exception e) {}
        }
        return null;
    }
    
    protected static String getHost() {
        if (HostManager.isVirtualHostEnabled()) {
            return HostManager.getCurrentProfile() + " : ";
        }
        return "";
    }
    
    protected static LogInfoHelper getLogInfoHelper() {
        LogInfoHelper logInfoHelper = null;
        
        ApplicationContext ac = SecurityUtil.getApplicationContext();
        if (ac != null) {
            logInfoHelper = (LogInfoHelper) ac.getBean("logInfoHelper");
        }
        if (logInfoHelper == null) {
            logInfoHelper = new LogInfoHelperImpl();
        }
        return logInfoHelper;
    }
}
