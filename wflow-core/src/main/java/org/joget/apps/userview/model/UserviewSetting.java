package org.joget.apps.userview.model;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.lib.DefaultTheme;

public class UserviewSetting extends Element {

    private UserviewTheme theme;
    private UserviewPermission permission;

    public UserviewTheme getTheme() {
        return theme;
    }

    public void setTheme(UserviewTheme theme) {
        this.theme = theme;
    }

    public UserviewPermission getPermission() {
        return permission;
    }

    public void setPermission(UserviewPermission permission) {
        this.permission = permission;
    }

    public String getLabel() {
        return "Layout";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        String settingOptions = AppUtil.readPluginResource(DefaultTheme.class.getName(), "/properties/userview/userviewSetting.json", null, true, "message/userview/userviewSetting");
        return settingOptions;
    }

    @Override
    public Object getProperty(String property) {
        if (property.startsWith("mobile") && getTheme().getProperties().containsKey(property)) {
            return getTheme().getProperty(property);
        }
        return super.getProperty(property);
    }

    @Override
    public String getPropertyString(String property) {
        if (property.startsWith("mobile") && getTheme().getProperties().containsKey(property)) {
            return getTheme().getPropertyString(property);
        }
        return super.getPropertyString(property);
    }
}
