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
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.util.Strings;
import org.eclipse.jgit.util.FileUtils;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.ServerUtil;
import org.joget.commons.util.SetupManager;

@Plugin(name = "LogViewerAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class LogViewerAppender extends AbstractAppender {

    protected LoadingCache<String, Writer> qwCache = null;

    protected static final int SIZE = 40;
    public static final int MAX_FILESIZE = 200 * 1024; //200kb
    public static final String SYSTEM_PROPERTY_NODE_NAME = "wflow.name";

    public static final String LOG_DIRECTORY = "app_logs";
    public static final String LOG_NAME = "viewer";
    public static final String LOG_NAME_EXT = ".log";
    public static final String LOG_ROLLING_EXT = ".rolling";
    public static final String CONSOLE_LOG = "CONSOLE_LOG";

    protected static boolean startLogging = false;

    protected static Map<String, Set<LogViewerEndpoint>> broadcastEndpoints = new HashMap<String, Set<LogViewerEndpoint>>();

    @PluginFactory
    public static LogViewerAppender createAppender(
            @PluginAttribute("name") final String name,
            @PluginElement("Filter") final Filter filter,
            @PluginElement("Layout") final StringLayout layout) {
        return new LogViewerAppender(name, filter, layout);
    }

    private LogViewerAppender(final String name, final Filter filter, final StringLayout layout) {
        super(name, filter, layout, true, null);
    }

    protected LoadingCache<String, Writer> getCache() {
        if (qwCache == null) {
            CacheLoader loader = new CacheLoader<String, Writer>() {
                @Override
                public Writer load(String filename) throws Exception {
                    Writer writer = null;
                    FileOutputStream ostream = null;

                    File file = new File(filename);
                    try {
                        if (!file.exists()) {
                            file.getParentFile().mkdirs();
                        }
                        ostream = new FileOutputStream(filename, true);

                        Writer fw = new OutputStreamWriter(ostream, "UTF-8");
                        writer = fw;

                        writeHeader(writer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return writer;
                }
            };

            RemovalListener<String, Writer> removalListener = new RemovalListener<String, Writer>() {
                @Override
                public void onRemoval(RemovalNotification<String, Writer> removal) {
                    Writer writer = removal.getValue();
                    try {
                        writeFooter(writer);
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

    protected synchronized Writer getWriter(String appId) {
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

            Writer writer = getCache().get(filename);

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

    protected boolean checkStartLogging(LogEvent event) {
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
    public void start() {
        super.start();
    }

    @Override
    public void append(LogEvent event) {
        if (checkStartLogging(event)) { 
            try {
                Writer systemWriter = getWriter(CONSOLE_LOG);
                Writer appWriter = null;

                String appId = getCurrentAppId();
                if (appId != null) {
                    appWriter = getWriter(appId);
                }
                String output = new String(getLayout().toByteArray(event));
                write(systemWriter, appWriter, output);

                if (systemWriter != null) {
                    systemWriter.flush();
                }
                if (appWriter != null) {
                    appWriter.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        reset();
        broadcastEndpoints = new HashMap<>();
        super.stop();
    }

    protected void write(Writer systemWriter, Writer appWriter, String string) throws IOException {
        if (systemWriter != null) {
            systemWriter.write(string);
        }
        if (appWriter != null) {
            appWriter.write(string);
        }
        broadcast(getCurrentAppId(), string);
        broadcast(CONSOLE_LOG, string);
    }

    protected void reset() {
        getCache().invalidateAll();
    }

    protected void writeFooter(Writer writer) throws IOException {
        Layout layout = getLayout();
        if (layout != null && layout.getFooter() != null) {
            byte[] footer = layout.getFooter();
            if (writer != null && footer != null) {
                String h = new String(layout.getHeader());
                writer.write(h);
            }
        }
    }

    protected void writeHeader(Writer writer) throws IOException {
        Layout layout = getLayout();
        if (layout != null && layout.getHeader() != null) {
            byte[] header = layout.getHeader();
            if (writer != null && header != null) {
                String h = new String(layout.getHeader());
                writer.write(h);
            }
        }
    }

    public static synchronized void registerEndpoint(String profile, String appId, LogViewerEndpoint endpoint) {
        String key = profile + ":" + appId;
        Set<LogViewerEndpoint> endpoints = broadcastEndpoints.get(key);
        if (endpoints == null) {
            endpoints = new HashSet<>();
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
        String lines[] = message.split(Strings.LINE_SEPARATOR);
        Set<LogViewerEndpoint> endpoints = broadcastEndpoints.get(key);
        if (endpoints != null && lines.length > 0) {
            Set<LogViewerEndpoint> invalidEndpoints = new HashSet<LogViewerEndpoint>();
            for (LogViewerEndpoint endpoint : endpoints) {
                synchronized (endpoint) {
                    try {
                        for (String line: lines) {
                            endpoint.session.getBasicRemote().sendText(line);
                        }
                    } catch (IllegalStateException e) {
                        // WebSocket connection closed, ignore
                        invalidEndpoints.add(endpoint);
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
