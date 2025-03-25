package org.joget.apps.app.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apm.APMUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.base.ConsolePagePlugin;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The controller for ConsolePagePlugin to handle path rendering
 */
@Controller
public class ConsolePagePluginController {
    
    @Autowired
    PluginManager pluginManager;
        
    @RequestMapping({"/console/plugin/(*:name)", "/console/plugin/(*:name)/(**:path)"})
    public String consolePagePlugin(ModelMap model, HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "name") String name, @RequestParam(value = "path", required = false) String path) throws IOException, ServletException {
        ConsolePagePlugin plugin = (ConsolePagePlugin) pluginManager.getPluginByTypeAndName(ConsolePagePlugin.class, name);
        if (plugin != null) {
            if (!plugin.isAuthorized()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return null;
            }
            
            return handleConsolePagePlugin(plugin, model, request, response, path);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        return null;
    }
    
    /**
     * Find the method in ConsolePagePlugin to render based on the path
     * 
     * @param plugin
     * @param model
     * @param request
     * @param response
     * @param path
     * @return
     * @throws IOException
     * @throws ServletException 
     */
    protected String handleConsolePagePlugin(ConsolePagePlugin plugin, ModelMap model, HttpServletRequest request, HttpServletResponse response, String path) throws IOException, ServletException {
        model.put("pluginName", plugin.getName());
        model.put("pluginIcon", plugin.getPluginIcon());
        model.put("pluginLabel", plugin.getLabel());
        model.put("menuLocation", plugin.getLocation().toString());
        
        Method method = null;
        Map<String, String> variables = new HashMap<>();
        if (path != null && !path.isEmpty() && !path.equals("/")) {
            path = "/" + path;
            
            AntPathMatcher matcher = new AntPathMatcher();
            
            //find the method to render the path
            Class<?> clazz = ClassUtils.getUserClass(plugin.getClass());
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(ConsolePagePlugin.Path.class)) {
                    ConsolePagePlugin.Path p = m.getAnnotation(ConsolePagePlugin.Path.class);
                    String[] pathPatterns = p.value();
                    for (String pattern : pathPatterns) {
                        if (matcher.match(pattern, path)) {
                            variables.putAll(matcher.extractUriTemplateVariables(pattern, path));
                            method = m;    
                            break;
                        }
                    }
                    if (method != null) {
                        break;
                    }
                }
            }
        }
        
        if (method != null) {
            try {
                //set parameters
                Parameter[] parameters = method.getParameters();
                List<Object> parameterValues = new ArrayList<Object>();

                for (Parameter parameter : parameters) {
                    Class clazz = parameter.getType();
                    String name = parameter.getName();
                    ConsolePagePlugin.PathParam pathParam = parameter.getAnnotation(ConsolePagePlugin.PathParam.class);
                    if (pathParam != null) {
                        name = pathParam.value();
                    }
                    
                    if (ModelMap.class.isAssignableFrom(clazz)) {
                        parameterValues.add(model);
                    } else if (HttpServletRequest.class.isAssignableFrom(clazz)) {
                        parameterValues.add(request);
                    } else if (HttpServletResponse.class.isAssignableFrom(clazz)) {
                        parameterValues.add(response);
                    } else if (variables.containsKey(name)) {
                        parameterValues.add(variables.get(name));
                    } else {
                        parameterValues.add(null);
                    }
                }

                Object result = method.invoke(plugin, parameterValues.toArray(new Object[0]));
                if (result instanceof String) {
                    return (String) result;
                } else {
                    return null;
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LogUtil.error(ConsolePagePluginController.class.getName(), ex, path);
                
                throw new IOException("Error executing path rendering method");
            }
        } else {
            String content = plugin.render(request, response);
            model.put("content", content);
            return "console/page/default";
        }
    }
    
    /**
     * Retrieve the menus based on location and sort it based on order
     * 
     * @param location
     * @return 
     */
    public static List<Map> getConsolePageMenus(String location) {
        List<Map> menus = new ArrayList<>();
        
        //add default menus
        switch (location) {
            case "DIRECTORY":
                addMenu(menus, "nav-users-orgchart", "/web/console/directory/orgs", "<i class=\"fas fa-sitemap\"></i>", ResourceBundleUtil.getMessage("console.header.submenu.label.organization"), 100);
                addMenu(menus, "nav-users-groups", "/web/console/directory/groups", "<i class=\"fas fa-users\"></i>", ResourceBundleUtil.getMessage("console.header.submenu.label.groups"), 200);
                addMenu(menus, "nav-users-users", "/web/console/directory/users", "<i class=\"fas fa-user\"></i>", ResourceBundleUtil.getMessage("console.header.submenu.label.users"), 300);
                break;
            case "MONITOR":
                addMenu(menus, "nav-monitor-running", "/web/console/monitor/running", "<i class=\"fas fa-play\"></i>", ResourceBundleUtil.getMessage("console.header.submenu.label.runningProcesses"), 100);
                if (AppUtil.hasNonArchivedProcessData())
                    addMenu(menus, "nav-monitor-completed", "/web/console/monitor/completed", "<i class=\"fas fa-stop\"></i>", ResourceBundleUtil.getMessage("console.header.submenu.label.completedProcesses"), 200);
                if (AppUtil.isArchivedProcessDataModeEnabled())
                    addMenu(menus, "nav-monitor-archived", "/web/console/monitor/archived", "<i class=\"fas fa-stop\"></i>", ResourceBundleUtil.getMessage("console.header.submenu.label.archivedProcesses"), 300);
                addMenu(menus, "nav-monitor-audit", "/web/console/monitor/audit", "<i class=\"fas fa-shoe-prints fa-rotate-270\"></i>", ResourceBundleUtil.getMessage("console.header.submenu.label.auditTrail"), 400);
                if (LogUtil.isDeployInTomcat() && !HostManager.isVirtualHostEnabled())
                    addMenu(menus, "nav-monitor-log", "/web/console/monitor/logs", "<i class=\"fas fa-scroll\"></i>", ResourceBundleUtil.getMessage("console.header.submenu.label.logs"), 500);
                addMenu(menus, "nav-monitor-slog", "/web/console/monitor/slogs", "<i class=\"fas fa-scroll\"></i>", ResourceBundleUtil.getMessage("console.log.mtitle"), 600);
                addMenu(menus, "nav-governance", "/web/console/monitor/governance", "<i class=\"fas fa-check-circle\"></i>", ResourceBundleUtil.getMessage("console.governance.healthCheck"), 700);
                if (APMUtil.isGlowrootAvailable())
                    addMenu(menus, "nav-monitor-apm", "/web/console/monitor/apm", "<i class=\"fas fa-tachometer-alt\"></i>", ResourceBundleUtil.getMessage("apm.performance"), 800);
                break;   
            case "SETTINGS":
                addMenu(menus, "nav-setting-general", "/web/console/setting/general", "<i class=\"fas fa-cog\"></i>", ResourceBundleUtil.getMessage("console.header.submenu.label.setting.general"), 100);
                if (!HostManager.isVirtualHostEnabled())
                    addMenu(menus, "nav-setting-datasource", "/web/console/setting/datasource", "<i class=\"fas fa-database\"></i>", ResourceBundleUtil.getMessage("console.header.submenu.label.setting.datasource"), 200);
                addMenu(menus, "nav-setting-directory", "/web/console/setting/directory", "<i class=\"fas fa-users-cog\"></i>", ResourceBundleUtil.getMessage("console.header.submenu.label.setting.directory"), 300);
                addMenu(menus, "nav-setting-plugin", "/web/console/setting/plugin", "<i class=\"fas fa-plug\"></i>", ResourceBundleUtil.getMessage("console.header.submenu.label.setting.plugin"), 400);
                addMenu(menus, "nav-setting-message", "/web/console/setting/message", "<i class=\"fas fa-language\"></i>", ResourceBundleUtil.getMessage("console.header.submenu.label.setting.message"), 500);
                break;        
        }
        
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        Collection<Plugin> plugins = pluginManager.list(ConsolePagePlugin.class);
        for (Plugin plugin : plugins) {
            ConsolePagePlugin p = (ConsolePagePlugin) plugin;
            if (p.getLocation().toString().equalsIgnoreCase(location) && p.isAuthorized()) {
                String name = p.getName();
                addMenu(menus, name, "/web/console/plugin/"+name, p.getPluginIcon(), ((Plugin) p).getI18nLabel(), p.getOrder());
            }
        }
        
        //sort the menus by order
        Collections.sort(menus, (Map m1, Map m2) -> Integer.compare((int) m1.get("order"), (int) m2.get("order")));
        
        return menus;
    }
    
    /**
     * Add menu to list
     * @param menus
     * @param id
     * @param url
     * @param icon
     * @param label
     * @param order 
     */
    protected static void addMenu(List<Map> menus, String id, String url, String icon, String label, int order) {
        Map menu = new HashMap();
        menu.put("id", id);
        menu.put("url", url);
        menu.put("icon", icon);
        menu.put("label", label);
        menu.put("order", order);
        
        menus.add(menu);
    }
}
