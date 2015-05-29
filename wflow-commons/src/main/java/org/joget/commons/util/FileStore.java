package org.joget.commons.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.joget.commons.spring.model.Setting;
import org.springframework.web.multipart.MultipartFile;

public class FileStore {
    private static Integer fileSizeLimit;
    
    public static long getFileSizeLimitLong() {
        if (fileSizeLimit != -1) {
            return fileSizeLimit * 1024 * 1024L;
        }
        return -1L;
    }
    
    public static void updateFileSizeLimit() {
        fileSizeLimit = -1;
        
        SetupManager setupManager = (SetupManager) SecurityUtil.getApplicationContext().getBean("setupManager");
        Setting setting = setupManager.getSettingByProperty("fileSizeLimit");

        if (setting != null && setting.getValue() != null && !setting.getValue().isEmpty()) {
            try {
                fileSizeLimit = Integer.parseInt(setting.getValue());
            } catch (Exception e) {
                LogUtil.debug(FileStore.class.getName(), "System Setting for File Size limit is not a valid number");
            }
        }
    }
    
    public static int getFileSizeLimit() {
        if (fileSizeLimit == null) {
            updateFileSizeLimit();
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

    public static Map<String, MultipartFile[]> getFileMap() {
        return (Map<String, MultipartFile[]>) fileStore.get();
    }
    
    public static Collection<String> getFileErrorList() {
        return (Collection<String>) filesExceedLimit.get();
    }

    public static Iterator<String> getFileNames() {
        return ((Map<String, MultipartFile[]>) fileStore.get()).keySet().iterator();
    }

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
    
    public static MultipartFile[] getFiles(String name) throws FileLimitException {
        Collection<String> exceedLimitList = (ArrayList<String>) filesExceedLimit.get();
                
        if (exceedLimitList.contains(name)) {
            LogUtil.info(FileStore.class.getName(), name + " - File size exceed limit.");
            throw new FileLimitException("File size exceed limit.");
        }
        
        return ((Map<String, MultipartFile[]>) fileStore.get()).get(name);
    }

    public static void clear() {
        fileStore.set(new HashMap<String, MultipartFile>());
        filesExceedLimit.set(new ArrayList<String>());
    }
}
