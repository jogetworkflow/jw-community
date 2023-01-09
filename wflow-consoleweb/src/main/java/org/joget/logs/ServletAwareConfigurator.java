package org.joget.logs;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;

public class ServletAwareConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpServletRequest httpservletRequest = getField(request, HttpServletRequest.class);
        String hostname = (httpservletRequest != null) ? httpservletRequest.getServerName() : null;
        HostManager.setCurrentProfile(null);
        HostManager.setCurrentHost(hostname);
        DynamicDataSourceManager.getCurrentProfile();
    }

    private static < I, F > F getField(I instance, Class < F > fieldType) {
        F field = null;
        try {
            // for tomcat (org.apache.tomcat.websocket.server.WsHandshakeRequest) and websphere
            field = (F)FieldUtils.readField(instance, "request", true);
        } catch (Exception e) {
            LogUtil.debug(ServletAwareConfigurator.class.getName(), "Cannot get request from " + instance);
        }
        if (field == null) {
            try {
                // for jboss (io.undertow.websockets.jsr.handshake.ExchangeHandshakeRequest)
                Object exchange = FieldUtils.readField(instance, "exchange", true);
                if (exchange != null) {
                    field = (F)FieldUtils.readField(exchange, "request", true);
                }
            } catch (Exception e) {
                LogUtil.debug(ServletAwareConfigurator.class.getName(), "Cannot get request from " + instance);
            }            
        }
        
        return field;
    }
}
