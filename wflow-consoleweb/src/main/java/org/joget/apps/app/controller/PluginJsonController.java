package org.joget.apps.app.controller;

import java.io.IOException;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.HashVariablePlugin;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.ApplicationPlugin;
import org.joget.plugin.base.AuditTrailPlugin;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.DeadlinePlugin;
import org.joget.workflow.model.ParticipantPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PluginJsonController {

    @Autowired
    private PluginManager pluginManager;
    @Autowired
    private AppService appService;

    @RequestMapping("/json/plugin/listDefault")
    public void pluginListDefault(Writer writer, @RequestParam(value = "className", required = false) String className, @RequestParam(value = "name", required = false) String filter, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException {
        List<Plugin> pluginList = null;

        try {
            if (className != null && !className.trim().isEmpty()) {
                Class clazz;
                if (pluginManager.getCustomPluginInterface(className) != null) {
                    clazz = pluginManager.getCustomPluginInterface(className).getClassObj();
                } else {
                    clazz = Class.forName(className);
                }
                pluginList = (List) pluginManager.list(clazz);
            } else {
                pluginList = new ArrayList<Plugin>();

                Collection<Plugin> fullPluginList = pluginManager.list();

                for (Plugin plugin : fullPluginList) {
                    if (plugin instanceof AuditTrailPlugin || plugin instanceof DeadlinePlugin || plugin instanceof ParticipantPlugin || plugin instanceof ApplicationPlugin) {
                        pluginList.add(plugin);
                    }
                }
            }
            
            //sort with plugin label
            Collections.sort(pluginList, new Comparator<Plugin>() {

                public int compare(Plugin o1, Plugin o2) {
                    if (o1.getI18nLabel() == null || o1.getI18nLabel().isEmpty() || o2.getI18nLabel() == null || o2.getI18nLabel().isEmpty()) {
                        return 0;
                    }
                    return o1.getI18nLabel().compareTo(o2.getI18nLabel());
                }
            });

            JSONObject jsonObject = new JSONObject();
            int counter = 0;

            Map<String, String> pluginType = PluginManager.getPluginType();
            for (Plugin plugin : pluginList) {
                if (plugin.getI18nLabel() == null || plugin.getI18nLabel().isEmpty()) {
                    continue;
                }
                if (filter != null && !filter.isEmpty() && !plugin.getI18nLabel().toLowerCase().contains(filter.toLowerCase())) {
                    continue;
                }
                
                if (counter >= start && counter < start + rows) {
                    Map data = new HashMap();
                    data.put("id", ClassUtils.getUserClass(plugin).getName());
                    data.put("name", plugin.getI18nLabel());
                    data.put("description", plugin.getI18nDescription());
                    data.put("version", plugin.getVersion());
                    
                    String type = "";
                    for (String c : pluginType.keySet()) {
                        Class clazz;
                        if (pluginManager.getCustomPluginInterface(c) != null) {
                            clazz = pluginManager.getCustomPluginInterface(c).getClassObj();
                        } else {
                            clazz = Class.forName(c);
                        }
                        if (clazz.isInstance(plugin)) {
                            if (!type.isEmpty()) {
                                type += ", ";
                            }
                            type += pluginType.get(c);
                        }
                    }
                    data.put("plugintype", type);
                    
                    jsonObject.accumulate("data", data);
                }
                counter++;
            }

            jsonObject.accumulate("total", counter);
            jsonObject.accumulate("start", start);
            jsonObject.write(writer);
        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "");
        }
    }

    @RequestMapping("/json/plugin/list")
    public void pluginList(Writer writer, @RequestParam(value = "className", required = false) String className, @RequestParam(value = "name", required = false) String filter, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException {
        List<Plugin> pluginList = null;

        try {
            if (className != null && !className.trim().isEmpty()) {
                Class clazz;
                if (pluginManager.getCustomPluginInterface(className) != null) {
                    clazz = pluginManager.getCustomPluginInterface(className).getClassObj();
                } else {
                    clazz = Class.forName(className);
                }
                pluginList = (List) pluginManager.list(clazz);
            } else {
                pluginList = (List) pluginManager.list();
            }
            
            //sort with plugin label
            Collections.sort(pluginList, new Comparator<Plugin>() {

                public int compare(Plugin o1, Plugin o2) {
                    if (o1.getI18nLabel() == null || o1.getI18nLabel().isEmpty() || o2.getI18nLabel() == null || o2.getI18nLabel().isEmpty()) {
                        return 0;
                    }
                    return o1.getI18nLabel().compareTo(o2.getI18nLabel());
                }
            });

            JSONObject jsonObject = new JSONObject();
            int counter = 0;
            
            Map<String, String> pluginType = PluginManager.getPluginType();
            for (Plugin plugin : pluginList) {
                if (plugin.getI18nLabel() == null || plugin.getI18nLabel().isEmpty()) {
                    continue;
                }
                if (filter != null && !filter.isEmpty() && !plugin.getI18nLabel().toLowerCase().contains(filter.toLowerCase())) {
                    continue;
                }
                
                if (counter >= start && counter < start + rows) {
                    Map data = new HashMap();
                    data.put("id", ClassUtils.getUserClass(plugin).getName());
                    data.put("name", plugin.getI18nLabel());
                    data.put("description", plugin.getI18nDescription());
                    data.put("version", plugin.getVersion());
                    
                    String type = "";
                    for (String c : pluginType.keySet()) {
                        Class clazz;
                        if (pluginManager.getCustomPluginInterface(c) != null) {
                            clazz = pluginManager.getCustomPluginInterface(c).getClassObj();
                        } else {
                            clazz = Class.forName(c);
                        }
                        
                        if (clazz.isInstance(plugin)) {
                            if (!type.isEmpty()) {
                                type += ", ";
                            }
                            type += pluginType.get(c);
                        }
                    }
                    data.put("plugintype", type);
                    data.put("uninstallable", (pluginManager.isOsgi(data.get("id").toString())) ? "<span class=\"tick\"></span>" : "");
                    
                    jsonObject.accumulate("data", data);
                }
                counter++;
            }

            jsonObject.accumulate("total", counter);
            jsonObject.accumulate("start", start);
            jsonObject.write(writer);
        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "");
        }
    }

    @RequestMapping("/json/plugin/listOsgi")
    public void pluginListOsgi(Writer writer, @RequestParam(value = "className", required = false) String className, @RequestParam(value = "name", required = false) String filter, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws JSONException {
        List<Plugin> pluginList = null;

        try {
            if (className != null && !className.trim().isEmpty()) {
                Class clazz;
                if (pluginManager.getCustomPluginInterface(className) != null) {
                    clazz = pluginManager.getCustomPluginInterface(className).getClassObj();
                } else {
                    clazz = Class.forName(className);
                }
                pluginList = new ArrayList(pluginManager.listOsgiPlugin(clazz));
            } else {
                pluginList = new ArrayList(pluginManager.listOsgiPlugin(null));
            }
            
            //sort with plugin label
            Collections.sort(pluginList, new Comparator<Plugin>() {

                public int compare(Plugin o1, Plugin o2) {
                    if (o1.getI18nLabel() == null || o1.getI18nLabel().isEmpty() || o2.getI18nLabel() == null || o2.getI18nLabel().isEmpty()) {
                        return 0;
                    }
                    return o1.getI18nLabel().compareTo(o2.getI18nLabel());
                }
            });
            
            JSONObject jsonObject = new JSONObject();
            int counter = 0;

            Map<String, String> pluginType = PluginManager.getPluginType();
            for (Plugin plugin : pluginList) {
                if (plugin.getI18nLabel() == null || plugin.getI18nLabel().isEmpty()) {
                    continue;
                }
                if (filter != null && !filter.isEmpty() && !plugin.getI18nLabel().toLowerCase().contains(filter.toLowerCase())) {
                    continue;
                }
                
                if (counter >= start && counter < start + rows) {
                    Map data = new HashMap();
                    data.put("id", ClassUtils.getUserClass(plugin).getName());
                    data.put("name", plugin.getI18nLabel());
                    data.put("description", plugin.getI18nDescription());
                    data.put("version", plugin.getVersion());
                    
                    String type = "";
                    for (String c : pluginType.keySet()) {
                        Class clazz;
                        if (pluginManager.getCustomPluginInterface(c) != null) {
                            clazz = pluginManager.getCustomPluginInterface(c).getClassObj();
                        } else {
                            clazz = Class.forName(c);
                        }
                        if (clazz.isInstance(plugin)) {
                            if (!type.isEmpty()) {
                                type += ", ";
                            }
                            type += pluginType.get(c);
                        }
                    }
                    data.put("plugintype", type);
                    data.put("uninstallable", (pluginManager.isOsgi(data.get("id").toString())) ? "<span class=\"tick\"></span>" : "");
                    
                    jsonObject.accumulate("data", data);
                }
                counter++;
            }

            jsonObject.accumulate("total", counter);
            jsonObject.accumulate("start", start);
            jsonObject.write(writer);
        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "");
        }
    }

    @RequestMapping("/json/app/(*:appId)/(~:appVersion)/plugin/(*:pluginName)/service")
    public void service(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "appVersion", required = false) String appVersion, @RequestParam String pluginName) throws IOException, ServletException {
        AppDefinition appDef = appService.getAppDefinition(appId, appVersion);
        AppUtil.setCurrentAppDefinition(appDef);
        boolean found = false;
        if (pluginName != null && !pluginName.isEmpty()) {
            Plugin plugin = pluginManager.getPlugin(pluginName);
            if (plugin != null && plugin instanceof PluginWebSupport) {
                found = true;
                PluginWebSupport pluginWeb = (PluginWebSupport) plugin;
                pluginWeb.webService(request, response);
            }
        }
        if (!found) {
            // send 404 not found
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @RequestMapping("/json/plugin/(*:pluginName)/service")
    public void pluginService(HttpServletRequest request, HttpServletResponse response, @RequestParam String pluginName) throws IOException, ServletException {
        boolean found = false;
        if (pluginName != null && !pluginName.isEmpty()) {
            Plugin plugin = pluginManager.getPlugin(pluginName);
            if (plugin != null && plugin instanceof PluginWebSupport) {
                found = true;
                PluginWebSupport pluginWeb = (PluginWebSupport) plugin;
                pluginWeb.webService(request, response);
            }
        }
        if (!found) {
            // send 404 not found
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    @RequestMapping("/json/hash/options")
    public void HashVariableOptions(Writer writer) throws JSONException {
        try {
            Collection<Plugin> pluginList = pluginManager.list(HashVariablePlugin.class);
            
            JSONArray jsonArray = new JSONArray();
            
            List<String> syntaxs = new ArrayList<String> (); 
            for (Plugin p : pluginList) {
                HashVariablePlugin hashVariablePlugin = (HashVariablePlugin) p;
                if (hashVariablePlugin.availableSyntax() != null) {
                    syntaxs.addAll(hashVariablePlugin.availableSyntax());
                }
            }
            Collections.sort(syntaxs);
            for (String key : syntaxs) {
                jsonArray.put(key);
            }
            jsonArray.write(writer);
        } catch (Exception e) {
            LogUtil.error(this.getClass().getName(), e, "");
        }
    }
}
