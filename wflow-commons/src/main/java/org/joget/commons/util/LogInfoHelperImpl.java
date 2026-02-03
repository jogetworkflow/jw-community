package org.joget.commons.util;

public class LogInfoHelperImpl implements LogInfoHelper {

    @Override
    public String prepareAdditionalLogMessage(String className, String level, String message) {
        return message;
    }
}
