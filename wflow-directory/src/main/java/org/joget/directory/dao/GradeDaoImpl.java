package org.joget.directory.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.Employment;
import org.joget.directory.model.Grade;

public class GradeDaoImpl extends AbstractSpringDao implements GradeDao {

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

    public Boolean addGrade(Grade grade) {
        try {
            save("Grade", grade);
            return true;
        } catch (Exception e) {
            LogUtil.error(GradeDaoImpl.class.getName(), e, "Add Grade Error!");
            return false;
        }
    }

    public Boolean updateGrade(Grade grade) {
        try {
            merge("Grade", grade);
            return true;
        } catch (Exception e) {
            LogUtil.error(GradeDaoImpl.class.getName(), e, "Update Grade Error!");
            return false;
        }
    }

    public Boolean deleteGrade(String id) {
        try {
            Grade grade = getGrade(id);

            if (grade != null && grade.getEmployments() != null && grade.getEmployments().size() > 0) {
                for (Employment e : (Set<Employment>) grade.getEmployments()) {
                    employmentDao.unassignUserFromGrade(e.getUserId(), id);
                }
            }
            delete("Grade", grade);
            return true;
        } catch (Exception e) {
            LogUtil.error(GradeDaoImpl.class.getName(), e, "Delete Grade Error!");
            return false;
        }
    }

    public Grade getGrade(String id) {
        try {
            return (Grade) find("Grade", id);
        } catch (Exception e) {
            LogUtil.error(GradeDaoImpl.class.getName(), e, "Get Grade Error!");
            return null;
        }
    }

    public Grade getGradeByName(String name) {
        try {
            Grade grade = new Grade();
            grade.setName(name);
            List grades = findByExample("Grade", grade);

            if (grades.size() > 0) {
                return (Grade) grades.get(0);
            }
        } catch (Exception e) {
            LogUtil.error(GradeDaoImpl.class.getName(), e, "Get Grade By Name Error!");
        }

        return null;
    }

    public Collection<Grade> getGradesByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows) {
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
            return find("Grade", condition, param.toArray(), sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(GradeDaoImpl.class.getName(), e, "Get Grades By Organization Id Error!");
        }

        return null;
    }

    public Long getTotalGradesByOrganizationId(String filterString, String organizationId) {
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
            return count("Grade", condition, param.toArray());
        } catch (Exception e) {
            LogUtil.error(GradeDaoImpl.class.getName(), e, "Get Total Grades By Organization Id Error!");
        }

        return 0L;
    }

    public Collection<Grade> findGrades(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows) {
        try {
            return find("Grade", condition, params, sort, desc, start, rows);
        } catch (Exception e) {
            LogUtil.error(GradeDaoImpl.class.getName(), e, "Find Grades Error!");
        }

        return null;
    }

    public Long countGrades(String condition, Object[] params) {
        try {
            return count("Grade", condition, params);
        } catch (Exception e) {
            LogUtil.error(GradeDaoImpl.class.getName(), e, "Count Grades Error!");
        }

        return 0L;
    }
}
