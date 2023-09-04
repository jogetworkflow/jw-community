package org.joget.apps.app.model;

import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.joget.plugin.property.model.PropertyEditable;


public interface CreateAppOption extends PropertyEditable {
    
    /**
     * Get the icon for the option
     * 
     * @return 
     */
    public String getPluginIcon();
    
    /**
     * Whether this option should appear in the create app page
     * 
     * @return 
     */
    public Boolean isAvailable();
    
    /**
     * Create app definition 
     * 
     * @return error message
     */
    public Collection<String> createAppDefinition(String appId, String appName, HttpServletRequest request);
}
