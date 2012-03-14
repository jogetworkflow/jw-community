package org.joget.commons.util;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import org.springframework.web.multipart.MultipartFile;

public class FileManager {
    
    public static String getBaseDirectory() {
        return SetupManager.getBaseDirectory() + File.separator + "app_tempfile" + File.separator;
    }
    
    public static String storeFile(MultipartFile file) {
        if (file != null && !file.getOriginalFilename().isEmpty()) {
            String id = UuidGenerator.getInstance().getUuid();
            FileOutputStream out = null;
            String path =  id + File.separator;
            try {
                File uploadFile = new File(getBaseDirectory() + path + file.getOriginalFilename());
                if (!uploadFile.isDirectory()) {
                    //create directories if not exist
                    new File(getBaseDirectory() + path).mkdirs();

                    // write file
                    out = new FileOutputStream(uploadFile);
                    out.write(file.getBytes());
                }
            } catch (Exception ex) {
                LogUtil.error(FileManager.class.getName(), ex, "");
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception ex) {
                    }
                }
                return path + file.getOriginalFilename();
            }
        }
        return null;
    }
    
    public static File getFileByPath(String path) {
        if (path != null) {
            try {
                File file = new File(getBaseDirectory() + URLDecoder.decode(path, "UTF-8"));
                if (file.exists() && !file.isDirectory()) {
                    return file;
                }
            } catch (Exception e) {}
        }
        return null;
    }
    
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
}
