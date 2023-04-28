package org.kecak.plugin.base;

import java.util.Map;

/**
 * Interface for text based Web Socket Plugin
 */
public interface PluginWebSocket {
    String PROPERTIES_APP_DEF = "appDef";
    String PROPERTIES_SESSION = "session";

    String PROPERTIES_PLUGIN = "plugin";

    String PROPERTIES_PLUGIN_MANAGER = "pluginManager";

    default String onOpen(Map<String, Object> properties) {
        return "";
    }

    String onMessage(String incoming, Map<String, Object> properties);

    default void onClose(Map<String, Object> properties) { }
}
