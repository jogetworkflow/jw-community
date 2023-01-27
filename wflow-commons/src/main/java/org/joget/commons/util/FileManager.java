package org.joget.commons.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility methods used by the system to manager temporary files
 * 
 */
public class FileManager {
    public final static Integer THUMBNAIL_SIZE = 60; 
    public final static String THUMBNAIL_EXT = ".thumb.jpg"; 
    
    public static final long CLEANER_INTERVAL_MS = 12 * 60 * 60 * 1000; // 12 hours
    public static final long EXPIRES_MS = 24 * 60 * 60 * 1000; // 24 hours
    
    protected static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    
    static {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                FileManager.performCleanTempFile();
            }
        }, 10000, CLEANER_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Gets directory path to temporary files folder
     * @return 
     */
    public static String getBaseDirectory() {
        return SetupManager.getBaseDirectory() + File.separator + "app_tempfile" + File.separator;
    }
    
    /**
     * Stores files post to the HTTP request to temporary files folder
     * @param file
     * @param customFileName
     * @return the relative path of the stored temporary file, NULL if failure.
     */
    public static String storeFile(MultipartFile file, String customFileName) {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            if (customFileName == null || customFileName.isEmpty()) {
                customFileName = file.getOriginalFilename();
            }
            
            String id = UuidGenerator.getInstance().getUuid();
            String path =  id + File.separator;
            String filename = path;
            try {
                try {
                    filename += URLDecoder.decode(customFileName, "UTF-8");
                } catch (Exception e) {
                    filename += customFileName.replaceAll("%", ""); //remove % to prevent java.lang.IllegalArgumentException in future use
                }
                filename = SecurityUtil.normalizedFileName(filename);
                File uploadFile = new File(getBaseDirectory(), filename);
                if (!uploadFile.isDirectory()) {
                    //create directories if not exist
                    new File(getBaseDirectory(), path).mkdirs();

                    // write file
                    file.transferTo(uploadFile);
                }
            } catch (Exception ex) {
                LogUtil.error(FileManager.class.getName(), ex, "");
            }
            return filename;
        }
        return null;
    }
    
    /**
     * Stores files post to the HTTP request to temporary files folder
     * @param file
     * @return the relative path of the stored temporary file, NULL if failure.
     */
    public static String storeFile(MultipartFile file) {
        return storeFile(file, null);
    }
    
    /**
     * Gets the temporary file from temporary files folder by relative path
     * @param path
     * @return 
     */
    public static File getFileByPath(String path) {
        if (path != null) {
            path = SecurityUtil.normalizedFileName(path);
            try {
                File file = new File(getBaseDirectory(), path);
                if (file.exists() && !file.isDirectory()) {
                    return file;
                }
            } catch (Exception e) {}
        }
        return null;
    }
    
    /**
     * Deletes the temporary file from temporary files folder by relative path
     * @param path 
     */
    public static void deleteFileByPath(String path) {
        File file = getFileByPath(path);
        File directory = file.getParentFile();
        
        if (file != null && file.exists()) {
            file.delete();
        }

        if (directory != null && directory.exists()) {
            directory.delete();
        }
    }
    
    /**
     * Deletes a file
     * @param file 
     */
    public static void deleteFile(File file) {
        if (!file.exists()) {
            return;
        }
        
        if (file.isDirectory()){
            for (File f : file.listFiles()){
                deleteFile(f);
            }
        }
        file.delete();
    }
    
    /**
     * Generates a thumbnail of a image file in temporary files folder by relative path
     * @param path
     * @param thumbWidth
     * @param thumbHeight 
     */
    public static void createThumbnail(String path, Integer thumbWidth, Integer thumbHeight) {
        if (thumbWidth == null) {
            thumbWidth = THUMBNAIL_SIZE;
        }
        if (thumbHeight == null) {
            thumbHeight = THUMBNAIL_SIZE;
        }
        
        BufferedOutputStream out = null;

        try{
            try {
                path = URLDecoder.decode(path, "UTF-8");
            } catch (Exception e) {
                path = path.replaceAll("%", ""); //remove % to prevent java.lang.IllegalArgumentException in future use
            }
            path = SecurityUtil.normalizedFileName(path);
            File imageFile = new File(getBaseDirectory(), path);
            Image image = Toolkit.getDefaultToolkit().getImage(imageFile.getAbsolutePath());
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
            
            out = new BufferedOutputStream(new FileOutputStream(imageFile.getAbsolutePath() + THUMBNAIL_EXT));
            ImageIO.write(thumbImage, "jpeg", out);

            out.flush();
        } catch (Exception ex) {
            LogUtil.error(FileManager.class.getName(), ex, "");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception ex) {
                LogUtil.error(FileManager.class.getName(), ex, "");
            }
        }
    }
    
    public static void performCleanTempFile() {
        LogUtil.debug(FileManager.class.getName(), "Performing temp file cleaning...");
        try {
            if (HostManager.isVirtualHostEnabled()) {
                //loop all profiles
                Properties profiles = DynamicDataSourceManager.getProfileProperties();
                Set<String> profileSet = new HashSet(profiles.values());
                for (String profile : profileSet) {
                    if (profile.contains(",")) {
                        continue;
                    }
                    LogUtil.debug(FileManager.class.getName(), "Performing temp file cleaning for " + profile);
                    String baseDirectory = SetupManager.getBaseSharedDirectory() + File.separator + SetupManager.DIRECTORY_PROFILES + File.separator + profile + File.separator + "app_tempfile" + File.separator;
                    cleanDirectory(baseDirectory);
                }
            } else {
                cleanDirectory(getBaseDirectory());
            }
        } catch (Exception e) {
            LogUtil.error(FileManager.class.getName(), e, "");
        }
        LogUtil.debug(FileManager.class.getName(), "Performing temp file cleaning completed.");
    }
    
    protected static void cleanDirectory(String dirPath) throws IOException {
        File dir = new File(dirPath);
        if (dir.exists() && dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    long diff = new Date().getTime() - f.lastModified();
                    if (diff > EXPIRES_MS) {
                        FileUtils.deleteDirectory(f);
                    }
                }
            }
        }
    }
    
    /**
     * Performs shutdown tasks e.g. stops the ScheduledExecutorService thread
     */
    public static void shutdown() {
        scheduledExecutorService.shutdown();
    }
}
