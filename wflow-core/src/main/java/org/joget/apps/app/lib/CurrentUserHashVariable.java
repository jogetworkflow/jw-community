package org.joget.apps.app.lib;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.context.ApplicationContext;

public class CurrentUserHashVariable extends DefaultHashVariablePlugin {
    private User user = null;
    
    @Override
    public String processHashVariable(String variableKey) {
        ApplicationContext appContext = AppUtil.getApplicationContext();
        WorkflowUserManager workflowUserManager = (WorkflowUserManager) appContext.getBean("workflowUserManager");

        String username = workflowUserManager.getCurrentUsername();
        String attribute = variableKey;

        if (WorkflowUserManager.ROLE_ANONYMOUS.equals(username)) {
            return "";
        }
        
        return getUserAttribute(username, attribute);
    }

    public String getName() {
        return "Current User Hash Variable";
    }

    public String getPrefix() {
        return "currentUser";
    }

    public String getVersion() {
        return "5.0.0";
    }

    public String getDescription() {
        return "";
    }

    protected String getUserAttribute(String username, String attribute) {
        String attributeValue = null;

        try {
            if (user == null) {
                ApplicationContext appContext = AppUtil.getApplicationContext();
                WorkflowUserManager workflowUserManager = (WorkflowUserManager) appContext.getBean("workflowUserManager");
                user = workflowUserManager.getCurrentUser();
            }
            
            if (user != null) {
                if (attribute.equalsIgnoreCase("roles")) {
                    Set<Role> roles = user.getRoles();
                    Set<String> values = new LinkedHashSet<String>();
                    if (roles != null && !roles.isEmpty()) {
                        for (Role r : roles) {
                            values.add(r.getId());
                        }
                    }
                    attributeValue = StringUtils.join(values, ";");
                    
                } else if (attribute.equalsIgnoreCase("fullName")) {
                    attributeValue = DirectoryUtil.getUserFullName(user);
                } else {
                    //convert first character to upper case
                    char firstChar = attribute.charAt(0);
                    firstChar = Character.toUpperCase(firstChar);
                    attribute = firstChar + attribute.substring(1, attribute.length());

                    Method method = User.class.getDeclaredMethod("get" + attribute, new Class[]{});
                    String returnResult = ((Object) method.invoke(user, new Object[]{})).toString();
                    if (returnResult == null || attribute.equals("Password")) {
                        returnResult = "";
                    }
                    attributeValue = returnResult;
                }
            }
        } catch (Exception e) {
            LogUtil.error(CurrentUserHashVariable.class.getName(), e, "Error retrieving user attribute " + attribute);
        }
        return attributeValue;
    }

    public String getLabel() {
        return "Current User Hash Variable";
    }

    public String getClassName() {
        return this.getClass().getName();
    }
    
    public String getPropertyOptions() {
        return "";
    }
    
    @Override
    public Collection<String> availableSyntax() {
        Collection<String> syntax = new ArrayList<String>();
        syntax.add("currentUser.fullName");
        syntax.add("currentUser.username");
        syntax.add("currentUser.firstName");
        syntax.add("currentUser.lastName");
        syntax.add("currentUser.email");
        syntax.add("currentUser.active");
        syntax.add("currentUser.timeZone");
        syntax.add("currentUser.locale");
        syntax.add("currentUser.roles");
        
        return syntax;
    }
}
