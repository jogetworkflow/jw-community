package org.joget.apps.app.lib;

import org.joget.apps.app.dao.EnvironmentVariableDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.EnvironmentVariable;
import org.joget.apps.app.model.HashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.springframework.context.ApplicationContext;

public class EnvironmentVariableHashVariable extends HashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        AppDefinition appDef = (AppDefinition) getProperty("appDefinition");
        if (appDef != null) {
            ApplicationContext appContext = AppUtil.getApplicationContext();
            EnvironmentVariableDao environmentVariableDao = (EnvironmentVariableDao) appContext.getBean("environmentVariableDao");
            EnvironmentVariable env = environmentVariableDao.loadById(variableKey, appDef);
            if (env != null) {
                return env.getValue();
            }
        }
        return null;
    }

    public String getName() {
        return "EnvironmentVariableHashVariable";
    }

    public String getPrefix() {
        return "envVariable";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String getDescription() {
        return "";
    }
}
