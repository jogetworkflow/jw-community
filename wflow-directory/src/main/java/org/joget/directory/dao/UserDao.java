package org.joget.directory.dao;

import java.util.Collection;
import org.joget.directory.model.User;

public interface UserDao {

    Boolean addUser(User user);

    Boolean updateUser(User user);

    Boolean deleteUser(String username);

    Boolean assignUserToGroup(String userId, String groupId);

    Boolean unassignUserFromGroup(String userId, String groupId);

    User getUser(String username);

    User getUserById(String id);

    User getHodByDepartmentId(String departmentId);

    Collection<User> getUsers(String filterString, String organizationId, String departmentId, String gardeId, String groupId, String roleId, String active, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalUsers(String filterString, String organizationId, String departmentId, String gardeId, String groupId, String roleId, String active);

    Collection<User> getUsersNotInGroup(String filterString, String groupId, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalUsersNotInGroup(String filterString, String groupId);

    Collection<User> findUsers(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows);

    Long countUsers(String condition, Object[] params);

    Collection<User> getUsersSubordinate(String username, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalUsersSubordinate(String username);
}
