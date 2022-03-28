package org.joget.governance.model;

import org.joget.plugin.base.Plugin;
import org.joget.plugin.property.model.PropertyEditable;

public interface GovAppHealthCheck extends Plugin, PropertyEditable {
    GovHealthCheckResult performAppCheck(String appId, String version);
}
