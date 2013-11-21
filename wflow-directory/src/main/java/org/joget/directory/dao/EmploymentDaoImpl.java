package org.joget.directory.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Department;
import org.joget.directory.model.Employment;
import org.joget.directory.model.EmploymentReportTo;
import org.joget.directory.model.Grade;
import org.joget.directory.model.Organization;
import org.joget.directory.model.User;

public class EmploymentDaoImpl extends AbstractSpringDao implements EmploymentDao {

    private EmploymentReportToDao employmentReportToDao;
    private UserDao userDao;
    private OrganizationDao organizationDao;
    private DepartmentDao departmentDao;
    private GradeDao gradeDao;

    public GradeDao getGradeDao() {
        return gradeDao;
    }

    public void setGradeDao(GradeDao gradeDao) {
        this.gradeDao = gradeDao;
    }

    public OrganizationDao getOrganizationDao() {
        return organizationDao;
    }

    public void setOrganizationDao(OrganizationDao organizationDao) {
        this.organizationDao = organizationDao;
    }

    public EmploymentReportToDao getEmploymentReportToDao() {
        return employmentReportToDao;
    }

    public void setEmploymentReportToDao(EmploymentReportToDao employmentReportToDao) {
        this.employmentReportToDao = employmentReportToDao;
    }

    public DepartmentDao getDepartmentDao() {
        return departmentDao;
    }

    public void setDepartmentDao(DepartmentDao departmentDao) {
        this.departmentDao = departmentDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public Boolean addEmployment(Employment employment) {
        try {
            User user = employment.getUser();
            if (user != null) {
                employment.setHods(new HashSet());
                Set<Employment> employments = new HashSet<Employment>();
                employments.add(employment);
                user.setEmployments(employments);
            }
            save("Employment", employment);
            return true;
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Add Employment Error!");
            return false;
        }
    }

    public Boolean updateEmployment(Employment employment) {
        try {
            merge("Employment", employment);
            return true;
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Update Employment Error!");
            return false;
        }
    }

    public Boolean deleteEmployment(String id) {
        try {
            Employment employment = getEmployment(id);

            if (employment != null) {
                // clear department HOD
                Department dept = employment.getDepartment();
                if (dept != null) {
                    Employment hod = dept.getHod();
                    if (hod != null && id.equals(hod.getId())) {
                        dept.setHod(null);
                        departmentDao.updateDepartment(dept);
                    }
                }
                
                // clear employment
                employment.setOrganization(null);
                employment.setDepartment(null);
                employment.setGrade(null);
                employment.setUser(null);
                employment.getHods().clear();

                if (employment.getSubordinates() != null) {
                    for (EmploymentReportTo r : (Set<EmploymentReportTo>) employment.getSubordinates()) {
                        employmentReportToDao.deleteEmploymentReportTo(r.getId());
                    }
                }

                if (employment.getEmploymentReportTo() != null) {
                    employmentReportToDao.deleteEmploymentReportTo(employment.getEmploymentReportTo().getId());
                }

                delete("Employment", employment);
            }
            return true;
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Delete Employment Error!");
            return false;
        }
    }

    public Employment getEmployment(String id) {
        try {
            return (Employment) find("Employment", id);
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Get Employment Error!");
            return null;
        }
    }

    public Collection<Employment> findEmployments(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            return find("Employment", condition, params, sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Find Employments Error!");
        }

        return null;
    }

    public Long countEmployments(String condition, Object[] params) {
        try {
            return count("Employment", condition, params);
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Count Employments Error!");
        }

        return 0L;
    }

    public Collection<Employment> getEmployments(String filterString, String organizationId, String departmentId, String gradeId, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.user.username like ? or e.user.firstName like ? or e.user.lastName like ? or e.employeeCode like ? or e.role like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            if (organizationId != null) {
                condition += " and e.organization.id = ?";
                param.add(organizationId);
            }
            if (departmentId != null) {
                condition += " and e.department.id = ?";
                param.add(departmentId);
            }
            if (gradeId != null) {
                condition += " and e.grade.id = ?";
                param.add(gradeId);
            }

            return find("Employment", condition, param.toArray(), sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Get Employments Error!");
        }

        return null;
    }

    public Long getTotalEmployments(String filterString, String organizationId, String departmentId, String gradeId) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.user.username like ? or e.user.firstName like ? or e.user.lastName like ? or e.employeeCode like ? or e.role like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            if (organizationId != null) {
                condition += " and e.organization.id = ?";
                param.add(organizationId);
            }
            if (departmentId != null) {
                condition += " and e.department.id = ?";
                param.add(departmentId);
            }
            if (gradeId != null) {
                condition += " and e.grade.id = ?";
                param.add(gradeId);
            }

            return count("Employment", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Get Total Employments Error!");
        }

        return 0L;
    }

    public Boolean assignUserAsDepartmentHOD(String userId, String departmentId) {
        try {
            User user = userDao.getUserById(userId);
            Department department = departmentDao.getDepartment(departmentId);

            //get only 1st employment
            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && department != null) {
                Employment employment = (Employment) user.getEmployments().iterator().next();
                    employment.getHods().clear();
                    employment.getHods().add(department);
                saveOrUpdate("Employment", employment);
                department.setHod(employment);
                departmentDao.updateDepartment(department);
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Assign User As Department Hod Error!");
        }
        return false;
    }

    public Boolean unassignUserAsDepartmentHOD(String userId, String departmentId) {
        try {
            User user = userDao.getUserById(userId);
            Department department = departmentDao.getDepartment(departmentId);

            //get only 1st employment
            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0) {
                Employment employment = (Employment) user.getEmployments().iterator().next();
                if (department != null && employment.getHods().contains(department)) {
                    employment.getHods().clear();
                    saveOrUpdate("Employment", employment);
                    department.setHod(null);
                    departmentDao.updateDepartment(department);
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Unassign User As Department Hod Error!");
        }
        return false;
    }

    public Boolean assignUserReportTo(String userId, String reportToUserId) {
        try {
            User user = userDao.getUserById(userId);
            User reportToUser = userDao.getUserById(reportToUserId);

            //get only 1st employment
            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && reportToUser != null && reportToUser.getEmployments() != null && reportToUser.getEmployments().size() > 0) {
                Employment employment = (Employment) user.getEmployments().iterator().next();
                Employment reportToEmployment = (Employment) reportToUser.getEmployments().iterator().next();

                if (employment.getEmploymentReportTo() != null) {
                    //delete current report to
                    employmentReportToDao.deleteEmploymentReportTo(employment.getEmploymentReportTo().getId());
                }

                EmploymentReportTo employmentReportTo = new EmploymentReportTo();
                employmentReportTo.setSubordinate(employment);
                employmentReportTo.setReportTo(reportToEmployment);
                employmentReportToDao.addEmploymentReportTo(employmentReportTo);

                return true;
            }
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Assign User Report To Error!");
        }
        return false;
    }

    public Boolean unassignUserReportTo(String userId) {
        try {
            User user = userDao.getUserById(userId);

            //get only 1st employment
            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0) {
                Employment employment = (Employment) user.getEmployments().iterator().next();

                if (employment.getEmploymentReportTo() != null) {
                    return employmentReportToDao.deleteEmploymentReportTo(employment.getEmploymentReportTo().getId());
                }
            }
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Unassign User Report To Error!");
        }
        return false;
    }

    public Boolean assignUserToOrganization(String userId, String organizationId) {
        try {
            User user = userDao.getUserById(userId);
            Organization organization = organizationDao.getOrganization(organizationId);

            //get only 1st employment
            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && organization != null) {
                Employment employment = (Employment) user.getEmployments().iterator().next();
                if (!organization.getId().equals(employment.getOrganizationId())) {
                    if (employment.getHods() != null && !employment.getHods().isEmpty() && employment.getDepartment() != null) {
                        Department orgDepartment = employment.getDepartment();
                        orgDepartment.setHod(null);
                        departmentDao.updateDepartment(orgDepartment);
                    }
                    
                    employment.setOrganizationId(organization.getId());
                    employment.setDepartmentId(null);
                    employment.setGradeId(null);
                    employment.getHods().clear();
                    saveOrUpdate("Employment", employment);
                }
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Assign User To Organization Error!");
        }
        return false;
    }

    public Boolean unassignUserFromOrganization(String userId, String organizationId) {
        try {
            User user = userDao.getUserById(userId);
            Organization organization = organizationDao.getOrganization(organizationId);

            //get only 1st employment
            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && organization != null) {
                Employment employment = (Employment) user.getEmployments().iterator().next();
                if (organization.getId().equals(employment.getOrganizationId())) {
                    if (employment.getHods() != null && !employment.getHods().isEmpty() && employment.getDepartment() != null) {
                        Department orgDepartment = employment.getDepartment();
                        orgDepartment.setHod(null);
                        departmentDao.updateDepartment(orgDepartment);
                    }
                    
                    employment.setOrganizationId(null);
                    employment.setDepartmentId(null);
                    employment.setGradeId(null);
                    employment.getHods().clear();
                    saveOrUpdate("Employment", employment);
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Unassign User From Organization Error!");
        }
        return false;
    }

    public Boolean assignUserToDepartment(String userId, String departmentId) {
        try {
            User user = userDao.getUserById(userId);
            Department department = departmentDao.getDepartment(departmentId);

            //get only 1st employment
            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && department != null) {
                Employment employment = (Employment) user.getEmployments().iterator().next();
                if (!department.getId().equals(employment.getDepartmentId())) {
                    if (employment.getHods() != null && !employment.getHods().isEmpty() && employment.getDepartment() != null) {
                        Department orgDepartment = employment.getDepartment();
                        orgDepartment.setHod(null);
                        departmentDao.updateDepartment(orgDepartment);
                    }
                    
                    employment.setOrganizationId(department.getOrganization().getId());
                    employment.setDepartmentId(department.getId());
                    employment.getHods().clear();
                    saveOrUpdate("Employment", employment);
                }
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Assign User To Department Error!");
        }
        return false;
    }

    public Boolean unassignUserFromDepartment(String userId, String departmentId) {
        try {
            User user = userDao.getUserById(userId);
            Department department = departmentDao.getDepartment(departmentId);

            //get only 1st employment
            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && department != null) {
                Employment employment = (Employment) user.getEmployments().iterator().next();
                if (department.getId().equals(employment.getDepartmentId())) {
                    if (employment.getHods() != null && !employment.getHods().isEmpty() && employment.getDepartment() != null) {
                        Department orgDepartment = employment.getDepartment();
                        orgDepartment.setHod(null);
                        departmentDao.updateDepartment(orgDepartment);
                    }
                    
                    employment.setDepartmentId(null);
                    employment.getHods().clear();
                    saveOrUpdate("Employment", employment);
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Unassign User from Department Error!");
        }
        return false;
    }

    public Boolean assignUserToGrade(String userId, String gradeId) {
        try {
            User user = userDao.getUserById(userId);
            Grade grade = gradeDao.getGrade(gradeId);

            //get only 1st employment
            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && grade != null) {
                Employment employment = (Employment) user.getEmployments().iterator().next();
                if (!grade.getId().equals(employment.getGradeId())) {
                    employment.setGradeId(grade.getId());
                    saveOrUpdate("Employment", employment);
                }
                return true;
            }
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Assign User To Grade Error!");
        }
        return false;
    }

    public Boolean unassignUserFromGrade(String userId, String gradeId) {
        try {
            User user = userDao.getUserById(userId);
            Grade grade = gradeDao.getGrade(gradeId);

            //get only 1st employment
            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && grade != null) {
                Employment employment = (Employment) user.getEmployments().iterator().next();
                if (grade.getId().equals(employment.getGradeId())) {
                    employment.setGradeId(null);
                    saveOrUpdate("Employment", employment);
                    return true;
                }
            }
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "UnAssign User from Grade Error!");
        }
        return false;
    }

    public Collection<Employment> getEmploymentsNoHaveOrganization(String filterString, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.user.username like ? or e.user.firstName like ? or e.user.lastName like ? or e.user.email like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            condition += " and (e.organizationId is null or e.organizationId = '')";

            return find("Employment", condition, param.toArray(), sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Get Users No Have Organization Error!");
        }

        return null;
    }

    public Long getTotalEmploymentsNoHaveOrganization(String filterString) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.user.username like ? or e.user.firstName like ? or e.user.lastName like ? or e.user.email like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            condition += " and (e.organizationId is null or e.organizationId = '')";

            return count("Employment", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Get Total Employments No Have Organization Error!");
        }

        return 0L;
    }

    public Collection<Employment> getEmploymentsNotInDepartment(String filterString, String organizationId, String departmentId, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.user.username like ? or e.user.firstName like ? or e.user.lastName like ? or e.user.email like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            condition += " and (e.organizationId = ? or e.organizationId is null) and (e.departmentId <> ? or e.departmentId is null or e.departmentId = '')";
            param.add(organizationId);
            param.add(departmentId);

            return find("Employment", condition, param.toArray(), sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Get Employments Not In Department Error!");
        }

        return null;
    }

    public Long getTotalEmploymentsNotInDepartment(String filterString, String organizationId, String departmentId) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.user.username like ? or e.user.firstName like ? or e.user.lastName like ? or e.user.email like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            condition += " and (e.organizationId = ? or e.organizationId is null) and (e.departmentId <> ? or e.departmentId is null or e.departmentId = '')";
            param.add(organizationId);
            param.add(departmentId);

            return count("Employment", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Get Total Employments Not In Department Error!");
        }

        return 0L;
    }

    public Collection<Employment> getEmploymentsNotInGrade(String filterString, String organizationId, String gradeId, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.user.username like ? or e.user.firstName like ? or e.user.lastName like ? or e.user.email like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            condition += " and e.organizationId = ? and (e.gradeId <> ? or e.gradeId is null or e.gradeId = '')";
            param.add(organizationId);
            param.add(gradeId);

            return find("Employment", condition, param.toArray(), sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Get Employments Not In Grade Error!");
        }

        return null;
    }

    public Long getTotalEmploymentsNotInGrade(String filterString, String organizationId, String gradeId) {
        try {
            if (filterString == null) {
                filterString = "";
            }
            Collection param = new ArrayList();
            String condition = "where (e.user.username like ? or e.user.firstName like ? or e.user.lastName like ? or e.user.email like ?)";
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");
            param.add("%" + filterString + "%");

            condition += " and e.organizationId = ? and (e.gradeId <> ? or e.gradeId is null or e.gradeId = '')";
            param.add(organizationId);
            param.add(gradeId);

            return count("Employment", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Get Total Employments Not In Grade Error!");
        }

        return 0L;
    }
}
