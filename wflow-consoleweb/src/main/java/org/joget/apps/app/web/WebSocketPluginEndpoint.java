package org.joget.apps.app.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSocket;
import org.springframework.context.ApplicationContext;

/**
 * Endpoint for plugin WebSocket
 */
@ServerEndpoint(value = "/web/socket/plugin/{className}")
public class WebSocketPluginEndpoint {

    private Map<String, PluginWebSocket> sessions = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("className") String className) throws IOException {

        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final PluginManager pluginManager = (PluginManager) applicationContext.getBean("pluginManager");
        final Plugin plugin = pluginManager.getPlugin(className);

        if (!(plugin instanceof PluginWebSocket)) {
            throw new IOException("Error loading plugin");
        }
        sessions.put(session.getId(), (PluginWebSocket) plugin);

        ((PluginWebSocket) plugin).onOpen(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        final String sessionId = session.getId();
        final PluginWebSocket plugin = sessions.get(sessionId);
        plugin.onMessage(message, session);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        // WebSocket connection closes
        final String sessionId = session.getId();
        final PluginWebSocket plugin = sessions.get(sessionId);
        plugin.onClose(session);
        sessions.remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // WebSocket connection closes
        final String sessionId = session.getId();
        final PluginWebSocket plugin = sessions.get(sessionId);
        plugin.onError(session, throwable);
        sessions.remove(session.getId());
        
        // Do error handling here
        LogUtil.error(getClass().getName(), throwable, "onError : session [" + session.getId() + "]");
    }
}
