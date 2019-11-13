package org.joget.logs;

import java.io.IOException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SecurityUtil;

@ServerEndpoint(value = "/web/applog/{appId}", configurator = ServletAwareConfigurator.class)
public class LogViewerEndpoint {
    
    protected Session session;
    protected LogViewerThread thread;
    
    @OnOpen
    public void onOpen(Session session, @PathParam("appId") String appId, EndpointConfig config) throws IOException {
        appId = SecurityUtil.validateStringInput(appId);
        this.session = session;
        this.thread = new LogViewerThread(HostManager.getCurrentProfile(), appId, this);
        this.thread.start();
    }
 
    @OnClose
    public void onClose(Session session) throws IOException {
        try {
            this.thread.close();
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
