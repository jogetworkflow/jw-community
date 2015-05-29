package org.joget.directory.model.service;

import java.util.Collection;
import org.joget.directory.model.Department;
import org.joget.directory.model.Employment;
import org.joget.directory.model.Grade;
import org.joget.directory.model.Group;
import org.joget.directory.model.Organization;
import org.joget.directory.model.User;

public interface ExtDirectoryManager extends DirectoryManager {

    Collection<Group> getGroupsByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalGroupsByOrganizationId(String filterString, String organizationId);

    Collection<User> getUsers(String filterString, String organizationId, String departmentId, String gardeId, String groupId, String roleId, String active, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalUsers(String filterString, String organizationId, String departmentId, String gardeId, String groupId, String roleId, String active);

    Collection<User> getUsersSubordinate(String username, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalUsersSubordinate(String username);

    Collection<Employment> getEmployments(String filterString, String organizationId, String departmentId, String gradeId, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalEmployments(String filterString, String organizationId, String departmentId, String gradeId);

    Department getDepartmentByName(String name);

    Department getParentDepartment(String id);

    Department getParentDepartmentByName(String name);

    Collection<Department> getDepartmentsByParentId(String filterString, String parentId, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalDepartmentsByParentId(String filterString, String parentId);

    Collection<Department> getDepartmentsByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalDepartmentnsByOrganizationId(String filterString, String organizationId);

    Grade getGradeByName(String name);

    Collection<Grade> getGradesByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalGradesByOrganizationId(String filterString, String organizationId);

    Organization getOrganization(String id);

    Organization getOrganizationByName(String name);

    Collection<Organization> getOrganizationsByFilter(String filterString, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalOrganizationsByFilter(String filterString);

    Employment getEmployment(String id);

    Collection<Group> getGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup);
}
