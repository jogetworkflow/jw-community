package org.joget.apps.userview.lib;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewCategory;
import org.joget.apps.userview.model.UserviewPwaTheme;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.StringUtil;
import org.joget.directory.model.User;
import org.joget.workflow.util.WorkflowUtil;

import de.bripkens.gravatar.DefaultImage;
import de.bripkens.gravatar.Gravatar;
import de.bripkens.gravatar.Rating;

public class Dx8TrimedaTheme extends AjaxUniversalTheme {
    @Override
    public String getLabel() {
        return "DX 8 Trimeda";
    }

    @Override
    public String getVersion() {
        return "8.2.0";
    }

    @Override
    public String getName() {
        return "DX 8 Trimeda";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/trimeda.json", null, true, null);
    }

    public String getTrimedaPathName() {
        return "trimeda";
    }

    @Override
    public String getJsCssLib(Map<String, Object> data) {
        String jsCssLink = super.getJsCssLib(data);
        
        //Remove ajaxuniversal.min.js
        jsCssLink = jsCssLink.replace("<script src=\"" + data.get("context_path") + "/wro/ajaxuniversal.min.js\" defer></script>\n", "");
        jsCssLink = jsCssLink.replace("<script>loadCSS(\"" + data.get("context_path") + "/wro/ajaxuniversal.min.css" + "\")</script>\n", "");
        
        //Add own CSS & JS
        jsCssLink += "<script>\n";
        jsCssLink += "loadCSS(\"" + data.get("context_path") + "/wro/trimeda.min.css" + "\")\n";
        jsCssLink += "</script>\n";

        jsCssLink += "<script src=\"" + data.get("context_path") + "/wro/trimeda.min.js\" defer></script>\n";
        
        jsCssLink += "<style>" + generateLessCss() + "</style>";
        
        return jsCssLink;
    }

    @Override
    public String getHeader(Map<String, Object> data) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();

        super.getHeader(data);

        String sidebarToggled = "";
        if (data.get("body_classes").toString().contains("sidebar-toggled")) {
            sidebarToggled = "toggled";
        }

        data.put("header_inner_before", "<div class=\"navbar-inner\"><div class=\"container-fluid\"><div class='hi-trigger ma-trigger " + sidebarToggled + "' id=\"sidebar-trigger\"><div class=\"line-wrap d-none\"><div class=\"line top d-none\"></div><div class=\"line center d-none\"></div><div class=\"line bottom d-none\"></div></div><img id ='light-thumbnail' src='" + data.get("context_path") + "/" + getTrimedaPathName() + "/img/thumbnail_bar.svg'></img><img id='dark-thumbnail' src='" + data.get("context_path") + "/" + getTrimedaPathName() + "/img/thumbnail_dark_bar.svg'></img></div>");
        
        return UserviewUtil.getTemplate(this, data, "/templates/userview/header.ftl");
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
                String url = (email != null && !email.isEmpty())
                        ? new Gravatar()
                                .setSize(40)
                                .setHttps(true)
                                .setRating(Rating.PARENTAL_GUIDANCE_SUGGESTED)
                                .setStandardDefaultImage(DefaultImage.IDENTICON)
                                .getUrl(email)
                        : "//www.gravatar.com/avatar/default?d=identicon";
                profileImageTag = "<img class=\"gravatar\" alt=\"gravatar\" width=\"30\" height=\"30\" data-lazysrc=\"" + url + "\" onError=\"this.onerror = '';this.style.display='none';\"/> ";
            } else if ("hashVariable".equals(getPropertyString("userImage"))) {
                String url = AppUtil.processHashVariable(getPropertyString("userImageUrlHash"), null, StringUtil.TYPE_HTML, null, AppUtil.getCurrentAppDefinition());
                if (AppUtil.containsHashVariable(url) || url == null || url.isEmpty()) {
                    url = data.get("context_path") + "/universal/user.png";
                }
                profileImageTag = "<img alt=\"profile\" width=\"30\" height=\"30\" src=\"" + url + "\" /> ";
            }

            html += "<li class=\"user-link dropdown\">\n"
                    + "    <a data-toggle=\"dropdown\" href=\"javascript:;\" class=\"btn dropdown-toggle type-" + getPropertyString("userImage") + "\">\n"
                    + "	     " + profileImageTag + "\n"
                    + "	     <span class=\"caret\">" + user.getFirstName()+ " " +user.getLastName() + "</span>\n"
                    + "    </a>\n";

            html += "<ul class=\"dropdown-menu\">\n";
            if (!"true".equals(getPropertyString("profile")) && !user.getReadonly()) {
                html += "<div class=\"profile-item\">\n"
                        + "                <div class=\"profile-name dropdown-header\">\n"
                        + "                    "+user.getFirstName()+ " " +user.getLastName()
                        + "                 <a href=\"javascript:;\"><i class=\"zmdi zmdi-close\"></i></a>"
                        + "                </div>\n"
                        + "        <div class=\"menu-content d-flex align-items-center\">\n"
                        + "            <!--begin::Avatar-->\n"
                        + "            <div class=\"symbol symbol-50px\">\n"
                        + "             <div class='profile-img-bg'></div> \n"
                        +               profileImageTag
                        + "             <div class='profile-info'> \n"
                        + "                 <a href=\"" + data.get("base_link") + PROFILE + "\">" + ResourceBundleUtil.getMessage("theme.universal.profile") + "</a>\n"
                        + "                 <a href=\"#\" class=\"profile-email \">\n"
                        + "                    " + email + "                </a>\n"
                        + "              </div>\n"
                        + "            </div>\n"
                        + "            <!--end::Avatar-->\n"
                        + "\n"
                        + "            <!--begin::Username-->\n"
                        + "            <!--end::Username-->\n"
                        + "        </div>\n"
                        + "        <div class=\"separator\"></div>\n"
                        + "    </div>";
            }

            Object[] shortcut = (Object[]) getProperty("userMenu");
            if (shortcut != null && shortcut.length > 0) {
                for (Object o : shortcut) {
                    Map link = (HashMap) o;
                    String href = link.get("href").toString();
                    String label = link.get("label").toString();
                    String target = (link.get("target") == null) ? "" : link.get("target").toString();

                    if ("divider".equalsIgnoreCase(label)) {
                        html += "<li class=\"divider\"></li>\n";
                    } else if (href.isEmpty()) {
                        html += "<li class=\"dropdown-menu-title\"><span>" + label + "</span></li>\n";
                    } else {
                        if (!href.contains("/")) {
                            href = data.get("base_link") + href;
                        }
                        html += "<li><a href=\"" + href + "\" target=\"" + target + "\">" + label + "</a></li>\n";
                    }
                }
            }

            html += "    <li id='logout-link'><a href=\"" + data.get("logout_link") + "\">" + ResourceBundleUtil.getMessage("theme.universal.logout") + "</a></li>\n"
                    + "</ul>";

        } else {
            html += "<li class=\"user-link\">\n"
                    + "    <a href=\"" + data.get("login_link") + "\" class=\"btn\">\n"
                    + "	     <i class=\"fa fa-user white\"></i> " + ResourceBundleUtil.getMessage("ubuilder.login") + "\n"
                    + "    </a>\n";
        }
        html += "</li>";
        return html;
    }

    @Override
    protected String generateLessCss() {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        String css = "";

        css += ":root{";

        if (!getPropertyString("dx8background").isEmpty()) {
            css += "--theme-background:"+getPropertyString("dx8background")+ ";";
        } else {
            css += "--theme-background:#ECF5F3;";
        }

        if (!getPropertyString("dx8contentbackground").isEmpty()) {
            css += "--theme-content-background:"+getPropertyString("dx8contentbackground")+ ";";
        } else {
            css += "--theme-content-background:#FFFFFF;";
        }

        if (!getPropertyString("dx8headerColor").isEmpty()) {
            css += "--theme-header:"+getPropertyString("dx8headerColor")+ ";";
        }else {
            css += "--theme-header:#ECF5F3;";
        }

        if (!getPropertyString("dx8headerFontColor").isEmpty()) {
            css += "--theme-header-font:"+getPropertyString("dx8headerFontColor")+ ";";
        } else {
            css += "--theme-header-font: #003A70;";
        }


        if (!getPropertyString("dx8navBackground").isEmpty()) {
            css += "--theme-sidebar:"+getPropertyString("dx8navBackground")+ ";";
        } else {
            css += "--theme-sidebar:#ECF5F3;";
        }

        if (!getPropertyString("dx8navLinkBackground").isEmpty()) {
            css += "--theme-sidebar-link-bg-img:url('"+getPropertyString("dx8navLinkBackground")+ "')';";
        }else {
            if (getPropertyString("horizontal_menu").equals("true") || getPropertyString("horizontal_menu").equals("horizontal_inline")) {
                css += "--theme-sidebar-link-bg-img:url('"+ request.getContextPath() + "/" + getTrimedaPathName() + "/img/horizontal-menu-bg.png" + "');";
                css += "--theme-vertical-sidebar-link-bg-img:url('"+ request.getContextPath() + "/" + getTrimedaPathName() + "/img/side-menu-bg.png" + "');";
            } else {
                css += "--theme-sidebar-link-bg-img:url('"+ request.getContextPath() + "/" + getTrimedaPathName() + "/img/side-menu-bg.png" + "');";
            }
        }

        if (!getPropertyString("dx8navDropdownBackground").isEmpty()) {
            css += "--theme-sidebar-dropdown-bg-color:"+getPropertyString("dx8navDropdownBackground")+ ";";
        } else {
            css += "--theme-sidebar-dropdown-bg-color:#F8FDFF;";
        }

        if (!getPropertyString("dx8navLinkColor").isEmpty()) {
            css += "--theme-sidebar-link:"+getPropertyString("dx8navLinkColor")+ ";";
        } else {
            css += "--theme-sidebar-link:#003A70;";
        }

        if (!getPropertyString("dx8navLinkIcon").isEmpty()) {
            css += "--theme-sidebar-icon:"+getPropertyString("dx8navLinkIcon")+ ";";
        } else {
            css += "--theme-sidebar-icon:#2C5E8A;";
        }

        if (!getPropertyString("dx8navBadge").isEmpty()) {
            css += "--theme-sidebar-badge:"+getPropertyString("dx8navBadge")+";";
        } else {
            css += "--theme-sidebar-badge:#4B6EEC;";
        }

        if (!getPropertyString("dx8navBadgeText").isEmpty()) {
            css += "--theme-sidebar-badge-text:"+getPropertyString("dx8navBadgeText")+";";
        } else {
            css += "--theme-sidebar-badge-text:#FFFFFF;";
        }

        if (!getPropertyString("dx8navActiveLinkBackground").isEmpty()) {
            css += "--theme-sidebar-active-link-bg:"+getPropertyString("dx8navActiveLinkBackground")+ ";";
        } else {
            css += "--theme-sidebar-active-link-bg:#D3EDF7;";
        }

        if (!getPropertyString("dx8navActiveLinkColor").isEmpty()) {
            css += "--theme-sidebar-active-link:"+getPropertyString("dx8navActiveLinkColor")+ ";";
        } else {
            css += "--theme-sidebar-active-link:#003A70;";
        }

        if (!getPropertyString("dx8navActiveIconColor").isEmpty()) {
            css += "--theme-sidebar-active-icon:"+getPropertyString("dx8navActiveIconColor")+ ";";
        } else {
            css += "--theme-sidebar-active-icon:#4B6EEC;";
        }
        
        css += "--theme-nav-scrollbar-thumb:initial;";
        
        if (!getPropertyString("dx8buttonBackground").isEmpty()) {
            css += "--theme-button-bg:"+getPropertyString("dx8buttonBackground")+ ";";
        } else {
            css += "--theme-button-bg:#4B6EEC;";
        }
        
        if (!getPropertyString("dx8buttonColor").isEmpty()) {
            css += "--theme-button:"+getPropertyString("dx8buttonColor")+ ";";
        } else {
            css += "--theme-button:#FFFFFF;";
        }

        if (!getPropertyString("dx8fieldOutlineColor").isEmpty()) {
            css += "--field-border-color:"+getPropertyString("dx8fieldOutlineColor")+ ";";
        } else {
            css += "--field-border-color:#D6DFE8;";
        }

        if (!getPropertyString("dx8mainBoxBackgroundColor").isEmpty()) {
            css += "--main-box-background-color:"+getPropertyString("dx8mainBoxBackgroundColor")+ ";";
        } else {
            css += "--main-box-background-color:#EEF1F1;";
        }

        if (!getPropertyString("dx8secondaryBoxBackgroundColor").isEmpty()) {
            css += "--secondary-box-background-color:"+getPropertyString("dx8secondaryBoxBackgroundColor")+ ";";
        } else {
            css += "--secondary-box-background-color:#FAFBFC;";
        }

        if (!getPropertyString("dx8dropdownBackgroundColor").isEmpty()) {
            css += "--dropdown-bg-color:"+getPropertyString("dx8dropdownBackgroundColor")+ ";";
        } else {
            css += "--dropdown-bg-color:#164B7D;";
        }

        if (!getPropertyString("dx8primaryColor").isEmpty()) {
            css += "--theme-primary:"+getPropertyString("dx8primaryColor")+ ";";
        } else {
            css += "--theme-primary:#1677FF;";
        }

        if (!getPropertyString("dx8headingBgColor").isEmpty()) {
            css += "--theme-heading-bg:"+getPropertyString("dx8headingBgColor")+ ";";
        } else {
            css += "--theme-heading-bg:#FFFFFF;";
        }

        if (!getPropertyString("dx8headingFontColor").isEmpty()) {
            css += "--theme-heading-color:"+getPropertyString("dx8headingFontColor")+ ";";
        } else {
            css += "--theme-heading-color:rgba(0, 0, 0, 0.88);";
        }

        if (!getPropertyString("dx8fontColor").isEmpty()) {
            css += "--theme-font-color:"+getPropertyString("dx8fontColor")+ ";";
        } else {
            css += "--theme-font-color:#003A70;";
        }

        if (!getPropertyString("dx8secondaryFontColor").isEmpty()) {
            css += "--secondary-text-color:"+getPropertyString("dx8secondaryFontColor")+ ";";
        } else {
            css += "--secondary-text-color:#000000;";
        }

        if (!getPropertyString("dx8generalAccentColor").isEmpty()) {
            css += "--general-accent-color:"+getPropertyString("dx8generalAccentColor")+ ";";
        } else {
            css += "--general-accent-color:#4D799E;";
        }

        if (!getPropertyString("dx8contentFontColor").isEmpty()) {
            css += "--theme-content-color:"+getPropertyString("dx8contentFontColor")+ ";";
        } else {
            css += "--theme-content-color:#003A70;";
        }

        if (!getPropertyString("dx8footerBackground").isEmpty()) {
            css += "--theme-footer-bg:"+getPropertyString("dx8footerBackground")+ ";";
        } else {
            css += "--theme-footer-bg:#ECF5F3;";
        }

        if (!getPropertyString("dx8footerColor").isEmpty()) {
            css += "--theme-footer:"+getPropertyString("dx8footerColor")+ ";";
        } else {
            css += "--theme-footer:#003A70;";
        }

        if (!getPropertyString("dx8linkColor").isEmpty()) {
            css += "--theme-link:"+getPropertyString("dx8linkColor")+ ";";
        } else {
            css += "--theme-link:#5a83a5;";
        }

        if (!getPropertyString("dx8linkActiveColor").isEmpty()) {
            css += "--theme-link-active:"+getPropertyString("dx8linkActiveColor")+ ";";
        } else {
            css += "--theme-link-active:#003a70;";
        }

        css += "}";

        return css;          
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
            html += "<li class=\"inbox-notification dropdown\" data-url=\"" + url + "\">\n"
                  + "    <a data-toggle=\"dropdown\" href=\"javascript:;\" class=\"btn dropdown-toggle\">\n"
                  + "	 <i class=\"far fa-bell\"></i><span class=\"badge\"></span>\n"
                  + "    </a>\n"
                  + "    <ul class=\"dropdown-menu notifications\">\n"
                  + "<span class='dropdown-header'>" + ResourceBundleUtil.getMessage("userview.inboxmenu.allAssignments") + "</span>\n"
                  + "        <li class=\"loading\"><a><span><i class=\"fa fa-spinner fa-spin fa-3x\"></i></span></a></li>\n"
                  + "        <li><a href=\"" + data.get("base_link") + INBOX + "\" class=\"dropdown-menu-sub-footer\">" + ResourceBundleUtil.getMessage("theme.universal.viewAllTask") + "</a></li>\n"  
                  + "        <a href=\"#\" class=\"refresh\" >" + ResourceBundleUtil.getMessage("general.method.label.refresh") + "</a>"
                  + "    </ul>\n"
                  + "<li>";
        }
        
        return html;
    }

    @Override
    protected String getHomeLink(Map<String, Object> data) {
        String home_page_link = data.get("context_path").toString() + "/home";
        if (!getPropertyString("homeUrl").isEmpty()) {
            home_page_link = getPropertyString("homeUrl");
        }
        return "<li class=\"\"><a class=\"btn\" href=\"" + home_page_link + "\" title=\"" + ResourceBundleUtil.getMessage("theme.universal.home") + "\"><svg xmlns=\"http://www.w3.org/2000/svg\" fill=\"none\" viewBox=\"0 0 24 24\" stroke-width=\"2.25\" stroke=\"currentColor\" class=\"size-6\">\r\n" + //
                        "  <path stroke-linecap=\"round\" stroke-linejoin=\"round\" d=\"m2.25 12 8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25\" />\r\n" + //
                        "</svg>\r\n" + //
                        "</a></li>\n";
    }

    @Override
    protected String getBreadcrumb(Map<String, Object> data) {
        String breadcrumb = "<ul class=\"breadcrumb\"><li><i class=\"fa fa-home\"></i> <a href=\"" + data.get("home_page_link") + "\">" + ResourceBundleUtil.getMessage("theme.universal.home") + "</a> <span class='separator'> - </span></li>";
        if ((Boolean) data.get("is_login_page") || (Boolean) data.get("embed")) {
            return "";
        } else if (userview.getCurrent() != null) {
            UserviewCategory category = userview.getCurrentCategory();
            if (!(category.getMenus().size() <= 1 && ((Boolean) data.get("combine_single_menu_category"))) && !"yes".equals(category.getPropertyString("hide"))) {
                breadcrumb += "<li><a href=\"" + getCategoryLink(category, data) + "\">" + StringUtil.stripAllHtmlTag(category.getPropertyString("label")) + "</a> <span class='separator'> - </span> </li>";
            }
            breadcrumb += "<li><a>" + StringUtil.stripAllHtmlTag(userview.getCurrent().getPropertyString("label")) + "</a></li>";
        } else if (PROFILE.equals(userview.getParamString("menuId"))) {
            breadcrumb += "<li><a>" + ResourceBundleUtil.getMessage("theme.universal.profile") + "</a></li>";
        } else if (INBOX.equals(userview.getParamString("menuId"))) {
            breadcrumb += "<li><a>" + ResourceBundleUtil.getMessage("theme.universal.inbox") + "</a></li>";
        } else if (UserviewPwaTheme.PWA_OFFLINE_MENU_ID.equals(userview.getParamString("menuId")) || UserviewPwaTheme.PAGE_UNAVAILABLE_MENU_ID.equals(userview.getParamString("menuId"))) {
            breadcrumb += "<li><a>" + ResourceBundleUtil.getMessage("pwa.offline.breadcrumbTitle") + "</a></li>";
        } else {
            breadcrumb += "<li><a>" + ResourceBundleUtil.getMessage("ubuilder.pageNotFound") + "</a></li>";
        }
        breadcrumb += "</ul>";

        return breadcrumb;
    }

    @Override
    public String getContentContainer(Map<String, Object> data) {

        return super.getContentContainer(data);
    }
}
