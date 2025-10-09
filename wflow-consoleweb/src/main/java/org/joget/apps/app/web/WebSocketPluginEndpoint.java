package org.joget.apps.app.web;

import java.io.IOException;
import javax.servlet.http.HttpSession;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginWebSocket;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

/**
 * Endpoint for plugin WebSocket
 */
@ServerEndpoint(value = "/web/socket/plugin/{className}", configurator = WebSocketPluginConfigurator.class)
public class WebSocketPluginEndpoint {

    //the WebSocketPluginEndpoint is not a singleton, each connection is a new instance. 
    private PluginWebSocket plugin;
    private final String profile = HostManager.getCurrentProfile();

    @OnOpen
    public void onOpen(Session session, @PathParam("className") String className) throws IOException {

        final ApplicationContext applicationContext = AppUtil.getApplicationContext();
        final PluginManager pluginManager = (PluginManager) applicationContext.getBean("pluginManager");
        final Plugin p = pluginManager.getPlugin(className);

        if (!(p instanceof PluginWebSocket)) {
            throw new IOException("Error loading plugin");
        }
        
        plugin = (PluginWebSocket) p;
        
        setSecurityContext(session);
        plugin.onOpen(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        setSecurityContext(session);
        plugin.onMessage(message, session);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        setSecurityContext(session);
        plugin.onClose(session);
        plugin = null;
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        
        setSecurityContext(session);
        plugin.onError(session, throwable);
        plugin = null;
        
        // Do error handling here
        LogUtil.error(getClass().getName(), throwable, "onError : session [" + session.getId() + "]");
    }
    
    /**
     * Set security context for websocket, so that current user is available to retrieve in plugin implementation using WorkflowUserManager
     * @param session 
     */
    protected final void setSecurityContext(Session session) {
        try {
            HostManager.setCurrentProfile(profile);
            
            HttpSession httpSession = (HttpSession) session.getUserProperties().get(HttpSession.class.getName());

            if (httpSession != null) {
                Object contextFromSession = httpSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                if (contextFromSession != null) {
                    SecurityContextHolder.setContext((SecurityContext) contextFromSession);
                    
                    //clear the user in case there is other processing set the user to roleAnonymous already
                    WorkflowUserManager wum = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
                    wum.clearCurrentThreadUser();
                }
            }
        } catch (Exception e) {
            LogUtil.debug(getClass().getName(), "HttpSession is already invalidated");
        }
    }
}
