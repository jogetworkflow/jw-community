package org.joget.plugin.base;

/**
 * Plugin implemented this interface will be call during plugin register and unregister
 */
public interface ActivationAwarePlugin {
    
    /**
     * This method will be called after the plugin is registered to the service
     */
    public void afterRegister();
    
    /**
     * This method will be called before the plugin is going to unregister from the service
     */
    public void beforeUnregister();
}
