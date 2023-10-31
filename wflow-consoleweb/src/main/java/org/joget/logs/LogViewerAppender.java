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
import java.net.ConnectException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
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
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.PluginThread;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.ServerUtil;
import org.joget.commons.util.SetupManager;
import org.joget.commons.util.StringUtil;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;

@Plugin(name = "LogViewerAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class LogViewerAppender extends AbstractAppender {

    protected LoadingCache<String, Writer> qwCache = null;
    protected static final Map<String, BlockingQueue<String>> messages = new HashMap<String, BlockingQueue<String>>();
    protected static final Map<String, Boolean> processingMessage = new HashMap<String, Boolean>();
    protected static final Set<String> unreachableNodes = new HashSet<String>();

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
                    filename = SecurityUtil.normalizedFileName(filename);
                    
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
            String filename = getFileName(appId, null);

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

    public static String getFilepath(String appId, String node) {
        if (appId == null) {
            appId = getCurrentAppId();
        }
        String serverName;
        if (node != null && !node.isEmpty()) {
            serverName = File.separator + node;
        } else {
            serverName = ServerUtil.getServerName();
            if (!serverName.isEmpty()) {
                serverName = File.separator + serverName;
            }
        }
        return SetupManager.getBaseDirectory() + File.separator + LOG_DIRECTORY + SecurityUtil.normalizedFileName(serverName + File.separator + appId);
    }

    public static String getFileName(String appId, String node) {
        return getFilepath(appId, node) + File.separator + LOG_NAME + LOG_NAME_EXT;
    }

    protected String getFileName() {
        return getFileName(null, null);
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

    public static synchronized void registerEndpoint(String profile, String appId, LogViewerEndpoint endpoint, String node) {
        String key = node + ":" + profile + ":" + appId;
        Set<LogViewerEndpoint> endpoints = broadcastEndpoints.get(key);
        if (endpoints == null) {
            endpoints = new HashSet<>();
            broadcastEndpoints.put(key, endpoints);
        }
        endpoints.add(endpoint);
    }

    public static synchronized void removeEndpoint(String profile, String appId, LogViewerEndpoint endpoint, String node) {
        String key = node + ":" + profile + ":" + appId;
        Set<LogViewerEndpoint> endpoints = broadcastEndpoints.get(key);
        if (endpoints != null) {
            endpoints.remove(endpoint);
            if (endpoints.isEmpty()) {
                broadcastEndpoints.remove(key);
            }
        }
    }
    
    public static void broadcast(String appId, String message) {
        broadcast(appId, message, null);
    }

    public static void broadcast(final String appId, final String message, final String node) {
        // run it in new thread, so it won't block the current thread when something is not right with broadcast.
        Thread newThread = new PluginThread(new Runnable() {
            @Override
            public void run() {
                
                String key;
                if (node == null) {
                    key = ServerUtil.getServerName() + ":" + HostManager.getCurrentProfile() + ":" + appId;
                } else {
                    key = node + ":" + HostManager.getCurrentProfile() + ":" + appId;
                }
                
                //broadcast to registered endpoints based on key  
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
        });
        newThread.start();
        
        //if the server list having other cluster node, broadcast to other server
        if (node == null && appId !=null && !appId.isEmpty()) {
            String[] servers = ServerUtil.getServerList();
            if (servers.length > 1) {
                for (String server : servers) {
                    if (!ServerUtil.getServerName().equalsIgnoreCase(server) && !unreachableNodes.contains(server)) {
                        broadcastClusterNode(message, server, appId);
                    }
                }
            }
        }
    }
    
    /**
     * queue message to broadcast to cluster node
     * @param message
     * @param node
     * @param appId 
     */
    public static void broadcastClusterNode(String message, String node, String appId) {
        String key = node + ":" + HostManager.getCurrentProfile() + ":" + appId;
        
        BlockingQueue<String> queue = messages.get(key);
        if (queue == null) {
            queue = new LinkedBlockingQueue<String>();
            messages.put(key, queue);
        }
        queue.offer(message);
        
        sendMessage(node, appId);
    }
    
    /**
     * Send queued message to cluster node with token
     * @param node
     * @param appId 
     */
    protected static void sendMessage(final String node, final String appId) {
        final String key = node + ":" + HostManager.getCurrentProfile() + ":" + appId;
        
        if (unreachableNodes.contains(node)) {
            messages.remove(key);
            processingMessage.remove(key);
            return;
        }
        
        synchronized (processingMessage) {
            Boolean isProcessing = processingMessage.get(key);
            if (isProcessing == null || !isProcessing) {
                processingMessage.put(key, true); //stop sending message and start collecting it
                
                final BlockingQueue<String> queue = messages.get(key);
                if (queue == null || queue.isEmpty()) {
                    processingMessage.put(key, false); 
                    return;
                }
                
                // run it in new thread, so it won't block the current thread when something is not right with http call.
                Thread newThread = new PluginThread(new Runnable() {
                    @Override
                    public void run() {
                        //prepare message
                        int i = 0;
                        StringBuilder sb = new StringBuilder();
                        do {
                            if (i != 0) {
                                sb.append(Strings.LINE_SEPARATOR);
                            }  
                            sb.append(queue.poll());
                            i++;
                        } while (i < 20 && !queue.isEmpty());

                        String message = sb.toString();
                        
                        HttpServletRequest httpRequest = WorkflowUtil.getHttpServletRequest();
                        String nodeIp = ServerUtil.getIPAddress(node);
                        if (httpRequest != null && nodeIp != null && !nodeIp.isEmpty()) {
                            CloseableHttpClient client = null;
                            try {
                                String currentNode = ServerUtil.getServerName();
                                final String token = getLogViewerToken(currentNode);
                                updateJsonIPWhitelist(currentNode);

                                String broadcastURL = "http://" + nodeIp + ":" + httpRequest.getLocalPort() + "/jw/web/json/log/broadcast?";

                                broadcastURL = StringUtil.addParamsToUrl(broadcastURL, "node", currentNode);
                                broadcastURL = StringUtil.addParamsToUrl(broadcastURL, "profile", HostManager.getCurrentProfile());
                                broadcastURL = StringUtil.addParamsToUrl(broadcastURL, "appId", appId);

                                String url = broadcastURL;
                                
                                client = HttpClients.custom()
                                .setRetryHandler(new HttpRequestRetryHandler() {
                                    @Override
                                    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                                        return false; // do not need to retry as it is not critical
                                    }
                                }).build();
                                HttpRequestBase request = new HttpPost(url);
                                StringEntity requestEntity = new StringEntity(message, "UTF-8");
                                ((HttpPost) request).setEntity(requestEntity);
                                request.setHeader("token", token);
                                client.execute(request);
                            } catch (Exception e) {
                                e.printStackTrace();

                                //remove from server list when the server is not reachable
                                if (e instanceof ConnectTimeoutException || e instanceof ConnectException) {
                                    unreachableNodes.add(node);
                                }
                            } finally {
                                if (client != null) {
                                    try {
                                        client.close();
                                    } catch (Exception e){}
                                }
                                
                                synchronized (processingMessage) { //release the processing
                                    processingMessage.put(key, false);
                                }
                                
                                sendMessage(node, appId); //if there is unsend message, send it now
                            }
                        }
                    }
                });
                newThread.start();
            }
        }
    }
    
    //get or create log viewer token
    public static String getLogViewerToken(String node) {
        SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
        Setting setting = setupManager.getSettingByProperty(node + "LogToken");
        if (setting == null) {
            //generate random 12 chars string
            String token = RandomStringUtils.random(12, true, true);
            setupManager.updateSetting(node + "LogToken", token);
            return token;
        }
        return setting.getValue();
    }
    
    //update or add ip address to JsonIPWhitelist
    public static void updateJsonIPWhitelist(String node) {
        SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
        Setting setting = setupManager.getSettingByProperty("jsonpIPWhitelist");
        String JsonIPWhitelist = "";
        String currentIpAddress = ServerUtil.getIPAddress(node);
        if (setting != null && setting.getValue() != null) {
            JsonIPWhitelist = setting.getValue();
        }
        if (!JsonIPWhitelist.contains(currentIpAddress)) {
            //check if JsonIPWhitelist is not empty & last character is not ;
            if (!JsonIPWhitelist.isEmpty() && !JsonIPWhitelist.substring(JsonIPWhitelist.length() - 1).equals(";")) {
                currentIpAddress = ";" + currentIpAddress;
            }
            JsonIPWhitelist = JsonIPWhitelist + currentIpAddress;
            WorkflowUserManager wum = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
            try {
                wum.setSystemThreadUser(true); //to log it as system user
                setupManager.updateSetting("jsonpIPWhitelist", JsonIPWhitelist);
            } finally {
                wum.setSystemThreadUser(false);
            }
        }
    }
}
