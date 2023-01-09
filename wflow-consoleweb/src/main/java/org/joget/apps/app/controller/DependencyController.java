package org.joget.apps.app.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.bouncycastle.util.encoders.Base64;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.DependenciesUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class DependencyController {
    
    @Autowired
    AppService appService;
    
    @Autowired
    AppDefinitionDao appDefinitionDao;
    
    @RequestMapping("/dependency/tree/image/(*:id)")
    public void getTreeImage(OutputStream out, @RequestParam("id") String id, @RequestParam("postfix") String postfix, @RequestParam("data") String data, HttpServletRequest request, HttpServletResponse response) throws IOException {
        byte[] img = null;
        if (data != null && data.startsWith("data:")) {
            // cut off "data:"
            try {
                String raw = data.substring(5);
                String mime = raw.substring(0, raw.indexOf(';'));
                // looking for MimeTyp image/...
                if (mime != null && mime.startsWith("image/")) {
                    String base64data = raw.substring(mime.length() + 1);
                    if (base64data.startsWith("base64,")) {
                        // cut off "base64,"
                        String imgData = base64data.substring(7).trim();
                        img = Base64.decode(imgData);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(DependencyController.class.getName(), e, "");
            }
        }
        
        if (img == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        id = SecurityUtil.validateStringInput(id);
        postfix = SecurityUtil.validateStringInput(postfix);
        response.setHeader("Content-Type", "image/png");
        response.setHeader("Content-Disposition", "attachment; filename="+id+postfix+".png");

        try {
            out.write(img);
        } finally {
            out.close();
        }
    }
    
    @RequestMapping("/json/dependency/app/(*:appId)/(~:version)/check")
    public void checkDependency(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("keyword") String keyword, @RequestParam(value = "type", required = false) String type, @RequestParam(value = "callback", required = false) String callback, HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
        
        
        JSONObject result = new JSONObject();
        JSONArray usages = DependenciesUtil.getDependencies(appId, version, type, keyword, request);
        
        if (usages.length() > 0) {
            result.put("usages", usages);
        } else {
            result.put("usages", ResourceBundleUtil.getMessage("dependency.usage.noUsageFound"));
        }
        result.put("size", usages.length());
        
        AppUtil.writeJson(writer, result, callback);
    }
    
    @RequestMapping("/json/dependency/app/(*:appId)/(~:version)/checkOther")
    public void checkDependencyInOtherApp(Writer writer, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("keyword") String keyword, @RequestParam(value = "type", required = false) String type, @RequestParam(value = "callback", required = false) String callback, HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
        JSONObject result = new JSONObject();
        JSONObject usages = new JSONObject();
        
        Collection<AppDefinition> appDefinitionList = appDefinitionDao.findLatestVersions(null, null, null, "name", Boolean.FALSE, null, null);
        for (AppDefinition appDef : appDefinitionList) {
            if (!appId.equals(appDef.getAppId())) {
                JSONArray appUsages = DependenciesUtil.getDependencies(appDef.getAppId(), appDef.getVersion().toString(), type, keyword, request);
                if (appUsages.length() > 0) {
                    usages.put(appDef.getName() + " (" + ResourceBundleUtil.getMessage("console.app.common.label.version") + " " + appDef.getVersion().toString() + ")", appUsages);
                }
            }
        }
        
        if (usages.length() > 0) {
            result.put("usages", usages);
        } else {
            result.put("usages", ResourceBundleUtil.getMessage("dependency.usage.noUsageFound"));
        }
        result.put("size", usages.length());
        
        AppUtil.writeJson(writer, result, callback);
    }
}
