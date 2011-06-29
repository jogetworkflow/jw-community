package org.joget.apps.userview.model;

import java.util.Collection;

public class Userview extends Element {

    private Collection<UserviewCategory> categories;
    private UserviewSetting setting;
    private UserviewMenu current;
    private UserviewCategory currentCategory;

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
}
