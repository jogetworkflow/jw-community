package org.joget.plugin.enterprise;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewTheme;

public class CorporatiTheme extends UserviewTheme {

    @Override
    public String getCss() {
        String contextPath = AppUtil.getRequestContextPath();
        String cssPath = contextPath + "/plugin/org.joget.plugin.enterprise.CorporatiTheme/themes/corporati/corporati.css";
        String cssLink = "<link rel='stylesheet' href='" + cssPath + "'>";
        String css = "</style>\n" + cssLink;

        // add selected scheme CSS
        String colorScheme = getPropertyString("colorScheme");
        if ("green".equals(colorScheme)) {
            String additionalCssPath = contextPath + "/plugin/org.joget.plugin.enterprise.CorporatiTheme/themes/corporati/corporatiGreen.css";
            String additionalCssLink = "<link rel='stylesheet' href='" + additionalCssPath + "'>";
            css += "</style>\n" + additionalCssLink;
        } else if ("blue".equals(colorScheme)) {
            String additionalCssPath = contextPath + "/plugin/org.joget.plugin.enterprise.CorporatiTheme/themes/corporati/corporatiBlue.css";
            String additionalCssLink = "<link rel='stylesheet' href='" + additionalCssPath + "'>";
            css += "</style>\n" + additionalCssLink;
        } else if ("red".equals(colorScheme)) {
            String additionalCssPath = contextPath + "/plugin/org.joget.plugin.enterprise.CorporatiTheme/themes/corporati/corporatiRed.css";
            String additionalCssLink = "<link rel='stylesheet' href='" + additionalCssPath + "'>";
            css += "</style>\n" + additionalCssLink;
        } else if ("yellow".equals(colorScheme)) {
            String additionalCssPath = contextPath + "/plugin/org.joget.plugin.enterprise.CorporatiTheme/themes/corporati/corporatiYellow.css";
            String additionalCssLink = "<link rel='stylesheet' href='" + additionalCssPath + "'>";
            css += "</style>\n" + additionalCssLink;
        }

        // add custom CSS URL
        String customCssUrl = getPropertyString("cssUrl");
        if (customCssUrl != null && !customCssUrl.trim().isEmpty()) {
            if (customCssUrl.startsWith("/")) {
                customCssUrl = contextPath + customCssUrl;
            }
            css +=  "\n<link rel='stylesheet' href='" + customCssUrl + "'>";
        }
        
        // add custom CSS
        String customCss = getPropertyString("css");
        css +=  "\n<style type='text/css'>" + customCss;

        return css;
    }

    @Override
    public String getJavascript() {
        String js = getPropertyString("js");
        if ("true".equals(getPropertyString("collapsibleMenu"))) {
            js += "\n$(document).ready(function(){$(\".category-label, .category-label a\").click(function(){";
            js += "var element = ($(this).hasClass(\"category-label\"))? $(this) : $(this).parent();";
            js += "var container = $(element).parent().find(\".menu-container\");";
            js += "if($(container).is(\":visible\")){$(container).hide();}else{$(container).show();}return false;";
            js += "});});";
        }
        
        js += "\n$(document).ready(function(){if($(\".menu.current\").length === 1) {var ctop = $(\".menu.current\").offset().top;";
        js += "if (ctop > 140) {";
        js += "$(\"#navigation\").scrollTop($(\".menu.current\").offset().top - 140);";
        js += "}}});";
        
        return js;
    }

    @Override
    public String getHeader() {
        if (getPropertyString("customHeader") != null && getPropertyString("customHeader").trim().length() > 0) {
            return getPropertyString("customHeader");
        }
        return null;
    }

    @Override
    public String getFooter() {
        if (getPropertyString("customFooter") != null && getPropertyString("customFooter").trim().length() > 0) {
            return getPropertyString("customFooter");
        }
        return null;
    }

    @Override
    public String getPageTop() {
        return null;
    }

    @Override
    public String getPageBottom() {
        if (getPropertyString("sideBar") != null && getPropertyString("sideBar").trim().length() > 0) {
            return "<div id=\"sideBar\">" + getPropertyString("sideBar") + "</div>";
        }
        return null;
    }

    @Override
    public String getBeforeContent() {
        if (getPropertyString("customBanner") != null && getPropertyString("customBanner").trim().length() > 0) {
            return getPropertyString("customBanner");
        }
        return null;
    }

    public String getName() {
        return "Corporati Theme";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Corporati Theme";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/corporatiTheme.json", null, true, "message/userview/corporatiTheme");
    }
}

