package org.joget.apps.ext;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginProperty;
import org.joget.plugin.base.PluginWebSupport;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Plugin to add content to the web console header, footer and body
 */
public class ConsoleWebPlugin implements Plugin, PluginWebSupport {

    @Override
    public String getName() {
        return "Console Web Plugin";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public PluginProperty[] getPluginProperties() {
        return null;
    }

    @Override
    public Object execute(Map properties) {
        return null;
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // get content
        String content = "";
        String path = request.getServletPath();
        String spot = request.getParameter("spot");
        if ("header".equals(spot)) {
            content = getHeader(path);
        } else if ("footer".equals(spot)) {
            content = getFooter(path);
        } else if ("login".equals(spot)) {
            content = getLogin();
        } else if ("home".equals(spot)) {
            content = getHome();
        } else if ("welcome".equals(spot)) {
            content = getWelcome();
        }
            
        // output content
        PrintWriter writer = response.getWriter();
        writer.write(content);
    }    

    /**
     * Content to be added to the web console header.
     * @param path Current JSP path e.g. /WEB-INF/jsp/console/home.jsp
     * @return 
     */
    public String getHeader(String path) {
        ResourceBundleMessageSource messageSource = (ResourceBundleMessageSource)AppUtil.getApplicationContext().getBean("messageSource");
        Locale locale = new Locale(AppUtil.getAppLocale());
        String header = messageSource.getMessage("console.header.top.title", null, "", locale);
        return header;
    }

    /**
     * Content to be added to the web console footer.
     * @param path Current JSP path e.g. /WEB-INF/jsp/console/home.jsp
     * @return 
     */
    public String getFooter(String path) {
        ResourceBundleMessageSource messageSource = (ResourceBundleMessageSource)AppUtil.getApplicationContext().getBean("messageSource");
        Locale locale = new Locale(AppUtil.getAppLocale());
        String revision = messageSource.getMessage("console.footer.label.revision", null, "", locale);
        String footer = "© Joget Workflow - Open Dynamics Inc. All Rights Reserved. " + revision;
        return footer;
    }
    
    /**
     * Additional content in the login page
     * @return 
     */
    protected String getLogin() {
        return "";
    }

    /**
     * Additional content in the home page
     * @return 
     */
    protected String getHome() {
        return "";
    }

    /**
     * Welcome content in the home page.
     * @return 
     */
    protected String getWelcome() {
        String content = "<div id=\"getting-started\">"
                + "<iframe id=\"frame\" style=\"display:none; height:200px; width:100%; overflow:hidden;\" src=\"http://www.joget.org/updates/welcome?src=v3\" frameborder=\"0\"></iframe>"
                + "<a href=\"http://www.joget.org/help?src=wmc\" target=\"www.joget.org\" id=\"link\"></a>"
                + "</div>"
                + "<div class=\"clear\"></div>"
                + "<script type=\"text/javascript\">"
                + "var image = new Image();"
                + "image.src = \"http://www.joget.org/images/welcome.png\";"
                + "$(image).load(function(){"
                + "$('#link').hide();$('#frame').show();"
                + "});"
                + "</script>";
        return content;
    }
    
}
