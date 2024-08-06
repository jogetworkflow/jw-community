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
        String color = "--header-default-bgcolor:#ffffff;";
        color += "--header-button-default-color: #20252a;";
        color += "--header-link-default-color: #20252a;";

        //For sidebar
        color += "--sidebar-default-bgcolor:#2D353C;";
        color += "--sidebar-menu-default-bgcolor:#2D353C;";
        color += "--sidebar-menu-default-color:#FFFFFF;";
        color += "--sidebar-icon-default-color:#ffffff;";

        color += "--sidebar-active-link-default-bgcolor:#20252a;";
        color += "--sidebar-active-link-default-color:#FFFFFF;";
        color += "--sidebar-active-icon-default-color:#00ACAC;";
        color += "--sidebar-badge-default-bgcolor: #00ACAC;";
        color += "--sidebar-badge-default-color: #FFFFFF;";
        color += "--sidebar-scrollbar-thumb-default-color: #999999;";

        return color;
    }
}
