package org.joget.plugin.base;

public class PluginException extends RuntimeException {

    public PluginException() {
    }

    public PluginException(String message) {
        super(message);
    }

    public PluginException(Throwable t) {
        super(t);
    }

    public PluginException(String message, Throwable t) {
        super(message, t);
    }
}
