package org.joget.workflow.shark;

import org.joget.directory.model.Group;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.enhydra.shark.api.admin.UserGroupManagerAdmin;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class WorkflowUserGroupManagerAdmin implements UserGroupManagerAdmin {

    @Autowired
    @Qualifier("main")
    private DirectoryManager directoryManager;

    public void addGroupToGroup(String groupName, String subgroupName)
            throws Exception {
    }

    public void addUserToGroup(String groupName, String username)
            throws Exception {
    }

    public void createGroup(String groupName, String description)
            throws Exception {
    }

    public void createUser(String groupName, String username, String password,
            String firstName, String lastName, String emailAddress)
            throws Exception {
    }

    public void moveGroup(String currentParentGroup, String newParentGroup,
            String subgroupName) throws Exception {
    }

    public void moveUser(String currentGroup, String newGroup, String username)
            throws Exception {
    }

    public void removeGroup(String groupName) throws Exception {
    }

    public void removeGroupFromGroup(String groupName, String subgroupName)
            throws Exception {
    }

    public void removeGroupTree(String groupName) throws Exception {
    }

    public void removeUser(String username) throws Exception {
    }

    public void removeUserFromGroup(String groupName, String username)
            throws Exception {
    }

    public void removeUsersFromGroupTree(String groupName) throws Exception {
    }

    public void setPassword(String username, String password) throws Exception {
    }

    public void updateGroup(String groupName, String description)
            throws Exception {
    }

    public void updateUser(String username, String firstName, String lastName,
            String emailAddress) throws Exception {
    }

    public void configure(CallbackUtilities cus) throws Exception {
    }

    public boolean doesGroupBelongToGroup(WMSessionHandle shandle,
            String groupName, String subgroupName) throws Exception {
        return false;
    }

    public boolean doesGroupExist(WMSessionHandle shandle, String groupName)
            throws Exception {
        try {
            getGroupByName(groupName);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public boolean doesUserBelongToGroup(WMSessionHandle shandle,
            String groupName, String username) throws Exception {
        return directoryManager.isUserInGroup(username, groupName);
    }

    public boolean doesUserExist(WMSessionHandle shandle, String username)
            throws Exception {
        try {
            getUserByUsername(username);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public String[] getAllGroupnames(WMSessionHandle shandle) throws Exception {
        Collection groupNames = new ArrayList();
        Collection<Group> groupList = directoryManager.getGroupList();
        for (Group group : groupList) {
            groupNames.add(group.getName());
        }
        return (String[]) groupNames.toArray(new String[0]);
    }

    public String[] getAllGroupnamesForUser(WMSessionHandle shandle,
            String userName) throws Exception {
        Collection groupNames = new ArrayList();
        Collection<Group> groupList = directoryManager.getGroupByUsername(userName);
        for (Group group : groupList) {
            groupNames.add(group.getName());
        }
        return (String[]) groupNames.toArray(new String[0]);
    }

    public String[] getAllImmediateSubgroupsForGroup(WMSessionHandle shandle,
            String groupName) throws Exception {
        return null;
    }

    public String[] getAllImmediateUsersForGroup(WMSessionHandle shandle,
            String groupName) throws Exception {
        Collection userNames = new ArrayList();
        Collection<User> userList = directoryManager.getUserByGroupName(groupName);
        for (User user : userList) {
            userNames.add(user.getUsername());
        }
        return (String[]) userNames.toArray(new String[0]);
    }

    public String[] getAllSubgroupsForGroups(WMSessionHandle shandle,
            String[] groupNames) throws Exception {
        return null;
    }

    public String[] getAllUsers(WMSessionHandle shandle) throws Exception {
        Collection userNames = new ArrayList();
        Collection<User> userList = directoryManager.getUserList();
        for (User user : userList) {
            userNames.add(user.getUsername());
        }
        return (String[]) userNames.toArray(new String[0]);
    }

    public String[] getAllUsersForGroups(WMSessionHandle shandle, String[] groupNames) throws Exception {
        Collection userList = new HashSet();
        if (groupNames != null) {
            for (int i = 0; i < groupNames.length; i++) {
                Group group = getGroupByName(groupNames[i]);
                if (group != null) {
                    Collection<User> groupUsers = directoryManager.getUserByGroupId(group.getId());
                    for (User user : groupUsers) {
                        userList.add(user.getUsername());
                    }
                }
            }
        }
        return (String[]) userList.toArray(new String[0]);
    }

    public String getGroupDescription(WMSessionHandle shandle, String groupName) throws Exception {
        return null;
    }

    public String[] getGroups(WMSessionHandle sessionHandle, String expression) throws Exception {
        return null;
    }

    public String[] getObjects(WMSessionHandle sessionHandle, String expression) throws Exception {
        return null;
    }

    public String getUserEMailAddress(WMSessionHandle shandle, String username) throws Exception {
        User user = getUserByUsername(username);
        return user.getEmail();
    }

    public String getUserFirstName(WMSessionHandle shandle, String username) throws Exception {
        User user = getUserByUsername(username);
        return user.getFirstName();
    }

    public String getUserLastName(WMSessionHandle shandle, String username) throws Exception {
        User user = getUserByUsername(username);
        return user.getLastName();
    }

    public String getUserPassword(WMSessionHandle shandle, String username) throws Exception {
        User user = getUserByUsername(username);
        return user.getPassword();
    }

    public String getUserRealName(WMSessionHandle shandle, String username) throws Exception {
        User user = getUserByUsername(username);
        return user.getFirstName();
    }

    public boolean validateUser(String username, String pwd) throws Exception {
        try {
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    protected User getUserByUsername(String username) throws SecurityException {
        return directoryManager.getUserByUsername(username);
    }

    protected Group getGroupByName(String groupName) throws SecurityException {
        return directoryManager.getGroupByName(groupName);
    }
}
