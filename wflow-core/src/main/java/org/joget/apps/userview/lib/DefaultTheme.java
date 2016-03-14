package org.joget.apps.userview.lib;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewTheme;

public class DefaultTheme extends UserviewTheme {

    @Override
    public String getCss() {
        String css = AppUtil.readPluginResource(getClassName(), "/resources/themes/default/default.css");
        css += getPropertyString("css");
        css = css.replaceAll("@@contextPath@@", getRequestParameterString("contextPath"));
        return css;
    }

    @Override
    public String getJavascript() {
        return getPropertyString("js");
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
        if (getPropertyString("pageTop") != null && getPropertyString("pageTop").trim().length() > 0) {
            return getPropertyString("pageTop");
        }
        return null;
    }

    @Override
    public String getPageBottom() {
        if (getPropertyString("pageBottom") != null && getPropertyString("pageBottom").trim().length() > 0) {
            return getPropertyString("pageBottom");
        }
        return null;
    }

    @Override
    public String getBeforeContent() {
        if (getPropertyString("beforeContent") != null && getPropertyString("beforeContent").trim().length() > 0) {
            return getPropertyString("beforeContent");
        }
        return null;
    }

    public String getName() {
        return "V3 Default Theme";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "V3 Default Theme";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/defaultTheme.json", null, true, "message/userview/defaultTheme");
    }
}
