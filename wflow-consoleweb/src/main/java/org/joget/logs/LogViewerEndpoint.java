package org.joget.logs;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PluginThread;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.ServerUtil;

@ServerEndpoint(value = "/web/applog/{appId}", configurator = ServletAwareConfigurator.class)
public class LogViewerEndpoint {
    
    protected Session session;
    protected LogViewerThread logViewer;
    
    @OnOpen
    public void onOpen(Session session, @PathParam("appId") String appId, EndpointConfig config) throws IOException {
        Map<String, List<String>> params = session.getRequestParameterMap();
        String node = ServerUtil.getServerName();
        
        List<String> nodes = params.get("node");
        //verify the node value
        List nodeList = Arrays.asList(ServerUtil.getServerList());
        if (nodes != null && nodeList.size() > 0) {
            if (nodeList.contains(nodes.get(0))) {
                node = nodes.get(0);
            }
        }
        appId = SecurityUtil.validateStringInput(appId);
        this.session = session;
        this.logViewer = new LogViewerThread(HostManager.getCurrentProfile(), appId, this, node);
        Thread thread = new PluginThread(this.logViewer);
        thread.setDaemon(true);
        thread.start();
    }
 
    @OnClose
    public void onClose(Session session) throws IOException {
        try {
            this.logViewer.close();
        } finally {
            HostManager.resetProfile();
        }
    }
    
    @OnError
    public void onError(Throwable thr) {
        if (LogUtil.isDebugEnabled(LogViewerEndpoint.class.getName())) {
            LogUtil.error(LogViewerEndpoint.class.getName(), thr, "");
        }
        HostManager.resetProfile();
    }
}
