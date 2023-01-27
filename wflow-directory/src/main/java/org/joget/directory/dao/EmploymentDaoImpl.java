package org.joget.directory.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.Session;
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
            
            User user = employment.getUser();
            if (user != null) {
                //update all employments under same user
                Set<Employment> employments = user.getEmployments();
                for (Employment e : employments) {
                    if (!e.getId().equals(employment.getId())) {
                        e.setEmployeeCode(employment.getEmployeeCode());
                        e.setRole(employment.getRole());
                        e.setStartDate(employment.getStartDate());
                        e.setEndDate(employment.getEndDate());
                        merge("Employment", e);
                    }
                }
            }
            
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
                    employment.getSubordinates().clear();
                }

                if (employment.getEmploymentReportTo() != null) {
                    employmentReportToDao.deleteEmploymentReportTo(employment.getEmploymentReportTo().getId());
                    employment.setEmploymentReportTo(null);
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

            return findDistinct("Employment", condition, param.toArray(), sort, desc, start, rows);
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

            return countDistinct("Employment", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Get Total Employments Error!");
        }

        return 0L;
    }

    public Boolean assignUserAsDepartmentHOD(String userId, String departmentId) {
        try {
            User user = userDao.getUserById(userId);
            Department department = departmentDao.getDepartment(departmentId);

            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && department != null) {
                for (Employment employment : (Set<Employment>) user.getEmployments()) {
                    if (department.getId().equals(employment.getDepartmentId())) {
                        employment.getHods().clear();
                        employment.getHods().add(department);
                        saveOrUpdate("Employment", employment);
                        department.setHod(employment);
                        departmentDao.updateDepartment(department);
                    }
                }
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

            if (department != null && user != null && user.getEmployments() != null && user.getEmployments().size() > 0) {
                for (Employment employment : (Set<Employment>) user.getEmployments()) {
                    if (department.getId().equals(employment.getDepartmentId()) && !employment.getHods().isEmpty()) {
                        employment.getHods().clear();
                        saveOrUpdate("Employment", employment);
                        department.setHod(null);
                        departmentDao.updateDepartment(department);
                    }
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

            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && reportToUser != null && reportToUser.getEmployments() != null && reportToUser.getEmployments().size() > 0) {
                Employment ue = null;
                Employment rte = null;
                
                for (Employment employment : (Set<Employment>)user.getEmployments()) {
                    if (ue == null || employment.getEmploymentReportTo() != null) {
                        ue = employment;
                    }
                }
                for (Employment reportToEmployment : (Set<Employment>)reportToUser.getEmployments()) {
                    if (rte == null || (reportToEmployment.getSubordinates() != null && !reportToEmployment.getSubordinates().isEmpty())) {
                        rte = reportToEmployment;
                    }
                }
                
                if (ue != null && ue.getEmploymentReportTo() != null) {
                    //delete current report to
                    employmentReportToDao.deleteEmploymentReportTo(ue.getEmploymentReportTo().getId());
                }
                
                EmploymentReportTo employmentReportTo = new EmploymentReportTo();
                employmentReportTo.setSubordinate(ue);
                employmentReportTo.setReportTo(rte);
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

            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0) {
                for (Employment employment : (Set<Employment>)user.getEmployments()) {
                    if (employment.getEmploymentReportTo() != null) {
                        //delete current report to
                        employmentReportToDao.deleteEmploymentReportTo(employment.getEmploymentReportTo().getId());
                    }
                }
                return true;
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

            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && organization != null) {
                boolean found = false;
                Employment isNull = null;
                Employment first = null;
                
                for (Employment employment : (Set<Employment>) user.getEmployments()) {
                    if (first == null) {
                        first = employment;
                    }
                    if (employment.getOrganizationId() == null) {
                        isNull = employment;
                    } else if (organization.getId().equals(employment.getOrganizationId())) {
                        found = true;
                    }
                }
                
                if (!found) {
                    if (isNull == null) {
                        isNull = new Employment();
                        isNull.setUserId(userId);
                        if (first != null) {
                            isNull.setEmployeeCode(first.getEmployeeCode());
                            isNull.setRole(first.getRole());
                            isNull.setStartDate(first.getStartDate());
                            isNull.setEndDate(first.getEndDate());
                        }
                        isNull.setHods(new HashSet());
                    }
                    isNull.setOrganizationId(organization.getId());
                    isNull.setDepartmentId(null);
                    isNull.setGradeId(null);
                    isNull.getHods().clear();
                    saveOrUpdate("Employment", isNull);
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

            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && organization != null) {
                //always keep 1, delete the others;
                int size = user.getEmployments().size();
                for (Employment e : (Set<Employment>) user.getEmployments()) {
                    if (organization.getId().equals(e.getOrganizationId())) {
                        if (size > 1) {
                            delete("Employment", e);
                            size--;
                        } else {
                            e.setOrganizationId(null);
                            e.setDepartmentId(null);
                            e.setGradeId(null);
                            e.getHods().clear();
                            saveOrUpdate("Employment", e);
                        }
                    }
                }
                
                return true;
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

            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && department != null) {
                boolean found = false;
                Employment isNull = null;
                Employment first = null;
                
                for (Employment employment : (Set<Employment>) user.getEmployments()) {
                    if (first == null) {
                        first = employment;
                    }
                    if (employment.getOrganizationId() == null 
                            || (employment.getOrganizationId().equals(department.getOrganization().getId()) && employment.getDepartmentId() == null)) {
                        isNull = employment;
                    } else if (department.getOrganization().getId().equals(employment.getOrganizationId()) && department.getId().equals(employment.getDepartmentId())) {
                        found = true;
                    }
                }
                
                if (!found) {
                    if (isNull == null) {
                        isNull = new Employment();
                        isNull.setUserId(userId);
                        if (first != null) {
                            isNull.setEmployeeCode(first.getEmployeeCode());
                            isNull.setRole(first.getRole());
                            isNull.setStartDate(first.getStartDate());
                            isNull.setEndDate(first.getEndDate());
                        }
                        isNull.setHods(new HashSet());
                    }
                    isNull.setOrganizationId(department.getOrganization().getId());
                    isNull.setDepartmentId(department.getId());
                    saveOrUpdate("Employment", isNull);
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

            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && department != null) {
                int sameOrg = 0;
                Employment found = null;
                
                for (Employment e : (Set<Employment>) user.getEmployments()) {
                    if (department.getOrganization().getId().equals(e.getOrganizationId())) {
                        if (department.getId().equals(e.getDepartmentId())) {
                            found = e;
                        }
                        sameOrg++;
                    }
                }
                
                if (found != null) {
                    if (found.getHods() != null && !found.getHods().isEmpty() && found.getDepartment() != null) {
                        Department orgDepartment = found.getDepartment();
                        orgDepartment.setHod(null);
                        departmentDao.updateDepartment(orgDepartment);
                    }
                    if (sameOrg > 1) {
                        delete("Employment", found);
                    } else {
                        found.setDepartmentId(null);
                        found.getHods().clear();
                        saveOrUpdate("Employment", found);
                    }
                }
                return true;
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

            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && grade != null) {
                Employment isNull = null;
                Employment first = null;
                boolean isUpdated = false;
                
                for (Employment employment : (Set<Employment>) user.getEmployments()) {
                    if (first == null) {
                        first = employment;
                    }
                    if (employment.getOrganizationId() == null) {
                        isNull = employment;
                    } else if (grade.getOrganization().getId().equals(employment.getOrganizationId())) {
                        employment.setGradeId(grade.getId());
                        saveOrUpdate("Employment", employment);
                        isUpdated = true;
                    }
                }
                
                if (isUpdated) {
                    return true;
                }
                
                if (isNull == null) {
                    isNull = new Employment();
                    isNull.setUserId(userId);
                    if (first != null) {
                        isNull.setEmployeeCode(first.getEmployeeCode());
                        isNull.setRole(first.getRole());
                        isNull.setStartDate(first.getStartDate());
                        isNull.setEndDate(first.getEndDate());
                    }
                    isNull.setHods(new HashSet());
                }
                isNull.setOrganizationId(grade.getOrganization().getId());
                isNull.setGradeId(grade.getId());
                saveOrUpdate("Employment", isNull);
                
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

            if (user != null && user.getEmployments() != null && user.getEmployments().size() > 0 && grade != null) {
                Collection<Employment> sameOrgs = new ArrayList<Employment>();

                for (Employment e : (Set<Employment>) user.getEmployments()) {
                    if (grade.getOrganization().getId().equals(e.getOrganizationId())) {
                        sameOrgs.add(e);
                    }
                }
                
                int size = sameOrgs.size();
                for (Employment e : sameOrgs) {
                    if (gradeId.equals(e.getGradeId())) {
                        if (size > 1 && e.getDepartmentId() == null) {
                            delete("Employment", e);
                            size--;
                        } else {
                            e.setGradeId(null);
                            saveOrUpdate("Employment", e);
                        }
                    }
                }
                
                return true;
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

            condition += " and e.userId not in (select sub.userId from Employment as sub where sub.organizationId is not null) ";

            return findDistinct("Employment", condition, param.toArray(), sort, desc, start, rows);
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

            condition += " and e.userId not in (select sub.userId from Employment as sub where sub.organizationId is not null) ";

            return countDistinct("Employment", condition, param.toArray());
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

            condition += " and e.userId not in (select sub.userId from Employment as sub where sub.organizationId = ? and sub.departmentId = ?) and e.organizationId = ?";
            param.add(organizationId);
            param.add(departmentId);
            param.add(organizationId);

            return findDistinct("Employment", condition, param.toArray(), sort, desc, start, rows);
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

            condition += " and e.userId not in (select sub.userId from Employment as sub where sub.organizationId = ? and sub.departmentId = ?) and e.organizationId = ?";
            param.add(organizationId);
            param.add(departmentId);
            param.add(organizationId);

            return countDistinct("Employment", condition, param.toArray());
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

            condition += " and e.userId not in (select sub.userId from Employment as sub where sub.organizationId = ? and sub.gradeId = ?) and e.organizationId = ?";
            param.add(organizationId);
            param.add(gradeId);
            param.add(organizationId);

            return findDistinct("Employment", condition, param.toArray(), sort, desc, start, rows);
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

            condition += " and e.userId not in (select sub.userId from Employment as sub where sub.organizationId = ? and sub.gradeId = ?) and e.organizationId = ?";
            param.add(organizationId);
            param.add(gradeId);
            param.add(organizationId);

            return countDistinct("Employment", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Get Total Employments Not In Grade Error!");
        }

        return 0L;
    }
    
    public Collection<Employment> getEmploymentsNotInOrganization(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows) {
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

            condition += " and e.userId not in (select sub.userId from Employment as sub where sub.organizationId = ?) ";
            param.add(organizationId);

            return findDistinct("Employment", condition, param.toArray(), sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Get Users No Have Organization Error!");
        }

        return null;
    }

    public Long getTotalEmploymentsNotInOrganization(String filterString, String organizationId) {
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

            condition += " and e.userId not in (select sub.userId from Employment as sub where sub.organizationId = ?) ";
            param.add(organizationId);

            return countDistinct("Employment", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(EmploymentDaoImpl.class.getName(), e, "Get Total Employments No Have Organization Error!");
        }

        return 0L;
    }
    
    @Override
    protected void delete(String entityName, Object obj) {
        //transfer reportTo if exist
        Employment e = (Employment) obj;
        if (e.getEmploymentReportTo() != null || (e.getSubordinates() != null && !e.getSubordinates().isEmpty())) {
            User u = e.getUser();
            if (u.getEmployments().size() > 1) {
                for (Employment o : (Set<Employment>) u.getEmployments()) {
                    if (!o.getId().equals(e.getId())) {
                        if (e.getEmploymentReportTo() != null) {
                            EmploymentReportTo ert = e.getEmploymentReportTo();
                            ert.setSubordinate(o);
                            employmentReportToDao.updateEmploymentReportTo(ert);
                        }
                        if (e.getSubordinates() != null && !e.getSubordinates().isEmpty()) {
                            for (EmploymentReportTo ert : (Set<EmploymentReportTo>) e.getSubordinates()) {
                                ert.setReportTo(o);
                                employmentReportToDao.updateEmploymentReportTo(ert);
                            }
                        }
                        
                        break;
                    }
                }
            }
        }
        
        super.delete(entityName, obj);
    }
    
    protected Collection<Employment> findDistinct(final String entityName, final String condition, final Object[] params, final String sort, final Boolean desc, final Integer start, final Integer rows) {
        Session session = findSession();
        
        String query = "SELECT e.userId, e.id FROM " + entityName + " e " + condition;
        Query q = session.createQuery(query);
        if (params != null) {
            int i = 0;
            for (Object param : params) {
                q.setParameter(i, param);
                i++;
            }
        }
        
        Map<String, String> ids = new HashMap<String, String>();
        Collection results = q.list();
        if (results != null) {
            for (Object o : results) {
                Object[] temp = (Object[]) o;
                if (!ids.containsKey(temp[0].toString())) {
                    ids.put(temp[0].toString(), temp[1].toString());
                }
            }
            
            if (!ids.isEmpty()) {
                query = "SELECT e FROM " + entityName + " e where e.id IN (:ids)";

                if (sort != null && !sort.equals("")) {
                    String filteredSort = filterSpace(sort);
                    query += " ORDER BY " + filteredSort;

                    if (desc) {
                        query += " DESC";
                    }
                }
                q = session.createQuery(query);

                int s = (start == null) ? 0 : start;
                q.setFirstResult(s);

                if (rows != null && rows > 0) {
                    q.setMaxResults(rows);
                }

                q.setParameterList("ids", ids.values().toArray(new String[0]));

                return (Collection<Employment>) q.list();
            }
        }
        
        return new ArrayList<Employment>();
    }

    protected Long countDistinct(final String entityName, final String condition, final Object[] params) {
        Session session = findSession();
        Query q = session.createQuery("SELECT COUNT(e.userId) FROM " + entityName + " e " + condition + " group by e.userId");

        if (params != null) {
            int i = 0;
            for (Object param : params) {
                q.setParameter(i, param);
                i++;
            }
        }
        
        List result = q.list();
        return new Long(result.size());
    }
}
