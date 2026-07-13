package org.joget.apps.form.service;

import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.commons.util.FileManager;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.UuidGenerator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUtil implements ApplicationContextAware {
    public static final String PATH_VARIABLE = "{tempFilePath}";
    public static final String COPY_ROW_VARIABLE = "{copyRow}";

    static ApplicationContext appContext;

    /**
     * Utility method to retrieve the ApplicationContext of the system
     * @return 
     */
    public static ApplicationContext getApplicationContext() {
        return appContext;
    }
    
    /**
     * Method used to check for the duplicate file name of files in the 
     * FormRowSet object against the existing files in the target upload directory.
     * It will update the file name by appending with number if found duplicated.
     * @param results
     * @param element a Form object
     * @param primaryKeyValue 
     */
    public static void checkAndUpdateFileName(FormRowSet results, Element element, String primaryKeyValue) {
        String tableName = getTableName(element);
        checkAndUpdateFileName(results, tableName, primaryKeyValue);
    }
    
    /**
     * Method used to check for the duplicate file name of files in the 
     * FormRowSet object against the existing files in the target upload directory.
     * It will update the file name by appending with number if found duplicated.
     * @param results
     * @param tableName
     * @param primaryKeyValue 
     */
    public static void checkAndUpdateFileName(FormRowSet results, String tableName, String primaryKeyValue) {

        resolveCopiedRowFiles(results, tableName);
        duplicateSharedTempFiles(results);

        Map<String, Set<String>> existedFileNameByPath = new HashMap<String, Set<String>>();
        
        for (int i = 0; i < results.size(); i++) {
            FormRow row = results.get(i);
            String id = row.getId();
            if (id != null && !id.isEmpty()) {
                Map<String, String[]> tempFilePathMap = row.getTempFilePathMap();
                if (tempFilePathMap != null && !tempFilePathMap.isEmpty()) {
                    for (Iterator<String> j = tempFilePathMap.keySet().iterator(); j.hasNext();) {
                        String fieldId = j.next();
                        String[] paths = tempFilePathMap.get(fieldId);
                        
                        //if field id exist in deleteFilePath, do not need to update file name
                        boolean replaceFile = false;
                        if (row.getDeleteFilePaths(fieldId) != null) {
                            replaceFile = true;
                        }
                        
                        List<String> newPaths = new ArrayList<String>();
                        
                        for (String path : paths) {
                            if (!path.endsWith(FileManager.THUMBNAIL_EXT)) {
                                File file = FileManager.getFileByPath(path);
                                if (file != null) {
                                    String fileName = file.getName();
                                    String uploadPath = getUploadPath(tableName, id);

                                    Set<String> pathExistedNames = existedFileNameByPath.get(uploadPath);
                                    if (pathExistedNames == null) {
                                        pathExistedNames = new HashSet<String>();
                                        existedFileNameByPath.put(uploadPath, pathExistedNames);
                                    }
                                    String newFileName = validateFileName(fileName, uploadPath, pathExistedNames, replaceFile);
                                    pathExistedNames.add(newFileName);
                                    
                                    if (!newFileName.equals(file.getName())) {
                                        if (row.containsKey(fieldId)) {
                                            String value = row.getProperty(fieldId);
                                            if (fileName.contains(";")) {
                                                // When the file name has a ";", replace the first occurrence
                                                value = value.replaceFirst(StringUtil.escapeRegex(fileName), StringUtil.escapeRegex(newFileName));
                                            } else {
                                                // First occurrence
                                                int index = value.indexOf(fileName);
                                                if (index != -1) {
                                                    // Second occurrence
                                                    index = value.indexOf(fileName, index + 1);
                                                    if (index != -1) {
                                                        value = value.substring(0, index) + newFileName + value.substring(index + fileName.length());
                                                    } else {
                                                        // When removeFile is false and the same file is added after it's deleted
                                                        value = value.replaceFirst(StringUtil.escapeRegex(fileName), StringUtil.escapeRegex(newFileName));
                                                    }
                                                }
                                            }
                                            row.put(fieldId, value);
                                        }
                                        
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
    
    /**
     * Validate the file name against the existing files in the target upload directory.
     * @param fileName
     * @param path
     * @param existedFileName a set of file names which not yet exist in the target upload directory but the current checking file should not has the same file name in it.  
     * @return the new file name with number appended when duplicate file is found.
     */
    public static String validateFileName(String fileName, String path, Set<String> existedFileName) {
        return validateFileName(fileName, path, existedFileName, false);
    }
    
    /**
     * Validate the file name against the existing files in the target upload directory.
     * @param fileName
     * @param path
     * @param existedFileName a set of file names which not yet exist in the target upload directory but the current checking file should not has the same file name in it.  
     * @param replaceFile
     * @return the new file name with number appended when duplicate file is found.
     */
    public static String validateFileName(String fileName, String path, Set<String> existedFileName, boolean replaceFile) {
        boolean fileExist = true;
        int count = 1;
        
        String name = fileName;
        String ext = "";
        if (fileName.contains(".")) {
            name = fileName.substring(0, fileName.lastIndexOf("."));
            ext = fileName.substring(fileName.lastIndexOf("."));
        }
        if (name.contains(";")) {
            name = name.replaceAll(StringUtil.escapeRegex(";"), "");
        }
        fileName = name + ext;
        String tempPath = path + fileName;
        
        if (!replaceFile) {
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
        }
        
        return fileName;
    }

    /**
     * Store files in the FormRowSet to target upload directory of a form data record
     * @param results
     * @param element a Form object
     * @param primaryKeyValue 
     */
    public static void storeFileFromFormRowSet(FormRowSet results, Element element, String primaryKeyValue) {
        String tableName = getTableName(element);
        storeFileFromFormRowSet(results, tableName, primaryKeyValue);
    }

    /**
     * Store files in the FormRowSet to target upload directory of a form data record
     * @param results
     * @param tableName
     * @param primaryKeyValue 
     */
    public static void storeFileFromFormRowSet(FormRowSet results, String tableName, String primaryKeyValue) {
        for (FormRow row : results) {
            String id = row.getId();
            
            //delete files
            Map<String, String[]> deleteFilePathMap = row.getDeleteFilePathMap();
            if (deleteFilePathMap != null && !deleteFilePathMap.isEmpty()) {
                for (String fieldId : deleteFilePathMap.keySet()) {
                    String[] paths = deleteFilePathMap.get(fieldId);
                    for (String path : paths) {
                        if (!path.isEmpty()) {
                            try {
                                File file = FileUtil.getFile(path, tableName, id);
                                if (file != null) {
                                    File thumb = FileUtil.getFile(path + FileManager.THUMBNAIL_EXT, tableName, id);
                                    if (thumb != null) {
                                        thumb.delete();
                                    }
                                    
                                    file.delete();
                                }
                            } catch (Exception e) {
                                LogUtil.error(FileUtil.class.getName(), e, path);
                            }
                        }
                    }
                }
            }
            
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
    
    /**
     * Resolves COPY_ROW_VARIABLE sentinels ({@code {copyRow}<sourceRowId>/<fileName>})
     * emitted when a spreadsheet row with an already-saved file/image is copied to another row.
     * @param results
     * @param tableName
     */
    protected static void resolveCopiedRowFiles(FormRowSet results, String tableName) {
        if (results == null || results.isEmpty()) {
            return;
        }
        for (FormRow row : results) {
            Map<String, String[]> tempFilePathMap = row.getTempFilePathMap();
            if (tempFilePathMap == null || tempFilePathMap.isEmpty()) {
                continue;
            }
            for (Map.Entry<String, String[]> entry : tempFilePathMap.entrySet()) {
                String[] paths = entry.getValue();
                if (paths == null || paths.length == 0) continue;
                boolean changed = false;
                String[] newPaths = paths.clone();
                for (int i = 0; i < newPaths.length; i++) {
                    String path = newPaths[i];
                    if (path == null || !path.startsWith(COPY_ROW_VARIABLE)) continue;
                    // thumbnails are co-located with their main file when the main file is copied
                    if (path.endsWith(FileManager.THUMBNAIL_EXT)) continue;
                    String ref = path.substring(COPY_ROW_VARIABLE.length());
                    int slash = ref.indexOf('/');
                    if (slash <= 0 || slash >= ref.length() - 1) continue;
                    String srcRowId = ref.substring(0, slash);
                    String fileName = ref.substring(slash + 1);
                    try {
                        File src = getFile(fileName, tableName, srcRowId);
                        if (src != null && src.exists()) {
                            String copiedPath = copyToFreshTempDir(src);
                            if (copiedPath != null) {
                                newPaths[i] = copiedPath;
                                changed = true;
                                String newDirId = copiedPath.substring(0, copiedPath.indexOf(File.separator));
                                File thumbSrc = getFile(fileName + FileManager.THUMBNAIL_EXT, tableName, srcRowId);
                                colocateThumbnail(tempFilePathMap, thumbSrc, path + FileManager.THUMBNAIL_EXT, newDirId, src.getName());
                            }
                        }
                    } catch (Exception ex) {
                        LogUtil.error(FileUtil.class.getName(), ex, "Failed to resolve copied row file: " + path);
                    }
                }
                if (changed) {
                    entry.setValue(newPaths);
                }
            }
        }
    }

    /**
     * Copies a relocated file's thumbnail into the same temp dir as its main
     * file and updates the matching map entry, preserving the {@code <mainPath>.thumb.jpg} linkage
     * that {@link #checkAndUpdateFileName} needs when renaming duplicate file names.
     * @param oldThumbValue
     * @param mainFileName
     */
    private static void colocateThumbnail(Map<String, String[]> tempFilePathMap, File thumbSrc, String oldThumbValue, String newDirId, String mainFileName) {
        if (thumbSrc == null || !thumbSrc.exists()) {
            return;
        }
        // only copy if the map actually references the thumbnail, otherwise the copy would be orphaned in temp
        boolean referenced = false;
        for (String[] values : tempFilePathMap.values()) {
            if (values != null && Arrays.asList(values).contains(oldThumbValue)) {
                referenced = true;
                break;
            }
        }
        if (!referenced) {
            return;
        }
        try {
            File destDir = new File(FileManager.getBaseDirectory(), newDirId);
            destDir.mkdirs();
            FileCopyUtils.copy(thumbSrc, new File(destDir, mainFileName + FileManager.THUMBNAIL_EXT));
            String newThumbPath = newDirId + File.separator + mainFileName + FileManager.THUMBNAIL_EXT;
            for (Map.Entry<String, String[]> entry : tempFilePathMap.entrySet()) {
                String[] values = entry.getValue();
                if (values == null) continue;
                boolean changed = false;
                for (int k = 0; k < values.length; k++) {
                    if (oldThumbValue.equals(values[k])) {
                        values[k] = newThumbPath;
                        changed = true;
                    }
                }
                if (changed) {
                    entry.setValue(values);
                }
            }
        } catch (Exception ex) {
            LogUtil.error(FileUtil.class.getName(), ex, "Failed to co-locate thumbnail: " + oldThumbValue);
        }
    }

    /**
     * @param results 
     */
    protected static void duplicateSharedTempFiles(FormRowSet results) {
        if (results == null || results.isEmpty()) {
            return;
        }
        
        // pass 1: count references to each temp path across all rows
        Map<String, Integer> pathRefCount = new HashMap<String, Integer>();
        for (FormRow row : results) {
            Map<String, String[]> tempFilePathMap = row.getTempFilePathMap();
            if (tempFilePathMap == null || tempFilePathMap.isEmpty()) {
                continue;
            }
            for (String[] paths : tempFilePathMap.values()) {
                if (paths == null) continue;
                for (String path : paths) {
                    if (path != null && !path.isEmpty()) {
                        Integer count = pathRefCount.get(path);
                        pathRefCount.put(path, (count == null) ? 1 : count + 1);
                    }
                }
            }
        }
        
        // early exit if nothing is shared
        boolean anyShared = false;
        for (Integer c : pathRefCount.values()) {
            if (c != null && c > 1) {
                anyShared = true;
                break;
            }
        }
        if (!anyShared) {
            return;
        }
        
        // pass 2: for each row that references a shared path beyond the first occurrence,
        // copy the temp file to a fresh UUID temp dir and rewrite this row's path entry
        Set<String> firstSeen = new HashSet<String>();
        for (FormRow row : results) {
            Map<String, String[]> tempFilePathMap = row.getTempFilePathMap();
            if (tempFilePathMap == null || tempFilePathMap.isEmpty()) {
                continue;
            }
            for (Map.Entry<String, String[]> entry : tempFilePathMap.entrySet()) {
                String[] paths = entry.getValue();
                if (paths == null || paths.length == 0) continue;
                boolean changed = false;
                String[] newPaths = paths.clone();
                for (int i = 0; i < newPaths.length; i++) {
                    String path = newPaths[i];
                    if (path == null || path.isEmpty()) continue;
                    // thumbnails are co-located with their main file when the main file is copied
                    if (path.endsWith(FileManager.THUMBNAIL_EXT)) continue;
                    Integer count = pathRefCount.get(path);
                    if (count == null || count <= 1) continue;
                    if (firstSeen.add(path)) {
                        // first occurrence keeps the original temp file
                        continue;
                    }
                    File src = FileManager.getFileByPath(path);
                    if (src == null) continue;
                    String copiedPath = copyToFreshTempDir(src);
                    if (copiedPath != null) {
                        newPaths[i] = copiedPath;
                        changed = true;
                        String newDirId = copiedPath.substring(0, copiedPath.indexOf(File.separator));
                        File thumbSrc = FileManager.getFileByPath(path + FileManager.THUMBNAIL_EXT);
                        colocateThumbnail(tempFilePathMap, thumbSrc, path + FileManager.THUMBNAIL_EXT, newDirId, src.getName());
                    }
                }
                if (changed) {
                    entry.setValue(newPaths);
                }
            }
        }
    }
    
    /**
     * @param src the existing temp file to copy
     * @return relative path of the new copy, or null on failure
     */
    public static String copyToFreshTempDir(File src) {
        if (src == null || !src.exists() || src.isDirectory()) {
            return null;
        }
        try {
            String id = UuidGenerator.getInstance().getUuid();
            File destDir = new File(FileManager.getBaseDirectory(), id);
            destDir.mkdirs();
            File dest = new File(destDir, src.getName());
            FileCopyUtils.copy(src, dest);
            
            return id + File.separator + src.getName();
        } catch (IOException ex) {
            LogUtil.error(FileUtil.class.getName(), ex, "Failed to copy shared temp file: " + src.getAbsolutePath());
            return null;
        }
    }
    
    /**
     * Store file to target upload directory of a form data record
     * @param file
     * @param element A Form object
     * @param primaryKeyValue 
     */
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
    
    /**
     * Store file to target upload directory of a form data record
     * @param file
     * @param element A Form object
     * @param primaryKeyValue 
     */
    public static void storeFile(File file, Element element, String primaryKeyValue) {
        String tableName = getTableName(element);
        storeFile(file, tableName, primaryKeyValue);
    }
    
    /**
     * Store file to target upload directory of a form data record
     * @param file
     * @param tableName
     * @param primaryKeyValue 
     */
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

    /**
     * Gets the file from target upload directory of a form data record
     * @param fileName
     * @param element A Form object
     * @param primaryKeyValue
     * @return
     * @throws IOException 
     */
    public static File getFile(String fileName, Element element, String primaryKeyValue) throws IOException {
        // validate input
        String normalizedFileName = SecurityUtil.normalizedFileName(fileName);
        
        // get file
        String path = getUploadPath(element, primaryKeyValue);
        return new File(path + normalizedFileName);
    }
    
    /**
     * Gets the file from target upload directory of a form data record
     * @param fileName
     * @param tableName
     * @param primaryKeyValue
     * @return
     * @throws IOException 
     */
    public static File getFile(String fileName, String tableName, String primaryKeyValue) throws IOException {
        // validate input
        String normalizedFileName = SecurityUtil.normalizedFileName(fileName);
                
        String path = getUploadPath(tableName, primaryKeyValue);
        return new File(path + normalizedFileName);
    }
    
    /**
     * Delete the file from target upload directory of a form data record
     * @param fileName
     * @param element A Form object
     * @param primaryKeyValue
     */
    public static void deleteFile(String fileName, Element element, String primaryKeyValue) {
        try {
            File file = getFile(fileName, element, primaryKeyValue);

            if (file != null && file.exists()) {
                file.delete();
                
                //delete thumbnail 
                FileUtil.deleteFile(fileName + FileManager.THUMBNAIL_EXT, element, primaryKeyValue);
            }
        } catch (Exception e) {
            LogUtil.error(FileUtil.class.getName(), e, fileName);
        }
    }
    
    /**
     * Gets the file from target upload directory of a form data record
     * @param fileName
     * @param tableName
     * @param primaryKeyValue
     */
    public static void deleteFile(String fileName, String tableName, String primaryKeyValue) {
        try {
            File file = getFile(fileName, tableName, primaryKeyValue);

            if (file != null && file.exists()) {
                file.delete();
                
                //delete thumbnail
                FileUtil.deleteFile(fileName + FileManager.THUMBNAIL_EXT, tableName, primaryKeyValue);
            }
        } catch (Exception e) {
            LogUtil.error(FileUtil.class.getName(), e, fileName);
        }
    }
    
    /**
     * Delete the files from target upload directory of a form data record
     * @param element A Form object
     * @param primaryKeyValue
     */
    public static void deleteFiles(Element element, String primaryKeyValue) {
        try {
            String path = getUploadPath(element, primaryKeyValue);
            File directory = new File(path);

            if (directory.exists()) {
                FileUtils.deleteDirectory(directory);
            }
        } catch (Exception e) {
            LogUtil.error(FileUtil.class.getName(), e, primaryKeyValue);
        }
    }
    
    /**
     * Delete the files from target upload directory of a form data record
     * @param tableName
     * @param primaryKeyValue
     */
    public static void deleteFiles(String tableName, String primaryKeyValue) {
        try {
            String path = getUploadPath(tableName, primaryKeyValue);
            File directory = new File(path);

            if (directory.exists()) {
                FileUtils.deleteDirectory(directory);
            }
        } catch (Exception e) {
            LogUtil.error(FileUtil.class.getName(), e, primaryKeyValue);
        }
    }

    /**
     * Gets the target upload directory of a form data record
     * @param element A Form object
     * @param primaryKeyValue
     * @return 
     */
    public static String getUploadPath(Element element, String primaryKeyValue) {
        String tableName = getTableName(element);
        return getUploadPath(tableName, primaryKeyValue);
    }

    /**
     * Gets the target upload directory of a form data record
     * @param tableName
     * @param primaryKeyValue
     * @return 
     */
    public static String getUploadPath(String tableName, String primaryKeyValue) {
        // validate input
        String normalizedTableName = SecurityUtil.normalizedFileName(tableName);
        
        String normalizedPrimaryKeyValue = SecurityUtil.normalizedFileName(primaryKeyValue);

        String formUploadPath = SetupManager.getBaseDirectory();

        // determine base path
        SetupManager setupManager = (SetupManager) appContext.getBean("setupManager");
        String dataFileBasePath = setupManager.getSettingValue("dataFileBasePath");
        if (dataFileBasePath != null && dataFileBasePath.length() > 0) {
            formUploadPath = dataFileBasePath;
        }

        return formUploadPath + File.separator + "app_formuploads" + File.separator + normalizedTableName + File.separator + normalizedPrimaryKeyValue + File.separator;
    }
    
    /**
     * Method used to gets the table name from the properties of a Form object
     * @param element A Form object
     * @return 
     */
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
    
    /**
     * Method used by the system to set an ApplicationContext object
     * @param appContext
     * @throws BeansException 
     */
    public void setApplicationContext(ApplicationContext appContext) throws BeansException {
        FileUtil.appContext = appContext;
    }
}