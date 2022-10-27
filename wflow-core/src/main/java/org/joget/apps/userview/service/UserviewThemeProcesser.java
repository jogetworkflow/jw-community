package org.joget.apps.userview.service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.MobileUtil;
import org.joget.apps.userview.lib.DefaultV5EmptyTheme;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewCategory;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.model.UserviewPage;
import org.joget.apps.userview.model.UserviewPwaTheme;
import org.joget.apps.userview.model.UserviewTheme;
import org.joget.apps.userview.model.UserviewV5Theme;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.directory.model.service.UserSecurity;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;

public class UserviewThemeProcesser {

    Userview userview;
    UserviewV5Theme defaultTheme;
    UserviewV5Theme theme;
    HttpServletRequest request;
    String redirectUrl = null;
    String alertMessage = null;
    boolean isAuthorized = false;
    boolean isLoginPage = false;
    boolean isPwaOfflinePage = false;
    boolean isPwaUnavailablePage = false;
    boolean isQuickEditEnabled = AppUtil.isQuickEditEnabled();

    public UserviewThemeProcesser(Userview userview, HttpServletRequest request) {
        this.userview = userview;
        this.request = request;
        
        isAuthorized = userview.getSetting().getPermission() == null || (userview.getSetting().getPermission() != null && userview.getSetting().getPermission().isAuthorize());
        if (!isAuthorized) {
            this.userview.setCurrent(null);
        }
    }

    public String getPreviewView() {
        if (userview.getSetting().getTheme() != null && !(userview.getSetting().getTheme() instanceof UserviewV5Theme)) {
            return "ubuilder/preview";
        }

        init();

        return "ubuilder/v5view";
    }

    public String getLoginView() {
        if (userview.getSetting().getTheme() != null && !(userview.getSetting().getTheme() instanceof UserviewV5Theme)) {
            return "ubuilder/login";
        }
        
        String loginRedirection = loginRedirection();
        if (loginRedirection != null) {
            return loginRedirection;
        }

        isLoginPage = true;
        init();

        return "ubuilder/v5view";
    }

    public String getView() {
        
        if (userview.getSetting().getTheme() != null && !(userview.getSetting().getTheme() instanceof UserviewV5Theme)) {
            return "ubuilder/view";
        }

        String mobileViewRedirection = mobileViewRedirection();
        if (mobileViewRedirection != null) {
            return mobileViewRedirection;
        }
        
        init();
        
        //check if it's PWA theme offline page
        if(theme instanceof UserviewPwaTheme){
            String menuId = request.getParameter("menuId");
            if(UserviewPwaTheme.PWA_OFFLINE_MENU_ID.equals(menuId)){
                isPwaOfflinePage = true;
                return "ubuilder/v5view";
            }
        }
        
        //check if it's PWA theme page unavailable page
        if(theme instanceof UserviewPwaTheme){
            String menuId = request.getParameter("menuId");
            if(UserviewPwaTheme.PAGE_UNAVAILABLE_MENU_ID.equals(menuId)){
                isPwaUnavailablePage = true;
                return "ubuilder/v5view";
            }
        }

        String homePageRedirection = homePageRedirection();
        if (homePageRedirection != null) {
            return homePageRedirection;
        }

        String loginPageRedirection = loginPageRedirection();
        if (loginPageRedirection != null) {
            return loginPageRedirection;
        }

        String themeRedirection = theme.handleRedirection();
        if (themeRedirection != null) {
            return themeRedirection;
        }

        return "ubuilder/v5view";
    }

    public void init() {
        if (userview.getSetting().getTheme() != null) {
            theme = (UserviewV5Theme) userview.getSetting().getTheme();
        } else {
            theme = new DefaultV5EmptyTheme();
        }
    }

    public String getHtml() {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        if (request != null) {
            String componentId = WorkflowUtil.getHttpServletRequest().getHeader("__ajax_component");
            if (componentId != null) {
                return getComponentHtml(componentId);
            }
        }
        
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("processor", this);
        data.put("params", userview.getParams());
        data.put("userview", userview);
        data.put("appId", userview.getParamString("appId"));
        data.put("is_login_page", isLoginPage);
        if (isLoginPage) {
            data.put("login_form_footer", DirectoryUtil.getLoginFormFooter());

            if (request.getSession() != null) {
                Throwable exception = (Throwable) request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
                if (exception != null) {
                    data.put("login_exception", exception.getMessage());
                }
            }
            data.put("login_error_classes", "form-errors alert alert-warning");
        }
        data.put("context_path", request.getContextPath());
        data.put("build_number", ResourceBundleUtil.getMessage("build.number"));
        String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
        data.put("right_to_left", "true".equalsIgnoreCase(rightToLeft));
        String locale = AppUtil.getAppLocale();
        String language = AppUtil.getAppLanguage();
        data.put("locale", locale);
        data.put("language", language);
        data.put("embed", "true".equalsIgnoreCase(userview.getParamString("embed")));
        data.put("body_id", getBodyId());
        data.put("body_classes", getBodyClasses(rightToLeft, locale));
        data.put("base_link", request.getContextPath() + getBaseLink());
        data.put("home_page_link", request.getContextPath() + getHomePageLink());
        data.put("title", getTitle());
        data.put("hide_nav", false);
        data.put("nav_id", "navigation");
        data.put("nav_classes", "nav-collapse sidebar-nav");
        data.put("categories_container_id", "category-container");
        data.put("categories_container_classes", "nav nav-tabs nav-stacked main-menu");
        data.put("category_classes", "category");
        data.put("first_category_classes", "first");
        data.put("last_category_classes", "last");
        data.put("current_category_classes", "current-category active");
        data.put("combine_single_menu_category", false);
        data.put("menus_container_classes", "menu-container");
        data.put("menu_classes", "menu");
        data.put("first_menu_classes", "first");
        data.put("last_menu_classes", "last");
        data.put("current_menu_classes", "current active");
        data.put("main_container_id", "main");
        data.put("sidebar_id", "sidebar");
        data.put("content_id", "content");

        WorkflowUserManager wum = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        User user = wum.getCurrentUser();
        boolean isLoggedIn = user != null;
        data.put("is_logged_in", isLoggedIn);
        if (isLoggedIn) {
            data.put("username", wum.getCurrentUsername());
            data.put("user", user);
            data.put("logout_link", request.getContextPath() + "/j_spring_security_logout");
        } else {
            data.put("login_link", request.getContextPath() + getLoginLink());
        }
        
        if (request != null) {
            if("true".equalsIgnoreCase(request.getHeader("__ajax_menu_count"))) {
                return getAjaxMenusCount(data);
            }
        }
        
        data.put("content", getContent(data));

        String handleMenuResponse = handleMenuResponse();
        if (handleMenuResponse != null) {
            return handleMenuResponse;
        }

        data.put("metas", getMetas(data));
        data.put("joget_header", getJogetHeader());
        data.put("js_css_lib", getJsCssLib(data));
        data.put("fav_icon_link", getFavIconLink(data));
        data.put("js", getJs(data));
        data.put("css", getCss(data));
        data.put("head", getHead(data));
        if (!"true".equalsIgnoreCase(userview.getParamString("embed"))) {
            if (isAuthorized) {
                data.put("menus", getMenus(data));
            }
            data.put("header", getHeader(data));
            data.put("footer", getFooter(data));
        }
        data.put("joget_footer", getJogetFooter());
        data.put("content_container", getContentContainer(data));

        return getLayout(data);
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
    
    public String getRedirectUrl() {
        return redirectUrl;
    }
    
    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }
    
    public String getAlertMessage() {
        return alertMessage;
    }

    protected String mobileViewRedirection() {
        boolean mobileAgent = (!MobileUtil.isMobileDisabled() && MobileUtil.isMobileUserAgent(request));
        boolean disableMobileView = userview.getSetting().getTheme().isMobileViewDisabled();
        boolean desktopCookie = false;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("desktopSite".equals(cookie.getName())) {
                    if ("true".equalsIgnoreCase(cookie.getValue())) {
                        desktopCookie = true;
                    }
                    break;
                }
            }
        }

        if (mobileAgent && !disableMobileView && !desktopCookie) {
            String url = "/web/mobile/" + userview.getParamString("appId") + "/" + userview.getPropertyString("id") + "/" + userview.getParamString("key") + "/";
            if (!userview.getParamString("menuId").isEmpty()) {
                url += userview.getParamString("menuId"); 
            }
            return "redirect:" + url;
        }
        return null;
    }

    protected String homePageRedirection() {
        String homePageLink = getHomePageLink();
        if (userview.getParamString("menuId").isEmpty() && !userview.getPropertyString("homeMenuId").isEmpty()) {
            return "redirect:" + homePageLink + (request.getQueryString() == null ? "" : ("?" + StringUtil.decodeURL(request.getQueryString())));
        }
        return null;
    }
    
    protected String loginRedirection() {
        boolean isAnonymous = WorkflowUtil.isCurrentUserAnonymous();
        if (!isAnonymous) {
            String url = getBaseLink();
            String menuId = userview.getParamString("menuId");
            //check current redirect url is exist, else redirect to home
            boolean isExist = false;
            
            if (userview.getCurrent() != null && (menuId.equals(userview.getCurrent().getPropertyString("id")) || menuId.equals(userview.getCurrent().getPropertyString("customId")))) {
                isExist = true;
            }
            
            if (!isExist) {
                if (menuId != null && !menuId.isEmpty()) {
                    for (UserviewCategory c : userview.getCategories()) {
                        for (UserviewMenu m : c.getMenus()) {
                            if (menuId.equals(m.getPropertyString("id")) || menuId.equals(m.getPropertyString("customId"))) {
                                isExist = true;
                                break;
                            }
                        }
                        if (isExist) {
                            break;
                        }
                    }
                    if (!isExist) {
                        UserviewTheme temp = userview.getSetting().getTheme();
                        if (temp != null && temp instanceof UserviewV5Theme) {
                            String[] themeDefinedMenus = ((UserviewV5Theme) temp).themeDefinedMenusId();
                            if (themeDefinedMenus != null) {
                                for (String dm : themeDefinedMenus) {
                                    if (dm.equals(menuId)) {
                                        isExist = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (isExist) {
                if (!userview.getParamString("menuId").isEmpty()) {
                    url += menuId;
                }
                if (request.getQueryString() != null) {
                    url += "?" + StringUtil.decodeURL(request.getQueryString());
                } else if (request.getQueryString() == null && request.getHeader("referer") != null) {
                    String referer = request.getHeader("referer");
                    if (referer != null && referer.contains("?")) {
                        String queryString = referer.substring(referer.indexOf("?"));
                        url += queryString;
                    }
                }
            } else {
                url += userview.getProperty("homeMenuId");
            }
            
            return "redirect:" + url;
        }
        return null;
    }
        
    protected String loginPageRedirection() {
        boolean isAnonymous = WorkflowUtil.isCurrentUserAnonymous();
        boolean hasCurrentPage = userview.getCurrent() != null;
        if ((!isAuthorized || !hasCurrentPage) && isAnonymous) {
            return "redirect:" + getLoginLink() + (request.getQueryString() == null ? "" : ("?" + StringUtil.decodeURL(request.getQueryString())));
        }
        return null;
    }

    protected String getBodyId() {
        String bodyId = "";

        if (isLoginPage) {
            bodyId = "login";
        } else if (!isAuthorized) {
            bodyId = "unauthorize";
        } else if (userview.getCurrent() != null) {
            bodyId = userview.getCurrent().getPropertyString("customId");
            if (bodyId.isEmpty()) {
                bodyId = userview.getCurrent().getPropertyString("id");
            }
        } else {
            bodyId = "pageNotFound";
        }

        return bodyId;
    }

    public String getBodyClasses(String rightToLeft, String locale) {
        String classes = "";
        classes += ("true".equalsIgnoreCase(userview.getParamString("embed"))) ? "embeded " : "";
        classes += ("true".equalsIgnoreCase(rightToLeft) || locale.startsWith("ar")) ? "rtl " : "";
        classes += locale;
        return classes;
    }

    public String getJogetHeader() {
        String cp = request.getContextPath();
        String bn = ResourceBundleUtil.getMessage("build.number");
        String html = "<script type=\"text/javascript\" src=\"" + cp + "/wro/common.preload.js?build=" + bn + "\"></script>\n"
                + "<script type=\"text/javascript\" src=\"" + cp + "/wro/common.js?build=" + bn + "\" defer></script>\n"
                + "<script>loadCSS(\"" + cp + "/wro/common.css" + "\")</script>\n"
                + "<script type=\"text/javascript\">\n";

        UserSecurity us = DirectoryUtil.getUserSecurity();
        if (!(us != null && us.getAllowSessionTimeout())) {
            html += "$(document).ready(function() {\n"
                    + "            window.setInterval(\"keepMeAlive('image_alive')\", 200000);\n"
                    + "        });\n"
                    + "        function keepMeAlive(imgName) {  \n"
                    + "             myImg = document.getElementById(imgName);   \n"
                    + "             if (myImg) {\n"
                    + "                 myImg.src = myImg.src.replace(/\\?.*$/, '?' + Math.random());   \n"
                    + "             } else { \n"
                    + "                 $('body').append('<img id=\"image_alive\" style=\"display:none;\" width=\"1\" height=\"1\" src=\"" + cp + "/images/v3/cj.gif?\" alt=\"\">');\n"
                    + "             } \n"
                    + "        }  ";
        }

        //fix IE browser
        html += "if ($.browser.msie) {\n"
                + "            $(document).on(\"keydown\", function (e) {\n"
                + "                if (e.which === 8 && !$(e.target).is(\"input:not([readonly]), textarea:not([readonly])\")) {\n"
                + "                    e.preventDefault();\n"
                + "                }\n"
                + "            });\n"
                + "        }\n";

        //userview print feature
        html += "function userviewPrint(){\n"
                + "            $('head').append('<link id=\"userview_print_css\" rel=\"stylesheet\" href=\"" + cp + "/wro/userview_print.min.css\" type=\"text/css\" media=\"print\"/>');\n"
                + "            setTimeout(\"do_print()\", 1000); \n"
                + "        }\n"
                + "        function do_print(){\n"
                + "            window.print();\n"
                + "            $('#userview_print_css').remove();\n"
                + "        }\n";

        if ("true".equalsIgnoreCase(userview.getParamString("isPreview"))) {
            html += "$(document).ready(function(){\n$('a:not(.dropdown):not([data-toggle])').click(function(){\n"
                    + "        var action = $(this).attr('href');\n"
                    + "if (action !== \"\" && action !== undefined && action !== \"#\" && action !== \"javascript:;\"){\n"
                    + "        $('#preview').attr('action', action);\n"
                    + "        $('#preview').submit();\n"
                    + "}\n"
                    + "        return false;\n"
                    + "    });\n"
                    + "\n});\n";
        }

        html += "UI.base = \"" + request.getContextPath() + "\";\n"
                + "        UI.userview_app_id = '"+userview.getParamString("appId")+"';\n"
                + "        UI.userview_id = '"+userview.getPropertyString("id")+"';\n"
                + "        UI.locale = '"+AppUtil.getAppLocale()+"';\n";

        if (alertMessage != null && !alertMessage.isEmpty()) {
            html += "alert(\"" + alertMessage + "\");\n";
        }

        html += "</script>\n";

        html += "    <script>\n" +
                "        ConnectionManager.tokenName = \"" + SecurityUtil.getCsrfTokenName() + "\";\n" +
                "        ConnectionManager.tokenValue = \"" + SecurityUtil.getCsrfTokenValue(request) + "\";\n" +
                "        JPopup.tokenName = \"" + SecurityUtil.getCsrfTokenName() + "\";\n" +
                "        JPopup.tokenValue = \"" + SecurityUtil.getCsrfTokenValue(request) + "\";\n" +
                "    </script>";
        
        return html;
    }

    public String getJogetFooter() {
        String html = "";
        
        if (!"true".equalsIgnoreCase(theme.getPropertyString("disableHelpGuide"))) {
            html = "<script type=\"text/javascript\">\n"
                + "            HelpGuide.base = \"" + request.getContextPath() + "\"\n"
                + "            HelpGuide.attachTo = \"#help-container\";\n"
                + "            HelpGuide.key = \"help.web.userview."+userview.getParamString("appId")+"."+userview.getParamString("userviewId")+"."+getBodyId()+"\";\n"
                + "            $(window).on('load', function() {\n"
                + "                setTimeout(function() {"
                + "                    HelpGuide.show();\n"
                + "                }, 0);"
                + "            });\n"
                + "        </script>\n";
        }

        html += AppUtil.getSystemAlert() + "\n";

        Map<String, Object> modelMap = new HashMap<String, Object>();
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        modelMap.put("appId", userview.getParamString("appId"));
        modelMap.put("appVersion", appDef.getVersion().toString());
        modelMap.put("userviewId", userview.getParamString("userviewId"));
        
        if (!"true".equalsIgnoreCase(userview.getParamString("isPreview")) && !"true".equalsIgnoreCase(userview.getParamString("isTemplate"))) {
            html += UserviewUtil.renderJspAsString("ubuilder/adminBar.jsp", modelMap) + "\n\n";
        }
        
        if ("true".equalsIgnoreCase(userview.getParamString("isPreview"))) {
            html += "<!--[if IE]><div id=\"preview-label\" class=\"ie\">"+ResourceBundleUtil.getMessage("fbuilder.preview")+"</div><![endif]-->\n"
                    + "        <!--[if !IE]><!--><div id=\"preview-label\">"+ResourceBundleUtil.getMessage("fbuilder.preview")+"</div><!--<![endif]-->        \n"
                    + "        <div style=\"display:none\" id=\"preview-form\">\n"
                    + "            <form id=\"preview\" action=\"\" method=\"post\">\n"
                    + "                <input type=\"hidden\" name=\""+SecurityUtil.getCsrfTokenName()+"\" value=\"" + StringEscapeUtils.escapeHtml(SecurityUtil.getCsrfTokenValue(request)) + "\"/>\"/>\n"
                    + "                <input type=\"hidden\" name=\"json\" value=\"" + StringEscapeUtils.escapeHtml(userview.getParamString("json")) + "\"/>\"/>\n"
                    + "            </form>\n"
                    + "        </div>\n";
        } else if ("true".equals(userview.getSetting().getPropertyString("tempDisablePermissionChecking"))) {
            html += "<!--[if IE]><div id=\"preview-label\" class=\"ie testing\"><a onclick=\"$('#preview-label').remove()\">x</a> "+ResourceBundleUtil.getMessage("ubuilder.permissionDisabled")+"</div><![endif]-->\n";
            html += "<!--[if !IE]><!--><div id=\"preview-label\" class=\"testing\"><a onclick=\"$('#preview-label').remove()\">x</a> "+ResourceBundleUtil.getMessage("ubuilder.permissionDisabled")+"</div><!--<![endif]-->";
        }

        return html;
    }

    public String getTitle() {
        String title = userview.getPropertyString("name");
        if (userview.getCurrent() != null) {
            title += "&nbsp;&gt;&nbsp;" + userview.getCurrent().getPropertyString("label");
        }

        return StringUtil.stripAllHtmlTag(title);
    }
    
    protected String getBaseLink() {
        String key = userview.getParamString("key");
        if (key.isEmpty()) {
            key = Userview.USERVIEW_KEY_EMPTY_VALUE;
        }
        return "/web/" + ("true".equalsIgnoreCase(userview.getParamString("embed")) ? "embed/" : "") + "userview/" + userview.getParamString("appId") + "/" + userview.getPropertyString("id") + "/" + key + "/";
    }

    protected String getHomePageLink() {
        String customHomePage = theme.getCustomHomepage();
        if (customHomePage == null || customHomePage.isEmpty()) {
            customHomePage = userview.getPropertyString("homeMenuId");
        }
            
        return getBaseLink() + customHomePage;
    }

    protected String getLoginLink() {
        String key = userview.getParamString("key");
        if (key.isEmpty()) {
            key = Userview.USERVIEW_KEY_EMPTY_VALUE;
        }
        return "/web/" + ("true".equalsIgnoreCase(userview.getParamString("embed")) ? "embed/" : "") + "ulogin/" + userview.getParamString("appId") + "/" + userview.getPropertyString("id") + "/" + key + "/" + userview.getParamString("menuId");
    }

    protected String getMetas(Map<String, Object> data) {
        String content = theme.getMetas(data);
        if (content != null) {
            return content;
        } else {
            return getDefaultTheme().getMetas(data);
        }
    }

    protected String getCss(Map<String, Object> data) {
        String content = theme.getCss(data);
        if (content != null) {
            return content;
        } else {
            return getDefaultTheme().getCss(data);
        }
    }

    protected String getFavIconLink(Map<String, Object> data) {
        String content = theme.getFavIconLink(data);
        if (content != null) {
            return content;
        } else {
            return getDefaultTheme().getFavIconLink(data);
        }
    }

    protected String getJsCssLib(Map<String, Object> data) {
        String content = theme.getJsCssLib(data);
        if (content != null) {
            return content;
        } else {
            return getDefaultTheme().getJsCssLib(data);
        }
    }

    protected String getJs(Map<String, Object> data) {
        String content = theme.getJs(data);
        if (content != null) {
            return content;
        } else {
            return getDefaultTheme().getJs(data);
        }
    }

    protected String getHead(Map<String, Object> data) {
        String content = theme.getHead(data);
        if (content != null) {
            return content;
        } else {
            return getDefaultTheme().getHead(data);
        }
    }

    protected String getHeader(Map<String, Object> data) {
        String content = theme.getHeader(data);
        if (content != null) {
            return content;
        } else {
            return getDefaultTheme().getHeader(data);
        }
    }

    protected String getFooter(Map<String, Object> data) {
        String content = theme.getFooter(data);
        if (content != null) {
            return content;
        } else {
            return getDefaultTheme().getFooter(data);
        }
    }

    protected String getMenus(Map<String, Object> data) {
        String content = "";
        String menu = theme.getMenus(data);
        if (menu == null) {
            menu = getDefaultTheme().getMenus(data);
        }
        if (isQuickEditEnabled) {
            String label = ResourceBundleUtil.getMessage("adminBar.label.menu") + ": " + userview.getPropertyString("name");
            String url = request.getContextPath() + "/web/console/app/" + userview.getParamString("appId") + "/" + userview.getParamString("appVersion") + "/userview/builder/" + userview.getPropertyString("id");
            content += "<div class=\"quickEdit\" style=\"display: none\">\n";
            content += "    <a href=\"" + url + "\" target=\"_blank\"><i class=\"fas fa-pencil-alt\"></i> " + label + "</a>\n";
            content += "</div>\n";
        }
        content += menu;
        return content;
    }
    
    protected String getAjaxMenusCount(Map<String, Object> data) {
        String content = theme.getAjaxMenusCount(data);
        if (content == null) {
            content = getDefaultTheme().getAjaxMenusCount(data);
        }
        return content;
    }

    protected String getLayout(Map<String, Object> data) {
        String content = theme.getLayout(data);
        if (content != null) {
            return content;
        } else {
            return getDefaultTheme().getLayout(data);
        }
    }

    protected String getContentContainer(Map<String, Object> data) {
        String content = theme.getContentContainer(data);
        if (content != null) {
            return content;
        } else {
            return getDefaultTheme().getContentContainer(data);
        }
    }

    protected String getLoginForm(Map<String, Object> data) {
        String content = theme.getLoginForm(data);
        if (content != null) {
            return content;
        } else {
            return getDefaultTheme().getLoginForm(data);
        }
    }
    
    //check is there any custom content handled by theme
    protected String getCustomContent(Map<String, Object> data) {
        String content = theme.getCustomContent(data);
        if (content != null) {
            return content;
        } else {
            return getDefaultTheme().getCustomContent(data);
        }
    }

    protected String getContent(Map<String, Object> data) {
        String content = "";
        try {
            if(isPwaOfflinePage){
                UserviewPwaTheme pwaTheme = (UserviewPwaTheme) theme;
                return pwaTheme.handlePwaOfflinePage(data);
            }else if(isPwaUnavailablePage){
                UserviewPwaTheme pwaTheme = (UserviewPwaTheme) theme;
                return pwaTheme.handlePwaUnavailablePage(data);
            }else if (isLoginPage) {
                return getLoginForm(data);
            } else if (!isAuthorized) {
                return "<h3>"+ResourceBundleUtil.getMessage("ubuilder.noAuthorize")+"</h3>";
            } else {
                String customContent = getCustomContent(data);
                if (customContent != null) {
                    return customContent;
                } else if (userview.getCurrent() != null) {
                    if (isQuickEditEnabled) {
                        String label = ResourceBundleUtil.getMessage("adminBar.label.page") + ": " + userview.getCurrent().getPropertyString("label");
                        String url = request.getContextPath() + "/web/console/app/" + userview.getParamString("appId") + "/" + userview.getParamString("appVersion") + "/userview/builder/" + userview.getPropertyString("id") + "?menuId=" + userview.getCurrent().getPropertyString("id");
                        content += "<div class=\"quickEdit\" style=\"display: none\">\n";
                        content += "    <a href=\"" + url + "\" target=\"_blank\"><i class=\"fas fa-pencil-alt\"></i> " + label + "</a>\n";
                        content += "</div>\n";
                    }
                    if (userview.getCurrent().getUserview() == null) {
                        userview.getCurrent().setUserview(userview);
                    }
                    UserviewPage page = new UserviewPage(userview.getCurrent());
                    content += page.render();
                } else {
                    String pageNotFound = theme.handlePageNotFound(data);
                    if (pageNotFound != null) {
                        return pageNotFound;
                    } else {
                        return getDefaultTheme().handlePageNotFound(data);
                    }
                }
            }
        } catch (Exception e) {
            String errorHandle = theme.handleContentError(e, data);
            if (errorHandle != null) {
                return errorHandle;
            } else {
                return getDefaultTheme().handleContentError(e, data);
            }
        }

        return content;
    }

    protected String handleMenuResponse() {
        String menuAlertMessage = null;
        String menuRedirectUrl = null;
        String redirectParent = null;
        if (userview.getCurrent() != null) {
            menuAlertMessage = userview.getCurrent().getPropertyString(UserviewMenu.ALERT_MESSAGE_PROPERTY);
            menuRedirectUrl = userview.getCurrent().getPropertyString(UserviewMenu.REDIRECT_URL_PROPERTY);
            redirectParent = userview.getCurrent().getPropertyString(UserviewMenu.REDIRECT_PARENT_PROPERTY);
        } else if (userview.getProperty("pageNotFoundMenu") != null) {
            try {
                UserviewMenu menu = (UserviewMenu) userview.getProperty("pageNotFoundMenu");
                menuAlertMessage = menu.getPropertyString(UserviewMenu.ALERT_MESSAGE_PROPERTY);
                menuRedirectUrl = menu.getPropertyString(UserviewMenu.REDIRECT_URL_PROPERTY);
                redirectParent = menu.getPropertyString(UserviewMenu.REDIRECT_PARENT_PROPERTY);
            } catch (Exception e) {}
        }

        if ((menuAlertMessage != null && !menuAlertMessage.isEmpty()) || (redirectParent != null && !redirectParent.isEmpty() && !redirectParent.equalsIgnoreCase("false"))
                || (menuRedirectUrl != null && (menuRedirectUrl.contains("SCRIPT_RELOAD_PARENT") || menuRedirectUrl.contains("SCRIPT_CLOSE_POPUP")))) {
            if (menuRedirectUrl != null && !menuRedirectUrl.isEmpty()) {
                if (menuRedirectUrl.contains("SCRIPT_RELOAD_PARENT")) {
                    menuRedirectUrl = "SCRIPT_RELOAD_PARENT";
                } else if (menuRedirectUrl.contains("SCRIPT_CLOSE_POPUP")) {
                    menuRedirectUrl = "SCRIPT_CLOSE_POPUP";
                }
                Map<String, String> data = new HashMap<String, String>();
                data.put("alertMessage", menuAlertMessage);
                data.put("redirectUrl", menuRedirectUrl);
                data.put("redirectParent", redirectParent);
                return UserviewUtil.getTemplate(getDefaultTheme(), data, "/templates/userview/redirect.ftl");
            } else {
                alertMessage = menuAlertMessage;
            }
        } else if (menuRedirectUrl != null && !menuRedirectUrl.isEmpty()) {
            if (!menuRedirectUrl.toLowerCase(Locale.ENGLISH).startsWith("http") && !menuRedirectUrl.toLowerCase(Locale.ENGLISH).startsWith("/") && !menuRedirectUrl.startsWith(request.getContextPath())) {
                redirectUrl = "/web/";
                if ("true".equalsIgnoreCase(userview.getParamString("embed"))) {
                    redirectUrl += "embed/";
                }
                redirectUrl += "userview/" + userview.getParamString("appId") + "/";
                redirectUrl += userview.getPropertyString("id") + "/";
                redirectUrl += userview.getParamString("key") + "/" + menuRedirectUrl;
            } else if (menuRedirectUrl.startsWith(request.getContextPath())) {
                redirectUrl = menuRedirectUrl.replaceFirst(request.getContextPath(), "");
            } else {
                redirectUrl = menuRedirectUrl;
            }
        }
        return null;
    }

    protected UserviewV5Theme getDefaultTheme() {
        if (defaultTheme == null) {
            defaultTheme = new DefaultV5EmptyTheme();
        }
        return defaultTheme;
    }
    
    protected String getComponentHtml(String componentId) {
        String html = "";
        
        if (userview.getCurrent() != null) {
            UserviewPage page = new UserviewPage(userview.getCurrent());
            html = page.renderComponent(componentId, null);

            String handleMenuResponse = handleMenuResponse();
            if (handleMenuResponse != null) {
                return handleMenuResponse;
            } else if (redirectUrl != null) {
                return "<script>location.href = \""+redirectUrl+"\";</script>";
            }
            
            if (alertMessage != null && !alertMessage.isEmpty()) {
                html = "<script>alert(\"" + StringUtil.escapeString(alertMessage, StringUtil.TYPE_JAVASCIPT, null) + "\");</script>" + html;
            }
        }
        
        return html;
    }
}
