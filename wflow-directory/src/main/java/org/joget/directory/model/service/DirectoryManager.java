package org.joget.directory.model.service;

import org.joget.directory.model.Department;
import org.joget.directory.model.Grade;
import org.joget.directory.model.Group;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import java.util.Collection;

/**
 * Interface of Directory Manager service to interact with Directory Source
 * 
 */
public interface DirectoryManager {

    /**
     * Method called to authenticate a particular User against the directory
     * @param username
     * @param password
     * @return boolean value reflecting if the authentication was successful
     */
    boolean authenticate(String username, String password);

    /**
     * Method called to retrieve a specific Group by id
     * @param groupId
     * @return specified Group
     */
    Group getGroupById(String groupId);

    /**
     * Method called to retrieve a specific Group by name
     * @param groupName
     * @return specified Group
     */
    Group getGroupByName(String groupName);

    /**
     * Method called to retrieve a collection of Group in which a user belongs to
     * @param username
     * @return Collection of Group objects in which the user belongs to
     */
    Collection<Group> getGroupByUsername(String username);

    /**
     * Generic method called to retrieve the entire Group listing within the directory
     * @return Collection of Group objects
     */
    Collection<Group> getGroupList();

    /**
     * Method called to retrieve the entire Group listing within the directory filtered by search parameters
     * @param nameFilter Filtering values to limit the listing
     * @param sort Field name to sort by
     * @param desc Boolean value to signify if the sorting is descending
     * @param start Starting row to list from
     * @param rows The number of rows to return
     * @return Collection of Group objects matching the search criteria
     */
    Collection<Group> getGroupList(String nameFilter, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Method call to retrieve the number of Groups available
     * @return Number of Groups in the directory
     */
    Long getTotalGroups();

    /**
     * Method called to retrieve a listing of Users by Department id
     * @param departmentId
     * @return Collection of User objects
     */
    Collection<User> getUserByDepartmentId(String departmentId);

    /**
     * Method called to retrieve a listing of Users by Grade id
     * @param gradeId
     * @return Collection of User objects
     */
    Collection<User> getUserByGradeId(String gradeId);

    /**
     * Method called to retrieve a listing of Users by Group id
     * @param groupId
     * @return Collection of User objects
     */
    Collection<User> getUserByGroupId(String groupId);

    /**
     * Method called to retrieve a listing of Users by Group name
     * @param groupName
     * @return Collection of User objects
     */
    Collection<User> getUserByGroupName(String groupName);

    /**
     * Method called to retrieve a specific User by User id
     * @param userId
     * @return User object matching the specified id
     */
    User getUserById(String userId);

    /**
     * Method called to retrieve a listing of Users by Organization id
     * @param orgaizationId
     * @return Collection of User objects
     */
    Collection<User> getUserByOrganizationId(String organizationId);

    /**
     * Method called to retrieve a specific User by Username
     * @param username
     * @return User object matching the specified Username
     */
    User getUserByUsername(String username);

    /**
     * Generic method called to retrieve the entire User listing within the directory
     * @return Collection of User objects
     */
    Collection<User> getUserList();

    /**
     * Method called to retrieve the entire User listing within the directory filtered by search parameters
     * @param nameFilter Filtering values to limit the listing
     * @param sort Field name to sort by
     * @param desc Boolean value to signify if the sorting is descending
     * @param start Start row to list from
     * @param rows The number of rows to return
     * @return Collection of User objects that matches the search criteria
     */
    Collection<User> getUserList(String nameFilter, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Method called to retrieve the number of Users available
     * @return Number of Users available in the directory
     */
    Long getTotalUsers();

    /**
     * Method called to check if a User belongs to a specific Group
     * @param username
     * @param groupName
     * @return Boolean value to signify if the User belongs in the group
     */
    boolean isUserInGroup(String username, String groupName);

    /**
     * Method called to retrieve the Roles belonging to a specific User by Username
     * @param username
     * @return Collection of Roles
     */
    Collection<Role> getUserRoles(String username);

    /**
     * Method called to return a specific HOD for a Department
     * @param departmentId Department Id to search by
     * @return User object for the head of department
     */
    User getDepartmentHod(String departmentId);

    /**
     * Method called to retrieve a listing of HODs in which a specific User reports to
     * @param username
     * @return Collection of Users
     */
    Collection<User> getUserHod(String username);

    /**
     * Method called to retrieve a listing of all the subordinates which reports to a specific User
     * @param username
     * @return Collection of Users
     */
    Collection<User> getUserSubordinate(String username);

    /**
     * Method called to retrieve a listing of Users which belongs to the same Department as the specific Username
     * @param username
     * @return Collection of Users
     */
    Collection<User> getUserDepartmentUser(String username);

    /**
     * Method called to retrieve a listing of Users which belongs to a specified Department and Grade
     * @param departmentId
     * @param gradeId
     * @return Collection of Users
     */
    Collection<User> getDepartmentUserByGradeId(String departmentId, String gradeId);

    /**
     * Method called to retrieve a specific Department by Department Id
     * @param departmentId
     * @return Department object that matches the specified Department Id
     */
    Department getDepartmentById(String departmentId);

    /**
     * Generic method called to retrieve all Departments within the directory
     * @return Collection of Departments
     */
    Collection<Department> getDepartmentList();

    /**
     * Method called to retrieve all Departments within the directory filtered by search parameters
     * @param sort Field name to sort by
     * @param desc Boolean value signifying if the sorting is in descending order
     * @param start Row to start the listing from
     * @param rows The number of rows to return
     * @return Collection of Departments that matches the search criteria
     */
    Collection<Department> getDepartmentList(String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Method called to retrieve all Departments within a specific Organization filtered by search parameters
     * @param organizationId Organization Id to search by
     * @param sort Field name to sort by
     * @param desc Boolean value signifying if the sorting is in descending order
     * @param start Row to start the listing from
     * @param rows The number of rows to return
     * @return Collection of Departments that matches the search criteria
     */
    Collection<Department> getDepartmentListByOrganization(String organizationId, String sort, Boolean desc, Integer start, Integer rows);

    /**
     * Method called to retrieve the number of Departments within the directory
     * @param organizationId
     * @return Number of available Departments within the directory
     */
    Long getTotalDepartments(String organizationId);

    /**
     * Method called to retrieve a specific Grade by Grade id
     * @param gradeId
     * @return Grade object matching the specified Grade id
     */
    Grade getGradeById(String gradeId);

    /**
     * Generic method called to retrieve all Grades within the directory
     * @return Collection of Grades within the directory
     */
    Collection<Grade> getGradeList();
}
