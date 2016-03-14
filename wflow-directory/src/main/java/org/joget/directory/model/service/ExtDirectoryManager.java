package org.joget.directory.model.service;

import java.util.Collection;
import org.joget.directory.model.Department;
import org.joget.directory.model.Employment;
import org.joget.directory.model.Grade;
import org.joget.directory.model.Group;
import org.joget.directory.model.Organization;
import org.joget.directory.model.User;

/**
 * Extended interface of Directory Manager service to interact with Directory Source
 * 
 */
public interface ExtDirectoryManager extends DirectoryManager {

    /**
     * Method called to retrieve all groups within a specific Organization filtered by search parameters
     * @param filterString
     * @param organizationId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<Group> getGroupsByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Method call to retrieve the number of Groups available within a specific Organization filtered by search parameters
     * @param filterString
     * @param organizationId
     * @return 
     */
    Long getTotalGroupsByOrganizationId(String filterString, String organizationId);

    /**
     * Method called to retrieve the users within the directory filtered by search parameters
     * @param filterString
     * @param organizationId
     * @param departmentId
     * @param gardeId
     * @param groupId
     * @param roleId
     * @param active
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<User> getUsers(String filterString, String organizationId, String departmentId, String gardeId, String groupId, String roleId, String active, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Method call to retrieve the number of Users available within the directory filtered by search parameters
     * @param filterString
     * @param organizationId
     * @param departmentId
     * @param gardeId
     * @param groupId
     * @param roleId
     * @param active
     * @return 
     */
    Long getTotalUsers(String filterString, String organizationId, String departmentId, String gardeId, String groupId, String roleId, String active);

    /**
     * Method called to retrieve a listing of all the subordinates which reports to a specific User filtered by search parameters
     * @param username
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<User> getUsersSubordinate(String username, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Method called to retrieve the number of subordinates which reports to a specific User
     * @param username
     * @return 
     */
    Long getTotalUsersSubordinate(String username);

    /**
     * Method called to retrieve the users' employment info within the directory filtered by search parameters
     * @param filterString
     * @param organizationId
     * @param departmentId
     * @param gradeId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<Employment> getEmployments(String filterString, String organizationId, String departmentId, String gradeId, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Method called to retrieve the number of users' employment info within the directory
     * @param filterString
     * @param organizationId
     * @param departmentId
     * @param gradeId
     * @return 
     */
    Long getTotalEmployments(String filterString, String organizationId, String departmentId, String gradeId);

    /**
     * Method called to retrieve a specific Department by Department Name
     * @param name
     * @return 
     */
    Department getDepartmentByName(String name);

    /**
     * Method called to retrieve a parent Department of a Department
     * @param id
     * @return 
     */
    Department getParentDepartment(String id);

    /**
     * Method called to retrieve a parent Department of a Department by Department Name
     * @param name
     * @return 
     */
    Department getParentDepartmentByName(String name);

    /**
     * Method called to retrieve all sub Departments within a specific Department filtered by search parameters
     * @param filterString
     * @param parentId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<Department> getDepartmentsByParentId(String filterString, String parentId, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Method called to retrieve the number of sub Departments within a specific Department 
     * @param filterString
     * @param parentId
     * @return 
     */
    Long getTotalDepartmentsByParentId(String filterString, String parentId);

    /**
     * Method called to retrieve all Departments within a specific Organization filtered by search parameters
     * @param filterString
     * @param organizationId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<Department> getDepartmentsByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Method called to retrieve the number of Departments within a specific Organization filtered by search parameters
     * @param filterString
     * @param organizationId
     * @return 
     */
    Long getTotalDepartmentnsByOrganizationId(String filterString, String organizationId);

    /**
     * Method called to retrieve a specific Grade by Grade Name
     * @param name
     * @return 
     */
    Grade getGradeByName(String name);

    /**
     * Method called to retrieve all Grades within a specific Organization filtered by search parameters
     * @param filterString
     * @param organizationId
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<Grade> getGradesByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Method called to retrieve the number of Grades within a specific Organization filtered by search parameters
     * 
     * @param filterString
     * @param organizationId
     * @return 
     */
    Long getTotalGradesByOrganizationId(String filterString, String organizationId);

    /**
     * Method called to retrieve a specific Organization by Id
     * @param id
     * @return 
     */
    Organization getOrganization(String id);

    /**
     * Method called to retrieve a specific Organization by Name
     * @param name
     * @return 
     */
    Organization getOrganizationByName(String name);

    /**
     * Method called to retrieve all Organizations filtered by search parameters
     * @param filterString
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<Organization> getOrganizationsByFilter(String filterString, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Method called to retrieve the number of Organizations
     * @param filterString
     * @return 
     */
    Long getTotalOrganizationsByFilter(String filterString);

    /**
     * Method called to retrieve a specific Employment info 
     * @param id
     * @return 
     */
    Employment getEmployment(String id);

    /**
     * Method called to retrieve a collection of Group in/not in which a user belongs to and filtered by search parameters
     * @param filterString
     * @param userId
     * @param organizationId
     * @param inGroup
     * @param sort
     * @param desc
     * @param start
     * @param rows
     * @return 
     */
    Collection<Group> getGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Method called to retrieve the number of Group in/not in which a user belongs to
     * @param filterString
     * @param userId
     * @param organizationId
     * @param inGroup
     * @return 
     */
    Long getTotalGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup);
}
