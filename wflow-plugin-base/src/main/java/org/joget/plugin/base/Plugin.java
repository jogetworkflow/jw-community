package org.joget.plugin.base;

import java.util.Map;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.property.model.PropertyEditable;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Basic interface of a plugin 
 * 
 */
public interface Plugin extends PropertyEditable {

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
    @Deprecated
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
    @Deprecated
    Object execute(Map properties);
    
    /**
     * Return plugin label. This value will be used when a Resource Bundle 
     * Message Key "<i>plugin.className</i>.pluginlabel" is not found by getI18nLabel() method.
     * 
     * Default implementation added for old plugin that still using getPluginProperties method to provide plugin
     * configuration options and didn't implement PropertyEditable
     * 
     * @return
     */
    @Override
    default public String getLabel() {
        return getName();
    }
    
    /**
     * Return the plugin properties options in JSON format.
     * 
     * Default implementation added for old plugin that still using getPluginProperties method to provide plugin
     * configuration options and didn't implement PropertyEditable
     * 
     * @return
     */
    @Override
    default public String getPropertyOptions() {
        
        try {
            PluginProperty[] properties = getPluginProperties();

            if (properties != null && properties.length > 0) {
                JSONArray propsArr = new JSONArray();
                for (PluginProperty p : properties) {
                    JSONObject pObj = new JSONObject();
                    pObj.put("name", p.getName());
                    pObj.put("label", p.getLabel());
                    pObj.put("type", p.getType());
                    pObj.put("value", p.getValue());
                    
                    String[] options = p.getOptions();
                    if (options != null) {
                        JSONArray optionsArr = new JSONArray();
                        for (String o : options) {
                            JSONObject oObj = new JSONObject();
                            oObj.put("value", o);
                            oObj.put("label", o);
                            optionsArr.put(oObj);
                        }
                        pObj.put("options", optionsArr);
                    }
                    propsArr.put(pObj);
                }   
                JSONObject pageObj = new JSONObject();
                pageObj.put("title", getLabel());
                pageObj.put("properties", propsArr);
                
                JSONArray propetiesArr = new JSONArray();
                propetiesArr.put(pageObj);
                
                return propetiesArr.toString();
            }
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "fail to backward compatible this plugin");
        }
        
        return "";
    }
}
