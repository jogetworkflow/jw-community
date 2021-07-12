package org.joget.plugin.base;

import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.property.model.PropertyEditable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Parent abstract class of org.joget.plugin.base.ExtDefaultPlugin
 * 
 * To develop a plugin, one must extends org.joget.plugin.base.ExtDefaultPlugin 
 * instead of this class
 */
public abstract class DefaultPlugin implements Plugin, BundleActivator {

    protected ServiceRegistration registration;

    /**
     * Method used by Felix OSGI framework to register the plugin
     * 
     * @param context 
     */
    public void start(BundleContext context) {
        registration = context.registerService(getClass().getName(), this, null);
    }

    /**
     * Method used by Felix OSGI framework to unregister the plugin
     * 
     * @param context 
     */
    public void stop(BundleContext context) {
        registration.unregister();
    }
    
    /**
     * Return a plugin label for the plugin based on language setting. 
     * 
     * It will auto look for Resource Bundle Message Key "<i>plugin.className</i>.pluginLabel". 
     * If resource key not found, org.joget.plugin.property.model.PropertyEditable.getLabel() 
     * will be use if the plugin also implemented org.joget.plugin.property.model.PropertyEditable 
     * interface. Else, value from getName() method is use. OSGI plugin is required 
     * to override this method to provide an internationalization label. 
     * 
     * @return 
     */
    public String getI18nLabel() {
        String label = ResourceBundleUtil.getMessage(getClass().getName() + ".pluginLabel");
        if (label == null || label.isEmpty()) {
            if (this instanceof PropertyEditable) {
                label = ((PropertyEditable) this).getLabel();
            }
            if (label == null) {
                label = getName();
            }
            if (label == null) {
                label = "";
            }
        }
        return label;
    }
    
    /**
     * Return a plugin description for the plugin based on language setting. 
     * 
     * It will auto look for Resource Bundle Message Key "<i>plugin.className</i>.pluginDesc". 
     * If resource key not found, value from org.joget.plugin.base.Plugin.getDescription() is use. 
     * OSGI plugin is required to override this method to provide an internationalization description. 
     * 
     * @return 
     */
    public String getI18nDescription() {
        String desc = ResourceBundleUtil.getMessage(getClass().getName() + ".pluginDesc");
        if (desc == null || desc.isEmpty()) {
            desc = getDescription();
        }
        return desc;
    }
    
    /**
     * Return a plugin helplink for the plugin based on language setting. 
     * 
     * It will auto look for Resource Bundle Message Key "<i>plugin.className</i>.helplink". 
     * OSGI plugin is required to override this method to provide an internationalization help link. 
     * 
     * @return 
     */
    public String getHelpLink() {
        String helplink = ResourceBundleUtil.getMessage(getClass().getName() + ".helplink");
        return helplink;
    }
}
