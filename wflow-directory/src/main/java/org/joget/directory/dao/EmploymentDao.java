package org.joget.directory.dao;

import java.util.Collection;
import org.joget.directory.model.Employment;

public interface EmploymentDao {

    Boolean addEmployment(Employment employment);

    Boolean updateEmployment(Employment employment);

    Boolean deleteEmployment(String id);

    Employment getEmployment(String id);

    Collection<Employment> getEmployments(String filterString, String organizationId, String departmentId, String gradeId, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalEmployments(String filterString, String organizationId, String departmentId, String gradeId);

    Collection<Employment> findEmployments(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows);

    Long countEmployments(String condition, Object[] params);

    Boolean assignUserAsDepartmentHOD(String userId, String departmentId);

    Boolean unassignUserAsDepartmentHOD(String userId, String departmentId);

    Boolean assignUserToOrganization(String userId, String organizationId);

    Boolean unassignUserFromOrganization(String userId, String organizationId);

    Boolean assignUserToDepartment(String userId, String departmentId);

    Boolean unassignUserFromDepartment(String userId, String departmentId);

    Boolean assignUserToGrade(String userId, String gradeId);

    Boolean unassignUserFromGrade(String userId, String gradeId);

    Boolean assignUserReportTo(String userId, String reportToUserId);

    Boolean unassignUserReportTo(String userId);

    Collection<Employment> getEmploymentsNoHaveOrganization(String filterString, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalEmploymentsNoHaveOrganization(String filterString);

    Collection<Employment> getEmploymentsNotInDepartment(String filterString, String organizationId, String departmentId, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalEmploymentsNotInDepartment(String filterString, String organizationId, String departmentId);

    Collection<Employment> getEmploymentsNotInGrade(String filterString, String organizationId, String gradeId, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalEmploymentsNotInGrade(String filterString, String organizationId, String gradeId);
}
