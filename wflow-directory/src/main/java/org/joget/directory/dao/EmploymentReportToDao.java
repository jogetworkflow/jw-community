package org.joget.directory.dao;

import org.joget.directory.model.EmploymentReportTo;

public interface EmploymentReportToDao {

    Boolean addEmploymentReportTo(EmploymentReportTo employmentReportTo);

    Boolean updateEmploymentReportTo(EmploymentReportTo employmentReportTo);

    Boolean deleteEmploymentReportTo(String id);

    EmploymentReportTo getEmploymentReportTo(String id);
}
