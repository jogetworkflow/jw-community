package org.joget.apps.app.controller;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.dao.AppResourceDao;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AppResource;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppResourceUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.userview.model.PwaOfflineNotSupported;
import org.joget.apps.userview.model.PwaOfflineReadonly;
import org.joget.apps.userview.model.PwaOfflineValidation;
import org.joget.apps.userview.model.PwaOfflineValidation.WARNING_TYPE;
import org.joget.commons.util.FileLimitException;
import org.joget.commons.util.FileStore;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.base.CustomPluginInterface;
import org.joget.plugin.base.HiddenPlugin;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class PropertyJsonController {

    @Autowired
    PluginManager pluginManager;
    
    @Autowired
    AppService appService;
    
    @Autowired
    PluginDefaultPropertiesDao pluginDefaultPropertiesDao;
    
    @Resource
    AppResourceDao appResourceDao;

    @RequestMapping("/property/json/getElements")
    public void getElements(Writer writer, @RequestParam("classname") String className, @RequestParam(value = "exclude", required = false) String exclude, @RequestParam(value = "includeHidden", required = false) Boolean includeHidden, @RequestParam(value = "pwaValidation", required = false) Boolean pwaValidation) throws Exception {
        JSONArray jsonArray = new JSONArray();
        
        if (includeHidden == null) {
            includeHidden = false;
        }
        if (pwaValidation == null) {
            pwaValidation = false;
        }
        
        Collection<String> excludeList = new ArrayList<String>();
        if (exclude != null && !exclude.isEmpty()) {
            excludeList.addAll(Arrays.asList(exclude.split(";")));
        }
        
        try {
            // get available elements from the plugin manager
            Class clazz;
            CustomPluginInterface cpi = pluginManager.getCustomPluginInterface(className);
            if (cpi != null) {
                clazz = cpi.getClassObj();
            } else {
                clazz = Class.forName(className);
            }
            
            Collection<Plugin> elementList = pluginManager.list(clazz);
            Map<String, String> empty = new HashMap<String, String>();
            empty.put("value", "");
            empty.put("label", "");
            jsonArray.put(empty);

            for (Plugin p : elementList) {
                String pClassName = ClassUtils.getUserClass(p).getName();
                if (!((!includeHidden && p instanceof HiddenPlugin) || excludeList.contains(pClassName))) {
                    Map<String, String> option = new HashMap<String, String>();
                    option.put("value", pClassName);
                    option.put("label", p.getI18nLabel());
                    if (pwaValidation) {
                        if (p instanceof PwaOfflineValidation) {
                            option.put("pwaValidation", "checking");
                        } else if (p instanceof PwaOfflineReadonly) {
                            option.put("pwaValidation", "readonly");
                        } else if (p instanceof PwaOfflineNotSupported) {
                            option.put("pwaValidation", "notSupported");
                        } else {
                            option.put("pwaValidation", "supported");
                        }
                    }
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
            json = PropertyUtil.injectHelpLink(((Plugin) element).getHelpLink(), element.getPropertyOptions());
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
            json = PropertyUtil.injectHelpLink(((Plugin) element).getHelpLink(), element.getPropertyOptions());
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
    
    @RequestMapping("/property/json/(*:appId)/(~:version)/getAppResources")
    public void getAppResources(HttpServletRequest request, Writer writer, @RequestParam(value = "appId", required = true) String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback) throws IOException {
        try {
            JSONArray jsonArray = new JSONArray();
            if (appId != null && !appId.trim().isEmpty()) {
                AppDefinition appDef = appService.getAppDefinition(appId, version);

                Collection<AppResource> resources = appDef.getResourceList();
                for (AppResource r : resources) {
                    Map<String, String> option = new HashMap<String, String>();
                    option.put("value", r.getId());
                    option.put("permission", r.getPermissionClass());
                    
                    String filename = r.getId();
                    try {
                        filename = URLEncoder.encode(filename, "UTF-8");
                    } catch (Exception e){}
            
                    option.put("url", request.getContextPath() + "/web/app/" + appId + "/" + appDef.getVersion() + "/resources/" + filename);
                    jsonArray.put(option);
                }
            }
            jsonArray.write(writer);
        } catch (Exception ex) {
            LogUtil.error(this.getClass().getName(), ex, "getAppResources Error!");
        }
    }
    
    @RequestMapping(value = "/property/json/(*:appId)/(~:version)/appResourceUpload", method = RequestMethod.POST)
    public void appResourcesUpload(HttpServletRequest request, Writer writer, @RequestParam(value = "appId", required = true) String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "isPublic", required = false) Boolean isPublic) throws IOException, JSONException {
        JSONObject obj = new JSONObject();
        try {
            MultipartFile file = null;
            if (appId != null && !appId.trim().isEmpty()) {
                AppDefinition appDef = appService.getAppDefinition(appId, version);
                try {
                    file = FileStore.getFile("app_resource");
                    if (file != null && file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty()) {
                        AppResource appResource = AppResourceUtil.storeFile(appDef, file, isPublic);
            
                        obj.put("value", appResource.getId());
                        obj.put("permission", appResource.getPermissionClass());
                        obj.put("url", request.getContextPath() + "/web/app/" + appId + "/" + appDef.getVersion() + "/resources/" + appResource.getId());
                    }
                } catch (FileLimitException e) {
                    obj.put("error", ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
                }
            }
        } catch (Exception ex) {
            obj.put("error", ex.getLocalizedMessage());
        } finally {
            FileStore.clear();
        }
        obj.write(writer);
    }
    
    @RequestMapping("/property/json/(*:appId)/(~:version)/pwaValidation")
    public void pwaValidation(Writer writer, @RequestParam(value = "appId", required = true) String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("className") String className, @RequestParam("properties") String properties) throws Exception {
        JSONArray arr = new JSONArray();
        if (appId != null && !appId.trim().isEmpty()) {
            AppDefinition appDef = appService.getAppDefinition(appId, version);
            Plugin plugin = pluginManager.getPlugin(className);
            if (plugin != null && plugin instanceof PwaOfflineValidation) {
                ((PropertyEditable) plugin).setProperties(PropertyUtil.getPropertiesValueFromJson(properties));
                Map<WARNING_TYPE, String[]> warnings = ((PwaOfflineValidation) plugin).validation();
                if (warnings != null) {
                    for (Entry<WARNING_TYPE, String[]> e : warnings.entrySet()) {
                        JSONObject err = new JSONObject();
                        err.put("type", e.getKey().toString());
                        err.put("messages", e.getValue());
                        arr.put(err);
                    }
                }
            }
        }
        arr.write(writer);
    }
}
