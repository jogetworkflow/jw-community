package org.joget.commons.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.joget.commons.spring.model.Setting;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class to act as a temporary holder to manage the files posted in a HTTP request
 * 
 */
public class FileStore {
    private static Map<String, Integer> fileSizeLimitMap = new HashMap<String, Integer>(); 
    
    /**
     * Get the file size limit of the system in byte
     * @return 
     */
    public static long getFileSizeLimitLong() {
        Integer fileSizeLimit = getFileSizeLimit();
        if (fileSizeLimit != -1) {
            return fileSizeLimit * 1024 * 1024L;
        }
        return -1L;
    }
    
    /**
     * Method call to refresh the file size limit based on system setting
     */
    public static void updateFileSizeLimit() {
        Integer fileSizeLimit = -1;
        
        SetupManager setupManager = (SetupManager) SecurityUtil.getApplicationContext().getBean("setupManager");
        Setting setting = setupManager.getSettingByProperty("fileSizeLimit");

        if (setting != null && setting.getValue() != null && !setting.getValue().isEmpty()) {
            try {
                fileSizeLimit = Integer.parseInt(setting.getValue());
                
                
            } catch (Exception e) {
                LogUtil.debug(FileStore.class.getName(), "System Setting for File Size limit is not a valid number");
            }
        }
        
        String profile = DynamicDataSourceManager.getCurrentProfile();
        fileSizeLimitMap.put(profile, fileSizeLimit);
    }
    
    /**
     * Get the file size limit of the system in MB
     * @return 
     */
    public static int getFileSizeLimit() {
        String profile = DynamicDataSourceManager.getCurrentProfile();
        Integer fileSizeLimit = fileSizeLimitMap.get(profile);
        
        if (fileSizeLimit == null) {
            updateFileSizeLimit();
            fileSizeLimit = fileSizeLimitMap.get(profile);
        }
        return fileSizeLimit;
    }

    private static ThreadLocal fileStore = new ThreadLocal() {

        @Override
        protected synchronized Object initialValue() {
            return new HashMap<String, MultipartFile[]>();
        }
    };
    
    private static ThreadLocal filesExceedLimit = new ThreadLocal() {

        @Override
        protected synchronized Object initialValue() {
            return new ArrayList<String>();
        }
    };

    /**
     * Sets the posted files in a HTTP request to a ThreadLocal object for 
     * temporary storing
     * @param fileMap a map of field name & its files
     */
    public static void setFileMap(Map<String, MultipartFile[]> fileMap) {
        Collection<String> exceedLimitList = new ArrayList<String> ();
        Map<String, MultipartFile[]> files = new HashMap<String, MultipartFile[]>();
        
        if (getFileSizeLimit() > 0) {
            for (String key : fileMap.keySet()) {
                MultipartFile[] tempFiles = fileMap.get(key);
                boolean exceedLimit = false;
                for (MultipartFile file : tempFiles) {
                    if (file.getSize() > getFileSizeLimitLong()) {
                        exceedLimit = true;
                        exceedLimitList.add(key);
                    }
                }
                if (!exceedLimit) {
                    files.put(key, tempFiles);
                }
            }
        } else {
            files = fileMap;
        }
        
        fileStore.set(files);
        filesExceedLimit.set(exceedLimitList);
    }

    /**
     * Gets all the posted files of the current HTTP request
     * @return a map of field name & its files
     */
    public static Map<String, MultipartFile[]> getFileMap() {
        return (Map<String, MultipartFile[]>) fileStore.get();
    }
    
    /**
     * Gets a list of the field name which has file exceed the file limit 
     * in the current HTTP request
     * @return 
     */
    public static Collection<String> getFileErrorList() {
        return (Collection<String>) filesExceedLimit.get();
    }

    /**
     * Gets the Iterator of all upload field name of the current HTTP request
     * @return 
     */
    public static Iterator<String> getFileNames() {
        return ((Map<String, MultipartFile[]>) fileStore.get()).keySet().iterator();
    }

    /**
     * Convenient method to retrieves the posted file in current HTTP request 
     * based on field name
     * @return 
     */
    public static MultipartFile getFile(String name) throws FileLimitException {
        Collection<String> exceedLimitList = (ArrayList<String>) filesExceedLimit.get();
                
        if (exceedLimitList.contains(name)) {
            LogUtil.info(FileStore.class.getName(), name + " - File size exceed limit.");
            throw new FileLimitException("File size exceed limit.");
        }
        
        MultipartFile[] files = ((Map<String, MultipartFile[]>) fileStore.get()).get(name);
        if (files != null && files.length > 0) {
            return files[0];
        } else {
            return null;
        }
    }
    
    /**
     * Method to retrieves the posted files in current HTTP request based on 
     * field name
     * @return 
     */
    public static MultipartFile[] getFiles(String name) throws FileLimitException {
        Collection<String> exceedLimitList = (ArrayList<String>) filesExceedLimit.get();
                
        if (exceedLimitList.contains(name)) {
            LogUtil.info(FileStore.class.getName(), name + " - File size exceed limit.");
            throw new FileLimitException("File size exceed limit.");
        }
        
        return ((Map<String, MultipartFile[]>) fileStore.get()).get(name);
    }

    /**
     * Method used by the system to clear the ThreadLocal object after a HTTP
     * request is finish processing
     */
    public static void clear() {
        fileStore.set(new HashMap<String, MultipartFile>());
        filesExceedLimit.set(new ArrayList<String>());
    }
    
    /**
     * Method used by the system to clear the limit of a profile after profile removed
     * request is finish processing
     */
    public static void clearLimit(String profile) {
        fileSizeLimitMap.remove(profile);
    }
}
