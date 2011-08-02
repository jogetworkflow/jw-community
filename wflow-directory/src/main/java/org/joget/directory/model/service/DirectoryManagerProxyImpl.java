package org.joget.directory.model.service;

import org.joget.directory.ext.DirectoryManagerAuthenticatorImpl;
import org.joget.commons.spring.model.Setting;
import org.joget.commons.util.CsvUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;
import org.joget.directory.model.Department;
import org.joget.directory.model.Employment;
import org.joget.directory.model.Grade;
import org.joget.directory.model.Group;
import org.joget.directory.model.Organization;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.plugin.base.PluginManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;

public class DirectoryManagerProxyImpl implements ExtDirectoryManager {

    private SetupManager setupManager;
    private PluginManager pluginManager;
    private ExtDirectoryManager defaultDirectoryManagerImpl;

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public SetupManager getSetupManager() {
        return setupManager;
    }

    public void setSetupManager(SetupManager setupManager) {
        this.setupManager = setupManager;
    }

    public ExtDirectoryManager getDefaultDirectoryManagerImpl() {
        return defaultDirectoryManagerImpl;
    }

    public void setDefaultDirectoryManagerImpl(ExtDirectoryManager defaultDirectoryManagerImpl) {
        this.defaultDirectoryManagerImpl = defaultDirectoryManagerImpl;
    }

    public boolean isExtended() {
        if (getDirectoryManagerImpl() instanceof ExtDirectoryManager) {
            return true;
        }
        return false;
    }

    public boolean isCustomDirectoryManager() {
        if (getCustomDirectoryManagerImpl() != null) {
            return true;
        }
        return false;
    }

    private DirectoryManagerAuthenticator getDirectoryManagerAuthenticator() {
        DirectoryManagerAuthenticator authenticator = (DirectoryManagerAuthenticator)pluginManager.getPlugin(DirectoryManagerAuthenticatorImpl.class.getName());
        return authenticator;
    }

    private DirectoryManager getCustomDirectoryManagerImpl() {
        try {
            Setting setting = getSetupManager().getSettingByProperty("directoryManagerImpl");

            if (setting != null && setting.getValue() != null && setting.getValue().trim().length() > 0) {

                DirectoryManagerPlugin directoryManagerPlugin = (DirectoryManagerPlugin) getPluginManager().getPlugin(setting.getValue());

                if (directoryManagerPlugin != null) {

                    //get plugin properties (if any)
                    Map propertyMap = new HashMap();
                    Setting propertySetting = getSetupManager().getSettingByProperty("directoryManagerImplProperties");

                    if (propertySetting != null && propertySetting.getValue() != null && propertySetting.getValue().trim().length() > 0) {
                        String properties = propertySetting.getValue();
                        if (!(directoryManagerPlugin instanceof PropertyEditable)) {
                            propertyMap = CsvUtil.getPluginPropertyMap(properties);
                        } else {
                            propertyMap = PropertyUtil.getPropertiesValueFromJson(properties);
                        }
                    }

                    LogUtil.debug(getClass().getName(), "DirectoryManager Plugin Found: " + setting.getValue());
                    return directoryManagerPlugin.getDirectoryManagerImpl(propertyMap);
                }
            }
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        }
        return null;
    }

    private DirectoryManager getDirectoryManagerImpl() {
        DirectoryManager customDirectoryManager = getCustomDirectoryManagerImpl();

        if (customDirectoryManager != null) {
            return customDirectoryManager;
        }
        return getDefaultDirectoryManagerImpl();
    }

    private ExtDirectoryManager getExtDirectoryManagerImpl() {
        if (getDirectoryManagerImpl() instanceof ExtDirectoryManager) {
            return (ExtDirectoryManager) getDirectoryManagerImpl();
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public Collection<Group> getGroupsByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows) {
        if (isExtended()) {
            return getExtDirectoryManagerImpl().getGroupsByOrganizationId(filterString, organizationId, sort, desc, start, rows);
        } else {
            LogUtil.debug(getClass().getName(), "getGroupsByOrganizationId in backward compatible support mode");
            return getDirectoryManagerImpl().getGroupList(filterString, sort, desc, start, rows);
        }
    }

    public Long getTotalGroupsByOrganizationId(String filterString, String organizationId) {
        if (isExtended()) {
            return getExtDirectoryManagerImpl().getTotalGroupsByOrganizationId(filterString, organizationId);
        } else {
            LogUtil.debug(getClass().getName(), "getTotalGroupsByOrganizationId in backward compatible support mode");
            return getDirectoryManagerImpl().getTotalGroups();
        }
    }

    public Collection<User> getUsers(String filterString, String organizationId, String departmentId, String gardeId, String groupId, String roleId, String active, String sort, Boolean desc, Integer start, Integer rows) {
        if (isExtended()) {
            return getExtDirectoryManagerImpl().getUsers(filterString, organizationId, departmentId, gardeId, groupId, roleId, active, sort, desc, start, rows);
        } else {
            LogUtil.debug(getClass().getName(), "getUsers in backward compatible support mode");
            return getDirectoryManagerImpl().getUserList(filterString, sort, desc, start, rows);
        }
    }

    public Long getTotalUsers(String filterString, String organizationId, String departmentId, String gardeId, String groupId, String roleId, String active) {
        if (isExtended()) {
            return getExtDirectoryManagerImpl().getTotalUsers(filterString, organizationId, departmentId, gardeId, groupId, roleId, active);
        } else {
            LogUtil.debug(getClass().getName(), "getUsers in backward compatible support mode");
            return getDirectoryManagerImpl().getTotalUsers();
        }
    }

    public Collection<User> getUsersSubordinate(String username, String sort, Boolean desc, Integer start, Integer rows) {
        return getExtDirectoryManagerImpl().getUsersSubordinate(username, sort, desc, start, rows);
    }

    public Long getTotalUsersSubordinate(String username) {
        return getExtDirectoryManagerImpl().getTotalUsersSubordinate(username);
    }

    public Department getDepartmentByName(String name) {
        return getExtDirectoryManagerImpl().getDepartmentByName(name);
    }

    public Department getParentDepartment(String id) {
        return getExtDirectoryManagerImpl().getParentDepartment(id);
    }

    public Department getParentDepartmentByName(String name) {
        return getExtDirectoryManagerImpl().getParentDepartmentByName(name);
    }

    public Collection<Department> getDepartmentsByParentId(String filterString, String parentId, String sort, Boolean desc, Integer start, Integer rows) {
        return getExtDirectoryManagerImpl().getDepartmentsByParentId(filterString, parentId, sort, desc, start, rows);
    }

    public Long getTotalDepartmentsByParentId(String filterString, String parentId) {
        return getExtDirectoryManagerImpl().getTotalDepartmentsByParentId(filterString, parentId);
    }

    public Collection<Department> getDepartmentsByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows) {
        if (isExtended()) {
            return getExtDirectoryManagerImpl().getDepartmentsByOrganizationId(filterString, organizationId, sort, desc, start, rows);
        } else {
            LogUtil.debug(getClass().getName(), "getDepartmentsByOrganizationId in backward compatible support mode");
            return getDirectoryManagerImpl().getDepartmentListByOrganization(organizationId, sort, desc, start, rows);
        }
    }

    public Long getTotalDepartmentnsByOrganizationId(String filterString, String organizationId) {
        if (isExtended()) {
            return getExtDirectoryManagerImpl().getTotalDepartmentnsByOrganizationId(filterString, organizationId);
        } else {
            LogUtil.debug(getClass().getName(), "getTotalDepartmentnsByOrganizationId in backward compatible support mode");
            return getDirectoryManagerImpl().getTotalDepartments(organizationId);
        }
    }

    public Grade getGradeByName(String name) {
        return getExtDirectoryManagerImpl().getGradeByName(name);
    }

    public Collection<Grade> getGradesByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows) {
        return getExtDirectoryManagerImpl().getGradesByOrganizationId(filterString, organizationId, sort, desc, start, rows);
    }

    public Long getTotalGradesByOrganizationId(String filterString, String organizationId) {
        return getExtDirectoryManagerImpl().getTotalGradesByOrganizationId(filterString, organizationId);
    }

    public Organization getOrganization(String id) {
        return getExtDirectoryManagerImpl().getOrganization(id);
    }

    public Organization getOrganizationByName(String name) {
        return getExtDirectoryManagerImpl().getOrganizationByName(name);
    }

    public Collection<Organization> getOrganizationsByFilter(String filterString, String sort, Boolean desc, Integer start, Integer rows) {
        return getExtDirectoryManagerImpl().getOrganizationsByFilter(filterString, sort, desc, start, rows);
    }

    public Long getTotalOrganizationsByFilter(String filterString) {
        return getExtDirectoryManagerImpl().getTotalOrganizationsByFilter(filterString);
    }

    public Collection<Employment> getEmployments(String filterString, String organizationId, String departmentId, String gradeId, String sort, Boolean desc, Integer start, Integer rows) {
        return getExtDirectoryManagerImpl().getEmployments(filterString, organizationId, departmentId, gradeId, sort, desc, start, rows);
    }

    public Long getTotalEmployments(String filterString, String organizationId, String departmentId, String gradeId) {
        return getExtDirectoryManagerImpl().getTotalEmployments(filterString, organizationId, departmentId, gradeId);
    }

    public Employment getEmployment(String id) {
        return getExtDirectoryManagerImpl().getEmployment(id);
    }

    public Collection<Group> getGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup, String sort, Boolean desc, Integer start, Integer rows) {
        return getExtDirectoryManagerImpl().getGroupsByUserId(filterString, userId, organizationId, inGroup, sort, desc, start, rows);
    }

    public Long getTotalGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup) {
        return getExtDirectoryManagerImpl().getTotalGroupsByUserId(filterString, userId, organizationId, inGroup);
    }

    public boolean authenticate(String username, String password) {
        DirectoryManagerAuthenticator authenticator = getDirectoryManagerAuthenticator();
        DirectoryManager directoryManager = getDirectoryManagerImpl();
        boolean authenticated = authenticator.authenticate(directoryManager, username, password);
        return authenticated;
    }

    public Group getGroupById(String groupId) {
        return getDirectoryManagerImpl().getGroupById(groupId);
    }

    public Group getGroupByName(String groupName) {
        return getDirectoryManagerImpl().getGroupByName(groupName);
    }

    public Collection<Group> getGroupByUsername(String username) {
        return getDirectoryManagerImpl().getGroupByUsername(username);
    }

    public Collection<Group> getGroupList() {
        return getDirectoryManagerImpl().getGroupList();
    }

    public Collection<Group> getGroupList(String nameFilter, String sort, Boolean desc, Integer start, Integer rows) {
        return getDirectoryManagerImpl().getGroupList(nameFilter, sort, desc, start, rows);
    }

    public Long getTotalGroups() {
        return getDirectoryManagerImpl().getTotalGroups();
    }

    public Collection<User> getUserByDepartmentId(String departmentId) {
        return getDirectoryManagerImpl().getUserByDepartmentId(departmentId);
    }

    public Collection<User> getUserByGradeId(String gradeId) {
        return getDirectoryManagerImpl().getUserByGradeId(gradeId);
    }

    public Collection<User> getUserByGroupId(String groupId) {
        return getDirectoryManagerImpl().getUserByGroupId(groupId);
    }

    public Collection<User> getUserByGroupName(String groupName) {
        return getDirectoryManagerImpl().getUserByGroupName(groupName);
    }

    public User getUserById(String userId) {
        return getDirectoryManagerImpl().getUserById(userId);
    }

    public Collection<User> getUserByOrganizationId(String orgaizationId) {
        return getDirectoryManagerImpl().getUserByOrganizationId(orgaizationId);
    }

    public User getUserByUsername(String username) {
        return getDirectoryManagerImpl().getUserByUsername(username);
    }

    public Collection<User> getUserList() {
        return getDirectoryManagerImpl().getUserList();
    }

    public Collection<User> getUserList(String nameFilter, String sort, Boolean desc, Integer start, Integer rows) {
        return getDirectoryManagerImpl().getUserList(nameFilter, sort, desc, start, rows);
    }

    public Long getTotalUsers() {
        return getDirectoryManagerImpl().getTotalUsers();
    }

    public boolean isUserInGroup(String username, String groupName) {
        return getDirectoryManagerImpl().isUserInGroup(username, groupName);
    }

    public Collection<Role> getUserRoles(String username) {
        return getDirectoryManagerImpl().getUserRoles(username);
    }

    public User getDepartmentHod(String departmentId) {
        return getDirectoryManagerImpl().getDepartmentHod(departmentId);
    }

    public Collection<User> getUserHod(String username) {
        return getDirectoryManagerImpl().getUserHod(username);
    }

    public Collection<User> getUserSubordinate(String username) {
        return getDirectoryManagerImpl().getUserSubordinate(username);
    }

    public Collection<User> getUserDepartmentUser(String username) {
        return getDirectoryManagerImpl().getUserDepartmentUser(username);
    }

    public Collection<User> getDepartmentUserByGradeId(String departmentId, String gradeId) {
        return getDirectoryManagerImpl().getDepartmentUserByGradeId(departmentId, gradeId);
    }

    public Department getDepartmentById(String departmentId) {
        return getDirectoryManagerImpl().getDepartmentById(departmentId);
    }

    public Collection<Department> getDepartmentList() {
        return getDirectoryManagerImpl().getDepartmentList();
    }

    public Collection<Department> getDepartmentList(String sort, Boolean desc, Integer start, Integer rows) {
        return getDirectoryManagerImpl().getDepartmentList(sort, desc, start, rows);
    }

    public Collection<Department> getDepartmentListByOrganization(String organizationId, String sort, Boolean desc, Integer start, Integer rows) {
        return getDirectoryManagerImpl().getDepartmentListByOrganization(organizationId, sort, desc, start, rows);
    }

    public Long getTotalDepartments(String organizationId) {
        return getDirectoryManagerImpl().getTotalDepartments(organizationId);
    }

    public Grade getGradeById(String gradeId) {
        return getDirectoryManagerImpl().getGradeById(gradeId);
    }

    public Collection<Grade> getGradeList() {
        return getDirectoryManagerImpl().getGradeList();
    }
}
