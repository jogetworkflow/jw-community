package org.joget.plugin.base;

/**
 * Plugin implemented this interface will be call during plugin register and unregister
 * 
 * The plugin instance used to call afterRegister and beforeUnregister will not be the same 
 * object. This is just use to trigger the events and it is stateless. 
 * Please use static variable or other implementation to store plugin instance if
 * same plugin instance are needed.
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
