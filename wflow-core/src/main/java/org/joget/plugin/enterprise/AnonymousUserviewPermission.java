package org.joget.plugin.enterprise;

import org.joget.apps.datalist.model.DatalistPermission;
import org.joget.apps.userview.model.UserviewPermission;
import org.joget.apps.form.model.FormPermission;

public class AnonymousUserviewPermission extends UserviewPermission implements FormPermission, DatalistPermission {
    public String getName() {
        return "Anonymous Userview Permission";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Is Anonymous";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
    
    @Override
    public boolean isAuthorize() {
        return getCurrentUser() == null;
    }
}
