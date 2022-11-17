package org.joget.apps.app.lib;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.directory.model.service.DirectoryUtil;
import org.springframework.context.ApplicationContext;

public class UserHashVariable extends DefaultHashVariablePlugin {
    private Map<String, User> userCache = new HashMap<String, User>();
    
    @Override
    public String processHashVariable(String variableKey) {
        //if variableKey contains unprocessing hash variable as username
        if (variableKey.startsWith("{") && variableKey.contains("}")) {
            return null;
        }
        
        String username = variableKey;
        String attribute = "";
        for (String v : availableSyntax()) {
            v = v.replaceAll("user.USERNAME", "");
            if (username.contains(v)) {
                username = username.replaceAll(v, "");
                attribute = v.substring(1);
            }
        }
        
        return getUserAttribute(username, attribute);
    }

    public String getName() {
        return "User Hash Variable";
    }

    public String getPrefix() {
        return "user";
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
            User user = userCache.get(username);
            if (user == null) {
                ApplicationContext appContext = AppUtil.getApplicationContext();
                DirectoryManager directoryManager = (DirectoryManager) appContext.getBean("directoryManager");
                user = directoryManager.getUserByUsername(username);
                userCache.put(username, user);
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
    
    @Override
    public Collection<String> availableSyntax() {
        Collection<String> syntax = new ArrayList<String>();
        syntax.add("user.USERNAME.fullName");
        syntax.add("user.USERNAME.firstName");
        syntax.add("user.USERNAME.lastName");
        syntax.add("user.USERNAME.email");
        syntax.add("user.USERNAME.active");
        syntax.add("user.USERNAME.timeZone");
        syntax.add("user.USERNAME.locale");
        syntax.add("user.USERNAME.roles");
        
        return syntax;
    }
}
