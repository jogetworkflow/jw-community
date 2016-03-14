package org.joget.apps.app.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppService;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.HiddenPlugin;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PropertyJsonController {

    @Autowired
    PluginManager pluginManager;
    
    @Autowired
    AppService appService;
    
    @Autowired
    PluginDefaultPropertiesDao pluginDefaultPropertiesDao;

    @RequestMapping("/property/json/getElements")
    public void getElements(Writer writer, @RequestParam("classname") String className) throws Exception {
        JSONArray jsonArray = new JSONArray();

        try {
            // get available elements from the plugin manager
            Collection<Plugin> elementList = pluginManager.list(Class.forName(className));
            Map<String, String> empty = new HashMap<String, String>();
            empty.put("value", "");
            empty.put("label", "");
            jsonArray.put(empty);

            for (Plugin p : elementList) {
                if (!(p instanceof HiddenPlugin)) {
                    PropertyEditable element = (PropertyEditable) p;
                    Map<String, String> option = new HashMap<String, String>();
                    option.put("value", element.getClassName());
                    option.put("label", p.getI18nLabel());
                    jsonArray.put(option);
                }
            }
        } catch (Exception ex) {
            LogUtil.error(this.getClass().getName(), ex, "getElements Error!");
        }

        jsonArray.write(writer);
    }

    @RequestMapping("/property/json/getPropertyOptions")
    public void getProperties(Writer writer, @RequestParam("value") String value) throws Exception {
        String json = "";
        PropertyEditable element = (PropertyEditable) pluginManager.getPlugin(value);
        if (element != null) {
            json = element.getPropertyOptions();
        }

        writer.write(json);
    }
    
    @RequestMapping("/property/json/(*:appId)/(~:version)/getPropertyOptions")
    public void getProperties(Writer writer, @RequestParam(value = "appId", required = true) String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("value") String value, @RequestParam(value = "callback", required = false) String callback) throws IOException {
        if (appId != null && !appId.trim().isEmpty()) {
            appService.getAppDefinition(appId, version);
        }

        String json = "";
        PropertyEditable element = (PropertyEditable) pluginManager.getPlugin(value);
        if (element != null) {
            json = element.getPropertyOptions();
        }

        writer.write(json);        
    }
    
    @RequestMapping("/property/json/(*:appId)/(~:version)/getDefaultProperties")
    public void getDefaultProperties(Writer writer, @RequestParam(value = "appId", required = true) String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("value") String value, @RequestParam(value = "callback", required = false) String callback) throws IOException {
        String json = "";
        if (appId != null && !appId.trim().isEmpty()) {
            AppDefinition appDef = appService.getAppDefinition(appId, version);
            
            Plugin plugin = pluginManager.getPlugin(value);
            if (plugin != null) {
                    PluginDefaultProperties pluginDefaultProperties = pluginDefaultPropertiesDao.loadById(value, appDef);

                    if (pluginDefaultProperties != null) {
                        json = pluginDefaultProperties.getPluginProperties();
                        json = PropertyUtil.propertiesJsonLoadProcessing(json);
                    }
            }
        }
        writer.write(json);        
    }
}
