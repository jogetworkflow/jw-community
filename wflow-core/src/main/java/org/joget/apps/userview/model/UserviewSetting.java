package org.joget.apps.userview.model;

import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.property.model.PropertyEditable;

public class UserviewSetting extends Element implements PropertyEditable {

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
        return "[{title:'" + ResourceBundleUtil.getMessage("userview.userviewsetting.configLayout") + "', properties:[{name:'theme',label:'" + ResourceBundleUtil.getMessage("userview.userviewsetting.theme") + "',type:'elementselect',options_ajax:'[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.userview.model.UserviewTheme',url:'[CONTEXT_PATH]/web/property/json/getPropertyOptions'}]},{title:'" + ResourceBundleUtil.getMessage("userview.userviewsetting.configPermission") + "', properties:[{name:'permission',label:'" + ResourceBundleUtil.getMessage("userview.userviewsetting.permission") + "',type:'elementselect',options_ajax:'[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.userview.model.UserviewPermission',url:'[CONTEXT_PATH]/web/property/json/getPropertyOptions'}]}]";
    }

    public String getDefaultPropertyValues() {
        return "";
    }
}
