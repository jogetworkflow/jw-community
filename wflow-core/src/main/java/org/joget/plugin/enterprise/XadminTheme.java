package org.joget.plugin.enterprise;

import de.bripkens.gravatar.DefaultImage;
import de.bripkens.gravatar.Gravatar;
import de.bripkens.gravatar.Rating;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.app.service.MobileUtil;
import org.joget.apps.userview.lib.HtmlPage;
import org.joget.apps.userview.lib.Link;
import org.joget.apps.userview.model.CachedUserviewMenu;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewCategory;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryUtil;
import static org.joget.plugin.enterprise.UniversalTheme.INBOX;
import org.joget.workflow.util.WorkflowUtil;

public class XadminTheme extends UniversalTheme {
    
    @Override
    public String getName() {
        return "DX X-Admin Theme";
    }

    @Override
    public String getVersion() {
        return "7.0.0";
    }

    @Override
    public String getDescription() {
        return "A universal responsive Userview Theme based on open source X-Admin template";
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
    public String getPathName() {
        return "xadmin";
    }
    
    @Override
    public String getMetas(Map<String, Object> data) {
        String maxScale = "";
        if (MobileUtil.isIOS()) {
            maxScale = ", maximum-scale=1"; //used to prevent text field zoom on focus in ios
        }
        
        String meta = "<meta charset=\"UTF-8\">\n"; 
        meta += "<meta name=\"renderer\" content=\"webkit|ie-comp|ie-stand\">\n";
        meta += "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\n";
        meta += "<meta name=\"viewport\" content=\"width=device-width,user-scalable=yes, minimum-scale=0.4, initial-scale=0.8"+maxScale+", target-densitydpi=low-dpi\" />\n";
        meta += "<meta http-equiv=\"Cache-Control\" content=\"no-siteapp\" />";
                
        meta += super.getInternalMetas(data) + "\n";        

        return meta;
    }
    
    @Override
    public String getJsCssLib(Map<String, Object> data) {
        String path = data.get("context_path").toString();
        String jsCssLink = "";
        
        jsCssLink += "<link rel=\"stylesheet\" href=\""+path+"/wro/xadmin.min.css\">\n";
        jsCssLink += "<script>loadCSS(\"" + data.get("context_path") + "/xadmin/css/font.css" + "\")</script>\n";
        jsCssLink += "<script type=\"text/javascript\" src=\""+path+"/xadmin/lib/layui/layui.js\"></script>\n";
        jsCssLink += "<script type=\"text/javascript\" src=\""+path+"/wro/xadmin.min.js\"></script>\n";
        jsCssLink += "<!--[if lt IE 9]>\n";
        jsCssLink += "<script src=\""+path+"/lib/html5.min.js\"></script>\n";
        jsCssLink += "<script src=\""+path+"/lib/respond.min.js\"></script>\n";
        jsCssLink += "<![endif]-->\n";
        
        if (data.containsKey("is_login_page") && ((Boolean) data.get("is_login_page"))) {
            jsCssLink += "<link rel=\"stylesheet\" href=\""+path+"/xadmin/css/login.css\">\n";
        }
        
        jsCssLink += "<style>" + generateLessCss() + "</style>";
        
        jsCssLink += "<script>";
        if (!"true".equalsIgnoreCase(getPropertyString("reopenTab")) || "true".equalsIgnoreCase(userview.getParamString("isPreview"))) {
            jsCssLink += "var is_remember = false;";
        }
        jsCssLink += "var _enableResponsiveTable = true;</script>";
        
        jsCssLink += getInternalJsCssLib(data);
        
        return jsCssLink;
    }
    
    @Override
    protected String generateLessCss() {
        String css = "";
        String lessVariables = "";
        String background = "#F2F1F2";
        String headerColor = "#000051";
        String navBackground = "#EEEEEE";
        String navLinkBackground = "#EEEEEE";
        String navLinkColor = "#333333";
        String navActiveLinkBackground = "#1A237E";
        String navActiveLinkColor = "#ffffff";
        String buttonColor = "#ffffff";
        String buttonBackground = "#1A237E";
        String primaryColor = "#534BAE";
        String fontColor = "#666666";
        String linkColor = "#2196f3";
        String linkActiveColor = "#0069c0";
        String footerBackground = "#F2F1F2";
        String footerColor = "#666666";
        
        if (!getPropertyString("xbackground").isEmpty()) {
            background = getPropertyString("xbackground");
        }
        if (!getPropertyString("xheaderColor").isEmpty()) {
            headerColor = getPropertyString("xheaderColor");
        }
        if (!getPropertyString("xnavBackground").isEmpty()) {
            navBackground = getPropertyString("xnavBackground");
        }
        if (!getPropertyString("xnavLinkBackground").isEmpty()) {
            navLinkBackground = getPropertyString("xnavLinkBackground");
        }
        if (!getPropertyString("xnavLinkColor").isEmpty()) {
            navLinkColor = getPropertyString("xnavLinkColor");
        }
        if (!getPropertyString("xnavActiveLinkBackground").isEmpty()) {
            navActiveLinkBackground = getPropertyString("xnavActiveLinkBackground");
        }
        if (!getPropertyString("xnavActiveLinkColor").isEmpty()) {
            navActiveLinkColor = getPropertyString("xnavActiveLinkColor");
        }
        if (!getPropertyString("xbuttonColor").isEmpty()) {
            buttonColor = getPropertyString("xbuttonColor");
        }
        if (!getPropertyString("xbuttonBackground").isEmpty()) {
            buttonBackground = getPropertyString("xbuttonBackground");
        }
        if (!getPropertyString("xprimaryColor").isEmpty()) {
            primaryColor = getPropertyString("xprimaryColor");
        }
        if (!getPropertyString("xfontColor").isEmpty()) {
            fontColor = getPropertyString("xfontColor");
        }
        if (!getPropertyString("xfooterBackground").isEmpty()) {
            footerBackground = getPropertyString("xfooterBackground");
        }
        if (!getPropertyString("xfooterColor").isEmpty()) {
            footerColor = getPropertyString("xfooterColor");
        }
        if (!getPropertyString("xlinkColor").isEmpty()) {
            linkColor = getPropertyString("xlinkColor");
        }
        if (!getPropertyString("xlinkActiveColor").isEmpty()) {
            linkActiveColor = getPropertyString("xlinkActiveColor");
        }
        
        lessVariables += "@background: " + background + ";";
        lessVariables += "@headerColor: " + headerColor + ";";
        lessVariables += "@navBackground: " + navBackground + ";";
        lessVariables += "@navLinkBackground: " + navLinkBackground + ";";
        lessVariables += "@navLinkColor: " + navLinkColor + ";";
        lessVariables += "@navActiveLinkBackground: " + navActiveLinkBackground + ";";
        lessVariables += "@navActiveLinkColor: " + navActiveLinkColor + ";";
        lessVariables += "@buttonColor: " + buttonColor + ";";
        lessVariables += "@buttonBackground : " + buttonBackground + ";";
        lessVariables += "@primaryColor : " + primaryColor + ";";
        lessVariables += "@fontColor : " + fontColor + ";";
        lessVariables += "@footerBackground : " + footerBackground + ";";
        lessVariables += "@footerColor : " + footerColor + ";";
        lessVariables += "@linkColor : " + linkColor + ";";
        lessVariables += "@linkActiveColor : " + linkActiveColor + ";";
        
        try {
            // process LESS
            String less = AppUtil.readPluginResource(getClass().getName(), "resources/themes/" + getPathName() + "/theme.less");
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
        } catch (Exception e) {
            LogUtil.error(XadminTheme.class.getName(), e, "");
        }
        return css;
    }
    
    protected String getPrimaryColor() {
        String headerColor = "#222222";
        if (!getPropertyString("headerColor").isEmpty()) {
            headerColor = getPropertyString("headerColor");
        }
        return headerColor;
    }
    
    @Override
    public String getHeader(Map<String, Object> data) {
        if (isIndex()) {
            data.put("header_menus", getHeaderMenus(data));
            if (!getPropertyString("logo").isEmpty()) {
                data.put("header_name_inner_before", "<img class=\"logo\" alt=\"logo\" src=\""+getPropertyString("logo")+"\" />");
            }
            return UserviewUtil.getTemplate(this, data, "/templates/xadmin/header.ftl");
        } else {
            return "";
        }
    }
    
    @Override
    public String getFooter(Map<String, Object> data) {
        if (!((Boolean) data.get("embed")) && !isIndex() && !((data.containsKey("is_login_page") && ((Boolean) data.get("is_login_page"))))) {
            return super.getFooter(data);
        }
        return "";
    }
    
    protected String getHeaderMenus(Map<String, Object> data) {
        String menus = getShortcut(data);
        menus += getUserMenu(data);
        return menus;
    }
    
    @Override
    protected String getUserMenu(Map<String, Object> data) {
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
                profileImageTag = "<img class=\"gravatar\" alt=\"gravatar\" width=\"30\" height=\"30\" data-lazysrc=\""+url+"\" onError=\"this.onerror = '';this.style.display='none';\"/> ";
            } else if ("hashVariable".equals(getPropertyString("userImage"))) {
                String url = AppUtil.processHashVariable(getPropertyString("userImageUrlHash"), null, StringUtil.TYPE_HTML, null, AppUtil.getCurrentAppDefinition());
                if (AppUtil.containsHashVariable(url) || url == null || url.isEmpty()) {
                    url = data.get("context_path") + "/" + getPathName() + "/user.png";
                }
                profileImageTag = "<img alt=\"profile-img profile\" width=\"30\" height=\"30\" src=\""+url+"\" /> ";
            } else {
                profileImageTag = "<i class=\"icon iconfont\">&#xe6b8;</i> ";
            }
            
            html += "<ul class=\"layui-nav right user-menu\" lay-filter=\"\">\n";
            
            html += getInbox(data);
            
            html += "<li class=\"layui-nav-item\">\n";
            html += "<a href=\"javascript:;\">"+profileImageTag+StringUtil.stripHtmlTag(DirectoryUtil.getUserFullName(user), new String[]{}) + "</a>\n";
            html += "<dl class=\"layui-nav-child\">\n";
            
            if (!"true".equals(getPropertyString("profile")) && !user.getReadonly()) {
                String profileLabel = ResourceBundleUtil.getMessage("theme.universal.profile");
                html += "<dd><a onclick=\"xadmin.open('"+profileLabel+"','"+data.get("base_link") + PROFILE+"?embed=true')\">"+profileLabel+"</a></dd>\n";
            }
            
            Object[] shortcut = (Object[]) getProperty("userMenu");
            if (shortcut != null && shortcut.length > 0) {
                for (Object o : shortcut) {
                    Map link = (HashMap) o;
                    String href = link.get("href").toString();
                    String label = link.get("label").toString();
                    String target = (link.get("target") == null)?"":link.get("target").toString();
                    if ("divider".equalsIgnoreCase(label)) {
                        html += "<dd class=\"divider\"></dd>\n";
                    } else if (href.isEmpty()) {
                         html += "<dd class=\"dropdown-menu-title\"><span>" + label + "</span></dd>\n";
                    } else {
                        if (!href.contains("/")) {
                            href = data.get("base_link") + href;
                        }
                        String attr = "";
                        if ("tab".equalsIgnoreCase(target)) {
                            attr = "onclick=\"xadmin.add_tab('"+StringUtil.escapeString(label, StringUtil.TYPE_HTML + ";" + StringUtil.TYPE_JAVASCIPT, null)+"','"+href+"')\"";
                        } else if ("popup".equalsIgnoreCase(target)) {
                            attr = "onclick=\"xadmin.open('"+StringUtil.escapeString(label, StringUtil.TYPE_HTML + ";" + StringUtil.TYPE_JAVASCIPT, null)+"','"+href+"')\"";
                        } else {
                            attr = "href=\"" + href + "\" target=\""+target+"\"";
                        }
                        html += "<dd><a "+attr+">" + label + "</a></dd>\n";
                    }
                }
            }
            html += "<dd><a href=\"" + data.get("logout_link") + "\">"+ResourceBundleUtil.getMessage("theme.universal.logout")+"</a></dd>\n";
            html += "</dl></li>\n";
            html += "<li id=\"help-container\"></li></ul>";
        } else {
            html += "<ul class=\"layui-nav right\" lay-filter=\"\">\n";
            html += "<li class=\"layui-nav-item\">\n";
            html += "<a href=\"" + data.get("login_link") + "\"><i class=\"icon iconfont\">&#xe6b8;</i> "+ResourceBundleUtil.getMessage("ubuilder.login") + "</a>\n";
            html += "</li>\n";
            html += "<li id=\"help-container\"></li></ul>";
        }
        return html;
    }
    
    @Override
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
                        shortcutHtml += "<dd class=\"divider\"></dd>\n";
                    } else if (href.isEmpty()) {
                         shortcutHtml += "<dd class=\"dropdown-menu-title\"><span>" + label + "</span></dd>\n";
                    } else {
                        if (!href.contains("/")) {
                            href = data.get("base_link") + href;
                        }
                        String attr = "";
                        if ("tab".equalsIgnoreCase(target)) {
                            attr = "onclick=\"xadmin.add_tab('"+StringUtil.escapeString(label, StringUtil.TYPE_HTML + ";" + StringUtil.TYPE_JAVASCIPT, null)+"','"+href+"')\"";
                        } else if ("popup".equalsIgnoreCase(target)) {
                            attr = "onclick=\"xadmin.open('"+StringUtil.escapeString(label, StringUtil.TYPE_HTML + ";" + StringUtil.TYPE_JAVASCIPT, null)+"','"+href+"')\"";
                        } else {
                            attr = "href=\"" + href + "\" target=\""+target+"\"";
                        }
                        shortcutHtml += "<dd><a "+attr+">" + label + "</a></dd>\n";
                    }
                }
            }
        }
        
        String html = "";
        if (!shortcutHtml.isEmpty()) {
            html = "<ul class=\"layui-nav left fast-add\" lay-filter=\"\">\n";
            html += "<li class=\"layui-nav-item\">\n";
            html += "<a href=\"javascript:;\">"+getPropertyString("shortcutLinkLabel")+"</a>";
            html += "<dl class=\"layui-nav-child\">";
            html += shortcutHtml;
            html += "</dl></li></ul>";
        }
        return html;
    }
    
    @Override
    protected String getInbox(Map<String, Object> data) {
        String html = "";
        
        if (!getPropertyString("inbox").isEmpty()) {
            String url = data.get("context_path") + "/web/json/plugin/" + getClassName() + "/service?_a=getAssignment";
            if ("current".equals(getPropertyString("inbox"))) {
                try {
                    url += "&appId=" + URLEncoder.encode(userview.getParamString("appId"), "UTF-8");
                } catch (UnsupportedEncodingException e){}
            }
            html += "<li class=\"layui-nav-item inbox-notification\"\" data-url=\"" + url + "\">\n"
                  + "    <a href=\"javascript:;\"><i class=\"icon iconfont\">&#xe713;</i><span class=\"badge red\">0</span></a>\n"
                  + "    <dl class=\"layui-nav-child\">\n"
                  + "        <dd class=\"inbox-title\"><span>" + ResourceBundleUtil.getMessage("theme.universal.inboxTaskMessage") + "</span><a href=\"#\" class=\"refresh\"><i class=\"layui-icon\">&#xe666;</i></a></dd>"
                  + "        <dd class=\"loading\"><a><span><i class=\"fa fa-spinner fa-spin fa-3x\"></i></span></a></dd>\n"
                  + "        <dd class=\"\"><a data-href=\""+data.get("base_link") + INBOX+"\" onclick=\"xadmin.add_tab('"+StringUtil.escapeString(ResourceBundleUtil.getMessage("theme.universal.viewAllTask"), StringUtil.TYPE_HTML + ";" + StringUtil.TYPE_JAVASCIPT, null)+"','"+data.get("base_link") + INBOX+"',true)\" class=\"dropdown-menu-sub-footer\">" + ResourceBundleUtil.getMessage("theme.universal.viewAllTask") + "</a></dd>\n"  
                  + "    </dl>\n"
                  + "</li>";
        }
        
        return html;
    }
    
    @Override
    public String getContentContainer(Map<String, Object> data) {
        if (isIndex()) {
            String url = userview.getParamString("url");
            String menuLabel = "";
            String tempId = "";
            if (url == null || url.isEmpty()) {
                String key = userview.getParamString("key");
                if (key.isEmpty()) {
                    key = Userview.USERVIEW_KEY_EMPTY_VALUE;
                }
                if ("true".equalsIgnoreCase(userview.getParamString("isPreview"))) {
                    url = data.get("context_path").toString() + "/web/console/app/" + userview.getParamString("appId") + "/" + userview.getParamString("appVersion") + "/userview/builderPreview/" + userview.getPropertyString("id") + "/" + getUserview().getPropertyString("homeMenuId");
                } else {
                    url = data.get("context_path").toString() + "/web/userview/" + userview.getParamString("appId") + "/" + userview.getPropertyString("id") + "/" + key + "/" + getUserview().getPropertyString("homeMenuId");
                }
                tempId = getUserview().getPropertyString("homeMenuId");
            } else {
                tempId = url.substring(url.lastIndexOf("/") + 1);
                if (tempId.contains("?")) {
                    tempId = tempId.substring(0, tempId.indexOf("?") - 1);
                }
            }
            if (PROFILE.equals(tempId)) {
                menuLabel = ResourceBundleUtil.getMessage("theme.universal.profile");
            } else if (INBOX.equals(tempId)) {
                menuLabel = ResourceBundleUtil.getMessage("theme.universal.inbox");
            } else {
                for (UserviewCategory c : userview.getCategories()) {
                    if (c.getMenus() != null) {
                        for (UserviewMenu m : c.getMenus()) {
                            String menuId = m.getPropertyString("id");
                            if (m.getPropertyString("customId") != null && m.getPropertyString("customId").trim().length() > 0) {
                                menuId = m.getPropertyString("customId");
                            }
                            if (tempId.equals(menuId)) {
                                menuLabel = StringUtil.stripAllHtmlTag(m.getPropertyString("label"));
                                break;
                            }
                        }
                    }
                    if (!menuLabel.isEmpty()) {
                        break;
                    }
                }
            }
            if (menuLabel.isEmpty()) {
                menuLabel = " ";
            }
            data.put("preloadLabel", menuLabel);
            data.put("preloadUrl", url);
            return UserviewUtil.getTemplate(this, data, "/templates/xadmin/content.ftl");
        } else {
            if (!((Boolean) data.get("embed"))) {
                String ContentInnerBefore = getBreadcrumb(data);
                if (getPropertyString("fontControl").equalsIgnoreCase("true")) {
                    ContentInnerBefore += getFontSizeController(data);
                }
                data.put("content_before", ContentInnerBefore + "<div class=\"layui-fluid\">");
                data.put("content_after", "</div>");
                data.put("content_classes", "layui-row layui-col-space15");

                data.put("main_container_after", getMenuScript(data));
            }
            return UserviewUtil.getTemplate(this, data, "/templates/userview/contentContainer.ftl");
        }
    }
    
    @Override
    protected String getFontSizeController(Map<String, Object> data) {
        String fontController = "<div class=\"adjustfontSize\">\n"
                + "      <div style=\"float:right\">\n"
                + "            <div class=\"btn-group\" role=\"group\" aria-label=\"Basic example\">\n"
                + "            <span> "+ ResourceBundleUtil.getMessage("theme.universal.fontSize") +":</span>\n"
                + "                  <button id=\"smallFont\" type=\"button\" class=\"buttonFontSize\"><i class=\"fas fa-font\" style=\"font-size:13px\"></i></button>\n"
                + "                  <button id=\"mediumFont\" type=\"button\" class=\"buttonFontSize\"><i class=\"fas fa-font\" style=\"font-size:17px\"></i></button>\n"
                + "                  <button id=\"bigFont\" type=\"button\" class=\"buttonFontSize\"><i class=\"fas fa-font\" style=\"font-size:20px\"></i></button>\n"
                + "            </div>\n"
                + "      </div>\n"
                + "      <div style=\"clear:both\"></div>\n"
                + "</div>";
        if ((Boolean) data.get("is_login_page") || (Boolean) data.get("embed")) {
            return "";
        } else {
            return fontController;
        }
    }
    
    protected String getMenuScript(Map<String, Object> data){
        String title = " ";
        String menu = "";
        if (userview.getCurrent() != null) {
            if (PROFILE.equals(getRequestParameter("menuId"))) {
                title = ResourceBundleUtil.getMessage("theme.universal.profile");
            } else if (INBOX.equals(getRequestParameter("menuId"))) {
                title = ResourceBundleUtil.getMessage("theme.universal.inbox");
            } else {
                title = StringUtil.stripAllHtmlTag(userview.getCurrent().getPropertyString("label"));
            }
            String decoMenu = userview.getCurrent().getDecoratedMenu();
            if (decoMenu != null) {
                menu = transformMenu(userview.getCurrentCategory(), userview.getCurrent(), decoMenu);
            }
        }
        String script = "<script>";
        script += "$(function(){";
        script += "    function initPage() {";
        script += "        setTimeout(function(){";
        script += "            layui.use(['layer', 'element'], function(){";
        script += "                if (layer !== undefined && element !== undefined ) {";
        script += "                    xadmin.updateTabTitle(\""+StringUtil.escapeString(title, StringUtil.TYPE_JAVASCIPT, null)+"\");";
        script += "                    xadmin.updateMenu(\""+StringUtil.escapeString(menu, StringUtil.TYPE_JAVASCIPT, null)+"\");";    
        script += "                } else {";
        script += "                    initPage();";
        script += "                }";
        script += "            });";
        script += "        }, 1000);";  
        script += "    }";
        script += "    initPage();";
        script += "});";
        script += "</script>";
        return script;
    }
    
    @Override
    public String getLayout(Map<String, Object> data) {
        if (!"true".equalsIgnoreCase(getPropertyString("disabledFineFont"))) {
            data.put("html_attributes", "class=\"x-admin-sm\"");
        }
        if ((getRequestParameter("menuId") == null || getUserview().getPropertyString("homeMenuId").equals(getRequestParameter("menuId")))) {
            data.put("body_classes", data.get("body_classes").toString());
        }
        if (!((Boolean) data.get("embed")) && !getPropertyString("logo").isEmpty()) {
            data.put("body_classes", ((data.get("body_classes") != null)?data.get("body_classes").toString():"") +" has-logo");
        }
        if (!((Boolean) data.get("embed"))) {
            if (!isIndex()) {
                data.put("body_classes", ((data.get("body_classes") != null)?data.get("body_classes").toString():"") +" inner-frame");
            } else {
                data.put("body_classes", ((data.get("body_classes") != null)?data.get("body_classes").toString():"") +" index-window");
            }
        }

        return UserviewUtil.getTemplate(this, data, "/templates/userview/layout.ftl");
    }
    
    @Override
    public String getLoginForm(Map<String, Object> data) {
        data.put("hide_nav", true);
        if (!getPropertyString("logo").isEmpty()) {
            data.put("logo", "<img class=\"logo\" alt=\"logo\" src=\""+getPropertyString("logo")+"\" />");
        }
        if (!getPropertyString("loginBackground").isEmpty()) {
            data.put("loginBackground", "<style>#login{background-image:url('"+getPropertyString("loginBackground")+"');}</style>");
        }
        data.put("login_title", StringUtil.stripHtmlRelaxed(userview.getPropertyString("name")));
        if (!data.containsKey("login_form_before")) {
            if (getProperties().containsKey("loginPageTop")) {
                data.put("login_form_before", getPropertyString("loginPageTop"));
            } else {
                data.put("login_form_before", this.userview.getSetting().getPropertyString("loginPageTop"));
            }
        }
        if (!data.containsKey("login_form_after")) {
            if (getProperties().containsKey("loginPageBottom")) {
                data.put("login_form_after", getPropertyString("loginPageBottom"));
            } else {
                data.put("login_form_after", this.userview.getSetting().getPropertyString("loginPageBottom"));
            }
        }
        return UserviewUtil.getTemplate(this, data, "/templates/xadmin/login.ftl");
    }
    
    @Override
    public String getMenus(Map<String, Object> data) {
        if (isIndex()) {
            data.put("nav_classes", "left-nav");
            data.put("nav_inner_before", "<div id=\"side-nav\">");
            data.put("categories_container_id", "nav");
            data.put("nav_inner_after", "</div>");
            data.put("menus_container_classes", "sub-menu");
            
            if ("true".equals(getPropertyString("displayCategoryLabel"))) {
                data.put("combine_single_menu_category", false);
            } else {
                data.put("combine_single_menu_category", true);
            }

            return UserviewUtil.getTemplate(this, data, "/templates/userview/menus.ftl");
        } else {
            return "";
        }
    }
    
    @Override
    protected String getBreadcrumb(Map<String, Object> data) {
        String html = "<div class=\"x-nav\"><span class=\"layui-breadcrumb\">";
        html += "<a><i class=\"layui-icon\">&#xe68e;</i> "+ResourceBundleUtil.getMessage("theme.universal.home") +"</a>";
        if ((Boolean) data.get("is_login_page") || (Boolean) data.get("embed")) {
            return "";
        } else if (userview.getCurrent() != null) {
            UserviewCategory category = userview.getCurrentCategory();
            if (!(category.getMenus().size() <= 1 && !"yes".equals(category.getPropertyString("hide")))) {
                html += "<a>" + StringUtil.stripAllHtmlTag(category.getPropertyString("label")) + "</a>";
            }
            html += "<a><cite>" + StringUtil.stripAllHtmlTag(userview.getCurrent().getPropertyString("label")) + "</cite></a>";
        } else if (PROFILE.equals(userview.getParamString("menuId"))) {
            html += "<a><cite>" + ResourceBundleUtil.getMessage("theme.universal.profile") + "</cite></a>";
        } else if (INBOX.equals(userview.getParamString("menuId"))) {
            html += "<a><cite>" + ResourceBundleUtil.getMessage("theme.universal.inbox") + "</cite></a>";
        } else {
            html += "<a><cite>" + ResourceBundleUtil.getMessage("ubuilder.pageNotFound") + "</cite></a>";
        }
        
        html += "</span><a class=\"layui-btn layui-btn-small\" style=\"line-height:1.6em;margin-top:4px;float:right\" onclick=\"location.reload()\" title=\""+ResourceBundleUtil.getMessage("general.method.label.refresh")+"\"><i class=\"layui-icon layui-icon-refresh\" style=\"line-height:30px\"></i></a></div>";
        return html;
    }
    
    @Override
    public String decorateCategoryLabel(UserviewCategory category) {
        String label = StringUtil.stripHtmlRelaxed(category.getPropertyString("label"));
        String icon = "";
        if (label.contains("</i>") && label.trim().startsWith("<i")) {
            icon = label.substring(0, label.indexOf("</i>") + 4);
            label = label.substring(label.indexOf("</i>") + 4);
            icon = icon.replace("<i", "<i lay-tips=\"" + StringUtil.stripAllHtmlTag(label) + "\"");
            icon = icon.replace("class=\"", "class=\"left-nav-li ");
        } else {
            icon = "<i class=\"iconfont left-nav-li\" lay-tips=\""+StringUtil.stripAllHtmlTag(label)+"\">&#xe6b4;</i>";
        }
        return icon + "<cite>" + label + "</cite><i class=\"iconfont nav_right\">&#xe697;</i>";
    }
    
    @Override
    public String decorateMenu(UserviewCategory category, UserviewMenu menu) {
        String decoratedMenu = menu.getDecoratedMenu();
        if (((menu instanceof Link) || ((menu instanceof CachedUserviewMenu) && ((CachedUserviewMenu) menu).instanceOf(Link.class))) || decoratedMenu == null || decoratedMenu.isEmpty()) {
            return getMenuHtml(category, menu, "", null);
        } else {
            return transformMenu(category, menu, decoratedMenu);
        }
    }
    
    protected String transformMenu(UserviewCategory category, UserviewMenu menu, String decoratedMenu) {
        if (decoratedMenu.contains("badge")) {
            String label = StringUtil.stripAllHtmlTag(menu.getPropertyString("label"));
            String badge = StringUtil.stripAllHtmlTag(decoratedMenu);
            badge = badge.replaceFirst(StringUtil.escapeRegex(label), "");
            return getMenuHtml(category, menu, "<span class='pull-right badge rowCount'>"+badge+"</span>", null);
        } else {
            
        }
        return decoratedMenu;
    }
    
    protected String getMenuHtml(UserviewCategory category, UserviewMenu menu, String extra, String onclick) {
        // sanitize label
        String label = menu.getPropertyString("label");
        if (label != null) {
            label = StringUtil.stripHtmlRelaxed(label);
        }
        String icon = "";
        if (label.contains("</i>") && label.trim().startsWith("<i")) {
            icon = label.substring(0, label.indexOf("</i>") + 4);
            label = label.substring(label.indexOf("</i>") + 4);
        } else if (category.getMenus().size() == 1) {
            icon = "<i class=\"iconfont\">&#xe6b4;</i>";
        }
        String url = menu.getUrl();
        if ((menu instanceof Link) || ((menu instanceof CachedUserviewMenu) && ((CachedUserviewMenu) menu).instanceOf(Link.class))) {
            url = menu.getPropertyString("url");
            if ("blank".equals(menu.getPropertyString("target"))) {
                onclick = "onclick=\"window.open('" + url + "');return false;\"";
            } else if ("self".equals(menu.getPropertyString("target"))) {
                onclick = "onclick=\"xadmin.redirect('"+StringUtil.escapeString(StringUtil.stripAllHtmlTag(label), StringUtil.TYPE_JAVASCIPT, null)+"','" + url + "');return false;\"";
            }
        }
        if (category.getMenus().size() == 1) {
            icon = icon.replace("<i", "<i lay-tips=\"" + StringUtil.stripAllHtmlTag(label) + "\"");
            icon = icon.replace("class=\"", "class=\"left-nav-li ");
        }
        if (onclick == null) {
            onclick = "onclick=\"xadmin.add_tab('"+StringUtil.escapeString(StringUtil.stripAllHtmlTag(label), StringUtil.TYPE_JAVASCIPT, null)+"','"+url+"',true)\"";
        }
        return "<a class=\"menu-link default\" "+onclick+">" + icon + "<cite>" + label + "</cite>"+extra+"</a>";
    }
    
    @Override
    public String handleRedirection() {
        if (!isIndex()) {
            HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
            String referer = request.getHeader("referer");
            
            String key = userview.getParamString("key");
            if (key.isEmpty()) {
                key = Userview.USERVIEW_KEY_EMPTY_VALUE;
            }
                
            if (!"true".equalsIgnoreCase(userview.getParamString("embed")) && !(referer != null && referer.contains("/" + userview.getPropertyString("id") + "/" + key + "/"))) {
                String url = request.getRequestURI();
                if (url.contains("/" + userview.getPropertyString("id") + "/") && url.endsWith("/_index")) {
                    return null;
                }

                url += (request.getQueryString() == null?"":("?" + StringUtil.decodeURL(request.getQueryString())));
                try {
                    url = URLEncoder.encode(url, "UTF-8");
                } catch (Exception e) {
                }

                return "redirect:/web/userview/" + userview.getParamString("appId") + "/" + userview.getPropertyString("id") + "/" + key + "/_index?url="+url;
            } 
        }
        return null;
    }
    
    @Override
    public String getCustomHomepage() {
        if (isIndex() && userview.getCurrent() == null) {
            UserviewMenu dummy = new HtmlPage();
            dummy.setProperties(new HashMap<String, Object>());
            dummy.setProperty("id", "_index");
            userview.setCurrent(dummy); //set a dummy menu
        }
        return "_index";
    }
    
    protected boolean isIndex() {
        return "_index".equals(userview.getParamString("menuId"))
                || ("true".equalsIgnoreCase(userview.getParamString("isPreview")) && "".equals(userview.getParamString("menuId")));
    }
    
    @Override
    public Set<String> getOfflineStaticResources() {
        Set<String> urls = new HashSet<String>();
        String contextPath = AppUtil.getRequestContextPath();
        String bn = ResourceBundleUtil.getMessage("build.number");
        urls.add(contextPath + "/wro/common.css");
        urls.add(contextPath + "/wro/xadmin.min.css");
        urls.add(contextPath + "/xadmin/css/font.css");
        urls.add(contextPath + "/js/fontawesome5/css/all.min.css");
        urls.add(contextPath + "/wro/common.preload.js?build=" + bn);
        urls.add(contextPath + "/wro/common.js?build=" + bn);
        urls.add(contextPath + "/wro/form_common.js?build=" + bn);
        urls.add(contextPath + "/xadmin/lib/layui/layui.js");
        urls.add(contextPath + "/wro/xadmin.min.js");
        urls.add(contextPath + "/xadmin/lib/html5.min.js");
        urls.add(contextPath + "/xadmin/lib/respond.min.js");
        urls.add(contextPath + "/xadmin/css/login.css");
        
        return urls;
    }
    
    @Override
    public Set<String> getCacheUrls(String appId, String userviewId, String userviewKey) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        String contextPath = request.getContextPath();
        
        Set<String> urls = new HashSet<String>();
        
        urls.add(contextPath + "/web/userview/" + appId + "/" + userviewId + "/"+userviewKey+"/index");
        urls.add(contextPath + "/web/userview/" + appId + "/" + userviewId + "/"+userviewKey+"/_index");
        
        if (!getPropertyString("urlsToCache").isEmpty()) {
            String urlsToCache = getPropertyString("urlsToCache");
            if (urlsToCache != null) {
                StringTokenizer st = new StringTokenizer(urlsToCache, "\n");
                while (st.hasMoreTokens()) {
                    String url = st.nextToken().trim();
                    if (url.startsWith("/") && !url.startsWith(contextPath)) {
                        url = contextPath + url;
                    }
                    urls.add(url);
                }
            }
        }
        
        return urls;
    }
}
