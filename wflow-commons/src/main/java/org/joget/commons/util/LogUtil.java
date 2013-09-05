package org.joget.commons.util;

import java.io.File;
import java.net.URLDecoder;
import org.apache.commons.logging.LogFactory;

public class LogUtil {

    public static void info(String className, String message) {
        LogFactory.getLog(className).info(message);
    }

    public static void debug(String className, String message) {
        LogFactory.getLog(className).debug(message);
    }

    public static void warn(String className, String message) {
        LogFactory.getLog(className).warn(message);
    }

    public static void error(String className, Throwable e, String message) {
        if (message != null && message.trim().length() > 0) {
            LogFactory.getLog(className).error(message, e);
        } else {
            LogFactory.getLog(className).error(e.toString(), e);
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
}
