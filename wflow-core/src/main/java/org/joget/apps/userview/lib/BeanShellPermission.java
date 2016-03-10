package org.joget.apps.userview.lib;

import bsh.Interpreter;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormPermission;
import org.joget.apps.userview.model.UserviewPermission;
import org.joget.commons.util.LogUtil;

public class BeanShellPermission extends UserviewPermission implements FormPermission {
    @Override
    public boolean isAuthorize() {
        return executeScript();
    }
        
    protected boolean executeScript() {    
        String script = getPropertyString("script");
        
        Object result = null;
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.setClassLoader(getClass().getClassLoader());
            interpreter.set("user", getCurrentUser());
            interpreter.set("requestParams", getRequestParameters());
            
            result = interpreter.eval(script);
            return (Boolean) result;
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Error executing script");
        }

        return false;
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
}
