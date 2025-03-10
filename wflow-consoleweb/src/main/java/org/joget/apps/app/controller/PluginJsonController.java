package org.joget.apps.app.controller;

import java.io.IOException;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
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
                pluginList = new ArrayList<Plugin>(pluginManager.list(clazz));
            } else {
                pluginList = new ArrayList<Plugin>();

                Collection<Plugin> fullPluginList = pluginManager.list();

                for (Plugin plugin : fullPluginList) {
                    if (plugin instanceof AuditTrailPlugin || plugin instanceof DeadlinePlugin || plugin instanceof ParticipantPlugin || plugin instanceof ApplicationPlugin) {
                        pluginList.add(plugin);
                    }
                }
            }
            
            writePluginsResponse(pluginList, filter, start, rows, false, writer);
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
                pluginList = new ArrayList<Plugin>(pluginManager.list(clazz));
            } else {
                pluginList = new ArrayList<Plugin>(pluginManager.list());
            }

            writePluginsResponse(pluginList, filter, start, rows, true, writer);
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
                pluginList = new ArrayList<Plugin>(pluginManager.listOsgiPlugin(clazz));
            } else {
                pluginList = new ArrayList<Plugin>(pluginManager.listOsgiPlugin(null));
            }

            writePluginsResponse(pluginList, filter, start, rows, true, writer);
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
    
    /**
     * Sort the list with label using treemap
     * @param pluginList
     * @return 
     */
    protected Map<String, Plugin> sortPluginList(List<Plugin> pluginList) {
        Map<String, Plugin> sorted = new TreeMap<String, Plugin>();
        for (Plugin plugin : pluginList) {
            String label = plugin.getI18nLabel();
            if (label != null) {
                sorted.put(label, plugin);
            }
        }
        return sorted;
    }
    
    /**
     * Sort the plugin list and write it to response
     * 
     * @param pluginList
     * @param filter
     * @param start
     * @param rows
     * @param checkUnintallable
     * @param writer
     */
    protected void writePluginsResponse(List<Plugin> pluginList, String filter, int start, int rows, boolean checkUnintallable, Writer writer) {
        Map<String, Plugin> sortedPluginList = sortPluginList(pluginList);
        
        JSONObject jsonObject = new JSONObject();
        int counter = 0;

        Map<String, String> pluginType = PluginManager.getPluginType();
        for (Entry<String, Plugin> entry : sortedPluginList.entrySet()) {
            try {
                String label = entry.getKey();
                Plugin plugin = entry.getValue();
                if (label == null || label.isEmpty()) {
                    continue;
                }
                if (filter != null && !filter.isEmpty() && !label.toLowerCase().contains(filter.toLowerCase())) {
                    continue;
                }

                if (counter >= start && counter < start + rows) {
                    Map data = new HashMap();
                    data.put("id", ClassUtils.getUserClass(plugin).getName());
                    data.put("name", label);
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
                    
                    if (checkUnintallable) {
                        data.put("uninstallable", (pluginManager.isOsgi(data.get("id").toString())) ? "<span class=\"tick\"></span>" : "");
                    }

                    jsonObject.accumulate("data", data);
                }
                counter++;
            } catch (Exception ex) {
                //exception because of plugin uninstalled in other request, just ignore it
            }
        }

        jsonObject.accumulate("total", counter);
        jsonObject.accumulate("start", start);
        jsonObject.write(writer);
    }
}
