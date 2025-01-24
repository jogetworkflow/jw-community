package org.joget.apps.userview.model;

import java.util.Collection;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.lib.DefaultTheme;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginManager;

public class UserviewCategory extends Element {

    private UserviewPermission permission;
    private Collection<UserviewMenu> menus;

    public UserviewPermission getPermission() {
        return permission;
    }

    public void setPermission(UserviewPermission permission) {
        this.permission = permission;
    }

    public Collection<UserviewMenu> getMenus() {
        return menus;
    }

    public void setMenus(Collection<UserviewMenu> menus) {
        this.menus = menus;
    }

    public String getLabel() {
        return "Category";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        String megaMenuExtension = "";
        
        //Get enterprise plugin
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        Plugin plugin = pluginManager.getPlugin("org.joget.plugin.enterprise.InformationTileComponent");
        
        //If not null, it means enterprise
        if (plugin != null) {
            megaMenuExtension =  AppUtil.readPluginResource(DefaultTheme.class.getName(), "/properties/userview/userviewCategoryMegaMenu.json", null, true, null);
        }

        return AppUtil.readPluginResource(DefaultTheme.class.getName(), "/properties/userview/userviewCategory.json", new String[]{megaMenuExtension}, true, null);
    }

    public String getDefaultPropertyValues() {
        return "";
    }
}
