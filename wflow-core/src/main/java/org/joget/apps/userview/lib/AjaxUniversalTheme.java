package org.joget.apps.userview.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.MobileUtil;
import org.joget.apps.userview.model.SupportBuilderColorConfig;
import org.joget.apps.userview.service.UserviewThemeProcesser;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.directory.model.User;
import org.joget.plugin.enterprise.UniversalTheme;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;

public class AjaxUniversalTheme extends UniversalTheme implements SupportBuilderColorConfig {
    protected Boolean isAjaxContent = null;
    
    @Override
    public String getName() {
        return "DX 8 Plain Theme";
    }

    @Override
    public String getVersion() {
        return "8.0-SNAPSHOT";
    }

    @Override
    public String getLabel() {
        return getName();
    }
    
    @Override
    public String getClassName() {
        return getClass().getName();
    }
    
    @Override
    protected String getInternalJsCssLib(Map<String, Object> data) {
        String jscss = "";
        jscss += super.getInternalJsCssLib(data);
        if (!isAjaxContent(data) && !"true".equalsIgnoreCase(userview.getParamString("isPreview")) && !(data.get("is_login_page") != null && ((Boolean) data.get("is_login_page"))) && !(data.get("is_popup_view") != null && ((Boolean) data.get("is_popup_view")))) {
            jscss += "\n<script src=\"" + data.get("context_path") + "/ajaxuniversal/js/ajaxtheme.js\" defer></script>";
            jscss += "\n<script>" + getContentPlaceholderRules() + "</script>";
        }
        return jscss;
    }
    
    @Override
    public String getLayout(Map<String, Object> data) {
        if (isAjaxContent(data)) {
            UserviewThemeProcesser processor = (UserviewThemeProcesser) data.get("processor");
            if (processor.getAlertMessage() != null && !processor.getAlertMessage().isEmpty()) {
                data.put("userview_menu_alert", "<script>alert(\"" + StringUtil.escapeString(processor.getAlertMessage(), StringUtil.TYPE_JAVASCIPT, null) + "\");</script>");
            }
            if (processor.getRedirectUrl() != null && !processor.getRedirectUrl().isEmpty() && !isCurrentUserviewUrl(processor.getRedirectUrl())) {
                String redirectUrl = processor.getRedirectUrl(); //the redirect url from UserviewThemeProcesser.handleMenuResponse() usually without context path
                if (redirectUrl.startsWith("/web/")) {
                    redirectUrl = data.get("context_path") + redirectUrl;
                }
                data.put("content", "<script>window.location.href = \""+redirectUrl+"\";</script>");
                processor.setRedirectUrl(null);
            }
            
            String analyzerData = processor.getAnalyzerStatus();
            if (analyzerData != null && !"true".equalsIgnoreCase(userview.getParamString("isPreview")) && !"true".equalsIgnoreCase(userview.getParamString("isTemplate"))) {
                data.put("analyzer", analyzerData);
            }
            
            if (showHomeBanner()) {
                data.put("main_container_before", "<div class=\"home_banner\"><div class=\"home_banner_inner\">"+getPropertyString("homeAttractBanner")+"</div></div>");
            }
            
            return UserviewUtil.getTemplate(this, data, "/templates/ajaxuniversal/ajaxlayout.ftl");
        } else {
            if ("true".equals(getPropertyString("horizontal_menu"))) {
                data.put("body_classes", data.get("body_classes").toString() + " horizontal_menu");
            } else if ("horizontal_inline".equals(getPropertyString("horizontal_menu"))){
                data.put("body_classes", data.get("body_classes").toString() + " horizontal_menu inline_menu");
            } else if ("no".equals(getPropertyString("horizontal_menu"))) {
                data.put("body_classes", data.get("body_classes").toString() + " horizontal_menu no_menu");
            }

            if (showHomeBanner()) {
                data.put("body_classes", data.get("body_classes").toString() + " has_home_banner");
            }
            data.put("body_inner_before", "<div class=\"page-loader\"><div class=\"spinner\"></div></div>");
            if ("true".equals(getPropertyString("darkMode"))) {
                data.put("body_inner_before", data.get("body_inner_before").toString() +
                                "<script>" + 
                                "const theme = localStorage.getItem(\"theme\");\n" +
                                "if (theme === \"auto\"){\n"+
                                        "$(\"body\").addClass((window.matchMedia(\"(prefers-color-scheme: dark)\").matches ? \"dark\" : \"light\") + \"-mode\");\n" +                                        
                                        "}else{\n"+
                                "$(\"body\").addClass(theme + \"-mode\");\n" +
                                        "};\n"+
                                "</script>\n");
            }
            if("true".equals(getPropertyString("compactTheme"))) {
                data.put("body_inner_before", data.get("body_inner_before").toString() +
                                "<script>" + 
                                "const density = localStorage.getItem(\"density\");\n" +
                                "$(\"body\").addClass(density + \"-mode\");\n" +
                                "</script>\n");
            }
            return UserviewUtil.getTemplate(this, data, "/templates/userview/layout.ftl");
        }
    }
    
    @Override
    public String getHeader(Map<String, Object> data) {
        if (isAjaxContent(data)) {
            return "";
        } else {
            if ("true".equals(getPropertyString("horizontal_menu"))) {
                data.put("header_help_before", "<span class=\"grow\"></span>");
            }
            
            return super.getHeader(data);
        }
    }
    
    @Override
    public String getFooter(Map<String, Object> data) {
        if (isAjaxContent(data)) {
            return "";
        } else {
            return super.getFooter(data);
        }
    }
    
    @Override
    public String getContentContainer(Map<String, Object> data) {
        if (isAjaxContent(data)) {
            String contentInnerBefore = getBreadcrumb(data);
            if (getPropertyString("fontControl").equalsIgnoreCase("true")) {
                contentInnerBefore += getFontSizeController(data);
            }
            data.put("content_inner_before", contentInnerBefore);
            return null;
        } else {
            return super.getContentContainer(data);
        }
    }
    
    @Override
    public String getMenus(Map<String, Object> data) {
        if (isAjaxContent(data)) {
            data.put("combine_single_menu_category", true);
            return UserviewUtil.getTemplate(this, data, "/templates/userview/menus.ftl");
        } else {
            if ("minimized".equals(getPropertyString("horizontal_menu"))) {
                data.put("body_classes", data.get("body_classes").toString() + " sidebar-minimized");
                setProperty("horizontal_menu", "");
            }
            
            if (!getPropertyString("logo").isEmpty()) {
                data.put("brand_logo", "<div class=\"logo_container\"><a href=\""+data.get("home_page_link")+"\"><img class=\"logo\" alt=\"logo\" src=\""+getPropertyString("logo")+"\" /></a></div>");
            } else {
                String classes = (String) data.get("sidebar_brand_classes");
                if (classes == null) {
                    classes = "";
                }
                data.put("sidebar_brand_classes", classes + " no_logo");
            }

            data.put("nav_before", UserviewUtil.getTemplate(this, data, "/templates/ajaxuniversal/sidebar_brand.ftl"));
            
            return super.getMenus(data);
        }
    }
    
    @Override
    public String getJsCssLib(Map<String, Object> data) {
        if (isAjaxContent(data)) {
            return "";
        } else {
            String jsCssLink = "";
            
            jsCssLink += "<link href=\"" + data.get("context_path") + "/wro/ajaxuniversal.preload.min.css" + "\" rel=\"stylesheet\" />\n";
            
            jsCssLink += "<link rel=\"preload\" href=\"" + data.get("context_path") + "/js/fontawesome5/fonts/fontawesome-webfont.woff2?v=4.6.1\" as=\"font\" crossorigin />\n";
            jsCssLink += "<link rel=\"preload\" href=\"" + data.get("context_path") + "/js/fontawesome5/webfonts/fa-brands-400.woff2\" as=\"font\" crossorigin />\n";
            jsCssLink += "<link rel=\"preload\" href=\"" + data.get("context_path") + "/js/fontawesome5/webfonts/fa-solid-900.woff2\" as=\"font\" crossorigin />\n";
            jsCssLink += "<link rel=\"preload\" href=\"" + data.get("context_path") + "/universal/lib/material-design-iconic-font/fonts/Material-Design-Iconic-Font.woff2?v=2.2.0\" as=\"font\" crossorigin />\n";
            jsCssLink += "<script>loadCSS(\"" + data.get("context_path") + "/wro/ajaxuniversal.min.css" + "\")</script>\n";

            jsCssLink += "<style>" + generateLessCss() + "</style>";

            jsCssLink += "<script src=\"" + data.get("context_path") + "/wro/ajaxuniversal.min.js\" defer></script>\n";

            if (enableResponsiveSwitch()) {
                jsCssLink += "<script src=\"" + data.get("context_path") + "/universal/lib/responsive-switch.min.js\" defer></script>\n";
            } 
            jsCssLink += "<script>var _enableResponsiveTable = true;</script>\n";
            jsCssLink += getInternalJsCssLib(data);
            if ("true".equals(getPropertyString("darkMode"))) {
                jsCssLink += "<script src=\"" + data.get("context_path") + "/wro/darkTheme.js\" defer></script>\n";
                jsCssLink += "<link rel=\"stylesheet\" href=\"" + data.get("context_path") + "/wro/darkTheme.css\"></link>\n";
            }
            
            if ("true".equals(getPropertyString("compactTheme"))) {
                jsCssLink += "<script src=\"" + data.get("context_path") + "/wro/compactTheme.js\" defer></script>\n";
                jsCssLink += "<link rel=\"stylesheet\" href=\"" + data.get("context_path") + "/wro/compactTheme.css\"></link>\n";
            }
            
            if (MobileUtil.isIE()) {
                jsCssLink += "<script src=\"" + data.get("context_path") + "/js/ie/fetch.js\"></script>\n";
                jsCssLink += "<script src=\"" + data.get("context_path") + "/js/ie/css-vars-ponyfill.js\"></script>\n";
                jsCssLink += "<script>cssVars();</script>\n";
            }

            return jsCssLink;
        }
    }
    
    @Override
    public Set<String> getOfflineStaticResources() {
        Set<String> urls = new HashSet<String>();
        String contextPath = AppUtil.getRequestContextPath();
        String pathName = getPathName();
        String bn = ResourceBundleUtil.getMessage("build.number");
        urls.add(contextPath + "/wro/common.css");
        urls.add(contextPath + "/wro/ajaxuniversal.preload.min.css");
        urls.add(contextPath + "/wro/ajaxuniversal.min.css");
        urls.add(contextPath + "/wro/common.preload.js?build=" + bn);
        urls.add(contextPath + "/wro/common.js?build=" + bn);
        urls.add(contextPath + "/wro/form_common.js?build=" + bn);
        urls.add(contextPath + "/wro/ajaxuniversal.preload.min.js");
        urls.add(contextPath + "/wro/ajaxuniversal.min.js");
        urls.add(contextPath + "/universal/lib/responsive-switch.min.js");
        
        return urls;
    }
    
    @Override
    public String getCss(Map<String, Object> data) {
        if (isAjaxContent(data)) {
            return "";
        } else {
            return super.getCss(data);
        }
    }
    
    @Override
    public String getJs(Map<String, Object> data) {
        if (isAjaxContent(data)) {
            return "";
        } else {
            return super.getJs(data);
        }
    }
    
    @Override
    public String getMetas(Map<String, Object> data) {
        if (isAjaxContent(data)) {
            return "";
        } else {
            return super.getMetas(data);
        }
    }
    
    @Override
    public String getHead(Map<String, Object> data) {
        if (isAjaxContent(data)) {
            return "";
        } else {
            return super.getHead(data);
        }
    }
    
    @Override
    public String getFavIconLink(Map<String, Object> data) {
        if (isAjaxContent(data)) {
            return "";
        } else {
            return super.getFavIconLink(data);
        }
    }
    
    @Override
    public String getLoginForm(Map<String, Object> data) {
        String name = "<div class=\"login_form_brand\">";
        if (!getPropertyString("logo").isEmpty()) {
            name += "<div class=\"login_form_logo_container\"><img class=\"logo\" alt=\"logo\" src=\""+getPropertyString("logo")+"\" /></div>";
        } else {
            name += "<div class=\"login_form_logo_container\"><i class=\"fas fa-user-circle\"></i></div>";
        }
        name += "<h1>" + ResourceBundleUtil.getMessage("console.login.label.loginTo") + " " + userview.getPropertyString("name") + "</h1></div>";
        data.put("login_form_inner_before", name);
        return super.getLoginForm(data);
    }
    
    protected boolean isAjaxContent(Map<String, Object> data) {
        if (isAjaxContent == null) {
            isAjaxContent = "true".equalsIgnoreCase(WorkflowUtil.getHttpServletRequest().getHeader("__ajax_theme_loading"));
        }
        return isAjaxContent;
    }
    
    @Override
    protected String getPrimaryColor() {
        if (!getPropertyString("dx8headerColor").isEmpty()) {
            return getPropertyString("dx8headerColor");
        }
        return "#FFFFFF";
    }
    
    @Override
    protected String generateLessCss() {
        String css = ":root{";
        if (!getPropertyString("dx8colorScheme").isEmpty()) {
            String[] colors = getPropertyString("dx8colorScheme").split(";");
            for (int i=0; i < colors.length; i++) {
                if (!colors[i].isEmpty()) {
                    css += "--theme-color"+(i+1)+":"+colors[i]+ ";";
                }
            }
        }
        if (!getPropertyString("dx8backgroundImage").isEmpty()) {
            css += "--theme-background-image:url('"+getPropertyString("dx8backgroundImage")+ "');";
        }
        if (!getPropertyString("dx8background").isEmpty()) {
            css += "--theme-background:"+getPropertyString("dx8background")+ ";";
        }
        if (!getPropertyString("dx8contentbackground").isEmpty()) {
            css += "--theme-content-background:"+getPropertyString("dx8contentbackground")+ ";";
        }
        if (!getPropertyString("dx8headerColor").isEmpty()) {
            css += "--theme-header:"+getPropertyString("dx8headerColor")+ ";";
        }
        if (!getPropertyString("dx8headerFontColor").isEmpty()) {
            css += "--theme-header-font:"+getPropertyString("dx8headerFontColor")+ ";";
        }
        if (!getPropertyString("dx8navBackground").isEmpty()) {
            css += "--theme-sidebar:"+getPropertyString("dx8navBackground")+ ";";
        }
        if (!getPropertyString("dx8navLinkBackground").isEmpty()) {
            css += "--theme-sidebar-link-bg:"+getPropertyString("dx8navLinkBackground")+ ";";
        }
        if (!getPropertyString("dx8navLinkColor").isEmpty()) {
            css += "--theme-sidebar-link:"+getPropertyString("dx8navLinkColor")+ ";";
        }
        if (!getPropertyString("dx8navLinkIcon").isEmpty()) {
            css += "--theme-sidebar-icon:"+getPropertyString("dx8navLinkIcon")+ ";";
        }
        if (!getPropertyString("dx8navBadge").isEmpty()) {
            css += "--theme-sidebar-badge:"+getPropertyString("dx8navBadge")+";";
        }
        if (!getPropertyString("dx8navBadgeText").isEmpty()) {
            css += "--theme-sidebar-badge-text:"+getPropertyString("dx8navBadgeText")+";";
        }
        if (!getPropertyString("dx8navActiveLinkBackground").isEmpty()) {
            css += "--theme-sidebar-active-link-bg:"+getPropertyString("dx8navActiveLinkBackground")+ ";";
        }
        if (!getPropertyString("dx8navActiveLinkColor").isEmpty()) {
            css += "--theme-sidebar-active-link:"+getPropertyString("dx8navActiveLinkColor")+ ";";
        }
        if (!getPropertyString("dx8navActiveIconColor").isEmpty()) {
            css += "--theme-sidebar-active-icon:"+getPropertyString("dx8navActiveIconColor")+ ";";
        }
        if (!getPropertyString("dx8navScrollbarThumb").isEmpty()) {
            css += "--theme-nav-scrollbar-thumb:"+getPropertyString("dx8navScrollbarThumb")+ ";";
        }
        if (!getPropertyString("dx8buttonBackground").isEmpty()) {
            css += "--theme-button-bg:"+getPropertyString("dx8buttonBackground")+ ";";
        }
        if (!getPropertyString("dx8buttonColor").isEmpty()) {
            css += "--theme-button:"+getPropertyString("dx8buttonColor")+ ";";
        }
        if (!getPropertyString("dx8primaryColor").isEmpty()) {
            css += "--theme-primary:"+getPropertyString("dx8primaryColor")+ ";";
        }
        if (!getPropertyString("dx8headingBgColor").isEmpty()) {
            css += "--theme-heading-bg:"+getPropertyString("dx8headingBgColor")+ ";";
        }
        if (!getPropertyString("dx8headingFontColor").isEmpty()) {
            css += "--theme-heading-color:"+getPropertyString("dx8headingFontColor")+ ";";
        }
        if (!getPropertyString("dx8fontColor").isEmpty()) {
            css += "--theme-font-color:"+getPropertyString("dx8fontColor")+ ";";
        }
        if (!getPropertyString("dx8contentFontColor").isEmpty()) {
            css += "--theme-content-color:"+getPropertyString("dx8contentFontColor")+ ";";
        }
        if (!getPropertyString("dx8footerBackground").isEmpty()) {
            css += "--theme-footer-bg:"+getPropertyString("dx8footerBackground")+ ";";
        }
        if (!getPropertyString("dx8footerColor").isEmpty()) {
            css += "--theme-footer:"+getPropertyString("dx8footerColor")+ ";";
        }
        if (!getPropertyString("dx8linkColor").isEmpty()) {
            css += "--theme-link:"+getPropertyString("dx8linkColor")+ ";";
        }
        if (!getPropertyString("dx8linkActiveColor").isEmpty()) {
            css += "--theme-link-active:"+getPropertyString("dx8linkActiveColor")+ ";";
        }
        css += "}";
        return css;        
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/ajaxUniversalTheme.json", null, true, null);
    }
    
    @Override
    public String builderThemeCss() {
        return generateLessCss();
    }
    
    protected String getContentPlaceholderRules() {
        Map<String, String> rules = getUserview().getContentPlaceholderRules();
        String script = "";
        String value = "";
        
        if (!rules.isEmpty())  {
            for (String url : rules.keySet()) {
                if (!script.isEmpty()) {
                    script += ", ";
                }
                script += "\"" + url + "\" : ";
                value = rules.get(url);
                if (value.startsWith("{") && value.endsWith("}")) {
                    script += value;
                } else {
                    script += "\"" + value + "\"";
                }
            }
            script = "var ajaxContentPlaceholder = {" + script + "};";
        }
        return script;
    }
    
    protected boolean isCurrentUserviewUrl(String url) {
        return url.contains("/web/userview/"+userview.getParamString("appId")+"/"+userview.getParamString("userviewId"));
    }
    
    @Override
    public String getServiceWorkerTemplate(String appId, String userviewId, String userviewKey) {
        // read template from cache
        String key = "serviceWorkerTemplate:"+appId+":"+userviewId+":"+userviewKey;
        String html = "";
        Cache cache = (Cache) AppUtil.getApplicationContext().getBean("cssCache");
        if (cache != null) {
            Element element = cache.get(key);
            if (element != null) {
                html = (String) element.getObjectValue();
            }
        }
        if (html == null || html.isEmpty()) {
            // not available in cache
            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        
            UserviewThemeProcesser processer = new UserviewThemeProcesser(userview, request);
            processer.init();
            
            userview.getParams().put("isTemplate", "true");

            Map<String, Object> data = new HashMap<String, Object>();
            data.put("offlineTemplate", true);
            data.put("processor", processer);
            data.put("params", userview.getParams());
            data.put("userview", userview);
            data.put("appId", userview.getParamString("appId"));
            data.put("is_login_page", false);
            data.put("context_path", request.getContextPath());
            data.put("build_number", ResourceBundleUtil.getMessage("build.number"));
            String locale = AppUtil.getAppLocale();
            String language = AppUtil.getAppLanguage();
            data.put("locale", locale);
            data.put("language", language);
            data.put("embed", "true".equalsIgnoreCase(userview.getParamString("embed")));
            data.put("body_id", "offline");
            data.put("body_classes", processer.getBodyClasses(locale));
            data.put("base_link", "/web/userview/" + appId + "/" + userviewId + "/" + userviewKey + "/");
            data.put("home_page_link", "/web/userview/" + appId + "/" + userviewId + "/" + userviewKey + "/index");
            data.put("title", "{{TEMPLATE_TITLE}}");
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
                data.put("login_link", "/web/ulogin/" + appId + "/" + userviewId + "/" + userviewKey + "/index");
            }

            data.put("content", "{{TEMPLATE_CONTENT}}");
            data.put("metas", getMetas(data));
            data.put("joget_header", processer.getJogetHeader());
            data.put("js_css_lib", getJsCssLib(data));
            data.put("fav_icon_link", getFavIconLink(data));
            data.put("js", getJs(data));
            data.put("css", getCss(data));
            data.put("head", getHead(data));
            data.put("categories_container_inner_after", "{{TEMPLATE_MENUS}}");
            data.put("menus", getMenus(data));
            data.put("header", getHeader(data));
            data.put("footer", getFooter(data));
            data.put("joget_footer", processer.getJogetFooter());
            data.put("content_container", getContentContainer(data));

            html = getLayout(data);
            html = html.replaceAll("<textarea id=\\\"analyzerJson\\\".+</textarea>", ""); //remove analyzer to prevent serviceworker change
            html = StringUtil.escapeString(html, StringUtil.TYPE_JAVASCIPT, null);
            
            if (cache != null) {
                Element element = new Element(key, html);
                cache.put(element);
            }
        }
        return html;
    }
    
    /**
     * To remove Breadcrumb from offline template
     * 
     * @param data
     * @return 
     */
    @Override
    protected String getBreadcrumb(Map<String, Object> data) {
        if (data.containsKey("offlineTemplate")) {
            return "";
        } else {
            return super.getBreadcrumb(data);
        }
    }
    
    @Override
    protected String getNavbar(Map<String, Object> data) {
        String html = "<div class=\"nav-no-collapse header-nav\"><ul class=\"nav pull-right\">\n";
        html += getHomeLink(data);
        if ((Boolean) data.get("is_logged_in")) {
            html += getInbox(data);
        }
        html += getShortcut(data);
        if ("true".equals(getPropertyString("darkMode"))) {
            html += getThemeSwitch(data);
        }
        if ("true".equals(getPropertyString("compactTheme"))) {
            html += getCompactThemeSwitch(data);
        }
        html += getUserMenu(data);
        html += "</ul></div>\n";
        return html;
    }
    
    protected String getThemeSwitch(Map<String, Object> data) {
        return "<li class=\"theme-selection dropdown\">\n"
                + "    <a data-toggle=\"dropdown\" href=\"javascript:;\" class=\"btn dropdown-toggle\">\n"
                + "	 <i class=\"zmdi zmdi-brightness-6\"></i>\n"
                + "    </a>\n"
                + "    <ul id=\"theme-selector\" class=\"dropdown-menu themes\">\n"
                + "        <div id=\"dropdown-title\"><span class=\"header\">" + ResourceBundleUtil.getMessage("theme.ajaxUniversalTheme.darkTheme.appearance") + "</span></div>\n"
                + "        <li data-value=\"light\"><span class=\"header\">" + ResourceBundleUtil.getMessage("theme.ajaxUniversalTheme.darkTheme.lightTheme") + "</span></li>\n"
                + "        <li data-value=\"dark\"><span class=\"header\">" + ResourceBundleUtil.getMessage("theme.ajaxUniversalTheme.darkTheme.darkTheme") + "</span></li>\n"
                + "        <li data-value=\"auto\"><span class=\"header\">" + ResourceBundleUtil.getMessage("theme.ajaxUniversalTheme.darkTheme.deviceTheme") + "</span></li>\n"
                + "    </ul>\n"
                + "<li>";
    }
    
    protected String getCompactThemeSwitch(Map<String, Object> data) {
        return "<li class=\"density-selection dropdown\">\n"
                + "    <a data-toggle=\"dropdown\" href=\"javascript:;\" class=\"btn dropdown-toggle\">\n"
                + "	 <i class=\"fas fa-compress\"></i>\n"
                + "    </a>\n"
                + "    <ul id=\"density-selector\" class=\"dropdown-menu themes\">\n"
                + "        <div id=\"dropdown-title\"><span class=\"header\">" + ResourceBundleUtil.getMessage("theme.ajaxUniversalTheme.compactTheme.density") + "</span></div>\n"
                + "        <li data-value=\"normal\"><span class=\"header\">" + ResourceBundleUtil.getMessage("theme.ajaxUniversalTheme.compactTheme.normalmode") + "</span></li>\n"
                + "        <li data-value=\"compact\"><span class=\"header\">" + ResourceBundleUtil.getMessage("theme.ajaxUniversalTheme.compactTheme.compactmode") + "</span></li>\n"
                + "    </ul>\n"
                + "<li>";
    }
    
    
}
