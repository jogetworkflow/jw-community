package org.joget.apps.app.web;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

/**
 * Configurator for plugin websocket endpoint to make sure httpSession is available
 */
public class WebSocketPluginConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        // Get the HttpSession from the HandshakeRequest
        HttpSession httpSession = (HttpSession) request.getHttpSession();

        // Store the HttpSession as a user property in the WebSocket session
        config.getUserProperties().put(HttpSession.class.getName(), httpSession);
    }
}