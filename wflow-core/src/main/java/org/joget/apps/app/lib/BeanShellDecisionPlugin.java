package org.joget.apps.app.lib;

import java.util.Map;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.workflow.model.DecisionPluginDefault;
import org.joget.apps.app.service.AppUtil;
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

        DecisionResult result = new DecisionResult();
        setProperty("result", result);
        
        return (DecisionResult) AppPluginUtil.executeScript(script, properties);
    }
    
    @Override
    public String getDeveloperMode() {
        return "advanced";
    }
    
    @Override
    public String getPluginIcon() {
        return "<i class=\"las la-code\"></i>";
    }
}
