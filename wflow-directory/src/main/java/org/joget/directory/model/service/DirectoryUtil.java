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

@Service("directoryUtil")
public class DirectoryUtil implements ApplicationContextAware {
    public final static String CUSTOM_IMPL_PROPERTIES = "customDirectoryManagerImplProperties";
    public final static String IMPL_PROPERTIES = "directoryManagerImplProperties";
    public final static String ROLE_ANONYMOUS = "roleAnonymous";

    static ApplicationContext appContext;

    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        appContext = ac;
    }

    public static ApplicationContext getApplicationContext() {
        return appContext;
    }
    
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
    
    public static String getOverriddenDirectoryManagerClassName() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        return directoryManager.getCustomDirectoryManagerClassName();
    }

    public static boolean isExtDirectoryManager() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        return directoryManager.isExtended();
    }

    public static boolean isCustomDirectoryManager() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        return directoryManager.isCustomDirectoryManager();
    }

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
    
    public static UserSecurity getUserSecurity() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        if (directoryManager.getDirectoryManagerImpl() instanceof UserSecurityFactory) {
            UserSecurityFactory factory = (UserSecurityFactory) directoryManager.getDirectoryManagerImpl();
            return factory.getUserSecurity();
        }
        return null;
    }
    
    public static String getLoginFormFooter() {
        UserSecurity us = getUserSecurity();
        if (us != null) {
            return us.getLoginFormFooter();
        }
        return "";
    }

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
