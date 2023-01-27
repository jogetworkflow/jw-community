package org.joget.plugin.base;

import java.util.Map;

/**
 * Interface for Audit Trail Plugin. 
 * 
 */
public interface AuditTrailPlugin {

    /**
     * To execute the extra processing based on Audit Trail Event.
     * An org.joget.apps.app.model.AuditTrail object is passed as "auditTrail" property.
     * 
     * @param props
     * @return is not used for now.
     */
    Object execute(Map props);
}
