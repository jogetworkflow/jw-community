package org.joget.apps.userview.lib;

import org.joget.apps.form.model.FormPermission;
import org.joget.apps.userview.model.UserviewPermission;
import org.joget.directory.model.User;

public class LoggedInUserPermission extends UserviewPermission implements FormPermission {

    @Override
    public boolean isAuthorize() {
        User user = getCurrentUser();
        if (user != null) {
            return true;
        }
        return false;
    }

    public String getName() {
        return "Logged In User Permission";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Logged In User";
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
    
}
