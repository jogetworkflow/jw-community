package org.joget.apps.userview.model;

import org.joget.apps.app.service.MobileUtil;
import org.joget.commons.util.ResourceBundleUtil;

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
        boolean mobileDisabled = MobileUtil.isMobileDisabled();
        String options = "[{title:'" + ResourceBundleUtil.getMessage("userview.userviewsetting.configLayout") + "', properties:["
                + "{name:'theme',label:'" + ResourceBundleUtil.getMessage("userview.userviewsetting.theme") + "',value:'org.joget.apps.userview.lib.DefaultTheme',type:'elementselect',options_ajax:'[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.userview.model.UserviewTheme',url:'[CONTEXT_PATH]/web/property/json/getPropertyOptions'},";
        if (!mobileDisabled) {
            options += "{name:'mobileViewDisabled',label:'" + ResourceBundleUtil.getMessage("mobile.setting.viewDisabled") + "',type:'checkbox',value :'false',options:[{value:'true',label :''}]},";
        }
        options += "{name:'userviewDescription',label:'" + ResourceBundleUtil.getMessage("ubuilder.description") + "',required:'false',type:'textarea'}"
                + "]},{title:'" + ResourceBundleUtil.getMessage("userview.userviewsetting.configPermission") + "', properties:[{name:'permission',label:'" + ResourceBundleUtil.getMessage("userview.userviewsetting.permission") + "',type:'elementselect',options_ajax:'[CONTEXT_PATH]/web/property/json/getElements?classname=org.joget.apps.userview.model.UserviewPermission',url:'[CONTEXT_PATH]/web/property/json/getPropertyOptions'}]},"
                + "{title:'" + ResourceBundleUtil.getMessage("userview.userviewsetting.loginPage") + "', properties:[{name:'loginPageTop', label: '" + ResourceBundleUtil.getMessage("userview.userviewsetting.loginPageTop") + "', type:'textarea', rows:'10', cols:'50'}, {name:'loginPageBottom', label: '" + ResourceBundleUtil.getMessage("userview.userviewsetting.loginPageBottom") + "', type:'textarea', rows:'10', cols:'50'}]}";
        if (!mobileDisabled) {
            options += ",{title:'" + ResourceBundleUtil.getMessage("mobile.setting.options") + "',properties:["
                + "{label:'" + ResourceBundleUtil.getMessage("mobile.setting.general") + "',type:'header'},"
                + "{name:'mobileCacheEnabled',label:'" + ResourceBundleUtil.getMessage("mobile.setting.cacheEnabled") + "',type:'checkbox',value :'false',options:[{value:'true',label :''}]},"
                + "{name:'mobileLoginRequired',label:'" + ResourceBundleUtil.getMessage("mobile.setting.loginRequired") + "',type:'checkbox',value :'false',options:[{value:'true',label :''}]},"
                + "{label:'" + ResourceBundleUtil.getMessage("mobile.setting.ui") + "',type:'header'},"
                + "{name:'mobileViewBackgroundUrl',label:'" + ResourceBundleUtil.getMessage("mobile.setting.viewBackgroundUrl") + "',type:'textfield',size:'50'},"
                + "{name:'mobileViewBackgroundColor',label:'" + ResourceBundleUtil.getMessage("mobile.setting.viewBackgroundColor") + "',type:'textfield'},"
                + "{name:'mobileViewBackgroundStyle',label:'" + ResourceBundleUtil.getMessage("mobile.setting.viewBackgroundStyle") + "',type:'selectbox',value :'repeat',options:[{value:'no-repeat',label :'no-repeat'},{value:'repeat',label :'repeat'},{value:'repeat-x',label :'repeat-x'},{value:'repeat-y',label :'repeat-y'},{value:'width',label :'100% width'}]},"
                + "{name:'mobileViewTranslucent',label:'" + ResourceBundleUtil.getMessage("mobile.setting.viewTranslucent") + "',type:'checkbox',value :'true',options:[{value:'true',label :''}]},"
                + "{name:'mobileViewLogoUrl',label:'" + ResourceBundleUtil.getMessage("mobile.setting.viewLogoUrl") + "',type:'textfield',size:'50'},"
                + "{name:'mobileViewLogoWidth',label:'" + ResourceBundleUtil.getMessage("mobile.setting.viewLogoWidth") + "',type:'textfield'},"
                + "{name:'mobileViewLogoHeight',label:'" + ResourceBundleUtil.getMessage("mobile.setting.viewLogoHeight") + "',type:'textfield'},"
                + "{name:'mobileViewLogoAlign',label:'" + ResourceBundleUtil.getMessage("mobile.setting.viewLogoAlign") + "',type:'selectbox',value :'left',options:[{value:'left',label :'left'},{value:'center',label :'center'},{value:'right',label :'right'}]},"
                + "{name:'mobileViewCustomCss',label:'" + ResourceBundleUtil.getMessage("mobile.setting.viewCustomCss") + "',type:'textarea',rows:'10',cols:'50'}]}";
        }
        options += "]";
        return options;
    }
}
