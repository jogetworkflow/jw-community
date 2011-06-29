package org.joget.commons.util;

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
}
