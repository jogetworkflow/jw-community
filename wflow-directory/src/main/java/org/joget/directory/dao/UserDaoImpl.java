package org.joget.directory.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Department;
import org.joget.directory.model.Employment;
import org.joget.directory.model.Group;
import org.joget.directory.model.Role;
import org.joget.directory.model.User;
import org.joget.directory.model.service.DirectoryUtil;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.context.ApplicationContext;

public class UserDaoImpl extends AbstractSpringDao implements UserDao {

    private GroupDao groupDao;
    private RoleDao roleDao;
    private EmploymentDao employmentDao;
    private DepartmentDao departmentDao;

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

    public GroupDao getGroupDao() {
        return groupDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public RoleDao getRoleDao() {
        return roleDao;
    }

    public void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public Boolean addUser(User user) {
        try {
            
            adminRoleFilter(user);
            
            save("User", user);
            return true;
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Add User Error!");
            return false;
        }
    }

    public Boolean updateUser(User user) {
        try {
            adminRoleFilter(user);
            
            merge("User", user);
            return true;
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Update User Error!");
            return false;
        }
    }

    public Boolean updateUserEmploymentInfo(String username, Employment info) {
        try {
            User user = getUser(username);
            if (user != null) {
                if (user.getEmployments() != null && user.getEmployments().size() > 0) {
                    Employment employment = (Employment) user.getEmployments().iterator().next();
                    info.setId(employment.getId());
                    getEmploymentDao().updateEmployment(info);
                } else {
                    user.getEmployments().add(info);
                    saveOrUpdate("User", user);
                }
            }

            return true;
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Update User Employment Info Error!");
            return false;
        }
    }

    public Boolean deleteUser(String username) {
        try {
            User user = getUser(username);
            if (user != null) {
                Set<Group> groups = user.getGroups();
                Set<Role> roles = user.getRoles();
                Set<Employment> employments = user.getEmployments();

                if (groups != null && groups.size() > 0) {
                    user.getGroups().removeAll(groups);
                }
                if (roles != null && roles.size() > 0) {
                    user.getRoles().removeAll(roles);
                }
                if (employments != null && employments.size() > 0) {
                    for (Employment e : employments) {
                        getEmploymentDao().deleteEmployment(e.getId());
                    }
                    employments.clear();
                }
                delete("User", user);
                
                UserMetaDataDao umdDao = (UserMetaDataDao) DirectoryUtil.getApplicationContext().getBean("userMetaDataDao");
                umdDao.deleteUserMetaDatas(username);
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Delete User Error!");
            return false;
        }
    }

    public User getUser(String username) {
        try {
            User user = new User();
            user.setUsername(username);
            List users = findByExample("User", user);

            if (users.size() > 0) {
                return (User) users.get(0);
            }
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Get User Error!");
        }

        return null;
    }

    public User getUserById(String id) {
        try {
            return (User) find("User", id);
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Get User By Id Error!");
            return null;
        }
    }

    public User getHodByDepartmentId(String departmentId) {
        try {
            if (departmentId != null) {
                Department department = departmentDao.getDepartment(departmentId);
                if (department != null && department.getHod() != null) {
                    Employment employment = department.getHod();
                    if (employment.getUser() != null) {
                        return getUserById(employment.getUserId());
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Get Hod By Department Id Error!");
        }

        return null;
    }

    public Collection<User> findUsers(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            return find("User", condition, params, sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Find Users Error!");
        }

        return null;
    }

    public Long countUsers(String condition, Object[] params) {
        try {
            return count("User", condition, params);
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Count Users Error!");
        }

        return 0L;
    }

    public Collection<User> getUsers(String filterString, String organizationId, String departmentId, String gardeId, String groupId, String roleId, String active, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.username like ? or e.firstName like ? or e.lastName like ? or e.email like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            if (organizationId != null || departmentId != null || gardeId != null) {
                condition += " and e.id in (select u.id from User u join u.employments em where 1=1";
                if (organizationId != null) {
                    condition += " and em.organization.id = ?";
                    param.add(organizationId);
                }
                if (departmentId != null) {
                    condition += " and em.department.id = ?";
                    param.add(departmentId);
                }
                if (gardeId != null) {
                    condition += " and em.grade.id = ?";
                    param.add(gardeId);
                }
                condition += ")";
            }
            if (groupId != null) {
                condition += " and e.id in (select u.id from User u join u.groups g where g.id = ?)";
                param.add(groupId);
            }
            if (roleId != null) {
                condition += " and e.id in (select u.id from User u join u.roles r where r.id = ?)";
                param.add(roleId);
            }
            if (active != null) {
                condition += " and e.active = ?";
                param.add(("1".equals(active) ? 1 : 0));
            }

            return find("User", condition, param.toArray(), sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Get Users Error!");
        }

        return null;
    }

    public Long getTotalUsers(String filterString, String organizationId, String departmentId, String gardeId, String groupId, String roleId, String active) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.username like ? or e.firstName like ? or e.lastName like ? or e.email like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            if (organizationId != null || departmentId != null || gardeId != null) {
                condition += " and e.id in (select u.id from User u join u.employments em where 1=1";
                if (organizationId != null) {
                    condition += " and em.organization.id = ?";
                    param.add(organizationId);
                }
                if (departmentId != null) {
                    condition += " and em.department.id = ?";
                    param.add(departmentId);
                }
                if (gardeId != null) {
                    condition += " and em.grade.id = ?";
                    param.add(gardeId);
                }
                condition += ")";
            }
            if (groupId != null) {
                condition += " and e.id in (select u.id from User u join u.groups g where g.id = ?)";
                param.add(groupId);
            }
            if (roleId != null) {
                condition += " and e.id in (select u.id from User u join u.roles r where r.id = ?)";
                param.add(roleId);
            }
            if (active != null) {
                condition += " and e.active = ?";
                param.add(("1".equals(active) ? 1 : 0));
            }

            return count("User", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Count Users Error!");
        }

        return 0L;
    }

    public Boolean assignUserToGroup(String userId, String groupId) {
        try {
            User user = getUserById(userId);
            Group group = getGroupDao().getGroup(groupId);
            if (user != null && group != null) {
                user.getGroups().add(group);
                saveOrUpdate("User", user);
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Assign User From Group Error!");
        }
        return false;
    }

    public Boolean unassignUserFromGroup(String userId, String groupId) {
        try {
            User user = getUserById(userId);
            Group group = getGroupDao().getGroup(groupId);
            if (user != null && group != null) {
                user.getGroups().remove(group);
                saveOrUpdate("User", user);
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Unassign User From Group Error!");
        }
        return false;
    }

    public Collection<User> getUsersNotInGroup(String filterString, String groupId, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.username like ? or e.firstName like ? or e.lastName like ? or e.email like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            if (groupId != null) {
                condition += " and e.id not in (select u.id from User u join u.groups g where g.id = ?)";
                param.add(groupId);
            }

            return find("User", condition, param.toArray(), sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Get Users Not In Group Error!");
        }

        return null;
    }

    public Long getTotalUsersNotInGroup(String filterString, String groupId) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.username like ? or e.firstName like ? or e.lastName like ? or e.email like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            if (groupId != null) {
                condition += " and e.id not in (select u.id from User u join u.groups g where g.id = ?)";
                param.add(groupId);
            }

            return count("User", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Get Total Users Not In Group Error!");
        }

        return 0L;
    }

    public Collection<User> getUsersSubordinate(String username, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            Collection param = new ArrayList();
            String condition = "where e.id in (select t.subordinate.user.id from EmploymentReportTo t where t.reportTo.user.id = ?)";
            param.add(username);

            return find("User", condition, param.toArray(), sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Get Users Subordinate Error!");
        }

        return null;
    }

    public Long getTotalUsersSubordinate(String username) {
        try {
            Collection param = new ArrayList();
            String condition = "where e.id in (select t.subordinate.user.id from EmploymentReportTo t where t.reportTo.user.id = ?)";
            param.add(username);

            return count("User", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(UserDaoImpl.class.getName(), e, "Get Total Users Not In Group Error!");
        }

        return 0L;
    }
    
    protected void adminRoleFilter(User user) {
        ApplicationContext ac = DirectoryUtil.getApplicationContext();
        
        if (ac != null) {
            WorkflowUserManager workflowUserManager = (WorkflowUserManager) DirectoryUtil.getApplicationContext().getBean("workflowUserManager");
            if (workflowUserManager != null && !(workflowUserManager.isCurrentUserInRole(WorkflowUserManager.ROLE_ADMIN) || workflowUserManager.isSystemUser())){
                Role adminRole = roleDao.getRole(WorkflowUserManager.ROLE_ADMIN);
                if (user.getRoles() != null && user.getRoles().contains(adminRole)) {
                    user.getRoles().remove(adminRole);
                    Role userRole = roleDao.getRole("ROLE_USER");
                    if (userRole != null && !user.getRoles().contains(userRole)) {
                        user.getRoles().add(userRole);
                    }
                }
            }
        }
    }
}
