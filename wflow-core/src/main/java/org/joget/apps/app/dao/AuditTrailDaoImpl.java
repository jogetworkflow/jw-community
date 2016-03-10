package org.joget.apps.app.dao;

import org.joget.commons.spring.model.AbstractSpringDao;
import java.util.List;
import org.joget.apps.app.model.AuditTrail;

public class AuditTrailDaoImpl extends AbstractSpringDao implements AuditTrailDao {

    public static final String ENTITY_NAME = "AuditTrail";

    public void addAuditTrail(AuditTrail auditTrail) {
        super.save(ENTITY_NAME, auditTrail);
    }

    public AuditTrail getAuditTrailByUsername(String username) {
        AuditTrail auditTrail = new AuditTrail();
        auditTrail.setUsername(username);
        return (AuditTrail) super.findByExample(ENTITY_NAME, auditTrail);
    }

    public List<AuditTrail> getAuditTrails(String sort, Boolean desc, Integer start, Integer rows) {
        return (List<AuditTrail>) super.find(ENTITY_NAME, "", new Object[]{}, sort, desc, start, rows);
    }

    public List<AuditTrail> getAuditTrails(String condition, Object[] param, String sort, Boolean desc, Integer start, Integer rows) {
        return (List<AuditTrail>) super.find(ENTITY_NAME, condition, param, sort, desc, start, rows);
    }

    public Long count(String condition, Object[] params) {
        return super.count(ENTITY_NAME, condition, params);
    }

    public void deleteAuditTrail(AuditTrail auditTrail) {
        super.delete(ENTITY_NAME, auditTrail);
    }
}
