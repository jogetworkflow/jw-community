package org.joget.apps.app.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.joget.apps.app.model.AppDefinition;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;

public class AppDevUtil {

    public static final String FILE_APP_PROPERTIES = "app.properties";
    
    public static String getAppDevBaseDirectory() {
        String dir = SetupManager.getBaseDirectory() + File.separator + "app_src";
        return dir;
    }
    
    public static Properties getAppDevProperties(AppDefinition appDef) {
        // load from FILE_APP_PROPERTIES
        Properties props = new Properties();
        String baseDir = AppDevUtil.getAppDevBaseDirectory();
        String projectDirName = appDef.getAppId();
        File projectDir = AppDevUtil.dirSetup(baseDir, projectDirName);
        File file = new File(projectDir, FILE_APP_PROPERTIES);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            props.load(fis);
        } catch(IOException e) {
            LogUtil.debug(AppDevUtil.class.getName(), FILE_APP_PROPERTIES + " could not be loaded for " + appDef.getAppId());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch(IOException e) {
                    // ignore
                }
            }
        }
        return props;
    }
    
    public static void setAppDevProperties(AppDefinition appDef, Properties props) throws IOException {
        String baseDir = AppDevUtil.getAppDevBaseDirectory();
        String projectDirName = appDef.getAppId();
        File projectDir = AppDevUtil.dirSetup(baseDir, projectDirName);
        File file = new File(projectDir, FILE_APP_PROPERTIES);
        Properties currentProps = new Properties();
        if (file.exists()) {
        try (FileInputStream fis = new FileInputStream(file)) {
            currentProps.load(fis);
        }
        }
        currentProps.putAll(props);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            currentProps.store(fos, "");
        }
    }    

    public static File dirSetup(String baseDir, String projectDirName) {
        // create project directory
        File projectDir = new File(baseDir, projectDirName);
        if (!projectDir.exists()) {
            projectDir.mkdirs();
            LogUtil.debug(AppDevUtil.class.getName(), "Create app project directory");
        }
        return projectDir;
    }

}
