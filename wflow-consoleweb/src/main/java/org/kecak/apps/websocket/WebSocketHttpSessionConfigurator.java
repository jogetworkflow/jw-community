package org.kecak.apps.websocket;

import org.joget.workflow.util.WorkflowUtil;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class WebSocketHttpSessionConfigurator extends ServerEndpointConfig.Configurator{
    private String currentUsername = null;

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        super.modifyHandshake(config, request, response);
        currentUsername = WorkflowUtil.getCurrentUsername();
    }

    public String getCurrentUsername() {
        return currentUsername;
    }
}
