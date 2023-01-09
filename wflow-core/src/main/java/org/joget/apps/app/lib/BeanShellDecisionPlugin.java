package org.joget.apps.app.lib;

import bsh.Interpreter;
import java.util.HashMap;
import java.util.Map;
import org.joget.workflow.model.DecisionPluginDefault;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.workflow.model.DecisionResult;

public class BeanShellDecisionPlugin extends DecisionPluginDefault {

    @Override
    public String getName() {
        return "BeanShellDecisionPlugin";
    }

    @Override
    public String getVersion() {
        return "7.0.0";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getLabel() {
        return "Bean Shell";
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/app/beanShellDecisionPlugin.json", null, true, null);
    }

    @Override
    public DecisionResult getDecision(String processDefId, String processId, String routeId, Map<String, String> variables) {
        String script = getPropertyString("script");

        Map<String, String> replaceMap = new HashMap<String, String>();
        replaceMap.put("\n", "\\\\n");

        DecisionResult result = new DecisionResult();
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.setClassLoader(getClass().getClassLoader());
            for (Object key : getProperties().keySet()) {
                interpreter.set(key.toString(), getProperty(key.toString()));
            }
            interpreter.set("result", result);
            LogUtil.debug(getClass().getName(), "Executing script " + script);
            result = (DecisionResult) interpreter.eval(script);
            
            return result;
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Error executing script");
            return null;
        }
    }
}
