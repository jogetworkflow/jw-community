package org.joget.apps.workflow.controller;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.workflow.util.XpdlImageUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class XpdlImageWebController {

    @RequestMapping("/console/images/xpdl/thumbnail/(*:processDefId)")
    public void getXpdlThumbnail(OutputStream out, @RequestParam("processDefId") String processDefId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        processDefId = processDefId.replaceAll(":", "#");

        File file = null;
        try {
            file = new File(XpdlImageUtil.getXpdlImagePath(processDefId), XpdlImageUtil.THUMBNAIL_PREFIX + processDefId + XpdlImageUtil.IMAGE_EXTENSION);
        } catch(Exception e) {
            //ingore
        }
        if (file == null || !file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        byte[] bbuf = new byte[65536];
        DataInputStream in = new DataInputStream(new FileInputStream(file));

        int length = 0;
        
        try {
            response.setContentType("image/png");
            while ((in != null) && ((length = in.read(bbuf)) != -1)) {
                out.write(bbuf, 0, length);
            }
        } finally {
            in.close();
            out.flush();
            out.close();
        }
    }

    @RequestMapping("/console/images/xpdl/(*:processDefId)")
    public void getXpdlImage(OutputStream out, @RequestParam("processDefId") String processDefId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        processDefId = processDefId.replaceAll(":", "#");
        
        File file = null;
        try {
            file = new File(XpdlImageUtil.getXpdlImagePath(processDefId), processDefId + XpdlImageUtil.IMAGE_EXTENSION);
        } catch(Exception e) {
            //ingore
        }
        if (file == null || !file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        byte[] bbuf = new byte[65536];
        DataInputStream in = new DataInputStream(new FileInputStream(file));

        int length = 0;
        
        try {
            response.setContentType("image/png");
            while ((in != null) && ((length = in.read(bbuf)) != -1)) {
                out.write(bbuf, 0, length);
            }
        } finally {
            in.close();
            out.flush();
            out.close();
        }
    }
}
