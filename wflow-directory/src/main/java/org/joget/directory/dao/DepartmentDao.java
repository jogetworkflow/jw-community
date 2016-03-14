package org.joget.directory.dao;

import java.util.Collection;
import org.joget.directory.model.Department;

public interface DepartmentDao {

    Boolean addDepartment(Department department);

    Boolean updateDepartment(Department department);

    Boolean deleteDepartment(String id);

    Department getDepartment(String id);

    Department getDepartmentByName(String name);

    Department getParentDepartment(String id);

    Department getParentDepartmentByName(String name);

    Collection<Department> getDepartmentsByParentId(String filterString, String parentId, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalDepartmentsByParentId(String filterString, String parentId);

    Collection<Department> getDepartmentsByOrganizationId(String filterString, String organizationId, String sort, Boolean desc, Integer start, Integer rows);

    Long getTotalDepartmentsByOrganizationId(String filterString, String organizationId);

    Collection<Department> findDepartments(String condition, Object[] params, String sort, Boolean desc, Integer start, Integer rows);

    Long countDepartments(String condition, Object[] params);
}
