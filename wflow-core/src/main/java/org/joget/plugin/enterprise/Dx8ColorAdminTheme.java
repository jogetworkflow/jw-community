package org.joget.plugin.enterprise;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.lib.AjaxUniversalTheme;

public class Dx8ColorAdminTheme extends AjaxUniversalTheme {
    
    @Override
    public String getName() {
        return "DX 8 Color Admin Theme";
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
    public String getJsCssLib(Map<String, Object> data) {
        String jsCssLink = super.getJsCssLib(data);

        if (!"true".equalsIgnoreCase(getPropertyString("usingLocalFonts"))) {
            jsCssLink += "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n";
            jsCssLink += "<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\">\n";
            jsCssLink += "<link href=\"https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&display=swap\" rel=\"stylesheet\">";
            jsCssLink += "<link href=\"" + data.get("context_path") + "/wro/dx8ColorAdmin.min.css" + "\" rel=\"stylesheet\" />\n";
        } else {
            jsCssLink += "<link href=\"" + data.get("context_path") + "/wro/dx8ColorAdminWithFont.min.css" + "\" rel=\"stylesheet\" />\n";
        }
        return jsCssLink;
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/dx8ColorAdminTheme.json", null, true, null);
    }

    @Override
    protected String generateColorDesign() {
        //The css variables below are unique to Angle Theme, and Color Admin Theme alone
        //as specific customizations are needed to make it look apart from the other two DX8 Themes
        String colorScheme = getPropertyString("dx8colorScheme");
        String[] colors = colorScheme.split(";");

        String color = "--header-default-bgcolor:#ffffff;";
        color += "--header-link-default-color:#20252A;";
        color += "--header-button-default-color:#20252A;";
        color += "--sidebar-brand-default-color:#20252A;";

        //If horizontal menu inline is enabled, change default font color to follow header font
        if (!getPropertyString("horizontal_menu").isEmpty() && getPropertyString("horizontal_menu").equals("horizontal_inline")) {
            color += "--sidebar-default-bgcolor:#FFFFFF;";
            color += "--sidebar-menu-default-color:#20252A;";
            color += "--sidebar-icon-default-color:#20252A;";
            color += "--sidebar-active-link-default-color:#20252A;";
        } else {
            // Safely access the color array with a check
            if (colors.length > 5) {
                color += "--sidebar-default-bgcolor:" + colors[5] + ";";
            }
            color += "--sidebar-menu-default-color:#ffffff;";
            color += "--sidebar-icon-default-color:#ffffff;";
            color += "--sidebar-active-link-default-color:#ffffff;";
        }
        color += "--sidebar-active-icon-default-color:#00ACAC;";

        if (!colorScheme.isEmpty()) {
            for (int i=0; i < colors.length; i++) {
                if (!colors[i].isEmpty()) {
                    color += "--dx8theme-color"+(i+1)+":"+colors[i]+ ";";
                }
            }
        }
        return color;
    }
}
