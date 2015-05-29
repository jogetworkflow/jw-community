package org.joget.apps.userview.model;

import java.util.Map;
import org.joget.directory.model.User;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginProperty;
import org.joget.plugin.property.model.PropertyEditable;

public abstract class UserviewPermission extends ExtElement {

    private User currentUser;

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public abstract boolean isAuthorize();
}
