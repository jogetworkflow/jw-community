package org.joget.plugin.base;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PluginWebSupport {
    public void webService(HttpServletRequest request, HttpServletResponse response)  throws ServletException, IOException;
}
