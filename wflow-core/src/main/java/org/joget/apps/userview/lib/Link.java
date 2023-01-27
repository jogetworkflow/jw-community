package org.joget.apps.userview.lib;

import java.util.Set;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewBuilderPalette;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.commons.util.StringUtil;

public class Link extends UserviewMenu {

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getLabel() {
        return "Link";
    }

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-link\"></i>";
    }

    @Override
    public String getRenderPage() {
        if ("iframe".equals(getPropertyString("target"))) {
            return "<iframe src ='" + getPropertyString("url") + "' style='width:100%;min-height:500px;'><p>Your browser does not support iframes.</p></iframe>";
        }
        return null;
    }

    public String getName() {
        return "Link Menu";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/link.json", null, true, "message/userview/link");
    }

    @Override
    public String getDecoratedMenu() {
        // sanitize label
        String label = getPropertyString("label");
        if (label != null) {
            label = StringUtil.stripHtmlRelaxed(label);
        }
        
        if ("blank".equals(getPropertyString("target"))) {
            return "<a onclick=\"window.open('" + getPropertyString("url") + "');return false;\" class=\"menu-link\"><span>" + label + "</span></a>";
        } else if ("self".equals(getPropertyString("target"))) {
            return "<a onclick=\"window.location = '" + getPropertyString("url") + "';return false;\" class=\"menu-link\"><span>" + label + "</span></a>";
        }
        return null;
    }

    @Override
    public boolean isHomePageSupported() {
        if ("iframe".equals(getPropertyString("target"))) {
            return true;
        }
        return false;
    }

    @Override
    public String getCategory() {
        return UserviewBuilderPalette.CATEGORY_GENERAL;
    }
    
    @Override
    public Set<String> getOfflineCacheUrls() {
        if ("true".equalsIgnoreCase(getPropertyString("enableOffline"))) {
            Set<String> urls = super.getOfflineCacheUrls();
            urls.add(getPropertyString("url"));
            return urls;
        }
        return null;
    }
}
