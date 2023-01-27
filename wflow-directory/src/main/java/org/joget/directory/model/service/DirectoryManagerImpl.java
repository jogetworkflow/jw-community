package org.joget.directory.model.service;

import org.joget.directory.model.Department;
import org.joget.directory.model.Employment;
import org.joget.directory.model.EmploymentReportTo;
import org.joget.directory.model.Grade;
import org.joget.directory.model.Group;
import org.joget.directory.model.Organization;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.joget.commons.util.StringUtil;
import org.joget.directory.dao.DepartmentDao;
import org.joget.directory.dao.EmploymentDao;
import org.joget.directory.dao.GradeDao;
import org.joget.directory.dao.GroupDao;
import org.joget.directory.dao.OrganizationDao;
import org.joget.directory.dao.RoleDao;
import org.joget.directory.dao.UserDao;

public class DirectoryManagerImpl implements ExtDirectoryManager {

    private UserDao userDao;
    private GroupDao groupDao;
    private OrganizationDao organizationDao;
    private DepartmentDao departmentDao;
    private EmploymentDao employmentDao;
    private GradeDao gradeDao;
    private RoleDao roleDao;

    public DepartmentDao getDepartmentDao() {
        return departmentDao;
    }

    public void setDepartmentDao(DepartmentDao departmentDao) {
        this.departmentDao = departmentDao;
    }

    public EmploymentDao getEmploymentDao() {
        return employmentDao;
    }

    public void setEmploymentDao(EmploymentDao employmentDao) {
        this.employmentDao = employmentDao;
    }

    public GradeDao getGradeDao() {
        return gradeDao;
    }

    public void setGradeDao(GradeDao gradeDao) {
        this.gradeDao = gradeDao;
    }

    public GroupDao getGroupDao() {
        return groupDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public OrganizationDao getOrganizationDao() {
        return organizationDao;
    }

    public void setOrganizationDao(OrganizationDao organizationDao) {
        this.organizationDao = organizationDao;
    }

    public RoleDao getRoleDao() {
        return roleDao;
    }

    public void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public Collection<Group> getGroupsByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows) {
        return getGroupDao().getGroupsByOrganizationId(filterString, organizationId, sort, desc, start, rows);
    }

    public Long getTotalGroupsByOrganizationId(String filterString, String organizationId) {
        return getGroupDao().getTotalGroupsByOrganizationId(filterString, organizationId);
    }

    public Collection<User> getUsersSubordinate(String username, String sort, Boolean desc, Integer start, Integer rows) {
        return getUserDao().getUsersSubordinate(username, sort, desc, start, rows);
    }

    public Long getTotalUsersSubordinate(String username) {
        return getUserDao().getTotalUsersSubordinate(username);
    }

    public Long getTotalUserList(String filterString) {
        return getUserDao().getTotalUsers(filterString, null, null, null, null, null, null);
    }

    public Department getDepartmentByName(String name) {
        return getDepartmentDao().getDepartmentByName(name);
    }

    public Department getParentDepartment(String id) {
        return getDepartmentDao().getParentDepartment(id);
    }

    public Department getParentDepartmentByName(String name) {
        return getDepartmentDao().getDepartmentByName(name);
    }

    public Collection<Department> getDepartmentsByParentId(String filterString, String parentId, String sort, Boolean desc, Integer start, Integer rows) {
        return getDepartmentDao().getDepartmentsByParentId(filterString, parentId, sort, desc, start, rows);
    }

    public Long getTotalDepartmentsByParentId(String filterString, String parentId) {
        return getDepartmentDao().getTotalDepartmentsByParentId(filterString, parentId);
    }

    public Collection<Department> getDepartmentsByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows) {
        return getDepartmentDao().getDepartmentsByOrganizationId(filterString, organizationId, sort, desc, start, rows);
    }

    public Long getTotalDepartmentnsByOrganizationId(String filterString, String organizationId) {
        return getDepartmentDao().getTotalDepartmentsByOrganizationId(filterString, organizationId);
    }

    public Grade getGradeByName(String name) {
        return getGradeDao().getGradeByName(name);
    }

    public Collection<Grade> getGradesByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows) {
        return getGradeDao().getGradesByOrganizationId(filterString, organizationId, sort, desc, start, rows);
    }

    public Long getTotalGradesByOrganizationId(String filterString, String organizationId) {
        return getGradeDao().getTotalGradesByOrganizationId(filterString, organizationId);
    }

    public Organization getOrganization(String id) {
        return getOrganizationDao().getOrganization(id);
    }

    public Organization getOrganizationByName(String name) {
        return getOrganizationDao().getOrganizationByName(name);
    }

    public Collection<Organization> getOrganizationsByFilter(String filterString, String sort, Boolean desc, Integer start, Integer rows) {
        return getOrganizationDao().getOrganizationsByFilter(filterString, sort, desc, start, rows);
    }

    public Long getTotalOrganizationsByFilter(String filterString) {
        return getOrganizationDao().getTotalOrganizationsByFilter(filterString);
    }

    public Employment getEmployment(String id) {
        return getEmploymentDao().getEmployment(id);
    }

    public boolean authenticate(String username, String password) {
        User user = getUserByUsername(username);

        String userPassword = (user != null) ? user.getPassword() : null;

        // temporary code to check for unencrypted password and encrypt it for comparison
        if (userPassword != null && userPassword.length() != 32) {
            userPassword = StringUtil.md5Base16(userPassword);
        }

        // compare passwords
        boolean validLogin = (user != null && userPassword != null && password != null && userPassword.equals(StringUtil.md5Base16(password)));

        if (!validLogin) {
            validLogin = (user != null && userPassword != null && user.getLoginHash().equalsIgnoreCase(password));
        }

        // check for active flag
        boolean active = (user != null && user.getActive() == User.ACTIVE);

        if (!validLogin || !active) {
            return false;
        } else {
            return true;
        }
    }

    public Group getGroupById(String groupId) {
        return getGroupDao().getGroup(groupId);
    }

    public Group getGroupByName(String groupName) {
        return getGroupDao().getGroupByName(groupName);
    }

    public Collection<Group> getGroupByUsername(String username) {
        User user = getUserDao().getUser(username);
        if (user != null) {
            return getGroupDao().getGroupsByUserId("", user.getId(), null, true, "name", false, null, null);
        }
        return null;
    }

    public Collection<Group> getGroupList() {
        return getGroupDao().getGroupsByOrganizationId(null, null, null, null, null, null);
    }

    public Collection<Group> getGroupList(String filterString, String sort, Boolean desc, Integer start, Integer rows) {
        return getGroupDao().getGroupsByOrganizationId(filterString, null, sort, desc, start, rows);
    }

    public Long getTotalGroups() {
        return getGroupDao().getTotalGroupsByOrganizationId(null, null);
    }

    public Collection<User> getUserByDepartmentId(String departmentId) {
        return getUserDao().getUsers(null, null, departmentId, null, null, null, null, "username", false, null, null);
    }

    public Collection<User> getUserByGradeId(String gradeId) {
        return getUserDao().getUsers(null, null, null, gradeId, null, null, null, "username", false, null, null);
    }

    public Collection<User> getUserByGroupId(String groupId) {
        return getUserDao().getUsers(null, null, null, null, groupId, null, null, "username", false, null, null);
    }

    public Collection<User> getUserByGroupName(String groupName) {
        Group group = getGroupDao().getGroupByName(groupName);
        if (group != null) {
            return getUserDao().getUsers(null, null, null, null, group.getId(), null, null, "username", false, null, null);
        }
        return null;
    }

    public User getUserById(String userId) {
        return getUserDao().getUserById(userId);
    }

    public User getUserByUsername(String username) {
        return getUserDao().getUser(username);
    }

    public Collection<User> getUserList() {
        return getUserDao().getUsers(null, null, null, null, null, null, null, "username", false, null, null);
    }

    public Collection<User> getUserList(String filterString, String sort, Boolean desc, Integer start, Integer rows) {
        return getUserDao().getUsers(filterString, null, null, null, null, null, null, sort, desc, start, rows);
    }

    public Long getTotalUsers() {
        return getUserDao().getTotalUsers(null, null, null, null, null, null, null);
    }

    public boolean isUserInGroup(String username, String groupName) {
        Group group = getGroupDao().getGroupByName(groupName);

        if (group != null) {
            Collection<User> users = getUserDao().getUsers(username, null, null, null, group.getId(), null, null, null, null, null, null);

            if (users != null) {
                for (User user : users) {
                    if (user.getUsername().equals(username)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public Collection<Role> getUserRoles(String username) {
        return getRoleDao().getUserRoles(username, null, null, null, null);
    }

    public User getDepartmentHod(String departmentId) {
        Department department = getDepartmentDao().getDepartment(departmentId);
        if (department != null) {
            User hod = getDepartmentHod(department);
            while (department != null && hod == null) {
                // no HOD or user is HOD, so look for HOD of parent department
                department = department.getParent();
                if (department != null) {
                    hod = getDepartmentHod(department);
                }
            }
            return hod;
        }
        return null;
    }
    
    protected User getDepartmentHod(Department department) {
        if (department != null && department.getHod() != null) {
            return employmentDao.getEmployment(department.getHod().getId()).getUser();
        }
        return null;
    }

    public Collection<User> getUserHod(String username) {
        Collection<User> userList = new ArrayList();

        User user = getUserByUsername(username);
        if (user != null && user.getEmployments() != null) {
            Collection<Employment> employments = user.getEmployments();

            if (employments != null && !employments.isEmpty()) {
                //find reportTo
                for (Employment e : employments) {
                    if (e.getEmploymentReportTo() != null) {
                        userList.add(e.getEmploymentReportTo().getReportTo().getUser());
                    }
                }

                if (userList.isEmpty()) {
                    for (Employment e : employments) {
                        Department dept = e.getDepartment();
                        if (dept != null) {
                            User hod = getDepartmentHod(dept.getId());
                            if (hod != null) {
                                userList.add(hod);
                            }
                        }
                    }
                }
            }
        }

        return userList;
    }

    public Collection<User> getUserSubordinate(String username) {
        return getUserDao().getUsersSubordinate(username, null, null, null, null);
    }

    public Collection<User> getUserDepartmentUser(String username) {
        User user = getUserDao().getUser(username);
        if (user != null && user.getEmployments() != null && !user.getEmployments().isEmpty()) {
            Collection<Employment> employments = user.getEmployments();
            Collection<User> users = new HashSet<User>();
            for (Employment e : employments) {
                if (e.getDepartment() != null) {
                    users.addAll(getUserDao().getUsers(null, null, e.getDepartment().getId(), null, null, null, null, "username", false, null, null));
                }
            }
            return users;
        }
        return null;
    }

    public Collection<User> getDepartmentUserByGradeId(String departmentId, String gradeId) {
        return getUserDao().getUsers("", null, departmentId, gradeId, null, null, null, "username", false, null, null);
    }

    public Department getDepartmentById(String departmentId) {
        return getDepartmentDao().getDepartment(departmentId);
    }

    public Collection<Department> getDepartmentList() {
        return getDepartmentDao().getDepartmentsByOrganizationId(null, null, null, null, null, null);
    }

    public Collection<Department> getDepartmentList(String sort, Boolean desc, Integer start, Integer rows) {
        return getDepartmentDao().getDepartmentsByOrganizationId(null, null, sort, desc, start, rows);
    }

    public Collection<Department> getDepartmentListByOrganization(String organizationId, String sort, Boolean desc, Integer start, Integer rows) {
        return getDepartmentDao().getDepartmentsByOrganizationId(null, organizationId, sort, desc, start, rows);
    }

    public Long getTotalDepartments(String organizationId) {
        return getDepartmentDao().getTotalDepartmentsByOrganizationId(null, organizationId);
    }

    public Grade getGradeById(String gradeId) {
        return getGradeDao().getGrade(gradeId);
    }

    public Collection<Grade> getGradeList() {
        return getGradeDao().getGradesByOrganizationId(null, null, null, null, null, null);
    }

    public Collection<User> getUsers(String filterString, String organizationId, String departmentId, String gardeId, String groupId, String roleId, String active, String sort, Boolean desc, Integer start, Integer rows) {
        return getUserDao().getUsers(filterString, organizationId, departmentId, gardeId, groupId, roleId, active, sort, desc, start, rows);
    }

    public Long getTotalUsers(String filterString, String organizationId, String departmentId, String gardeId, String groupId, String roleId, String active) {
        return getUserDao().getTotalUsers(filterString, organizationId, departmentId, gardeId, groupId, roleId, active);
    }

    public Collection<User> getUserByOrganizationId(String organizationId) {
        return getUserDao().getUsers(null, organizationId, null, null, null, null, null, "username", false, null, null);
    }

    public Collection<Employment> getEmployments(String filterString, String organizationId, String departmentId, String gradeId, String sort, Boolean desc, Integer start, Integer rows) {
        return getEmploymentDao().getEmployments(filterString, organizationId, departmentId, gradeId, sort, desc, start, rows);
    }

    public Long getTotalEmployments(String filterString, String organizationId, String departmentId, String gradeId) {
        return getEmploymentDao().getTotalEmployments(filterString, organizationId, departmentId, gradeId);
    }

    public Collection<Group> getGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup, String sort, Boolean desc, Integer start, Integer rows) {
        return getGroupDao().getGroupsByUserId(filterString, userId, organizationId, inGroup, sort, desc, start, rows);
    }

    public Long getTotalGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup) {
        return getGroupDao().getTotalGroupsByUserId(filterString, userId, organizationId, inGroup);
    }
}
