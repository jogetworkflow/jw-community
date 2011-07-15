package org.joget.workflow.util;

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;

public class XpdlImageUtil {

    public static final String IMAGE_FOLDER = "app_xpdlImages";
    public static final String IMAGE_EXTENSION = ".jpg";
    public static final String THUMBNAIL_PREFIX = "thumb-";
    public static final int THUMBNAIL_SIZE = 400;

    public static String getXpdlImagePath(String processDefId) {
        ApplicationContext appContext = WorkflowUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        WorkflowProcess process = workflowManager.getProcess(processDefId);

        return SetupManager.getBaseDirectory() + IMAGE_FOLDER + File.separator + process.getPackageId() + File.separator;
    }

    public static File getXpdlImage(String designerwebBaseUrl, String processDefId) {
        File file = new File(getXpdlImagePath(processDefId) + processDefId + IMAGE_EXTENSION);
        if (!file.exists()) {
            generateXpdlImage(designerwebBaseUrl, processDefId);
            file = new File(getXpdlImagePath(processDefId) + processDefId + IMAGE_EXTENSION);
        }
        return file;
    }

    public static File getXpdlThumbnail(String designerwebBaseUrl, String processDefId) throws Exception {
        File file = new File(getXpdlImagePath(processDefId) + THUMBNAIL_PREFIX + processDefId + IMAGE_EXTENSION);
        if (!file.exists()) {
            generateXpdlImage(designerwebBaseUrl, processDefId);
            file = new File(getXpdlImagePath(processDefId) + THUMBNAIL_PREFIX + processDefId + IMAGE_EXTENSION);
        }
        return file;
    }

    public static void generateXpdlImage(final String designerwebBaseUrl, final String processDefId) {
        generateXpdlImage(designerwebBaseUrl, processDefId, false);
    }

    public static void generateXpdlImage(final String designerwebBaseUrl, final String processDefId, boolean asynchronous) {
        final String baseDir = getXpdlImagePath(processDefId);
        ApplicationContext appContext = WorkflowUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        final WorkflowProcess process = workflowManager.getProcess(processDefId);
        final byte[] xpdlBytes = workflowManager.getPackageContent(process.getPackageId(), process.getVersion());
        Thread thread = new Thread(new Runnable() {
            public void run() {
                FileOutputStream fos = null;
                try {
                    String fileName = processDefId + IMAGE_EXTENSION;
                    File file = new File(baseDir);
                    file.mkdirs();
                    file = new File(baseDir + fileName);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    HttpClient httpClient = new HttpClient();
                    String url = designerwebBaseUrl + "/jwdesigner/viewer/viewer.jsp?processId=" + process.getEncodedId();
                    PostMethod post = new PostMethod(url);
                    NameValuePair[] data = {
                        new NameValuePair("xpdl", new String(xpdlBytes, "UTF-8")),
                        new NameValuePair("packageId", process.getPackageId()),
                        new NameValuePair("processId", processDefId)
                    };
                    post.setRequestBody(data);
                    post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    httpClient.executeMethod(post);

                    InputStream is = post.getResponseBodyAsStream();

                    byte[] buffer = new byte[1024];
                    int byteReaded = is.read(buffer);
                    while (byteReaded != -1) {
                        bos.write(buffer, 0, byteReaded);
                        byteReaded = is.read(buffer);
                    }
                    bos.flush();

                    // output to file
                    byte[] contents = bos.toByteArray();
                    ByteArrayInputStream bis = new ByteArrayInputStream(contents);
                    fos = new FileOutputStream(file);
                    buffer = new byte[1024];
                    byteReaded = bis.read(buffer);
                    while (byteReaded != -1) {
                        fos.write(buffer, 0, byteReaded);
                        byteReaded = bis.read(buffer);
                    }
                    fos.flush();
                    
                    createThumbnail(baseDir, processDefId);
                } catch (Exception ex) {
                    LogUtil.error(XpdlImageUtil.class.getName(), ex, "Error generating xpdl image [processDefId=" + processDefId + "]");
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (Exception ex) {
                        LogUtil.error(XpdlImageUtil.class.getName(), ex, "");
                    }
                }
            }
        });
        if (asynchronous) {
            thread.start();
        }
        else {
            thread.run();
        }
    }

    public static void createThumbnail(String path, String processDefId) {
        int thumbWidth = THUMBNAIL_SIZE;
        int thumbHeight = THUMBNAIL_SIZE;

        BufferedOutputStream out = null;

        try{
            Image image = Toolkit.getDefaultToolkit().getImage(path + processDefId + IMAGE_EXTENSION);
            MediaTracker mediaTracker = new MediaTracker(new Container());
            mediaTracker.addImage(image, 0);
            mediaTracker.waitForID(0);

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

            out = new BufferedOutputStream(new FileOutputStream(path + THUMBNAIL_PREFIX + processDefId + IMAGE_EXTENSION));
            ImageIO.write(thumbImage, "jpeg", out);

            out.flush();
        } catch (Exception ex) {
            LogUtil.error(XpdlImageUtil.class.getName(), ex, "Error generating xpdl thumbnail [processDefId=" + processDefId + "]");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception ex) {
                LogUtil.error(XpdlImageUtil.class.getName(), ex, "");
            }
        }
    }
}