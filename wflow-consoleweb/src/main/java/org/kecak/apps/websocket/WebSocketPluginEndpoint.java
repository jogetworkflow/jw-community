package org.kecak.apps.websocket;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * End point for websocket without mentioning application definition
 */
@ServerEndpoint(value = "/web/websocket/plugin/{className}", configurator = WebSocketHttpSessionConfigurator.class)
public class WebSocketPluginEndpoint extends AbstractWebSocketPluginEndpoint{
    @OnOpen
    public void onOpen(Session session, EndpointConfig config,
                       @PathParam("className") String className) throws IOException {
        super.onOpen(session, config, className);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        super.onMessage(message, session);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        super.onClose(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        super.onError(session, throwable);
    }
}
