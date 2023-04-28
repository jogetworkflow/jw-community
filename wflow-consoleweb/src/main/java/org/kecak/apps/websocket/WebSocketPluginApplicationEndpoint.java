package org.kecak.apps.websocket;

import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.springframework.context.ApplicationContext;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * Testing web socket according to
 * https://www.baeldung.com/java-websockets
 */
@ServerEndpoint(value = "/web/websocket/app/{appId}/{appVersion}/plugin/{className}", configurator = WebSocketHttpSessionConfigurator.class)
public class WebSocketPluginApplicationEndpoint extends AbstractWebSocketPluginEndpoint {
    @OnOpen
    public void onOpen(Session session, EndpointConfig config,
                       @PathParam("appId") String appId, @PathParam("appVersion") Long appVersion, @PathParam("className") String className) throws IOException {

        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final AppDefinitionDao appDefinitionDao = (AppDefinitionDao) applicationContext.getBean("appDefinitionDao");

        final AppDefinition appDefinition;
        if (appVersion == null) {
            appDefinition = appDefinitionDao.getPublishedAppDefinition(appId);
        } else {
            appDefinition = appDefinitionDao.loadVersion(appId, appVersion);
        }

        if (appDefinition != null) {
            AppUtil.setCurrentAppDefinition(appDefinition);
        }

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
