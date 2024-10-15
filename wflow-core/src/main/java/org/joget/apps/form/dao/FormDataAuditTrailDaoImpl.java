package org.joget.apps.form.dao;

import java.util.List;
import org.joget.apps.form.model.FormDataAuditTrail;
import org.joget.commons.spring.model.AbstractSpringDao;

public class FormDataAuditTrailDaoImpl extends AbstractSpringDao implements FormDataAuditTrailDao {
    
    public static final String ENTITY_NAME = "FormDataAuditTrail";

    public void addAuditTrail(FormDataAuditTrail auditTrail) {
        super.save(ENTITY_NAME, auditTrail);
    }

    public List<FormDataAuditTrail> getAuditTrails(String sort, Boolean desc, Integer start, Integer rows) {
        return (List<FormDataAuditTrail>) super.find(ENTITY_NAME, "", new Object[]{}, sort, desc, start, rows);
    }

    public List<FormDataAuditTrail> getAuditTrails(String condition, Object[] param, String sort, Boolean desc, Integer start, Integer rows) {
        return (List<FormDataAuditTrail>) super.find(ENTITY_NAME, condition, param, sort, desc, start, rows);
    }

    public Long count(String condition, Object[] params) {
        return super.count(ENTITY_NAME, condition, params);
    }

    public void deleteAuditTrail(FormDataAuditTrail auditTrail) {
        super.delete(ENTITY_NAME, auditTrail);
    }
}
