package org.kecak.apps.logs;

import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.kecak.plugin.base.PluginWebSocket;
import org.springframework.context.ApplicationContext;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Testing web socket according to
 * https://www.baeldung.com/java-websockets
 */
@ServerEndpoint(value = "/web/websocket/plugin/{className}")
public class WebSocketPluginEndpoint {
    private Map<String, PluginWebSocket> sessions = new HashMap<>();

    @OnOpen
    public void onOpen(Session session,
                       @PathParam("className") String className) throws IOException {

        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final PluginManager pluginManager = (PluginManager) applicationContext.getBean("pluginManager");
        final Plugin plugin = pluginManager.getPlugin(className);

        if (!(plugin instanceof PluginWebSocket)) {
            throw new IOException("Error loading plugin");
        }
        sessions.put(session.getId(), (PluginWebSocket) plugin);

        session.getBasicRemote().sendText(((PluginWebSocket) plugin).onOpen(session.getId()));
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        final String sessionId = session.getId();
        final PluginWebSocket plugin = sessions.get(sessionId);
        try {
            session.getBasicRemote().sendText(plugin.onMessage(sessionId, message));
        } catch (IOException e) {
            LogUtil.error(getClass().getName(), e, e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        // WebSocket connection closes
        final String sessionId = session.getId();
        final PluginWebSocket plugin = sessions.get(sessionId);

        {
            plugin.onClose(sessionId);
        }
        sessions.remove(sessionId);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        LogUtil.error(getClass().getName(), throwable, "onError : session [" + session.getId() + "]");
    }
}
