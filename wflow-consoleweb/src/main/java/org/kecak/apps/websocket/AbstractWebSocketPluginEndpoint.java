package org.kecak.apps.websocket;

import org.joget.apps.app.model.AppDefinition;
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
 * Abstract implementation of web socket endpoint
 */
public abstract class AbstractWebSocketPluginEndpoint {
    protected final Map<String, Map<String, Object>> sessions = new HashMap<>();

    public void onOpen(Session session, EndpointConfig config, String className) throws IOException {

        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final PluginManager pluginManager = (PluginManager) applicationContext.getBean("pluginManager");
        final WorkflowUserManager workflowUserManager = (WorkflowUserManager) applicationContext.getBean("workflowUserManager");
        final Plugin plugin = pluginManager.getPlugin(className);
        final WebSocketHttpSessionConfigurator configurator = (WebSocketHttpSessionConfigurator) ((ServerEndpointConfig)config).getConfigurator();

        final Map<String, Object> properties = new HashMap<>();
        properties.put(PluginWebSocket.PROPERTIES_SESSION, session);

        workflowUserManager.setCurrentThreadUser(configurator.getCurrentUsername());
        if (!(plugin instanceof PluginWebSocket)) {
            throw new IOException("Error loading plugin");
        }

        properties.put(PluginWebSocket.PROPERTIES_PLUGIN, plugin);

        final AppDefinition appDefinition = AppUtil.getCurrentAppDefinition();
        if(appDefinition != null) {
            properties.put(PluginWebSocket.PROPERTIES_APP_DEF, appDefinition);
        }

        properties.put(PluginWebSocket.PROPERTIES_PLUGIN_MANAGER, pluginManager);


        sessions.put(session.getId(), properties);

        session.getBasicRemote().sendText(((PluginWebSocket) plugin).onOpen(properties));
    }

    public void onMessage(String message, Session session) {
        final String sessionId = session.getId();
        final Map<String, Object> properties = sessions.get(sessionId);
        final PluginWebSocket plugin = (PluginWebSocket) properties.get(PluginWebSocket.PROPERTIES_PLUGIN);
        try {
            session.getBasicRemote().sendText(plugin.onMessage(message, properties));
        } catch (IOException e) {
            LogUtil.error(getClass().getName(), e, e.getMessage());
        }
    }

    public void onClose(Session session) throws IOException {
        // WebSocket connection closes
        final String sessionId = session.getId();
        final Map<String, Object> properties = sessions.get(sessionId);
        final PluginWebSocket plugin = (PluginWebSocket) properties.get(PluginWebSocket.PROPERTIES_PLUGIN);

        plugin.onClose(properties);
        sessions.remove(sessionId);
    }

    public void onError(Session session, Throwable throwable) {
        LogUtil.error(getClass().getName(), throwable, "onError : session [" + session.getId() + "]");
    }
}
