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
        try {
            plugin.onOpen(session);
        } finally {
            clearSecurityContext();
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        setSecurityContext(session);
        try {
            plugin.onMessage(message, session);
        } finally {
            clearSecurityContext();
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        setSecurityContext(session);
        try {
            plugin.onClose(session);
        } finally {
            clearSecurityContext();
            plugin = null;
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        
        setSecurityContext(session);
        try {
            plugin.onError(session, throwable);
        } finally {
            clearSecurityContext();
            plugin = null;
        }

        // Do error handling here
        LogUtil.error(getClass().getName(), throwable, "onError : session [" + session.getId() + "]");
    }
    
    /**
     * Set security context for websocket, so that current user is available to retrieve in plugin implementation using WorkflowUserManager
     * @param session 
     */
    protected final void setSecurityContext(Session session) {
        try {
            // No matching resetProfile() call for this connection's lifetime, so this must not
            // claim HostManager's one-shot "value to restore" slot on this thread -- see
            // setCurrentProfileWithoutTracking's javadoc.
            HostManager.setCurrentProfileWithoutTracking(profile);
            
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

    /**
     * Clears the identity this callback copied onto the pooled Tomcat worker thread, so the NEXT
     * (possibly anonymous) request landing on it does not inherit this connection's user. Each
     * callback copies the session's SecurityContext onto the thread and never cleared it before, so
     * the identity leaked to whatever ran next on that worker. Mirrors the HTTP request filter's own
     * finally-clear. Best-effort and idempotent.
     */
    protected final void clearSecurityContext() {
        SecurityContextHolder.clearContext();
        try {
            WorkflowUserManager wum = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
            wum.clearCurrentThreadUser();
        } catch (Exception e) {
            // best-effort: the thread user is also re-established by the next request's own filter
        }
    }
}
