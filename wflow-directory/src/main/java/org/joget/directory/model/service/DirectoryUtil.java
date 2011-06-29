package org.joget.directory.model.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.directory.model.Department;
import org.joget.directory.model.Group;
import org.joget.directory.model.User;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service("directoryUtil")
public class DirectoryUtil implements ApplicationContextAware {

    static ApplicationContext appContext;

    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        appContext = ac;
    }

    public static ApplicationContext getApplicationContext() {
        return appContext;
    }

    public static boolean isExtDirectoryManager() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        return directoryManager.isExtended();
    }

    public static boolean isCustomDirectoryManager() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        return directoryManager.isCustomDirectoryManager();
    }

    public static Map<String, User> getUsersMap() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        Collection<User> users = directoryManager.getUserList(null, "username", false, null, null);
        Map<String, User> usersMap = new HashMap<String, User>();
        for (User user : users) {
            usersMap.put(user.getId(), user);
        }
        return usersMap;
    }

    public static Map<String, Group> getGroupsMap() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        Collection<Group> groups = directoryManager.getGroupList(null, "name", false, null, null);
        Map<String, Group> groupsMap = new HashMap<String, Group>();
        for (Group group : groups) {
            groupsMap.put(group.getId(), group);
        }
        return groupsMap;
    }

    public static Map<String, Department> getDepartmentsMap() {
        DirectoryManagerProxyImpl directoryManager = (DirectoryManagerProxyImpl) appContext.getBean("directoryManager");
        Collection<Department> departments = directoryManager.getDepartmentList("name", false, null, null);
        Map<String, Department> departmentsMap = new HashMap<String, Department>();
        for (Department department : departments) {
            departmentsMap.put(department.getId(), department);
        }
        return departmentsMap;
    }
}
