package org.joget.apps.userview.model;

import java.util.Collection;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.lib.DefaultTheme;

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
        return AppUtil.readPluginResource(DefaultTheme.class.getName(), "/properties/userview/userviewCategory.json", null, true, null);
    }

    public String getDefaultPropertyValues() {
        return "";
    }
}
