package org.joget.plugin.base;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for Web Service Plugin
 * 
 */
public interface PluginWebSupport {
    
    /**
     * Work similar to a Servlet
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    public void webService(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException;
}
