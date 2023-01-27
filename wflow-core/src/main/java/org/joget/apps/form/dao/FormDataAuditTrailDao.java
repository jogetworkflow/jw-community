package org.joget.apps.form.dao;

import java.util.List;
import org.joget.apps.form.model.FormDataAuditTrail;

public interface FormDataAuditTrailDao {
    public void addAuditTrail(FormDataAuditTrail auditTrail);

    public List<FormDataAuditTrail> getAuditTrails(String sort, Boolean desc, Integer start, Integer rows);

    public List<FormDataAuditTrail> getAuditTrails(String condition, Object[] param, String sort, Boolean desc, Integer start, Integer rows);

    public Long count(String condition, Object[] params);

    public void deleteAuditTrail(FormDataAuditTrail auditTrail);
}
