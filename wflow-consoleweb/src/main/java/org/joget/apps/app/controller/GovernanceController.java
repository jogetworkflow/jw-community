package org.joget.apps.app.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.ServerUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.governance.model.GovHealthCheckResult;
import org.joget.governance.service.GovHealthCheckManager;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GovernanceController {
    
    @Autowired
    GovHealthCheckManager govHealthCheckManager;
    
    @Autowired
    PluginManager pluginManager;
    
    @Autowired
    SetupManager setupManager;
    
    @RequestMapping("/console/monitor/governance")
    public String governance(ModelMap map) {
        
        map.put("serverName", ServerUtil.getServerName());
        map.put("interval", govHealthCheckManager.getCheckInterval());
        map.put("checker", govHealthCheckManager.getGovHealthChecker());
        map.put("lastResult", govHealthCheckManager.getLastResultsJson());
        
        return "console/monitor/governance";
    }
    
    @RequestMapping("/console/monitor/governance/alert")
    public String governanceAlertConfig(ModelMap map) throws IOException {
        String properties = setupManager.getSettingValue("governance_alert");
        if (properties == null || properties.isEmpty()) {
            properties = "{}";
        }
        map.addAttribute("alertProp", PropertyUtil.propertiesJsonLoadProcessing(properties));
        return "console/monitor/governanceAlert";
    }
    
    @RequestMapping(value = "/console/monitor/governance/alert/submit", method = RequestMethod.POST)
    public String governanceAlertConfigSubmit(ModelMap map, @RequestParam(value = "properties", required = false) String properties, HttpServletRequest request) {
        Setting propertySetting = setupManager.getSettingByProperty("governance_alert");
        if (propertySetting == null) {
            propertySetting = new Setting();
            propertySetting.setProperty("governance_alert");
        }

        propertySetting.setValue(PropertyUtil.propertiesJsonStoreProcessing(propertySetting.getValue(), properties));
        setupManager.saveSetting(propertySetting);

        String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
        String url = contextPath + "/web/console/monitor/governance";
        map.addAttribute("url", url);
        return "console/dialogClose";
    }
    
    @RequestMapping("/console/monitor/governance/config")
    public String governancePluginConfig(ModelMap map, @RequestParam("pluginclass") String pluginclass, HttpServletRequest request) throws IOException {
        pluginclass = SecurityUtil.validateStringInput(pluginclass);
        Plugin plugin = pluginManager.getPlugin(pluginclass);
        
        if (plugin != null) {
            String properties = setupManager.getSettingValue("governance_"+pluginclass);
            map.addAttribute("properties", PropertyUtil.propertiesJsonLoadProcessing(properties));

            if (plugin instanceof PropertyEditable) {
                PropertyEditable pe = (PropertyEditable) plugin;
                map.addAttribute("propertyEditable", pe);
                map.addAttribute("propertiesDefinition", PropertyUtil.injectHelpLink(plugin.getHelpLink(), pe.getPropertyOptions()));
            }

            map.addAttribute("plugin", plugin);

            String url = request.getContextPath() + "/web/console/monitor/governance/config/submit?pluginclass=" + StringEscapeUtils.escapeHtml(pluginclass);
            map.addAttribute("actionUrl", url);
            
            return "console/plugin/pluginConfig";
        } else {
            return "error404";
        }
    }
    
    @RequestMapping(value = "/console/monitor/governance/config/submit", method = RequestMethod.POST)
    public String governancePluginConfigSubmit(ModelMap map, @RequestParam("pluginclass") String pluginclass, @RequestParam(value = "pluginProperties", required = false) String pluginProperties, HttpServletRequest request) {
        Plugin plugin = (Plugin) pluginManager.getPlugin(pluginclass);

        //save plugin
        Setting propertySetting = setupManager.getSettingByProperty("governance_"+pluginclass);
        if (propertySetting == null) {
            propertySetting = new Setting();
            propertySetting.setProperty("governance_"+pluginclass);
        }

        propertySetting.setValue(PropertyUtil.propertiesJsonStoreProcessing(propertySetting.getValue(), pluginProperties));
        setupManager.saveSetting(propertySetting);

        String contextPath = WorkflowUtil.getHttpServletRequest().getContextPath();
        String url = contextPath + "/web/console/monitor/governance";
        map.addAttribute("url", url);
        return "console/dialogClose";
    }
    
    @RequestMapping(value = "/governance/updateInterval", method = RequestMethod.POST)
    public void updateInterval(Writer writer, @RequestParam("interval") String interval) throws IOException, JSONException {
        govHealthCheckManager.updateCheckInterval(interval);
    }
    
    @RequestMapping(value = "/governance/activate", method = RequestMethod.POST)
    public void activate(Writer writer, @RequestParam("pluginClass") String pluginClass) throws IOException, JSONException {
        govHealthCheckManager.activate(pluginClass);
    }
    
    @RequestMapping(value = "/governance/deactivate", method = RequestMethod.POST)
    public void deactivate(Writer writer, @RequestParam("pluginClass") String pluginClass) throws IOException, JSONException {
        govHealthCheckManager.deactivate(pluginClass);
    }
    
    @RequestMapping(value = "/governance/suppress", method = RequestMethod.POST)
    public void suppress(Writer writer, @RequestParam("pluginClass") String pluginClass, @RequestParam("detail") String detail) throws IOException, JSONException {
        String json = govHealthCheckManager.suppress(pluginClass, StringUtil.stripAllHtmlTag(detail));
        writer.write(json);
    }
    
    @RequestMapping(value = "/governance/deleteData", method = RequestMethod.POST)
    public void deleteData(Writer writer) throws IOException, JSONException {
        govHealthCheckManager.cleanData();
    }
    
    @RequestMapping(value = "/governance/checkNow", method = RequestMethod.POST)
    public void checkNow(Writer writer) throws IOException, JSONException {
        govHealthCheckManager.runCheck();
        String json = govHealthCheckManager.getLastResultsJson();
        writer.write(json);
    }
    
    @RequestMapping("/governance/lastResult")
    public void lastResult(Writer writer, @RequestParam(value = "lastCheck", required = false) Long lastCheckTimestamp) throws IOException, JSONException {
        if (lastCheckTimestamp != null) {
            Date lastCheck = new Date(lastCheckTimestamp);
            Map<String,GovHealthCheckResult> results = govHealthCheckManager.getLastResults();
            if (results != null && results.containsKey("lastCheckDate")) {
                Date resultDate = results.get("lastCheckDate").getTimestamp();
                if (lastCheck.equals(resultDate)) {
                    return;
                }
            }
        }
        
        String json = govHealthCheckManager.getLastResultsJson();
        writer.write(json);
    }
}
