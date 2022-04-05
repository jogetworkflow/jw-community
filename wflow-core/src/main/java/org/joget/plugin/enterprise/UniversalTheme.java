package org.joget.plugin.enterprise;

import com.asual.lesscss.LessEngine;
import de.bripkens.gravatar.DefaultImage;
import de.bripkens.gravatar.Gravatar;
import de.bripkens.gravatar.Rating;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.MobileUtil;
import org.joget.apps.userview.lib.InboxMenu;
import org.joget.apps.userview.model.UserviewCategory;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.model.UserviewV5Theme;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.commons.util.TimeZoneUtil;
import org.joget.directory.model.User;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.json.JSONObject;

public class UniversalTheme extends UserviewV5Theme implements PluginWebSupport {
    protected final static String PROFILE = "_ja_profile"; 
    protected final static String INBOX = "_ja_inbox"; 
    protected static LessEngine lessEngine = new LessEngine();
    
    public enum Color {
        RED("#F44336", "#D32F2F", ""),
        PINK("#E91E63", "#C2185B", ""),
        PURPLE("#9C27B0", "#7B1FA2", ""),
        DEEP_PURPLE("#673AB7", "#512DA8", ""),
        INDIGO("#3F51B5", "#303F9F", ""),
        BLUE("#2196F3", "#1976D2", ""),
        LIGHT_BLUE("#03A9F4", "#0288D1", ""),
        CYAN("#00BCD4", "#0097A7", ""),
        TEAL("#009688", "#00796B", ""),
        GREEN("#4CAF50", "#388E3C", ""),
        LIGHT_GREEN("#8BC34A", "#689F38", ""),
        LIME("#CDDC39", "#AFB42B", ""),
        YELLOW("#FFEB3B", "#FBC02D", ""),
        AMBER("#FFC107", "#FFA000", ""),
        ORANGE("#FF9800", "#F57C00", ""),
        DEEP_ORANGE("#FF5722", "#E64A19", ""),
        BROWN("#795548", "#795548", ""),
        GREY("#9E9E9E", "#616161", ""),
        BLUE_GREY("#607D8B", "#455A64", ""),
        DEEP_GREY("#2B343A", "#1E262B", "#222c32"),
        LAVENDERBLUSH("#FFF0F5", "", ""),
        THISTLE("#D8BFD8", "", ""),
        PLUM("#DDA0DD", "", ""),
        LAVENDER("#E6E6FA", "", ""),
        GHOSTWHITE("#F8F8FF", "", ""),
        DARKROYALBLUE("#3b5998", "", ""),
        ROYALBLUE("#4169E1", "", ""),
        CORNFLOWERBLUE("#6495ED", "", ""),
        ALICEBLUE("#F0F8FF", "", ""),
        LIGHTSTEELBLUE("#B0C4DE", "", ""),
        STEELBLUE("#4682B4", "", ""),
        LIGHTSKYBLUE("#87CEFA", "", ""),
        SKYBLUE("#87CEEB", "", ""),
        DEEPSKYBLUE("#00BFFF", "", ""),
        AZURE("#F0FFFF", "", ""),
        LIGHTCYAN("#E1FFFF", "", ""),
        IVORY("#FFFFF0", "", ""),
        LEMONCHIFFON("#FFFACD", "", ""),
        WHEAT("#F5DEB3", "", ""),
        LIGHTGREY("#D3D3D3", "", ""),
        SILVER("#C0C0C0", "", ""),
        BLACK("#000000", "#222222", ""),
        WHITE("#FFFFFF", "", "#DDDDDD");
        
        private final String color;  
        private final String dark; 
        private final String light;
        Color(String color, String dark, String light) {
            this.color = color;
            this.dark = dark;
            this.light = light;
        }
    }
    
    @Override
    public String getName() {
        return "V6 Universal Theme";
    }

    @Override
    public String getVersion() {
        return "6.0.0";
    }

    @Override
    public String getDescription() {
        return "A universal responsive Userview Theme based on Material Design";
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
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/universalTheme.json", null, true, null);
    }
    
    @Override
    public String getMetas(Map<String, Object> data) {

        String meta = super.getMetas(data) + "\n";
        if ((Boolean) data.get("is_login_page")) {
            meta += "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\">\n";
        } else {
            meta += "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n";
        }
        meta += "<meta name=\"msapplication-tap-highlight\" content=\"no\"/>\n";
        return meta;
    }
    
    @Override
    public String getCss(Map<String, Object> data) {
        String css = getPropertyString("css");
        if ("true".equals(getPropertyString("removeAssignmentTitle"))) {
            css += "\nbody .viewForm-body-header, body .runProcess-body-header {display:none;}";
        }
        return css;
    }
    
    @Override
    public String getJs(Map<String, Object> data) {
        return getPropertyString("js");
    }
    
    @Override
    public String getJsCssLib(Map<String, Object> data) {
        String path = data.get("context_path") + "/universal";

        String jsCssLink = "";
        jsCssLink += "<link href=\"" + data.get("context_path") + "/wro/universal.preload.min.css" + "\" rel=\"stylesheet\" />\n";
        jsCssLink += "<script>loadCSS(\"" + data.get("context_path") + "/wro/universal.min.css" + "\")</script>\n";
        
        jsCssLink += "<style>" + generateLessCss() + "</style>";

        jsCssLink += "<script src=\"" + data.get("context_path") + "/wro/universal.preload.min.js\"></script>\n";
        jsCssLink += "<script src=\"" + data.get("context_path") + "/wro/universal.min.js\" async></script>\n";
        
        if (enableResponsiveSwitch()) {
            jsCssLink += "<script src=\"" + path + "/lib/responsive-switch.min.js\" defer></script>\n";
        }        
        jsCssLink += "<script>var _enableResponsiveTable = true;</script>\n";
        
        return jsCssLink;
    }

    protected String generateLessCss() {
        String css = "";
        String lessVariables = "";
        String primary = "";
        String dark = "darken(@primary , 10%)";
        String light = "lighten(@primary , 5%)";
        String accent = "";
        String lightAccent = "lighten(@accent , 10%)";
        String button = "#D8DADA";
        String buttonText = "#333";
        String font = "";
        
        if ("custom".equals(getPropertyString("primaryColor"))) {
            primary = getPropertyString("customPrimary");
            if (!getPropertyString("customPrimaryDark").isEmpty()) {
                dark = getPropertyString("customPrimaryDark");
            }
            if (!getPropertyString("customPrimaryLight").isEmpty()) {
                light = getPropertyString("customPrimaryLight");
            }
        } else {
            Color p = Color.valueOf(getPropertyString("primaryColor")); 
            if (p != null) {
                primary = p.color;
                dark = (p.dark.isEmpty())?dark:p.dark;
                if ("light".equals(getPropertyString("themeScheme"))) {
                    light = "screen(@primary, #eeeeee)";
                } else {
                    light = (p.light.isEmpty())?light:p.light;
                }
            }
        }
        
        if ("custom".equals(getPropertyString("accentColor"))) {
            accent = getPropertyString("customAccent");
            if (!getPropertyString("customAccentLight").isEmpty()) {
                lightAccent = getPropertyString("customAccentLight");
            }
        } else {
            Color a = Color.valueOf(getPropertyString("accentColor")); 
            if (a != null) {
                accent = a.color;
                lightAccent = (a.light.isEmpty())?lightAccent:a.light;
            }
        }
        
        if ("custom".equals(getPropertyString("buttonColor"))) {
            button = getPropertyString("customButton");
        } else if (!getPropertyString("buttonColor").isEmpty()) {
            Color a = Color.valueOf(getPropertyString("buttonColor")); 
            if (a != null) {
                button = a.color;
            }
        }
        
        if ("custom".equals(getPropertyString("buttonTextColor"))) {
            buttonText = getPropertyString("customButtonText");
        } else if (!getPropertyString("buttonColor").isEmpty()) {
            Color a = Color.valueOf(getPropertyString("buttonTextColor")); 
            if (a != null) {
                buttonText = a.color;
            }
        }
        
        if ("custom".equals(getPropertyString("fontColor"))) {
            font = getPropertyString("customFontColor");
        } else {
            Color a = Color.valueOf(getPropertyString("fontColor")); 
            if (a != null) {
                font = a.color;
            }
        }
        
        if ("light".equals(getPropertyString("themeScheme"))) {
            String menuFont = "#000000";
            if ("custom".equals(getPropertyString("menuFontColor"))) {
                menuFont = getPropertyString("customMenuFontColor");
            } else if (!getPropertyString("menuFontColor").isEmpty()) {
                Color a = Color.valueOf(getPropertyString("menuFontColor")); 
                if (a != null) {
                    menuFont = a.color;
                }
            }
            
            lessVariables += "@primary: " + primary + "; @darkPrimary: " + dark + "; @lightPrimary: " + light + "; @accent: " + accent + "; @lightAccent: " + lightAccent + "; @menuFont: " + menuFont + "; @button: " + button + "; @buttonText: " + buttonText + "; @defaultFontColor : " + font + ";";
        } else {
            lessVariables += "@primary: " + primary + "; @darkPrimary: " + dark + "; @lightPrimary: " + light + "; @accent: " + accent + "; @lightAccent: " + lightAccent + "; @button: " + button + "; @buttonText: " + buttonText + "; @defaultFontColor : " + font + ";";
        }
        
        // process LESS
        String less = AppUtil.readPluginResource(getClass().getName(), "resources/themes/universal/" + getPropertyString("themeScheme") + ".less");
        less = lessVariables + "\n" + less;
        // read CSS from cache
        Cache cache = (Cache) AppUtil.getApplicationContext().getBean("cssCache");
        if (cache != null) {
            Element element = cache.get(less);
            if (element != null) {
                css = (String) element.getObjectValue();
            }
        }
        if (css == null || css.isEmpty()) {
            // not available in cache, compile LESS
            css = compileLess(less);
            // store CSS in cache
            if (cache != null) {
                Element element = new Element(less, css);
                cache.put(element);
            }
        }
        return css;
    }

    protected String compileLess(String less) {
        String css = "";
        try {
            css = lessEngine.compile(less);
        } catch(Exception e) {
            LogUtil.error(this.getClass().getName(), e, "Error compiling LESS");
            LogUtil.debug(this.getClass().getName(), "LESS: " + less);
        }
        return css;
    }
    
    @Override
    public String getHeader(Map<String, Object> data) {
        if ("true".equals(getPropertyString("horizontal_menu"))) {
            data.put("header_after", UserviewUtil.getTemplate(this, data, "/templates/universalTheme_horizontalMenu.ftl"));
        } else if ("horizontal_inline".equals(getPropertyString("horizontal_menu"))) {
            data.put("header_name_inner_after", UserviewUtil.getTemplate(this, data, "/templates/universalTheme_horizontalMenu.ftl"));
        }
        
        data.put("header_classes", "navbar");
        data.put("header_inner_before", "<div class=\"navbar-inner\"><div class=\"container-fluid\"><div class=\"hi-trigger ma-trigger\" id=\"sidebar-trigger\"><div class=\"line-wrap\"><div class=\"line top\"></div><div class=\"line center\"></div><div class=\"line bottom\"></div></div></div>");
        data.put("header_inner_after", "</div></div>" + getPropertyString("subheader"));
        data.put("header_link_classes", "brand");
        data.put("header_info_classes", "inline-block");
        data.put("header_name_classes", "inline-block");
        
        if (!getPropertyString("logo").isEmpty()) {
            data.put("header_name_inner_before", "<img class=\"logo\" src=\""+getPropertyString("logo")+"\" />");
        }
        
        data.put("header_description_classes", "inline-block visible-desktop");
        data.put("header_description_span_classes", "brand");
        data.put("header_message_after", getNavbar(data));
        return UserviewUtil.getTemplate(this, data, "/templates/userview/header.ftl");
    }
    
    @Override
    public String getContentContainer(Map<String, Object> data) {
        if (!getPropertyString("horizontal_menu").isEmpty()) {
            data.put("hide_nav", true);
        }
        
        if ((getRequestParameter("menuId") == null || getUserview().getPropertyString("homeMenuId").equals(getRequestParameter("menuId"))) && !getPropertyString("homeAttractBanner").isEmpty()) {
            data.put("main_container_before", "<div class=\"home_banner\"><div class=\"home_banner_inner\">"+getPropertyString("homeAttractBanner")+"</div></div>");
        }
        
        data.put("main_container_classes", "container-fluid-full");
        data.put("main_container_inner_classes", "row-fluid");
        data.put("sidebar_classes", "span2");
        if (((Boolean) data.get("embed")) || ((Boolean) data.get("hide_nav"))) {
            data.put("content_classes", "span12");
        } else {
            data.put("content_classes", "span10");
        }

        data.put("content_inner_before", getBreadcrumb(data));
        return super.getContentContainer(data);
    }
    
    @Override
    public String getLayout(Map<String, Object> data) {
        if ("true".equals(getPropertyString("horizontal_menu"))) {
            data.put("body_classes", data.get("body_classes").toString() + " horizontal_menu");
        } else if ("horizontal_inline".equals(getPropertyString("horizontal_menu"))){
            data.put("body_classes", data.get("body_classes").toString() + " horizontal_menu inline_menu");
        } else if ("no".equals(getPropertyString("horizontal_menu"))) {
            data.put("body_classes", data.get("body_classes").toString() + " horizontal_menu no_menu");
        }
        
        if ((getRequestParameter("menuId") == null || getUserview().getPropertyString("homeMenuId").equals(getRequestParameter("menuId"))) && !getPropertyString("homeAttractBanner").isEmpty()) {
            data.put("body_classes", data.get("body_classes").toString() + " has_home_banner");
        }
        
        data.put("body_inner_before", "<div class=\"page-loader\"><div class=\"preloader pl-xl\" style=\"width:80px\"><svg class=\"pl-circular\" viewBox=\"25 25 50 50\"><circle class=\"plc-path\" cx=\"50\" cy=\"50\" r=\"20\" /></svg></div></div>");
        return UserviewUtil.getTemplate(this, data, "/templates/userview/layout.ftl");
    }
    
    public String getLoginForm(Map<String, Object> data) {
        data.put("hide_nav", true);
        return super.getLoginForm(data);
    }
    
    @Override
    public String handlePageNotFound(Map<String, Object> data) {
        if (PROFILE.equals(userview.getParamString("menuId"))) {
            return pageProfile(data);
        } else if (INBOX.equals(userview.getParamString("menuId"))) {
            return pageInbox(data);
        } else {
            return super.handlePageNotFound(data);
        }
    }
    
    protected String getUserMenu(Map<String, Object> data) {
        if (!getPropertyString("horizontal_menu").isEmpty()) {
            String html = "";
            if ((Boolean) data.get("is_logged_in")) {
                User user = (User) data.get("user");
                String email = user.getEmail();
                if (email == null) {
                    email = "";
                }
                if (email.contains(";")) {
                    email = email.split(";")[0];
                }
                if (email.contains(",")) {
                    email = email.split(",")[0];
                }

                String profileImageTag = "";
                if (getPropertyString("userImage").isEmpty()) {
                    String url = (email != null && !email.isEmpty()) ? 
                        new Gravatar()
                            .setSize(20)
                            .setHttps(true)
                            .setRating(Rating.PARENTAL_GUIDANCE_SUGGESTED)
                            .setStandardDefaultImage(DefaultImage.IDENTICON)
                            .getUrl(email)
                        : "//www.gravatar.com/avatar/default?d=identicon";
                    profileImageTag = "<img class=\"gravatar\" alt=\"gravatar\" width=\"30\" height=\"30\" src=\""+url+"\" /> ";
                } else if ("hashVariable".equals(getPropertyString("userImage"))) {
                    String url = AppUtil.processHashVariable(getPropertyString("userImageUrlHash"), null, StringUtil.TYPE_HTML, null, AppUtil.getCurrentAppDefinition());
                    if (AppUtil.containsHashVariable(url) || url == null || url.isEmpty()) {
                        url = data.get("context_path") + "/universal/user.png";
                    }
                    profileImageTag = "<img src=\""+url+"\" /> ";
                }
                
                html += "<li class=\"user-link dropdown\">\n"
                      + "    <a data-toggle=\"dropdown\" class=\"btn dropdown-toggle\">\n"
                      + "	     " + profileImageTag + StringUtil.stripHtmlTag(user.getFirstName(), new String[]{}) + " " + StringUtil.stripHtmlTag(user.getLastName(), new String[]{}) + "\n"
                      + "	     <span class=\"caret\"></span>\n"
                      + "    </a>\n";

                html += "<ul class=\"dropdown-menu\">\n";
                if (!"true".equals(getPropertyString("profile")) && !user.getReadonly()) {
                    html += "    <li><a href=\"" + data.get("base_link") + PROFILE +"\"><i class=\"fa fa-user\"></i> " + ResourceBundleUtil.getMessage("theme.universal.profile") + "</a></li>\n";
                }
                
                Object[] shortcut = (Object[]) getProperty("userMenu");
                if (shortcut != null && shortcut.length > 0) {
                    for (Object o : shortcut) {
                        Map link = (HashMap) o;
                        String href = link.get("href").toString();
                        String label = link.get("label").toString();
                        String target = (link.get("target") == null)?"":link.get("target").toString();

                        if ("divider".equalsIgnoreCase(label)) {
                            html += "<li class=\"divider\"></li>\n";
                        } else if (href.isEmpty()) {
                             html += "<li class=\"dropdown-menu-title\"><span>" + label + "</span></li>\n";
                        } else {
                            if (!href.contains("/")) {
                                href = data.get("base_link") + href;
                            }
                            html += "<li><a href=\"" + href + "\" target=\""+target+"\">" + label + "</a></li>\n";
                        }
                    }
                }
                
                html += "    <li><a href=\"" + data.get("logout_link") + "\"><i class=\"fa fa-power-off\"></i> " + ResourceBundleUtil.getMessage("theme.universal.logout") + "</a></li>\n"
                      + "</ul>";

            } else {
                html += "<li class=\"user-link\">\n"
                      + "    <a href=\"" + data.get("login_link") + "\" class=\"btn\">\n"
                      + "	     <i class=\"fa fa-user white\"></i> " + ResourceBundleUtil.getMessage("ubuilder.login") + "\n"
                      + "    </a>\n";
            }
            html += "</li>";
            return html;
        } else {
            return "";
        }
    }
    
    protected String getSidebarUserMenu(Map<String, Object> data) {
        String html = "<ul class=\"user-menu nav nav-tabs nav-stacked main-menu\">";
        if ((Boolean) data.get("is_logged_in")) {
            User user = (User) data.get("user");
            String email = user.getEmail();
            if (email == null) {
                email = "";
            }
            if (email.contains(";")) {
                email = email.split(";")[0];
            }
            if (email.contains(",")) {
                email = email.split(",")[0];
            }
            
            String profileImageTag = "";
            if (getPropertyString("userImage").isEmpty()) {
                String url = (email != null && !email.isEmpty()) ? 
                    new Gravatar()
                        .setSize(20)
                        .setHttps(true)
                        .setRating(Rating.PARENTAL_GUIDANCE_SUGGESTED)
                        .setStandardDefaultImage(DefaultImage.IDENTICON)
                        .getUrl(email)
                    : "//www.gravatar.com/avatar/default?d=identicon";
                profileImageTag = "<img class=\"gravatar\" alt=\"gravatar\" width=\"30\" height=\"30\" src=\""+url+"\" /> ";
            } else if ("hashVariable".equals(getPropertyString("userImage"))) {
                String url = AppUtil.processHashVariable(getPropertyString("userImageUrlHash"), null, StringUtil.TYPE_HTML, null, AppUtil.getCurrentAppDefinition());
                if (AppUtil.containsHashVariable(url) || url == null || url.isEmpty()) {
                    url = data.get("context_path") + "/universal/user.png";
                }
                profileImageTag = "<img src=\""+url+"\" /> ";
            }
            
            html += "<li class=\"mm-profile user-link\">\n"
                  + "    <a class=\"dropdown\">\n"
                  + "        "+profileImageTag+"\n"  
                  + "	     <span>" + StringUtil.stripHtmlTag(user.getFirstName(), new String[]{}) + " " + StringUtil.stripHtmlTag(user.getLastName(), new String[]{}) + "</span>\n"
                  + "	     <small>" + email + "</small>\n"
                  + "    </a>\n";
            
            html += "<ul>\n";
            if (!"true".equals(getPropertyString("profile")) && !user.getReadonly()) {
                String activeCss = "";
                if (PROFILE.equals(userview.getParamString("menuId"))) {
                    activeCss = " class=\"active\"";
                }
                html += "    <li "+activeCss+"><a href=\"" + data.get("base_link") + PROFILE +"\"><span><i class=\"fa fa-user\"></i> " + ResourceBundleUtil.getMessage("theme.universal.profile") + "</span></a></li>\n";
            }
            
            Object[] shortcut = (Object[]) getProperty("userMenu");
            if (shortcut != null && shortcut.length > 0) {
                for (Object o : shortcut) {
                    Map link = (HashMap) o;
                    String href = link.get("href").toString();
                    String label = link.get("label").toString();
                    String target = (link.get("target") == null)?"":link.get("target").toString();

                    if ("divider".equalsIgnoreCase(label)) {
                        html += "<li class=\"divider\"></li>\n";
                    } else if (href.isEmpty()) {
                         html += "<li class=\"dropdown-menu-title\"><span>" + label + "</span></li>\n";
                    } else {
                        if (!href.contains("/")) {
                            href = data.get("base_link") + href;
                        }
                        html += "<li><a href=\"" + href + "\" target=\""+target+"\">" + label + "</a></li>\n";
                    }
                }
            }
            
            html += "    <li><a href=\"" + data.get("logout_link") + "\"><span><i class=\"fa fa-power-off\"></i> " + ResourceBundleUtil.getMessage("theme.universal.logout") + "</span></a></li>\n";
            html += "</ul>";

        } else {
            String profileImageTag = "";
            if (getPropertyString("userImage").isEmpty() || "hashVariable".equals(getPropertyString("userImage"))) {
                String url = data.get("context_path") + "/universal/user.png";
                profileImageTag = "<img src=\""+url+"\" /> ";
            }
            
            html += "<li class=\"mm-profile user-link\">\n"
                  + "    <a href=\"" + data.get("login_link") + "\" >\n"
                  + "        "+profileImageTag+"\n" 
                  + "	     <span>Visitor</span>\n"  
                  + "	     <small class=\"login_link\">" + ResourceBundleUtil.getMessage("ubuilder.login") + "</small>\n"
                  + "    </a>\n";
        }
        html += "</li></ul>";
        return html;
    }
    
    @Override
    public String getMenus(Map<String, Object> data) {
        data.put("combine_single_menu_category", true);
        data.put("categories_container_before", getSidebarUserMenu(data));
        
        return super.getMenus(data);
    }
    
    @Override
    public String getFooter(Map<String, Object> data) {
        if (enableResponsiveSwitch()) {
            data.put("footer_inner_after", "<div id=\"responsiveSwitch\"><p><a href=\"#\" class=\"rs-link\" data-link-desktop=\""+ ResourceBundleUtil.getMessage("theme.universal.switchDesktop") +"\" data-link-responsive=\""+ ResourceBundleUtil.getMessage("theme.universal.switchMobile") +"\"></a></p></div>" + getPropertyString("subfooter"));
        } else {
            data.put("footer_inner_after", getPropertyString("subfooter"));
        }
        return super.getFooter(data);
    }
    
    protected String getHomeLink(Map<String, Object> data) {
        return "<li class=\"\"><a class=\"btn\" href=\"" + data.get("home_page_link") + "\"><i class=\"fa fa-home\"></i></a></li>\n";
    }
    
    protected String getNavbar(Map<String, Object> data) {
        String html = "<div class=\"nav-no-collapse header-nav\"><ul class=\"nav pull-right\">\n";
        html += getHomeLink(data);
        if ((Boolean) data.get("is_logged_in")) {
            html += getInbox(data);
        }
        html += getShortcut(data);
        html += getUserMenu(data);
        html += "</ul></div>\n";
        return html;
    }
    
    protected String getInbox(Map<String, Object> data) {
        String html = "";
        
        if (!getPropertyString("inbox").isEmpty()) {
            String url = data.get("context_path") + "/web/json/plugin/org.joget.plugin.enterprise.UniversalTheme/service?_a=getAssignment";
            if ("current".equals(getPropertyString("inbox"))) {
                try {
                    url += "&appId=" + URLEncoder.encode(userview.getParamString("appId"), "UTF-8");
                } catch (UnsupportedEncodingException e){}
            }
            html += "<li class=\"inbox-notification dropdown\" data-url=\"" + url + "\">\n"
                  + "    <a data-toggle=\"dropdown\" class=\"btn dropdown-toggle\">\n"
                  + "	 <i class=\"fa fa-tasks white\"></i><span class=\"badge red\">0</span>\n"
                  + "    </a>\n"
                  + "    <ul class=\"dropdown-menu notifications\">\n"
                  + "        <li class=\"dropdown-menu-title\"><span>" + ResourceBundleUtil.getMessage("theme.universal.inboxTaskMessage") + "</span><a href=\"#\" class=\"refresh\"><i class=\"fa fa-refresh\"></i></a></li>"
                  + "        <li class=\"loading\"><a><span><i class=\"fa fa-spinner fa-spin fa-3x\"></i></span></a></li>\n"
                  + "        <li><a href=\"" + data.get("base_link") + INBOX + "\" class=\"dropdown-menu-sub-footer\">" + ResourceBundleUtil.getMessage("theme.universal.viewAllTask") + "</a></li>\n"  
                  + "    </ul>\n"
                  + "<li>";
        }
        
        return html;
    }
    
    protected String getShortcut(Map<String, Object> data) {
        String shortcutHtml = "";
        
        Object[] shortcut = (Object[]) getProperty("shortcut");
        if (shortcut != null && shortcut.length > 0) {
            for (Object o : shortcut) {
                Map link = (HashMap) o;
                String href = link.get("href").toString();
                String label = link.get("label").toString();
                String target = (link.get("target") == null)?"":link.get("target").toString();
                boolean isPublic = "true".equalsIgnoreCase((String) link.get("isPublic"));
                
                if ((Boolean) data.get("is_logged_in") || (!((Boolean) data.get("is_logged_in")) && isPublic)) {
                    if ("divider".equalsIgnoreCase(label)) {
                        shortcutHtml += "<li class=\"divider\"></li>\n";
                    } else if (href.isEmpty()) {
                         shortcutHtml += "<li class=\"dropdown-menu-title\"><span>" + label + "</span></li>\n";
                    } else {
                        if (!href.contains("/")) {
                            href = data.get("base_link") + href;
                        }
                        shortcutHtml += "<li><a href=\"" + href + "\" target=\""+target+"\">" + label + "</a></li>\n";
                    }
                }
            }
        }
        
        String html = "";
        if (!shortcutHtml.isEmpty()) {
            html = "<li class=\"shortcut-link dropdown\">\n"
                  + "    <a data-toggle=\"dropdown\" class=\"btn dropdown-toggle\">\n"
                  + "	     <i class=\"fa fa-th-list white\"></i> " + getPropertyString("shortcutLinkLabel") + "\n"
                  + "	     <span class=\"caret\"></span>\n"
                  + "    </a>\n";
            
            html += "<ul class=\"dropdown-menu\">\n";
            html += shortcutHtml;
            html += "</ul></li>";
        }
        
        return html;
    }
    
    protected String getBreadcrumb(Map<String, Object> data) {
        String breadcrumb = "<ul class=\"breadcrumb\"><li><i class=\"fa fa-home\"></i> <a href=\"" + data.get("home_page_link") + "\">" + ResourceBundleUtil.getMessage("theme.universal.home") + "</a> <i class=\"fa fa-angle-right\"></i></li>";
        if ((Boolean) data.get("is_login_page") || (Boolean) data.get("embed")) {
            return "";
        } else if (userview.getCurrent() != null) {
            UserviewCategory category = userview.getCurrentCategory();
            if (!(category.getMenus().size() <= 1 && ((Boolean) data.get("combine_single_menu_category"))) && !"yes".equals(category.getPropertyString("hide"))) {
                breadcrumb += "<li><a href=\"" + getCategoryLink(category, data) + "\">" + StringUtil.stripAllHtmlTag(category.getPropertyString("label")) + "</a> <i class=\"fa fa-angle-right\"></i></li>";
            }
            breadcrumb += "<li><a>" + StringUtil.stripAllHtmlTag(userview.getCurrent().getPropertyString("label")) + "</a></li>";
        } else if (PROFILE.equals(userview.getParamString("menuId"))) {
            breadcrumb += "<li><a>" + ResourceBundleUtil.getMessage("theme.universal.profile") + "</a></li>";
        } else if (INBOX.equals(userview.getParamString("menuId"))) {
            breadcrumb += "<li><a>" + ResourceBundleUtil.getMessage("theme.universal.inbox") + "</a></li>";
        } else {
            breadcrumb += "<li><a>" + ResourceBundleUtil.getMessage("ubuilder.pageNotFound") + "</a></li>";
        }
        breadcrumb += "</ul>";

        return breadcrumb;
    }
    
    protected String getCategoryLink(UserviewCategory category, Map<String, Object> data) {
        UserviewMenu menu = category.getMenus().iterator().next();
        if (menu.isHomePageSupported()) {
            return menu.getUrl();
        }
        return "";
    }
    
    protected boolean enableResponsiveSwitch() {
        return MobileUtil.isMobileUserAgent() && "true".equals(getPropertyString("enableResponsiveSwitch"));
    }
    
    protected String pageProfile(Map<String, Object> data) {
        String html = "";
        try {
            UserProfileMenu profile = new UserProfileMenu();
            userview.setProperty("pageNotFoundMenu", profile);
            Map<String, Object> props = new HashMap<String, Object>();
            props.put("id", PROFILE);
            props.put("customId", PROFILE);
            props.put("menuId", PROFILE);
            props.put("label", ResourceBundleUtil.getMessage("theme.universal.profile"));
            profile.setRequestParameters(userview.getParams());
            profile.setProperties(props);
            profile.setUserview(userview);
            profile.setUrl(data.get("base_link") + PROFILE);
            profile.setKey(userview.getParamString("key"));
            html += UserviewUtil.getUserviewMenuHtml(profile);
        } catch (Exception e) {
            html += handleContentError(e, data);
        }
        return html;
    }
    
    protected String pageInbox(Map<String, Object> data) {
        String html = "";
        try {
            UserviewMenu menu = null;
            if ("current".equals(getPropertyString("inbox"))) {
                menu = new InboxMenu();
            } else if ("all".equals(getPropertyString("inbox"))) {
                menu = new UniversalInboxMenu();
            }
            if (menu != null) {
                userview.setProperty("pageNotFoundMenu", menu);
                Map<String, Object> props = new HashMap<String, Object>();
                props.put("id", INBOX);
                props.put("customId", INBOX);
                props.put("menuId", INBOX);
                props.put("label", "");
                
                if ("current".equals(getPropertyString("inbox"))) {
                    props.put(InboxMenu.PROPERTY_FILTER, InboxMenu.PROPERTY_FILTER_ALL);
                }
                
                menu.setRequestParameters(userview.getParams());
                menu.setProperties(props);
                menu.setUserview(userview);
                menu.setUrl(data.get("base_link") + INBOX);
                menu.setKey(userview.getParamString("key"));
                html += UserviewUtil.getUserviewMenuHtml(menu);
            }
        } catch (Exception e) {
            html += handleContentError(e, data);
        }
        return html;
    }

    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("_a");

        if ("getAssignment".equals(action)) {
            try {
                String appId = request.getParameter("appId");
                WorkflowManager wm = (WorkflowManager) AppUtil.getApplicationContext().getBean("workflowManager");
                if (appId != null && appId.isEmpty()) {
                    appId = null;
                }
                int count = wm.getAssignmentSize(appId, null, null);
                Collection<WorkflowAssignment> assignments = wm.getAssignmentListLite(appId, null, null, null, "a.activated", true, 0, 5);
        
                JSONObject jsonObj = new JSONObject();
                jsonObj.accumulate("count", count);
                
                String format = AppUtil.getAppDateFormat();
                Collection<Map<String, String>> datas = new ArrayList<Map<String, String>>();
                for (WorkflowAssignment a : assignments) {
                    Map<String, String> data = new HashMap<String, String>();
                    data.put("processId", a.getProcessId());
                    data.put("processDefId", a.getProcessDefId());
                    data.put("processRequesterId", a.getProcessRequesterId());
                    data.put("processName", a.getProcessName());
                    data.put("processVersion", a.getProcessVersion());
                    data.put("activityId", a.getActivityId());
                    data.put("activityDefId", a.getActivityDefId());
                    data.put("activityName", a.getActivityName());
                    data.put("assigneeName", a.getAssigneeName());
                    data.put("dateCreated", TimeZoneUtil.convertToTimeZone(a.getDateCreated(), null, format));
                    datas.add(data);
                }
                
                jsonObj.put("data", datas);

                jsonObj.write(response.getWriter());
            } catch (Exception ex) {
                LogUtil.error(this.getClass().getName(), ex, "Get assignment error!");
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }
    
    @Override
    public String getFavIconLink(Map<String, Object> data) {
        String fav = getPropertyString("fav_icon");
        if (!fav.isEmpty()) {
            return fav;
        } else {
            return super.getFavIconLink(data);
        }
    }
    
    @Override
    public boolean isMobileViewDisabled() {
        return true;
    }
    
    @Override
    public String[] themeDefinedMenusId() {
        return new String[] {PROFILE, INBOX};
    }
}
