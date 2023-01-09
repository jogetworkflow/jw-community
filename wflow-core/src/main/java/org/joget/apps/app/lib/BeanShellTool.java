package org.joget.apps.app.lib;

import bsh.Interpreter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.plugin.base.ApplicationPlugin;
import org.joget.plugin.base.ExtDefaultPlugin;
import org.joget.workflow.model.ParticipantPlugin;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.util.WorkflowUtil;

public class BeanShellTool extends ExtDefaultPlugin implements ApplicationPlugin, ParticipantPlugin {

    public String getName() {
        return "Bean Shell Tool";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "Executes standard Java syntax";
    }

    public Object execute(Map properties) {
        String script = (String) properties.get("script");
        WorkflowAssignment wfAssignment = (WorkflowAssignment) properties.get("workflowAssignment");

        Map<String, String> replaceMap = new HashMap<String, String>();
        replaceMap.put("\n", "\\\\n");

        script = WorkflowUtil.processVariable(script, "", wfAssignment, "", replaceMap);

        return executeScript(script, properties);
    }

    public Collection<String> getActivityAssignments(Map props) {
        String script = (String) props.get("script");
        script = WorkflowUtil.processVariable(script, "", null);
        return (Collection<String>) executeScript(script, props);
    }

    public String getLabel() {
        return "Bean Shell Tool";
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/app/beanShellTool.json", null, true, null);
    }

    protected Object executeScript(String script, Map properties) {
        Object result = null;
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.setClassLoader(getClass().getClassLoader());
            for (Object key : properties.keySet()) {
                interpreter.set(key.toString(), properties.get(key));
            }
            LogUtil.debug(getClass().getName(), "Executing script " + script);
            result = interpreter.eval(script);
            return result;
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, "Error executing script");
            return null;
        }
    }
}
