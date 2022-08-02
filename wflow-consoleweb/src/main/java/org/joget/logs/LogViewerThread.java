package org.joget.logs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;

public class LogViewerThread implements Runnable {
    private InputStream inputStream = null;
    private BufferedReader reader = null;
    private String currentFilename;
    private final String appId;
    private final String node;
    private final String profile;
    private final LogViewerEndpoint endpoint;

    public LogViewerThread(String profile, String appId, LogViewerEndpoint endpoint, String node) {
        this.appId = appId;
        this.profile = profile;
        this.endpoint = endpoint;
        this.node = node;
    }

    @Override
    public void run() {
        currentFilename = LogViewerAppender.getFileName(appId, node) + LogViewerAppender.LOG_ROLLING_EXT;
        readFile(currentFilename);

        currentFilename = LogViewerAppender.getFileName(appId, node);
        readFile(currentFilename);

        LogViewerAppender.registerEndpoint(profile, appId, endpoint, node);
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
        } catch(FileNotFoundException | UnsupportedEncodingException ex) { 
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
            } catch (IOException e) {
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
        } catch (IOException e) {
        } finally {
            LogViewerAppender.removeEndpoint(profile, appId, endpoint, node);
            HostManager.resetProfile();
        }
    }   
}
