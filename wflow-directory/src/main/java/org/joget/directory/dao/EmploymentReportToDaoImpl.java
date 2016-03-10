package org.joget.directory.dao;

import org.joget.commons.spring.model.AbstractSpringDao;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.EmploymentReportTo;

public class EmploymentReportToDaoImpl extends AbstractSpringDao implements EmploymentReportToDao {

    public Boolean addEmploymentReportTo(EmploymentReportTo employmentReportTo) {
        try {
            save("EmploymentReportTo", employmentReportTo);
            return true;
        } catch (Exception e) {
            LogUtil.error(EmploymentReportToDaoImpl.class.getName(), e, "Add Employment Report To Error!");
            return false;
        }
    }

    public Boolean updateEmploymentReportTo(EmploymentReportTo employmentReportTo) {
        try {
            merge("EmploymentReportTo", employmentReportTo);
            return true;
        } catch (Exception e) {
            LogUtil.error(EmploymentReportToDaoImpl.class.getName(), e, "Update Employment Report To Error!");
            return false;
        }
    }

    public Boolean deleteEmploymentReportTo(String id) {
        try {
            EmploymentReportTo employmentReportTo = getEmploymentReportTo(id);

            if (employmentReportTo != null) {
                employmentReportTo.setReportTo(null);
                employmentReportTo.setSubordinate(null);

                delete("EmploymentReportTo", employmentReportTo);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LogUtil.error(EmploymentReportToDaoImpl.class.getName(), e, "Delete Employment Report To Error!");
            return false;
        }
    }

    public EmploymentReportTo getEmploymentReportTo(String id) {
        try {
            return (EmploymentReportTo) find("EmploymentReportTo", id);
        } catch (Exception e) {
            LogUtil.error(EmploymentReportToDaoImpl.class.getName(), e, "Get Employment Report To Error!");
            return null;
        }
    }
}
