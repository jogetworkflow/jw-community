package org.joget.apps.userview.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Userview extends Element {
    public static String USERVIEW_KEY_NAME = "keyName";
    public static String USERVIEW_KEY_VALUE = "keyValue";
    public static String USERVIEW_KEY_EMPTY_VALUE = "_";

    private Collection<UserviewCategory> categories;
    private UserviewSetting setting;
    private UserviewMenu current;
    private UserviewCategory currentCategory;
    private Map<String, Object> params;
    private Map<String, String> contentPlaceholderRules = new LinkedHashMap();
    private String permissionKey = Permission.DEFAULT;

    public Collection<UserviewCategory> getCategories() {
        return categories;
    }

    public void setCategories(Collection<UserviewCategory> categories) {
        this.categories = categories;
    }

    public UserviewSetting getSetting() {
        return setting;
    }

    public void setSetting(UserviewSetting setting) {
        this.setting = setting;
    }

    public UserviewMenu getCurrent() {
        return current;
    }

    public void setCurrent(UserviewMenu current) {
        this.current = current;
    }

    public UserviewCategory getCurrentCategory() {
        return currentCategory;
    }

    public void setCurrentCategory(UserviewCategory currentCategory) {
        this.currentCategory = currentCategory;
    }

    public String getPropertyOptions() {
        return "";
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
    
    public Object getParam (String key) {
        if (params != null) {
            return params.get(key);
        }
        return null;
    }
    
    public String getParamString (String key) {
        if (params != null && params.containsKey(key)) {
            return params.get(key).toString();
        }
        return "";
    }
    
    public void addContentPlaceholderRule(String url, String rule) {
        contentPlaceholderRules.put(url, rule);
    }
    
    public Map<String, String> getContentPlaceholderRules() {
        return contentPlaceholderRules;
    }

    public String getPermissionKey() {
        return permissionKey;
    }

    public void setPermissionKey(String permissionKey) {
        this.permissionKey = permissionKey;
    }
}
