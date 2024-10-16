package org.joget.plugin.base;

import jakarta.websocket.Session;

/**
 * Interface for text based Web Socket Plugin
 */
public interface PluginWebSocket {

    default void onOpen(Session session) {
    }

    void onMessage(String message, Session session);

    default void onClose(Session session) {
    }
    
    default void onError(Session session, Throwable throwable) {
    }
}
