package org.joget.apps.app.lib;

import java.lang.reflect.Method;
import org.joget.apps.app.model.HashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.context.ApplicationContext;

public class PerformerHashVariable extends HashVariablePlugin {

    @Override
    public String processHashVariable(String variableKey) {
        WorkflowAssignment wfAssignment = (WorkflowAssignment) this.getProperty("workflowAssignment");
        if (wfAssignment != null) {
            try {
                ApplicationContext appContext = AppUtil.getApplicationContext();
                WorkflowManager workflowManager = (WorkflowManager) appContext.getBean("workflowManager");

                String username = workflowManager.getUserByProcessIdAndActivityDefId(wfAssignment.getProcessDefId(), wfAssignment.getProcessId(), variableKey.substring(0, variableKey.indexOf(".")));

                if (username != null && username.trim().length() > 0) {

                    String attribute = variableKey.substring(variableKey.indexOf(".") + 1);
                    String returnResult = getUserAttribute(username, attribute);
                    if (returnResult != null) {
                        return returnResult;
                    }

                }
            } catch (Exception e) {
                LogUtil.error(PerformerHashVariable.class.getName(), e, "");
            }
        }
        return null;
    }

    public String getName() {
        return "PerformerHashVariable";
    }

    public String getPrefix() {
        return "performer";
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
            LogUtil.error(PerformerHashVariable.class.getName(), e, "Error retrieving user attribute " + attribute);
        }
        return attributeValue;
    }
}
