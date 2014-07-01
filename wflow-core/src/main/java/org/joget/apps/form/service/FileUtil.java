package org.joget.apps.form.service;

import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.commons.util.FileManager;
import static org.joget.commons.util.FileManager.getBaseDirectory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUtil implements ApplicationContextAware {

    static ApplicationContext appContext;

    public static ApplicationContext getApplicationContext() {
        return appContext;
    }
    
    public static void checkAndUpdateFileName(FormRowSet results, Element element, String primaryKeyValue) {
        for (int i = 0; i < results.size(); i++) {
            FormRow row = results.get(i);
            String id = row.getId();
            if (id != null && !id.isEmpty()) {
                Map<String, String> tempFilePathMap = row.getTempFilePathMap();
                if (tempFilePathMap != null && !tempFilePathMap.isEmpty()) {
                    for (Iterator<String> j = tempFilePathMap.keySet().iterator(); j.hasNext();) {
                        String fieldId = j.next();
                        String path = tempFilePathMap.get(fieldId);
                        File file = FileManager.getFileByPath(path);
                        if (file != null) {
                            String fileName = file.getName();
                            String uploadPath = getUploadPath(element, id);

                            fileName = validateFileName(fileName, uploadPath);

                            if (row.containsKey(fieldId)) {
                                row.put(fieldId, fileName);
                            }
                            
                            if (!fileName.equals(file.getName())) {
                                String newPath = path.replace(file.getName(), fileName);
                                
                                file.renameTo(new File(file.getParentFile(), fileName));
                                tempFilePathMap.put(fieldId, newPath);
                            }
                        }
                    }
                }
            }
        }
            
    }
    
    public static String validateFileName(String fileName, String path) {
        String tempPath = path + fileName;
        boolean fileExist = true;
        int count = 1;
        
        String ext = "", name = "";
                
        if (fileName.endsWith(FileManager.THUMBNAIL_EXT)) {
            ext = FileManager.THUMBNAIL_EXT;
            fileName = fileName.replace(FileManager.THUMBNAIL_EXT, "");
        }
        
        name = fileName.substring(0, fileName.lastIndexOf("."));
        ext = fileName.substring(fileName.lastIndexOf(".")) + ext;
        fileName = name + ext;
        
        do {
            File file = new File(tempPath);
            
            if (file.exists()) {
                fileName = name + "("+count+")" + ext;
                tempPath = path + fileName;
            } else {
                fileExist = false;
            }
            count ++;
        } while (fileExist);
        
        return fileName;
    }

    public static void storeFileFromFormRowSet(FormRowSet results, Element element, String primaryKeyValue) {
        for (int i = 0; i < results.size(); i++) {
            FormRow row = results.get(i);
            String id = row.getId();
            Map<String, String> tempFilePathMap = row.getTempFilePathMap();
            if (tempFilePathMap != null && !tempFilePathMap.isEmpty()) {
                for (Iterator<String> j = tempFilePathMap.keySet().iterator(); j.hasNext();) {
                    String fieldId = j.next();
                    String path = tempFilePathMap.get(fieldId);
                    File file = FileManager.getFileByPath(path);
                    if (file != null) {
                        File tempFileDirectory = file.getParentFile();
                        FileUtil.storeFile(file, element, id);
                        
                        //check if directory is empty
                        if (tempFileDirectory.listFiles().length == 0) {
                            FileManager.deleteFile(tempFileDirectory);
                        }
                    }
                }
            }
        }
    }
    
    public static void storeFile(MultipartFile file, Element element, String primaryKeyValue) {
        FileOutputStream out = null;
        try {
            String path = getUploadPath(element, primaryKeyValue);

            File uploadFile = new File(path + file.getOriginalFilename());
            if (!uploadFile.isDirectory()) {
                //create directories if not exist
                new File(path).mkdirs();

                // write file
                out = new FileOutputStream(uploadFile);
                out.write(file.getBytes());
            }
        } catch (Exception ex) {
            LogUtil.error(FileUtil.class.getName(), ex, "");
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ex) {
                }
            }
        }
    }
    
    public static void storeFile(File file, Element element, String primaryKeyValue) {
        if (file != null && file.exists()) {
            String path = getUploadPath(element, primaryKeyValue);
            File newDirectory = new File(path);
            if (!newDirectory.exists()) {
                newDirectory.mkdirs();
            }
                
            file.renameTo(new File(newDirectory, file.getName()));
        }
    }

    public static File getFile(String fileName, Element element, String primaryKeyValue) throws IOException {
        String path = getUploadPath(element, primaryKeyValue);
        return new File(path + fileName);
    }

    public static String getUploadPath(Element element, String primaryKeyValue) {
        String formUploadPath = SetupManager.getBaseDirectory();

        // determine base path
        SetupManager setupManager = (SetupManager) appContext.getBean("setupManager");
        String dataFileBasePath = setupManager.getSettingValue("dataFileBasePath");
        if (dataFileBasePath != null && dataFileBasePath.length() > 0) {
            formUploadPath = dataFileBasePath;
        }

        // determine table name
        String tableName = "";
        if (element != null) {
            Form form = FormUtil.findRootForm(element);
            tableName = form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME);
            if (tableName == null) {
                tableName = "";
            }
        }

        return formUploadPath + "app_formuploads" + File.separator + tableName + File.separator + primaryKeyValue + File.separator;
    }

    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        FileUtil.appContext = appContext;
    }
}
