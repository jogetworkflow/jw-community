package org.joget.apps.userview.lib;

import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DatalistPermission;
import org.joget.apps.form.model.FormPermission;
import org.joget.apps.userview.model.UserviewPermission;

public class BeanShellPermission extends UserviewPermission implements FormPermission, DatalistPermission {
    @Override
    public boolean isAuthorize() {
        return executeScript();
    }
        
    protected boolean executeScript() {    
        String script = getPropertyString("script");

        setProperty("user", getCurrentUser());
        setProperty("requestParams", getRequestParameters());
        
        return (Boolean) AppPluginUtil.executeScript(script, properties);
    }

    public String getName() {
        return "Bean Shell Permission";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Bean Shell Script";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/userview/beanShellPermission.json", null, true, null);
    }
    
    @Override
    public String getDeveloperMode() {
        return "advanced";
    }
}
