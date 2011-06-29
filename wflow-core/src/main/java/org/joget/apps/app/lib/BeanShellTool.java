package org.joget.apps.app.lib;

import bsh.Interpreter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joget.apps.app.service.AppUtil;
import org.joget.plugin.base.ApplicationPlugin;
import org.joget.workflow.model.ParticipantPlugin;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.base.PluginProperty;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.util.WorkflowUtil;

public class BeanShellTool implements Plugin, ApplicationPlugin, ParticipantPlugin, PropertyEditable {

    public String getName() {
        return "BeanShellTool";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String getDescription() {
        return "Executes standard Java syntax";
    }

    public PluginProperty[] getPluginProperties() {
        return null;
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
        return AppUtil.readPluginResource(getClass().getName(), "/properties/app/beanShellTool.json", null, true, "message/app/beanShellTool");
    }

    public String getDefaultPropertyValues() {
        return "";
    }

    protected Object executeScript(String script, Map properties) {
        Object result = null;
        try {
            Interpreter interpreter = new Interpreter();
            interpreter.setClassLoader(getClass().getClassLoader());
            for (Object key : properties.keySet()) {
                interpreter.set(key.toString(), properties.get(key));
            }
            Logger.getLogger(getClass().getName()).log(Level.FINE, "Executing script " + script);
            result = interpreter.eval(script);
            return result;
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error executing script", e);
            return null;
        }
    }
}
