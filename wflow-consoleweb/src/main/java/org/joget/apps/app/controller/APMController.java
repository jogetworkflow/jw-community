package org.joget.apps.app.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.joget.apm.APMUtil;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class APMController {
    
    @Autowired
    AppService appService;
    
    @RequestMapping({"/console/monitor/apm", "/console/app/(*:appId)/(~:version)/performance"})
    public String monitorApm(ModelMap map, @RequestParam(value = "appId", required = false) String appId, @RequestParam(value = "version", required = false) String version) {
        try {
            String glowrootUrl = WorkflowUtil.getSystemSetupValue("glowrootUrl");
            if (glowrootUrl == null || glowrootUrl.isEmpty()) {
                glowrootUrl = "http://localhost:4000";
            }
            
            URL siteURL = new URL(glowrootUrl);
            HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.connect();

            int code = connection.getResponseCode();
            if (code == 200) {
                MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
                Object totalMemory = mBeanServer.getAttribute(new ObjectName("java.lang","type","OperatingSystem"), "TotalPhysicalMemorySize");
                CompositeDataSupport maxHeap = (CompositeDataSupport) mBeanServer.getAttribute(new ObjectName("java.lang","type","Memory"), "HeapMemoryUsage");
                
                map.put("totalMemory", totalMemory);
                map.put("maxHeap", maxHeap.get("max"));
                
                if (appId == null) {
                    return "apm/view";
                } else {
                    AppDefinition appDef = appService.getAppDefinition(appId, version);
                    map.addAttribute("appId", appDef.getId());
                    map.addAttribute("appVersion", appDef.getVersion());
                    map.addAttribute("appDefinition", appDef);
                    return "console/apps/performance";
                }
            }
        } catch (Exception e) {
        }
        
        return "apm/unavailable";
    }
    
    @RequestMapping("/console/monitor/apm/redirect")
    public String monitorApmRedirect(ModelMap map, @RequestParam("url") String url) {
        String appId = "";
        String appVersion = "";
        String userviewId = "";
        String menuId = "";
        
        Pattern pattern = Pattern.compile("^http.*/userview/([^/]+)/([^/]+)(.*)");
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            appId = matcher.group(1);
            userviewId = matcher.group(2);
            menuId = matcher.group(3);
        }
        
        if (!appId.isEmpty()) {
            Long version = appService.getPublishedVersion(appId);
            if (version == null) {
                return "redirect:/404";
            } else {
                appVersion = version.toString();
            }
        } else {
            return "redirect:/404";
        }
        
        if (!menuId.isEmpty()) {
            menuId = "?menuId=" + menuId.substring(1); 
        }
        
        return "redirect:/web/console/app/" + appId + "/" + appVersion + "/userview/builder/" + userviewId + menuId;
    }
    
    @RequestMapping("/json/console/monitor/apm/retrieve/summary")
    public void monitorApmRetrieveSummary(HttpServletRequest httpRequest, @RequestParam(value = "appId", required = false) String appId, @RequestParam("from") Long form, @RequestParam("to") Long to, Writer writer) throws IOException {
        writer.write(APMUtil.getSummaries(httpRequest.getServerName(), appId , form, to));
    }
    
    @RequestMapping({"/json/console/monitor/apm/retrieve/(*:action)/(*:subaction)", "/json/console/monitor/apm/retrieve/(*:action)/(*:subaction)/(*:method)"})
    public void monitorApmRetrieve(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Writer writer, @RequestParam("action") String action, @RequestParam("subaction") String subaction, @RequestParam(value = "method", required = false) String method) {
        CloseableHttpClient client = null;
        HttpRequestBase request = null;
        
        try {
            String query = httpRequest.getQueryString();
            if (query != null) {
                query = query.replaceAll(StringUtil.escapeRegex("%25"), StringUtil.escapeRegex("%"));
            } else {
                query = "";
            }
            String url = "http://localhost:4000/backend/"+action+"/"+subaction;
            
            if (method != null) {
                url += "/" + method;
            }
                    
            url += "?"+ query;
            
            url = StringUtil.removeParamFromUrl(url, "_");
            
            client = HttpClients.createDefault();
            
            if ("GET".equalsIgnoreCase(httpRequest.getMethod())) {
                request = new HttpGet(url);
            } else {
                request = new HttpPost(url);
                
                // Read payload from request
                StringBuilder buffer = new StringBuilder();
                BufferedReader reader = httpRequest.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String data = buffer.toString();

                StringEntity requestEntity = new StringEntity(data, "UTF-8");
                ((HttpPost) request).setEntity(requestEntity);
                request.setHeader("Content-type", "application/json");
            }
            
            HttpResponse response = client.execute(request);
            writer.write(EntityUtils.toString(response.getEntity(), "UTF-8"));
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        } finally {
            try {
                if (request != null) {
                    request.releaseConnection();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException ex) {
                LogUtil.error(getClass().getName(), ex, "");
            }
        }    
    }
}
