package org.joget.apps.form.service;

import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.commons.util.FileManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUtil implements ApplicationContextAware {

    static ApplicationContext appContext;

    public static ApplicationContext getApplicationContext() {
        return appContext;
    }
    
    public static void checkAndUpdateFileName(FormRowSet results, Element element, String primaryKeyValue) {
        String tableName = getTableName(element);
        checkAndUpdateFileName(results, tableName, primaryKeyValue);
    }
    
    public static void checkAndUpdateFileName(FormRowSet results, String tableName, String primaryKeyValue) {
        Set<String> existedFileName = new HashSet<String>();
        
        for (int i = 0; i < results.size(); i++) {
            FormRow row = results.get(i);
            String id = row.getId();
            if (id != null && !id.isEmpty()) {
                Map<String, String[]> tempFilePathMap = row.getTempFilePathMap();
                if (tempFilePathMap != null && !tempFilePathMap.isEmpty()) {
                    for (Iterator<String> j = tempFilePathMap.keySet().iterator(); j.hasNext();) {
                        String fieldId = j.next();
                        String[] paths = tempFilePathMap.get(fieldId);
                        List<String> newPaths = new ArrayList<String>();
                        
                        for (String path : paths) {
                            if (!path.endsWith(FileManager.THUMBNAIL_EXT)) {
                                File file = FileManager.getFileByPath(path);
                                if (file != null) {
                                    String fileName = file.getName();
                                    String uploadPath = getUploadPath(tableName, id);

                                    String newFileName = validateFileName(fileName, uploadPath, existedFileName);
                                    existedFileName.add(newFileName);

                                    if (row.containsKey(fieldId)) {
                                        String value = row.getProperty(fieldId);
                                        value = value.replace(fileName, newFileName);
                                        row.put(fieldId, value);
                                    }

                                    if (!newFileName.equals(file.getName())) {
                                        String newPath = path.replace(file.getName(), newFileName);

                                        file.renameTo(new File(file.getParentFile(), newFileName));
                                        newPaths.add(newPath);

                                        //handle thumb image
                                        String thumbPath = path + FileManager.THUMBNAIL_EXT;
                                        File thumbFile = FileManager.getFileByPath(thumbPath);
                                        if (thumbFile != null) {
                                            String newThumbFilename = newFileName + FileManager.THUMBNAIL_EXT;
                                            String newThumbPath = thumbPath.replace(thumbFile.getName(), newThumbFilename);

                                            thumbFile.renameTo(new File(thumbFile.getParentFile(), newThumbFilename));

                                            for (String key : tempFilePathMap.keySet()) {
                                                List<String> thumbs = new ArrayList<String> (Arrays.asList(tempFilePathMap.get(key)));
                                                if (thumbs.contains(thumbPath)) {
                                                    int index = thumbs.indexOf(thumbPath);
                                                    thumbs.set(index, newThumbPath);
                                                    tempFilePathMap.put(key, thumbs.toArray(new String[]{}));
                                                    break;
                                                }
                                            }
                                        }
                                    } else {
                                        newPaths.add(path);
                                    }
                                }
                            }
                        }
                        if (!newPaths.isEmpty()) {
                            tempFilePathMap.put(fieldId, newPaths.toArray(new String[]{}));
                        }
                    }
                }
            }
        }
    }
    
    public static String validateFileName(String fileName, String path, Set<String> existedFileName) {
        String tempPath = path + fileName;
        boolean fileExist = true;
        int count = 1;
        
        String name = fileName.substring(0, fileName.lastIndexOf("."));
        String ext = fileName.substring(fileName.lastIndexOf("."));
        fileName = name + ext;
        
        do {
            File file = new File(tempPath);
            
            if (file.exists() || existedFileName.contains(fileName)) {
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
        String tableName = getTableName(element);
        storeFileFromFormRowSet(results, tableName, primaryKeyValue);
    }

    public static void storeFileFromFormRowSet(FormRowSet results, String tableName, String primaryKeyValue) {
        for (FormRow row : results) {
            String id = row.getId();
            Map<String, String[]> tempFilePathMap = row.getTempFilePathMap();
            if (tempFilePathMap != null && !tempFilePathMap.isEmpty()) {
                for (String fieldId : tempFilePathMap.keySet()) {
                    String[] paths = tempFilePathMap.get(fieldId);
                    for (String path : paths) {
                        File file = FileManager.getFileByPath(path);
                        if (file != null) {
                            File tempFileDirectory = file.getParentFile();
                            FileUtil.storeFile(file, tableName, id);

                            //check if directory is empty
                            if (tempFileDirectory.listFiles().length == 0) {
                                FileManager.deleteFile(tempFileDirectory);
                            }
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
        String tableName = getTableName(element);
        storeFile(file, tableName, primaryKeyValue);
    }
    
    public static void storeFile(File file, String tableName, String primaryKeyValue) {
        if (file != null && file.exists()) {
            String path = getUploadPath(tableName, primaryKeyValue);
            File newDirectory = new File(path);
            if (!newDirectory.exists()) {
                newDirectory.mkdirs();
            }
                
            boolean result = file.renameTo(new File(newDirectory, file.getName()));
            if (!result) {
                try {
                    FileCopyUtils.copy(file, new File(newDirectory, file.getName()));
                } catch (IOException ex) {
                    LogUtil.error(FileUtil.class.getName(), ex, ex.getMessage());
                }
            }
        }
    }

    public static File getFile(String fileName, Element element, String primaryKeyValue) throws IOException {
        String path = getUploadPath(element, primaryKeyValue);
        return new File(path + fileName);
    }

    public static String getUploadPath(Element element, String primaryKeyValue) {
        String tableName = getTableName(element);
        return getUploadPath(tableName, primaryKeyValue);
    }

    public static String getUploadPath(String tableName, String primaryKeyValue) {
        String formUploadPath = SetupManager.getBaseDirectory();

        // determine base path
        SetupManager setupManager = (SetupManager) appContext.getBean("setupManager");
        String dataFileBasePath = setupManager.getSettingValue("dataFileBasePath");
        if (dataFileBasePath != null && dataFileBasePath.length() > 0) {
            formUploadPath = dataFileBasePath;
        }

        return formUploadPath + File.separator + "app_formuploads" + File.separator + tableName + File.separator + primaryKeyValue + File.separator;
    }
    
    public static String getTableName (Element element) {
        // determine table name
        String tableName = "";
        if (element != null) {
            Form form = FormUtil.findRootForm(element);
            tableName = form.getPropertyString(FormUtil.PROPERTY_TABLE_NAME);
            if (tableName == null) {
                tableName = "";
            }
        }
        return tableName;
    }

    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        FileUtil.appContext = appContext;
    }
}