package org.kecak.plugin.base;

/**
 * Interface for text based Web Socket Plugin
 */
public interface PluginWebSocket {
    default String onOpen() {
        return "";
    }

    String onMessage(String incoming);

    default void onClose() { }
}
