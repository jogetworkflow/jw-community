package org.joget.governance.model;

import java.util.Date;
import org.joget.plugin.base.Plugin;
import org.joget.plugin.property.model.PropertyEditable;

public interface GovHealthCheck extends Plugin, PropertyEditable {
    
    String getCategory();

    String getSortPriority();
    
    String getInfoLink();
    
    GovHealthCheckResult performCheck(Date lastCheck, long intervalInMs, GovHealthCheckResult prevResult);
    
    boolean isConfigurable();
}
