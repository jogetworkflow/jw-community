package org.joget.apps.app.lib;

import org.joget.apps.app.dao.MessageDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.model.Message;
import org.joget.apps.app.service.AppUtil;
import org.springframework.context.ApplicationContext;

public class AppMessageHashVariable extends DefaultHashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        AppDefinition appDef = (AppDefinition) getProperty("appDefinition");
        if (appDef != null) {
            ApplicationContext appContext = AppUtil.getApplicationContext();
            MessageDao messageDao = (MessageDao) appContext.getBean("messageDao");
            Message message = messageDao.loadByMessageKey(variableKey, AppUtil.getAppLocale(), appDef);
            if (message != null) {
                return message.getMessage();
            }
        }
        return null;
    }

    public String getName() {
        return "App Message Hash Variable";
    }

    public String getPrefix() {
        return "i18n";
    }

    public String getVersion() {
        return "3.0.0";
    }

    public String getDescription() {
        return "";
    }

    public String getLabel() {
        return "App Message Hash Variable";
    }

    public String getClassName() {
        return this.getClass().getName();
    }
    
    public String getPropertyOptions() {
        return "";
    }
}
