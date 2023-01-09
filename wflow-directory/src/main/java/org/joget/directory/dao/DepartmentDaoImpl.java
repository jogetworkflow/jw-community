package org.joget.directory.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Department;
import org.joget.directory.model.Employment;

public class DepartmentDaoImpl extends AbstractSpringDao implements DepartmentDao {

    private OrganizationDao organizationDao;
    private EmploymentDao employmentDao;

    public EmploymentDao getEmploymentDao() {
        return employmentDao;
    }

    public void setEmploymentDao(EmploymentDao employmentDao) {
        this.employmentDao = employmentDao;
    }

    public OrganizationDao getOrganizationDao() {
        return organizationDao;
    }

    public void setOrganizationDao(OrganizationDao organizationDao) {
        this.organizationDao = organizationDao;
    }

    public Boolean addDepartment(Department department) {
        try {
            save("Department", department);
            return true;
        } catch (Exception e) {
            LogUtil.error(DepartmentDaoImpl.class.getName(), e, "Add Department Error!");
            return false;
        }
    }

    public Boolean updateDepartment(Department department) {
        try {
            merge("Department", department);
            return true;
        } catch (Exception e) {
            LogUtil.error(DepartmentDaoImpl.class.getName(), e, "Update Department Error!");
            return false;
        }
    }

    public Boolean deleteDepartment(String id) {
        try {
            Department department = getDepartment(id);
            if (department != null) {
                Set<Department> childs = department.getChildrens();
                Set<Employment> employments = department.getEmployments();

                if (childs != null) {
                    for (Department child : childs) {
                        delete("Department", child);
                    }
                }

                department.setHod(null);

                if (employments != null) {
                    for (Employment employment : employments) {
                        employmentDao.unassignUserReportTo(employment.getUserId());
                        employmentDao.unassignUserFromDepartment(employment.getUserId(), id);
                    }
                    employments.clear();
                }

                delete("Department", department);
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(DepartmentDaoImpl.class.getName(), e, "Delete Department Error!");
            return false;
        }
    }

    public Department getDepartment(String id) {
        try {
            return (Department) find("Department", id);
        } catch (Exception e) {
            LogUtil.error(DepartmentDaoImpl.class.getName(), e, "Get Department Error!");
            return null;
        }
    }

    public Department getDepartmentByName(String name) {
        try {
            Department department = new Department();
            department.setName(name);
            List departments = findByExample("Department", department);

            if (departments.size() > 0) {
                return (Department) departments.get(0);
            }
        } catch (Exception e) {
            LogUtil.error(DepartmentDaoImpl.class.getName(), e, "Get Department By Name Error!");
        }

        return null;
    }

    public Department getParentDepartment(String id) {
        try {
            Department department = getDepartment(id);
            return department.getParent();
        } catch (Exception e) {
            LogUtil.error(DepartmentDaoImpl.class.getName(), e, "Get Parent Department Error!");
        }

        return null;
    }

    public Department getParentDepartmentByName(String name) {
        try {
            Department department = getDepartmentByName(name);
            return department.getParent();
        } catch (Exception e) {
            LogUtil.error(DepartmentDaoImpl.class.getName(), e, "Get Parent Department By Name Error!");
        }

        return null;
    }

    public Collection<Department> getDepartmentsByParentId(String filterString, String parentId, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            return find("Department", "where (e.id like ? or e.name like ? or e.description like ?) and e.parent.id = ?", new Object[]{"%" + filterString + "%", "%" + filterString + "%", "%" + filterString + "%", parentId}, sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(DepartmentDaoImpl.class.getName(), e, "Get Departments By Parent Id Error!");
        }

        return null;
    }

    public Long getTotalDepartmentsByParentId(String filterString, String parentId) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            return count("Department", "where (e.id like ? or e.name like ? or e.description like ?) and e.parent.id = ?", new Object[]{"%" + filterString + "%", "%" + filterString + "%", "%" + filterString + "%", parentId});
        } catch (Exception e) {
            LogUtil.error(DepartmentDaoImpl.class.getName(), e, "Get Total Departments By Parent Id Error!");
        }

        return 0L;
    }

    public Collection<Department> getDepartmentsByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.id like ? or e.name like ? or e.description like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            if (organizationId != null) {
                condition += " and e.organization.id = ?";
                param.add(organizationId);
            }
            return find("Department", condition, param.toArray(), sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(DepartmentDaoImpl.class.getName(), e, "Get Departments By Organization Id Error!");
        }

        return null;
    }

    public Long getTotalDepartmentsByOrganizationId(String filterString, String organizationId) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.id like ? or e.name like ? or e.description like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            if (organizationId != null) {
                condition += " and e.organization.id = ?";
                param.add(organizationId);
            }
            return count("Department", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(DepartmentDaoImpl.class.getName(), e, "Get Total Departmentns By Organization Id Error!");
        }

        return 0L;
    }

    public Collection<Department> findDepartments(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            return find("Department", condition, params, sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(DepartmentDaoImpl.class.getName(), e, "Find Departments Error!");
        }

        return null;
    }

    public Long countDepartments(String condition, Object[] params) {
        try {
            return count("Department", condition, params);
        } catch (Exception e) {
            LogUtil.error(DepartmentDaoImpl.class.getName(), e, "Count Departments Error!");
        }

        return 0L;
    }
}
