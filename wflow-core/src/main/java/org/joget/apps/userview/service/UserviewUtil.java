package org.joget.apps.userview.service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.directwebremoting.util.SwallowingHttpServletResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.model.UserviewTheme;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

@Service("userviewUtil")
public class UserviewUtil implements ApplicationContextAware, ServletContextAware {

    static ApplicationContext appContext;
    static ServletContext servletContext;

    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        appContext = ac;
    }

    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    public void setServletContext(ServletContext sc) throws BeansException {
        servletContext = sc;
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    public static String getTemplate(UserviewTheme theme, Map data, String templatePath) {
        return getTemplate(theme, data, templatePath, null);
    }

    public static String getTemplate(UserviewTheme theme, Map data, String templatePath, String translationPath) {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");

        if (data == null) {
            data = new HashMap();
        }

        data.put("theme", theme);

        String content = pluginManager.getPluginFreeMarkerTemplate(data, theme.getClassName(), templatePath, translationPath);
        return content;
    }
    
    public static String getUserviewMenuHtml(UserviewMenu menu) throws RuntimeException {
        String html = "";
        
        String jspPage = menu.getReadyJspPage();
        if (jspPage != null && !jspPage.isEmpty()) {
            Map<String, Object> modelMap = new HashMap<String, Object>();
            modelMap.put("properties", menu.getProperties());
            modelMap.put("requestParameters", menu.getRequestParameters());
            html += UserviewUtil.renderJspAsString(jspPage, modelMap);
        } else {
            html += menu.getReadyRenderPage();
        }
        
        return html;
    }

    public static String renderJspAsString(String viewName, Map<String, Object> modelMap) {
        if (viewName == null) {
            return null;
        }

        String result = null;

        StringWriter sout = new StringWriter();
        StringBuffer sbuffer = sout.getBuffer();

        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();
        HttpServletResponse swallowingResponse = new SwallowingHttpServletResponse(response, sout, "UTF-8");

        try {
            //Add the modelMap to the request as attributes
            addModelAsRequestAttributes(request, modelMap);

            // Using UTF-8 for the rendered JSP
            swallowingResponse.setContentType("text/html; charset=utf-8");

            RequestDispatcher dispatcher = servletContext.getRequestDispatcher("/WEB-INF/jsp/" + viewName);

            dispatcher.include(request, swallowingResponse);

            result = sbuffer.toString();
        } catch (Exception e) {
            LogUtil.error("UserviewUtil", e, viewName);
        }

        return result;
    }

    private static void addModelAsRequestAttributes(ServletRequest request, Map<String, Object> modelMap) {
        if (modelMap != null && request != null) {
            for (Map.Entry<String, Object> entry : modelMap.entrySet()) {
                String modelName = entry.getKey();
                Object modelValue = entry.getValue();
                if (modelValue != null) {
                    request.setAttribute(modelName, modelValue);
                } else {
                    request.removeAttribute(modelName);
                }
            }
        }
    }
}
