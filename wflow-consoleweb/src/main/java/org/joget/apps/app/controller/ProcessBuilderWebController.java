package org.joget.apps.app.controller;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.model.ProcessFormModifier;
import org.joget.apps.app.model.ProcessMappingInfo;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.ext.ConsoleWebPlugin;
import org.joget.commons.util.FileLimitException;
import org.joget.commons.util.FileStore;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.directory.model.Department;
import org.joget.directory.model.Group;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.XpdlImageUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ProcessBuilderWebController {

    @Autowired
    AppService appService;    
    
    @Autowired
    WorkflowManager workflowManager;    
    
    @Autowired
    PluginManager pluginManager;
    
    @Resource
    FormDefinitionDao formDefinitionDao;
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/process/builder")
    public String processBuilder(ModelMap model, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException {
        // verify app version
        ConsoleWebPlugin consoleWebPlugin = (ConsoleWebPlugin)pluginManager.getPlugin(ConsoleWebPlugin.class.getName());
        String page = consoleWebPlugin.verifyAppVersion(appId, version);
        if (page != null) {
            return page;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        model.addAttribute("appId", appDef.getId());
        model.addAttribute("version", appDef.getVersion());
        model.addAttribute("appDefinition", appDef);
        return "pbuilder/pbuilder";
    }
    
    @RequestMapping("/console/app/(*:appId)/(~:version)/process/mapper")
    public String processMapper(ModelMap model, HttpServletRequest request, HttpServletResponse response, @RequestParam("appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException {
        // verify app version
        ConsoleWebPlugin consoleWebPlugin = (ConsoleWebPlugin)pluginManager.getPlugin(ConsoleWebPlugin.class.getName());
        String page = consoleWebPlugin.verifyAppVersion(appId, version);
        if (page != null) {
            return page;
        }

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        model.addAttribute("appId", appDef.getId());
        model.addAttribute("version", appDef.getVersion());
        model.addAttribute("appDefinition", appDef);
        return "pbuilder/pmapper";
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/process/mapping")
    public void processMapping(Writer writer, HttpServletRequest request, @RequestParam("appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        
        Map<String, Plugin> pluginsMap = new HashMap<String, Plugin>();
        Map<String, String> formsMap = new HashMap<String, String>();
        Map<String, Group> groupMaps = new HashMap<String, Group>();
        Map<String, Department> deptMaps = new HashMap<String, Department>();
        
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDefinition = appDef.getPackageDefinition();
        if (packageDefinition != null) {
            Map<String, PackageActivityForm> activityFormMap = packageDefinition.getPackageActivityFormMap();
            JSONObject activityForms = new JSONObject();
            if (activityFormMap != null && !activityFormMap.isEmpty()) {
                for (String k : activityFormMap.keySet()) {
                    JSONObject o = new JSONObject();
                    PackageActivityForm f = activityFormMap.get(k);
                    
                    populateActivityForm(o, f, appDef, formsMap);
                    activityForms.put(k, o);
                }
            }
            jsonObject.put("activityForms", activityForms);
            
            Map<String, PackageActivityPlugin> activityMap = packageDefinition.getPackageActivityPluginMap();
            JSONObject activityPlugins = new JSONObject();
            if (activityMap != null && !activityMap.isEmpty()) {
                for (String k : activityMap.keySet()) {
                    JSONObject o = new JSONObject();
                    PackageActivityPlugin p = activityMap.get(k);
                    
                    populateActivityPlugin(o, p, pluginsMap);
                    activityPlugins.put(k, o);
                }
            }
            jsonObject.put("activityPlugins", activityPlugins);
            
            Map<String, PackageParticipant> participantMap = packageDefinition.getPackageParticipantMap();
            JSONObject participants = new JSONObject();
            if (participantMap != null && !participantMap.isEmpty()) {
                for (String k : participantMap.keySet()) {
                    JSONObject o = new JSONObject();
                    PackageParticipant p = participantMap.get(k);
                    
                    populateParticipant(request, o, p, pluginsMap, groupMaps, deptMaps);
                    participants.put(k, o);
                }
            }
            jsonObject.put("participants", participants);
            jsonObject.put("packageVersion", packageDefinition.getVersion().toString());
            
            Map<String, Plugin> modifierPluginMap = pluginManager.loadPluginMap(ProcessFormModifier.class);
            jsonObject.put("modifierPluginCount", modifierPluginMap.size());
            if (modifierPluginMap.size() == 1) {
                jsonObject.put("modifierPlugin", modifierPluginMap.keySet().iterator().next());
            }
        }
        AppUtil.writeJson(writer, jsonObject, callback);
    }
    
    @RequestMapping("/json/console/app/(*:appId)/(~:version)/process/(*:processDefId)/mapping/(*:type)/(*:id)")
    public void activityMapping(Writer writer, HttpServletRequest request, @RequestParam("appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam("processDefId") String processDefId, @RequestParam("type") String type, @RequestParam("id") String id, @RequestParam(value = "callback", required = false) String callback) throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject();
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        PackageDefinition packageDefinition = appDef.getPackageDefinition();
        
        if (packageDefinition != null) {
            if ("participant".equals(type) || "whitelist".equals(type)) {
                Map<String, Plugin> pluginsMap = new HashMap<String, Plugin>();        
                Map<String, Group> groupMaps = new HashMap<String, Group>();
                Map<String, Department> deptMaps = new HashMap<String, Department>();
                PackageParticipant participant = packageDefinition.getPackageParticipant(processDefId, id);
                if (participant != null) {
                    populateParticipant(request, jsonObject, participant, pluginsMap, groupMaps, deptMaps);
                }
            } else if ("start".equals(type) || "activity".equals(type)) {
                Map<String, String> formsMap = new HashMap<String, String>();
                PackageActivityForm form = packageDefinition.getPackageActivityForm(processDefId, id);
                if (form != null) {
                    populateActivityForm(jsonObject, form, appDef, formsMap);
                }
                PackageActivityPlugin plugin = packageDefinition.getPackageActivityPlugin(processDefId, id);
                if (plugin != null) {
                    Map<String, Plugin> pluginsMap = new HashMap<String, Plugin>();
                    JSONObject modifierObject = new JSONObject();
                    populateActivityPlugin(modifierObject, plugin, pluginsMap);
                    jsonObject.put("modifier", modifierObject);
                }
            } else {
                Map<String, Plugin> pluginsMap = new HashMap<String, Plugin>();
                PackageActivityPlugin plugin = packageDefinition.getPackageActivityPlugin(processDefId, id);
                if (plugin != null) {
                    populateActivityPlugin(jsonObject, plugin, pluginsMap);
                }
            }
        }
                    
        AppUtil.writeJson(writer, jsonObject, callback);
    }
    
    protected void populateActivityForm(JSONObject o, PackageActivityForm f, AppDefinition appDef, Map<String, String> formsMap) throws JSONException {
        o.put("activityDefId", f.getActivityDefId());
        o.put("processDefId", f.getProcessDefId());
        o.put("formId", f.getFormId());
        o.put("formUrl", f.getFormUrl());
        o.put("disableSaveAsDraft", f.getDisableSaveAsDraft());
        o.put("autoContinue", f.isAutoContinue());
        o.put("type", f.getType());

        if (f.getFormId() != null && !f.getFormId().isEmpty()) {
            if (!formsMap.containsKey(f.getFormId())) {
                FormDefinition formDefinition = formDefinitionDao.loadById(f.getFormId(), appDef);
                if (formDefinition != null) {
                    formsMap.put(f.getFormId(), formDefinition.getName());
                }
            }
            o.put("formName", formsMap.get(f.getFormId()));
        }
    }
    
    protected void populateActivityPlugin(JSONObject o, PackageActivityPlugin p, Map<String, Plugin> pluginsMap) throws JSONException {
        o.put("activityDefId", p.getActivityDefId());
        o.put("processDefId", p.getProcessDefId());
        o.put("pluginClassName", p.getPluginName());

        if (!pluginsMap.containsKey(p.getPluginName())) {
            Plugin plugin = pluginManager.getPlugin(p.getPluginName());
            if (plugin != null) {
                pluginsMap.put(p.getPluginName(), plugin);
            }
        }
        if (pluginsMap.containsKey(p.getPluginName())) {
            o.put("pluginLabel", pluginsMap.get(p.getPluginName()).getI18nLabel());
            o.put("pluginVersion", pluginsMap.get(p.getPluginName()).getVersion());
            
            Plugin plugin = pluginsMap.get(p.getPluginName());
            if (plugin instanceof ProcessMappingInfo && p.getPluginProperties() != null) {
                Map propertiesMap = AppPluginUtil.getDefaultProperties(plugin, p.getPluginProperties(), AppUtil.getCurrentAppDefinition(), null);
                if (plugin instanceof PropertyEditable) {
                    ((PropertyEditable) plugin).setProperties(propertiesMap);
                }   
                String info = ((ProcessMappingInfo) plugin).getMappingInfo();
                if (info != null && !info.isEmpty()) {
                    o.put("mappingInfo", info);
                }
            }
        } else {
            o.put("pluginLabel", p.getPluginName());
            o.put("pluginVersion", ResourceBundleUtil.getMessage("console.process.config.label.mapParticipants.unavailable"));
        }
    }
    
    protected void populateParticipant(HttpServletRequest request, JSONObject o, PackageParticipant p, Map<String, Plugin> pluginsMap, Map<String, Group> groupMaps, Map<String, Department> deptMaps) throws JSONException {
        o.put("participantId", p.getParticipantId());
        o.put("processDefId", p.getProcessDefId());
        o.put("type", p.getType());
        o.put("typeLabel", ResourceBundleUtil.getMessage("console.process.config.label.mapParticipants.type."+p.getType()));

        if ("plugin".equals(p.getType())) {
            if (!pluginsMap.containsKey(p.getValue())) {
                Plugin plugin = pluginManager.getPlugin(p.getValue());
                if (plugin != null) {
                    pluginsMap.put(p.getValue(), plugin);
                }
            }
            if (pluginsMap.containsKey(p.getValue())) {
                String htmlValue = pluginsMap.get(p.getValue()).getI18nLabel() + " (" + ResourceBundleUtil.getMessage("console.plugin.label.version") + " " +pluginsMap.get(p.getValue()).getVersion()+")";
                o.put("htmlValue", htmlValue);
            } else {
                String htmlValue = p.getValue() + " (" + ResourceBundleUtil.getMessage("console.process.config.label.mapParticipants.unavailable");
                o.put("htmlValue", htmlValue);
            }
        } else if ("group".equals(p.getType())) {
            if (groupMaps.isEmpty()) {
                groupMaps.putAll(DirectoryUtil.getGroupsMap());
            }
            String htmlValue = "";
            String values = p.getValue();
            values = values.replaceAll(";", ",");
            for (String v : values.split(",")) {
                if (groupMaps.containsKey(v)) {
                    if (DirectoryUtil.isExtDirectoryManager()) {
                        htmlValue += "<div class=\"single_value\"><a href=\""+request.getContextPath()+"/web/console/directory/group/view/"+v+"\" target=\"_blank\">"+groupMaps.get(v).getName()+"</a> <a class=\"remove_single\" value=\""+v+"\"><i class=\"fas fa-times-circle\"></i></a></div>";
                    } else {
                        htmlValue += "<div class=\"single_value\">" + groupMaps.get(v).getName() + " <a class=\"remove_single\" value=\""+v+"\"><i class=\"fas fa-times-circle\"></i></a></div>";
                    }
                } else {
                    htmlValue += "<div class=\"single_value\"><span class=\"unavailable\">"+v+" " + ResourceBundleUtil.getMessage("console.process.config.label.mapParticipants.unavailable") + "</span> <a class=\"remove_single\" value=\""+v+"\"><i class=\"fas fa-times-circle\"></i></a></div>";
                }
            }
            o.put("htmlValue", htmlValue);
        } else if ("hod".equals(p.getType()) || "department".equals(p.getType())) {
            if (deptMaps.isEmpty()) {
                deptMaps.putAll(DirectoryUtil.getDepartmentsMap());
            }
            String htmlValue = "";
            if (deptMaps.containsKey(p.getValue())) {
                if (DirectoryUtil.isExtDirectoryManager()) {
                    htmlValue += "<a href=\""+request.getContextPath()+"/web/console/directory/dept/view/"+p.getValue()+".\" target=\"_blank\">"+deptMaps.get(p.getValue()).getName()+"</a>";
                } else {
                    htmlValue += deptMaps.get(p.getValue()).getName();
                }
            } else {
                htmlValue += "<span class=\"unavailable\">"+p.getValue()+" " + ResourceBundleUtil.getMessage("console.process.config.label.mapParticipants.unavailable") + "</span>";
            }
            o.put("htmlValue", htmlValue);
        } else if ("user".equals(p.getType())) {
            String htmlValue = "";
            String values = p.getValue();
            values = values.replaceAll(";", ",");
            for (String v : values.split(",")) {
                if (DirectoryUtil.isExtDirectoryManager()) {
                    htmlValue += "<div class=\"single_value\"><a href=\""+request.getContextPath()+"/web/console/directory/user/view/"+v+".\" target=\"_blank\">"+v+"</a> <a class=\"remove_single\" value=\""+v+"\"><i class=\"fas fa-times-circle\"></i></a></div>";
                } else {
                    htmlValue += "<div class=\"single_value\">" + v + " <a class=\"remove_single\" value=\""+v+"\"><i class=\"fas fa-times-circle\"></i></a></div>";
                }
            }
            o.put("htmlValue", htmlValue);
        } else if ("workflowVariable".endsWith(p.getType())) {
            String[] values = p.getValue().split(",");
            String htmlValue = "<font class=\"ftl_label\">" + ResourceBundleUtil.getMessage("console.app.process.common.label.variableId") + " :</font> " + values[0] + "<br/>"+ResourceBundleUtil.getMessage("console.process.config.label.mapParticipants.variable."+values[1]);
            o.put("htmlValue", htmlValue);
        } else if (p.getType().startsWith("requester")) {
            String htmlValue = ResourceBundleUtil.getMessage("console.process.config.label.mapParticipants.previousActivity");
            if (p.getValue() != null && !p.getValue().isEmpty()) {        
                htmlValue = "<font class=\"ftl_label\">" + ResourceBundleUtil.getMessage("console.app.process.common.label.definitionId") + " :</font> " + p.getValue();
            } else {
                htmlValue = "<font class=\"ftl_label\">" + ResourceBundleUtil.getMessage("console.process.config.label.mapParticipants.previousActivity") + "</font> ";
            }
            o.put("htmlValue", htmlValue);
        } else if ("role".equals(p.getType())) {
            o.put("htmlValue", ResourceBundleUtil.getMessage("console.process.config.label.mapParticipants.role."+p.getValue()));
        }
    }

    @RequestMapping(value="/console/app/(*:appId)/(~:version)/process/screenshot/(*:processDefId)")
    public String processBuilderScreenshot(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "processDefId") String processDefId, @RequestParam(value = "callback", required = false) String callback) throws IOException {
        // get process info
        WorkflowProcess wfProcess = workflowManager.getProcess(processDefId);

        // get process xpdl
        byte[] xpdlBytes = workflowManager.getPackageContent(wfProcess.getPackageId(), wfProcess.getVersion());
        if (xpdlBytes != null) {
            String xpdl = null;

            try {
                xpdl = new String(xpdlBytes, "UTF-8");
            } catch (Exception e) {
                LogUtil.debug(ConsoleWebController.class.getName(), "XPDL cannot load");
            }

            map.addAttribute("appId", appId);
            map.addAttribute("wfProcess", wfProcess);
            map.addAttribute("xpdl", xpdl);
            map.addAttribute("callback", callback);
        }

        String viewer = "pbuilder/pscreenshot";
        return viewer;    
    }
    
    @RequestMapping(value="/console/app/(*:appId)/(~:version)/process/builder/screenshot/submit", method = RequestMethod.POST)
    public void processBuilderScreenshotSubmit(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "processDefId") String processDefId) throws IOException {

        // validate input
        appId = SecurityUtil.validateStringInput(appId);        
        processDefId = SecurityUtil.validateStringInput(processDefId);        
        
        // get base64 encoded image in POST body
        MultipartFile xpdlimage = null;
        try {
            xpdlimage = FileStore.getFile("xpdlimage");
        } catch (FileLimitException e) {
            LogUtil.warn(ProcessBuilderWebController.class.getName(), ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
            return;
        }
        if (xpdlimage != null) {
            ByteArrayInputStream stream = null;
            try {
                stream =new ByteArrayInputStream(xpdlimage.getBytes());
                String imageBase64 = IOUtils.toString(stream, "UTF-8");
                imageBase64 = imageBase64.substring("data:image/png;base64,".length());

                // convert into bytes
                byte[] decodedBytes = Base64.decodeBase64(imageBase64.getBytes());        

                // save into image file
                String filename = processDefId + XpdlImageUtil.IMAGE_EXTENSION;
                String path = SetupManager.getBaseDirectory() + File.separator + "app_xpdlImages" + File.separator + appId;
                new File(path).mkdirs();
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedBytes));
                File f = new File(path + File.separator + filename);
                ImageIO.write(image, "png", f);

                // create thumbnail
                createThumbnail(image, path, processDefId);

                LogUtil.debug(getClass().getName(), "Created screenshot for process " + appId);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }
    }
    
    protected void createThumbnail(Image image, String path, String processDefId) {
        int thumbWidth = 400;
        int thumbHeight = 400;

        BufferedOutputStream out = null;

        try{
            double thumbRatio = (double) thumbWidth / (double) thumbHeight;
            int imageWidth = image.getWidth(null);
            int imageHeight = image.getHeight(null);
            double imageRatio = (double) imageWidth / (double) imageHeight;
            if (thumbRatio < imageRatio) {
                thumbHeight = (int) (thumbWidth / imageRatio);
            } else {
                thumbWidth = (int) (thumbHeight * imageRatio);
            }

            BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics2D = thumbImage.createGraphics();
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);

            out = new BufferedOutputStream(new FileOutputStream(path + File.separator + "thumb-" + processDefId + XpdlImageUtil.IMAGE_EXTENSION));
            ImageIO.write(thumbImage, "png", out);

            out.flush();
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "Error generating thumbnail [processDefId=" + processDefId + "]");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception ex) {
                LogUtil.error(getClass().getName(), ex, "");
            }
        }
    }    
}
