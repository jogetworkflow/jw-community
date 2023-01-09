package org.joget.plugin.base;

import java.util.Map;

/**
 * Basic interface of a plugin 
 * 
 */
public interface Plugin {

    /**
     * Return a unique name for the plugin. You can override a existing System plugin by providing the same name as the System plugin.
     * 
     * @return 
     */
    String getName();
    
    /**
     * Return a label for the plugin based on language setting 
     * 
     * @return 
     */
    String getI18nLabel();

    /**
     * Return plugin version 
     * 
     * @return 
     */
    String getVersion();

    /**
     * Return a plugin description for the plugin based on language setting 
     * 
     * @return 
     */
    String getI18nDescription();
    
    /**
     * Return a plugin description. This value will be used when a Resource 
     * Bundle Message Key "plugin.className.pluginDesc" is not found by getI18nDescription method. 
     * 
     * @return 
     */
    String getDescription();
    
    /**
     * Return a plugin help link. 
     * 
     * @return 
     */
    String getHelpLink();

    /**
     * Return a set of plugin properties to configure by admin user
     * 
     * @Deprecated Since version 3, Joget introduced a better UI for plugin
     * configuration. A plugin should implement org.joget.plugin.property.model.PropertyEditable 
     * interface to provide the plugin configuration options.
     * 
     * @return 
     */
    PluginProperty[] getPluginProperties();

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
    Object execute(Map properties);
}
