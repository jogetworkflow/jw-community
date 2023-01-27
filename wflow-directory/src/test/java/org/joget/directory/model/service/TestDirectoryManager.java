package org.joget.directory.model.service;

import org.joget.directory.model.Group;
import org.joget.directory.model.Department;
import org.joget.directory.model.User;
import org.joget.directory.model.Organization;
import java.util.Collection;
import org.joget.commons.util.LogUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import org.joget.directory.dao.DepartmentDao;
import org.joget.directory.dao.EmploymentDao;
import org.joget.directory.dao.GroupDao;
import org.joget.directory.dao.OrganizationDao;
import org.joget.directory.dao.UserDao;
import org.joget.directory.model.Employment;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:directoryApplicationContext.xml"})
public class TestDirectoryManager {

    public static final String TEST_ORGANIZATION = "TEST_ORGANIZATION";
    public static final String TEST_DEPARTMENT_PARENT = "TEST_DEPARTMENT_PARENT";
    public static final String TEST_DEPARTMENT_CHILD = "TEST_DEPARTMENT_CHILD";
    public static final String TEST_GROUP = "TEST_GROUP";
    public static final String TEST_USER = "TEST_USER";
    public static final String TEST_DEPARTMENT_PARENT_HOD = "TEST_DEPARTMENT_PARENT_HOD";
    public static final String TEST_DEPARTMENT_CHILD_HOD = "TEST_DEPARTMENT_CHILD_HOD";
    public static final String TEST_USER_HOD = "TEST_USER_HOD";
    
    @Autowired
    @Qualifier("default")
    private DirectoryManager directoryManager;
    
    @Autowired
    private OrganizationDao organizationDao;
    
    @Autowired
    private DepartmentDao departmentDao;
    
    @Autowired
    private EmploymentDao employmentDao;
    
    @Autowired
    private UserDao userDao;
    
    @Autowired
    private GroupDao groupDao;
    
    @Before
    public void setUp() {
        addOrganization(TEST_ORGANIZATION);
        addDepartment(TEST_DEPARTMENT_PARENT);
        addDepartment(TEST_DEPARTMENT_CHILD);
        addGroup(TEST_GROUP);
        addUser(TEST_USER);
        addUser(TEST_DEPARTMENT_PARENT_HOD);
        addUser(TEST_DEPARTMENT_CHILD_HOD);
        addUser(TEST_USER_HOD);
    }

    @After
    public void tearDown() {
        deleteDepartment(TEST_DEPARTMENT_CHILD);
        deleteDepartment(TEST_DEPARTMENT_PARENT);
        deleteOrganization(TEST_ORGANIZATION);
        deleteGroup(TEST_GROUP);
        deleteUser(TEST_USER);
        deleteUser(TEST_DEPARTMENT_PARENT_HOD);
        deleteUser(TEST_DEPARTMENT_CHILD_HOD);
        deleteUser(TEST_USER_HOD);
    }

    @Test
    @Rollback(true)
    public void testUsersAndGroups() {
                
        // assign user to group
        LogUtil.info(getClass().getName(), "testUsersAndGroups: assign user to group");
        userDao.assignUserToGroup(TEST_USER, TEST_GROUP);
        
        // verify user
        LogUtil.info(getClass().getName(), "testUsersAndGroups: verify user");
        User user = directoryManager.getUserByUsername(TEST_USER);
        Assert.isTrue(TEST_USER.equals(user.getFirstName()));

        // verify group
        LogUtil.info(getClass().getName(), "testUsersAndGroups: verify group");
        Group group = null;
        Collection<Group> groupList = directoryManager.getGroupByUsername(TEST_USER);
        if (!groupList.isEmpty()) {
            group = groupList.iterator().next();
        }
        Assert.isTrue(group != null && TEST_GROUP.equals(group.getId()));

        // unassign user from group
        LogUtil.info(getClass().getName(), "testUsersAndGroups: unassign user from group");
        userDao.unassignUserFromGroup(TEST_USER, TEST_GROUP);
        groupList = directoryManager.getGroupByUsername(TEST_USER);
        Assert.isTrue(groupList == null || groupList.isEmpty());

        // reassign user to group
        LogUtil.info(getClass().getName(), "testUsersAndGroups: reassign user to group");
        userDao.assignUserToGroup(TEST_USER, TEST_GROUP);
    }
    
    @Test
    @Rollback(true)
    public void testOrganizationChart() {
        
        // assign parent department to organization
        LogUtil.info(getClass().getName(), "testOrganizationChart: assign parent department to organization");
        Department dept = directoryManager.getDepartmentById(TEST_DEPARTMENT_PARENT);
        Organization organization = organizationDao.getOrganization(TEST_ORGANIZATION);
        dept.setOrganization(organization);
        departmentDao.updateDepartment(dept);
        Department loadedDept = directoryManager.getDepartmentById(dept.getId());
        Assert.isTrue(loadedDept.getOrganization().getId().equals(organization.getId()));

        // assign sub-department to parent and organization
        LogUtil.info(getClass().getName(), "testOrganizationChart: assign sub-department to parent and organization");
        Department child = directoryManager.getDepartmentById(TEST_DEPARTMENT_CHILD);
        child.setOrganization(organization);
        child.setParent(loadedDept);
        departmentDao.updateDepartment(child);
        Collection<Department> subDepartments = departmentDao.getDepartmentsByParentId(null, TEST_DEPARTMENT_PARENT, null, null, null, null);
        Assert.isTrue(((Department) subDepartments.iterator().next()).getId().equals(child.getId()));

        // assign dept HOD
        LogUtil.info(getClass().getName(), "testOrganizationChart: assign dept HOD");
        addEmployment(TEST_DEPARTMENT_PARENT_HOD, TEST_DEPARTMENT_PARENT, TEST_ORGANIZATION);
        employmentDao.assignUserAsDepartmentHOD(TEST_DEPARTMENT_PARENT_HOD, TEST_DEPARTMENT_PARENT);
        addEmployment(TEST_DEPARTMENT_CHILD_HOD, TEST_DEPARTMENT_CHILD, TEST_ORGANIZATION);
        employmentDao.assignUserAsDepartmentHOD(TEST_DEPARTMENT_CHILD_HOD, TEST_DEPARTMENT_CHILD);

        // assign user to dept
        LogUtil.info(getClass().getName(), "testOrganizationChart: assign user to dept");
        addEmployment(TEST_USER, TEST_DEPARTMENT_CHILD, TEST_ORGANIZATION);
        Collection<User> userHodList = directoryManager.getUserHod(TEST_USER);
        String usernameHod = null;
        if (userHodList != null && !userHodList.isEmpty()) {
            User userHod = userHodList.iterator().next();
            usernameHod = userHod.getUsername();
        }
        Assert.isTrue(TEST_DEPARTMENT_CHILD_HOD.equals(usernameHod));        
        
        // unassign dept HOD
        LogUtil.info(getClass().getName(), "testOrganizationChart: unassign dept HOD");
        employmentDao.unassignUserAsDepartmentHOD(TEST_DEPARTMENT_CHILD_HOD, TEST_DEPARTMENT_CHILD);
        userHodList = directoryManager.getUserHod(TEST_USER);
        usernameHod = null;
        if (userHodList != null && !userHodList.isEmpty()) {
            User userHod = userHodList.iterator().next();
            usernameHod = userHod.getUsername();
        }
        Assert.isTrue(TEST_DEPARTMENT_PARENT_HOD.equals(usernameHod));        
        
        
        // set user direct report to HOD
        LogUtil.info(getClass().getName(), "testOrganizationChart: set user direct report to HOD");
        addEmployment(TEST_USER_HOD, TEST_DEPARTMENT_CHILD, TEST_ORGANIZATION);
        employmentDao.assignUserReportTo(TEST_USER, TEST_USER_HOD);
        userHodList = directoryManager.getUserHod(TEST_USER);
        User userHod = userHodList.iterator().next();
        Assert.isTrue(TEST_USER_HOD.equals(userHod.getUsername()));        
    }

    @Test
    @Rollback(true)
    public void testDeletion() {
        // delete user
        LogUtil.info(getClass().getName(), "testDeletion: delete user");
        userDao.deleteUser(TEST_DEPARTMENT_CHILD_HOD);
        User testUser = directoryManager.getUserByUsername(TEST_DEPARTMENT_CHILD_HOD);
        Assert.isTrue(testUser == null);
        
        // delete department
        LogUtil.info(getClass().getName(), "testDeletion: delete department");
        departmentDao.deleteDepartment(TEST_DEPARTMENT_CHILD);
        Department testDept = directoryManager.getDepartmentById(TEST_DEPARTMENT_CHILD);
        Assert.isTrue(testDept == null);
        
        // delete organization
        LogUtil.info(getClass().getName(), "testDeletion: delete organization");
        organizationDao.deleteOrganization(TEST_ORGANIZATION);
        Organization testOrg = organizationDao.getOrganization(TEST_ORGANIZATION);
        Assert.isTrue(testOrg == null);
    }
    
    protected void addOrganization(String id) {
        LogUtil.info(getClass().getName(), "addOrganization");
        Organization organization = new Organization();
        organization.setId(id);
        organization.setName(id);
        organization.setDescription(id);
        organizationDao.addOrganization(organization);
    }

    protected void addDepartment(String id) {
        LogUtil.info(getClass().getName(), "addDepartment");
        Department department = new Department();
        department.setId(id);
        department.setName(id);
        department.setDescription(id);
        departmentDao.addDepartment(department);
    }

    protected void addGroup(String id) {
        LogUtil.info(getClass().getName(), "addGroup");
        Group group = new Group();
        group.setId(id);
        group.setName(id);
        group.setDescription(id);
        groupDao.addGroup(group);
    }

    protected void addUser(String username) {
        LogUtil.info(getClass().getName(), "addUser");
        User user = new User();
        user.setId(username);
        user.setUsername(username);
        user.setFirstName(username);
        userDao.addUser(user);
    }

    protected void addEmployment(String username, String departmentId, String organizationId) {
        LogUtil.info(getClass().getName(), "addEmployment");
        Employment employment = new Employment();
        employment.setUserId(username);
        employment.setEmployeeCode(username);
        employment.setDepartmentId(departmentId);
        employmentDao.updateEmployment(employment);
    }

    protected void deleteOrganization(String id) {
        LogUtil.info(getClass().getName(), "deleteOrganization");
        organizationDao.deleteOrganization(id);
    }

    protected void deleteDepartment(String id) {
        LogUtil.info(getClass().getName(), "deleteDepartment");
        departmentDao.deleteDepartment(id);
    }

    protected void deleteGroup(String id) {
        LogUtil.info(getClass().getName(), "deleteGroup");
        groupDao.deleteGroup(id);
    }

    protected void deleteUser(String username) {
        LogUtil.info(getClass().getName(), "deleteUser");
        userDao.deleteUser(username);
    }
    
}
