package org.joget.commons.util;

import java.io.File;
import java.net.URLDecoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogUtil {

    protected static Log getLog(String className) {
        return LogFactory.getLog(className);
    }
    
    public static void info(String className, String message) {
        Log log = getLog(className);
        
        if (log.isInfoEnabled()) {
            String clean = (message != null) ? message.replace( '\n', '_' ).replace( '\r', '_' ) : null;
            log.info(getHost() + clean);
        }
    }

    public static void debug(String className, String message) {
        Log log = getLog(className);
        
        if (log.isDebugEnabled()) {
            String clean = (message != null) ? message.replace( '\n', '_' ).replace( '\r', '_' ) : null;
            LogFactory.getLog(className).debug(getHost() + clean);
        }
    }

    public static void warn(String className, String message) {
        Log log = getLog(className);
        
        if (log.isWarnEnabled()) {
            String clean = (message != null) ? message.replace( '\n', '_' ).replace( '\r', '_' ) : null;
            log.warn(getHost() + clean);
        }
    }

    public static void error(String className, Throwable e, String message) {
        Log log = getLog(className);
        
        if (log.isErrorEnabled()) {
            if (message != null && message.trim().length() > 0) {
                String clean = message.replace( '\n', '_' ).replace( '\r', '_' );
                log.error(getHost() + clean, e);
            } else {
                log.error(getHost() + e.toString(), e);
            }
        }
    }
    
    public static Boolean isDeployInTomcat() {
        if (System.getProperty("catalina.base") != null) {
            return true;
        }
        return false;
    }
    
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
    
    public static File getTomcatLogFile(String filename) {
        String path = System.getProperty("catalina.base");
        if (path != null) {
            try {
                String pureFilename = (new File(URLDecoder.decode(filename, "UTF-8"))).getName();
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
}
