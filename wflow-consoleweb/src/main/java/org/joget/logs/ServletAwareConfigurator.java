package org.joget.logs;

import java.lang.reflect.Field;
import javax.servlet.http.HttpServletRequest;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.HostManager;

public class ServletAwareConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpServletRequest httpservletRequest = getField(request, HttpServletRequest.class);
        String hostname = httpservletRequest.getServerName();
        HostManager.setCurrentProfile(null);
        HostManager.setCurrentHost(hostname);
        DynamicDataSourceManager.getCurrentProfile();
    }

    private static < I, F > F getField(I instance, Class < F > fieldType) {
        try {
            for (Class < ? > type = instance.getClass(); type != Object.class; type = type.getSuperclass()) {
                for (Field field: type.getDeclaredFields()) {
                    if (fieldType.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        return (F) field.get(instance);
                    }
                }
            }
        } catch (Exception e) {}
        return null;
    }
}
