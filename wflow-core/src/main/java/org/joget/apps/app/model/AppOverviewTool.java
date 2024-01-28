package org.joget.apps.app.model;

import org.joget.plugin.base.Plugin;
import org.joget.plugin.property.model.PropertyEditable;
import org.json.JSONObject;

public interface AppOverviewTool extends Plugin, PropertyEditable {
    
    /**
     * Scan the app builder elements 
     * @param key
     * @param path
     * @param pluginClassName
     * @param properties
     * @param propertiesString
     * @param parentObject 
     * @param data 
     */
    public void scan(String key, String path, String pluginClassName, JSONObject properties, String propertiesString, JSONObject parentObject, AppOverviewData data);
    
    /**
     * Get font icon to render in advance tools
     * 
     * @return 
     */
    public String getIcon();
}
