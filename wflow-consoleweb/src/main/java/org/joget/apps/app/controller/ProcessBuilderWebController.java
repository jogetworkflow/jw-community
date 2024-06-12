package org.joget.apps.app.controller;

import com.github.underscore.lodash.U;
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
import java.sql.SQLException;
import java.util.Iterator;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.hibernate.JDBCException;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.dao.PackageDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.PackageActivityForm;
import org.joget.apps.app.model.PackageActivityPlugin;
import org.joget.apps.app.model.PackageDefinition;
import org.joget.apps.app.model.PackageParticipant;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.ext.ConsoleWebPlugin;
import org.joget.commons.util.FileLimitException;
import org.joget.commons.util.FileStore;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PropertyUtil;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.XpdlImageUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
    
    @Autowired
    PackageDefinitionDao packageDefinitionDao;
    
    @Autowired
    SetupManager setupManager;
    
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
        String systemTheme = setupManager.getSettingValue("systemTheme");
        model.addAttribute("systemTheme", systemTheme);
        model.addAttribute("appId", appDef.getId());
        model.addAttribute("version", appDef.getVersion());
        model.addAttribute("appDefinition", appDef);
        model.addAttribute("packageVersion", (appDef.getPackageDefinition() != null)?(appDef.getPackageDefinition().getVersion()):1);
        
        model.addAttribute("json", PropertyUtil.propertiesJsonLoadProcessing(AppUtil.getXpdlAndMappingJson(appDef)));
        
        return "pbuilder/pbuilder";
    }
    
    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/process/builder/save", method = RequestMethod.POST)
    @Transactional(rollbackFor = { RuntimeException.class, SQLException.class, JDBCException.class })
    public String save(Writer writer, HttpServletRequest request, @RequestParam("appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "json", required = false) String json) throws Exception {
        // verify app version
        ConsoleWebPlugin consoleWebPlugin = (ConsoleWebPlugin)pluginManager.getPlugin(ConsoleWebPlugin.class.getName());
        String page = consoleWebPlugin.verifyAppVersion(appId, version);
        if (page != null) {
            return page;
        }
        
        //retrieve from request body if the json is send in file
        json = AppUtil.getSubmittedJsonDefinition(json);
        
        JSONObject jsonObject = new JSONObject();

        AppDefinition appDef = appService.getAppDefinition(appId, version);
        String oriJson = AppUtil.getXpdlAndMappingJson(appDef);
        json = PropertyUtil.propertiesJsonStoreProcessing(oriJson, json);
        
        String diff = "";
        // get diff file in POST body
        MultipartFile diffFile = null;
        try {
            diffFile = FileStore.getFile("diffFile");
        } catch (FileLimitException e) {
            LogUtil.warn(AppUtil.class.getName(), ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
        }
        
        if (diffFile != null) {
            try {
                diff =  new String(diffFile.getBytes(), "UTF-8");
            } catch (IOException e) {
                LogUtil.error(AppUtil.class.getName(), e, "Fail to retrieve submitted diff file");
            }
        }
        
        boolean success = true;
        String error = "";
        
        try {
            if (diff != null && !diff.isEmpty()) {
                JSONObject diffObject = new JSONObject(diff);
                JSONObject newObject = new JSONObject(json);
        
                //check there is change in xpdl
                if (diffObject.has("xpdl")) {
                    String newXpdlJson = newObject.getJSONObject("xpdl").toString();
                    String xpdl = U.jsonToXml(newXpdlJson);
                    
                    try {
                        // deploy package
                        appService.deployWorkflowPackage(appId, version, xpdl.getBytes("UTF-8"), true);
                    } catch (Exception ex) {
                        success = false;
                        error = ex.getMessage().replace(":", "");
                    }
                }
                
                if (success) {
                    //compare mapping
                    PackageDefinition packageDef = packageDefinitionDao.loadAppPackageDefinition(appId, appDef.getVersion());
                    if (packageDef == null) {
                        packageDef = packageDefinitionDao.createPackageDefinition(appDef, appDef.getVersion());
                    }
                    
                    boolean hasChanges = false;
                    
                    //check there is change in activity form mapping
                    if (diffObject.has("activityForms")) {
                        hasChanges = true;
                        
                        JSONObject newFormMappings = newObject.getJSONObject("activityForms");
                        JSONObject diffFormMappings = diffObject.getJSONObject("activityForms");
                        
                        Iterator keys = diffFormMappings.keys();
                        while (keys.hasNext()) {
                            String key = keys.next().toString();
                            if (key.startsWith("_")) {
                                key = key.substring(1);
                            }
                            
                            if (!newFormMappings.has(key)) { 
                                //the mapping is removed
                                String[] temp = key.substring(1).split("::");
                                packageDef.removePackageActivityForm(temp[0], temp[1]);
                            } else {
                                //update it
                                String[] temp = key.split("::");
                                JSONObject mapping = newFormMappings.getJSONObject(key);

                                PackageActivityForm activityForm = packageDef.getPackageActivityForm(temp[0], temp[1]);
                                boolean isNew = false;
                                if (activityForm == null) {
                                    isNew = true;
                                    activityForm = new PackageActivityForm();
                                    activityForm.setProcessDefId(temp[0]);
                                    activityForm.setActivityDefId(temp[1]);
                                }

                                if (mapping.has("type") && mapping.has("formUrl") && PackageActivityForm.ACTIVITY_FORM_TYPE_EXTERNAL.equals(mapping.getString("type")) && mapping.getString("formUrl") != null) {
                                    activityForm.setType(PackageActivityForm.ACTIVITY_FORM_TYPE_EXTERNAL);
                                    activityForm.setFormUrl(mapping.getString("formUrl"));
                                    activityForm.setFormIFrameStyle(mapping.has("formIFrameStyle")?mapping.getString("formIFrameStyle"):"");
                                    activityForm.setFormId(null);
                                    activityForm.setDisableSaveAsDraft(null);
                                } else if (mapping.has("formId")) {
                                    activityForm.setType(PackageActivityForm.ACTIVITY_FORM_TYPE_SINGLE);
                                    activityForm.setFormId(mapping.getString("formId"));
                                    activityForm.setDisableSaveAsDraft(mapping.has("disableSaveAsDraft")?mapping.getBoolean("disableSaveAsDraft"):false);
                                    activityForm.setFormUrl(null);
                                    activityForm.setFormIFrameStyle(null);
                                }
                                activityForm.setAutoContinue(mapping.has("autoContinue")?mapping.getBoolean("autoContinue"):false);
                                if (isNew) {
                                    packageDef.addPackageActivityForm(activityForm);
                                }
                            }
                        }
                    }
                    
                    //check there is change in plugin mapping
                    if (diffObject.has("activityPlugins")) {
                        hasChanges = true;
                        
                        JSONObject newPluginMappings = newObject.getJSONObject("activityPlugins");
                        JSONObject diffPluginMappings = diffObject.getJSONObject("activityPlugins");
                        
                        Iterator keys = diffPluginMappings.keys();
                        while (keys.hasNext()) {
                            String key = keys.next().toString();
                            if (key.startsWith("_")) {
                                key = key.substring(1);
                            }
                            
                            if (!newPluginMappings.has(key)) { 
                                //the mapping is removed
                                String[] temp = key.split("::");
                                packageDef.removePackageActivityPlugin(temp[0], temp[1]);
                            } else {
                                //update it
                                String[] temp = key.split("::");
                                JSONObject mapping = newPluginMappings.getJSONObject(key);

                                PackageActivityPlugin activityPlugin = packageDef.getPackageActivityPlugin(temp[0], temp[1]);
                                boolean isNew = false;
                                if (activityPlugin == null) {
                                    isNew = true;
                                    activityPlugin = new PackageActivityPlugin();
                                    activityPlugin.setProcessDefId(temp[0]);
                                    activityPlugin.setActivityDefId(temp[1]);
                                }

                                activityPlugin.setPluginName(mapping.has("className")?mapping.getString("className"):"");
                                activityPlugin.setPluginProperties(mapping.has("properties")?mapping.getJSONObject("properties").toString():"");

                                if (isNew) {
                                    packageDef.addPackageActivityPlugin(activityPlugin);
                                }
                            }
                        }
                    }
                    
                    //check there is change in participant mapping
                    if (diffObject.has("participants")) {
                        hasChanges = true;
                        
                        JSONObject newParticipantMappings = newObject.getJSONObject("participants");
                        JSONObject diffParticipantMappings = diffObject.getJSONObject("participants");
                        
                        Iterator keys = diffParticipantMappings.keys();
                        while (keys.hasNext()) {
                            String key = keys.next().toString();
                            if (key.startsWith("_")) {
                                key = key.substring(1);
                            }
                            
                            if (!newParticipantMappings.has(key)) { 
                                //the mapping is removed
                                String[] temp = key.split("::");
                                packageDef.removePackageParticipant(temp[0], temp[1]);
                            } else {
                                //update it
                                String[] temp = key.split("::");
                                JSONObject mapping = newParticipantMappings.getJSONObject(key);

                                PackageParticipant participant = packageDef.getPackageParticipant(temp[0], temp[1]);
                                boolean isNew = false;
                                if (participant == null) {
                                    isNew = true;
                                    participant = new PackageParticipant();
                                    participant.setProcessDefId(temp[0]);
                                    participant.setParticipantId(temp[1]);
                                }

                                participant.setType(mapping.has("type")?mapping.getString("type"):"");
                                participant.setValue(mapping.has("value")?mapping.getString("value"):"");

                                if (PackageParticipant.TYPE_PLUGIN.equals(participant.getType()) && mapping.has("properties")) {
                                    participant.setPluginProperties(mapping.getJSONObject("properties").toString());
                                } else {
                                    participant.setPluginProperties(null);
                                }

                                if (isNew) {
                                    packageDef.addPackageParticipant(participant);
                                }
                            }
                        }
                    }

                    if (hasChanges) {
                        packageDefinitionDao.saveOrUpdate(packageDef);
                    }
                }
            }
        } catch (Exception e) {
            success = false;
            LogUtil.error(ProcessBuilderWebController.class.getName(), e, "");
        }

        if (success) {
            jsonObject.put("success", success);
            jsonObject.put("data", PropertyUtil.propertiesJsonLoadProcessing(AppUtil.getXpdlAndMappingJson(appDef)));
            
            JSONObject props = new JSONObject();
            props.put("packageVersion", appDef.getPackageDefinition().getVersion());
            
            jsonObject.put("properties", props);
        } else {
            jsonObject.put("error", error);
        }
        jsonObject.write(writer);
        
        return null;
    }
    
    @RequestMapping({"/console/app/(*:appId)/(~:version)/process/builder/json"})
    public void getJson(Writer writer, HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String json = AppUtil.getXpdlAndMappingJson(appDef);
        writer.write(PropertyUtil.propertiesJsonLoadProcessing(json));
    }
    
    /**
     * Used by process builder > advance tool > XPDL to convert xpdl to json format
     * @param writer
     * @param request
     * @param response
     * @param appId
     * @param version
     * @param xpdl
     * @throws IOException 
     */
    @RequestMapping(value = "/console/app/(*:appId)/(~:version)/process/builder/xpdlJson", method = RequestMethod.POST)
    public void getXpdlJson(Writer writer, HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version) throws IOException {
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String xpdl = "";
        // get xpdl file in POST body
        MultipartFile xpdlFile = null;
        try {
            xpdlFile = FileStore.getFile("xpdlFile");
        } catch (FileLimitException e) {
            LogUtil.warn(AppUtil.class.getName(), ResourceBundleUtil.getMessage("general.error.fileSizeTooLarge", new Object[]{FileStore.getFileSizeLimit()}));
        }

        if (xpdlFile != null) {
            try {
                xpdl =  new String(xpdlFile.getBytes(), "UTF-8");
            } catch (IOException e) {
                LogUtil.error(AppUtil.class.getName(), e, "Fail to retrieve submitted XPDL");
            }
        }
        
        String xpdlJson = U.xmlToJson(xpdl);
        writer.write(xpdlJson);
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
