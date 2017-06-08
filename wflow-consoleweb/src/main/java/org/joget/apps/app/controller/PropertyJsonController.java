package org.joget.apps.app.controller;

import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.joget.apps.app.dao.AppResourceDao;
import org.joget.apps.app.dao.PluginDefaultPropertiesDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.AppResource;
import org.joget.apps.app.model.PluginDefaultProperties;
import org.joget.apps.app.service.AppResourceUtil;
import org.joget.apps.app.service.AppService;
import org.joget.commons.util.FileLimitException;
import org.joget.commons.util.FileStore;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
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
    public void getElements(Writer writer, @RequestParam("classname") String className, @RequestParam(value = "exclude", required = false) String exclude) throws Exception {
        JSONArray jsonArray = new JSONArray();
        
        Collection<String> excludeList = new ArrayList<String>();
        if (exclude != null && !exclude.isEmpty()) {
            excludeList.addAll(Arrays.asList(exclude.split(";")));
        }
        
        try {
            // get available elements from the plugin manager
            Collection<Plugin> elementList = pluginManager.list(Class.forName(className));
            Map<String, String> empty = new HashMap<String, String>();
            empty.put("value", "");
            empty.put("label", "");
            jsonArray.put(empty);

            for (Plugin p : elementList) {
                String pClassName = ClassUtils.getUserClass(p).getName();
                if (!(p instanceof HiddenPlugin || excludeList.contains(pClassName))) {
                    Map<String, String> option = new HashMap<String, String>();
                    option.put("value", pClassName);
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
                        AppResource appResource = appResourceDao.loadById(file.getOriginalFilename(), appDef);
                        if (appResource != null) { //replace
                            appResource.setFilesize(file.getSize());
                            appResourceDao.update(appResource);
                        } else {
                            appResource = new AppResource();
                            appResource.setAppDefinition(appDef);
                            appResource.setAppId(appDef.getAppId());
                            appResource.setAppVersion(appDef.getVersion());
                            appResource.setId(file.getOriginalFilename());
                            appResource.setFilesize(file.getSize());
                            
                            if (isPublic != null && isPublic) {
                                appResource.setPermissionClass("");
                                appResource.setPermissionProperties("{\"permission\": { \"className\": \"\", \"properties\": {}}}");
                            } else {
                                appResource.setPermissionClass("org.joget.apps.userview.lib.LoggedInUserPermission");
                                appResource.setPermissionProperties("{\"permission\": { \"className\": \"org.joget.apps.userview.lib.LoggedInUserPermission\", \"properties\": {}}}");
                            }
                            appResourceDao.add(appResource);
                        }
                        
                        AppResourceUtil.storeFile(appId, version, file);
                        
                        String filename = appResource.getId();
                        try {
                            filename = URLEncoder.encode(filename, "UTF-8");
                        } catch (Exception e){}
            
                        obj.put("value", appResource.getId());
                        obj.put("permission", appResource.getPermissionClass());
                        obj.put("url", request.getContextPath() + "/web/app/" + appId + "/" + appDef.getVersion() + "/resources/" + filename);
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
}
