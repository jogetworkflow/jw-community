package org.joget.apps.ext;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.dao.AppDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.base.PluginProperty;
import org.joget.plugin.base.PluginWebSupport;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Plugin to add content to the web console header, footer and body
 */
public class ConsoleWebPlugin extends ExtDefaultPlugin implements PluginWebSupport {

    @Override
    public String getName() {
        return "Console Web Plugin";
    }

    @Override
    public String getVersion() {
        return "5.0.0";
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
        } else if ("logo".equals(spot)) {
            content = getLogo(path);
        } else if ("footer".equals(spot)) {
            content = getFooter(path);
        } else if ("login".equals(spot)) {
            content = getLogin();
        } else if ("home".equals(spot)) {
            content = getHome();
        } else if ("welcome".equals(spot)) {
            content = getWelcome();
        } else if ("settings".equals(spot)) {
            content = getSettings(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
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
        MessageSource messageSource = (MessageSource)AppUtil.getApplicationContext().getBean("messageSource");
        Locale locale = LocaleContextHolder.getLocale();
        String header = messageSource.getMessage("console.header.top.title", null, "", locale);
        return header;
    }

    /**
     * Content to be added to the web console header.
     * @param path Current JSP path e.g. /WEB-INF/jsp/console/home.jsp
     * @return 
     */
    public String getLogo(String path) {
        MessageSource messageSource = (MessageSource)AppUtil.getApplicationContext().getBean("messageSource");
        Locale locale = LocaleContextHolder.getLocale();
        String header = messageSource.getMessage("console.header.top.logo", null, "", locale);
        return header;
    }

    /**
     * Content to be added to the web console footer.
     * @param path Current JSP path e.g. /WEB-INF/jsp/console/home.jsp
     * @return 
     */
    public String getFooter(String path) {
        MessageSource messageSource = (MessageSource)AppUtil.getApplicationContext().getBean("messageSource");
        Locale locale = LocaleContextHolder.getLocale();
        String revision = messageSource.getMessage("console.footer.label.revision", null, "", locale);
        String footer = "&copy; Joget DX - Joget Inc. All Rights Reserved. " + revision;
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
                + "<iframe id=\"welcomeFrame\" style=\"height:100vh; width:100%; box-sizing: border-box; overflow:hidden;\" frameborder=\"0\"></iframe>"
                + "</div>"
                + "<div class=\"clear\"></div>"
                + "<script>$(window).on(\"load\", function() { $(\"#welcomeFrame\").attr(\"src\", \"https://www.joget.org/updates/DX7/welcome/?src=community\"); });</script>";
        return content;
    }
    
    /**
     * Returns information regarding an app.
     * @param appId
     * @param version
     * @return 
     */
    public String getAppInfo(String appId, String version) {
        return "";
    }
    
    /**
     * Verify valid app license, to return the appropriate page.
     * @param appId
     * @param version
     * @return 
     */
    public String verifyAppVersion(String appId, String version) {
        AppService appService = (AppService)AppUtil.getApplicationContext().getBean("appService");
        AppDefinitionDao appDefinitionDao = (AppDefinitionDao)AppUtil.getApplicationContext().getBean("appDefinitionDao");
        AppDefinition appDef = appService.getAppDefinition(appId, version);
        if (appDef != null) {
            return null;
        }
        // get latest version
        Long latestVersion = appDefinitionDao.getLatestVersion(appId);
        if (latestVersion != null && latestVersion != 0) {
            return "redirect:/web/console/app/" + appId + "/processes";
        } else {
            // no version found, redirect to home page
            return "redirect:/web/console/home";
        }
    }
    
    /**
     * System info in the System Settings
     * @param request
     * @param response
     * @return 
     */
    public String getSettings(HttpServletRequest request, HttpServletResponse response) {
        HashMap model = new HashMap();
        PluginManager pluginManager = (PluginManager)AppUtil.getApplicationContext().getBean("pluginManager");
        String content = pluginManager.getPluginFreeMarkerTemplate(model, getClass().getName(), "/templates/settings.ftl", null);
        return content;
    }
    
}
