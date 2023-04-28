package org.kecak.apps.websocket;

import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.kecak.plugin.base.PluginWebSocket;
import org.springframework.context.ApplicationContext;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Testing web socket according to
 * https://www.baeldung.com/java-websockets
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
