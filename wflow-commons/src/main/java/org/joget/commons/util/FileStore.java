package org.joget.commons.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public class FileStore {

    private static ThreadLocal fileStore = new ThreadLocal() {

        @Override
        protected synchronized Object initialValue() {
            return new HashMap<String, MultipartFile>();
        }
    };

    public static void setFileMap(Map<String, MultipartFile> fileMap) {
        fileStore.set(fileMap);
    }

    public static Map<String, MultipartFile> getFileMap() {
        return (Map<String, MultipartFile>) fileStore.get();
    }

    public static Iterator<String> getFileNames() {
        return ((Map<String, MultipartFile>) fileStore.get()).keySet().iterator();
    }

    public static MultipartFile getFile(String name) {
        return ((Map<String, MultipartFile>) fileStore.get()).get(name);
    }

    public static void clear() {
        fileStore.set(new HashMap<String, MultipartFile>());
    }
}
