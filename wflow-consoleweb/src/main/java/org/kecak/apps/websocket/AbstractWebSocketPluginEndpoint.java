package org.kecak.apps.websocket;

import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.kecak.plugin.base.PluginWebSocket;
import org.springframework.context.ApplicationContext;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Testing web socket according to
 * https://www.baeldung.com/java-websockets
 */
public abstract class AbstractWebSocketPluginEndpoint {
    protected final Map<String, PluginWebSocket> sessions = new HashMap<>();

    public void onOpen(Session session, EndpointConfig config, String className) throws IOException {

        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final PluginManager pluginManager = (PluginManager) applicationContext.getBean("pluginManager");
        final WorkflowUserManager workflowUserManager = (WorkflowUserManager) applicationContext.getBean("workflowUserManager");
        final Plugin plugin = pluginManager.getPlugin(className);
        final WebSocketHttpSessionConfigurator configurator = (WebSocketHttpSessionConfigurator) ((ServerEndpointConfig)config).getConfigurator();

        workflowUserManager.setCurrentThreadUser(configurator.getCurrentUsername());
        if (!(plugin instanceof PluginWebSocket)) {
            throw new IOException("Error loading plugin");
        }
        sessions.put(session.getId(), (PluginWebSocket) plugin);

        session.getBasicRemote().sendText(((PluginWebSocket) plugin).onOpen(session.getId()));
    }

    public void onMessage(String message, Session session) {
        final String sessionId = session.getId();
        final PluginWebSocket plugin = sessions.get(sessionId);
        try {
            session.getBasicRemote().sendText(plugin.onMessage(sessionId, message));
        } catch (IOException e) {
            LogUtil.error(getClass().getName(), e, e.getMessage());
        }
    }

    public void onClose(Session session) throws IOException {
        // WebSocket connection closes
        final String sessionId = session.getId();
        final PluginWebSocket plugin = sessions.get(sessionId);

        {
            plugin.onClose(sessionId);
        }
        sessions.remove(sessionId);
    }

    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        LogUtil.error(getClass().getName(), throwable, "onError : session [" + session.getId() + "]");
    }
}
