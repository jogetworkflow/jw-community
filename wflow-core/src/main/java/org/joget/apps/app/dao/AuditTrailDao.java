package org.joget.apps.app.dao;

import java.util.List;
import org.joget.apps.app.model.AuditTrail;

public interface AuditTrailDao {

    public void addAuditTrail(AuditTrail auditTrail);

    public AuditTrail getAuditTrailByUsername(String username);

    public List<AuditTrail> getAuditTrails(String sort, Boolean desc, Integer start, Integer rows);

    public List<AuditTrail> getAuditTrails(String condition, Object[] param, String sort, Boolean desc, Integer start, Integer rows);

    public Long count(String condition, Object[] params);

    public void deleteAuditTrail(AuditTrail auditTrail);
}
