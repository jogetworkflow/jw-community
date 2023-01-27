package org.joget.directory.dao;

import java.util.Collection;
import org.joget.directory.model.Role;

public interface RoleDao {

    Boolean addRole(Role role);

    Boolean updateRole(Role role);

    Boolean deleteRole(String id);

    Role getRole(String id);

    Role getRoleByName(String name);

    Collection<Role> getRoles(String filterString, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalRoles(String filterString);

    Collection<Role> getUserRoles(String username, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalUserRoles(String username);

    Collection<Role> findRoles(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows);

    Long countRoles(String condition, Object[] params);
}
