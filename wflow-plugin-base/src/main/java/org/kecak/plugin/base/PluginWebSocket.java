package org.kecak.plugin.base;

/**
 * Interface for text based Web Socket Plugin
 */
public interface PluginWebSocket {
    default String onOpen(String sessionId) {
        return "";
    }

    String onMessage(String sessionId, String incoming);

    default void onClose(String sessionId) { }
}
