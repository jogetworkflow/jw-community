package org.joget.plugin.base;

import java.util.Map;

/**
 * A base abstract class that must be extended by every plugins
 * 
 */
public abstract class ExtDefaultPlugin extends DefaultPlugin {
    
    /**
     * Return a set of plugin properties to configure by admin user
     * 
     * @Deprecated Since version 3, Joget introduced a better UI for plugin
     * configuration. A plugin should implement org.joget.plugin.property.model.PropertyEditable 
     * interface to provide the plugin configuration options.
     * 
     * @return 
     */
    @Deprecated
    public PluginProperty[] getPluginProperties() {
        return null;
    }
    
    /**
     * To execute a plugin
     * 
     * @Deprecated This method is only use by Process Tool plugin therefore it had
     * been moved to org.joget.plugin.base.DefaultApplicationPlugin
     * 
     * @param pluginProperties Properties to be used by the plugin during execution
     * 
     * @return
     */
    public Object execute(Map properties) {
        return null;
    }
    
    public boolean isHiddenPlugin() {
        return this instanceof HiddenPlugin;
    }
    
    public String getPluginIcon() {
        return "";
    }
}
