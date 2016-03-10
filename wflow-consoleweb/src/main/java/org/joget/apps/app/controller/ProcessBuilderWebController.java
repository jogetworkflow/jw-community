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
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.ext.ConsoleWebPlugin;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.SetupManager;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.XpdlImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProcessBuilderWebController {

    @Autowired
    AppService appService;    
    
    @Autowired
    WorkflowManager workflowManager;    
    
    @Autowired
    PluginManager pluginManager;
    
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

    @RequestMapping(value="/console/app/(*:appId)/(~:version)/process/screenshot/(*:processDefId)")
    public String processBuilderScreenshot(ModelMap map, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "processDefId") String processDefId) throws IOException {
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
        }

        String viewer = "pbuilder/pscreenshot";
        return viewer;    
    }
    
    @RequestMapping(value="/console/app/(*:appId)/(~:version)/process/builder/screenshot/submit", method = RequestMethod.POST)
    public void processBuilderScreenshotSubmit(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "appId") String appId, @RequestParam(value = "version", required = false) String version, @RequestParam(value = "processDefId") String processDefId) throws IOException {

        // validate input
        SecurityUtil.validateStringInput(appId);        
        SecurityUtil.validateStringInput(processDefId);        
        
        // get base64 encoded image in POST body
        String imageBase64 = request.getParameter("base64data");
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
