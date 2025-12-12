package org.joget.apps.app.web;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import org.joget.logs.ServletAwareConfigurator;

/**
 * Configurator for plugin websocket endpoint to make sure httpSession is available
 */
public class WebSocketPluginConfigurator extends ServletAwareConfigurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        super.modifyHandshake(config, request, response);
        
        // Get the HttpSession from the HandshakeRequest
        HttpSession httpSession = (HttpSession) request.getHttpSession();

        if (httpSession != null) {
            // Store the HttpSession as a user property in the WebSocket session
            config.getUserProperties().put(HttpSession.class.getName(), httpSession);
        }
    }
}