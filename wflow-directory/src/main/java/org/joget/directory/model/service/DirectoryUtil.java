package org.joget.directory.model.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.SetupManager;
import org.joget.directory.model.Department;
import org.joget.directory.model.Group;
import org.joget.directory.model.User;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * Utility methods to quick access to Directory Manager 
 * 
 */
@Service("directoryUtil")
public class DirectoryUtil implements ApplicationContextAware {
    public final static String CUSTOM_IMPL_PROPERTIES = "customDirectoryManagerImplProperties";
    public final static String IMPL_PROPERTIES = "directoryManagerImplProperties";
    public final static String ROLE_ANONYMOUS = "roleAnonymous";

    static ApplicationContext appContext;

    /**
     * Used by system to set an ApplicationContext object
     * @param ac
     * @throws BeansException 
     */
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        appContext = ac;
    }

    /**
     * Utility method to retrieve the ApplicationContext of the system
     * @return 
     */
    public static ApplicationContext getApplicationContext() {
        return appContext;
    }
    
    /**
     * Flag to indicate there is a custom Directory Manager in used
     * @return 
     */
    public static boolean isOverridden() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        if (directoryManager != null && directoryManager.getDirectoryManagerImpl() != null 
            && directoryManager.getCustomDirectoryManagerClassName() != null) {
            
            SetupManager setupManager = (SetupManager) appContext.getBean("setupManager");
            if (setupManager != null) {
                Setting setting = setupManager.getSettingByProperty("directoryManagerImpl");
                if (setting != null && setting.getValue() != null && !setting.getValue().isEmpty()) {
                    if (setting.getValue().equals(directoryManager.getCustomDirectoryManagerClassName())) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Get the class name of the custom Directory Manager in used
     * @return 
     */
    public static String getOverriddenDirectoryManagerClassName() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        return directoryManager.getCustomDirectoryManagerClassName();
    }

    /**
     * Flag to indicate the custom Directory Manager in used is implemented the 
     * ExtDirectoryManager interface
     * @return 
     */
    public static boolean isExtDirectoryManager() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        return directoryManager.isExtended();
    }

    /**
     * Flag to indicate the custom Directory Manager in used is a extend implementation of
     * the Joget default internal directory manager
     * @return 
     */
    public static boolean isCustomDirectoryManager() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        return directoryManager.isCustomDirectoryManager();
    }

    /**
     * Flag to indicate the user return by the directory manager is readonly
     * @param username
     * @return 
     */
    public static Boolean userIsReadonly(String username) {
        if (username != null && !username.isEmpty() && !ROLE_ANONYMOUS.equals(username)) {
            DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
            User user = directoryManager.getUserByUsername(username);
            if (user != null) {
                return user.getReadonly();
            }
        }
        return false;
    }
    
    /**
     * Get the user security enhancements implementation of the directory manager in used
     * @return 
     */
    public static UserSecurity getUserSecurity() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        if (directoryManager.getDirectoryManagerImpl() instanceof UserSecurityFactory) {
            UserSecurityFactory factory = (UserSecurityFactory) directoryManager.getDirectoryManagerImpl();
            return factory.getUserSecurity();
        }
        return null;
    }
    
    /**
     * Get the HTML template to inject after a login form from user security 
     * enhancements implementation.
     * @return 
     */
    public static String getLoginFormFooter() {
        UserSecurity us = getUserSecurity();
        if (us != null) {
            return us.getLoginFormFooter();
        }
        return "";
    }

    /**
     * Convenient method to retrieve all users in a map of id-value pair
     * @return 
     */
    public static Map<String, User> getUsersMap() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        Collection<User> users = directoryManager.getUserList(null, "username", false, null, null);
        Map<String, User> usersMap = new HashMap<String, User>();
        if (users != null) {
            for (User user : users) {
                usersMap.put(user.getId(), user);
            }
        }
        return usersMap;
    }

    /**
     * Convenient method to retrieve all groups in a map of id-value pair
     * @return 
     */
    public static Map<String, Group> getGroupsMap() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        Collection<Group> groups = directoryManager.getGroupList(null, "name", false, null, null);
        Map<String, Group> groupsMap = new HashMap<String, Group>();
        if (groups != null) {
            for (Group group : groups) {
                groupsMap.put(group.getId(), group);
            }
        }
        return groupsMap;
    }

    /**
     * Convenient method to retrieve all departments in a map of id-value pair
     * @return 
     */
    public static Map<String, Department> getDepartmentsMap() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        Collection<Department> departments = directoryManager.getDepartmentList("name", false, null, null);
        Map<String, Department> departmentsMap = new HashMap<String, Department>();
        if (departments != null) {
            for (Department department : departments) {
                departmentsMap.put(department.getId(), department);
            }
        }
        return departmentsMap;
    }
}
