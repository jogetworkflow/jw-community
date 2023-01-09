package org.joget.web.presence;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.directory.model.User;
import org.joget.workflow.model.service.WorkflowUserManager;

@WebServlet(urlPatterns = "/web/presence", asyncSupported = true)
public class PresenceServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check that it is SSE request
        if ("text/event-stream".equals(request.getHeader("Accept"))) {
            // This a Tomcat specific - makes request asynchronous
            request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);

            // Set header fields
            response.setContentType("text/event-stream");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");
            response.setCharacterEncoding("UTF-8");

            // Start asynchronous context and add listeners to remove it in case of errors
            PresenceManager.registerRequest(request);

        } else {
            response.sendRedirect(request.getContextPath());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("join".equals(request.getParameter("action"))) {
            WorkflowUserManager wum = (WorkflowUserManager)AppUtil.getApplicationContext().getBean("workflowUserManager");
            User user = wum.getCurrentUser();
            String path = PresenceManager.getPath(request);
            String sessionId = request.getSession().getId();
            PresenceManager.join(path, sessionId, user);
        } else if ("leave".equals(request.getParameter("action"))) {
            String path = PresenceManager.getPath(request);
            String sessionId = request.getSession().getId();
            PresenceManager.leave(path, sessionId);
        } else {
            response.sendRedirect(request.getContextPath());
        }
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        PresenceManager.startNotifier();
    }

    @Override
    public void destroy() {
        PresenceManager.stopNotifier();
    }    
}
