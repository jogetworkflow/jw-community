package org.joget.logs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;

public class LogViewerThread extends Thread {
    private InputStream inputStream = null;
    private BufferedReader reader = null;
    private String currentFilename;
    private String appId;
    private String profile;
    private LogViewerEndpoint endpoint;

    public LogViewerThread(String profile, String appId, LogViewerEndpoint endpoint) {
        this.appId = appId;
        this.profile = profile;
        this.endpoint = endpoint;
    }

    @Override
    public void run() {
        HostManager.setCurrentProfile(profile);
        
        currentFilename = LogViewerAppender.getFileName(appId) + LogViewerAppender.LOG_ROLLING_EXT;
        readFile(currentFilename);
        
        currentFilename = LogViewerAppender.getFileName(appId);
        readFile(currentFilename);
        
        LogViewerAppender.registerEndpoint(profile, appId, endpoint);
        HostManager.resetProfile();
    }
    
    protected void readFile(String filename) {
        inputStream = null;
        try {
            inputStream = new FileInputStream(filename);
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            
            String line;
            try {
                while((line = reader.readLine()) != null) {
                    try {
                        endpoint.session.getBasicRemote().sendText(line, true);
                    } catch(IllegalArgumentException | IllegalStateException ise) {
                        // ignore
                    }
                }
            } catch (IOException e) {
                if (LogUtil.isDebugEnabled(LogViewerThread.class.getName())) {
                    LogUtil.error(LogViewerThread.class.getName(), e, "");
                }
            }
        } catch(Exception ex) { 
            if (LogUtil.isDebugEnabled(LogViewerThread.class.getName())) {
                LogUtil.error(LogViewerThread.class.getName(), ex, "");
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
            }
            
            inputStream = null;
            reader = null;
        }
    }
    
    public void close() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
        } finally {
            LogViewerAppender.removeEndpoint(profile, appId, endpoint);
            HostManager.resetProfile();
        }
    }   
}
