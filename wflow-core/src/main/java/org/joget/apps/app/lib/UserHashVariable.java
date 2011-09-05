package org.joget.apps.app.lib;

import java.lang.reflect.Method;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.springframework.context.ApplicationContext;

public class UserHashVariable extends DefaultHashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        String username = variableKey.substring(0, variableKey.indexOf("."));
        String attribute = variableKey.substring(variableKey.indexOf(".") + 1);

        return getUserAttribute(username, attribute);
    }

    public String getName() {
        return "User Hash Variable";
    }

    public String getPrefix() {
        return "user.";
    }

    public String getVersion() {
        return "1.0.0";
    }

    public String getDescription() {
        return "";
    }

    protected String getUserAttribute(String username, String attribute) {
        String attributeValue = null;

        try {
            ApplicationContext appContext = AppUtil.getApplicationContext();
            DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
            User user = directoryManager.getUserByUsername(username);

            if (user != null) {
                //convert first character to upper case
                char firstChar = attribute.charAt(0);
                firstChar = Character.toUpperCase(firstChar);
                attribute = firstChar + attribute.substring(1, attribute.length());

                Method method = User.class.getDeclaredMethod("get" + attribute, new Class[]{});
                String returnResult = (String) method.invoke(user, new Object[]{});
                if (returnResult == null || attribute.equals("Password")) {
                    returnResult = "";
                }

                attributeValue = returnResult;
            }
        } catch (Exception e) {
            LogUtil.error(UserHashVariable.class.getName(), e, "Error retrieving user attribute " + attribute);
        }
        return attributeValue;
    }

    public String getLabel() {
        return "User Hash Variable";
    }

    public String getClassName() {
        return this.getClass().getName();
    }

    public String getPropertyOptions() {
        return "";
    }
}
