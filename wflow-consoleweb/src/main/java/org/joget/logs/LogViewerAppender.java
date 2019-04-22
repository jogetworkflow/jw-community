package org.joget.logs;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.jgit.util.FileUtils;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.ServerUtil;
import org.joget.commons.util.SetupManager;

public class LogViewerAppender extends AppenderSkeleton {
    protected LoadingCache<String, CountingQuietWriter> qwCache = null;
    
    protected static final int SIZE = 40;
    public static final int MAX_FILESIZE = 200*1024; //200kb
    public static final String SYSTEM_PROPERTY_NODE_NAME = "wflow.name";
    
    public static final String LOG_DIRECTORY = "app_logs";
    public static final String LOG_NAME = "viewer";
    public static final String LOG_NAME_EXT = ".log";
    public static final String LOG_ROLLING_EXT = ".rolling";
    public static final String CONSOLE_LOG = "CONSOLE_LOG";
    
    protected static boolean startLogging = false;
    
    protected static Map<String, Set<LogViewerEndpoint>> broadcastEndpoints = new HashMap<String, Set<LogViewerEndpoint>>();
    
    public LogViewerAppender() {}
    
    public LogViewerAppender (Layout layout) throws IOException {
        this.layout = layout;
    }
    
    protected LoadingCache<String, CountingQuietWriter> getCache() {
        if (qwCache == null) {
            CacheLoader loader = new CacheLoader<String, CountingQuietWriter> () {
                @Override
                public CountingQuietWriter load(String filename) throws Exception {
                    CountingQuietWriter writer = null;
                    FileOutputStream ostream = null;
                    
                    File file = new File(filename);
                    try {
                        if (!file.exists()) {
                            file.getParentFile().mkdirs();
                        }
                        ostream = new FileOutputStream(filename, true);
                    
                        Writer fw = new OutputStreamWriter(ostream, "UTF-8");
                        writer = new CountingQuietWriter(fw, errorHandler);

                        writeHeader(writer);
                    } catch (Exception e) {
                        errorHandler.error("getWriter("+filename+") call failed.",
                                        e, ErrorCode.FILE_OPEN_FAILURE);
                    }
                    return writer;
                }
            };
            
            RemovalListener<String, CountingQuietWriter> removalListener = new RemovalListener<String, CountingQuietWriter>() {
                @Override
                public void onRemoval(RemovalNotification<String, CountingQuietWriter> removal) {
                    CountingQuietWriter writer = removal.getValue();
                    writeFooter(writer);
                    try {
                        writer.close();
                    } catch (Exception e) {
                    }
                }
            };
            
            qwCache = CacheBuilder.newBuilder()
                        .maximumSize(SIZE)
                        .expireAfterAccess(10, TimeUnit.MINUTES)
                        .removalListener(removalListener)
                        .build(loader);
        }
        
        return qwCache;
    }
    
    
    protected synchronized CountingQuietWriter getWriter(String appId) {
        try {
            String filename = getFileName(appId);
            
            //check rolling
            File currentFile = new File(filename);
            long size = currentFile.length();
            if (size >= MAX_FILESIZE) {
                //delete previous rolling file if exist
                File file = new File(filename + LOG_ROLLING_EXT);
                if (file.exists()) {
                    file.delete();
                }
                
                //remove from cache to close it
                getCache().invalidate(filename);
                
                //rename the current to rolling
                FileUtils.rename(new File(filename), new File(filename + LOG_ROLLING_EXT));
            }            
            
            CountingQuietWriter writer = getCache().get(filename);            

            return writer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    } 
    
    protected static String getCurrentAppId() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        if (appDef != null) {
            return appDef.getAppId();
        } else {
            return null;
        }
    }
    
    public static String getFilepath(String appId) {
        if (appId == null) {
            appId = getCurrentAppId();
        }
        String serverName = ServerUtil.getServerName();
        if (!serverName.isEmpty()) {
            serverName = File.separator + serverName;
        }
        return SetupManager.getBaseDirectory() + File.separator + LOG_DIRECTORY + serverName + File.separator + appId;
    }
    
    public static String getFileName(String appId) {
        return getFilepath(appId) + File.separator + LOG_NAME + LOG_NAME_EXT;
    }
    
    protected String getFileName() {
        return getFileName(null);
    }
    
    protected boolean checkStartLogging(LoggingEvent event) {
        if (!startLogging) {
            //only start logging after first found ResponseOverrideFilter, else will causing Context fail to init
            if ("org.displaytag.filter.ResponseOverrideFilter".equals(event.getLoggerName())) {
                startLogging = true;
                return false;
            }
        }
        return startLogging;
    }

    @Override
    protected void append(LoggingEvent event) {
        if (checkStartLogging(event)) { 
            CountingQuietWriter systemWriter = getWriter(CONSOLE_LOG);
            CountingQuietWriter appWriter = null;
            
            String appId = getCurrentAppId();
            if (appId != null) {
                appWriter = getWriter(appId);
            }
            write(systemWriter, appWriter, this.layout.format(event));

            if (layout.ignoresThrowable()) {
                String[] s = event.getThrowableStrRep();
                if (s != null) {
                    int len = s.length;
                    for (int i = 0; i < len; i++) {
                        write(systemWriter, appWriter, s[i]);
                        write(systemWriter, appWriter, Layout.LINE_SEP);
                    }
                }
            }
            
            if (systemWriter != null) {
                systemWriter.flush();
            }
            if (appWriter != null) {
                appWriter.flush();
            }
        }
    }
    
    protected void write(CountingQuietWriter systemWriter, CountingQuietWriter appWriter, String string) {
        if (systemWriter != null) {
            systemWriter.write(string);
        }
        if (appWriter != null) {
            appWriter.write(string);
        }
        broadcast(getCurrentAppId(), string);
        broadcast(CONSOLE_LOG, string);
    }

    @Override
    public synchronized void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        reset();
        broadcastEndpoints = new HashMap<String, Set<LogViewerEndpoint>>();
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }
    
    protected void reset() {
        getCache().invalidateAll();
    }
    
    protected void writeFooter(CountingQuietWriter writer) {
        if (layout != null) {
            String f = layout.getFooter();
            if (f != null && writer != null) {
                writer.write(f);
                writer.flush();
            }
        }
    }

    protected void writeHeader(CountingQuietWriter writer) {
        if (layout != null) {
            String h = layout.getHeader();
            if (h != null && writer != null) {
                writer.write(h);
            }
        }
    }
    
    public static synchronized void registerEndpoint(String profile, String appId, LogViewerEndpoint endpoint) {
        String key = profile + ":" + appId;
        Set<LogViewerEndpoint> endpoints = broadcastEndpoints.get(key);
        if (endpoints == null) {
            endpoints = new HashSet<LogViewerEndpoint>();
            broadcastEndpoints.put(key, endpoints);
        }
        endpoints.add(endpoint);
    }
    
    public static synchronized void removeEndpoint(String profile, String appId, LogViewerEndpoint endpoint) {
        String key = profile + ":" + appId;
        Set<LogViewerEndpoint> endpoints = broadcastEndpoints.get(key);
        if (endpoints != null) {
            endpoints.remove(endpoint);
            if (endpoints.isEmpty()) {
                broadcastEndpoints.remove(key);
            }
        }
    }
    
    protected static void broadcast(String appId, String message) {
        String key = HostManager.getCurrentProfile() + ":" + appId;
        Set<LogViewerEndpoint> endpoints = broadcastEndpoints.get(key);
        if (endpoints != null) {
            Set<LogViewerEndpoint> invalidEndpoints = new HashSet<LogViewerEndpoint>();
            for (LogViewerEndpoint endpoint : endpoints) {
                synchronized (endpoint) {
                    try {
                        endpoint.session.getBasicRemote().sendText(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                        invalidEndpoints.add(endpoint);
                    }
                }
            }
            if (!invalidEndpoints.isEmpty()) {
                endpoints.removeAll(invalidEndpoints);
                
                if (endpoints.isEmpty()) {
                    broadcastEndpoints.remove(key);
                }
            }
        }
    }
}
