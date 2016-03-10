package org.joget.plugin.base;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SampleApplicationPlugin extends DefaultApplicationPlugin implements PluginWebSupport{

    public String getName() {
        return "Sample Application Plugin";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String getDescription() {
        return "Sample Application Plugin";
    }

    public PluginProperty[] getPluginProperties() {
        return null;
    }

    public Object execute(Map properties) {
        return null;
    }

    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String arg1 = request.getParameter("arg1");
        String arg2 = request.getParameter("arg2");

        response.getWriter().write("{arg1:\""+arg1+"\", arg2:\""+arg2+"\"}");
    }

    public String getLabel() {
        return "Sample Application Plugin";
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
}
