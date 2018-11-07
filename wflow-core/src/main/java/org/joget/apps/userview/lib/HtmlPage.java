package org.joget.apps.userview.lib;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.apps.userview.model.UserviewMenu;

public class HtmlPage extends UserviewMenu {

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return "HTML Page";
    }

    @Override
    public String getIcon() {
        return "<i><span style=\"font-size: 60%;top: -9px;font-weight: bold;\">HTML</span></i>";
    }

    @Override
    public String getRenderPage() {
        String html = "<div class=\"ui-html\">\n"
            + getPropertyString("content")
            + "\n<div class=\"ui-html-footer\"></div>"
            + "\n</div>";
        return html;
    }

    public String getName() {
        return "HTML Page Menu";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/htmlPage.json", null, true, "message/userview/htmlPage");
    }

    @Override
    public String getDecoratedMenu() {
        return null;
    }

    @Override
    public boolean isHomePageSupported() {
        return true;
    }

    @Override
    public String getCategory() {
        return UserviewBuilderPalette.CATEGORY_GENERAL;
    }
}
