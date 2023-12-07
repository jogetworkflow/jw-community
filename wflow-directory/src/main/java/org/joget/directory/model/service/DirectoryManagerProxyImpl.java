package org.joget.directory.model.service;

import java.util.ArrayList;
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
import org.joget.commons.util.StringUtil;
import org.joget.plugin.property.model.PropertyEditable;
import org.joget.plugin.property.service.PropertyUtil;

public class DirectoryManagerProxyImpl implements ExtDirectoryManager {

    private SetupManager setupManager;
    private PluginManager pluginManager;
    private ExtDirectoryManager defaultDirectoryManagerImpl;
    private String customDirectoryManagerClassName;

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

    public String getCustomDirectoryManagerClassName() {
        return customDirectoryManagerClassName;
    }

    public void setCustomDirectoryManagerClassName(String customDirectoryManagerClassName) {
        this.customDirectoryManagerClassName = customDirectoryManagerClassName;
    }

    public boolean isExtended() {
        if (getDirectoryManagerImpl() instanceof ExtDirectoryManager) {
            return true;
        }
        return false;
    }

    public boolean isCustomDirectoryManager() {
        if (getCustomDirectoryManagerImpl() != null && !(getCustomDirectoryManagerImpl() instanceof DirectoryManagerImpl)) {
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
            String propertiesName = DirectoryUtil.IMPL_PROPERTIES; 
            String className = "";
            
            Setting setting = getSetupManager().getSettingByProperty("directoryManagerImpl");
            if (setting != null && setting.getValue() != null && !setting.getValue().isEmpty()) {
                className = setting.getValue();
            } else {
                className = getCustomDirectoryManagerClassName();
            }
            
            if (className != null && className.equals(getCustomDirectoryManagerClassName())) {
                propertiesName = DirectoryUtil.CUSTOM_IMPL_PROPERTIES;
            }
            
            if (className != null && !className.isEmpty()) {

                DirectoryManagerPlugin directoryManagerPlugin = (DirectoryManagerPlugin) getPluginManager().getPlugin(className);

                if (directoryManagerPlugin != null) {

                    //get plugin properties (if any)
                    Map propertyMap = new HashMap();
                    Setting propertySetting = getSetupManager().getSettingByProperty(propertiesName);
                    
                    if (propertySetting == null && getCustomDirectoryManagerClassName() != null) {
                        String properties = "";
                        
                        try {
                            properties = PropertyUtil.getDefaultPropertyValues(((PropertyEditable) directoryManagerPlugin).getPropertyOptions());
                            properties = properties.replaceAll("\\\\n", "\\n");
                        } catch (Exception e){}
                        
                        propertySetting = new Setting();
                        propertySetting.setProperty(propertiesName);
                        propertySetting.setValue(properties);
                        
                        getSetupManager().saveSetting(propertySetting);
                    }
                    
                    if (propertySetting != null && propertySetting.getValue() != null && propertySetting.getValue().trim().length() > 0) {
                        String properties = propertySetting.getValue();
                        if (!(directoryManagerPlugin instanceof PropertyEditable)) {
                            properties = StringUtil.decryptContent(properties);
                            propertyMap = CsvUtil.getPluginPropertyMap(properties);
                        } else {
                            propertyMap = PropertyUtil.getPropertiesValueFromJson(properties);
                            
                            //using HashVariableSupportedMap to parse hash variable and decrypt content only when needed
                            ((PropertyEditable) directoryManagerPlugin).setProperties(propertyMap);
                            propertyMap = ((PropertyEditable) directoryManagerPlugin).getProperties();
                        }
                    }

                    LogUtil.debug(getClass().getName(), "DirectoryManager Plugin Found: " + className);
                    return directoryManagerPlugin.getDirectoryManagerImpl(propertyMap);
                }
            }
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "");
        }
        return null;
    }

    public DirectoryManager getDirectoryManagerImpl() {
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
        if (username != null && !username.isEmpty() && !DirectoryUtil.ROLE_ANONYMOUS.equals(username)) {        
            return getExtDirectoryManagerImpl().getUsersSubordinate(username, sort, desc, start, rows);
        }
        return new ArrayList<User>();
    }

    public Long getTotalUsersSubordinate(String username) {
        if (username != null && !username.isEmpty() && !DirectoryUtil.ROLE_ANONYMOUS.equals(username)) { 
            return getExtDirectoryManagerImpl().getTotalUsersSubordinate(username);
        }
        return 0L;
    }

    public Department getDepartmentByName(String name) {
        if (name != null && !name.isEmpty()) {
            return getExtDirectoryManagerImpl().getDepartmentByName(name);
        }
        return null;
    }

    public Department getParentDepartment(String id) {
        if (id != null && !id.isEmpty()) {
            return getExtDirectoryManagerImpl().getParentDepartment(id);
        }
        return null;
    }

    public Department getParentDepartmentByName(String name) {
        if (name != null && !name.isEmpty()) {
            return getExtDirectoryManagerImpl().getParentDepartmentByName(name);
        }
        return null;
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
        if (name != null && !name.isEmpty()) {
            return getExtDirectoryManagerImpl().getGradeByName(name);
        }
        return null;
    }

    public Collection<Grade> getGradesByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows) {
        return getExtDirectoryManagerImpl().getGradesByOrganizationId(filterString, organizationId, sort, desc, start, rows);
    }

    public Long getTotalGradesByOrganizationId(String filterString, String organizationId) {
        return getExtDirectoryManagerImpl().getTotalGradesByOrganizationId(filterString, organizationId);
    }

    public Organization getOrganization(String id) {
        if (id != null && !id.isEmpty()) {
            return getExtDirectoryManagerImpl().getOrganization(id);
        }
        return null;
    }

    public Organization getOrganizationByName(String name) {
        if (name != null && !name.isEmpty()) {
            return getExtDirectoryManagerImpl().getOrganizationByName(name);
        }
        return null;
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
        if (id != null && !id.isEmpty()) {
            return getExtDirectoryManagerImpl().getEmployment(id);
        }
        return null;
    }

    public Collection<Group> getGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup, String sort, Boolean desc, Integer start, Integer rows) {
        return getExtDirectoryManagerImpl().getGroupsByUserId(filterString, userId, organizationId, inGroup, sort, desc, start, rows);
    }

    public Long getTotalGroupsByUserId(String filterString, String userId, String organizationId, Boolean inGroup) {
        return getExtDirectoryManagerImpl().getTotalGroupsByUserId(filterString, userId, organizationId, inGroup);
    }

    public boolean authenticate(String username, String password) {
        if (username != null && !username.isEmpty() && !DirectoryUtil.ROLE_ANONYMOUS.equals(username)) {
            DirectoryManagerAuthenticator authenticator = getDirectoryManagerAuthenticator();
            DirectoryManager directoryManager = getDirectoryManagerImpl();
            boolean authenticated = authenticator.authenticate(directoryManager, username, password);
            return authenticated;
        }
        return false;
    }

    public Group getGroupById(String groupId) {
        if (groupId != null && !groupId.isEmpty()) {
            return getDirectoryManagerImpl().getGroupById(groupId);
        }
        return null;
    }

    public Group getGroupByName(String groupName) {
        if (groupName != null && !groupName.isEmpty()) {
            return getDirectoryManagerImpl().getGroupByName(groupName);
        }
        return null;
    }

    public Collection<Group> getGroupByUsername(String username) {
        if (username != null && !username.isEmpty() && !DirectoryUtil.ROLE_ANONYMOUS.equals(username)) {
            return getDirectoryManagerImpl().getGroupByUsername(username);
        }
        return new ArrayList<Group>();
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
        if (departmentId != null && !departmentId.isEmpty()) {
            return getDirectoryManagerImpl().getUserByDepartmentId(departmentId);
        }
        return new ArrayList<User>();
    }

    public Collection<User> getUserByGradeId(String gradeId) {
        if (gradeId != null && !gradeId.isEmpty()) {
            return getDirectoryManagerImpl().getUserByGradeId(gradeId);
        }
        return new ArrayList<User>();
    }

    public Collection<User> getUserByGroupId(String groupId) {
        if (groupId != null && !groupId.isEmpty()) {
            return getDirectoryManagerImpl().getUserByGroupId(groupId);
        }
        return new ArrayList<User>();
    }

    public Collection<User> getUserByGroupName(String groupName) {
        if (groupName != null && !groupName.isEmpty()) {
            return getDirectoryManagerImpl().getUserByGroupName(groupName);
        }
        return new ArrayList<User>();
    }

    public User getUserById(String userId) {
        if (userId != null && !userId.isEmpty() && !DirectoryUtil.ROLE_ANONYMOUS.equals(userId)) {
            return getDirectoryManagerImpl().getUserById(userId);
        }
        return null;
    }

    public Collection<User> getUserByOrganizationId(String orgaizationId) {
        if (orgaizationId != null && !orgaizationId.isEmpty()) {
            return getDirectoryManagerImpl().getUserByOrganizationId(orgaizationId);
        }
        return new ArrayList<User>();
    }

    public User getUserByUsername(String username) {
        if (username != null && !username.isEmpty()  && !DirectoryUtil.ROLE_ANONYMOUS.equals(username)) {
            return getDirectoryManagerImpl().getUserByUsername(username);
        }
        return null;
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
        if (username != null && !username.isEmpty()  && !DirectoryUtil.ROLE_ANONYMOUS.equals(username)) {
            return getDirectoryManagerImpl().isUserInGroup(username, groupName);
        }
        return false;
    }

    public Collection<Role> getUserRoles(String username) {
        if (username != null && !username.isEmpty()  && !DirectoryUtil.ROLE_ANONYMOUS.equals(username)) {
            return getDirectoryManagerImpl().getUserRoles(username);
        }
        return new ArrayList<Role>();
    }

    public User getDepartmentHod(String departmentId) {
        if (departmentId != null && !departmentId.isEmpty()) {
            return getDirectoryManagerImpl().getDepartmentHod(departmentId);
        }
        return null;
    }

    public Collection<User> getUserHod(String username) {
        if (username != null && !username.isEmpty()  && !DirectoryUtil.ROLE_ANONYMOUS.equals(username)) {
            return getDirectoryManagerImpl().getUserHod(username);
        }
        return new ArrayList<User>();
    }

    public Collection<User> getUserSubordinate(String username) {
        if (username != null && !username.isEmpty()  && !DirectoryUtil.ROLE_ANONYMOUS.equals(username)) {
            return getDirectoryManagerImpl().getUserSubordinate(username);
        }
        return new ArrayList<User>();
    }

    public Collection<User> getUserDepartmentUser(String username) {
        if (username != null && !username.isEmpty()  && !DirectoryUtil.ROLE_ANONYMOUS.equals(username)) {
            return getDirectoryManagerImpl().getUserDepartmentUser(username);
        }
        return new ArrayList<User>();
    }

    public Collection<User> getDepartmentUserByGradeId(String departmentId, String gradeId) {
        return getDirectoryManagerImpl().getDepartmentUserByGradeId(departmentId, gradeId);
    }

    public Department getDepartmentById(String departmentId) {
        if (departmentId != null && !departmentId.isEmpty()) {
            return getDirectoryManagerImpl().getDepartmentById(departmentId);
        }
        return null;
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
        if (organizationId != null && !organizationId.isEmpty()) {
            return getDirectoryManagerImpl().getTotalDepartments(organizationId);
        }
        return 0L;
    }

    public Grade getGradeById(String gradeId) {
        if (gradeId != null && !gradeId.isEmpty()) {
            return getDirectoryManagerImpl().getGradeById(gradeId);
        }
        return null;
    }

    public Collection<Grade> getGradeList() {
        return getDirectoryManagerImpl().getGradeList();
    }
}
