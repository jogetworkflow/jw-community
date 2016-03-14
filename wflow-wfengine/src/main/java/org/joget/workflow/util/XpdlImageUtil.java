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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.joget.commons.util.DynamicDataSourceManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.joget.workflow.model.WorkflowProcess;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;

/**
 * Utility methods used to generate XPDl image
 * 
 */
public class XpdlImageUtil {
    
    public static final String IMAGE_FOLDER = "app_xpdlImages";
    public static final String IMAGE_EXTENSION = ".png";
    public static final String THUMBNAIL_PREFIX = "thumb-";
    public static final int THUMBNAIL_SIZE = 400;

    /**
     * Gets the XPDL image path in wflow/app_xpdlImages folder
     * @param processDefId
     * @return 
     */
    public static String getXpdlImagePath(String processDefId) {
        ApplicationContext appContext = WorkflowUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        WorkflowProcess process = workflowManager.getProcess(processDefId);

        File path = new File(SetupManager.getBaseDirectory(), IMAGE_FOLDER + File.separator + process.getPackageId() + File.separator);
        return path.getAbsolutePath();
    }

    /**
     * Gets the XPDL image.
     * @param designerwebBaseUrl
     * @param processDefId
     * @return 
     */
    public static File getXpdlImage(String designerwebBaseUrl, String processDefId) {
        File file = new File(getXpdlImagePath(processDefId), processDefId + IMAGE_EXTENSION);
        if (!file.exists()) {
            generateXpdlImage(designerwebBaseUrl, processDefId);
            file = new File(getXpdlImagePath(processDefId), processDefId + IMAGE_EXTENSION);
        }
        return file;
    }

    /**
     * Gets the XPDL image thumbnail. 
     * @param designerwebBaseUrl
     * @param processDefId
     * @return 
     */
    public static File getXpdlThumbnail(String designerwebBaseUrl, String processDefId) {
        File file = new File(getXpdlImagePath(processDefId), THUMBNAIL_PREFIX + processDefId + IMAGE_EXTENSION);
        if (!file.exists()) {
            generateXpdlImage(designerwebBaseUrl, processDefId);
            file = new File(getXpdlImagePath(processDefId), THUMBNAIL_PREFIX + processDefId + IMAGE_EXTENSION);
        }
        return file;
    }

    /**
     * Queue a task for XPDL image generation
     * 
     * @deprecated this is not used in v5 since the Workflow Designer is replaced
     * by a web-based Process Builder
     * 
     * @param designerwebBaseUrl
     * @param processDefId 
     */
    public static void generateXpdlImage(final String designerwebBaseUrl, final String processDefId) {
        generateXpdlImage(designerwebBaseUrl, processDefId, false);
    }

    /**
     * Queue a task for XPDL image generation
     * 
     * @deprecated this is not used in v5 since the Workflow Designer is replaced
     * by a web-based Process Builder
     * 
     * @param designerwebBaseUrl
     * @param processDefId
     * @param asynchronous 
     */
    public static void generateXpdlImage(final String designerwebBaseUrl, final String processDefId, boolean asynchronous) {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        
        TaskExecutor executor = (TaskExecutor) WorkflowUtil.getApplicationContext().getBean("xpdlImageExecutor");
        executor.execute(new XpdlImageTask(profile, designerwebBaseUrl, processDefId));
    }

    /**
     * Create the XDPL image
     * 
     * @deprecated this is not used in v5 since the Workflow Designer is replaced
     * by a web-based Process Builder
     * 
     * @param designerwebBaseUrl
     * @param processDefId 
     */
    public static void createXpdlImage(String designerwebBaseUrl, String processDefId) {
        String baseDir = getXpdlImagePath(processDefId);
        ApplicationContext appContext = WorkflowUtil.getApplicationContext();
        WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");
        WorkflowProcess process = workflowManager.getProcess(processDefId);
        byte[] xpdlBytes = workflowManager.getPackageContent(process.getPackageId(), process.getVersion());
            
        FileOutputStream fos = null;
        CloseableHttpClient httpClient = null;
        try {
            String fileName = processDefId + IMAGE_EXTENSION;
            File file = new File(baseDir);
            file.mkdirs();
            file = new File(baseDir, fileName);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            String url = designerwebBaseUrl + "/viewer/viewer.jsp?processId=" + process.getEncodedId();
            URL urlObj = new URL(url);

            HttpClientBuilder httpClientBuilder = HttpClients.custom();
            HttpPost post = new HttpPost(url);
            List<NameValuePair> data = new ArrayList<NameValuePair>();
            data.add(new BasicNameValuePair("xpdl", new String(xpdlBytes, "UTF-8")));
            data.add(new BasicNameValuePair("packageId", process.getPackageId()));
            data.add(new BasicNameValuePair("processId", processDefId));
            post.setEntity(new UrlEncodedFormEntity(data));
            post.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            if ("https".equals(urlObj.getProtocol())) {
                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
                httpClientBuilder.setSSLSocketFactory(sslsf);
            }
            
            // execute request
            httpClient = httpClientBuilder.build();
            HttpResponse response = httpClient.execute(post);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();

            try {
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
            } finally {
                if (is != null) {
                    is.close();
                }
            }

            createThumbnail(baseDir, processDefId);
        } catch (Exception ex) {
            LogUtil.error(XpdlImageUtil.class.getName(), ex, "Error generating xpdl image [processDefId=" + processDefId + "]");
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception ex) {
                LogUtil.error(XpdlImageUtil.class.getName(), ex, "");
            }
        }
    }
    
    /**
     * Create the XPDL image thumbnail
     * 
     * @deprecated this is not used in v5 since the Workflow Designer is replaced
     * by a web-based Process Builder
     * 
     * @param path
     * @param processDefId 
     */
    public static void createThumbnail(String path, String processDefId) {
        int thumbWidth = THUMBNAIL_SIZE;
        int thumbHeight = THUMBNAIL_SIZE;

        BufferedOutputStream out = null;

        try{
            Image image = Toolkit.getDefaultToolkit().getImage(new File(path, processDefId + IMAGE_EXTENSION).getAbsolutePath());
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

            out = new BufferedOutputStream(new FileOutputStream(new File(path, THUMBNAIL_PREFIX + processDefId + IMAGE_EXTENSION)));
            ImageIO.write(thumbImage, "png", out);

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