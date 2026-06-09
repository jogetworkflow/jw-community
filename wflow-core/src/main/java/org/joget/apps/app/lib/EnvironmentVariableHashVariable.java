package org.joget.apps.app.lib;

import org.joget.apps.app.dao.EnvironmentVariableDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.model.EnvironmentVariable;
import org.joget.apps.app.service.AppUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.springframework.context.ApplicationContext;

import java.util.HashSet;
import java.util.Set;

public class EnvironmentVariableHashVariable extends DefaultHashVariablePlugin {
    
    private static final ThreadLocal<Set<String>> processedHashVariablesThreadLocal = ThreadLocal.withInitial(HashSet::new);

    @Override
    public String processHashVariable(String variableKey) {
        try {
            // prevent circular referenced hash variables
            final Set<String> processedHashVariables = processedHashVariablesThreadLocal.get();
            if (processedHashVariables.contains(variableKey)) {
                throw new CircularReferencedHashVariableException("The hash variable '" + getPrefix() + "." + variableKey + "' is referenced by another hash variable in a circular manner.");
            }
            processedHashVariables.add(variableKey);

            // limit processing this hash variable type to prevent infinite recursion
            if (processedHashVariables.size() > HashVariableRecursionDepthException.MAX_RECURSION) {
                throw new HashVariableRecursionDepthException("The hash variable '" + getPrefix() + "." + variableKey + "' has exceeded the recursion depth.");
            }

            AppDefinition appDef = (AppDefinition) getProperty("appDefinition");
            if (appDef != null) {
                ApplicationContext appContext = AppUtil.getApplicationContext();
                EnvironmentVariableDao environmentVariableDao = (EnvironmentVariableDao) appContext.getBean("environmentVariableDao");
                EnvironmentVariable env = environmentVariableDao.loadById(variableKey, appDef);
                if (env != null) {
                    String value = env.getValue();
                    WorkflowAssignment wfAssignment = (WorkflowAssignment) getProperty("workflowAssignment");
                    value = AppUtil.processHashVariable(value, wfAssignment, null, null, appDef);
                    return value;
                }
            }
            return null;
        } finally {
            processedHashVariablesThreadLocal.remove();
        }
    }

    public String getName() {
        return "Environment Variable Hash Variable";
    }

    public String getPrefix() {
        return "envVariable";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "Environment Variable Hash Variable";
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }

    @Override
    public String getPropertyAssistantDefinition() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/assist/environmentVariableHashVariable.json", null, true, null);
    }
}
