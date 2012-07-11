package org.joget.plugin.base;

import java.util.Map;

public interface Plugin {

    String getName();

    String getVersion();

    String getDescription();

    PluginProperty[] getPluginProperties();

    /**
     * 
     * @param pluginProperties Properties to be used by the plugin during execution
     * @return
     */
    Object execute(Map properties);
}
