package org.joget.apps.userview.lib;

import bsh.Interpreter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.FormPermission;
import org.joget.apps.userview.model.UserviewPermission;

public class BeanShellPermission extends UserviewPermission implements FormPermission {
    @Override
    public boolean isAuthorize() {
        String script = getPropertyString("script");
        
        Object result = null;
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.setClassLoader(getClass().getClassLoader());
            interpreter.set("user", getCurrentUser());
            interpreter.set("requestParams", getRequestParameters());
            
            Logger.getLogger(getClass().getName()).log(Level.FINE, "Executing script " + script);
            result = interpreter.eval(script);
            return (Boolean) result;
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error executing script", e);
        }

        return false;
    }

    public String getName() {
        return "Bean Shell Permission";
    }

    public String getVersion() {
        return "3.0.0";
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
