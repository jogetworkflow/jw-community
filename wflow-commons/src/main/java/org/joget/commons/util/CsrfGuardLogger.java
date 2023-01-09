package org.joget.commons.util;

import org.owasp.csrfguard.CsrfGuard;
import org.owasp.csrfguard.log.ILogger;
import org.owasp.csrfguard.log.LogLevel;

/**
 * Implementation of the CsrfGuard ILogger that uses LogUtil.
 */
public class CsrfGuardLogger implements ILogger {

    public void log(String msg) {
        LogUtil.info(CsrfGuard.class.getName(), msg);
    }

    public void log(LogLevel level, String msg) {
        switch (level) {
            case Trace:
                LogUtil.debug(CsrfGuard.class.getName(), msg);
                break;
            case Debug:
                LogUtil.debug(CsrfGuard.class.getName(), msg);
                break;
            case Info:
                LogUtil.info(CsrfGuard.class.getName(), msg);
                break;
            case Warning:
                LogUtil.warn(CsrfGuard.class.getName(), msg);
                break;
            case Error:
                LogUtil.error(CsrfGuard.class.getName(), null, msg);
                break;
            case Fatal:
                LogUtil.error(CsrfGuard.class.getName(), null, msg);
                break;
            default:
                throw new RuntimeException("unsupported log level " + level);
        }
    }

    public void log(Exception exception) {
        LogUtil.error(CsrfGuard.class.getName(), exception, exception.getLocalizedMessage());
    }

    public void log(LogLevel level, Exception exception) {
        switch (level) {
            case Trace:
                LogUtil.debug(CsrfGuard.class.getName(), exception.getLocalizedMessage());
                break;
            case Debug:
                LogUtil.debug(CsrfGuard.class.getName(), exception.getLocalizedMessage());
                break;
            case Info:
                LogUtil.info(CsrfGuard.class.getName(), exception.getLocalizedMessage());
                break;
            case Warning:
                LogUtil.warn(CsrfGuard.class.getName(), exception.getLocalizedMessage());
                break;
            case Error:
                LogUtil.error(CsrfGuard.class.getName(), null, exception.getLocalizedMessage());
                break;
            case Fatal:
                LogUtil.error(CsrfGuard.class.getName(), null, exception.getLocalizedMessage());
                break;
            default:
                throw new RuntimeException("unsupported log level " + level);
        }
    }

}
