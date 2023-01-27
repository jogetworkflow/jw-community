package org.joget.apps.app.web;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class PluginResourceServlet extends HttpServlet {

    public static final int BUFFER_SIZE = 65536;
    ServletConfig config;

    @Override
    public void init(ServletConfig config) {
        this.config = config;
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // reset profile and set hostname
        HostManager.initHost();

        boolean found = false;

        // get plugin name and requested resource url
        String pluginName = null;
        String resourceUrl = null;
        String fileName = null;
        String pathInfo = request.getPathInfo();
        int i = pathInfo.indexOf('/', 1);
        if (i > 0) {
            pluginName = pathInfo.substring(1, i);
            resourceUrl = pathInfo.substring(i);
            int j = pathInfo.lastIndexOf('/');
            if (j > 0) {
                fileName = pathInfo.substring(j + 1);
            }
        }

        if (pluginName != null && resourceUrl != null && fileName != null) {
            InputStream input = null;
            ServletOutputStream stream = null;

            try {
                // prepend prefix
                String prefix = config.getInitParameter("prefix");
                if (prefix != null && prefix.trim().length() > 0) {
                    resourceUrl = prefix + resourceUrl;
                }

                // get resource input stream
                PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
                if (pluginManager != null) {
                    input = pluginManager.getPluginResource(pluginName, resourceUrl);
                }
                if (input != null) {
                    // set header for download
//                    response.addHeader("Content-Disposition", "attachment; filename=" + fileName);

                    String contentType = request.getSession().getServletContext().getMimeType(resourceUrl);
                    if (contentType != null) {
                        response.setContentType(contentType);
                    }
                    
                    // write output
                    stream = response.getOutputStream();
                    byte[] bbuf = new byte[BUFFER_SIZE];
                    int length = 0;
                    while ((length = input.read(bbuf)) != -1) {
                        stream.write(bbuf, 0, length);
                    }
                    found = true;
                }

            } catch (Exception e) {
                LogUtil.error(PluginResourceServlet.class.getName(), e, "");
            } finally {
                if (stream != null) {
                    stream.flush();
                    stream.close();
                }
                if (input != null) {
                    input.close();
                }
            }
        }

        if (!found) {
            // send 404 not found
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }

    }

    @Override
    public void destroy() {
        PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
        if (pluginManager != null) {
            pluginManager.shutdown();
        }
    }
    
}
